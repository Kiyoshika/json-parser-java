# Java JSON Parser
This is a very minimalistic JSON parser I wrote to practice Java. It is not feature complete and probably wouldn't recommend using it in production.

The parser works as a very basic state machine that uses either expected tokens or termination tokens to proceed to the next state.

## Current Features
* Can parse `String`, `int`, `double`, `Object` (nested JSON) and `null` values

## Upcoming Features
* Support for `Array`
* Reflection to parse a JSON directly into a user-defined class
* Modifying JSON after parsing
* Writing JSON back to string

## Known Issues
* Parser may break if keys or values contain escaped quotes

## Basic Usage
You can also take a look at the tests, but here's some basic usage of the library:

```java
JsonParser parser = new JsonParser();
String jsonString = """
    {
        \"key\": {
            \"innerKey\": 10
        },
        \"key2\": 1.23,
        \"key3\": \"Hello, world!\",
        \"key4\": null
    }
    """;
JsonResult result = parser.parse(jsonString);

int innerKey = result.getObject("key").getInt("innerkey"); // 10
double key2 = result.getDouble("key2"); // 1.23
String key3 = result.getString("key3"); // Hello, world!
boolean key4 = result.isNull("key4"); // true
```

Keys and values must use DOUBLE quotes and NOT single quotes!

DO NOT use `get()` methods to check if a value is null since this will return null if the key is not present in JSON. Use the `isNull()` method instead.

Currently, a `JsonResult` object is returned which has basic getters with type conversions but will hopefully feature reflection to parse directly into a user-defined class. `JsonResult` also has a generic `get()` method which returns an `Object` type so you would have to do the conversion yourself. This method is not recommended unless you are doing something out of the ordinary.

## Building
* Run `./gradlew build` from the root directory
* A jar file will be found in `./lib/build/libs/json-parser-[VERSION].jar`
    * `[VERSION]` is the version of the library you're building.