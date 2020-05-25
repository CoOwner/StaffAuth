package com.sun.mail.util;

import java.io.*;

public class TraceOutputStream extends FilterOutputStream
{
    private boolean trace;
    private boolean quote;
    private OutputStream traceOut;
    
    public TraceOutputStream(final OutputStream out, final OutputStream traceOut) {
        super(out);
        this.trace = false;
        this.quote = false;
        this.traceOut = traceOut;
    }
    
    public void setTrace(final boolean trace) {
        this.trace = trace;
    }
    
    public void setQuote(final boolean quote) {
        this.quote = quote;
    }
    
    public void write(final int b) throws IOException {
        if (this.trace) {
            if (this.quote) {
                this.writeByte(b);
            }
            else {
                this.traceOut.write(b);
            }
        }
        this.out.write(b);
    }
    
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.trace) {
            if (this.quote) {
                for (int i = 0; i < len; ++i) {
                    this.writeByte(b[off + i]);
                }
            }
            else {
                this.traceOut.write(b, off, len);
            }
        }
        this.out.write(b, off, len);
    }
    
    private final void writeByte(int b) throws IOException {
        b &= 0xFF;
        if (b > 127) {
            this.traceOut.write(77);
            this.traceOut.write(45);
            b &= 0x7F;
        }
        if (b == 13) {
            this.traceOut.write(92);
            this.traceOut.write(114);
        }
        else if (b == 10) {
            this.traceOut.write(92);
            this.traceOut.write(110);
            this.traceOut.write(10);
        }
        else if (b == 9) {
            this.traceOut.write(92);
            this.traceOut.write(116);
        }
        else if (b < 32) {
            this.traceOut.write(94);
            this.traceOut.write(64 + b);
        }
        else {
            this.traceOut.write(b);
        }
    }
}
