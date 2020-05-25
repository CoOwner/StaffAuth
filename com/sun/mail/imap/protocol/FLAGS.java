package com.sun.mail.imap.protocol;

import javax.mail.*;
import com.sun.mail.iap.*;

public class FLAGS extends Flags implements Item
{
    public static char[] name;
    public int msgno;
    private static final long serialVersionUID = 439049847053756670L;
    
    public FLAGS(final IMAPResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        final String[] flags = r.readSimpleList();
        if (flags != null) {
            for (int i = 0; i < flags.length; ++i) {
                final String s = flags[i];
                if (s.length() >= 2 && s.charAt(0) == '\\') {
                    switch (Character.toUpperCase(s.charAt(1))) {
                        case 'S': {
                            this.add(Flag.SEEN);
                            break;
                        }
                        case 'R': {
                            this.add(Flag.RECENT);
                            break;
                        }
                        case 'D': {
                            if (s.length() >= 3) {
                                final char c = s.charAt(2);
                                if (c == 'e' || c == 'E') {
                                    this.add(Flag.DELETED);
                                }
                                else if (c == 'r' || c == 'R') {
                                    this.add(Flag.DRAFT);
                                }
                                break;
                            }
                            this.add(s);
                            break;
                        }
                        case 'A': {
                            this.add(Flag.ANSWERED);
                            break;
                        }
                        case 'F': {
                            this.add(Flag.FLAGGED);
                            break;
                        }
                        case '*': {
                            this.add(Flag.USER);
                            break;
                        }
                        default: {
                            this.add(s);
                            break;
                        }
                    }
                }
                else {
                    this.add(s);
                }
            }
        }
    }
    
    static {
        FLAGS.name = new char[] { 'F', 'L', 'A', 'G', 'S' };
    }
}
