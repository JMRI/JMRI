The goal is to separate connectivity (which is specific to the physical layout) from the geometry of its presentation in a particular display, i.e. a panel.

For example, the x,y coordinates of a point in the (present) LayoutEditor objects defines a position in the AWT coordinates of the display.

The separation allows various advantages:

 - It's part of having a single definition of the physical layout, which can be looked at by one or more panels.  Having panel-specific coordinate information live in panel-specific objects instead of physical-layout specific ones (i.e. in View objects instead of Model objects) allows this.
 
 - It provides consistency in the layout-specific information, even if the display changes.
 
That 2nd point requires some amplification.  The connections between layout objects are encoded in [LayoutConnectivity objects](https://www.jmri.org/JavaDoc/doc/jmri/jmrit/display/layoutEditor/LayoutConnectivity.html).  These contain references to their two connected blocks as getBlock1() and getBlock2(), along with a direction from 1 to 2 encoded as one of Path.NORTH, Path.NORTH_EAST, Path.EAST through Path.NORTH_WEST.  
 
These connectivity objects are _not_ persisted; they are recreated after the LayoutTrack objects have been loaded.  The LayoutTrack (and subclass) objects contain name references to their connected objects.  These are be matched back and forth to create the connection information in LayoutConnectivity objects.
 
But where does the direction come from?  It comes from finding a center point (a Point2D) defined by the LayoutTrack objects at each end, and using those to find a compass direction.
 
And there's the rub; that's a screen-specific direction.  Should you change the layout on the screen, it can and will change.  If you have two layouts, you can get different versions.
 
The basic call used is Path.computeDirection, which is used in PositionablePoint, LayoutSlip, LayoutTurnout, and TrackSegment.

There are a few places where this direction is explicitly used for computations.  For example, in LayoutEditorTools#setSignalsAtBlockBoundaryFromMenu it's compared to EAST and SOUTH to set `showEast` and `showWest` boolean variables which act inconsistently, and don't take into account the semi-cardinals NORTHWEST, etc.

At the same time, 'east' and 'west' are built in to the layout module via methods like getEastBoundSignal(),  getEastBoundSignalHead(), getEastBoundSignalMastName(), getEastBoundSensor() and similar along with matching West methods.  There are no similar methods for North, South, SouthWest, SouthBySouthWest, etc.

(As an aside, Path has a additional directions that LayoutEditor doesn't use: CCW, CW, LEFT, RIGHT, UP, DOWN)

The immediate design issue is that connectivity model objects will occasionally need to create new direction information, which refers to (screen) geometric information that they don't have. It's not clear how to handle that:

 - Some form of connectivity assignment, i.e. "If you have an incoming EAST, outgoing is WEST"
 - What's wrong with calling them all NONE until some reason comes along?
 - Provide a call-back from some outside object that can provide directional information; if there's just on panel, it can use that geometry
  
The (perhaps temporary) choice is to defer this to central methods in the LayoutModels (i.e. LayoutEditor) class.
