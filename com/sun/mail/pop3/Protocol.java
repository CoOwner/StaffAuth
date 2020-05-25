package com.sun.mail.pop3;

import java.net.*;
import java.security.*;
import java.util.*;
import com.sun.mail.util.*;
import java.io.*;

class Protocol
{
    private Socket socket;
    private DataInputStream input;
    private PrintWriter output;
    private static final int POP3_PORT = 110;
    private static final String CRLF = "\r\n";
    private boolean debug;
    private PrintStream out;
    private String apopChallenge;
    private static char[] digits;
    
    Protocol(final String host, int port, final boolean debug, final PrintStream out, final Properties props, final String prefix, final boolean isSSL) throws IOException {
        this.debug = false;
        this.apopChallenge = null;
        this.debug = debug;
        this.out = out;
        final String apop = props.getProperty(prefix + ".apop.enable");
        final boolean enableAPOP = apop != null && apop.equalsIgnoreCase("true");
        Response r = null;
        try {
            if (port == -1) {
                port = 110;
            }
            if (debug) {
                out.println("DEBUG POP3: connecting to host \"" + host + "\", port " + port + ", isSSL " + isSSL);
            }
            this.socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
            this.input = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "iso-8859-1")));
            r = this.simpleCommand(null);
        }
        catch (IOException ioe) {
            try {
                this.socket.close();
            }
            finally {
                throw ioe;
            }
        }
        if (!r.ok) {
            try {
                this.socket.close();
            }
            finally {
                throw new IOException("Connect failed");
            }
        }
        if (enableAPOP) {
            final int challStart = r.data.indexOf(60);
            final int challEnd = r.data.indexOf(62, challStart);
            if (challStart != -1 && challEnd != -1) {
                this.apopChallenge = r.data.substring(challStart, challEnd + 1);
            }
            if (debug) {
                out.println("DEBUG POP3: APOP challenge: " + this.apopChallenge);
            }
        }
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.socket != null) {
            this.quit();
        }
    }
    
    synchronized String login(final String user, final String password) throws IOException {
        String dpw = null;
        if (this.apopChallenge != null) {
            dpw = this.getDigest(password);
        }
        Response r;
        if (this.apopChallenge != null && dpw != null) {
            r = this.simpleCommand("APOP " + user + " " + dpw);
        }
        else {
            r = this.simpleCommand("USER " + user);
            if (!r.ok) {
                return (r.data != null) ? r.data : "USER command failed";
            }
            r = this.simpleCommand("PASS " + password);
        }
        if (!r.ok) {
            return (r.data != null) ? r.data : "login failed";
        }
        return null;
    }
    
    private String getDigest(final String password) {
        final String key = this.apopChallenge + password;
        byte[] digest;
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest(key.getBytes("iso-8859-1"));
        }
        catch (NoSuchAlgorithmException nsae) {
            return null;
        }
        catch (UnsupportedEncodingException uee) {
            return null;
        }
        return toHex(digest);
    }
    
    private static String toHex(final byte[] bytes) {
        final char[] result = new char[bytes.length * 2];
        int index = 0;
        int i = 0;
        while (index < bytes.length) {
            final int temp = bytes[index] & 0xFF;
            result[i++] = Protocol.digits[temp >> 4];
            result[i++] = Protocol.digits[temp & 0xF];
            ++index;
        }
        return new String(result);
    }
    
    synchronized boolean quit() throws IOException {
        boolean ok = false;
        try {
            final Response r = this.simpleCommand("QUIT");
            ok = r.ok;
        }
        finally {
            try {
                this.socket.close();
            }
            finally {
                this.socket = null;
                this.input = null;
                this.output = null;
            }
        }
        return ok;
    }
    
    synchronized Status stat() throws IOException {
        final Response r = this.simpleCommand("STAT");
        final Status s = new Status();
        if (r.ok && r.data != null) {
            try {
                final StringTokenizer st = new StringTokenizer(r.data);
                s.total = Integer.parseInt(st.nextToken());
                s.size = Integer.parseInt(st.nextToken());
            }
            catch (Exception ex) {}
        }
        return s;
    }
    
    synchronized int list(final int msg) throws IOException {
        final Response r = this.simpleCommand("LIST " + msg);
        int size = -1;
        if (r.ok && r.data != null) {
            try {
                final StringTokenizer st = new StringTokenizer(r.data);
                st.nextToken();
                size = Integer.parseInt(st.nextToken());
            }
            catch (Exception ex) {}
        }
        return size;
    }
    
    synchronized InputStream list() throws IOException {
        final Response r = this.multilineCommand("LIST", 128);
        return r.bytes;
    }
    
    synchronized InputStream retr(final int msg, final int size) throws IOException {
        final Response r = this.multilineCommand("RETR " + msg, size);
        return r.bytes;
    }
    
    synchronized InputStream top(final int msg, final int n) throws IOException {
        final Response r = this.multilineCommand("TOP " + msg + " " + n, 0);
        return r.bytes;
    }
    
    synchronized boolean dele(final int msg) throws IOException {
        final Response r = this.simpleCommand("DELE " + msg);
        return r.ok;
    }
    
    synchronized String uidl(final int msg) throws IOException {
        final Response r = this.simpleCommand("UIDL " + msg);
        if (!r.ok) {
            return null;
        }
        final int i = r.data.indexOf(32);
        if (i > 0) {
            return r.data.substring(i + 1);
        }
        return null;
    }
    
    synchronized boolean uidl(final String[] uids) throws IOException {
        final Response r = this.multilineCommand("UIDL", 15 * uids.length);
        if (!r.ok) {
            return false;
        }
        final LineInputStream lis = new LineInputStream(r.bytes);
        String line = null;
        while ((line = lis.readLine()) != null) {
            final int i = line.indexOf(32);
            if (i >= 1) {
                if (i >= line.length()) {
                    continue;
                }
                final int n = Integer.parseInt(line.substring(0, i));
                if (n <= 0 || n > uids.length) {
                    continue;
                }
                uids[n - 1] = line.substring(i + 1);
            }
        }
        return true;
    }
    
    synchronized boolean noop() throws IOException {
        final Response r = this.simpleCommand("NOOP");
        return r.ok;
    }
    
    synchronized boolean rset() throws IOException {
        final Response r = this.simpleCommand("RSET");
        return r.ok;
    }
    
    private Response simpleCommand(String cmd) throws IOException {
        if (this.socket == null) {
            throw new IOException("Folder is closed");
        }
        if (cmd != null) {
            if (this.debug) {
                this.out.println("C: " + cmd);
            }
            cmd += "\r\n";
            this.output.print(cmd);
            this.output.flush();
        }
        final String line = this.input.readLine();
        if (line == null) {
            if (this.debug) {
                this.out.println("S: EOF");
            }
            throw new EOFException("EOF on socket");
        }
        if (this.debug) {
            this.out.println("S: " + line);
        }
        final Response r = new Response();
        if (line.startsWith("+OK")) {
            r.ok = true;
        }
        else {
            if (!line.startsWith("-ERR")) {
                throw new IOException("Unexpected response: " + line);
            }
            r.ok = false;
        }
        final int i;
        if ((i = line.indexOf(32)) >= 0) {
            r.data = line.substring(i + 1);
        }
        return r;
    }
    
    private Response multilineCommand(final String cmd, final int size) throws IOException {
        final Response r = this.simpleCommand(cmd);
        if (!r.ok) {
            return r;
        }
        final SharedByteArrayOutputStream buf = new SharedByteArrayOutputStream(size);
        int lastb = 10;
        int b;
        while ((b = this.input.read()) >= 0) {
            if (lastb == 10 && b == 46) {
                if (this.debug) {
                    this.out.write(b);
                }
                b = this.input.read();
                if (b == 13) {
                    if (this.debug) {
                        this.out.write(b);
                    }
                    b = this.input.read();
                    if (this.debug) {
                        this.out.write(b);
                        break;
                    }
                    break;
                }
            }
            buf.write(b);
            if (this.debug) {
                this.out.write(b);
            }
            lastb = b;
        }
        if (b < 0) {
            throw new EOFException("EOF on socket");
        }
        r.bytes = buf.toStream();
        return r;
    }
    
    static {
        Protocol.digits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
}
