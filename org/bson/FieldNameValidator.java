package org.bson;

public interface FieldNameValidator
{
    boolean validate(final String p0);
    
    FieldNameValidator getValidatorForField(final String p0);
}
