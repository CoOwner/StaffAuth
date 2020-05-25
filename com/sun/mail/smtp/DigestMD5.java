package com.sun.mail.smtp;

import java.security.*;
import java.util.*;
import com.sun.mail.util.*;
import java.io.*;

public class DigestMD5
{
    private PrintStream debugout;
    private MessageDigest md5;
    private String uri;
    private String clientResponse;
    private static char[] digits;
    
    public DigestMD5(final PrintStream debugout) {
        this.debugout = debugout;
        if (debugout != null) {
            debugout.println("DEBUG DIGEST-MD5: Loaded");
        }
    }
    
    public byte[] authClient(final String host, final String user, final String passwd, String realm, final String serverChallenge) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
        SecureRandom random;
        try {
            random = new SecureRandom();
            this.md5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            if (this.debugout != null) {
                this.debugout.println("DEBUG DIGEST-MD5: " + ex);
            }
            throw new IOException(ex.toString());
        }
        final StringBuffer result = new StringBuffer();
        this.uri = "smtp/" + host;
        final String nc = "00000001";
        final String qop = "auth";
        final byte[] bytes = new byte[32];
        if (this.debugout != null) {
            this.debugout.println("DEBUG DIGEST-MD5: Begin authentication ...");
        }
        final Hashtable map = this.tokenize(serverChallenge);
        if (realm == null) {
            final String text = map.get("realm");
            realm = ((text != null) ? new StringTokenizer(text, ",").nextToken() : host);
        }
        final String nonce = map.get("nonce");
        random.nextBytes(bytes);
        b64os.write(bytes);
        b64os.flush();
        final String cnonce = bos.toString();
        bos.reset();
        this.md5.update(this.md5.digest(ASCIIUtility.getBytes(user + ":" + realm + ":" + passwd)));
        this.md5.update(ASCIIUtility.getBytes(":" + nonce + ":" + cnonce));
        this.clientResponse = toHex(this.md5.digest()) + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":";
        this.md5.update(ASCIIUtility.getBytes("AUTHENTICATE:" + this.uri));
        this.md5.update(ASCIIUtility.getBytes(this.clientResponse + toHex(this.md5.digest())));
        result.append("username=\"" + user + "\"");
        result.append(",realm=\"" + realm + "\"");
        result.append(",qop=" + qop);
        result.append(",nc=" + nc);
        result.append(",nonce=\"" + nonce + "\"");
        result.append(",cnonce=\"" + cnonce + "\"");
        result.append(",digest-uri=\"" + this.uri + "\"");
        result.append(",response=" + toHex(this.md5.digest()));
        if (this.debugout != null) {
            this.debugout.println("DEBUG DIGEST-MD5: Response => " + result.toString());
        }
        b64os.write(ASCIIUtility.getBytes(result.toString()));
        b64os.flush();
        return bos.toByteArray();
    }
    
    public boolean authServer(final String serverResponse) throws IOException {
        final Hashtable map = this.tokenize(serverResponse);
        this.md5.update(ASCIIUtility.getBytes(":" + this.uri));
        this.md5.update(ASCIIUtility.getBytes(this.clientResponse + toHex(this.md5.digest())));
        final String text = toHex(this.md5.digest());
        if (!text.equals(map.get("rspauth"))) {
            if (this.debugout != null) {
                this.debugout.println("DEBUG DIGEST-MD5: Expected => rspauth=" + text);
            }
            return false;
        }
        return true;
    }
    
    private Hashtable tokenize(final String serverResponse) throws IOException {
        final Hashtable map = new Hashtable();
        final byte[] bytes = serverResponse.getBytes();
        String key = null;
        final StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(new BASE64DecoderStream(new ByteArrayInputStream(bytes, 4, bytes.length - 4))));
        tokens.ordinaryChars(48, 57);
        tokens.wordChars(48, 57);
        int ttype;
        while ((ttype = tokens.nextToken()) != -1) {
            switch (ttype) {
                case -3: {
                    if (key == null) {
                        key = tokens.sval;
                        continue;
                    }
                }
                case 34: {
                    if (this.debugout != null) {
                        this.debugout.println("DEBUG DIGEST-MD5: Received => " + key + "='" + tokens.sval + "'");
                    }
                    if (map.containsKey(key)) {
                        map.put(key, map.get(key) + "," + tokens.sval);
                    }
                    else {
                        map.put(key, tokens.sval);
                    }
                    key = null;
                    continue;
                }
            }
        }
        return map;
    }
    
    private static String toHex(final byte[] bytes) {
        final char[] result = new char[bytes.length * 2];
        int index = 0;
        int i = 0;
        while (index < bytes.length) {
            final int temp = bytes[index] & 0xFF;
            result[i++] = DigestMD5.digits[temp >> 4];
            result[i++] = DigestMD5.digits[temp & 0xF];
            ++index;
        }
        return new String(result);
    }
    
    static {
        DigestMD5.digits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
}
