package cn.olange.pins.view;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.ImageLoadingWorker;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.DateUtils;
import cn.olange.pins.utils.ImageUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public class PinContentInfoPanel extends JPanel implements Disposable {
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
	private JLabel topic;
	private JsonObject pinItem;
	private Project project;
	private String contentStr;
	private ImageLoadingWorker imageLoadingWorker;

	public PinContentInfoPanel(JsonObject pinItem, Project project) {
		this.pinItem = pinItem;
		this.project = project;
		this.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 0, 10), -1, -1));
		this.add(pinInfo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		this.initData();
	}

	private void initData() {
		JsonObject actor  = this.pinItem.get("author_user_info").getAsJsonObject();
		String avatarUrl = actor.get("avatar_large").getAsString();
		if (StringUtils.isNotEmpty(avatarUrl)) {
			ApplicationManager.getApplication().executeOnPooledThread(() -> {
				try {
					URL avatarLarge = new URL(avatarUrl);
					ImageIcon icon = new ImageIcon(avatarLarge);
					Image image = ImageUtil.scaleImage(icon.getImage(), 40, 40);
					BufferedImage result = ImageUtils.makeRoundedCorner(ImageUtil.toBufferedImage(image), 40);
					icon.setImage(result);
					UIUtil.invokeLaterIfNeeded(()->{
						avatar.setIcon(icon);
					});
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			});
		}
		Config config = JuejinPersistentConfig.getInstance().getState();
		this.nickname.setText(actor.get("user_name").getAsString());
		this.job.setText(actor.get("job_title").getAsString());
		this.company.setText(actor.get("company").getAsString());

		JsonObject target = this.pinItem.getAsJsonObject("msg_Info");

		this.contentStr = target.get("content").getAsString();
		this.initExpandContent();
		this.prise.setText(target.get("digg_count").getAsString());
		JsonObject user_interact = this.pinItem.get("user_interact").getAsJsonObject();
		if (user_interact.get("is_digg").getAsBoolean()) {
			this.prise.setIcon(IconLoader.getIcon("/icons/prised.png"));
		}
		content.setForeground(UIUtil.getInactiveTextColor());
		this.expand.setForeground(JBColor.blue);
		this.prise.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!config.isLogined()) {
					return;
				}
				ApplicationManager.getApplication().invokeLater(()->{
					boolean ret = PinsService.getInstance(project).praise(pinItem.get("msg_id").getAsString(), config.getCookieValue());
					if (ret) {
						SwingUtilities.invokeLater(()->{
							if (user_interact.get("is_digg").getAsBoolean()) {
								user_interact.addProperty("is_digg", false);
								prise.setIcon(IconLoader.getIcon("/icons/prise.png"));
								prise.setText(String.valueOf(pinItem.getAsJsonObject("msg_Info").get("digg_count").getAsInt() - 1));
							} else {
								user_interact.addProperty("is_digg", true);
								prise.setIcon(IconLoader.getIcon("/icons/prised.png"));
								prise.setText(String.valueOf(pinItem.getAsJsonObject("msg_Info").get("digg_count").getAsInt() + 1));
							}
						});
					}
				});

			}
		});
		int commentCount = target.get("comment_count").getAsInt();
		this.comment.setText(String.valueOf(commentCount));
		String createdAt = target.get("ctime").getAsString();
		this.createTime.setText(DateUtils.getDistanceDate(createdAt));
		this.browswebtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				BrowserLauncher.getInstance().browse("https://juejin.im/pin/" + PinContentInfoPanel.this.pinItem.get("msg_id").getAsString(), WebBrowserManager.getInstance().getFirstActiveBrowser());
			}
		});

		JsonArray pictures = target.getAsJsonArray("pic_list");
		if (pictures != null && pictures.size() > 0) {
			JTextArea log = new JTextArea(4, 4);
			imageLoadingWorker = new ImageLoadingWorker(project, log, imageContent, pictures);
			imageLoadingWorker.execute();
			imageContent.setVisible(true);
		} else {
			imageContent.setVisible(false);
		}
		JsonElement jsonElement = pinItem.get("topic");
		this.topic.setForeground(JBColor.BLUE);
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			this.topic.setVisible(true);
			this.topic.setText(jsonElement.getAsJsonObject().get("title").getAsString());
		} else {
			this.topic.setVisible(false);
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

	@Override
	public void dispose() {
		if (imageLoadingWorker != null) {
			ApplicationManager.getApplication().invokeLater(()->{
				imageLoadingWorker.cancel(true);
			});
		}
	}
}
