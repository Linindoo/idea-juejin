package cn.olange.pins.model;

import cn.olange.pins.view.PinItemDecorator;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PinListCellRender extends ColoredListCellRenderer {
	private Project project;
	private Map<String,Component> componentMap;
	public PinListCellRender(Project project) {
		this.project = project;
		this.componentMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int i, boolean selected, boolean b1) {
		if (value == null) {
			return this;
		}
		JsonObject pins = (JsonObject) value;
		String id = pins.get("msg_id").getAsString();
       		Component component = componentMap.get(id);
		if (component == null) {
			component = new PinItemDecorator(pins,project);
			componentMap.put(id, component);
		}
		this.mySelected = selected;
		this.myForeground = this.isEnabled() ? list.getForeground() : UIUtil.getLabelDisabledForeground();
		this.mySelectionForeground = list.getSelectionForeground();
		if (UIUtil.isUnderWin10LookAndFeel()) {
			component.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
		} else {
			component.setBackground(selected ? list.getSelectionBackground() : null);
		}
		return component;
	}

	@Override
	protected void customizeCellRenderer(@NotNull JList jList, Object o, int i, boolean b, boolean b1) {

	}
}
