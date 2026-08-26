package com.epsilon.subnetexplorer.logic;

import java.util.ArrayList;
import java.util.List;


public final class VLSMPlanner {

    private VLSMPlanner() {
    }

  
    public static class Requirement {
        public final String name;
        public final long hostsNeeded;

        public Requirement(String name, long hostsNeeded) {
            this.name = name;
            this.hostsNeeded = hostsNeeded;
        }
    }

   
    public static class Allocation {
        public final String name;
        public final long hostsNeeded;
        public final int cidr;
        public final String subnetMask;
        public final String networkAddress;
        public final String broadcastAddress;
        public final String firstUsableHost;
        public final String lastUsableHost;
        public final long usableHosts;

        public Allocation(String name, long hostsNeeded, int cidr, String subnetMask,
                           String networkAddress, String broadcastAddress,
                           String firstUsableHost, String lastUsableHost, long usableHosts) {
            this.name = name;
            this.hostsNeeded = hostsNeeded;
            this.cidr = cidr;
            this.subnetMask = subnetMask;
            this.networkAddress = networkAddress;
            this.broadcastAddress = broadcastAddress;
            this.firstUsableHost = firstUsableHost;
            this.lastUsableHost = lastUsableHost;
            this.usableHosts = usableHosts;
        }
    }

   
    public static List<Allocation> plan(String baseIp, int baseCidr, List<Requirement> requirements) {
        if (requirements.isEmpty()) {
            throw new IllegalArgumentException("Add at least one department/segment before planning.");
        }

      
        List<Requirement> sorted = new ArrayList<>(requirements);
        sorted.sort((a, b) -> Long.compare(b.hostsNeeded, a.hostsNeeded));

        long baseMask = IPUtils.maskLongFromCidr(baseCidr);
        long blockStart = IPUtils.ipToLong(baseIp) & baseMask;
        long blockSizeTotal = 1L << (32 - baseCidr);
        long limit = blockStart + blockSizeTotal; 

        long cursor = blockStart;
        List<Allocation> results = new ArrayList<>();

        for (Requirement req : sorted) {
            if (req.hostsNeeded <= 0) {
                throw new IllegalArgumentException("\"" + req.name + "\" needs a positive number of hosts.");
            }

            int chosenCidr = smallestCidrFor(req.hostsNeeded);
            long size = 1L << (32 - chosenCidr);

            if (cursor + size > limit) {
                throw new IllegalArgumentException(
                        "Not enough address space in " + baseIp + "/" + baseCidr +
                        " to fit \"" + req.name + "\" (" + req.hostsNeeded + " hosts). " +
                        "Try a larger base network.");
            }

            long network = cursor;
            long broadcast = network + size - 1;
            long firstUsable = (chosenCidr >= 31) ? network : network + 1;
            long lastUsable = (chosenCidr >= 31) ? broadcast : broadcast - 1;
            long usable = (chosenCidr == 32) ? 1 : (chosenCidr == 31) ? 2 : size - 2;

            results.add(new Allocation(
                    req.name,
                    req.hostsNeeded,
                    chosenCidr,
                    IPUtils.maskFromCidr(chosenCidr),
                    IPUtils.longToIp(network),
                    IPUtils.longToIp(broadcast),
                    IPUtils.longToIp(firstUsable),
                    IPUtils.longToIp(lastUsable),
                    usable
            ));

            cursor += size;
        }

        return results;
    }

    
    private static int smallestCidrFor(long hostsNeeded) {
        for (int cidr = 30; cidr >= 0; cidr--) {
            long usable = (1L << (32 - cidr)) - 2;
            if (usable >= hostsNeeded) {
                return cidr;
            }
        }
        return 0;
    }
}
