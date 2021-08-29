package customtransformer;

public final class PrimitiveUtils {

    public static int toPrimInt(Integer d) {
        return d == null ? 0 : d;
    }

    public static Integer toInt(int d) {
        return d;
    }

    public static Long intToLong(Integer i) {
        return Long.valueOf(i);
    }

    public static Integer longToInt(Long l) {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
            throw new RuntimeException("What you're trying to do seems fishy");
        }
        return Integer.valueOf(String.valueOf(l));
    }
}
