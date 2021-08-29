package teste;

import java.util.ArrayList;
import java.util.List;

public class PersonFrom {

    String s1;
    String imNotMapped;
    LevelFrom level;

    NameFrom name;

    List<String> list = new ArrayList<>();
    List<Integer> numberList = new ArrayList<>();
    List<NameFrom> nameList = new ArrayList<>();
    ArrayList<String> list2 = new ArrayList<>();

    public NameFrom getName() {
        return name;
    }

    public void setName(NameFrom name) {
        this.name = name;
    }

    public List<Integer> getNumberList() {
        return numberList;
    }

    public List<NameFrom> getNameList() {
        return nameList;
    }

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

    public LevelFrom getLevel() {
        return level;
    }

    public void setLevel(LevelFrom level) {
        this.level = level;
    }
}
