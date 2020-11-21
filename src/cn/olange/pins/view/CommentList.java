package cn.olange.pins.view;

import cn.olange.pins.model.CommentNode;
import cn.olange.pins.model.CommentTreeCellRender;
import cn.olange.pins.model.CommentTreeNode;
import cn.olange.pins.model.NeedMore;
import cn.olange.pins.service.PinsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.find.actions.ShowUsagesAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.packageDependencies.ui.TreeModel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.jdesktop.swingx.JXTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommentList extends JPanel implements Disposable {
	private final Project project;
	private final String pinID;
	private JPanel main;
	private JsonArray commentData = new JsonArray();
	private int pageSize = 20;
	private int pageNum = 1;
	private String cursor = "0";
	private LoadingDecorator loadingDecorator;
	private Alarm commentAlarm;
	private JTree jxTree;
	private volatile ProgressIndicatorBase progressIndicatorBase;

	public CommentList(Project project, String pinID) {
		super();
		this.project = project;
		this.pinID = pinID;
		loadingDecorator = new LoadingDecorator(main, this, 0);
		this.commentAlarm = new Alarm();
		this.initData();
	}

	private void initData() {
		this.setLayout(new BorderLayout());
		this.add(loadingDecorator.getComponent(), BorderLayout.CENTER);
		jxTree = new SimpleTree();
		this.main.setLayout(new BorderLayout());
		this.jxTree.setCellRenderer(new CommentTreeCellRender());
		jxTree.setOpaque(false);
		jxTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jxTree.setRootVisible(false);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
		jxTree.setModel(new DefaultTreeModel(root));
//		jxTree.addTreeWillExpandListener(new TreeWillListener(tree, toolWindow, project));
		JBScrollPane scrollPane = new JBScrollPane(this.jxTree) {
			public Dimension getMinimumSize() {
				Dimension size = super.getMinimumSize();
				size.height = jxTree.getPreferredScrollableViewportSize().height;
				size.width = jxTree.getPreferredScrollableViewportSize().width;
				return size;
			}
		};
		this.main.add(scrollPane, BorderLayout.CENTER);
		jxTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = jxTree.getSelectionPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
				if (node.getUserObject() instanceof NeedMore) {
					System.out.println("more");
					TreePath parentPath = selectionPath.getParentPath();
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
					if (parentNode.getUserObject() instanceof CommentTreeNode) {
						CommentTreeNode treeNode = (CommentTreeNode) parentNode.getUserObject();
						getCommentReply(treeNode, parentNode);
					} else {
						loadMoreComment(parentNode);
					}
				}
			}
		});
		this.scheduleComment();
	}

	private void loadMoreComment(DefaultMutableTreeNode parentNode) {

	}

	private void getCommentReply(CommentTreeNode treeNode, DefaultMutableTreeNode parentNode) {

	}

	public void scheduleComment() {
		if (this.commentAlarm != null && !this.commentAlarm.isDisposed()) {
			this.commentAlarm.cancelAllRequests();
			this.commentAlarm.addRequest(this::findComments, 100);
		}
	}

	private void findComments() {
		final ModalityState state = ModalityState.current();
		if (this.progressIndicatorBase != null && !this.progressIndicatorBase.isCanceled()) {
			this.progressIndicatorBase.cancel();
		}
		this.commentAlarm.cancelAllRequests();
		final ProgressIndicatorBase progressIndicatorWhenSearchStarted = new ProgressIndicatorBase() {
			public void stop() {
				super.stop();
				loadingDecorator.stopLoading();
			}
		};
		this.progressIndicatorBase = progressIndicatorWhenSearchStarted;
		final AtomicInteger resultsCount = new AtomicInteger();
		loadingDecorator.startLoading(false);
		ProgressIndicatorUtils.scheduleWithWriteActionPriority(this.progressIndicatorBase, new ReadTask() {
			public ReadTask.Continuation performInReadAction(@NotNull ProgressIndicator indicator) {
				if (this.isCancelled()) {
					loadingDecorator.stopLoading();
				} else {
					ApplicationManager.getApplication().invokeLater(() -> {
						if (this.isCancelled()) {
							loadingDecorator.stopLoading();
						} else {
							PinsService instance = PinsService.getInstance(project);
							instance.getComments(cursor, pageSize, pinID, result -> {
								if (result.isSuccess()) {
									JsonObject commentResult = (JsonObject) result.getResult();
									JsonArray comments = commentResult.getAsJsonArray("data");
									commentData.addAll(comments);
									UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
										DefaultTreeModel treeMode = (DefaultTreeModel) jxTree.getModel();
										DefaultMutableTreeNode root = (DefaultMutableTreeNode) jxTree.getModel().getRoot();
										root.removeAllChildren();
										for (int i = 0; i < commentData.size(); i++) {
											JsonObject comment = commentData.get(i).getAsJsonObject();
											DefaultMutableTreeNode commentNode = new DefaultMutableTreeNode(new CommentNode(1, comment));
											root.add(commentNode);
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

										}
										if (commentResult.get("has_more").getAsBoolean()) {
											root.add(new DefaultMutableTreeNode(new NeedMore()));
										}
										treeMode.reload();
										jxTree.updateUI();
										jxTree.expandRow(0);
										loadingDecorator.stopLoading();
									});
								}
							});
						}
					}, state);
					boolean continueSearch = resultsCount.incrementAndGet() < ShowUsagesAction.getUsagesPageSize();
					if (!continueSearch) {
						loadingDecorator.stopLoading();
					}
				}
				return new Continuation(() -> {
					if (!this.isCancelled() && resultsCount.get() == 0) {
//						StatusText emptyText = pinTable.getEmptyText();
//						emptyText.clear();
					}
					loadingDecorator.stopLoading();
				}, state);
			}

			boolean isCancelled() {
				return progressIndicatorWhenSearchStarted != CommentList.this.progressIndicatorBase || progressIndicatorWhenSearchStarted.isCanceled();
			}

			public void onCanceled(@NotNull ProgressIndicator indicator) {
				if (CommentList.this.isShowing() && progressIndicatorWhenSearchStarted == CommentList.this.progressIndicatorBase) {
					CommentList.this.scheduleComment();
				}
			}
		});
	}

//	private void refreshData() {
//		for (int i = 0; i < this.commentData.size(); i++) {
//			JsonObject comment = (JsonObject) this.commentData.get(i);
//			CommentItem commentInfo = new CommentItem(comment, project);
//			this.commentbox.add(commentInfo);
//		}
//		this.commentbox.updateUI();
//	}
//
//
//	private void validatePage(JsonObject data) {
//		this.cursor = data.get("cursor").getAsString();
//		boolean has_more = data.get("has_more").getAsBoolean();
//		UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
//			if (has_more) {
//				this.loadmore.setVisible(false);
//			} else {
//				this.loadmore.setVisible(true);
//			}
//		});
//
//	}

	@Override
	public void dispose() {
	}
}
