package cn.olange.pins.model;

import com.google.gson.JsonObject;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MycellRender  extends ColoredTableCellRenderer {
    @Override
    protected void customizeCellRenderer(JTable jTable, @Nullable Object value, boolean b, boolean b1, int i, int i1) {
        if (value instanceof JsonObject) {
            JsonObject ruleObj = (JsonObject) value;
            JsonObject msg_info = ruleObj.getAsJsonObject("msg_Info");
            this.append("(" + msg_info.get("comment_count").getAsString() + ")" + msg_info.get("content").getAsString(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }

        setBorder(null);
    }
}
