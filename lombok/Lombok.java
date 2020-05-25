package lombok;

public class Lombok
{
    public static RuntimeException sneakyThrow(final Throwable t) {
        if (t == null) {
            throw new NullPointerException("t");
        }
        sneakyThrow0(t);
        return null;
    }
    
    private static <T extends Throwable> void sneakyThrow0(final Throwable t) throws T, Throwable {
        throw t;
    }
    
    public static <T> T preventNullAnalysis(final T value) {
        return value;
    }
    
    public static <T> T checkNotNull(final T value, final String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
}
