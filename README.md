# IP Subnet Explorer

A small Java Swing desktop app with two tools:

1. **Subnet Calculator** — enter an IP + CIDR (or dotted mask) and optionally
   a MAC address. Get network/broadcast address, usable host range, wildcard
   mask, IP class, private/public status, a binary breakdown of the IP vs.
   mask, and (for the MAC) a vendor lookup against a small sample OUI table.
2. **VLSM Planner** — enter a base network (e.g. `192.168.1.0` / `24`) and a
   list of departments/segments with the hosts each one needs. The app
   allocates the smallest subnet that fits each requirement (classic VLSM:
   largest requirement first, packed contiguously) and shows the full
   breakdown for each allocated subnet.

## How to import into Eclipse

1. Open Eclipse → `File > Import... > General > Existing Projects into Workspace`.
2. Point it at this `IPSubnetExplorer` folder (the one containing this README).
3. Eclipse will detect the `src` folder automatically. If it asks for a
   `.project` file and doesn't find one, instead use:
   `File > New > Java Project`, uncheck "Use default location", point it at
   this folder, and Eclipse will pick up the existing `src` tree.
4. Right-click `Main.java` (in `com.epsilon.subnetexplorer`) →
   `Run As > Java Application`.

Requires JDK 8 or newer. No external libraries — pure Java SE (Swing only).

## Project structure

```
src/com/epsilon/subnetexplorer/
├── Main.java                  entry point
├── logic/
│   ├── IPUtils.java            core IP/CIDR bit math
│   ├── SubnetInfo.java          computes all derived subnet values
│   ├── VLSMPlanner.java         greedy VLSM allocation algorithm
│   └── OUILookup.java           small sample MAC-vendor table
└── gui/
    ├── UITheme.java             shared colors/fonts
    ├── RoundedPanel.java        card-style panel
    ├── RoundedButton.java       styled accent button
    ├── ZebraRenderer.java       striped table rows
    ├── MainFrame.java           app window + tabs
    ├── SubnetCalculatorPanel.java
    └── VLSMPlannerPanel.java
```

## Notes / limitations

- The MAC vendor lookup uses a small hand-picked sample table (~25 entries),
  not the full IEEE OUI database (which has tens of thousands of entries) —
  it's there to demonstrate the concept, not for production use.
- `/31` subnets are treated per RFC 3021 (both addresses usable, point-to-point
  links); `/32` is treated as a single host.
