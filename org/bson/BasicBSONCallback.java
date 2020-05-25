package org.bson;

import java.util.regex.*;
import java.util.*;
import org.bson.types.*;

public class BasicBSONCallback implements BSONCallback
{
    private Object root;
    private final LinkedList<BSONObject> stack;
    private final LinkedList<String> nameStack;
    
    public BasicBSONCallback() {
        this.stack = new LinkedList<BSONObject>();
        this.nameStack = new LinkedList<String>();
        this.reset();
    }
    
    @Override
    public Object get() {
        return this.root;
    }
    
    public BSONObject create() {
        return new BasicBSONObject();
    }
    
    protected BSONObject createList() {
        return new BasicBSONList();
    }
    
    @Override
    public BSONCallback createBSONCallback() {
        return new BasicBSONCallback();
    }
    
    public BSONObject create(final boolean array, final List<String> path) {
        return array ? this.createList() : this.create();
    }
    
    @Override
    public void objectStart() {
        if (this.stack.size() > 0) {
            throw new IllegalStateException("Illegal object beginning in current context.");
        }
        this.root = this.create(false, null);
        this.stack.add((BSONObject)this.root);
    }
    
    @Override
    public void objectStart(final String name) {
        this.nameStack.addLast(name);
        final BSONObject o = this.create(false, this.nameStack);
        this.stack.getLast().put(name, o);
        this.stack.addLast(o);
    }
    
    @Override
    public Object objectDone() {
        final BSONObject o = this.stack.removeLast();
        if (this.nameStack.size() > 0) {
            this.nameStack.removeLast();
        }
        else if (this.stack.size() > 0) {
            throw new IllegalStateException("Illegal object end in current context.");
        }
        return BSON.hasDecodeHooks() ? BSON.applyDecodingHooks(o) : o;
    }
    
    @Override
    public void arrayStart() {
        this.root = this.create(true, null);
        this.stack.add((BSONObject)this.root);
    }
    
    @Override
    public void arrayStart(final String name) {
        this.nameStack.addLast(name);
        final BSONObject o = this.create(true, this.nameStack);
        this.stack.getLast().put(name, o);
        this.stack.addLast(o);
    }
    
    @Override
    public Object arrayDone() {
        return this.objectDone();
    }
    
    @Override
    public void gotNull(final String name) {
        this.cur().put(name, null);
    }
    
    @Override
    public void gotUndefined(final String name) {
    }
    
    @Override
    public void gotMinKey(final String name) {
        this.cur().put(name, new MinKey());
    }
    
    @Override
    public void gotMaxKey(final String name) {
        this.cur().put(name, new MaxKey());
    }
    
    @Override
    public void gotBoolean(final String name, final boolean value) {
        this._put(name, value);
    }
    
    @Override
    public void gotDouble(final String name, final double value) {
        this._put(name, value);
    }
    
    @Override
    public void gotInt(final String name, final int value) {
        this._put(name, value);
    }
    
    @Override
    public void gotLong(final String name, final long value) {
        this._put(name, value);
    }
    
    @Override
    public void gotDecimal128(final String name, final Decimal128 value) {
        this._put(name, value);
    }
    
    @Override
    public void gotDate(final String name, final long millis) {
        this._put(name, new Date(millis));
    }
    
    @Override
    public void gotRegex(final String name, final String pattern, final String flags) {
        this._put(name, Pattern.compile(pattern, BSON.regexFlags(flags)));
    }
    
    @Override
    public void gotString(final String name, final String value) {
        this._put(name, value);
    }
    
    @Override
    public void gotSymbol(final String name, final String value) {
        this._put(name, value);
    }
    
    @Override
    public void gotTimestamp(final String name, final int time, final int increment) {
        this._put(name, new BSONTimestamp(time, increment));
    }
    
    @Override
    public void gotObjectId(final String name, final ObjectId id) {
        this._put(name, id);
    }
    
    @Override
    public void gotDBRef(final String name, final String namespace, final ObjectId id) {
        this._put(name, new BasicBSONObject("$ns", namespace).append("$id", id));
    }
    
    @Deprecated
    @Override
    public void gotBinaryArray(final String name, final byte[] data) {
        this.gotBinary(name, (byte)0, data);
    }
    
    @Override
    public void gotBinary(final String name, final byte type, final byte[] data) {
        if (type == 0 || type == 2) {
            this._put(name, data);
        }
        else {
            this._put(name, new Binary(type, data));
        }
    }
    
    @Override
    public void gotUUID(final String name, final long part1, final long part2) {
        this._put(name, new UUID(part1, part2));
    }
    
    @Override
    public void gotCode(final String name, final String code) {
        this._put(name, new Code(code));
    }
    
    @Override
    public void gotCodeWScope(final String name, final String code, final Object scope) {
        this._put(name, new CodeWScope(code, (BSONObject)scope));
    }
    
    protected void _put(final String name, final Object value) {
        this.cur().put(name, BSON.hasDecodeHooks() ? BSON.applyDecodingHooks(value) : value);
    }
    
    protected BSONObject cur() {
        return this.stack.getLast();
    }
    
    protected String curName() {
        return this.nameStack.peekLast();
    }
    
    protected void setRoot(final Object root) {
        this.root = root;
    }
    
    protected boolean isStackEmpty() {
        return this.stack.size() < 1;
    }
    
    @Override
    public void reset() {
        this.root = null;
        this.stack.clear();
        this.nameStack.clear();
    }
}
