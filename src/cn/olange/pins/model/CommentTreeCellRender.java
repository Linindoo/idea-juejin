package cn.olange.pins.model;

import cn.olange.pins.view.CommentItem;
import cn.olange.pins.view.CommentReply;
import com.google.gson.JsonObject;
import com.intellij.ide.IdeBundle;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class CommentTreeCellRender extends JPanel implements TreeCellRenderer {
    private SimpleTextAttributes simpleTextAttributes = new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground());
    private static final int MARGIN = 2;
    private JBLabel title;
    private final ColoredTreeCellRenderer treeCellRenderer;
    private ColoredTreeCellRenderer  moreCellRenderer;
    public CommentTreeCellRender() {
        setLayout(new BorderLayout());
        title = new JBLabel();
        treeCellRenderer = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object value, boolean b, boolean b1, boolean b2, int i, boolean b3) {
                this.append((String) value, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                setBorder(null);
            }
        };
        add(treeCellRenderer, BorderLayout.CENTER);
        add(title, BorderLayout.WEST);
        moreCellRenderer = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
                this.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));
                this.append(IdeBundle.message("search.everywhere.points.more", new Object[0]), simpleTextAttributes);
                this.setIpad(JBInsets.create(1, 7));
                this.setMyBorder(null);
            }
        };
        setBorder(JBUI.Borders.empty(MARGIN, MARGIN, MARGIN, 0));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Color color = selected ? tree.getForeground() : tree.getBackground();
        setBackground(color);
        title.setBackground(color);
        title.setForeground(JBColor.blue);
        treeCellRenderer.setBackground(color);
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) value;
        if (mutableTreeNode.getUserObject() instanceof CommentNode) {
            CommentNode commentTreeNode = (CommentNode) mutableTreeNode.getUserObject() ;
            if (commentTreeNode.getLevel() == 1) {
                String userName = commentTreeNode.getItem().get("user_info").getAsJsonObject().get("user_name").getAsString();
                this.title.setText(userName + ":");
                String commentContent = commentTreeNode.getItem().getAsJsonObject("comment_info").get("comment_content").getAsString();
                treeCellRenderer.getTreeCellRendererComponent(tree, commentContent, selected, expanded, leaf, row, hasFocus);
            } else if (commentTreeNode.getLevel() == 2) {
                String userName = commentTreeNode.getItem().get("user_info").getAsJsonObject().get("user_name").getAsString();
                this.title.setText(userName + " 回复:");
                String commentContent = commentTreeNode.getItem().getAsJsonObject("reply_info").get("reply_content").getAsString();
                treeCellRenderer.getTreeCellRendererComponent(tree, commentContent, selected, expanded, leaf, row, hasFocus);
            }
        } else if (mutableTreeNode.getUserObject()  instanceof NeedMore) {
            return moreCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }else {
            this.title.setText("评论列表");
            treeCellRenderer.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
        }
        return this;
    }
}
