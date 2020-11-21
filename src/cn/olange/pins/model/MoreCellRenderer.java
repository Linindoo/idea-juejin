package cn.olange.pins.model;

import com.intellij.ide.IdeBundle;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MoreCellRenderer extends ColoredTableCellRenderer {
    private SimpleTextAttributes simpleTextAttributes = new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground());

    @Override
    protected int getMinHeight() {
        return -1;
    }


    @Override
    protected void customizeCellRenderer(JTable jTable, @Nullable Object o, boolean b, boolean b1, int i, int i1) {
        this.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));
        this.append(IdeBundle.message("search.everywhere.points.more", new Object[0]), simpleTextAttributes);
        this.setIpad(JBInsets.create(1, 7));
        this.setMyBorder(null);
    }
}
