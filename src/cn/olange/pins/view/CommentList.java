package cn.olange.pins.view;

import cn.olange.pins.model.*;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.find.actions.ShowUsagesAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CommentList extends JPanel implements Disposable {
	private final Project project;
	private final String pinID;
	private JsonArray commentData = new JsonArray();
	private int pageSize = 20;
	private int pageNum = 1;
	private String cursor = "0";
	private Alarm commentAlarm;
	private SimpleTree jxTree;
	private DefaultMutableTreeNode root;
	private Map<String, String> commentReplyCursorMap = new HashMap<>();
	private JsonObject replyCommentInfo;
	private JLabel replyUserLabel;
	private JPanel replyPanel;

	public CommentList(Project project, String pinID) {
		super();
		this.project = project;
		this.pinID = pinID;
		this.commentAlarm = new Alarm();
		this.initData();
	}

	private void initData() {
		this.setLayout(new BorderLayout());
		JTextField textField = new JTextField();
		JPanel bottomPanel = new JPanel(new BorderLayout());
		replyPanel = new JPanel();
		replyPanel.setLayout(new BorderLayout());
		replyUserLabel = new JLabel();
		replyPanel.add(replyUserLabel, BorderLayout.WEST);
		replyPanel.add(new ReplyUserActionButton(new ReplyUserActionButton.ClearAction() {
			@Override
			public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
				replyPanel.setVisible(false);
				replyCommentInfo = null;
			}
		}), BorderLayout.CENTER);
		bottomPanel.add(replyPanel, BorderLayout.WEST);
		replyPanel.setVisible(false);
		bottomPanel.add(textField, BorderLayout.CENTER);
		JButton commentBtn = new JButton("评论");
		Config config = JuejinPersistentConfig.getInstance().getState();
		commentBtn.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = textField.getText();
				if (StringUtils.isNotEmpty(text) && config.isLogined()) {
					ApplicationManager.getApplication().invokeLater(() -> {
						PinsService pinsService = PinsService.getInstance(project);
						if (replyCommentInfo == null || replyCommentInfo.isJsonNull()) {
							pinsService.comment(pinID, text, config.getCookieValue());
						} else {
							JsonElement reply_info = replyCommentInfo.get("reply_info");
							String commentID, replyID = "", replyUserID = "";
							if (reply_info == null || reply_info.isJsonNull()) {
								commentID = replyCommentInfo.get("comment_id").getAsString();
							} else {
								commentID = reply_info.getAsJsonObject().get("reply_comment_id").getAsString();
								replyID = reply_info.getAsJsonObject().get("reply_id").getAsString();
								replyUserID = reply_info.getAsJsonObject().get("reply_user_iD").getAsString();
							}
							pinsService.replyComment(pinID, commentID, replyID, replyUserID, text, config.getCookieValue());
						}
						replyCommentInfo = null;
						replyPanel.setVisible(false);
						cursor = "0";
						scheduleComment(null);
					});
				}
			}
		});
		if (!config.isLogined()) {
			commentBtn.setEnabled(false);
			commentBtn.setToolTipText("未登录");
		}
		bottomPanel.add(commentBtn, BorderLayout.EAST);
		this.add(bottomPanel, BorderLayout.NORTH);
		jxTree = new SimpleTree();
		JBScrollPane scrollPane = new JBScrollPane(this.jxTree);
		scrollPane.setBorder(JBUI.Borders.empty());
		this.add(scrollPane, BorderLayout.CENTER);
		jxTree.setOpaque(false);
		jxTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jxTree.setRootVisible(false);
		jxTree.getEmptyText().clear();
		root = new DefaultMutableTreeNode("");
		jxTree.setModel(new DefaultTreeModel(root));
		this.jxTree.setCellRenderer(new CommentTreeCellRender());
		jxTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = jxTree.getSelectionPath();
				if (selectionPath == null) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
				if (node.getUserObject() instanceof NeedMore) {
					TreePath parentPath = selectionPath.getParentPath();
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
					parentNode.remove(node);
					if (parentNode.getUserObject() instanceof CommentNode) {
						CommentNode treeNode = (CommentNode) parentNode.getUserObject();
						getCommentReply(treeNode, parentNode);
					} else {
						loadMoreComment(parentNode);
					}
				} else if (node.getUserObject() instanceof CommentNode) {
					CommentNode commentTreeNode = (CommentNode) node.getUserObject();
					replyUser(commentTreeNode.getItem());
				}
			}
		});
		scheduleComment(root);
	}

	private void replyUser(JsonObject commentInfo) {
		this.replyCommentInfo = commentInfo;
		this.replyUserLabel.setText("回复 " + replyCommentInfo.get("user_info").getAsJsonObject().get("user_name").getAsString());
		this.replyPanel.setVisible(true);
	}

	private void loadMoreComment(DefaultMutableTreeNode parentNode) {
		ApplicationManager.getApplication().invokeLater(()->{
			this.scheduleComment(parentNode);
		});
	}

	private void getCommentReply(CommentNode treeNode, DefaultMutableTreeNode parentNode) {
		JsonObject item = treeNode.getItem();
		String reply_comment_id = item.get("comment_id").getAsString();
		String replyCursor = commentReplyCursorMap.get(reply_comment_id);
		if (this.commentAlarm != null && !this.commentAlarm.isDisposed()) {
			this.commentAlarm.cancelAllRequests();
			if (StringUtils.isEmpty(replyCursor)) {
				this.commentAlarm.addRequest(() -> getCommentReply(reply_comment_id, "0", parentNode), 100);
			}else {
				this.commentAlarm.addRequest(() -> getCommentReply(reply_comment_id, replyCursor, parentNode), 100);
			}
		}

	}

	public void scheduleComment(DefaultMutableTreeNode parentNode) {
		if (this.commentAlarm != null && !this.commentAlarm.isDisposed()) {
			this.commentAlarm.cancelAllRequests();
			if (parentNode == null) {
				jxTree.setModel(new DefaultTreeModel(root));
				this.commentAlarm.addRequest(() -> findComments(new DefaultMutableTreeNode("")), 100);
			} else {
				this.commentAlarm.addRequest(()-> findComments(parentNode), 100);
			}
		}
	}

	private void findComments(DefaultMutableTreeNode parentNode) {
		final ModalityState state = ModalityState.current();
		this.commentAlarm.cancelAllRequests();
		ApplicationManager.getApplication().invokeLater(() -> {
			jxTree.setPaintBusy(true);
			PinsService instance = PinsService.getInstance(project);
			instance.getComments(cursor, pageSize, pinID, result -> {
				if (result.isSuccess()) {
					SwingUtilities.invokeLater(() -> {
						JsonObject commentResult = (JsonObject) result.getResult();
						cursor = commentResult.get("cursor").getAsString();
						JsonArray comments = commentResult.getAsJsonArray("data");
						DefaultTreeModel treeMode = (DefaultTreeModel) jxTree.getModel();
						for (int i = 0; i < comments.size(); i++) {
							JsonObject comment = comments.get(i).getAsJsonObject();
							DefaultMutableTreeNode commentNode = new DefaultMutableTreeNode(new CommentNode(1, comment));
							JsonArray reply_infos = comment.getAsJsonArray("reply_infos");
							if (reply_infos != null) {
								int replyCout = comment.get("comment_info").getAsJsonObject().get("reply_count").getAsInt();
								for (JsonElement reply_info : reply_infos) {
									commentNode.add(new DefaultMutableTreeNode(new CommentNode(2, reply_info.getAsJsonObject())));
								}
								if (replyCout > reply_infos.size()) {
									commentNode.add(new DefaultMutableTreeNode(new NeedMore()));
								}
							}
							parentNode.add(commentNode);
						}
						if (commentResult.get("has_more").getAsBoolean()) {
							parentNode.add(new DefaultMutableTreeNode(new NeedMore()));
						}
						treeMode.reload(parentNode);
						jxTree.setPaintBusy(false);
					});
				} else {
					jxTree.getEmptyText().setText("数据获取失败");
				}
			});
		}, state);
	}

	private void getCommentReply(String commentID, String replyCursor, DefaultMutableTreeNode parentNode) {
		final ModalityState state = ModalityState.current();
		this.commentAlarm.cancelAllRequests();
		ApplicationManager.getApplication().invokeLater(() -> {
			jxTree.setPaintBusy(true);
			PinsService instance = PinsService.getInstance(project);
			instance.getCommentReply(replyCursor, pageSize, commentID, pinID, result -> {
				if (result.isSuccess()) {
					SwingUtilities.invokeLater(() -> {
						JsonObject commentResult = (JsonObject) result.getResult();
						String newReplyCursor = commentResult.get("cursor").getAsString();
						commentReplyCursorMap.put(commentID, newReplyCursor);
						JsonArray commentReply = commentResult.getAsJsonArray("data");
						DefaultTreeModel treeMode = (DefaultTreeModel) jxTree.getModel();
						for (int i = 0; i < commentReply.size(); i++) {
							JsonObject reply = commentReply.get(i).getAsJsonObject();
							DefaultMutableTreeNode commentNode = new DefaultMutableTreeNode(new CommentNode(2, reply));
							parentNode.add(commentNode);
						}
						if (commentResult.get("has_more").getAsBoolean()) {
							parentNode.add(new DefaultMutableTreeNode(new NeedMore()));
						}
						treeMode.nodeStructureChanged(parentNode);
						jxTree.setPaintBusy(false);
					});
				}
			});
		}, state);
	}

	@Override
	public void dispose() {
	}
}
