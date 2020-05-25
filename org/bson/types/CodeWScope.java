package org.bson.types;

import org.bson.*;

public class CodeWScope extends Code
{
    private final BSONObject scope;
    private static final long serialVersionUID = -6284832275113680002L;
    
    public CodeWScope(final String code, final BSONObject scope) {
        super(code);
        this.scope = scope;
    }
    
    public BSONObject getScope() {
        return this.scope;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        final CodeWScope c = (CodeWScope)o;
        return this.getCode().equals(c.getCode()) && this.scope.equals(c.scope);
    }
    
    @Override
    public int hashCode() {
        return this.getCode().hashCode() ^ this.scope.hashCode();
    }
}
