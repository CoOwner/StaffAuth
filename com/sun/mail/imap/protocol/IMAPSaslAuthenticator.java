package com.sun.mail.imap.protocol;

import javax.security.auth.callback.*;
import java.util.*;
import com.sun.mail.util.*;
import javax.security.sasl.*;
import java.io.*;
import com.sun.mail.iap.*;

public class IMAPSaslAuthenticator implements SaslAuthenticator
{
    private IMAPProtocol pr;
    private String name;
    private Properties props;
    private boolean debug;
    private PrintStream out;
    private String host;
    
    public IMAPSaslAuthenticator(final IMAPProtocol pr, final String name, final Properties props, final boolean debug, final PrintStream out, final String host) {
        this.pr = pr;
        this.name = name;
        this.props = props;
        this.debug = debug;
        this.out = out;
        this.host = host;
    }
    
    public boolean authenticate(final String[] mechs, final String realm, final String authzid, final String u, final String p) throws ProtocolException {
        final Vector v = new Vector();
        String tag = null;
        Response r = null;
        boolean done = false;
        if (this.debug) {
            this.out.print("IMAP SASL DEBUG: Mechanisms:");
            for (int i = 0; i < mechs.length; ++i) {
                this.out.print(" " + mechs[i]);
            }
            this.out.println();
        }
        final String r2 = realm;
        final String u2 = u;
        final String p2 = p;
        final CallbackHandler cbh = new CallbackHandler() {
            public void handle(final Callback[] callbacks) {
                if (IMAPSaslAuthenticator.this.debug) {
                    IMAPSaslAuthenticator.this.out.println("IMAP SASL DEBUG: callback length: " + callbacks.length);
                }
                for (int i = 0; i < callbacks.length; ++i) {
                    if (IMAPSaslAuthenticator.this.debug) {
                        IMAPSaslAuthenticator.this.out.println("IMAP SASL DEBUG: callback " + i + ": " + callbacks[i]);
                    }
                    if (callbacks[i] instanceof NameCallback) {
                        final NameCallback ncb = (NameCallback)callbacks[i];
                        ncb.setName(u2);
                    }
                    else if (callbacks[i] instanceof PasswordCallback) {
                        final PasswordCallback pcb = (PasswordCallback)callbacks[i];
                        pcb.setPassword(p2.toCharArray());
                    }
                    else if (callbacks[i] instanceof RealmCallback) {
                        final RealmCallback rcb = (RealmCallback)callbacks[i];
                        rcb.setText((r2 != null) ? r2 : rcb.getDefaultText());
                    }
                    else if (callbacks[i] instanceof RealmChoiceCallback) {
                        final RealmChoiceCallback rcb2 = (RealmChoiceCallback)callbacks[i];
                        if (r2 == null) {
                            rcb2.setSelectedIndex(rcb2.getDefaultChoice());
                        }
                        else {
                            final String[] choices = rcb2.getChoices();
                            for (int k = 0; k < choices.length; ++k) {
                                if (choices[k].equals(r2)) {
                                    rcb2.setSelectedIndex(k);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
        SaslClient sc;
        try {
            sc = Sasl.createSaslClient(mechs, authzid, this.name, this.host, (Map<String, ?>)this.props, cbh);
        }
        catch (SaslException sex) {
            if (this.debug) {
                this.out.println("IMAP SASL DEBUG: Failed to create SASL client: " + sex);
            }
            return false;
        }
        if (sc == null) {
            if (this.debug) {
                this.out.println("IMAP SASL DEBUG: No SASL support");
            }
            return false;
        }
        if (this.debug) {
            this.out.println("IMAP SASL DEBUG: SASL client " + sc.getMechanismName());
        }
        try {
            tag = this.pr.writeCommand("AUTHENTICATE " + sc.getMechanismName(), null);
        }
        catch (Exception ex) {
            if (this.debug) {
                this.out.println("IMAP SASL DEBUG: AUTHENTICATE Exception: " + ex);
            }
            return false;
        }
        final OutputStream os = this.pr.getIMAPOutputStream();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] CRLF = { 13, 10 };
        while (!done) {
            try {
                r = this.pr.readResponse();
                if (r.isContinuation()) {
                    byte[] ba = r.readByteArray().getNewBytes();
                    if (ba.length > 0) {
                        ba = BASE64DecoderStream.decode(ba);
                    }
                    if (this.debug) {
                        this.out.println("IMAP SASL DEBUG: challenge: " + ASCIIUtility.toString(ba, 0, ba.length) + " :");
                    }
                    ba = sc.evaluateChallenge(ba);
                    done = sc.isComplete();
                    if (ba == null) {
                        if (this.debug) {
                            this.out.println("IMAP SASL DEBUG: no response");
                        }
                        done = true;
                        os.write(CRLF);
                        os.flush();
                        bos.reset();
                    }
                    else {
                        if (this.debug) {
                            this.out.println("IMAP SASL DEBUG: response: " + ASCIIUtility.toString(ba, 0, ba.length) + " :");
                        }
                        ba = BASE64EncoderStream.encode(ba);
                        bos.write(ba);
                        bos.write(CRLF);
                        os.write(bos.toByteArray());
                        os.flush();
                        bos.reset();
                    }
                }
                else if (r.isTagged() && r.getTag().equals(tag)) {
                    done = true;
                }
                else if (r.isBYE()) {
                    done = true;
                }
                else {
                    v.addElement(r);
                }
            }
            catch (Exception ioex) {
                if (this.debug) {
                    ioex.printStackTrace();
                }
                r = Response.byeResponse(ioex);
                done = true;
            }
        }
        if (sc.isComplete()) {
            final String qop = (String)sc.getNegotiatedProperty("javax.security.sasl.qop");
            if (qop != null && (qop.equalsIgnoreCase("auth-int") || qop.equalsIgnoreCase("auth-conf"))) {
                if (this.debug) {
                    this.out.println("IMAP SASL DEBUG: Mechanism requires integrity or confidentiality");
                }
                return false;
            }
        }
        final Response[] responses = new Response[v.size()];
        v.copyInto(responses);
        this.pr.notifyResponseHandlers(responses);
        this.pr.handleResult(r);
        return true;
    }
}
