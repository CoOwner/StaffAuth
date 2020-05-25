package com.mongodb.connection.netty;

import org.bson.*;
import java.nio.*;

final class NettyByteBuf implements ByteBuf
{
    private io.netty.buffer.ByteBuf proxied;
    private boolean isWriting;
    
    public NettyByteBuf(final io.netty.buffer.ByteBuf proxied) {
        this.isWriting = true;
        this.proxied = proxied;
    }
    
    public NettyByteBuf(final io.netty.buffer.ByteBuf proxied, final boolean isWriting) {
        this(proxied);
        this.isWriting = isWriting;
    }
    
    io.netty.buffer.ByteBuf asByteBuf() {
        return this.proxied;
    }
    
    @Override
    public int capacity() {
        return this.proxied.capacity();
    }
    
    @Override
    public ByteBuf put(final int index, final byte b) {
        this.proxied.setByte(index, (int)b);
        return this;
    }
    
    @Override
    public int remaining() {
        if (this.isWriting) {
            return this.proxied.writableBytes();
        }
        return this.proxied.readableBytes();
    }
    
    @Override
    public ByteBuf put(final byte[] src, final int offset, final int length) {
        this.proxied.writeBytes(src, offset, length);
        return this;
    }
    
    @Override
    public boolean hasRemaining() {
        return this.remaining() > 0;
    }
    
    @Override
    public ByteBuf put(final byte b) {
        this.proxied.writeByte((int)b);
        return this;
    }
    
    @Override
    public ByteBuf flip() {
        this.isWriting = !this.isWriting;
        return this;
    }
    
    @Override
    public byte[] array() {
        return this.proxied.array();
    }
    
    @Override
    public int limit() {
        if (this.isWriting) {
            return this.proxied.writerIndex() + this.remaining();
        }
        return this.proxied.readerIndex() + this.remaining();
    }
    
    @Override
    public ByteBuf position(final int newPosition) {
        if (this.isWriting) {
            this.proxied.writerIndex(newPosition);
        }
        else {
            this.proxied.readerIndex(newPosition);
        }
        return this;
    }
    
    @Override
    public ByteBuf clear() {
        this.proxied.clear();
        return this;
    }
    
    @Override
    public ByteBuf order(final ByteOrder byteOrder) {
        this.proxied = this.proxied.order(byteOrder);
        return this;
    }
    
    @Override
    public byte get() {
        return this.proxied.readByte();
    }
    
    @Override
    public byte get(final int index) {
        return this.proxied.getByte(index);
    }
    
    @Override
    public ByteBuf get(final byte[] bytes) {
        this.proxied.readBytes(bytes);
        return this;
    }
    
    @Override
    public ByteBuf get(final int index, final byte[] bytes) {
        this.proxied.getBytes(index, bytes);
        return this;
    }
    
    @Override
    public ByteBuf get(final byte[] bytes, final int offset, final int length) {
        this.proxied.readBytes(bytes, offset, length);
        return this;
    }
    
    @Override
    public ByteBuf get(final int index, final byte[] bytes, final int offset, final int length) {
        this.proxied.getBytes(index, bytes, offset, length);
        return this;
    }
    
    @Override
    public long getLong() {
        return this.proxied.readLong();
    }
    
    @Override
    public long getLong(final int index) {
        return this.proxied.getLong(index);
    }
    
    @Override
    public double getDouble() {
        return this.proxied.readDouble();
    }
    
    @Override
    public double getDouble(final int index) {
        return this.proxied.getDouble(index);
    }
    
    @Override
    public int getInt() {
        return this.proxied.readInt();
    }
    
    @Override
    public int getInt(final int index) {
        return this.proxied.getInt(index);
    }
    
    @Override
    public int position() {
        if (this.isWriting) {
            return this.proxied.writerIndex();
        }
        return this.proxied.readerIndex();
    }
    
    @Override
    public ByteBuf limit(final int newLimit) {
        if (this.isWriting) {
            throw new UnsupportedOperationException("Can not set the limit while writing");
        }
        this.proxied.writerIndex(newLimit);
        return this;
    }
    
    @Override
    public ByteBuf asReadOnly() {
        return this;
    }
    
    @Override
    public ByteBuf duplicate() {
        return new NettyByteBuf(this.proxied.duplicate().retain(), this.isWriting);
    }
    
    @Override
    public ByteBuffer asNIO() {
        if (this.isWriting) {
            return this.proxied.nioBuffer(this.proxied.writerIndex(), this.proxied.writableBytes());
        }
        return this.proxied.nioBuffer(this.proxied.readerIndex(), this.proxied.readableBytes());
    }
    
    @Override
    public int getReferenceCount() {
        return this.proxied.refCnt();
    }
    
    @Override
    public ByteBuf retain() {
        this.proxied.retain();
        return this;
    }
    
    @Override
    public void release() {
        this.proxied.release();
    }
}
