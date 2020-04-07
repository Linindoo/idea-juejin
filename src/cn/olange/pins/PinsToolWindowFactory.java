package cn.olange.pins;

import cn.olange.pins.view.PinsToolWindowPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;


public class PinsToolWindowFactory implements ToolWindowFactory {
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		PinsToolWindowPanel pins = new PinsToolWindowPanel(project);
		Content content = contentFactory.createContent(pins, "推荐", false);
		content.setPreferredFocusableComponent(pins);
		content.setDisposer(pins);
		toolWindow.getContentManager().addContent(content);
	}
}
