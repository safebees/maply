package mapper;

import teste.NameFrom;
import teste.NameTo;
import customtransformer.PrimitiveUtils;

public final class NameToMapper {

    private NameToMapper() {
    }

    public static NameFrom toNameFrom(NameTo from) {
        var to = new NameFrom();
        to.setEinInteger(PrimitiveUtils.toPrimInt(from.getEinInteger()));
        to.setS1(from.getS1());

        // custom code start
        // custom code end
        return to;
    }
}

