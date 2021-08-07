package cn.olange.pins.action;

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

public class LuckDrawAction extends AnAction {
    public LuckDrawAction() {
        super(IconLoader.getIcon("/icons/luckdraw.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Config config = JuejinPersistentConfig.getInstance().getState();
        if (!config.isLogined()) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "未登录", "请先登录", NotificationType.WARNING), anActionEvent.getProject());
            return;
        }
        if (StringUtils.isEmpty(config.getCookieValue())) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "登录失败", "请先配置用户登录信息", NotificationType.WARNING), anActionEvent.getProject());
            return;
        }
        PinsService instance = PinsService.getInstance(anActionEvent.getProject());
        JsonObject result = instance.luckDraw(config.getCookieValue());
        if (result == null || result.isJsonNull()) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖失败", "请重试", NotificationType.WARNING), anActionEvent.getProject());
        } else if (!result.get("data").isJsonNull()) {
            String lottery_name = result.get("data").getAsJsonObject().get("lottery_name").getAsString();
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖结果", "抽奖的结果是： " + lottery_name, NotificationType.INFORMATION), anActionEvent.getProject());
        } else {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖失败", result.get("err_msg").getAsString(), NotificationType.WARNING), anActionEvent.getProject());
        }
    }
}
