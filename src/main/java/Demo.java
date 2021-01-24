import jodd.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<Integer>();
        list.stream().reduce((x, y) -> x + y).get();
    }
}
