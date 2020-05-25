package com.mongodb;

import java.io.*;

public final class ServerCursor implements Serializable
{
    private static final long serialVersionUID = -7013636754565190109L;
    private final long id;
    private final ServerAddress address;
    
    public ServerCursor(final long id, final ServerAddress address) {
        if (id == 0L) {
            throw new IllegalArgumentException();
        }
        if (address == null) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.address = address;
    }
    
    public long getId() {
        return this.id;
    }
    
    public ServerAddress getAddress() {
        return this.address;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerCursor that = (ServerCursor)o;
        return this.id == that.id && this.address.equals(that.address);
    }
    
    @Override
    public int hashCode() {
        int result = (int)(this.id ^ this.id >>> 32);
        result = 31 * result + this.address.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "ServerCursor{getId=" + this.id + ", address=" + this.address + '}';
    }
}
