package org.bson.types;

import org.bson.*;

public class CodeWithScope extends Code
{
    private final Document scope;
    private static final long serialVersionUID = -6284832275113680002L;
    
    public CodeWithScope(final String code, final Document scope) {
        super(code);
        this.scope = scope;
    }
    
    public Document getScope() {
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
        if (!super.equals(o)) {
            return false;
        }
        final CodeWithScope that = (CodeWithScope)o;
        if (this.scope != null) {
            if (this.scope.equals(that.scope)) {
                return true;
            }
        }
        else if (that.scope == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.getCode().hashCode() ^ this.scope.hashCode();
    }
}
