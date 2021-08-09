import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class MapperBuilder {


    public Map<Class<?>, ArrayList<Class<?>>> classMapperMap = new HashMap<>();
    public Map<Class<?>, ArrayList<Class<?>>> enumMapperMap = new HashMap<>();

    // replace
    public List<MapperHolder> classMappers = new ArrayList<>();
    public List<MapperHolder> enumMappers = new ArrayList<>();
    public List<ConverterHolder> converter = new ArrayList<>();
    private String location;
    private String packageName;


    MapperBuilder() {

    }

    private static String calculateImport(String aClass) {
        return String.format("import %s;", aClass);
    }

    public MapperBuilder map(Class<?> from, Class<?> to) {
        ArrayList<Class<?>> mapperHolders = classMapperMap.get(from);

        if (mapperHolders == null) {
            ArrayList<Class<?>> classes = new ArrayList<>();
            classes.add(to);
            classMapperMap.put(from, classes);
        } else {
            mapperHolders.add(to);
        }


        MapperHolder e = new MapperHolder();
        e.from = from;
        e.to = to;
        classMappers.add(e);
        return this;
    }

    public MapperBuilder mapEnum(Class<?> from, Class<?> to) {


        ArrayList<Class<?>> mapperHolders = enumMapperMap.get(from);

        if (mapperHolders == null) {
            ArrayList<Class<?>> classes = new ArrayList<>();
            classes.add(to);
            classMapperMap.put(from, classes);
        } else {
            mapperHolders.add(to);
        }


        MapperHolder e = new MapperHolder();
        e.from = from;
        e.to = to;
        enumMappers.add(e);
        return this;
    }

    public Pair<String, Set<String>> clculateClassMapping(Class<?> from, Class<?> to) {

        HashSet<String> imports = new HashSet<>();

        try {

            String method = "";
            String listFields = "";
            String notMappedFields = "";

            PropertyDescriptor[] fromProperties = Introspector.getBeanInfo(from).getPropertyDescriptors();
            PropertyDescriptor[] toProperties = Introspector.getBeanInfo(to).getPropertyDescriptors();

            for (PropertyDescriptor fromProp : fromProperties) {

                var hasMapper = false;

                for (PropertyDescriptor toProp : toProperties) {

                    if (!fromProp.getName().equals(toProp.getName())) {
                        continue;
                    }

                    if (fromProp.getReadMethod() != null && !"class".equals(fromProp.getName())) {
                        if (toProp.getWriteMethod() != null && !"class".equals(toProp.getName())) {
                            hasMapper = true;


                            Class<?> fromType = fromProp.getPropertyType();
                            Class<?> toType = toProp.getPropertyType();

                            // in case the from and to is the same
                            if (fromType.equals(toType)) {
                                method += "         to." +
                                        fromProp.getWriteMethod().getName()
                                        + "(from." + fromProp.getReadMethod().getName()
                                        + "());\n";
                            } else {
                                // in case the from and to is not the same but mappable
                                MapperType mapperType = calculateMappableType(toType, fromType);
                                if (mapperType != null) {

                                    if (mapperType == MapperType.BASE_CONVERTER) {

                                        for (ConverterHolder conver : converter) {
                                            if (conver.from.equals(fromType) &&
                                                    conver.to.equals(toType)) {

                                                imports.add(calculateImport(conver.cla.getName()));

                                                // calculate the converter
                                                method += "to." +
                                                        fromProp.getWriteMethod().getName()
                                                        + "(" +
                                                        conver.cla.getSimpleName()
                                                        + "."
                                                        + conver.methodName
                                                        + "("
                                                        + "from." + fromProp.getReadMethod().getName()
                                                        + "()));\n";

                                                continue;
                                            }
                                        }


                                    } else if (mapperType == MapperType.ENUM) {

                                        String mapperFileName = calculateMapperFileName(fromProp.getPropertyType());

                                        imports.add(calculateImport(packageName + "." + mapperFileName));
                                        method += "to." +
                                                fromProp.getWriteMethod().getName()
                                                + "(" + mapperFileName +
                                                ".to" + toProp.getPropertyType().getSimpleName()
                                                + "(from." + fromProp.getReadMethod().getName()
                                                + "()));\n";

                                    }
                                }
                            }
                        } else if (fromProp.getPropertyType() == List.class &&
                                toProp.getPropertyType() == List.class) {
                            hasMapper = true;

                            // in case the from and to is both a List

                            ParameterizedType toListType = (ParameterizedType) toProp.getReadMethod().getGenericReturnType();
                            Class<?> toListClass = (Class<?>) toListType.getActualTypeArguments()[0];

                            ParameterizedType fromType = (ParameterizedType) fromProp.getReadMethod().getGenericReturnType();
                            Class<?> fromListClass = (Class<?>) fromType.getActualTypeArguments()[0];

                            String fieldTransformer = null;

                            if (fromListClass.equals(toListClass)) {
                                fieldTransformer = "o";
                            } else {
                                if (calculateMappableType(toListClass, fromListClass) != null) {
                                    //todo easy converters int -> Integer etc
                                    fieldTransformer = calculateMapperFileName(
                                            fromListClass) +
                                            ".to" + toListClass.getSimpleName() + "(o)";
                                }
                            }

                            if (fieldTransformer != null) {
                                listFields +=
                                        "        for (var o : from." + fromProp.getReadMethod().getName() + "()) {\n" +
                                                "            to."
                                                + toProp.getReadMethod().getName() +
                                                "().add(" +
                                                fieldTransformer +
                                                ");\n" +
                                                "       }\n";
                            }
                        }
                    }
                }

                if (!hasMapper) {
                    if (fromProp.getWriteMethod() != null) {
                        notMappedFields += "        // " + fromProp.getWriteMethod().getName() + "\n";
                    } else {
                        notMappedFields += "        // " + fromProp.getReadMethod().getName() + "\n";
                    }
                }
            }

            var allMappers = method
                    +
                    listFields
                    +
                    "\n// following fields ar not mapped!!! please map them yourself\n"
                    +
                    notMappedFields;


            return new ImmutablePair<>(allMappers, imports);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String enumConverter(Class<?> from, Class<?> to) {

        HashSet<String> imports = new HashSet<>();


        String method = "";

        Object[] fromEnumValues = from.getEnumConstants();
        Object[] toEnumValues = to.getEnumConstants();

        for (Object fromEnumValue : fromEnumValues) {
            for (Object toEnumValue : toEnumValues) {
                if (fromEnumValue.toString().equals(toEnumValue.toString())) {
                    method += "if(" +
                            from.getSimpleName()
                            + "." + fromEnumValue.toString() + " == from){" +
                            "return " +
                            to.getSimpleName() +
                            "." + toEnumValue.toString()
                            + ";" +
                            "}\n";
                    continue;
                }
            }
        }

        return method;

    }

    private MapperType calculateMappableType(Class<?> toClass, Class<?> fromClass) {
        // check if there is a Type
        for (MapperHolder mapperHolder : classMappers) {
            if (mapperHolder.from.equals(fromClass) &&
                    mapperHolder.to.equals(toClass)) {
                return MapperType.GENERATED;
            }
        }
        // check if there is an enum
        for (MapperHolder mapperHolder : enumMappers) {
            if (mapperHolder.from.equals(fromClass) &&
                    mapperHolder.to.equals(toClass)) {
                return MapperType.ENUM;
            }
        }
        // check if there is an BaseType converter
        for (ConverterHolder conver : converter) {
            if (conver.from.equals(fromClass) &&
                    conver.to.equals(toClass)) {
                return MapperType.BASE_CONVERTER;
            }
        }
        return null;
    }

    public void build() {
        var imports = "";
        var methods = "";

        ///THE NEW BLOCK WITH SEPARATED CLASSES
        // class Mappers

        for (Map.Entry<Class<?>, ArrayList<Class<?>>> mapperHolder : classMapperMap.entrySet()) {
            Class<?> inClass = mapperHolder.getKey();
            String mapperName = calculateMapperFileName(inClass);
            String fileDestination = location + mapperName + ".java";

            Map<String, String> customCodeMap = ExistingMapperReader.getCustomCode(fileDestination);

            Set<String> importSet = new HashSet<>();
            methods = "";

            importSet.add(calculateImport(inClass.getName()));

            for (Class<?> outClass : mapperHolder.getValue()) {
                importSet.add(calculateImport(outClass.getName()));

                String customCode = clacluateCustomCode(inClass, customCodeMap, outClass);


                if (inClass.isEnum() && outClass.isEnum()) {
                    var allMethods = enumConverter(inClass, outClass);

                    methods += String.format(
                            "    public static %s to%s(%s from) {\n" +
                                    "       %s\n" +
                                    "       %s\n" +
                                    "       return null;\n" +
                                    "    }\n",
                            outClass.getSimpleName(),
                            outClass.getSimpleName(),
                            inClass.getSimpleName(),
                            allMethods,
                            customCode);

                } else if (!inClass.isEnum() && !outClass.isEnum()) {

                    var allMethods = clculateClassMapping(inClass, outClass);

                    methods += String.format(
                            "    public static %s to%s(%s from) {\n" +
                                    "       var to = new %s();\n" +
                                    "       %s\n" +
                                    "       %s\n" +
                                    "       return to;\n" +
                                    "    }\n",
                            outClass.getSimpleName(),
                            outClass.getSimpleName(),
                            inClass.getSimpleName(),
                            outClass.getSimpleName(),
                            allMethods.getKey(),
                            customCode
                    );

                    importSet.addAll(allMethods.getValue());
                } else {
                    throw new RuntimeException("we can only map enums to other enums");
                }
            }


            imports = "";
            for (String o : importSet) {
                imports += o + "\n";
            }

            String fileContent = "package " + packageName + ";\n" +
                    "\n" +
                    imports +
                    "\n" +
                    "public final class " + mapperName + " {\n" +
                    "    private " + mapperName + "() {\n" +
                    "    }\n" +
                    methods +
                    "\n" +
                    "}\n";
            FileUtils.write(fileDestination, fileContent);

        }
    }

    private String clacluateCustomCode(Class<?> inClass, Map<String, String> customCodeMap, Class<?> outClass) {
        String customCode = customCodeMap.get(inClass.getSimpleName() + outClass.getSimpleName());
        customCode = customCode == null ? "" : "\n" + customCode;

        return "\n        " + MapGenConstants.CUSTOM_CODE_START
                + customCode
                + "\n        " + MapGenConstants.CUSTOM_CODE_END;

    }

    private static String calculateMapperFileName(Class<?> clas) {
        return clas.getSimpleName() + "Mapper";
    }

    /**
     * add a converter class which converts / transforms one value to another
     */
    public MapperBuilder converter(Class<?> con) {
        for (Method declaredMethod : con.getDeclaredMethods()) {
            if (declaredMethod.getParameters().length == 1 &&
                    declaredMethod.getReturnType() != Void.class) {
                ConverterHolder converterHolder = new ConverterHolder();
                converterHolder.from = (declaredMethod.getParameters()[0]).getType();
                converterHolder.to = declaredMethod.getReturnType();
                converterHolder.cla = con;
                converterHolder.methodName = declaredMethod.getName();
                converter.add(converterHolder);
            }
        }
        return this;
    }

    public MapperBuilder location(String s) {
        this.location = s;
        return this;
    }

    public MapperBuilder packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

}
