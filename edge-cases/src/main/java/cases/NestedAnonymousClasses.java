package cases;

import java.util.Comparator;
import java.util.List;

public class NestedAnonymousClasses {
    public Comparator<String> byLengthThenName() {
        return new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                Comparator<String> nested = new Comparator<String>() {
                    @Override
                    public int compare(String a, String b) {
                        return a.compareToIgnoreCase(b);
                    }
                };
                int length = Integer.compare(left.length(), right.length());
                return length != 0 ? length : nested.compare(left, right);
            }
        };
    }

    public void sort(List<String> names) {
        names.sort(byLengthThenName());
    }
}
