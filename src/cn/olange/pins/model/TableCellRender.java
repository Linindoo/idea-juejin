package cn.olange.pins.model;

import com.google.gson.JsonObject;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TableCellRender extends JPanel implements TableCellRenderer {
    private static final int MARGIN = 2;
    private JBLabel title;
    private final ColoredTableCellRenderer myRule;
    private MoreCellRenderer moreCellRenderer;
    public TableCellRender() {
        setLayout(new BorderLayout());
        title = new JBLabel();
        myRule = new MycellRender();
        add(myRule, BorderLayout.CENTER);
        add(title, BorderLayout.WEST);
        moreCellRenderer = new MoreCellRenderer();
        setBorder(JBUI.Borders.empty(MARGIN, MARGIN, MARGIN, 0));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        myRule.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = isSelected ? table.getSelectionBackground() : table.getBackground();
        setBackground(color);
        title.setBackground(color);
        title.setForeground(JBColor.blue);
        myRule.setBackground(color);
        if (value instanceof JsonObject) {
            JsonObject ruleObj = (JsonObject) value;
            this.title.setText(ruleObj.getAsJsonObject("author_user_info").get("user_name").getAsString());
        } else if (value instanceof NeedMore) {
            return moreCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,row,column);
        }
        return this;
    }
}
