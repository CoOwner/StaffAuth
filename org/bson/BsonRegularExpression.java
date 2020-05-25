package org.bson;

import org.bson.assertions.*;
import java.util.*;

public final class BsonRegularExpression extends BsonValue
{
    private final String pattern;
    private final String options;
    
    public BsonRegularExpression(final String pattern, final String options) {
        this.pattern = Assertions.notNull("pattern", pattern);
        this.options = ((options == null) ? "" : this.sortOptionCharacters(options));
    }
    
    public BsonRegularExpression(final String pattern) {
        this(pattern, null);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.REGULAR_EXPRESSION;
    }
    
    public String getPattern() {
        return this.pattern;
    }
    
    public String getOptions() {
        return this.options;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonRegularExpression that = (BsonRegularExpression)o;
        return this.options.equals(that.options) && this.pattern.equals(that.pattern);
    }
    
    @Override
    public int hashCode() {
        int result = this.pattern.hashCode();
        result = 31 * result + this.options.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BsonRegularExpression{pattern='" + this.pattern + '\'' + ", options='" + this.options + '\'' + '}';
    }
    
    private String sortOptionCharacters(final String options) {
        final char[] chars = options.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
