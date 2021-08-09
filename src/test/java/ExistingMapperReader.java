import org.apache.commons.lang3.StringUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExistingMapperReader {

    public static Map<String, String> getCustomCode(String filePath) {
        try {

            var customCodeMap = new HashMap<String, String>();

            var ref = new Object() {
                String lastFromTo = null;
                String customCode = null;
            };

            Files.lines(Path.of(filePath)).forEach((s) -> {
                if (s.contains("public static")) {
                    ref.lastFromTo =
                            StringUtils.substringBetween(s, "(", " ") +
                                    StringUtils.substringBetween(s, "public static ", " ");
                } else if (s.contains(MapGenConstants.CUSTOM_CODE_START)) {
                    ref.customCode = "";
                } else if (s.contains(MapGenConstants.CUSTOM_CODE_END)) {
                    if (!StringUtils.isEmpty(ref.customCode)) {
                        customCodeMap.put(ref.lastFromTo, ref.customCode);
                    }
                } else {
                    ref.customCode += "\n" + s;
                }
            });
            return customCodeMap;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
}
