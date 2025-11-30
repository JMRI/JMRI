# JMRI Layout Editor Turnout and Crossing Valid Routes

This document describes the valid connection routes for different turnout types and level crossings in JMRI's Layout Editor. Keep this file in sync when adding new turnout/slip/crossover/crossing variants.

## Connection Point Layout

All turnouts and crossings use connection points designated **A**, **B**, **C**, and **D**. The specific arrangement and valid routes vary by element type.

## Valid Routes by Element Type

### Right-Hand (RH) and Left-Hand (LH) Turnouts

```
Right-hand       Left-hand

                       C
                      //
A ==**== B       A ==**== B
     \\
      C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - throat to normal/closed, continuing route
- **A-C** (ROUTE_AC) - throat to thrown, diverging route

### Wye Turnout

```
   Wye
      B
     //
A ==**
     \\
      C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - throat to one route
- **A-C** (ROUTE_AC) - throat to other route

*Note: Both B and C are diverging routes; no "straight through" path exists.*

### Double Crossover (Double XOver)

```
    Double
A ==**==**== B
     \\//
      XX
     //\\
D ==**==**== C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - straight through, continuing route
- **C-D** (ROUTE_CD) - straight through, continuing route
- **A-C** (ROUTE_AC) - crossover, diverging route
- **B-D** (ROUTE_BD) - crossover, diverging route### Right-Hand Single Crossover (RH XOver)

```
Right-hand Crossover
A ==**===== B
     \\
      \\
D ====**== C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - straight through, continuing route
- **C-D** (ROUTE_CD) - straight through, continuing route
- **A-C** (ROUTE_AC) - crossover, diverging route

### Left-Hand Single Crossover (LH XOver)

```
Left-hand Crossover
A ====**== B
      //
     //
D ==**===== C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - straight through, continuing route
- **C-D** (ROUTE_CD) - straight through, continuing route
- **B-D** (ROUTE_BD) - crossover, diverging route### Single Slip

```
\\      //
  A==-==D
   \\ //
     X
   // \\
  B==-==C
 //      \\
```

**Valid Routes:**
- **A-C** (ROUTE_AC) - straight through one direction
- **B-D** (ROUTE_BD) - straight through other direction
- **A-D** (ROUTE_AD) - slip crossing

### Single Slip

```
\\      //
  A==-==D
   \\ //
     X
   // \\
  B==-==C
 //      \\
```

**Valid Routes:**
- **A-C** (ROUTE_AC) - straight through one direction
- **B-D** (ROUTE_BD) - straight through other direction
- **A-D** (ROUTE_AD) - slip crossing
- **B-C** (ROUTE_BC) - slip crossing

### Level Crossings

```
Level Crossing

     C
     |
     |
A ===+===B
     |
     |
     D
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - straight through, continuing route
- **C-D** (ROUTE_CD) - crossing path, continuing route

*Note: Level crossings are fixed track elements with no switching mechanism. Both routes are always available simultaneously.*

**Geometry Specification:**
- **Path A-B**: Specified with `direction="straight"` and `length` attribute
- **Path C-D**: Specified with `direction="crossing"` and `angle` attribute for crossing angle
  - Optional `length` attribute for crossing path length
  - If `length` is omitted, crossing path length defaults to same as straight path

## Key Differences

- **Regular turnouts** (RH, LH, Wye): One throat point (A) with two possible routes
- **Crossovers**: Two parallel straight-through paths plus crossover connection(s)
- **Slips**: Two crossing paths plus slip crossing paths, with connection points arranged differently than crossovers
- **Level crossings**: Two fixed intersecting paths (one straight, one crossing) with no switching mechanism

## Route Constants

The following route constants are defined in the JMRI code:

- `ROUTE_AC = 0x02` - A-C route connection
- `ROUTE_BD = 0x04` - B-D route connection
- `ROUTE_AD = 0x06` - A-D route connection
- `ROUTE_BC = 0x08` - B-C route connection

## Source References

This information is derived from:
- `java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java`
- `java/src/jmri/jmrit/display/layoutEditor/LayoutSlip.java`
- `java/src/jmri/jmrit/display/layoutEditor/LayoutLevelXing.java`
- `xml/schema/tracktiles.xsd` - Track tile XML schema
- Various turnout and crossing-specific implementation classes
