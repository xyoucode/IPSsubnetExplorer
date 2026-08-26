package com.epsilon.subnetexplorer.logic;


public class SubnetInfo {

    public final String ipAddress;
    public final int cidr;
    public final String subnetMask;
    public final String wildcardMask;
    public final String networkAddress;
    public final String broadcastAddress;
    public final String firstUsableHost;
    public final String lastUsableHost;
    public final long totalAddresses;
    public final long usableHosts;
    public final String ipClass;
    public final boolean privateOrReserved;
    public final String binaryIp;
    public final String binaryMask;

    private SubnetInfo(String ipAddress, int cidr, String subnetMask, String wildcardMask,
                        String networkAddress, String broadcastAddress, String firstUsableHost,
                        String lastUsableHost, long totalAddresses, long usableHosts,
                        String ipClass, boolean privateOrReserved, String binaryIp, String binaryMask) {
        this.ipAddress = ipAddress;
        this.cidr = cidr;
        this.subnetMask = subnetMask;
        this.wildcardMask = wildcardMask;
        this.networkAddress = networkAddress;
        this.broadcastAddress = broadcastAddress;
        this.firstUsableHost = firstUsableHost;
        this.lastUsableHost = lastUsableHost;
        this.totalAddresses = totalAddresses;
        this.usableHosts = usableHosts;
        this.ipClass = ipClass;
        this.privateOrReserved = privateOrReserved;
        this.binaryIp = binaryIp;
        this.binaryMask = binaryMask;
    }

    public static SubnetInfo calculate(String ip, int cidr) {
        long ipLong = IPUtils.ipToLong(ip);
        long maskLong = IPUtils.maskLongFromCidr(cidr);
        long wildcard = (~maskLong) & 0xFFFFFFFFL;
        long network = ipLong & maskLong;
        long broadcast = network | wildcard;
        long totalAddresses = 1L << (32 - cidr);

        long firstUsable;
        long lastUsable;
        long usableHosts;

        if (cidr == 32) {
            firstUsable = network;
            lastUsable = network;
            usableHosts = 1;
        } else if (cidr == 31) {
            
            firstUsable = network;
            lastUsable = broadcast;
            usableHosts = 2;
        } else {
            firstUsable = network + 1;
            lastUsable = broadcast - 1;
            usableHosts = totalAddresses - 2;
        }

        return new SubnetInfo(
                ip,
                cidr,
                IPUtils.longToIp(maskLong),
                IPUtils.longToIp(wildcard),
                IPUtils.longToIp(network),
                IPUtils.longToIp(broadcast),
                IPUtils.longToIp(firstUsable),
                IPUtils.longToIp(lastUsable),
                totalAddresses,
                usableHosts,
                IPUtils.getIPClass(ipLong),
                IPUtils.isPrivateOrReserved(ipLong),
                IPUtils.toBinaryGroups(ipLong),
                IPUtils.toBinaryGroups(maskLong)
        );
    }
}
