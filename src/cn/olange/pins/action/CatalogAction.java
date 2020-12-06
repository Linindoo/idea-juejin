package cn.olange.pins.action;

import cn.olange.pins.PinsToolWindowFactory;
import cn.olange.pins.model.CatalogTag;
import cn.olange.pins.model.Config;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.DataKeys;
import cn.olange.pins.view.PinsToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CatalogAction extends ToggleAction {
    private CatalogTag tag;

    public CatalogAction(CatalogTag tag) {
        super(tag.getLabel());
        this.tag = tag;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        return tag.isSelected();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean b) {
        tag.setSelected(b);
        if (b) {
            Config config = JuejinPersistentConfig.getInstance().getState();
            config.setCurentCatalog(tag.getValue());
            PinsToolWindowPanel pinsToolWindowPanel = PinsToolWindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.JUEJIN_PROJECTS_PINPANEL);
            if (pinsToolWindowPanel != null) {
                pinsToolWindowPanel.refrshPage();
            }
        }
    }
}
