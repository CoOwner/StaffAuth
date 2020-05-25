package com.mongodb;

import java.io.*;
import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import org.bson.*;
import java.util.*;
import java.lang.reflect.*;

@Immutable
public class WriteConcern implements Serializable
{
    private static final long serialVersionUID = 1884671104750417011L;
    private static final Map<String, WriteConcern> NAMED_CONCERNS;
    private final Object w;
    private final Integer wTimeoutMS;
    private final Boolean fsync;
    private final Boolean journal;
    public static final WriteConcern ACKNOWLEDGED;
    public static final WriteConcern W1;
    public static final WriteConcern W2;
    public static final WriteConcern W3;
    public static final WriteConcern UNACKNOWLEDGED;
    @Deprecated
    public static final WriteConcern FSYNCED;
    public static final WriteConcern JOURNALED;
    @Deprecated
    public static final WriteConcern REPLICA_ACKNOWLEDGED;
    @Deprecated
    public static final WriteConcern NORMAL;
    @Deprecated
    public static final WriteConcern SAFE;
    public static final WriteConcern MAJORITY;
    @Deprecated
    public static final WriteConcern FSYNC_SAFE;
    @Deprecated
    public static final WriteConcern JOURNAL_SAFE;
    @Deprecated
    public static final WriteConcern REPLICAS_SAFE;
    
    @Deprecated
    public WriteConcern() {
        this(0);
    }
    
    public WriteConcern(final int w) {
        this(w, null, null, null);
    }
    
    public WriteConcern(final String w) {
        this(w, null, null, null);
        Assertions.isTrueArgument("w != null", w != null);
    }
    
    public WriteConcern(final int w, final int wTimeoutMS) {
        this(w, wTimeoutMS, null, null);
    }
    
    @Deprecated
    public WriteConcern(final boolean fsync) {
        this(null, null, fsync, null);
    }
    
    @Deprecated
    public WriteConcern(final int w, final int wTimeoutMS, final boolean fsync) {
        this(w, wTimeoutMS, fsync, null);
    }
    
    @Deprecated
    public WriteConcern(final int w, final int wTimeoutMS, final boolean fsync, final boolean journal) {
        this((Object)w, wTimeoutMS, fsync, journal);
    }
    
    @Deprecated
    public WriteConcern(final String w, final int wTimeoutMS, final boolean fsync, final boolean journal) {
        this((Object)Assertions.notNull("w", w), wTimeoutMS, fsync, journal);
    }
    
    private WriteConcern(final Object w, final Integer wTimeoutMS, final Boolean fsync, final Boolean journal) {
        if (w instanceof Integer) {
            Assertions.isTrueArgument("w >= 0", (int)w >= 0);
        }
        else if (w != null) {
            Assertions.isTrueArgument("w must be String or int", w instanceof String);
        }
        Assertions.isTrueArgument("wtimeout >= 0", wTimeoutMS == null || wTimeoutMS >= 0);
        this.w = w;
        this.wTimeoutMS = wTimeoutMS;
        this.fsync = fsync;
        this.journal = journal;
    }
    
    public Object getWObject() {
        return this.w;
    }
    
    public int getW() {
        Assertions.isTrue("w is an Integer", this.w != null && this.w instanceof Integer);
        return (int)this.w;
    }
    
    public String getWString() {
        Assertions.isTrue("w is a String", this.w != null && this.w instanceof String);
        return (String)this.w;
    }
    
    public Integer getWTimeout(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return (this.wTimeoutMS == null) ? null : Integer.valueOf((int)timeUnit.convert(this.wTimeoutMS, TimeUnit.MILLISECONDS));
    }
    
    @Deprecated
    public int getWtimeout() {
        return (this.wTimeoutMS == null) ? 0 : this.wTimeoutMS;
    }
    
    public Boolean getJournal() {
        return this.journal;
    }
    
    @Deprecated
    public boolean getJ() {
        return this.journal != null && this.journal;
    }
    
    @Deprecated
    public boolean getFsync() {
        return this.fsync != null && this.fsync;
    }
    
    @Deprecated
    public boolean fsync() {
        return this.getFsync();
    }
    
    @Deprecated
    public boolean callGetLastError() {
        return this.isAcknowledged();
    }
    
    public boolean isServerDefault() {
        return this.equals(WriteConcern.ACKNOWLEDGED);
    }
    
    public BsonDocument asDocument() {
        final BsonDocument document = new BsonDocument();
        this.addW(document);
        this.addWTimeout(document);
        this.addFSync(document);
        this.addJ(document);
        return document;
    }
    
    public boolean isAcknowledged() {
        return !(this.w instanceof Integer) || (int)this.w > 0 || (this.journal != null && this.journal) || (this.fsync != null && this.fsync);
    }
    
    public static WriteConcern valueOf(final String name) {
        return WriteConcern.NAMED_CONCERNS.get(name.toLowerCase());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final WriteConcern that = (WriteConcern)o;
        Label_0062: {
            if (this.w != null) {
                if (this.w.equals(that.w)) {
                    break Label_0062;
                }
            }
            else if (that.w == null) {
                break Label_0062;
            }
            return false;
        }
        Label_0095: {
            if (this.wTimeoutMS != null) {
                if (this.wTimeoutMS.equals(that.wTimeoutMS)) {
                    break Label_0095;
                }
            }
            else if (that.wTimeoutMS == null) {
                break Label_0095;
            }
            return false;
        }
        Label_0128: {
            if (this.fsync != null) {
                if (this.fsync.equals(that.fsync)) {
                    break Label_0128;
                }
            }
            else if (that.fsync == null) {
                break Label_0128;
            }
            return false;
        }
        if (this.journal != null) {
            if (this.journal.equals(that.journal)) {
                return true;
            }
        }
        else if (that.journal == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.w != null) ? this.w.hashCode() : 0;
        result = 31 * result + ((this.wTimeoutMS != null) ? this.wTimeoutMS.hashCode() : 0);
        result = 31 * result + ((this.fsync != null) ? this.fsync.hashCode() : 0);
        result = 31 * result + ((this.journal != null) ? this.journal.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "WriteConcern{w=" + this.w + ", wTimeout=" + this.wTimeoutMS + " ms, fsync=" + this.fsync + ", journal=" + this.journal;
    }
    
    public WriteConcern withW(final int w) {
        return new WriteConcern((Object)w, this.wTimeoutMS, this.fsync, this.journal);
    }
    
    public WriteConcern withW(final String w) {
        Assertions.notNull("w", w);
        return new WriteConcern((Object)w, this.wTimeoutMS, this.fsync, this.journal);
    }
    
    @Deprecated
    public WriteConcern withFsync(final boolean fsync) {
        return new WriteConcern(this.w, this.wTimeoutMS, fsync, this.journal);
    }
    
    public WriteConcern withJournal(final Boolean journal) {
        return new WriteConcern(this.w, this.wTimeoutMS, this.fsync, journal);
    }
    
    @Deprecated
    public WriteConcern withJ(final boolean journal) {
        return this.withJournal(journal);
    }
    
    public WriteConcern withWTimeout(final long wTimeout, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        final long newWTimeOutMS = TimeUnit.MILLISECONDS.convert(wTimeout, timeUnit);
        Assertions.isTrueArgument("wTimeout >= 0", wTimeout >= 0L);
        Assertions.isTrueArgument("wTimeout <= 2147483647 ms", newWTimeOutMS <= 2147483647L);
        return new WriteConcern(this.w, (int)newWTimeOutMS, this.fsync, this.journal);
    }
    
    private void addW(final BsonDocument document) {
        if (this.w instanceof String) {
            document.put("w", new BsonString((String)this.w));
        }
        else if (this.w instanceof Integer) {
            document.put("w", new BsonInt32((int)this.w));
        }
    }
    
    private void addJ(final BsonDocument document) {
        if (this.journal != null) {
            document.put("j", BsonBoolean.valueOf(this.journal));
        }
    }
    
    private void addFSync(final BsonDocument document) {
        if (this.fsync != null) {
            document.put("fsync", BsonBoolean.valueOf(this.fsync));
        }
    }
    
    private void addWTimeout(final BsonDocument document) {
        if (this.wTimeoutMS != null) {
            document.put("wtimeout", new BsonInt32(this.wTimeoutMS));
        }
    }
    
    @Deprecated
    public static Majority majorityWriteConcern(final int wtimeout, final boolean fsync, final boolean j) {
        return new Majority(wtimeout, fsync, j);
    }
    
    static {
        ACKNOWLEDGED = new WriteConcern(null, null, null, null);
        W1 = new WriteConcern(1);
        W2 = new WriteConcern(2);
        W3 = new WriteConcern(3);
        UNACKNOWLEDGED = new WriteConcern(0);
        FSYNCED = WriteConcern.ACKNOWLEDGED.withFsync(true);
        JOURNALED = WriteConcern.ACKNOWLEDGED.withJournal(true);
        REPLICA_ACKNOWLEDGED = new WriteConcern(2);
        NORMAL = WriteConcern.UNACKNOWLEDGED;
        SAFE = WriteConcern.ACKNOWLEDGED;
        MAJORITY = new WriteConcern("majority");
        FSYNC_SAFE = WriteConcern.FSYNCED;
        JOURNAL_SAFE = WriteConcern.JOURNALED;
        REPLICAS_SAFE = WriteConcern.REPLICA_ACKNOWLEDGED;
        NAMED_CONCERNS = new HashMap<String, WriteConcern>();
        for (final Field f : WriteConcern.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(WriteConcern.class)) {
                final String key = f.getName().toLowerCase();
                try {
                    WriteConcern.NAMED_CONCERNS.put(key, (WriteConcern)f.get(null));
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    @Deprecated
    public static class Majority extends WriteConcern
    {
        private static final long serialVersionUID = -4128295115883875212L;
        
        public Majority() {
            this(0, false, false);
        }
        
        public Majority(final int wtimeout, final boolean fsync, final boolean j) {
            super("majority", wtimeout, fsync, j);
        }
    }
}
