package com.sun.mail.handlers;

import javax.activation.*;

public class text_html extends text_plain
{
    private static ActivationDataFlavor myDF;
    
    protected ActivationDataFlavor getDF() {
        return text_html.myDF;
    }
    
    static {
        text_html.myDF = new ActivationDataFlavor(String.class, "text/html", "HTML String");
    }
}
