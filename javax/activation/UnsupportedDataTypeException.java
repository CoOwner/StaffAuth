package javax.activation;

import java.io.*;

public class UnsupportedDataTypeException extends IOException
{
    public UnsupportedDataTypeException() {
    }
    
    public UnsupportedDataTypeException(final String s) {
        super(s);
    }
}
