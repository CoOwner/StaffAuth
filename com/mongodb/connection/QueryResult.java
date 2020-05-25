package com.mongodb.connection;

import java.util.*;
import com.mongodb.*;

public class QueryResult<T>
{
    private final MongoNamespace namespace;
    private final List<T> results;
    private final long cursorId;
    private final ServerAddress serverAddress;
    
    public QueryResult(final MongoNamespace namespace, final List<T> results, final long cursorId, final ServerAddress serverAddress) {
        this.namespace = namespace;
        this.results = results;
        this.cursorId = cursorId;
        this.serverAddress = serverAddress;
    }
    
    QueryResult(final MongoNamespace namespace, final ReplyMessage<T> replyMessage, final ServerAddress address) {
        this(namespace, replyMessage.getDocuments(), replyMessage.getReplyHeader().getCursorId(), address);
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public ServerCursor getCursor() {
        return (this.cursorId == 0L) ? null : new ServerCursor(this.cursorId, this.serverAddress);
    }
    
    public List<T> getResults() {
        return this.results;
    }
    
    public ServerAddress getAddress() {
        return this.serverAddress;
    }
}
