package cn.olange.pins.view;

import cn.olange.pins.utils.DateUtils;
import cn.olange.pins.utils.ImageUtils;
import com.google.gson.JsonObject;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ImageUtil;
import io.netty.util.internal.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class CommentReply extends JPanel{
	private JLabel replytarget;
	private JPanel main;
	private JLabel createtime;
	private JLabel like;
	private JLabel avatar;
	private JLabel nickname;
	private JLabel job;
	private JLabel company;
	private JTextPane replyText;
	private JsonObject reply;

	public CommentReply(JsonObject reply) {
		this.reply = reply;
		this.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 0, 10), -1, -1));
		this.add(main, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		try {
			this.initData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initData() throws IOException {
		if (this.reply == null) {
			return;
		}
		this.replyText.setText(reply.get("content").getAsString());
		JsonObject userInfo = reply.getAsJsonObject("userInfo");
		String avatarLarge = userInfo.get("avatarLarge").getAsString();
		if (!StringUtil.isNullOrEmpty(avatarLarge)) {
			ImageIcon icon = new ImageIcon(new URL(avatarLarge));
			Image image = ImageUtil.scaleImage(icon.getImage(), 40, 40);
			BufferedImage result = ImageUtils.makeRoundedCorner(ImageUtil.toBufferedImage(image), 40);
			icon.setImage(result);
			this.avatar.setIcon(icon);
		}
		JsonObject respUserInfo = reply.getAsJsonObject("respUserInfo");
		this.replytarget.setText(respUserInfo.get("username").getAsString());
		this.nickname.setText(userInfo.get("username").getAsString());
		this.job.setText(userInfo.get("jobTitle").getAsString());
		this.company.setText(userInfo.get("company").getAsString());
		this.like.setText(String.valueOf(reply.get("likesCount").getAsString()));
		this.createtime.setText(DateUtils.getDistanceDate(reply.get("createdAt").getAsString()));
	}
}
