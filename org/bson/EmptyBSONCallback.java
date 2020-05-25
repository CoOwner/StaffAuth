package org.bson;

import org.bson.types.*;

public class EmptyBSONCallback implements BSONCallback
{
    @Override
    public void objectStart() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void objectStart(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public Object objectDone() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public Object get() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public BSONCallback createBSONCallback() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void arrayStart() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void arrayStart(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public Object arrayDone() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotNull(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotUndefined(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotMinKey(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotMaxKey(final String name) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotBoolean(final String name, final boolean value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotDouble(final String name, final double value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotInt(final String name, final int value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotLong(final String name, final long value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotDecimal128(final String name, final Decimal128 value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotDate(final String name, final long millis) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotString(final String name, final String value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotSymbol(final String name, final String value) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotRegex(final String name, final String pattern, final String flags) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotTimestamp(final String name, final int time, final int increment) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotObjectId(final String name, final ObjectId id) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotDBRef(final String name, final String namespace, final ObjectId id) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Deprecated
    @Override
    public void gotBinaryArray(final String name, final byte[] data) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotBinary(final String name, final byte type, final byte[] data) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotUUID(final String name, final long part1, final long part2) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotCode(final String name, final String code) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public void gotCodeWScope(final String name, final String code, final Object scope) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
}
