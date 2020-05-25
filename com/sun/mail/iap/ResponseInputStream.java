package com.sun.mail.iap;

import java.io.*;
import com.sun.mail.util.*;

public class ResponseInputStream
{
    private static final int minIncrement = 256;
    private static final int maxIncrement = 262144;
    private byte[] buffer;
    private int sz;
    private int idx;
    private BufferedInputStream bin;
    
    public ResponseInputStream(final InputStream in) {
        this.buffer = null;
        this.sz = 0;
        this.idx = 0;
        this.bin = new BufferedInputStream(in, 2048);
    }
    
    public ByteArray readResponse() throws IOException {
        this.buffer = new byte[128];
        this.idx = 0;
        this.sz = 128;
        this.read0();
        return new ByteArray(this.buffer, 0, this.idx);
    }
    
    private void read0() throws IOException {
        int b = 0;
        boolean gotCRLF = false;
        while (!gotCRLF && (b = this.bin.read()) != -1) {
            switch (b) {
                case 10: {
                    if (this.idx > 0 && this.buffer[this.idx - 1] == 13) {
                        gotCRLF = true;
                        break;
                    }
                    break;
                }
            }
            if (this.idx >= this.sz) {
                if (this.sz < 262144) {
                    this.growBuffer(this.sz);
                }
                else {
                    this.growBuffer(262144);
                }
            }
            this.buffer[this.idx++] = (byte)b;
        }
        if (b == -1) {
            throw new IOException();
        }
        if (this.idx >= 5 && this.buffer[this.idx - 3] == 125) {
            int i;
            for (i = this.idx - 4; i >= 0 && this.buffer[i] != 123; --i) {}
            if (i < 0) {
                return;
            }
            int count = 0;
            try {
                count = ASCIIUtility.parseInt(this.buffer, i + 1, this.idx - 3);
            }
            catch (NumberFormatException e) {
                return;
            }
            if (count > 0) {
                final int avail = this.sz - this.idx;
                if (count > avail) {
                    this.growBuffer((256 > count - avail) ? 256 : (count - avail));
                }
                while (count > 0) {
                    final int actual = this.bin.read(this.buffer, this.idx, count);
                    count -= actual;
                    this.idx += actual;
                }
            }
            this.read0();
        }
    }
    
    private void growBuffer(final int incr) {
        final byte[] nbuf = new byte[this.sz + incr];
        if (this.buffer != null) {
            System.arraycopy(this.buffer, 0, nbuf, 0, this.idx);
        }
        this.buffer = nbuf;
        this.sz += incr;
    }
}
