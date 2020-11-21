package cn.olange.pins.action;

import cn.olange.pins.model.PageOperation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshPinsAction extends AnAction implements DumbAware {
	private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;
	private PageOperation pageOperation;

	public RefreshPinsAction(PageOperation  pageOperation) {
		super(REFRESH_ICON);
		this.pageOperation = pageOperation;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		pageOperation.refrshPage();
	}
}
