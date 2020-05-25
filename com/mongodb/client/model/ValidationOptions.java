package com.mongodb.client.model;

import org.bson.conversions.*;

public final class ValidationOptions
{
    private Bson validator;
    private ValidationLevel validationLevel;
    private ValidationAction validationAction;
    
    public Bson getValidator() {
        return this.validator;
    }
    
    public ValidationOptions validator(final Bson validator) {
        this.validator = validator;
        return this;
    }
    
    public ValidationLevel getValidationLevel() {
        return this.validationLevel;
    }
    
    public ValidationOptions validationLevel(final ValidationLevel validationLevel) {
        this.validationLevel = validationLevel;
        return this;
    }
    
    public ValidationAction getValidationAction() {
        return this.validationAction;
    }
    
    public ValidationOptions validationAction(final ValidationAction validationAction) {
        this.validationAction = validationAction;
        return this;
    }
}
