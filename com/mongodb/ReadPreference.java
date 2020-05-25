package com.mongodb;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import java.util.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import org.bson.*;

@Immutable
public abstract class ReadPreference
{
    private static final ReadPreference PRIMARY;
    private static final ReadPreference SECONDARY;
    private static final ReadPreference SECONDARY_PREFERRED;
    private static final ReadPreference PRIMARY_PREFERRED;
    private static final ReadPreference NEAREST;
    
    ReadPreference() {
    }
    
    public abstract boolean isSlaveOk();
    
    public abstract String getName();
    
    public abstract BsonDocument toDocument();
    
    public final List<ServerDescription> choose(final ClusterDescription clusterDescription) {
        switch (clusterDescription.getType()) {
            case REPLICA_SET: {
                return this.chooseForReplicaSet(clusterDescription);
            }
            case SHARDED:
            case STANDALONE: {
                return this.chooseForNonReplicaSet(clusterDescription);
            }
            case UNKNOWN: {
                return Collections.emptyList();
            }
            default: {
                throw new UnsupportedOperationException("Unsupported cluster type: " + clusterDescription.getType());
            }
        }
    }
    
    protected abstract List<ServerDescription> chooseForNonReplicaSet(final ClusterDescription p0);
    
    protected abstract List<ServerDescription> chooseForReplicaSet(final ClusterDescription p0);
    
    public static ReadPreference primary() {
        return ReadPreference.PRIMARY;
    }
    
    public static ReadPreference primaryPreferred() {
        return ReadPreference.PRIMARY_PREFERRED;
    }
    
    public static ReadPreference secondary() {
        return ReadPreference.SECONDARY;
    }
    
    public static ReadPreference secondaryPreferred() {
        return ReadPreference.SECONDARY_PREFERRED;
    }
    
    public static ReadPreference nearest() {
        return ReadPreference.NEAREST;
    }
    
    public static ReadPreference primaryPreferred(final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(Collections.emptyList(), maxStaleness, timeUnit);
    }
    
    public static ReadPreference secondary(final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryReadPreference(Collections.emptyList(), maxStaleness, timeUnit);
    }
    
    public static ReadPreference secondaryPreferred(final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(Collections.emptyList(), maxStaleness, timeUnit);
    }
    
    public static ReadPreference nearest(final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.NearestReadPreference(Collections.emptyList(), maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference primaryPreferred(final TagSet tagSet) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(Collections.singletonList(tagSet), null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference secondary(final TagSet tagSet) {
        return new TaggableReadPreference.SecondaryReadPreference(Collections.singletonList(tagSet), null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference secondaryPreferred(final TagSet tagSet) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(Collections.singletonList(tagSet), null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference nearest(final TagSet tagSet) {
        return new TaggableReadPreference.NearestReadPreference(Collections.singletonList(tagSet), null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference primaryPreferred(final TagSet tagSet, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(Collections.singletonList(tagSet), maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference secondary(final TagSet tagSet, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryReadPreference(Collections.singletonList(tagSet), maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference secondaryPreferred(final TagSet tagSet, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(Collections.singletonList(tagSet), maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference nearest(final TagSet tagSet, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.NearestReadPreference(Collections.singletonList(tagSet), maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference primaryPreferred(final List<TagSet> tagSetList) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(tagSetList, null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference secondary(final List<TagSet> tagSetList) {
        return new TaggableReadPreference.SecondaryReadPreference(tagSetList, null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference secondaryPreferred(final List<TagSet> tagSetList) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(tagSetList, null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference nearest(final List<TagSet> tagSetList) {
        return new TaggableReadPreference.NearestReadPreference(tagSetList, null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference primaryPreferred(final List<TagSet> tagSetList, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(tagSetList, maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference secondary(final List<TagSet> tagSetList, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryReadPreference(tagSetList, maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference secondaryPreferred(final List<TagSet> tagSetList, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(tagSetList, maxStaleness, timeUnit);
    }
    
    public static TaggableReadPreference nearest(final List<TagSet> tagSetList, final long maxStaleness, final TimeUnit timeUnit) {
        return new TaggableReadPreference.NearestReadPreference(tagSetList, maxStaleness, timeUnit);
    }
    
    public static ReadPreference valueOf(final String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        final String nameToCheck = name.toLowerCase();
        if (nameToCheck.equals(ReadPreference.PRIMARY.getName().toLowerCase())) {
            return ReadPreference.PRIMARY;
        }
        if (nameToCheck.equals(ReadPreference.SECONDARY.getName().toLowerCase())) {
            return ReadPreference.SECONDARY;
        }
        if (nameToCheck.equals(ReadPreference.SECONDARY_PREFERRED.getName().toLowerCase())) {
            return ReadPreference.SECONDARY_PREFERRED;
        }
        if (nameToCheck.equals(ReadPreference.PRIMARY_PREFERRED.getName().toLowerCase())) {
            return ReadPreference.PRIMARY_PREFERRED;
        }
        if (nameToCheck.equals(ReadPreference.NEAREST.getName().toLowerCase())) {
            return ReadPreference.NEAREST;
        }
        throw new IllegalArgumentException("No match for read preference of " + name);
    }
    
    public static TaggableReadPreference valueOf(final String name, final List<TagSet> tagSetList) {
        return valueOf(name, tagSetList, null, TimeUnit.MILLISECONDS);
    }
    
    public static TaggableReadPreference valueOf(final String name, final List<TagSet> tagSetList, final long maxStaleness, final TimeUnit timeUnit) {
        return valueOf(name, tagSetList, Long.valueOf(maxStaleness), timeUnit);
    }
    
    private static TaggableReadPreference valueOf(final String name, final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
        Assertions.notNull("name", name);
        Assertions.notNull("tagSetList", tagSetList);
        Assertions.notNull("timeUnit", timeUnit);
        final String nameToCheck = name.toLowerCase();
        if (nameToCheck.equals(ReadPreference.PRIMARY.getName().toLowerCase())) {
            throw new IllegalArgumentException("Primary read preference can not also specify tag sets or max staleness");
        }
        if (nameToCheck.equals(ReadPreference.SECONDARY.getName().toLowerCase())) {
            return new TaggableReadPreference.SecondaryReadPreference(tagSetList, maxStaleness, timeUnit);
        }
        if (nameToCheck.equals(ReadPreference.SECONDARY_PREFERRED.getName().toLowerCase())) {
            return new TaggableReadPreference.SecondaryPreferredReadPreference(tagSetList, maxStaleness, timeUnit);
        }
        if (nameToCheck.equals(ReadPreference.PRIMARY_PREFERRED.getName().toLowerCase())) {
            return new TaggableReadPreference.PrimaryPreferredReadPreference(tagSetList, maxStaleness, timeUnit);
        }
        if (nameToCheck.equals(ReadPreference.NEAREST.getName().toLowerCase())) {
            return new TaggableReadPreference.NearestReadPreference(tagSetList, maxStaleness, timeUnit);
        }
        throw new IllegalArgumentException("No match for read preference of " + name);
    }
    
    static {
        PRIMARY = new PrimaryReadPreference();
        SECONDARY = new TaggableReadPreference.SecondaryReadPreference();
        SECONDARY_PREFERRED = new TaggableReadPreference.SecondaryPreferredReadPreference();
        PRIMARY_PREFERRED = new TaggableReadPreference.PrimaryPreferredReadPreference();
        NEAREST = new TaggableReadPreference.NearestReadPreference();
    }
    
    private static final class PrimaryReadPreference extends ReadPreference
    {
        @Override
        public boolean isSlaveOk() {
            return false;
        }
        
        @Override
        public String toString() {
            return this.getName();
        }
        
        @Override
        public boolean equals(final Object o) {
            return o != null && this.getClass() == o.getClass();
        }
        
        @Override
        public int hashCode() {
            return this.getName().hashCode();
        }
        
        @Override
        public BsonDocument toDocument() {
            return new BsonDocument("mode", new BsonString(this.getName()));
        }
        
        @Override
        protected List<ServerDescription> chooseForReplicaSet(final ClusterDescription clusterDescription) {
            return clusterDescription.getPrimaries();
        }
        
        @Override
        protected List<ServerDescription> chooseForNonReplicaSet(final ClusterDescription clusterDescription) {
            return clusterDescription.getAny();
        }
        
        @Override
        public String getName() {
            return "primary";
        }
    }
}
