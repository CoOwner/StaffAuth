package org.bson;

public final class BsonTimestamp extends BsonValue implements Comparable<BsonTimestamp>
{
    private final int seconds;
    private final int inc;
    
    public BsonTimestamp() {
        this.seconds = 0;
        this.inc = 0;
    }
    
    public BsonTimestamp(final int seconds, final int inc) {
        this.seconds = seconds;
        this.inc = inc;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.TIMESTAMP;
    }
    
    public int getTime() {
        return this.seconds;
    }
    
    public int getInc() {
        return this.inc;
    }
    
    @Override
    public String toString() {
        return "Timestamp{seconds=" + this.seconds + ", inc=" + this.inc + '}';
    }
    
    @Override
    public int compareTo(final BsonTimestamp ts) {
        if (this.getTime() != ts.getTime()) {
            return this.getTime() - ts.getTime();
        }
        return this.getInc() - ts.getInc();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonTimestamp timestamp = (BsonTimestamp)o;
        return this.seconds == timestamp.seconds && this.inc == timestamp.inc;
    }
    
    @Override
    public int hashCode() {
        int result = this.seconds;
        result = 31 * result + this.inc;
        return result;
    }
}
