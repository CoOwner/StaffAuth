package com.sun.mail.iap;

import java.net.*;
import java.util.*;
import com.sun.mail.util.*;
import java.io.*;

public class Protocol
{
    protected String host;
    private Socket socket;
    protected boolean debug;
    protected boolean quote;
    protected PrintStream out;
    private TraceInputStream traceInput;
    private ResponseInputStream input;
    private TraceOutputStream traceOutput;
    private DataOutputStream output;
    private int tagCounter;
    private Vector handlers;
    private long timestamp;
    protected static final byte[] CRLF;
    
    public Protocol(final String host, final int port, final boolean debug, final PrintStream out, final Properties props, final String prefix, final boolean isSSL) throws IOException, ProtocolException {
        this.tagCounter = 0;
        this.handlers = null;
        this.host = host;
        this.debug = debug;
        this.out = out;
        this.socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
        final String s = props.getProperty("mail.debug.quote");
        this.quote = (s != null && s.equalsIgnoreCase("true"));
        this.initStreams(out);
        this.processGreeting(this.readResponse());
        this.timestamp = System.currentTimeMillis();
    }
    
    private void initStreams(final PrintStream out) throws IOException {
        (this.traceInput = new TraceInputStream(this.socket.getInputStream(), out)).setTrace(this.debug);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        (this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), out)).setTrace(this.debug);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
    }
    
    public Protocol(final InputStream in, final OutputStream out, final boolean debug) throws IOException {
        this.tagCounter = 0;
        this.handlers = null;
        this.host = "localhost";
        this.debug = debug;
        this.quote = false;
        this.out = System.out;
        (this.traceInput = new TraceInputStream(in, System.out)).setTrace(debug);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        (this.traceOutput = new TraceOutputStream(out, System.out)).setTrace(debug);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
        this.timestamp = System.currentTimeMillis();
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public synchronized void addResponseHandler(final ResponseHandler h) {
        if (this.handlers == null) {
            this.handlers = new Vector();
        }
        this.handlers.addElement(h);
    }
    
    public synchronized void removeResponseHandler(final ResponseHandler h) {
        if (this.handlers != null) {
            this.handlers.removeElement(h);
        }
    }
    
    public void notifyResponseHandlers(final Response[] responses) {
        if (this.handlers == null) {
            return;
        }
        for (int i = 0; i < responses.length; ++i) {
            final Response r = responses[i];
            if (r != null) {
                final int size = this.handlers.size();
                if (size == 0) {
                    return;
                }
                final Object[] h = new Object[size];
                this.handlers.copyInto(h);
                for (int j = 0; j < size; ++j) {
                    ((ResponseHandler)h[j]).handleResponse(r);
                }
            }
        }
    }
    
    protected void processGreeting(final Response r) throws ProtocolException {
        if (r.isBYE()) {
            throw new ConnectionException(this, r);
        }
    }
    
    protected ResponseInputStream getInputStream() {
        return this.input;
    }
    
    protected OutputStream getOutputStream() {
        return this.output;
    }
    
    protected boolean supportsNonSyncLiterals() {
        return false;
    }
    
    public Response readResponse() throws IOException, ProtocolException {
        return new Response(this);
    }
    
    public String writeCommand(final String command, final Argument args) throws IOException, ProtocolException {
        final String tag = "A" + Integer.toString(this.tagCounter++, 10);
        this.output.writeBytes(tag + " " + command);
        if (args != null) {
            this.output.write(32);
            args.write(this);
        }
        this.output.write(Protocol.CRLF);
        this.output.flush();
        return tag;
    }
    
    public synchronized Response[] command(final String command, final Argument args) {
        final Vector v = new Vector();
        boolean done = false;
        String tag = null;
        Response r = null;
        try {
            tag = this.writeCommand(command, args);
        }
        catch (LiteralException lex) {
            v.addElement(lex.getResponse());
            done = true;
        }
        catch (Exception ex) {
            v.addElement(Response.byeResponse(ex));
            done = true;
        }
        while (!done) {
            try {
                r = this.readResponse();
            }
            catch (IOException ioex) {
                r = Response.byeResponse(ioex);
            }
            catch (ProtocolException pex) {
                continue;
            }
            v.addElement(r);
            if (r.isBYE()) {
                done = true;
            }
            if (r.isTagged() && r.getTag().equals(tag)) {
                done = true;
            }
        }
        final Response[] responses = new Response[v.size()];
        v.copyInto(responses);
        this.timestamp = System.currentTimeMillis();
        return responses;
    }
    
    public void handleResult(final Response response) throws ProtocolException {
        if (response.isOK()) {
            return;
        }
        if (response.isNO()) {
            throw new CommandFailedException(response);
        }
        if (response.isBAD()) {
            throw new BadCommandException(response);
        }
        if (response.isBYE()) {
            this.disconnect();
            throw new ConnectionException(this, response);
        }
    }
    
    public void simpleCommand(final String cmd, final Argument args) throws ProtocolException {
        final Response[] r = this.command(cmd, args);
        this.notifyResponseHandlers(r);
        this.handleResult(r[r.length - 1]);
    }
    
    public void startTLS(final String cmd) throws IOException, ProtocolException {
        this.simpleCommand(cmd, null);
        this.socket = SocketFetcher.startTLS(this.socket);
        this.initStreams(this.out);
    }
    
    protected synchronized void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            }
            catch (IOException ex) {}
            this.socket = null;
        }
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        this.disconnect();
    }
    
    static {
        CRLF = new byte[] { 13, 10 };
    }
}
