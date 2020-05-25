package com.mongodb.connection;

import com.mongodb.*;
import javax.security.sasl.*;
import com.mongodb.internal.authentication.*;
import java.io.*;
import java.security.spec.*;
import javax.crypto.spec.*;
import javax.crypto.*;
import java.security.*;
import java.util.*;

class ScramSha1Authenticator extends SaslAuthenticator
{
    private final RandomStringGenerator randomStringGenerator;
    
    ScramSha1Authenticator(final MongoCredential credential) {
        this(credential, new DefaultRandomStringGenerator());
    }
    
    ScramSha1Authenticator(final MongoCredential credential, final RandomStringGenerator randomStringGenerator) {
        super(credential);
        this.randomStringGenerator = randomStringGenerator;
    }
    
    @Override
    public String getMechanismName() {
        return AuthenticationMechanism.SCRAM_SHA_1.getMechanismName();
    }
    
    @Override
    protected SaslClient createSaslClient(final ServerAddress serverAddress) {
        return new ScramSha1SaslClient(this.getCredential(), this.randomStringGenerator);
    }
    
    private static class ScramSha1SaslClient implements SaslClient
    {
        private static final String GS2_HEADER = "n,,";
        private static final int RANDOM_LENGTH = 24;
        private final Base64Codec base64Codec;
        private final MongoCredential credential;
        private String clientFirstMessageBare;
        private final RandomStringGenerator randomStringGenerator;
        private String rPrefix;
        private byte[] serverSignature;
        private int step;
        
        ScramSha1SaslClient(final MongoCredential credential, final RandomStringGenerator randomStringGenerator) {
            this.credential = credential;
            this.base64Codec = new Base64Codec();
            this.randomStringGenerator = randomStringGenerator;
        }
        
        @Override
        public String getMechanismName() {
            return AuthenticationMechanism.SCRAM_SHA_1.getMechanismName();
        }
        
        @Override
        public boolean hasInitialResponse() {
            return true;
        }
        
        @Override
        public byte[] evaluateChallenge(final byte[] challenge) throws SaslException {
            if (this.step == 0) {
                ++this.step;
                return this.computeClientFirstMessage();
            }
            if (this.step == 1) {
                ++this.step;
                return this.computeClientFinalMessage(challenge);
            }
            if (this.step != 2) {
                throw new SaslException("Too many steps involved in the SCRAM-SHA-1 negotiation.");
            }
            ++this.step;
            final String serverResponse = this.encodeUTF8(challenge);
            final HashMap<String, String> map = this.parseServerResponse(serverResponse);
            if (!MessageDigest.isEqual(this.decodeBase64(map.get("v")), this.serverSignature)) {
                throw new SaslException("Server signature was invalid.");
            }
            return challenge;
        }
        
        @Override
        public boolean isComplete() {
            return this.step > 2;
        }
        
        @Override
        public byte[] unwrap(final byte[] incoming, final int offset, final int len) throws SaslException {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
        
        @Override
        public byte[] wrap(final byte[] outgoing, final int offset, final int len) throws SaslException {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
        
        @Override
        public Object getNegotiatedProperty(final String propName) {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
        
        @Override
        public void dispose() throws SaslException {
        }
        
        private byte[] computeClientFirstMessage() throws SaslException {
            final String userName = "n=" + this.prepUserName(this.credential.getUserName());
            this.rPrefix = this.randomStringGenerator.generate(24);
            final String nonce = "r=" + this.rPrefix;
            this.clientFirstMessageBare = userName + "," + nonce;
            final String clientFirstMessage = "n,," + this.clientFirstMessageBare;
            return this.decodeUTF8(clientFirstMessage);
        }
        
        private byte[] computeClientFinalMessage(final byte[] challenge) throws SaslException {
            final String serverFirstMessage = this.encodeUTF8(challenge);
            final HashMap<String, String> map = this.parseServerResponse(serverFirstMessage);
            final String r = map.get("r");
            if (!r.startsWith(this.rPrefix)) {
                throw new SaslException("Server sent an invalid nonce.");
            }
            final String s = map.get("s");
            final String i = map.get("i");
            final String channelBinding = "c=" + this.encodeBase64(this.decodeUTF8("n,,"));
            final String nonce = "r=" + r;
            final String clientFinalMessageWithoutProof = channelBinding + "," + nonce;
            final byte[] saltedPassword = this.hi(NativeAuthenticationHelper.createAuthenticationHash(this.credential.getUserName(), this.credential.getPassword()), this.decodeBase64(s), Integer.parseInt(i));
            final byte[] clientKey = this.hmac(saltedPassword, "Client Key");
            final byte[] storedKey = this.h(clientKey);
            final String authMessage = this.clientFirstMessageBare + "," + serverFirstMessage + "," + clientFinalMessageWithoutProof;
            final byte[] clientSignature = this.hmac(storedKey, authMessage);
            final byte[] clientProof = this.xor(clientKey, clientSignature);
            final byte[] serverKey = this.hmac(saltedPassword, "Server Key");
            this.serverSignature = this.hmac(serverKey, authMessage);
            final String proof = "p=" + this.encodeBase64(clientProof);
            final String clientFinalMessage = clientFinalMessageWithoutProof + "," + proof;
            return this.decodeUTF8(clientFinalMessage);
        }
        
        private byte[] decodeBase64(final String str) {
            return this.base64Codec.decode(str);
        }
        
        private byte[] decodeUTF8(final String str) throws SaslException {
            try {
                return str.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new SaslException("UTF-8 is not a supported encoding.", e);
            }
        }
        
        private String encodeBase64(final byte[] bytes) {
            return this.base64Codec.encode(bytes);
        }
        
        private String encodeUTF8(final byte[] bytes) throws SaslException {
            try {
                return new String(bytes, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new SaslException("UTF-8 is not a supported encoding.", e);
            }
        }
        
        private byte[] h(final byte[] data) throws SaslException {
            try {
                return MessageDigest.getInstance("SHA-1").digest(data);
            }
            catch (NoSuchAlgorithmException e) {
                throw new SaslException("SHA-1 could not be found.", e);
            }
        }
        
        private byte[] hi(final String password, final byte[] salt, final int iterations) throws SaslException {
            final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 160);
            SecretKeyFactory keyFactory;
            try {
                keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            }
            catch (NoSuchAlgorithmException e) {
                throw new SaslException("Unable to find PBKDF2WithHmacSHA1.", e);
            }
            try {
                return keyFactory.generateSecret(spec).getEncoded();
            }
            catch (InvalidKeySpecException e2) {
                throw new SaslException("Invalid key spec for PBKDC2WithHmacSHA1.", e2);
            }
        }
        
        private byte[] hmac(final byte[] bytes, final String key) throws SaslException {
            final SecretKeySpec signingKey = new SecretKeySpec(bytes, "HmacSHA1");
            Mac mac;
            try {
                mac = Mac.getInstance("HmacSHA1");
            }
            catch (NoSuchAlgorithmException e) {
                throw new SaslException("Could not find HmacSHA1.", e);
            }
            try {
                mac.init(signingKey);
            }
            catch (InvalidKeyException e2) {
                throw new SaslException("Could not initialize mac.", e2);
            }
            return mac.doFinal(this.decodeUTF8(key));
        }
        
        private HashMap<String, String> parseServerResponse(final String response) {
            final HashMap<String, String> map = new HashMap<String, String>();
            final String[] split;
            final String[] pairs = split = response.split(",");
            for (final String pair : split) {
                final String[] parts = pair.split("=", 2);
                map.put(parts[0], parts[1]);
            }
            return map;
        }
        
        private String prepUserName(final String userName) {
            return userName.replace("=", "=3D").replace(",", "=2D");
        }
        
        private byte[] xor(final byte[] a, final byte[] b) {
            final byte[] result = new byte[a.length];
            for (int i = 0; i < a.length; ++i) {
                result[i] = (byte)(a[i] ^ b[i]);
            }
            return result;
        }
    }
    
    public static class DefaultRandomStringGenerator implements RandomStringGenerator
    {
        @Override
        public String generate(final int length) {
            final int comma = 44;
            final int low = 33;
            final int high = 126;
            final int range = high - low;
            final Random random = new SecureRandom();
            final char[] text = new char[length];
            for (int i = 0; i < length; ++i) {
                int next;
                for (next = random.nextInt(range) + low; next == comma; next = random.nextInt(range) + low) {}
                text[i] = (char)next;
            }
            return new String(text);
        }
    }
    
    public interface RandomStringGenerator
    {
        String generate(final int p0);
    }
}
