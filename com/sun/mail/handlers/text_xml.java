package com.sun.mail.handlers;

import javax.activation.*;

public class text_xml extends text_plain
{
    private static ActivationDataFlavor myDF;
    
    protected ActivationDataFlavor getDF() {
        return text_xml.myDF;
    }
    
    static {
        text_xml.myDF = new ActivationDataFlavor(String.class, "text/xml", "XML String");
    }
}
