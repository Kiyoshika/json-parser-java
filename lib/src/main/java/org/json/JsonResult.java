package org.json;

import java.util.HashMap;
import java.util.HashSet;

public class JsonResult {
    private HashMap<String, Object> items = new HashMap<>();
    private HashSet<String> nullValues = new HashSet();

    public boolean containsKey(String key) {
        return this.items.containsKey(key);
    }

    public boolean isNull(String key) {
        return this.nullValues.contains(key);
    }

    public void addNull(String key) {
        this.nullValues.add(key);
    }
    
    public void add(String key, Object value) {
        this.items.put(key, value);
    }

    public <T> T get(String key) {
        return (T) this.items.get(key);
    }

    public int getInt(String key) {
        return (int)this.get(key);
    }

    public String getString(String key) {
        return (String)this.get(key);
    }

    public JsonResult getObject(String key) {
        return (JsonResult)this.get(key);
    }

    public double getDouble(String key) {
        return (double)this.get(key);
    }

    public JsonArray getArray(String key) {
        return (JsonArray)this.get(key);
    }
}
