package com.mongodb.client.model;

import org.bson.conversions.*;
import java.util.*;

public class Facet
{
    private final String name;
    private final List<? extends Bson> pipeline;
    
    public Facet(final String name, final List<? extends Bson> pipeline) {
        this.name = name;
        this.pipeline = pipeline;
    }
    
    public Facet(final String name, final Bson... pipeline) {
        this(name, Arrays.asList(pipeline));
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<? extends Bson> getPipeline() {
        return this.pipeline;
    }
}
