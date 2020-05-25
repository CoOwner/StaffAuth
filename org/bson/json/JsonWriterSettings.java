package org.bson.json;

import org.bson.*;

public class JsonWriterSettings extends BsonWriterSettings
{
    private final boolean indent;
    private final String newLineCharacters;
    private final String indentCharacters;
    private final JsonMode outputMode;
    
    public JsonWriterSettings() {
        this(JsonMode.STRICT, false, null, null);
    }
    
    public JsonWriterSettings(final JsonMode outputMode) {
        this(outputMode, false, null, null);
    }
    
    public JsonWriterSettings(final boolean indent) {
        this(JsonMode.STRICT, indent, indent ? "  " : null, null);
    }
    
    public JsonWriterSettings(final JsonMode outputMode, final boolean indent) {
        this(outputMode, indent, indent ? "  " : null, null);
    }
    
    public JsonWriterSettings(final JsonMode outputMode, final String indentCharacters) {
        this(outputMode, true, indentCharacters, null);
    }
    
    public JsonWriterSettings(final JsonMode outputMode, final String indentCharacters, final String newLineCharacters) {
        this(outputMode, true, indentCharacters, newLineCharacters);
    }
    
    private JsonWriterSettings(final JsonMode outputMode, final boolean indent, final String indentCharacters, final String newLineCharacters) {
        if (indent) {
            if (indentCharacters == null) {
                throw new IllegalArgumentException("indent characters can not be null if indent is enabled");
            }
        }
        else {
            if (newLineCharacters != null) {
                throw new IllegalArgumentException("new line characters can not be null if indent is disabled.");
            }
            if (indentCharacters != null) {
                throw new IllegalArgumentException("indent characters can not be null if indent is disabled.");
            }
        }
        if (outputMode == null) {
            throw new IllegalArgumentException("output mode can not be null");
        }
        this.indent = indent;
        this.newLineCharacters = ((newLineCharacters != null) ? newLineCharacters : System.getProperty("line.separator"));
        this.indentCharacters = indentCharacters;
        this.outputMode = outputMode;
    }
    
    public boolean isIndent() {
        return this.indent;
    }
    
    public String getNewLineCharacters() {
        return this.newLineCharacters;
    }
    
    public String getIndentCharacters() {
        return this.indentCharacters;
    }
    
    public JsonMode getOutputMode() {
        return this.outputMode;
    }
}
