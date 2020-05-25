package com.sun.mail.iap;

import java.io.*;

public interface Literal
{
    int size();
    
    void writeTo(final OutputStream p0) throws IOException;
}
