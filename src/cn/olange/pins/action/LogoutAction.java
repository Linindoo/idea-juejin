package cn.olange.pins.action;

import cn.olange.pins.PinsToolWindowFactory;
import cn.olange.pins.model.Config;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class LogoutAction extends AnAction {
    public LogoutAction() {
        super(IconLoader.getIcon("/icons/logout.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        System.out.println("logout");
        Config config = JuejinPersistentConfig.getInstance().getConfig();
        if (!config.isLogined()) {
            return;
        }
        PinsToolWindowFactory.updateTitle(anActionEvent.getProject(), "");
        config.setLogined(false);
        JuejinPersistentConfig.getInstance().setInitConfig(config);
    }
}
