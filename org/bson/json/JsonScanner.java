package org.bson.json;

import org.bson.*;

class JsonScanner
{
    private final JsonBuffer buffer;
    
    public void setBufferPosition(final int newPosition) {
        this.buffer.setPosition(newPosition);
    }
    
    public int getBufferPosition() {
        return this.buffer.getPosition();
    }
    
    public JsonScanner(final JsonBuffer buffer) {
        this.buffer = buffer;
    }
    
    public JsonScanner(final String json) {
        this(new JsonBuffer(json));
    }
    
    public JsonToken nextToken() {
        int c;
        for (c = this.buffer.read(); c != -1 && Character.isWhitespace(c); c = this.buffer.read()) {}
        if (c == -1) {
            return new JsonToken(JsonTokenType.END_OF_FILE, "<eof>");
        }
        switch (c) {
            case 123: {
                return new JsonToken(JsonTokenType.BEGIN_OBJECT, "{");
            }
            case 125: {
                return new JsonToken(JsonTokenType.END_OBJECT, "}");
            }
            case 91: {
                return new JsonToken(JsonTokenType.BEGIN_ARRAY, "[");
            }
            case 93: {
                return new JsonToken(JsonTokenType.END_ARRAY, "]");
            }
            case 40: {
                return new JsonToken(JsonTokenType.LEFT_PAREN, "(");
            }
            case 41: {
                return new JsonToken(JsonTokenType.RIGHT_PAREN, ")");
            }
            case 58: {
                return new JsonToken(JsonTokenType.COLON, ":");
            }
            case 44: {
                return new JsonToken(JsonTokenType.COMMA, ",");
            }
            case 34:
            case 39: {
                return this.scanString((char)c);
            }
            case 47: {
                return this.scanRegularExpression();
            }
            default: {
                if (c == 45 || Character.isDigit(c)) {
                    return this.scanNumber((char)c);
                }
                if (c == 36 || c == 95 || Character.isLetter(c)) {
                    return this.scanUnquotedString();
                }
                final int position = this.buffer.getPosition();
                this.buffer.unread(c);
                throw new JsonParseException("Invalid JSON input. Position: %d. Character: '%c'.", new Object[] { position, c });
            }
        }
    }
    
    private JsonToken scanRegularExpression() {
        final int start = this.buffer.getPosition() - 1;
        int options = -1;
        RegularExpressionState state = RegularExpressionState.IN_PATTERN;
        while (true) {
            final int c = this.buffer.read();
            Label_0260: {
                switch (state) {
                    case IN_PATTERN: {
                        switch (c) {
                            case -1: {
                                state = RegularExpressionState.INVALID;
                                break Label_0260;
                            }
                            case 47: {
                                state = RegularExpressionState.IN_OPTIONS;
                                options = this.buffer.getPosition();
                                break Label_0260;
                            }
                            case 92: {
                                state = RegularExpressionState.IN_ESCAPE_SEQUENCE;
                                break Label_0260;
                            }
                            default: {
                                state = RegularExpressionState.IN_PATTERN;
                                break Label_0260;
                            }
                        }
                        break;
                    }
                    case IN_ESCAPE_SEQUENCE: {
                        state = RegularExpressionState.IN_PATTERN;
                        break;
                    }
                    case IN_OPTIONS: {
                        switch (c) {
                            case 105:
                            case 109:
                            case 115:
                            case 120: {
                                state = RegularExpressionState.IN_OPTIONS;
                                break Label_0260;
                            }
                            case -1:
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = RegularExpressionState.DONE;
                                break Label_0260;
                            }
                            default: {
                                if (Character.isWhitespace(c)) {
                                    state = RegularExpressionState.DONE;
                                    break Label_0260;
                                }
                                state = RegularExpressionState.INVALID;
                                break Label_0260;
                            }
                        }
                        break;
                    }
                }
            }
            switch (state) {
                case DONE: {
                    this.buffer.unread(c);
                    final int end = this.buffer.getPosition();
                    final BsonRegularExpression regex = new BsonRegularExpression(this.buffer.substring(start + 1, options - 1), this.buffer.substring(options, end));
                    return new JsonToken(JsonTokenType.REGULAR_EXPRESSION, regex);
                }
                case INVALID: {
                    throw new JsonParseException("Invalid JSON regular expression. Position: %d.", new Object[] { this.buffer.getPosition() });
                }
                default: {
                    continue;
                }
            }
        }
    }
    
    private JsonToken scanUnquotedString() {
        final int start = this.buffer.getPosition() - 1;
        int c;
        for (c = this.buffer.read(); c == 36 || c == 95 || Character.isLetterOrDigit(c); c = this.buffer.read()) {}
        this.buffer.unread(c);
        final String lexeme = this.buffer.substring(start, this.buffer.getPosition());
        return new JsonToken(JsonTokenType.UNQUOTED_STRING, lexeme);
    }
    
    private JsonToken scanNumber(final char firstChar) {
        int c = firstChar;
        final int start = this.buffer.getPosition() - 1;
        NumberState state = null;
        switch (c) {
            case 45: {
                state = NumberState.SAW_LEADING_MINUS;
                break;
            }
            case 48: {
                state = NumberState.SAW_LEADING_ZERO;
                break;
            }
            default: {
                state = NumberState.SAW_INTEGER_DIGITS;
                break;
            }
        }
        JsonTokenType type = JsonTokenType.INT64;
        while (true) {
            c = this.buffer.read();
            Label_0983: {
                switch (state) {
                    case SAW_LEADING_MINUS: {
                        switch (c) {
                            case 48: {
                                state = NumberState.SAW_LEADING_ZERO;
                                break Label_0983;
                            }
                            case 73: {
                                state = NumberState.SAW_MINUS_I;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_INTEGER_DIGITS;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_LEADING_ZERO: {
                        switch (c) {
                            case 46: {
                                state = NumberState.SAW_DECIMAL_POINT;
                                break Label_0983;
                            }
                            case 69:
                            case 101: {
                                state = NumberState.SAW_EXPONENT_LETTER;
                                break Label_0983;
                            }
                            case -1:
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = NumberState.DONE;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_INTEGER_DIGITS;
                                    break Label_0983;
                                }
                                if (Character.isWhitespace(c)) {
                                    state = NumberState.DONE;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_INTEGER_DIGITS: {
                        switch (c) {
                            case 46: {
                                state = NumberState.SAW_DECIMAL_POINT;
                                break Label_0983;
                            }
                            case 69:
                            case 101: {
                                state = NumberState.SAW_EXPONENT_LETTER;
                                break Label_0983;
                            }
                            case -1:
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = NumberState.DONE;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_INTEGER_DIGITS;
                                    break Label_0983;
                                }
                                if (Character.isWhitespace(c)) {
                                    state = NumberState.DONE;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_DECIMAL_POINT: {
                        type = JsonTokenType.DOUBLE;
                        if (Character.isDigit(c)) {
                            state = NumberState.SAW_FRACTION_DIGITS;
                            break;
                        }
                        state = NumberState.INVALID;
                        break;
                    }
                    case SAW_FRACTION_DIGITS: {
                        switch (c) {
                            case 69:
                            case 101: {
                                state = NumberState.SAW_EXPONENT_LETTER;
                                break Label_0983;
                            }
                            case -1:
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = NumberState.DONE;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_FRACTION_DIGITS;
                                    break Label_0983;
                                }
                                if (Character.isWhitespace(c)) {
                                    state = NumberState.DONE;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_EXPONENT_LETTER: {
                        type = JsonTokenType.DOUBLE;
                        switch (c) {
                            case 43:
                            case 45: {
                                state = NumberState.SAW_EXPONENT_SIGN;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_EXPONENT_DIGITS;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_EXPONENT_SIGN: {
                        if (Character.isDigit(c)) {
                            state = NumberState.SAW_EXPONENT_DIGITS;
                            break;
                        }
                        state = NumberState.INVALID;
                        break;
                    }
                    case SAW_EXPONENT_DIGITS: {
                        switch (c) {
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = NumberState.DONE;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isDigit(c)) {
                                    state = NumberState.SAW_EXPONENT_DIGITS;
                                    break Label_0983;
                                }
                                if (Character.isWhitespace(c)) {
                                    state = NumberState.DONE;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                    case SAW_MINUS_I: {
                        boolean sawMinusInfinity = true;
                        final char[] nfinity = { 'n', 'f', 'i', 'n', 'i', 't', 'y' };
                        for (int i = 0; i < nfinity.length; ++i) {
                            if (c != nfinity[i]) {
                                sawMinusInfinity = false;
                                break;
                            }
                            c = this.buffer.read();
                        }
                        if (!sawMinusInfinity) {
                            state = NumberState.INVALID;
                            break;
                        }
                        type = JsonTokenType.DOUBLE;
                        switch (c) {
                            case -1:
                            case 41:
                            case 44:
                            case 93:
                            case 125: {
                                state = NumberState.DONE;
                                break Label_0983;
                            }
                            default: {
                                if (Character.isWhitespace(c)) {
                                    state = NumberState.DONE;
                                    break Label_0983;
                                }
                                state = NumberState.INVALID;
                                break Label_0983;
                            }
                        }
                        break;
                    }
                }
            }
            switch (state) {
                case INVALID: {
                    throw new JsonParseException("Invalid JSON number");
                }
                case DONE: {
                    this.buffer.unread(c);
                    final String lexeme = this.buffer.substring(start, this.buffer.getPosition());
                    if (type == JsonTokenType.DOUBLE) {
                        return new JsonToken(JsonTokenType.DOUBLE, Double.parseDouble(lexeme));
                    }
                    final long value = Long.parseLong(lexeme);
                    if (value < -2147483648L || value > 2147483647L) {
                        return new JsonToken(JsonTokenType.INT64, value);
                    }
                    return new JsonToken(JsonTokenType.INT32, (int)value);
                }
                default: {
                    continue;
                }
            }
        }
    }
    
    private JsonToken scanString(final char quoteCharacter) {
        final StringBuilder sb = new StringBuilder();
        while (true) {
            int c = this.buffer.read();
            Label_0373: {
                switch (c) {
                    case 92: {
                        c = this.buffer.read();
                        switch (c) {
                            case 39: {
                                sb.append('\'');
                                break Label_0373;
                            }
                            case 34: {
                                sb.append('\"');
                                break Label_0373;
                            }
                            case 92: {
                                sb.append('\\');
                                break Label_0373;
                            }
                            case 47: {
                                sb.append('/');
                                break Label_0373;
                            }
                            case 98: {
                                sb.append('\b');
                                break Label_0373;
                            }
                            case 102: {
                                sb.append('\f');
                                break Label_0373;
                            }
                            case 110: {
                                sb.append('\n');
                                break Label_0373;
                            }
                            case 114: {
                                sb.append('\r');
                                break Label_0373;
                            }
                            case 116: {
                                sb.append('\t');
                                break Label_0373;
                            }
                            case 117: {
                                final int u1 = this.buffer.read();
                                final int u2 = this.buffer.read();
                                final int u3 = this.buffer.read();
                                final int u4 = this.buffer.read();
                                if (u4 != -1) {
                                    final String hex = new String(new char[] { (char)u1, (char)u2, (char)u3, (char)u4 });
                                    sb.append((char)Integer.parseInt(hex, 16));
                                    break Label_0373;
                                }
                                break Label_0373;
                            }
                            default: {
                                throw new JsonParseException("Invalid escape sequence in JSON string '\\%c'.", new Object[] { c });
                            }
                        }
                        break;
                    }
                    default: {
                        if (c == quoteCharacter) {
                            return new JsonToken(JsonTokenType.STRING, sb.toString());
                        }
                        if (c != -1) {
                            sb.append((char)c);
                            break;
                        }
                        break;
                    }
                }
            }
            if (c == -1) {
                throw new JsonParseException("End of file in JSON string.");
            }
        }
    }
    
    private enum NumberState
    {
        SAW_LEADING_MINUS, 
        SAW_LEADING_ZERO, 
        SAW_INTEGER_DIGITS, 
        SAW_DECIMAL_POINT, 
        SAW_FRACTION_DIGITS, 
        SAW_EXPONENT_LETTER, 
        SAW_EXPONENT_SIGN, 
        SAW_EXPONENT_DIGITS, 
        SAW_MINUS_I, 
        DONE, 
        INVALID;
    }
    
    private enum RegularExpressionState
    {
        IN_PATTERN, 
        IN_ESCAPE_SEQUENCE, 
        IN_OPTIONS, 
        DONE, 
        INVALID;
    }
}
