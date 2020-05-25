package lombok.launch;

import java.util.*;
import java.lang.reflect.*;

class Main
{
    static ClassLoader createShadowClassLoader() {
        return new ShadowClassLoader(Main.class.getClassLoader(), "lombok", null, Arrays.asList(new String[0]), Arrays.asList("lombok.patcher.Symbols"));
    }
    
    public static void main(final String[] args) throws Throwable {
        final ClassLoader cl = createShadowClassLoader();
        final Class<?> mc = cl.loadClass("lombok.core.Main");
        try {
            mc.getMethod("main", String[].class).invoke(null, args);
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
