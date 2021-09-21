package mapper;

import teste.PersonTo;
import java.lang.Integer;
import teste.PersonFrom;
import teste.NameFrom;
import java.lang.String;
import customtransformer.PrimitiveUtils;

public final class PersonFromMapper {

    private PersonFromMapper() {
    }

    public static PersonTo toPersonTo(PersonFrom from) {
        var to = new PersonTo();
        to.setLevel(LevelFromMapper.toLevelTo(from.getLevel()));
        to.setName(NameFromMapper.toNameTo(from.getName()));
        to.setS1(from.getS1());
        for (String o : from.getList()) {
            to.getList().add(o);
        }
        for (NameFrom o : from.getNameList()) {
            to.getNameList().add(NameFromMapper.toNameTo(o));
        }
        for (Integer o : from.getNumberList()) {
            to.getNumberList().add(PrimitiveUtils.intToLong(o));
        }

        // TODO following fields are not mapped!!! please map them yourself
        // getList2

        // These fields could not be assigned be the generator, but are apparently mapped in the custom code block
        // getImNotMapped

        // custom code start
        System.out.println(from.getImNotMapped());
        // custom code end
        return to;
    }
}

