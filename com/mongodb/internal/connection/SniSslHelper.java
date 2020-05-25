package com.mongodb.internal.connection;

import com.mongodb.*;
import javax.net.ssl.*;

interface SniSslHelper
{
    void enableSni(final ServerAddress p0, final SSLParameters p1);
}
