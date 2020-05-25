package org.bson;

public class BSONException extends RuntimeException
{
    private static final long serialVersionUID = -4415279469780082174L;
    private Integer errorCode;
    
    public BSONException(final String msg) {
        super(msg);
        this.errorCode = null;
    }
    
    public BSONException(final int errorCode, final String msg) {
        super(msg);
        this.errorCode = null;
        this.errorCode = errorCode;
    }
    
    public BSONException(final String msg, final Throwable t) {
        super(msg, t);
        this.errorCode = null;
    }
    
    public BSONException(final int errorCode, final String msg, final Throwable t) {
        super(msg, t);
        this.errorCode = null;
        this.errorCode = errorCode;
    }
    
    public Integer getErrorCode() {
        return this.errorCode;
    }
    
    public boolean hasErrorCode() {
        return this.errorCode != null;
    }
}
