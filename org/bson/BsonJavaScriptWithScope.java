package org.bson;

public class BsonJavaScriptWithScope extends BsonValue
{
    private final String code;
    private final BsonDocument scope;
    
    public BsonJavaScriptWithScope(final String code, final BsonDocument scope) {
        if (code == null) {
            throw new IllegalArgumentException("code can not be null");
        }
        if (scope == null) {
            throw new IllegalArgumentException("scope can not be null");
        }
        this.code = code;
        this.scope = scope;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.JAVASCRIPT_WITH_SCOPE;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public BsonDocument getScope() {
        return this.scope;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonJavaScriptWithScope that = (BsonJavaScriptWithScope)o;
        return this.code.equals(that.code) && this.scope.equals(that.scope);
    }
    
    @Override
    public int hashCode() {
        int result = this.code.hashCode();
        result = 31 * result + this.scope.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BsonJavaScriptWithScope{code=" + this.getCode() + "scope=" + this.scope + '}';
    }
    
    static BsonJavaScriptWithScope clone(final BsonJavaScriptWithScope from) {
        return new BsonJavaScriptWithScope(from.code, from.scope.clone());
    }
}
