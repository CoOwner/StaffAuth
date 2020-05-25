package com.mongodb.internal.validator;

import org.bson.*;

public class UpdateFieldNameValidator implements FieldNameValidator
{
    @Override
    public boolean validate(final String fieldName) {
        return fieldName.startsWith("$");
    }
    
    @Override
    public FieldNameValidator getValidatorForField(final String fieldName) {
        return new NoOpFieldNameValidator();
    }
}
