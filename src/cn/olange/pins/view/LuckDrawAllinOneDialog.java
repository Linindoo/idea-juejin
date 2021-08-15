package cn.olange.pins.view;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class LuckDrawAllinOneDialog  extends DialogWrapper {
    private JPanel main;
    private JRadioButton allbtn;
    private JRadioButton otherbtn;
    private JPanel inputPanel;
    private IntegerField times;
    private ButtonGroup radioGroup;
    private Project project;

    public LuckDrawAllinOneDialog(Project project) {
        super(project, false, IdeModalityType.PROJECT);
        this.project = project;
        this.setTitle("梭哈一场不是梦");
        this.init();
        inputPanel.setLayout(new BorderLayout());
        times = new IntegerField("messageInterval", 1, Integer.MAX_VALUE);
        inputPanel.add(times, BorderLayout.WEST);
        radioGroup = new ButtonGroup();
        radioGroup.add(allbtn);
        radioGroup.add(otherbtn);
        otherbtn.setSelected(true);
        allbtn.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (allbtn.isSelected()) {
                    times.setEnabled(false);
                }
            }
        });
        otherbtn.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (otherbtn.isSelected()) {
                    times.setEnabled(true);
                }
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return main;
    }


    @Override
    protected void doOKAction() {
        PinsService instance = PinsService.getInstance(project);
        Config config = JuejinPersistentConfig.getInstance().getState();
        int luckDrawCount = 0;
        if (allbtn.isSelected()) {
            luckDrawCount = Integer.MAX_VALUE;
        } else {
            try {
                times.validateContent();
            } catch (ConfigurationException e) {
                return;
            }
            luckDrawCount = times.getValue();
        }
        int finalLuckDrawCount = luckDrawCount;
        this.getOKAction().setEnabled(false);
        ApplicationManager.getApplication().invokeLater(() -> {
            for (int i = 1; i < finalLuckDrawCount; i++) {
                JsonObject result = instance.luckDraw(config.getCookieValue());
                if (result == null || result.isJsonNull()) {
                    Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖失败", "请重试", NotificationType.WARNING), project);
                    return;
                } else if (!result.get("data").isJsonNull()) {
                    String lottery_name = result.get("data").getAsJsonObject().get("lottery_name").getAsString();
                    Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖结果", "抽奖的结果是： " + lottery_name, NotificationType.INFORMATION), project);
                } else {
                    Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, "抽奖失败", result.get("err_msg").getAsString(), NotificationType.WARNING), project);
                    return;
                }
            }
            UIUtil.invokeLaterIfNeeded(()->{
                LuckDrawAllinOneDialog.super.doOKAction();
            });
        });
    }
}
