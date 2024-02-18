package org.json;

public enum JsonState {
    // first '{' token
    START_BODY,
    // first double quote after START_BODY that begins the key name
    START_KEY_QUOTE,
    // content inside the key which can be anything besides a double quote
    KEY_CONTENT,
    END_KEY_QUOTE,
    // the colon (:) separating the key and value
    KEY_VALUE_SEPARATOR,
    // content after the keyseparator. Can be a string (starts/ends with a quote), number containing decimal, array or another object
    VALUE_CONTENT,
    // the comma (,) separating a value and the next key (if any; this state is optional)
    VALUE_KEY_SEPARATOR,
    // the final '}' token
    END_BODY
}
