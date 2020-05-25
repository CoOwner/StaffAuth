package com.mongodb.client.model;

public final class GraphLookupOptions
{
    private Integer maxDepth;
    private String depthField;
    
    public GraphLookupOptions depthField(final String field) {
        this.depthField = field;
        return this;
    }
    
    public String getDepthField() {
        return this.depthField;
    }
    
    public GraphLookupOptions maxDepth(final Integer max) {
        this.maxDepth = max;
        return this;
    }
    
    public Integer getMaxDepth() {
        return this.maxDepth;
    }
    
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder().append("GraphLookupOptions{");
        if (this.depthField != null) {
            stringBuilder.append("depthField='").append(this.depthField).append('\'');
            if (this.maxDepth != null) {
                stringBuilder.append(", ");
            }
        }
        if (this.maxDepth != null) {
            stringBuilder.append("maxDepth=").append(this.maxDepth);
        }
        return stringBuilder.append('}').toString();
    }
}
