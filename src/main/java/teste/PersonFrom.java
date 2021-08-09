package teste;

import java.util.ArrayList;
import java.util.List;

public class PersonFrom {

    String s1;
    String imNotMapped;
    LevelFrom level;

    List<String> list = new ArrayList<>();
    ArrayList<String> list2 = new ArrayList<>();

    List<NameFrom> nameList = new ArrayList<>();

    public String getImNotMapped() {
        return imNotMapped;
    }

    public void setImNotMapped(String imNotMapped) {
        this.imNotMapped = imNotMapped;
    }

    public String getS1() {
        return s1;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public List<String> getList() {
        return list;
    }

    public List<String> getList2() {
        return list2;
    }

    public List<NameFrom> getNameList() {
        return nameList;
    }

    public LevelFrom getLevel() {
        return level;
    }

    public void setLevel(LevelFrom level) {
        this.level = level;
    }
}
