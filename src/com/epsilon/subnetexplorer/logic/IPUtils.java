package com.epsilon.subnetexplorer.logic;


public final class IPUtils {

    private IPUtils() {
       
    }

    
    public static boolean isValidIPv4(String ip) {
        if (ip == null) return false;
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) return false;
        for (String p : parts) {
            if (p.isEmpty() || p.length() > 3) return false;
            for (char c : p.toCharArray()) {
                if (!Character.isDigit(c)) return false;
            }
            int value = Integer.parseInt(p);
            if (value < 0 || value > 255) return false;
            
            if (p.length() > 1 && p.charAt(0) == '0') return false;
        }
        return true;
    }

   
    public static long ipToLong(String ip) {
        if (!isValidIPv4(ip)) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }
        String[] parts = ip.trim().split("\\.");
        long result = 0;
        for (String p : parts) {
            result = (result << 8) | Integer.parseInt(p);
        }
        return result & 0xFFFFFFFFL;
    }
    
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }

   
    public static boolean looksLikeDottedMask(String s) {
        return s != null && s.contains(".");
    }

    
    public static int cidrFromMask(String mask) {
        if (!isValidIPv4(mask)) {
            throw new IllegalArgumentException("Invalid subnet mask: " + mask);
        }
        long m = ipToLong(mask);
        String binary = String.format("%32s", Long.toBinaryString(m)).replace(' ', '0');
        int firstZero = binary.indexOf('0');
        int lastOne = binary.lastIndexOf('1');
        if (firstZero != -1 && lastOne != -1 && firstZero < lastOne) {
            throw new IllegalArgumentException("Mask is not contiguous: " + mask);
        }
        return Long.bitCount(m);
    }

   
    public static String maskFromCidr(int cidr) {
        return longToIp(maskLongFromCidr(cidr));
    }

   
    public static long maskLongFromCidr(int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("CIDR must be between 0 and 32: " + cidr);
        }
        if (cidr == 0) return 0L;
        return (0xFFFFFFFFL << (32 - cidr)) & 0xFFFFFFFFL;
    }

   
    public static String toBinaryGroups(long value) {
        String bits = String.format("%32s", Long.toBinaryString(value & 0xFFFFFFFFL)).replace(' ', '0');
        return bits.substring(0, 8) + "." + bits.substring(8, 16) + "." +
               bits.substring(16, 24) + "." + bits.substring(24, 32);
    }

    public static String getIPClass(long ip) {
        int firstOctet = (int) ((ip >> 24) & 0xFF);
        if (firstOctet < 128) return "A";
        if (firstOctet < 192) return "B";
        if (firstOctet < 224) return "C";
        if (firstOctet < 240) return "D (Multicast)";
        return "E (Experimental)";
    }

   
    public static boolean isPrivateOrReserved(long ip) {
        return inRange(ip, "10.0.0.0", 8) ||
               inRange(ip, "172.16.0.0", 12) ||
               inRange(ip, "192.168.0.0", 16) ||
               inRange(ip, "127.0.0.0", 8) ||
               inRange(ip, "169.254.0.0", 16);
    }

    private static boolean inRange(long ip, String networkIp, int cidr) {
        long mask = maskLongFromCidr(cidr);
        long network = ipToLong(networkIp) & mask;
        return (ip & mask) == network;
    }
}
