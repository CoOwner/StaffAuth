package com.sun.mail.imap.protocol;

import java.util.*;

public class UIDSet
{
    public long start;
    public long end;
    
    public UIDSet() {
    }
    
    public UIDSet(final long start, final long end) {
        this.start = start;
        this.end = end;
    }
    
    public long size() {
        return this.end - this.start + 1L;
    }
    
    public static UIDSet[] createUIDSets(final long[] msgs) {
        final Vector v = new Vector();
        int j;
        for (int i = 0; i < msgs.length; i = j - 1, ++i) {
            final UIDSet ms = new UIDSet();
            ms.start = msgs[i];
            for (j = i + 1; j < msgs.length && msgs[j] == msgs[j - 1] + 1L; ++j) {}
            ms.end = msgs[j - 1];
            v.addElement(ms);
        }
        final UIDSet[] msgsets = new UIDSet[v.size()];
        v.copyInto(msgsets);
        return msgsets;
    }
    
    public static String toString(final UIDSet[] msgsets) {
        if (msgsets == null || msgsets.length == 0) {
            return null;
        }
        int i = 0;
        final StringBuffer s = new StringBuffer();
        final int size = msgsets.length;
        while (true) {
            final long start = msgsets[i].start;
            final long end = msgsets[i].end;
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
    
    public static long size(final UIDSet[] msgsets) {
        long count = 0L;
        if (msgsets == null) {
            return 0L;
        }
        for (int i = 0; i < msgsets.length; ++i) {
            count += msgsets[i].size();
        }
        return count;
    }
}
