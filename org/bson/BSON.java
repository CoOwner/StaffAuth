package org.bson;

import org.bson.util.*;
import java.util.concurrent.*;
import java.util.*;

public class BSON
{
    public static final byte EOO = 0;
    public static final byte NUMBER = 1;
    public static final byte STRING = 2;
    public static final byte OBJECT = 3;
    public static final byte ARRAY = 4;
    public static final byte BINARY = 5;
    public static final byte UNDEFINED = 6;
    public static final byte OID = 7;
    public static final byte BOOLEAN = 8;
    public static final byte DATE = 9;
    public static final byte NULL = 10;
    public static final byte REGEX = 11;
    public static final byte REF = 12;
    public static final byte CODE = 13;
    public static final byte SYMBOL = 14;
    public static final byte CODE_W_SCOPE = 15;
    public static final byte NUMBER_INT = 16;
    public static final byte TIMESTAMP = 17;
    public static final byte NUMBER_LONG = 18;
    public static final byte MINKEY = -1;
    public static final byte MAXKEY = Byte.MAX_VALUE;
    public static final byte B_GENERAL = 0;
    public static final byte B_FUNC = 1;
    public static final byte B_BINARY = 2;
    public static final byte B_UUID = 3;
    private static final int FLAG_GLOBAL = 256;
    private static final int[] FLAG_LOOKUP;
    private static volatile boolean encodeHooks;
    private static volatile boolean decodeHooks;
    private static final ClassMap<List<Transformer>> encodingHooks;
    private static final ClassMap<List<Transformer>> decodingHooks;
    
    public static boolean hasEncodeHooks() {
        return BSON.encodeHooks;
    }
    
    public static boolean hasDecodeHooks() {
        return BSON.decodeHooks;
    }
    
    public static void addEncodingHook(final Class<?> clazz, final Transformer transformer) {
        BSON.encodeHooks = true;
        List<Transformer> transformersForClass = BSON.encodingHooks.get(clazz);
        if (transformersForClass == null) {
            transformersForClass = new CopyOnWriteArrayList<Transformer>();
            BSON.encodingHooks.put(clazz, transformersForClass);
        }
        transformersForClass.add(transformer);
    }
    
    public static void addDecodingHook(final Class<?> clazz, final Transformer transformer) {
        BSON.decodeHooks = true;
        List<Transformer> transformersForClass = BSON.decodingHooks.get(clazz);
        if (transformersForClass == null) {
            transformersForClass = new CopyOnWriteArrayList<Transformer>();
            BSON.decodingHooks.put(clazz, transformersForClass);
        }
        transformersForClass.add(transformer);
    }
    
    public static Object applyEncodingHooks(final Object objectToEncode) {
        Object transformedObject = objectToEncode;
        if (!hasEncodeHooks() || objectToEncode == null || BSON.encodingHooks.size() == 0) {
            return transformedObject;
        }
        final List<Transformer> transformersForObject = BSON.encodingHooks.get(objectToEncode.getClass());
        if (transformersForObject != null) {
            for (final Transformer transformer : transformersForObject) {
                transformedObject = transformer.transform(objectToEncode);
            }
        }
        return transformedObject;
    }
    
    public static Object applyDecodingHooks(final Object objectToDecode) {
        Object transformedObject = objectToDecode;
        if (!hasDecodeHooks() || objectToDecode == null || BSON.decodingHooks.size() == 0) {
            return transformedObject;
        }
        final List<Transformer> transformersForObject = BSON.decodingHooks.get(objectToDecode.getClass());
        if (transformersForObject != null) {
            for (final Transformer transformer : transformersForObject) {
                transformedObject = transformer.transform(objectToDecode);
            }
        }
        return transformedObject;
    }
    
    public static List<Transformer> getEncodingHooks(final Class<?> clazz) {
        return BSON.encodingHooks.get(clazz);
    }
    
    public static void clearEncodingHooks() {
        BSON.encodeHooks = false;
        BSON.encodingHooks.clear();
    }
    
    public static void removeEncodingHooks(final Class<?> clazz) {
        BSON.encodingHooks.remove(clazz);
    }
    
    public static void removeEncodingHook(final Class<?> clazz, final Transformer transformer) {
        getEncodingHooks(clazz).remove(transformer);
    }
    
    public static List<Transformer> getDecodingHooks(final Class<?> clazz) {
        return BSON.decodingHooks.get(clazz);
    }
    
    public static void clearDecodingHooks() {
        BSON.decodeHooks = false;
        BSON.decodingHooks.clear();
    }
    
    public static void removeDecodingHooks(final Class<?> clazz) {
        BSON.decodingHooks.remove(clazz);
    }
    
    public static void removeDecodingHook(final Class<?> clazz, final Transformer transformer) {
        getDecodingHooks(clazz).remove(transformer);
    }
    
    public static void clearAllHooks() {
        clearEncodingHooks();
        clearDecodingHooks();
    }
    
    public static byte[] encode(final BSONObject doc) {
        return new BasicBSONEncoder().encode(doc);
    }
    
    public static BSONObject decode(final byte[] bytes) {
        return new BasicBSONDecoder().readObject(bytes);
    }
    
    public static int regexFlags(final String s) {
        int flags = 0;
        if (s == null) {
            return flags;
        }
        for (final char f : s.toLowerCase().toCharArray()) {
            flags |= regexFlag(f);
        }
        return flags;
    }
    
    public static int regexFlag(final char c) {
        final int flag = BSON.FLAG_LOOKUP[c];
        if (flag == 0) {
            throw new IllegalArgumentException(String.format("Unrecognized flag [%c]", c));
        }
        return flag;
    }
    
    public static String regexFlags(final int flags) {
        int processedFlags = flags;
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < BSON.FLAG_LOOKUP.length; ++i) {
            if ((processedFlags & BSON.FLAG_LOOKUP[i]) > 0) {
                buf.append((char)i);
                processedFlags -= BSON.FLAG_LOOKUP[i];
            }
        }
        if (processedFlags > 0) {
            throw new IllegalArgumentException("Some flags could not be recognized.");
        }
        return buf.toString();
    }
    
    public static int toInt(final Object number) {
        if (number == null) {
            throw new IllegalArgumentException("Argument shouldn't be null");
        }
        if (number instanceof Number) {
            return ((Number)number).intValue();
        }
        if (number instanceof Boolean) {
            return ((boolean)number) ? 1 : 0;
        }
        throw new IllegalArgumentException("Can't convert: " + number.getClass().getName() + " to int");
    }
    
    static {
        (FLAG_LOOKUP = new int[65535])[103] = 256;
        BSON.FLAG_LOOKUP[105] = 2;
        BSON.FLAG_LOOKUP[109] = 8;
        BSON.FLAG_LOOKUP[115] = 32;
        BSON.FLAG_LOOKUP[99] = 128;
        BSON.FLAG_LOOKUP[120] = 4;
        BSON.FLAG_LOOKUP[100] = 1;
        BSON.FLAG_LOOKUP[116] = 16;
        BSON.FLAG_LOOKUP[117] = 64;
        BSON.encodeHooks = false;
        BSON.decodeHooks = false;
        encodingHooks = new ClassMap<List<Transformer>>();
        decodingHooks = new ClassMap<List<Transformer>>();
    }
}
