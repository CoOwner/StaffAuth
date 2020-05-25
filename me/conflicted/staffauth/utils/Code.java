package me.conflicted.staffauth.utils;

import java.util.*;

public class Code
{
    public static String getCode() {
        final String uString = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return uString;
    }
}
