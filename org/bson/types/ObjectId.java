package org.bson.types;

import java.io.*;
import java.util.concurrent.atomic.*;
import org.bson.assertions.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.util.logging.*;
import java.util.*;
import java.lang.management.*;
import org.bson.diagnostics.*;

public final class ObjectId implements Comparable<ObjectId>, Serializable
{
    private static final long serialVersionUID = 3670079982654483072L;
    static final Logger LOGGER;
    private static final int LOW_ORDER_THREE_BYTES = 16777215;
    private static final int MACHINE_IDENTIFIER;
    private static final short PROCESS_IDENTIFIER;
    private static final AtomicInteger NEXT_COUNTER;
    private static final char[] HEX_CHARS;
    private final int timestamp;
    private final int machineIdentifier;
    private final short processIdentifier;
    private final int counter;
    
    public static ObjectId get() {
        return new ObjectId();
    }
    
    public static boolean isValid(final String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException();
        }
        final int len = hexString.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            final char c = hexString.charAt(i);
            if (c < '0' || c > '9') {
                if (c < 'a' || c > 'f') {
                    if (c < 'A' || c > 'F') {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static int getGeneratedMachineIdentifier() {
        return ObjectId.MACHINE_IDENTIFIER;
    }
    
    public static int getGeneratedProcessIdentifier() {
        return ObjectId.PROCESS_IDENTIFIER;
    }
    
    public static int getCurrentCounter() {
        return ObjectId.NEXT_COUNTER.get();
    }
    
    public static ObjectId createFromLegacyFormat(final int time, final int machine, final int inc) {
        return new ObjectId(time, machine, inc);
    }
    
    public ObjectId() {
        this(new Date());
    }
    
    public ObjectId(final Date date) {
        this(dateToTimestampSeconds(date), ObjectId.MACHINE_IDENTIFIER, ObjectId.PROCESS_IDENTIFIER, ObjectId.NEXT_COUNTER.getAndIncrement(), false);
    }
    
    public ObjectId(final Date date, final int counter) {
        this(date, ObjectId.MACHINE_IDENTIFIER, ObjectId.PROCESS_IDENTIFIER, counter);
    }
    
    public ObjectId(final Date date, final int machineIdentifier, final short processIdentifier, final int counter) {
        this(dateToTimestampSeconds(date), machineIdentifier, processIdentifier, counter);
    }
    
    public ObjectId(final int timestamp, final int machineIdentifier, final short processIdentifier, final int counter) {
        this(timestamp, machineIdentifier, processIdentifier, counter, true);
    }
    
    private ObjectId(final int timestamp, final int machineIdentifier, final short processIdentifier, final int counter, final boolean checkCounter) {
        if ((machineIdentifier & 0xFF000000) != 0x0) {
            throw new IllegalArgumentException("The machine identifier must be between 0 and 16777215 (it must fit in three bytes).");
        }
        if (checkCounter && (counter & 0xFF000000) != 0x0) {
            throw new IllegalArgumentException("The counter must be between 0 and 16777215 (it must fit in three bytes).");
        }
        this.timestamp = timestamp;
        this.machineIdentifier = machineIdentifier;
        this.processIdentifier = processIdentifier;
        this.counter = (counter & 0xFFFFFF);
    }
    
    public ObjectId(final String hexString) {
        this(parseHexString(hexString));
    }
    
    public ObjectId(final byte[] bytes) {
        this(ByteBuffer.wrap(Assertions.notNull("bytes", bytes)));
    }
    
    ObjectId(final int timestamp, final int machineAndProcessIdentifier, final int counter) {
        this(legacyToBytes(timestamp, machineAndProcessIdentifier, counter));
    }
    
    public ObjectId(final ByteBuffer buffer) {
        Assertions.notNull("buffer", buffer);
        Assertions.isTrueArgument("buffer.remaining() >=12", buffer.remaining() >= 12);
        this.timestamp = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
        this.machineIdentifier = makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
        this.processIdentifier = (short)makeInt((byte)0, (byte)0, buffer.get(), buffer.get());
        this.counter = makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
    }
    
    private static byte[] legacyToBytes(final int timestamp, final int machineAndProcessIdentifier, final int counter) {
        final byte[] bytes = { int3(timestamp), int2(timestamp), int1(timestamp), int0(timestamp), int3(machineAndProcessIdentifier), int2(machineAndProcessIdentifier), int1(machineAndProcessIdentifier), int0(machineAndProcessIdentifier), int3(counter), int2(counter), int1(counter), int0(counter) };
        return bytes;
    }
    
    public byte[] toByteArray() {
        final ByteBuffer buffer = ByteBuffer.allocate(12);
        this.putToByteBuffer(buffer);
        return buffer.array();
    }
    
    public void putToByteBuffer(final ByteBuffer buffer) {
        Assertions.notNull("buffer", buffer);
        Assertions.isTrueArgument("buffer.remaining() >=12", buffer.remaining() >= 12);
        buffer.put(int3(this.timestamp));
        buffer.put(int2(this.timestamp));
        buffer.put(int1(this.timestamp));
        buffer.put(int0(this.timestamp));
        buffer.put(int2(this.machineIdentifier));
        buffer.put(int1(this.machineIdentifier));
        buffer.put(int0(this.machineIdentifier));
        buffer.put(short1(this.processIdentifier));
        buffer.put(short0(this.processIdentifier));
        buffer.put(int2(this.counter));
        buffer.put(int1(this.counter));
        buffer.put(int0(this.counter));
    }
    
    public int getTimestamp() {
        return this.timestamp;
    }
    
    public int getMachineIdentifier() {
        return this.machineIdentifier;
    }
    
    public short getProcessIdentifier() {
        return this.processIdentifier;
    }
    
    public int getCounter() {
        return this.counter;
    }
    
    public Date getDate() {
        return new Date(this.timestamp * 1000L);
    }
    
    public String toHexString() {
        final char[] chars = new char[24];
        int i = 0;
        for (final byte b : this.toByteArray()) {
            chars[i++] = ObjectId.HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = ObjectId.HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ObjectId objectId = (ObjectId)o;
        return this.counter == objectId.counter && this.machineIdentifier == objectId.machineIdentifier && this.processIdentifier == objectId.processIdentifier && this.timestamp == objectId.timestamp;
    }
    
    @Override
    public int hashCode() {
        int result = this.timestamp;
        result = 31 * result + this.machineIdentifier;
        result = 31 * result + this.processIdentifier;
        result = 31 * result + this.counter;
        return result;
    }
    
    @Override
    public int compareTo(final ObjectId other) {
        if (other == null) {
            throw new NullPointerException();
        }
        final byte[] byteArray = this.toByteArray();
        final byte[] otherByteArray = other.toByteArray();
        for (int i = 0; i < 12; ++i) {
            if (byteArray[i] != otherByteArray[i]) {
                return ((byteArray[i] & 0xFF) < (otherByteArray[i] & 0xFF)) ? -1 : 1;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return this.toHexString();
    }
    
    @Deprecated
    public int getTimeSecond() {
        return this.timestamp;
    }
    
    @Deprecated
    public long getTime() {
        return this.timestamp * 1000L;
    }
    
    @Deprecated
    public String toStringMongod() {
        return this.toHexString();
    }
    
    private static int createMachineIdentifier() {
        int machinePiece;
        try {
            final StringBuilder sb = new StringBuilder();
            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                final NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                final byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    final ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    }
                    catch (BufferUnderflowException ex) {}
                }
            }
            machinePiece = sb.toString().hashCode();
        }
        catch (Throwable t) {
            machinePiece = new SecureRandom().nextInt();
            ObjectId.LOGGER.log(Level.WARNING, "Failed to get machine identifier from network interface, using random number instead", t);
        }
        machinePiece &= 0xFFFFFF;
        return machinePiece;
    }
    
    private static short createProcessIdentifier() {
        short processId;
        try {
            final String processName = ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short)Integer.parseInt(processName.substring(0, processName.indexOf(64)));
            }
            else {
                processId = (short)ManagementFactory.getRuntimeMXBean().getName().hashCode();
            }
        }
        catch (Throwable t) {
            processId = (short)new SecureRandom().nextInt();
            ObjectId.LOGGER.log(Level.WARNING, "Failed to get process identifier from JMX, using random number instead", t);
        }
        return processId;
    }
    
    private static byte[] parseHexString(final String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an ObjectId: [" + s + "]");
        }
        final byte[] b = new byte[12];
        for (int i = 0; i < b.length; ++i) {
            b[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }
    
    private static int dateToTimestampSeconds(final Date time) {
        return (int)(time.getTime() / 1000L);
    }
    
    private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
        return b3 << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | (b0 & 0xFF);
    }
    
    private static byte int3(final int x) {
        return (byte)(x >> 24);
    }
    
    private static byte int2(final int x) {
        return (byte)(x >> 16);
    }
    
    private static byte int1(final int x) {
        return (byte)(x >> 8);
    }
    
    private static byte int0(final int x) {
        return (byte)x;
    }
    
    private static byte short1(final short x) {
        return (byte)(x >> 8);
    }
    
    private static byte short0(final short x) {
        return (byte)x;
    }
    
    static {
        LOGGER = Loggers.getLogger("ObjectId");
        NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());
        HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
