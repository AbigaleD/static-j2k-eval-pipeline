package cases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenericWildcardsAndCheckedExceptions {
    public <T extends Number & Comparable<T>> List<T> copyPositive(List<? extends T> input) throws IOException {
        List<T> result = new ArrayList<>();
        for (T item : input) {
            if (item.doubleValue() < 0) {
                throw new IOException("negative value: " + item);
            }
            result.add(item);
        }
        return result;
    }
}
