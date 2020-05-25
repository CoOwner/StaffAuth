package com.mongodb.internal.validator;

import org.bson.*;

public class NoOpFieldNameValidator implements FieldNameValidator
{
    @Override
    public boolean validate(final String fieldName) {
        return true;
    }
    
    @Override
    public FieldNameValidator getValidatorForField(final String fieldName) {
        return this;
    }
}
