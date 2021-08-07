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

import java.util.Calendar;
import java.util.Date;

public class DailySignAction extends AnAction {
    public DailySignAction() {
        super(IconLoader.getIcon("/icons/sign.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Config config = JuejinPersistentConfig.getInstance().getState();
        if (config.isDailySign() && config.getSignDate() != null) {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            Calendar signDate = Calendar.getInstance();
            signDate.setTime(config.getSignDate());
            if (now.get(Calendar.DAY_OF_YEAR) - signDate.get(Calendar.DAY_OF_YEAR) < 1) {
                Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "签到提示", "已经签过到了，不需要再签到了", NotificationType.WARNING), anActionEvent.getProject());
                return;
            }
        }
        if (!config.isLogined()) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "未登录", "请先登录", NotificationType.WARNING), anActionEvent.getProject());
            return;
        }
        if (StringUtils.isEmpty(config.getCookieValue())) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "未登录", "请先配置用户登录信息", NotificationType.WARNING), anActionEvent.getProject());
            return;
        }
        PinsService instance = PinsService.getInstance(anActionEvent.getProject());
        JsonObject result = instance.toSign("", config.getCookieValue());
        if (result == null || result.isJsonNull()) {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "签到失败", "请重试", NotificationType.WARNING), anActionEvent.getProject());
        } else if (!result.get("data").isJsonNull()) {
            JsonObject data = result.get("data").getAsJsonObject();
            int incr_point = data.get("incr_point").getAsInt();
            int sum_point = data.get("sum_point").getAsInt();
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "签到成功", "矿石数： +" + incr_point + ",现总数为：" + sum_point, NotificationType.INFORMATION), anActionEvent.getProject());
        } else {
            Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "签到失败", result.get("err_msg").getAsString(), NotificationType.WARNING), anActionEvent.getProject());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }
}
