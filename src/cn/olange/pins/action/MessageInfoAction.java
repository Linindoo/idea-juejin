package cn.olange.pins.action;

import cn.olange.pins.model.Config;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.view.MessageInfoPanel;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MessageInfoAction extends AnAction {
    private long lastTime = 0;
    private Disposable disposable;

    public MessageInfoAction() {
        super(AllIcons.General.ShowInfos);
        disposable = Disposer.newDisposable();
    }

    @Override
    public void update(AnActionEvent e) {
        Config config = JuejinPersistentConfig.getInstance().getState();
        if (config.isLogined() && System.currentTimeMillis() - lastTime > 1000 * 10) {
            refreshMessageInfo(e, config);
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MessageInfoPanel messageInfoPanel = new MessageInfoPanel(e.getProject());
        PopupUtil.showBalloonForComponent(messageInfoPanel, "消息", MessageType.INFO, true, disposable);
        Balloon balloon = JBPopupFactory.getInstance().createBalloonBuilder(messageInfoPanel)
                .setFillColor(messageInfoPanel.getBackground())
                .createBalloon();
        balloon.show(new RelativePoint(e.getInputEvent().getComponent(), new Point(20, 30)), Balloon.Position.below);
    }

    private void refreshMessageInfo(AnActionEvent e, Config config) {
        PinsService pinsService = PinsService.getInstance(e.getProject());
        JsonObject messageInfo = pinsService.getMessageInfo(config.getCookieValue());
        if (messageInfo != null && !messageInfo.get("data").isJsonNull()) {
            UIUtil.invokeLaterIfNeeded(() -> {
                JsonObject data = messageInfo.get("data").getAsJsonObject();
                int total = data.get("total").getAsInt();
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
