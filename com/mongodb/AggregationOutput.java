package com.mongodb;

import java.util.*;

public class AggregationOutput
{
    private final List<DBObject> results;
    
    AggregationOutput(final List<DBObject> results) {
        this.results = results;
    }
    
    public Iterable<DBObject> results() {
        return this.results;
    }
}
