package cn.olange.pins.model;

import cn.olange.pins.view.CommentReply;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CommentReplyCellRender extends JLabel  implements ListCellRenderer  {

	private Map<String,Component> componentMap;

	public CommentReplyCellRender() {
		componentMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			return this;
		}
		JsonObject reply = (JsonObject) value;
		String id = reply.get("id").getAsString();
		Component component = componentMap.get(id);
		if (component == null) {
			CommentReply commentReply = new CommentReply(reply);
			componentMap.put(id, commentReply);
			return commentReply;
		}
		return component;
	}
}
