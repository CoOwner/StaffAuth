package com.mongodb.internal.authentication;

import java.nio.charset.*;
import java.io.*;
import com.mongodb.internal.*;
import org.bson.*;

public final class NativeAuthenticationHelper
{
    private static final Charset UTF_8_CHARSET;
    
    public static String createAuthenticationHash(final String userName, final char[] password) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(userName.length() + 20 + password.length);
        try {
            bout.write(userName.getBytes(NativeAuthenticationHelper.UTF_8_CHARSET));
            bout.write(":mongo:".getBytes(NativeAuthenticationHelper.UTF_8_CHARSET));
            bout.write(new String(password).getBytes(NativeAuthenticationHelper.UTF_8_CHARSET));
        }
        catch (IOException ioe) {
            throw new RuntimeException("impossible", ioe);
        }
        return HexUtils.hexMD5(bout.toByteArray());
    }
    
    public static BsonDocument getAuthCommand(final String userName, final char[] password, final String nonce) {
        return getAuthCommand(userName, createAuthenticationHash(userName, password), nonce);
    }
    
    public static BsonDocument getAuthCommand(final String userName, final String authHash, final String nonce) {
        final String key = nonce + userName + authHash;
        final BsonDocument cmd = new BsonDocument();
        cmd.put("authenticate", new BsonInt32(1));
        cmd.put("user", new BsonString(userName));
        cmd.put("nonce", new BsonString(nonce));
        cmd.put("key", new BsonString(HexUtils.hexMD5(key.getBytes(NativeAuthenticationHelper.UTF_8_CHARSET))));
        return cmd;
    }
    
    public static BsonDocument getNonceCommand() {
        return new BsonDocument("getnonce", new BsonInt32(1));
    }
    
    private NativeAuthenticationHelper() {
    }
    
    static {
        UTF_8_CHARSET = Charset.forName("UTF-8");
    }
}
