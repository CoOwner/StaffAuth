package com.sun.mail.imap.protocol;

import java.util.*;

public class MessageSet
{
    public int start;
    public int end;
    
    public MessageSet() {
    }
    
    public MessageSet(final int start, final int end) {
        this.start = start;
        this.end = end;
    }
    
    public int size() {
        return this.end - this.start + 1;
    }
    
    public static MessageSet[] createMessageSets(final int[] msgs) {
        final Vector v = new Vector();
        int j;
        for (int i = 0; i < msgs.length; i = j - 1, ++i) {
            final MessageSet ms = new MessageSet();
            ms.start = msgs[i];
            for (j = i + 1; j < msgs.length && msgs[j] == msgs[j - 1] + 1; ++j) {}
            ms.end = msgs[j - 1];
            v.addElement(ms);
        }
        final MessageSet[] msgsets = new MessageSet[v.size()];
        v.copyInto(msgsets);
        return msgsets;
    }
    
    public static String toString(final MessageSet[] msgsets) {
        if (msgsets == null || msgsets.length == 0) {
            return null;
        }
        int i = 0;
        final StringBuffer s = new StringBuffer();
        final int size = msgsets.length;
        while (true) {
            final int start = msgsets[i].start;
            final int end = msgsets[i].end;
            if (end > start) {
                s.append(start).append(':').append(end);
            }
            else {
                s.append(start);
            }
            if (++i >= size) {
                break;
            }
            s.append(',');
        }
        return s.toString();
    }
    
    public static int size(final MessageSet[] msgsets) {
        int count = 0;
        if (msgsets == null) {
            return 0;
        }
        for (int i = 0; i < msgsets.length; ++i) {
            count += msgsets[i].size();
        }
        return count;
    }
}
