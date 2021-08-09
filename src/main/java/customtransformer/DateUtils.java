package customtransformer;

import java.time.*;
import java.util.Date;

public final class DateUtils {

    public static Date toDate(LocalDate d) {
        return java.util.Date.from(d.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}
