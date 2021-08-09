package customtransformer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class PrimitiveUtils {

    public static int toPrimInt(Integer d) {
        return d == null ? 0 : d;
    }

    public static Integer toInt(int d) {
        return d;
    }
}
