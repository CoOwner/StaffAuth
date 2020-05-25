package com.sun.mail.pop3;

import java.lang.reflect.*;
import java.io.*;
import javax.mail.*;

public class POP3Store extends Store
{
    private String name;
    private int defaultPort;
    private boolean isSSL;
    private Protocol port;
    private POP3Folder portOwner;
    private String host;
    private int portNum;
    private String user;
    private String passwd;
    boolean rsetBeforeQuit;
    boolean disableTop;
    boolean forgetTopHeaders;
    Constructor messageConstructor;
    
    public POP3Store(final Session session, final URLName url) {
        this(session, url, "pop3", 110, false);
    }
    
    public POP3Store(final Session session, final URLName url, String name, final int defaultPort, final boolean isSSL) {
        super(session, url);
        this.name = "pop3";
        this.defaultPort = 110;
        this.isSSL = false;
        this.port = null;
        this.portOwner = null;
        this.host = null;
        this.portNum = -1;
        this.user = null;
        this.passwd = null;
        this.rsetBeforeQuit = false;
        this.disableTop = false;
        this.forgetTopHeaders = false;
        this.messageConstructor = null;
        if (url != null) {
            name = url.getProtocol();
        }
        this.name = name;
        this.defaultPort = defaultPort;
        this.isSSL = isSSL;
        String s = session.getProperty("mail." + name + ".rsetbeforequit");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.rsetBeforeQuit = true;
        }
        s = session.getProperty("mail." + name + ".disabletop");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.disableTop = true;
        }
        s = session.getProperty("mail." + name + ".forgettopheaders");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.forgetTopHeaders = true;
        }
        s = session.getProperty("mail." + name + ".message.class");
        if (s != null) {
            if (session.getDebug()) {
                session.getDebugOut().println("DEBUG: POP3 message class: " + s);
            }
            try {
                final ClassLoader cl = this.getClass().getClassLoader();
                Class messageClass = null;
                try {
                    messageClass = cl.loadClass(s);
                }
                catch (ClassNotFoundException ex2) {
                    messageClass = Class.forName(s);
                }
                final Class[] c = { Folder.class, Integer.TYPE };
                this.messageConstructor = messageClass.getConstructor((Class[])c);
            }
            catch (Exception ex) {
                if (session.getDebug()) {
                    session.getDebugOut().println("DEBUG: failed to load POP3 message class: " + ex);
                }
            }
        }
    }
    
    protected synchronized boolean protocolConnect(final String host, int portNum, final String user, final String passwd) throws MessagingException {
        if (host == null || passwd == null || user == null) {
            return false;
        }
        if (portNum == -1) {
            final String portstring = this.session.getProperty("mail." + this.name + ".port");
            if (portstring != null) {
                portNum = Integer.parseInt(portstring);
            }
        }
        if (portNum == -1) {
            portNum = this.defaultPort;
        }
        this.host = host;
        this.portNum = portNum;
        this.user = user;
        this.passwd = passwd;
        try {
            this.port = this.getPort(null);
        }
        catch (EOFException eex) {
            throw new AuthenticationFailedException(eex.getMessage());
        }
        catch (IOException ioex) {
            throw new MessagingException("Connect failed", ioex);
        }
        return true;
    }
    
    public synchronized boolean isConnected() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: invokespecial   javax/mail/Store.isConnected:()Z
        //     4: ifne            9
        //     7: iconst_0       
        //     8: ireturn        
        //     9: aload_0         /* this */
        //    10: dup            
        //    11: astore_1       
        //    12: monitorenter   
        //    13: aload_0         /* this */
        //    14: getfield        com/sun/mail/pop3/POP3Store.port:Lcom/sun/mail/pop3/Protocol;
        //    17: ifnonnull       32
        //    20: aload_0         /* this */
        //    21: aload_0         /* this */
        //    22: aconst_null    
        //    23: invokevirtual   com/sun/mail/pop3/POP3Store.getPort:(Lcom/sun/mail/pop3/POP3Folder;)Lcom/sun/mail/pop3/Protocol;
        //    26: putfield        com/sun/mail/pop3/POP3Store.port:Lcom/sun/mail/pop3/Protocol;
        //    29: goto            40
        //    32: aload_0         /* this */
        //    33: getfield        com/sun/mail/pop3/POP3Store.port:Lcom/sun/mail/pop3/Protocol;
        //    36: invokevirtual   com/sun/mail/pop3/Protocol.noop:()Z
        //    39: pop            
        //    40: iconst_1       
        //    41: aload_1        
        //    42: monitorexit    
        //    43: ireturn        
        //    44: astore_2        /* ioex */
        //    45: aload_0         /* this */
        //    46: invokespecial   javax/mail/Store.close:()V
        //    49: iconst_0       
        //    50: aload_1        
        //    51: monitorexit    
        //    52: ireturn        
        //    53: astore_3       
        //    54: iconst_0       
        //    55: aload_1        
        //    56: monitorexit    
        //    57: ireturn        
        //    58: astore          4
        //    60: iconst_0       
        //    61: aload_1        
        //    62: monitorexit    
        //    63: ireturn        
        //    64: astore          5
        //    66: aload_1        
        //    67: monitorexit    
        //    68: aload           5
        //    70: athrow         
        //    Exceptions:
        //  Try           Handler
        //  Start  End    Start  End    Type                           
        //  -----  -----  -----  -----  -------------------------------
        //  13     41     44     64     Ljava/io/IOException;
        //  45     49     53     58     Ljavax/mail/MessagingException;
        //  45     49     58     64     Any
        //  53     54     58     64     Any
        //  58     60     58     64     Any
        //  13     43     64     71     Any
        //  44     52     64     71     Any
        //  53     57     64     71     Any
        //  58     63     64     71     Any
        //  64     68     64     71     Any
        // 
        // The error that occurred was:
        // 
        // java.lang.NullPointerException
        //     at com.strobel.decompiler.ast.AstBuilder.convertLocalVariables(AstBuilder.java:2895)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2445)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:211)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    synchronized Protocol getPort(final POP3Folder owner) throws IOException {
        if (this.port != null && this.portOwner == null) {
            this.portOwner = owner;
            return this.port;
        }
        final Protocol p = new Protocol(this.host, this.portNum, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), "mail." + this.name, this.isSSL);
        String msg = null;
        if ((msg = p.login(this.user, this.passwd)) != null) {
            try {
                p.quit();
            }
            catch (IOException ioex) {
                return (Protocol)ioex;
            }
            finally {
                throw new EOFException(msg);
            }
        }
        if (this.port == null && owner != null) {
            this.port = p;
            this.portOwner = owner;
        }
        if (this.portOwner == null) {
            this.portOwner = owner;
        }
        return p;
    }
    
    synchronized void closePort(final POP3Folder owner) {
        if (this.portOwner == owner) {
            this.port = null;
            this.portOwner = null;
        }
    }
    
    public synchronized void close() throws MessagingException {
        try {
            if (this.port != null) {
                this.port.quit();
            }
        }
        catch (IOException ioex) {}
        finally {
            this.port = null;
            super.close();
        }
    }
    
    public Folder getDefaultFolder() throws MessagingException {
        this.checkConnected();
        return new DefaultFolder(this);
    }
    
    public Folder getFolder(final String name) throws MessagingException {
        this.checkConnected();
        return new POP3Folder(this, name);
    }
    
    public Folder getFolder(final URLName url) throws MessagingException {
        this.checkConnected();
        return new POP3Folder(this, url.getFile());
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.port != null) {
            this.close();
        }
    }
    
    private void checkConnected() throws MessagingException {
        if (!super.isConnected()) {
            throw new MessagingException("Not connected");
        }
    }
}
