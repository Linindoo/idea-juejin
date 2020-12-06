package cn.olange.pins.action;

import cn.olange.pins.model.Config;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.view.MessageInfoPanel;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MessageInfoAction extends AnAction {
    private long lastTime = 0;
    private JsonObject messageData;

    public MessageInfoAction() {
        super(AllIcons.General.ShowInfos);
    }

    @Override
    public void update(AnActionEvent e) {
        Config config = JuejinPersistentConfig.getInstance().getState();
        if (config.isLogined() && System.currentTimeMillis() - lastTime > config.getMessageRefreshInterval()) {
            refreshMessageInfo(e, config);
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Disposable disposable = Disposer.newDisposable();
        MessageInfoPanel messageInfoPanel = new MessageInfoPanel(e.getProject(), messageData, disposable);
        Balloon balloon = JBPopupFactory.getInstance().createBalloonBuilder(messageInfoPanel)
                .setFillColor(messageInfoPanel.getBackground())
                .setAnimationCycle(0)
                .setDisposable(disposable)
                .setFadeoutTime(0)
                .setRequestFocus(true)
                .createBalloon();
        balloon.show(new RelativePoint(e.getInputEvent().getComponent(), new Point(15, 30)), Balloon.Position.below);
    }

    private void refreshMessageInfo(AnActionEvent e, Config config) {
        PinsService pinsService = PinsService.getInstance(e.getProject());
        JsonObject messageInfo = pinsService.getMessageInfo(config.getCookieValue());
        if (messageInfo != null && !messageInfo.get("data").isJsonNull()) {
            UIUtil.invokeLaterIfNeeded(() -> {
                messageData= messageInfo.get("data").getAsJsonObject();
                int total = messageData.get("total").getAsInt();
                if (total > 0) {
                    e.getPresentation().setIcon(AllIcons.General.Information);
                    e.getPresentation().setMultipleChoice(true);
                    e.getPresentation().setText(String.format("你有%s条未读消息",total));
                } else {
                    e.getPresentation().setIcon(AllIcons.General.ShowInfos);
                    e.getPresentation().setText("暂无消息");
                }
            });
        }
    }
}
