package cn.olange.pins.model;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RunAnythingMore extends JPanel {
    static final RunAnythingMore instance = new RunAnythingMore();
    final JLabel label = new JLabel(" load more ...");

    private RunAnythingMore() {
        super(new BorderLayout());
        this.add(this.label, "Center");
    }

    static RunAnythingMore get(boolean isSelected) {
        instance.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));
        instance.setBackground(UIUtil.getListBackground(isSelected, true));
        instance.label.setForeground(UIUtil.getLabelDisabledForeground());
//			instance.label.setFont(RunAnythingUtil.getTitleFont());
        instance.label.setBackground(UIUtil.getListBackground(isSelected, true));
        instance.label.setBorder(JBUI.Borders.emptyLeft(1));
        return instance;
    }
}
