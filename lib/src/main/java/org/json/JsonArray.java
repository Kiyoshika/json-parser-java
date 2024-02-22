package org.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    private List<Object> arrayItems = new ArrayList<Object>();

    public JsonArray() {}

    public void add(Object item) {
        this.arrayItems.add(item);
    }

    public Object get(int i) {
        return this.arrayItems.get(i);
    }

    public JsonResult getObject(int i) {
        return (JsonResult)this.arrayItems.get(i);
    }

    public int getInt(int i) {
        return (int)this.get(i);
    }

    public String getString(int i) {
        return (String)this.get(i);
    }

    public double getDouble(int i) {
        return (double)this.get(i);
    }

    public JsonArray getArray(int i) {
        return (JsonArray)this.get(i);
    }

    public boolean isNull(int i) {
        return this.get(i) == null;
    }

}
