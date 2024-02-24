package org.json;

public class JsonItem {
    private Object value;
    private JsonType type;

    public JsonItem(Object value, JsonType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public int getInt() {
        return (int)this.value;
    }

    public double getDouble() {
        return (double)this.value;
    }

    public String getString() {
        return (String)this.value;
    }

    public JsonArray getArray() {
        return (JsonArray)this.value;
    }

    public JsonResult getObject() {
        return (JsonResult)this.value;
    }

    public boolean isNull() {
        return this.type == JsonType.NULL;
    }

    public JsonType getType() {
        return this.type;
    }
}
