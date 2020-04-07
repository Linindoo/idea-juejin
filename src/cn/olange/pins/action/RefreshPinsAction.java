package cn.olange.pins.action;

import cn.olange.pins.model.Handler;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshPinsAction extends AnAction implements DumbAware {
	private Handler<Void> refreshInvokeHandler;

	private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;

	public RefreshPinsAction(Handler<Void> handler) {
		super(REFRESH_ICON);
		refreshInvokeHandler = handler;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		refreshInvokeHandler.handle(null);

	}
}
