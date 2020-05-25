package com.mongodb.connection;

import com.mongodb.assertions.*;
import java.util.*;

public class ServerVersion implements Comparable<ServerVersion>
{
    private final List<Integer> versionList;
    
    public ServerVersion() {
        this.versionList = Collections.unmodifiableList((List<? extends Integer>)Arrays.asList(0, 0, 0));
    }
    
    public ServerVersion(final List<Integer> versionList) {
        Assertions.notNull("versionList", versionList);
        Assertions.isTrue("version array has three elements", versionList.size() == 3);
        this.versionList = Collections.unmodifiableList((List<? extends Integer>)new ArrayList<Integer>(versionList));
    }
    
    public ServerVersion(final int majorVersion, final int minorVersion) {
        this(Arrays.asList(majorVersion, minorVersion, 0));
    }
    
    public List<Integer> getVersionList() {
        return this.versionList;
    }
    
    @Override
    public int compareTo(final ServerVersion o) {
        int retVal = 0;
        for (int i = 0; i < this.versionList.size(); ++i) {
            retVal = this.versionList.get(i).compareTo(o.versionList.get(i));
            if (retVal != 0) {
                break;
            }
        }
        return retVal;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerVersion that = (ServerVersion)o;
        return this.versionList.equals(that.versionList);
    }
    
    @Override
    public int hashCode() {
        return this.versionList.hashCode();
    }
    
    @Override
    public String toString() {
        return "ServerVersion{versionList=" + this.versionList + '}';
    }
}
