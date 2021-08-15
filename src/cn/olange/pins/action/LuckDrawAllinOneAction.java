package cn.olange.pins.action;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.view.LuckDrawAllinOneDialog;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class LuckDrawAllinOneAction extends AnAction {
    public LuckDrawAllinOneAction() {
        super(IconLoader.getIcon("/icons/all_in_one.png"));
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
        LuckDrawAllinOneDialog luckDrawAllinOneDialog = new LuckDrawAllinOneDialog(anActionEvent.getProject());
        luckDrawAllinOneDialog.show();
    }
}
