package mapper;

import teste.LevelFrom;
import teste.LevelTo;

public final class LevelFromMapper {

    private LevelFromMapper() {
    }

    public static LevelTo toLevelTo(LevelFrom from) {
        if (LevelFrom.LOW == from) {
            return LevelTo.LOW;
        } else if (LevelFrom.MEDIUM == from) {
            return LevelTo.MEDIUM;
        } else if (LevelFrom.HIGH == from) {
            return LevelTo.HIGH;
        }

        // custom code start
        // custom code end
        return null;
    }
}

