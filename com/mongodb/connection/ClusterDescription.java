package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import com.mongodb.selector.*;
import java.util.*;
import com.mongodb.*;

@Immutable
public class ClusterDescription
{
    private final ClusterConnectionMode connectionMode;
    private final ClusterType type;
    private final List<ServerDescription> serverDescriptions;
    private final ClusterSettings clusterSettings;
    private final ServerSettings serverSettings;
    
    public ClusterDescription(final ClusterConnectionMode connectionMode, final ClusterType type, final List<ServerDescription> serverDescriptions) {
        this(connectionMode, type, serverDescriptions, null, null);
    }
    
    public ClusterDescription(final ClusterConnectionMode connectionMode, final ClusterType type, final List<ServerDescription> serverDescriptions, final ClusterSettings clusterSettings, final ServerSettings serverSettings) {
        Assertions.notNull("all", serverDescriptions);
        this.connectionMode = Assertions.notNull("connectionMode", connectionMode);
        this.type = Assertions.notNull("type", type);
        this.serverDescriptions = new ArrayList<ServerDescription>(serverDescriptions);
        this.clusterSettings = clusterSettings;
        this.serverSettings = serverSettings;
    }
    
    public ClusterSettings getClusterSettings() {
        return this.clusterSettings;
    }
    
    public ServerSettings getServerSettings() {
        return this.serverSettings;
    }
    
    public boolean isCompatibleWithDriver() {
        for (final ServerDescription cur : this.serverDescriptions) {
            if (!cur.isCompatibleWithDriver()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasReadableServer(final ReadPreference readPreference) {
        Assertions.notNull("readPreference", readPreference);
        return !new ReadPreferenceServerSelector(readPreference).select(this).isEmpty();
    }
    
    public boolean hasWritableServer() {
        return !new WritableServerSelector().select(this).isEmpty();
    }
    
    public ClusterConnectionMode getConnectionMode() {
        return this.connectionMode;
    }
    
    public ClusterType getType() {
        return this.type;
    }
    
    public List<ServerDescription> getServerDescriptions() {
        return Collections.unmodifiableList((List<? extends ServerDescription>)this.serverDescriptions);
    }
    
    @Deprecated
    public Set<ServerDescription> getAll() {
        final Set<ServerDescription> serverDescriptionSet = new TreeSet<ServerDescription>(new Comparator<ServerDescription>() {
            @Override
            public int compare(final ServerDescription o1, final ServerDescription o2) {
                final int val = o1.getAddress().getHost().compareTo(o2.getAddress().getHost());
                if (val != 0) {
                    return val;
                }
                return this.integerCompare(o1.getAddress().getPort(), o2.getAddress().getPort());
            }
            
            private int integerCompare(final int p1, final int p2) {
                return (p1 < p2) ? -1 : ((p1 == p2) ? 0 : 1);
            }
        });
        serverDescriptionSet.addAll(this.serverDescriptions);
        return Collections.unmodifiableSet((Set<? extends ServerDescription>)serverDescriptionSet);
    }
    
    @Deprecated
    public ServerDescription getByServerAddress(final ServerAddress serverAddress) {
        for (final ServerDescription cur : this.serverDescriptions) {
            if (cur.isOk() && cur.getAddress().equals(serverAddress)) {
                return cur;
            }
        }
        return null;
    }
    
    @Deprecated
    public List<ServerDescription> getPrimaries() {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return serverDescription.isPrimary();
            }
        });
    }
    
    @Deprecated
    public List<ServerDescription> getSecondaries() {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return serverDescription.isSecondary();
            }
        });
    }
    
    @Deprecated
    public List<ServerDescription> getSecondaries(final TagSet tagSet) {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return serverDescription.isSecondary() && serverDescription.hasTags(tagSet);
            }
        });
    }
    
    @Deprecated
    public List<ServerDescription> getAny() {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return serverDescription.isOk();
            }
        });
    }
    
    @Deprecated
    public List<ServerDescription> getAnyPrimaryOrSecondary() {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return serverDescription.isPrimary() || serverDescription.isSecondary();
            }
        });
    }
    
    @Deprecated
    public List<ServerDescription> getAnyPrimaryOrSecondary(final TagSet tagSet) {
        return this.getServersByPredicate(new Predicate() {
            @Override
            public boolean apply(final ServerDescription serverDescription) {
                return (serverDescription.isPrimary() || serverDescription.isSecondary()) && serverDescription.hasTags(tagSet);
            }
        });
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ClusterDescription that = (ClusterDescription)o;
        return this.connectionMode == that.connectionMode && this.type == that.type && this.serverDescriptions.size() == that.serverDescriptions.size() && this.serverDescriptions.containsAll(that.serverDescriptions);
    }
    
    @Override
    public int hashCode() {
        int result = this.connectionMode.hashCode();
        result = 31 * result + this.type.hashCode();
        result = 31 * result + this.serverDescriptions.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "ClusterDescription{type=" + this.getType() + ", connectionMode=" + this.connectionMode + ", serverDescriptions=" + this.serverDescriptions + '}';
    }
    
    public String getShortDescription() {
        final StringBuilder serverDescriptions = new StringBuilder();
        String delimiter = "";
        for (final ServerDescription cur : this.serverDescriptions) {
            serverDescriptions.append(delimiter).append(cur.getShortDescription());
            delimiter = ", ";
        }
        return String.format("{type=%s, servers=[%s]", this.type, serverDescriptions);
    }
    
    private List<ServerDescription> getServersByPredicate(final Predicate predicate) {
        final List<ServerDescription> membersByTag = new ArrayList<ServerDescription>();
        for (final ServerDescription cur : this.serverDescriptions) {
            if (predicate.apply(cur)) {
                membersByTag.add(cur);
            }
        }
        return membersByTag;
    }
    
    private interface Predicate
    {
        boolean apply(final ServerDescription p0);
    }
}
