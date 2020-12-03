package cn.olange.pins.model;

import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonObject;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TableCellRender extends JPanel implements TableCellRenderer {
    private SimpleTextAttributes simpleTextAttributes = new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground());
    private static final int MARGIN = 2;
    private JBLabel title;
    private final ColoredTableCellRenderer myRule;
    private ColoredTableCellRenderer moreCellRenderer;
    public TableCellRender() {
        setLayout(new BorderLayout());
        title = new JBLabel();
        myRule = new ColoredTableCellRenderer() {
            @Override
            protected void customizeCellRenderer(JTable jTable, @Nullable Object value, boolean b, boolean b1, int i, int i1) {
                if (value instanceof JsonObject) {
                    JsonObject ruleObj = (JsonObject) value;
                    JsonObject msg_info = ruleObj.getAsJsonObject("msg_Info");
                    this.append("(" + msg_info.get("comment_count").getAsString() + ")" + msg_info.get("content").getAsString(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }

                setBorder(null);
            }
        };
        add(myRule, BorderLayout.CENTER);
        add(title, BorderLayout.WEST);
        moreCellRenderer = new ColoredTableCellRenderer() {
            @Override
            protected void customizeCellRenderer(JTable jTable, @Nullable Object o, boolean b, boolean b1, int i, int i1) {
                this.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));
                this.append("...", simpleTextAttributes);
                this.setIpad(JBInsets.create(1, 7));
                this.setMyBorder(null);
            }
        };
        setBorder(JBUI.Borders.empty(MARGIN, MARGIN, MARGIN, 0));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        myRule.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = isSelected ? table.getSelectionBackground() : table.getBackground();
        setBackground(color);
        Config config = JuejinPersistentConfig.getInstance().getState();
        title.setBackground(color);
        myRule.setBackground(color);
        if (value instanceof JsonObject) {
            JsonObject item = (JsonObject) value;
            if (config.isLogined() && Comparing.strEqual(config.getUserId(), item.getAsJsonObject("author_user_info").get("user_id").getAsString())) {
                this.title.setForeground(JBColor.GREEN);
            } else {
                title.setForeground(JBColor.blue);
            }
            this.title.setText(item.getAsJsonObject("author_user_info").get("user_name").getAsString());
        } else if (value instanceof NeedMore) {
            return moreCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        return this;
    }
}
