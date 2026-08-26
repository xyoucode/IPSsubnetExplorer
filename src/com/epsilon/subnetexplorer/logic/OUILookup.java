package com.epsilon.subnetexplorer.logic;

import java.util.HashMap;
import java.util.Map;


public final class OUILookup {

    private static final Map<String, String> TABLE = new HashMap<>();

    static {
        TABLE.put("00:1A:2B", "Cisco Systems");
        TABLE.put("00:50:56", "VMware, Inc.");
        TABLE.put("00:0C:29", "VMware, Inc.");
        TABLE.put("3C:5A:B4", "Google, Inc.");
        TABLE.put("F4:5C:89", "Apple, Inc.");
        TABLE.put("A4:83:E7", "Apple, Inc.");
        TABLE.put("00:1B:63", "Apple, Inc.");
        TABLE.put("B8:27:EB", "Raspberry Pi Foundation");
        TABLE.put("DC:A6:32", "Raspberry Pi Foundation");
        TABLE.put("00:15:5D", "Microsoft (Hyper-V)");
        TABLE.put("00:03:FF", "Microsoft Corporation");
        TABLE.put("00:1C:23", "Dell Inc.");
        TABLE.put("D4:BE:D9", "Dell Inc.");
        TABLE.put("00:1F:29", "Hewlett Packard");
        TABLE.put("3C:D9:2B", "Hewlett Packard Enterprise");
        TABLE.put("00:16:6F", "Intel Corporation");
        TABLE.put("00:1B:21", "Intel Corporation");
        TABLE.put("00:26:B9", "Dell Inc.");
        TABLE.put("18:66:DA", "Samsung Electronics");
        TABLE.put("00:14:22", "TP-Link Technologies");
        TABLE.put("50:C7:BF", "TP-Link Technologies");
        TABLE.put("00:1E:8C", "D-Link Corporation");
        TABLE.put("00:26:5A", "Netgear Inc.");
        TABLE.put("00:E0:4C", "Realtek Semiconductor");
        TABLE.put("00:18:0A", "Huawei Technologies");
        TABLE.put("00:E0:FC", "Huawei Technologies");
    }

    private OUILookup() {
    }

    public static boolean isValidMac(String mac) {
        if (mac == null) return false;
        String cleaned = mac.trim();
        return cleaned.matches("^([0-9A-Fa-f]{2}[:\\-]){5}[0-9A-Fa-f]{2}$");
    }

 
    public static String lookup(String mac) {
        if (!isValidMac(mac)) {
            throw new IllegalArgumentException("Invalid MAC address format: " + mac);
        }
        String normalized = mac.trim().replace('-', ':').toUpperCase();
        String prefix = normalized.substring(0, 8); // "XX:XX:XX"
        String vendor = TABLE.get(prefix);
        if (vendor == null) {
            return "Unknown (not in local sample OUI table of " + TABLE.size() + " entries)";
        }
        return vendor;
    }
}
