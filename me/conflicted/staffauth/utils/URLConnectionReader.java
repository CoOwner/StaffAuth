package me.conflicted.staffauth.utils;

import java.net.*;
import java.io.*;

public class URLConnectionReader
{
    public static String get(final String s) throws IOException {
        final URL conn = new URL(s);
        final URLConnection yc = conn.openConnection();
        final BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        final String inputLine;
        if ((inputLine = in.readLine()) != null) {
            return inputLine;
        }
        in.close();
        return null;
    }
}
