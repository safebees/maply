package mapper;

import teste.NameFrom;
import teste.NameTo;
import teste.NameTo2;
import customtransformer.PrimitiveUtils;

public final class NameFromMapper {
    private NameFromMapper() {
    }

    public static NameTo toNameTo(NameFrom from) {
        var to = new NameTo();
        to.setEinInteger(PrimitiveUtils.toInt(from.getEinInteger()));
        to.setS1(from.getS1());

// following fields ar not mapped!!! please map them yourself
        // getClass


        //custom code start
        //custom code end
        return to;
    }

    public static NameTo2 toNameTo2(NameFrom from) {
        var to = new NameTo2();
        to.setEinInteger(PrimitiveUtils.toInt(from.getEinInteger()));
        to.setS1(from.getS1());

// following fields ar not mapped!!! please map them yourself
        // getClass


        //custom code start
        //custom code end
        return to;
    }

}

