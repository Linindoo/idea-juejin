package cn.olange.pins.model;

import com.google.gson.JsonObject;

public class CommentNode {
    private int level;
    private JsonObject item;

    public CommentNode(int level, JsonObject item) {
        this.level = level;
        this.item = item;
    }

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
