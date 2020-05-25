package com.mongodb.selector;

import com.mongodb.annotations.*;
import java.util.*;
import com.mongodb.connection.*;

@ThreadSafe
public interface ServerSelector
{
    List<ServerDescription> select(final ClusterDescription p0);
}
