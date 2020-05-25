package com.mongodb.connection;

import com.mongodb.*;
import org.bson.types.*;
import java.util.concurrent.*;
import java.text.*;
import com.mongodb.assertions.*;
import com.mongodb.annotations.*;
import java.util.*;

@Immutable
public class ServerDescription
{
    static final int MIN_DRIVER_WIRE_VERSION = 0;
    static final int MAX_DRIVER_WIRE_VERSION = 3;
    private static final int DEFAULT_MAX_DOCUMENT_SIZE = 16777216;
    private final ServerAddress address;
    private final ServerType type;
    private final String canonicalAddress;
    private final Set<String> hosts;
    private final Set<String> passives;
    private final Set<String> arbiters;
    private final String primary;
    private final int maxDocumentSize;
    private final TagSet tagSet;
    private final String setName;
    private final long roundTripTimeNanos;
    private final boolean ok;
    private final ServerConnectionState state;
    private final ServerVersion version;
    private final int minWireVersion;
    private final int maxWireVersion;
    private final ObjectId electionId;
    private final Integer setVersion;
    private final Date lastWriteDate;
    private final long lastUpdateTimeNanos;
    private final Throwable exception;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getCanonicalAddress() {
        return this.canonicalAddress;
    }
    
    public boolean isCompatibleWithDriver() {
        return !this.ok || (this.minWireVersion <= 3 && this.maxWireVersion >= 0);
    }
    
    public static int getDefaultMaxDocumentSize() {
        return 16777216;
    }
    
    public static int getDefaultMinWireVersion() {
        return 0;
    }
    
    public static int getDefaultMaxWireVersion() {
        return 0;
    }
    
    public ServerAddress getAddress() {
        return this.address;
    }
    
    public boolean isReplicaSetMember() {
        return this.type.getClusterType() == ClusterType.REPLICA_SET;
    }
    
    public boolean isShardRouter() {
        return this.type == ServerType.SHARD_ROUTER;
    }
    
    public boolean isStandAlone() {
        return this.type == ServerType.STANDALONE;
    }
    
    public boolean isPrimary() {
        return this.ok && (this.type == ServerType.REPLICA_SET_PRIMARY || this.type == ServerType.SHARD_ROUTER || this.type == ServerType.STANDALONE);
    }
    
    public boolean isSecondary() {
        return this.ok && (this.type == ServerType.REPLICA_SET_SECONDARY || this.type == ServerType.SHARD_ROUTER || this.type == ServerType.STANDALONE);
    }
    
    public Set<String> getHosts() {
        return this.hosts;
    }
    
    public Set<String> getPassives() {
        return this.passives;
    }
    
    public Set<String> getArbiters() {
        return this.arbiters;
    }
    
    public String getPrimary() {
        return this.primary;
    }
    
    public int getMaxDocumentSize() {
        return this.maxDocumentSize;
    }
    
    public TagSet getTagSet() {
        return this.tagSet;
    }
    
    public int getMinWireVersion() {
        return this.minWireVersion;
    }
    
    public int getMaxWireVersion() {
        return this.maxWireVersion;
    }
    
    public ObjectId getElectionId() {
        return this.electionId;
    }
    
    public Integer getSetVersion() {
        return this.setVersion;
    }
    
    public Date getLastWriteDate() {
        return this.lastWriteDate;
    }
    
    public long getLastUpdateTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.lastUpdateTimeNanos, TimeUnit.NANOSECONDS);
    }
    
    public boolean hasTags(final TagSet desiredTags) {
        return this.ok && (this.type == ServerType.STANDALONE || this.type == ServerType.SHARD_ROUTER || this.tagSet.containsAll(desiredTags));
    }
    
    public String getSetName() {
        return this.setName;
    }
    
    public boolean isOk() {
        return this.ok;
    }
    
    public ServerConnectionState getState() {
        return this.state;
    }
    
    public ServerType getType() {
        return this.type;
    }
    
    public ClusterType getClusterType() {
        return this.type.getClusterType();
    }
    
    public ServerVersion getVersion() {
        return this.version;
    }
    
    public long getRoundTripTimeNanos() {
        return this.roundTripTimeNanos;
    }
    
    public Throwable getException() {
        return this.exception;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerDescription that = (ServerDescription)o;
        if (this.maxDocumentSize != that.maxDocumentSize) {
            return false;
        }
        if (this.ok != that.ok) {
            return false;
        }
        if (!this.address.equals(that.address)) {
            return false;
        }
        if (!this.arbiters.equals(that.arbiters)) {
            return false;
        }
        Label_0122: {
            if (this.canonicalAddress != null) {
                if (this.canonicalAddress.equals(that.canonicalAddress)) {
                    break Label_0122;
                }
            }
            else if (that.canonicalAddress == null) {
                break Label_0122;
            }
            return false;
        }
        if (!this.hosts.equals(that.hosts)) {
            return false;
        }
        if (!this.passives.equals(that.passives)) {
            return false;
        }
        Label_0191: {
            if (this.primary != null) {
                if (this.primary.equals(that.primary)) {
                    break Label_0191;
                }
            }
            else if (that.primary == null) {
                break Label_0191;
            }
            return false;
        }
        Label_0224: {
            if (this.setName != null) {
                if (this.setName.equals(that.setName)) {
                    break Label_0224;
                }
            }
            else if (that.setName == null) {
                break Label_0224;
            }
            return false;
        }
        if (this.state != that.state) {
            return false;
        }
        if (!this.tagSet.equals(that.tagSet)) {
            return false;
        }
        if (this.type != that.type) {
            return false;
        }
        if (!this.version.equals(that.version)) {
            return false;
        }
        if (this.minWireVersion != that.minWireVersion) {
            return false;
        }
        if (this.maxWireVersion != that.maxWireVersion) {
            return false;
        }
        Label_0341: {
            if (this.electionId != null) {
                if (this.electionId.equals(that.electionId)) {
                    break Label_0341;
                }
            }
            else if (that.electionId == null) {
                break Label_0341;
            }
            return false;
        }
        Label_0374: {
            if (this.setVersion != null) {
                if (this.setVersion.equals(that.setVersion)) {
                    break Label_0374;
                }
            }
            else if (that.setVersion == null) {
                break Label_0374;
            }
            return false;
        }
        Label_0407: {
            if (this.lastWriteDate != null) {
                if (this.lastWriteDate.equals(that.lastWriteDate)) {
                    break Label_0407;
                }
            }
            else if (that.lastWriteDate == null) {
                break Label_0407;
            }
            return false;
        }
        if (this.lastUpdateTimeNanos != that.lastUpdateTimeNanos) {
            return false;
        }
        final Class<?> thisExceptionClass = (this.exception != null) ? this.exception.getClass() : null;
        final Class<?> thatExceptionClass = (that.exception != null) ? that.exception.getClass() : null;
        Label_0483: {
            if (thisExceptionClass != null) {
                if (thisExceptionClass.equals(thatExceptionClass)) {
                    break Label_0483;
                }
            }
            else if (thatExceptionClass == null) {
                break Label_0483;
            }
            return false;
        }
        final String thisExceptionMessage = (this.exception != null) ? this.exception.getMessage() : null;
        final String thatExceptionMessage = (that.exception != null) ? that.exception.getMessage() : null;
        if (thisExceptionMessage != null) {
            if (thisExceptionMessage.equals(thatExceptionMessage)) {
                return true;
            }
        }
        else if (thatExceptionMessage == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.address.hashCode();
        result = 31 * result + this.type.hashCode();
        result = 31 * result + ((this.canonicalAddress != null) ? this.canonicalAddress.hashCode() : 0);
        result = 31 * result + this.hosts.hashCode();
        result = 31 * result + this.passives.hashCode();
        result = 31 * result + this.arbiters.hashCode();
        result = 31 * result + ((this.primary != null) ? this.primary.hashCode() : 0);
        result = 31 * result + this.maxDocumentSize;
        result = 31 * result + this.tagSet.hashCode();
        result = 31 * result + ((this.setName != null) ? this.setName.hashCode() : 0);
        result = 31 * result + ((this.electionId != null) ? this.electionId.hashCode() : 0);
        result = 31 * result + ((this.setVersion != null) ? this.setVersion.hashCode() : 0);
        result = 31 * result + ((this.lastWriteDate != null) ? this.lastWriteDate.hashCode() : 0);
        result = 31 * result + (int)(this.lastUpdateTimeNanos ^ this.lastUpdateTimeNanos >>> 32);
        result = 31 * result + (this.ok ? 1 : 0);
        result = 31 * result + this.state.hashCode();
        result = 31 * result + this.version.hashCode();
        result = 31 * result + this.minWireVersion;
        result = 31 * result + this.maxWireVersion;
        result = 31 * result + ((this.exception == null) ? 0 : this.exception.getClass().hashCode());
        result = 31 * result + ((this.exception == null) ? 0 : this.exception.getMessage().hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "ServerDescription{address=" + this.address + ", type=" + this.type + ", state=" + this.state + ((this.state == ServerConnectionState.CONNECTED) ? (", ok=" + this.ok + ", version=" + this.version + ", minWireVersion=" + this.minWireVersion + ", maxWireVersion=" + this.maxWireVersion + ", maxDocumentSize=" + this.maxDocumentSize + ", roundTripTimeNanos=" + this.roundTripTimeNanos) : "") + (this.isReplicaSetMember() ? (", setName='" + this.setName + '\'' + ", canonicalAddress=" + this.canonicalAddress + ", hosts=" + this.hosts + ", passives=" + this.passives + ", arbiters=" + this.arbiters + ", primary='" + this.primary + '\'' + ", tagSet=" + this.tagSet + ", electionId=" + this.electionId + ", setVersion=" + this.setVersion + ", lastWriteDate=" + this.lastWriteDate + ", lastUpdateTimeNanos=" + this.lastUpdateTimeNanos) : "") + ((this.exception == null) ? "" : (", exception=" + this.translateExceptionToString())) + '}';
    }
    
    public String getShortDescription() {
        return "{address=" + this.address + ", type=" + this.type + (this.tagSet.iterator().hasNext() ? (", " + this.tagSet) : "") + ((this.state == ServerConnectionState.CONNECTED) ? (", roundTripTime=" + this.getRoundTripFormattedInMilliseconds() + " ms") : "") + ", state=" + this.state + ((this.exception == null) ? "" : (", exception=" + this.translateExceptionToString())) + '}';
    }
    
    private String translateExceptionToString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(this.exception);
        builder.append("}");
        for (Throwable cur = this.exception.getCause(); cur != null; cur = cur.getCause()) {
            builder.append(", caused by ");
            builder.append("{");
            builder.append(cur);
            builder.append("}");
        }
        return builder.toString();
    }
    
    private String getRoundTripFormattedInMilliseconds() {
        return new DecimalFormat("#0.0").format(this.roundTripTimeNanos / 1000.0 / 1000.0);
    }
    
    ServerDescription(final Builder builder) {
        this.address = Assertions.notNull("address", builder.address);
        this.type = Assertions.notNull("type", builder.type);
        this.state = Assertions.notNull("state", builder.state);
        this.version = Assertions.notNull("version", builder.version);
        this.canonicalAddress = builder.canonicalAddress;
        this.hosts = builder.hosts;
        this.passives = builder.passives;
        this.arbiters = builder.arbiters;
        this.primary = builder.primary;
        this.maxDocumentSize = builder.maxDocumentSize;
        this.tagSet = builder.tagSet;
        this.setName = builder.setName;
        this.roundTripTimeNanos = builder.roundTripTimeNanos;
        this.ok = builder.ok;
        this.minWireVersion = builder.minWireVersion;
        this.maxWireVersion = builder.maxWireVersion;
        this.electionId = builder.electionId;
        this.setVersion = builder.setVersion;
        this.lastWriteDate = builder.lastWriteDate;
        this.lastUpdateTimeNanos = builder.lastUpdateTimeNanos;
        this.exception = builder.exception;
    }
    
    @NotThreadSafe
    public static class Builder
    {
        private ServerAddress address;
        private ServerType type;
        private String canonicalAddress;
        private Set<String> hosts;
        private Set<String> passives;
        private Set<String> arbiters;
        private String primary;
        private int maxDocumentSize;
        private TagSet tagSet;
        private String setName;
        private long roundTripTimeNanos;
        private boolean ok;
        private ServerConnectionState state;
        private ServerVersion version;
        private int minWireVersion;
        private int maxWireVersion;
        private ObjectId electionId;
        private Integer setVersion;
        private Date lastWriteDate;
        private long lastUpdateTimeNanos;
        private Throwable exception;
        
        public Builder() {
            this.type = ServerType.UNKNOWN;
            this.hosts = Collections.emptySet();
            this.passives = Collections.emptySet();
            this.arbiters = Collections.emptySet();
            this.maxDocumentSize = 16777216;
            this.tagSet = new TagSet();
            this.version = new ServerVersion();
            this.minWireVersion = 0;
            this.maxWireVersion = 0;
            this.lastUpdateTimeNanos = Time.nanoTime();
        }
        
        public Builder address(final ServerAddress address) {
            this.address = address;
            return this;
        }
        
        public Builder canonicalAddress(final String canonicalAddress) {
            this.canonicalAddress = canonicalAddress;
            return this;
        }
        
        public Builder type(final ServerType type) {
            this.type = Assertions.notNull("type", type);
            return this;
        }
        
        public Builder hosts(final Set<String> hosts) {
            this.hosts = ((hosts == null) ? Collections.emptySet() : Collections.unmodifiableSet((Set<? extends String>)new HashSet<String>(hosts)));
            return this;
        }
        
        public Builder passives(final Set<String> passives) {
            this.passives = ((passives == null) ? Collections.emptySet() : Collections.unmodifiableSet((Set<? extends String>)new HashSet<String>(passives)));
            return this;
        }
        
        public Builder arbiters(final Set<String> arbiters) {
            this.arbiters = ((arbiters == null) ? Collections.emptySet() : Collections.unmodifiableSet((Set<? extends String>)new HashSet<String>(arbiters)));
            return this;
        }
        
        public Builder primary(final String primary) {
            this.primary = primary;
            return this;
        }
        
        public Builder maxDocumentSize(final int maxDocumentSize) {
            this.maxDocumentSize = maxDocumentSize;
            return this;
        }
        
        public Builder tagSet(final TagSet tagSet) {
            this.tagSet = ((tagSet == null) ? new TagSet() : tagSet);
            return this;
        }
        
        public Builder roundTripTime(final long roundTripTime, final TimeUnit timeUnit) {
            this.roundTripTimeNanos = timeUnit.toNanos(roundTripTime);
            return this;
        }
        
        public Builder setName(final String setName) {
            this.setName = setName;
            return this;
        }
        
        public Builder ok(final boolean ok) {
            this.ok = ok;
            return this;
        }
        
        public Builder state(final ServerConnectionState state) {
            this.state = state;
            return this;
        }
        
        public Builder version(final ServerVersion version) {
            Assertions.notNull("version", version);
            this.version = version;
            return this;
        }
        
        public Builder minWireVersion(final int minWireVersion) {
            this.minWireVersion = minWireVersion;
            return this;
        }
        
        public Builder maxWireVersion(final int maxWireVersion) {
            this.maxWireVersion = maxWireVersion;
            return this;
        }
        
        public Builder electionId(final ObjectId electionId) {
            this.electionId = electionId;
            return this;
        }
        
        public Builder setVersion(final Integer setVersion) {
            this.setVersion = setVersion;
            return this;
        }
        
        public Builder lastWriteDate(final Date lastWriteDate) {
            this.lastWriteDate = lastWriteDate;
            return this;
        }
        
        public Builder lastUpdateTimeNanos(final long lastUpdateTimeNanos) {
            this.lastUpdateTimeNanos = lastUpdateTimeNanos;
            return this;
        }
        
        public Builder exception(final Throwable exception) {
            this.exception = exception;
            return this;
        }
        
        public ServerDescription build() {
            return new ServerDescription(this);
        }
    }
}
