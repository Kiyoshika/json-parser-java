/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.json;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonParserTest {
    @Test public void simpleKeyValue() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key\": \"Hello, world!\" }");
        assertEquals(result.getString("key"), "Hello, world!");
    }

    @Test public void multipleUniqueKeys() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key1\": \"value1\", \"key2\": 22.23, \"key3\": -123 }");
        assertEquals(result.getString("key1"), "value1");
        assertEquals(result.getDouble("key2"), 22.23, 0.00);
        assertEquals(result.getInt("key3"), -123);
    }

    @Test public void objectValue() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key\": { \"innerkey\": 10 }, \"key2\": 1 }");
        assertEquals(result.getObject("key").getInt("innerkey"), 10);
        assertEquals(result.getInt("key2"), 1);
    }

    @Test public void duplicatekeys() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1, \"key\": 2 }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1, \"key\": null }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": null, \"key\": 1 }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": null, \"key\": null }"));
    }

    @Test public void emptyKey() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"\": 1 }"));
    }

    @Test public void emptyValue() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": }"));
    }

    @Test public void missingSeparator() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\" 1 }"));
    }

    @Test public void missingStartBody() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("\"key\": 1 }"));
        assertThrows(Exception.class, () -> parser.parse(" \"key\": 1 }"));
    }

    @Test public void missingEndBody() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": {\"key2\": 1 }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": {\"key2\": 1 } "));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": [1,2,3]"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": [1,2,3] "));
    }

    @Test public void extraDelimiter() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1, }"));
    }

    @Test public void nullValues() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key\": null, \"key2\": 1 }");
        assertEquals(result.isNull("key"), true);
        assertEquals(result.isNull("key2"), false);
    }

    @Test public void arrayValue() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key\": [\"string one\", -123, 2.345, null, [1, 2, 3], { \"inner key\": \"value\" }]}");
        JsonArray array = result.getArray("key");
        assertEquals(array.getString(0), "string one");
        assertEquals(array.getInt(1), -123);
        assertEquals(array.getDouble(2), 2.345, 0.00);
        assertEquals(array.isNull(3), true);
        JsonArray innerArray = array.getArray(4);
        assertEquals(innerArray.getInt(0), 1);
        assertEquals(innerArray.getInt(1), 2);
        assertEquals(innerArray.getInt(2), 3);
        JsonResult innerObject = array.getObject(5);
        assertEquals(innerObject.getString("inner key"), "value");
    }

    @Test public void badNullValues() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": none }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": NULL }"));
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": n"));
    }

    @Test public void unDelimitedKey() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key: 1 }"));
    }

    @Test public void unDelimitedValue() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": \"value }"));
    }

    @Test public void addValues() throws Exception {
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse("{ \"key\": \"value\" }");
        // overwrite value
        result.setValue("key", new JsonItem(13, JsonType.INTEGER));
        assertEquals(result.getInt("key"), 13);
        assertFalse(result.isNull("key"));

        // nullify a value
        result.setValue("key", new JsonItem(null, JsonType.NULL));
        assertTrue(result.isNull("key"));

        // other value types (now using unique keys)
        result.addString("key2", "string value");
        assertEquals(result.getString("key2"), "string value");

        result.addDouble("key3", 3.14159);
        assertEquals(result.getDouble("key3"), 3.14159, 0.00);

        JsonResult subObj = new JsonResult();
        subObj.addString("innerkey", "innervalue");
        result.addObject("key4", subObj);
        assertEquals(result.getObject("key4").getString("innerkey"), "innervalue");

        JsonArray array = new JsonArray();
        array.addInt(1);
        array.addDouble(2.2);
        array.addString("3");
        result.addArray("key5", array);
        JsonArray getArray = result.getArray("key5");
        assertEquals(getArray.getInt(0), 1);
        assertEquals(getArray.getDouble(1), 2.2, 0.00);
        assertEquals(getArray.getString(2), "3");
    }

    @Test public void emptyJson() throws Exception {
        JsonParser parser = new JsonParser();
        // verifying no exceptions happen
        parser.parse("{}");
        parser.parse("{ }");
        parser.parse("{  }");
    }

    @Test public void parseArrayDirectly() throws Exception {
        JsonArray array = JsonArray.fromString("[{\"key\": 1}, {\"key\": 2}, {\"key\": 3}]");
        assertEquals(array.getObject(0).getInt("key"), 1);
        assertEquals(array.getObject(1).getInt("key"), 2);
        assertEquals(array.getObject(2).getInt("key"), 3);

        // leading spaces
        JsonArray array2 = JsonArray.fromString("   [{\"key\": 1}, {\"key\": 2}, {\"key\": 3}]");
        assertEquals(array2.getObject(0).getInt("key"), 1);
        assertEquals(array2.getObject(1).getInt("key"), 2);
        assertEquals(array2.getObject(2).getInt("key"), 3);
    }

    @Test public void nullkeys() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ null: \"value\" }"));

        JsonResult obj = new JsonResult();
        obj.addString("key", "value");

        JsonArray array = new JsonArray();
        array.addString("value");

        JsonResult result = new JsonResult();
        assertThrows(Exception.class, () -> result.addString(null, "value"));
        assertThrows(Exception.class, () -> result.addInt(null, 1));
        assertThrows(Exception.class, () -> result.addDouble(null, 2.2));
        assertThrows(Exception.class, () -> result.addNull(null));
        assertThrows(Exception.class, () -> result.addObject(null, obj));
        assertThrows(Exception.class, () -> result.addArray(null, array));
    }

    @Test public void jsonToString() throws Exception {
        JsonResult json = new JsonResult();
        json.addString("key", "some value");
        json.addInt("key2", -123);
        json.addDouble("key3", 3.14159);
        json.addNull("key4");

        JsonArray array = new JsonArray();
        array.addString("string value");
        array.addInt(-123);
        array.addDouble(3.14159);
        array.addNull();
        array.addObject(json);

        JsonResult json2 = new JsonResult();
        json2.addString("key", "some value");
        json2.addInt("key2", -123);
        json2.addDouble("key3", 3.14159);
        json2.addNull("key4");
        json2.addObject("key5", json);
        json2.addArray("key6", array);

        String jsonString = json2.toString();

        // check if string parses back into json without errors
        JsonParser parser = new JsonParser();
        parser.parse(jsonString);
    }
}
