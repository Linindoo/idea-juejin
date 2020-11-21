package cn.olange.pins.action;

import cn.olange.pins.PinsToolWindowFactory;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;


public class LoginAction extends AnAction {

    public LoginAction() {
        super(IconLoader.getIcon("/icons/login.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        System.out.println("login");
        Config config = JuejinPersistentConfig.getInstance().getConfig();
        if (config.isLogined()) {
            return;
        }
        if (StringUtils.isEmpty(config.getCookieValue())) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "登录失败", "请先配置用户登录信息", NotificationType.WARNING), anActionEvent.getProject());
            return;
        }
        PinsService pinsService = PinsService.getInstance(anActionEvent.getProject());
        JsonObject userInfo = pinsService.getUserInfo(config.getCookieValue());
        if (userInfo == null || userInfo.isJsonNull()) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "登录失败", "请重试", NotificationType.WARNING), anActionEvent.getProject());
        } else if (!userInfo.get("data").isJsonNull()) {
            String userName = userInfo.get("data").getAsJsonObject().get("user_name").getAsString();
            config.setNickname(userName);
            config.setLogined(true);
            PinsToolWindowFactory.updateTitle(anActionEvent.getProject(), userName);
            JuejinPersistentConfig.getInstance().setInitConfig(config);
        } else {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "登录失败", "请确保用户参数配置正确", NotificationType.WARNING), anActionEvent.getProject());
        }
    }
}
