package cn.olange.pins.action;

import cn.olange.pins.setting.SettingConfigurable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

public class ConfigAction extends AnAction {
    public ConfigAction() {
        super( AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), SettingConfigurable.DISPLAY_NAME);
    }
}
