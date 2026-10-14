# JMRI Layout Editor Turnout and Crossing Valid Routes

This document describes the valid connection routes for different turnout types and level crossings in JMRI's Layout Editor. Keep this file in sync when adding new turnout/slip/crossover/crossing variants.

## Connection Point Layout

All turnouts and crossings use connection points designated **A**, **B**, **C**, and **D**. The specific arrangement and valid routes vary by element type.

## Valid Routes by Element Type

### Right-Hand (RH) and Left-Hand (LH) Turnouts

Throat A on the left. Continuing leg B straight ahead. The diverging leg C
goes down for a right-hand turnout, up for a left-hand turnout.

```
Right-hand              Left-hand

                              C
                             //
A ====**==== B        A ====**==== B
       \\
        C
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - throat to normal/closed, continuing route
- **A-C** (ROUTE_AC) - throat to thrown, diverging route

### Wye Turnout

Throat A on the left. Both legs fan out (B up, C down).

```
   Wye
        B  (closed / continuing)
       //
A ====<
       \\
        C  (thrown / diverging)
```

**Valid Routes:**
- **A-B** (ROUTE_AB) - throat to continuing leg, active when the turnout is closed (continuing route)
- **A-C** (ROUTE_AC) - throat to diverging leg, active when the turnout is thrown (diverging route)

*Note: On a wye both legs diverge geometrically but the route roles still follow the standard convention - B is the closed/continuing leg and C is the thrown/diverging leg (same as ROUTE_AB/ROUTE_AC for RH and LH turnouts).*

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
- **B-D** (ROUTE_BD) - crossover, diverging route

### Right-Hand Single Crossover (RH XOver)

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
- **B-D** (ROUTE_BD) - crossover, diverging route

### Single Slip

A single slip has the two straight-through routes plus **one** slip crossing
(A-D). There is no B-C connection.

```
\\      //
  A==-==D
   \\ //
     X
   // \\
  B     C
 //      \\
```

**Valid Routes:**
- **A-C** (ROUTE_AC) - straight through one direction
- **B-D** (ROUTE_BD) - straight through other direction
- **A-D** (ROUTE_AD) - slip crossing

*Note: Unlike a double slip, a single slip has no B-C crossing route. The code removes STATE_BC for SINGLE_SLIP.*

### Double Slip

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

  A       D
   \\    //
     \\ //
       X
     // \\
   //    \\
  B       C
```

**Valid Routes:**
- **A-C** (ROUTE_AC) - straight through, continuing route
- **B-D** (ROUTE_BD) - straight through, continuing route

*Note: Level crossings are fixed track elements with no switching mechanism.
Both routes are always available simultaneously. The two straight segments
A-C and B-D may be in the same or different Layout Blocks.*

**Geometry Specification:**
- **Straight path**: Specified with `direction="straight"` and `length` attribute
- **Crossing path**: Specified with `direction="crossing"` and `angle` attribute for crossing angle
  - Optional `length` attribute for crossing path length
  - If `length` is omitted, crossing path length defaults to same as straight path

## Key Differences

- **Regular turnouts** (RH, LH, Wye): One throat point (A) with two possible routes
- **Crossovers**: Two parallel straight-through paths plus crossover connection(s)
- **Slips**: Two crossing paths plus slip crossing paths, with connection points arranged differently than crossovers
- **Level crossings**: Two fixed intersecting paths (one straight, one crossing) with no switching mechanism

## Route/State Constants

For turnouts and slips, the diverging/crossing connections are encoded as
`STATE_*` constants in `LayoutTurnout` (and inherited by `LayoutSlip`):

- `STATE_AC = 0x02` - A-C connection
- `STATE_BD = 0x04` - B-D connection
- `STATE_AD = 0x06` - A-D connection
- `STATE_BC = 0x08` - B-C connection (double slip only)

*Note: The plain A-B "closed/continuing" route of an RH/LH/Wye turnout is not
one of these bit constants. It corresponds to the turnout's CLOSED state. The
`(ROUTE_xx)` labels used above are descriptive shorthand for the connection
points, not literal constant names.*

## Source References

This information is derived from:
- `java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java`
- `java/src/jmri/jmrit/display/layoutEditor/LayoutTurnoutView.java`
- `java/src/jmri/jmrit/display/layoutEditor/LayoutSlip.java`
- `java/src/jmri/jmrit/display/layoutEditor/LevelXing.java`
- `xml/schema/tracktiles.xsd` - Track tile XML schema
- Various turnout and crossing-specific implementation classes
