# JMRI Layout Editor Turnout Valid Paths

This document describes the valid connection paths for different turnout types in JMRI's Layout Editor.

## Connection Point Layout

All turnouts use connection points designated **A**, **B**, **C**, and **D**. The specific arrangement and valid paths vary by turnout type.

## Valid Paths by Turnout Type

### Right-Hand (RH) and Left-Hand (LH) Turnouts

```
Right-hand       Left-hand

                       C
                      //
A ==**== B       A ==**== B
     \\
      C
```

**Valid Paths:**
- **A to B** (throat to normal/closed) - continuing route
- **A to C** (throat to thrown) - diverging route

### Wye Turnout

```
   Wye
      B
     //
A ==**
     \\
      C
```

**Valid Paths:**
- **A to B** (throat to one route)
- **A to C** (throat to other route)

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

**Valid Paths:**
- **A to B** (straight through) - continuing route
- **C to D** (straight through) - continuing route  
- **A to C** (crossover) - diverging route
- **B to D** (crossover) - diverging route

### Right-Hand Single Crossover (RH XOver)

```
Right-hand Crossover
A ==**===== B
     \\
      \\
D ====**== C
```

**Valid Paths:**
- **A to B** (straight through) - continuing route
- **C to D** (straight through) - continuing route
- **A to C** (crossover) - diverging route

### Left-Hand Single Crossover (LH XOver)

```
Left-hand Crossover
A ====**== B
      //
     //
D ==**===== C
```

**Valid Paths:**
- **A to B** (straight through) - continuing route  
- **C to D** (straight through) - continuing route
- **B to D** (crossover) - diverging route

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

**Valid Paths:**
- **A to C** (STATE_AC) - straight through one direction
- **B to D** (STATE_BD) - straight through other direction  
- **A to D** (STATE_AD) - slip crossing

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

**Valid Paths:**
- **A to C** (STATE_AC) - straight through one direction
- **B to D** (STATE_BD) - straight through other direction
- **A to D** (STATE_AD) - slip crossing one way
- **B to C** (STATE_BC) - slip crossing other way

## Key Differences

- **Regular turnouts** (RH, LH, Wye): One throat point (A) with two possible routes
- **Crossovers**: Two parallel straight-through paths plus crossover connection(s)
- **Slips**: Two crossing paths plus slip crossing paths, with connection points arranged differently than crossovers

## State Constants

The following state constants are defined in the JMRI code:

- `STATE_AC = 0x02` - A to C connection
- `STATE_BD = 0x04` - B to D connection  
- `STATE_AD = 0x06` - A to D connection
- `STATE_BC = 0x08` - B to C connection

## Source References

This information is derived from:
- `java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java`
- `java/src/jmri/jmrit/display/layoutEditor/LayoutSlip.java`
- Various turnout-specific implementation classes

*Generated from JMRI source code documentation and comments.*