package com.mongodb;

import com.mongodb.client.model.*;
import java.util.concurrent.*;
import java.util.*;

public class MapReduceCommand
{
    private final String mapReduce;
    private final String map;
    private final String reduce;
    private String finalize;
    private ReadPreference readPreference;
    private final OutputType outputType;
    private final String outputCollection;
    private String outputDB;
    private final DBObject query;
    private DBObject sort;
    private int limit;
    private long maxTimeMS;
    private Map<String, Object> scope;
    private Boolean jsMode;
    private Boolean verbose;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    
    public MapReduceCommand(final DBCollection inputCollection, final String map, final String reduce, final String outputCollection, final OutputType type, final DBObject query) {
        this.mapReduce = inputCollection.getName();
        this.map = map;
        this.reduce = reduce;
        this.outputCollection = outputCollection;
        this.outputType = type;
        this.query = query;
        this.outputDB = null;
        this.verbose = true;
    }
    
    public void setVerbose(final Boolean verbose) {
        this.verbose = verbose;
    }
    
    public Boolean isVerbose() {
        return this.verbose;
    }
    
    public String getInput() {
        return this.mapReduce;
    }
    
    public String getMap() {
        return this.map;
    }
    
    public String getReduce() {
        return this.reduce;
    }
    
    public String getOutputTarget() {
        return this.outputCollection;
    }
    
    public OutputType getOutputType() {
        return this.outputType;
    }
    
    public String getFinalize() {
        return this.finalize;
    }
    
    public void setFinalize(final String finalize) {
        this.finalize = finalize;
    }
    
    public DBObject getQuery() {
        return this.query;
    }
    
    public DBObject getSort() {
        return this.sort;
    }
    
    public void setSort(final DBObject sort) {
        this.sort = sort;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public void setMaxTime(final long maxTime, final TimeUnit timeUnit) {
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
    }
    
    public Map<String, Object> getScope() {
        return this.scope;
    }
    
    public void setScope(final Map<String, Object> scope) {
        this.scope = scope;
    }
    
    public Boolean getJsMode() {
        return this.jsMode;
    }
    
    public void setJsMode(final Boolean jsMode) {
        this.jsMode = jsMode;
    }
    
    public String getOutputDB() {
        return this.outputDB;
    }
    
    public void setOutputDB(final String outputDB) {
        this.outputDB = outputDB;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public void setBypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
    }
    
    public DBObject toDBObject() {
        final BasicDBObject cmd = new BasicDBObject();
        ((HashMap<String, String>)cmd).put("mapreduce", this.mapReduce);
        ((HashMap<String, String>)cmd).put("map", this.map);
        ((HashMap<String, String>)cmd).put("reduce", this.reduce);
        if (this.verbose != null) {
            ((HashMap<String, Boolean>)cmd).put("verbose", this.verbose);
        }
        final BasicDBObject out = new BasicDBObject();
        switch (this.outputType) {
            case INLINE: {
                ((HashMap<String, Integer>)out).put("inline", 1);
                break;
            }
            case REPLACE: {
                ((HashMap<String, String>)out).put("replace", this.outputCollection);
                break;
            }
            case MERGE: {
                ((HashMap<String, String>)out).put("merge", this.outputCollection);
                break;
            }
            case REDUCE: {
                ((HashMap<String, String>)out).put("reduce", this.outputCollection);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unexpected output type");
            }
        }
        if (this.outputDB != null) {
            ((HashMap<String, String>)out).put("db", this.outputDB);
        }
        ((HashMap<String, BasicDBObject>)cmd).put("out", out);
        if (this.query != null) {
            ((HashMap<String, DBObject>)cmd).put("query", this.query);
        }
        if (this.finalize != null) {
            ((HashMap<String, String>)cmd).put("finalize", this.finalize);
        }
        if (this.sort != null) {
            ((HashMap<String, DBObject>)cmd).put("sort", this.sort);
        }
        if (this.limit > 0) {
            ((HashMap<String, Integer>)cmd).put("limit", this.limit);
        }
        if (this.scope != null) {
            ((HashMap<String, Map<String, Object>>)cmd).put("scope", this.scope);
        }
        if (this.jsMode != null) {
            ((HashMap<String, Boolean>)cmd).put("jsMode", this.jsMode);
        }
        if (this.maxTimeMS != 0L) {
            ((HashMap<String, Long>)cmd).put("maxTimeMS", this.maxTimeMS);
        }
        return cmd;
    }
    
    public void setReadPreference(final ReadPreference preference) {
        this.readPreference = preference;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public void setCollation(final Collation collation) {
        this.collation = collation;
    }
    
    @Override
    public String toString() {
        return this.toDBObject().toString();
    }
    
    public enum OutputType
    {
        REPLACE, 
        MERGE, 
        REDUCE, 
        INLINE;
    }
}
