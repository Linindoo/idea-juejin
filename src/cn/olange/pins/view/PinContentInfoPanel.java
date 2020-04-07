package cn.olange.pins.view;

import cn.olange.pins.utils.DateUtils;
import cn.olange.pins.utils.ImageUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ImageUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class PinContentInfoPanel extends JPanel {
	private JTextPane content;
	private JLabel avatar;
	private JLabel job;
	private JLabel company;
	private JLabel nickname;
	private JLabel prise;
	private JLabel comment;
	public JPanel pinInfo;
	private JLabel createTime;
	private JPanel imageContent;
	private JLabel expand;
	private JLabel browswebtn;
	private JsonObject pinItem;
	private Project project;
	private boolean showCommentDialog;
	private String contentStr;

	public PinContentInfoPanel(JsonObject pinItem, Project project, boolean showCommentDialog) {
		this.showCommentDialog = showCommentDialog;
		this.pinItem = pinItem;
		this.project = project;
		this.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 0, 10), -1, -1));
		this.add(pinInfo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		try {
			this.initData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PinContentInfoPanel(JsonObject pinItem, Project project) {
		this(pinItem, project,false);
	}

	private void initData() throws IOException {
		JsonObject node = this.pinItem.getAsJsonObject("node");
		JsonArray actors = node.getAsJsonArray("actors");
		JsonObject actor = (JsonObject) actors.get(0);
		String avatarUrl = actor.get("avatarLarge").getAsString();
		if (StringUtils.isNotEmpty(avatarUrl)) {
			URL avatarLarge = new URL(avatarUrl);
			ImageIcon icon = new ImageIcon(avatarLarge);
			Image image = ImageUtils.scaleImage(icon.getImage(), 40, 40);
			BufferedImage result = ImageUtils.makeRoundedCorner(ImageUtil.toBufferedImage(image), 40);
			icon.setImage(result);
			this.avatar.setIcon(icon);
		}
		this.nickname.setText(actor.get("username").getAsString());
		this.job.setText(actor.get("jobTitle").getAsString());
		this.company.setText(actor.get("company").getAsString());

		JsonArray targets = node.getAsJsonArray("targets");
		JsonObject target = (JsonObject) targets.get(0);
		this.contentStr = target.get("content").getAsString();
		this.initExpandContent();
		this.prise.setText(target.get("likeCount").getAsString());
		Integer commentCount = target.get("commentCount").getAsInt();
		this.comment.setText(String.valueOf(commentCount));
		String createdAt = target.get("createdAt").getAsString();
		this.createTime.setText(DateUtils.getDistanceDate(createdAt));
		this.browswebtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				BrowserLauncher.getInstance().browse("https://juejin.im/pin/" + node.get("id").getAsString(), WebBrowserManager.getInstance().getFirstActiveBrowser());
			}
		});
		if (commentCount > 0 && this.showCommentDialog) {
			this.comment.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					PinDetailDialog pinDetailDialog = new PinDetailDialog(pinItem, project);
					pinDetailDialog.show();
				}
			});
		}

		JsonArray pictures = target.getAsJsonArray("pictures");
		if (pictures != null) {
			int row = pictures.size() % 3 == 0 ? pictures.size() / 3 : pictures.size() / 3 + 1;
			this.imageContent.setLayout(new GridLayoutManager(row+1, 3, new Insets(10, 10, 0, 10), -1, 10));
			for (int i = 0; i < pictures.size(); i++) {
				String imageUrl = pictures.get(i).getAsString();
				String thumbnailUrl = ImageUtils.getThumbnailUrl(imageUrl);
				ImageIcon picCon = new ImageIcon(new URL(thumbnailUrl));
				picCon.setImage(ImageUtils.scaleImage(picCon.getImage(), 115, 79));
				JLabel jLabel = new JLabel();
				jLabel.setPreferredSize(new Dimension(115,79));
				jLabel.setIcon(picCon);
				int finalI = i;
				jLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						ImagePreViewDialog imagePreViewDialog = new ImagePreViewDialog(project, pictures, finalI);
						imagePreViewDialog.show();
					}
				});
				int grow = i / 3;
				int gc = i % 3;
				this.imageContent.add(jLabel, new GridConstraints(grow , gc, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
			}
			JLabel label = new JLabel();
			label.setPreferredSize(new Dimension(115,79));
			if (pictures.size() == 1) {
				this.imageContent.add(label, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
				this.imageContent.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
			} else if (pictures.size() == 2) {
				this.imageContent.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
			}
		}
	}

	private void initExpandContent() {
		if (this.contentStr == null || this.contentStr.length() < 100) {
			this.expand.setVisible(false);
			this.content.setText(this.contentStr);
		} else {
			this.expand.setVisible(true);
			this.content.setText(this.contentStr.substring(0, 100));
		}
		this.expand.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ("展开".equalsIgnoreCase(expand.getText())) {
					expandContent(true);
				} else {
					expandContent(false);
				}
			}
		});
	}

	private void expandContent(boolean expand) {
		if (expand) {
			this.expand.setText("收起");
			this.content.setText(this.contentStr);
		} else {
			this.expand.setText("展开");
			this.content.setText(this.contentStr.substring(0, 100));
		}
	}
}
