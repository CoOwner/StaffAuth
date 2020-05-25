package com.mongodb.connection;

import org.bson.*;
import java.util.concurrent.atomic.*;
import org.bson.assertions.*;
import java.util.*;
import java.nio.*;

class CompositeByteBuf implements ByteBuf
{
    private final List<Component> components;
    private final AtomicInteger referenceCount;
    private int position;
    private int limit;
    
    public CompositeByteBuf(final List<ByteBuf> buffers) {
        this.referenceCount = new AtomicInteger(1);
        Assertions.notNull("buffers", buffers);
        Assertions.isTrueArgument("buffer list not empty", !buffers.isEmpty());
        this.components = new ArrayList<Component>(buffers.size());
        int offset = 0;
        for (final ByteBuf cur : buffers) {
            final Component component = new Component(cur.duplicate().order(ByteOrder.LITTLE_ENDIAN), offset);
            this.components.add(component);
            offset = component.endOffset;
        }
        this.limit = this.components.get(this.components.size() - 1).endOffset;
    }
    
    CompositeByteBuf(final CompositeByteBuf from) {
        this.referenceCount = new AtomicInteger(1);
        this.components = from.components;
        this.position = from.position();
        this.limit = from.limit();
    }
    
    @Override
    public ByteBuf order(final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            throw new UnsupportedOperationException(String.format("Only %s is supported", ByteOrder.BIG_ENDIAN));
        }
        return this;
    }
    
    @Override
    public int capacity() {
        return this.components.get(this.components.size() - 1).endOffset;
    }
    
    @Override
    public int remaining() {
        return this.limit() - this.position();
    }
    
    @Override
    public boolean hasRemaining() {
        return this.remaining() > 0;
    }
    
    @Override
    public int position() {
        return this.position;
    }
    
    @Override
    public ByteBuf position(final int newPosition) {
        if (newPosition < 0 || newPosition > this.limit) {
            throw new IndexOutOfBoundsException(String.format("%d is out of bounds", newPosition));
        }
        this.position = newPosition;
        return this;
    }
    
    @Override
    public ByteBuf clear() {
        this.position = 0;
        this.limit = this.capacity();
        return this;
    }
    
    @Override
    public int limit() {
        return this.limit;
    }
    
    @Override
    public byte get() {
        this.checkIndex(this.position);
        ++this.position;
        return this.get(this.position - 1);
    }
    
    @Override
    public byte get(final int index) {
        this.checkIndex(index);
        final Component component = this.findComponent(index);
        return component.buffer.get(index - component.offset);
    }
    
    @Override
    public ByteBuf get(final byte[] bytes) {
        this.checkIndex(this.position, bytes.length);
        this.position += bytes.length;
        return this.get(this.position - bytes.length, bytes);
    }
    
    @Override
    public ByteBuf get(final int index, final byte[] bytes) {
        return this.get(index, bytes, 0, bytes.length);
    }
    
    @Override
    public ByteBuf get(final byte[] bytes, final int offset, final int length) {
        this.checkIndex(this.position, length);
        this.position += length;
        return this.get(this.position - length, bytes, offset, length);
    }
    
    @Override
    public ByteBuf get(final int index, final byte[] bytes, final int offset, final int length) {
        this.checkDstIndex(index, length, offset, bytes.length);
        int i = this.findComponentIndex(index);
        int curIndex = index;
        int curOffset = offset;
        int localLength;
        for (int curLength = length; curLength > 0; curLength -= localLength, ++i) {
            final Component c = this.components.get(i);
            localLength = Math.min(curLength, c.buffer.capacity() - (curIndex - c.offset));
            c.buffer.get(curIndex - c.offset, bytes, curOffset, localLength);
            curIndex += localLength;
            curOffset += localLength;
        }
        return this;
    }
    
    @Override
    public long getLong() {
        this.position += 8;
        return this.getLong(this.position - 8);
    }
    
    @Override
    public long getLong(final int index) {
        this.checkIndex(index, 8);
        final Component component = this.findComponent(index);
        if (index + 8 <= component.endOffset) {
            return component.buffer.getLong(index - component.offset);
        }
        return ((long)this.getInt(index) & 0xFFFFFFFFL) | ((long)this.getInt(index + 4) & 0xFFFFFFFFL) << 32;
    }
    
    @Override
    public double getDouble() {
        this.position += 8;
        return this.getDouble(this.position - 8);
    }
    
    @Override
    public double getDouble(final int index) {
        return Double.longBitsToDouble(this.getLong(index));
    }
    
    @Override
    public int getInt() {
        this.position += 4;
        return this.getInt(this.position - 4);
    }
    
    @Override
    public int getInt(final int index) {
        this.checkIndex(index, 4);
        final Component component = this.findComponent(index);
        if (index + 4 <= component.endOffset) {
            return component.buffer.getInt(index - component.offset);
        }
        return (this.getShort(index) & 0xFFFF) | (this.getShort(index + 2) & 0xFFFF) << 16;
    }
    
    private int getShort(final int index) {
        this.checkIndex(index, 2);
        return (short)((this.get(index) & 0xFF) | (this.get(index + 1) & 0xFF) << 8);
    }
    
    @Override
    public byte[] array() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
    
    @Override
    public ByteBuf limit(final int newLimit) {
        if (newLimit < 0 || newLimit > this.capacity()) {
            throw new IndexOutOfBoundsException(String.format("%d is out of bounds", newLimit));
        }
        this.limit = newLimit;
        return this;
    }
    
    @Override
    public ByteBuf put(final int index, final byte b) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ByteBuf put(final byte[] src, final int offset, final int length) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ByteBuf put(final byte b) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ByteBuf flip() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ByteBuf asReadOnly() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ByteBuf duplicate() {
        return new CompositeByteBuf(this);
    }
    
    @Override
    public ByteBuffer asNIO() {
        if (this.components.size() == 1) {
            final ByteBuffer byteBuffer = this.components.get(0).buffer.asNIO().duplicate();
            byteBuffer.position(this.position).limit(this.limit);
            return byteBuffer;
        }
        final byte[] bytes = new byte[this.remaining()];
        this.get(this.position, bytes, 0, bytes.length);
        return ByteBuffer.wrap(bytes);
    }
    
    @Override
    public int getReferenceCount() {
        return this.referenceCount.get();
    }
    
    @Override
    public ByteBuf retain() {
        if (this.referenceCount.incrementAndGet() == 1) {
            this.referenceCount.decrementAndGet();
            throw new IllegalStateException("Attempted to increment the reference count when it is already 0");
        }
        return this;
    }
    
    @Override
    public void release() {
        if (this.referenceCount.decrementAndGet() < 0) {
            this.referenceCount.incrementAndGet();
            throw new IllegalStateException("Attempted to decrement the reference count below 0");
        }
    }
    
    private Component findComponent(final int index) {
        return this.components.get(this.findComponentIndex(index));
    }
    
    private int findComponentIndex(final int index) {
        for (int i = this.components.size() - 1; i >= 0; --i) {
            final Component cur = this.components.get(i);
            if (index >= cur.offset) {
                return i;
            }
        }
        throw new IndexOutOfBoundsException(String.format("%d is out of bounds", index));
    }
    
    private void checkIndex(final int index) {
        this.ensureAccessible();
        if (index < 0 || index >= this.capacity()) {
            throw new IndexOutOfBoundsException(String.format("index: %d (expected: range(0, %d))", index, this.capacity()));
        }
    }
    
    private void checkIndex(final int index, final int fieldLength) {
        this.ensureAccessible();
        if (index < 0 || index > this.capacity() - fieldLength) {
            throw new IndexOutOfBoundsException(String.format("index: %d, length: %d (expected: range(0, %d))", index, fieldLength, this.capacity()));
        }
    }
    
    private void checkDstIndex(final int index, final int length, final int dstIndex, final int dstCapacity) {
        this.checkIndex(index, length);
        if (dstIndex < 0 || dstIndex > dstCapacity - length) {
            throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", dstIndex, length, dstCapacity));
        }
    }
    
    private void ensureAccessible() {
        if (this.referenceCount.get() == 0) {
            throw new IllegalStateException("Reference count is 0");
        }
    }
    
    private static final class Component
    {
        private final ByteBuf buffer;
        private final int length;
        private final int offset;
        private final int endOffset;
        
        Component(final ByteBuf buffer, final int offset) {
            this.buffer = buffer;
            this.length = buffer.limit() - buffer.position();
            this.offset = offset;
            this.endOffset = offset + this.length;
        }
    }
}
