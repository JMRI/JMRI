// jmri.jmrit.display.LayoutEditor.java
package jmri.jmrit.display.layoutEditor;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.Memory;
import jmri.Reporter;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

import jmri.jmrit.display.*;

import java.awt.*;

import java.awt.geom.*;
import java.awt.event.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Math;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import java.util.ResourceBundle;
import jmri.util.SystemType;


/**
 * Provides a scrollable Layout Panel and editor toolbars (that can be
 *		hidden)
 * <P>
 * This module serves as a manager for the LayoutTurnout, Layout Block, 
 *		PositionablePoint, Track Segment, and LevelXing objects which are
 *      integral subparts of the LayoutEditor class.
 * <P> 
 * All created objects are put on specific levels depending on their
 *		type (higher levels are in front):
 * Note that higher numbers appear behind lower numbers.
 * <P>
 * The "contents" List keeps track of all text and icon label objects 
 *		added to the targetframe for later manipulation.  Other Lists
 *      keep track of drawn items.
 * <P>
 * Based in part on PanelEditor.java (Bob Jacobsen (c) 2002, 2003). In
 *		particular, text and icon label items are copied from Panel
 *		editor, as well as some of the control design.
 *
 * @author Dave Duchamp  Copyright: (c) 2004-2007
 * @version $Revision$
 */

public class LayoutEditor extends jmri.jmrit.display.panelEditor.PanelEditor {

	// Defined text resource
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

   // size of point boxes
	private static final double SIZE = 3.0;
	private static final double SIZE2 = SIZE*2.;  // must be twice SIZE
	
	// connection types
	final public static  int NONE = 0;
	final public static  int POS_POINT = 1;
	final public static  int TURNOUT_A = 2;  // throat for RH, LH, and WYE turnouts
	final public static  int TURNOUT_B = 3;  // continuing route for RH or LH turnouts
	final public static  int TURNOUT_C = 4;  // diverging route for RH or LH turnouts
	final public static  int TURNOUT_D = 5;  // double-crossover or single crossover only
	final public static  int LEVEL_XING_A = 6;  
	final public static  int LEVEL_XING_B = 7;
	final public static  int LEVEL_XING_C = 8;
	final public static  int LEVEL_XING_D = 9;
	final public static  int TRACK = 10;
	final public static  int TURNOUT_CENTER = 11; // non-connection points should be last
	final public static  int LEVEL_XING_CENTER = 12;
	final public static  int TURNTABLE_CENTER = 13;
	final public static  int LAYOUT_POS_LABEL = 14;
	final public static  int LAYOUT_POS_JCOMP = 15;
	final public static  int MULTI_SENSOR = 16;
	final public static  int MARKER = 17;
    final public static  int TRACK_CIRCLE_CENTRE = 18;
	final public static  int SLIP_CENTER = 20; // 
	final public static  int SLIP_A = 21; // offset for slip connection points
	final public static  int SLIP_B = 22; // offset for slip connection points
	final public static  int SLIP_C = 23; // offset for slip connection points
	final public static  int SLIP_D = 24; // offset for slip connection points
	final public static  int TURNTABLE_RAY_OFFSET = 50; // offset for turntable connection points

    
	// dashed line parameters
	//private static int minNumDashes = 3;
	//private static double maxDashLength = 10;
	
    // Operational instance variables - not saved to disk
    //private jmri.TurnoutManager tm = null;
	private LayoutEditor thisPanel = null;
	private JPanel topEditBar = null;
	private JPanel helpBar = null;
	protected boolean skipIncludedTurnout = false;
    public ArrayList<PositionableLabel> backgroundImage = new ArrayList<PositionableLabel>();  // background images
    public ArrayList<SensorIcon> sensorImage = new ArrayList<SensorIcon>();  // sensor images
    public ArrayList<SignalHeadIcon> signalHeadImage = new ArrayList<SignalHeadIcon>();  // signal head images
    public ArrayList<SignalMastIcon> signalMastImage = new ArrayList<SignalMastIcon>();  // signal mast images
	public ArrayList<LocoIcon> markerImage = new ArrayList<LocoIcon>(); // marker images
	public ArrayList<PositionableLabel> labelImage = new ArrayList<PositionableLabel>(); // layout positionable label images
	public ArrayList<AnalogClock2Display> clocks = new ArrayList<AnalogClock2Display>();  // fast clocks
	public ArrayList<MultiSensorIcon> multiSensors = new ArrayList<MultiSensorIcon>(); // multi-sensor images
	
	public LayoutEditorAuxTools auxTools = null;
	private ConnectivityUtil conTools = null;
        
    private ButtonGroup itemGroup = null;
    private JTextField blockIDField = new JTextField(8);
	private JTextField blockSensor = new JTextField(5);
    
    private JCheckBox turnoutRHBox = new JCheckBox(rb.getString("RightHandAbbreviation"));
    private JCheckBox turnoutLHBox = new JCheckBox(rb.getString("LeftHandAbbreviation"));
    private JCheckBox turnoutWYEBox = new JCheckBox(rb.getString("WYEAbbreviation"));
    private JCheckBox doubleXoverBox = new JCheckBox(rb.getString("DoubleCrossOver"));
    private JCheckBox rhXoverBox = new JCheckBox(rb.getString("RHCrossOver"));
    private JCheckBox lhXoverBox = new JCheckBox(rb.getString("LHCrossOver"));
	private JPanel rotationPanel = new JPanel();
    private JTextField rotationField = new JTextField(3);
    private JTextField nextTurnout = new JTextField(5);
    private JPanel extraTurnoutPanel = new JPanel();
    private JTextField extraTurnout = new JTextField(5);
    
    private JCheckBox levelXingBox = new JCheckBox(rb.getString("LevelCrossing"));
    private JCheckBox layoutSingleSlipBox = new JCheckBox(rb.getString("LayoutSingleSlip"));
    private JCheckBox layoutDoubleSlipBox = new JCheckBox(rb.getString("LayoutDoubleSlip"));
    private JCheckBox endBumperBox = new JCheckBox(rb.getString("EndBumper"));
    private JCheckBox anchorBox = new JCheckBox(rb.getString("Anchor"));
    private JCheckBox trackBox = new JCheckBox(rb.getString("TrackSegment"));

	private JCheckBox dashedLine = new JCheckBox(rb.getString("Dashed"));
	private JCheckBox mainlineTrack = new JCheckBox(rb.getString("MainlineBox"));

    private JCheckBox sensorBox = new JCheckBox(rb.getString("SensorIcon"));
    private JTextField nextSensor = new JTextField(5);
    public MultiIconEditor sensorIconEditor = null;
    public JFrame sensorFrame;
    
    private JCheckBox signalBox = new JCheckBox(rb.getString("SignalIcon"));
    private JTextField nextSignalHead = new JTextField(5);
    public MultiIconEditor signalIconEditor = null;
    public JFrame signalFrame;
    
    private JCheckBox signalMastBox = new JCheckBox(rb.getString("SignalMastIcon"));
    private JTextField nextSignalMast = new JTextField(5);
    
    private JCheckBox textLabelBox = new JCheckBox(rb.getString("TextLabel"));
    private JTextField textLabel = new JTextField(8);
	
	private JCheckBox memoryBox = new JCheckBox(rb.getString("Memory"));
    private JTextField textMemory = new JTextField(8);
    
    private JCheckBox iconLabelBox = new JCheckBox(rb.getString("IconLabel"));
    private MultiIconEditor iconEditor = null;
    private JFrame iconFrame = null;

    private JCheckBox multiSensorBox = new JCheckBox(rb.getString("MultiSensor")+"...");
    private MultiSensorIconFrame multiSensorFrame = null;
    
    private JLabel xLabel = new JLabel("00");
    private JLabel yLabel = new JLabel("00");

	private boolean delayedPopupTrigger = false;
	private transient Point2D currentPoint = new Point2D.Double(100.0,100.0);
	private transient Point2D dLoc = new Point2D.Double(0.0,0.0);
	//private int savedMSX = 0;
	//private int savedMSY = 0;
    private int height = 100;
    private int width = 100;
    //private int numTurnouts = 0;
	private TrackSegment newTrack = null;
	private boolean panelChanged = false;

	// selection variables
	private boolean selectionActive = false;
	private double selectionX = 0.0;
	private double selectionY = 0.0;
	private double selectionWidth = 0.0;
	private double selectionHeight = 0.0;
   
	// Option menu items 
    private JCheckBoxMenuItem editModeItem = null;
    private JCheckBoxMenuItem positionableItem = null;
    private JCheckBoxMenuItem controlItem = null;
    private JCheckBoxMenuItem animationItem = null;
    private JCheckBoxMenuItem showHelpItem = null;
    private JCheckBoxMenuItem showGridItem = null;
    private JCheckBoxMenuItem autoAssignBlocksItem = null;
    private JMenu scrollMenu = null;
    private JRadioButtonMenuItem scrollBoth = null;
    private JRadioButtonMenuItem scrollNone = null;
    private JRadioButtonMenuItem scrollHorizontal = null;
    private JRadioButtonMenuItem scrollVertical = null;
    private JMenu tooltipMenu = null;
    private JRadioButtonMenuItem tooltipAlways = null;
    private JRadioButtonMenuItem tooltipNone = null;
    private JRadioButtonMenuItem tooltipInEdit = null;
    private JRadioButtonMenuItem tooltipNotInEdit = null;
	private JCheckBoxMenuItem snapToGridOnAddItem = null;
	private JCheckBoxMenuItem snapToGridOnMoveItem = null;
	private JCheckBoxMenuItem antialiasingOnItem = null;
	private JCheckBoxMenuItem turnoutCirclesOnItem = null;
	private JCheckBoxMenuItem skipTurnoutItem = null;
	private JCheckBoxMenuItem turnoutDrawUnselectedLegItem = null;
	private ButtonGroup trackColorButtonGroup = null;
	private ButtonGroup trackOccupiedColorButtonGroup = null;
	private ButtonGroup trackAlternativeColorButtonGroup = null;
    private ButtonGroup textColorButtonGroup = null;
    private ButtonGroup backgroundColorButtonGroup = null;
	private ButtonGroup turnoutCircleColorButtonGroup = null;
	private ButtonGroup turnoutCircleSizeButtonGroup = null;
	private Color[] trackColors = new Color[13];
	private Color[] trackOccupiedColors = new Color[13];
	private Color[] trackAlternativeColors = new Color[13];
    private Color[] textColors = new Color[13];
    private Color[] backgroundColors = new Color[13];
	private Color[] turnoutCircleColors = new Color[14];
	private int[] turnoutCircleSizes = new int[8];
	private JRadioButtonMenuItem[] trackColorMenuItems = new JRadioButtonMenuItem[13];
	private JRadioButtonMenuItem[] trackOccupiedColorMenuItems = new JRadioButtonMenuItem[13];
	private JRadioButtonMenuItem[] trackAlternativeColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] backgroundColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] textColorMenuItems = new JRadioButtonMenuItem[13];
	private JRadioButtonMenuItem[] turnoutCircleColorMenuItems = new JRadioButtonMenuItem[14];
	private JRadioButtonMenuItem[] turnoutCircleSizeMenuItems = new JRadioButtonMenuItem[8];
	private int trackColorCount = 0;
	private int trackOccupiedColorCount = 0;
	private int trackAlternativeColorCount = 0;
    private int textColorCount = 0;
	private int turnoutCircleColorCount = 0;
	private int turnoutCircleSizeCount = 0;
    private boolean turnoutDrawUnselectedLeg = true;
    private int backgroundColorCount = 0;
    private boolean autoAssignBlocks = false;
	
	// Selected point information
    //private final static int TURNOUT = 1;      // possible object types
    //private final static int LEVEL_XING = 2;
    //private final static int POINT = 3;
	private transient Point2D startDel = new Point2D.Double(0.0,0.0); // starting delta coordinates
	private Object selectedObject = null; // selected object, null if nothing selected
	private Object prevSelectedObject = null; // previous selected object, for undo
	private int selectedPointType = 0;   // connection type within the selected object
	//private boolean selectedNeedsConnect = false; // true if selected object is unconnected
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED") // no Serializable support at present
	private Object foundObject = null; // found object, null if nothing found
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED") // no Serializable support at present
	private transient Point2D foundLocation = new Point2D.Double(0.0,0.0);  // location of found object

	private int foundPointType = 0;   // connection type within the found object

	@SuppressWarnings("unused")
	private boolean foundNeedsConnect = false; // true if found point needs a connection
	private Object beginObject = null; // begin track segment connection object, null if none
	private transient Point2D beginLocation = new Point2D.Double(0.0,0.0);  // location of begin object
	private int beginPointType = 0;   // connection type within begin connection object
	private transient Point2D currentLocation = new Point2D.Double(0.0,0.0); // current location
	
	// program default turnout size parameters
	private double turnoutBXDefault = 20.0;  // RH, LH, WYE
	private double turnoutCXDefault = 20.0;
	private double turnoutWidDefault = 10.0;
	private double xOverLongDefault = 30.0;   // DOUBLE_XOVER, RH_XOVER, LH_XOVER
	private double xOverHWidDefault = 10.0;
	private double xOverShortDefault = 10.0;
	
	// Lists of items that describe the Layout, and allow it to be drawn
	//		Each of the items must be saved to disk over sessions
	public ArrayList<LayoutTurnout> turnoutList = new ArrayList<LayoutTurnout>();  // LayoutTurnouts
	public ArrayList<TrackSegment> trackList = new ArrayList<TrackSegment>();  // TrackSegment list
	public ArrayList<PositionablePoint> pointList = new ArrayList<PositionablePoint>();  // PositionablePoint list
	public ArrayList<LevelXing> xingList = new ArrayList<LevelXing>();  // LevelXing list
	public ArrayList<LayoutSlip> slipList = new ArrayList<LayoutSlip>();  // Layout slip list
	public ArrayList<LayoutTurntable> turntableList = new ArrayList<LayoutTurntable>(); // Turntable list
	// counts used to determine unique internal names
	private int numAnchors = 0;
	private int numEndBumpers = 0;
	private int numTrackSegments = 0;
	private int numLevelXings = 0;
	private int numLayoutSlips = 0;
	private int numLayoutTurnouts = 0;
	private int numLayoutTurntables = 0;
	// Lists of items that facilitate tools and drawings
	public ArrayList<SignalHeadIcon> signalList = new ArrayList<SignalHeadIcon>();  // Signal Head Icons
	public ArrayList<MemoryIcon> memoryLabelList = new ArrayList<MemoryIcon>(); // Memory Label List
    public ArrayList<SensorIcon> sensorList = new ArrayList<SensorIcon>();  // Sensor Icons
    public ArrayList<SignalMastIcon> signalMastList = new ArrayList<SignalMastIcon>();  // Signal Head Icons
    
    // persistent instance variables - saved to disk with Save Panel
	private int windowWidth = 0;
	private int windowHeight = 0;
	private int panelWidth = 0;
	private int panelHeight = 0;
	private int upperLeftX = 0;
	private int upperLeftY = 0;
    private float mainlineTrackWidth = 4.0F;
    private float sideTrackWidth = 2.0F;
	private Color defaultTrackColor = Color.black;
	private Color defaultOccupiedTrackColor = Color.black;
	private Color defaultAlternativeTrackColor = Color.black;
    private Color defaultBackgroundColor = Color.lightGray;
    private Color defaultTextColor = Color.black;
	private Color turnoutCircleColor = defaultTrackColor; //matches earlier versions
	private int   turnoutCircleSize = 2;  //matches earlier versions
    private String layoutName = "";
	private double xScale = 1.0;
	private double yScale = 1.0;
	private boolean animatingLayout = true;
	private boolean showHelpBar = true;
	private boolean drawGrid = false;
	private boolean snapToGridOnAdd = false;
	private boolean snapToGridOnMove = false;
	private boolean antialiasingOn = false;
	private boolean turnoutCirclesWithoutEditMode = false;
	private boolean tooltipsWithoutEditMode = false;
	private boolean tooltipsInEditMode = true;
	// turnout size parameters - saved with panel
	private double turnoutBX = turnoutBXDefault;  // RH, LH, WYE
	private double turnoutCX = turnoutCXDefault;
	private double turnoutWid = turnoutWidDefault;
	private double xOverLong = xOverLongDefault;   // DOUBLE_XOVER, RH_XOVER, LH_XOVER
	private double xOverHWid = xOverHWidDefault;
	private double xOverShort = xOverShortDefault;
	
	// saved state of options when panel was loaded or created
	private boolean savedEditMode = true;
    private boolean savedPositionable = true;
    private boolean savedControlLayout = true;
    private boolean savedAnimatingLayout = true;
	private boolean savedShowHelpBar = false;
	// Antialiasing rendering
	private static final RenderingHints antialiasing = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
    
    public LayoutEditor() { this("My Layout");}
    
    public LayoutEditor(String name) {
        super(name);
        layoutName = name;
        // initialize frame
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        // initialize menu bar
        JMenuBar menuBar = new JMenuBar();
		// set up File menu
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rbx.getString("MenuItemStore")));
        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rbx.getString("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (deletePanel()) {
                        dispose(true);
                    }
                }
            });
        setJMenuBar(menuBar);
        // setup Options menu
		setupOptionMenu(menuBar);
		// setup Tools menu
		setupToolsMenu(menuBar);
		// setup Zoom menu
		setupZoomMenu(menuBar);
		// setup Zoom menu
		setupMarkerMenu(menuBar);
		// setup Help menu
        addHelpMenu("package.jmri.jmrit.display.LayoutEditor", true);
		
        // setup group for radio buttons selecting items to add and line style
        itemGroup = new ButtonGroup();
        itemGroup.add(turnoutRHBox);
        itemGroup.add(turnoutLHBox);
        itemGroup.add(turnoutWYEBox);
        itemGroup.add(doubleXoverBox);
        itemGroup.add(rhXoverBox);
        itemGroup.add(lhXoverBox);
        itemGroup.add(levelXingBox);
        itemGroup.add(layoutSingleSlipBox);
        itemGroup.add(layoutDoubleSlipBox);
        itemGroup.add(endBumperBox);
        itemGroup.add(anchorBox);
        itemGroup.add(trackBox);
		itemGroup.add(multiSensorBox);
        itemGroup.add(sensorBox);
        itemGroup.add(signalBox);
        itemGroup.add(signalMastBox);
        itemGroup.add(textLabelBox);
        itemGroup.add(memoryBox);
        itemGroup.add(iconLabelBox);
        
        ActionListener selectionListAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                Component[] extra = extraTurnoutPanel.getComponents();
                if (layoutSingleSlipBox.isSelected() || layoutDoubleSlipBox.isSelected()){
                    for (Component item: extra) {
                         item.setEnabled(true);
                    }
                }
                else {
                    for (Component item: extra) {
                         item.setEnabled(false);
                    }
                }
            }
        };
            
        turnoutRHBox.addActionListener(selectionListAction);
        turnoutLHBox.addActionListener(selectionListAction);
        turnoutWYEBox.addActionListener(selectionListAction);
        doubleXoverBox.addActionListener(selectionListAction);
        rhXoverBox.addActionListener(selectionListAction);
        lhXoverBox.addActionListener(selectionListAction);
        levelXingBox.addActionListener(selectionListAction);
        layoutSingleSlipBox.addActionListener(selectionListAction);
        layoutDoubleSlipBox.addActionListener(selectionListAction);
        endBumperBox.addActionListener(selectionListAction);
        anchorBox.addActionListener(selectionListAction);
        trackBox.addActionListener(selectionListAction);
		multiSensorBox.addActionListener(selectionListAction);
        sensorBox.addActionListener(selectionListAction);
        signalBox.addActionListener(selectionListAction);
        signalMastBox.addActionListener(selectionListAction);
        textLabelBox.addActionListener(selectionListAction);
        memoryBox.addActionListener(selectionListAction);
        iconLabelBox.addActionListener(selectionListAction);
        
		turnoutRHBox.setSelected(true);
		dashedLine.setSelected(false);
		mainlineTrack.setSelected(false);
        // setup top edit bar
        topEditBar = new JPanel();
        topEditBar.setLayout(new BoxLayout(topEditBar, BoxLayout.Y_AXIS));
		// add first row of edit tool bar items
        JPanel top1 = new JPanel();
		Dimension coordSize = xLabel.getPreferredSize();
        coordSize.width *= 2;
        xLabel.setPreferredSize(coordSize);
        yLabel.setPreferredSize(coordSize);
        top1.add(new JLabel(rb.getString("Location")+" - x:"));
        top1.add(xLabel);
        top1.add(new JLabel(" y:"));
        top1.add(yLabel);
		// add turnout items
        top1.add (new JLabel("    "+rb.getString("Turnout")+": "));
        top1.add (new JLabel(rb.getString("Name")));
        top1.add (nextTurnout);
		nextTurnout.setToolTipText(rb.getString("TurnoutNameToolTip"));
        JLabel extraTurnLabel = new JLabel(rb.getString("SecondName"));
        extraTurnLabel.setEnabled(false);
        extraTurnout.setEnabled(false);
        extraTurnoutPanel.add (extraTurnLabel);
        extraTurnoutPanel.add (extraTurnout);
        extraTurnout.setToolTipText(rb.getString("TurnoutNameToolTip"));
        top1.add(extraTurnoutPanel);
		top1.add (new JLabel(rb.getString("Type")));
        top1.add (turnoutRHBox);
		turnoutRHBox.setToolTipText(rb.getString("RHToolTip"));
        top1.add (turnoutLHBox);
		turnoutLHBox.setToolTipText(rb.getString("LHToolTip"));
        top1.add (turnoutWYEBox);
		turnoutWYEBox.setToolTipText(rb.getString("WYEToolTip"));
        top1.add (doubleXoverBox);
		doubleXoverBox.setToolTipText(rb.getString("DoubleCrossOverToolTip"));
        top1.add (rhXoverBox);
		rhXoverBox.setToolTipText(rb.getString("RHCrossOverToolTip"));
        top1.add (lhXoverBox);
		lhXoverBox.setToolTipText(rb.getString("LHCrossOverToolTip"));
        top1.add (layoutSingleSlipBox);
		layoutSingleSlipBox.setToolTipText(rb.getString("SingleSlipToolTip"));
        top1.add (layoutDoubleSlipBox);
		layoutDoubleSlipBox.setToolTipText(rb.getString("DoubleSlipToolTip"));
		rotationPanel.add (new JLabel("    "+rb.getString("Rotation")));
		rotationPanel.add (rotationField);
        top1.add(rotationPanel);
		rotationField.setToolTipText(rb.getString("RotationToolTip"));
        topEditBar.add(top1);
		// add second row of edit tool bar items
        JPanel top2 = new JPanel();
        top2.add(new JLabel(rb.getString("BlockID")));
        top2.add(blockIDField);
		blockIDField.setToolTipText(rb.getString("BlockIDToolTip"));
        top2.add(new JLabel(rb.getString("OccupancySensor")));
        top2.add(blockSensor);
		blockSensor.setText("");
		blockSensor.setToolTipText(rb.getString("OccupancySensorToolTip"));
		top2.add (new JLabel("  "+rb.getString("Track")+":  "));
        top2.add (levelXingBox);
		levelXingBox.setToolTipText(rb.getString("LevelCrossingToolTip"));
        top2.add (trackBox);
		trackBox.setToolTipText(rb.getString("TrackSegmentToolTip"));
		top2.add (dashedLine);
		dashedLine.setToolTipText(rb.getString("DashedCheckBoxTip"));
		top2.add (mainlineTrack);
		mainlineTrack.setToolTipText(rb.getString("MainlineCheckBoxTip"));
        topEditBar.add(top2);
		// add third row of edit tool bar items
        JPanel top3 = new JPanel();
        top3.add(new JLabel("  "+rb.getString("Nodes")+": "));
        top3.add (endBumperBox);
		endBumperBox.setToolTipText(rb.getString("EndBumperToolTip"));
        top3.add (anchorBox);
		anchorBox.setToolTipText(rb.getString("AnchorToolTip"));
        top3.add(new JLabel("   "+rb.getString("Labels")+": "));
		top3.add (textLabelBox);
		textLabelBox.setToolTipText(rb.getString("TextLabelToolTip"));
        top3.add (textLabel);
		textLabel.setToolTipText(rb.getString("TextToolTip"));		
		top3.add (memoryBox);
		memoryBox.setToolTipText(rb.getString("MemoryBoxToolTip"));
        top3.add (textMemory);
		textMemory.setToolTipText(rb.getString("MemoryToolTip"));		
        topEditBar.add(top3);
		// add fourth row of edit tool bar items
        JPanel top4 = new JPanel();
		// multi sensor 
		top4.add (multiSensorBox);
		multiSensorBox.setToolTipText(rb.getString("MultiSensorToolTip"));		
		// change icon
		top4.add (new JLabel("    "));
        JButton changeIcon = new JButton(rb.getString("ChangeIcons")+"...");
        changeIcon.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
					if (sensorBox.isSelected())
						sensorFrame.setVisible(true);
					else if (signalBox.isSelected())
						signalFrame.setVisible(true);
					else if (iconLabelBox.isSelected())
						iconFrame.setVisible(true);
                }
            } );
        top4.add(changeIcon);
		changeIcon.setToolTipText(rb.getString("ChangeIconToolTip"));
		// sensor icon
		top4.add (new JLabel("    "));
        top4.add (sensorBox);
		sensorBox.setToolTipText(rb.getString("SensorBoxToolTip"));
        top4.add (nextSensor);
		nextSensor.setToolTipText(rb.getString("SensorIconToolTip"));
        sensorIconEditor = new MultiIconEditor(4);
        sensorIconEditor.setIcon(0, "Active:","resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        sensorIconEditor.setIcon(1, "Inactive", "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        sensorIconEditor.setIcon(2, "Inconsistent:", "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.setIcon(3, "Unknown:","resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.complete();
        sensorFrame = new JFrame(rb.getString("EditSensorIcons"));
		sensorFrame.getContentPane().add(new JLabel("  "+rb.getString("IconChangeInfo")+"  "),BorderLayout.NORTH);
        sensorFrame.getContentPane().add(sensorIconEditor);
        sensorFrame.pack();
		// signal icon
		top4.add (new JLabel("    "));
        top4.add (signalBox);
		signalBox.setToolTipText(rb.getString("SignalMastBoxToolTip"));
        top4.add (nextSignalHead);
		nextSignalHead.setToolTipText(rb.getString("SignalIconToolTip"));
        signalIconEditor = new MultiIconEditor(10);
		signalIconEditor.setIcon(0, "Red:","resources/icons/smallschematics/searchlights/left-red-short.gif");
		signalIconEditor.setIcon(1, "Flash red:", "resources/icons/smallschematics/searchlights/left-flashred-short.gif");
		signalIconEditor.setIcon(2, "Yellow:", "resources/icons/smallschematics/searchlights/left-yellow-short.gif");
		signalIconEditor.setIcon(3, "Flash yellow:", "resources/icons/smallschematics/searchlights/left-flashyellow-short.gif");
		signalIconEditor.setIcon(4, "Green:","resources/icons/smallschematics/searchlights/left-green-short.gif");
		signalIconEditor.setIcon(5, "Flash green:","resources/icons/smallschematics/searchlights/left-flashgreen-short.gif");
		signalIconEditor.setIcon(6, "Dark:","resources/icons/smallschematics/searchlights/left-dark-short.gif");
		signalIconEditor.setIcon(7, "Held:","resources/icons/smallschematics/searchlights/left-held-short.gif");
        signalIconEditor.setIcon(8, "Lunar","resources/icons/smallschematics/searchlights/left-lunar-short-marker.gif");
        signalIconEditor.setIcon(9, "Flash Lunar","resources/icons/smallschematics/searchlights/left-flashlunar-short-marker.gif");
        signalIconEditor.complete();
        signalFrame = new JFrame(rb.getString("EditSignalIcons"));
		signalFrame.getContentPane().add(new JLabel("  "+rb.getString("IconChangeInfo")+"  "),BorderLayout.NORTH);
        signalFrame.getContentPane().add(signalIconEditor);
        signalFrame.pack();
        signalFrame.setVisible(false);
        
        top4.add (new JLabel("    "));
        top4.add (signalMastBox);
        top4.add (nextSignalMast);
		// icon label
		top4.add (new JLabel("    "));
        top4.add (iconLabelBox);
		iconLabelBox.setToolTipText(rb.getString("IconLabelToolTip"));
        iconEditor = new MultiIconEditor(1);
        iconEditor.setIcon(0, "","resources/icons/smallschematics/tracksegments/block.gif");
        iconEditor.complete();
        iconFrame = new JFrame(rb.getString("EditIcon"));
        iconFrame.getContentPane().add(iconEditor);
        iconFrame.pack();

		topEditBar.add(top4);
        contentPane.add(topEditBar);
        topEditBar.setVisible(false);

        // set to full screen
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        height = screenDim.height-120;
        width = screenDim.width-20;
        // Let Editor make target, and use this frame
        super.setTargetPanel(null, null);
        super.setTargetPanelSize(width, height);
        setSize(screenDim.width, screenDim.height);
        topEditBar.setSize(screenDim.width, topEditBar.getPreferredSize().height);
        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("SansSerif", Font.PLAIN, 12),
                                                     Color.black, new Color(215, 225, 255), Color.black));
		// setup help bar
		helpBar = new JPanel();
        helpBar.setLayout(new BoxLayout(helpBar, BoxLayout.Y_AXIS));
        JPanel help1 = new JPanel();
		help1.add(new JLabel(rb.getString("Help1")));
		helpBar.add(help1);
        JPanel help2 = new JPanel();
		help2.add(new JLabel(rb.getString("Help2")));
		helpBar.add(help2);
        JPanel help3 = new JPanel();
                switch (SystemType.getType()) {
                    case SystemType.MACOSX:
                        help3.add(new JLabel(rb.getString("Help3Mac")));
                        break;
                    case SystemType.WINDOWS:
                        help3.add(new JLabel(rb.getString("Help3Win")));
                        break;
                    case SystemType.LINUX:
                        help3.add(new JLabel(rb.getString("Help3Win")));
                        break;
                    default:
                        help3.add(new JLabel(rb.getString("Help3")));
                }
		helpBar.add(help3);

        contentPane.add(helpBar);
        helpBar.setVisible(false);
		
        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);
        // confirm that panel hasn't already been loaded
        if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
        	log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
        }
		jmri.jmrit.display.PanelMenu.instance().addEditorPanel(this);
    	thisPanel = this;
        thisPanel.setFocusable(true);
        thisPanel.addKeyListener(this);
		resetDirty();
		// establish link to LayoutEditorAuxTools
		auxTools = new LayoutEditorAuxTools(thisPanel);
		if (auxTools==null) log.error("Unable to create link to LayoutEditorAuxTools"); 
    }

    protected void init(String name) {}

    public void initView() {
        editModeItem.setSelected(isEditable());
        positionableItem.setSelected(allPositionable());
        controlItem.setSelected(allControlling());
		if (isEditable()) setAllShowTooltip(tooltipsInEditMode);
		else setAllShowTooltip(tooltipsWithoutEditMode);
        switch (_scrollState) {
            case SCROLL_NONE:
                scrollNone.setSelected(true);
                break;
            case SCROLL_BOTH:
                scrollBoth.setSelected(true);
                break;
            case SCROLL_HORIZONTAL:
                scrollHorizontal.setSelected(true);
                break;
            case SCROLL_VERTICAL:
                scrollVertical.setSelected(true);
                break;
            default: break;
        }
    }

    public void setSize(int w, int h) {
        log.debug("Frame size now w="+width+", h="+height);
        super.setSize(w,h);
    }

    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        boolean save = (isDirty() || (savedEditMode!=isEditable()) ||
                (savedPositionable!=allPositionable()) ||
                (savedControlLayout!=allControlling()) ||					
                (savedAnimatingLayout!=animatingLayout) ||					
                (savedShowHelpBar!=showHelpBar) );
        targetWindowClosing(save);
    }
	
	LayoutEditorTools tools = null;
    jmri.jmrit.signalling.AddEntryExitPairAction entryExit = null;
	void setupToolsMenu(JMenuBar menuBar) {
		JMenu toolsMenu = new JMenu(rb.getString("MenuTools"));
		menuBar.add(toolsMenu);
		// scale track diagram 
        JMenuItem scaleItem = new JMenuItem(rb.getString("ScaleTrackDiagram")+"...");
        toolsMenu.add(scaleItem);
        scaleItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					// bring up scale track diagram dialog
					scaleTrackDiagram();
                }
            });
		// translate selection 
        JMenuItem moveItem = new JMenuItem(rb.getString("TranslateSelection")+"...");
        toolsMenu.add(moveItem);
        moveItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					// bring up translate selection dialog
					moveSelection();
                }
            });
		// undo translate selection 
        JMenuItem undoMoveItem = new JMenuItem(rb.getString("UndoTranslateSelection"));
        toolsMenu.add(undoMoveItem);
        undoMoveItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					// undo previous move selection 
					undoMoveSelection();
                }
            });
		// reset turnout size to program defaults
		JMenuItem undoTurnoutSize = new JMenuItem(rb.getString("ResetTurnoutSize"));
		toolsMenu.add(undoTurnoutSize);
		undoTurnoutSize.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// undo previous move selection 
					resetTurnoutSize();
				}
			});
		toolsMenu.addSeparator();
		// skip turnout
		skipTurnoutItem = new JCheckBoxMenuItem(rb.getString("SkipInternalTurnout"));
        toolsMenu.add(skipTurnoutItem);
        skipTurnoutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    skipIncludedTurnout = skipTurnoutItem.isSelected();
                }
            });                    
        skipTurnoutItem.setSelected(skipIncludedTurnout);		
		// set signals at turnout
		JMenuItem turnoutItem = new JMenuItem(rb.getString("SignalsAtTurnout")+"...");
        toolsMenu.add(turnoutItem);
        turnoutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at turnout tool dialog
					tools.setSignalsAtTurnout(signalIconEditor,signalFrame);
                }
            });
		// set signals at block boundary
		JMenuItem boundaryItem = new JMenuItem(rb.getString("SignalsAtBoundary")+"...");
        toolsMenu.add(boundaryItem);
        boundaryItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at block boundary tool dialog
					tools.setSignalsAtBlockBoundary(signalIconEditor,signalFrame);
                }
            });
		// set signals at crossover turnout
		JMenuItem xoverItem = new JMenuItem(rb.getString("SignalsAtXoverTurnout")+"...");
        toolsMenu.add(xoverItem);
        xoverItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at double crossover tool dialog
					tools.setSignalsAtXoverTurnout(signalIconEditor,signalFrame);
                }
            });
		// set signals at level crossing
		JMenuItem xingItem = new JMenuItem(rb.getString("SignalsAtLevelXing")+"...");
        toolsMenu.add(xingItem);
        xingItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at level crossing tool dialog
					tools.setSignalsAtLevelXing(signalIconEditor,signalFrame);
                }
            });
		// set signals at throat-to-throat turnouts
		JMenuItem tToTItem = new JMenuItem(rb.getString("SignalsAtTToTTurnout")+"...");
        toolsMenu.add(tToTItem);
        tToTItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at throat-to-throat turnouts tool dialog
					tools.setSignalsAtTToTTurnouts(signalIconEditor,signalFrame);
                }
            });
		// set signals at 3-way turnout
		JMenuItem way3Item = new JMenuItem(rb.getString("SignalsAt3WayTurnout")+"...");
        toolsMenu.add(way3Item);
        way3Item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at 3-way turnout tool dialog
					tools.setSignalsAt3WayTurnout(signalIconEditor,signalFrame);
                }
            });
		JMenuItem slipItem = new JMenuItem(rb.getString("SignalsAtSlip")+"...");
        toolsMenu.add(slipItem);
        slipItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(thisPanel);
					}
					// bring up signals at throat-to-throat turnouts tool dialog
					tools.setSignalsAtSlip(signalIconEditor,signalFrame);
                }
            });

        JMenuItem entryExitItem = new JMenuItem("Entry Exit"+"...");
        toolsMenu.add(entryExitItem);
        entryExitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                if(entryExit == null)
                    entryExit = new jmri.jmrit.signalling.AddEntryExitPairAction("ENTRY EXIT", thisPanel);
                entryExit.actionPerformed(event);
            }
        });
        
	}

	protected JMenu setupOptionMenu(JMenuBar menuBar) {
        JMenu optionMenu = new JMenu(rbx.getString("Options"));
        menuBar.add(optionMenu);
		// edit mode item
        editModeItem = new JCheckBoxMenuItem(rb.getString("EditMode"));
        optionMenu.add(editModeItem);
        editModeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllEditable(editModeItem.isSelected());
					if (isEditable()) {
						helpBar.setVisible(showHelpBar);
						setAllShowTooltip(tooltipsInEditMode);
					}
					else {
						setAllShowTooltip(tooltipsWithoutEditMode);
					}
					awaitingIconChange = false;
                }
            });
        editModeItem.setSelected(isEditable());
		// positionable item
        positionableItem = new JCheckBoxMenuItem(rb.getString("AllowRepositioning"));
        optionMenu.add(positionableItem);
        positionableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllPositionable(positionableItem.isSelected());
                }
            });                    
        positionableItem.setSelected(allPositionable());
		// controlable item
		controlItem = new JCheckBoxMenuItem(rb.getString("AllowLayoutControl"));
        optionMenu.add(controlItem);
        controlItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllControlling(controlItem.isSelected());
                }
            });                    
        controlItem.setSelected(allControlling());

		// animation item
		animationItem = new JCheckBoxMenuItem(rb.getString("AllowTurnoutAnimation"));
        optionMenu.add(animationItem);
        animationItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    boolean mode = animationItem.isSelected();
                    setTurnoutAnimation(mode);
                }
            });                    
        animationItem.setSelected(true);
		// show help item
		showHelpItem = new JCheckBoxMenuItem(rb.getString("ShowEditHelp"));
        optionMenu.add(showHelpItem);
        showHelpItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    showHelpBar = showHelpItem.isSelected();
					if (isEditable()) {
						helpBar.setVisible(showHelpBar);
					}
                }
            });                    
        showHelpItem.setSelected(showHelpBar);
		// show grid item
		showGridItem = new JCheckBoxMenuItem(rb.getString("ShowEditGrid"));
        optionMenu.add(showGridItem);
        showGridItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    drawGrid = showGridItem.isSelected();
					repaint();
                }
            });                    
        showGridItem.setSelected(drawGrid);
		// snap to grid on add item
		snapToGridOnAddItem = new JCheckBoxMenuItem(rb.getString("SnapToGridOnAdd"));
        optionMenu.add(snapToGridOnAddItem);
        snapToGridOnAddItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    snapToGridOnAdd = snapToGridOnAddItem.isSelected();
					repaint();
                }
            });                    
        snapToGridOnAddItem.setSelected(snapToGridOnAdd);
		// snap to grid on move item
		snapToGridOnMoveItem = new JCheckBoxMenuItem(rb.getString("SnapToGridOnMove"));
        optionMenu.add(snapToGridOnMoveItem);
        snapToGridOnMoveItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    snapToGridOnMove = snapToGridOnMoveItem.isSelected();
					repaint();
                }
            });                    
        snapToGridOnMoveItem.setSelected(snapToGridOnMove);

		// Show/Hide Scroll Bars
        scrollMenu = new JMenu(rb.getString("ScrollBarsSubMenu"));
        optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollBoth = new JRadioButtonMenuItem(rbx.getString("ScrollBoth"));
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.setSelected(_scrollState==SCROLL_BOTH);
        scrollBoth.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _scrollState = SCROLL_BOTH;
                    setScroll(_scrollState);
                    repaint();
                }
            });
        scrollNone = new JRadioButtonMenuItem(rbx.getString("ScrollNone"));
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.setSelected(_scrollState==SCROLL_NONE);
        scrollNone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _scrollState = SCROLL_NONE;
                    setScroll(_scrollState);
                    repaint();
                }
            });
        scrollHorizontal = new JRadioButtonMenuItem(rbx.getString("ScrollHorizontal"));
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.setSelected(_scrollState==SCROLL_HORIZONTAL);
        scrollHorizontal.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _scrollState = SCROLL_HORIZONTAL;
                    setScroll(_scrollState);
                    repaint();
                }
            });
        scrollVertical = new JRadioButtonMenuItem(rbx.getString("ScrollVertical"));
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.setSelected(_scrollState==SCROLL_VERTICAL);
        scrollVertical.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _scrollState = SCROLL_VERTICAL;
                    setScroll(_scrollState);
                    repaint();
                }
            });

		// Tooltip options
        tooltipMenu = new JMenu(rb.getString("TooltipSubMenu"));
        optionMenu.add(tooltipMenu);
        ButtonGroup tooltipGroup = new ButtonGroup();
        tooltipNone = new JRadioButtonMenuItem(rb.getString("TooltipNone"));
        tooltipGroup.add(tooltipNone);
        tooltipMenu.add(tooltipNone);
        tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    tooltipsInEditMode = false;
                    tooltipsWithoutEditMode = false;
                    setAllShowTooltip(false);
                }
            });
        tooltipAlways = new JRadioButtonMenuItem(rb.getString("TooltipAlways"));
        tooltipGroup.add(tooltipAlways);
        tooltipMenu.add(tooltipAlways);
        tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipAlways.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    tooltipsInEditMode = true;
                    tooltipsWithoutEditMode = true;
                    setAllShowTooltip(true);
                }
            });
        tooltipInEdit = new JRadioButtonMenuItem(rb.getString("TooltipEdit"));
        tooltipGroup.add(tooltipInEdit);
        tooltipMenu.add(tooltipInEdit);
        tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipInEdit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    tooltipsInEditMode = true;
                    tooltipsWithoutEditMode = false;
                    setAllShowTooltip(isEditable());
                }
            });
        tooltipNotInEdit = new JRadioButtonMenuItem(rb.getString("TooltipNotEdit"));
        tooltipGroup.add(tooltipNotInEdit);
        tooltipMenu.add(tooltipNotInEdit);
        tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipNotInEdit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    tooltipsInEditMode = false;
                    tooltipsWithoutEditMode = true;
                    setAllShowTooltip(!isEditable());
                }
            });
        // antialiasing
		antialiasingOnItem = new JCheckBoxMenuItem(rb.getString("AntialiasingOn"));
        optionMenu.add(antialiasingOnItem);
        antialiasingOnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    antialiasingOn = antialiasingOnItem.isSelected();
					repaint();
                }
            });                    
        antialiasingOnItem.setSelected(antialiasingOn);
		// title item
        optionMenu.addSeparator();
        JMenuItem titleItem = new JMenuItem(rb.getString("EditTitle")+"...");
        optionMenu.add(titleItem);
        titleItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    // prompt for name
                    String newName = (String)JOptionPane.showInputDialog(getTargetFrame(), 
                                        rb.getString("EnterTitle")+":", rb.getString("EditTitleMessageTitle"), 
                                            JOptionPane.PLAIN_MESSAGE, null, null,layoutName);
                    if (newName==null) return;  // cancelled
                    if(newName.equals(layoutName)){
                        return;
                    }
                    if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(newName)){
                    	JOptionPane.showMessageDialog(null, rb.getString("CanNotRename"), rb.getString("PanelExist"),
                    			JOptionPane.ERROR_MESSAGE);
                    	return;
                    }
                    setTitle(newName);
                    layoutName = newName;
					jmri.jmrit.display.PanelMenu.instance().renameEditorPanel(thisPanel);
					setDirty(true);
                }
            });
		// add background image
        JMenuItem backgroundItem = new JMenuItem(rb.getString("AddBackground")+"...");
        optionMenu.add(backgroundItem);
        backgroundItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addBackground();
					setDirty(true);
					repaint();
                }
            });
        
        JMenu backgroundColorMenu = new JMenu(rb.getString("SetBackgroundColor"));
		backgroundColorButtonGroup = new ButtonGroup();
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Black"), Color.black);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Gray"),Color.gray);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("LightGray"),Color.lightGray);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("White"),Color.white);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Red"),Color.red);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Pink"),Color.pink);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Orange"),Color.orange);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Yellow"),Color.yellow);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Green"),Color.green);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Blue"),Color.blue);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Magenta"),Color.magenta);
		addBackgroundColorMenuEntry(backgroundColorMenu, rb.getString("Cyan"),Color.cyan);
        optionMenu.add(backgroundColorMenu);
		// add fast clock
        JMenuItem clockItem = new JMenuItem(rb.getString("AddFastClock"));
        optionMenu.add(clockItem);
        clockItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addClock();
					setDirty(true);
					repaint();
                }
            });
		// add turntable
        JMenuItem turntableItem = new JMenuItem(rb.getString("AddTurntable"));
        optionMenu.add(turntableItem);
        turntableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addTurntable(windowCenter());					
					setDirty(true);
					repaint();
                }
            });
		// add reporter
        JMenuItem reporterItem = new JMenuItem(rb.getString("AddReporter")+"...");
        optionMenu.add(reporterItem);
        reporterItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					Point2D pt = windowCenter();
					enterReporter((int)pt.getX() , (int)pt.getY());
					setDirty(true);
					repaint();
                }
            });
		// set location and size
        JMenuItem locationItem = new JMenuItem(rb.getString("SetLocation"));
        optionMenu.add(locationItem);
        locationItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setCurrentPositionAndSize();
					log.debug("Bounds:"+upperLeftX+", "+upperLeftY+", "+windowWidth+", "+windowHeight+", "+panelWidth+", "+panelHeight);
                }
            });
		// set track width 
        JMenuItem widthItem = new JMenuItem(rb.getString("SetTrackWidth")+"...");
        optionMenu.add(widthItem);
        widthItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					// bring up enter track width dialog
					enterTrackWidth();
                }
            });
		// track color item
        JMenu trkColourMenu = new JMenu(rb.getString("DefaultTrackColor"));
        optionMenu.add(trkColourMenu);
		JMenu trackColorMenu = new JMenu(rb.getString("DefaultTrackColor"));
		trackColorButtonGroup = new ButtonGroup();
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Black"), Color.black);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Gray"),Color.gray);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("LightGray"),Color.lightGray);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("White"),Color.white);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Red"),Color.red);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Pink"),Color.pink);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Orange"),Color.orange);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Yellow"),Color.yellow);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Green"),Color.green);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Blue"),Color.blue);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Magenta"),Color.magenta);
		addTrackColorMenuEntry(trackColorMenu, rb.getString("Cyan"),Color.cyan);
        trkColourMenu .add(trackColorMenu);
        
		JMenu trackOccupiedColorMenu = new JMenu(rb.getString("DefaultOccupiedTrackColor"));
		trackOccupiedColorButtonGroup = new ButtonGroup();
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Black"), Color.black);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Gray"),Color.gray);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("LightGray"),Color.lightGray);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("White"),Color.white);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Red"),Color.red);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Pink"),Color.pink);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Orange"),Color.orange);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Yellow"),Color.yellow);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Green"),Color.green);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Blue"),Color.blue);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Magenta"),Color.magenta);
		addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, rb.getString("Cyan"),Color.cyan);
        trkColourMenu .add(trackOccupiedColorMenu);
        
		JMenu trackAlternativeColorMenu = new JMenu(rb.getString("DefaultAlternativeTrackColor"));
		trackAlternativeColorButtonGroup = new ButtonGroup();
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Black"), Color.black);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Gray"),Color.gray);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("LightGray"),Color.lightGray);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("White"),Color.white);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Red"),Color.red);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Pink"),Color.pink);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Orange"),Color.orange);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Yellow"),Color.yellow);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Green"),Color.green);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Blue"),Color.blue);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Magenta"),Color.magenta);
		addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, rb.getString("Cyan"),Color.cyan);
        trkColourMenu .add(trackAlternativeColorMenu);

		JMenu textColorMenu = new JMenu(rb.getString("DefaultTextColor"));
		textColorButtonGroup = new ButtonGroup();
		addTextColorMenuEntry(textColorMenu, rb.getString("Black"), Color.black);
		addTextColorMenuEntry(textColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addTextColorMenuEntry(textColorMenu, rb.getString("Gray"),Color.gray);
		addTextColorMenuEntry(textColorMenu, rb.getString("LightGray"),Color.lightGray);
		addTextColorMenuEntry(textColorMenu, rb.getString("White"),Color.white);
		addTextColorMenuEntry(textColorMenu, rb.getString("Red"),Color.red);
		addTextColorMenuEntry(textColorMenu, rb.getString("Pink"),Color.pink);
		addTextColorMenuEntry(textColorMenu, rb.getString("Orange"),Color.orange);
		addTextColorMenuEntry(textColorMenu, rb.getString("Yellow"),Color.yellow);
		addTextColorMenuEntry(textColorMenu, rb.getString("Green"),Color.green);
		addTextColorMenuEntry(textColorMenu, rb.getString("Blue"),Color.blue);
		addTextColorMenuEntry(textColorMenu, rb.getString("Magenta"),Color.magenta);
		addTextColorMenuEntry(textColorMenu, rb.getString("Cyan"),Color.cyan);
        optionMenu.add(textColorMenu);
        
        //turnout options submenu
        JMenu turnoutOptionsMenu = new JMenu(rb.getString("TurnoutOptions"));
        optionMenu.add(turnoutOptionsMenu);

        // circle on Turnouts 
		turnoutCirclesOnItem = new JCheckBoxMenuItem(rb.getString("TurnoutCirclesOn"));
		turnoutOptionsMenu.add(turnoutCirclesOnItem);
        turnoutCirclesOnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    turnoutCirclesWithoutEditMode = turnoutCirclesOnItem.isSelected();
					repaint();
                }
            });                    
        turnoutCirclesOnItem.setSelected(turnoutCirclesWithoutEditMode);

        // select turnout circle color 
        JMenu turnoutCircleColorMenu = new JMenu(rb.getString("TurnoutCircleColor"));
		turnoutCircleColorButtonGroup = new ButtonGroup();
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("UseDefaultTrackColor"), null);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Black"), Color.black);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("DarkGray"),Color.darkGray);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Gray"),Color.gray);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("LightGray"),Color.lightGray);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("White"),Color.white);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Red"),Color.red);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Pink"),Color.pink);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Orange"),Color.orange);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Yellow"),Color.yellow);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Green"),Color.green);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Blue"),Color.blue);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Magenta"),Color.magenta);
		addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, rb.getString("Cyan"),Color.cyan);
        turnoutOptionsMenu.add(turnoutCircleColorMenu);
        
        // select turnout circle size 
        JMenu turnoutCircleSizeMenu = new JMenu(rb.getString("TurnoutCircleSize"));
		turnoutCircleSizeButtonGroup = new ButtonGroup();
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "1", 1);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "2", 2);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "3", 3);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "4", 4);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "5", 5);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "6", 6);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "7", 7);
		addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "8", 8);
        turnoutOptionsMenu.add(turnoutCircleSizeMenu);
        
        // enable drawing of unselected leg (helps when diverging angle is small)
		turnoutDrawUnselectedLegItem = new JCheckBoxMenuItem(rb.getString("TurnoutDrawUnselectedLeg"));
		turnoutOptionsMenu.add(turnoutDrawUnselectedLegItem);
		turnoutDrawUnselectedLegItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                	turnoutDrawUnselectedLeg = turnoutDrawUnselectedLegItem.isSelected();
					repaint();
                }
            });                    
		turnoutDrawUnselectedLegItem.setSelected(turnoutDrawUnselectedLeg);

        		// show grid item
		autoAssignBlocksItem = new JCheckBoxMenuItem(rb.getString("AutoAssignBlock"));
        optionMenu.add(autoAssignBlocksItem);
        autoAssignBlocksItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    autoAssignBlocks = autoAssignBlocksItem.isSelected();
                }
            });                    
        autoAssignBlocksItem.setSelected(autoAssignBlocks);
        
        return optionMenu;
	}

	private void setupZoomMenu(JMenuBar menuBar) {
        JMenu zoomMenu = new JMenu(rb.getString("MenuZoom"));
        menuBar.add(zoomMenu);
		ButtonGroup zoomButtonGroup = new ButtonGroup();
		// edit mode item
        JRadioButtonMenuItem noZoomItem = new JRadioButtonMenuItem(rb.getString("NoZoom"));
        zoomMenu.add(noZoomItem);
        noZoomItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setZoom(1.0);
                }
            });
		zoomButtonGroup.add(noZoomItem);
        JRadioButtonMenuItem zoom15Item = new JRadioButtonMenuItem("x 1.5");
        zoomMenu.add(zoom15Item);
        zoom15Item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setZoom(1.5);
                }
            });
		zoomButtonGroup.add(zoom15Item);
        JRadioButtonMenuItem zoom20Item = new JRadioButtonMenuItem("x 2.0");
        zoomMenu.add(zoom20Item);
        zoom20Item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setZoom(2.0);
                }
            });
		zoomButtonGroup.add(zoom20Item);
        JRadioButtonMenuItem zoom30Item = new JRadioButtonMenuItem("x 3.0");
        zoomMenu.add(zoom30Item);
        zoom30Item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setZoom(3.0);
                }
            });
		zoomButtonGroup.add(zoom30Item);
        JRadioButtonMenuItem zoom40Item = new JRadioButtonMenuItem("x 4.0");
        zoomMenu.add(zoom40Item);
        zoom40Item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					setZoom(4.0);
                }
            });			
		zoomButtonGroup.add(zoom40Item);
		noZoomItem.setSelected(true);
	}
	
	private void setZoom(double factor) {
        setPaintScale(factor);
	}

	private Point2D windowCenter() {
		// Returns window's center coordinates converted to layout space
		// Used for initial setup of turntables and reporters
		// First of all compute center of window in screen coordinates
		Point pt = getLocationOnScreen();
		Dimension dim = getSize();
		pt.x += dim.width/2;
		pt.y += dim.height/2 + 40; // 40 = approx. difference between upper and lower menu areas
		// Now convert to layout space
		SwingUtilities.convertPointFromScreen(pt, getTargetPanel());
		pt.x /= getPaintScale();
		pt.y /= getPaintScale();
		return pt;
	}
	private void setupMarkerMenu(JMenuBar menuBar) {
        JMenu markerMenu = new JMenu(rbx.getString("MenuMarker"));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(rbx.getString("AddLoco")+"..."){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(rbx.getString("AddLocoRoster")+"..."){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(rbx.getString("RemoveMarkers")){
        	public void actionPerformed(ActionEvent e) {
        		removeMarkers();
            }
        });
	}
    /**
     * Remove marker icons from panel
     */
    protected void removeMarkers() {
		for (int i = markerImage.size(); i >0 ; i--) {
			LocoIcon il = markerImage.get(i-1);
			if ( (il != null) && (il.isActive()) ) {
				markerImage.remove(i-1);
				il.remove();
				il.dispose();
				setDirty(true);
			}
		}
        super.removeMarkers();
		repaint();
	}

	// operational variables for enter track width pane
	private JmriJFrame enterTrackWidthFrame = null;
	private boolean enterWidthOpen = false;
	private boolean trackWidthChange = false;
	private JTextField sideWidthField = new JTextField(6);
	private JTextField mainlineWidthField = new JTextField(6);
	private JButton trackWidthDone;
	private JButton trackWidthCancel;

	// display dialog for entering track widths
	protected void enterTrackWidth() {
		if (enterWidthOpen) {
			enterTrackWidthFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (enterTrackWidthFrame == null) {
            enterTrackWidthFrame = new JmriJFrame( rb.getString("SetTrackWidth") );
            enterTrackWidthFrame.addHelpMenu("package.jmri.jmrit.display.EnterTrackWidth", true);
            enterTrackWidthFrame.setLocation(70,30);
            Container theContentPane = enterTrackWidthFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			// setup side track width
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel sideWidthLabel = new JLabel( rb.getString("SideTrackWidth"));
            panel2.add(sideWidthLabel);
            panel2.add(sideWidthField);
            sideWidthField.setToolTipText( rb.getString("SideTrackWidthHint") );
            theContentPane.add(panel2);
			// setup mainline track width
            JPanel panel3 = new JPanel(); 
            panel3.setLayout(new FlowLayout());
			JLabel mainlineWidthLabel = new JLabel( rb.getString("MainlineTrackWidth"));
            panel3.add(mainlineWidthLabel);
            panel3.add(mainlineWidthField);
            mainlineWidthField.setToolTipText( rb.getString("MainlineTrackWidthHint") );
            theContentPane.add(panel3);
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(trackWidthDone = new JButton(rb.getString("Done")));
            trackWidthDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    trackWidthDonePressed(e);
                }
            });
            trackWidthDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(trackWidthCancel = new JButton(rb.getString("Cancel")));
            trackWidthCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    trackWidthCancelPressed(e);
                }
            });
            trackWidthCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel5);
		}
		// Set up for Entry of Track Widths
		mainlineWidthField.setText(""+getMainlineTrackWidth());
		sideWidthField.setText(""+getSideTrackWidth());
		enterTrackWidthFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					trackWidthCancelPressed(null);
				}
			});
        enterTrackWidthFrame.pack();
        enterTrackWidthFrame.setVisible(true);	
		trackWidthChange = false;	
		enterWidthOpen = true;
	}	
	void trackWidthDonePressed(ActionEvent a) {
		String newWidth = "";
		float wid = 0.0F;
		// get side track width
		newWidth = sideWidthField.getText().trim();
		try {
			wid = Float.parseFloat(newWidth);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(enterTrackWidthFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		if ( (wid<=0.99) || (wid>10.0) ) {
			JOptionPane.showMessageDialog(enterTrackWidthFrame,
					java.text.MessageFormat.format(rb.getString("Error2"),
					new Object[]{" "+wid+" "}),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (sideTrackWidth!=wid) {
			sideTrackWidth = wid;
			trackWidthChange = true;
		}
		// get mainline track width
		newWidth = mainlineWidthField.getText().trim();
		try {
			wid = Float.parseFloat(newWidth);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(enterTrackWidthFrame,rb.getString("EntryError")+": "+
					e+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		if ( (wid<=0.99) || (wid>10.0) ) {
			JOptionPane.showMessageDialog(enterTrackWidthFrame,
					java.text.MessageFormat.format(rb.getString("Error2"),
					new Object[]{" "+wid+" "}),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (mainlineTrackWidth!=wid) {
			mainlineTrackWidth = wid;
			trackWidthChange = true;
		}
		// success - hide dialog and repaint if needed
		enterWidthOpen = false;
		enterTrackWidthFrame.setVisible(false);
		enterTrackWidthFrame.dispose();
		enterTrackWidthFrame = null;
		if (trackWidthChange) {
			repaint();
			setDirty(true);
		}
	}
	void trackWidthCancelPressed(ActionEvent a) {
		enterWidthOpen = false;
		enterTrackWidthFrame.setVisible(false);
		enterTrackWidthFrame.dispose();
		enterTrackWidthFrame = null;
		if (trackWidthChange) {
			repaint();
			setDirty(true);
		}
	}
	
	// operational variables for enter reporter pane
	private JmriJFrame enterReporterFrame = null;
	private boolean reporterOpen = false;
	private JTextField xPositionField = new JTextField(6);
	private JTextField yPositionField = new JTextField(6);
	private JTextField reporterNameField = new JTextField(6);
	private JButton reporterDone;
	private JButton reporterCancel;

	// display dialog for entering Reporters
	protected void enterReporter(int defaultX, int defaultY) {
		if (reporterOpen) {
			enterReporterFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (enterReporterFrame == null) {
            enterReporterFrame = new JmriJFrame( rb.getString("AddReporter") );
//            enterReporterFrame.addHelpMenu("package.jmri.jmrit.display.AddReporterLabel", true);
            enterReporterFrame.setLocation(70,30);
            Container theContentPane = enterReporterFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			// setup reporter entry
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel reporterLabel = new JLabel( rb.getString("ReporterName")+":");
            panel2.add(reporterLabel);
            panel2.add(reporterNameField);
            reporterNameField.setToolTipText( rb.getString("ReporterNameHint") );
            theContentPane.add(panel2);
			// setup coordinates entry
            JPanel panel3 = new JPanel(); 
            panel3.setLayout(new FlowLayout());
			JLabel xCoordLabel = new JLabel( rb.getString("ReporterLocationX"));
            panel3.add(xCoordLabel);
            panel3.add(xPositionField);
            xPositionField.setToolTipText( rb.getString("ReporterLocationXHint") );
 			JLabel yCoordLabel = new JLabel( rb.getString("ReporterLocationY"));
            panel3.add(yCoordLabel);
            panel3.add(yPositionField);
            yPositionField.setToolTipText( rb.getString("ReporterLocationYHint") );
			theContentPane.add(panel3);
			// set up Add and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(reporterDone = new JButton(rb.getString("AddNewLabel")));
            reporterDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    reporterDonePressed(e);
                }
            });
            reporterDone.setToolTipText( rb.getString("ReporterDoneHint") );
			// Cancel
            panel5.add(reporterCancel = new JButton(rb.getString("Cancel")));
            reporterCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    reporterCancelPressed(e);
                }
            });
            reporterCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel5);
			
		}
		// Set up for Entry of Reporter Icon
		xPositionField.setText(""+defaultX);
		yPositionField.setText(""+defaultY);
		enterReporterFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					reporterCancelPressed(null);
				}
			});
        enterReporterFrame.pack();
        enterReporterFrame.setVisible(true);	
		reporterOpen = true;
	}	
	void reporterDonePressed(ActionEvent a) {
		// get size of current panel
		Dimension dim = getTargetPanelSize();
		// get x coordinate
		String newX = "";
		int xx = 0;
		newX = xPositionField.getText().trim();
		try {
			xx = Integer.parseInt(newX);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(enterReporterFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		if ( ( xx<=0) || (xx>dim.width) ) {
			JOptionPane.showMessageDialog(enterReporterFrame,
					java.text.MessageFormat.format(rb.getString("Error2a"),
					new Object[]{" "+xx+" "}),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// get y coordinate
		String newY = "";
		int yy = 0;
		newY = yPositionField.getText().trim();
		try {
			yy = Integer.parseInt(newY);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(enterReporterFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		if ( ( yy<=0) || (yy>dim.height) ) {
			JOptionPane.showMessageDialog(enterReporterFrame,
					java.text.MessageFormat.format(rb.getString("Error2a"),
					new Object[]{" "+yy+" "}),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// get reporter name
		Reporter reporter = null;
		String rName = reporterNameField.getText().trim();
        if (InstanceManager.reporterManagerInstance()!=null) {
            reporter = InstanceManager.reporterManagerInstance().
                provideReporter(rName);
            if (reporter == null) {
				JOptionPane.showMessageDialog(enterReporterFrame,
					java.text.MessageFormat.format(rb.getString("Error18"),
					new Object[]{rName}),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			if ( !rName.equals(reporter.getUserName()) ) 
				rName = rName.toUpperCase();
		}
		else {
			JOptionPane.showMessageDialog(enterReporterFrame,
					rb.getString("Error17"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// add the reporter icon
		addReporter(rName,xx,yy);
		// success - repaint the panel
		repaint();
        enterReporterFrame.setVisible(true);	
	}
	void reporterCancelPressed(ActionEvent a) {
		reporterOpen = false;
		enterReporterFrame.setVisible(false);
		enterReporterFrame.dispose();
		enterReporterFrame = null;
		repaint();
	}

	// operational variables for scale/translate track diagram pane
	private JmriJFrame scaleTrackDiagramFrame = null;
	private boolean scaleTrackDiagramOpen = false;
	private JTextField xFactorField = new JTextField(6);
	private JTextField yFactorField = new JTextField(6);
	private JTextField xTranslateField = new JTextField(6);
	private JTextField yTranslateField = new JTextField(6);
	private JButton scaleTrackDiagramDone;
	private JButton scaleTrackDiagramCancel;

	// display dialog for scaling the track diagram
	protected void scaleTrackDiagram() {
		if (scaleTrackDiagramOpen) {
			scaleTrackDiagramFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (scaleTrackDiagramFrame == null) {
            scaleTrackDiagramFrame = new JmriJFrame( rb.getString("ScaleTrackDiagram") );
            scaleTrackDiagramFrame.addHelpMenu("package.jmri.jmrit.display.ScaleTrackDiagram", true);
            scaleTrackDiagramFrame.setLocation(70,30);
            Container theContentPane = scaleTrackDiagramFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			// setup x translate
            JPanel panel31 = new JPanel(); 
            panel31.setLayout(new FlowLayout());
			JLabel xTranslateLabel = new JLabel( rb.getString("XTranslateLabel"));
            panel31.add(xTranslateLabel);
            panel31.add(xTranslateField);
            xTranslateField.setToolTipText( rb.getString("XTranslateHint") );
            theContentPane.add(panel31);
			// setup y translate
            JPanel panel32 = new JPanel(); 
            panel32.setLayout(new FlowLayout());
			JLabel yTranslateLabel = new JLabel( rb.getString("YTranslateLabel"));
            panel32.add(yTranslateLabel);
            panel32.add(yTranslateField);
            yTranslateField.setToolTipText( rb.getString("YTranslateHint") );
            theContentPane.add(panel32);
			// setup information message 1
            JPanel panel33 = new JPanel(); 
            panel33.setLayout(new FlowLayout());
			JLabel message1Label = new JLabel( rb.getString("Message1Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);			
			// setup x factor
            JPanel panel21 = new JPanel(); 
            panel21.setLayout(new FlowLayout());
			JLabel xFactorLabel = new JLabel( rb.getString("XFactorLabel"));
            panel21.add(xFactorLabel);
            panel21.add(xFactorField);
            xFactorField.setToolTipText( rb.getString("FactorHint") );
            theContentPane.add(panel21);
			// setup y factor
            JPanel panel22 = new JPanel(); 
            panel22.setLayout(new FlowLayout());
			JLabel yFactorLabel = new JLabel( rb.getString("YFactorLabel"));
            panel22.add(yFactorLabel);
            panel22.add(yFactorField);
            yFactorField.setToolTipText( rb.getString("FactorHint") );
            theContentPane.add(panel22);
			// setup information message 2
            JPanel panel23 = new JPanel(); 
            panel23.setLayout(new FlowLayout());
			JLabel message2Label = new JLabel( rb.getString("Message2Label"));
            panel23.add(message2Label);
            theContentPane.add(panel23);			
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(scaleTrackDiagramDone = new JButton(rb.getString("ScaleTranslate")));
            scaleTrackDiagramDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scaleTrackDiagramDonePressed(e);
                }
            });
            scaleTrackDiagramDone.setToolTipText( rb.getString("ScaleTranslateHint") );
            panel5.add(scaleTrackDiagramCancel = new JButton(rb.getString("Cancel")));
            scaleTrackDiagramCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scaleTrackDiagramCancelPressed(e);
                }
            });
            scaleTrackDiagramCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel5);
		}
		// Set up for Entry of Scale and Translation
		xFactorField.setText("1.0");
		yFactorField.setText("1.0");
		xTranslateField.setText("0");
		yTranslateField.setText("0");
		scaleTrackDiagramFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					scaleTrackDiagramCancelPressed(null);
				}
			});
        scaleTrackDiagramFrame.pack();
        scaleTrackDiagramFrame.setVisible(true);	
		scaleTrackDiagramOpen = true;
	}	
	void scaleTrackDiagramDonePressed(ActionEvent a) {
		String newText = "";
		boolean scaleChange = false;
		boolean translateError = false;
		float xTranslation = 0.0F;
		float yTranslation = 0.0F;
		float xFactor = 1.0F;
		float yFactor = 1.0F;
		// get x translation
		newText = xTranslateField.getText().trim();
		try {
			xTranslation = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(scaleTrackDiagramFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// get y translation
		newText = yTranslateField.getText().trim();
		try {
			yTranslation = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(scaleTrackDiagramFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// get x factor
		newText = xFactorField.getText().trim();
		try {
			xFactor = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(scaleTrackDiagramFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// get y factor
		newText = yFactorField.getText().trim();
		try {
			yFactor = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(scaleTrackDiagramFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// here when all numbers read in successfully - check for translation
		if ( (xTranslation!=0.0F) || (yTranslation!=0.0F) ) {
			// apply translation
			if ( translateTrack(xTranslation,yTranslation) )
				scaleChange = true;
			else {
				log.error("Error translating track diagram");
				translateError = true;
			}
		}
		if ( !translateError && ( (xFactor!=1.0) || (yFactor!=1.0) ) ) {
			// apply scale change
			if ( scaleTrack(xFactor,yFactor) )
				scaleChange = true;
			else
				log.error("Error scaling track diagram");
		}		
		// success - dispose of the dialog and repaint if needed
		scaleTrackDiagramOpen = false;
		scaleTrackDiagramFrame.setVisible(false);
		scaleTrackDiagramFrame.dispose();
		scaleTrackDiagramFrame = null;
		if (scaleChange) {
			repaint();
			setDirty(true);
		}
	}
	void scaleTrackDiagramCancelPressed(ActionEvent a) {
		scaleTrackDiagramOpen = false;
		scaleTrackDiagramFrame.setVisible(false);
		scaleTrackDiagramFrame.dispose();
		scaleTrackDiagramFrame = null;
	}
	boolean translateTrack (float xDel, float yDel) {
		// loop over all defined turnouts
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
			Point2D center = t.getCoordsCenter();
			t.setCoordsCenter(new Point2D.Double(center.getX()+xDel,center.getY()+yDel));
		}
		// loop over all defined level crossings
		for (int i = 0; i<xingList.size();i++) {
			LevelXing x = xingList.get(i);
			Point2D center = x.getCoordsCenter();
			x.setCoordsCenter(new Point2D.Double(center.getX()+xDel,center.getY()+yDel));
		}
		// loop over all defined level crossings
		for (int i = 0; i<slipList.size();i++) {
			LayoutSlip x = slipList.get(i);
			Point2D center = x.getCoordsCenter();
			x.setCoordsCenter(new Point2D.Double(center.getX()+xDel,center.getY()+yDel));
		}
		// loop over all defined turntables
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable x = turntableList.get(i);
			Point2D center = x.getCoordsCenter();
			x.setCoordsCenter(new Point2D.Double(center.getX()+xDel,center.getY()+yDel));
		}
		// loop over all defined Anchor Points and End Bumpers
		for (int i = 0; i<pointList.size();i++) {
			PositionablePoint p = pointList.get(i);
			Point2D coord = p.getCoords();
			p.setCoords(new Point2D.Double(coord.getX()+xDel,coord.getY()+yDel));
		}
		return true;
	}
	boolean scaleTrack (float xFactor, float yFactor) {
		// loop over all defined turnouts
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
			t.scaleCoords(xFactor,yFactor);
		}
		// loop over all defined level crossings
		for (int i = 0; i<xingList.size();i++) {
			LevelXing x = xingList.get(i);
			x.scaleCoords(xFactor,yFactor);
		}
		// loop over all defined level crossings
		for (int i = 0; i<slipList.size();i++) {
			LayoutSlip x = slipList.get(i);
			x.scaleCoords(xFactor,yFactor);
		}
		// loop over all defined turntables
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable x = turntableList.get(i);
			x.scaleCoords(xFactor,yFactor);
		}
		// loop over all defined Anchor Points and End Bumpers
		for (int i = 0; i<pointList.size();i++) {
			PositionablePoint p = pointList.get(i);
			Point2D coord = p.getCoords();
			p.setCoords(new Point2D.Double(round(coord.getX()*xFactor),
										round(coord.getY()*yFactor)));
		}
		// update the overall scale factors
		xScale = xScale*xFactor;
		yScale = yScale*yFactor;
		return true;
	}
	double round (double x) {
		int i = (int)(x+0.5);
		return (i);
	}

	// operational variables for move selection pane
	private JmriJFrame moveSelectionFrame = null;
	private boolean moveSelectionOpen = false;
	private JTextField xMoveField = new JTextField(6);
	private JTextField yMoveField = new JTextField(6);
	private JButton moveSelectionDone;
	private JButton moveSelectionCancel;
	private boolean canUndoMoveSelection = false;
	private double undoDeltaX = 0.0;
	private double undoDeltaY = 0.0;
	private Rectangle2D undoRect;

	// display dialog for translation a selection
	protected void moveSelection() {
		if (!selectionActive || (selectionWidth==0.0) || (selectionHeight==0.0) ) {
			// no selection has been made - nothing to move
			JOptionPane.showMessageDialog(this,rb.getString("Error12"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (moveSelectionOpen) {
			moveSelectionFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (moveSelectionFrame == null) {
            moveSelectionFrame = new JmriJFrame( rb.getString("TranslateSelection") );
            moveSelectionFrame.addHelpMenu("package.jmri.jmrit.display.TranslateSelection", true);
            moveSelectionFrame.setLocation(70,30);
            Container theContentPane = moveSelectionFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			// setup x translate
            JPanel panel31 = new JPanel(); 
            panel31.setLayout(new FlowLayout());
			JLabel xMoveLabel = new JLabel( rb.getString("XTranslateLabel"));
            panel31.add(xMoveLabel);
            panel31.add(xMoveField);
            xMoveField.setToolTipText( rb.getString("XTranslateHint") );
            theContentPane.add(panel31);
			// setup y translate
            JPanel panel32 = new JPanel(); 
            panel32.setLayout(new FlowLayout());
			JLabel yMoveLabel = new JLabel( rb.getString("YTranslateLabel"));
            panel32.add(yMoveLabel);
            panel32.add(yMoveField);
            yMoveField.setToolTipText( rb.getString("YTranslateHint") );
            theContentPane.add(panel32);
			// setup information message 
            JPanel panel33 = new JPanel(); 
            panel33.setLayout(new FlowLayout());
			JLabel message1Label = new JLabel( rb.getString("Message3Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);			
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(moveSelectionDone = new JButton(rb.getString("MoveSelection")));
            moveSelectionDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveSelectionDonePressed(e);
                }
            });
            moveSelectionDone.setToolTipText( rb.getString("MoveSelectionHint") );
            panel5.add(moveSelectionCancel = new JButton(rb.getString("Cancel")));
            moveSelectionCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveSelectionCancelPressed(e);
                }
            });
            moveSelectionCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel5);
		}
		// Set up for Entry of Translation
		xMoveField.setText("0");
		yMoveField.setText("0");
		moveSelectionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					moveSelectionCancelPressed(null);
				}
			});
        moveSelectionFrame.pack();
        moveSelectionFrame.setVisible(true);	
		moveSelectionOpen = true;
	}	
	void moveSelectionDonePressed(ActionEvent a) {
		String newText = "";
		float xTranslation = 0.0F;
		float yTranslation = 0.0F;
		// get x translation
		newText = xMoveField.getText().trim();
		try {
			xTranslation = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(moveSelectionFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// get y translation
		newText = yMoveField.getText().trim();
		try {
			yTranslation = Float.parseFloat(newText);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(moveSelectionFrame,rb.getString("EntryError")+": "+
					e+" "+rb.getString("TryAgain"),rb.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
            return;
		}
		// here when all numbers read in - translation if entered
		if ( (xTranslation!=0.0F) || (yTranslation!=0.0F) ) {
			// set up selection rectangle
			Rectangle2D selectRect = new Rectangle2D.Double (selectionX, selectionY, 
															selectionWidth, selectionHeight);
			// set up undo information
			undoRect = new Rectangle2D.Double (selectionX+xTranslation, selectionY+yTranslation, 
															selectionWidth, selectionHeight);
			undoDeltaX = -xTranslation;
			undoDeltaY = -yTranslation;
			canUndoMoveSelection = true;
			// apply translation to icon items within the selection
            List <Positionable> contents = getContents();
			for (int i = 0; i<contents.size(); i++) {
				Positionable c = contents.get(i);
				Point2D upperLeft = c.getLocation();
				if (selectRect.contains(upperLeft)) {
					int xNew = (int)(upperLeft.getX()+xTranslation);
					int yNew = (int)(upperLeft.getY()+yTranslation);
					c.setLocation(xNew,yNew);
				}
 			}
			// loop over all defined turnouts
			for (int i = 0; i<turnoutList.size();i++) {
				LayoutTurnout t = turnoutList.get(i);
				Point2D center = t.getCoordsCenter();
				if (selectRect.contains(center)) {
					t.setCoordsCenter(new Point2D.Double(center.getX()+xTranslation,
																center.getY()+yTranslation));
				}
			}
			// loop over all defined level crossings
			for (int i = 0; i<xingList.size();i++) {
				LevelXing x = xingList.get(i);
				Point2D center = x.getCoordsCenter();
				if (selectRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+xTranslation,
																center.getY()+yTranslation));
				}
			}
			// loop over all defined level crossings
			for (int i = 0; i<slipList.size();i++) {
				LayoutSlip x = slipList.get(i);
				Point2D center = x.getCoordsCenter();
				if (selectRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+xTranslation,
																center.getY()+yTranslation));
				}
			}
			// loop over all defined turntables
			for (int i = 0; i<turntableList.size();i++) {
				LayoutTurntable x = turntableList.get(i);
				Point2D center = x.getCoordsCenter();
				if (selectRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+xTranslation,
																center.getY()+yTranslation));
				}
			}
			// loop over all defined Anchor Points and End Bumpers
			for (int i = 0; i<pointList.size();i++) {
				PositionablePoint p = pointList.get(i);
				Point2D coord = p.getCoords();
				if (selectRect.contains(coord)) {
					p.setCoords(new Point2D.Double(coord.getX()+xTranslation,
																coord.getY()+yTranslation));
				}
			}
			repaint();
			setDirty(true);
		}
		// success - hide dialog 
		moveSelectionOpen = false;
		moveSelectionFrame.setVisible(false);
		moveSelectionFrame.dispose();
		moveSelectionFrame = null;
	}
	void moveSelectionCancelPressed(ActionEvent a) {
		moveSelectionOpen = false;
		moveSelectionFrame.setVisible(false);
		moveSelectionFrame.dispose();
		moveSelectionFrame = null;
	}
	void undoMoveSelection() {
		if (canUndoMoveSelection) {
            List <Positionable> contents = getContents();
			for (int i = 0; i<contents.size(); i++) {
				Positionable c = contents.get(i);
				Point2D upperLeft = c.getLocation();
				if (undoRect.contains(upperLeft)) {
					int xNew = (int)(upperLeft.getX()+undoDeltaX);
					int yNew = (int)(upperLeft.getY()+undoDeltaY);
					c.setLocation(xNew,yNew);
				}
 			}
			for (int i = 0; i<turnoutList.size();i++) {
				LayoutTurnout t = turnoutList.get(i);
				Point2D center = t.getCoordsCenter();
				if (undoRect.contains(center)) {
					t.setCoordsCenter(new Point2D.Double(center.getX()+undoDeltaX,
															center.getY()+undoDeltaY));
				}
			}
			for (int i = 0; i<xingList.size();i++) {
				LevelXing x = xingList.get(i);
				Point2D center = x.getCoordsCenter();
				if (undoRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+undoDeltaX,
																center.getY()+undoDeltaY));
				}
			}
			for (int i = 0; i<slipList.size();i++) {
				LayoutSlip x = slipList.get(i);
				Point2D center = x.getCoordsCenter();
				if (undoRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+undoDeltaX,
																center.getY()+undoDeltaY));
				}
			}
			for (int i = 0; i<turntableList.size();i++) {
				LayoutTurntable x = turntableList.get(i);
				Point2D center = x.getCoordsCenter();
				if (undoRect.contains(center)) {
					x.setCoordsCenter(new Point2D.Double(center.getX()+undoDeltaX,
																center.getY()+undoDeltaY));
				}
			}
			for (int i = 0; i<pointList.size();i++) {
				PositionablePoint p = pointList.get(i);
				Point2D coord = p.getCoords();
				if (undoRect.contains(coord)) {
					p.setCoords(new Point2D.Double(coord.getX()+undoDeltaX,
																coord.getY()+undoDeltaY));
				}
			}
			repaint();
			canUndoMoveSelection = false;
		}
		return;
	}
	
	public void setCurrentPositionAndSize() {
		// save current panel location and size
		Dimension dim = getSize();
		// Compute window size based on LayoutEditor size
		windowHeight = dim.height;
		windowWidth = dim.width;
		// Compute layout size based on LayoutPane size
		dim = getTargetPanelSize();
		panelHeight = (int)(dim.height/getPaintScale());
		panelWidth = (int)(dim.width/getPaintScale());
		Point pt = getLocationOnScreen();
		upperLeftX = pt.x;
		upperLeftY = pt.y;
		log.debug("setCurrentPositionAndSize Position - "+upperLeftX+","+upperLeftY+" WindowSize - "+windowWidth+","+windowHeight+" PanelSize - "+panelWidth+","+panelHeight);	
		setDirty(true);
	}

    void addBackgroundColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				//final String desiredName = name;
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					if (!defaultBackgroundColor.equals(desiredColor)) {
						defaultBackgroundColor = desiredColor;
                        setBackgroundColor(desiredColor);
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        backgroundColorButtonGroup.add(r);
        if (defaultBackgroundColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
		backgroundColorMenuItems[backgroundColorCount] = r;
		backgroundColors[backgroundColorCount] = color;
		backgroundColorCount ++;
    }
    
    void addTrackColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				//final String desiredName = name;
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					if (!defaultTrackColor.equals(desiredColor)) {
						defaultTrackColor = desiredColor;
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        trackColorButtonGroup.add(r);
        if (defaultTrackColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
		trackColorMenuItems[trackColorCount] = r;
		trackColors[trackColorCount] = color;
		trackColorCount ++;
    }
    
    void addTrackOccupiedColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				//final String desiredName = name;
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					if (!defaultOccupiedTrackColor.equals(desiredColor)) {
						defaultOccupiedTrackColor = desiredColor;
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        trackOccupiedColorButtonGroup.add(r);
        if (defaultOccupiedTrackColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
		trackOccupiedColorMenuItems[trackOccupiedColorCount] = r;
		trackOccupiedColors[trackOccupiedColorCount] = color;
		trackOccupiedColorCount ++;
    }
    
    void addTrackAlternativeColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				//final String desiredName = name;
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					if (!defaultAlternativeTrackColor.equals(desiredColor)) {
						defaultAlternativeTrackColor = desiredColor;
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        trackAlternativeColorButtonGroup.add(r);
        if (defaultAlternativeTrackColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
		trackAlternativeColorMenuItems[trackAlternativeColorCount] = r;
		trackAlternativeColors[trackAlternativeColorCount] = color;
		trackAlternativeColorCount ++;
    }
    
	protected void setOptionMenuTrackColor() {
		for (int i = 0;i<trackColorCount;i++) {
			if (trackColors[i].equals(defaultTrackColor)) 
				trackColorMenuItems[i].setSelected(true);
			else 
				trackColorMenuItems[i].setSelected(false);
		}	
        for (int i = 0;i<trackOccupiedColorCount;i++) {
			if (trackOccupiedColors[i].equals(defaultOccupiedTrackColor)) 
				trackOccupiedColorMenuItems[i].setSelected(true);
			else 
				trackOccupiedColorMenuItems[i].setSelected(false);
		}
        for (int i = 0;i<trackAlternativeColorCount;i++) {
			if (trackAlternativeColors[i].equals(defaultAlternativeTrackColor)) 
				trackAlternativeColorMenuItems[i].setSelected(true);
			else 
				trackAlternativeColorMenuItems[i].setSelected(false);
		}
	}
    
    void addTextColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				//final String desiredName = name;
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					if (!defaultTextColor.equals(desiredColor)) {
						defaultTextColor = desiredColor;
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        textColorButtonGroup.add(r);
        if (defaultTextColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
		textColorMenuItems[textColorCount] = r;
		textColors[textColorCount] = color;
		textColorCount ++;
    }

    void addTurnoutCircleColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
				final Color desiredColor = color;
				public void actionPerformed(ActionEvent e) { 
					turnoutCircleColor = desiredColor;
					setDirty(true);
					repaint();
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        turnoutCircleColorButtonGroup.add(r);
        if (turnoutCircleColor.equals(color)) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
        turnoutCircleColorMenuItems[turnoutCircleColorCount] = r;
        turnoutCircleColors[turnoutCircleColorCount] = color;
        turnoutCircleColorCount ++;
    }

    void addTurnoutCircleSizeMenuEntry(JMenu menu, final String name, final int size) {
        ActionListener a = new ActionListener() {
				final int desiredSize = size;
				public void actionPerformed(ActionEvent e) { 
					if (turnoutCircleSize!=desiredSize) {
						turnoutCircleSize = desiredSize;
						setDirty(true);
						repaint();
					}
				}
			};
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        turnoutCircleSizeButtonGroup.add(r);
        if (turnoutCircleSize == size) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
        turnoutCircleSizeMenuItems[turnoutCircleSizeCount] = r;
        turnoutCircleSizes[turnoutCircleSizeCount] = size;
        turnoutCircleSizeCount ++;
    }

    protected void setOptionMenuTurnoutCircleColor() {
		for (int i = 0;i<turnoutCircleColorCount;i++) {
			if (turnoutCircleColors[i] == null && turnoutCircleColor == null)
				turnoutCircleColorMenuItems[i].setSelected(true);
			else if (turnoutCircleColors[i] != null && turnoutCircleColors[i].equals(turnoutCircleColor)) 
				turnoutCircleColorMenuItems[i].setSelected(true);
			else 
				turnoutCircleColorMenuItems[i].setSelected(false);
		}	
	}
    
    protected void setOptionMenuTurnoutCircleSize() {
		for (int i = 0;i<turnoutCircleSizeCount;i++) {
			if (turnoutCircleSizes[i] == turnoutCircleSize) 
				turnoutCircleSizeMenuItems[i].setSelected(true);
			else 
				turnoutCircleSizeMenuItems[i].setSelected(false);
		}	
	}
    

	protected void setOptionMenuTextColor() {
		for (int i = 0;i<textColorCount;i++) {
			if (textColors[i].equals(defaultTextColor)) 
				textColorMenuItems[i].setSelected(true);
			else 
				textColorMenuItems[i].setSelected(false);
		}	
	}    
    
	protected void setOptionMenuBackgroundColor() {
		for (int i = 0;i<backgroundColorCount;i++) {
			if (backgroundColors[i].equals(defaultBackgroundColor)) 
				backgroundColorMenuItems[i].setSelected(true);
			else 
				backgroundColorMenuItems[i].setSelected(false);
		}	
	}    
    
    public void setScroll(int state) {
        if (isEditable()) { 
            //In edit mode the scroll bars are always displayed, however we will want to set the scroll for when we exit edit mode
            super.setScroll(SCROLL_BOTH);
            _scrollState = state;
        } else {
            super.setScroll(state);
        }
    }
	
	/** 
	 * Add a layout turntable at location specified
	 */
	public void addTurntable(Point2D pt) {
		numLayoutTurntables ++;
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "TUR"+numLayoutTurntables;
			if (findLayoutTurntableByName(name)==null) duplicate = false;
			if (duplicate) numLayoutTurntables ++;
		}
		LayoutTurntable x = new LayoutTurntable(name,pt,this);		
		//if (x != null) {
		turntableList.add(x);
		setDirty(true);
		//}
		x.addRay(0.0);
		x.addRay(90.0);
		x.addRay(180.0);
		x.addRay(270.0);	
	}
	
	/**
	 * Allow external trigger of re-draw
	 */
	public void redrawPanel() { 
		repaint(); 
	}
	
	/**
	 * Allow external set/reset of awaitingIconChange
	 */
	public void setAwaitingIconChange() { 
		awaitingIconChange = true;
	}
	public void resetAwaitingIconChange() { 
		awaitingIconChange = false;
	}
	
	/**
	 * Allow external reset of dirty bit
	 */
	public void resetDirty() { 
		setDirty(false);
		savedEditMode = isEditable();
		savedPositionable = allPositionable();
		savedControlLayout = allControlling();
		savedAnimatingLayout = animatingLayout;					
		savedShowHelpBar = showHelpBar;
	}

	/**
	 * Allow external set of dirty bit
	 */
	public void setDirty(boolean val) { panelChanged = val; }	
	public void setDirty() { setDirty(true); }
	
	/**
	 * Check the dirty state
	 */
	public boolean isDirty() { return panelChanged; }
	
	/*
	 * Get mouse coordinates and adjust for zoom
	 */
	private void calcLocation(MouseEvent event, int dX, int dY) {
		xLoc = (int)((event.getX() + dX)/getPaintScale());
		yLoc = (int)((event.getY() + dY)/getPaintScale());
		dLoc.setLocation(xLoc,yLoc);
	}
	
	/**
	 * Handle a mouse pressed event
     */
    public void mousePressed(MouseEvent event) 
    {
		// initialize cursor position
        _anchorX = xLoc;
        _anchorY = yLoc;
        _lastX = _anchorX;
        _lastY = _anchorY;
		calcLocation(event,0,0);
        if (isEditable()) {
			boolean prevSelectionActive = selectionActive;
			selectionActive = false;
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
			if (event.isPopupTrigger()) {
				if (event.isMetaDown() || event.isAltDown()) { 
					// if requesting a popup and it might conflict with moving, delay the request to mouseReleased
					delayedPopupTrigger = true;
				}
				else {
					// no possible conflict with moving, display the popup now
					checkPopUp(event);
				}
			}
			if (event.isMetaDown() || event.isAltDown()) {	
				// if moving an item, identify the item for mouseDragging
				selectedObject = null;
				selectedPointType = NONE;
				if (checkSelect(dLoc, false)) {
					selectedObject = foundObject;
					selectedPointType = foundPointType;
					//selectedNeedsConnect = foundNeedsConnect;
					startDel.setLocation(foundLocation.getX()-dLoc.getX(), foundLocation.getY()-dLoc.getY());
					foundObject = null;
				}
				else {
					selectedObject = checkMarkers(dLoc);
					if (selectedObject!=null) {
						selectedPointType = MARKER;
						startDel.setLocation((((LocoIcon)selectedObject).getX()-dLoc.getX()), 
												(((LocoIcon)selectedObject).getY()-dLoc.getY()));
						//selectedNeedsConnect = false;
					}
					else {
						selectedObject = checkClocks(dLoc);
						if (selectedObject!=null) {
							selectedPointType = LAYOUT_POS_JCOMP;
							startDel.setLocation((((PositionableJComponent)selectedObject).getX()-dLoc.getX()), 
												(((PositionableJComponent)selectedObject).getY()-dLoc.getY()));
							//selectedNeedsConnect = false;
						}
						else {
							selectedObject = checkMultiSensors(dLoc);
							if (selectedObject!=null) {
								selectedPointType = MULTI_SENSOR;
								startDel.setLocation((((MultiSensorIcon)selectedObject).getX()-dLoc.getX()), 
												(((MultiSensorIcon)selectedObject).getY()-dLoc.getY()));
								//selectedNeedsConnect = false;
							}
						}
					}
					if (selectedObject==null) {
						selectedObject = checkSensorIcons(dLoc);
						if (selectedObject==null) {
							selectedObject = checkSignalHeadIcons(dLoc);
							if (selectedObject==null) {
								selectedObject = checkLabelImages(dLoc);
                                if(selectedObject==null) {
                                    selectedObject = checkSignalMastIcons(dLoc);
                                }
							}
						}
						if (selectedObject!=null) {
							selectedPointType = LAYOUT_POS_LABEL;
							startDel.setLocation((((PositionableLabel)selectedObject).getX()-dLoc.getX()), 
												(((PositionableLabel)selectedObject).getY()-dLoc.getY()));
                            if (selectedObject instanceof MemoryIcon) {
                                MemoryIcon pm = (MemoryIcon) selectedObject;
                                if (pm.getPopupUtility().getFixedWidth()==0){
                                    startDel.setLocation((pm.getOriginalX()-dLoc.getX()), 
                                                        (pm.getOriginalY()-dLoc.getY()));
                                }
                            }

							//selectedNeedsConnect = false;

						}
						else {
							selectedObject = checkBackgrounds(dLoc);
							if (selectedObject!=null) {
								selectedPointType = LAYOUT_POS_LABEL;
								startDel.setLocation((((PositionableLabel)selectedObject).getX()-dLoc.getX()), 
													(((PositionableLabel)selectedObject).getY()-dLoc.getY()));
								//selectedNeedsConnect = false;
							}
						}
					}					
				}
			}
			else if (event.isShiftDown() && trackBox.isSelected() && (!event.isPopupTrigger()) ) {
				// starting a Track Segment, check for free connection point
				selectedObject = null;
				if (checkSelect(dLoc, true)) {
					// match to a free connection point
					beginObject = foundObject;
					beginPointType = foundPointType;
					beginLocation = foundLocation;
				}
				else {
					foundObject = null;
					beginObject = null;
				}
			}
			else if ( (!event.isShiftDown()) && (!event.isControlDown()) && (!event.isPopupTrigger()) ) {
			// check if controlling a turnout in edit mode
				selectedObject = null;
				if (allControlling()) {
					// check if mouse is on a turnout 
					selectedObject = null;
					for (int i = 0; i<turnoutList.size();i++) {
						LayoutTurnout t = turnoutList.get(i);
						// check the center point
						Point2D pt = t.getCoordsCenter();
						Rectangle2D r = new Rectangle2D.Double(
								pt.getX()-SIZE2,pt.getY()-SIZE2,2.0*SIZE2,2.0*SIZE2);
						if (r.contains(dLoc)) {
							// mouse was pressed on this turnout
							selectedObject = t;
							selectedPointType = TURNOUT_CENTER;
							break;
						}
					}
                    for(LayoutSlip sl : slipList){
                        // check the center point
                        Point2D pt = sl.getCoordsCenter();
                        Rectangle2D r = new Rectangle2D.Double(
                                pt.getX()-(SIZE2*2.0),pt.getY()-(SIZE2*2.0),4.0*SIZE2,4.0*SIZE2);
                        if (r.contains(dLoc)) {
                            // mouse was pressed on this turnout
                            selectedObject = sl;
                            selectedPointType = SLIP_CENTER;
                            break;
                        }
                    }
                    for (int i = 0; i<turntableList.size();i++) {
                        LayoutTurntable x = turntableList.get(i);
                        for (int k = 0; k<x.getNumberRays(); k++) {
                            if (x.getRayConnectOrdered(k)!=null) {
                                // check the A connection point
                                Point2D pt = x.getRayCoordsOrdered(k);
                                Rectangle2D r = new Rectangle2D.Double(
                                        pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
                                if (r.contains(dLoc)) {
                                    // mouse was pressed on this connection point
                                    selectedObject = x;
                                    selectedPointType = TURNTABLE_RAY_OFFSET+x.getRayIndex(k);
                                    break;
                                }
                            }
                        }
                    }
				}
				// initialize starting selection - cancel any previous selection rectangle
				selectionActive = true;
				selectionX = dLoc.getX();
				selectionY = dLoc.getY();
				selectionWidth = 0.0;
				selectionHeight = 0.0;
			}
			if (prevSelectionActive) repaint();	
        }
		
		else if (allControlling() && (!event.isMetaDown()) && (!event.isPopupTrigger()) && 
						(!event.isAltDown()) &&(!event.isShiftDown()) && (!event.isControlDown()) ) {
			// not in edit mode - check if mouse is on a turnout (using wider search range)
			selectedObject = null;
			for (int i = 0; i<turnoutList.size();i++) {
				LayoutTurnout t = turnoutList.get(i);
				// check a rectangle as large as turnout circle, but at least size 4
				Point2D pt = t.getCoordsCenter();
                double size = SIZE * turnoutCircleSize;
                if (size < SIZE2*2.0) size = SIZE2*2.0;
				Rectangle2D r = new Rectangle2D.Double(
						pt.getX()-size, pt.getY()-size, size+size, size+size);
				if (r.contains(dLoc)) {
					// mouse was pressed on this turnout
					selectedObject = t;
					selectedPointType = TURNOUT_CENTER;
					break;
				}
			}
            for(LayoutSlip sl : slipList){
				// check the center point
				Point2D pt = sl.getCoordsCenter();
				Rectangle2D r = new Rectangle2D.Double(
						pt.getX()-(SIZE2*2.0),pt.getY()-(SIZE2*2.0),4.0*SIZE2,4.0*SIZE2);
				if (r.contains(dLoc)) {
					// mouse was pressed on this turnout
					selectedObject = sl;
					selectedPointType = SLIP_CENTER;
					break;
				}
			}
            for (int i = 0; i<turntableList.size();i++) {
                LayoutTurntable x = turntableList.get(i);
                for (int k = 0; k<x.getNumberRays(); k++) {
                    if (x.getRayConnectOrdered(k)!=null) {
                        // check the A connection point
                        Point2D pt = x.getRayCoordsOrdered(k);
                        Rectangle2D r = new Rectangle2D.Double(
                                pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
                        if (r.contains(dLoc)) {
                            // mouse was pressed on this connection point
                            selectedObject = x;
                            selectedPointType = TURNTABLE_RAY_OFFSET+x.getRayIndex(k);
                            break;
                        }
                    }
                }
            }
		}
		else if ( (event.isMetaDown() || event.isAltDown()) &&
							(!event.isShiftDown()) && (!event.isControlDown()) ) {
			// not in edit mode - check if moving a marker if there are any
			selectedObject = checkMarkers(dLoc);
			if (selectedObject!=null) {
				selectedPointType = MARKER;
				startDel.setLocation((((LocoIcon)selectedObject).getX()-dLoc.getX()), 
												(((LocoIcon)selectedObject).getY()-dLoc.getY()));
				//selectedNeedsConnect = false;
			}
		}
		else if ( event.isPopupTrigger() && (!event.isShiftDown()) ) {
			// not in edit mode - check if a marker popup menu is being requested
			LocoIcon lo = checkMarkers(dLoc);
			if (lo!=null) delayedPopupTrigger = true;
		}
        if (!event.isPopupTrigger() && !isDragging) {
            List <Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                selections.get(0).doMousePressed(event);
            }
        }
        //thisPanel.setFocusable(true);
        thisPanel.requestFocusInWindow();

        return;
    }
	
	private boolean checkSelect(Point2D loc, boolean requireUnconnected) {
		// check positionable points, if any
		for (int i = 0; i<pointList.size();i++) {
			PositionablePoint p = pointList.get(i);
			if ( (p!=selectedObject) && !requireUnconnected || 
					(p.getConnect1()==null) || 
					((p.getType()!=PositionablePoint.END_BUMPER) && 
												(p.getConnect2()==null)) ) {
				Point2D pt = p.getCoords();
				Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
				if (r.contains(loc)) {
					// mouse was pressed on this connection point
					foundLocation = pt;
					foundObject = p;
					foundPointType = POS_POINT;
					foundNeedsConnect = ((p.getConnect1()==null)||(p.getConnect2()==null));
					return true;
				}
			}
		}
		// check turnouts, if any
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
			if (t!=selectedObject) {
				if (!requireUnconnected) {
					// check the center point
					Point2D pt = t.getCoordsCenter();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE2,pt.getY() - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = t;
						foundPointType = TURNOUT_CENTER;
						foundNeedsConnect = false;
						return true;
					}
				}
				if (!requireUnconnected || (t.getConnectA()==null)) {
					// check the A connection point
					Point2D pt = t.getCoordsA();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = t;
						foundPointType = TURNOUT_A;
						foundNeedsConnect = (t.getConnectA()==null);
						return true;
					}
				}
				if (!requireUnconnected || (t.getConnectB()==null)) {
					// check the B connection point
					Point2D pt = t.getCoordsB();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = t;
						foundPointType = TURNOUT_B;
						foundNeedsConnect = (t.getConnectB()==null);
						return true;
					}
				}
				if (!requireUnconnected || (t.getConnectC()==null)) {
					// check the C connection point
					Point2D pt = t.getCoordsC();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = t;
						foundPointType = TURNOUT_C;
						foundNeedsConnect = (t.getConnectC()==null);
						return true;
					}
				}
				if (( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
						(t.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
						(t.getTurnoutType()==LayoutTurnout.LH_XOVER) ) && (
						!requireUnconnected || (t.getConnectD()==null))) {
					// check the D connection point, double crossover turnouts only
					Point2D pt = t.getCoordsD();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = t;
						foundPointType = TURNOUT_D;
						foundNeedsConnect = (t.getConnectD()==null);
						return true;
					}
				}
			}
		}
				
		// check level Xings, if any
		for (int i = 0; i<xingList.size();i++) {
			LevelXing x = xingList.get(i);
			if (x!=selectedObject) {
				if (!requireUnconnected) {
					// check the center point
					Point2D pt = x.getCoordsCenter();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE2,pt.getY() - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = LEVEL_XING_CENTER;
						foundNeedsConnect = false;
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectA()==null)) {
					// check the A connection point
					Point2D pt = x.getCoordsA();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = LEVEL_XING_A;
						foundNeedsConnect = (x.getConnectA()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectB()==null)) {
					// check the B connection point
					Point2D pt = x.getCoordsB();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = LEVEL_XING_B;
						foundNeedsConnect = (x.getConnectB()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectC()==null)) {
					// check the C connection point
					Point2D pt = x.getCoordsC();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = LEVEL_XING_C;
						foundNeedsConnect = (x.getConnectC()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectD()==null)) {
					// check the D connection point
					Point2D pt = x.getCoordsD();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = LEVEL_XING_D;
						foundNeedsConnect = (x.getConnectD()==null);
						return true;
					}
				}
			}
		}
        
		// check level Xings, if any
        for(LayoutSlip x: slipList){
			if (x!=selectedObject) {
				if (!requireUnconnected) {
					// check the center point
					Point2D pt = x.getCoordsCenter();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE2,pt.getY() - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = SLIP_CENTER;
						foundNeedsConnect = false;
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectA()==null)) {
					// check the A connection point
					Point2D pt = x.getCoordsA();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = SLIP_A;
						foundNeedsConnect = (x.getConnectA()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectB()==null)) {
					// check the B connection point
					Point2D pt = x.getCoordsB();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = SLIP_B;
						foundNeedsConnect = (x.getConnectB()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectC()==null)) {
					// check the C connection point
					Point2D pt = x.getCoordsC();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = SLIP_C;
						foundNeedsConnect = (x.getConnectC()==null);
						return true;
					}
				}
				if (!requireUnconnected || (x.getConnectD()==null)) {
					// check the D connection point
					Point2D pt = x.getCoordsD();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this connection point
						foundLocation = pt;
						foundObject = x;
						foundPointType = SLIP_D;
						foundNeedsConnect = (x.getConnectD()==null);
						return true;
					}
				}
			}
		}
		// check turntables, if any
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable x = turntableList.get(i);
			if (x!=selectedObject) {
				if (!requireUnconnected) {
					// check the center point
					Point2D pt = x.getCoordsCenter();
					Rectangle2D r = new Rectangle2D.Double(
							pt.getX() - SIZE2,pt.getY() - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
					if (r.contains(loc)) {
						// mouse was pressed on this center point
						foundLocation = pt;
						foundObject = x;
						foundPointType = TURNTABLE_CENTER;
						foundNeedsConnect = false;
						return true;
					}
				}
				for (int k = 0; k<x.getNumberRays(); k++) {
					if (!requireUnconnected || (x.getRayConnectOrdered(k)==null)) {
						Point2D pt = x.getRayCoordsOrdered(k);
						Rectangle2D r = new Rectangle2D.Double(
								pt.getX() - SIZE,pt.getY() - SIZE,SIZE2,SIZE2);
						if (r.contains(loc)) {
							// mouse was pressed on this connection point
							foundLocation = pt;
							foundObject = x;
							foundPointType = TURNTABLE_RAY_OFFSET+x.getRayIndex(k);
							foundNeedsConnect = (x.getRayConnectOrdered(k)==null);
							return true;
						}
					}
				}
			}
		}
		
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
            if (t.getCircle()){
                Point2D pt = t.getCoordsCenterCircle();
                Rectangle2D r = new Rectangle2D.Double(
                        pt.getX() - SIZE2,pt.getY() - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
                if (r.contains(loc)) {
                    // mouse was pressed on this connection point
                    foundLocation = pt;
                    foundObject = t;
                    foundPointType = TRACK_CIRCLE_CENTRE;
                    foundNeedsConnect = false;
                    return true;
                }
            }
		}
		// no connection point found
		foundObject = null;
		return false;
	}
	
	private TrackSegment checkTrackSegments(Point2D loc) {
		// check Track Segments, if any
		for (int i = 0; i<trackList.size(); i++) {
			TrackSegment tr = trackList.get(i);
			Object o = tr.getConnect1();
			int type = tr.getType1();
			// get coordinates of first end point
			Point2D pt1 = getEndCoords(o,type);
			o = tr.getConnect2();
			type = tr.getType2();
			// get coordinates of second end point
			Point2D pt2 = getEndCoords(o,type);
			// construct a detection rectangle
			double cX = (pt1.getX() + pt2.getX())/2.0D;
			double cY = (pt1.getY() + pt2.getY())/2.0D;			
			Rectangle2D r = new Rectangle2D.Double(
						cX - SIZE2,cY - SIZE2,SIZE2+SIZE2,SIZE2+SIZE2);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in detection rectangle
				return tr;
			}
		}
		return null;
	}
	
	private PositionableLabel checkBackgrounds(Point2D loc) {
		// check background images, if any
		for (int i=backgroundImage.size()-1; i>=0; i--) {
			PositionableLabel b = backgroundImage.get(i);
			double x = b.getX();
			double y = b.getY();
			double w = b.maxWidth();
			double h = b.maxHeight();			
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in background image
				return b;
			}
		}
		return null;
	}
	
	private SensorIcon checkSensorIcons(Point2D loc) {
		// check sensor images, if any
 		for (int i=sensorImage.size()-1; i>=0; i--) {
			SensorIcon s = sensorImage.get(i);
			double x = s.getX();
			double y = s.getY();
			double w = s.maxWidth();
			double h = s.maxHeight();			
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in sensor icon image
				return s;
			}
		}
		return null;
	}
	
	private SignalHeadIcon checkSignalHeadIcons(Point2D loc) {
		// check signal head images, if any
		for (int i=signalHeadImage.size()-1; i>=0; i--) {
			SignalHeadIcon s = signalHeadImage.get(i);
			double x = s.getX();
			double y = s.getY();
			double w = s.maxWidth();
			double h = s.maxHeight();			
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in signal head image
				return s;
			}
		}
		return null;
	}
    
    private SignalMastIcon checkSignalMastIcons(Point2D loc) {
		// check signal head images, if any
		for (int i=signalMastImage.size()-1; i>=0; i--) {
			SignalMastIcon s = signalMastImage.get(i);
			double x = s.getX();
			double y = s.getY();
			double w = s.maxWidth();
			double h = s.maxHeight();			
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in signal head image
				return s;
			}
		}
		return null;
	}
		
	private PositionableLabel checkLabelImages(Point2D loc) {
           PositionableLabel l =null;
           int level = 0;
           for (int i=labelImage.size()-1; i>=0; i--) {
                   PositionableLabel s = labelImage.get(i);
                   double x = s.getX();
                   double y = s.getY();
                   double w = 10.0;
                   double h = 5.0;
                   if (s.isIcon() || s.isRotated()) {
                           w = s.maxWidth();
                           h = s.maxHeight();
                   }
                   else if (s.isText()) {
                           h = s.getFont().getSize();
                           w = (h*2*(s.getText().length()))/3;
                   }

                   Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
                   // Test this detection rectangle
                   if (r.contains(loc)) {
                       // mouse was pressed in label image
                       if (s.getDisplayLevel()>=level){
                       //Check to make sure that we are returning the highest level label.
                           l = s;
                           level = s.getDisplayLevel();
                       }
                   }
            }
            return l;
	}
	
	private AnalogClock2Display checkClocks(Point2D loc) {
		// check clocks, if any
		for (int i=clocks.size()-1; i>=0; i--) {
			AnalogClock2Display s = clocks.get(i);
			double x = s.getX();
			double y = s.getY();
			double w = s.getFaceWidth();
			double h = s.getFaceHeight();
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in clock image
				return s;
			}
		}
		return null;
	}
	
	private MultiSensorIcon checkMultiSensors(Point2D loc) {
		// check multi sensor icons, if any
		for (int i=multiSensors.size()-1; i>=0; i--) {
			MultiSensorIcon s = multiSensors.get(i);
			double x = s.getX();
			double y = s.getY();
			double w = s.maxWidth();
			double h = s.maxHeight();
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in multi sensor image
				return s;
			}
		}
		return null;
	}
	
	private LocoIcon checkMarkers(Point2D loc) {
		// check marker icons, if any
		for (int i=markerImage.size()-1; i>=0; i--) {
			LocoIcon l = markerImage.get(i);
			double x = l.getX();
			double y = l.getY();
			double w = l.maxWidth();
			double h = l.maxHeight();
			Rectangle2D r = new Rectangle2D.Double(x ,y ,w ,h);
			// Test this detection rectangle
			if (r.contains(loc)) {
				// mouse was pressed in marker icon
				return l;
			}
		}
		return null;
	}

	public Point2D getEndCoords(Object o, int type) {
		switch (type) {
			case POS_POINT:
				return ((PositionablePoint)o).getCoords();
			case TURNOUT_A:
				return ((LayoutTurnout)o).getCoordsA();
			case TURNOUT_B:
				return ((LayoutTurnout)o).getCoordsB();
			case TURNOUT_C:
				return ((LayoutTurnout)o).getCoordsC();
			case TURNOUT_D:
				return ((LayoutTurnout)o).getCoordsD();
			case LEVEL_XING_A:
				return ((LevelXing)o).getCoordsA();
			case LEVEL_XING_B:
				return ((LevelXing)o).getCoordsB();
			case LEVEL_XING_C:
				return ((LevelXing)o).getCoordsC();
			case LEVEL_XING_D:
				return ((LevelXing)o).getCoordsD();
			case SLIP_A:
				return ((LayoutSlip)o).getCoordsA();
			case SLIP_B:
				return ((LayoutSlip)o).getCoordsB();
			case SLIP_C:
				return ((LayoutSlip)o).getCoordsC();
			case SLIP_D:
				return ((LayoutSlip)o).getCoordsD();
			default: 
				if (type>=TURNTABLE_RAY_OFFSET) {
					return ((LayoutTurntable)o).getRayCoordsIndexed(type-TURNTABLE_RAY_OFFSET);
				}
		}
		return (new Point2D.Double(0.0,0.0));
	}			

    public void mouseReleased(MouseEvent event)
    {   
        super.setToolTip(null);
		// initialize mouse position
		calcLocation(event, 0, 0);
        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
            if ((!event.isPopupTrigger()) && (!event.isMetaDown()) && (!event.isAltDown()) 
												&& event.isShiftDown()) {
				currentPoint = new Point2D.Double(xLoc, yLoc);
				if (snapToGridOnAdd) {
					xLoc = ((xLoc+4)/10)*10;
					yLoc = ((yLoc+4)/10)*10;
					currentPoint.setLocation(xLoc,yLoc);
				}
                if (turnoutRHBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.RH_TURNOUT);
                }
                else if (turnoutLHBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.LH_TURNOUT);
                }
                else if (turnoutWYEBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.WYE_TURNOUT);
                }
                else if (doubleXoverBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.DOUBLE_XOVER);
                }
                else if (rhXoverBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.RH_XOVER);
                }
                else if (lhXoverBox.isSelected()) {
					addLayoutTurnout(LayoutTurnout.LH_XOVER);
                }
                else if (levelXingBox.isSelected()) {
					addLevelXing();
                }
                else if (layoutSingleSlipBox.isSelected()) {
					addLayoutSlip(LayoutSlip.SINGLE_SLIP);
                }
                else if (layoutDoubleSlipBox.isSelected()) {
					addLayoutSlip(LayoutSlip.DOUBLE_SLIP);
                }
                else if (endBumperBox.isSelected()) {
					addEndBumper();
                }
                else if (anchorBox.isSelected()) {
					addAnchor();
                }
                else if (trackBox.isSelected()) {
					if ( (beginObject!=null) && (foundObject!=null) &&
							(beginObject!=foundObject) ) {
						addTrackSegment();
						setCursor(Cursor.getDefaultCursor());
					}
					beginObject = null;
					foundObject = null;
                }
                else if (multiSensorBox.isSelected()) {
                    startMultiSensor();
                }
                else if (sensorBox.isSelected()) {
                    addSensor();
                }
                else if (signalBox.isSelected()) {
                    addSignalHead();
                }
                else if (textLabelBox.isSelected()) {
                    addLabel();
                }
                else if (memoryBox.isSelected()) {
                    addMemory();
                }
                else if (iconLabelBox.isSelected()) {
                    addIcon();
                }
                else if (signalMastBox.isSelected()) {
                    addSignalMast();
                }
                else {
                    log.warn("No item selected in panel edit mode");
                }
				selectedObject = null;
                repaint();
            }
			else if ( (event.isPopupTrigger() || delayedPopupTrigger)  && !isDragging) {
				selectedObject = null;
				selectedPointType = NONE;
				whenReleased = event.getWhen();
				checkPopUp(event);
			}
			// check if controlling turnouts
			else if ( ( selectedObject!=null) && (selectedPointType==TURNOUT_CENTER) && 
					allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
						(!event.isShiftDown()) && (!event.isControlDown()) ) {
				// controlling layout, in edit mode
				LayoutTurnout t = (LayoutTurnout)selectedObject;
				t.toggleTurnout();
			}
            else if ( ( selectedObject!=null) && (selectedPointType==SLIP_CENTER) && 
					allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
						(!event.isShiftDown()) && (!event.isControlDown()) ) {
				// controlling layout, in edit mode
                LayoutSlip t = (LayoutSlip)selectedObject;
                t.toggleState();
			}
            else if ( ( selectedObject!=null) && (selectedPointType>=TURNTABLE_RAY_OFFSET) && 
					allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
						(!event.isShiftDown()) && (!event.isControlDown()) ) {
				// controlling layout, in edit mode
                LayoutTurntable t =  (LayoutTurntable)selectedObject;
                t.setPosition(selectedPointType-TURNTABLE_RAY_OFFSET);
			}
			if ( (trackBox.isSelected()) && (beginObject!=null) && (foundObject!=null) ) {
				// user let up shift key before releasing the mouse when creating a track segment
				setCursor(Cursor.getDefaultCursor());
				beginObject = null;
				foundObject = null;
				repaint();
			}
            createSelectionGroups();
        }
		// check if controlling turnouts out of edit mode
		else if ( ( selectedObject!=null) && (selectedPointType==TURNOUT_CENTER) && 
				allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
					(!event.isShiftDown()) && (!delayedPopupTrigger) ) {
			// controlling layout, not in edit mode
			LayoutTurnout t = (LayoutTurnout)selectedObject;
			t.toggleTurnout();
		}
        // check if controlling turnouts out of edit mode
        else if ( ( selectedObject!=null) && (selectedPointType==SLIP_CENTER) && 
				allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
					(!event.isShiftDown()) && (!delayedPopupTrigger) ) {
			// controlling layout, not in edit mode
			LayoutSlip t = (LayoutSlip)selectedObject;
			t.toggleState();
		}
        else if ( ( selectedObject!=null) && (selectedPointType>=TURNTABLE_RAY_OFFSET) && 
				allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) && 
					(!event.isShiftDown()) && (!delayedPopupTrigger) ) {
                LayoutTurntable t =  (LayoutTurntable)selectedObject;
                t.setPosition(selectedPointType-TURNTABLE_RAY_OFFSET);
        }
		// check if requesting marker popup out of edit mode
		else if ( (event.isPopupTrigger() || delayedPopupTrigger) && (!isDragging) ) {
			LocoIcon lo = checkMarkers(dLoc);
			if (lo!=null) showPopUp(lo, event);
            else {
                if (checkSelect(dLoc, false)) {
                    // show popup menu
                    switch (foundPointType) {
                        case TURNOUT_CENTER:
                            ((LayoutTurnout)foundObject).showPopUp(event, isEditable());
                            break;
                        case LEVEL_XING_CENTER:
                            ((LevelXing)foundObject).showPopUp(event, isEditable());
                            break;
                        case SLIP_CENTER:
                            ((LayoutSlip)foundObject).showPopUp(event, isEditable());
                            break;
                        default: break;
                    }
                }
                AnalogClock2Display c = checkClocks(dLoc);
                if (c!=null){
                    showPopUp(c, event);
                } else {
                    SignalMastIcon sm = checkSignalMastIcons(dLoc);
                    if (sm!=null) {
                        showPopUp(sm, event);
                    } else {
                        PositionableLabel im = checkLabelImages(dLoc);
                        if(im!=null){
                            showPopUp(im, event);
                        }
                    }
                }
            }
		}
        if (!event.isPopupTrigger() && !isDragging) {
            List <Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                selections.get(0).doMouseReleased(event);
                whenReleased = event.getWhen();
            }
        }
		if (selectedObject!=null) {
			// An object was selected, deselect it
			prevSelectedObject = selectedObject;
			selectedObject = null;
		}
		isDragging = false;
		delayedPopupTrigger = false;
        thisPanel.requestFocusInWindow();
        return;
    }
	
	private void checkPopUp(MouseEvent event) {
		if (checkSelect(dLoc, false)) {
			// show popup menu
			switch (foundPointType) {
				case POS_POINT:
					((PositionablePoint)foundObject).showPopUp(event);
					break;
				case TURNOUT_CENTER:
					((LayoutTurnout)foundObject).showPopUp(event, isEditable());
					break;
				case LEVEL_XING_CENTER:
					((LevelXing)foundObject).showPopUp(event, isEditable());
					break;
				case SLIP_CENTER:
					((LayoutSlip)foundObject).showPopUp(event, isEditable());
					break;
				case TURNTABLE_CENTER:
					((LayoutTurntable)foundObject).showPopUp(event);
					break;
                default: break;
			}
            if(foundPointType>=TURNTABLE_RAY_OFFSET){
                LayoutTurntable t = (LayoutTurntable)foundObject;
                if(t.isTurnoutControlled()){
                    ((LayoutTurntable)foundObject).showRayPopUp(event, foundPointType-TURNTABLE_RAY_OFFSET);
                }
            }
		}
		else {
			TrackSegment tr = checkTrackSegments(dLoc);
			if (tr!=null) {
				tr.showPopUp(event);
			}
			else {
				SensorIcon s = checkSensorIcons(dLoc);
				if (s!=null) {
					showPopUp(s, event);
				}
				else {
					LocoIcon lo = checkMarkers(dLoc);
					if (lo!=null) {
						showPopUp(lo, event);
					}
					else {
						SignalHeadIcon sh = checkSignalHeadIcons(dLoc);
						if (sh!=null) {
							showPopUp(sh, event);
						}
						else {
							AnalogClock2Display c = checkClocks(dLoc);
							if (c!=null) {
								showPopUp(c, event);
							}
							else {
								MultiSensorIcon ms = checkMultiSensors(dLoc);
								if (ms!=null) {
									showPopUp(ms, event);
								}
								else {
									PositionableLabel lb = checkLabelImages(dLoc);
									if (lb!=null) {
										showPopUp(lb, event);
									}
									else {
										PositionableLabel b = checkBackgrounds(dLoc);
										if (b!=null) {
											showPopUp(b, event);
										}
                                        else {
                                            SignalMastIcon sm = checkSignalMastIcons(dLoc);
                                            if (sm!=null) {
                                                showPopUp(sm, event);
                                            }
                                        }
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
    /**
    * Select the menu items to display for the Positionable's popup 
    */
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent)p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            if(showAlignPopup()){
                setShowAlignmentMenu(popup);
                popup.add(new AbstractAction(rb.getString("Remove")) {
                    public void actionPerformed(ActionEvent e) {
                        deleteSelectedItems();
					}
                }
                );
            } else {
                if (p.doViemMenu()) {
                    popup.add(p.getNameString());
                    if (p.isPositionable()) {
                        setShowCoordinatesMenu(p, popup);
                    }
                    setDisplayLevelMenu(p, popup);
                    setPositionableMenu(p, popup);
                }

                boolean popupSet =false;
                popupSet = p.setRotateOrthogonalMenu(popup);
                popupSet = p.setRotateMenu(popup);
                if (popupSet) { 
                    popup.addSeparator();
                    popupSet = false;
                }
                popupSet = p.setEditIconMenu(popup);
                popupSet = p.setTextEditMenu(popup);

                PositionablePopupUtil util = p.getPopupUtility();
                if (util!=null) {
                    util.setFixedTextMenu(popup);
                    util.setTextMarginMenu(popup);
                    util.setTextBorderMenu(popup);
                    util.setTextFontMenu(popup);
                    util.setBackgroundMenu(popup);
                    util.setTextJustificationMenu(popup);
                    util.setTextOrientationMenu(popup);
                    popup.addSeparator();
                    util.propertyUtil(popup);
                    util.setAdditionalEditPopUpMenu(popup);
                    popupSet = true;
                }
                if (popupSet) { 
                    popup.addSeparator();
                    popupSet = false;
                }
                p.setDisableControlMenu(popup);
                setShowAlignmentMenu(popup);
                // for Positionables with unique settings
                p.showPopUp(popup);
                setShowTooltipMenu(p, popup);

                setRemoveMenu(p, popup);
                if (p.doViemMenu()) {
                    setHiddenMenu(p, popup);
                }
            }
        } else {
            p.showPopUp(popup);
            PositionablePopupUtil util = p.getPopupUtility();
            if(util!=null)
                util.setAdditionalViewPopUpMenu(popup);
        }        
        popup.show((Component)p, p.getWidth()/2+(int)((getPaintScale()-1.0)*p.getX()),
                    p.getHeight()/2+(int)((getPaintScale()-1.0)*p.getY()));
        /*popup.show((Component)p, event.getX(), event.getY());*/
    }

	private long whenReleased = 0;  // used to identify event that was popup trigger
	private boolean awaitingIconChange = false;
    public void mouseClicked(MouseEvent event)
	{
		if ( (!event.isMetaDown()) && (!event.isPopupTrigger()) && (!event.isAltDown()) &&
					(!awaitingIconChange) && (!event.isShiftDown()) && (!event.isControlDown()) ) {
			calcLocation(event, 0, 0);
            List <Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                selections.get(0).doMouseClicked(event);
            }
		}
		else if ( event.isPopupTrigger() && whenReleased != event.getWhen()) {
			calcLocation(event, 0, 0);
			if (isEditable()) {
				selectedObject = null;
				selectedPointType = NONE;
				checkPopUp(event);
			}
			else {
				LocoIcon lo = checkMarkers(dLoc);
				if (lo!=null) showPopUp(lo, event);
			}		
		}
        if (event.isControlDown() && !event.isPopupTrigger()){
            if (checkSelect(dLoc, false)) {
			// show popup menu
                switch (foundPointType) {
                    case POS_POINT:
                        amendSelectionGroup((PositionablePoint)foundObject);
                        break;
                    case TURNOUT_CENTER:
                        amendSelectionGroup((LayoutTurnout)foundObject);
                        break;
                    case LEVEL_XING_CENTER:
                        amendSelectionGroup((LevelXing)foundObject);
                        break;
                    case SLIP_CENTER:
                        amendSelectionGroup((LayoutSlip)foundObject);
                        break;
                    case TURNTABLE_CENTER:
                        amendSelectionGroup((LayoutTurntable)foundObject);
                        break;
                    default: break;
                }
            } else {
            
                PositionableLabel s = checkSensorIcons(dLoc);
				if (s!=null) {
					amendSelectionGroup(s);
				}
                else {
                    PositionableLabel sh = checkSignalHeadIcons(dLoc);
                    if (sh!=null) {
                        amendSelectionGroup(sh);
                    }
                    else {
                        PositionableLabel ms = checkMultiSensors(dLoc);
                        if (ms!=null) {
                            amendSelectionGroup(ms);
                        }
                        else {
                            PositionableLabel lb = checkLabelImages(dLoc);
                            if (lb!=null) {
                                amendSelectionGroup(lb);
                            }
                            else {
                                PositionableLabel b = checkBackgrounds(dLoc);
                                if (b!=null) {
                                    amendSelectionGroup(b);
                                }
                                else {
                                    PositionableLabel sm = checkSignalMastIcons(dLoc);
                                    if (sm!=null) {
                                        amendSelectionGroup(sm);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        else if(selectionWidth==0 || selectionHeight==0){
            clearSelectionGroups();
        }
        //thisPanel.setFocusable(true);
        thisPanel.requestFocusInWindow();
        return;
	}
    
    private ArrayList<LayoutTurnout> _turnoutSelection = null; //new ArrayList<LayoutTurnout>();  // LayoutTurnouts
	private ArrayList<PositionablePoint> _pointSelection = null; //new ArrayList<PositionablePoint>();  // PositionablePoint list
	private ArrayList<LevelXing> _xingSelection = null; //new ArrayList<LevelXing>();  // LevelXing list
	private ArrayList<LayoutSlip> _slipSelection = null; //new ArrayList<LevelXing>();  // LayoutSlip list
	private ArrayList<LayoutTurntable> _turntableSelection = null; //new ArrayList<LayoutTurntable>(); // Turntable list
    private ArrayList<Positionable> _positionableSelection = null;
    
    private void highLightSelection(Graphics2D g){
        java.awt.Stroke stroke = g.getStroke();
        Color color = g.getColor();
        g.setColor(new Color(204, 207, 88));
        g.setStroke(new java.awt.BasicStroke(2.0f));
        if (_positionableSelection!=null){
            for (int i = 0; i<_positionableSelection.size(); i++) {
                Positionable c = _positionableSelection.get(i);
                g.drawRect(c.getX(), c.getY(), c.maxWidth(), c.maxHeight());
            }
        }
        // loop over all defined turnouts
        if (_turnoutSelection!=null){
            for (int i = 0; i<_turnoutSelection.size();i++) {
                LayoutTurnout t = _turnoutSelection.get(i);
                int minx = (int) Math.min(Math.min(t.getCoordsA().getX(), t.getCoordsB().getX()),Math.min(t.getCoordsC().getX(), t.getCoordsD().getX()));
                int miny = (int) Math.min(Math.min(t.getCoordsA().getY(), t.getCoordsB().getY()),Math.min(t.getCoordsC().getY(), t.getCoordsD().getY()));
                int maxx = (int) Math.max(Math.max(t.getCoordsA().getX(), t.getCoordsB().getX()),Math.max(t.getCoordsC().getX(), t.getCoordsD().getX()));
                int maxy = (int) Math.max(Math.max(t.getCoordsA().getY(), t.getCoordsB().getY()),Math.max(t.getCoordsC().getY(), t.getCoordsD().getY()));
                int width = maxx-minx;
                int height = maxy-miny;
                int x = (int) t.getCoordsCenter().getX()-(width/2);
                int y = (int) t.getCoordsCenter().getY()-(height/2);
                g.drawRect(x, y, width, height);
                }
        }
        if (_xingSelection!=null){
        // loop over all defined level crossings
            for (int i = 0; i<_xingSelection.size();i++) {
                LevelXing xing = _xingSelection.get(i);
                int minx = (int) Math.min(Math.min(xing.getCoordsA().getX(), xing.getCoordsB().getX()),Math.min(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int miny = (int) Math.min(Math.min(xing.getCoordsA().getY(), xing.getCoordsB().getY()),Math.min(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int maxx = (int) Math.max(Math.max(xing.getCoordsA().getX(), xing.getCoordsB().getX()),Math.max(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int maxy = (int) Math.max(Math.max(xing.getCoordsA().getY(), xing.getCoordsB().getY()),Math.max(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int width = maxx-minx;
                int height = maxy-miny;
                int x = (int) xing.getCoordsCenter().getX()-(width/2);
                int y = (int) xing.getCoordsCenter().getY()-(height/2);
                g.drawRect(x, y, width, height);
                }
        }
        if (_slipSelection!=null){
        // loop over all defined level crossings
            for (int i = 0; i<_slipSelection.size();i++) {
                LayoutSlip xing = _slipSelection.get(i);
                int minx = (int) Math.min(Math.min(xing.getCoordsA().getX(), xing.getCoordsB().getX()),Math.min(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int miny = (int) Math.min(Math.min(xing.getCoordsA().getY(), xing.getCoordsB().getY()),Math.min(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int maxx = (int) Math.max(Math.max(xing.getCoordsA().getX(), xing.getCoordsB().getX()),Math.max(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int maxy = (int) Math.max(Math.max(xing.getCoordsA().getY(), xing.getCoordsB().getY()),Math.max(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int width = maxx-minx;
                int height = maxy-miny;
                int x = (int) xing.getCoordsCenter().getX()-(width/2);
                int y = (int) xing.getCoordsCenter().getY()-(height/2);
                g.drawRect(x, y, width, height);
            }
        }
        // loop over all defined turntables
        if (_turntableSelection!=null){
            for (int i = 0; i<_turntableSelection.size();i++) {
                LayoutTurntable tt = _turntableSelection.get(i);
                Point2D center = tt.getCoordsCenter();
                int x = (int) center.getX() - (int)tt.getRadius();
                int y = (int) center.getY() - (int)tt.getRadius();
                g.drawRect(x, y, ((int)tt.getRadius()*2), ((int)tt.getRadius()*2));
            }
        }
        // loop over all defined Anchor Points and End Bumpers
        if (_pointSelection!=null){
            for (int i = 0; i<_pointSelection.size();i++) {
                PositionablePoint p = _pointSelection.get(i);
                Point2D coord = p.getCoords();
                g.drawRect((int)coord.getX()-4, (int)coord.getY()-4, 9, 9);
            }
        }
        g.setColor(color);
        g.setStroke(stroke);
    }
    
    private void createSelectionGroups(){
        List <Positionable> contents = getContents();
        Rectangle2D selectRect = new Rectangle2D.Double (selectionX, selectionY, 
                                                selectionWidth, selectionHeight);
        for (int i = 0; i<contents.size(); i++) {
            Positionable c = contents.get(i);
            Point2D upperLeft = c.getLocation();
            if (selectRect.contains(upperLeft)) {
                if (_positionableSelection==null) _positionableSelection = new ArrayList<Positionable>();
                if(!_positionableSelection.contains(c))
                    _positionableSelection.add(c);
            }
        }
        // loop over all defined turnouts
        for (int i = 0; i<turnoutList.size();i++) {
            LayoutTurnout t = turnoutList.get(i);
            Point2D center = t.getCoordsCenter();
            if (selectRect.contains(center)) {
                if (_turnoutSelection==null) _turnoutSelection = new ArrayList<LayoutTurnout>();
                if(!_turnoutSelection.contains(t))
                    _turnoutSelection.add(t);
            }
        }
        // loop over all defined level crossings
        for (int i = 0; i<xingList.size();i++) {
            LevelXing x = xingList.get(i);
            Point2D center = x.getCoordsCenter();
            if (selectRect.contains(center)) {
                if (_xingSelection==null) _xingSelection = new ArrayList<LevelXing>();
                if(!_xingSelection.contains(x))
                    _xingSelection.add(x);
            }
        }
        // loop over all defined level crossings
        for (int i = 0; i<slipList.size();i++) {
            LayoutSlip x = slipList.get(i);
            Point2D center = x.getCoordsCenter();
            if (selectRect.contains(center)) {
                if (_slipSelection==null) _slipSelection = new ArrayList<LayoutSlip>();
                if(!_slipSelection.contains(x))
                    _slipSelection.add(x);
            }
        }
        // loop over all defined turntables
        for (int i = 0; i<turntableList.size();i++) {
            LayoutTurntable x = turntableList.get(i);
            Point2D center = x.getCoordsCenter();
            if (selectRect.contains(center)) {
                if (_turntableSelection==null) _turntableSelection = new ArrayList<LayoutTurntable>();
                if(!_turntableSelection.contains(x))
                    _turntableSelection.add(x);
            }
        }
        // loop over all defined Anchor Points and End Bumpers
        for (int i = 0; i<pointList.size();i++) {
            PositionablePoint p = pointList.get(i);
            Point2D coord = p.getCoords();
            if (selectRect.contains(coord)) {
                if (_pointSelection==null) _pointSelection = new ArrayList<PositionablePoint>();
                if(!_pointSelection.contains(p))
                    _pointSelection.add(p);
            }
        }
        repaint();
    }
    
    private void clearSelectionGroups(){
        _pointSelection=null;
        _turntableSelection=null;
        _xingSelection=null;
        _slipSelection=null;
        _turnoutSelection=null;
        _positionableSelection=null;
    }
    
    boolean noWarnGlobalDelete = false;
    
    private void deleteSelectedItems() {
        if(!noWarnGlobalDelete){
            int selectedValue = JOptionPane.showOptionDialog(this,
                rb.getString("Question6"),rb.getString("WarningTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
                rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
            if (selectedValue == 1) return;   // return without creating if "No" response
            if (selectedValue == 2) {
                // Suppress future warnings, and continue
                noWarnGlobalDelete = true;
            }
        }
        if(_positionableSelection!=null){
            for(Positionable comp: _positionableSelection){
                remove(comp);
            }
        }
        if(_pointSelection!=null){
            boolean oldPosPoint = noWarnPositionablePoint;
            noWarnPositionablePoint = true;
            for(PositionablePoint point: _pointSelection){
                removePositionablePoint(point);
            }
            noWarnPositionablePoint = oldPosPoint;
        }
        
        if(_xingSelection!=null){
            boolean oldLevelXing = noWarnLevelXing;
            noWarnLevelXing = true;
            for(LevelXing point: _xingSelection){
                removeLevelXing(point);
            }
            noWarnLevelXing = oldLevelXing;
        }
        if(_slipSelection!=null){
            boolean oldSlip = noWarnSlip;
            noWarnSlip = true;
            for(LayoutSlip point: _slipSelection){
                removeLayoutSlip(point);
            }
            noWarnSlip = oldSlip;
        }
        if(_turntableSelection!=null){
            boolean oldTurntable = noWarnTurntable;
            noWarnTurntable = true;
            for(LayoutTurntable point: _turntableSelection){
                removeTurntable(point);
            }
            noWarnTurntable = oldTurntable;
        }
        if(_turnoutSelection!=null){
            boolean oldTurnout = noWarnLayoutTurnout;
            noWarnLayoutTurnout = true;
            for(LayoutTurnout point: _turnoutSelection){
                removeLayoutTurnout(point);
            }
            noWarnLayoutTurnout = oldTurnout;
        }
        selectionActive = false;
        clearSelectionGroups();
        repaint();
    
    }
    private void amendSelectionGroup(Positionable p){
        if (_positionableSelection==null){
            _positionableSelection = new ArrayList <Positionable>();
        }
        boolean removed = false;
        for(int i=0; i<_positionableSelection.size();i++){
            if (_positionableSelection.get(i)==p){
                _positionableSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _positionableSelection.add(p);
        if (_positionableSelection.size()==0){
            _positionableSelection=null;
        }
        repaint();
    }
    
    private void amendSelectionGroup(LayoutTurnout p){
        if (_turnoutSelection==null){
            _turnoutSelection = new ArrayList <LayoutTurnout>();
        }
        boolean removed = false;
        for(int i=0; i<_turnoutSelection.size();i++){
            if (_turnoutSelection.get(i)==p){
                _turnoutSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _turnoutSelection.add(p);
        if (_turnoutSelection.size()==0){
            _turnoutSelection=null;
        }
        repaint();
    }
    
    private void amendSelectionGroup(PositionablePoint p){
        if (_pointSelection==null){
            _pointSelection = new ArrayList <PositionablePoint>();
        }
        boolean removed = false;
        for(int i=0; i<_pointSelection.size();i++){
            if (_pointSelection.get(i)==p){
                _pointSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _pointSelection.add(p);
        if (_pointSelection.size()==0){
            _pointSelection=null;
        }
        repaint();
    }
    
    private void amendSelectionGroup(LevelXing p){
        if (_xingSelection==null){
            _xingSelection = new ArrayList <LevelXing>();
        }
        boolean removed = false;
        for(int i=0; i<_xingSelection.size();i++){
            if (_xingSelection.get(i)==p){
                _xingSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _xingSelection.add(p);
        if (_xingSelection.size()==0){
            _xingSelection=null;
        }
        repaint();
    }
    
    private void amendSelectionGroup(LayoutSlip p){
        if (_slipSelection==null){
            _slipSelection = new ArrayList <LayoutSlip>();
        }
        boolean removed = false;
        for(int i=0; i<_slipSelection.size();i++){
            if (_slipSelection.get(i)==p){
                _slipSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _slipSelection.add(p);
        if (_slipSelection.size()==0){
            _slipSelection=null;
        }
        repaint();
    }
    
    private void amendSelectionGroup(LayoutTurntable p){
        if (_turntableSelection==null){
            _turntableSelection = new ArrayList <LayoutTurntable>();
        }
        boolean removed = false;
        for(int i=0; i<_turntableSelection.size();i++){
            if (_turntableSelection.get(i)==p){
                _turntableSelection.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _turntableSelection.add(p);
        if (_turntableSelection.size()==0){
            _turntableSelection=null;
        }
        repaint();
    }

    public void alignSelection(boolean alignX){
        int sum = 0;
        int cnt = 0;

        if(_positionableSelection!=null){
            for (int i = 0; i<_positionableSelection.size(); i++){
                Positionable comp = _positionableSelection.get(i);
                if (!getFlag(OPTION_POSITION, comp.isPositionable()))  { continue; }
                if (alignX) {
                    sum += comp.getX();
                } else {
                    sum += comp.getY();
                }
                cnt++;
            }
        }
        
        if(_pointSelection!=null){
            for (int i = 0; i<_pointSelection.size(); i++){
                PositionablePoint comp = _pointSelection.get(i);
                if (alignX) {
                    sum += comp.getCoords().getX();
                } else {
                    sum += comp.getCoords().getY();
                }
                cnt++;
            }
        }
        
        if(_turnoutSelection!=null){
            for (int i = 0; i<_turnoutSelection.size(); i++){
                LayoutTurnout comp = _turnoutSelection.get(i);
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }
        
        if(_xingSelection!=null){
            for (int i = 0; i<_xingSelection.size(); i++){
                LevelXing comp = _xingSelection.get(i);
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }
        if(_slipSelection!=null){
            for (int i = 0; i<_slipSelection.size(); i++){
                LayoutSlip comp = _slipSelection.get(i);
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }
        if(_turntableSelection!=null){
            for (int i = 0; i<_turntableSelection.size(); i++){
                LayoutTurntable comp = _turntableSelection.get(i);
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }
        
        int ave = Math.round((float)sum/cnt);
        if(_positionableSelection!=null){
            for (int i=0; i<_positionableSelection.size(); i++) {
                Positionable comp = _positionableSelection.get(i);
                if (!getFlag(OPTION_POSITION, comp.isPositionable()))  { continue; }
                if (alignX) {
                    comp.setLocation(ave, comp.getY());
                } else {
                    comp.setLocation(comp.getX(), ave);
                }
            }
        }
        if(_pointSelection!=null){
            for (int i=0; i<_pointSelection.size(); i++) {
                PositionablePoint comp = _pointSelection.get(i);
                if (alignX) {
                    comp.setCoords(new Point2D.Double(ave, comp.getCoords().getY()));
                } else {
                    comp.setCoords(new Point2D.Double(comp.getCoords().getX(), ave));
                }
            }
        }
        if(_turnoutSelection!=null){
            for (int i=0; i<_turnoutSelection.size(); i++) {
                LayoutTurnout comp = _turnoutSelection.get(i);
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }
        if(_xingSelection!=null){
            for (int i=0; i<_xingSelection.size(); i++) {
                LevelXing comp = _xingSelection.get(i);
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }
        if(_slipSelection!=null){
            for (int i=0; i<_slipSelection.size(); i++) {
                LayoutSlip comp = _slipSelection.get(i);
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }
        if(_turntableSelection!=null){
            for (int i=0; i<_turntableSelection.size(); i++) {
                LayoutTurntable comp = _turntableSelection.get(i);
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }
        repaint();
    }
    
    protected boolean showAlignPopup() {
        if (_positionableSelection!=null) {
            return true;
        } else if (_pointSelection!=null) {
            return true;
        } else if (_turnoutSelection!=null){
            return true;
        } else if (_turntableSelection!=null){
            return true;
        } else if (_xingSelection!=null){
            return true;
        } else if (_slipSelection!=null){
            return true;
        }
        return false;
    }
    
        /**
    * Offer actions to align the selected Positionable items either
    * Horizontally (at avearage y coord) or Vertically (at avearage x coord).
    */
    public boolean setShowAlignmentMenu(JPopupMenu popup) {
        if (showAlignPopup()) {
            JMenu edit = new JMenu(rb.getString("EditAlignment"));
            edit.add(new AbstractAction(rb.getString("AlignX")) {
                public void actionPerformed(ActionEvent e) {
                    alignSelection(true);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignY")) {
                public void actionPerformed(ActionEvent e) {
                    alignSelection(false);
                }
            });
            popup.add(edit);
            return true;
        }
        return false;
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_DELETE){
            deleteSelectedItems();
            return;
        }
        if (_positionableSelection!=null){
            for (int i = 0; i<_positionableSelection.size(); i++) {
                Positionable c = _positionableSelection.get(i);
                int xNew;
                int yNew;
                if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth()==0)) {
                    MemoryIcon pm = (MemoryIcon) c;
                    xNew = (int)(returnNewXPostition(e, pm.getOriginalX()));
                    yNew = (int)(returnNewYPostition(e, pm.getOriginalY()));
                } else {
                    Point2D upperLeft = c.getLocation();
                    xNew = (int)(returnNewXPostition(e, upperLeft.getX()));
                    yNew = (int)(returnNewYPostition(e, upperLeft.getY()));
                }
                c.setLocation(xNew,yNew);
            }
        }
        // loop over all defined turnouts
        if (_turnoutSelection!=null){
            for (int i = 0; i<_turnoutSelection.size();i++) {
                LayoutTurnout t = _turnoutSelection.get(i);
                Point2D center = t.getCoordsCenter();
                t.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                                returnNewYPostition(e, center.getY())));
                }
        }
        if (_xingSelection!=null){
        // loop over all defined level crossings
            for (int i = 0; i<_xingSelection.size();i++) {
                LevelXing x = _xingSelection.get(i);
                Point2D center = x.getCoordsCenter();
                x.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                                returnNewYPostition(e, center.getY())));
                }
        }
        if (_slipSelection!=null){
        // loop over all defined level crossings
            for (int i = 0; i<_slipSelection.size();i++) {
                LayoutSlip x = _slipSelection.get(i);
                Point2D center = x.getCoordsCenter();
                x.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                                returnNewYPostition(e, center.getY())));
                }
        }
        // loop over all defined turntables
        if (_turntableSelection!=null){
            for (int i = 0; i<_turntableSelection.size();i++) {
                LayoutTurntable x = _turntableSelection.get(i);
                Point2D center = x.getCoordsCenter();
                x.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                                returnNewYPostition(e, center.getY())));
            }
        }
        // loop over all defined Anchor Points and End Bumpers
        if (_pointSelection!=null){
            for (int i = 0; i<_pointSelection.size();i++) {
                PositionablePoint p = _pointSelection.get(i);
                Point2D coord = p.getCoords();
                p.setCoords(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                                                returnNewYPostition(e, coord.getY())));
            }
        }
        repaint();
    }
    
    private double returnNewXPostition(KeyEvent e, double val){
        if(e.isShiftDown()){
            switch (e.getKeyCode()){
                case KeyEvent.VK_LEFT: val=val-1;
                                    break;
                case KeyEvent.VK_RIGHT: val=val+1;
                                        break;
                default: break;
            }
        } else {
            switch (e.getKeyCode()){
                case KeyEvent.VK_LEFT: val=val-5;
                                    break;
                case KeyEvent.VK_RIGHT: val=val+5;
                                        break;
                default: break;
            }
        }
        if (val<0) val = 0;
        return val;
    
    }
    
    private double returnNewYPostition(KeyEvent e, double val){
        if(e.isShiftDown()){
            switch (e.getKeyCode()){
                case KeyEvent.VK_UP: val=val-1;
                                    break;
                case KeyEvent.VK_DOWN: val=val+1;
                                    break;
            }
        } else {
            switch (e.getKeyCode()){
                case KeyEvent.VK_UP: val=val-5;
                                    break;
                case KeyEvent.VK_DOWN: val=val+5;
                                    break;
            }
        }
        if (val<0) val = 0;
        return val;
    
    }

    int _prevNumSel = 0;
    public void mouseMoved(MouseEvent event)
    {
        calcLocation(event, 0, 0);
        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
        List <Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        int numSel = selections.size();
        if (numSel > 0) {
            selection = selections.get(0); 
        }
        if (selection!=null && selection.getDisplayLevel()>BKG && selection.showTooltip()) {
            showToolTip(selection, event);
        } else {
            super.setToolTip(null);
        }
        if (numSel != _prevNumSel) {
           repaint();
           _prevNumSel = numSel; 
        }
    }

	private boolean isDragging = false;
    public void mouseDragged(MouseEvent event)
    {
		// initialize mouse position
		calcLocation(event, 0, 0);
		// ignore this event if still at the original point
		if ((!isDragging) && (xLoc==getAnchorX()) && (yLoc==getAnchorY())) return;
		// process this mouse dragged event
        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
		}
		Point2D newPos = new Point2D.Double(dLoc.getX() + startDel.getX(),
						dLoc.getY() + startDel.getY());
		if ((selectedObject!=null) && (event.isMetaDown() || event.isAltDown()) && (selectedPointType==MARKER)) {
			// marker moves regardless of editMode or positionable
			PositionableLabel pl = (PositionableLabel)selectedObject;
			int xint = (int)newPos.getX();
			int yint = (int)newPos.getY();
			// don't allow negative placement, object could become unreachable
			if (xint<0) xint = 0;
			if (yint<0) yint = 0;
			pl.setLocation(xint, yint);
			isDragging = true;
			repaint();
			return;
		}
		if (isEditable()) {
			if ((selectedObject!=null) && (event.isMetaDown() || event.isAltDown()) && allPositionable()) {
				// moving a point
				if (snapToGridOnMove) {
					int xx = (((int)newPos.getX()+4)/10)*10;
					int yy = (((int)newPos.getY()+4)/10)*10;
					newPos.setLocation(xx,yy);
				}
                if (_pointSelection!=null||_turntableSelection!=null||_xingSelection!=null||
                            _turnoutSelection!=null||_positionableSelection!=null){
                    int offsetx = xLoc - _lastX;
                    int offsety = yLoc - _lastY;
                    //We should do a move based upon a selection group.
                    int xNew;
                    int yNew;
                    if (_positionableSelection!=null){
                        for (int i = 0; i<_positionableSelection.size(); i++) {
                            Positionable c = _positionableSelection.get(i);
                            if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth()==0)) {
                                MemoryIcon pm = (MemoryIcon) c;
                                xNew = (pm.getOriginalX()+offsetx);
                                yNew = (pm.getOriginalY()+offsety);
                            } else {
                                Point2D upperLeft = c.getLocation();
                                xNew = (int)(upperLeft.getX()+offsetx);
                                yNew = (int)(upperLeft.getY()+offsety);
                            }
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            c.setLocation(xNew,yNew);
                        }
                    }
                    
                    if (_turnoutSelection!=null){
                        for (int i = 0; i<_turnoutSelection.size();i++) {
                            LayoutTurnout t = _turnoutSelection.get(i);
                            Point2D center = t.getCoordsCenter();
                            xNew = (int) center.getX()+offsetx;
                            yNew = (int) center.getY()+offsety;
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            t.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }
                    if (_xingSelection!=null){
                    // loop over all defined level crossings
                        for (int i = 0; i<_xingSelection.size();i++) {
                            LevelXing x = _xingSelection.get(i);
                            Point2D center = x.getCoordsCenter();
                            xNew = (int) center.getX()+offsetx;
                            yNew = (int) center.getY()+offsety;
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            x.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }
                    if (_slipSelection!=null){
                    // loop over all defined level crossings
                        for (int i = 0; i<_slipSelection.size();i++) {
                            LayoutSlip x = _slipSelection.get(i);
                            Point2D center = x.getCoordsCenter();
                            xNew = (int) center.getX()+offsetx;
                            yNew = (int) center.getY()+offsety;
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            x.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }
                    // loop over all defined turntables
                    if (_turntableSelection!=null){
                        for (int i = 0; i<_turntableSelection.size();i++) {
                            LayoutTurntable x = _turntableSelection.get(i);
                            Point2D center = x.getCoordsCenter();
                            xNew = (int) center.getX()+offsetx;
                            yNew = (int) center.getY()+offsety;
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            x.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }
                    // loop over all defined Anchor Points and End Bumpers
                    if (_pointSelection!=null){
                        for (int i = 0; i<_pointSelection.size();i++) {
                            PositionablePoint p = _pointSelection.get(i);
                            Point2D coord = p.getCoords();
                            xNew = (int) coord.getX()+offsetx;
                            yNew = (int) coord.getY()+offsety;
                            if (xNew<0) xNew=0;
                            if (yNew<0) yNew=0;
                            p.setCoords(new Point2D.Double(xNew, yNew));
                        }
                    }
                    _lastX = xLoc;
                    _lastY = yLoc;
                } else {
                    switch (selectedPointType) {
                        case POS_POINT:
                            ((PositionablePoint)selectedObject).setCoords(newPos);
                            isDragging = true;
                            break;
                        case TURNOUT_CENTER:
                            ((LayoutTurnout)selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        case TURNOUT_A:
                            LayoutTurnout o = (LayoutTurnout)selectedObject;
                            o.setCoordsA(newPos);
                            break;
                        case TURNOUT_B:
                            o = (LayoutTurnout)selectedObject;
                            o.setCoordsB(newPos);
                            break;
                        case TURNOUT_C:
                            o = (LayoutTurnout)selectedObject;
                            o.setCoordsC(newPos);
                            break;
                        case TURNOUT_D:
                            o = (LayoutTurnout)selectedObject;
                            o.setCoordsD(newPos);
                            break;
                        case LEVEL_XING_CENTER:
                            ((LevelXing)selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        case LEVEL_XING_A:
                            LevelXing x = (LevelXing)selectedObject;
                            x.setCoordsA(newPos);
                            break;
                        case LEVEL_XING_B:
                            x = (LevelXing)selectedObject;
                            x.setCoordsB(newPos);
                            break;
                        case LEVEL_XING_C:
                            x = (LevelXing)selectedObject;
                            x.setCoordsC(newPos);
                            break;
                        case LEVEL_XING_D:
                            x = (LevelXing)selectedObject;
                            x.setCoordsD(newPos);
                            break;
                        case SLIP_CENTER:
                            ((LayoutSlip)selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        case SLIP_A:
                            LayoutSlip sl = (LayoutSlip)selectedObject;
                            sl.setCoordsA(newPos);
                            break;
                        case SLIP_B:
                            sl = (LayoutSlip)selectedObject;
                            sl.setCoordsB(newPos);
                            break;
                        case SLIP_C:
                            sl = (LayoutSlip)selectedObject;
                            sl.setCoordsC(newPos);
                            break;
                        case SLIP_D:
                            sl = (LayoutSlip)selectedObject;
                            sl.setCoordsD(newPos);
                            break;
                        case TURNTABLE_CENTER:
                            ((LayoutTurntable)selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        case LAYOUT_POS_LABEL:
                            PositionableLabel l = (PositionableLabel)selectedObject;
                            if (l.isPositionable()) {
                                int xint = (int)newPos.getX();
                                int yint = (int)newPos.getY();
                                // don't allow negative placement, object could become unreachable
                                if (xint<0) xint = 0;
                                if (yint<0) yint = 0;
                                l.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        case LAYOUT_POS_JCOMP:
                            PositionableJComponent c = (PositionableJComponent)selectedObject;
                            if (c.isPositionable()) {
                                int xint = (int)newPos.getX();
                                int yint = (int)newPos.getY();
                                // don't allow negative placement, object could become unreachable
                                if (xint<0) xint = 0;
                                if (yint<0) yint = 0;
                                c.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        case MULTI_SENSOR:
                            PositionableLabel pl = (PositionableLabel)selectedObject;
                            if (pl.isPositionable()) {
                                int xint = (int)newPos.getX();
                                int yint = (int)newPos.getY();
                                // don't allow negative placement, object could become unreachable
                                if (xint<0) xint = 0;
                                if (yint<0) yint = 0;
                                pl.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        case TRACK_CIRCLE_CENTRE: TrackSegment t = (TrackSegment)selectedObject;
                                                    reCalculateTrackSegmentAngle(t, newPos.getX(), newPos.getY());
                                                break;
                        default:
                            if (selectedPointType>=TURNTABLE_RAY_OFFSET) {
                                LayoutTurntable turn = (LayoutTurntable)selectedObject;
                                turn.setRayCoordsIndexed(newPos.getX(),newPos.getY(),
                                                selectedPointType-TURNTABLE_RAY_OFFSET);
                            }
                    }
                }
				repaint();
			}			
			else if ( (beginObject!=null) && event.isShiftDown() 
											&& trackBox.isSelected() ) {
				// dragging from first end of Track Segment
				currentLocation.setLocation(xLoc,yLoc);
				boolean needResetCursor = (foundObject!=null);
				if (checkSelect(currentLocation, true)) {
					// have match to free connection point, change cursor
					setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				}
				else if (needResetCursor) {
					setCursor(Cursor.getDefaultCursor());
				}
				repaint();
			}
			else if ( selectionActive && (!event.isShiftDown()) && (!event.isAltDown()) && (!event.isMetaDown()) ) {
				selectionWidth = xLoc - selectionX;
				selectionHeight = yLoc - selectionY;
				repaint();
			}
		} else {
			Rectangle r = new Rectangle(event.getX(), event.getY(), 1, 1);
			(       (JComponent) event.getSource()).scrollRectToVisible(r);
        }
        return;
    }

	@SuppressWarnings("unused")
	private void updateLocation(Object o,int pointType,Point2D newPos) {
		switch (pointType) {
			case TURNOUT_A:
				((LayoutTurnout)o).setCoordsA(newPos);
				break;
			case TURNOUT_B:
				((LayoutTurnout)o).setCoordsB(newPos);
				break;
			case TURNOUT_C:
				((LayoutTurnout)o).setCoordsC(newPos);
				break;
			case TURNOUT_D:
				((LayoutTurnout)o).setCoordsD(newPos);
				break;
			case LEVEL_XING_A:
				((LevelXing)o).setCoordsA(newPos);
				break;
			case LEVEL_XING_B:
				((LevelXing)o).setCoordsB(newPos);
				break;
			case LEVEL_XING_C:
				((LevelXing)o).setCoordsC(newPos);
				break;
			case LEVEL_XING_D:
				((LevelXing)o).setCoordsD(newPos);
				break;
			case SLIP_A:
				((LayoutSlip)o).setCoordsA(newPos);
				break;
			case SLIP_B:
				((LayoutSlip)o).setCoordsB(newPos);
				break;
			case SLIP_C:
				((LayoutSlip)o).setCoordsC(newPos);
				break;
			case SLIP_D:
				((LayoutSlip)o).setCoordsD(newPos);
				break;
			default:
				if (pointType>=TURNTABLE_RAY_OFFSET) {
					LayoutTurntable turn = (LayoutTurntable)o;
					turn.setRayCoordsIndexed(newPos.getX(),newPos.getY(),
									pointType-TURNTABLE_RAY_OFFSET);
				}
		}
		setDirty(true);
	}
	public void setLoc(int x, int y) {
		if (isEditable()) {
			xLoc = x;
			yLoc = y;
			xLabel.setText(Integer.toString(xLoc));
			yLabel.setText(Integer.toString(yLoc));
		}
	}
    
    /**
     * Add an Anchor point. 
     */
    public void addAnchor() {
		numAnchors ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "A"+numAnchors;
			if (findPositionablePointByName(name)==null) duplicate = false;
			if (duplicate) numAnchors ++;
		}
		// create object
		PositionablePoint o = new PositionablePoint(name, 
							PositionablePoint.ANCHOR, currentPoint, this);
		//if (o!=null) {
		pointList.add(o);
		setDirty(true);
		//}
	}

    /**
     * Add an End Bumper point. 
     */
    public void addEndBumper() {
		numEndBumpers ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "EB"+numEndBumpers;
			if (findPositionablePointByName(name)==null) duplicate = false;
			if (duplicate) numEndBumpers ++;
		}
		// create object
		PositionablePoint o = new PositionablePoint(name, 
							PositionablePoint.END_BUMPER, currentPoint, this);
		//if (o!=null) {
		pointList.add(o);
		setDirty(true);
		//}
	}

    /**
     * Add a Track Segment 
     */
    public void addTrackSegment() {
		numTrackSegments ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "T"+numTrackSegments;
			if (findTrackSegmentByName(name)==null) duplicate = false;
			if (duplicate) numTrackSegments ++;
		}
		// create object
		newTrack = new TrackSegment(name,beginObject,beginPointType,
						foundObject,foundPointType,dashedLine.isSelected(),
						mainlineTrack.isSelected(),this);
		if (newTrack!=null) {
			trackList.add(newTrack);
			setDirty(true);
			// link to connected objects
			setLink(newTrack,TRACK,beginObject,beginPointType);
			setLink(newTrack,TRACK,foundObject,foundPointType);
			// check on layout block
			LayoutBlock b = provideLayoutBlock(blockIDField.getText().trim());
			if (b!=null) {
				newTrack.setLayoutBlock(b);
				auxTools.setBlockConnectivityChanged();
				// check on occupancy sensor
				String sensorName = (blockSensor.getText().trim());
				if (sensorName.length()>0) {
					if (!validateSensor(sensorName,b,this)) {
						b.setOccupancySensorName("");
					}
					else {
						blockSensor.setText( b.getOccupancySensorName() );
					}
				}
				newTrack.updateBlockInfo();
			}
			
		}
		else {
			log.error("Failure to create a new Track Segment");
		}
	}

    /**
     * Add a Level Crossing 
     */
    public void addLevelXing() {
		numLevelXings ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "X"+numLevelXings;
			if (findLevelXingByName(name)==null) duplicate = false;
			if (duplicate) numLevelXings ++;
		}
		// create object
		LevelXing o = new LevelXing(name,currentPoint,this);
		//if (o!=null) {
		xingList.add(o);
		setDirty(true);
		// check on layout block
		LayoutBlock b = provideLayoutBlock(blockIDField.getText().trim());
		if (b!=null) {
			o.setLayoutBlockAC(b);
			o.setLayoutBlockBD(b);
			// check on occupancy sensor
			String sensorName = (blockSensor.getText().trim());
			if (sensorName.length()>0) {
				if (!validateSensor(sensorName,b,this)) {
					b.setOccupancySensorName("");
				}
				else {
					blockSensor.setText( b.getOccupancySensorName() );
				}
			}
		}
		//}
    }
    
    /**
     * Add a LayoutSlip
     */
    public void addLayoutSlip(int type) {
        double rot = 0.0;
		String s = rotationField.getText().trim();
		if (s.length()<1) {
			rot = 0.0;
		}
		else {
			try {
				rot = Double.parseDouble(s);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(this, rb.getString("Error3")+" "+
						e,rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
        numLayoutSlips ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "SL"+numLayoutSlips;
			if (findLayoutSlipByName(name)==null) duplicate = false;
			if (duplicate) numLayoutSlips ++;
		}
		// create object
		LayoutSlip o = new LayoutSlip(name,currentPoint, rot, this, type);
        slipList.add(o);
        setDirty(true);
        
        // check on layout block
        LayoutBlock b = provideLayoutBlock(blockIDField.getText().trim());
        if (b!=null) {
            o.setLayoutBlock(b);
            // check on occupancy sensor
            String sensorName = (blockSensor.getText().trim());
            if (sensorName.length()>0) {
                if (!validateSensor(sensorName,b,this)) {
                    b.setOccupancySensorName("");
                }
                else {
                    blockSensor.setText( b.getOccupancySensorName() );
                }
            }
        }
        String turnoutName = nextTurnout.getText().trim();
        if ( validatePhysicalTurnout(turnoutName, this) ) {
            // turnout is valid and unique.
            o.setTurnout(turnoutName);
            if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
                nextTurnout.setText(turnoutName.toUpperCase());
            }
        }
        else {
            o.setTurnout("");
            nextTurnout.setText("");
        }
        turnoutName = extraTurnout.getText().trim();
        if ( validatePhysicalTurnout(turnoutName, this) ) {
            // turnout is valid and unique.
            o.setTurnoutB(turnoutName);
            if (o.getTurnoutB().getSystemName().equals(turnoutName.toUpperCase())) {
                extraTurnout.setText(turnoutName.toUpperCase());
            }
        }
        else {
            o.setTurnoutB("");
            extraTurnout.setText("");
        }
    }

    /**
     * Add a Layout Turnout 
     */
    public void addLayoutTurnout(int type) {
		// get the rotation entry
		double rot = 0.0;
		String s = rotationField.getText().trim();
		if (s.length()<1) {
			rot = 0.0;
		}
		else {
			try {
				rot = Double.parseDouble(s);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(this, rb.getString("Error3")+" "+
						e,rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		numLayoutTurnouts ++;
		// get unique name
		String name = "";
		boolean duplicate = true;
		while (duplicate) {
			name = "TO"+numLayoutTurnouts;
			if (findLayoutTurnoutByName(name)==null) duplicate = false;
			if (duplicate) numLayoutTurnouts ++;
		}
		// create object
		LayoutTurnout o = new LayoutTurnout(name,type,
										currentPoint,rot,xScale,yScale,this);
		//if (o!=null) {
		turnoutList.add(o);
		setDirty(true);
		// check on layout block
		LayoutBlock b = provideLayoutBlock(blockIDField.getText().trim());
		if (b!=null) {
			o.setLayoutBlock(b);
			// check on occupancy sensor
			String sensorName = (blockSensor.getText().trim());
			if (sensorName.length()>0) {
				if (!validateSensor(sensorName,b,this)) {
					b.setOccupancySensorName("");
				}
				else {
					blockSensor.setText( b.getOccupancySensorName() );
				}
			}
		}
		// set default continuing route Turnout State
		o.setContinuingSense(Turnout.CLOSED);
		// check on a physical turnout
		String turnoutName = nextTurnout.getText().trim();
		if ( validatePhysicalTurnout(turnoutName, this) ) {
			// turnout is valid and unique.
			o.setTurnout(turnoutName);
			if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
				nextTurnout.setText(turnoutName.toUpperCase());
			}
		}
		else {
			o.setTurnout("");
			nextTurnout.setText("");
		}
		//}
    }
	
	/**
	 * Validates that a physical turnout exists and is unique among Layout Turnouts
	 *    Returns true if valid turnout was entered, false otherwise
	 */
	public boolean validatePhysicalTurnout(String turnoutName, Component openPane) {
		// check if turnout name was entered
		if (turnoutName.length() < 1) {
			// no turnout entered
			return false;
		}
		// ensure that this turnout is unique among Layout Turnouts
		LayoutTurnout t = null;
		for (int i=0;i<turnoutList.size();i++) {
			t = turnoutList.get(i);
			log.debug("LT '"+t.getName()+"', Turnout tested '"+t.getTurnoutName()+"' ");
			Turnout to = t.getTurnout();
            /*Only check for the second turnout if the type is a double cross over
            otherwise the second turnout is used to throw an additional turnout at 
            the same time.*/
            Turnout to2 = null;
            if(t.getTurnoutType()>=LayoutTurnout.DOUBLE_XOVER){
                to2 = t.getSecondTurnout();
            }
			if (to!=null) {
				if ( (to.getSystemName().equals(turnoutName.toUpperCase())) ||
					((to.getUserName()!=null) && (to.getUserName().equals(turnoutName))) ) {
					JOptionPane.showMessageDialog(openPane,
							java.text.MessageFormat.format(rb.getString("Error4"),
							new Object[]{turnoutName}),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} 
            if (to2!=null){
                if ( (to2.getSystemName().equals(turnoutName.toUpperCase())) ||
					((to2.getUserName()!=null) && (to2.getUserName().equals(turnoutName))) ) {
					JOptionPane.showMessageDialog(openPane,
							java.text.MessageFormat.format(rb.getString("Error4"),
							new Object[]{turnoutName}),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
            }
		}
        for(LayoutSlip slip: slipList){
            Turnout to = slip.getTurnout();
            if(to!=null){
                if(to.getSystemName().equals(turnoutName) || (to.getUserName()!=null && to.getUserName().equals(turnoutName))){
                    JOptionPane.showMessageDialog(openPane,
							java.text.MessageFormat.format(rb.getString("Error4"),
							new Object[]{turnoutName}),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
                }
            }
            to=slip.getTurnoutB();
            if(to!=null){
                if(to.getSystemName().equals(turnoutName) || (to.getUserName()!=null && to.getUserName().equals(turnoutName))){
                    JOptionPane.showMessageDialog(openPane,
							java.text.MessageFormat.format(rb.getString("Error4"),
							new Object[]{turnoutName}),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
                }
            }
        }
		// check that the unique turnout name corresponds to a defined physical turnout
		Turnout to = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
		if (to == null) {
			// There is no turnout corresponding to this name
			JOptionPane.showMessageDialog(openPane,
					java.text.MessageFormat.format(rb.getString("Error8"),
					new Object[]{turnoutName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

    /**
     * Adds a link in the 'to' object to the 'from' object
     */
    private void setLink(Object fromObject,int fromPointType,
											Object toObject,int toPointType) {
		switch (toPointType) {
			case POS_POINT:
				if (fromPointType==TRACK) {
					((PositionablePoint)toObject).setTrackConnection(
													(TrackSegment)fromObject);
				}
				else {
					log.error("Attempt to set a non-TRACK connection to a Positionable Point");
				}
				break;
			case TURNOUT_A:
				((LayoutTurnout)toObject).setConnectA(fromObject,fromPointType);
				break;
			case TURNOUT_B:
				((LayoutTurnout)toObject).setConnectB(fromObject,fromPointType);
				break;
			case TURNOUT_C:
				((LayoutTurnout)toObject).setConnectC(fromObject,fromPointType);
				break;
			case TURNOUT_D:
				((LayoutTurnout)toObject).setConnectD(fromObject,fromPointType);
				break;
			case LEVEL_XING_A:
				((LevelXing)toObject).setConnectA(fromObject,fromPointType);
				break;
			case LEVEL_XING_B:
				((LevelXing)toObject).setConnectB(fromObject,fromPointType);
				break;
			case LEVEL_XING_C:
				((LevelXing)toObject).setConnectC(fromObject,fromPointType);
				break;
			case LEVEL_XING_D:
				((LevelXing)toObject).setConnectD(fromObject,fromPointType);
				break;
			case SLIP_A:
				((LayoutSlip)toObject).setConnectA(fromObject,fromPointType);
				break;
			case SLIP_B:
				((LayoutSlip)toObject).setConnectB(fromObject,fromPointType);
				break;
			case SLIP_C:
				((LayoutSlip)toObject).setConnectC(fromObject,fromPointType);
				break;
			case SLIP_D:
				((LayoutSlip)toObject).setConnectD(fromObject,fromPointType);
                break;
			case TRACK:
				// should never happen, Track Segment links are set in ctor
				log.error("Illegal request to set a Track Segment link");
				break;
			default:
				if ( (toPointType>=TURNTABLE_RAY_OFFSET) && (fromPointType==TRACK) ) {
					((LayoutTurntable)toObject).setRayConnect((TrackSegment)fromObject,
											toPointType-TURNTABLE_RAY_OFFSET);
				}					
		}
	}
	
    /**
     * Return a layout block with the entered name, creating a new one if needed.
	 *   Note that the entered name becomes the user name of the LayoutBlock, and
	 *		a system name is automatically created by LayoutBlockManager if needed.
     */
    public LayoutBlock provideLayoutBlock(String s) {
		LayoutBlock blk;
        if (s.length() < 1) {
            if(!autoAssignBlocks){
                // nothing entered
                return null;
            } else {
                blk = InstanceManager.layoutBlockManagerInstance().createNewLayoutBlock();
                if (blk == null) {
                    log.error("Unable to create a layout block");
                    return null;
                }
                // initialize the new block
                blk.initializeLayoutBlock();
                blk.initializeLayoutBlockRouting();
                blk.setBlockTrackColor(defaultTrackColor);
                blk.setBlockOccupiedColor(defaultOccupiedTrackColor);
                blk.setBlockExtraColor(defaultAlternativeTrackColor);
            }
		} else {
            // check if this Layout Block already exists
            blk = InstanceManager.layoutBlockManagerInstance().getByUserName(s);
            if (blk == null) {
                blk = InstanceManager.layoutBlockManagerInstance().createNewLayoutBlock(null,s);
                if (blk == null) {
                    log.error("Failure to create LayoutBlock '"+s+"'.");
                    return null;
                }
                else {
                    // initialize the new block
                    blk.initializeLayoutBlock();
                    blk.initializeLayoutBlockRouting();
                    blk.setBlockTrackColor(defaultTrackColor);
                    blk.setBlockOccupiedColor(defaultOccupiedTrackColor);
                    blk.setBlockExtraColor(defaultAlternativeTrackColor);
                }
            }
        }
		// set both new and previously existing block
		blk.addLayoutEditor(this);
		setDirty(true);
		blk.incrementUse();
		return blk;
	}
    
	/**
	 * Validates that the supplied occupancy sensor name corresponds to an existing sensor
	 *   and is unique among all blocks.  If valid, returns true and sets the block sensor
	 *   name in the block.  Else returns false, and does nothing to the block.
	 */
	public boolean validateSensor(String sensorName, LayoutBlock blk, Component openFrame) {
		// check if anything entered	
		if (sensorName.length()<1) {
			// no sensor entered
			return false;
		}
		// get a validated sensor corresponding to this name and assigned to block
		Sensor s = blk.validateSensor(sensorName,openFrame);
		if (s==null) {
			// There is no sensor corresponding to this name
			return false;
		}
		return true;
	}
	
    /**
     * Return a layout block with the given name if one exists.
	 * Registers this LayoutEditor with the layout block.
	 * This method is designed to be used when a panel is loaded. The calling
	 *		method must handle whether the use count should be incremented.
     */
    public LayoutBlock getLayoutBlock(String blockID) {
		// check if this Layout Block already exists
		LayoutBlock blk = InstanceManager.layoutBlockManagerInstance().getByUserName(blockID);
		if (blk==null) {
			log.error("LayoutBlock '"+blockID+"' not found when panel loaded");
			return null;
		}
		blk.addLayoutEditor(this);
		return blk;
	}
	
	/** 
	 * Remove object from all Layout Editor temmporary lists of items not part of track schematic
	 */
	protected boolean remove(Object s) {
		boolean found = false;
		for (int i = 0; i<sensorImage.size();i++) {
			if (s == sensorImage.get(i)) {
				sensorImage.remove(i);
				found = true;
				break;
			}
		}
        for (int i = 0; i<sensorList.size();i++) {
			if (s == sensorList.get(i)) {
				sensorList.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<backgroundImage.size();i++) {
			if (s == backgroundImage.get(i)) {
				backgroundImage.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<memoryLabelList.size();i++) {
			if (s == memoryLabelList.get(i)) {
				memoryLabelList.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<signalList.size();i++) {
			if (s == signalList.get(i)) {
				signalList.remove(i);
				found = true;
				break;
			}
		}
        for (int i = 0; i<signalMastList.size();i++) {
			if (s == signalMastList.get(i)) {
				signalMastList.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<multiSensors.size();i++) {
			if (s == multiSensors.get(i)) {
				multiSensors.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<clocks.size();i++) {
			if (s == clocks.get(i)) {
				clocks.remove(i);
				found = true;
				break;
			}
		}
        for (int i = 0; i<signalMastImage.size();i++) {
			if (s == signalMastImage.get(i)) {
				signalMastImage.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<signalHeadImage.size();i++) {
			if (s == signalHeadImage.get(i)) {
				signalHeadImage.remove(i);
				found = true;
				break;
			}
		}
        for (int i = 0; i<signalMastImage.size();i++) {
			if (s == signalMastImage.get(i)) {
				signalMastImage.remove(i);
				found = true;
				break;
			}
		}
		for (int i = 0; i<labelImage.size();i++) {
			if (s == labelImage.get(i)) {
				labelImage.remove(i);
				found = true;
				break;
			}
		}
        super.removeFromContents((Positionable)s);
		if (found) {
			setDirty(true);
			repaint();
		}
        return found;
	}

    public boolean removeFromContents(Positionable l) {
        return remove(l);
    }

	boolean noWarnPositionablePoint = false;
	
    /**
     * Remove a PositionablePoint -- an Anchor or an End Bumper. 
     */
	protected boolean removePositionablePoint(PositionablePoint o) {
		// First verify with the user that this is really wanted
		if (!noWarnPositionablePoint) {
			int selectedValue = JOptionPane.showOptionDialog(this,
					rb.getString("Question2"),rb.getString("WarningTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
					rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
			if (selectedValue == 1) return(false);   // return without creating if "No" response
			if (selectedValue == 2) {
				// Suppress future warnings, and continue
				noWarnPositionablePoint = true;
			}
		}
		// remove from selection information
		if (selectedObject==o) selectedObject = null;
		if (prevSelectedObject==o) prevSelectedObject = null;
		// remove connections if any
		TrackSegment t = o.getConnect1();
		if (t!=null) removeTrackSegment(t);
		t = o.getConnect2();
		if (t!=null) removeTrackSegment(t);
		// delete from array
		for (int i = 0; i<pointList.size();i++) {
			PositionablePoint p = pointList.get(i);
			if (p==o) {
				// found object
				pointList.remove(i);
				setDirty(true);
				repaint();
				return(true);
			}
		}
		return (false);	
	}
	
	boolean noWarnLayoutTurnout = false;
	
    /**
     * Remove a LayoutTurnout
     */
	protected boolean removeLayoutTurnout(LayoutTurnout o) {
		// First verify with the user that this is really wanted
		if (!noWarnLayoutTurnout) {
			int selectedValue = JOptionPane.showOptionDialog(this,
					rb.getString("Question1"),rb.getString("WarningTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
					rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
			if (selectedValue == 1) return(false);   // return without removing if "No" response
			if (selectedValue == 2) {
				// Suppress future warnings, and continue
				noWarnLayoutTurnout = true;
			}
		}
		// remove from selection information
		if (selectedObject==o) selectedObject = null;
		if (prevSelectedObject==o) prevSelectedObject = null;
		// remove connections if any
		TrackSegment t = (TrackSegment)o.getConnectA();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectB();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectC();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectD();
		if (t!=null) removeTrackSegment(t);
		// decrement Block use count(s)
		LayoutBlock b = o.getLayoutBlock();
		if (b!=null) b.decrementUse();
		if ( (o.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ||
				(o.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
					(o.getTurnoutType()==LayoutTurnout.LH_XOVER) ) {
			LayoutBlock b2 = o.getLayoutBlockB();
			if ( (b2!=null) && (b2!=b) ) b2.decrementUse();
			LayoutBlock b3 = o.getLayoutBlockC();
			if ( (b3!=null) && (b3!=b) && (b3!=b2) ) b3.decrementUse();
			LayoutBlock b4 = o.getLayoutBlockD();
			if ( (b4!=null) && (b4!=b) &&
						(b4!=b2) && (b4!=b3) ) b4.decrementUse();
		}	
		// delete from array
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout lt = turnoutList.get(i);
			if (lt==o) {
				// found object
				turnoutList.remove(i);
				setDirty(true);
				repaint();
				return(true);
			}
		}
		return(false);	
	}
	
	boolean noWarnLevelXing = false;
	
    /**
     * Remove a Level Crossing
     */
	protected boolean removeLevelXing (LevelXing o) {
		// First verify with the user that this is really wanted
		if (!noWarnLevelXing) {
			int selectedValue = JOptionPane.showOptionDialog(this,
					rb.getString("Question3"),rb.getString("WarningTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
					rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
			if (selectedValue == 1) return(false);   // return without creating if "No" response
			if (selectedValue == 2) {
				// Suppress future warnings, and continue
				noWarnLevelXing = true;
			}
		}
		// remove from selection information
		if (selectedObject==o) selectedObject = null;
		if (prevSelectedObject==o) prevSelectedObject = null;
		// remove connections if any
		TrackSegment t = (TrackSegment)o.getConnectA();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectB();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectC();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectD();
		if (t!=null) removeTrackSegment(t);
		// decrement block use count if any blocks in use
		LayoutBlock lb = o.getLayoutBlockAC();
		if (lb != null) lb.decrementUse();
		LayoutBlock lbx = o.getLayoutBlockBD();
		if (lbx!=null && lb!=null && lbx!=lb) lb.decrementUse();
		// delete from array
		for (int i = 0; i<xingList.size();i++) {
			LevelXing lx = xingList.get(i);
			if (lx==o) {
				// found object
				xingList.remove(i);
				o.remove();
				setDirty(true);
				repaint();
				return(true);
			}
		}
		return(false);	
	}
    
    boolean noWarnSlip = false;
    
    protected boolean removeLayoutSlip (LayoutTurnout o) {
        if(!(o instanceof LayoutSlip)){
            return false;
        } 
		// First verify with the user that this is really wanted
		if (!noWarnSlip) {
			int selectedValue = JOptionPane.showOptionDialog(this,
					rb.getString("Question5"),rb.getString("WarningTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
					rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
			if (selectedValue == 1) return(false);   // return without creating if "No" response
			if (selectedValue == 2) {
				// Suppress future warnings, and continue
				noWarnSlip = true;
			}
		}
		// remove from selection information
		if (selectedObject==o) selectedObject = null;
		if (prevSelectedObject==o) prevSelectedObject = null;
		// remove connections if any
		TrackSegment t = (TrackSegment)o.getConnectA();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectB();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectC();
		if (t!=null) removeTrackSegment(t);
		t = (TrackSegment)o.getConnectD();
		if (t!=null) removeTrackSegment(t);
		// decrement block use count if any blocks in use
		LayoutBlock lb = o.getLayoutBlock();
		if (lb != null) lb.decrementUse();

		// delete from array
		for (int i = 0; i<slipList.size();i++) {
			LayoutSlip lx = slipList.get(i);
			if (lx==o) {
				// found object
				slipList.remove(i);
				o.remove();
				setDirty(true);
				repaint();
				return(true);
			}
		}
		return(false);	
	}
	
	boolean noWarnTurntable = false;
	
    /**
     * Remove a Layout Turntable
     */
	protected boolean removeTurntable (LayoutTurntable o) {
		// First verify with the user that this is really wanted
		if (!noWarnTurntable) {
			int selectedValue = JOptionPane.showOptionDialog(this,
					rb.getString("Question4"),rb.getString("WarningTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
					rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
			if (selectedValue == 1) return(false);   // return without creating if "No" response
			if (selectedValue == 2) {
				// Suppress future warnings, and continue
				noWarnTurntable = true;
			}
		}
		// remove from selection information
		if (selectedObject==o) selectedObject = null;
		if (prevSelectedObject==o) prevSelectedObject = null;
		// remove connections if any
		for (int j = 0; j<o.getNumberRays();j++) {
			TrackSegment t = o.getRayConnectOrdered(j);
			if (t!=null) removeTrackSegment(t);
		}
		// delete from array
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable lx = turntableList.get(i);
			if (lx==o) {
				// found object
				turntableList.remove(i);
				o.remove();
				setDirty(true);
				repaint();
				return(true);
			}
		}
		return(false);	
	}

    /**
     * Remove a Track Segment 
     */
	protected void removeTrackSegment(TrackSegment o) {
		// save affected blocks
		LayoutBlock block1 = null;
		LayoutBlock block2 = null;
		LayoutBlock block = o.getLayoutBlock();
		// remove any connections
		int type = o.getType1();
		if (type==POS_POINT) {
			PositionablePoint p = (PositionablePoint)(o.getConnect1());
			if (p!=null) {
				p.removeTrackConnection(o);
				if (p.getConnect1()!=null) 
					block1 = p.getConnect1().getLayoutBlock();
				else if (p.getConnect2()!=null)
					block1 = p.getConnect2().getLayoutBlock();
			}
		}
		else {
			block1 = getAffectedBlock(o.getConnect1(),type);
			disconnect(o.getConnect1(),type);
		}
		type = o.getType2();
		if (type==POS_POINT) {
			PositionablePoint p = (PositionablePoint)(o.getConnect2());
			if (p!=null) {
				p.removeTrackConnection(o);
				if (p.getConnect1()!=null) 
					block2 = p.getConnect1().getLayoutBlock();
				else if (p.getConnect2()!=null)
					block2 = p.getConnect2().getLayoutBlock();
			}
		}
		else {
			block2 = getAffectedBlock(o.getConnect2(),type);
			disconnect(o.getConnect2(),type);
		}
		// delete from array
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
			if (t==o) {
				// found object
				trackList.remove(i);
			}
		}
		// update affected blocks
		if (block!=null) {
			// decrement Block use count
			block.decrementUse();
			auxTools.setBlockConnectivityChanged();
			block.updatePaths();
		}
		if ( (block1!=null) && (block1!=block) ) block1.updatePaths();	
		if ( (block2!=null) && (block2!=block) && (block2!=block1) ) block2.updatePaths();
		// 
		setDirty(true);
		repaint();
	}
	
	private void disconnect(Object o, int type) {
		if (o==null) return;
		switch (type) {
			case TURNOUT_A:
				((LayoutTurnout)o).setConnectA(null,NONE);
				break;
			case TURNOUT_B:
				((LayoutTurnout)o).setConnectB(null,NONE);
				break;
			case TURNOUT_C:
				((LayoutTurnout)o).setConnectC(null,NONE);
				break;
			case TURNOUT_D:
				((LayoutTurnout)o).setConnectD(null,NONE);
				break;
			case LEVEL_XING_A:
				((LevelXing)o).setConnectA(null,NONE);
				break;
			case LEVEL_XING_B:
				((LevelXing)o).setConnectB(null,NONE);
				break;
			case LEVEL_XING_C:
				((LevelXing)o).setConnectC(null,NONE);
				break;
			case LEVEL_XING_D:
				((LevelXing)o).setConnectD(null,NONE);
				break;
			case SLIP_A:
				((LayoutSlip)o).setConnectA(null,NONE);
				break;
			case SLIP_B:
				((LayoutSlip)o).setConnectB(null,NONE);
				break;
			case SLIP_C:
				((LayoutSlip)o).setConnectC(null,NONE);
				break;
			case SLIP_D:
				((LayoutSlip)o).setConnectD(null,NONE);
				break;
			default:
				if (type>=TURNTABLE_RAY_OFFSET) {
					((LayoutTurntable)o).setRayConnect(null,type-TURNTABLE_RAY_OFFSET);
				}					
		}
	}
	
	public LayoutBlock getAffectedBlock(Object o, int type) {
		if (o==null) return null;
		switch (type) {
			case TURNOUT_A:
				return ((LayoutTurnout)o).getLayoutBlock();
			case TURNOUT_B:
				return ((LayoutTurnout)o).getLayoutBlockB();
			case TURNOUT_C:
				return ((LayoutTurnout)o).getLayoutBlockC();
			case TURNOUT_D:
				return ((LayoutTurnout)o).getLayoutBlockD();
			case LEVEL_XING_A:
				return ((LevelXing)o).getLayoutBlockAC();
			case LEVEL_XING_B:
				return ((LevelXing)o).getLayoutBlockBD();
			case LEVEL_XING_C:
				return ((LevelXing)o).getLayoutBlockAC();
			case LEVEL_XING_D:
				return ((LevelXing)o).getLayoutBlockBD();
			case SLIP_A:
				return ((LayoutSlip)o).getLayoutBlock();
			case SLIP_B:
				return ((LayoutSlip)o).getLayoutBlock();
			case SLIP_C:
				return ((LayoutSlip)o).getLayoutBlock();
			case SLIP_D:
				return ((LayoutSlip)o).getLayoutBlock();
			case TRACK:
				return ((TrackSegment)o).getLayoutBlock();
		}
		return null;
	}
	
    /**
     * Add a sensor indicator to the Draw Panel
     */
    void addSensor() {
		if ((nextSensor.getText()).trim().length()<=0) {
			JOptionPane.showMessageDialog(this,rb.getString("Error10"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", 
                                                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"),this);
//        l.setActiveIcon(sensorIconEditor.getIcon(0));
//        l.setInactiveIcon(sensorIconEditor.getIcon(1));
//        l.setInconsistentIcon(sensorIconEditor.getIcon(2));
//        l.setUnknownIcon(sensorIconEditor.getIcon(3));
        l.setIcon("SensorStateActive", sensorIconEditor.getIcon(0));
        l.setIcon("SensorStateInactive", sensorIconEditor.getIcon(1));
        l.setIcon("BeanStateInconsistent", sensorIconEditor.getIcon(2));
        l.setIcon("BeanStateUnknown", sensorIconEditor.getIcon(3));
		l.setSensor(nextSensor.getText().trim());
        l.setDisplayLevel(SENSORS);
		//Sensor xSensor = l.getSensor();
		if (l.getSensor() != null) {
			if ( (l.getNamedSensor().getName()==null) || 
					(!(l.getNamedSensor().getName().equals(nextSensor.getText().trim()))) ) 
				nextSensor.setText(l.getNamedSensor().getName());
		}
        nextSensor.setText(l.getNamedSensor().getName());
        setNextLocation(l);
		setDirty(true);
        putItem(l);
    }
    
    public void putSensor(SensorIcon l){
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SENSORS);
    }

    /**
     * Add a signal head to the Panel
     */
    void addSignalHead() {
		// check for valid signal head entry
		String tName = nextSignalHead.getText().trim();
        SignalHead mHead = null;
		if ( (tName!=null) && (!tName.equals("")) ) {
			mHead = InstanceManager.signalHeadManagerInstance().getSignalHead(tName);
			/*if (mHead == null) 
				mHead = InstanceManager.signalHeadManagerInstance().getByUserName(tName);
			else */
			nextSignalHead.setText(tName);
		}
        if (mHead == null) {
			// There is no signal head corresponding to this name
			JOptionPane.showMessageDialog(thisPanel,
					java.text.MessageFormat.format(rb.getString("Error9"),
					new Object[]{tName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		// create and set up signal icon	
        SignalHeadIcon l = new SignalHeadIcon(this);
        l.setSignalHead(tName);
        l.setIcon(rbean.getString("SignalHeadStateRed"), signalIconEditor.getIcon(0));
        l.setIcon(rbean.getString("SignalHeadStateFlashingRed"), signalIconEditor.getIcon(1));
        l.setIcon(rbean.getString("SignalHeadStateYellow"), signalIconEditor.getIcon(2));
        l.setIcon(rbean.getString("SignalHeadStateFlashingYellow"), signalIconEditor.getIcon(3));
        l.setIcon(rbean.getString("SignalHeadStateGreen"), signalIconEditor.getIcon(4));
        l.setIcon(rbean.getString("SignalHeadStateFlashingGreen"), signalIconEditor.getIcon(5));
        l.setIcon(rbean.getString("SignalHeadStateDark"), signalIconEditor.getIcon(6));
        l.setIcon(rbean.getString("SignalHeadStateHeld"), signalIconEditor.getIcon(7));
        l.setIcon(rbean.getString("SignalHeadStateLunar"), signalIconEditor.getIcon(8));
        l.setIcon(rbean.getString("SignalHeadStateFlashingLunar"), signalIconEditor.getIcon(9));
        setNextLocation(l);
		setDirty(true);
        putSignal(l);
    }
    public void putSignal(SignalHeadIcon l) {
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SIGNALS);
    }

    SignalHead getSignalHead(String name) {
        SignalHead sh = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
        if (sh == null) sh = InstanceManager.signalHeadManagerInstance().getByUserName(name);
        if (sh == null) log.warn("did not find a SignalHead named "+name);
        return sh;
    }
    
    void addSignalMast() {
		// check for valid signal head entry
		String tName = nextSignalMast.getText().trim();
        SignalMast mMast = null;
		if ( (tName!=null) && (!tName.equals("")) ) {
			mMast = InstanceManager.signalMastManagerInstance().getSignalMast(tName);
			nextSignalMast.setText(tName);
		}
        if (mMast == null) {
			// There is no signal head corresponding to this name
			JOptionPane.showMessageDialog(thisPanel,
					java.text.MessageFormat.format(rb.getString("Error9"),
					new Object[]{tName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		// create and set up signal icon	
        SignalMastIcon l = new SignalMastIcon(this);
        l.setSignalMast(tName);
        setNextLocation(l);
		setDirty(true);
        putSignalMast(l);
    }
    
    public void putSignalMast(SignalMastIcon l) {
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SIGNALS);
    }

    SignalMast getSignalMast(String name) {
        SignalMast sh = InstanceManager.signalMastManagerInstance().getBySystemName(name);
        if (sh == null) sh = InstanceManager.signalMastManagerInstance().getByUserName(name);
        if (sh == null) log.warn("did not find a SignalMast named "+name);
        return sh;
    }

    /**
     * Add a label to the Draw Panel
     */
    void addLabel() {
        PositionableLabel l = super.addLabel(textLabel.getText().trim());
		setDirty(true);
        l.setForeground(defaultTextColor);
    }

    public void putItem(Positionable l) {
        super.putItem(l);
        if (l instanceof SensorIcon) {
            sensorImage.add((SensorIcon)l);	
            sensorList.add((SensorIcon)l);
        } else if (l instanceof LocoIcon) {
            markerImage.add((LocoIcon)l);	
        } else if (l instanceof SignalHeadIcon) {
            signalHeadImage.add((SignalHeadIcon)l);
            signalList.add((SignalHeadIcon)l);
        }  else if (l instanceof SignalMastIcon) {
            signalMastImage.add((SignalMastIcon)l);
            signalMastList.add((SignalMastIcon)l);
        } else if (l instanceof MemoryIcon) {
            memoryLabelList.add((MemoryIcon)l);
        } else if (l instanceof AnalogClock2Display) {
            clocks.add((AnalogClock2Display)l);	
        } else if (l instanceof MultiSensorIcon) {
            multiSensors.add((MultiSensorIcon)l);	
        } if (l instanceof PositionableLabel) {
            if ( !(((PositionableLabel)l).isBackground()) ) {
			    labelImage.add((PositionableLabel)l);
		    } else {
			    backgroundImage.add((PositionableLabel)l);
            }
		}
    }
    
     /**
     * Add a memory label to the Draw Panel
     */
    void addMemory() {
		if ((textMemory.getText()).trim().length()<=0) {
			JOptionPane.showMessageDialog(this, rb.getString("Error11"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
        MemoryIcon l = new MemoryIcon("   ", this);
        l.setMemory(textMemory.getText().trim());
		Memory xMemory = l.getMemory();
		if (xMemory != null) {
			if ( (xMemory.getUserName() == null) || 
					(!(xMemory.getUserName().equals(textMemory.getText().trim())))  ) {
				// put the system name in the memory field
				textMemory.setText(xMemory.getSystemName());
			}
		}
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        l.setForeground(defaultTextColor);
		setDirty(true);
        putItem(l);
    }
   
	/**
	 * Add a Reporter Icon to the panel
	 */
    void addReporter(String textReporter,int xx,int yy) {
        ReporterIcon l = new ReporterIcon(this);
        l.setReporter(textReporter);
        l.setLocation(xx,yy);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
		setDirty(true);
        putItem(l);
   }

    /**
     * Add an icon to the target
     */
    void addIcon() {
        PositionableLabel l = new PositionableLabel(iconEditor.getIcon(0), this);
        setNextLocation(l);
        l.setDisplayLevel(ICONS);
		setDirty(true);
        putItem(l);
        l.updateSize();
    }
    
    /**
     * Add a loco marker to the target
     */
    public LocoIcon addLocoIcon (String name){
    	LocoIcon l = new LocoIcon(this);
		Point2D pt = windowCenter();
        l.setLocation( (int)pt.getX(), (int)pt.getY() );
        putLocoIcon(l, name);
        l.setPositionable(true);
        return l;
     }
    
    public void putLocoIcon(LocoIcon l, String name) {
    	super.putLocoIcon(l, name);
		markerImage.add(l);  
    }
    
    JFileChooser inputFileChooser;
	
	/** 
	 * Add a background image
	 */
	public void addBackground() {
        if (inputFileChooser == null) {
            inputFileChooser = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"resources"+java.io.File.separator+"icons");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            filt.addExtension("gif");
            filt.addExtension("jpg");
            inputFileChooser.setFileFilter(filt);
        }
        inputFileChooser.rescanCurrentDirectory();
        
        int retVal = inputFileChooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
//        NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
//                                       inputFileChooser.getSelectedFile().getPath());
 
        String name = inputFileChooser.getSelectedFile().getPath();

        // convert to portable path
        name = jmri.util.FileUtil.getPortableFilename(name);
        
        // setup icon
        backgroundImage.add(super.setUpBackground(name));
	}
	
	/**
	 * Remove a background image from the list of background images
	 */
	protected void removeBackground(PositionableLabel b) {
		for (int i=0; i<backgroundImage.size(); i++) {
			if (b == backgroundImage.get(i)) {
				backgroundImage.remove(i);
				setDirty(true);
				return;
			}
		}
	}

    /**
     * Invoke a window to allow you to add a MultiSensor indicator to the target
     */
	private int multiLocX;
	private int multiLocY;
    void startMultiSensor() {
		multiLocX = xLoc;
		multiLocY = yLoc;
        if (multiSensorFrame == null) {
            // create a common edit frame
            multiSensorFrame = new MultiSensorIconFrame(this);
            multiSensorFrame.initComponents();
            multiSensorFrame.pack();
        }  
        multiSensorFrame.setVisible(true);
    }
    // Invoked when window has new multi-sensor ready
    public void addMultiSensor(MultiSensorIcon l) {
		l.setLocation(multiLocX,multiLocY);
		setDirty(true);
        putItem(l);
		multiSensorFrame = null;
    }
 
    /**
     * Set object location and size for icon and label object as it is created.
     * Size comes from the preferredSize; location comes
     * from the fields where the user can spec it.
     */
    protected void setNextLocation(Positionable obj) {
        obj.setLocation(xLoc,yLoc);
    }
	
	public ConnectivityUtil getConnectivityUtil() {
		if (conTools == null) {
			conTools = new ConnectivityUtil(thisPanel);
		}
		if (conTools==null) log.error("Unable to establish link to Connectivity Tools for Layout Editor panel "+layoutName);
		return conTools;
	}
	public LayoutEditorTools getLETools() {
		if (tools == null) {
			tools = new LayoutEditorTools(thisPanel);
		}
		if (tools==null) log.error("Unable to establish link to Layout Editor Tools for Layout Editor panel "+layoutName);
		return tools;
	}
	
	/** 
	 * Invoked by DeletePanel menu item
	 *     Validate user intent before deleting
	 */
	public boolean deletePanel() {
		// verify deletion
		if (!super.deletePanel()) return false;   // return without deleting if "No" response
		
		turnoutList.clear();
		trackList.clear();
		pointList.clear();
		xingList.clear();
        slipList.clear();
		turntableList.clear();
        return true;
    }

    /**
     *  Control whether target panel items are editable.
     *  Does this by invoke the {@link Editor#setAllEditable} function of
     *  the parent class. This also controls the relevant pop-up menu items
     *  (which are the primary way that items are edited).
     * @param editable true for editable.
     */
    public void setAllEditable(boolean editable) {
    	int restoreScroll = _scrollState;
        super.setAllEditable(editable);
        topEditBar.setVisible(editable);
        setShowHidden(editable);
        if (editable) {
        	setScroll(SCROLL_BOTH);
        	_scrollState = restoreScroll;
        	helpBar.setVisible(showHelpBar);
        } else {
        	setScroll(_scrollState);
        	helpBar.setVisible(false);
        }
        awaitingIconChange = false;
        editModeItem.setSelected(editable);
        repaint();
    }

    /**
     *  Control whether panel items are positionable.
	 *  Markers are always positionable.
     * @param state true for positionable.
     */
    public void setAllPositionable(boolean state) {
        super.setAllPositionable(state);
		for (int i = 0; i<markerImage.size(); i++) {
			((Positionable)markerImage.get(i)).setPositionable(true);
		}
    }

    
    /**
     *  Control whether target panel items are controlling layout items.
     *  Does this by invoke the {@link Positionable#setControlling} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items.
     * @param state true for controlling.
     */
    public void setTurnoutAnimation(boolean state) {
        if (animationItem.isSelected()!=state) animationItem.setSelected(state);
        animatingLayout = state;
		repaint();
    }
	
    public boolean isAnimating() {
        return animatingLayout;
    }
	public int getLayoutWidth() {return panelWidth;}
	public int getLayoutHeight() {return panelHeight;}
	public int getWindowWidth() {return windowWidth;}
	public int getWindowHeight() {return windowHeight;}
	public int getUpperLeftX() {return upperLeftX;}
	public int getUpperLeftY() {return upperLeftY;}
	public boolean getScroll() {
        // deprecated but kept to allow opening files
        // on version 2.5.1 and earlier
        if (_scrollState==SCROLL_NONE) return false;
        else return true;
    }
	public int getMainlineTrackWidth() {
		int wid = (int)mainlineTrackWidth;
		return wid;
	}
	public int getSideTrackWidth() {
		int wid = (int)sideTrackWidth;
		return wid;
	}
	public double getXScale() {return xScale;}
	public double getYScale() {return yScale;}
	public String getDefaultTrackColor() {return colorToString(defaultTrackColor);}
	public String getDefaultOccupiedTrackColor() {return colorToString(defaultOccupiedTrackColor);}
	public String getDefaultAlternativeTrackColor() {return colorToString(defaultAlternativeTrackColor);}
    public String getDefaultTextColor() {return colorToString(defaultTextColor);}
	public String getTurnoutCircleColor() {return colorToString(turnoutCircleColor);}
	public int getTurnoutCircleSize() {return turnoutCircleSize;}
	public boolean getTurnoutDrawUnselectedLeg() {return turnoutDrawUnselectedLeg;}
	public String getLayoutName() {return layoutName;}
	public boolean getShowHelpBar() {return showHelpBar;}
	public boolean getDrawGrid() {return drawGrid;}
	public boolean getSnapOnAdd() {return snapToGridOnAdd;}
	public boolean getSnapOnMove() {return snapToGridOnMove;}
	public boolean getAntialiasingOn() {return antialiasingOn;}
	public boolean getTurnoutCircles() {return turnoutCirclesWithoutEditMode;}
	public boolean getTooltipsNotEdit() {return tooltipsWithoutEditMode;}
	public boolean getTooltipsInEdit() {return tooltipsInEditMode;}
    public boolean getAutoBlockAssignment() { return autoAssignBlocks;}
	public void setLayoutDimensions(int windowW, int windowH, int x, int y, int panelW, int panelH) {
		upperLeftX = x;
		upperLeftY = y;
		windowWidth = windowW;
		windowHeight = windowH;
		panelWidth = panelW;
		panelHeight = panelH;
		setTargetPanelSize(panelWidth,panelHeight);
		setLocation(upperLeftX,upperLeftY);
		setSize(windowWidth,windowHeight);		
		log.debug("setLayoutDimensions Position - "+upperLeftX+","+upperLeftY+" windowSize - "+windowWidth+","+windowHeight+" panelSize - "+panelWidth+","+panelHeight);		
	}
	public void setMainlineTrackWidth(int w) {mainlineTrackWidth = w;}
	public void setSideTrackWidth(int w) {sideTrackWidth = w;}
	public void setDefaultTrackColor(String color) {
		defaultTrackColor = stringToColor(color);
		setOptionMenuTrackColor();
	}
    public void setDefaultOccupiedTrackColor(String color) {
		defaultOccupiedTrackColor = stringToColor(color);
		setOptionMenuTrackColor();
	}
    public void setDefaultAlternativeTrackColor(String color) {
		defaultAlternativeTrackColor = stringToColor(color);
		setOptionMenuTrackColor();
	}
	public void setTurnoutCircleColor(String color) {
		turnoutCircleColor = stringToColor(color);
		setOptionMenuTurnoutCircleColor();
	}
	public void setTurnoutCircleSize(int size) {
		turnoutCircleSize = size;
		setOptionMenuTurnoutCircleSize();
	}
	public void setTurnoutDrawUnselectedLeg(boolean state) {
		if (turnoutDrawUnselectedLeg != state) {
			turnoutDrawUnselectedLeg = state;
			turnoutDrawUnselectedLegItem.setSelected(turnoutDrawUnselectedLeg);
		}
	}
	public void setDefaultTextColor(String color) {
		defaultTextColor = stringToColor(color);
		setOptionMenuTextColor();
	}
	public void setDefaultBackgroundColor(String color) {
		defaultBackgroundColor = stringToColor(color);
		setOptionMenuBackgroundColor();
	}
	public void setXScale(double xSc) {xScale = xSc;}
	public void setYScale(double ySc) {yScale = ySc;}
	public void setLayoutName(String name) {layoutName = name;}
	public void setShowHelpBar(boolean state) {
		if (showHelpBar!=state) {
			showHelpBar = state;
			showHelpItem.setSelected(showHelpBar);
			if (isEditable()) {
				helpBar.setVisible(showHelpBar);
			}
		}
	}
	public void setDrawGrid(boolean state) {
		if (drawGrid != state) {
			drawGrid = state;
			showGridItem.setSelected(drawGrid);
		}
	}
	public void setSnapOnAdd(boolean state) {
		if (snapToGridOnAdd != state) {
			snapToGridOnAdd = state;
			snapToGridOnAddItem.setSelected(snapToGridOnAdd);
		}
	}
	public void setSnapOnMove(boolean state) {
		if (snapToGridOnMove != state) {
			snapToGridOnMove = state;
			snapToGridOnMoveItem.setSelected(snapToGridOnMove);
		}
	}
	public void setAntialiasingOn(boolean state) {
		if (antialiasingOn != state) {
			antialiasingOn = state;
			antialiasingOnItem.setSelected(antialiasingOn);
		}
	}
	public void setTurnoutCircles(boolean state) {
		if (turnoutCirclesWithoutEditMode != state) {
			turnoutCirclesWithoutEditMode = state;
			turnoutCirclesOnItem.setSelected(turnoutCirclesWithoutEditMode);
		}
	}
    
    public void setAutoBlockAssignment(boolean boo){
        if(autoAssignBlocks != boo){
            autoAssignBlocks = boo;
            autoAssignBlocksItem.setSelected(autoAssignBlocks);
        }
    }
	public void setTooltipsNotEdit(boolean state) {
		if (tooltipsWithoutEditMode != state) {
			tooltipsWithoutEditMode = state;
			setTooltipSubMenu();
		}
	}
	public void setTooltipsInEdit(boolean state) {
		if (tooltipsInEditMode != state) {
			tooltipsInEditMode = state;
			setTooltipSubMenu();
		}
	}
	private void setTooltipSubMenu() {
        tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
	}
	// accessor routines for turnout size parameters	
	public void setTurnoutBX(double bx) {
		turnoutBX = bx;
		setDirty(true);
	}
	public double getTurnoutBX() {return turnoutBX;}
	public void setTurnoutCX(double cx) {
		turnoutCX = cx;
		setDirty(true);
	}
	public double getTurnoutCX() {return turnoutCX;}
	public void setTurnoutWid(double wid) {
		turnoutWid = wid;
		setDirty(true);
	}
	public double getTurnoutWid() {return turnoutWid;}
	public void setXOverLong(double lg) {
		xOverLong = lg;
		setDirty(true);
	}
	public double getXOverLong() {return xOverLong;}
	public void setXOverHWid(double hwid) {
		xOverHWid =  hwid;
		setDirty(true);
	}
	public double getXOverHWid() {return xOverHWid;}
	public void setXOverShort(double sh) {
		xOverShort =  sh;
		setDirty(true);
	}
	public double getXOverShort() {return xOverShort;}
	// reset turnout sizes to program defaults
	private void resetTurnoutSize() {
		turnoutBX = turnoutBXDefault;
		turnoutCX = turnoutCXDefault;
		turnoutWid = turnoutWidDefault;
		xOverLong = xOverLongDefault;
		xOverHWid = xOverHWidDefault;
		xOverShort = xOverShortDefault;
		setDirty(true);
	}
		
	// final initialization routine for loading a LayoutEditor
	public void setConnections() {
		// initialize TrackSegments if any
		if (trackList.size()>0) {
			for (int i = 0; i<trackList.size(); i++) {
				(trackList.get(i)).setObjects(this);
			}
		}
		// initialize PositionablePoints if any
		if (pointList.size()>0) {
			for (int i = 0; i<pointList.size(); i++) {
				(pointList.get(i)).setObjects(this);
			}
		}
		// initialize LevelXings if any
		if (xingList.size()>0) {
			for (int i = 0; i<xingList.size(); i++) {
				(xingList.get(i)).setObjects(this);
			}
		}
		// initialize LevelXings if any
		if (slipList.size()>0) {
			for (LayoutSlip l : slipList) {
				l.setObjects(this);
			}
		}
		// initialize LayoutTurntables if any
		if (turntableList.size()>0) {
			for (int i = 0; i<turntableList.size(); i++) {
				(turntableList.get(i)).setObjects(this);
			}
		}
		// initialize LayoutTurnouts if any
		if (turnoutList.size()>0) {
			for (int i = 0; i<turnoutList.size(); i++) {
				(turnoutList.get(i)).setObjects(this);
			}
		}
		auxTools.initializeBlockConnectivity();
		log.debug("Initializing Block Connectivity for "+layoutName);
		// reset the panel changed bit
		resetDirty();
	}
	
	// utility routines
	public static String colorToString(Color color) {
		if (color == null) return "track";
		else if (color.equals(Color.black)) return "black";
		else if (color.equals(Color.darkGray)) return "darkGray";
		else if (color.equals(Color.gray)) return "gray";
		else if (color.equals(Color.lightGray)) return "lightGray";
		else if (color.equals(Color.white)) return "white";
		else if (color.equals(Color.red)) return "red";
		else if (color.equals(Color.pink)) return "pink";
		else if (color.equals(Color.orange)) return "orange";
		else if (color.equals(Color.yellow)) return "yellow";
		else if (color.equals(Color.green)) return "green";
		else if (color.equals(Color.blue)) return "blue";
		else if (color.equals(Color.magenta)) return "magenta";
		else if (color.equals(Color.cyan)) return "cyan";
		log.error ("unknown color sent to colorToString");
		return "black";
	}
	public static Color stringToColor(String string) {
		if(string.equals("black")) return Color.black;
		else if (string.equals("darkGray")) return Color.darkGray;	
		else if (string.equals("gray")) return Color.gray;	
		else if (string.equals("lightGray")) return Color.lightGray;	
		else if (string.equals("white")) return Color.white;	
		else if (string.equals("red")) return Color.red;	
		else if (string.equals("pink")) return Color.pink;	
		else if (string.equals("orange")) return Color.orange;	
		else if (string.equals("yellow")) return Color.yellow;	
		else if (string.equals("green")) return Color.green;
		else if (string.equals("blue")) return Color.blue;	
		else if (string.equals("magenta")) return Color.magenta;	
		else if (string.equals("cyan")) return Color.cyan;	
		else if (string.equals("track")) return null;	
		log.error("unknown color text '"+string+"' sent to stringToColor");
		return Color.black;
	}
	public TrackSegment findTrackSegmentByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<trackList.size(); i++) {
			TrackSegment t = trackList.get(i);
			if (t.getID().equals(name)) {
				return t;
			}
		}
		return null;
	}
	public PositionablePoint findPositionablePointByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<pointList.size(); i++) {
			PositionablePoint p = pointList.get(i);
			if (p.getID().equals(name)) {
				return p;
			}
		}
		return null;
	}
	public PositionablePoint findPositionablePointAtTrackSegments(TrackSegment tr1, TrackSegment tr2) {
		for (int i = 0; i<pointList.size(); i++) {
			PositionablePoint p = pointList.get(i);
			if ( ( (p.getConnect1()==tr1) && (p.getConnect2()==tr2) ) ||
					( (p.getConnect1()==tr2) && (p.getConnect2()==tr1) ) ) {
				return p;
			}
		}
		return null;
	}

    /**
    * Returns an array list of track segments matching the block name.
    */
    public ArrayList<TrackSegment> findTrackSegmentByBlock(String name) {
		if (name.length()<=0) return null;
        ArrayList<TrackSegment> ts = new ArrayList<TrackSegment>();
		for (int i = 0; i<trackList.size(); i++) {
			TrackSegment t = trackList.get(i);
			if (t.getBlockName().equals(name)) {
				ts.add(t);
			}
		}
		return ts;
	}
    
    public PositionablePoint findPositionablePointByEastBoundSignal(String signalName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getEastBoundSignal().equals(signalName))
                return p;
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignal(String signalName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getWestBoundSignal().equals(signalName))
                return p;
        }
        return null;
    }

    public PositionablePoint findPositionablePointByEastBoundSignalMast(String signalMastName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getEastBoundSignalMast().equals(signalMastName))
                return p;
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignalMast(String signalMastName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getWestBoundSignalMast().equals(signalMastName))
                return p;

        }
        return null;
    }
    
    public LayoutTurnout findLayoutTurnoutBySignalMast(String signalMastName){
        for(int i = 0; i<turnoutList.size(); i++){
            LayoutTurnout t = turnoutList.get(i);
            if((t.getSignalAMast().equals(signalMastName)) ||
                (t.getSignalBMast().equals(signalMastName)) ||
                (t.getSignalCMast().equals(signalMastName)) ||
                (t.getSignalDMast().equals(signalMastName)))
                return t;
        }
        return null;
    }
    
    public LayoutTurnout findLayoutTurnoutBySensor(String sensorName){
        for(int i = 0; i<turnoutList.size(); i++){
            LayoutTurnout t = turnoutList.get(i);
            if((t.getSensorA().equals(sensorName)) ||
                (t.getSensorB().equals(sensorName)) ||
                (t.getSensorC().equals(sensorName)) ||
                (t.getSensorD().equals(sensorName)))
                return t;
        }
        return null;
    }
    
    public LevelXing findLevelXingBySignalMast(String signalMastName){
        for(int i = 0; i<xingList.size(); i++){
            LevelXing l = xingList.get(i);
            if((l.getSignalAMastName().equals(signalMastName)) ||
                (l.getSignalBMastName().equals(signalMastName)) ||
                (l.getSignalCMastName().equals(signalMastName)) ||
                (l.getSignalDMastName().equals(signalMastName)))
                return l;
        }
        return null;
    }
    
    public LevelXing findLevelXingBySensor(String sensorName){
        for(int i = 0; i<xingList.size(); i++){
            LevelXing l = xingList.get(i);
            if((l.getSensorAName().equals(sensorName)) ||
                (l.getSensorBName().equals(sensorName)) ||
                (l.getSensorCName().equals(sensorName)) ||
                (l.getSensorDName().equals(sensorName)))
                return l;
        }
        return null;
    }

    public LayoutSlip findLayoutSlipBySignalMast(String signalMastName){
        for(LayoutSlip l: slipList){
            if((l.getSignalAMast().equals(signalMastName)) ||
                (l.getSignalBMast().equals(signalMastName)) ||
                (l.getSignalCMast().equals(signalMastName)) ||
                (l.getSignalDMast().equals(signalMastName)))
                return l;
        }
        return null;
    }
    
    public LayoutSlip findLayoutSlipBySensor(String sensorName){
        for(LayoutSlip l: slipList){
            if((l.getSensorA().equals(sensorName)) ||
                (l.getSensorB().equals(sensorName)) ||
                (l.getSensorC().equals(sensorName)) ||
                (l.getSensorD().equals(sensorName)))
                return l;
        }
        return null;
    }
    
    public PositionablePoint findPositionablePointByEastBoundSensor(String sensorName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getEastBoundSensor().equals(sensorName))
                return p;
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSensor(String sensorName){
        for (int i = 0; i<pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);
            if (p.getWestBoundSensor().equals(sensorName))
                return p;

        }
        return null;
    }

	public LayoutTurnout findLayoutTurnoutByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<turnoutList.size(); i++) {
			LayoutTurnout t = turnoutList.get(i);
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}
    
    public LayoutTurnout findLayoutTurnoutByTurnoutName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<turnoutList.size(); i++) {
			LayoutTurnout t = turnoutList.get(i);
			if (t.getTurnoutName().equals(name)) {
				return t;
			}
		}
		return null;
	}
	public LevelXing findLevelXingByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<xingList.size(); i++) {
			LevelXing x = xingList.get(i);
			if (x.getID().equals(name)) {
				return x;
			}
		}
		return null;
	}
    
    public LayoutSlip findLayoutSlipByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<slipList.size(); i++) {
			LayoutSlip x = slipList.get(i);
			if (x.getName().equals(name)) {
				return x;
			}
		}
		return null;
	}
    
	public LayoutTurntable findLayoutTurntableByName(String name) {
		if (name.length()<=0) return null;
		for (int i = 0; i<turntableList.size(); i++) {
			LayoutTurntable x = turntableList.get(i);
			if (x.getID().equals(name)) {
				return x;
			}
		}
		return null;
	}
	public Object findObjectByTypeAndName(int type,String name) {
		if (name.length()<=0) return null;
		switch (type) {
			case NONE:
				return null;
			case POS_POINT:
				return findPositionablePointByName(name);
			case TURNOUT_A:
			case TURNOUT_B:
			case TURNOUT_C:
			case TURNOUT_D:
				return findLayoutTurnoutByName(name);
			case LEVEL_XING_A:
			case LEVEL_XING_B:
			case LEVEL_XING_C:
			case LEVEL_XING_D:
				return findLevelXingByName(name);
			case SLIP_A:
			case SLIP_B:
			case SLIP_C:
			case SLIP_D:
				return findLayoutSlipByName(name);
			case TRACK:
				return findTrackSegmentByName(name);
			default:
				if (type>=TURNTABLE_RAY_OFFSET)
					return findLayoutTurntableByName(name);
		}
		log.error("did not find Object '"+name+"' of type "+type);
		return null;
	}

    /**
     *  Special internal class to allow drawing of layout to a JLayeredPane
     *  This is the 'target' pane where the layout is displayed
     */
    protected void paintTargetPanel(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        //drawPositionableLabelBorder(g2);
        // Optional antialising, to eliminate (reduce) staircase on diagonal lines
        if(antialiasingOn) g2.setRenderingHints(antialiasing);
        if (isEditable() && drawGrid) drawPanelGrid(g2);
        g2.setColor(defaultTrackColor);			
        main = false;
        g2.setStroke(new BasicStroke(sideTrackWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        drawHiddenTrack(g2);
        drawDashedTrack(g2,false);
        drawDashedTrack(g2,true);
        drawSolidTrack(g2,false);
        drawSolidTrack(g2,true);
        drawTurnouts(g2);
        drawXings(g2);
        drawSlips(g2);
        drawTurntables(g2);
        drawTrackInProgress(g2);
        g2.setStroke(new BasicStroke(1.0F,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        drawPoints(g2);
        
        if (isEditable()) {
            drawTurnoutRects(g2);
            drawXingRects(g2);
            drawSlipRects(g2);
            drawTrackOvals(g2);
            drawSelectionRect(g2);
            drawTurntableRects(g2);
            drawMemoryRects(g2);
            drawTrackCircleCentre(g2);
            highLightSelection(g2);
        }
		else if (turnoutCirclesWithoutEditMode) {
			drawTurnoutCircles(g2);
		}
    }
	
	boolean main = true;
	float trackWidth = sideTrackWidth;
	
	protected void setTrackStrokeWidth(Graphics2D g2, boolean need) {
		if (main == need) return;
		main = need;
		// change track stroke width
		if ( main ) {
			trackWidth = mainlineTrackWidth;
			g2.setStroke(new BasicStroke(mainlineTrackWidth,BasicStroke.CAP_BUTT,
															BasicStroke.JOIN_ROUND));
		}
		else {
			trackWidth = sideTrackWidth;
			g2.setStroke(new BasicStroke(sideTrackWidth,BasicStroke.CAP_BUTT,
															BasicStroke.JOIN_ROUND));
		}
	}
		
	protected void drawTurnouts(Graphics2D g2)
	{
		//float trackWidth = sideTrackWidth;
		// loop over all defined turnouts
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
			LayoutBlock b = t.getLayoutBlock();
			if (b!=null) {
				g2.setColor(b.getBlockColor());
			}
			else {
				g2.setColor(defaultTrackColor);
			}
            if(!(t.getHidden() && !isEditable())){
			if (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) {
				//  double crossover turnout
				Turnout t1 = t.getTurnout();
				if (t1==null) {
					// no physical turnout linked - draw A corner
					setTrackStrokeWidth(g2,t.isMainlineA());
					g2.draw(new Line2D.Double(t.getCoordsA(),
										midpoint(t.getCoordsA(),t.getCoordsB())));
					setTrackStrokeWidth(g2,false);
					g2.draw(new Line2D.Double(t.getCoordsA(),
										midpoint(t.getCoordsA(),t.getCoordsC())));
					// change block if needed
					b = t.getLayoutBlockB();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw B corner
					setTrackStrokeWidth(g2,t.isMainlineB());
					g2.draw(new Line2D.Double(t.getCoordsB(),
										midpoint(t.getCoordsA(),t.getCoordsB())));
					setTrackStrokeWidth(g2,false);
					g2.draw(new Line2D.Double(t.getCoordsB(),
										midpoint(t.getCoordsB(),t.getCoordsD())));
					// change block if needed
					b = t.getLayoutBlockC();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw C corner
					setTrackStrokeWidth(g2,t.isMainlineC());
					g2.draw(new Line2D.Double(t.getCoordsC(),
										midpoint(t.getCoordsC(),t.getCoordsD())));
					setTrackStrokeWidth(g2,false);
					g2.draw(new Line2D.Double(t.getCoordsC(),
										midpoint(t.getCoordsA(),t.getCoordsC())));
					// change block if needed
					b = t.getLayoutBlockD();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw D corner
					setTrackStrokeWidth(g2,t.isMainlineD());
					g2.draw(new Line2D.Double(t.getCoordsD(),
										midpoint(t.getCoordsC(),t.getCoordsD())));
					setTrackStrokeWidth(g2,false);
					g2.draw(new Line2D.Double(t.getCoordsD(),
										midpoint(t.getCoordsB(),t.getCoordsD())));
				}
				else {
					int state = Turnout.CLOSED;
					if (animatingLayout)
						state = t1.getKnownState();
					if ( state == Turnout.CLOSED ) {
						// continuing path - not crossed over
						setTrackStrokeWidth(g2,t.isMainlineA());
						g2.draw(new Line2D.Double(t.getCoordsA(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						g2.draw(new Line2D.Double(t.getCoordsA(),
												third(t.getCoordsA(),t.getCoordsC())));
                                                
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						g2.draw(new Line2D.Double(t.getCoordsB(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						g2.draw(new Line2D.Double(t.getCoordsB(),
												third(t.getCoordsB(),t.getCoordsD())));
                                                
						b = t.getLayoutBlockC();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
                        setTrackStrokeWidth(g2,t.isMainlineC());
						g2.draw(new Line2D.Double(t.getCoordsC(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						g2.draw(new Line2D.Double(t.getCoordsC(),
												third(t.getCoordsC(),t.getCoordsA())));
                                                
						b = t.getLayoutBlockD();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						g2.draw(new Line2D.Double(t.getCoordsD(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						g2.draw(new Line2D.Double(t.getCoordsD(),
												third(t.getCoordsD(),t.getCoordsB())));
					}
					else if (state == Turnout.THROWN) {
						// diverting (crossed) path 
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						setTrackStrokeWidth(g2,t.isMainlineA());
						g2.draw(new Line2D.Double(t.getCoordsA(),
												third(t.getCoordsA(),t.getCoordsB())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockColor());
						g2.draw(new Line2D.Double(t.getCoordsA(),t.getCoordsCenter()));
                        
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockTrackColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						g2.draw(new Line2D.Double(t.getCoordsB(),
												third(t.getCoordsB(),t.getCoordsA())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockColor());
                        
						g2.draw(new Line2D.Double(t.getCoordsB(),t.getCoordsCenter()));
						
                        b = t.getLayoutBlockC();
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineC());
						g2.draw(new Line2D.Double(t.getCoordsC(),
												third(t.getCoordsC(),t.getCoordsD())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockColor());
						g2.draw(new Line2D.Double(t.getCoordsC(),t.getCoordsCenter()));
						
                        b = t.getLayoutBlockD();
						
                        if (b!=null) g2.setColor(b.getBlockTrackColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						g2.draw(new Line2D.Double(t.getCoordsD(),
												third(t.getCoordsD(),t.getCoordsC())));
						setTrackStrokeWidth(g2,false);
                        if (b!=null) g2.setColor(b.getBlockColor());
						g2.draw(new Line2D.Double(t.getCoordsD(),t.getCoordsCenter()));
					}
					else {
						// unknown or inconsistent
						setTrackStrokeWidth(g2,t.isMainlineA());
						g2.draw(new Line2D.Double(t.getCoordsA(),
												third(t.getCoordsA(),t.getCoordsB())));
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(t.getCoordsA(),
												third(t.getCoordsA(),t.getCoordsC())));
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						g2.draw(new Line2D.Double(t.getCoordsB(),
												third(t.getCoordsB(),t.getCoordsA())));
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(t.getCoordsB(),
												third(t.getCoordsB(),t.getCoordsD())));
						b = t.getLayoutBlockC();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineC());
						g2.draw(new Line2D.Double(t.getCoordsC(),
												third(t.getCoordsC(),t.getCoordsD())));
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(t.getCoordsC(),
												third(t.getCoordsC(),t.getCoordsA())));
						b = t.getLayoutBlockD();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						g2.draw(new Line2D.Double(t.getCoordsD(),
												third(t.getCoordsD(),t.getCoordsC())));
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(t.getCoordsD(),
												third(t.getCoordsD(),t.getCoordsB())));
					}
				}
			}
			else if ( (t.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
							(t.getTurnoutType()==LayoutTurnout.LH_XOVER) ) {
				//  LH and RH crossover turnouts
				int ttype = t.getTurnoutType();
				Turnout t1 = t.getTurnout();
				if (t1==null) {
					// no physical turnout linked - draw A corner
					setTrackStrokeWidth(g2,t.isMainlineA());
					g2.draw(new Line2D.Double(t.getCoordsA(),
										midpoint(t.getCoordsA(),t.getCoordsB())));
					if (ttype == LayoutTurnout.RH_XOVER) {
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(midpoint(t.getCoordsA(),t.getCoordsB()),
																t.getCoordsCenter()));
					}
					// change block if needed
					b = t.getLayoutBlockB();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw B corner
					setTrackStrokeWidth(g2,t.isMainlineB());
					g2.draw(new Line2D.Double(t.getCoordsB(),
										midpoint(t.getCoordsA(),t.getCoordsB())));
					if (ttype == LayoutTurnout.LH_XOVER) {
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(midpoint(t.getCoordsA(),t.getCoordsB()),
																t.getCoordsCenter()));
					}
					// change block if needed
					b = t.getLayoutBlockC();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw C corner
					setTrackStrokeWidth(g2,t.isMainlineC());
					g2.draw(new Line2D.Double(t.getCoordsC(),
										midpoint(t.getCoordsC(),t.getCoordsD())));
					if (ttype == LayoutTurnout.RH_XOVER) {										
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(midpoint(t.getCoordsC(),t.getCoordsD()),
																t.getCoordsCenter()));
					}
					// change block if needed
					b = t.getLayoutBlockD();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
					// draw D corner
					setTrackStrokeWidth(g2,t.isMainlineD());
					g2.draw(new Line2D.Double(t.getCoordsD(),
										midpoint(t.getCoordsC(),t.getCoordsD())));
					if (ttype == LayoutTurnout.LH_XOVER) {
						setTrackStrokeWidth(g2,false);
						g2.draw(new Line2D.Double(midpoint(t.getCoordsC(),t.getCoordsD()),
																t.getCoordsCenter()));
					}
				}
				else {
					int state = Turnout.CLOSED;
					if (animatingLayout)
						state = t1.getKnownState();
					if ( state == Turnout.CLOSED ) {
						// continuing path - not crossed over
						setTrackStrokeWidth(g2,t.isMainlineA());
						g2.draw(new Line2D.Double(t.getCoordsA(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
						if (ttype == LayoutTurnout.RH_XOVER) {										
							setTrackStrokeWidth(g2,false);
                            if (b!=null) g2.setColor(b.getBlockTrackColor());
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsA(),t.getCoordsB()))));
						}
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						g2.draw(new Line2D.Double(t.getCoordsB(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
						if (ttype == LayoutTurnout.LH_XOVER) {										
							setTrackStrokeWidth(g2,false);
                            if (b!=null) g2.setColor(b.getBlockTrackColor());
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsA(),t.getCoordsB()))));
						}
						b = t.getLayoutBlockC();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineC());
						g2.draw(new Line2D.Double(t.getCoordsC(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
						if (ttype == LayoutTurnout.RH_XOVER) {										
							setTrackStrokeWidth(g2,false);
                            if (b!=null) g2.setColor(b.getBlockTrackColor());
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsC(),t.getCoordsD()))));
						}
						b = t.getLayoutBlockD();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						g2.draw(new Line2D.Double(t.getCoordsD(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
						if (ttype == LayoutTurnout.LH_XOVER) {										
							setTrackStrokeWidth(g2,false);
                            if (b!=null) g2.setColor(b.getBlockTrackColor());
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsC(),t.getCoordsD()))));
						}
					}
					else if (state == Turnout.THROWN) {
						// diverting (crossed) path 
						setTrackStrokeWidth(g2,t.isMainlineA());
						if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsA(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(midpoint(t.getCoordsA(),t.getCoordsB()),
																t.getCoordsCenter()));
						}
						else if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsA(),
												fourth(t.getCoordsA(),t.getCoordsB())));
						}
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsB(),
												midpoint(t.getCoordsB(),t.getCoordsA())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(midpoint(t.getCoordsA(),t.getCoordsB()),
																t.getCoordsCenter()));
						}
						else if (ttype == LayoutTurnout.RH_XOVER) {	
							g2.draw(new Line2D.Double(t.getCoordsB(),
												fourth(t.getCoordsB(),t.getCoordsA())));
						}									
						b = t.getLayoutBlockC();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineC());
						if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsC(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(midpoint(t.getCoordsC(),t.getCoordsD()),
																t.getCoordsCenter()));
						}
						else if (ttype == LayoutTurnout.LH_XOVER) {	
							g2.draw(new Line2D.Double(t.getCoordsC(),
												fourth(t.getCoordsC(),t.getCoordsD())));
						}
						b = t.getLayoutBlockD();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsD(),
												midpoint(t.getCoordsD(),t.getCoordsC())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(midpoint(t.getCoordsC(),t.getCoordsD()),
																t.getCoordsCenter()));
						}
						else if (ttype == LayoutTurnout.RH_XOVER) {	
							g2.draw(new Line2D.Double(t.getCoordsD(),
												fourth(t.getCoordsD(),t.getCoordsC())));
						}									
					}
					else {
						// unknown or inconsistent
						setTrackStrokeWidth(g2,t.isMainlineA());
						if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsA(),
												midpoint(t.getCoordsA(),t.getCoordsB())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsA(),t.getCoordsB()))));
						}
						else if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsA(),
												fourth(t.getCoordsA(),t.getCoordsB())));
						}
						b = t.getLayoutBlockB();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineB());
						if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsB(),
												midpoint(t.getCoordsB(),t.getCoordsA())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsA(),t.getCoordsB()))));
						}
						else if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsB(),
												fourth(t.getCoordsB(),t.getCoordsA())));
						}
						b = t.getLayoutBlockC();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineC());
						if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsC(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsC(),t.getCoordsD()))));
						}
						else if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsC(),
												fourth(t.getCoordsC(),t.getCoordsD())));
						}
						b = t.getLayoutBlockD();
						if (b!=null) g2.setColor(b.getBlockColor());
						else g2.setColor(defaultTrackColor);
						setTrackStrokeWidth(g2,t.isMainlineD());
						if (ttype == LayoutTurnout.LH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsD(),
												midpoint(t.getCoordsC(),t.getCoordsD())));
							setTrackStrokeWidth(g2,false);
							g2.draw(new Line2D.Double(t.getCoordsCenter(),
									third(t.getCoordsCenter(),midpoint(t.getCoordsC(),t.getCoordsD()))));
						}
						else if (ttype == LayoutTurnout.RH_XOVER) {										
							g2.draw(new Line2D.Double(t.getCoordsD(),
												fourth(t.getCoordsD(),t.getCoordsC())));
						}
					}
				}
			}
			else {
				// LH, RH, or WYE Turnouts
				Turnout t2 = t.getTurnout();
				if (t2==null) {
					// no physical turnout linked - draw connected
					setTrackStrokeWidth(g2,t.isMainlineA());
					g2.draw(new Line2D.Double(t.getCoordsA(),t.getCoordsCenter()));
					setTrackStrokeWidth(g2,t.isMainlineB());
					g2.draw(new Line2D.Double(t.getCoordsB(),t.getCoordsCenter()));
					setTrackStrokeWidth(g2,t.isMainlineC());
					g2.draw(new Line2D.Double(t.getCoordsC(),t.getCoordsCenter()));
				}
				else {
					setTrackStrokeWidth(g2,t.isMainlineA());
					//line from throat to center
					g2.draw(new Line2D.Double(t.getCoordsA(),t.getCoordsCenter()));
					int state = Turnout.CLOSED;
					if (animatingLayout)
						state = t2.getKnownState();
					switch (state) {
						case Turnout.CLOSED:
							if (t.getContinuingSense()==Turnout.CLOSED) {
								setTrackStrokeWidth(g2,t.isMainlineB());
								//line from continuing leg to center
								g2.draw(new Line2D.Double(t.getCoordsB(),t.getCoordsCenter()));
								if (turnoutDrawUnselectedLeg) {
									//line from diverging leg halfway to center
									setTrackStrokeWidth(g2,t.isMainlineC());
									if (b!=null) g2.setColor(b.getBlockTrackColor());
									g2.draw(new Line2D.Double(t.getCoordsC(),
											midpoint(t.getCoordsCenter(),t.getCoordsC())));
								}
							}
							else { 
								setTrackStrokeWidth(g2,t.isMainlineC());
								//line from diverging leg to center
								g2.draw(new Line2D.Double(t.getCoordsC(),t.getCoordsCenter()));
								if (turnoutDrawUnselectedLeg) {
									//line from continuing leg halfway to center
									setTrackStrokeWidth(g2,t.isMainlineB());
									if (b!=null) g2.setColor(b.getBlockTrackColor());
									g2.draw(new Line2D.Double(t.getCoordsB(),
											midpoint(t.getCoordsCenter(),t.getCoordsB())));
								}
							}
							break;
						case Turnout.THROWN:
							if (t.getContinuingSense()==Turnout.THROWN) {
								setTrackStrokeWidth(g2,t.isMainlineB());
								g2.draw(new Line2D.Double(t.getCoordsB(),t.getCoordsCenter()));
								if (turnoutDrawUnselectedLeg) {
									setTrackStrokeWidth(g2,t.isMainlineC());
									if (b!=null) g2.setColor(b.getBlockTrackColor());
									g2.draw(new Line2D.Double(t.getCoordsC(),
											midpoint(t.getCoordsCenter(),t.getCoordsC())));
								}
							}
							else { 
								setTrackStrokeWidth(g2,t.isMainlineC());
								g2.draw(new Line2D.Double(t.getCoordsC(),t.getCoordsCenter()));
								if (turnoutDrawUnselectedLeg) {
									setTrackStrokeWidth(g2,t.isMainlineB());
									if (b!=null) g2.setColor(b.getBlockTrackColor());
									g2.draw(new Line2D.Double(t.getCoordsB(),
											midpoint(t.getCoordsCenter(),t.getCoordsB())));
								}
							}
							break;
						default:
							// inconsistent or unknown
							setTrackStrokeWidth(g2,t.isMainlineC());
							g2.draw(new Line2D.Double(t.getCoordsC(),
											midpoint(t.getCoordsCenter(),t.getCoordsC())));
							setTrackStrokeWidth(g2,t.isMainlineB());
							g2.draw(new Line2D.Double(t.getCoordsB(),
											midpoint(t.getCoordsCenter(),t.getCoordsB())));
					}
				}
			}
            }
		}
	}
	
	private Point2D midpoint (Point2D p1,Point2D p2) {
		return new Point2D.Double((p1.getX()+p2.getX())/2.0,(p1.getY()+p2.getY())/2.0);
	}
	
	protected Point2D third (Point2D p1,Point2D p2) {
		return new Point2D.Double( p1.getX()+((p2.getX()-p1.getX())/3.0),
						p1.getY()+((p2.getY()-p1.getY())/3.0) );
	}
	
	private Point2D fourth (Point2D p1,Point2D p2) {
		return new Point2D.Double( p1.getX()+((p2.getX()-p1.getX())/4.0),
						p1.getY()+((p2.getY()-p1.getY())/4.0) );
	}
	
	private void drawXings(Graphics2D g2)
	{
		// loop over all defined level crossings
		for (int i = 0; i<xingList.size();i++) {
			LevelXing x = xingList.get(i);
			if ( x.isMainlineBD() && (!x.isMainlineAC()) ) {
				drawXingAC(g2,x);
				drawXingBD(g2,x);
			}
			else {
				drawXingBD(g2,x);
				drawXingAC(g2,x);
			}				
		}
	}
	private void drawXingAC(Graphics2D g2,LevelXing x) {
		// set color - check for an AC block
		LayoutBlock b = x.getLayoutBlockAC();
		if (b!=null) {
			g2.setColor(b.getBlockColor());
		}
		else {
			g2.setColor(defaultTrackColor);
		}
		// set track width for AC block
		setTrackStrokeWidth(g2,x.isMainlineAC());
		// draw AC segment	
		g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsC()));
	}
	private void drawXingBD(Graphics2D g2,LevelXing x) {
		// set color - check for an BD block
		LayoutBlock b = x.getLayoutBlockBD();
		if (b!=null) {
			g2.setColor(b.getBlockColor());
		}
		else {
			g2.setColor(defaultTrackColor);
		}
		// set track width for BD block
		setTrackStrokeWidth(g2,x.isMainlineBD());
		// draw BD segment	
		g2.draw(new Line2D.Double(x.getCoordsB(),x.getCoordsD()));
	}
    
    private void drawSlips(Graphics2D g2) {
        for (int i = 0; i<slipList.size();i++) {
            LayoutSlip x = slipList.get(i);
            LayoutBlock b = x.getLayoutBlock();
            setTrackStrokeWidth(g2,x.isMainline());
            Color mainColour;
            Color subColour;
            if (b!=null) {
                mainColour = b.getBlockColor();
                subColour = b.getBlockTrackColor();
            }
            else {
                mainColour = defaultTrackColor;
                subColour = defaultTrackColor;
            }
            
            g2.setColor(subColour);
            
            g2.draw(new Line2D.Double(x.getCoordsA(),
                third(x.getCoordsA(),x.getCoordsC())));
            g2.draw(new Line2D.Double(x.getCoordsC(),
                third(x.getCoordsC(),x.getCoordsA())));
                
            g2.draw(new Line2D.Double(x.getCoordsB(),
                third(x.getCoordsB(),x.getCoordsD())));
            g2.draw(new Line2D.Double(x.getCoordsD(),
                third(x.getCoordsD(),x.getCoordsB())));
                    
            if(x.getSlipType()==LayoutSlip.DOUBLE_SLIP){
                if (x.getSlipState()==LayoutSlip.STATE_AC){
                    g2.draw(new Line2D.Double(x.getCoordsA(),
                    third(x.getCoordsA(),x.getCoordsD())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsD(),
                        third(x.getCoordsD(),x.getCoordsA())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsB(),
                        third(x.getCoordsB(),x.getCoordsC())));
                        
                    g2.draw(new Line2D.Double(x.getCoordsC(),
                        third(x.getCoordsC(),x.getCoordsB())));
                    
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsC()));

                } else if (x.getSlipState()==LayoutSlip.STATE_BD){
                    g2.draw(new Line2D.Double(x.getCoordsB(),
                        third(x.getCoordsB(),x.getCoordsC())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsC(),
                        third(x.getCoordsC(),x.getCoordsB())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsA(),
                    third(x.getCoordsA(),x.getCoordsD())));
                    g2.draw(new Line2D.Double(x.getCoordsD(),
                        third(x.getCoordsD(),x.getCoordsA())));
                        
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsB(),x.getCoordsD()));
                
                } else if (x.getSlipState()==LayoutSlip.STATE_AD){
                    g2.draw(new Line2D.Double(x.getCoordsB(),
                        third(x.getCoordsB(),x.getCoordsC())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsC(),
                        third(x.getCoordsC(),x.getCoordsB())));
                        
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsD()));
                
                } else if (x.getSlipState()==LayoutSlip.STATE_BC){
                
                    g2.draw(new Line2D.Double(x.getCoordsA(),
                    third(x.getCoordsA(),x.getCoordsD())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsD(),
                        third(x.getCoordsD(),x.getCoordsA())));
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsB(),x.getCoordsC()));
                }
                else {
                    g2.draw(new Line2D.Double(x.getCoordsB(),
                        third(x.getCoordsB(),x.getCoordsC())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsC(),
                        third(x.getCoordsC(),x.getCoordsB())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsA(),
                    third(x.getCoordsA(),x.getCoordsD())));
                    
                    g2.draw(new Line2D.Double(x.getCoordsD(),
                        third(x.getCoordsD(),x.getCoordsA())));
                }
            } else {
                g2.draw(new Line2D.Double(x.getCoordsA(),
                    third(x.getCoordsA(),x.getCoordsD())));
                    
                g2.draw(new Line2D.Double(x.getCoordsD(),
                    third(x.getCoordsD(),x.getCoordsA())));
                if (x.getSlipState()==LayoutSlip.STATE_AD){
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsD()));
                
                } else if (x.getSlipState()==LayoutSlip.STATE_BD){
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsB(),x.getCoordsD()));
                    if(x.singleSlipStraightEqual()){
                        g2.setColor(mainColour);
                        g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsC()));
                    }
                
                } else if (x.getSlipState()==LayoutSlip.STATE_AC){
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(x.getCoordsA(),x.getCoordsC()));
                    if(x.singleSlipStraightEqual()){
                        g2.setColor(mainColour);
                        g2.draw(new Line2D.Double(x.getCoordsB(),x.getCoordsD()));
                    }
                } else {
                    g2.draw(new Line2D.Double(x.getCoordsA(),
                        third(x.getCoordsA(),x.getCoordsD())));
                        
                    g2.draw(new Line2D.Double(x.getCoordsD(),
                        third(x.getCoordsD(),x.getCoordsA())));
                }
            }
        }
	}
	
	private void drawTurnoutCircles(Graphics2D g2)
	{
		// loop over all defined turnouts
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
            if(!(t.getHidden() && !isEditable())){
                Point2D pt = t.getCoordsCenter();
                double size = SIZE * turnoutCircleSize;
                g2.setColor(turnoutCircleColor != null ? turnoutCircleColor : defaultTrackColor);
                g2.draw(new Ellipse2D.Double (
                                pt.getX()-size, pt.getY()-size, size+size, size+size));
            }
		}
	}

	private void drawTurnoutRects(Graphics2D g2)
	{
		// loop over all defined turnouts
		for (int i = 0; i<turnoutList.size();i++) {
			LayoutTurnout t = turnoutList.get(i);
			Point2D pt = t.getCoordsCenter();
            g2.setColor(turnoutCircleColor != null ? turnoutCircleColor : defaultTrackColor);
			g2.draw(new Ellipse2D.Double (
							pt.getX()-SIZE2, pt.getY()-SIZE2, SIZE2+SIZE2, SIZE2+SIZE2));
			pt = t.getCoordsA();
 			if (t.getConnectA()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = t.getCoordsB();
 			if (t.getConnectB()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = t.getCoordsC();
 			if (t.getConnectC()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			if ( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
					(t.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
						(t.getTurnoutType()==LayoutTurnout.LH_XOVER) ) {
				pt = t.getCoordsD();
				if (t.getConnectD()==null) {
					g2.setColor(Color.red);
				}
				else {
					g2.setColor(Color.green);
				}
				g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			}
		}
	}
	
	private void drawTurntables(Graphics2D g2)
	{
		// loop over all defined layout turntables
		if (turntableList.size()<=0) return;
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable x = turntableList.get(i);
			// draw turntable circle - default track color, side track width
			setTrackStrokeWidth(g2,false);
			Point2D c = x.getCoordsCenter();
			double r = x.getRadius();
			g2.setColor(defaultTrackColor);
			g2.draw(new Ellipse2D.Double (
				c.getX()-r, c.getY()-r, r+r, r+r));
			// draw ray tracks
			for (int j = 0; j<x.getNumberRays(); j++) {
				Point2D pt = x.getRayCoordsOrdered(j);
				TrackSegment t = x.getRayConnectOrdered(j);
				if (t!=null) {
					setTrackStrokeWidth(g2,t.getMainline());
					LayoutBlock b = t.getLayoutBlock();
					if (b!=null) g2.setColor(b.getBlockColor());
					else g2.setColor(defaultTrackColor);
				}
				else {
					setTrackStrokeWidth(g2,false);
					g2.setColor(defaultTrackColor);
				}
				g2.draw(new Line2D.Double(new Point2D.Double(
						pt.getX()-((pt.getX()-c.getX())*0.2),
							pt.getY()-((pt.getY()-c.getY())*0.2)), pt));
			}
            if(x.isTurnoutControlled() && x.getPosition()!=-1){
                Point2D pt = x.getRayCoordsIndexed(x.getPosition());
                g2.draw(new Line2D.Double(new Point2D.Double(
						pt.getX()-((pt.getX()-c.getX())*1.8/*2*/),
							pt.getY()-((pt.getY()-c.getY())*1.8/**2*/)), pt));
            }
		}
	}
	
	private void drawXingRects(Graphics2D g2)
	{
		// loop over all defined level crossings
		for (int i = 0; i<xingList.size();i++) {
			LevelXing x = xingList.get(i);
			Point2D pt = x.getCoordsCenter();
			g2.setColor(defaultTrackColor);
			g2.draw(new Ellipse2D.Double (
							pt.getX()-SIZE2, pt.getY()-SIZE2, SIZE2+SIZE2, SIZE2+SIZE2));
			pt = x.getCoordsA();
 			if (x.getConnectA()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsB();
 			if (x.getConnectB()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsC();
 			if (x.getConnectC()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsD();
 			if (x.getConnectD()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
		}
	}
    
    private void drawSlipRects(Graphics2D g2)
	{
		// loop over all defined level crossings
		for (int i = 0; i<slipList.size();i++) {
			LayoutSlip x = slipList.get(i);
			Point2D pt = x.getCoordsCenter();
			g2.setColor(defaultTrackColor);
			g2.draw(new Ellipse2D.Double (
							pt.getX()-SIZE2, pt.getY()-SIZE2, SIZE2+SIZE2, SIZE2+SIZE2));
			pt = x.getCoordsA();
 			if (x.getConnectA()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsB();
 			if (x.getConnectB()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsC();
 			if (x.getConnectC()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			pt = x.getCoordsD();
 			if (x.getConnectD()==null) {
				g2.setColor(Color.red);
			}
			else {
				g2.setColor(Color.green);
			}
			g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
		}
	}
	
	private void drawTurntableRects(Graphics2D g2)
	{
		// loop over all defined turntables
		for (int i = 0; i<turntableList.size();i++) {
			LayoutTurntable x = turntableList.get(i);
			Point2D pt = x.getCoordsCenter();
			g2.setColor(defaultTrackColor);
			g2.draw(new Ellipse2D.Double (
							pt.getX()-SIZE2, pt.getY()-SIZE2, SIZE2+SIZE2, SIZE2+SIZE2));
			for (int j = 0; j<x.getNumberRays();j++) {
				pt = x.getRayCoordsOrdered(j);
				if (x.getRayConnectOrdered(j)==null) {
					g2.setColor(Color.red);
				}
				else {
					g2.setColor(Color.green);
				}
				g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
			}
		}
	}
	
	private void drawHiddenTrack(Graphics2D g2)
	{
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
			if (isEditable() && t.getHidden()) {
				LayoutBlock b = t.getLayoutBlock();
				if (b!=null) g2.setColor(b.getBlockColor());
				else g2.setColor(defaultTrackColor);
				g2.setStroke(new BasicStroke(1.0F,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
				g2.draw(new Line2D.Double(getCoords(t.getConnect1(),t.getType1()),
										getCoords(t.getConnect2(),t.getType2())));
				setTrackStrokeWidth(g2,!main);
			}
		}
	}
	private void drawDashedTrack(Graphics2D g2, boolean mainline)
	{
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
			if ( (!t.getHidden()) && t.getDashed() && (mainline == t.getMainline()) ) {		
				LayoutBlock b = t.getLayoutBlock();
				if (b!=null) g2.setColor(b.getBlockColor());
				else g2.setColor(defaultTrackColor);
				setTrackStrokeWidth(g2,mainline);
                if (t.getArc()){
                    CalculateTrackSegmentAngle(t);
                    Stroke drawingStroke;
                    Stroke originalStroke = g2.getStroke();
                    if (mainline)
                        drawingStroke = new BasicStroke(mainlineTrackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                    else
                        drawingStroke = new BasicStroke(sideTrackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                    g2.setStroke(drawingStroke);
                    g2.draw(new Arc2D.Double(t.getCX(), t.getCY(), t.getCW(), t.getCH(), t.getStartadj(), t.getTmpAngle(), Arc2D.OPEN));
                    g2.setStroke(originalStroke);
                } else {
                    Point2D end1 = getCoords(t.getConnect1(),t.getType1());
                    Point2D end2 = getCoords(t.getConnect2(),t.getType2());
                    double delX = end1.getX() - end2.getX();
                    double delY = end1.getY() - end2.getY();
                    double cLength = Math.sqrt( (delX*delX) + (delY*delY) );
                    // note: The preferred dimension of a dash (solid + blank space) is 
                    //         5 * the track width - about 60% solid and 40% blank.
                    int nDashes = (int)( cLength / ((trackWidth)*5.0) );
                    if (nDashes < 3) nDashes = 3;
                    double delXDash = -delX/( (nDashes) - 0.5 );
                    double delYDash = -delY/( (nDashes) - 0.5 );
                    double begX = end1.getX();
                    double begY = end1.getY();
                    for (int k = 0; k<nDashes; k++) {
                        g2.draw(new Line2D.Double(new Point2D.Double(begX,begY),
                            new Point2D.Double((begX+(delXDash*0.5)),(begY+(delYDash*0.5)))));
                        begX += delXDash;
                        begY += delYDash;
                    }
                }
			}
		}
	}
	
	/* draw all track segments which are not hidden, not dashed, and that match the isMainline parm */
	private void drawSolidTrack(Graphics2D g2, boolean isMainline)
	{
		for (int i = 0; i<trackList.size();i++) {
            setTrackStrokeWidth(g2, isMainline);
			TrackSegment t = trackList.get(i);
			if ( (!t.getHidden()) && (!t.getDashed()) && (isMainline == t.getMainline()) ) {		
				LayoutBlock b = t.getLayoutBlock();
				if (b!=null) g2.setColor(b.getBlockColor());
				else g2.setColor(defaultTrackColor);
				//setTrackStrokeWidth(g2,mainline);
                if(t.getArc()){
                    CalculateTrackSegmentAngle(t);
                    g2.draw(new Arc2D.Double(t.getCX(), t.getCY(), t.getCW(), t.getCH(), t.getStartadj(), t.getTmpAngle(), Arc2D.OPEN));
                } else {
                    g2.draw(new Line2D.Double(getCoords(t.getConnect1(),t.getType1()), getCoords(t.getConnect2(),t.getType2())));
                }
                t.trackRedrawn();
			}
		}
	}
    
    /*
     * Calculates the initally parameters for drawing a circular track segment.
     */
    private void CalculateTrackSegmentAngle(TrackSegment t){
        Point2D pt1 = getCoords(t.getConnect1(),t.getType1());
        Point2D pt2 = getCoords(t.getConnect2(),t.getType2());
        if (t.getFlip()){
            pt1 = getCoords(t.getConnect2(),t.getType2());
            pt2 = getCoords(t.getConnect1(),t.getType1());
        }
        if((t.getTmpPt1()!=pt1) || (t.getTmpPt2()!=pt2) || t.trackNeedsRedraw()){
            t.setTmpPt1(pt1);
            t.setTmpPt2(pt2);
            //setTrackStrokeWidth(g2,false);
            double pt2x;
            double pt2y;
            double pt1x;
            double pt1y;
            pt2x = pt2.getX();
            pt2y = pt2.getY();
            pt1x = pt1.getX();
            pt1y = pt1.getY();

            if (t.getAngle() == 0.0D)
                t.setTmpAngle(90.0D);
            else
                t.setTmpAngle(t.getAngle());
            // Convert angle to radiants in order to speed up maths
            double halfAngle = java.lang.Math.toRadians(t.getTmpAngle())/2.0D;
            double chord;
            double a;
            double o;
            double radius;
            // Compute arc's chord
            a = pt2x - pt1x;
            o = pt2y - pt1y;
            chord=java.lang.Math.sqrt(((a*a)+(o*o)));
            t.setChordLength(chord);
            // Make sure chord is not null 
            // In such a case (pt1 == pt2), there is no arc to draw
            if (chord > 0.0D) {
                radius = (chord/2)/(java.lang.Math.sin(halfAngle));
                // Circle
                double startRad = java.lang.Math.atan2(a, o) - halfAngle;
                t.setStartadj(java.lang.Math.toDegrees(startRad));
                if(t.getCircle()){
                    // Circle - Compute center
                    t.setCentreX(pt2x - java.lang.Math.cos(startRad) * radius);
                    t.setCentreY(pt2y + java.lang.Math.sin(startRad) * radius);
                    // Circle - Compute rectangle required by Arc2D.Double
                    t.setCW(radius * 2.0D);
                    t.setCH(radius * 2.0D);
                    t.setCX(t.getCentreX()-(radius));
                    t.setCY(t.getCentreY()-(radius));
                } 
                else {
                    // Elipse - Round start angle to the closest multiple of 90
                    t.setStartadj(java.lang.Math.round(t.getStartadj() / 90.0D) * 90.0D);
                    // Elipse - Compute rectangle required by Arc2D.Double
                    t.setCW(java.lang.Math.abs(a)*2.0D);
                    t.setCH(java.lang.Math.abs(o)*2.0D);
                    // Elipse - Adjust rectangle corner, depending on quadrant
                    if (o * a < 0.0D)
                        a = -a;
                    else
                        o = -o;
                    t.setCX(java.lang.Math.min(pt1x, pt2x)-java.lang.Math.max(a, 0.0D));
                    t.setCY(java.lang.Math.min(pt1y, pt2y)-java.lang.Math.max(o, 0.0D));
                }
            }
        }
    }
    /*
     * The recalculation method is used when the user changes the angle dynamically in edit mode
     * by dragging the centre of the cirle
     */
    private void reCalculateTrackSegmentAngle(TrackSegment t, double x, double y){
        
        double pt2x;
        double pt2y;
        double pt1x;
        double pt1y;

        pt2x = t.getTmpPt2().getX();
        pt2y = t.getTmpPt2().getY();
        pt1x = t.getTmpPt1().getX();
        pt1y = t.getTmpPt1().getY();
        if (t.getFlip()){
            pt1x = t.getTmpPt2().getX();
            pt1y = t.getTmpPt2().getY();
            pt2x = t.getTmpPt1().getX();
            pt2y = t.getTmpPt1().getY();
        }
        //Point 1 to new point length
        double a;
        double o;
        double la;
        // Compute arc's chord
        a = pt2x - x;
        o = pt2y - y;
        la=java.lang.Math.sqrt(((a*a)+(o*o)));
        
        double lb;
        a = pt1x - x;
        o = pt1y - y;
        lb=java.lang.Math.sqrt(((a*a)+(o*o)));

        double newangle=Math.toDegrees(Math.acos((-t.getChordLength()*t.getChordLength()+la*la+lb*lb)/(2*la*lb)));
        t.setAngle(newangle);
        
    }
	
    /*
     * Draws a square at the circles centre, that then allows the user to dynamically change
     * the angle by dragging the mouse.
     */
	private void drawTrackCircleCentre(Graphics2D g2)
	{
		// loop over all defined turnouts
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
            if (t.getCircle()){
                Point2D pt = t.getCoordsCenterCircle();
                g2.setColor(Color.black);
                g2.draw(new Rectangle2D.Double (
							pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
            }
		}
	}
    private void drawTrackInProgress(Graphics2D g2)
	{
		// check for segment in progress
		if ( isEditable() && (beginObject!=null) && trackBox.isSelected() ) {
			g2.setColor(defaultTrackColor);
			setTrackStrokeWidth(g2,false);
			g2.draw(new Line2D.Double(beginLocation,currentLocation));
		}
	}

	private void drawTrackOvals(Graphics2D g2)
	{
		// loop over all defined track segments
		g2.setColor(defaultTrackColor);
		for (int i = 0; i<trackList.size();i++) {
			TrackSegment t = trackList.get(i);
			Point2D pt1 = getCoords(t.getConnect1(),t.getType1());
			Point2D pt2 = getCoords(t.getConnect2(),t.getType2());
			double cX = (pt1.getX() + pt2.getX())/2.0D;
			double cY = (pt1.getY() + pt2.getY())/2.0D;
            g2.draw(new Ellipse2D.Double (cX-SIZE2, cY-SIZE2, SIZE2+SIZE2, SIZE2+SIZE2));
            if (t.getArc()) {
                LayoutBlock b = t.getLayoutBlock();
				if (b!=null) g2.setColor(b.getBlockColor());
				else g2.setColor(defaultTrackColor);
                g2.draw(new Line2D.Double(getCoords(t.getConnect1(),t.getType1()), getCoords(t.getConnect2(),t.getType2())));
                if (t.getCircle()){
                    g2.draw(new Line2D.Double(getCoords(t.getConnect1(),t.getType1()), new Point2D.Double(t.getCentreX(),t.getCentreY())));
                    g2.draw(new Line2D.Double(getCoords(t.getConnect2(),t.getType2()), new Point2D.Double(t.getCentreX(),t.getCentreY())));

                }
                g2.setColor(defaultTrackColor);
			}
		}
	}

	private void drawPoints(Graphics2D g2)
	{
		for (int i = 0; i<pointList.size();i++) {
			PositionablePoint p = pointList.get(i);
			switch (p.getType()) {
				case PositionablePoint.ANCHOR:
					// nothing to draw unless in edit mode
					if (isEditable()) {
						// in edit mode, draw locater rectangle
						Point2D pt = p.getCoords();
						if ((p.getConnect1()==null) || (p.getConnect2()==null)) {
							g2.setColor(Color.red);
						}
						else {
							g2.setColor(Color.green);
						}
						g2.draw(new Rectangle2D.Double (
									pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
					}
					break;
				case PositionablePoint.END_BUMPER:
					// nothing to draw unless in edit mode
					if (isEditable()) {
						// in edit mode, draw locater rectangle
						Point2D pt = p.getCoords();
						if (p.getConnect1()==null) {
							g2.setColor(Color.red);
						}
						else {
							g2.setColor(Color.green);
						}
						g2.draw(new Rectangle2D.Double (
									pt.getX()-SIZE, pt.getY()-SIZE, SIZE2, SIZE2));
					}
					break;
				default:
					log.error("Illegal type of Positionable Point");
			}
		}
	}

	private void drawSelectionRect(Graphics2D g2) {
		if ( selectionActive && (selectionWidth!=0.0) && (selectionHeight!=0.0) ){
			g2.setColor(defaultTrackColor);
			g2.setStroke(new BasicStroke(1.0F,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
			g2.draw(new Rectangle2D.Double (selectionX, selectionY, selectionWidth, selectionHeight));
		}
	}

	private void drawMemoryRects(Graphics2D g2) {
		if (memoryLabelList.size()<=0) return;
		g2.setColor(defaultTrackColor);
		g2.setStroke(new BasicStroke(1.0F,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
		for (int i = 0;i<memoryLabelList.size();i++) {
			MemoryIcon l = memoryLabelList.get(i);
			g2.draw(new Rectangle2D.Double (l.getX(), l.getY(), l.getSize().width, l.getSize().height));
		}
	}

	private void drawPanelGrid(Graphics2D g2) {
		Dimension dim = getSize();
		double pix = 10.0;
		double maxX = dim.width;
		double maxY = dim.height;
		Point2D startPt = new Point2D.Double(0.0, 10.0);
		Point2D stopPt = new Point2D.Double(maxX, 10.0);
		BasicStroke narrow = new BasicStroke(1.0F,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
		BasicStroke wide = new BasicStroke(2.0F,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
		g2.setColor(Color.gray);
		g2.setStroke(narrow);
		// draw horizontal lines
		while (pix<maxY) {
			startPt.setLocation(0.0,pix);
			stopPt.setLocation(maxX,pix);
			if ( (((int)pix) % 100) < 5.0) {
				g2.setStroke(wide);
				g2.draw(new Line2D.Double(startPt,stopPt));
				g2.setStroke(narrow);
			}
			else {
				g2.draw(new Line2D.Double(startPt,stopPt));
			}
			pix += 10.0;
		}
		// draw vertical lines
		pix = 10.0;
		while (pix<maxX) {
			startPt.setLocation(pix,0.0);
			stopPt.setLocation(pix,maxY);
			if ( (((int)pix) % 100) < 5.0) {
				g2.setStroke(wide);
				g2.draw(new Line2D.Double(startPt,stopPt));
				g2.setStroke(narrow);
			}
			else {
				g2.draw(new Line2D.Double(startPt,stopPt));
			}
			pix += 10.0;
		}
	}

	protected Point2D getCoords(Object o, int type) {
		if (o != null) {
			switch (type) {
				case POS_POINT:
					return ((PositionablePoint)o).getCoords();
				case TURNOUT_A:
					return ((LayoutTurnout)o).getCoordsA();
				case TURNOUT_B:
					return ((LayoutTurnout)o).getCoordsB();
				case TURNOUT_C:
					return ((LayoutTurnout)o).getCoordsC();
				case TURNOUT_D:
					return ((LayoutTurnout)o).getCoordsD();
				case LEVEL_XING_A:
					return ((LevelXing)o).getCoordsA();
				case LEVEL_XING_B:
					return ((LevelXing)o).getCoordsB();
				case LEVEL_XING_C:
					return ((LevelXing)o).getCoordsC();
				case LEVEL_XING_D:
					return ((LevelXing)o).getCoordsD();
				case SLIP_A:
					return ((LayoutSlip)o).getCoordsA();
				case SLIP_B:
					return ((LayoutSlip)o).getCoordsB();
				case SLIP_C:
					return ((LayoutSlip)o).getCoordsC();
                case SLIP_D:
					return ((LayoutSlip)o).getCoordsD();
				default: 
					if (type>=TURNTABLE_RAY_OFFSET)
						return ((LayoutTurntable)o).getRayCoordsIndexed(type-TURNTABLE_RAY_OFFSET);
			}
		}
		else {
			log.error("Null connection point of type "+type);
		}
		return (new Point2D.Double(0.0,0.0));
	}

    protected boolean showAlignPopup(Positionable l) {
        return false;
    }
	
    public void showToolTip(Positionable selection, MouseEvent event) {
        ToolTip tip = selection.getTooltip();
        tip.setLocation(selection.getX()+selection.getWidth()/2, selection.getY()+selection.getHeight());
        tip.setText(selection.getNameString());
        setToolTip(tip);
    }
    
    @Override
    public void addToPopUpMenu(jmri.NamedBean nb, JMenuItem item, int menu){
        if(nb==null || item==null){
            return;
        }
        if(nb instanceof Sensor){
            for(SensorIcon si:sensorList){
                if(si.getNamedBean()==nb && si.getPopupUtility()!=null){
                    switch(menu){
                        case VIEWPOPUPONLY : si.getPopupUtility().addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : si.getPopupUtility().addEditPopUpMenu(item); break;
                        default: si.getPopupUtility().addEditPopUpMenu(item);
                                 si.getPopupUtility().addViewPopUpMenu(item);
                    }
                }
            }
        } else if (nb instanceof SignalHead){
            for(SignalHeadIcon si:signalList){
                if(si.getNamedBean()==nb && si.getPopupUtility()!=null){
                    switch(menu){
                        case VIEWPOPUPONLY : si.getPopupUtility().addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : si.getPopupUtility().addEditPopUpMenu(item); break;
                        default: si.getPopupUtility().addEditPopUpMenu(item);
                                 si.getPopupUtility().addViewPopUpMenu(item);
                    }
                }
            }
        } else if (nb instanceof SignalMast){
            for(SignalMastIcon si:signalMastList){
                if(si.getNamedBean()==nb && si.getPopupUtility()!=null){
                    switch(menu){
                        case VIEWPOPUPONLY : si.getPopupUtility().addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : si.getPopupUtility().addEditPopUpMenu(item); break;
                        default: si.getPopupUtility().addEditPopUpMenu(item);
                                 si.getPopupUtility().addViewPopUpMenu(item);
                    }
                }
            }
        } else if (nb instanceof Memory){
            for(MemoryIcon si: memoryLabelList){
                if(si.getNamedBean()==nb && si.getPopupUtility()!=null){
                    switch(menu){
                        case VIEWPOPUPONLY : si.getPopupUtility().addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : si.getPopupUtility().addEditPopUpMenu(item); break;
                        default: si.getPopupUtility().addEditPopUpMenu(item);
                                 si.getPopupUtility().addViewPopUpMenu(item);
                    }
                }
            }
        } else if (nb instanceof Turnout){
            for(LayoutTurnout ti: turnoutList){
                if(ti.getTurnout().equals(nb)){
                    switch(menu){
                        case VIEWPOPUPONLY : ti.addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : ti.addEditPopUpMenu(item); break;
                        default: ti.addEditPopUpMenu(item);
                                 ti.addViewPopUpMenu(item);
                    }
                    break;
                }
            }
            for(LayoutSlip ls: slipList){
                if(ls.getTurnout()==nb||ls.getTurnoutB()==nb){
                    switch(menu){
                        case VIEWPOPUPONLY : ls.addViewPopUpMenu(item); break;
                        case EDITPOPUPONLY : ls.addEditPopUpMenu(item); break;
                        default: ls.addEditPopUpMenu(item);
                                 ls.addViewPopUpMenu(item);
                    }
                    break;
                }
            
            }
        }
    }
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutEditor.class.getName());
}