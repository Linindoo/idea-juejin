package cn.olange.pins.model;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ReplyUserActionButton extends ActionButton {

    public ReplyUserActionButton(@NotNull AnAction action) {
        super(action, action.getTemplatePresentation().clone(), "unknown", ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
        this.setLook(ActionButtonLook.INPLACE_LOOK);
        this.setFocusable(false);
        this.updateIcon();
    }

    protected DataContext getDataContext() {
        return DataManager.getInstance().getDataContext(this);
    }

    public int getPopState() {
        return this.isSelected() ? 2 : super.getPopState();
    }

    boolean isRolloverState() {
        return super.isRollover();
    }

    public Icon getIcon() {
        if (this.isEnabled() && this.isSelected()) {
            Icon selectedIcon = this.myPresentation.getSelectedIcon();
            if (selectedIcon != null) {
                return selectedIcon;
            }
        }

        return super.getIcon();
    }

    public static abstract class ClearAction extends DumbAwareAction {
        public ClearAction() {
            super(AllIcons.Actions.Close);
            this.getTemplatePresentation().setHoveredIcon(AllIcons.Actions.CloseHovered);
        }
    }
}
