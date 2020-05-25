package com.mongodb;

import org.bson.*;
import java.nio.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.util.regex.*;
import org.bson.types.*;

public class Bytes extends BSON
{
    public static final ByteOrder ORDER;
    public static final int QUERYOPTION_TAILABLE = 2;
    public static final int QUERYOPTION_SLAVEOK = 4;
    public static final int QUERYOPTION_OPLOGREPLAY = 8;
    public static final int QUERYOPTION_NOTIMEOUT = 16;
    public static final int QUERYOPTION_AWAITDATA = 32;
    public static final int QUERYOPTION_EXHAUST = 64;
    public static final int QUERYOPTION_PARTIAL = 128;
    public static final int RESULTFLAG_CURSORNOTFOUND = 1;
    public static final int RESULTFLAG_ERRSET = 2;
    public static final int RESULTFLAG_SHARDCONFIGSTALE = 4;
    public static final int RESULTFLAG_AWAITCAPABLE = 8;
    
    public static byte getType(final Object object) {
        if (object == null) {
            return 10;
        }
        if (object instanceof Integer || object instanceof Short || object instanceof Byte || object instanceof AtomicInteger) {
            return 16;
        }
        if (object instanceof Long || object instanceof AtomicLong) {
            return 18;
        }
        if (object instanceof Number) {
            return 1;
        }
        if (object instanceof String) {
            return 2;
        }
        if (object instanceof List) {
            return 4;
        }
        if (object instanceof byte[]) {
            return 5;
        }
        if (object instanceof ObjectId) {
            return 7;
        }
        if (object instanceof Boolean) {
            return 8;
        }
        if (object instanceof Date) {
            return 9;
        }
        if (object instanceof BSONTimestamp) {
            return 17;
        }
        if (object instanceof Pattern) {
            return 11;
        }
        if (object instanceof DBObject || object instanceof DBRef) {
            return 3;
        }
        if (object instanceof CodeWScope) {
            return 15;
        }
        if (object instanceof Code) {
            return 13;
        }
        return -1;
    }
    
    static {
        ORDER = ByteOrder.LITTLE_ENDIAN;
    }
    
    static class OptionHolder
    {
        final OptionHolder _parent;
        int _options;
        boolean _hasOptions;
        
        OptionHolder(final OptionHolder parent) {
            this._options = 0;
            this._hasOptions = false;
            this._parent = parent;
        }
        
        void set(final int options) {
            this._options = options;
            this._hasOptions = true;
        }
        
        int get() {
            if (this._hasOptions) {
                return this._options;
            }
            if (this._parent == null) {
                return 0;
            }
            return this._parent.get();
        }
        
        void add(final int option) {
            this.set(this.get() | option);
        }
        
        void reset() {
            this._hasOptions = false;
        }
    }
}
