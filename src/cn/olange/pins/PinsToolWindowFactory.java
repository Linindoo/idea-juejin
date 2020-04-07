package cn.olange.pins;

import cn.olange.pins.view.PinsToolWindowPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;


public class PinsToolWindowFactory implements ToolWindowFactory {
	private String HotTopic = "f0a2fbbc03d4d46266e40762139c414c";
	private String RecommendedTopic = "249431a8e4d85e459f6c29eb808e76d0";
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(new PinsToolWindowPanel(project, HotTopic,"hot"), "热门", false));
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(new PinsToolWindowPanel(project, RecommendedTopic, "recommend"), "推荐", false));
	}
}
