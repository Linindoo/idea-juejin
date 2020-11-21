package cn.olange.pins.model;

import com.google.gson.JsonObject;

import javax.swing.tree.DefaultMutableTreeNode;

public class CommentTreeNode extends DefaultMutableTreeNode {

    private JsonObject item;
    private int level;

    public CommentTreeNode(JsonObject item, int level) {
        super(item);
        this.item = item;
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public JsonObject getItem() {
        return item;
    }

    public void setItem(JsonObject item) {
        this.item = item;
    }
}
