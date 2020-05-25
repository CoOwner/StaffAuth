package org.bson.types;

import java.io.*;
import java.util.*;

public final class BSONTimestamp implements Comparable<BSONTimestamp>, Serializable
{
    private static final long serialVersionUID = -3268482672267936464L;
    private final int inc;
    private final Date time;
    
    public BSONTimestamp() {
        this.inc = 0;
        this.time = null;
    }
    
    public BSONTimestamp(final int time, final int increment) {
        this.time = new Date(time * 1000L);
        this.inc = increment;
    }
    
    public int getTime() {
        if (this.time == null) {
            return 0;
        }
        return (int)(this.time.getTime() / 1000L);
    }
    
    public int getInc() {
        return this.inc;
    }
    
    @Override
    public String toString() {
        return "TS time:" + this.time + " inc:" + this.inc;
    }
    
    @Override
    public int compareTo(final BSONTimestamp ts) {
        if (this.getTime() != ts.getTime()) {
            return this.getTime() - ts.getTime();
        }
        return this.getInc() - ts.getInc();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.inc;
        result = prime * result + this.getTime();
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BSONTimestamp) {
            final BSONTimestamp t2 = (BSONTimestamp)obj;
            return this.getTime() == t2.getTime() && this.getInc() == t2.getInc();
        }
        return false;
    }
}
