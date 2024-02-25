# Java JSON Parser
This is a very minimalistic JSON parser I wrote to practice Java. Use in production at your own risk (:

The parser works as a very basic state machine that uses either expected tokens or termination tokens to proceed to the next state.

## Warning
The `master` branch is the active development branch and my be unstable compared to the releases.

If you're looking for a more "stable" version, check the [releases](https://github.com/Kiyoshika/json-parser-java/releases).

## Current Features
* Can parse `String`, `int`, `double`, `boolean`, `Object` (nested JSON), `null` and `Array` values.
* Modifying/creating JSON (`JsonResult`) objects/arrays
* Writing JSON objects/arrays to string

## Upcoming Features
* Reflection to parse a JSON directly into a user-defined class

## Known Issues
* Parser may break if keys or values contain escaped quotes

## Basic Usage
You can also take a look at the tests, but here's some basic usage of the library:

### Parsing Single Objects

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
        \"key5\": [1, 2.2, [1, 2, 3], { \"inner key\": \"inner value\"}, false],
        \"key6\": true
    }
    """;
JsonResult result = parser.parse(jsonString);

// retrieve values
int innerKey = result.getObject("key").getInt("innerkey"); // 10
double key2 = result.getDouble("key2"); // 1.23
String key3 = result.getString("key3"); // Hello, world!
boolean key4 = result.isNull("key4"); // true
boolean key6 = result.getBoolean("key6"); // true

// handling arrays
JsonArray key5 = result.getArray("key5");
int arrayValue1 = key5.getInt(0); // 1
double arrayValue2 = key5.getDouble(1); // 2.2
JsonArray arrayValue3 = key5.getArray(2); // [1, 2, 3]
JsonResult arrayValue4 = key5.getObject(3); // { "inner key": "inner value" }
boolean arrayValue5 = key5.getBoolean(4); // false

// overwrite values
// overwrites key2 from 1.23 -> "hello"
result.setValue("key2", new JsonItem("hello", JsonType.STRING));

// add new values
result.addString("newkey", "new value");
result.addInt("intkey", 12);
```

Keys and values must use DOUBLE quotes and NOT single quotes!

DO NOT use `get()` methods to check if a value is null since this will return null if the key is not present in JSON. Use the `isNull()` method instead.

### Parsing Arrays
If given an array of JsonObjects (or primitives), you can parse the arrays directly.

```java
String arrayString = """
[
    {\"key\": 1},
    {\"key\": 2},
    {\"key\": 3}
]
""";

JsonArray array = JsonArray.fromString(arrayString);
array.getObject(0).getInt("key"); // 1
array.getObject(1).getInt("key"); // 2
array.getObject(2).getInt("key"); // 3

// overwrite value (changes 2 -> 42)
array.getObject(1).setValue("key", new JsonItem(42, JsonType.INTEGER));

// add value
array.addString("hello"); // [ 1, 42, 3, "hello" ]
array.addNull(); // [ 1, 42, 3, "hello", null ]
```

### Writing to String
Both objects (`JsonResult`) and arrays (`JsonArray`) have a `toString()` method to convert either representation to a string.

## Building
* Run `./gradlew build` from the root directory
* A jar file will be found in `./lib/build/libs/json-parser-[VERSION].jar`
    * `[VERSION]` is the version of the library you're building.
