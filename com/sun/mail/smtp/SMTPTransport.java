package com.sun.mail.smtp;

import java.net.*;
import javax.mail.*;
import javax.mail.internet.*;
import com.sun.mail.util.*;
import java.io.*;
import java.util.*;

public class SMTPTransport extends Transport
{
    private String name;
    private int defaultPort;
    private boolean isSSL;
    private MimeMessage message;
    private Address[] addresses;
    private Address[] validSentAddr;
    private Address[] validUnsentAddr;
    private Address[] invalidAddr;
    boolean sendPartiallyFailed;
    MessagingException exception;
    private Hashtable extMap;
    private boolean quitWait;
    private String saslRealm;
    private boolean reportSuccess;
    private boolean useStartTLS;
    private boolean useRset;
    private PrintStream out;
    private String localHostName;
    private String lastServerResponse;
    private int lastReturnCode;
    private static final String[] ignoreList;
    private static final byte[] CRLF;
    private static final String UNKNOWN = "UNKNOWN";
    private DigestMD5 md5support;
    private BufferedInputStream serverInput;
    private LineInputStream lineInputStream;
    private OutputStream serverOutput;
    private Socket serverSocket;
    private static char[] hexchar;
    
    public SMTPTransport(final Session session, final URLName urlname) {
        this(session, urlname, "smtp", 25, false);
    }
    
    protected SMTPTransport(final Session session, final URLName urlname, String name, final int defaultPort, final boolean isSSL) {
        super(session, urlname);
        this.name = "smtp";
        this.defaultPort = 25;
        this.isSSL = false;
        this.sendPartiallyFailed = false;
        this.quitWait = false;
        this.saslRealm = "UNKNOWN";
        if (urlname != null) {
            name = urlname.getProtocol();
        }
        this.name = name;
        this.defaultPort = defaultPort;
        this.isSSL = isSSL;
        this.out = session.getDebugOut();
        String s = session.getProperty("mail." + name + ".quitwait");
        this.quitWait = (s == null || s.equalsIgnoreCase("true"));
        s = session.getProperty("mail." + name + ".reportsuccess");
        this.reportSuccess = (s != null && s.equalsIgnoreCase("true"));
        s = session.getProperty("mail." + name + ".starttls.enable");
        this.useStartTLS = (s != null && s.equalsIgnoreCase("true"));
        s = session.getProperty("mail." + name + ".userset");
        this.useRset = (s != null && s.equalsIgnoreCase("true"));
    }
    
    public String getLocalHost() {
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                this.localHostName = this.session.getProperty("mail." + this.name + ".localhost");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                this.localHostName = this.session.getProperty("mail." + this.name + ".localaddress");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                final InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        }
        catch (UnknownHostException ex) {}
        return this.localHostName;
    }
    
    public void setLocalHost(final String localhost) {
        this.localHostName = localhost;
    }
    
    public synchronized void connect(final Socket socket) throws MessagingException {
        this.serverSocket = socket;
        super.connect();
    }
    
    public String getSASLRealm() {
        if (this.saslRealm == "UNKNOWN") {
            this.saslRealm = this.session.getProperty("mail." + this.name + ".sasl.realm");
            if (this.saslRealm == null) {
                this.saslRealm = this.session.getProperty("mail." + this.name + ".saslrealm");
            }
        }
        return this.saslRealm;
    }
    
    public void setSASLRealm(final String saslRealm) {
        this.saslRealm = saslRealm;
    }
    
    public boolean getReportSuccess() {
        return this.reportSuccess;
    }
    
    public void setReportSuccess(final boolean reportSuccess) {
        this.reportSuccess = reportSuccess;
    }
    
    public boolean getStartTLS() {
        return this.useStartTLS;
    }
    
    public void setStartTLS(final boolean useStartTLS) {
        this.useStartTLS = useStartTLS;
    }
    
    public boolean getUseRset() {
        return this.useRset;
    }
    
    public void setUseRset(final boolean useRset) {
        this.useRset = useRset;
    }
    
    public String getLastServerResponse() {
        return this.lastServerResponse;
    }
    
    private synchronized DigestMD5 getMD5() {
        if (this.md5support == null) {
            this.md5support = new DigestMD5(this.debug ? this.out : null);
        }
        return this.md5support;
    }
    
    protected boolean protocolConnect(String host, int port, final String user, final String passwd) throws MessagingException {
        final String ehloStr = this.session.getProperty("mail." + this.name + ".ehlo");
        final boolean useEhlo = ehloStr == null || !ehloStr.equalsIgnoreCase("false");
        final String authStr = this.session.getProperty("mail." + this.name + ".auth");
        final boolean useAuth = authStr != null && authStr.equalsIgnoreCase("true");
        if (this.debug) {
            this.out.println("DEBUG SMTP: useEhlo " + useEhlo + ", useAuth " + useAuth);
        }
        if (useAuth && (user == null || passwd == null)) {
            return false;
        }
        if (port == -1) {
            final String portstring = this.session.getProperty("mail." + this.name + ".port");
            if (portstring != null) {
                port = Integer.parseInt(portstring);
            }
            else {
                port = this.defaultPort;
            }
        }
        if (host == null || host.length() == 0) {
            host = "localhost";
        }
        boolean succeed = false;
        if (this.serverSocket != null) {
            this.openServer();
        }
        else {
            this.openServer(host, port);
        }
        if (useEhlo) {
            succeed = this.ehlo(this.getLocalHost());
        }
        if (!succeed) {
            this.helo(this.getLocalHost());
        }
        if (this.useStartTLS && this.supportsExtension("STARTTLS")) {
            this.startTLS();
            this.ehlo(this.getLocalHost());
        }
        if (useAuth && (this.supportsExtension("AUTH") || this.supportsExtension("AUTH=LOGIN"))) {
            if (this.debug) {
                this.out.println("DEBUG SMTP: Attempt to authenticate");
                if (!this.supportsAuthentication("LOGIN") && this.supportsExtension("AUTH=LOGIN")) {
                    this.out.println("DEBUG SMTP: use AUTH=LOGIN hack");
                }
            }
            if (this.supportsAuthentication("LOGIN") || this.supportsExtension("AUTH=LOGIN")) {
                int resp = this.simpleCommand("AUTH LOGIN");
                if (resp == 530) {
                    this.startTLS();
                    resp = this.simpleCommand("AUTH LOGIN");
                }
                try {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    final OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
                    if (resp == 334) {
                        b64os.write(ASCIIUtility.getBytes(user));
                        b64os.flush();
                        resp = this.simpleCommand(bos.toByteArray());
                        bos.reset();
                    }
                    if (resp == 334) {
                        b64os.write(ASCIIUtility.getBytes(passwd));
                        b64os.flush();
                        resp = this.simpleCommand(bos.toByteArray());
                        bos.reset();
                    }
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
                catch (IOException ex2) {
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
                finally {
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
            }
            else if (this.supportsAuthentication("PLAIN")) {
                int resp = this.simpleCommand("AUTH PLAIN");
                try {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    final OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
                    if (resp == 334) {
                        b64os.write(0);
                        b64os.write(ASCIIUtility.getBytes(user));
                        b64os.write(0);
                        b64os.write(ASCIIUtility.getBytes(passwd));
                        b64os.flush();
                        resp = this.simpleCommand(bos.toByteArray());
                    }
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
                catch (IOException ex2) {
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
                finally {
                    if (resp != 235) {
                        this.closeConnection();
                        return false;
                    }
                }
            }
            else {
                final DigestMD5 md5;
                if (this.supportsAuthentication("DIGEST-MD5") && (md5 = this.getMD5()) != null) {
                    int resp = this.simpleCommand("AUTH DIGEST-MD5");
                    try {
                        if (resp == 334) {
                            final byte[] b = md5.authClient(host, user, passwd, this.getSASLRealm(), this.lastServerResponse);
                            resp = this.simpleCommand(b);
                            if (resp == 334) {
                                if (!md5.authServer(this.lastServerResponse)) {
                                    resp = -1;
                                }
                                else {
                                    resp = this.simpleCommand(new byte[0]);
                                }
                            }
                        }
                        if (resp != 235) {
                            this.closeConnection();
                            return false;
                        }
                    }
                    catch (Exception ex) {
                        if (this.debug) {
                            this.out.println("DEBUG SMTP: DIGEST-MD5: " + ex);
                        }
                        if (resp != 235) {
                            this.closeConnection();
                            return false;
                        }
                    }
                    finally {
                        if (resp != 235) {
                            this.closeConnection();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public synchronized void sendMessage(final Message message, final Address[] addresses) throws MessagingException, SendFailedException {
        this.checkConnected();
        if (!(message instanceof MimeMessage)) {
            if (this.debug) {
                this.out.println("DEBUG SMTP: Can only send RFC822 msgs");
            }
            throw new MessagingException("SMTP can only send RFC822 messages");
        }
        for (int i = 0; i < addresses.length; ++i) {
            if (!(addresses[i] instanceof InternetAddress)) {
                throw new MessagingException(addresses[i] + " is not an InternetAddress");
            }
        }
        this.message = (MimeMessage)message;
        this.addresses = addresses;
        this.validUnsentAddr = addresses;
        this.expandGroups();
        boolean use8bit = false;
        if (message instanceof SMTPMessage) {
            use8bit = ((SMTPMessage)message).getAllow8bitMIME();
        }
        if (!use8bit) {
            final String ebStr = this.session.getProperty("mail." + this.name + ".allow8bitmime");
            use8bit = (ebStr != null && ebStr.equalsIgnoreCase("true"));
        }
        if (this.debug) {
            this.out.println("DEBUG SMTP: use8bit " + use8bit);
        }
        while (true) {
            if (use8bit && this.supportsExtension("8BITMIME")) {
                this.convertTo8Bit(this.message);
                try {
                    this.mailFrom();
                    this.rcptTo();
                    this.message.writeTo(this.data(), SMTPTransport.ignoreList);
                    this.finishData();
                    if (this.sendPartiallyFailed) {
                        if (this.debug) {
                            this.out.println("DEBUG SMTP: Sending partially failed because of invalid destination addresses");
                        }
                        this.notifyTransportListeners(3, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
                        throw new SMTPSendFailedException(".", this.lastReturnCode, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
                    }
                    this.notifyTransportListeners(1, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
                }
                catch (MessagingException mex) {
                    if (this.debug) {
                        mex.printStackTrace(this.out);
                    }
                    this.notifyTransportListeners(2, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
                    throw mex;
                }
                catch (IOException ex) {
                    if (this.debug) {
                        ex.printStackTrace(this.out);
                    }
                    try {
                        this.closeConnection();
                    }
                    catch (MessagingException ex2) {}
                    this.notifyTransportListeners(2, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
                    throw new MessagingException("IOException while sending message", ex);
                }
                finally {
                    final Address[] validSentAddr = null;
                    this.invalidAddr = validSentAddr;
                    this.validUnsentAddr = validSentAddr;
                    this.validSentAddr = validSentAddr;
                    this.addresses = null;
                    this.message = null;
                    this.exception = null;
                    this.sendPartiallyFailed = false;
                }
                return;
            }
            continue;
        }
    }
    
    public synchronized void close() throws MessagingException {
        if (!super.isConnected()) {
            return;
        }
        try {
            if (this.serverSocket != null) {
                this.sendCommand("QUIT");
                if (this.quitWait) {
                    final int resp = this.readServerResponse();
                    if (resp != 221 && resp != -1) {
                        this.out.println("DEBUG SMTP: QUIT failed with " + resp);
                    }
                }
            }
        }
        finally {
            this.closeConnection();
        }
    }
    
    private void closeConnection() throws MessagingException {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        }
        catch (IOException ioex) {
            throw new MessagingException("Server Close Failed", ioex);
        }
        finally {
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
        }
    }
    
    public synchronized boolean isConnected() {
        if (!super.isConnected()) {
            return false;
        }
        try {
            if (this.useRset) {
                this.sendCommand("RSET");
            }
            else {
                this.sendCommand("NOOP");
            }
            final int resp = this.readServerResponse();
            if (resp >= 0 && resp != 421) {
                return true;
            }
            try {
                this.closeConnection();
            }
            catch (MessagingException ex2) {}
            return false;
        }
        catch (Exception ex) {
            try {
                this.closeConnection();
            }
            catch (MessagingException ex3) {}
            return false;
        }
    }
    
    private void expandGroups() {
        Vector groups = null;
        for (int i = 0; i < this.addresses.length; ++i) {
            final InternetAddress a = (InternetAddress)this.addresses[i];
            if (a.isGroup()) {
                if (groups == null) {
                    groups = new Vector();
                    for (int k = 0; k < i; ++k) {
                        groups.addElement(this.addresses[k]);
                    }
                }
                try {
                    final InternetAddress[] ia = a.getGroup(true);
                    if (ia != null) {
                        for (int j = 0; j < ia.length; ++j) {
                            groups.addElement(ia[j]);
                        }
                    }
                    else {
                        groups.addElement(a);
                    }
                }
                catch (ParseException pex) {
                    groups.addElement(a);
                }
            }
            else if (groups != null) {
                groups.addElement(a);
            }
        }
        if (groups != null) {
            final InternetAddress[] newa = new InternetAddress[groups.size()];
            groups.copyInto(newa);
            this.addresses = newa;
        }
    }
    
    private void convertTo8Bit(final MimePart part) {
        try {
            if (part.isMimeType("text/*")) {
                final String enc = part.getEncoding();
                if (enc.equalsIgnoreCase("quoted-printable") || enc.equalsIgnoreCase("base64")) {
                    final InputStream is = part.getInputStream();
                    if (this.is8Bit(is)) {
                        part.setHeader("Content-Transfer-Encoding", "8bit");
                    }
                }
            }
            else if (part.isMimeType("multipart/*")) {
                final MimeMultipart mp = (MimeMultipart)part.getContent();
                for (int count = mp.getCount(), i = 0; i < count; ++i) {
                    this.convertTo8Bit((MimePart)mp.getBodyPart(i));
                }
            }
        }
        catch (IOException ioex) {}
        catch (MessagingException ex) {}
    }
    
    private boolean is8Bit(final InputStream is) {
        int linelen = 0;
        boolean need8bit = false;
        try {
            int b;
            while ((b = is.read()) >= 0) {
                b &= 0xFF;
                if (b == 13 || b == 10) {
                    linelen = 0;
                }
                else {
                    if (b == 0) {
                        return false;
                    }
                    if (++linelen > 998) {
                        return false;
                    }
                }
                if (b > 127) {
                    need8bit = true;
                }
            }
        }
        catch (IOException ex) {
            return false;
        }
        if (this.debug && need8bit) {
            this.out.println("DEBUG SMTP: found an 8bit part");
        }
        return need8bit;
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            this.closeConnection();
        }
        catch (MessagingException ex) {}
    }
    
    private void helo(final String domain) throws MessagingException {
        if (domain != null) {
            this.issueCommand("HELO " + domain, 250);
        }
        else {
            this.issueCommand("HELO", 250);
        }
    }
    
    private boolean ehlo(final String domain) throws MessagingException {
        String cmd;
        if (domain != null) {
            cmd = "EHLO " + domain;
        }
        else {
            cmd = "EHLO";
        }
        this.sendCommand(cmd);
        final int resp = this.readServerResponse();
        if (resp == 250) {
            final BufferedReader rd = new BufferedReader(new StringReader(this.lastServerResponse));
            this.extMap = new Hashtable();
            try {
                boolean first = true;
                String line;
                while ((line = rd.readLine()) != null) {
                    if (first) {
                        first = false;
                    }
                    else {
                        if (line.length() < 5) {
                            continue;
                        }
                        line = line.substring(4);
                        final int i = line.indexOf(32);
                        String arg = "";
                        if (i > 0) {
                            arg = line.substring(i + 1);
                            line = line.substring(0, i);
                        }
                        if (this.debug) {
                            this.out.println("DEBUG SMTP: Found extension \"" + line + "\", arg \"" + arg + "\"");
                        }
                        this.extMap.put(line.toUpperCase(), arg);
                    }
                }
            }
            catch (IOException ex) {}
        }
        return resp == 250;
    }
    
    private void mailFrom() throws MessagingException {
        String from = null;
        if (this.message instanceof SMTPMessage) {
            from = ((SMTPMessage)this.message).getEnvelopeFrom();
        }
        if (from == null || from.length() <= 0) {
            from = this.session.getProperty("mail." + this.name + ".from");
        }
        if (from == null || from.length() <= 0) {
            final Address[] fa;
            Address me;
            if (this.message != null && (fa = this.message.getFrom()) != null && fa.length > 0) {
                me = fa[0];
            }
            else {
                me = InternetAddress.getLocalAddress(this.session);
            }
            if (me == null) {
                throw new MessagingException("can't determine local email address");
            }
            from = ((InternetAddress)me).getAddress();
        }
        String cmd = "MAIL FROM:" + this.normalizeAddress(from);
        if (this.supportsExtension("DSN")) {
            String ret = null;
            if (this.message instanceof SMTPMessage) {
                ret = ((SMTPMessage)this.message).getDSNRet();
            }
            if (ret == null) {
                ret = this.session.getProperty("mail." + this.name + ".dsn.ret");
            }
            if (ret != null) {
                cmd = cmd + " RET=" + ret;
            }
        }
        if (this.supportsExtension("AUTH")) {
            String submitter = null;
            if (this.message instanceof SMTPMessage) {
                submitter = ((SMTPMessage)this.message).getSubmitter();
            }
            if (submitter == null) {
                submitter = this.session.getProperty("mail." + this.name + ".submitter");
            }
            if (submitter != null) {
                try {
                    final String s = this.xtext(submitter);
                    cmd = cmd + " AUTH=" + s;
                }
                catch (IllegalArgumentException ex) {
                    if (this.debug) {
                        this.out.println("DEBUG SMTP: ignoring invalid submitter: " + submitter + ", Exception: " + ex);
                    }
                }
            }
        }
        String ext = null;
        if (this.message instanceof SMTPMessage) {
            ext = ((SMTPMessage)this.message).getMailExtension();
        }
        if (ext == null) {
            ext = this.session.getProperty("mail." + this.name + ".mailextension");
        }
        if (ext != null && ext.length() > 0) {
            cmd = cmd + " " + ext;
        }
        this.issueSendCommand(cmd, 250);
    }
    
    private void rcptTo() throws MessagingException {
        final Vector valid = new Vector();
        final Vector validUnsent = new Vector();
        final Vector invalid = new Vector();
        int retCode = -1;
        MessagingException mex = null;
        boolean sendFailed = false;
        MessagingException sfex = null;
        final Address[] validSentAddr = null;
        this.invalidAddr = validSentAddr;
        this.validUnsentAddr = validSentAddr;
        this.validSentAddr = validSentAddr;
        boolean sendPartial = false;
        if (this.message instanceof SMTPMessage) {
            sendPartial = ((SMTPMessage)this.message).getSendPartial();
        }
        if (!sendPartial) {
            final String sp = this.session.getProperty("mail." + this.name + ".sendpartial");
            sendPartial = (sp != null && sp.equalsIgnoreCase("true"));
        }
        if (this.debug && sendPartial) {
            this.out.println("DEBUG SMTP: sendPartial set");
        }
        boolean dsn = false;
        String notify = null;
        if (this.supportsExtension("DSN")) {
            if (this.message instanceof SMTPMessage) {
                notify = ((SMTPMessage)this.message).getDSNNotify();
            }
            if (notify == null) {
                notify = this.session.getProperty("mail." + this.name + ".dsn.notify");
            }
            if (notify != null) {
                dsn = true;
            }
        }
        for (int i = 0; i < this.addresses.length; ++i) {
            sfex = null;
            final InternetAddress ia = (InternetAddress)this.addresses[i];
            String cmd = "RCPT TO:" + this.normalizeAddress(ia.getAddress());
            if (dsn) {
                cmd = cmd + " NOTIFY=" + notify;
            }
            this.sendCommand(cmd);
            retCode = this.readServerResponse();
            switch (retCode) {
                case 250:
                case 251: {
                    valid.addElement(ia);
                    if (!this.reportSuccess) {
                        break;
                    }
                    sfex = new SMTPAddressSucceededException(ia, cmd, retCode, this.lastServerResponse);
                    if (mex == null) {
                        mex = sfex;
                        break;
                    }
                    mex.setNextException(sfex);
                    break;
                }
                case 501:
                case 503:
                case 550:
                case 551:
                case 553: {
                    if (!sendPartial) {
                        sendFailed = true;
                    }
                    invalid.addElement(ia);
                    sfex = new SMTPAddressFailedException(ia, cmd, retCode, this.lastServerResponse);
                    if (mex == null) {
                        mex = sfex;
                        break;
                    }
                    mex.setNextException(sfex);
                    break;
                }
                case 450:
                case 451:
                case 452:
                case 552: {
                    if (!sendPartial) {
                        sendFailed = true;
                    }
                    validUnsent.addElement(ia);
                    sfex = new SMTPAddressFailedException(ia, cmd, retCode, this.lastServerResponse);
                    if (mex == null) {
                        mex = sfex;
                        break;
                    }
                    mex.setNextException(sfex);
                    break;
                }
                default: {
                    if (retCode >= 400 && retCode <= 499) {
                        validUnsent.addElement(ia);
                    }
                    else {
                        if (retCode < 500 || retCode > 599) {
                            if (this.debug) {
                                this.out.println("DEBUG SMTP: got response code " + retCode + ", with response: " + this.lastServerResponse);
                            }
                            final String _lsr = this.lastServerResponse;
                            final int _lrc = this.lastReturnCode;
                            if (this.serverSocket != null) {
                                this.issueCommand("RSET", 250);
                            }
                            this.lastServerResponse = _lsr;
                            this.lastReturnCode = _lrc;
                            throw new SMTPAddressFailedException(ia, cmd, retCode, _lsr);
                        }
                        invalid.addElement(ia);
                    }
                    if (!sendPartial) {
                        sendFailed = true;
                    }
                    sfex = new SMTPAddressFailedException(ia, cmd, retCode, this.lastServerResponse);
                    if (mex == null) {
                        mex = sfex;
                        break;
                    }
                    mex.setNextException(sfex);
                    break;
                }
            }
        }
        if (sendPartial && valid.size() == 0) {
            sendFailed = true;
        }
        if (sendFailed) {
            invalid.copyInto(this.invalidAddr = new Address[invalid.size()]);
            this.validUnsentAddr = new Address[valid.size() + validUnsent.size()];
            int i = 0;
            for (int j = 0; j < valid.size(); ++j) {
                this.validUnsentAddr[i++] = valid.elementAt(j);
            }
            for (int j = 0; j < validUnsent.size(); ++j) {
                this.validUnsentAddr[i++] = validUnsent.elementAt(j);
            }
        }
        else if (this.reportSuccess || (sendPartial && (invalid.size() > 0 || validUnsent.size() > 0))) {
            this.sendPartiallyFailed = true;
            this.exception = mex;
            invalid.copyInto(this.invalidAddr = new Address[invalid.size()]);
            validUnsent.copyInto(this.validUnsentAddr = new Address[validUnsent.size()]);
            valid.copyInto(this.validSentAddr = new Address[valid.size()]);
        }
        else {
            this.validSentAddr = this.addresses;
        }
        if (this.debug) {
            if (this.validSentAddr != null && this.validSentAddr.length > 0) {
                this.out.println("DEBUG SMTP: Verified Addresses");
                for (int l = 0; l < this.validSentAddr.length; ++l) {
                    this.out.println("DEBUG SMTP:   " + this.validSentAddr[l]);
                }
            }
            if (this.validUnsentAddr != null && this.validUnsentAddr.length > 0) {
                this.out.println("DEBUG SMTP: Valid Unsent Addresses");
                for (int k = 0; k < this.validUnsentAddr.length; ++k) {
                    this.out.println("DEBUG SMTP:   " + this.validUnsentAddr[k]);
                }
            }
            if (this.invalidAddr != null && this.invalidAddr.length > 0) {
                this.out.println("DEBUG SMTP: Invalid Addresses");
                for (int m = 0; m < this.invalidAddr.length; ++m) {
                    this.out.println("DEBUG SMTP:   " + this.invalidAddr[m]);
                }
            }
        }
        if (sendFailed) {
            if (this.debug) {
                this.out.println("DEBUG SMTP: Sending failed because of invalid destination addresses");
            }
            this.notifyTransportListeners(2, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
            final String lsr = this.lastServerResponse;
            final int lrc = this.lastReturnCode;
            try {
                if (this.serverSocket != null) {
                    this.issueCommand("RSET", 250);
                }
            }
            catch (MessagingException ex3) {
                try {
                    this.close();
                }
                catch (MessagingException ex2) {
                    if (this.debug) {
                        ex2.printStackTrace(this.out);
                    }
                }
            }
            finally {
                this.lastServerResponse = lsr;
                this.lastReturnCode = lrc;
            }
            throw new SendFailedException("Invalid Addresses", mex, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
        }
    }
    
    private OutputStream data() throws MessagingException {
        this.issueSendCommand("DATA", 354);
        return new SMTPOutputStream(this.serverOutput);
    }
    
    private void finishData() throws MessagingException {
        this.issueSendCommand("\r\n.", 250);
    }
    
    private void startTLS() throws MessagingException {
        this.issueCommand("STARTTLS", 220);
        try {
            this.serverSocket = SocketFetcher.startTLS(this.serverSocket);
            this.initStreams();
        }
        catch (IOException ioex) {
            this.closeConnection();
            throw new MessagingException("Could not convert socket to TLS", ioex);
        }
    }
    
    private void openServer(final String server, int port) throws MessagingException {
        if (this.debug) {
            this.out.println("DEBUG SMTP: trying to connect to host \"" + server + "\", port " + port + ", isSSL " + this.isSSL);
        }
        try {
            final Properties props = this.session.getProperties();
            this.serverSocket = SocketFetcher.getSocket(server, port, props, "mail." + this.name, this.isSSL);
            port = this.serverSocket.getPort();
            this.initStreams();
            int r = -1;
            if ((r = this.readServerResponse()) != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    this.out.println("DEBUG SMTP: could not connect to host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port + ", response: " + r);
            }
            if (this.debug) {
                this.out.println("DEBUG SMTP: connected to host \"" + server + "\", port: " + port + "\n");
            }
        }
        catch (UnknownHostException uhex) {
            throw new MessagingException("Unknown SMTP host: " + server, uhex);
        }
        catch (IOException ioe) {
            throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port, ioe);
        }
    }
    
    private void openServer() throws MessagingException {
        int port = -1;
        String server = "UNKNOWN";
        try {
            port = this.serverSocket.getPort();
            server = this.serverSocket.getInetAddress().getHostName();
            if (this.debug) {
                this.out.println("DEBUG SMTP: starting protocol to host \"" + server + "\", port " + port);
            }
            this.initStreams();
            int r = -1;
            if ((r = this.readServerResponse()) != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    this.out.println("DEBUG SMTP: got bad greeting from host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Got bad greeting from SMTP host: " + server + ", port: " + port + ", response: " + r);
            }
            if (this.debug) {
                this.out.println("DEBUG SMTP: protocol started to host \"" + server + "\", port: " + port + "\n");
            }
        }
        catch (IOException ioe) {
            throw new MessagingException("Could not start protocol to SMTP host: " + server + ", port: " + port, ioe);
        }
    }
    
    private void initStreams() throws IOException {
        final Properties props = this.session.getProperties();
        final PrintStream out = this.session.getDebugOut();
        final boolean debug = this.session.getDebug();
        final String s = props.getProperty("mail.debug.quote");
        final boolean quote = s != null && s.equalsIgnoreCase("true");
        final TraceInputStream traceInput = new TraceInputStream(this.serverSocket.getInputStream(), out);
        traceInput.setTrace(debug);
        traceInput.setQuote(quote);
        final TraceOutputStream traceOutput = new TraceOutputStream(this.serverSocket.getOutputStream(), out);
        traceOutput.setTrace(debug);
        traceOutput.setQuote(quote);
        this.serverOutput = new BufferedOutputStream(traceOutput);
        this.serverInput = new BufferedInputStream(traceInput);
        this.lineInputStream = new LineInputStream(this.serverInput);
    }
    
    private void issueCommand(final String cmd, final int expect) throws MessagingException {
        this.sendCommand(cmd);
        if (this.readServerResponse() != expect) {
            throw new MessagingException(this.lastServerResponse);
        }
    }
    
    private void issueSendCommand(final String cmd, final int expect) throws MessagingException {
        this.sendCommand(cmd);
        final int ret;
        if ((ret = this.readServerResponse()) != expect) {
            final int vsl = (this.validSentAddr == null) ? 0 : this.validSentAddr.length;
            final int vul = (this.validUnsentAddr == null) ? 0 : this.validUnsentAddr.length;
            final Address[] valid = new Address[vsl + vul];
            if (vsl > 0) {
                System.arraycopy(this.validSentAddr, 0, valid, 0, vsl);
            }
            if (vul > 0) {
                System.arraycopy(this.validUnsentAddr, 0, valid, vsl, vul);
            }
            this.validSentAddr = null;
            this.validUnsentAddr = valid;
            throw new SMTPSendFailedException(cmd, ret, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
        }
    }
    
    private int simpleCommand(final String cmd) throws MessagingException {
        this.sendCommand(cmd);
        return this.readServerResponse();
    }
    
    private int simpleCommand(final byte[] cmd) throws MessagingException {
        this.sendCommand(cmd);
        return this.readServerResponse();
    }
    
    private void sendCommand(final String cmd) throws MessagingException {
        this.sendCommand(ASCIIUtility.getBytes(cmd));
    }
    
    private void sendCommand(final byte[] cmdBytes) throws MessagingException {
        try {
            this.serverOutput.write(cmdBytes);
            this.serverOutput.write(SMTPTransport.CRLF);
            this.serverOutput.flush();
        }
        catch (IOException ex) {
            throw new MessagingException("Can't send command to SMTP host", ex);
        }
    }
    
    private int readServerResponse() throws MessagingException {
        String serverResponse = "";
        int returnCode = 0;
        final StringBuffer buf = new StringBuffer(100);
        try {
            String line = null;
            do {
                line = this.lineInputStream.readLine();
                if (line == null) {
                    serverResponse = buf.toString();
                    if (serverResponse.length() == 0) {
                        serverResponse = "[EOF]";
                    }
                    this.lastServerResponse = serverResponse;
                    this.lastReturnCode = -1;
                    if (this.debug) {
                        this.out.println("DEBUG SMTP: EOF: " + serverResponse);
                    }
                    return -1;
                }
                buf.append(line);
                buf.append("\n");
            } while (this.isNotLastLine(line));
            serverResponse = buf.toString();
        }
        catch (IOException ioex) {
            if (this.debug) {
                this.out.println("DEBUG SMTP: exception reading response: " + ioex);
            }
            this.lastServerResponse = "";
            this.lastReturnCode = 0;
            throw new MessagingException("Exception reading response", ioex);
        }
        if (serverResponse != null && serverResponse.length() >= 3) {
            try {
                returnCode = Integer.parseInt(serverResponse.substring(0, 3));
            }
            catch (NumberFormatException nfe) {
                try {
                    this.close();
                }
                catch (MessagingException mex) {
                    if (this.debug) {
                        mex.printStackTrace(this.out);
                    }
                }
                returnCode = -1;
            }
            catch (StringIndexOutOfBoundsException ex) {
                try {
                    this.close();
                }
                catch (MessagingException mex) {
                    if (this.debug) {
                        mex.printStackTrace(this.out);
                    }
                }
                returnCode = -1;
            }
        }
        else {
            returnCode = -1;
        }
        if (returnCode == -1 && this.debug) {
            this.out.println("DEBUG SMTP: bad server response: " + serverResponse);
        }
        this.lastServerResponse = serverResponse;
        return this.lastReturnCode = returnCode;
    }
    
    private void checkConnected() {
        if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }
    
    private boolean isNotLastLine(final String line) {
        return line != null && line.length() >= 4 && line.charAt(3) == '-';
    }
    
    private String normalizeAddress(final String addr) {
        if (!addr.startsWith("<") && !addr.endsWith(">")) {
            return "<" + addr + ">";
        }
        return addr;
    }
    
    public boolean supportsExtension(final String ext) {
        return this.extMap != null && this.extMap.get(ext.toUpperCase()) != null;
    }
    
    public String getExtensionParameter(final String ext) {
        return (this.extMap == null) ? null : this.extMap.get(ext.toUpperCase());
    }
    
    private boolean supportsAuthentication(final String auth) {
        if (this.extMap == null) {
            return false;
        }
        final String a = this.extMap.get("AUTH");
        if (a == null) {
            return false;
        }
        final StringTokenizer st = new StringTokenizer(a);
        while (st.hasMoreTokens()) {
            final String tok = st.nextToken();
            if (tok.equalsIgnoreCase(auth)) {
                return true;
            }
        }
        return false;
    }
    
    private String xtext(final String s) {
        StringBuffer sb = null;
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            if (c >= '\u0080') {
                throw new IllegalArgumentException("Non-ASCII character in SMTP submitter: " + s);
            }
            if (c < '!' || c > '~' || c == '+' || c == '=') {
                if (sb == null) {
                    sb = new StringBuffer(s.length() + 4);
                    sb.append(s.substring(0, i));
                }
                sb.append('+');
                sb.append(SMTPTransport.hexchar[(c & '\u00f0') >> 4]);
                sb.append(SMTPTransport.hexchar[c & '\u000f']);
            }
            else if (sb != null) {
                sb.append(c);
            }
        }
        return (sb != null) ? sb.toString() : s;
    }
    
    static {
        ignoreList = new String[] { "Bcc", "Content-Length" };
        CRLF = new byte[] { 13, 10 };
        SMTPTransport.hexchar = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
}
