package com.sun.mail.imap.protocol;

import javax.mail.internet.*;
import java.util.*;
import com.sun.mail.iap.*;

public class BODYSTRUCTURE implements Item
{
    public static char[] name;
    public int msgno;
    public String type;
    public String subtype;
    public String encoding;
    public int lines;
    public int size;
    public String disposition;
    public String id;
    public String description;
    public String md5;
    public String attachment;
    public ParameterList cParams;
    public ParameterList dParams;
    public String[] language;
    public BODYSTRUCTURE[] bodies;
    public ENVELOPE envelope;
    private static int SINGLE;
    private static int MULTI;
    private static int NESTED;
    private int processedType;
    
    public BODYSTRUCTURE(final FetchResponse r) throws ParsingException {
        this.lines = -1;
        this.size = -1;
        this.msgno = r.getNumber();
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("BODYSTRUCTURE parse error: missing ``('' at start");
        }
        if (r.peekByte() == 40) {
            this.type = "multipart";
            this.processedType = BODYSTRUCTURE.MULTI;
            final Vector v = new Vector(1);
            final int i = 1;
            do {
                v.addElement(new BODYSTRUCTURE(r));
                r.skipSpaces();
            } while (r.peekByte() == 40);
            v.copyInto(this.bodies = new BODYSTRUCTURE[v.size()]);
            this.subtype = r.readString();
            if (r.readByte() == 41) {
                return;
            }
            this.cParams = this.parseParameters(r);
            if (r.readByte() == 41) {
                return;
            }
            byte b = r.readByte();
            if (b == 40) {
                this.disposition = r.readString();
                this.dParams = this.parseParameters(r);
                if (r.readByte() != 41) {
                    throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition in multipart");
                }
            }
            else {
                if (b != 78 && b != 110) {
                    throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad multipart disposition, b " + b);
                }
                r.skip(2);
            }
            if ((b = r.readByte()) == 41) {
                return;
            }
            if (b != 32) {
                throw new ParsingException("BODYSTRUCTURE parse error: missing space after disposition");
            }
            if (r.peekByte() == 40) {
                this.language = r.readStringList();
            }
            else {
                final String l = r.readString();
                if (l != null) {
                    final String[] la = { l };
                    this.language = la;
                }
            }
            while (r.readByte() == 32) {
                this.parseBodyExtension(r);
            }
        }
        else {
            this.type = r.readString();
            this.processedType = BODYSTRUCTURE.SINGLE;
            this.subtype = r.readString();
            if (this.type == null) {
                this.type = "application";
                this.subtype = "octet-stream";
            }
            this.cParams = this.parseParameters(r);
            this.id = r.readString();
            this.description = r.readString();
            this.encoding = r.readString();
            this.size = r.readNumber();
            if (this.size < 0) {
                throw new ParsingException("BODYSTRUCTURE parse error: bad ``size'' element");
            }
            if (this.type.equalsIgnoreCase("text")) {
                this.lines = r.readNumber();
                if (this.lines < 0) {
                    throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                }
            }
            else if (this.type.equalsIgnoreCase("message") && this.subtype.equalsIgnoreCase("rfc822")) {
                this.processedType = BODYSTRUCTURE.NESTED;
                this.envelope = new ENVELOPE(r);
                final BODYSTRUCTURE[] bs = { new BODYSTRUCTURE(r) };
                this.bodies = bs;
                this.lines = r.readNumber();
                if (this.lines < 0) {
                    throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                }
            }
            else {
                r.skipSpaces();
                final byte bn = r.peekByte();
                if (Character.isDigit((char)bn)) {
                    throw new ParsingException("BODYSTRUCTURE parse error: server erroneously included ``lines'' element with type " + this.type + "/" + this.subtype);
                }
            }
            if (r.peekByte() == 41) {
                r.readByte();
                return;
            }
            this.md5 = r.readString();
            if (r.readByte() == 41) {
                return;
            }
            final byte b2 = r.readByte();
            if (b2 == 40) {
                this.disposition = r.readString();
                this.dParams = this.parseParameters(r);
                if (r.readByte() != 41) {
                    throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition");
                }
            }
            else {
                if (b2 != 78 && b2 != 110) {
                    throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad single part disposition, b " + b2);
                }
                r.skip(2);
            }
            if (r.readByte() == 41) {
                return;
            }
            if (r.peekByte() == 40) {
                this.language = r.readStringList();
            }
            else {
                final String j = r.readString();
                if (j != null) {
                    final String[] la2 = { j };
                    this.language = la2;
                }
            }
            while (r.readByte() == 32) {
                this.parseBodyExtension(r);
            }
        }
    }
    
    public boolean isMulti() {
        return this.processedType == BODYSTRUCTURE.MULTI;
    }
    
    public boolean isSingle() {
        return this.processedType == BODYSTRUCTURE.SINGLE;
    }
    
    public boolean isNested() {
        return this.processedType == BODYSTRUCTURE.NESTED;
    }
    
    private ParameterList parseParameters(final Response r) throws ParsingException {
        r.skipSpaces();
        ParameterList list = null;
        final byte b = r.readByte();
        if (b == 40) {
            list = new ParameterList();
            do {
                final String name = r.readString();
                final String value = r.readString();
                list.set(name, value);
            } while (r.readByte() != 41);
        }
        else {
            if (b != 78 && b != 110) {
                throw new ParsingException("Parameter list parse error");
            }
            r.skip(2);
        }
        return list;
    }
    
    private void parseBodyExtension(final Response r) throws ParsingException {
        r.skipSpaces();
        final byte b = r.peekByte();
        if (b == 40) {
            r.skip(1);
            do {
                this.parseBodyExtension(r);
            } while (r.readByte() != 41);
        }
        else if (Character.isDigit((char)b)) {
            r.readNumber();
        }
        else {
            r.readString();
        }
    }
    
    static {
        BODYSTRUCTURE.name = new char[] { 'B', 'O', 'D', 'Y', 'S', 'T', 'R', 'U', 'C', 'T', 'U', 'R', 'E' };
        BODYSTRUCTURE.SINGLE = 1;
        BODYSTRUCTURE.MULTI = 2;
        BODYSTRUCTURE.NESTED = 3;
    }
}
