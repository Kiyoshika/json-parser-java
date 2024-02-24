package org.json;

import java.util.HashMap;
import java.util.HashSet;

public class JsonResult {
    private HashMap<String, JsonItem> items = new HashMap<>();
    private final String NON_NULL_KEY_MSG = "Key cannot be null.";

    public boolean containsKey(String key) {
        return this.items.containsKey(key);
    }

    public boolean isNull(String key) {
        JsonItem item = this.items.get(key);
        return item != null && item.getType() == JsonType.NULL;
    }

    public void addNull(String key) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }

        this.items.put(key, new JsonItem(null, JsonType.NULL));
    }
    
    public void addString(String key, String value) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }
        
        this.items.put(key, new JsonItem(value, JsonType.STRING));
    }

    public void addInt(String key, int value) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }
        
        this.items.put(key, new JsonItem(value, JsonType.INTEGER));
    }

    public void addDouble(String key, double value) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }
        
        this.items.put(key, new JsonItem(value, JsonType.DOUBLE));
    }

    public void addArray(String key, JsonArray value) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }
        
        this.items.put(key, new JsonItem(value, JsonType.ARRAY));
    }

    public void addObject(String key, JsonResult value) throws Exception {
        if (key == null) {
            throw new Exception(this.NON_NULL_KEY_MSG);
        }
        
        this.items.put(key, new JsonItem(value, JsonType.OBJECT));
    }

    public <T> T get(String key) {
        return (T) this.items.get(key).getValue();
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

    public void setValue(String key, JsonItem value) {
        this.items.put(key, value);
    }

    public String toString() {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append('{');

        int n_items = this.items.size();

        int i = 0;
        for (HashMap.Entry<String, JsonItem> item : this.items.entrySet()) {
            StringBuilder key = new StringBuilder();
            key.append('"');
            key.append(item.getKey());
            key.append("\":");
            jsonString.append(key.toString());

            JsonItem value = item.getValue();
            if (value.getType() == JsonType.ARRAY) {
                jsonString.append(((JsonArray)value.getValue()).toString());
            } else {
                if (value.getType() == JsonType.STRING) {
                    jsonString.append('"');
                }
                jsonString.append(value.getValue());
                if (value.getType() == JsonType.STRING) {
                    jsonString.append('"');
                }
            }

            i += 1;
            if (i < n_items) {
                jsonString.append(',');
            }

        }

        jsonString.append('}');

        return jsonString.toString();
    }
}
