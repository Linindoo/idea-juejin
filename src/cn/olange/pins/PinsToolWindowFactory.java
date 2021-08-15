package cn.olange.pins;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.view.PinsToolWindowPanel;
import com.google.gson.JsonObject;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;


public class PinsToolWindowFactory implements ToolWindowFactory {
	public static String ID = "沸点";
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(new PinsToolWindowPanel(project, "recommend"), "", false));
		PinsService pinsService = PinsService.getInstance(project);
		Config config = JuejinPersistentConfig.getInstance().getState();
		if (config.isLogined()) {
			ApplicationManager.getApplication().invokeLater(() -> {
				JsonObject userInfo = pinsService.getUserInfo(config.getCookieValue());
				if (userInfo != null && !userInfo.get("data").isJsonNull()) {
					JsonObject data = userInfo.get("data").getAsJsonObject();
					String userName = data.get("user_name").getAsString();
					config.setNickname(userName);
					config.setUserId(data.get("user_id").getAsString());
					config.setLogined(true);
					updateTitle(project, userName);
				} else {
					config.setNickname("");
					config.setLogined(false);
					updateTitle(project, "未登录");
				}

				JsonObject result = pinsService.dailyHasSign(config.getCookieValue());
				if (result != null && !result.isJsonNull() && !result.get("data").isJsonNull() && result.get("data").getAsBoolean()) {
					config.setDailySign(true);
					config.setSignDate(new Date());
				} else {
					if (config.isEnableAutoSign()) {
						JsonObject signResult = pinsService.toSign("", config.getCookieValue());
						if (signResult == null || signResult.isJsonNull()) {
							Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "自动签到失败", "请尝试手动签到", NotificationType.WARNING), project);
						} else if (!signResult.get("data").isJsonNull()) {
							JsonObject data = signResult.get("data").getAsJsonObject();
							int incr_point = data.get("incr_point").getAsInt();
							int sum_point = data.get("sum_point").getAsInt();
							Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "自动签到成功", "矿石数： +" + incr_point + ",现总数为：" + sum_point, NotificationType.INFORMATION), project);
						} else {
							Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "自动签到失败", signResult.get("err_msg").getAsString(), NotificationType.WARNING), project);
						}
					}
					config.setSignDate(null);
					config.setDailySign(false);
				}
			});
		} else {
			updateTitle(project, "未登录");
		}
	}

	public static void updateTitle(@NotNull Project project, String userName) {
		ToolWindow toolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
		if (StringUtils.isNotBlank(userName)) {
			toolWindows.setTitle(userName);
		} else {
			toolWindows.setTitle("");
		}
	}

	public static DataContext getDataContext(@NotNull Project project) {
		ToolWindow toolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
		DataContext dataContext = DataManager.getInstance().getDataContext(toolWindows.getContentManager().getContent(0).getComponent());
		return dataContext;
	}
}
