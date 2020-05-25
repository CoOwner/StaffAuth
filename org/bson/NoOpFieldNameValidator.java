package org.bson;

class NoOpFieldNameValidator implements FieldNameValidator
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
