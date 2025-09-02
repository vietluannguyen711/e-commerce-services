package com.example.e_commerce_services.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AttrParser {

    private AttrParser() {
    }

    // "color:black,size:M" -> Map[color=black, size=M]
    public static Map<String, String> parse(String attrs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (attrs == null || attrs.isBlank()) {
            return map;
        }
        for (String pair : attrs.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2 && !kv[0].isBlank() && !kv[1].isBlank()) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    // Map -> JSON string {"k":"v",...}
    public static String toJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
