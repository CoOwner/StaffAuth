package com.mongodb.connection;

import com.mongodb.assertions.*;

final class ChangeEvent<T>
{
    private final T previousValue;
    private final T newValue;
    
    public ChangeEvent(final T previousValue, final T newValue) {
        this.previousValue = Assertions.notNull("oldValue", previousValue);
        this.newValue = Assertions.notNull("newValue", newValue);
    }
    
    public T getPreviousValue() {
        return this.previousValue;
    }
    
    public T getNewValue() {
        return this.newValue;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ChangeEvent<?> that = (ChangeEvent<?>)o;
        if (!this.newValue.equals(that.newValue)) {
            return false;
        }
        if (this.previousValue != null) {
            if (this.previousValue.equals(that.previousValue)) {
                return true;
            }
        }
        else if (that.previousValue == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.previousValue != null) ? this.previousValue.hashCode() : 0;
        result = 31 * result + this.newValue.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "ChangeEvent{previousValue=" + this.previousValue + ", newValue=" + this.newValue + '}';
    }
}
