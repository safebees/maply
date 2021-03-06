import customtransformer.DateUtils;
import customtransformer.PrimitiveUtils;
import teste.*;

class MapperGenerator {
    public static void main(String[] args) {

        new MapperBuilder()
                .location("C:/workarea/workspace/maply/src/main/java/mapper/")
                .packageName("mapper")
                .converter(DateUtils.class)
                .converter(PrimitiveUtils.class)
                .mapEnum(LevelFrom.class, LevelTo.class)
                .map(NameFrom.class, NameTo.class)
                .map(NameFrom.class, NameTo2.class)
                .map(NameTo.class, NameFrom.class)
                .map(PersonFrom.class, PersonTo.class)
                .build();
    }
}
