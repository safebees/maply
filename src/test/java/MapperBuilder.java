import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class MapperBuilder {

    private static final int AMOUNT_SPACES_PER_INDENT = 4;
    private static final String NOT_MAPPED_FIELDS_TEXT = "// TODO following fields are not mapped!!! please map them yourself";
    private static final String ASSUMABLY_MAPPED_IN_CUSTOM_CODE_TEXT = "// These fields could not be assigned be the generator, but are apparently mapped in the custom code block";

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

    private String calculateImport(String aClass) {
        if (aClass == null ||
                (aClass.startsWith(packageName) && aClass.replace(packageName, "").codePoints().filter(e -> e == '.').count() == 1)) {
            // class to import is in the same package, so we don't have to import it
            return null;
        }
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

    public Pair<String, Set<String>> clculateClassMapping(Class<?> from, Class<?> to, String customCode) {

        HashSet<String> imports = new HashSet<>();

        try {

            String method = "";
            String listFields = "";
            String notMappedFields = "";
            String assumablyMappedInCustomCode = "";

            PropertyDescriptor[] fromProperties = Introspector.getBeanInfo(from).getPropertyDescriptors();
            PropertyDescriptor[] toProperties = Introspector.getBeanInfo(to).getPropertyDescriptors();

            for (PropertyDescriptor fromProp : fromProperties) {

                boolean hasMapper = false;

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
                                method += getIndent(2) + "to." + fromProp.getWriteMethod().getName()
                                        + "(from." + fromProp.getReadMethod().getName() + "());\n";
                            } else {
                                // in case the from and to is not the same but mappable
                                MapperType mapperType = calculateMappableType(toType, fromType);
                                if (mapperType != null) {

                                    if (mapperType == MapperType.BASE_CONVERTER) {
                                        // calculate the converter
                                        method += getIndent(2) + "to." + fromProp.getWriteMethod().getName() + "(" +
                                                getBaseConverterFieldTransformer(imports,
                                                        fromType,
                                                        toType,
                                                        "from." + fromProp.getReadMethod().getName() + "()") +
                                                ");\n";
                                        break;
                                    } else if (mapperType == MapperType.ENUM) {
                                        method += getIndent(2) + "to." +
                                                fromProp.getWriteMethod().getName() +
                                                "(" +
                                                getEnumFieldTransformer(imports,
                                                        fromProp,
                                                        toProp,
                                                        "from." + fromProp.getReadMethod().getName() + "()") +
                                                ");\n";
                                    } else if (mapperType == MapperType.GENERATED) {
                                        method += getIndent(2) + "to." +
                                                toProp.getWriteMethod().getName() +
                                                "(" +
                                                getGeneratedFieldTransformer(imports,
                                                        fromType,
                                                        toType,
                                                        "from." + fromProp.getReadMethod().getName() + "()") +
                                                ");\n";
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
                                MapperType mapperType = calculateMappableType(toListClass, fromListClass);
                                if (mapperType == MapperType.BASE_CONVERTER) {
                                    fieldTransformer = getBaseConverterFieldTransformer(imports, fromListClass, toListClass, "o");
                                } else {
                                    fieldTransformer = "o";
                                }
                            } else {
                                MapperType mapperType = calculateMappableType(toListClass, fromListClass);
                                if (mapperType == MapperType.GENERATED) {
                                    fieldTransformer = getGeneratedFieldTransformer(imports, fromListClass, toListClass, "o");
                                } else if (mapperType == MapperType.ENUM) {
                                    fieldTransformer = getEnumFieldTransformer(imports, fromProp, toProp, "o");
                                } else if (mapperType == MapperType.BASE_CONVERTER) {
                                    fieldTransformer = getBaseConverterFieldTransformer(imports, fromListClass, toListClass, "o");
                                }
                            }

                            if (fieldTransformer != null) {
                                imports.add(calculateImport(fromListClass.getPackageName() + "." + fromListClass.getSimpleName()));
                                listFields +=
                                        getIndent(2) + "for (" + fromListClass.getSimpleName() + " o : from." + fromProp.getReadMethod().getName() + "()) {\n" +
                                                getIndent(3) + "to." + toProp.getReadMethod().getName() +
                                                "().add(" +
                                                fieldTransformer +
                                                ");\n" +
                                                getIndent(2) + "}\n";
                            }
                        }
                    }
                }

                if (!hasMapper) {
                    if (fromProp.getReadMethod() == null) {
                        System.err.println("The property " + fromProp.getDisplayName() + " apparently has no read method");
                    }
                    if (!fromProp.getReadMethod().getName().equals("getClass")) {
                        if (customCode.contains("from." + fromProp.getReadMethod().getName() + "()")) {
                            // the getter of the from obj has been called in the custom code block
                            assumablyMappedInCustomCode += getIndent(2) + "// " + fromProp.getReadMethod().getName() + "\n";
                        } else {
                            notMappedFields += getIndent(2) + "// " + fromProp.getReadMethod().getName() + "\n";
                        }
                    }
                }
            }

            String allMappers = method + listFields +
                    (notMappedFields.equals("") ? "" :
                            "\n" + (getIndent(2) + NOT_MAPPED_FIELDS_TEXT + "\n" + notMappedFields)) +
                    (assumablyMappedInCustomCode.equals("") ? "" :
                            "\n" + (getIndent(2) + ASSUMABLY_MAPPED_IN_CUSTOM_CODE_TEXT + "\n" + assumablyMappedInCustomCode));

            return new ImmutablePair<>(allMappers, imports);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBaseConverterFieldTransformer(HashSet<String> imports,
                                                    Class<?> fromClass,
                                                    Class<?> toClass,
                                                    String fieldToMap) {
        for (ConverterHolder conver : converter) {
            if (conver.from.equals(fromClass) &&
                    conver.to.equals(toClass)) {
                imports.add(calculateImport(conver.cla.getName()));
                return conver.cla.getSimpleName()
                        + "."
                        + conver.methodName
                        + "(" + fieldToMap + ")";
            }
        }
        return null;
    }

    private String getEnumFieldTransformer(HashSet<String> imports,
                                           PropertyDescriptor fromProp,
                                           PropertyDescriptor toProp,
                                           String fieldToMap) {
        String mapperFileName = calculateMapperFileName(fromProp.getPropertyType());

        imports.add(calculateImport(packageName + "." + mapperFileName));
        return mapperFileName +
                ".to" + toProp.getPropertyType().getSimpleName()
                + "(" + fieldToMap + ")";
    }

    private String getGeneratedFieldTransformer(HashSet<String> imports,
                                           Class<?> fromClass,
                                           Class<?> toClass,
                                           String fieldToMap) {
        String mapperFileName = calculateMapperFileName(fromClass);

        imports.add(calculateImport(packageName + "." + mapperFileName));
        return mapperFileName +
                ".to" + toClass.getSimpleName()
                + "(" + fieldToMap + ")";
    }

    public String enumConverter(Class<?> from, Class<?> to) {

        String method = "";

        Object[] fromEnumValues = from.getEnumConstants();
        Object[] toEnumValues = to.getEnumConstants();

        for (Object fromEnumValue : fromEnumValues) {
            for (Object toEnumValue : toEnumValues) {
                if (fromEnumValue.toString().equals(toEnumValue.toString())) {
                    method += (method.equals("") ? getIndent(2) + "if (" : " else if (") +
                            from.getSimpleName() + "." + fromEnumValue.toString() + " == from) {\n" +
                            getIndent(3) + "return " + to.getSimpleName() + "." + toEnumValue.toString() + ";\n" +
                            getIndent(2) + "}";
                    break;
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

    private String getIndent(int amountTabs) {
        return " ".repeat(Math.max(0, amountTabs * AMOUNT_SPACES_PER_INDENT));
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
                            getIndent(1) + "public static %s to%s(%s from) {\n" +
                                    "%s\n" +
                                    "%s\n" +
                                    getIndent(2) + "return null;\n" +
                                    getIndent(1) + "}\n",
                            outClass.getSimpleName(),
                            outClass.getSimpleName(),
                            inClass.getSimpleName(),
                            allMethods,
                            customCode);

                } else if (!inClass.isEnum() && !outClass.isEnum()) {

                    var allMethods = clculateClassMapping(inClass, outClass, customCode);

                    methods += String.format(
                            "    public static %s to%s(%s from) {\n" +
                                    getIndent(2) + "var to = new %s();\n" +
                                    "%s" +
                                    "%s\n" +
                                    getIndent(2) + "return to;\n" +
                                    getIndent(1) + "}\n\n",
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
                // in case there were some imports from the same package, null was added to the set
                if (o == null) {
                    continue;
                }
                imports += o + "\n";
            }

            String fileContent = "package " + packageName + ";\n" +
                    "\n" +
                    imports +
                    "\n" +
                    "public final class " + mapperName + " {\n\n" +
                    getIndent(1) + "private " + mapperName + "() {\n" +
                    getIndent(1) + "}\n\n" +
                    methods.replaceAll("\n$|^\n", "") + "\n" +
                    "}\n";
            FileUtils.write(fileDestination, fileContent);

        }
    }

    private String clacluateCustomCode(Class<?> inClass, Map<String, String> customCodeMap, Class<?> outClass) {
        String customCode = customCodeMap.get(inClass.getSimpleName() + outClass.getSimpleName());
        customCode = customCode == null ? "" : "\n" + customCode;

        return "\n" + getIndent(2) + MapGenConstants.CUSTOM_CODE_START
                + customCode
                + "\n" + getIndent(2) + MapGenConstants.CUSTOM_CODE_END;

    }

    private static String calculateMapperFileName(Class<?> clazz) {
        return clazz.getSimpleName() + "Mapper";
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
