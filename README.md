# Java JSON Parser
This is a very minimalistic JSON parser I wrote to practice Java. It is not feature complete and probably wouldn't recommend using it in production.

The parser works as a very basic state machine that uses either expected tokens or termination tokens to proceed to the next state.

## Warning
The `master` branch is the active development branch and my be unstable compared to the releases.

If you're looking for a more "stable" version, check the [releases](https://github.com/Kiyoshika/json-parser-java/releases).

## Current Features
* Can parse `String`, `int`, `double`, `Object` (nested JSON), `null` and `Array` values.
* Modifying/creating JSON (`JsonResult`) using `setValue()` or `setNull()`

## Upcoming Features
* Reflection to parse a JSON directly into a user-defined class
* Writing JSON back to string

## Known Issues
* Parser may break if keys or values contain escaped quotes
* Duplicate keys are not checked across null and non-null values
    * i.e., you can use a duplicate key if it has both a null and non-null value

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
        \"key4\": null,
        \"key5\": [1, 2.2, [1, 2, 3], { \"inner key\": \"inner value\"}]
    }
    """;
JsonResult result = parser.parse(jsonString);

int innerKey = result.getObject("key").getInt("innerkey"); // 10
double key2 = result.getDouble("key2"); // 1.23
String key3 = result.getString("key3"); // Hello, world!
boolean key4 = result.isNull("key4"); // true

// handling arrays
JsonArray key5 = result.getArray("key5");
int arrayValue1 = key5.getInt(0); // 1
double arrayValue2 = key5.getDouble(1); // 2.2
JsonArray arrayValue3 = key5.getArray(2); // [1, 2, 3]
JsonResult arrayValue4 = key5.getObject(3); // { "inner key": "inner value" }
```

Keys and values must use DOUBLE quotes and NOT single quotes!

DO NOT use `get()` methods to check if a value is null since this will return null if the key is not present in JSON. Use the `isNull()` method instead.

Currently, a `JsonResult` object is returned which has basic getters with type conversions but will hopefully feature reflection to parse directly into a user-defined class. `JsonResult` also has a generic `get()` method which returns an `Object` type so you would have to do the conversion yourself. This method is not recommended unless you are doing something out of the ordinary.

## Building
* Run `./gradlew build` from the root directory
* A jar file will be found in `./lib/build/libs/json-parser-[VERSION].jar`
    * `[VERSION]` is the version of the library you're building.
