package mapper;

import teste.PersonTo;
import mapper.LevelFromMapper;
import teste.PersonFrom;

public final class PersonFromMapper {
    private PersonFromMapper() {
    }
    public static PersonTo toPersonTo(PersonFrom from) {
       var to = new PersonTo();
       to.setLevel(LevelFromMapper.toLevelTo(from.getLevel()));
         to.setS1(from.getS1());
        for (var o : from.getList()) {
            to.getList().add(o);
       }
        for (var o : from.getNameList()) {
            to.getNameList().add(NameFromMapper.toNameTo(o));
       }

// following fields ar not mapped!!! please map them yourself
        // getClass
        // setImNotMapped
        // getList2

       
        //custom code start






        var s = "";
        //custom code end
       return to;
    }

}

