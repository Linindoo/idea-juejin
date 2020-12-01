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
