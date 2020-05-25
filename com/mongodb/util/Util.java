package com.mongodb.util;

import com.mongodb.internal.*;
import java.nio.*;

public class Util
{
    public static String toHex(final byte[] bytes) {
        return HexUtils.toHex(bytes);
    }
    
    public static String hexMD5(final byte[] data) {
        return HexUtils.hexMD5(data);
    }
    
    public static String hexMD5(final ByteBuffer buf, final int offset, final int len) {
        return HexUtils.hexMD5(buf, offset, len);
    }
}
