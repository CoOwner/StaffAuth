package com.mongodb.client.model;

import com.mongodb.assertions.*;

public class Field<TExpression>
{
    private final String name;
    private TExpression value;
    
    public Field(final String name, final TExpression value) {
        this.name = Assertions.notNull("name", name);
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public TExpression getValue() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        final Field<?> field = (Field<?>)o;
        return this.name.equals(field.name) && ((this.value != null) ? this.value.equals(field.value) : (field.value == null));
    }
    
    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + ((this.value != null) ? this.value.hashCode() : 0);
        return result;
    }
}
