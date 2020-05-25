package org.bson.json;

class JsonBuffer
{
    private final String buffer;
    private int position;
    private boolean eof;
    
    public JsonBuffer(final String buffer) {
        this.buffer = buffer;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setPosition(final int position) {
        this.position = position;
    }
    
    public int read() {
        if (this.eof) {
            throw new JsonParseException("Trying to read past EOF.");
        }
        if (this.position >= this.buffer.length()) {
            this.eof = true;
            return -1;
        }
        return this.buffer.charAt(this.position++);
    }
    
    public void unread(final int c) {
        this.eof = false;
        if (c != -1 && this.buffer.charAt(this.position - 1) == c) {
            --this.position;
        }
    }
    
    public String substring(final int beginIndex) {
        return this.buffer.substring(beginIndex);
    }
    
    public String substring(final int beginIndex, final int endIndex) {
        return this.buffer.substring(beginIndex, endIndex);
    }
}
