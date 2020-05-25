package com.sun.mail.iap;

import java.util.*;
import com.sun.mail.util.*;
import java.io.*;

public class Argument
{
    protected Vector items;
    
    public Argument() {
        this.items = new Vector(1);
    }
    
    public void append(final Argument arg) {
        this.items.ensureCapacity(this.items.size() + arg.items.size());
        for (int i = 0; i < arg.items.size(); ++i) {
            this.items.addElement(arg.items.elementAt(i));
        }
    }
    
    public void writeString(final String s) {
        this.items.addElement(new AString(ASCIIUtility.getBytes(s)));
    }
    
    public void writeString(final String s, final String charset) throws UnsupportedEncodingException {
        if (charset == null) {
            this.writeString(s);
        }
        else {
            this.items.addElement(new AString(s.getBytes(charset)));
        }
    }
    
    public void writeBytes(final byte[] b) {
        this.items.addElement(b);
    }
    
    public void writeBytes(final ByteArrayOutputStream b) {
        this.items.addElement(b);
    }
    
    public void writeBytes(final Literal b) {
        this.items.addElement(b);
    }
    
    public void writeAtom(final String s) {
        this.items.addElement(new Atom(s));
    }
    
    public void writeNumber(final int i) {
        this.items.addElement(new Integer(i));
    }
    
    public void writeNumber(final long i) {
        this.items.addElement(new Long(i));
    }
    
    public void writeArgument(final Argument c) {
        this.items.addElement(c);
    }
    
    public void write(final Protocol protocol) throws IOException, ProtocolException {
        final int size = (this.items != null) ? this.items.size() : 0;
        final DataOutputStream os = (DataOutputStream)protocol.getOutputStream();
        for (int i = 0; i < size; ++i) {
            if (i > 0) {
                os.write(32);
            }
            final Object o = this.items.elementAt(i);
            if (o instanceof Atom) {
                os.writeBytes(((Atom)o).string);
            }
            else if (o instanceof Number) {
                os.writeBytes(((Number)o).toString());
            }
            else if (o instanceof AString) {
                this.astring(((AString)o).bytes, protocol);
            }
            else if (o instanceof byte[]) {
                this.literal((byte[])o, protocol);
            }
            else if (o instanceof ByteArrayOutputStream) {
                this.literal((ByteArrayOutputStream)o, protocol);
            }
            else if (o instanceof Literal) {
                this.literal((Literal)o, protocol);
            }
            else if (o instanceof Argument) {
                os.write(40);
                ((Argument)o).write(protocol);
                os.write(41);
            }
        }
    }
    
    private void astring(final byte[] bytes, final Protocol protocol) throws IOException, ProtocolException {
        final DataOutputStream os = (DataOutputStream)protocol.getOutputStream();
        final int len = bytes.length;
        if (len > 1024) {
            this.literal(bytes, protocol);
            return;
        }
        boolean quote = len == 0;
        boolean escape = false;
        for (final byte b : bytes) {
            if (b == 0 || b == 13 || b == 10 || (b & 0xFF) > 127) {
                this.literal(bytes, protocol);
                return;
            }
            if (b == 42 || b == 37 || b == 40 || b == 41 || b == 123 || b == 34 || b == 92 || (b & 0xFF) <= 32) {
                quote = true;
                if (b == 34 || b == 92) {
                    escape = true;
                }
            }
        }
        if (quote) {
            os.write(34);
        }
        if (escape) {
            for (final byte b : bytes) {
                if (b == 34 || b == 92) {
                    os.write(92);
                }
                os.write(b);
            }
        }
        else {
            os.write(bytes);
        }
        if (quote) {
            os.write(34);
        }
    }
    
    private void literal(final byte[] b, final Protocol protocol) throws IOException, ProtocolException {
        this.startLiteral(protocol, b.length).write(b);
    }
    
    private void literal(final ByteArrayOutputStream b, final Protocol protocol) throws IOException, ProtocolException {
        b.writeTo(this.startLiteral(protocol, b.size()));
    }
    
    private void literal(final Literal b, final Protocol protocol) throws IOException, ProtocolException {
        b.writeTo(this.startLiteral(protocol, b.size()));
    }
    
    private OutputStream startLiteral(final Protocol protocol, final int size) throws IOException, ProtocolException {
        final DataOutputStream os = (DataOutputStream)protocol.getOutputStream();
        final boolean nonSync = protocol.supportsNonSyncLiterals();
        os.write(123);
        os.writeBytes(Integer.toString(size));
        if (nonSync) {
            os.writeBytes("+}\r\n");
        }
        else {
            os.writeBytes("}\r\n");
        }
        os.flush();
        if (!nonSync) {
            while (true) {
                final Response r = protocol.readResponse();
                if (r.isContinuation()) {
                    break;
                }
                if (r.isTagged()) {
                    throw new LiteralException(r);
                }
            }
        }
        return os;
    }
}
