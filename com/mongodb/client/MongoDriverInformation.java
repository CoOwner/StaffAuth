package com.mongodb.client;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.*;

public final class MongoDriverInformation
{
    private final List<String> driverNames;
    private final List<String> driverVersions;
    private final List<String> driverPlatforms;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(final MongoDriverInformation mongoDriverInformation) {
        return new Builder(mongoDriverInformation);
    }
    
    public List<String> getDriverNames() {
        return this.driverNames;
    }
    
    public List<String> getDriverVersions() {
        return this.driverVersions;
    }
    
    public List<String> getDriverPlatforms() {
        return this.driverPlatforms;
    }
    
    private MongoDriverInformation(final List<String> driverNames, final List<String> driverVersions, final List<String> driverPlatforms) {
        this.driverNames = driverNames;
        this.driverVersions = driverVersions;
        this.driverPlatforms = driverPlatforms;
    }
    
    @NotThreadSafe
    public static final class Builder
    {
        private final MongoDriverInformation driverInformation;
        private String driverName;
        private String driverVersion;
        private String driverPlatform;
        
        public Builder driverName(final String driverName) {
            this.driverName = Assertions.notNull("driverName", driverName);
            return this;
        }
        
        public Builder driverVersion(final String driverVersion) {
            this.driverVersion = Assertions.notNull("driverVersion", driverVersion);
            return this;
        }
        
        public Builder driverPlatform(final String driverPlatform) {
            this.driverPlatform = Assertions.notNull("driverPlatform", driverPlatform);
            return this;
        }
        
        public MongoDriverInformation build() {
            Assertions.isTrue("You must also set the driver name when setting the driver version", this.driverName != null || this.driverVersion == null);
            final List<String> names = this.prependToList(this.driverInformation.getDriverNames(), this.driverName);
            final List<String> versions = this.prependToList(this.driverInformation.getDriverVersions(), this.driverVersion);
            final List<String> platforms = this.prependToList(this.driverInformation.getDriverPlatforms(), this.driverPlatform);
            return new MongoDriverInformation(names, versions, platforms, null);
        }
        
        private List<String> prependToList(final List<String> stringList, final String value) {
            if (value == null) {
                return stringList;
            }
            final ArrayList<String> newList = new ArrayList<String>();
            newList.add(value);
            newList.addAll(stringList);
            return Collections.unmodifiableList((List<? extends String>)newList);
        }
        
        private Builder() {
            final List<String> immutableEmptyList = Collections.unmodifiableList(Collections.emptyList());
            this.driverInformation = new MongoDriverInformation(immutableEmptyList, immutableEmptyList, immutableEmptyList, null);
        }
        
        private Builder(final MongoDriverInformation driverInformation) {
            this.driverInformation = Assertions.notNull("driverInformation", driverInformation);
        }
    }
}
