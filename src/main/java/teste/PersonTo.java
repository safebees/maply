package teste;

import java.util.ArrayList;
import java.util.List;

public class PersonTo {

    String s1;
    LevelTo level;

    NameTo name;

    List<String> list = new ArrayList<>();
    List<Integer> integerList = new ArrayList<>();
    List<Long> numberList = new ArrayList<>();
    List<NameTo> nameList = new ArrayList<>();
    ArrayList<String> list2 = new ArrayList<>();

    public NameTo getName() {
        return name;
    }

    public void setName(NameTo name) {
        this.name = name;
    }

    public List<Long> getNumberList() {
        return numberList;
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

    public ArrayList<String> getList2() {
        return list2;
    }

    public List<NameTo> getNameList() {
        return nameList;
    }

    public LevelTo getLevel() {
        return level;
    }

    public void setLevel(LevelTo level) {
        this.level = level;
    }
}
