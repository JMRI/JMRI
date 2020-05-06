This is a fast-evolving list of items for the restructuring of the display.layoutEditor package. 
It's in no particular order, items are removed as done, so please don't consider it documentation.

----

## MVC work
 -  *View  present and running, now start to move code for those methods
        rename methods left behind to ensure not accessed
 - once moved to View, break down to subclasses to removing dynamic typing

Usages from LayoutTrackView:

protected abstract void draw1(Graphics2D g2, boolean isMain, boolean isBlock);

    java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java:3442
    java/src/jmri/jmrit/display/layoutEditor/LayoutTurntable.java:1136
    java/src/jmri/jmrit/display/layoutEditor/TrackSegment.java:2295
    java/src/jmri/jmrit/display/layoutEditor/PositionablePoint.java:1760
    java/src/jmri/jmrit/display/layoutEditor/LevelXing.java:1508
    java/src/jmri/jmrit/display/layoutEditor/LayoutSlip.java:1093

    java/src/jmri/jmrit/display/layoutEditor/LayoutEditorChecks.java:379

abstract protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement)

    java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java:3781
    java/src/jmri/jmrit/display/layoutEditor/LayoutTurntable.java:1187
    java/src/jmri/jmrit/display/layoutEditor/TrackSegment.java:2328
    java/src/jmri/jmrit/display/layoutEditor/PositionablePoint.java:1768
    java/src/jmri/jmrit/display/layoutEditor/LevelXing.java:1527
    java/src/jmri/jmrit/display/layoutEditor/LayoutSlip.java:1267
    (others inherited)    
    
abstract protected void drawEditControls(Graphics2D g2);

    java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java:4511
    java/src/jmri/jmrit/display/layoutEditor/LayoutTurntable.java:1266
    java/src/jmri/jmrit/display/layoutEditor/TrackSegment.java:2385
    java/src/jmri/jmrit/display/layoutEditor/PositionablePoint.java:1789
    java/src/jmri/jmrit/display/layoutEditor/LevelXing.java:1632

   
abstract protected void drawDecorations(Graphics2D g2);
    java/src/jmri/jmrit/display/layoutEditor/LayoutTurnout.java:3436
    java/src/jmri/jmrit/display/layoutEditor/LayoutTurntable.java:1130
    java/src/jmri/jmrit/display/layoutEditor/TrackSegment.java:2431
    java/src/jmri/jmrit/display/layoutEditor/PositionablePoint.java:1754
    java/src/jmri/jmrit/display/layoutEditor/LevelXing.java:1500

    java/src/jmri/jmrit/display/layoutEditor/LayoutEditorChecks.java:379


===== 

Where do the PositionablePoint editors for End Bumper, etc live?

=========================================================

LayoutEditorComponent support of LayoutShapes, Memories, Blocks, etc as future problem
        layoutEditor.getLayoutShapes()
 
LayoutEditorComponent.drawTrackSegmentInProgress still uses LayoutTrack not LayoutTrackView
    but what does it actually do? calls to LayoutTrack.highlightUnconnected(..) in two forms

============================================================

- [ ] getId vs getName why? getName (257) much more common than getId (35), but is it right?

% grep -r 'String getName\(\)' java/src/jmri/jmrit/display/layoutEditor/
java/src/jmri/jmrit/display/layoutEditor//LayoutTrackDrawingOptions.java:    public String getName() {
java/src/jmri/jmrit/display/layoutEditor//LayoutShape.java:    public String getName() {
java/src/jmri/jmrit/display/layoutEditor//LayoutEditor.java:        public String getName() {
java/src/jmri/jmrit/display/layoutEditor//LayoutTrack.java:    final public String getName() {

% grep -r 'String getId\(\)' java/src/jmri/jmrit/display/layoutEditor/
java/src/jmri/jmrit/display/layoutEditor//LayoutBlock.java:    public String getId() {
java/src/jmri/jmrit/display/layoutEditor//LayoutTrack.java:    final public String getId() {

=========================================================

## Code Pushes

 - Operational code in the LayoutTrack tree needs to be pushed up and down.

isDisconnected in LayoutTrack (base) and PositionablePoint (subclass) seem very different;
    do they actually do the same thing?  Want to make one final implementation if possible.
    

- setConnection is similar in 
    LayoutTurnout, LayoutSlip, LevelXing
    
- Why is this considered common code by CI?
    import static java.lang.Float.POSITIVE_INFINITY; 
    
    

## Further items

 - Role of LayoutShape  (handled in LayoutEditorComponent similar to i.e. LayoutTracks, needs a view? but they're _shapes_)
 
## Minor Cleanups 
 - Sort out comments at the top of LayoutTrack & subclasses
 - Run a cleanup on imports via NetBeans; you've left quite a few behind...

==================

LayoutTrackDrawingOptions holds things like ballast color, etc.
Persisted by configurexml/LayoutTrackDrawingOptionsXml.java
Accessed and maintained by LayoutEditor.java
Accessed by LayoutEditor, maybe set?
Edited by LayoutEditorDialogs/LayoutTrackDrawingOptionsDialog
    Options -> Track Options -> Set Track Options that opens a window
There's also a Options -> Turnout Options that says in a sub menu
==================
 
This needs to get hooked up properly:
    [javac] /Users/jake/Documents/Trains/JMRI/projects/JMRI/java/src/jmri/jmrit/display/layoutEditor/LayoutEditorChecks.java:378: error: cannot find symbol
    [javac]             layoutEditor.getLayoutTrackEditors().editLayoutTrack(layoutTrack);
    [javac]                         ^
    [javac]   symbol:   method getLayoutTrackEditors()
    [javac]   location: variable layoutEditor of type LayoutEditor


===================

Add a control property for writing out the image files in 
./runtest.csh java/test/jmri/jmrit/display/layoutEditor/LoadAndStoreTest

Drop status output to System.err

Consider moving the write up once it's controlled.
===================

 TrackSegment HIDECON as an EnumSet
 https://docs.oracle.com/javase/7/docs/api/java/util/EnumSet.html
 
     public enum Style {
        BOLD, ITALIC, UNDERLINE, STRIKETHROUGH
    }

    public static void main(String[] args) {
        final EnumSet<Style> styles = EnumSet.noneOf(Style.class);
        styles.addAll(EnumSet.range(Style.BOLD, Style.STRIKETHROUGH)); // enable all constants
        styles.removeAll(EnumSet.of(Style.UNDERLINE, Style.STRIKETHROUGH)); // disable a couple
        assert EnumSet.of(Style.BOLD, Style.ITALIC).equals(styles); // check set contents are correct
        System.out.println(styles);
    }
--
public enum Flag {
    UPPERCASE, REVERSE, FULL_STOP, EMPHASISE;

    public static final EnumSet<Flag> ALL_OPTS = EnumSet.allOf(Flag.class);
}


    if (flags.contains(Flag.UPPERCASE)) value = value.toUpperCase();

 EnumSet.of(Flag.UPPERCASE))
 EnumSet.of(Flag.FULL_STOP, Flag.EMPHASISE)
 
 ============
 % grep "nothing to see" *.java  (Multiple copies removed below)
LayoutEditorChecks.java:                //nothing to see here... move along...
LayoutTurntable.java:            // nothing to see here, move along...
LevelXing.java:        // nothing to see here... move along...
PositionablePoint.java:        //nothing to see here... move along...
TrackSegment.java:        //nothing to see here, move along
TrackSegment.java:        //nothing to see here, move along

=================

Fix `//([a-zA-Z])` comments with `// \1`
   
=================
   
   Consider moving list management entirely out of Layout Manager to decrease size & complexity.
   
=============
   although it's deferring to the View classes mostly, LayoutComponent is
   still messing with i.e. isDisabled, isHidden instead of defettnig that to the objects
   
 ============
 
 Might still be enum:
 
 
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    public static final int NEIGHBOURCOL = 0;
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    static final int DIRECTIONCOL = 1;
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    static final int MUTUALCOL = 2;
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    static final int RELATCOL = 3;
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    static final int METRICCOL = 4;
layoutEditor/blockRoutingTable/LayoutBlockNeighbourTableModel.java:    static final int NUMCOL = 4 + 1;

layoutEditor/blockRoutingTable/LayoutBlockThroughPathsTableModel.java:    public static final int SOURCECOL = 0;
layoutEditor/blockRoutingTable/LayoutBlockThroughPathsTableModel.java:    static final int DESTINATIONCOL = 1;
layoutEditor/blockRoutingTable/LayoutBlockThroughPathsTableModel.java:    static final int ACTIVECOL = 2;
layoutEditor/blockRoutingTable/LayoutBlockThroughPathsTableModel.java:    static final int NUMCOL = 2 + 1;

layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    public static final int DESTCOL = 0;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int NEXTHOPCOL = 1;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int HOPCOUNTCOL = 2;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int DIRECTIONCOL = 3;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int METRICCOL = 4;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int LENGTHCOL = 5;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int STATECOL = 6;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int VALIDCOL = 7;
layoutEditor/blockRoutingTable/LayoutBlockRouteTableModel.java:    static final int NUMCOL = 7 + 1;

Note bits
layoutEditor/LayoutBlock.java:    public static final int RESERVED = 0x08;
layoutEditor/LayoutBlock.java:    final static int ADDITION = 0x00;
layoutEditor/LayoutBlock.java:    final static int UPDATE = 0x02;
layoutEditor/LayoutBlock.java:    final static int REMOVAL = 0x04;

Note bits, with an odd choice of NONE
layoutEditor/LayoutBlock.java:    final static int RXTX = 0x00;
layoutEditor/LayoutBlock.java:    final static int RXONLY = 0x02;
layoutEditor/LayoutBlock.java:    final static int TXONLY = 0x04;
layoutEditor/LayoutBlock.java:    final static int NONE = 0x08;

layoutEditor/ConnectivityUtil.java:    public static final int OVERALL = 0x00;
layoutEditor/ConnectivityUtil.java:    public static final int CONTINUING = 0x01;
layoutEditor/ConnectivityUtil.java:    public static final int DIVERGING = 0x02;

Note the following values repeat. Two Enums? Try and see if compiles.
layoutEditor/LayoutConnectivity.java:    final public static int NONE = 0;
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_AB = 1;  // continuing
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_CD = 2;  // continuing
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_AC = 3;  // xed over
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_BD = 4;  // xed over
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_AD = 1;  // continuing (slips)
layoutEditor/LayoutConnectivity.java:    final public static int XOVER_BOUNDARY_BC = 2;  // continuing (slips)

The following are bits, but there's no indication they are ever or'd:
layoutEditor/LayoutTurnout.java:    public static final int STATE_AC = 0x02;
layoutEditor/LayoutTurnout.java:    public static final int STATE_BD = 0x04;
layoutEditor/LayoutTurnout.java:    public static final int STATE_AD = 0x06;
layoutEditor/LayoutTurnout.java:    public static final int STATE_BC = 0x08;

These bits are or'd, see tests
layoutEditor/TrackSegment.java:    public static final int SHOWCON = 0x01;
layoutEditor/TrackSegment.java:    public static final int HIDECON = 0x02;     // flag set on a segment basis.
layoutEditor/TrackSegment.java:    public static final int HIDECONALL = 0x04;  // Used by layout editor for hiding all

 =============
 
 The LayoutTrack classes ($LETRK) use these from LayoutEditor
 layoutEditor.setDirty();
 layoutEditor.redrawPanel();   {just calls repaint?} see .paintTargetPanel abstract in ../Editor; LayoutEditor extends PanelEditor
 layoutEditor.repaint()
 layoutEditor.getLETools(). {lots of stuff}
 layoutEditor.getLEAuxTools(). {lots of stuff}
 layoutEditor.getFinder().{lots of stuff}
  layoutEditor.getLayoutEditorToolBarPanel()
  
  layoutEditor.setShowAlignmentMenu(popup)
  layoutEditor.isEditable()
  layoutEditor.getZoom()
  layoutEditor.getTargetPanel()
  
  layoutEditor.circleDiameter
  layoutEditor.circleRadius
  
 layoutEditor.isTurnoutDrawUnselectedLeg
 
 layoutEditor.getXOverLong()
 layoutEditor.getXOverHWid()
 layoutEditor.getXOverShort()
 layoutEditor.setXOverLong
 layoutEditor.setXOverHWid
 layoutEditor.setXOverShort
 
 The above are presisted to XML.  LayoutTurnout#setUpDefaultSize sets them from a specific
 turnout, depending on type, and is only invoked from "Use Size as Default" selection in 
 interface.  Also, are the if statements in setUpDefaultSize structured right?
 
 layoutEditor.isTurnoutFillControlCircles
 LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
 
 layoutEditor.layoutEditorControlRectAt(getCoordsA())
 layoutEditor. setSelectionRect
 
 layoutEditor.getXScale()
 layoutEditor.getYScale()
 
 layoutEditor.setTurnoutBX
 layoutEditor.setTurnoutCX
 layoutEditor.setTurnoutWid
 
 layoutEditor.getFinder()
 
layoutEditor.removeLayoutSlip
layoutEditor.removeLayoutTurnout
if (canRemove() && layoutEditor.removeLevelXing(LevelXing.this))

layoutEditor.setLink
layoutEditor.addAnchor

layoutEditor.getLayoutTrackDrawingOptions()

layoutEditor.getLayoutTrackEditors()
layoutEditor.getLayoutTracks()

layoutEditor.selectedObject
layoutEditor.prevSelectedObject

To understand the above, probably have to move the listener definitions out.

% grep Listener $LETRK | awk '{print $1}' | uniq -c
   8 LayoutTurntable.java:
   1 LevelXing.java:
   1 PositionablePoint.java:import
  21 PositionablePoint.java:
  49 TrackSegment.java:
  13 LayoutTurnout.java:
  11 LayoutSlip.java:

Mostly addActionListener via ()->, some named listeners, some addPropertyChangeListener

=============

getBlockName not in LayoutTrack, perhaps because there are two forms of internal variable:
LayoutTrack:
    protected NamedBeanHandle<LayoutBlock> namedLayoutBlockA = null;
 (There's a getBlockBName, getBlockCName, D but just getBlock for A in LayoutTurnout)
 
 But TrackSegment has it's own getBlockName referencing it's own
     private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

Maybe other subclasses?
And the getBlockName code could be simpler, see the getBlock one-line version.

=============
 
Example from LayoutEditor

        if ((lt.getConnectD() == null) && (lt.isTurnoutTypeXover() || lt.isTurnoutTypeSlip())) {
            if (lt instanceof LayoutSlip) {
                beginHitPointType = HitPointType.SLIP_D;
            } else {
                beginHitPointType = HitPointType.TURNOUT_D;
            }
            dLoc = lt.getCoordsD();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

Should that be a "getBeginHitPoint"?

(Scan for instanceOf)

---------

Example from LayoutEditor  (What's that stuff at the top selecting?)

                case TURNOUT_A:
                case TURNOUT_B:
                case TURNOUT_C:
                case TURNOUT_D:
                case SLIP_A:
                case SLIP_B:
                case SLIP_C:
                case SLIP_D: {
                    LayoutTurnout ft = (LayoutTurnout) foundTrack;
                    addTrackSegment();

                    if ((ft.getTurnoutType() == LayoutTurnout.TurnoutType.RH_TURNOUT) || (ft.getTurnoutType() == LayoutTurnout.TurnoutType.LH_TURNOUT)) {
                        rotateTurnout(ft);
                    }

                    // Assign a block to the new zero length track segment.
                    ft.setTrackSegmentBlock(foundHitPointType, true);
                    break;
                }

What are we _not_ rotating the other types?  Is this a conditional thing somehow?  
(check rotateTurnout method a few lines down which does a similar check and returns, but might be called from elsewhere)

---------

Figure this one out!  At least part of the comments is wrong...
TrackSegment 436
    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    //only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        //nothing to see here, move along
        throw new jmri.JmriException("Use getConnect1() or getConnect2() instead.");
    }

(Searching for "nothing to see here" is interesting)

=====
LayoutTrackDrawingOptions is mutable and doesn't have a constant hash


