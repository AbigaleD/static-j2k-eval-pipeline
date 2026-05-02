package cases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RawTypesAndLegacyCollections {
    private final List rows = new ArrayList();
    private final Map indexes = new HashMap();

    public void addRow(Object key, Object value) {
        Map row = new HashMap();
        row.put("key", key);
        row.put("value", value);
        rows.add(row);
        indexes.put(key, row);
    }

    public String valueAsString(Object key) {
        Object rowObject = indexes.get(key);
        if (!(rowObject instanceof Map)) {
            return "";
        }
        Map row = (Map) rowObject;
        Object value = row.get("value");
        return value == null ? "" : (String) value;
    }

    public List copyRows() {
        List copy = new ArrayList();
        for (Object row : rows) {
            copy.add((Map) row);
        }
        return copy;
    }
}
