package cases;

import java.util.Arrays;
import java.util.List;

public class OverloadsAndVarargs {
    public String join(String separator, String... parts) {
        return String.join(separator, parts);
    }

    public String join(char separator, List<String> parts) {
        String[] copy = parts.toArray(new String[0]);
        return join(Character.toString(separator), copy);
    }

    public List<String> tokenize(String value) {
        return Arrays.asList(value.split("\\s+"));
    }
}
