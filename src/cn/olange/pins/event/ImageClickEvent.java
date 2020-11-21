package cn.olange.pins.event;

import com.google.gson.JsonArray;

public abstract class ImageClickEvent {

    public abstract void viewImage(JsonArray pictures, int index);
}
