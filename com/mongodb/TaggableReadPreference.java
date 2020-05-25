package com.mongodb;

import com.mongodb.annotations.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.connection.*;
import org.bson.*;

@Immutable
public abstract class TaggableReadPreference extends ReadPreference
{
    private static final int SMALLEST_MAX_STALENESS_MS = 90000;
    private static final int IDLE_WRITE_PERIOD_MS = 10000;
    private final List<TagSet> tagSetList;
    private final Long maxStalenessMS;
    
    TaggableReadPreference() {
        this.tagSetList = new ArrayList<TagSet>();
        this.maxStalenessMS = null;
    }
    
    TaggableReadPreference(final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
        this.tagSetList = new ArrayList<TagSet>();
        Assertions.notNull("tagSetList", tagSetList);
        Assertions.isTrueArgument("maxStaleness is null or >= 0", maxStaleness == null || maxStaleness >= 0L);
        this.maxStalenessMS = ((maxStaleness == null) ? null : Long.valueOf(TimeUnit.MILLISECONDS.convert(maxStaleness, timeUnit)));
        for (final TagSet tagSet : tagSetList) {
            this.tagSetList.add(tagSet);
        }
    }
    
    @Override
    public boolean isSlaveOk() {
        return true;
    }
    
    @Override
    public BsonDocument toDocument() {
        final BsonDocument readPrefObject = new BsonDocument("mode", new BsonString(this.getName()));
        if (!this.tagSetList.isEmpty()) {
            readPrefObject.put("tags", this.tagsListToBsonArray());
        }
        if (this.maxStalenessMS != null) {
            readPrefObject.put("maxStalenessSeconds", new BsonInt64(TimeUnit.MILLISECONDS.toSeconds(this.maxStalenessMS)));
        }
        return readPrefObject;
    }
    
    public List<TagSet> getTagSetList() {
        return Collections.unmodifiableList((List<? extends TagSet>)this.tagSetList);
    }
    
    public Long getMaxStaleness(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        if (this.maxStalenessMS == null) {
            return null;
        }
        return timeUnit.convert(this.maxStalenessMS, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString() {
        return "ReadPreference{name=" + this.getName() + (this.tagSetList.isEmpty() ? "" : (", tagSetList=" + this.tagSetList)) + ((this.maxStalenessMS == null) ? "" : (", maxStalenessMS=" + this.maxStalenessMS)) + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final TaggableReadPreference that = (TaggableReadPreference)o;
        if (this.maxStalenessMS != null) {
            if (this.maxStalenessMS.equals(that.maxStalenessMS)) {
                return this.tagSetList.equals(that.tagSetList);
            }
        }
        else if (that.maxStalenessMS == null) {
            return this.tagSetList.equals(that.tagSetList);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.tagSetList.hashCode();
        result = 31 * result + this.getName().hashCode();
        result = 31 * result + ((this.maxStalenessMS != null) ? this.maxStalenessMS.hashCode() : 0);
        return result;
    }
    
    @Override
    protected List<ServerDescription> chooseForNonReplicaSet(final ClusterDescription clusterDescription) {
        return this.selectFreshServers(clusterDescription, clusterDescription.getAny());
    }
    
    protected static ClusterDescription copyClusterDescription(final ClusterDescription clusterDescription, final List<ServerDescription> selectedServers) {
        return new ClusterDescription(clusterDescription.getConnectionMode(), clusterDescription.getType(), selectedServers, clusterDescription.getClusterSettings(), clusterDescription.getServerSettings());
    }
    
    protected List<ServerDescription> selectFreshServers(final ClusterDescription clusterDescription, final List<ServerDescription> servers) {
        if (this.getMaxStaleness(TimeUnit.MILLISECONDS) == null) {
            return servers;
        }
        if (clusterDescription.getServerSettings() == null) {
            throw new MongoConfigurationException("heartbeat frequency must be provided in cluster description");
        }
        if (!this.serversAreAllThreeDotFour(clusterDescription)) {
            throw new MongoConfigurationException("Servers must all be at least version 3.4 when max staleness is configured");
        }
        if (clusterDescription.getType() != ClusterType.REPLICA_SET) {
            return servers;
        }
        final long heartbeatFrequencyMS = clusterDescription.getServerSettings().getHeartbeatFrequency(TimeUnit.MILLISECONDS);
        if (this.getMaxStaleness(TimeUnit.MILLISECONDS) >= Math.max(90000L, heartbeatFrequencyMS + 10000L)) {
            final List<ServerDescription> freshServers = new ArrayList<ServerDescription>(servers.size());
            final ServerDescription primary = this.findPrimary(clusterDescription);
            if (primary != null) {
                for (final ServerDescription cur : servers) {
                    if (cur.isPrimary()) {
                        freshServers.add(cur);
                    }
                    else {
                        if (this.getStalenessOfSecondaryRelativeToPrimary(primary, cur, heartbeatFrequencyMS) > this.getMaxStaleness(TimeUnit.MILLISECONDS)) {
                            continue;
                        }
                        freshServers.add(cur);
                    }
                }
            }
            else {
                final ServerDescription mostUpdateToDateSecondary = this.findMostUpToDateSecondary(clusterDescription);
                for (final ServerDescription cur2 : servers) {
                    if (mostUpdateToDateSecondary.getLastWriteDate().getTime() - cur2.getLastWriteDate().getTime() + heartbeatFrequencyMS <= this.getMaxStaleness(TimeUnit.MILLISECONDS)) {
                        freshServers.add(cur2);
                    }
                }
            }
            return freshServers;
        }
        if (90000L > heartbeatFrequencyMS + 10000L) {
            throw new MongoConfigurationException(String.format("Max staleness (%d sec) must be at least 90 seconds", this.getMaxStaleness(TimeUnit.SECONDS)));
        }
        throw new MongoConfigurationException(String.format("Max staleness (%d ms) must be at least the heartbeat period (%d ms) plus the idle write period (%d ms)", this.getMaxStaleness(TimeUnit.MILLISECONDS), heartbeatFrequencyMS, 10000));
    }
    
    private long getStalenessOfSecondaryRelativeToPrimary(final ServerDescription primary, final ServerDescription serverDescription, final long heartbeatFrequencyMS) {
        return primary.getLastWriteDate().getTime() + (serverDescription.getLastUpdateTime(TimeUnit.MILLISECONDS) - primary.getLastUpdateTime(TimeUnit.MILLISECONDS)) - serverDescription.getLastWriteDate().getTime() + heartbeatFrequencyMS;
    }
    
    private ServerDescription findPrimary(final ClusterDescription clusterDescription) {
        for (final ServerDescription cur : clusterDescription.getServerDescriptions()) {
            if (cur.isPrimary()) {
                return cur;
            }
        }
        return null;
    }
    
    private ServerDescription findMostUpToDateSecondary(final ClusterDescription clusterDescription) {
        ServerDescription mostUpdateToDateSecondary = null;
        for (final ServerDescription cur : clusterDescription.getServerDescriptions()) {
            if (cur.isSecondary() && (mostUpdateToDateSecondary == null || cur.getLastWriteDate().getTime() > mostUpdateToDateSecondary.getLastWriteDate().getTime())) {
                mostUpdateToDateSecondary = cur;
            }
        }
        return mostUpdateToDateSecondary;
    }
    
    private boolean serversAreAllThreeDotFour(final ClusterDescription clusterDescription) {
        for (final ServerDescription cur : clusterDescription.getServerDescriptions()) {
            if (cur.isOk() && cur.getMaxWireVersion() < 5) {
                return false;
            }
        }
        return true;
    }
    
    private BsonArray tagsListToBsonArray() {
        final BsonArray bsonArray = new BsonArray();
        for (final TagSet tagSet : this.tagSetList) {
            bsonArray.add(this.toDocument(tagSet));
        }
        return bsonArray;
    }
    
    private BsonDocument toDocument(final TagSet tagSet) {
        final BsonDocument document = new BsonDocument();
        for (final Tag tag : tagSet) {
            document.put(tag.getName(), new BsonString(tag.getValue()));
        }
        return document;
    }
    
    static class SecondaryReadPreference extends TaggableReadPreference
    {
        SecondaryReadPreference() {
        }
        
        SecondaryReadPreference(final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
            super(tagSetList, maxStaleness, timeUnit);
        }
        
        @Override
        public String getName() {
            return "secondary";
        }
        
        @Override
        protected List<ServerDescription> chooseForReplicaSet(final ClusterDescription clusterDescription) {
            List<ServerDescription> selectedServers = this.selectFreshServers(clusterDescription, clusterDescription.getSecondaries());
            if (!this.getTagSetList().isEmpty()) {
                final ClusterDescription nonStaleClusterDescription = TaggableReadPreference.copyClusterDescription(clusterDescription, selectedServers);
                selectedServers = Collections.emptyList();
                for (final TagSet tagSet : this.getTagSetList()) {
                    final List<ServerDescription> servers = nonStaleClusterDescription.getSecondaries(tagSet);
                    if (!servers.isEmpty()) {
                        selectedServers = servers;
                        break;
                    }
                }
            }
            return selectedServers;
        }
    }
    
    static class SecondaryPreferredReadPreference extends SecondaryReadPreference
    {
        SecondaryPreferredReadPreference() {
        }
        
        SecondaryPreferredReadPreference(final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
            super(tagSetList, maxStaleness, timeUnit);
        }
        
        @Override
        public String getName() {
            return "secondaryPreferred";
        }
        
        @Override
        protected List<ServerDescription> chooseForReplicaSet(final ClusterDescription clusterDescription) {
            List<ServerDescription> selectedServers = super.chooseForReplicaSet(clusterDescription);
            if (selectedServers.isEmpty()) {
                selectedServers = clusterDescription.getPrimaries();
            }
            return selectedServers;
        }
    }
    
    static class NearestReadPreference extends TaggableReadPreference
    {
        NearestReadPreference() {
        }
        
        NearestReadPreference(final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
            super(tagSetList, maxStaleness, timeUnit);
        }
        
        @Override
        public String getName() {
            return "nearest";
        }
        
        public List<ServerDescription> chooseForReplicaSet(final ClusterDescription clusterDescription) {
            List<ServerDescription> selectedServers = this.selectFreshServers(clusterDescription, clusterDescription.getAnyPrimaryOrSecondary());
            if (!this.getTagSetList().isEmpty()) {
                final ClusterDescription nonStaleClusterDescription = TaggableReadPreference.copyClusterDescription(clusterDescription, selectedServers);
                selectedServers = Collections.emptyList();
                for (final TagSet tagSet : this.getTagSetList()) {
                    final List<ServerDescription> servers = nonStaleClusterDescription.getAnyPrimaryOrSecondary(tagSet);
                    if (!servers.isEmpty()) {
                        selectedServers = servers;
                        break;
                    }
                }
            }
            return selectedServers;
        }
    }
    
    static class PrimaryPreferredReadPreference extends SecondaryReadPreference
    {
        PrimaryPreferredReadPreference() {
        }
        
        PrimaryPreferredReadPreference(final List<TagSet> tagSetList, final Long maxStaleness, final TimeUnit timeUnit) {
            super(tagSetList, maxStaleness, timeUnit);
        }
        
        @Override
        public String getName() {
            return "primaryPreferred";
        }
        
        @Override
        protected List<ServerDescription> chooseForReplicaSet(final ClusterDescription clusterDescription) {
            List<ServerDescription> selectedServers = this.selectFreshServers(clusterDescription, clusterDescription.getPrimaries());
            if (selectedServers.isEmpty()) {
                selectedServers = super.chooseForReplicaSet(clusterDescription);
            }
            return selectedServers;
        }
    }
}
