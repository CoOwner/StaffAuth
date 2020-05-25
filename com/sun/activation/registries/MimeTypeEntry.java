package com.sun.activation.registries;

public class MimeTypeEntry
{
    private String type;
    private String extension;
    
    public MimeTypeEntry(final String type, final String extension) {
        this.type = type;
        this.extension = extension;
    }
    
    public String getMIMEType() {
        return this.type;
    }
    
    public String getFileExtension() {
        return this.extension;
    }
    
    @Override
    public String toString() {
        return "MIMETypeEntry: " + this.type + ", " + this.extension;
    }
}
