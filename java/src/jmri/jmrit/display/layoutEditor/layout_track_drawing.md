# JMRI Layout Editor Track Drawing Architecture

This document describes the MVC architecture and code flow for drawing track elements in JMRI's Layout Editor.

## Track Drawing Classes and Methods

### TrackSegments

**Model Class**: `TrackSegment`
- Location: `java/src/jmri/jmrit/display/layoutEditor/TrackSegment.java`

**View Class**: `TrackSegmentView`
- Location: `java/src/jmri/jmrit/display/layoutEditor/TrackSegmentView.java`
- **Drawing Methods**:
  - `draw1(Graphics2D g2, boolean isMain, boolean isBlock)` - Main track line drawing
  - `draw2(Graphics2D g2, boolean isMain, float railDisplacement)` - Rail displacement drawing
  - `drawDecorations(Graphics2D g2)` - Track decorations (arrows, etc.)

**Coordinate Methods**:
- `getCoordsCenter()` - Returns center point
- `getCoords1()`, `getCoords2()` - Returns endpoint coordinates

### Turnouts (including Crossovers and Slips)

**Model Classes**:
- `LayoutTurnout` - Base turnout class
- `LayoutLHTurnout`, `LayoutRHTurnout`, etc. - Specific turnout types
- `LayoutSlip` - Slip turnouts

**View Classes**:
- **`LayoutTurnoutView`** - Main turnout view class
  - Location: `java/src/jmri/jmrit/display/layoutEditor/LayoutTurnoutView.java`
  - **Drawing Methods**:
    - `draw1(Graphics2D g2, boolean isMain, boolean isBlock)` (Lines 1995-2150) - Main track drawing with state logic
    - `draw2(Graphics2D g2, boolean isMain, float railDisplacement)` (Line 2390+) - Rail displacement
    - `drawTurnoutControls(Graphics2D g2)` (Line 3095) - Control circles
    - `drawEditControls(Graphics2D g2)` (Line 3128) - Edit control points
    - `drawDecorations(Graphics2D g2)` (Line 1988) - Track decorations

- **`LayoutSlipView`** - Slip turnout view
- **`LayoutDoubleXOverView`** - Double crossover view
- **`LayoutRHXOverView`**, **`LayoutLHXOverView`** - Single crossover views
- **`LayoutDoubleSlipView`**, **`LayoutSingleSlipView`** - Slip views

**Coordinate Methods** (LayoutTurnoutView):
- `getCoordsA()`, `getCoordsB()`, `getCoordsC()`, `getCoordsD()` (Lines 762-800) - Connector coordinates
- `setCoordsA()`, `setCoordsB()`, `setCoordsC()`, `setCoordsD()` (Lines 1255+) - Set connector positions
- Uses displacement vectors `dispA`, `dispB` from center point

### Level Crossings

**Model Class**: `LevelXing`

**View Class**: `LevelXingView`
- Location: `java/src/jmri/jmrit/display/layoutEditor/LevelXingView.java`
- **Drawing Methods**:
  - `draw1(Graphics2D g2, boolean isMain, boolean isBlock)` - Main crossing line drawing
  - `draw2(Graphics2D g2, boolean isMain, float railDisplacement)` - Rail displacement drawing
  - `drawDecorations(Graphics2D g2)` - Track decorations

**Coordinate Methods** (Lines 290-350):
- `getCoordsA()`, `getCoordsB()`, `getCoordsC()`, `getCoordsD()` - Connector coordinates
- `setCoordsA()`, `setCoordsB()`, `setCoordsC()`, `setCoordsD()` - Set connector positions
- Uses displacement vectors `dispA`, `dispB` for A-C and B-D paths

## Drawing Architecture

### MVC Pattern
- **Model**: Track element classes (`TrackSegment`, `LayoutTurnout`, `LevelXing`)
- **View**: View classes (`TrackSegmentView`, `LayoutTurnoutView`, `LevelXingView`)
- **Controller**: `LayoutEditor` manages user interactions and coordinates model/view

### Drawing Layers
1. **Block Layer** (`isBlock=true`): Block-colored track lines
2. **Main Layer** (`isMain=true/false`): Mainline vs branch line styling
3. **Rail Layer**: Rail displacement for 3D effect
4. **Control Layer**: Edit controls and turnout control circles
5. **Decoration Layer**: Arrows, labels, and other decorations

### Coordinate System
- **Center Point**: Each track element has a center coordinate
- **Displacement Vectors**: Relative positions of connectors from center
  - Turnouts: `dispA`, `dispB` define connector offsets
  - Level Crossings: `dispA` (A-C axis), `dispB` (B-D axis)
- **Scaling**: Global dimensions from `LayoutEditor.getTurnoutBX()`, `getTurnoutWid()`, etc.

## User Interaction Code Flow

### Shift-Click to Placed Tile Flow

1. **Mouse Event Capture**
   - `LayoutEditor.mousePressed()` - Detects shift-click
   - `currentPoint` set to click location

2. **Tile Selection Check**
   - `LayoutEditorToolBarPanel.getTurnoutDirection()` - Gets selected connector
   - Track tile catalog checked for selected tile

3. **Anchor Point Detection**
   - `LayoutEditor.findNearbyAnchorForTurnout()` (Line 5795) - Searches for nearby anchors
   - Checks anchor availability (< 2 connections)

4. **Tile-Aware Placement**
   - **Turnouts**: `LayoutEditor.addLayoutTurnoutWithTileSupport()` (Line 6143)
   - **Slips**: `LayoutEditor.addLayoutSlipWithTileSupport()` (Line 6041)
   - **Level Crossings**: `LayoutEditor.addLevelXingWithTileSupport()` (Line 6094)

5. **Connector Alignment**
   - `LayoutEditor.calculateConnectorPositions()` (Line 5711) - Calculates connector positions
   - Selected connector aligned to anchor point
   - Center position adjusted: `newCenter = center + offset`

6. **Tile Assignment**
   - Track element gets assigned selected `TrackTile`
   - Path length calculations become available

### Shift-Click to Non-Tile Placement Flow

1. **Mouse Event Capture**
   - Same as tile flow: `LayoutEditor.mousePressed()`

2. **No Tile Selected**
   - No tile catalog involvement
   - Standard placement logic used

3. **Standard Placement**
   - **Turnouts**: `LayoutEditor.addLayoutTurnout()` - Basic turnout creation
   - **Level Crossings**: `LayoutEditor.addLevelXing()` - Basic crossing creation
   - **Slips**: `LayoutEditor.addLayoutSlip()` - Basic slip creation

4. **Default Positioning**
   - Element placed at click location
   - Standard dimensions used from `LayoutEditor` dimension methods
   - No automatic anchor alignment

5. **Manual Connection**
   - User must manually connect to existing track segments
   - No automatic tile-based path calculations

### Mouse Release Processing

1. **Connection Detection**
   - `LayoutEditor.hitPointCheckLayoutTurnouts()` (Line 4472) - Checks for nearby connections
   - Automatic connection to nearby track segments

2. **Redraw**
   - `LayoutEditor.redrawPanel()` - Triggers view refresh
   - All view `draw1()`, `draw2()`, etc. methods called

3. **State Update**
   - `LayoutEditor.setDirty()` - Marks layout as modified
   - Edit history updated

## Key Differences: Tile vs Non-Tile

| Aspect | With Tile | Without Tile |
|--------|-----------|--------------|
| **Placement Method** | `add*WithTileSupport()` | `add*()` standard methods |
| **Anchor Detection** | Automatic via `findNearbyAnchor*()` | Manual connection required |
| **Connector Alignment** | Selected connector aligns to anchor | Default positioning |
| **Path Calculations** | Track tile provides geometry | No path length data |
| **Dimensions** | Could use tile-specific scaling | Global editor dimensions |

## Source References

- **Main Controller**: `java/src/jmri/jmrit/display/layoutEditor/LayoutEditor.java`
- **Track Views**: `java/src/jmri/jmrit/display/layoutEditor/*View.java`
- **Drawing Base**: `java/src/jmri/jmrit/display/layoutEditor/LayoutTrackView.java`
- **Edit Dialogs**: `java/src/jmri/jmrit/display/layoutEditor/LayoutEditorDialogs/`
- **Toolbar**: `java/src/jmri/jmrit/display/layoutEditor/LayoutEditorToolBarPanel.java`
