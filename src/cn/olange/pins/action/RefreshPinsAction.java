package cn.olange.pins.action;

import cn.olange.pins.PinsToolWindowFactory;
import cn.olange.pins.utils.DataKeys;
import cn.olange.pins.view.PinsToolWindowPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshPinsAction extends AnAction implements DumbAware {
	private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;

	public RefreshPinsAction() {
		super(REFRESH_ICON);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		PinsToolWindowPanel pinsToolWindowPanel = PinsToolWindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.JUEJIN_PROJECTS_PINPANEL);
		if (pinsToolWindowPanel != null) {
			pinsToolWindowPanel.refrshPage();
		}
	}
}
