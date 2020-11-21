package cn.olange.pins;

import cn.olange.pins.view.PinsToolWindowPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;


public class PinsToolWindowFactory implements ToolWindowFactory {
	public static String ID = "沸点";
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(new PinsToolWindowPanel(project, "recommend"), "", false));
	}

	public static void updateTitle(@NotNull Project project, String userName) {
		ToolWindow toolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
		if (StringUtils.isNotBlank(userName)) {
			toolWindows.setTitle(userName);
		} else {
			toolWindows.setTitle("");
		}
	}

	public static DataContext getDataContext(@NotNull Project project) {
		ToolWindow toolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
		DataContext dataContext = DataManager.getInstance().getDataContext(toolWindows.getContentManager().getContent(0).getComponent());
		return dataContext;
	}
}
