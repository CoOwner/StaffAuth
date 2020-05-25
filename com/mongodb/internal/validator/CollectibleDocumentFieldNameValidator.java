package com.mongodb.internal.validator;

import org.bson.*;
import java.util.*;

public class CollectibleDocumentFieldNameValidator implements FieldNameValidator
{
    private static final List<String> EXCEPTIONS;
    
    @Override
    public boolean validate(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name can not be null");
        }
        return !fieldName.contains(".") && (!fieldName.startsWith("$") || CollectibleDocumentFieldNameValidator.EXCEPTIONS.contains(fieldName));
    }
    
    @Override
    public FieldNameValidator getValidatorForField(final String fieldName) {
        return this;
    }
    
    static {
        EXCEPTIONS = Arrays.asList("$db", "$ref", "$id");
    }
}
