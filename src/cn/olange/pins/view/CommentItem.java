package cn.olange.pins.view;

import cn.olange.pins.model.CommentReplyCellRender;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.utils.DateUtils;
import cn.olange.pins.utils.ImageUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ImageUtil;
import io.netty.util.internal.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class CommentItem extends JPanel{
	private JLabel avatar;
	private JLabel job;
	private JLabel company;
	private JTextPane commentContent;
	private JLabel createtime;
	private JLabel like;
	private JPanel main;
	private JLabel nickname;
	private JList commentReply;
	private JPanel replyPanel;
	private JButton loadmore;
	private JsonObject comment;
	private Project project;
	private JsonArray commentReplys = new JsonArray();
	private int pageNum = 1;
	private int pageSize = 10;
	private String commentID;

	public CommentItem(JsonObject pinItem, Project project) {
		this.project = project;
		this.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 0, 10), -1, -1));
		this.add(main, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		this.comment = pinItem;
		this.commentID = pinItem.get("id").getAsString();
		try {
			this.initData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initData() throws IOException {
		if (this.comment == null) {
			return;
		}
		this.commentContent.setText(comment.get("content").getAsString());
		JsonObject userInfo = comment.getAsJsonObject("userInfo");
		String avatarLarge = userInfo.get("avatarLarge").getAsString();
		if (!StringUtil.isNullOrEmpty(avatarLarge)) {
			ImageIcon icon = new ImageIcon(new URL(avatarLarge));
			Image image = ImageUtils.scaleImage(icon.getImage(), 40, 40);
			BufferedImage result = ImageUtils.makeRoundedCorner(ImageUtil.toBufferedImage(image), 40);
			icon.setImage(result);
			this.avatar.setIcon(icon);
		}
		this.nickname.setText(userInfo.get("username").getAsString());
		this.job.setText(userInfo.get("jobTitle").getAsString());
		this.company.setText(userInfo.get("company").getAsString());
		this.like.setText(String.valueOf(comment.get("likesCount").getAsString()));
		this.createtime.setText(DateUtils.getDistanceDate(comment.get("createdAt").getAsString()));
		commentReply.setCellRenderer(new CommentReplyCellRender());
		PinsService instance = PinsService.getInstance(project);
		if (comment.get("replyCount").getAsInt() > 0) {
			this.replyPanel.setVisible(true);
			instance.getCommentReply(pageNum, pageSize, commentID, result -> {
				if (result.isSuccess()) {
					JsonObject resultData = (JsonObject) result.getResult();
					JsonObject data = resultData.getAsJsonObject("d");
					validatePage(data);
					JsonArray replys = data.getAsJsonArray("comments");
					commentReplys.addAll(replys);
					refreshData();
				} else {
					replyPanel.setVisible(false);
				}
			});
		} else {
			this.replyPanel.setVisible(false);
		}

		this.loadmore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pageNum++;
				instance.getCommentReply(pageNum, pageSize, commentID, result -> {
					if (result.isSuccess()) {
						JsonObject resultData = (JsonObject) result.getResult();
						JsonObject data = resultData.getAsJsonObject("d");
						validatePage(data);
						JsonArray replys = data.getAsJsonArray("comments");
						commentReplys.addAll(replys);
						refreshData();
					}
				});
			}
		});
	}

	private void refreshData() {
		DefaultListModel model = new DefaultListModel() {
			@Override
			public int getSize() {
				return commentReplys.size();
			}

			@Override
			public Object getElementAt(int index) {
				return commentReplys.get(index);
			}

		};
		commentReply.setModel(model);
	}
	private void validatePage(JsonObject data) {
		int count = data.get("count").getAsInt();
		if (this.pageNum * this.pageSize > count) {
			this.loadmore.setVisible(false);
		}
	}
}
