package taskmanager.util;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    public static String escape(String value) {
        if (value == null) return "";
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        return needQuotes ? "\"" + v + "\"" : v;
    }

    public static String unescape(String field) {
        if (field == null) return "";
        String f = field.trim();
        if (f.length() >= 2 && f.startsWith("\"") && f.endsWith("\"")) {
            f = f.substring(1, f.length() - 1).replace("\"\"", "\"");
        }
        return f;
    }

    /** RFC4180-style split on comma, respecting double-quoted fields. */
    public static String[] splitLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
