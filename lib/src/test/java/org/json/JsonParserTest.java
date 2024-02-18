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
        assertEquals(result.get("key1"), "value1");
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

    @Test public void missingStartBracket() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse(" \"key\": 1 }"));
    }

    @Test public void missingEndBracket() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1"));
    }

    @Test public void extraDelimiter() throws Exception {
        JsonParser parser = new JsonParser();
        assertThrows(Exception.class, () -> parser.parse("{ \"key\": 1, }"));
    }
}