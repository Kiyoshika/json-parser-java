package org.json;

import java.util.HashMap;

public class JsonResult {
    private HashMap<String, Object> items = new HashMap<>();

    public boolean containsKey(String key) {
        return this.items.containsKey(key);
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
}
