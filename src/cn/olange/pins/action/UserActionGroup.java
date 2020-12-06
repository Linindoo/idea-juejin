package cn.olange.pins.action;

import cn.olange.pins.model.Config;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UserActionGroup extends ActionGroup {

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        List<AnAction> anActionList = Lists.newArrayList();
        Config config = JuejinPersistentConfig.getInstance().getState();
        ActionManager actionManager = ActionManager.getInstance();
        if (config.isLogined()) {
            anActionList.add(actionManager.getAction("juejin.LogoutAction"));
            anActionList.add(actionManager.getAction("juejin.MessageAction"));
        } else {
            anActionList.add(actionManager.getAction("juejin.LoginAction"));
        }
        AnAction[] anActions = new AnAction[anActionList.size()];
        anActionList.toArray(anActions);
        return anActions;
    }
}
