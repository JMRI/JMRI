package jmri.jmrit.display.layoutEditor;

import apps.gui.GuiLafPreferencesManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboPopup;
import jmri.BlockManager;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.UserPreferencesManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.dispatcher.DispatcherFrame;
import jmri.jmrit.display.AnalogClock2Display;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.display.ToolTip;
import jmri.util.ColorUtil;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.SystemType;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a scrollable Layout Panel and editor toolbars (that can be hidden)
 * <P>
 * This module serves as a manager for the LayoutTurnout, Layout Block,
 * PositionablePoint, Track Segment, LayoutSlip and LevelXing objects which are
 * integral subparts of the LayoutEditor class.
 * <P>
 * All created objects are put on specific levels depending on their type
 * (higher levels are in front): Note that higher numbers appear behind lower
 * numbers.
 * <P>
 * The "contents" List keeps track of all text and icon label objects added to
 * the targetframe for later manipulation. Other Lists keep track of drawn
 * items.
 * <P>
 * Based in part on PanelEditor.java (Bob Jacobsen (c) 2002, 2003). In
 * particular, text and icon label items are copied from Panel editor, as well
 * as some of the control design.
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 */
@SuppressWarnings("serial")
public class LayoutEditor extends jmri.jmrit.display.panelEditor.PanelEditor implements java.beans.VetoableChangeListener, MouseWheelListener {

    //Operational instance variables - not saved to disk
    //private jmri.TurnoutManager tm = null;
    private LayoutEditor thisPanel = null;

    private JmriJFrame floatingEditToolBox = null;
    private JScrollPane floatingEditContent = null;
    private JPanel floatEditHelpPanel = null;
    private JPanel editToolBarPanel = null;
    private JScrollPane editToolBarScroll = null;
    private JPanel editToolBarContainer = null;
    private JPanel helpBarPanel = null;
    private JPanel helpBar = new JPanel();

    protected boolean skipIncludedTurnout = false;

    public LayoutEditorAuxTools auxTools = null;
    private ConnectivityUtil conTools = null;

    private Font toolBarFont = null;

    private ButtonGroup itemGroup = null;

    //top row of radio buttons
    private JLabel turnoutLabel = new JLabel();
    private JRadioButton turnoutRHButton = new JRadioButton(Bundle.getMessage("RightHandAbbreviation"));
    private JRadioButton turnoutLHButton = new JRadioButton(Bundle.getMessage("LeftHandAbbreviation"));
    private JRadioButton turnoutWYEButton = new JRadioButton(Bundle.getMessage("WYEAbbreviation"));
    private JRadioButton doubleXoverButton = new JRadioButton(Bundle.getMessage("DoubleCrossoverAbbreviation"));
    private JRadioButton rhXoverButton = new JRadioButton(Bundle.getMessage("RightCrossover")); //key is also used by Control Panel
    //Editor, placed in DisplayBundle
    private JRadioButton lhXoverButton = new JRadioButton(Bundle.getMessage("LeftCrossover")); //idem
    private JRadioButton layoutSingleSlipButton = new JRadioButton(Bundle.getMessage("LayoutSingleSlip"));
    private JRadioButton layoutDoubleSlipButton = new JRadioButton(Bundle.getMessage("LayoutDoubleSlip"));

    //Default flow layout definitions for JPanels
    private FlowLayout leftRowLayout = new FlowLayout(FlowLayout.LEFT, 5, 0);       //5 pixel gap between items, no vertical gap
    private FlowLayout centerRowLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);   //5 pixel gap between items, no vertical gap
    private FlowLayout rightRowLayout = new FlowLayout(FlowLayout.RIGHT, 5, 0);     //5 pixel gap between items, no vertical gap

    //top row of check boxes
    private JmriBeanComboBox turnoutNameComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JPanel turnoutNamePanel = new JPanel(leftRowLayout);
    private JPanel extraTurnoutPanel = new JPanel(leftRowLayout);
    private JmriBeanComboBox extraTurnoutNameComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JComboBox<String> rotationComboBox = null;
    private JPanel rotationPanel = new JPanel(leftRowLayout);

    //2nd row of radio buttons
    private JLabel trackLabel = new JLabel();
    private JRadioButton levelXingButton = new JRadioButton(Bundle.getMessage("LevelCrossing"));
    private JRadioButton trackButton = new JRadioButton(Bundle.getMessage("TrackSegment"));

    //2nd row of check boxes
    private JPanel trackSegmentPropertiesPanel = new JPanel(leftRowLayout);
    private JCheckBox mainlineTrack = new JCheckBox(Bundle.getMessage("MainlineBox"));
    private JCheckBox dashedLine = new JCheckBox(Bundle.getMessage("Dashed"));

    private JLabel blockNameLabel = new JLabel();
    private JmriBeanComboBox blockIDComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JLabel blockSensorNameLabel = new JLabel();
    private JLabel blockSensorLabel = new JLabel(Bundle.getMessage("BeanNameSensor"));
    private JmriBeanComboBox blockSensorComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    //3rd row of radio buttons (and any associated text fields)
    private JLabel nodesLabel = new JLabel();
    private JRadioButton endBumperButton = new JRadioButton(Bundle.getMessage("EndBumper"));
    private JRadioButton anchorButton = new JRadioButton(Bundle.getMessage("Anchor"));
    private JRadioButton edgeButton = new JRadioButton(Bundle.getMessage("EdgeConnector"));

    private JLabel labelsLabel = new JLabel();
    private JRadioButton textLabelButton = new JRadioButton(Bundle.getMessage("TextLabel"));
    private JTextField textLabelTextField = new JTextField(12);

    private JRadioButton memoryButton = new JRadioButton(Bundle.getMessage("BeanNameMemory"));
    private JmriBeanComboBox textMemoryComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(MemoryManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton blockContentsButton = new JRadioButton(Bundle.getMessage("BlockContentsLabel"));
    private JmriBeanComboBox blockContentsComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    //4th row of radio buttons (and any associated text fields)
    private JRadioButton multiSensorButton = new JRadioButton(Bundle.getMessage("MultiSensor") + "...");

    private JRadioButton signalMastButton = new JRadioButton(Bundle.getMessage("SignalMastIcon"));
    private JmriBeanComboBox signalMastComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalMastManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton sensorButton = new JRadioButton(Bundle.getMessage("SensorIcon"));
    private JmriBeanComboBox sensorComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton signalButton = new JRadioButton(Bundle.getMessage("SignalIcon"));
    private JmriBeanComboBox signalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton iconLabelButton = new JRadioButton(Bundle.getMessage("IconLabel"));

    private JButton changeIconsButton = new JButton(Bundle.getMessage("ChangeIcons") + "...");

    public MultiIconEditor sensorIconEditor = null;
    public JFrame sensorFrame = null;

    public MultiIconEditor signalIconEditor = null;
    public JFrame signalFrame = null;

    private MultiIconEditor iconEditor = null;
    private JFrame iconFrame = null;

    private MultiSensorIconFrame multiSensorFrame = null;

    private JLabel xLabel = new JLabel("00");
    private JLabel yLabel = new JLabel("00");

    private JPanel zoomPanel = new JPanel();
    private JLabel zoomLabel = new JLabel("x1");

    private JMenu zoomMenu = new JMenu(Bundle.getMessage("MenuZoom"));
    private JRadioButtonMenuItem zoom025Item = new JRadioButtonMenuItem("x 0.25");
    private JRadioButtonMenuItem zoom05Item = new JRadioButtonMenuItem("x 0.5");
    private JRadioButtonMenuItem zoom075Item = new JRadioButtonMenuItem("x 0.75");
    private JRadioButtonMenuItem noZoomItem = new JRadioButtonMenuItem(Bundle.getMessage("NoZoom"));
    private JRadioButtonMenuItem zoom15Item = new JRadioButtonMenuItem("x 1.5");
    private JRadioButtonMenuItem zoom20Item = new JRadioButtonMenuItem("x 2.0");
    private JRadioButtonMenuItem zoom30Item = new JRadioButtonMenuItem("x 3.0");
    private JRadioButtonMenuItem zoom40Item = new JRadioButtonMenuItem("x 4.0");
    private JRadioButtonMenuItem zoom50Item = new JRadioButtonMenuItem("x 5.0");
    private JRadioButtonMenuItem zoom60Item = new JRadioButtonMenuItem("x 6.0");

    private JPanel locationPanel = new JPanel();

    //end of main panel controls
    private boolean delayedPopupTrigger = false;
    private transient Point2D currentPoint = new Point2D.Double(100.0, 100.0);
    private transient Point2D dLoc = new Point2D.Double(0.0, 0.0);

    //private int savedMSX = 0;
    //private int savedMSY = 0;
    private int height = 100;
    private int width = 100;

    //private int numTurnouts = 0;
    private TrackSegment newTrack = null;
    private boolean panelChanged = false;

    //grid size in pixels
    private int gridSize1st = 10;
    // secondary grid
    private int gridSize2nd = 10;

    //size of point boxes
    private static final double SIZE = 3.0;
    private static final double SIZE2 = SIZE * 2.; //must be twice SIZE

    //NOTE: although these have been moved to the LayoutTurnout class 
    // I'm leaving a copy of them here so that any external use of these 
    // won't break. At some point in the future these should be @Deprecated.
    // All JMRI sources now use the ones in the LayoutTurnout class.
    //defined constants - turnout types
    public static final int RH_TURNOUT = LayoutTurnout.RH_TURNOUT;
    public static final int LH_TURNOUT = LayoutTurnout.LH_TURNOUT;
    public static final int WYE_TURNOUT = LayoutTurnout.WYE_TURNOUT;
    public static final int DOUBLE_XOVER = LayoutTurnout.DOUBLE_XOVER;
    public static final int RH_XOVER = LayoutTurnout.RH_XOVER;
    public static final int LH_XOVER = LayoutTurnout.LH_XOVER;
    public static final int SINGLE_SLIP = LayoutTurnout.SINGLE_SLIP;
    public static final int DOUBLE_SLIP = LayoutTurnout.DOUBLE_SLIP;

    // hit location (& connection) types (see NOTE above)
    public static final int NONE = LayoutTrack.NONE;
    public static final int POS_POINT = LayoutTrack.POS_POINT;
    public static final int TURNOUT_A = LayoutTrack.TURNOUT_A; //throat for RH, LH, and WYE turnouts
    public static final int TURNOUT_B = LayoutTrack.TURNOUT_B; //continuing route for RH or LH turnouts
    public static final int TURNOUT_C = LayoutTrack.TURNOUT_C; //diverging route for RH or LH turnouts
    public static final int TURNOUT_D = LayoutTrack.TURNOUT_D; //double-crossover or single crossover only
    public static final int LEVEL_XING_A = LayoutTrack.LEVEL_XING_A;
    public static final int LEVEL_XING_B = LayoutTrack.LEVEL_XING_B;
    public static final int LEVEL_XING_C = LayoutTrack.LEVEL_XING_C;
    public static final int LEVEL_XING_D = LayoutTrack.LEVEL_XING_D;
    public static final int TRACK = LayoutTrack.TRACK;
    public static final int TURNOUT_CENTER = LayoutTrack.TURNOUT_CENTER; //non-connection points should be last
    public static final int LEVEL_XING_CENTER = LayoutTrack.LEVEL_XING_CENTER;
    public static final int TURNTABLE_CENTER = LayoutTrack.TURNTABLE_CENTER;
    public static final int LAYOUT_POS_LABEL = LayoutTrack.LAYOUT_POS_LABEL;
    public static final int LAYOUT_POS_JCOMP = LayoutTrack.LAYOUT_POS_JCOMP;
    public static final int MULTI_SENSOR = LayoutTrack.MULTI_SENSOR;
    public static final int MARKER = LayoutTrack.MARKER;
    public static final int TRACK_CIRCLE_CENTRE = LayoutTrack.TRACK_CIRCLE_CENTRE;
    public static final int SLIP_CENTER = LayoutTrack.SLIP_CENTER; //should be @Deprecated (use SLIP_LEFT & SLIP_RIGHT instead)
    public static final int SLIP_A = LayoutTrack.SLIP_A;
    public static final int SLIP_B = LayoutTrack.SLIP_B;
    public static final int SLIP_C = LayoutTrack.SLIP_C;
    public static final int SLIP_D = LayoutTrack.SLIP_D;
    public static final int SLIP_LEFT = LayoutTrack.SLIP_LEFT;
    public static final int SLIP_RIGHT = LayoutTrack.SLIP_RIGHT;
    public static final int BEZIER_CONTROL_POINT_OFFSET_MIN = LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN;
    public static final int BEZIER_CONTROL_POINT_OFFSET_MAX = LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MAX;
    public static final int TURNTABLE_RAY_OFFSET = LayoutTrack.TURNTABLE_RAY_OFFSET;

    protected Color turnoutCircleColor = Color.black; //matches earlier versions
    protected int turnoutCircleSize = 4; //matches earlier versions

    //use turnoutCircleSize when you need an int and these when you need a double
    //note: these only change when setTurnoutCircleSize is called
    //using these avoids having to call getTurnoutCircleSize() and
    //the multiply (x2) and the int -> double conversion overhead
    private double circleRadius = SIZE * getTurnoutCircleSize();
    private double circleDiameter = 2.0 * circleRadius;

    //selection variables
    private boolean selectionActive = false;
    private double selectionX = 0.0;
    private double selectionY = 0.0;
    private double selectionWidth = 0.0;
    private double selectionHeight = 0.0;

    //Option menu items
    private JCheckBoxMenuItem editModeCheckBoxMenuItem = null;

    private JRadioButtonMenuItem toolBarSideTopButton = null;
    private JRadioButtonMenuItem toolBarSideLeftButton = null;
    private JRadioButtonMenuItem toolBarSideBottomButton = null;
    private JRadioButtonMenuItem toolBarSideRightButton = null;
    private JRadioButtonMenuItem toolBarSideFloatButton = null;

    private JMenu toolBarFontSizeMenu = new JMenu(Bundle.getMessage("FontSize"));
    private JCheckBoxMenuItem wideToolBarCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ToolBarWide"));
    private JMenu dropDownListsDisplayOrderMenu = new JMenu(Bundle.getMessage("DropDownListsDisplayOrder"));

    private JCheckBoxMenuItem positionableCheckBoxMenuItem = null;
    private JCheckBoxMenuItem controlCheckBoxMenuItem = null;
    private JCheckBoxMenuItem animationCheckBoxMenuItem = null;
    private JCheckBoxMenuItem showHelpCheckBoxMenuItem = null;
    private JCheckBoxMenuItem showGridCheckBoxMenuItem = null;
    private JCheckBoxMenuItem autoAssignBlocksCheckBoxMenuItem = null;
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

    private JCheckBoxMenuItem snapToGridOnAddCheckBoxMenuItem = null;
    private JCheckBoxMenuItem snapToGridOnMoveCheckBoxMenuItem = null;
    private JCheckBoxMenuItem antialiasingOnCheckBoxMenuItem = null;
    private JCheckBoxMenuItem highlightSelectedBlockCheckBoxMenuItem = null;
    private JCheckBoxMenuItem turnoutCirclesOnCheckBoxMenuItem = null;
    private JCheckBoxMenuItem skipTurnoutCheckBoxMenuItem = null;
    private JCheckBoxMenuItem turnoutDrawUnselectedLegCheckBoxMenuItem = null;
    private JCheckBoxMenuItem hideTrackSegmentConstructionLinesCheckBoxMenuItem = null;
    private JCheckBoxMenuItem useDirectTurnoutControlCheckBoxMenuItem = null;

    private ButtonGroup trackColorButtonGroup = null;
    private ButtonGroup trackOccupiedColorButtonGroup = null;
    private ButtonGroup trackAlternativeColorButtonGroup = null;
    private ButtonGroup textColorButtonGroup = null;
    private ButtonGroup backgroundColorButtonGroup = null;
    private ButtonGroup turnoutCircleColorButtonGroup = null;
    private ButtonGroup turnoutCircleSizeButtonGroup = null;

    private boolean turnoutDrawUnselectedLeg = true;
    private boolean autoAssignBlocks = false;

    //Selected point information
    private transient Point2D startDelta = new Point2D.Double(0.0, 0.0); //starting delta coordinates
    private Object selectedObject = null;       //selected object, null if nothing selected
    private Object prevSelectedObject = null;   //previous selected object, for undo
    private int selectedPointType = 0;          //connection type within the selected object

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
    private Object foundObject = null; //found object, null if nothing found

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
    private transient Point2D foundLocation = new Point2D.Double(0.0, 0.0); //location of found object

    private int foundPointType = 0; //connection type within the found object

    @SuppressWarnings("unused")
    private boolean foundNeedsConnect = false; //true if found point needs a connection
    private Object beginObject = null; //begin track segment connection object, null if
    //none
    private transient Point2D beginLocation = new Point2D.Double(0.0, 0.0); //location of begin object
    private int beginPointType = LayoutTrack.NONE; //connection type within begin connection object
    private transient Point2D currentLocation = new Point2D.Double(0.0, 0.0); //current location

    //Lists of items that describe the Layout, and allow it to be drawn
    //Each of the items must be saved to disk over sessions
    public ArrayList<PositionableLabel> backgroundImage = new ArrayList<>();    //background images
    public ArrayList<SensorIcon> sensorImage = new ArrayList<>();               //sensor images
    public ArrayList<SignalHeadIcon> signalHeadImage = new ArrayList<>();       //signal head images
    public ArrayList<LocoIcon> markerImage = new ArrayList<>();                 //marker images
    public ArrayList<PositionableLabel> labelImage = new ArrayList<>();         //positionable label images
    public ArrayList<AnalogClock2Display> clocks = new ArrayList<>();           //fast clocks
    public ArrayList<MultiSensorIcon> multiSensors = new ArrayList<>();         //multi-sensor images

    public ArrayList<LayoutTurnout> turnoutList = new ArrayList<>();        //LayoutTurnout list
    public ArrayList<TrackSegment> trackList = new ArrayList<>();           //TrackSegment list
    public ArrayList<PositionablePoint> pointList = new ArrayList<>();      //PositionablePoint list
    public ArrayList<LevelXing> xingList = new ArrayList<>();               //LevelXing list
    public ArrayList<LayoutSlip> slipList = new ArrayList<>();              //LayoutSlip list
    public ArrayList<LayoutTurntable> turntableList = new ArrayList<>();    //LayoutTurntable list

    public ArrayList<SignalHeadIcon> signalList = new ArrayList<>();                //Signal Head Icons
    public ArrayList<MemoryIcon> memoryLabelList = new ArrayList<>();               //Memory Label List
    public ArrayList<BlockContentsIcon> blockContentsLabelList = new ArrayList<>(); //BlockContentsIcon Label List
    public ArrayList<SensorIcon> sensorList = new ArrayList<>();                    //Sensor Icons
    public ArrayList<SignalMastIcon> signalMastList = new ArrayList<>();            //Signal Mast Icons

    //counts used to determine unique internal names
    private int numAnchors = 0;
    private int numEndBumpers = 0;
    private int numEdgeConnectors = 0;
    private int numTrackSegments = 0;
    private int numLevelXings = 0;
    private int numLayoutSlips = 0;
    private int numLayoutTurnouts = 0;
    private int numLayoutTurntables = 0;

    public LayoutEditorFindItems finder = new LayoutEditorFindItems(this);

    public LayoutEditorFindItems getFinder() {
        return finder;
    }

    //persistent instance variables - saved to disk with Save Panel
    private int upperLeftX = 0; // Note: These are _WINDOW_ upper left x & y
    private int upperLeftY = 0; // (not panel)

    private int windowWidth = 0;
    private int windowHeight = 0;

    private int panelWidth = 0;
    private int panelHeight = 0;

    private float mainlineTrackWidth = 4.0F;
    private float sideTrackWidth = 2.0F;

    private Color defaultTrackColor = Color.black;
    private Color defaultOccupiedTrackColor = Color.red;
    private Color defaultAlternativeTrackColor = Color.white;
    private Color defaultBackgroundColor = Color.lightGray;
    private Color defaultTextColor = Color.black;

    private String layoutName = "";
    private double xScale = 1.0;
    private double yScale = 1.0;
    private boolean animatingLayout = true;
    private boolean showHelpBar = true;
    private boolean drawGrid = true;

    private boolean snapToGridOnAdd = false;
    private boolean snapToGridOnMove = false;
    private boolean snapToGridInvert = false;

    private boolean antialiasingOn = false;
    private boolean highlightSelectedBlockFlag = false;

    private boolean turnoutCirclesWithoutEditMode = false;
    private boolean tooltipsWithoutEditMode = false;
    private boolean tooltipsInEditMode = true;

    //turnout size parameters - saved with panel
    private double turnoutBX = LayoutTurnout.turnoutBXDefault; //RH, LH, WYE
    private double turnoutCX = LayoutTurnout.turnoutCXDefault;
    private double turnoutWid = LayoutTurnout.turnoutWidDefault;
    private double xOverLong = LayoutTurnout.xOverLongDefault; //DOUBLE_XOVER, RH_XOVER, LH_XOVER
    private double xOverHWid = LayoutTurnout.xOverHWidDefault;
    private double xOverShort = LayoutTurnout.xOverShortDefault;
    private boolean useDirectTurnoutControl = false; //Uses Left click for closing points, Right click for throwing.

    //saved state of options when panel was loaded or created
    private boolean savedEditMode = true;
    private boolean savedPositionable = true;
    private boolean savedControlLayout = true;
    private boolean savedAnimatingLayout = true;
    private boolean savedShowHelpBar = true;

    //zoom
    private double maxZoom = 6.0;
    private double minZoom = 0.25;

    //Special sub group for color treatment when active
    JPanel blockPropertiesPanel = null;

    //A hash to store string -> KeyEvent constants, used to set keyboard shortcuts per locale
    private HashMap<String, Integer> stringsToVTCodes = new HashMap<>();

    //Antialiasing rendering
    private static final RenderingHints antialiasing = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    private enum ToolBarSide {
        eTOP("top"),
        eLEFT("left"),
        eBOTTOM("bottom"),
        eRIGHT("right"),
        eFLOAT("float");

        private String name;
        private static final Map<String, ToolBarSide> ENUM_MAP;

        ToolBarSide(String name) {
            this.name = name;
        }

        //Build an immutable map of String name to enum pairs.
        static {
            Map<String, ToolBarSide> map = new ConcurrentHashMap<>();

            for (ToolBarSide instance : ToolBarSide.values()) {
                map.put(instance.getName(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static ToolBarSide getName(String name) {
            return ENUM_MAP.get(name);
        }

        public String getName() {
            return this.name;
        }
    }

    private ToolBarSide toolBarSide = ToolBarSide.eTOP;
    private boolean toolBarIsWide = true;

    public LayoutEditor() {
        this("My Layout");
    }

    public LayoutEditor(@Nonnull String name) {
        super(name);
        layoutName = name;

        //initialise keycode map
        initStringsToVTCodes();

        //initialize menu bar
        JMenuBar menuBar = new JMenuBar();

        //set up File menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuFileMnemonic")));
        menuBar.add(fileMenu);
        jmri.configurexml.StoreXmlUserAction store = new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore"));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        store.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("MenuItemStoreAccelerator")), primary_modifier));
        fileMenu.add(store);
        fileMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent event) -> {
            if (deletePanel()) {
                dispose(true);
            }
        });
        setJMenuBar(menuBar);

        //setup Options menu
        setupOptionMenu(menuBar);

        //setup Tools menu
        setupToolsMenu(menuBar);

        //setup Zoom menu
        setupZoomMenu(menuBar);

        //setup marker menu
        setupMarkerMenu(menuBar);

        //Setup Dispatcher window
        setupDispatcherMenu(menuBar);

        //setup Help menu
        addHelpMenu("package.jmri.jmrit.display.LayoutEditor", true);

        //setup group for radio buttons selecting items to add and line style
        itemGroup = new ButtonGroup();
        itemGroup.add(turnoutRHButton);
        itemGroup.add(turnoutLHButton);
        itemGroup.add(turnoutWYEButton);
        itemGroup.add(doubleXoverButton);
        itemGroup.add(rhXoverButton);
        itemGroup.add(lhXoverButton);
        itemGroup.add(levelXingButton);
        itemGroup.add(layoutSingleSlipButton);
        itemGroup.add(layoutDoubleSlipButton);
        itemGroup.add(endBumperButton);
        itemGroup.add(anchorButton);
        itemGroup.add(edgeButton);
        itemGroup.add(trackButton);
        itemGroup.add(multiSensorButton);
        itemGroup.add(sensorButton);
        itemGroup.add(signalButton);
        itemGroup.add(signalMastButton);
        itemGroup.add(textLabelButton);
        itemGroup.add(memoryButton);
        itemGroup.add(blockContentsButton);
        itemGroup.add(iconLabelButton);

        //This is used to enable/disable property controls depending on which (radio) button is selected
        ActionListener selectionListAction = (ActionEvent a) -> {
            //turnout properties
            boolean e = (turnoutRHButton.isSelected()
                    || turnoutLHButton.isSelected()
                    || turnoutWYEButton.isSelected()
                    || doubleXoverButton.isSelected()
                    || rhXoverButton.isSelected()
                    || lhXoverButton.isSelected()
                    || layoutSingleSlipButton.isSelected()
                    || layoutDoubleSlipButton.isSelected());
            log.debug("turnoutPropertiesPanel is {}", e ? "enabled" : "disabled");
            turnoutNamePanel.setEnabled(e);

            for (Component i : turnoutNamePanel.getComponents()) {
                i.setEnabled(e);
            }
            rotationPanel.setEnabled(e);

            for (Component i : rotationPanel.getComponents()) {
                i.setEnabled(e);
            }

            //second turnout property
            e = (layoutSingleSlipButton.isSelected() || layoutDoubleSlipButton.isSelected());
            log.debug("extraTurnoutPanel is {}", e ? "enabled" : "disabled");

            for (Component i : extraTurnoutPanel.getComponents()) {
                i.setEnabled(e);
            }

            //track Segment properties
            e = trackButton.isSelected();
            log.debug("trackSegmentPropertiesPanel is {}", e ? "enabled" : "disabled");

            for (Component i : trackSegmentPropertiesPanel.getComponents()) {
                i.setEnabled(e);
            }

            //block properties
            e = (turnoutRHButton.isSelected()
                    || turnoutLHButton.isSelected()
                    || turnoutWYEButton.isSelected()
                    || doubleXoverButton.isSelected()
                    || rhXoverButton.isSelected()
                    || lhXoverButton.isSelected()
                    || layoutSingleSlipButton.isSelected()
                    || layoutDoubleSlipButton.isSelected()
                    || levelXingButton.isSelected()
                    || trackButton.isSelected());
            log.debug("blockPanel is {}", e ? "enabled" : "disabled");

            if (null != blockPropertiesPanel) {
                for (Component i : blockPropertiesPanel.getComponents()) {
                    i.setEnabled(e);
                }

                if (e) {
                    blockPropertiesPanel.setBackground(Color.lightGray);
                } else {
                    blockPropertiesPanel.setBackground(new Color(238, 238, 238));
                }
            } else {
                blockNameLabel.setEnabled(e);
                blockIDComboBox.setEnabled(e);
                blockSensorNameLabel.setEnabled(e);
                blockSensorLabel.setEnabled(e);
                blockSensorComboBox.setEnabled(e);
            }

            //enable/disable text label, memory & block contents text fields
            textLabelTextField.setEnabled(textLabelButton.isSelected());
            textMemoryComboBox.setEnabled(memoryButton.isSelected());
            blockContentsComboBox.setEnabled(blockContentsButton.isSelected());

            //enable/disable signal mast, sensor & signal head text fields
            signalMastComboBox.setEnabled(signalMastButton.isSelected());
            sensorComboBox.setEnabled(sensorButton.isSelected());
            signalHeadComboBox.setEnabled(signalButton.isSelected());

            //changeIconsButton
            e = (sensorButton.isSelected()
                    || signalButton.isSelected()
                    || iconLabelButton.isSelected());
            log.debug("changeIconsButton is {}", e ? "enabled" : "disabled");
            changeIconsButton.setEnabled(e);
        };

        turnoutRHButton.addActionListener(selectionListAction);
        turnoutLHButton.addActionListener(selectionListAction);
        turnoutWYEButton.addActionListener(selectionListAction);
        doubleXoverButton.addActionListener(selectionListAction);
        rhXoverButton.addActionListener(selectionListAction);
        lhXoverButton.addActionListener(selectionListAction);
        levelXingButton.addActionListener(selectionListAction);
        layoutSingleSlipButton.addActionListener(selectionListAction);
        layoutDoubleSlipButton.addActionListener(selectionListAction);
        endBumperButton.addActionListener(selectionListAction);
        anchorButton.addActionListener(selectionListAction);
        edgeButton.addActionListener(selectionListAction);
        trackButton.addActionListener(selectionListAction);
        multiSensorButton.addActionListener(selectionListAction);
        sensorButton.addActionListener(selectionListAction);
        signalButton.addActionListener(selectionListAction);
        signalMastButton.addActionListener(selectionListAction);
        textLabelButton.addActionListener(selectionListAction);
        memoryButton.addActionListener(selectionListAction);
        blockContentsButton.addActionListener(selectionListAction);
        iconLabelButton.addActionListener(selectionListAction);

        //first row of edit tool bar items
        //turnout items
        turnoutRHButton.setSelected(true);
        turnoutRHButton.setToolTipText(Bundle.getMessage("RHToolTip"));
        turnoutLHButton.setToolTipText(Bundle.getMessage("LHToolTip"));
        turnoutWYEButton.setToolTipText(Bundle.getMessage("WYEToolTip"));
        doubleXoverButton.setToolTipText(Bundle.getMessage("DoubleCrossoverToolTip"));
        rhXoverButton.setToolTipText(Bundle.getMessage("RHCrossoverToolTip"));
        lhXoverButton.setToolTipText(Bundle.getMessage("LHCrossoverToolTip"));
        layoutSingleSlipButton.setToolTipText(Bundle.getMessage("SingleSlipToolTip"));
        layoutDoubleSlipButton.setToolTipText(Bundle.getMessage("DoubleSlipToolTip"));

        String turnoutNameString = Bundle.getMessage("Name");
        JLabel turnoutNameLabel = new JLabel(turnoutNameString);
        turnoutNamePanel.add(turnoutNameLabel);

        setupComboBox(turnoutNameComboBox, false, true);
        turnoutNameComboBox.setToolTipText(Bundle.getMessage("TurnoutNameToolTip"));
        turnoutNamePanel.add(turnoutNameComboBox);

        // disable items that are already in use
        PopupMenuListener pml = new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // This method is called before the popup menu becomes visible.
                log.debug("PopupMenuWillBecomeVisible");
                Object o = e.getSource();
                if (o instanceof JmriBeanComboBox) {
                    JmriBeanComboBox jbcb = (JmriBeanComboBox) o;
                    jmri.Manager m = jbcb.getManager();
                    if (m != null) {
                        String[] systemNames = m.getSystemNameArray();
                        for (int idx = 0; idx < systemNames.length; idx++) {
                            String systemName = systemNames[idx];
                            jbcb.setItemEnabled(idx, validatePhysicalTurnout(systemName, null));
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // This method is called before the popup menu becomes invisible
                log.debug("PopupMenuWillBecomeInvisible");
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // This method is called when the popup menu is canceled
                log.debug("PopupMenuCanceled");
            }
        };

        turnoutNameComboBox.addPopupMenuListener(pml);
        turnoutNameComboBox.setEnabledColor(Color.green.darker().darker());
        turnoutNameComboBox.setDisabledColor(Color.red);

        setupComboBox(extraTurnoutNameComboBox, false, true);
        extraTurnoutNameComboBox.setToolTipText(Bundle.getMessage("SecondTurnoutNameToolTip"));

        extraTurnoutNameComboBox.addPopupMenuListener(pml);
        extraTurnoutNameComboBox.setEnabledColor(Color.green.darker().darker());
        extraTurnoutNameComboBox.setDisabledColor(Color.red);

        //this is enabled/disabled via selectionListAction above
        JLabel extraTurnoutLabel = new JLabel(Bundle.getMessage("SecondName"));
        extraTurnoutLabel.setEnabled(false);
        extraTurnoutPanel.add(extraTurnoutLabel);
        extraTurnoutPanel.add(extraTurnoutNameComboBox);
        extraTurnoutPanel.setEnabled(false);

        String[] angleStrings = {"-180", "-135", "-90", "-45", "0", "+45", "+90", "+135", "+180"};
        rotationComboBox = new JComboBox<>(angleStrings);
        rotationComboBox.setEditable(true);
        rotationComboBox.setSelectedIndex(4);
        rotationComboBox.setMaximumRowCount(9);
        rotationComboBox.setToolTipText(Bundle.getMessage("RotationToolTip"));

        JLabel rotationLabel = new JLabel(Bundle.getMessage("Rotation"));
        rotationPanel.add(rotationLabel);
        rotationPanel.add(rotationComboBox);

        zoomPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ZoomLabel"))));
        zoomPanel.add(zoomLabel);

        Dimension coordSize = xLabel.getPreferredSize();
        coordSize.width *= 2;
        xLabel.setPreferredSize(coordSize);
        yLabel.setPreferredSize(coordSize);

        locationPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Location"))));
        locationPanel.add(new JLabel("{x:"));
        locationPanel.add(xLabel);
        locationPanel.add(new JLabel(", y:"));
        locationPanel.add(yLabel);
        locationPanel.add(new JLabel("}    "));

        //second row of edit tool bar items
        levelXingButton.setToolTipText(Bundle.getMessage("LevelCrossingToolTip"));
        trackButton.setToolTipText(Bundle.getMessage("TrackSegmentToolTip"));

        //this is enabled/disabled via selectionListAction above
        trackSegmentPropertiesPanel.add(mainlineTrack);

        mainlineTrack.setSelected(false);
        mainlineTrack.setEnabled(false);
        mainlineTrack.setToolTipText(Bundle.getMessage("MainlineCheckBoxTip"));

        trackSegmentPropertiesPanel.add(dashedLine);
        dashedLine.setSelected(false);
        dashedLine.setEnabled(false);
        dashedLine.setToolTipText(Bundle.getMessage("DashedCheckBoxTip"));

        //the blockPanel is enabled/disabled via selectionListAction above
        setupComboBox(blockIDComboBox, false, true);
        blockIDComboBox.setToolTipText(Bundle.getMessage("BlockIDToolTip"));

        //change the block name
        blockIDComboBox.addActionListener((ActionEvent a) -> {
            //use the "Extra" color to highlight the selected block
            if (highlightSelectedBlockFlag) {
                highlightBlockInComboBox(blockIDComboBox);
            }
            String newName = blockIDComboBox.getDisplayName();
            LayoutBlock b = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(newName);
            if (b != null) {
                //if there is an occupancy sensor assigned already
                String sensorName = b.getOccupancySensorName();

                if (!sensorName.isEmpty()) {
                    //update the block sensor ComboBox
                    blockSensorComboBox.setText(sensorName);
                } else {
                    blockSensorComboBox.setText("");
                }
            }
        });

        setupComboBox(blockSensorComboBox, true, true);
        blockSensorComboBox.setToolTipText(Bundle.getMessage("OccupancySensorToolTip"));

        //third row of edit tool bar items
        endBumperButton.setToolTipText(Bundle.getMessage("EndBumperToolTip"));
        anchorButton.setToolTipText(Bundle.getMessage("AnchorToolTip"));
        edgeButton.setToolTipText(Bundle.getMessage("EdgeConnectorToolTip"));
        textLabelButton.setToolTipText(Bundle.getMessage("TextLabelToolTip"));

        textLabelTextField.setToolTipText(Bundle.getMessage("TextToolTip"));
        textLabelTextField.setEnabled(false);

        memoryButton.setToolTipText(Bundle.getMessage("MemoryButtonToolTip", Bundle.getMessage("Memory")));

        setupComboBox(textMemoryComboBox, true, false);
        textMemoryComboBox.setToolTipText(Bundle.getMessage("MemoryToolTip"));

        blockContentsButton.setToolTipText(Bundle.getMessage("BlockContentsButtonToolTip"));

        setupComboBox(blockContentsComboBox, true, false);
        blockContentsComboBox.setToolTipText(Bundle.getMessage("BlockContentsButtonToolTip"));

        blockContentsComboBox.addActionListener((ActionEvent a) -> {
            //use the "Extra" color to highlight the selected block
            if (highlightSelectedBlockFlag) {
                highlightBlockInComboBox(blockContentsComboBox);
            }
        });

        //fourth row of edit tool bar items
        //multi sensor...
        multiSensorButton.setToolTipText(Bundle.getMessage("MultiSensorToolTip"));

        //Signal Mast & text
        signalMastButton.setToolTipText(Bundle.getMessage("SignalMastButtonToolTip"));
        setupComboBox(signalMastComboBox, true, false);

        //sensor icon & text
        sensorButton.setToolTipText(Bundle.getMessage("SensorButtonToolTip"));

        setupComboBox(sensorComboBox, true, false);
        sensorComboBox.setToolTipText(Bundle.getMessage("SensorIconToolTip"));

        sensorIconEditor = new MultiIconEditor(4);
        sensorIconEditor.setIcon(0, Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateActive")),
                "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        sensorIconEditor.setIcon(1, Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateInactive")),
                "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        sensorIconEditor.setIcon(2, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateInconsistent")),
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.setIcon(3, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateUnknown")),
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.complete();

        //Signal icon & text
        signalButton.setToolTipText(Bundle.getMessage("SignalButtonToolTip"));

        setupComboBox(signalHeadComboBox, true, false);
        signalHeadComboBox.setToolTipText(Bundle.getMessage("SignalIconToolTip"));

        signalIconEditor = new MultiIconEditor(10);
        signalIconEditor.setIcon(0, "Red:", "resources/icons/smallschematics/searchlights/left-red-short.gif");
        signalIconEditor.setIcon(1, "Flash red:", "resources/icons/smallschematics/searchlights/left-flashred-short.gif");
        signalIconEditor.setIcon(2, "Yellow:", "resources/icons/smallschematics/searchlights/left-yellow-short.gif");
        signalIconEditor.setIcon(3,
                "Flash yellow:",
                "resources/icons/smallschematics/searchlights/left-flashyellow-short.gif");
        signalIconEditor.setIcon(4, "Green:", "resources/icons/smallschematics/searchlights/left-green-short.gif");
        signalIconEditor.setIcon(5, "Flash green:",
                "resources/icons/smallschematics/searchlights/left-flashgreen-short.gif");
        signalIconEditor.setIcon(6, "Dark:", "resources/icons/smallschematics/searchlights/left-dark-short.gif");
        signalIconEditor.setIcon(7, "Held:", "resources/icons/smallschematics/searchlights/left-held-short.gif");
        signalIconEditor.setIcon(8,
                "Lunar",
                "resources/icons/smallschematics/searchlights/left-lunar-short-marker.gif");
        signalIconEditor.setIcon(9,
                "Flash Lunar",
                "resources/icons/smallschematics/searchlights/left-flashlunar-short-marker.gif");
        signalIconEditor.complete();

        sensorFrame = new JFrame(Bundle.getMessage("EditSensorIcons"));
        sensorFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        sensorFrame.getContentPane().add(sensorIconEditor);
        sensorFrame.pack();

        signalFrame = new JFrame(Bundle.getMessage("EditSignalIcons"));
        signalFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        // no spaces around Label as that breaks html formatting
        signalFrame.getContentPane().add(signalIconEditor);
        signalFrame.pack();
        signalFrame.setVisible(false);

        //icon label
        iconLabelButton.setToolTipText(Bundle.getMessage("IconLabelToolTip"));

        //change icons...
        //this is enabled/disabled via selectionListAction above
        changeIconsButton.addActionListener((ActionEvent a) -> {
            if (sensorButton.isSelected()) {
                sensorFrame.setVisible(true);
            } else if (signalButton.isSelected()) {
                signalFrame.setVisible(true);
            } else if (iconLabelButton.isSelected()) {
                iconFrame.setVisible(true);
            } else {
                //explain to the user why nothing happens
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ChangeIconNotApplied"),
                        Bundle.getMessage("ChangeIcons"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        changeIconsButton.setToolTipText(Bundle.getMessage("ChangeIconToolTip"));
        changeIconsButton.setEnabled(false);

        //??
        iconEditor = new MultiIconEditor(1);
        iconEditor.setIcon(0, "", "resources/icons/smallschematics/tracksegments/block.gif");
        iconEditor.complete();
        iconFrame = new JFrame(Bundle.getMessage("EditIcon"));
        iconFrame.getContentPane().add(iconEditor);
        iconFrame.pack();

        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("SansSerif", Font.PLAIN, 12),
                Color.black, new Color(215, 225, 255), Color.black));

        //setup help bar
        helpBar.setLayout(new BoxLayout(helpBar, BoxLayout.PAGE_AXIS));
        JTextArea helpTextArea1 = new JTextArea(Bundle.getMessage("Help1"));
        helpBar.add(helpTextArea1);
        JTextArea helpTextArea2 = new JTextArea(Bundle.getMessage("Help2"));
        helpBar.add(helpTextArea2);

        String helpText3 = "";

        switch (SystemType.getType()) {
            case SystemType.MACOSX: {
                helpText3 = Bundle.getMessage("Help3Mac");
                break;
            }

            case SystemType.WINDOWS: {
                helpText3 = Bundle.getMessage("Help3Win");
                break;
            }

            case SystemType.LINUX: {
                helpText3 = Bundle.getMessage("Help3Win");
                break;
            }

            default:
                helpText3 = Bundle.getMessage("Help3");
        } //switch

        JTextArea helpTextArea3 = new JTextArea(helpText3);
        helpBar.add(helpTextArea3);

        //set to full screen
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        height = screenDim.height - 120;
        width = screenDim.width - 20;

        //Let Editor make target, and use this frame
        super.setTargetPanel(null, null);
        super.setTargetPanelSize(width, height);
        setSize(screenDim.width, screenDim.height);

        setupToolBar();

        //register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(this);
        }

        //confirm that panel hasn't already been loaded
        if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
        }
        ///TODO: verify that this isn't needed... then dead code strip
        ///InstanceManager.getDefault(PanelMenu.class).addEditorPanel(this);
        thisPanel = this;
        thisPanel.setFocusable(true);
        thisPanel.addKeyListener(this);
        resetDirty();

        //establish link to LayoutEditorAuxTools
        auxTools = new LayoutEditorAuxTools(thisPanel);

        SwingUtilities.invokeLater(() -> {
            //initialize preferences
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                String windowFrameRef = getWindowFrameRef();

                Object prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarSide");
                //log.debug("{}.toolBarSide is {}", windowFrameRef, prefsProp);
                if (prefsProp != null) {
                    ToolBarSide newToolBarSide = ToolBarSide.getName((String) prefsProp);
                    setToolBarSide(newToolBarSide);
                }

                //Note: since prefs default to false and we want wide to be the default
                //we invert it and save it as thin
                boolean prefsToolBarIsWide = prefsMgr.getSimplePreferenceState(windowFrameRef + ".toolBarThin");
                log.debug("{}.toolBarThin is {}", windowFrameRef, prefsProp);
                setToolBarWide(prefsToolBarIsWide);

                boolean prefsShowHelpBar = prefsMgr.getSimplePreferenceState(windowFrameRef + ".showHelpBar");
                //log.debug("{}.showHelpBar is {}", windowFrameRef, prefsShowHelpBar);
                setShowHelpBar(prefsShowHelpBar);

                boolean prefsAntialiasingOn = prefsMgr.getSimplePreferenceState(windowFrameRef + ".antialiasingOn");
                //log.debug("{}.antialiasingOn is {}", windowFrameRef, prefsAntialiasingOn);
                setAntialiasingOn(prefsAntialiasingOn);

                boolean prefsHighlightSelectedBlockFlag
                        = prefsMgr.getSimplePreferenceState(windowFrameRef + ".highlightSelectedBlock");
                //log.debug("{}.highlightSelectedBlock is {}", windowFrameRef, prefsHighlightSelectedBlockFlag);
                setHighlightSelectedBlock(prefsHighlightSelectedBlockFlag);

                prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarFontSize");
                //log.debug("{} prefsProp toolBarFontSize is {}", windowFrameRef, prefsProp);
                if (null != prefsProp) {
                    float toolBarFontSize = Float.parseFloat(prefsProp.toString());
                    //setupToolBarFontSizes(toolBarFontSize);
                }
                updateAllComboBoxesDropDownListDisplayOrderFromPrefs();

                //this doesn't work as expected (1st one called messes up 2nd?)
                Point prefsWindowLocation = prefsMgr.getWindowLocation(windowFrameRef);
                Dimension prefsWindowSize = prefsMgr.getWindowSize(windowFrameRef);
                log.debug("prefsMgr.prefsWindowLocation({}) is {}", windowFrameRef, prefsWindowLocation);
                log.debug("prefsMgr.prefsWindowSize is({}) {}", windowFrameRef, prefsWindowSize);

                //Point prefsWindowLocation = null;
                //Dimension prefsWindowSize = null;
                //use this instead?
                if (true) { //(Nope, it's not working ether: prefsProp always comes back null)
                    prefsProp = prefsMgr.getProperty(windowFrameRef, "windowRectangle2D");
                    log.debug("prefsMgr.getProperty({}, \"windowRectangle2D\") is {}", windowFrameRef, prefsProp);

                    if (null != prefsProp) {
                        Rectangle2D windowRectangle2D = (Rectangle2D) prefsProp;
                        prefsWindowLocation.setLocation(windowRectangle2D.getX(), windowRectangle2D.getY());
                        prefsWindowSize.setSize(windowRectangle2D.getWidth(), windowRectangle2D.getHeight());
                    }
                }

                if ((prefsWindowLocation != null) && (prefsWindowSize != null)
                        && (prefsWindowSize.width >= 640) && (prefsWindowSize.height >= 480)) {
                    //note: panel width & height comes from the saved (xml) panel (file) on disk
                    setLayoutDimensions(prefsWindowSize.width, prefsWindowSize.height,
                            prefsWindowLocation.x, prefsWindowLocation.y,
                            panelWidth, panelHeight, true);
                }
            }); //InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr)
        });
    } //LayoutEditor (constructor)

    private void createFloatingEditToolBox() {
        if (floatingEditToolBox == null) {
            if (floatingEditContent == null) {
                // Create the window content if necessary, normally on first load or switching between toolbox and toolbar
                createFloatingEditContent();
            }

            if (isEditable() && floatingEditToolBox == null) {
                //Create the window and add the toolbox content
                floatingEditToolBox = new JmriJFrame(Bundle.getMessage("ToolBox", layoutName));
                floatingEditToolBox.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                floatingEditToolBox.setContentPane(floatingEditContent);
                floatingEditToolBox.pack();
                floatingEditToolBox.setAlwaysOnTop(true);
                floatingEditToolBox.setVisible(true);
            }
        }
    }

    private void deleteFloatingEditToolBox() {
        if (floatingEditToolBox != null) {
            floatingEditToolBox.dispose();
            floatingEditToolBox = null;
        }
    }

    private void createFloatingEditContent() {
        /*
         * JFrame - floatingEditToolBox
         *     JScrollPane - floatingEditContent
         *         JPanel - floatingEditPanel
         *             JPanel - floatEditTabsPanel
         *                 JTabbedPane - floatEditTabsPane
         *                     ...
         *             JPanel - floatEditLocationPanel
         *                 ...
         *             JPanel - floatEditActionPanel  (currently disabled)
         *                 ...
         *             JPanel - floatEditHelpPanel
         *                 ...
         */

        FlowLayout floatContentLayout = new FlowLayout(FlowLayout.CENTER, 5, 2); //5 pixel gap between items, 2 vertical gap

        //Contains the block and sensor combo boxes.
        //It is moved to the appropriate detail pane when the tab changes.
        blockPropertiesPanel = new JPanel(floatContentLayout);
        String blockNameString = Bundle.getMessage("BlockID");
        blockSensorNameLabel = new JLabel(blockNameString);
        blockPropertiesPanel.add(blockNameLabel);
        blockPropertiesPanel.add(blockIDComboBox);
        blockPropertiesPanel.add(blockSensorLabel);
        blockPropertiesPanel.add(blockSensorComboBox);

        //Build the window content
        JPanel floatingEditPanel = new JPanel();
        floatingEditPanel.setLayout(new BoxLayout(floatingEditPanel, BoxLayout.Y_AXIS));

        //Begin the tabs structure
        JPanel floatEditTabsPanel = new JPanel();
        JTabbedPane floatEditTabsPane = new JTabbedPane();

        //Tab 0 - Turnouts
        JPanel floatEditTurnout = new JPanel();
        floatEditTurnout.setLayout(new BoxLayout(floatEditTurnout, BoxLayout.Y_AXIS));

        JPanel turnoutGroup1 = new JPanel(floatContentLayout);
        turnoutGroup1.add(turnoutRHButton);
        turnoutGroup1.add(turnoutLHButton);
        turnoutGroup1.add(turnoutWYEButton);
        floatEditTurnout.add(turnoutGroup1);

        JPanel turnoutGroup2 = new JPanel(floatContentLayout);
        turnoutGroup2.add(doubleXoverButton);
        turnoutGroup2.add(rhXoverButton);
        turnoutGroup2.add(lhXoverButton);
        floatEditTurnout.add(turnoutGroup2);

        JPanel turnoutGroup3 = new JPanel(floatContentLayout);
        turnoutGroup3.add(layoutSingleSlipButton);
        turnoutGroup3.add(layoutDoubleSlipButton);
        floatEditTurnout.add(turnoutGroup3);

        JPanel turnoutGroup4 = new JPanel(floatContentLayout);
        turnoutGroup4.add(turnoutNamePanel);
        turnoutGroup4.add(extraTurnoutPanel);
        floatEditTurnout.add(turnoutGroup4);

        JPanel turnoutGroup5 = new JPanel(floatContentLayout);
        turnoutGroup5.add(rotationPanel);
        floatEditTurnout.add(turnoutGroup5);

        floatEditTurnout.add(blockPropertiesPanel);
        floatEditTabsPane.addTab(Bundle.getMessage("TabTurnout"), null, floatEditTurnout, null);

        //Tab 1 - Track
        JPanel floatEditTrack = new JPanel();
        floatEditTrack.setLayout(new BoxLayout(floatEditTrack, BoxLayout.Y_AXIS));

        JPanel trackGroup1 = new JPanel(floatContentLayout);
        trackGroup1.add(endBumperButton);
        trackGroup1.add(anchorButton);
        trackGroup1.add(edgeButton);
        floatEditTrack.add(trackGroup1);

        JPanel trackGroup2 = new JPanel(floatContentLayout);
        trackGroup2.add(trackButton);
        trackGroup2.add(levelXingButton);
        floatEditTrack.add(trackGroup2);

        JPanel trackGroup3 = new JPanel(floatContentLayout);
        trackGroup3.add(trackSegmentPropertiesPanel);
        floatEditTrack.add(trackGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabTrack"), null, floatEditTrack, null);

        //Tab 2 - Labels
        JPanel floatEditLabel = new JPanel();
        floatEditLabel.setLayout(new BoxLayout(floatEditLabel, BoxLayout.Y_AXIS));

        JPanel labelGroup1 = new JPanel(floatContentLayout);
        labelGroup1.add(textLabelButton);
        labelGroup1.add(textLabelTextField);
        floatEditLabel.add(labelGroup1);

        JPanel labelGroup2 = new JPanel(floatContentLayout);
        labelGroup2.add(memoryButton);
        labelGroup2.add(textMemoryComboBox);
        floatEditLabel.add(labelGroup2);

        JPanel labelGroup3 = new JPanel(floatContentLayout);
        labelGroup3.add(blockContentsButton);
        labelGroup3.add(blockContentsComboBox);
        floatEditLabel.add(labelGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabLabel"), null, floatEditLabel, null);

        //Tab 3 - Icons
        JPanel floatEditIcon = new JPanel();
        floatEditIcon.setLayout(new BoxLayout(floatEditIcon, BoxLayout.Y_AXIS));

        JPanel iconGroup1 = new JPanel(floatContentLayout);
        iconGroup1.add(multiSensorButton);
        iconGroup1.add(changeIconsButton);
        floatEditIcon.add(iconGroup1);

        JPanel iconGroup2 = new JPanel(floatContentLayout);
        iconGroup2.add(sensorButton);
        iconGroup2.add(sensorComboBox);
        floatEditIcon.add(iconGroup2);

        JPanel iconGroup3 = new JPanel(floatContentLayout);
        iconGroup3.add(signalMastButton);
        iconGroup3.add(signalMastComboBox);
        floatEditIcon.add(iconGroup3);

        JPanel iconGroup4 = new JPanel(floatContentLayout);
        iconGroup4.add(signalButton);
        iconGroup4.add(signalHeadComboBox);
        floatEditIcon.add(iconGroup4);

        JPanel iconGroup5 = new JPanel(floatContentLayout);
        iconGroup5.add(iconLabelButton);
        floatEditIcon.add(iconGroup5);

        floatEditTabsPane.addTab(Bundle.getMessage("TabIcon"), null, floatEditIcon, null);
        floatEditTabsPanel.add(floatEditTabsPane);
        floatingEditPanel.add(floatEditTabsPanel);

        //End the tabs structure
        //The next 3 groups reside under the tab secton
        JPanel floatEditLocationPanel = new JPanel();
        floatEditLocationPanel.add(zoomPanel);
        floatEditLocationPanel.add(locationPanel);
        floatingEditPanel.add(floatEditLocationPanel);

// JPanel floatEditActionPanel = new JPanel();
// floatEditActionPanel.add(new JLabel("floatEditActionPanel", JLabel.CENTER));
// floatingEditPanel.add(floatEditActionPanel);
        floatEditHelpPanel = new JPanel();
        floatingEditPanel.add(floatEditHelpPanel);

        //Notice: End tree structure indenting
        //Create a scroll pane to hold the window content.
        floatingEditContent = new JScrollPane(floatingEditPanel);
        floatingEditContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        floatingEditContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Force the help panel width to the same as the tabs section
        int tabSectionWidth = floatEditTabsPanel.getWidth();

        //Change the textarea settings
        for (Component c : helpBar.getComponents()) {
            if (c instanceof JTextArea) {
                JTextArea j = (JTextArea) c;
                j.setSize(new Dimension(tabSectionWidth, j.getSize().height));
                j.setLineWrap(true);
                j.setWrapStyleWord(true);
            }
        }

        //Change the width of the help panel section
        floatEditHelpPanel.setMaximumSize(new Dimension(tabSectionWidth, Integer.MAX_VALUE));
        floatEditHelpPanel.add(helpBar);
        floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());

        floatEditTabsPane.addChangeListener((e) -> {
            //Move the block group between the turnouts and track tabs
            int selIndex = floatEditTabsPane.getSelectedIndex();

            if (selIndex == 0) {
                floatEditTurnout.add(blockPropertiesPanel);
            } else if (selIndex == 1) {
                floatEditTrack.add(blockPropertiesPanel);
            }
        });
    } //createFloatingEditContent

    private void setupToolBar() {
        //Initial setup for both horizontal and vertical
        Container contentPane = getContentPane();

        //remove these (if present) so we can add them back (without duplicates)
        if (editToolBarContainer != null) {
            editToolBarContainer.setVisible(false);
            contentPane.remove(editToolBarContainer);
        }

        if (helpBarPanel != null) {
            contentPane.remove(helpBarPanel);
        }

        deleteFloatingEditToolBox();
        if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
            createFloatingEditToolBox();
            return;
        }

        boolean toolBarIsVertical = (toolBarSide.equals(ToolBarSide.eRIGHT) || toolBarSide.equals(ToolBarSide.eLEFT));

        editToolBarPanel = new JPanel();
        editToolBarPanel.setLayout(new BoxLayout(editToolBarPanel, BoxLayout.PAGE_AXIS));

        JPanel outerBorderPanel = editToolBarPanel;
        JPanel innerBorderPanel = editToolBarPanel;

        Border blacklineBorder = BorderFactory.createLineBorder(Color.black);

        boolean useBorders = !(toolBarSide.equals(ToolBarSide.eTOP) || toolBarSide.equals(ToolBarSide.eBOTTOM));

        if (useBorders) {
            outerBorderPanel = new JPanel();
            outerBorderPanel.setLayout(new BoxLayout(outerBorderPanel, BoxLayout.PAGE_AXIS));
            TitledBorder outerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Track"));
            outerTitleBorder.setTitleJustification(TitledBorder.CENTER);
            outerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
            outerBorderPanel.setBorder(outerTitleBorder);

            innerBorderPanel = new JPanel();
            innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
            TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("BeanNameTurnout"));
            innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
            innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
            innerBorderPanel.setBorder(innerTitleBorder);
        }

        String blockNameString = Bundle.getMessage("BlockID");

        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        if (toolBarIsVertical) {
            FlowLayout verticalTitleLayout = new FlowLayout(FlowLayout.CENTER, 5, 5); //5 pixel gap between items, 5 vertical gap
            FlowLayout verticalContentLayout = new FlowLayout(FlowLayout.LEFT, 5, 2); //5 pixel gap between items, 2 vertical gap

            turnoutLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("BeanNameTurnout")));

            if (!useBorders) {
                JPanel vTop1TitlePanel = new JPanel(verticalTitleLayout);
                vTop1TitlePanel.add(turnoutLabel);
                vTop1TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1TitlePanel.getPreferredSize().height));
                innerBorderPanel.add(vTop1TitlePanel);
            }

            JPanel vTop1Panel = new JPanel(verticalContentLayout);
            vTop1Panel.add(turnoutLHButton);
            vTop1Panel.add(turnoutRHButton);
            vTop1Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop1Panel);

            JPanel vTop2Panel = new JPanel(verticalContentLayout);
            vTop2Panel.add(turnoutWYEButton);
            vTop2Panel.add(doubleXoverButton);
            vTop2Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop2Panel.getPreferredSize().height * 2));
            innerBorderPanel.add(vTop2Panel);

            JPanel vTop3Panel = new JPanel(verticalContentLayout);
            vTop3Panel.add(lhXoverButton);
            vTop3Panel.add(rhXoverButton);
            vTop3Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop3Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop3Panel);

            JPanel vTop4Panel = new JPanel(verticalContentLayout);
            vTop4Panel.add(layoutSingleSlipButton);
            vTop4Panel.add(layoutDoubleSlipButton);
            vTop4Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop4Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop4Panel);

            JPanel vTop5Panel = new JPanel(verticalContentLayout);
            vTop5Panel.add(turnoutNamePanel);
            vTop5Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop5Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop5Panel);

            JPanel vTop6Panel = new JPanel(verticalContentLayout);
            vTop6Panel.add(extraTurnoutPanel);
            vTop6Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop6Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop6Panel);

            JPanel vTop7Panel = new JPanel(verticalContentLayout);
            vTop7Panel.add(rotationPanel);
            vTop7Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop7Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop7Panel);

            if (useBorders) {
                outerBorderPanel.add(innerBorderPanel);
            }
            trackLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Track")));

            if (!useBorders) {
                JPanel vTop8TitlePanel = new JPanel(verticalTitleLayout);
                vTop8TitlePanel.add(trackLabel);
                vTop8TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop8TitlePanel.getPreferredSize().height));
                outerBorderPanel.add(vTop8TitlePanel);
            }

            JPanel vTop8Panel = new JPanel(verticalContentLayout);
            vTop8Panel.add(levelXingButton);
            vTop8Panel.add(trackButton);
            vTop8Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop8Panel.getPreferredSize().height));
            outerBorderPanel.add(vTop8Panel);

            //this would be vTop9Panel
            trackSegmentPropertiesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    trackSegmentPropertiesPanel.getPreferredSize().height));
            outerBorderPanel.add(trackSegmentPropertiesPanel);

            JPanel vTop10Panel = new JPanel(verticalContentLayout);
            blockNameLabel = new JLabel(blockNameString);
            vTop10Panel.add(blockNameLabel);
            vTop10Panel.add(blockIDComboBox);
            vTop10Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop10Panel.getPreferredSize().height));
            outerBorderPanel.add(vTop10Panel);

            JPanel vTop11Panel = new JPanel(verticalContentLayout);
            blockSensorNameLabel = new JLabel(blockNameString);
            vTop11Panel.add(blockSensorNameLabel);
            vTop11Panel.add(blockSensorLabel);
            vTop11Panel.add(blockSensorComboBox);
            vTop11Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop11Panel.getPreferredSize().height));
            outerBorderPanel.add(vTop11Panel);

            if (useBorders) {
                editToolBarPanel.add(outerBorderPanel);
            }

            JPanel nodesBorderPanel = editToolBarPanel;
            nodesLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Nodes")));

            if (useBorders) {
                nodesBorderPanel = new JPanel();
                nodesBorderPanel.setLayout(new BoxLayout(nodesBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Nodes"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                nodesBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop12TitlePanel = new JPanel(verticalTitleLayout);
                vTop12TitlePanel.add(nodesLabel);
                vTop12TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12TitlePanel.getPreferredSize().height));
                editToolBarPanel.add(vTop12TitlePanel);
            }

            JPanel vTop12Panel = new JPanel(verticalContentLayout);
            vTop12Panel.add(endBumperButton);
            vTop12Panel.add(anchorButton);
            vTop12Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12Panel.getPreferredSize().height));
            nodesBorderPanel.add(vTop12Panel);

            JPanel vTop13Panel = new JPanel(verticalContentLayout);
            vTop13Panel.add(edgeButton);
            vTop13Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop13Panel.getPreferredSize().height));
            nodesBorderPanel.add(vTop13Panel);

            if (useBorders) {
                editToolBarPanel.add(nodesBorderPanel);
            }

            JPanel labelsBorderPanel = editToolBarPanel;
            labelsLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Labels")));

            if (useBorders) {
                labelsBorderPanel = new JPanel();
                labelsBorderPanel.setLayout(new BoxLayout(labelsBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Labels"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                labelsBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop14TitlePanel = new JPanel(verticalTitleLayout);
                vTop14TitlePanel.add(labelsLabel);
                vTop14TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop14TitlePanel.getPreferredSize().height));
                editToolBarPanel.add(vTop14TitlePanel);
            }

            JPanel vTop14Panel = new JPanel(verticalContentLayout);
            vTop14Panel.add(textLabelButton);
            vTop14Panel.add(textLabelTextField);
            vTop14Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop14Panel.getPreferredSize().height));
            labelsBorderPanel.add(vTop14Panel);

            JPanel vTop15Panel = new JPanel(verticalContentLayout);
            vTop15Panel.add(memoryButton);
            vTop15Panel.add(textMemoryComboBox);
            vTop15Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop15Panel.getPreferredSize().height));
            labelsBorderPanel.add(vTop15Panel);

            JPanel vTop16Panel = new JPanel(verticalContentLayout);
            vTop16Panel.add(blockContentsButton);
            vTop16Panel.add(blockContentsComboBox);
            vTop16Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop16Panel.getPreferredSize().height));
            labelsBorderPanel.add(vTop16Panel);

            if (useBorders) {
                editToolBarPanel.add(labelsBorderPanel);
            }

            JPanel iconsBorderPanel = editToolBarPanel;

            if (useBorders) {
                iconsBorderPanel = new JPanel();
                iconsBorderPanel.setLayout(new BoxLayout(iconsBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("IconsTitle"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                iconsBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop17TitlePanel = new JPanel(verticalTitleLayout);
                JLabel iconsLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("IconsTitle")));
                vTop17TitlePanel.add(iconsLabel);
                vTop17TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop17TitlePanel.getPreferredSize().height));
                editToolBarPanel.add(vTop17TitlePanel);
            }

            JPanel vTop18Panel = new JPanel(verticalContentLayout);
            vTop18Panel.add(multiSensorButton);
            vTop18Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop18Panel.getPreferredSize().height));
            iconsBorderPanel.add(vTop18Panel);

            JPanel vTop20Panel = new JPanel(verticalContentLayout);
            vTop20Panel.add(sensorButton);
            vTop20Panel.add(sensorComboBox);
            vTop20Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop20Panel.getPreferredSize().height));
            iconsBorderPanel.add(vTop20Panel);

            JPanel vTop19Panel = new JPanel(verticalContentLayout);
            vTop19Panel.add(signalMastButton);
            vTop19Panel.add(signalMastComboBox);
            vTop19Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop19Panel.getPreferredSize().height));
            iconsBorderPanel.add(vTop19Panel);

            JPanel vTop21Panel = new JPanel(verticalContentLayout);
            vTop21Panel.add(signalButton);
            vTop21Panel.add(signalHeadComboBox);
            vTop21Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop21Panel.getPreferredSize().height));
            iconsBorderPanel.add(vTop21Panel);

            JPanel vTop22Panel = new JPanel(verticalContentLayout);
            vTop22Panel.add(iconLabelButton);
            vTop22Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop22Panel.getPreferredSize().height));
            vTop22Panel.add(changeIconsButton);
            iconsBorderPanel.add(vTop22Panel);

            if (useBorders) {
                editToolBarPanel.add(iconsBorderPanel);
            }
            editToolBarPanel.add(Box.createVerticalGlue());

            JPanel bottomPanel = new JPanel();
            zoomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, zoomPanel.getPreferredSize().height));
            locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, locationPanel.getPreferredSize().height));
            bottomPanel.add(zoomPanel);
            bottomPanel.add(locationPanel);
            bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bottomPanel.getPreferredSize().height));
            editToolBarPanel.add(bottomPanel, BorderLayout.SOUTH);
        } else {
            //Row 1
            JPanel hTop1Panel = new JPanel();
            hTop1Panel.setLayout(new BoxLayout(hTop1Panel, BoxLayout.LINE_AXIS));

            //Row 1 : Left Components
            JPanel hTop1Left = new JPanel(leftRowLayout);
            turnoutLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
            if (!useBorders) {
                hTop1Left.add(turnoutLabel);
            }
            hTop1Left.add(turnoutRHButton);
            hTop1Left.add(turnoutLHButton);
            hTop1Left.add(turnoutWYEButton);
            hTop1Left.add(doubleXoverButton);
            hTop1Left.add(rhXoverButton);
            hTop1Left.add(lhXoverButton);
            hTop1Left.add(layoutSingleSlipButton);
            hTop1Left.add(layoutDoubleSlipButton);
            hTop1Panel.add(hTop1Left);

            if (toolBarIsWide) {
                hTop1Panel.add(Box.createHorizontalGlue());

                JPanel hTop1Right = new JPanel(rightRowLayout);
                hTop1Right.add(turnoutNamePanel);
                hTop1Right.add(extraTurnoutPanel);
                hTop1Right.add(rotationPanel);
                hTop1Panel.add(hTop1Right);
            }
            innerBorderPanel.add(hTop1Panel);

            //row 2
            if (!toolBarIsWide) {
                JPanel hTop2Panel = new JPanel();
                hTop2Panel.setLayout(new BoxLayout(hTop2Panel, BoxLayout.LINE_AXIS));

                //Row 2 : Left Components
                JPanel hTop2Center = new JPanel(centerRowLayout);
                hTop2Center.add(turnoutNamePanel);
                hTop2Center.add(extraTurnoutPanel);
                hTop2Center.add(rotationPanel);
                hTop2Panel.add(hTop2Center);

                innerBorderPanel.add(hTop2Panel);
            }

            if (useBorders) {
                outerBorderPanel.add(innerBorderPanel);
            }

            //Row 3 (2 wide)
            JPanel hTop3Panel = new JPanel();
            hTop3Panel.setLayout(new BoxLayout(hTop3Panel, BoxLayout.LINE_AXIS));

            //Row 3 : Left Components
            JPanel hTop3Left = new JPanel(leftRowLayout);
            trackLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Track")));

            if (!useBorders) {
                hTop3Left.add(trackLabel);
            }
            hTop3Left.add(levelXingButton);
            hTop3Left.add(trackButton);
            hTop3Left.add(trackSegmentPropertiesPanel);

            hTop3Panel.add(hTop3Left);
            hTop3Panel.add(Box.createHorizontalGlue());

            //Row 3 : Center Components
            JPanel hTop3Center = new JPanel(centerRowLayout);
            blockNameLabel = new JLabel(blockNameString);
            hTop3Center.add(blockNameLabel);
            hTop3Center.add(blockIDComboBox);
            hTop3Center.add(blockSensorLabel);
            hTop3Center.add(blockSensorComboBox);

            hTop3Panel.add(hTop3Center);
            hTop3Panel.add(Box.createHorizontalGlue());

            if (toolBarIsWide) {
                //row 3 : Right Components
                JPanel hTop3Right = new JPanel(rightRowLayout);
                hTop3Right.add(zoomPanel);
                hTop3Right.add(locationPanel);
                hTop3Panel.add(hTop3Right);
            }
            outerBorderPanel.add(hTop3Panel);

            if (useBorders) {
                editToolBarPanel.add(outerBorderPanel);
            }

            //Row 4
            JPanel hTop4Panel = new JPanel();
            hTop4Panel.setLayout(new BoxLayout(hTop4Panel, BoxLayout.LINE_AXIS));

            //Row 4 : Left Components
            JPanel hTop4Left = new JPanel(leftRowLayout);
            nodesLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Nodes")));
            hTop4Left.add(nodesLabel);
            hTop4Left.add(endBumperButton);
            hTop4Left.add(anchorButton);
            hTop4Left.add(edgeButton);
            hTop4Panel.add(hTop4Left);
            hTop4Panel.add(Box.createHorizontalGlue());

            if (!toolBarIsWide) {
                //Row 4 : Right Components
                JPanel hTop4Right = new JPanel(rightRowLayout);
                hTop4Right.add(zoomPanel);
                hTop4Right.add(locationPanel);
                hTop4Panel.add(hTop4Right);
            }
            editToolBarPanel.add(hTop4Panel);

            //Row 5 Components (wide 4-center)
            labelsLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Labels")));
            if (toolBarIsWide) {
                JPanel hTop4Center = new JPanel(centerRowLayout);
                hTop4Center.add(labelsLabel);
                hTop4Center.add(textLabelButton);
                hTop4Center.add(textLabelTextField);
                hTop4Center.add(memoryButton);
                hTop4Center.add(textMemoryComboBox);
                hTop4Center.add(blockContentsButton);
                hTop4Center.add(blockContentsComboBox);
                hTop4Panel.add(hTop4Center);
                hTop4Panel.add(Box.createHorizontalGlue());
                editToolBarPanel.add(hTop4Panel);
            } else {
                editToolBarPanel.add(hTop4Panel);

                JPanel hTop5Left = new JPanel(leftRowLayout);
                hTop5Left.add(labelsLabel);
                hTop5Left.add(textLabelButton);
                hTop5Left.add(textLabelTextField);
                hTop5Left.add(memoryButton);
                hTop5Left.add(textMemoryComboBox);
                hTop5Left.add(blockContentsButton);
                hTop5Left.add(blockContentsComboBox);
                hTop5Left.add(Box.createHorizontalGlue());
                editToolBarPanel.add(hTop5Left);
            }

            //Row 6
            JPanel hTop6Panel = new JPanel();
            hTop6Panel.setLayout(new BoxLayout(hTop6Panel, BoxLayout.LINE_AXIS));

            //Row 6 : Left Components
            //JPanel hTop6Left = new JPanel(centerRowLayout);
            JPanel hTop6Left = new JPanel(leftRowLayout);
            hTop6Left.add(multiSensorButton);
            hTop6Left.add(changeIconsButton);
            hTop6Left.add(sensorButton);
            hTop6Left.add(sensorComboBox);
            hTop6Left.add(signalMastButton);
            hTop6Left.add(signalMastComboBox);
            hTop6Left.add(signalButton);
            hTop6Left.add(signalHeadComboBox);
            hTop6Left.add(new JLabel(" "));
            hTop6Left.add(iconLabelButton);

            hTop6Panel.add(hTop6Left);
            editToolBarPanel.add(hTop6Panel);
        } // if (toolBarIsVertical) {} else...

        editToolBarScroll = new JScrollPane(editToolBarPanel);

        if (toolBarIsVertical) {
            width = editToolBarScroll.getPreferredSize().width;
            height = screenDim.height;
        } else {
            width = screenDim.width;
            height = editToolBarScroll.getPreferredSize().height;
        }
        editToolBarContainer = new JPanel();
        editToolBarContainer.setLayout(new BoxLayout(editToolBarContainer, BoxLayout.PAGE_AXIS));
        editToolBarContainer.add(editToolBarScroll);

        //setup notification for when horizontal scrollbar changes visibility
        //editToolBarScroll.getViewport().addChangeListener(e -> {
        //log.warn("scrollbars visible: " + editToolBarScroll.getHorizontalScrollBar().isVisible());
        //});
        editToolBarContainer.setMinimumSize(new Dimension(width, height));
        editToolBarContainer.setPreferredSize(new Dimension(width, height));

        helpBarPanel = new JPanel();
        helpBarPanel.add(helpBar);

        for (Component c : helpBar.getComponents()) {
            if (c instanceof JTextArea) {
                JTextArea j = (JTextArea) c;
                j.setSize(new Dimension(width, j.getSize().height));
                j.setLineWrap(toolBarIsVertical);
                j.setWrapStyleWord(toolBarIsVertical);
            }
        }
        contentPane.setLayout(new BoxLayout(contentPane, toolBarIsVertical ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS));

        switch (toolBarSide) {
            case eTOP:
            case eLEFT:
                contentPane.add(editToolBarContainer, 0);
                break;

            case eBOTTOM:
            case eRIGHT:
                contentPane.add(editToolBarContainer);
                break;

            default:
                // fall through
                break;
        } //switch

        if (toolBarIsVertical) {
            editToolBarContainer.add(helpBarPanel);
        } else {
            contentPane.add(helpBarPanel);
        }
        helpBarPanel.setVisible(isEditable() && getShowHelpBar());
        editToolBarContainer.setVisible(isEditable());

        if (false) {
            //use the GuiLafPreferencesManager value
            ///doing this for now (since window prefs seem to be whacked)
            GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
            setupToolBarFontSizes(manager.getFontSize());
        } else {
            //see if the user preferences for the panel have a setting for it
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                //if it's been set as a preference for this panel use that
                //calculate the largest font size that will fill the current window
                //(without scrollbars)
                //font size 13 ==> min windowWidth width = 1592 pixels
                //font size 8 ==> min windowWidth width = 1132 pixels
                //(1592 - 1132) / (13 - 8) ==> 460 / 5 ==> 92 pixel per font size
                //1592 - (13 * 92) ==> 396 pixels
                //therefore:
                float newToolBarFontSize = (float) Math.floor(((windowWidth - 396.f) / 92.f) - 0.5f);
                newToolBarFontSize = (float) MathUtil.pin(newToolBarFontSize, 9.0, 12.0); //keep it between 9 & 12

                String windowFrameRef = getWindowFrameRef();
                Object prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarFontSize");

                //log.debug("{} prefsProp is {}", windowFrameRef, prefsProp);
                if (prefsProp != null) { //(yes)
                    newToolBarFontSize = Float.parseFloat(prefsProp.toString());
                } else { //(no)
                    //use the GuiLafPreferencesManager value
                    GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
                    newToolBarFontSize = manager.getFontSize();

                    //save it in user preferences for the panel
                    prefsMgr.setProperty(windowFrameRef, "toolBarFontSize", newToolBarFontSize);
                }
                setupToolBarFontSizes(newToolBarFontSize);
            });
        }
    } //setupToolBar()

    //
    //recursive routine to walk a container hierarchy and set all conponent fonts
    //
    private void recursiveSetFont(@Nonnull Container inContainer, @Nullable Font inFont) {
        if (false) { //<<== disabled as per <https://github.com/JMRI/JMRI/pull/3145#issuecomment-283940658>
            for (Component c : inContainer.getComponents()) {
                c.setFont(inFont);

                if (c instanceof Container) {
                    recursiveSetFont((Container) c, toolBarFont);
                }
            }
        }
    } //recursiveSetFont

    //
    //set the font sizes for all toolbar objects
    //
    private float toolBarFontSize = 12.f;

    private void setupToolBarFontSizes(float newToolBarFontSize) {
        if (toolBarFontSize != newToolBarFontSize) {
            toolBarFontSize = newToolBarFontSize;

            log.debug("Font size: {}", newToolBarFontSize);

            toolBarFont = zoomLabel.getFont();
            toolBarFont = toolBarFont.deriveFont(newToolBarFontSize);

            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                recursiveSetFont(floatingEditContent, toolBarFont);
            } else {
                recursiveSetFont(editToolBarPanel, toolBarFont);
            }
        }
    } //setupToolBarFontSizes

    @Override
    protected void init(String name) {
    }

    @Override
    public void initView() {
        editModeCheckBoxMenuItem.setSelected(isEditable());

        positionableCheckBoxMenuItem.setSelected(allPositionable());
        controlCheckBoxMenuItem.setSelected(allControlling());

        if (isEditable()) {
            setAllShowToolTip(tooltipsInEditMode);
        } else {
            setAllShowToolTip(tooltipsWithoutEditMode);
        }

        switch (_scrollState) {
            case Editor.SCROLL_NONE: {
                scrollNone.setSelected(true);
                break;
            }

            case Editor.SCROLL_BOTH: {
                scrollBoth.setSelected(true);
                break;
            }

            case Editor.SCROLL_HORIZONTAL: {
                scrollHorizontal.setSelected(true);
                break;
            }

            case Editor.SCROLL_VERTICAL: {
                scrollVertical.setSelected(true);
                break;
            }

            default: {
                break;
            }
        } //switch
    } //initView

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        log.debug("Frame size: {w:{}, h:{}}", width, height);
    }

    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        boolean save = (isDirty() || (savedEditMode != isEditable())
                || (savedPositionable != allPositionable())
                || (savedControlLayout != allControlling())
                || (savedAnimatingLayout != isAnimating())
                || (savedShowHelpBar != getShowHelpBar()));

        targetWindowClosing(save);
    } //targetWindowClosingEvent

    /**
     * Set up editable JmriBeanComboBoxes
     *
     * @param inComboBox     the editable JmriBeanComboBoxes to set up
     * @param inValidateMode boolean: if true, valid text == green, invalid text
     *                       == red background; if false, valid text == green,
     *                       invalid text == yellow background
     * @param inEnable       boolean to enable / disable the JmriBeanComboBox
     */
    public static void setupComboBox(@Nonnull JmriBeanComboBox inComboBox, boolean inValidateMode, boolean inEnable) {
        inComboBox.setEnabled(inEnable);
        inComboBox.setEditable(true);
        inComboBox.setValidateMode(inValidateMode);
        inComboBox.setText("");

        // find the max height of all popup items
        BasicComboPopup popup = (BasicComboPopup) inComboBox.getAccessibleContext().getAccessibleChild(0);
        JList list = popup.getList();
        ListModel lm = list.getModel();
        ListCellRenderer renderer = list.getCellRenderer();
        int maxItemHeight = 12; // pick some absolute minimum here
        for (int i = 0; i < lm.getSize(); ++i) {
            Object value = lm.getElementAt(i);
            Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
            maxItemHeight = Math.max(maxItemHeight, c.getPreferredSize().height);
        }

        int itemsPerScreen = inComboBox.getItemCount();
        // calculate the number of items that will fit on the screen
        if (!GraphicsEnvironment.isHeadless()) {
            // note: this line returns the maximum available size, accounting all
            // taskbars etc. no matter where they are aligned:
            Rectangle maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            itemsPerScreen = (int) maxWindowBounds.getHeight() / maxItemHeight;
        }

        // calculate an even division of the number of items (min 8)
        // that will fit on the screen
        int c = Math.max(8, inComboBox.getItemCount());
        while (c > itemsPerScreen) {
            c /= 2; // keeps this a even division of the number of items
        };
        inComboBox.setMaximumRowCount(c);

        inComboBox.setSelectedIndex(-1);
    } //setupComboBox

    /**
     * Grabs a subset of the possible KeyEvent constants and puts them into a
     * hash for fast lookups later. These lookups are used to enable bundles to
     * specify keyboard shortcuts on a per-locale basis.
     */
    private void initStringsToVTCodes() {
        Field[] fields = KeyEvent.class.getFields();

        for (Field field : fields) {
            String name = field.getName();

            if (name.startsWith("VK")) {
                int code = 0;
                try {
                    code = field.getInt(null);
                } catch (Exception e) {
                    //exceptions make me throw up...
                    log.error("This error message, which nobody will ever see, shuts my IDE up.");
                }

                String key = name.substring(3);

                //log.debug("VTCode[{}]:'{}'", key, code);
                stringsToVTCodes.put(key, code);
            }
        }
    } //initStringsToVTCodes

    private LayoutEditorTools tools = null;
    private jmri.jmrit.signalling.AddEntryExitPairAction entryExit = null;

    protected void setupToolsMenu(@Nonnull JMenuBar menuBar) {
        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));

        toolsMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuToolsMnemonic")));
        menuBar.add(toolsMenu);

        //scale track diagram
        JMenuItem scaleItem = new JMenuItem(Bundle.getMessage("ScaleTrackDiagram") + "...");
        toolsMenu.add(scaleItem);
        scaleItem.addActionListener((ActionEvent event) -> {
            //bring up scale track diagram dialog
            scaleTrackDiagram();
        });

        //translate selection
        JMenuItem moveItem = new JMenuItem(Bundle.getMessage("TranslateSelection") + "...");
        toolsMenu.add(moveItem);
        moveItem.addActionListener((ActionEvent event) -> {
            //bring up translate selection dialog
            moveSelection();
        });

        //undo translate selection
        JMenuItem undoMoveItem = new JMenuItem(Bundle.getMessage("UndoTranslateSelection"));
        toolsMenu.add(undoMoveItem);
        undoMoveItem.addActionListener((ActionEvent event) -> {
            //undo previous move selection
            undoMoveSelection();
        });

        //reset turnout size to program defaults
        JMenuItem undoTurnoutSize = new JMenuItem(Bundle.getMessage("ResetTurnoutSize"));
        toolsMenu.add(undoTurnoutSize);
        undoTurnoutSize.addActionListener((ActionEvent event) -> {
            //undo previous move selection
            resetTurnoutSize();
        });
        toolsMenu.addSeparator();

        //skip turnout
        skipTurnoutCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SkipInternalTurnout"));
        toolsMenu.add(skipTurnoutCheckBoxMenuItem);
        skipTurnoutCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            skipIncludedTurnout = skipTurnoutCheckBoxMenuItem.isSelected();
        });
        skipTurnoutCheckBoxMenuItem.setSelected(skipIncludedTurnout);

        //set signals at turnout
        JMenuItem turnoutItem = new JMenuItem(Bundle.getMessage("SignalsAtTurnout") + "...");
        toolsMenu.add(turnoutItem);
        turnoutItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at turnout tool dialog
            tools.setSignalsAtTurnout(signalIconEditor, signalFrame);
        });

        //set signals at block boundary
        JMenuItem boundaryItem = new JMenuItem(Bundle.getMessage("SignalsAtBoundary") + "...");
        toolsMenu.add(boundaryItem);
        boundaryItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at block boundary tool dialog
            tools.setSignalsAtBlockBoundary(signalIconEditor, signalFrame);
        });

        //set signals at crossover turnout
        JMenuItem xoverItem = new JMenuItem(Bundle.getMessage("SignalsAtXoverTurnout") + "...");
        toolsMenu.add(xoverItem);
        xoverItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at double crossover tool dialog
            tools.setSignalsAtXoverTurnout(signalIconEditor, signalFrame);
        });

        //set signals at level crossing
        JMenuItem xingItem = new JMenuItem(Bundle.getMessage("SignalsAtLevelXing") + "...");
        toolsMenu.add(xingItem);
        xingItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at level crossing tool dialog
            tools.setSignalsAtLevelXing(signalIconEditor, signalFrame);
        });

        //set signals at throat-to-throat turnouts
        JMenuItem tToTItem = new JMenuItem(Bundle.getMessage("SignalsAtTToTTurnout") + "...");
        toolsMenu.add(tToTItem);
        tToTItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at throat-to-throat turnouts tool dialog
            tools.setSignalsAtThroatToThroatTurnouts(signalIconEditor, signalFrame);
        });

        //set signals at 3-way turnout
        JMenuItem way3Item = new JMenuItem(Bundle.getMessage("SignalsAt3WayTurnout") + "...");
        toolsMenu.add(way3Item);
        way3Item.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at 3-way turnout tool dialog
            tools.setSignalsAt3WayTurnout(signalIconEditor, signalFrame);
        });

        JMenuItem slipItem = new JMenuItem(Bundle.getMessage("SignalsAtSlip") + "...");
        toolsMenu.add(slipItem);
        slipItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at throat-to-throat turnouts tool dialog
            tools.setSignalsAtSlip(signalIconEditor, signalFrame);
        });

        JMenuItem entryExitItem = new JMenuItem(Bundle.getMessage("EntryExit") + "...");
        toolsMenu.add(entryExitItem);
        entryExitItem.addActionListener((ActionEvent event) -> {
            if (entryExit == null) {
                entryExit = new jmri.jmrit.signalling.AddEntryExitPairAction("ENTRY EXIT", thisPanel);
            }
            entryExit.actionPerformed(event);
        });
    } //setupToolsMenu

    /**
     * Set up the Option menu.
     *
     * @param menuBar to add the option menu to
     * @return option menu that was added
     */
    protected JMenu setupOptionMenu(@Nonnull JMenuBar menuBar) {
        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));

        optionMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("OptionsMnemonic")));
        menuBar.add(optionMenu);

        //edit mode item
        editModeCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EditMode"));
        optionMenu.add(editModeCheckBoxMenuItem);
        editModeCheckBoxMenuItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("EditModeMnemonic")));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        editModeCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("EditModeAccelerator")), primary_modifier));
        editModeCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAllEditable(editModeCheckBoxMenuItem.isSelected());

            //show/hide the help bar
            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
            } else {
                helpBarPanel.setVisible(isEditable() && getShowHelpBar());
            }

            if (isEditable()) {
                setAllShowToolTip(tooltipsInEditMode);

                //redo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    if (!highlightBlockInComboBox(blockIDComboBox)) {
                        highlightBlockInComboBox(blockContentsComboBox);
                    }
                }
            } else {
                setAllShowToolTip(tooltipsWithoutEditMode);

                //undo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    highlightBlock(null);
                }
            }
            awaitingIconChange = false;
        });
        editModeCheckBoxMenuItem.setSelected(isEditable());

        //
        //create our (top) toolbar menu
        //
        JMenu toolBarMenu = new JMenu(Bundle.getMessage("ToolBar")); //used for ToolBar SubMenu
        optionMenu.add(toolBarMenu);

        //
        //create toolbar side menu items: (top, left, bottom, right)
        //
        toolBarSideTopButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideTop"));
        toolBarSideTopButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(ToolBarSide.eTOP);
        });
        toolBarSideTopButton.setSelected(toolBarSide.equals(ToolBarSide.eTOP));

        toolBarSideLeftButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideLeft"));
        toolBarSideLeftButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(ToolBarSide.eLEFT);
        });
        toolBarSideLeftButton.setSelected(toolBarSide.equals(ToolBarSide.eLEFT));

        toolBarSideBottomButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideBottom"));
        toolBarSideBottomButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(ToolBarSide.eBOTTOM);
        });
        toolBarSideBottomButton.setSelected(toolBarSide.equals(ToolBarSide.eBOTTOM));

        toolBarSideRightButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideRight"));
        toolBarSideRightButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(ToolBarSide.eRIGHT);
        });
        toolBarSideRightButton.setSelected(toolBarSide.equals(ToolBarSide.eRIGHT));

        toolBarSideFloatButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideFloat"));
        toolBarSideFloatButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(ToolBarSide.eFLOAT);
        });
        toolBarSideFloatButton.setSelected(toolBarSide.equals(ToolBarSide.eFLOAT));

        JMenu toolBarSideMenu = new JMenu(Bundle.getMessage("ToolBarSide")); //used for ScrollBarsSubMenu
        toolBarSideMenu.add(toolBarSideTopButton);
        toolBarSideMenu.add(toolBarSideLeftButton);
        toolBarSideMenu.add(toolBarSideBottomButton);
        toolBarSideMenu.add(toolBarSideRightButton);
        toolBarSideMenu.add(toolBarSideFloatButton);

        ButtonGroup toolBarSideGroup = new ButtonGroup();
        toolBarSideGroup.add(toolBarSideTopButton);
        toolBarSideGroup.add(toolBarSideLeftButton);
        toolBarSideGroup.add(toolBarSideBottomButton);
        toolBarSideGroup.add(toolBarSideRightButton);
        toolBarSideGroup.add(toolBarSideFloatButton);
        toolBarMenu.add(toolBarSideMenu);

        //
        //toolbar wide menu
        //
        toolBarMenu.add(wideToolBarCheckBoxMenuItem);
        wideToolBarCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            boolean newToolBarIsWide = wideToolBarCheckBoxMenuItem.isSelected();
            setToolBarWide(newToolBarIsWide);
        });
        wideToolBarCheckBoxMenuItem.setSelected(toolBarIsWide);

        //
        //create setup font size menu items
        //
        ButtonGroup toolBarFontSizeGroup = new ButtonGroup();

        String[] fontSizes = {"9", "10", "11", "12", "13", "14", "15", "16", "17", "18"};

        for (String fontSize : fontSizes) {
            float fontSizeFloat = Float.parseFloat(fontSize);
            JRadioButtonMenuItem fontSizeButton = new JRadioButtonMenuItem(fontSize);
            fontSizeButton.addActionListener((ActionEvent event) -> {
                setupToolBarFontSizes(fontSizeFloat);

                //save it in the user preferences for the window
                String windowFrameRef = getWindowFrameRef();
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                    prefsMgr.setProperty(windowFrameRef, "toolBarFontSize", fontSizeFloat);
                });

                ///doing this for now (since window prefs seem to be whacked)
                GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
                manager.setFontSize((int) fontSizeFloat);
            });
            toolBarFontSizeMenu.add(fontSizeButton);
            toolBarFontSizeGroup.add(fontSizeButton);
            fontSizeButton.setSelected(fontSizeFloat == toolBarFontSize);
        }
        toolBarFontSizeMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                String fontSizeString = String.valueOf((int) toolBarFontSize);

                for (Component c : toolBarFontSizeMenu.getMenuComponents()) {
                    if (c instanceof JRadioButtonMenuItem) {
                        JRadioButtonMenuItem crb = (JRadioButtonMenuItem) c;
                        String menuItemFontSizeString = crb.getText();
                        crb.setSelected(menuItemFontSizeString.equals(fontSizeString));
                    }
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        //toolBarMenu.add(toolBarFontSizeMenu); //<<== disabled as per
        //<https://github.com/JMRI/JMRI/pull/3145#issuecomment-283940658>
        //
        //setup drop down list display order menu
        //
        ButtonGroup dropDownListsDisplayOrderGroup = new ButtonGroup();

        String[] ddldoChoices = {"DropDownListsDisplayOrderDisplayName", "DropDownListsDisplayOrderUserName",
            "DropDownListsDisplayOrderSystemName", "DropDownListsDisplayOrderUserNameSystemName",
            "DropDownListsDisplayOrderSystemNameUserName"};

        for (String ddldoChoice : ddldoChoices) {
            JRadioButtonMenuItem ddldoChoiceMenuItem = new JRadioButtonMenuItem(Bundle.getMessage(ddldoChoice));
            ddldoChoiceMenuItem.addActionListener((ActionEvent event) -> {
                JRadioButtonMenuItem ddldoMenuItem = (JRadioButtonMenuItem) event.getSource();
                JPopupMenu parentMenu = (JPopupMenu) ddldoMenuItem.getParent();
                int ddldoInt = parentMenu.getComponentZOrder(ddldoMenuItem) + 1;
                JmriBeanComboBox.DisplayOptions ddldo = JmriBeanComboBox.DisplayOptions.valueOf(ddldoInt);

                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                    //change this comboboxes ddldo
                    String windowFrameRef = getWindowFrameRef();

                    //this is the preference name
                    String ddldoPrefName = "DropDownListsDisplayOrder";

                    //make a focused component specific preference name
                    Component focusedComponent = getFocusOwner();

                    if (focusedComponent instanceof JTextField) {
                        focusedComponent = SwingUtilities.getUnwrappedParent(focusedComponent);
                    }

                    if (focusedComponent instanceof JmriBeanComboBox) {
                        JmriBeanComboBox focusedJBCB = (JmriBeanComboBox) focusedComponent;

                        //now try to get a preference specific to this combobox
                        String ttt = focusedJBCB.getToolTipText();

                        if (null != ttt) {
                            //change the name of the preference based on the tool tip text
                            ddldoPrefName = String.format("%s.%s", ddldoPrefName, ttt);
                        }

                        //now set the combo box display order
                        focusedJBCB.setDisplayOrder(ddldo);
                    }

                    //update the users preference
                    String[] ddldoPrefs = {"DISPLAYNAME", "USERNAME", "SYSTEMNAME", "USERNAMESYSTEMNAME", "SYSTEMNAMEUSERNAME"};
                    prefsMgr.setProperty(windowFrameRef, ddldoPrefName, ddldoPrefs[ddldoInt]);
                });
            }); //addActionListener

            dropDownListsDisplayOrderMenu.add(ddldoChoiceMenuItem);
            dropDownListsDisplayOrderGroup.add(ddldoChoiceMenuItem);

            //if it matches the 1st choice then select it (for now; it will be updated later)
            ddldoChoiceMenuItem.setSelected(ddldoChoice.equals(ddldoChoices[0]));
        }
        dropDownListsDisplayOrderMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                ///TODO: update menu item based on focused combobox (if any)
                log.debug("update menu item based on focused combobox");
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        toolBarMenu.add(dropDownListsDisplayOrderMenu);

        //
        //positionable item
        //
        positionableCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowRepositioning"));
        optionMenu.add(positionableCheckBoxMenuItem);
        positionableCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAllPositionable(positionableCheckBoxMenuItem.isSelected());
        });
        positionableCheckBoxMenuItem.setSelected(allPositionable());

        //
        //controlable item
        //
        controlCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowLayoutControl"));
        optionMenu.add(controlCheckBoxMenuItem);
        controlCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAllControlling(controlCheckBoxMenuItem.isSelected());
            redrawPanel();
        });
        controlCheckBoxMenuItem.setSelected(allControlling());

        //
        //add "use direct turnout control" menu item
        //
        useDirectTurnoutControlCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("UseDirectTurnoutControl")); //IN18N
        optionMenu.add(useDirectTurnoutControlCheckBoxMenuItem);
        useDirectTurnoutControlCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setDirectTurnoutControl(useDirectTurnoutControlCheckBoxMenuItem.isSelected());
        });
        useDirectTurnoutControlCheckBoxMenuItem.setSelected(useDirectTurnoutControl);

        //
        //animation item
        //
        animationCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowTurnoutAnimation"));
        optionMenu.add(animationCheckBoxMenuItem);
        animationCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            boolean mode = animationCheckBoxMenuItem.isSelected();
            setTurnoutAnimation(mode);
        });
        animationCheckBoxMenuItem.setSelected(true);

        //
        //show help item
        //
        showHelpCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowEditHelp"));
        optionMenu.add(showHelpCheckBoxMenuItem);
        showHelpCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            boolean newShowHelpBar = showHelpCheckBoxMenuItem.isSelected();
            setShowHelpBar(newShowHelpBar);
        });
        showHelpCheckBoxMenuItem.setSelected(getShowHelpBar());

        //
        //show grid item
        //
        showGridCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowEditGrid"));
        showGridCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("ShowEditGridAccelerator")), primary_modifier));
        optionMenu.add(showGridCheckBoxMenuItem);
        showGridCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            drawGrid = showGridCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        showGridCheckBoxMenuItem.setSelected(getDrawGrid());

        //
        //snap to grid on add item
        //
        snapToGridOnAddCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnAdd"));
        snapToGridOnAddCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnAddAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        optionMenu.add(snapToGridOnAddCheckBoxMenuItem);
        snapToGridOnAddCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            snapToGridOnAdd = snapToGridOnAddCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        snapToGridOnAddCheckBoxMenuItem.setSelected(snapToGridOnAdd);

        //
        //snap to grid on move item
        //
        snapToGridOnMoveCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnMove"));
        snapToGridOnMoveCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnMoveAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        optionMenu.add(snapToGridOnMoveCheckBoxMenuItem);
        snapToGridOnMoveCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            snapToGridOnMove = snapToGridOnMoveCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        snapToGridOnMoveCheckBoxMenuItem.setSelected(snapToGridOnMove);

        //
        //specify grid square size
        //
        JMenuItem gridSizeItem = new JMenuItem(Bundle.getMessage("SetGridSizes") + "...");
        optionMenu.add(gridSizeItem);
        gridSizeItem.addActionListener((ActionEvent event) -> {
            enterGridSizes();
        });

        //
        //Show/Hide Scroll Bars
        //
        scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable")); //used for ScrollBarsSubMenu
        optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.setSelected(_scrollState == Editor.SCROLL_BOTH);
        scrollBoth.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_BOTH;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.setSelected(_scrollState == Editor.SCROLL_NONE);
        scrollNone.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_NONE;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.setSelected(_scrollState == Editor.SCROLL_HORIZONTAL);
        scrollHorizontal.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_HORIZONTAL;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.setSelected(_scrollState == Editor.SCROLL_VERTICAL);
        scrollVertical.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_VERTICAL;
            setScroll(_scrollState);
            redrawPanel();
        });

        //
        //Tooltip options
        //
        tooltipMenu = new JMenu(Bundle.getMessage("TooltipSubMenu"));
        optionMenu.add(tooltipMenu);
        ButtonGroup tooltipGroup = new ButtonGroup();
        tooltipNone = new JRadioButtonMenuItem(Bundle.getMessage("TooltipNone"));
        tooltipGroup.add(tooltipNone);
        tooltipMenu.add(tooltipNone);
        tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNone.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = false;
            setAllShowToolTip(false);
        });
        tooltipAlways = new JRadioButtonMenuItem(Bundle.getMessage("TooltipAlways"));
        tooltipGroup.add(tooltipAlways);
        tooltipMenu.add(tooltipAlways);
        tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipAlways.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = true;
            setAllShowToolTip(true);
        });
        tooltipInEdit = new JRadioButtonMenuItem(Bundle.getMessage("TooltipEdit"));
        tooltipGroup.add(tooltipInEdit);
        tooltipMenu.add(tooltipInEdit);
        tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipInEdit.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = false;
            setAllShowToolTip(isEditable());
        });
        tooltipNotInEdit = new JRadioButtonMenuItem(Bundle.getMessage("TooltipNotEdit"));
        tooltipGroup.add(tooltipNotInEdit);
        tooltipMenu.add(tooltipNotInEdit);
        tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipNotInEdit.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = true;
            setAllShowToolTip(!isEditable());
        });

        //
        //antialiasing
        //
        antialiasingOnCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AntialiasingOn"));
        optionMenu.add(antialiasingOnCheckBoxMenuItem);
        antialiasingOnCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            antialiasingOn = antialiasingOnCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        antialiasingOnCheckBoxMenuItem.setSelected(antialiasingOn);

        //
        //Highlight Selected Block
        //
        highlightSelectedBlockCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("HighlightSelectedBlock"));
        optionMenu.add(highlightSelectedBlockCheckBoxMenuItem);
        highlightSelectedBlockCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setHighlightSelectedBlock(highlightSelectedBlockCheckBoxMenuItem.isSelected());
        });
        highlightSelectedBlockCheckBoxMenuItem.setSelected(highlightSelectedBlockFlag);

        //
        //edit title item
        //
        optionMenu.addSeparator();
        JMenuItem titleItem = new JMenuItem(Bundle.getMessage("EditTitle") + "...");
        optionMenu.add(titleItem);
        titleItem.addActionListener((ActionEvent event) -> {
            //prompt for name
            String newName = (String) JOptionPane.showInputDialog(getTargetFrame(),
                    Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterTitle")),
                    Bundle.getMessage("EditTitleMessageTitle"),
                    JOptionPane.PLAIN_MESSAGE, null, null, layoutName);

            if (newName != null) {
                if (!newName.equals(layoutName)) {
                    if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(newName)) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("CanNotRename"), Bundle.getMessage("PanelExist"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        setTitle(newName);
                        layoutName = newName;
                        InstanceManager.getDefault(PanelMenu.class).renameEditorPanel(thisPanel);
                        setDirty();

                        if (toolBarSide.equals(ToolBarSide.eFLOAT) && isEditable()) {
                            // Rebuild the toolbox after a name change.
                            deleteFloatingEditToolBox();
                            createFloatingEditToolBox();
                        }
                    }
                }
            }
        });

        //
        //background image menu item
        //
        JMenuItem backgroundItem = new JMenuItem(Bundle.getMessage("AddBackground") + "...");
        optionMenu.add(backgroundItem);
        backgroundItem.addActionListener((ActionEvent event) -> {
            addBackground();
            //note: panel resized in addBackground
            setDirty();
            redrawPanel();
        });

        //
        //background color menu item
        //
        JMenu backgroundColorMenu = new JMenu(Bundle.getMessage("SetBackgroundColor"));
        backgroundColorButtonGroup = new ButtonGroup();
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Black"), Color.black);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("White"), Color.white);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Red"), Color.red);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Green"), Color.green);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addBackgroundColorMenuEntry(backgroundColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        optionMenu.add(backgroundColorMenu);

        //
        //add fast clock menu item
        //
        JMenuItem clockItem = new JMenuItem(Bundle.getMessage("AddItem", Bundle.getMessage("FastClock")));
        optionMenu.add(clockItem);
        clockItem.addActionListener((ActionEvent event) -> {
            AnalogClock2Display c = addClock();
            unionToPanelBounds(c.getBounds());
            setDirty();
            redrawPanel();
        });

        //
        //add turntable menu item
        //
        JMenuItem turntableItem = new JMenuItem(Bundle.getMessage("AddTurntable"));
        optionMenu.add(turntableItem);
        turntableItem.addActionListener((ActionEvent event) -> {
            addTurntable(windowCenter());
            //note: panel resized in addTurntable
            setDirty();
            redrawPanel();
        });

        //
        //add reporter menu item
        //
        JMenuItem reporterItem = new JMenuItem(Bundle.getMessage("AddReporter") + "...");
        optionMenu.add(reporterItem);
        reporterItem.addActionListener((ActionEvent event) -> {
            Point2D pt = windowCenter();
            enterReporter((int) pt.getX(), (int) pt.getY());
            //note: panel resized in enterReporter
            setDirty();
            redrawPanel();
        });

        //
        //set location and size menu item
        //
        JMenuItem locationItem = new JMenuItem(Bundle.getMessage("SetLocation"));
        optionMenu.add(locationItem);
        locationItem.addActionListener((ActionEvent event) -> {
            setCurrentPositionAndSize();
            log.debug("Bounds:{}, {}, {}, {}, {}, {}", upperLeftX, upperLeftY, windowWidth, windowHeight, panelWidth, panelHeight);
        });

        //
        //set track width menu item
        //
        JMenuItem widthItem = new JMenuItem(Bundle.getMessage("SetTrackWidth") + "...");
        optionMenu.add(widthItem);
        widthItem.addActionListener((ActionEvent event) -> {
            //bring up enter track width dialog
            enterTrackWidth();
        });

        //
        //track colors item menu item
        //
        JMenu trkColourMenu = new JMenu(Bundle.getMessage("TrackColorSubMenu"));
        optionMenu.add(trkColourMenu);

        JMenu trackColorMenu = new JMenu(Bundle.getMessage("DefaultTrackColor"));
        trackColorButtonGroup = new ButtonGroup();
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Black"), Color.black);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("White"), Color.white);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Red"), Color.red);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Green"), Color.green);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        trkColourMenu.add(trackColorMenu);

        JMenu trackOccupiedColorMenu = new JMenu(Bundle.getMessage("DefaultOccupiedTrackColor"));
        trackOccupiedColorButtonGroup = new ButtonGroup();
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Black"), Color.black);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("White"), Color.white);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Red"), Color.red);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Green"), Color.green);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        trkColourMenu.add(trackOccupiedColorMenu);

        JMenu trackAlternativeColorMenu = new JMenu(Bundle.getMessage("DefaultAlternativeTrackColor"));
        trackAlternativeColorButtonGroup = new ButtonGroup();
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Black"), Color.black);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("White"), Color.white);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Red"), Color.red);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Green"), Color.green);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        trkColourMenu.add(trackAlternativeColorMenu);

        //
        //add text color menu item
        //
        JMenu textColorMenu = new JMenu(Bundle.getMessage("DefaultTextColor"));
        textColorButtonGroup = new ButtonGroup();
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Black"), Color.black);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("White"), Color.white);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Red"), Color.red);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Green"), Color.green);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addTextColorMenuEntry(textColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        optionMenu.add(textColorMenu);

        //
        //add turnout options submenu
        //
        JMenu turnoutOptionsMenu = new JMenu(Bundle.getMessage("TurnoutOptions"));
        optionMenu.add(turnoutOptionsMenu);

        //circle on Turnouts
        turnoutCirclesOnCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("TurnoutCirclesOn"));
        turnoutOptionsMenu.add(turnoutCirclesOnCheckBoxMenuItem);
        turnoutCirclesOnCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            turnoutCirclesWithoutEditMode = turnoutCirclesOnCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        turnoutCirclesOnCheckBoxMenuItem.setSelected(turnoutCirclesWithoutEditMode);

        //select turnout circle color
        JMenu turnoutCircleColorMenu = new JMenu(Bundle.getMessage("TurnoutCircleColor"));
        turnoutCircleColorButtonGroup = new ButtonGroup();
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("UseDefaultTrackColor"), null);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Black"), Color.black);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("DarkGray"), Color.darkGray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Gray"), Color.gray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("White"), Color.white);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Red"), Color.red);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Pink"), Color.pink);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Orange"), Color.orange);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Yellow"), Color.yellow);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Green"), Color.green);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Blue"), Color.blue);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Magenta"), Color.magenta);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Cyan"), Color.cyan);
        turnoutOptionsMenu.add(turnoutCircleColorMenu);

        //select turnout circle size
        JMenu turnoutCircleSizeMenu = new JMenu(Bundle.getMessage("TurnoutCircleSize"));
        turnoutCircleSizeButtonGroup = new ButtonGroup();
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "1", 1);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "2", 2);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "3", 3);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "4", 4);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "5", 5);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "6", 6);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "7", 7);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "8", 8);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "9", 9);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu, "10", 10);
        turnoutOptionsMenu.add(turnoutCircleSizeMenu);

        //
        //add "enable drawing of unselected leg " menu item (helps when diverging angle is small)
        //
        turnoutDrawUnselectedLegCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("TurnoutDrawUnselectedLeg"));
        turnoutOptionsMenu.add(turnoutDrawUnselectedLegCheckBoxMenuItem);
        turnoutDrawUnselectedLegCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            turnoutDrawUnselectedLeg = turnoutDrawUnselectedLegCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        turnoutDrawUnselectedLegCheckBoxMenuItem.setSelected(turnoutDrawUnselectedLeg);

        //add show grid menu item
        autoAssignBlocksCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AutoAssignBlock"));
        optionMenu.add(autoAssignBlocksCheckBoxMenuItem);
        autoAssignBlocksCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            autoAssignBlocks = autoAssignBlocksCheckBoxMenuItem.isSelected();
        });
        autoAssignBlocksCheckBoxMenuItem.setSelected(autoAssignBlocks);

        //
        //add hideTrackSegmentConstructionLines menu item
        //
        hideTrackSegmentConstructionLinesCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("HideTrackConLines"));
        optionMenu.add(hideTrackSegmentConstructionLinesCheckBoxMenuItem);
        hideTrackSegmentConstructionLinesCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            int show = TrackSegment.SHOWCON;

            if (hideTrackSegmentConstructionLinesCheckBoxMenuItem.isSelected()) {
                show = TrackSegment.HIDECONALL;
            }

            for (TrackSegment ts : trackList) {
                ts.hideConstructionLines(show);
            }
            redrawPanel();
        });
        hideTrackSegmentConstructionLinesCheckBoxMenuItem.setSelected(autoAssignBlocks);

        return optionMenu;
    } //setupOptionMenu

    //
    //update drop down menu display order menu
    //
    private JmriBeanComboBox.DisplayOptions gDDMDO = JmriBeanComboBox.DisplayOptions.DISPLAYNAME;

    private void updateDropDownMenuDisplayOrderMenu() {
        Component focusedComponent = getFocusOwner();

        if (focusedComponent instanceof JmriBeanComboBox) {
            JmriBeanComboBox focusedJBCB = (JmriBeanComboBox) focusedComponent;
            gDDMDO = focusedJBCB.getDisplayOrder();
        }

        int idx = 0, ddmdoInt = gDDMDO.getValue();

        for (Component c : dropDownListsDisplayOrderMenu.getMenuComponents()) {
            if (c instanceof JRadioButtonMenuItem) {
                JRadioButtonMenuItem crb = (JRadioButtonMenuItem) c;
                crb.setSelected(ddmdoInt == idx);
                idx++;
            }
        }
    } //updateDropDownMenuDisplayOrderMenu

    //
    //update drop down menu display order for all combo boxes (from prefs)
    //
    private void updateAllComboBoxesDropDownListDisplayOrderFromPrefs() {
        //1st call the recursive funtion starting from the edit toolbar container
        updateComboBoxDropDownListDisplayOrderFromPrefs(editToolBarContainer);
        updateComboBoxDropDownListDisplayOrderFromPrefs(floatingEditContent);

        //and now that that's done update the drop down menu display order menu
        updateDropDownMenuDisplayOrderMenu();
    } //updateAllComboBoxesDropDownListDisplayOrderFromPrefs

    //
    //update drop down menu display order for all combo boxes (from prefs)
    //note: recursive function that walks down the component / container tree
    //
    private void updateComboBoxDropDownListDisplayOrderFromPrefs(@Nullable Component inComponent) {
        if (null != inComponent) {
            if (inComponent instanceof JmriBeanComboBox) {
                //try to get the preference
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                    String windowFrameRef = getWindowFrameRef();

                    //this is the preference name
                    String ddldoPrefName = "DropDownListsDisplayOrder";

                    //this is the default value if we can't find it in any preferences
                    String ddldoPref = "DISPLAYNAME";

                    Object ddldoProp = prefsMgr.getProperty(windowFrameRef, ddldoPrefName);

                    if (null != ddldoProp) {
                        //this will be the value if this combo box doesn't have a saved preference.
                        ddldoPref = ddldoProp.toString();
                    } else {
                        //save a default preference
                        prefsMgr.setProperty(windowFrameRef, ddldoPrefName, ddldoPref);
                    }

                    //now try to get a preference specific to this combobox
                    JmriBeanComboBox jbcb = (JmriBeanComboBox) inComponent;
                    if (inComponent instanceof JTextField) {
                        jbcb = (JmriBeanComboBox) SwingUtilities.getUnwrappedParent(jbcb);
                    }

                    if (jbcb instanceof JmriBeanComboBox) {
                        String ttt = jbcb.getToolTipText();
                        if (null != ttt) {
                            //change the name of the preference based on the tool tip text
                            ddldoPrefName = String.format("%s.%s", ddldoPrefName, ttt);
                            //try to get the preference
                            ddldoProp = prefsMgr.getProperty(getWindowFrameRef(), ddldoPrefName);
                            if (null != ddldoProp) { //if we found it...
                                ddldoPref = ddldoProp.toString(); //get it's (string value
                            } else { //otherwise...
                                //save it in the users preferences
                                prefsMgr.setProperty(windowFrameRef, ddldoPrefName, ddldoPref);
                            }
                        }
                    }

                    //now set the combo box display order
                    if (ddldoPref.equals("DISPLAYNAME")) {
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                    } else if (ddldoPref.equals("USERNAME")) {
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.USERNAME);
                    } else if (ddldoPref.equals("SYSTEMNAME")) {
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAME);
                    } else if (ddldoPref.equals("USERNAMESYSTEMNAME")) {
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
                    } else if (ddldoPref.equals("SYSTEMNAMEUSERNAME")) {
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
                    } else {
                        //must be a bogus value... lets re-set everything to DISPLAYNAME
                        ddldoPref = "DISPLAYNAME";
                        prefsMgr.setProperty(windowFrameRef, ddldoPrefName, ddldoPref);
                        jbcb.setDisplayOrder(JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                    }
                });
            } else if (inComponent instanceof Container) {
                for (Component c : ((Container) inComponent).getComponents()) {
                    updateComboBoxDropDownListDisplayOrderFromPrefs(c);
                }
            } else {
                //nothing to do here... move along...
            }
        } //if (null != inComponent) {
    } //updateComboBoxDropDownListDisplayOrderFromPrefs

    //
    //
    //
    private void setToolBarSide(ToolBarSide newToolBarSide) {
        // null if edit toolbar is not setup yet...
        if ((editModeCheckBoxMenuItem != null) && !newToolBarSide.equals(toolBarSide)) {
            toolBarSide = newToolBarSide;
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setProperty(getWindowFrameRef(), "toolBarSide", toolBarSide.getName());
            });

            setupToolBar(); //re-layout all the toolbar items

            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                createFloatingEditToolBox();
                if (null != editToolBarContainer) {
                    editToolBarContainer.setVisible(false);
                }
            } else {
                if (null != floatingEditToolBox) {
                    deleteFloatingEditToolBox();
                }
                floatingEditContent = null; // The switch to toolbar will move the toolbox content to the new toolbar
                editToolBarContainer.setVisible(isEditable());
            }
            toolBarSideTopButton.setSelected(toolBarSide.equals(ToolBarSide.eTOP));
            toolBarSideLeftButton.setSelected(toolBarSide.equals(ToolBarSide.eLEFT));
            toolBarSideBottomButton.setSelected(toolBarSide.equals(ToolBarSide.eBOTTOM));
            toolBarSideRightButton.setSelected(toolBarSide.equals(ToolBarSide.eRIGHT));
            toolBarSideFloatButton.setSelected(toolBarSide.equals(ToolBarSide.eFLOAT));

            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
            } else if (getShowHelpBar()) {
                //not sure why... but this is the only way I could
                //get everything to layout correctly
                //when the helpbar is visible...
                boolean editMode = isEditable();
                setAllEditable(!editMode);
                setAllEditable(editMode);
            } else {
                helpBarPanel.setVisible(isEditable() && getShowHelpBar());
            }
        }
    } //setToolBarSide

    //
    //
    //
    private void setToolBarWide(boolean newToolBarIsWide) {
        //null if edit toolbar not setup yet...
        if ((editModeCheckBoxMenuItem != null) && (toolBarIsWide != newToolBarIsWide)) {
            toolBarIsWide = newToolBarIsWide;

            wideToolBarCheckBoxMenuItem.setSelected(toolBarIsWide);

            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                //Note: since prefs default to false and we want wide to be the default
                //we invert it and save it as thin
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".toolBarThin", !toolBarIsWide);
            });

            setupToolBar(); //re-layout all the toolbar items

            if (getShowHelpBar()) {
                //not sure why, but this is the only way I could
                //get everything to layout correctly
                //when the helpbar is visible...
                boolean editMode = isEditable();
                setAllEditable(!editMode);
                setAllEditable(editMode);
            } else {
                helpBarPanel.setVisible(isEditable() && getShowHelpBar());
            }
        }
    } //setToolBarWide

    //
    //
    //
    private void setupZoomMenu(@Nonnull JMenuBar menuBar) {
        zoomMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuZoomMnemonic")));
        menuBar.add(zoomMenu);
        ButtonGroup zoomButtonGroup = new ButtonGroup();

        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        //add zoom choices to menu
        JMenuItem zoomInItem = new JMenuItem(Bundle.getMessage("ZoomIn"));
        zoomInItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("zoomInMnemonic")));
        String zoomInAccelerator = Bundle.getMessage("zoomInAccelerator");
        //log.debug("zoomInAccelerator: " + zoomInAccelerator);
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomInAccelerator), primary_modifier));
        zoomMenu.add(zoomInItem);
        zoomInItem.addActionListener((ActionEvent event) -> {
            zoomIn();
        });

        JMenuItem zoomOutItem = new JMenuItem(Bundle.getMessage("ZoomOut"));
        zoomOutItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("zoomOutMnemonic")));
        String zoomOutAccelerator = Bundle.getMessage("zoomOutAccelerator");
        //log.debug("zoomOutAccelerator: " + zoomOutAccelerator);
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomOutAccelerator), primary_modifier));
        zoomMenu.add(zoomOutItem);
        zoomOutItem.addActionListener((ActionEvent event) -> {
            zoomOut();
        });

        JMenuItem zoomFitItem = new JMenuItem(Bundle.getMessage("ZoomToFit"));
        zoomMenu.add(zoomFitItem);
        zoomFitItem.addActionListener((ActionEvent event) -> {
            zoomToFit();
        });
        zoomMenu.addSeparator();

        //add zoom choices to menu
        zoomMenu.add(zoom025Item);
        zoom025Item.addActionListener((ActionEvent event) -> {
            setZoom(0.25);
        });
        zoomButtonGroup.add(zoom025Item);

        zoomMenu.add(zoom05Item);
        zoom05Item.addActionListener((ActionEvent event) -> {
            setZoom(0.5);
        });
        zoomButtonGroup.add(zoom05Item);

        zoomMenu.add(zoom075Item);
        zoom075Item.addActionListener((ActionEvent event) -> {
            setZoom(0.75);
        });
        zoomButtonGroup.add(zoom075Item);

        String zoomNoneAccelerator = Bundle.getMessage("zoomNoneAccelerator");
        //log.debug("zoomNoneAccelerator: " + zoomNoneAccelerator);
        noZoomItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomNoneAccelerator), primary_modifier));

        zoomMenu.add(noZoomItem);
        noZoomItem.addActionListener((ActionEvent event) -> {
            setZoom(1.0);
        });
        zoomButtonGroup.add(noZoomItem);

        zoomMenu.add(zoom15Item);
        zoom15Item.addActionListener((ActionEvent event) -> {
            setZoom(1.5);
        });
        zoomButtonGroup.add(zoom15Item);

        zoomMenu.add(zoom20Item);
        zoom20Item.addActionListener((ActionEvent event) -> {
            setZoom(2.0);
        });
        zoomButtonGroup.add(zoom20Item);

        zoomMenu.add(zoom30Item);
        zoom30Item.addActionListener((ActionEvent event) -> {
            setZoom(3.0);
        });
        zoomButtonGroup.add(zoom30Item);

        zoomMenu.add(zoom40Item);
        zoom40Item.addActionListener((ActionEvent event) -> {
            setZoom(4.0);
        });
        zoomButtonGroup.add(zoom40Item);

        zoomMenu.add(zoom50Item);
        zoom50Item.addActionListener((ActionEvent event) -> {
            setZoom(5.0);
        });
        zoomButtonGroup.add(zoom50Item);

        zoomMenu.add(zoom60Item);
        zoom60Item.addActionListener((ActionEvent event) -> {
            setZoom(6.0);
        });
        zoomButtonGroup.add(zoom60Item);

        //note: because this LayoutEditor object was just instantiated its
        //zoom attribute is 1.0; if it's being instantiated from an XML file
        //that has a zoom attribute for this object then setZoom will be
        //called after this method returns and we'll select the appropriate
        //menu item then.
        noZoomItem.setSelected(true);

        //Note: We have to invoke this stuff later because _targetPanel is not setup yet
        SwingUtilities.invokeLater(() -> {
            //get the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                Object zoomProp = prefsMgr.getProperty(getWindowFrameRef(), "zoom");
                log.debug("{} zoom is {}", getWindowFrameRef(), zoomProp);

                if (zoomProp != null) {
                    //setZoom(maxZoom - (Double) zoomProp);
                    setZoom((Double) zoomProp);
                }
            });

            // get the scroll bars from the scroll pane
            JScrollPane scrollPane = getPanelScrollPane();
            if (scrollPane != null) {
                JScrollBar hsb = scrollPane.getHorizontalScrollBar();
                JScrollBar vsb = scrollPane.getVerticalScrollBar();

                // Increase scroll bar unit increments!!!
                vsb.setUnitIncrement(16);
                hsb.setUnitIncrement(16);

                // add scroll bar adjustment listeners
                vsb.addAdjustmentListener((AdjustmentEvent e) -> {
                    scrollBarAdjusted(e);
                });
                hsb.addAdjustmentListener((AdjustmentEvent e) -> {
                    scrollBarAdjusted(e);
                });

                // remove all mouse wheel listeners
                mouseWheelListeners = scrollPane.getMouseWheelListeners();
                for (MouseWheelListener mwl : mouseWheelListeners) {
                    scrollPane.removeMouseWheelListener(mwl);
                }

                // add my mouse wheel listener
                // (so mouseWheelMoved (below) will be called)
                scrollPane.addMouseWheelListener(this);
            }
        });
    } //setupZoomMenu

    private MouseWheelListener[] mouseWheelListeners;

    // scroll bar listener to update x & y coordinates in toolbar on scroll
    public void scrollBarAdjusted(@Nonnull AdjustmentEvent e) {
        //log.warn("scrollBarAdjusted");
        if (isEditable()) {
            // get the location of the mouse
            PointerInfo mpi = MouseInfo.getPointerInfo();
            Point mouseLoc = mpi.getLocation();
            // convert to target panel coordinates
            SwingUtilities.convertPointFromScreen(mouseLoc, getTargetPanel());
            // correct for scaling...
            double theZoom = getZoom();
            xLoc = (int) (mouseLoc.getX() / theZoom);
            yLoc = (int) (mouseLoc.getY() / theZoom);
            dLoc.setLocation(xLoc, yLoc);

            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
    }

    @Override
    public void mouseWheelMoved(@Nonnull MouseWheelEvent e) {
        //log.warn("mouseWheelMoved");
        if (e.isAltDown()) {
            // get the mouse position from the event and convert to target panel coordinates
            Component c = (Component) e.getSource();
            Point ep = e.getPoint();
            JComponent t = getTargetPanel();
            Point2D mousePos2D = SwingUtilities.convertPoint(c, ep, t);

            // get the old view port position
            JScrollPane scrollPane = getPanelScrollPane();
            JViewport viewPort = scrollPane.getViewport();
            Point2D oldViewPos2D = viewPort.getViewPosition();

            // convert from oldZoom (scaled) coordinates to image coordinates
            double oldZoom = getZoom();
            Point2D imP2D = MathUtil.divide(mousePos2D, oldZoom);
            Point2D ivP2D = MathUtil.divide(oldViewPos2D, oldZoom);
            // compute the delta (in image coordinates)
            Point2D iDeltaP2D = MathUtil.subtract(imP2D, ivP2D);

            // compute how much to change zoom
            double amount = Math.pow(1.1, e.getScrollAmount());
            if (e.getWheelRotation() < 0) {
                //reciprocal for zoom out
                amount = 1 / amount;
            }
            // set the new zoom
            double newZoom = setZoom(oldZoom * amount);
            // recalulate the amount (in case setZoom didn't zoom as much as we wanted)
            amount = newZoom / oldZoom;

            // convert the old delta to the new
            Point2D iNewDeltaP2D = MathUtil.divide(iDeltaP2D, amount);
            // calculate the new view position (in image coordinates)
            Point2D iNewViewPos2D = MathUtil.subtract(imP2D, iNewDeltaP2D);
            // convert from image coordinates to newZoom (scaled) coordinates
            Point2D newViewPos2D = MathUtil.multiply(iNewViewPos2D, newZoom);
            // set new view position
            viewPort.setViewPosition(MathUtil.point2DToPoint(newViewPos2D));
        } else {
            JScrollPane scrollPane = getPanelScrollPane();
            if (scrollPane.getVerticalScrollBar().isVisible()) {
                // Redispatch the event to the original MouseWheelListeners
                for (MouseWheelListener mwl : mouseWheelListeners) {
                    mwl.mouseWheelMoved(e);
                }
            } else {
                // proprogate event to ancestor
                Component ancestor = SwingUtilities.getAncestorOfClass(JScrollPane.class, scrollPane);

                MouseWheelEvent mwe = new MouseWheelEvent(
                        ancestor,
                        e.getID(),
                        e.getWhen(),
                        e.getModifiers(),
                        e.getX(),
                        e.getY(),
                        e.getXOnScreen(),
                        e.getYOnScreen(),
                        e.getClickCount(),
                        e.isPopupTrigger(),
                        e.getScrollType(),
                        e.getScrollAmount(),
                        e.getWheelRotation());

                ancestor.dispatchEvent(mwe);
            }
        }
    } // mouseWheelMoved

    //
    //
    //
    private void selectZoomMenuItem(double zoomFactor) {
        //this will put zoomFactor on 100% increments
        //(so it will more likely match one of these values)
        int newZoomFactor = ((int) Math.round(zoomFactor)) * 100;
        noZoomItem.setSelected(newZoomFactor == 100);
        zoom20Item.setSelected(newZoomFactor == 200);
        zoom30Item.setSelected(newZoomFactor == 300);
        zoom40Item.setSelected(newZoomFactor == 400);
        zoom50Item.setSelected(newZoomFactor == 500);
        zoom60Item.setSelected(newZoomFactor == 600);

        //this will put zoomFactor on 50% increments
        //(so it will more likely match one of these values)
        newZoomFactor = ((int) (zoomFactor * 2)) * 50;
        zoom05Item.setSelected(newZoomFactor == 50);
        zoom15Item.setSelected(newZoomFactor == 150);

        //this will put zoomFactor on 25% increments
        //(so it will more likely match one of these values)
        newZoomFactor = ((int) (zoomFactor * 4)) * 25;
        zoom025Item.setSelected(newZoomFactor == 25);
        zoom075Item.setSelected(newZoomFactor == 75);
    } //selectZoomMenuItem

    //
    //
    //
    public double setZoom(double zoomFactor) {
        double newZoom = MathUtil.pin(zoomFactor, minZoom, maxZoom);

        if (newZoom != getPaintScale()) {
            log.debug("zoom: {}", zoomFactor);
            setPaintScale(newZoom);
            zoomLabel.setText(String.format("x%1$,.2f", newZoom));
            selectZoomMenuItem(newZoom);

            //save the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setProperty(getWindowFrameRef(), "zoom", zoomFactor);
            });
        }
        return getPaintScale();
    } //setZoom

    //
    //
    //
    public double getZoom() {
        return getPaintScale();
    }

    //
    //
    //
    private double zoomIn() {
        double newScale = _paintScale;

        newScale *= 1.1;

        return setZoom(newScale);
    } //zoomIn

    //
    //
    //
    private double zoomOut() {
        double newScale = _paintScale;

        newScale /= 1.1;
        return setZoom(newScale);
    } //zoomOut

    //
    // TODO: make this public? (might be useful!)
    //
    private Rectangle2D calculateMinimumLayoutBounds() {
        // calculate a union of the bounds of everything on the layout
        Rectangle2D result = new Rectangle2D.Double();

        // combine all (onscreen) Components into a list of Components
        List<Component> listOfComponents = new ArrayList<>();
        listOfComponents.addAll(backgroundImage);
        listOfComponents.addAll(sensorImage);
        listOfComponents.addAll(signalHeadImage);
        listOfComponents.addAll(markerImage);
        listOfComponents.addAll(labelImage);
        listOfComponents.addAll(clocks);
        listOfComponents.addAll(multiSensors);
        listOfComponents.addAll(signalList);
        listOfComponents.addAll(memoryLabelList);
        listOfComponents.addAll(blockContentsLabelList);
        listOfComponents.addAll(sensorList);
        listOfComponents.addAll(signalMastList);

        // combine their bounds
        for (Component o : listOfComponents) {
            if (result.isEmpty()) {
                result = o.getBounds();
            } else {
                result = result.createUnion(o.getBounds());
            }
        }

        // combine all (onscreen) LayoutTracks into a list of LayoutTracks
        List<LayoutTrack> listOfLayoutTracks = new ArrayList<>();
        listOfLayoutTracks.addAll(turnoutList);
        listOfLayoutTracks.addAll(trackList);
        listOfLayoutTracks.addAll(pointList);
        listOfLayoutTracks.addAll(xingList);
        listOfLayoutTracks.addAll(slipList);
        listOfLayoutTracks.addAll(turntableList);

        // combine their bounds
        for (LayoutTrack o : listOfLayoutTracks) {
            if (result.isEmpty()) {
                result = o.getBounds();
            } else {
                result = result.createUnion(o.getBounds());
            }
        }

        // put a grid size margin around it
        result = MathUtil.inset(result, -gridSize1st);

        return result;
    } // calculateMinimumPanelBounds

    /**
     * resize panel bounds
     *
     * @param forceFlag if false only grow bigger
     * @return the new (?) panel bounds
     */
    private Rectangle2D resizePanelBounds(boolean forceFlag) {
        Rectangle2D panelBounds = getPanelBounds();
        Rectangle2D layoutBounds = calculateMinimumLayoutBounds();
        if (forceFlag) {
            panelBounds = layoutBounds;
        } else {
            panelBounds.add(layoutBounds);
        }
        panelBounds = MathUtil.offset(panelBounds, -panelBounds.getX(), -panelBounds.getY());

        panelWidth = (int) panelBounds.getWidth();
        panelHeight = (int) panelBounds.getHeight();
        setTargetPanelSize(panelWidth, panelHeight);
        return panelBounds;
    } // resizePanelBounds

    //
    //
    //
    private double zoomToFit() {
        Rectangle2D layoutBounds = resizePanelBounds(true);

        layoutBounds = new Rectangle2D.Double(0.0, 0.0, panelWidth, panelHeight);

        // calculate the bounds for the scroll pane
        JScrollPane scrollPane = getPanelScrollPane();
        Rectangle2D scrollBounds = scrollPane.getViewportBorderBounds();

        // don't let its orgin go negative
        scrollBounds = MathUtil.offset(scrollBounds, -Math.min(scrollBounds.getX(), 0.0), -Math.min(scrollBounds.getY(), 0.0));

        // calculate the horzontial and vertical scales
        double scaleWidth = scrollPane.getWidth() / layoutBounds.getWidth();
        double scaleHeight = scrollPane.getHeight() / layoutBounds.getHeight();

        // set the new zoom to the smallest of the two
        double result = setZoom(Math.min(scaleWidth, scaleHeight));

        // set the new zoom (return value may be different)
        result = setZoom(result);

        // calculate new scroll bounds
        scrollBounds = MathUtil.scale(layoutBounds, result);

        // don't let its orgin go negative
        scrollBounds = MathUtil.offset(scrollBounds, -Math.min(scrollBounds.getX(), 0.0), -Math.min(scrollBounds.getY(), 0.0));

        // and scroll to it
        scrollPane.scrollRectToVisible(MathUtil.rectangleForRectangle2D(scrollBounds));

        return result;
    } //zoomToFit

    //
    //
    //
    private Point2D windowCenter() {
        //Returns window's center coordinates converted to layout space
        //Used for initial setup of turntables and reporters
        return MathUtil.point2DToPoint(MathUtil.divide(MathUtil.center(getBounds()), getPaintScale()));
    } //windowCenter

    //
    //
    //
    private void setupMarkerMenu(@Nonnull JMenuBar menuBar) {
        JMenu markerMenu = new JMenu(Bundle.getMessage("MenuMarker"));

        markerMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuMarkerMnemonic")));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLoco") + "...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLocoRoster") + "...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("RemoveMarkers")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeMarkers();
            }
        });
    } //setupMarkerMenu

    //
    //
    //
    private void setupDispatcherMenu(@Nonnull JMenuBar menuBar) {
        JMenu dispMenu = new JMenu(Bundle.getMessage("MenuDispatcher"));

        dispMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuDispatcherMnemonic")));
        dispMenu.add(new JMenuItem(new jmri.jmrit.dispatcher.DispatcherAction(Bundle.getMessage("MenuItemOpen"))));
        menuBar.add(dispMenu);
        JMenuItem newTrainItem = new JMenuItem(Bundle.getMessage("MenuItemNewTrain"));
        dispMenu.add(newTrainItem);
        newTrainItem.addActionListener((ActionEvent event) -> {
            if (InstanceManager.getDefault(jmri.TransitManager.class).getSystemNameList().size() <= 0) {
                //Inform the user that there are no Transits available, and don't open the window
                javax.swing.JOptionPane.showMessageDialog(null,
                        ResourceBundle.getBundle("jmri.jmrit.dispatcher.DispatcherBundle").
                                getString("NoTransitsMessage"));
            } else {
                DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
                if (!df.getNewTrainActive()) {
                    df.getActiveTrainFrame().initiateTrain(event, null, null);
                    df.setNewTrainActive(true);
                } else {
                    df.getActiveTrainFrame().showActivateFrame(null);
                }
            }
        });
        menuBar.add(dispMenu);
    } //setupDispatcherMenu

    //
    //
    //
    boolean openDispatcherOnLoad = false;

    public boolean getOpenDispatcherOnLoad() {
        return openDispatcherOnLoad;
    }

    public void setOpenDispatcherOnLoad(Boolean boo) {
        openDispatcherOnLoad = boo;
    }

    /**
     * Remove marker icons from panel
     */
    @Override
    protected void removeMarkers() {
        for (int i = markerImage.size(); i > 0; i--) {
            LocoIcon il = markerImage.get(i - 1);

            if ((il != null) && (il.isActive())) {
                markerImage.remove(i - 1);
                il.remove();
                il.dispose();
                setDirty();
            }
        }
        super.removeMarkers();
        redrawPanel();
    } //removeMarkers

    /*======================================*\
    |* Dialog box to enter new track widths *|
    \*======================================*/
    //operational variables for enter track width pane
    private JmriJFrame enterTrackWidthFrame = null;
    private boolean enterTrackWidthOpen = false;
    private boolean trackWidthChange = false;
    private JTextField sideTrackWidthField = new JTextField(6);
    private JTextField mainlineTrackWidthField = new JTextField(6);
    private JButton trackWidthDone;
    private JButton trackWidthCancel;

    //display dialog for entering track widths
    protected void enterTrackWidth() {
        if (enterTrackWidthOpen) {
            enterTrackWidthFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (enterTrackWidthFrame == null) {
            enterTrackWidthFrame = new JmriJFrame(Bundle.getMessage("SetTrackWidth"));
            enterTrackWidthFrame.addHelpMenu("package.jmri.jmrit.display.EnterTrackWidth", true);
            enterTrackWidthFrame.setLocation(70, 30);
            Container theContentPane = enterTrackWidthFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup mainline track width (placed above side track for clarity, name 'panel3' kept)
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel mainlineWidthLabel = new JLabel(Bundle.getMessage("MainlineTrackWidth"));
            panel3.add(mainlineWidthLabel);
            panel3.add(mainlineTrackWidthField);
            mainlineTrackWidthField.setToolTipText(Bundle.getMessage("MainlineTrackWidthHint"));
            theContentPane.add(panel3);

            //setup side track width
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel sideWidthLabel = new JLabel(Bundle.getMessage("SideTrackWidth"));
            panel2.add(sideWidthLabel);
            panel2.add(sideTrackWidthField);
            sideTrackWidthField.setToolTipText(Bundle.getMessage("SideTrackWidthHint"));
            theContentPane.add(panel2);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(trackWidthDone = new JButton(Bundle.getMessage("ButtonDone")));
            trackWidthDone.addActionListener((ActionEvent event) -> {
                trackWidthDonePressed(event);
            });
            trackWidthDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(trackWidthDone);
                rootPane.setDefaultButton(trackWidthDone);
            });

            //Cancel
            panel5.add(trackWidthCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            trackWidthCancel.addActionListener((ActionEvent event) -> {
                trackWidthCancelPressed(event);
            });
            trackWidthCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Track Widths
        mainlineTrackWidthField.setText(Integer.toString(getMainlineTrackWidth()));
        sideTrackWidthField.setText(Integer.toString(getSideTrackWidth()));
        enterTrackWidthFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                trackWidthCancelPressed(null);
            }
        });
        enterTrackWidthFrame.pack();
        enterTrackWidthFrame.setVisible(true);
        trackWidthChange = false;
        enterTrackWidthOpen = true;
    } //enterTrackWidth

    void trackWidthDonePressed(ActionEvent a) {
        //get side track width
        String newWidth = sideTrackWidthField.getText().trim();
        float wid = 0.0F;
        try {
            wid = Float.parseFloat(newWidth);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ((wid < 1.0) || (wid > 10.0)) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2"),
                            new Object[]{String.format(" %s ", wid)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (sideTrackWidth != wid) {
            sideTrackWidth = wid;
            trackWidthChange = true;
        }

        //get mainline track width
        newWidth = mainlineTrackWidthField.getText().trim();
        try {
            wid = Float.parseFloat(newWidth);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((wid < 1.0) || (wid > 10.0)) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2"),
                            new Object[]{String.format(" %s ", wid)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if (mainlineTrackWidth != wid) {
                mainlineTrackWidth = wid;
                trackWidthChange = true;
            }

            //success - hide dialog and repaint if needed
            enterTrackWidthOpen = false;
            enterTrackWidthFrame.setVisible(false);
            enterTrackWidthFrame.dispose();
            enterTrackWidthFrame = null;

            if (trackWidthChange) {
                redrawPanel();
                setDirty();
            }
        }
    } //trackWidthDonePressed

    void trackWidthCancelPressed(ActionEvent a) {
        enterTrackWidthOpen = false;
        enterTrackWidthFrame.setVisible(false);
        enterTrackWidthFrame.dispose();
        enterTrackWidthFrame = null;

        if (trackWidthChange) {
            redrawPanel();
            setDirty();
        }
    } //trackWidthCancelPressed

    /*====================================*\
    |* Dialog box to enter new grid sizes *|
    \*====================================*/
    //operational variables for enter grid sizes pane
    private JmriJFrame enterGridSizesFrame = null;
    private boolean enterGridSizesOpen = false;
    private boolean gridSizesChange = false;
    private JTextField primaryGridSizeField = new JTextField(6);
    private JTextField secondaryGridSizeField = new JTextField(6);
    private JButton gridSizesDone;
    private JButton gridSizesCancel;

    //display dialog for entering grid sizes
    protected void enterGridSizes() {
        if (enterGridSizesOpen) {
            enterGridSizesFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (enterGridSizesFrame == null) {
            enterGridSizesFrame = new JmriJFrame(Bundle.getMessage("SetGridSizes"));
            enterGridSizesFrame.addHelpMenu("package.jmri.jmrit.display.EnterGridSizes", true);
            enterGridSizesFrame.setLocation(70, 30);
            Container theContentPane = enterGridSizesFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup primary grid sizes
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel primaryGridSIzeLabel = new JLabel(Bundle.getMessage("PrimaryGridSize"));
            panel3.add(primaryGridSIzeLabel);
            panel3.add(primaryGridSizeField);
            primaryGridSizeField.setToolTipText(Bundle.getMessage("PrimaryGridSizeHint"));
            theContentPane.add(panel3);

            //setup side track width
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel secondaryGridSizeLabel = new JLabel(Bundle.getMessage("SecondaryGridSize"));
            panel2.add(secondaryGridSizeLabel);
            panel2.add(secondaryGridSizeField);
            secondaryGridSizeField.setToolTipText(Bundle.getMessage("SecondaryGridSizeHint"));
            theContentPane.add(panel2);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(gridSizesDone = new JButton(Bundle.getMessage("ButtonDone")));
            gridSizesDone.addActionListener((ActionEvent event) -> {
                gridSizesDonePressed(event);
            });
            gridSizesDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(gridSizesDone);
                rootPane.setDefaultButton(gridSizesDone);
            });

            //Cancel
            panel5.add(gridSizesCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            gridSizesCancel.addActionListener((ActionEvent event) -> {
                gridSizesCancelPressed(event);
            });
            gridSizesCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Track Widths
        primaryGridSizeField.setText(Integer.toString(gridSize1st));
        secondaryGridSizeField.setText(Integer.toString(gridSize2nd));
        enterGridSizesFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                gridSizesCancelPressed(null);
            }
        });
        enterGridSizesFrame.pack();
        enterGridSizesFrame.setVisible(true);
        gridSizesChange = false;
        enterGridSizesOpen = true;
    } //enterGridSizes

    void gridSizesDonePressed(ActionEvent a) {
        String newGridSize = "";
        float siz = 0.0F;

        //get secondary grid size
        newGridSize = secondaryGridSizeField.getText().trim();
        try {
            siz = Float.parseFloat(newGridSize);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((siz < 5.0) || (siz > 100.0)) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", siz)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (gridSize2nd != siz) {
            gridSize2nd = (int) siz;
            gridSizesChange = true;
        }

        //get mainline track width
        newGridSize = primaryGridSizeField.getText().trim();
        try {
            siz = Float.parseFloat(newGridSize);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((siz < 5) || (siz > 100.0)) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", siz)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if (gridSize1st != siz) {
                gridSize1st = (int) siz;
                gridSizesChange = true;
            }

            //success - hide dialog and repaint if needed
            enterGridSizesOpen = false;
            enterGridSizesFrame.setVisible(false);
            enterGridSizesFrame.dispose();
            enterGridSizesFrame = null;

            if (gridSizesChange) {
                redrawPanel();
                setDirty();
            }
        }
    } //gridSizesDonePressed

    void gridSizesCancelPressed(ActionEvent a) {
        enterGridSizesOpen = false;
        enterGridSizesFrame.setVisible(false);
        enterGridSizesFrame.dispose();
        enterGridSizesFrame = null;

        if (gridSizesChange) {
            redrawPanel();
            setDirty();
        }
    } //gridSizesCancelPressed

    /*=======================================*\
    |* Dialog box to enter new reporter info *|
    \*=======================================*/
    //operational variables for enter reporter pane
    private JmriJFrame enterReporterFrame = null;
    private boolean reporterOpen = false;
    private JTextField xPositionField = new JTextField(6);
    private JTextField yPositionField = new JTextField(6);
    private JTextField reporterNameField = new JTextField(6);
    private JButton reporterDone;
    private JButton reporterCancel;

    //display dialog for entering Reporters
    protected void enterReporter(int defaultX, int defaultY) {
        if (reporterOpen) {
            enterReporterFrame.setVisible(true);

            return;
        }

        //Initialize if needed
        if (enterReporterFrame == null) {
            enterReporterFrame = new JmriJFrame(Bundle.getMessage("AddReporter"));

//enterReporterFrame.addHelpMenu("package.jmri.jmrit.display.AddReporterLabel", true);
            enterReporterFrame.setLocation(70, 30);
            Container theContentPane = enterReporterFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup reporter entry
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel reporterLabel = new JLabel(Bundle.getMessage("ReporterName"));
            panel2.add(reporterLabel);
            panel2.add(reporterNameField);
            reporterNameField.setToolTipText(Bundle.getMessage("ReporterNameHint"));
            theContentPane.add(panel2);

            //setup coordinates entry
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel xCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationX"));
            panel3.add(xCoordLabel);
            panel3.add(xPositionField);
            xPositionField.setToolTipText(Bundle.getMessage("ReporterLocationXHint"));
            JLabel yCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationY"));
            panel3.add(yCoordLabel);
            panel3.add(yPositionField);
            yPositionField.setToolTipText(Bundle.getMessage("ReporterLocationYHint"));
            theContentPane.add(panel3);

            //set up Add and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(reporterDone = new JButton(Bundle.getMessage("AddNewLabel")));
            reporterDone.addActionListener((ActionEvent event) -> {
                reporterDonePressed(event);
            });
            reporterDone.setToolTipText(Bundle.getMessage("ReporterDoneHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(reporterDone);
                rootPane.setDefaultButton(reporterDone);
            });

            //Cancel
            panel5.add(reporterCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            reporterCancel.addActionListener((ActionEvent event) -> {
                reporterCancelPressed(event);
            });
            reporterCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Reporter Icon
        xPositionField.setText(Integer.toString(defaultX));
        yPositionField.setText(Integer.toString(defaultY));
        enterReporterFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                reporterCancelPressed(null);
            }
        });
        enterReporterFrame.pack();
        enterReporterFrame.setVisible(true);
        reporterOpen = true;
    } //enterReporter

    void reporterDonePressed(ActionEvent a) {
        //get size of current panel
        Dimension dim = getTargetPanelSize();

        //get x coordinate
        String newX = "";
        int xx = 0;

        newX = xPositionField.getText().trim();
        try {
            xx = Integer.parseInt(newX);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((xx <= 0) || (xx > dim.width)) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", xx)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y coordinate
        String newY = "";
        int yy = 0;
        newY = yPositionField.getText().trim();
        try {
            yy = Integer.parseInt(newY);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((yy <= 0) || (yy > dim.height)) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", yy)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get reporter name
        Reporter reporter = null;
        String rName = reporterNameField.getText().trim();

        if (InstanceManager.getNullableDefault(jmri.ReporterManager.class) != null) {
            try {
                reporter = InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter(rName);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(enterReporterFrame,
                        java.text.MessageFormat.format(Bundle.getMessage("Error18"),
                                new Object[]{rName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);

                return;
            }

            if (!rName.equals(reporter.getDisplayName())) {
                rName = rName.toUpperCase();
            }
        } else {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    Bundle.getMessage("Error17"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //add the reporter icon
        addReporter(rName, xx, yy);

        //success - repaint the panel
        redrawPanel();
        enterReporterFrame.setVisible(true);
    } //reporterDonePressed

    void reporterCancelPressed(ActionEvent a) {
        reporterOpen = false;
        enterReporterFrame.setVisible(false);
        enterReporterFrame.dispose();
        enterReporterFrame = null;
        redrawPanel();
    } //reporterCancelPressed

    /*===============================*\
    |*  Dialog box to enter scale /  *|
    |*  translate track diagram info *|
    \*===============================*/
    //operational variables for scale/translate track diagram pane
    private JmriJFrame scaleTrackDiagramFrame = null;
    private boolean scaleTrackDiagramOpen = false;
    private JTextField xFactorField = new JTextField(6);
    private JTextField yFactorField = new JTextField(6);
    private JTextField xTranslateField = new JTextField(6);
    private JTextField yTranslateField = new JTextField(6);
    private JButton scaleTrackDiagramDone;
    private JButton scaleTrackDiagramCancel;

    //display dialog for scaling the track diagram
    protected void scaleTrackDiagram() {
        if (scaleTrackDiagramOpen) {
            scaleTrackDiagramFrame.setVisible(true);

            return;
        }

        //Initialize if needed
        if (scaleTrackDiagramFrame == null) {
            scaleTrackDiagramFrame = new JmriJFrame(Bundle.getMessage("ScaleTrackDiagram"));
            scaleTrackDiagramFrame.addHelpMenu("package.jmri.jmrit.display.ScaleTrackDiagram", true);
            scaleTrackDiagramFrame.setLocation(70, 30);
            Container theContentPane = scaleTrackDiagramFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xTranslateLabel = new JLabel(Bundle.getMessage("XTranslateLabel"));
            panel31.add(xTranslateLabel);
            panel31.add(xTranslateField);
            xTranslateField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yTranslateLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yTranslateLabel);
            panel32.add(yTranslateField);
            yTranslateField.setToolTipText(Bundle.getMessage("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message 1
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(Bundle.getMessage("Message1Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //setup x factor
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            JLabel xFactorLabel = new JLabel(Bundle.getMessage("XFactorLabel"));
            panel21.add(xFactorLabel);
            panel21.add(xFactorField);
            xFactorField.setToolTipText(Bundle.getMessage("FactorHint"));
            theContentPane.add(panel21);

            //setup y factor
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
            JLabel yFactorLabel = new JLabel(Bundle.getMessage("YFactorLabel"));
            panel22.add(yFactorLabel);
            panel22.add(yFactorField);
            yFactorField.setToolTipText(Bundle.getMessage("FactorHint"));
            theContentPane.add(panel22);

            //setup information message 2
            JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
            JLabel message2Label = new JLabel(Bundle.getMessage("Message2Label"));
            panel23.add(message2Label);
            theContentPane.add(panel23);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(scaleTrackDiagramDone = new JButton(Bundle.getMessage("ScaleTranslate")));
            scaleTrackDiagramDone.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramDonePressed(event);
            });
            scaleTrackDiagramDone.setToolTipText(Bundle.getMessage("ScaleTranslateHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(scaleTrackDiagramDone);
                rootPane.setDefaultButton(scaleTrackDiagramDone);
            });

            panel5.add(scaleTrackDiagramCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            scaleTrackDiagramCancel.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramCancelPressed(event);
            });
            scaleTrackDiagramCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Scale and Translation
        xFactorField.setText("1.0");
        yFactorField.setText("1.0");
        xTranslateField.setText("0");
        yTranslateField.setText("0");
        scaleTrackDiagramFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                scaleTrackDiagramCancelPressed(null);
            }
        });
        scaleTrackDiagramFrame.pack();
        scaleTrackDiagramFrame.setVisible(true);
        scaleTrackDiagramOpen = true;
    } //scaleTrackDiagram

    void scaleTrackDiagramDonePressed(ActionEvent a) {
        String newText = "";
        boolean scaleChange = false;
        boolean translateError = false;
        float xTranslation = 0.0F;
        float yTranslation = 0.0F;
        float xFactor = 1.0F;
        float yFactor = 1.0F;

        //get x translation
        newText = xTranslateField.getText().trim();
        try {
            xTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y translation
        newText = yTranslateField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get x factor
        newText = xFactorField.getText().trim();
        try {
            xFactor = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y factor
        newText = yFactorField.getText().trim();
        try {
            yFactor = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //here when all numbers read in successfully - check for translation
        if ((xTranslation != 0.0F) || (yTranslation != 0.0F)) {
            //apply translation
            if (translateTrack(xTranslation, yTranslation)) {
                scaleChange = true;
            } else {
                log.error("Error translating track diagram");
                translateError = true;
            }
        }

        if (!translateError && ((xFactor != 1.0) || (yFactor != 1.0))) {
            //apply scale change
            if (scaleTrack(xFactor, yFactor)) {
                scaleChange = true;
            } else {
                log.error("Error scaling track diagram");
            }
        }

        //success - dispose of the dialog and repaint if needed
        scaleTrackDiagramOpen = false;
        scaleTrackDiagramFrame.setVisible(false);
        scaleTrackDiagramFrame.dispose();
        scaleTrackDiagramFrame = null;

        if (scaleChange) {
            redrawPanel();
            setDirty();
        }
    } //scaleTrackDiagramDonePressed

    void scaleTrackDiagramCancelPressed(ActionEvent a) {
        scaleTrackDiagramOpen = false;
        scaleTrackDiagramFrame.setVisible(false);
        scaleTrackDiagramFrame.dispose();
        scaleTrackDiagramFrame = null;
    } //scaleTrackDiagramCancelPressed

    boolean translateTrack(float xDel, float yDel) {
        List<List> listOfLists = new ArrayList<>();
        listOfLists.add(turnoutList);
        listOfLists.add(xingList);
        listOfLists.add(slipList);
        listOfLists.add(turntableList);
        listOfLists.add(pointList);

        Point2D delta = new Point2D.Double(xDel, yDel);
        for (List<LayoutTrack> l : listOfLists) {
            for (LayoutTrack lt : l) {
                lt.setCoordsCenter(MathUtil.add(lt.getCoordsCenter(), delta));
            }
        }
        resizePanelBounds(true);
        return true;
    } //translateTrack

    /**
     * scale all LayoutTracks coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    boolean scaleTrack(float xFactor, float yFactor) {
        List<List> listOfLists = new ArrayList<>();
        listOfLists.add(turnoutList);
        listOfLists.add(xingList);
        listOfLists.add(slipList);
        listOfLists.add(turntableList);
        listOfLists.add(pointList);

        for (List<LayoutTrack> l : listOfLists) {
            for (LayoutTrack lt : l) {
                lt.scaleCoords(xFactor, yFactor);
            }
        }

        //update the overall scale factors
        xScale *= xFactor;
        yScale *= yFactor;

        resizePanelBounds(true);
        return true;
    } //scaleTrack

    /*=========================================*\
    |* Dialog box to enter move selection info *|
    \*=========================================*/
    //operational variables for move selection pane
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

    //display dialog for translation a selection
    protected void moveSelection() {
        if (!selectionActive || (selectionWidth == 0.0) || (selectionHeight == 0.0)) {
            //no selection has been made - nothing to move
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error12"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (moveSelectionOpen) {
            moveSelectionFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (moveSelectionFrame == null) {
            moveSelectionFrame = new JmriJFrame(Bundle.getMessage("TranslateSelection"));
            moveSelectionFrame.addHelpMenu("package.jmri.jmrit.display.TranslateSelection", true);
            moveSelectionFrame.setLocation(70, 30);
            Container theContentPane = moveSelectionFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xMoveLabel = new JLabel(Bundle.getMessage("XTranslateLabel"));
            panel31.add(xMoveLabel);
            panel31.add(xMoveField);
            xMoveField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yMoveLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yMoveLabel);
            panel32.add(yMoveField);
            yMoveField.setToolTipText(Bundle.getMessage("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(Bundle.getMessage("Message3Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(moveSelectionDone = new JButton(Bundle.getMessage("MoveSelection")));
            moveSelectionDone.addActionListener((ActionEvent event) -> {
                moveSelectionDonePressed(event);
            });
            moveSelectionDone.setToolTipText(Bundle.getMessage("MoveSelectionHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(moveSelectionDone);
                rootPane.setDefaultButton(moveSelectionDone);
            });

            panel5.add(moveSelectionCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            moveSelectionCancel.addActionListener((ActionEvent event) -> {
                moveSelectionCancelPressed(event);
            });
            moveSelectionCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Translation
        xMoveField.setText("0");
        yMoveField.setText("0");
        moveSelectionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                moveSelectionCancelPressed(null);
            }
        });
        moveSelectionFrame.pack();
        moveSelectionFrame.setVisible(true);
        moveSelectionOpen = true;
    } //moveSelection

    void moveSelectionDonePressed(ActionEvent a) {
        String newText = "";
        float xTranslation = 0.0F;
        float yTranslation = 0.0F;

        //get x translation
        newText = xMoveField.getText().trim();
        try {
            xTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(moveSelectionFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y translation
        newText = yMoveField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(moveSelectionFrame,
                    String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                            e, Bundle.getMessage("TryAgain")),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //here when all numbers read in - translation if entered
        if ((xTranslation != 0.0F) || (yTranslation != 0.0F)) {
            Rectangle2D selectionRect = getSelectionRect();

            //set up undo information
            undoRect = MathUtil.offset(selectionRect, xTranslation, yTranslation);
            undoDeltaX = -xTranslation;
            undoDeltaY = -yTranslation;
            canUndoMoveSelection = true;

            //apply translation to icon items within the selection
            List<Positionable> contents = getContents();

            for (Positionable c : contents) {
                Point2D upperLeft = c.getLocation();

                if (selectionRect.contains(upperLeft)) {
                    int xNew = (int) (upperLeft.getX() + xTranslation);
                    int yNew = (int) (upperLeft.getY() + yTranslation);
                    c.setLocation(xNew, yNew);
                }
            }

            // loop over all turnouts
            for (LayoutTurnout t : turnoutList) {
                Point2D center = t.getCoordsCenter();

                if (selectionRect.contains(center)) {
                    t.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                            center.getY() + yTranslation));
                }
            }

            // loop over all level crossings
            for (LevelXing x : xingList) {
                Point2D center = x.getCoordsCenter();

                if (selectionRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                            center.getY() + yTranslation));
                }
            }

            // loop over all slips
            for (LayoutSlip sl : slipList) {
                Point2D center = sl.getCoordsCenter();

                if (selectionRect.contains(center)) {
                    sl.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                            center.getY() + yTranslation));
                }
            }

            // loop over all turntables
            for (LayoutTurntable x : turntableList) {
                Point2D center = x.getCoordsCenter();

                if (selectionRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                            center.getY() + yTranslation));
                }
            }

            // loop over all Anchor Points and End Bumpers
            for (PositionablePoint p : pointList) {
                Point2D coord = p.getCoordsCenter();

                if (selectionRect.contains(coord)) {
                    p.setCoordsCenter(new Point2D.Double(coord.getX() + xTranslation,
                            coord.getY() + yTranslation));
                }
            }
            resizePanelBounds(false);
            setDirty();
            redrawPanel();
        }

        //success - hide dialog
        moveSelectionOpen = false;
        moveSelectionFrame.setVisible(false);
        moveSelectionFrame.dispose();
        moveSelectionFrame = null;
    } //moveSelectionDonePressed

    void moveSelectionCancelPressed(ActionEvent a) {
        moveSelectionOpen = false;
        moveSelectionFrame.setVisible(false);
        moveSelectionFrame.dispose();
        moveSelectionFrame = null;
    } //moveSelectionCancelPressed

    void undoMoveSelection() {
        if (canUndoMoveSelection) {
            List<Positionable> contents = getContents();

            for (Positionable c : contents) {
                Point2D upperLeft = c.getLocation();

                if (undoRect.contains(upperLeft)) {
                    int xNew = (int) (upperLeft.getX() + undoDeltaX);
                    int yNew = (int) (upperLeft.getY() + undoDeltaY);
                    c.setLocation(xNew, yNew);
                }
            }

            for (LayoutTurnout t : turnoutList) {
                Point2D center = t.getCoordsCenter();

                if (undoRect.contains(center)) {
                    t.setCoordsCenter(new Point2D.Double(center.getX() + undoDeltaX,
                            center.getY() + undoDeltaY));
                }
            }

            for (LevelXing x : xingList) {
                Point2D center = x.getCoordsCenter();

                if (undoRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + undoDeltaX,
                            center.getY() + undoDeltaY));
                }
            }

            for (LayoutSlip sl : slipList) {
                Point2D center = sl.getCoordsCenter();

                if (undoRect.contains(center)) {
                    sl.setCoordsCenter(new Point2D.Double(center.getX() + undoDeltaX,
                            center.getY() + undoDeltaY));
                }
            }

            for (LayoutTurntable x : turntableList) {
                Point2D center = x.getCoordsCenter();

                if (undoRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + undoDeltaX,
                            center.getY() + undoDeltaY));
                }
            }

            for (PositionablePoint p : pointList) {
                Point2D coord = p.getCoordsCenter();

                if (undoRect.contains(coord)) {
                    p.setCoordsCenter(new Point2D.Double(coord.getX() + undoDeltaX,
                            coord.getY() + undoDeltaY));
                }
            }
            resizePanelBounds(false);
            redrawPanel();
            canUndoMoveSelection = false;
        }
    } //undoMoveSelection

    public void setCurrentPositionAndSize() {
        //save current panel location and size
        Dimension dim = getSize();

        //Compute window size based on LayoutEditor size
        windowHeight = dim.height;
        windowWidth = dim.width;

        //Compute layout size based on LayoutPane size
        dim = getTargetPanelSize();
        panelHeight = (int) (dim.height / getPaintScale());
        panelWidth = (int) (dim.width / getPaintScale());
        Point pt = getLocationOnScreen();
        upperLeftX = pt.x;
        upperLeftY = pt.y;

        // TODO: figure out why this isn't working...
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
            String windowFrameRef = getWindowFrameRef();

            //the restore code for this isn't working...
            prefsMgr.setWindowLocation(windowFrameRef, new Point(upperLeftX, upperLeftY));
            prefsMgr.setWindowSize(windowFrameRef, new Dimension(windowWidth, windowHeight));

            if (true) {
                Point prefsWindowLocation = prefsMgr.getWindowLocation(windowFrameRef);

                if ((prefsWindowLocation.x != upperLeftX) || (prefsWindowLocation.y != upperLeftY)) {
                    log.error("setWindowLocation failure.");
                }
                Dimension prefsWindowSize = prefsMgr.getWindowSize(windowFrameRef);

                if ((prefsWindowSize.width != windowWidth) || (prefsWindowSize.height != windowHeight)) {
                    log.error("setWindowSize failure.");
                }
            }

            //we're going to use this instead
            if (true) { //(Nope, it's not working ether)
                //save it in the user preferences for the window
                Rectangle2D windowRectangle2D = new Rectangle2D.Double(upperLeftX, upperLeftY, windowWidth, windowHeight);
                prefsMgr.setProperty(windowFrameRef, "windowRectangle2D", windowRectangle2D);
                Object prefsProp = prefsMgr.getProperty(windowFrameRef, "windowRectangle2D");
                log.debug("testing prefsProp: {}", prefsProp);
            }
        });

        log.debug("setCurrentPositionAndSize Position - {},{} WindowSize - {},{} PanelSize - {},{}", upperLeftX, upperLeftY, windowWidth, windowHeight, panelWidth, panelHeight);
        setDirty();
    } //setCurrentPositionAndSize()

    private JRadioButtonMenuItem addButtonGroupMenuEntry(@Nonnull JMenu inMenu,
            ButtonGroup inButtonGroup, final String inName,
            boolean inSelected, ActionListener inActionListener) {
        JRadioButtonMenuItem result = new JRadioButtonMenuItem(inName);
        if (inActionListener != null) {
            result.addActionListener(inActionListener);
        }
        if (inButtonGroup != null) {
            inButtonGroup.add(result);
        }
        result.setSelected(inSelected);

        if (inMenu != null) {
            inMenu.add(result);
        }
        return result;
    }

    private void addBackgroundColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultBackgroundColor.equals(inColor)) {
                    defaultBackgroundColor = inColor;
                    setBackgroundColor(inColor);
                    setDirty();
                    redrawPanel();
                }
            }
        };
        addButtonGroupMenuEntry(inMenu, backgroundColorButtonGroup, inName,
                inColor == defaultBackgroundColor, a);
    } //addBackgroundColorMenuEntry

    private void addTrackColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultTrackColor.equals(inColor)) {
                    LayoutTrack.setDefaultTrackColor(inColor);
                    defaultTrackColor = inColor;
                    setDirty();
                    redrawPanel();
                }
            } //actionPerformed
        };
        addButtonGroupMenuEntry(inMenu, trackColorButtonGroup, inName,
                inColor == defaultTrackColor, a);
    } //addTrackColorMenuEntry

    private void addTrackOccupiedColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultOccupiedTrackColor.equals(inColor)) {
                    defaultOccupiedTrackColor = inColor;
                    setDirty();
                    redrawPanel();
                }
            } //actionPerformed
        };
        addButtonGroupMenuEntry(inMenu, trackOccupiedColorButtonGroup, inName,
                inColor == defaultOccupiedTrackColor, a);
    } //addTrackOccupiedColorMenuEntry

    private void addTrackAlternativeColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultAlternativeTrackColor.equals(inColor)) {
                    defaultAlternativeTrackColor = inColor;
                    setDirty();
                    redrawPanel();
                }
            } //actionPerformed
        };
        addButtonGroupMenuEntry(inMenu, trackAlternativeColorButtonGroup, inName,
                inColor == defaultAlternativeTrackColor, a);
    } //addTrackAlternativeColorMenuEntry

    private void addTextColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultTextColor.equals(inColor)) {
                    defaultTextColor = inColor;
                    setDirty();
                    redrawPanel();
                }
            } //actionPerformed
        };
        addButtonGroupMenuEntry(inMenu, textColorButtonGroup, inName,
                inColor == defaultTextColor, a);
    } //addTextColorMenuEntry

    private void addTurnoutCircleColorMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, @Nonnull final Color inColor) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTurnoutCircleColor(ColorUtil.colorToString(inColor));
                setDirty();
                redrawPanel();
            } //actionPerformed
        };
        addButtonGroupMenuEntry(inMenu, turnoutCircleColorButtonGroup, inName,
                (inColor != null) && (inColor == turnoutCircleColor), a);
    } //addTurnoutCircleColorMenuEntry

    private void addTurnoutCircleSizeMenuEntry(@Nonnull JMenu inMenu, @Nonnull String inName, final int inSize) {
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getTurnoutCircleSize() != inSize) {
                    setTurnoutCircleSize(inSize);
                    setDirty();
                    redrawPanel();
                }
            } //actionPerformed
        };
        JRadioButtonMenuItem r = addButtonGroupMenuEntry(inMenu,
                turnoutCircleSizeButtonGroup, inName,
                getTurnoutCircleSize() == inSize, a);
    } //addTurnoutCircleSizeMenuEntry

    protected void setOptionMenuTurnoutCircleSize() {
        String tcs = Integer.toString(getTurnoutCircleSize());
        Enumeration e = turnoutCircleSizeButtonGroup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton button = (AbstractButton) e.nextElement();
            String buttonName = button.getText();
            button.setSelected(buttonName.equals(tcs));
        }
    } //setOptionMenuTurnoutCircleSize

    protected void setOptionMenuTurnoutCircleColor() {
        setOptionMenuColor(turnoutCircleColorButtonGroup, turnoutCircleColor);
        // if nothing is selected...
        if (turnoutCircleSizeButtonGroup.getSelection() == null) {
            // then select the 1st button
            Enumeration e = turnoutCircleColorButtonGroup.getElements();
            AbstractButton button = (AbstractButton) e.nextElement();
            button.setSelected(true);
        }
    } //setOptionMenuTurnoutCircleColor

    protected void setOptionMenuTextColor() {
        setOptionMenuColor(textColorButtonGroup, defaultTextColor);
    } //setOptionMenuTextColor

    protected void setOptionMenuBackgroundColor() {
        setOptionMenuColor(backgroundColorButtonGroup, defaultBackgroundColor);
    } //setOptionMenuBackgroundColor

    protected void setOptionMenuTrackColor() {
        setOptionMenuColor(trackColorButtonGroup, defaultTrackColor);
        setOptionMenuColor(trackOccupiedColorButtonGroup, defaultOccupiedTrackColor);
        setOptionMenuColor(trackAlternativeColorButtonGroup, defaultAlternativeTrackColor);
    } //setOptionMenuTrackColor

    private void setOptionMenuColor(@Nonnull ButtonGroup inButtonGroup, @Nullable Color inColor) {
        Enumeration e = inButtonGroup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton button = (AbstractButton) e.nextElement();
            String buttonName = button.getText().replaceAll("\\s+", "");
            if (buttonName.equals("UseDefaultTrackColor")) {
                button.setSelected(false);
            } else {
                // make 1st character lower case
                buttonName = Character.toLowerCase(buttonName.charAt(0)) + buttonName.substring(1);
                Color buttonColor = ColorUtil.stringToColor(buttonName);
                button.setSelected(buttonColor == inColor);
            }
        }
    }

    @Override
    public void setScroll(int state) {
        if (isEditable()) {
            //In edit mode the scroll bars are always displayed, however we will want to set the scroll for when we exit edit mode
            super.setScroll(Editor.SCROLL_BOTH);
            _scrollState = state;
        } else {
            super.setScroll(state);
        }
    } //setScroll

    /**
     * Add a layout turntable at location specified
     *
     * @param pt x,y placement for turntable
     */
    public void addTurntable(@Nonnull Point2D pt) {
        //get unique name
        String name = finder.uniqueName("TUR", numLayoutTurntables++);
        LayoutTurntable lt = new LayoutTurntable(name, pt, this);

        turntableList.add(lt);

        lt.addRay(0.0);
        lt.addRay(90.0);
        lt.addRay(180.0);
        lt.addRay(270.0);
        setDirty();

        unionToPanelBounds(lt.getBounds());
    } //addTurntable

    /**
     * Allow external trigger of re-drawHidden
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
        savedAnimatingLayout = isAnimating();
        savedShowHelpBar = getShowHelpBar();
    } //resetDirty

    /**
     * Allow external set of dirty bit
     *
     * @param val true/false for panelChanged
     */
    public void setDirty(boolean val) {
        panelChanged = val;
    }

    public void setDirty() {
        setDirty(true);
    }

    /**
     * Check the dirty state
     */
    public boolean isDirty() {
        return panelChanged;
    }

    /*
     * Get mouse coordinates and adjust for zoom
     */
    private Point2D calcLocation(@Nonnull MouseEvent event, int dX, int dY) {
        xLoc = (int) ((event.getX() + dX) / getPaintScale());
        yLoc = (int) ((event.getY() + dY) / getPaintScale());
        dLoc.setLocation(xLoc, yLoc);
        return dLoc;
    } //calcLocation

    private Point2D calcLocation(@Nonnull MouseEvent event) {
        return calcLocation(event, 0, 0);
    } //calcLocation

    public enum LayoutEditorMode {
        UNKNOWN,
        EDIT_POPUP,
        EDIT_DRAG,
        EDIT_ADDING_TRACK_SEGMENT,
        CONTROLLING_TURNOUT,
        MOVING_MARKER,
        SELECTION_MOUSE_PRESSED,
        SELECTION_MOUSE_RELEASED,
        SELECTION_MOUSE_DRAGGED,
        SELECTION_MOUSE_CLICKED,
        ADDING_OBJECT,
        SHOW_POPUP,
        DROPPED_TURNOUT,
        DROPPED_POSITIONABLE_POINT,
    }

    private LayoutEditorMode layoutEditorMode = LayoutEditorMode.UNKNOWN;

    /**
     * Handle a mouse pressed event
     */
    @Override
    public void mousePressed(@Nonnull MouseEvent event) {
        //initialize cursor position
        _anchorX = xLoc;
        _anchorY = yLoc;
        _lastX = _anchorX;
        _lastY = _anchorY;
        calcLocation(event);

        if (isEditable()) {
            boolean prevSelectionActive = selectionActive;
            selectionActive = false;
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));

            if (event.isPopupTrigger()) {
                if (event.isMetaDown() || event.isAltDown()) {
                    //if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    //no possible conflict with moving, display the popup now
                    showEditPopUps(event);
                    layoutEditorMode = LayoutEditorMode.EDIT_POPUP;
                }
            }

            if (event.isMetaDown() || event.isAltDown()) {
                layoutEditorMode = LayoutEditorMode.EDIT_DRAG;
                //if dragging an item, identify the item for mouseDragging
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;

                if (hitPointCheckLayoutTracks(dLoc)) {
                    selectedObject = foundObject;
                    selectedPointType = foundPointType;
                    startDelta.setLocation(MathUtil.subtract(foundLocation, dLoc));
                    foundObject = null;
                } else {
                    selectedObject = checkMarkerPopUps(dLoc);

                    if (selectedObject != null) {
                        selectedPointType = LayoutTrack.MARKER;
                        startDelta.setLocation((((LocoIcon) selectedObject).getX() - dLoc.getX()),
                                (((LocoIcon) selectedObject).getY() - dLoc.getY()));
                    } else {
                        selectedObject = checkClockPopUps(dLoc);

                        if (selectedObject != null) {
                            selectedPointType = LayoutTrack.LAYOUT_POS_JCOMP;
                            startDelta.setLocation((((PositionableJComponent) selectedObject).getX() - dLoc.getX()),
                                    (((PositionableJComponent) selectedObject).getY() - dLoc.getY()));
                        } else {
                            selectedObject = checkMultiSensorPopUps(dLoc);

                            if (selectedObject != null) {
                                selectedPointType = LayoutTrack.MULTI_SENSOR;
                                startDelta.setLocation((((MultiSensorIcon) selectedObject).getX() - dLoc.getX()),
                                        (((MultiSensorIcon) selectedObject).getY() - dLoc.getY()));
                            }
                        }
                    }

                    if (selectedObject == null) {
                        selectedObject = checkSensorIconPopUps(dLoc);
                        if (selectedObject == null) {
                            selectedObject = checkSignalHeadIconPopUps(dLoc);
                            if (selectedObject == null) {
                                selectedObject = checkLabelImagePopUps(dLoc);
                                if (selectedObject == null) {
                                    selectedObject = checkSignalMastIconPopUps(dLoc);
                                }
                            }
                        }

                        if (selectedObject != null) {
                            selectedPointType = LayoutTrack.LAYOUT_POS_LABEL;
                            startDelta.setLocation((((PositionableLabel) selectedObject).getX() - dLoc.getX()),
                                    (((PositionableLabel) selectedObject).getY() - dLoc.getY()));

                            if (selectedObject instanceof MemoryIcon) {
                                MemoryIcon pm = (MemoryIcon) selectedObject;

                                if (pm.getPopupUtility().getFixedWidth() == 0) {
                                    startDelta.setLocation((pm.getOriginalX() - dLoc.getX()),
                                            (pm.getOriginalY() - dLoc.getY()));
                                }
                            }
                        } else {
                            selectedObject = checkBackgroundPopUps(dLoc);

                            if (selectedObject != null) {
                                selectedPointType = LayoutTrack.LAYOUT_POS_LABEL;
                                startDelta.setLocation((((PositionableLabel) selectedObject).getX() - dLoc.getX()),
                                        (((PositionableLabel) selectedObject).getY() - dLoc.getY()));
                            }
                        }
                    }
                }
            } else if (event.isShiftDown() && trackButton.isSelected() && (!event.isPopupTrigger())) {
                //starting a Track Segment, check for free connection point
                selectedObject = null;

                if (hitPointCheckLayoutTracks(dLoc, true)) {
                    //match to a free connection point
                    beginObject = foundObject;
                    beginPointType = foundPointType;
                    beginLocation = foundLocation;
                    //TODO: highlight all free connection points?
                    layoutEditorMode = LayoutEditorMode.EDIT_ADDING_TRACK_SEGMENT;
                } else {
                    //TODO: auto-add anchor point?
                    foundObject = null;
                    beginObject = null;
                }
            } else if ((!event.isShiftDown()) && (!event.isControlDown()) && (!event.isPopupTrigger())) {
                //check if controlling a turnout in edit mode
                selectedObject = null;

                if (allControlling()) {
                    if (checkControls(false)) {
                        layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
                    }
                }

                //initialize starting selection - cancel any previous selection rectangle
                selectionActive = true;
                selectionX = dLoc.getX();
                selectionY = dLoc.getY();
                selectionWidth = 0.0;
                selectionHeight = 0.0;
            }

            if (prevSelectionActive) {
                redrawPanel();
            }
        } else if (allControlling() && (!event.isMetaDown()) && (!event.isPopupTrigger())
                && (!event.isAltDown()) && (!event.isShiftDown()) && (!event.isControlDown())) {
            //not in edit mode - check if mouse is on a turnout (using wider search range)
            selectedObject = null;
            if (checkControls(true)) {
                layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
            }
        } else if ((event.isMetaDown() || event.isAltDown())
                && (!event.isShiftDown()) && (!event.isControlDown())) {
            //not in edit mode - check if moving a marker if there are any
            selectedObject = checkMarkerPopUps(dLoc);
            if (selectedObject != null) {
                layoutEditorMode = LayoutEditorMode.MOVING_MARKER;
                selectedPointType = LayoutTrack.MARKER;
                startDelta.setLocation((((LocoIcon) selectedObject).getX() - dLoc.getX()),
                        (((LocoIcon) selectedObject).getY() - dLoc.getY()));
            }
        } else if (event.isPopupTrigger() && (!event.isShiftDown())) {
            //not in edit mode - check if a marker popup menu is being requested
            LocoIcon lo = checkMarkerPopUps(dLoc);
            if (lo != null) {
                delayedPopupTrigger = true;
            }
        }

        if (!event.isPopupTrigger()) {
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                layoutEditorMode = LayoutEditorMode.SELECTION_MOUSE_PRESSED;
                selections.get(0).doMousePressed(event);
            }
        }
        thisPanel.requestFocusInWindow();
    } //mousePressed

    // this is a method to iterate over a list of lists of items
    // calling the predicate tester.test on each one
    // all matching items are then added to the resulting ArrayList
    private static List testEachItemInListOfLists(
            @Nonnull List<List> listOfListsOfObjects,
            @Nonnull Predicate<Object> tester) {
        List result = new ArrayList();
        for (List<Object> listOfObjects : listOfListsOfObjects) {
            List<Object> l = listOfObjects.stream().filter(o -> tester.test(o)).collect(Collectors.toList());
            result.addAll(l);
        }
        return result;
    }

    // this is a method to iterate over a list of lists of items
    // calling the predicate tester.test on each one
    // and return the first one that matches
    //TODO: make this public? (it is useful! ;-)
    private static Object findFirstMatchingItemInListOfLists(
            @Nonnull List<List> listOfListsOfObjects,
            @Nonnull Predicate<Object> tester) {
        Object result = null;
        for (List listOfObjects : listOfListsOfObjects) {
            Optional<Object> opt = listOfObjects.stream().filter(o -> tester.test(o)).findFirst();
            if (opt.isPresent()) {
                result = opt.get();
                break;
            }
        }
        return result;
    }

    private boolean checkControls(boolean useRectangles) {
        List<List> listOfLists = new ArrayList<>();
        listOfLists.add(turnoutList);
        listOfLists.add(slipList);
        listOfLists.add(turntableList);

        Object obj = findFirstMatchingItemInListOfLists(listOfLists,
                (Object o) -> {
                    LayoutTrack layoutTrack = (LayoutTrack) o;
                    selectedPointType = layoutTrack.findHitPointType(dLoc, useRectangles);
                    return (LayoutTrack.NONE != selectedPointType);
                }
        );
        if (null != obj) {
            if (obj instanceof LayoutTurntable) {
                LayoutTurntable layoutTurntable = (LayoutTurntable) obj;
                if (layoutTurntable.isConnectionType(selectedPointType)) {
                    try {
                        selectedObject = layoutTurntable.getConnection(selectedPointType);
                    } catch (jmri.JmriException e) {
                        // this should never happed because .isConnectionType will catch
                        // invalid connection types before .getConnection is called
                    }
                } else {
                    selectedPointType = LayoutTrack.NONE;
                }
            } else {
                selectedObject = obj;
            }
        }
        return (selectedObject != null);
    }

    // optional parameter avoid
    private boolean hitPointCheckLayoutTracks(@Nonnull Point2D loc, boolean requireUnconnected) {
        return hitPointCheckLayoutTracks(loc, requireUnconnected, null);
    }

    // optional parameter requireUnconnected
    private boolean hitPointCheckLayoutTracks(@Nonnull Point2D loc) {
        return hitPointCheckLayoutTracks(loc, false, null);
    }

    private boolean hitPointCheckLayoutTracks(@Nonnull Point2D loc,
            boolean requireUnconnected, @Nullable Object avoid) {
        boolean result = false; // assume failure (pessimist!)

        // these are all the types of objects we want to check
        List<List> listOfLists = new ArrayList<>();
        listOfLists.add(pointList);
        listOfLists.add(turnoutList);
        listOfLists.add(xingList);
        listOfLists.add(slipList);
        listOfLists.add(turntableList);
        listOfLists.add(trackList);

        foundPointType = LayoutTrack.NONE;
        Object obj = findFirstMatchingItemInListOfLists(listOfLists,
                (Object o) -> {
                    LayoutTrack layoutTrack = (LayoutTrack) o;
                    if ((layoutTrack != avoid) && (layoutTrack != selectedObject)) {
                        foundPointType = layoutTrack.findHitPointType(loc, false, requireUnconnected);
                    }
                    return (LayoutTrack.NONE != foundPointType);
                }
        );
        if (null != obj) {
            LayoutTrack layoutTrack = (LayoutTrack) obj;
            foundObject = layoutTrack;
            foundLocation = layoutTrack.getCoordsForConnectionType(foundPointType);
            foundNeedsConnect = layoutTrack.isDisconnected(foundPointType);
            result = true;
        }
        return result;
    } //hitPointCheckLayoutTracks

    private TrackSegment checkTrackSegmentPopUps(@Nonnull Point2D loc) {
        TrackSegment result = null;

        //NOTE: Rather than calculate all the hit rectangles for all
        // the points below and test if this location is in any of those
        // rectangles just create a hit rectangle for the location and
        // see if any of the points below are in it instead...
        Rectangle2D r = trackControlCircleRectAt(loc);

        //check Track Segments, if any
        for (TrackSegment ts : trackList) {
            if (r.contains(ts.getCentreSeg())) {
                result = ts;
                break;
            }
        }
        return result;
    } //checkTrackSegmentPopUps

    private PositionableLabel checkBackgroundPopUps(@Nonnull Point2D loc) {
        PositionableLabel result = null;
        //check background images, if any
        for (int i = backgroundImage.size() - 1; i >= 0; i--) {
            PositionableLabel b = backgroundImage.get(i);
            Rectangle2D r = b.getBounds();
            if (r.contains(loc)) {
                result = b;
                break;
            }
        }
        return result;
    } //checkBackgroundPopUps

    private SensorIcon checkSensorIconPopUps(@Nonnull Point2D loc) {
        SensorIcon result = null;
        //check sensor images, if any
        for (int i = sensorImage.size() - 1; i >= 0; i--) {
            SensorIcon s = sensorImage.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
            }
        }
        return result;
    } //checkSensorIconPopUps

    private SignalHeadIcon checkSignalHeadIconPopUps(@Nonnull Point2D loc) {
        SignalHeadIcon result = null;
        //check signal head images, if any
        for (int i = signalHeadImage.size() - 1; i >= 0; i--) {
            SignalHeadIcon s = signalHeadImage.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    } //checkSignalHeadIconPopUps

    private SignalMastIcon checkSignalMastIconPopUps(@Nonnull Point2D loc) {
        SignalMastIcon result = null;
        //check signal head images, if any
        for (int i = signalMastList.size() - 1; i >= 0; i--) {
            SignalMastIcon s = signalMastList.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    } //checkSignalMastIconPopUps

    private PositionableLabel checkLabelImagePopUps(@Nonnull Point2D loc) {
        PositionableLabel result = null;
        int level = 0;

        for (int i = labelImage.size() - 1; i >= 0; i--) {
            PositionableLabel s = labelImage.get(i);
            Rectangle2D r = s.getBounds();
            double x = s.getX();
            double y = s.getY();
            double w = 10.0;
            double h = 5.0;

            if (s.isIcon() || s.isRotated()) {
                w = s.maxWidth();
                h = s.maxHeight();
            } else if (s.isText()) {
                h = s.getFont().getSize();
                w = (h * 2 * (s.getText().length())) / 3;
            }
            r = new Rectangle2D.Double(x, y, w, h);

            if (r.contains(loc)) {
                if (s.getDisplayLevel() >= level) {
                    //Check to make sure that we are returning the highest level label.
                    result = s;
                    level = s.getDisplayLevel();
                }
            }
        }
        return result;
    } //checkLabelImagePopUps

    private AnalogClock2Display checkClockPopUps(@Nonnull Point2D loc) {
        AnalogClock2Display result = null;
        //check clocks, if any
        for (int i = clocks.size() - 1; i >= 0; i--) {
            AnalogClock2Display s = clocks.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    } //checkClockPopUps

    private MultiSensorIcon checkMultiSensorPopUps(@Nonnull Point2D loc) {
        MultiSensorIcon result = null;
        //check multi sensor icons, if any
        for (int i = multiSensors.size() - 1; i >= 0; i--) {
            MultiSensorIcon s = multiSensors.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    } //checkMultiSensorPopUps

    private LocoIcon checkMarkerPopUps(@Nonnull Point2D loc) {
        LocoIcon result = null;
        //check marker icons, if any
        for (int i = markerImage.size() - 1; i >= 0; i--) {
            LocoIcon l = markerImage.get(i);
            Rectangle2D r = l.getBounds();
            if (r.contains(loc)) {
                //mouse was pressed in marker icon
                result = l;
                break;
            }
        }
        return result;
    } //checkMarkerPopUps

    /**
     * get the coordinates for the connection type of the specified object
     *
     * @param o              the object (Layout track subclass)
     * @param connectionType the type of connection
     * @return the coordinates for the connection type of the specified object
     */
    public Point2D getCoords(@Nonnull Object o, int connectionType) {
        Point2D result = MathUtil.zeroPoint2D;
        if (o != null) {
            result = ((LayoutTrack) o).getCoordsForConnectionType(connectionType);
        } else {
            log.error("Null connection point of type {} {}", connectionType, getLayoutName());
        }
        return result;
    } //getCoords

    @Override
    public void mouseReleased(@Nonnull MouseEvent event) {
        super.setToolTip(null);

        //initialize mouse position
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));

            // released the mouse with shift down... see what we're adding
            if ((!event.isPopupTrigger()) && (!event.isMetaDown()) && event.isShiftDown()) {
                layoutEditorMode = LayoutEditorMode.ADDING_OBJECT;

                currentPoint = new Point2D.Double(xLoc, yLoc);

                if (snapToGridOnAdd != snapToGridInvert) {
                    // this snaps the current point to the grid
                    currentPoint = MathUtil.granulize(currentPoint, gridSize1st);
                    xLoc = (int) currentPoint.getX();
                    yLoc = (int) currentPoint.getY();
                    xLabel.setText(Integer.toString(xLoc));
                    yLabel.setText(Integer.toString(yLoc));
                }

                if (turnoutRHButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.RH_TURNOUT);
                } else if (turnoutLHButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.LH_TURNOUT);
                } else if (turnoutWYEButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.WYE_TURNOUT);
                } else if (doubleXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.DOUBLE_XOVER);
                } else if (rhXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.RH_XOVER);
                } else if (lhXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.LH_XOVER);
                } else if (levelXingButton.isSelected()) {
                    addLevelXing();
                } else if (layoutSingleSlipButton.isSelected()) {
                    addLayoutSlip(LayoutSlip.SINGLE_SLIP);
                } else if (layoutDoubleSlipButton.isSelected()) {
                    addLayoutSlip(LayoutSlip.DOUBLE_SLIP);
                } else if (endBumperButton.isSelected()) {
                    addEndBumper();
                } else if (anchorButton.isSelected()) {
                    addAnchor();
                } else if (edgeButton.isSelected()) {
                    addEdgeConnector();
                } else if (trackButton.isSelected()) {
                    if ((beginObject != null) && (foundObject != null)
                            && (beginObject != foundObject)) {
                        addTrackSegment();
                        setCursor(Cursor.getDefaultCursor());
                    }
                    beginObject = null;
                    foundObject = null;
                } else if (multiSensorButton.isSelected()) {
                    startMultiSensor();
                } else if (sensorButton.isSelected()) {
                    addSensor();
                } else if (signalButton.isSelected()) {
                    addSignalHead();
                } else if (textLabelButton.isSelected()) {
                    addLabel();
                } else if (memoryButton.isSelected()) {
                    addMemory();
                } else if (blockContentsButton.isSelected()) {
                    addBlockContents();
                } else if (iconLabelButton.isSelected()) {
                    addIcon();
                } else if (signalMastButton.isSelected()) {
                    addSignalMast();
                } else {
                    log.warn("No item selected in panel edit mode");
                }
                //resizePanelBounds(false);
                selectedObject = null;
                redrawPanel();
            } else if ((event.isPopupTrigger() || delayedPopupTrigger) && !isDragging) {
                layoutEditorMode = LayoutEditorMode.SHOW_POPUP;
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;
                whenReleased = event.getWhen();
                showEditPopUps(event);
            } else if ((selectedObject != null) && (selectedPointType == LayoutTrack.TURNOUT_CENTER)
                    && allControlling() && (!event.isMetaDown() && !event.isAltDown()) && (!event.isPopupTrigger())
                    && (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling turnouts, in edit mode
                layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
                LayoutTurnout t = (LayoutTurnout) selectedObject;
                t.toggleTurnout();
            } else if ((selectedObject != null) && ((selectedPointType == LayoutTrack.SLIP_LEFT)
                    || (selectedPointType == LayoutTrack.SLIP_RIGHT))
                    && allControlling() && (!event.isMetaDown() && !event.isAltDown()) && (!event.isPopupTrigger())
                    && (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling slips, in edit mode
                layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
                LayoutSlip sl = (LayoutSlip) selectedObject;
                sl.toggleState(selectedPointType);
            } else if ((selectedObject != null) && (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET)
                    && allControlling() && (!event.isMetaDown() && !event.isAltDown()) && (!event.isPopupTrigger())
                    && (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling turntable, in edit mode
                layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
                LayoutTurntable t = (LayoutTurntable) selectedObject;
                t.setPosition(selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
            } else if ((selectedObject != null) && ((selectedPointType == LayoutTrack.TURNOUT_CENTER)
                    || (selectedPointType == LayoutTrack.SLIP_CENTER)
                    || (selectedPointType == LayoutTrack.SLIP_LEFT)
                    || (selectedPointType == LayoutTrack.SLIP_RIGHT))
                    && allControlling() && (event.isMetaDown() && !event.isAltDown())
                    && (!event.isShiftDown()) && (!event.isControlDown()) && isDragging) {
                // We just dropped a turnout (or slip)... see if it will connect to anything
                layoutEditorMode = LayoutEditorMode.DROPPED_TURNOUT;
                hitPointCheckLayoutTurnouts((LayoutTurnout) selectedObject);
            } else if ((selectedObject != null) && (selectedPointType == LayoutTrack.POS_POINT)
                    && allControlling() && (event.isMetaDown() && !event.isAltDown())
                    && (!event.isShiftDown()) && (!event.isControlDown()) && isDragging) {
                // We just dropped a PositionablePoint... see if it will connect to anything
                layoutEditorMode = LayoutEditorMode.DROPPED_POSITIONABLE_POINT;
                PositionablePoint p = (PositionablePoint) selectedObject;
                if ((p.getConnect1() == null) || (p.getConnect2() == null)) {
                    checkPointOfPositionable(p);
                }
            }

            if ((trackButton.isSelected()) && (beginObject != null) && (foundObject != null)) {
                //user let up shift key before releasing the mouse when creating a track segment
                layoutEditorMode = LayoutEditorMode.UNKNOWN;
                setCursor(Cursor.getDefaultCursor());
                beginObject = null;
                foundObject = null;
                redrawPanel();
            }
            createSelectionGroups();
        } else if ((selectedObject != null) && (selectedPointType == LayoutTrack.TURNOUT_CENTER)
                && allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger())
                && (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            //controlling turnout out of edit mode
            layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
            LayoutTurnout t = (LayoutTurnout) selectedObject;
            if (useDirectTurnoutControl) {
                t.setState(jmri.Turnout.CLOSED);
            } else {
                t.toggleTurnout();
            }
        } else if ((selectedObject != null) && ((selectedPointType == LayoutTrack.SLIP_LEFT)
                || (selectedPointType == LayoutTrack.SLIP_RIGHT))
                && allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger())
                && (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            // controlling slip out of edit mode
            layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
            LayoutSlip sl = (LayoutSlip) selectedObject;
            sl.toggleState(selectedPointType);
        } else if ((selectedObject != null) && (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET)
                && allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger())
                && (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            // controlling turntable out of edit mode
            layoutEditorMode = LayoutEditorMode.CONTROLLING_TURNOUT;
            LayoutTurntable t = (LayoutTurntable) selectedObject;
            t.setPosition(selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
        } else if ((event.isPopupTrigger() || delayedPopupTrigger) && (!isDragging)) {
            //requesting marker popup out of edit mode
            layoutEditorMode = LayoutEditorMode.SHOW_POPUP;
            LocoIcon lo = checkMarkerPopUps(dLoc);
            if (lo != null) {
                showPopUp(lo, event);
            } else {
                if (hitPointCheckLayoutTracks(dLoc)) {
                    //show popup menu
                    switch (foundPointType) {
                        case LayoutTrack.TURNOUT_CENTER: {
                            if (useDirectTurnoutControl) {
                                LayoutTurnout t = (LayoutTurnout) foundObject;
                                t.setState(jmri.Turnout.THROWN);
                            } else {
                                ((LayoutTurnout) foundObject).showPopup(event);
                            }
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_CENTER: {
                            ((LevelXing) foundObject).showPopup(event);
                            break;
                        }

                        case LayoutTrack.SLIP_RIGHT:
                        case LayoutTrack.SLIP_LEFT: {
                            ((LayoutSlip) foundObject).showPopup(event);
                            break;
                        }

                        default: {
                            break;
                        }
                    } //switch
                }
                AnalogClock2Display c = checkClockPopUps(dLoc);
                if (c != null) {
                    showPopUp(c, event);
                } else {
                    SignalMastIcon sm = checkSignalMastIconPopUps(dLoc);
                    if (sm != null) {
                        showPopUp(sm, event);
                    } else {
                        PositionableLabel im = checkLabelImagePopUps(dLoc);

                        if (im != null) {
                            showPopUp(im, event);
                        }
                    }
                }
            }
        }

        if (!event.isPopupTrigger() && !isDragging) {
            List<Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                layoutEditorMode = LayoutEditorMode.SELECTION_MOUSE_RELEASED;
                selections.get(0).doMouseReleased(event);
                whenReleased = event.getWhen();
            }
        }

        //train icon needs to know when moved
        if (event.isPopupTrigger() && isDragging) {
            List<Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                layoutEditorMode = LayoutEditorMode.SELECTION_MOUSE_DRAGGED;
                selections.get(0).doMouseDragged(event);
            }
        }

        if (selectedObject != null) {
            //An object was selected, deselect it
            prevSelectedObject = selectedObject;
            selectedObject = null;
        }
        isDragging = false;
        delayedPopupTrigger = false;
        thisPanel.requestFocusInWindow();
    } //mouseReleased

    private void showEditPopUps(@Nonnull MouseEvent event) {
        if (hitPointCheckLayoutTracks(dLoc)) {
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    ((PositionablePoint) foundObject).showPopup(event);
                    break;
                }

                case LayoutTrack.TURNOUT_CENTER: {
                    ((LayoutTurnout) foundObject).showPopup(event);
                    break;
                }

                case LayoutTrack.LEVEL_XING_CENTER: {
                    ((LevelXing) foundObject).showPopup(event);
                    break;
                }

                case LayoutTrack.SLIP_LEFT:
                case LayoutTrack.SLIP_RIGHT: {
                    ((LayoutSlip) foundObject).showPopup(event);
                    break;
                }

                case LayoutTrack.TURNTABLE_CENTER: {
                    ((LayoutTurntable) foundObject).showPopup(event);
                    break;
                }
            } //switch

            if ((foundPointType >= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN)
                    && (foundPointType <= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MAX)) {
                ((TrackSegment) foundObject).showBezierPopUp(event, foundPointType);
            } else if (foundPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                LayoutTurntable t = (LayoutTurntable) foundObject;
                if (t.isTurnoutControlled()) {
                    ((LayoutTurntable) foundObject).showRayPopUp(event, foundPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
            }
        } else {
            do {
                TrackSegment ts = checkTrackSegmentPopUps(dLoc);
                if (ts != null) {
                    ts.showPopup(event);
                    break;
                }

                SensorIcon s = checkSensorIconPopUps(dLoc);
                if (s != null) {
                    showPopUp(s, event);
                    break;
                }

                LocoIcon lo = checkMarkerPopUps(dLoc);
                if (lo != null) {
                    showPopUp(lo, event);
                    break;
                }

                SignalHeadIcon sh = checkSignalHeadIconPopUps(dLoc);
                if (sh != null) {
                    showPopUp(sh, event);
                    break;
                }

                AnalogClock2Display c = checkClockPopUps(dLoc);
                if (c != null) {
                    showPopUp(c, event);
                    break;
                }

                MultiSensorIcon ms = checkMultiSensorPopUps(dLoc);
                if (ms != null) {
                    showPopUp(ms, event);
                    break;
                }

                PositionableLabel lb = checkLabelImagePopUps(dLoc);
                if (lb != null) {
                    showPopUp(lb, event);
                    break;
                }

                PositionableLabel b = checkBackgroundPopUps(dLoc);
                if (b != null) {
                    showPopUp(b, event);
                    break;
                }

                SignalMastIcon sm = checkSignalMastIconPopUps(dLoc);
                if (sm != null) {
                    showPopUp(sm, event);
                    break;
                }
            } while (false);
        } // if (hitPointCheckLayoutTracks(dLoc)) {...} else
    } //showEditPopUps

    /**
     * Select the menu items to display for the Positionable's popup
     */
    @Override
    protected void showPopUp(@Nonnull Positionable p, MouseEvent event) {
        if (!((JComponent) p).isVisible()) {
            return; //component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            JMenuItem jmi = null;

            if (showAlignPopup()) {
                setShowAlignmentMenu(popup);
                popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteSelectedItems();
                    }
                });
            } else {
                if (p.doViemMenu()) {
                    String objectType = p.getClass().getName();
                    objectType = objectType.substring(objectType.lastIndexOf(".") + 1);
                    jmi = popup.add(objectType);
                    jmi.setEnabled(false);

                    jmi = popup.add(p.getNameString());
                    jmi.setEnabled(false);

                    if (p.isPositionable()) {
                        setShowCoordinatesMenu(p, popup);
                    }
                    setDisplayLevelMenu(p, popup);
                    setPositionableMenu(p, popup);
                }

                boolean popupSet = false;
                popupSet |= p.setRotateOrthogonalMenu(popup);
                popupSet |= p.setRotateMenu(popup);
                if (popupSet) {
                    popup.addSeparator();
                    popupSet = false;
                }
                popupSet = p.setEditIconMenu(popup);
                popupSet = p.setTextEditMenu(popup);

                PositionablePopupUtil util = p.getPopupUtility();

                if (util != null) {
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

                //for Positionables with unique settings
                p.showPopUp(popup);
                setShowToolTipMenu(p, popup);

                setRemoveMenu(p, popup);

                if (p.doViemMenu()) {
                    setHiddenMenu(p, popup);
                }
            }
        } else {
            p.showPopUp(popup);
            PositionablePopupUtil util = p.getPopupUtility();

            if (util != null) {
                util.setAdditionalViewPopUpMenu(popup);
            }
        }
        popup.show((Component) p, p.getWidth() / 2 + (int) ((getPaintScale() - 1.0) * p.getX()),
                p.getHeight() / 2 + (int) ((getPaintScale() - 1.0) * p.getY()));

        /*popup.show((Component)p, event.getX(), event.getY());*/
    } //showPopUp()

    private long whenReleased = 0; //used to identify event that was popup trigger
    private boolean awaitingIconChange = false;

    @Override
    public void mouseClicked(@Nonnull MouseEvent event) {

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if ((!event.isMetaDown()) && (!event.isPopupTrigger()) && (!event.isAltDown())
                && (!awaitingIconChange) && (!event.isShiftDown()) && (!event.isControlDown())) {
            calcLocation(event);
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                layoutEditorMode = LayoutEditorMode.SELECTION_MOUSE_CLICKED;
                selections.get(0).doMouseClicked(event);
            }
        } else if (event.isPopupTrigger() && (whenReleased != event.getWhen())) {
            calcLocation(event);

            if (isEditable()) {
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;
                showEditPopUps(event);
            } else {
                LocoIcon lo = checkMarkerPopUps(dLoc);

                if (lo != null) {
                    showPopUp(lo, event);
                }
            }
        }

        if (event.isControlDown() && !event.isPopupTrigger()) {
            if (hitPointCheckLayoutTracks(dLoc)) {
                switch (foundPointType) {
                    case LayoutTrack.POS_POINT: {
                        amendSelectionGroup((PositionablePoint) foundObject);
                        break;
                    }

                    case LayoutTrack.TURNOUT_CENTER: {
                        amendSelectionGroup((LayoutTurnout) foundObject, dLoc);
                        break;
                    }

                    case LayoutTrack.LEVEL_XING_CENTER: {
                        amendSelectionGroup((LevelXing) foundObject);
                        break;
                    }

                    case LayoutTrack.SLIP_LEFT:
                    case LayoutTrack.SLIP_RIGHT: {
                        amendSelectionGroup((LayoutSlip) foundObject);
                        break;
                    }

                    case LayoutTrack.TURNTABLE_CENTER: {
                        amendSelectionGroup((LayoutTurntable) foundObject);
                        break;
                    }

                    default: {
                        break;
                    }
                } //switch
            } else {
                PositionableLabel s = checkSensorIconPopUps(dLoc);

                if (s != null) {
                    amendSelectionGroup(s);
                } else {
                    PositionableLabel sh = checkSignalHeadIconPopUps(dLoc);

                    if (sh != null) {
                        amendSelectionGroup(sh);
                    } else {
                        PositionableLabel ms = checkMultiSensorPopUps(dLoc);

                        if (ms != null) {
                            amendSelectionGroup(ms);
                        } else {
                            PositionableLabel lb = checkLabelImagePopUps(dLoc);

                            if (lb != null) {
                                amendSelectionGroup(lb);
                            } else {
                                PositionableLabel b = checkBackgroundPopUps(dLoc);

                                if (b != null) {
                                    amendSelectionGroup(b);
                                } else {
                                    PositionableLabel sm = checkSignalMastIconPopUps(dLoc);

                                    if (sm != null) {
                                        amendSelectionGroup(sm);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if ((selectionWidth == 0) || (selectionHeight == 0)) {
            clearSelectionGroups();
        }
        thisPanel.requestFocusInWindow();
    } //mouseClicked

    private void checkPointOfPositionable(@Nonnull PositionablePoint p) {
        TrackSegment t = p.getConnect1();

        if (t == null) {
            t = p.getConnect2();
        }

        //Nothing connected to this bit of track so ignore
        if (t == null) {
            return;
        }
        beginObject = p;
        beginPointType = LayoutTrack.POS_POINT;
        Point2D loc = p.getCoordsCenter();

        if (hitPointCheckLayoutTracks(loc, true, p)) {
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundObject;

                    if ((p2.getType() == PositionablePoint.ANCHOR) && p2.setTrackConnection(t)) {
                        if (t.getConnect1() == p) {
                            t.setNewConnect1(p2, foundPointType);
                        } else {
                            t.setNewConnect2(p2, foundPointType);
                        }
                        p.removeTrackConnection(t);

                        if ((p.getConnect1() == null) && (p.getConnect2() == null)) {
                            removePositionablePoint(p);
                        }
                    }
                    break;
                }
                case LayoutTrack.TURNOUT_A:
                case LayoutTrack.TURNOUT_B:
                case LayoutTrack.TURNOUT_C:
                case LayoutTrack.TURNOUT_D:
                case LayoutTrack.LEVEL_XING_A:
                case LayoutTrack.LEVEL_XING_B:
                case LayoutTrack.LEVEL_XING_C:
                case LayoutTrack.LEVEL_XING_D:
                case LayoutTrack.SLIP_A:
                case LayoutTrack.SLIP_B:
                case LayoutTrack.SLIP_C:
                case LayoutTrack.SLIP_D: {
                    LayoutTrack lt = (LayoutTrack) foundObject;
                    try {
                        if (lt.getConnection(foundPointType) == null) {
                            lt.setConnection(foundPointType, t, LayoutTrack.TRACK);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(lt, foundPointType);
                            } else {
                                t.setNewConnect2(lt, foundPointType);
                            }
                            p.removeTrackConnection(t);

                            if ((p.getConnect1() == null) && (p.getConnect2() == null)) {
                                removePositionablePoint(p);
                            }
                        }
                    } catch (jmri.JmriException e) {
                        log.debug("Unable to set location");
                    }
                    break;
                }

                default: {
                    if (foundPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                        LayoutTurntable tt = (LayoutTurntable) foundObject;
                        int ray = foundPointType - LayoutTrack.TURNTABLE_RAY_OFFSET;

                        if (tt.getRayConnectIndexed(ray) == null) {
                            tt.setRayConnect(t, ray);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(tt, foundPointType);
                            } else {
                                t.setNewConnect2(tt, foundPointType);
                            }
                            p.removeTrackConnection(t);

                            if ((p.getConnect1() == null) && (p.getConnect2() == null)) {
                                removePositionablePoint(p);
                            }
                        }
                    } else {
                        log.debug("No valid point, so will quit");
                        return;
                    }
                    break;
                }
            } //switch
            redrawPanel();

            if (t.getLayoutBlock() != null) {
                auxTools.setBlockConnectivityChanged();
            }
        }
        beginObject = null;
        foundObject = null;
    } //checkPointOfPositionable

    // We just dropped a turnout... see if it will connect to anything
    private void hitPointCheckLayoutTurnouts(@Nonnull LayoutTurnout lt) {
        beginObject = lt;

        if (lt.getConnectA() == null) {
            if (lt instanceof LayoutSlip) {
                beginPointType = LayoutTrack.SLIP_A;
            } else {
                beginPointType = LayoutTrack.TURNOUT_A;
            }
            dLoc = lt.getCoordsA();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if (lt.getConnectB() == null) {
            if (lt instanceof LayoutSlip) {
                beginPointType = LayoutTrack.SLIP_B;
            } else {
                beginPointType = LayoutTrack.TURNOUT_B;
            }
            dLoc = lt.getCoordsB();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if (lt.getConnectC() == null) {
            if (lt instanceof LayoutSlip) {
                beginPointType = LayoutTrack.SLIP_C;
            } else {
                beginPointType = LayoutTrack.TURNOUT_C;
            }
            dLoc = lt.getCoordsC();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if ((lt.getConnectD() == null) && ((lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                || (lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                || (lt.getTurnoutType() == LayoutTurnout.RH_XOVER)
                || (lt.getTurnoutType() == LayoutTurnout.SINGLE_SLIP)
                || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_SLIP))) {
            if (lt instanceof LayoutSlip) {
                beginPointType = LayoutTrack.SLIP_D;
            } else {
                beginPointType = LayoutTrack.TURNOUT_D;
            }
            dLoc = lt.getCoordsD();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }
        beginObject = null;
        foundObject = null;
    } //hitPointCheckLayoutTurnouts

    private void hitPointCheckLayoutTurnoutSubs(@Nonnull Point2D dLoc) {
        if (hitPointCheckLayoutTracks(dLoc, true)) {
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundObject;

                    if (((p2.getConnect1() == null) && (p2.getConnect2() != null))
                            || ((p2.getConnect1() != null) && (p2.getConnect2() == null))) {
                        TrackSegment t = p2.getConnect1();

                        if (t == null) {
                            t = p2.getConnect2();
                        }

                        if (t == null) {
                            return;
                        }
                        LayoutTurnout lt = (LayoutTurnout) beginObject;
                        try {
                            if (lt.getConnection(beginPointType) == null) {
                                lt.setConnection(beginPointType, t, LayoutTrack.TRACK);
                                p2.removeTrackConnection(t);

                                if (t.getConnect1() == p2) {
                                    t.setNewConnect1(lt, beginPointType);
                                } else {
                                    t.setNewConnect2(lt, beginPointType);
                                }
                                removePositionablePoint(p2);
                            }

                            if (t.getLayoutBlock() != null) {
                                auxTools.setBlockConnectivityChanged();
                            }
                        } catch (jmri.JmriException e) {
                            log.debug("Unable to set location");
                        }
                    }
                    break;
                }

                case LayoutTrack.TURNOUT_A:
                case LayoutTrack.TURNOUT_B:
                case LayoutTrack.TURNOUT_C:
                case LayoutTrack.TURNOUT_D:
                case LayoutTrack.SLIP_A:
                case LayoutTrack.SLIP_B:
                case LayoutTrack.SLIP_C:
                case LayoutTrack.SLIP_D: {
                    LayoutTurnout ft = (LayoutTurnout) foundObject;
                    addTrackSegment();

                    if ((ft.getTurnoutType() == LayoutTurnout.RH_TURNOUT) || (ft.getTurnoutType() == LayoutTurnout.LH_TURNOUT)) {
                        rotateTurnout(ft);
                    }
                    break;
                }

                default: {
                    log.warn("Unexpected foundPointType {} in hitPointCheckLayoutTurnoutSubs", foundPointType);
                    break;
                }
            } //switch
        }   // if (hitPointCheckLayoutTracks(dLoc, true))
    } //hitPointCheckLayoutTurnoutSubs

    private void rotateTurnout(@Nonnull LayoutTurnout t) {
        LayoutTurnout be = (LayoutTurnout) beginObject;

        if (((beginPointType == LayoutTrack.TURNOUT_A) && ((be.getConnectB() != null) || (be.getConnectC() != null)))
                || ((beginPointType == LayoutTrack.TURNOUT_B) && ((be.getConnectA() != null) || (be.getConnectC() != null)))
                || ((beginPointType == LayoutTrack.TURNOUT_C) && ((be.getConnectB() != null) || (be.getConnectA() != null)))) {
            return;
        }

        if ((be.getTurnoutType() != LayoutTurnout.RH_TURNOUT) && (be.getTurnoutType() != LayoutTurnout.LH_TURNOUT)) {
            return;
        }

        double x2;
        double y2;

        Point2D c;
        Point2D diverg;

        if ((foundPointType == LayoutTrack.TURNOUT_C) && (beginPointType == LayoutTrack.TURNOUT_C)) {
            c = t.getCoordsA();
            diverg = t.getCoordsB();
            x2 = be.getCoordsA().getX() - be.getCoordsB().getX();
            y2 = be.getCoordsA().getY() - be.getCoordsB().getY();
        } else if ((foundPointType == LayoutTrack.TURNOUT_C)
                && ((beginPointType == LayoutTrack.TURNOUT_A) || (beginPointType == LayoutTrack.TURNOUT_B))) {
            c = t.getCoordsCenter();
            diverg = t.getCoordsC();

            if (beginPointType == LayoutTrack.TURNOUT_A) {
                x2 = be.getCoordsB().getX() - be.getCoordsA().getX();
                y2 = be.getCoordsB().getY() - be.getCoordsA().getY();
            } else {
                x2 = be.getCoordsA().getX() - be.getCoordsB().getX();
                y2 = be.getCoordsA().getY() - be.getCoordsB().getY();
            }
        } else if (foundPointType == LayoutTrack.TURNOUT_B) {
            c = t.getCoordsA();
            diverg = t.getCoordsB();

            if (beginPointType == LayoutTrack.TURNOUT_B) {
                x2 = be.getCoordsA().getX() - be.getCoordsB().getX();
                y2 = be.getCoordsA().getY() - be.getCoordsB().getY();
            } else if (beginPointType == LayoutTrack.TURNOUT_A) {
                x2 = be.getCoordsB().getX() - be.getCoordsA().getX();
                y2 = be.getCoordsB().getY() - be.getCoordsA().getY();
            } else { //(beginPointType==TURNOUT_C){
                x2 = be.getCoordsCenter().getX() - be.getCoordsC().getX();
                y2 = be.getCoordsCenter().getY() - be.getCoordsC().getY();
            }
        } else if (foundPointType == LayoutTrack.TURNOUT_A) {
            c = t.getCoordsA();
            diverg = t.getCoordsB();

            if (beginPointType == LayoutTrack.TURNOUT_A) {
                x2 = be.getCoordsA().getX() - be.getCoordsB().getX();
                y2 = be.getCoordsA().getY() - be.getCoordsB().getY();
            } else if (beginPointType == LayoutTrack.TURNOUT_B) {
                x2 = be.getCoordsB().getX() - be.getCoordsA().getX();
                y2 = be.getCoordsB().getY() - be.getCoordsA().getY();
            } else { //(beginPointType==TURNOUT_C){
                x2 = be.getCoordsC().getX() - be.getCoordsCenter().getX();
                y2 = be.getCoordsC().getY() - be.getCoordsCenter().getY();
            }
        } else {
            return;
        }
        double x = diverg.getX() - c.getX();
        double y = diverg.getY() - c.getY();
        double radius = Math.toDegrees(Math.atan2(y, x));
        double eRadius = Math.toDegrees(Math.atan2(y2, x2));
        be.rotateCoords(radius - eRadius);

        Point2D conCord = be.getCoordsA();
        Point2D tCord = t.getCoordsC();

        if (foundPointType == LayoutTrack.TURNOUT_B) {
            tCord = t.getCoordsB();
        }

        if (foundPointType == LayoutTrack.TURNOUT_A) {
            tCord = t.getCoordsA();
        }

        if (beginPointType == LayoutTrack.TURNOUT_B) {
            conCord = be.getCoordsB();
        } else if (beginPointType == LayoutTrack.TURNOUT_C) {
            conCord = be.getCoordsC();
        } else if (beginPointType == LayoutTrack.TURNOUT_A) {
            conCord = be.getCoordsA();
        }
        x = conCord.getX() - tCord.getX();
        y = conCord.getY() - tCord.getY();
        Point2D offset = new Point2D.Double(be.getCoordsCenter().getX() - x, be.getCoordsCenter().getY() - y);
        be.setCoordsCenter(offset);

    } //rotateTurnout

    static class TurnoutSelection {

        boolean pointA = false;
        boolean pointB = false;
        boolean pointC = false;
        boolean pointD = false;

        TurnoutSelection() {
        }

        void setPointA(boolean boo) {
            pointA = boo;
        }

        void setPointB(boolean boo) {
            pointB = boo;
        }

        void setPointC(boolean boo) {
            pointC = boo;
        }

        void setPointD(boolean boo) {
            pointD = boo;
        }

        boolean getPointA() {
            return pointA;
        }

        boolean getPointB() {
            return pointB;
        }

        boolean getPointC() {
            return pointC;
        }

        boolean getPointD() {
            return pointD;
        }
    }

    private ArrayList<Positionable> _positionableSelection = null;
    private ArrayList<PositionablePoint> _pointSelection = null;
    private ArrayList<LayoutTurnout> _turnoutSelection = null;
    private ArrayList<LayoutSlip> _slipSelection = null;
    private ArrayList<LevelXing> _xingSelection = null;
    private ArrayList<LayoutTurntable> _turntableSelection = null;

    private void highLightSelection(@Nonnull Graphics2D g) {
        java.awt.Stroke stroke = g.getStroke();
        Color color = g.getColor();
        g.setColor(new Color(204, 207, 88));
        g.setStroke(new java.awt.BasicStroke(2.0f));

        if (_positionableSelection != null) {
            for (Positionable c : _positionableSelection) {
                g.drawRect(c.getX(), c.getY(), c.maxWidth(), c.maxHeight());
            }
        }

        List<List> listOfLists = new ArrayList<>();
        if (_xingSelection != null) {
            listOfLists.add(_xingSelection);
        }
        if (_slipSelection != null) {
            listOfLists.add(_slipSelection);
        }
        if (_turntableSelection != null) {
            listOfLists.add(_turntableSelection);
        }
        if (_pointSelection != null) {
            listOfLists.add(_pointSelection);
        }
        if (_turnoutSelection != null) {
            listOfLists.add(_turnoutSelection);
        }
        for (List<LayoutTrack> l : listOfLists) {
            for (LayoutTrack lt : l) {
                Rectangle2D r = lt.getBounds();
                if (r.isEmpty()) {
                    r = MathUtil.inset(r, -4.0);
                }
                r = MathUtil.centerRectangleOnPoint(r, lt.getCoordsCenter());
                g.draw(r);
            }
        }

        g.setColor(color);
        g.setStroke(stroke);
    } //highLightSelection

    protected void createSelectionGroups() {
        List<Positionable> contents = getContents();
        Rectangle2D selectionRect = getSelectionRect();

        for (Positionable c : contents) {
            if (selectionRect.contains(c.getLocation())) {
                if (_positionableSelection == null) {
                    _positionableSelection = new ArrayList<>();
                }

                if (!_positionableSelection.contains(c)) {
                    _positionableSelection.add(c);
                }
            }
        }

        // loop over all turnouts
        for (LayoutTurnout t : turnoutList) {
            Point2D center = t.getCoordsCenter();

            if (selectionRect.contains(center)) {
                if (_turnoutSelection == null) {
                    _turnoutSelection = new ArrayList<>();
                }

                if (!_turnoutSelection.contains(t)) {
                    _turnoutSelection.add(t);
                }

            }
        } // for (LayoutTurnout t : turnoutList)

        // loop over all level crossings
        for (LevelXing x : xingList) {
            Point2D center = x.getCoordsCenter();

            if (selectionRect.contains(center)) {
                if (_xingSelection == null) {
                    _xingSelection = new ArrayList<>();
                }

                if (!_xingSelection.contains(x)) {
                    _xingSelection.add(x);
                }
            }
        }

        // loop over all slips
        for (LayoutSlip sl : slipList) {
            Point2D center = sl.getCoordsCenter();

            if (selectionRect.contains(center)) {
                if (_slipSelection == null) {
                    _slipSelection = new ArrayList<>();
                }

                if (!_slipSelection.contains(sl)) {
                    _slipSelection.add(sl);
                }
            }
        }

        // loop over all turntables
        for (LayoutTurntable x : turntableList) {
            Point2D center = x.getCoordsCenter();

            if (selectionRect.contains(center)) {
                if (_turntableSelection == null) {
                    _turntableSelection = new ArrayList<>();
                }

                if (!_turntableSelection.contains(x)) {
                    _turntableSelection.add(x);
                }
            }
        }

        // loop over all Anchor Points and End Bumpers
        for (PositionablePoint p : pointList) {
            Point2D coord = p.getCoordsCenter();

            if (selectionRect.contains(coord)) {
                if (_pointSelection == null) {
                    _pointSelection = new ArrayList<>();
                }

                if (!_pointSelection.contains(p)) {
                    _pointSelection.add(p);
                }
            }
        }
        redrawPanel();
    } //createSelectionGroups

    protected void clearSelectionGroups() {
        _pointSelection = null;
        _turntableSelection = null;
        _xingSelection = null;
        _slipSelection = null;
        _turnoutSelection = null;
        _positionableSelection = null;
    } //clearSelectionGroups

    boolean noWarnGlobalDelete = false;

    private void deleteSelectedItems() {
        if (!noWarnGlobalDelete) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question6"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return; //return without creating if "No" response
            }

            if (selectedValue == 2) {
                //Suppress future warnings, and continue
                noWarnGlobalDelete = true;
            }
        }

        if (_positionableSelection != null) {
            for (Positionable comp : _positionableSelection) {
                remove(comp);
            }
        }

        if (_pointSelection != null) {
            boolean oldPosPoint = noWarnPositionablePoint;
            noWarnPositionablePoint = true;

            for (PositionablePoint point : _pointSelection) {
                removePositionablePoint(point);
            }
            noWarnPositionablePoint = oldPosPoint;
        }

        if (_xingSelection != null) {
            boolean oldLevelXing = noWarnLevelXing;
            noWarnLevelXing = true;

            for (LevelXing point : _xingSelection) {
                removeLevelXing(point);
            }
            noWarnLevelXing = oldLevelXing;
        }

        if (_slipSelection != null) {
            boolean oldSlip = noWarnSlip;
            noWarnSlip = true;

            for (LayoutSlip sl : _slipSelection) {
                removeLayoutSlip(sl);
            }
            noWarnSlip = oldSlip;
        }

        if (_turntableSelection != null) {
            boolean oldTurntable = noWarnTurntable;
            noWarnTurntable = true;

            for (LayoutTurntable point : _turntableSelection) {
                removeTurntable(point);
            }
            noWarnTurntable = oldTurntable;
        }

        if (_turnoutSelection != null) {
            boolean oldTurnout = noWarnLayoutTurnout;
            noWarnLayoutTurnout = true;

            for (LayoutTurnout lt : _turnoutSelection) {
                removeLayoutTurnout(lt);
            }
            noWarnLayoutTurnout = oldTurnout;
        }

        selectionActive = false;
        clearSelectionGroups();
        redrawPanel();
    } //deleteSelectedItems

    private void amendSelectionGroup(@Nonnull Positionable p) {
        if (_positionableSelection == null) {
            _positionableSelection = new ArrayList<>();
        }
        boolean removed = false;

        for (int i = 0; i < _positionableSelection.size(); i++) {
            if (_positionableSelection.get(i) == p) {
                _positionableSelection.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _positionableSelection.add(p);
        }

        if (_positionableSelection.size() == 0) {
            _positionableSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    private void amendSelectionGroup(@Nonnull LayoutTurnout p, @Nonnull Point2D dLoc) {
        if (_turnoutSelection == null) {
            _turnoutSelection = new ArrayList<>();
        }

        boolean removed = false;
        for (LayoutTurnout lt : _turnoutSelection) {
            if (lt == p) {
                _turnoutSelection.remove(lt);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _turnoutSelection.add(p);
        }
        if (_turnoutSelection.isEmpty()) {
            _turnoutSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    private void amendSelectionGroup(@Nonnull PositionablePoint p) {
        if (_pointSelection == null) {
            _pointSelection = new ArrayList<>();
        }
        boolean removed = false;

        for (int i = 0; i < _pointSelection.size(); i++) {
            if (_pointSelection.get(i) == p) {
                _pointSelection.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _pointSelection.add(p);
        }

        if (_pointSelection.size() == 0) {
            _pointSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    private void amendSelectionGroup(@Nonnull LevelXing p) {
        if (_xingSelection == null) {
            _xingSelection = new ArrayList<>();
        }
        boolean removed = false;

        for (int i = 0; i < _xingSelection.size(); i++) {
            if (_xingSelection.get(i) == p) {
                _xingSelection.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _xingSelection.add(p);
        }

        if (_xingSelection.size() == 0) {
            _xingSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    private void amendSelectionGroup(@Nonnull LayoutSlip p) {
        if (_slipSelection == null) {
            _slipSelection = new ArrayList<>();
        }
        boolean removed = false;

        for (int i = 0; i < _slipSelection.size(); i++) {
            if (_slipSelection.get(i) == p) {
                _slipSelection.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _slipSelection.add(p);
        }

        if (_slipSelection.size() == 0) {
            _slipSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    private void amendSelectionGroup(@Nonnull LayoutTurntable p) {
        if (_turntableSelection == null) {
            _turntableSelection = new ArrayList<>();
        }
        boolean removed = false;

        for (int i = 0; i < _turntableSelection.size(); i++) {
            if (_turntableSelection.get(i) == p) {
                _turntableSelection.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            _turntableSelection.add(p);
        }

        if (_turntableSelection.size() == 0) {
            _turntableSelection = null;
        }
        redrawPanel();
    } //amendSelectionGroup

    public void alignSelection(boolean alignX) {
        Point2D minPoint = MathUtil.infinityPoint2D;
        Point2D maxPoint = MathUtil.zeroPoint2D;
        Point2D sumPoint = MathUtil.zeroPoint2D;
        int cnt = 0;

        if (_positionableSelection != null) {
            for (Positionable comp : _positionableSelection) {
                if (!getFlag(Editor.OPTION_POSITION, comp.isPositionable())) {
                    continue;
                }
                Point2D p = MathUtil.pointToPoint2D(comp.getLocation());
                minPoint = MathUtil.min(minPoint, p);
                maxPoint = MathUtil.max(maxPoint, p);
                sumPoint = MathUtil.add(sumPoint, p);
                cnt++;
            }
        }

        List<List> listOfLists = new ArrayList<>();

        if (_turnoutSelection != null) {
            listOfLists.add(_turnoutSelection);
        }

        if (_pointSelection != null) {
            listOfLists.add(_pointSelection);
        }

        if (_xingSelection != null) {
            listOfLists.add(_xingSelection);
        }

        if (_slipSelection != null) {
            listOfLists.add(_slipSelection);
        }

        if (_turntableSelection != null) {
            listOfLists.add(_slipSelection);
        }

        for (List<LayoutTrack> l : listOfLists) {
            for (LayoutTrack lt : l) {
                Point2D p = lt.getCoordsCenter();
                minPoint = MathUtil.min(minPoint, p);
                maxPoint = MathUtil.max(maxPoint, p);
                sumPoint = MathUtil.add(sumPoint, p);
                cnt++;
            }
        }

        Point2D avePoint = MathUtil.divide(sumPoint, cnt);
        int aveX = (int) avePoint.getX();
        int aveY = (int) avePoint.getY();

        if (_positionableSelection != null) {
            for (Positionable comp : _positionableSelection) {
                if (!getFlag(Editor.OPTION_POSITION, comp.isPositionable())) {
                    continue;
                }

                if (alignX) {
                    comp.setLocation(aveX, comp.getY());
                } else {
                    comp.setLocation(comp.getX(), aveY);
                }
            }
        }

        for (List<LayoutTrack> l : listOfLists) {
            for (LayoutTrack lt : l) {
                if (alignX) {
                    lt.setCoordsCenter(new Point2D.Double(aveX, lt.getCoordsCenter().getY()));
                } else {
                    lt.setCoordsCenter(new Point2D.Double(lt.getCoordsCenter().getX(), aveY));
                }
            }
        }

        redrawPanel();
    } //alignSelection

    protected boolean showAlignPopup() {
        if (_positionableSelection != null) {
            return true;
        } else if (_pointSelection != null) {
            return true;
        } else if (_turnoutSelection != null) {
            return true;
        } else if (_turntableSelection != null) {
            return true;
        } else if (_xingSelection != null) {
            return true;
        } else if (_slipSelection != null) {
            return true;
        }
        return false;
    } //showAlignPopup

    /**
     * Offer actions to align the selected Positionable items either
     * Horizontally (at average y coord) or Vertically (at average x coord).
     */
    public boolean setShowAlignmentMenu(@Nonnull JPopupMenu popup) {
        if (showAlignPopup()) {
            JMenu edit = new JMenu(Bundle.getMessage("EditAlignment"));
            edit.add(new AbstractAction(Bundle.getMessage("AlignX")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alignSelection(true);
                }
            });
            edit.add(new AbstractAction(Bundle.getMessage("AlignY")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alignSelection(false);
                }
            });
            popup.add(edit);

            return true;
        }
        return false;
    } //setShowAlignmentMenu

    @Override
    public void keyPressed(@Nonnull KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedItems();
            return;
        }

        double deltaX = returnDeltaPositionX(e);
        double deltaY = returnDeltaPositionY(e);

        if ((deltaX != 0) || (deltaY != 0)) {
            Point2D delta = new Point2D.Double(deltaX, deltaY);
            if (_positionableSelection != null) {
                for (Positionable c : _positionableSelection) {
                    Point2D newPoint = c.getLocation();
                    if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth() == 0)) {
                        MemoryIcon pm = (MemoryIcon) c;
                        newPoint = new Point2D.Double(pm.getOriginalX(), pm.getOriginalY());
                    }
                    newPoint = MathUtil.add(newPoint, delta);
                    newPoint = MathUtil.max(MathUtil.zeroPoint2D, newPoint);
                    c.setLocation(MathUtil.point2DToPoint(newPoint));
                }
            }

            List<List> listOfLists = new ArrayList<>();

            if (_pointSelection != null) {
                listOfLists.add(_pointSelection);
            }

            if (_turnoutSelection != null) {
                listOfLists.add(_turnoutSelection);
            }

            if (_xingSelection != null) {
                listOfLists.add(_xingSelection);
            }

            if (_slipSelection != null) {
                listOfLists.add(_slipSelection);
            }

            if (_turntableSelection != null) {
                listOfLists.add(_slipSelection);
            }

            for (List<LayoutTrack> l : listOfLists) {
                for (LayoutTrack lt : l) {
                    Point2D newPoint = MathUtil.add(lt.getCoordsCenter(), delta);
                    newPoint = MathUtil.max(MathUtil.zeroPoint2D, newPoint);
                    lt.setCoordsCenter(newPoint);
                }
            }

            redrawPanel();
        }
    } //keyPressed

    private double returnDeltaPositionX(@Nonnull KeyEvent e) {
        double result = 0.0;
        double amount = e.isShiftDown() ? 5.0 : 1.0;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                result = -amount;
                break;
            }

            case KeyEvent.VK_RIGHT: {
                result = +amount;
                break;
            }

            default: {
                break;
            }
        } //switch
        return result;
    }

    private double returnDeltaPositionY(@Nonnull KeyEvent e) {
        double result = 0.0;
        double amount = e.isShiftDown() ? 5.0 : 1.0;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: {
                result = -amount;
                break;
            }

            case KeyEvent.VK_DOWN: {
                result = +amount;
                break;
            }

            default: {
                break;
            }
        } //switch
        return result;
    }

    private double returnNewPostitionX(@Nonnull KeyEvent e, double val) {
        double deltaX = returnDeltaPositionX(e);
        return Math.max(val + deltaX, 0.0);
    } //returnNewPostitionX

    private double returnNewPostitionY(@Nonnull KeyEvent e, double val) {
        double deltaY = returnDeltaPositionY(e);
        return Math.max(val + deltaY, 0.0);
    } //returnNewPostitionY

    int _prevNumSel = 0;

    @Override
    public void mouseMoved(@Nonnull MouseEvent event) {
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
        List<Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        int numSel = selections.size();

        if (numSel > 0) {
            selection = selections.get(0);
        }

        if ((selection != null) && (selection.getDisplayLevel() > Editor.BKG) && selection.showToolTip()) {
            showToolTip(selection, event);
        } else {
            super.setToolTip(null);
        }

        if (numSel != _prevNumSel) {
            redrawPanel();
            _prevNumSel = numSel;
        }
    } //mouseMoved

    private boolean isDragging = false;

    @Override
    public void mouseDragged(@Nonnull MouseEvent event) {
        //initialize mouse position
        calcLocation(event);

        //ignore this event if still at the original point
        if ((!isDragging) && (xLoc == getAnchorX()) && (yLoc == getAnchorY())) {
            return;
        }

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        //process this mouse dragged event
        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
        currentPoint = new Point2D.Double(dLoc.getX() + startDelta.getX(),
                dLoc.getY() + startDelta.getY());

        if ((selectedObject != null) && (event.isMetaDown() || event.isAltDown() || event.isAltDown()) && (selectedPointType == LayoutTrack.MARKER)) {
            //marker moves regardless of editMode or positionable
            PositionableLabel pl = (PositionableLabel) selectedObject;
            //don't allow negative placement, object could become unreachable
            int xNew = (int) Math.max(currentPoint.getX(), 0);
            int yNew = (int) Math.max(currentPoint.getY(), 0);
            pl.setLocation(xNew, yNew);
            isDragging = true;
            redrawPanel();

            return;
        }

        if (isEditable()) {
            if ((selectedObject != null) && event.isMetaDown() && allPositionable()) {
                //moving a point
                if (snapToGridOnMove != snapToGridInvert) {
                    // this snaps currentPoint to the grid
                    currentPoint = MathUtil.granulize(currentPoint, gridSize1st);
                    xLoc = (int) currentPoint.getX();
                    yLoc = (int) currentPoint.getY();
                    xLabel.setText(Integer.toString(xLoc));
                    yLabel.setText(Integer.toString(yLoc));
                }

                if ((_pointSelection != null) || (_turntableSelection != null)
                        || (_xingSelection != null)
                        || (_turnoutSelection != null)
                        || (_positionableSelection != null)) {
                    int offsetx = xLoc - _lastX;
                    int offsety = yLoc - _lastY;

                    //We should do a move based upon a selection group.
                    int xNew;
                    int yNew;

                    if (_positionableSelection != null) {
                        for (Positionable c : _positionableSelection) {
                            if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth() == 0)) {
                                MemoryIcon pm = (MemoryIcon) c;
                                xNew = (pm.getOriginalX() + offsetx);
                                yNew = (pm.getOriginalY() + offsety);
                            } else {
                                Point2D upperLeft = c.getLocation();
                                xNew = (int) (upperLeft.getX() + offsetx);
                                yNew = (int) (upperLeft.getY() + offsety);
                            }
                            //don't allow negative placement, object could become unreachable
                            xNew = Math.max(xNew, 0);
                            yNew = Math.max(yNew, 0);
                            c.setLocation(xNew, yNew);
                        }
                    }

                    List<List> listOfLists = new ArrayList<>();

                    if (_pointSelection != null) {
                        listOfLists.add(_pointSelection);
                    }

                    if (_turnoutSelection != null) {
                        listOfLists.add(_turnoutSelection);
                    }

                    if (_xingSelection != null) {
                        listOfLists.add(_xingSelection);
                    }

                    if (_slipSelection != null) {
                        listOfLists.add(_slipSelection);
                    }

                    if (_turntableSelection != null) {
                        listOfLists.add(_slipSelection);
                    }

                    for (List<LayoutTrack> l : listOfLists) {
                        for (LayoutTrack lt : l) {
                            Point2D center = lt.getCoordsCenter();
                            xNew = (int) center.getX() + offsetx;
                            yNew = (int) center.getY() + offsety;
                            //don't allow negative placement, object could become unreachable
                            xNew = Math.max(xNew, 0);
                            yNew = Math.max(yNew, 0);
                            lt.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }

                    _lastX = xLoc;
                    _lastY = yLoc;
                } else {
                    LayoutTurnout o;
                    LevelXing x;
                    LayoutSlip sl;

                    switch (selectedPointType) {
                        case LayoutTrack.POS_POINT: {
                            ((PositionablePoint) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.TURNOUT_CENTER: {
                            ((LayoutTurnout) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.TURNOUT_A: {
                            ((LayoutTurnout) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case LayoutTrack.TURNOUT_B: {
                            ((LayoutTurnout) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case LayoutTrack.TURNOUT_C: {
                            ((LayoutTurnout) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case LayoutTrack.TURNOUT_D: {
                            ((LayoutTurnout) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_CENTER: {
                            ((LevelXing) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_A: {
                            ((LevelXing) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_B: {
                            ((LevelXing) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_C: {
                            ((LevelXing) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_D: {
                            ((LevelXing) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case LayoutTrack.SLIP_LEFT:
                        case LayoutTrack.SLIP_RIGHT: {
                            ((LayoutSlip) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.SLIP_A: {
                            ((LayoutSlip) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case LayoutTrack.SLIP_B: {
                            ((LayoutSlip) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case LayoutTrack.SLIP_C: {
                            ((LayoutSlip) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case LayoutTrack.SLIP_D: {
                            ((LayoutSlip) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case LayoutTrack.TURNTABLE_CENTER: {
                            ((LayoutTurntable) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.LAYOUT_POS_LABEL: {
                            PositionableLabel l = (PositionableLabel) selectedObject;

                            if (l.isPositionable()) {
                                //don't allow negative placement, object could become unreachable
                                int xNew = (int) Math.max(currentPoint.getX(), 0);
                                int yNew = (int) Math.max(currentPoint.getY(), 0);
                                l.setLocation(xNew, yNew);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.LAYOUT_POS_JCOMP: {
                            PositionableJComponent c = (PositionableJComponent) selectedObject;

                            if (c.isPositionable()) {
                                //don't allow negative placement, object could become unreachable
                                int xNew = (int) Math.max(currentPoint.getX(), 0);
                                int yNew = (int) Math.max(currentPoint.getY(), 0);
                                c.setLocation(xNew, yNew);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.MULTI_SENSOR: {
                            PositionableLabel pl = (PositionableLabel) selectedObject;

                            if (pl.isPositionable()) {
                                //don't allow negative placement, object could become unreachable
                                int xNew = (int) Math.max(currentPoint.getX(), 0);
                                int yNew = (int) Math.max(currentPoint.getY(), 0);
                                pl.setLocation(xNew, yNew);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.TRACK_CIRCLE_CENTRE: {
                            TrackSegment t = (TrackSegment) selectedObject;
                            t.reCalculateTrackSegmentAngle(currentPoint.getX(), currentPoint.getY());
                            break;
                        }

                        default:
                            if ((foundPointType >= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN)
                                    && (foundPointType <= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MAX)) {
                                int index = selectedPointType - LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN;
                                ((TrackSegment) selectedObject).setBezierControlPoint(currentPoint, index);
                            } else if (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                                LayoutTurntable turn = (LayoutTurntable) selectedObject;
                                turn.setRayCoordsIndexed(currentPoint.getX(), currentPoint.getY(),
                                        selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                            }
                    } //switch
                }
                redrawPanel();
            } else if ((beginObject != null) && event.isShiftDown() && trackButton.isSelected()) {
                //dragging from first end of Track Segment
                currentLocation.setLocation(xLoc, yLoc);
                boolean needResetCursor = (foundObject != null);

                if (hitPointCheckLayoutTracks(currentLocation, true)) {
                    //have match to free connection point, change cursor
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if (needResetCursor) {
                    setCursor(Cursor.getDefaultCursor());
                }
                redrawPanel();
            } else if (selectionActive && !event.isShiftDown() && !event.isAltDown() && !event.isMetaDown()) {
                selectionWidth = xLoc - selectionX;
                selectionHeight = yLoc - selectionY;
                redrawPanel();
            }
        } else {
            Rectangle r = new Rectangle(event.getX(), event.getY(), 1, 1);
            ((JComponent) event.getSource()).scrollRectToVisible(r);
        }
    } //mouseDragged

    // @SuppressWarnings("unused")
    private void updateLocation(@Nonnull Object o, int pointType, @Nonnull Point2D newPos) {
        switch (pointType) {
            case LayoutTrack.TURNOUT_A: {
                ((LayoutTurnout) o).setCoordsA(newPos);
                break;
            }

            case LayoutTrack.TURNOUT_B: {
                ((LayoutTurnout) o).setCoordsB(newPos);
                break;
            }

            case LayoutTrack.TURNOUT_C: {
                ((LayoutTurnout) o).setCoordsC(newPos);
                break;
            }

            case LayoutTrack.TURNOUT_D: {
                ((LayoutTurnout) o).setCoordsD(newPos);
                break;
            }

            case LayoutTrack.LEVEL_XING_A: {
                ((LevelXing) o).setCoordsA(newPos);
                break;
            }

            case LayoutTrack.LEVEL_XING_B: {
                ((LevelXing) o).setCoordsB(newPos);
                break;
            }

            case LayoutTrack.LEVEL_XING_C: {
                ((LevelXing) o).setCoordsC(newPos);
                break;
            }

            case LayoutTrack.LEVEL_XING_D: {
                ((LevelXing) o).setCoordsD(newPos);
                break;
            }

            case LayoutTrack.SLIP_A: {
                ((LayoutSlip) o).setCoordsA(newPos);
                break;
            }

            case LayoutTrack.SLIP_B: {
                ((LayoutSlip) o).setCoordsB(newPos);
                break;
            }

            case LayoutTrack.SLIP_C: {
                ((LayoutSlip) o).setCoordsC(newPos);
                break;
            }

            case LayoutTrack.SLIP_D: {
                ((LayoutSlip) o).setCoordsD(newPos);
                break;
            }

            default:
                if ((foundPointType >= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN)
                        && (foundPointType <= LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MAX)) {
                    int index = pointType - LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN;
                    ((TrackSegment) o).setBezierControlPoint(newPos, index);
                } else if (pointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                    LayoutTurntable turn = (LayoutTurntable) o;
                    turn.setRayCoordsIndexed(newPos.getX(), newPos.getY(),
                            pointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
        } //switch
        setDirty();
    } //updateLocation

    /*
     * this function appears to be unused internally.
     * @deprecated since 4.3.5
     */
    @Deprecated
    public void setLoc(int x, int y) {
        if (isEditable()) {
            xLoc = x;
            yLoc = y;
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
    } //setLoc

    /**
     * Add an Anchor point.
     */
    public void addAnchor() {
        addAnchor(currentPoint);
    }

    private PositionablePoint addAnchor(Point2D p) {
        //get unique name
        String name = finder.uniqueName("A", numAnchors++);

        //create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.ANCHOR, p, this);

        //if (o!=null) {
        pointList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //}
        return o;
    } //addAnchor

    /**
     * Add an End Bumper point.
     */
    public void addEndBumper() {
        //get unique name
        String name = finder.uniqueName("EB", numEndBumpers++);

        //create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.END_BUMPER, currentPoint, this);

        //if (o!=null) {
        pointList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //}
    } //addEndBumper

    /**
     * Add an Edge Connector point.
     */
    public void addEdgeConnector() {
        //get unique name
        String name = finder.uniqueName("EC", numEdgeConnectors++);

        //create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.EDGE_CONNECTOR, currentPoint, this);

        //if (o!=null) {
        pointList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //}
    } //addEdgeConnector

    /**
     * Add a Track Segment
     */
    public void addTrackSegment() {
        //get unique name
        String name = finder.uniqueName("T", numTrackSegments++);

        //create object
        newTrack = new TrackSegment(name, beginObject, beginPointType,
                foundObject, foundPointType, dashedLine.isSelected(),
                mainlineTrack.isSelected(), this);

        trackList.add(newTrack);
        unionToPanelBounds(newTrack.getBounds());
        setDirty();

        //link to connected objects
        setLink(newTrack, LayoutTrack.TRACK, beginObject, beginPointType);
        setLink(newTrack, LayoutTrack.TRACK, foundObject, foundPointType);

        //check on layout block
        String newName = blockIDComboBox.getDisplayName();
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            newTrack.setLayoutBlock(b);
            auxTools.setBlockConnectivityChanged();

            //check on occupancy sensor
            String sensorName = blockSensorComboBox.getDisplayName();

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.setText(b.getOccupancySensorName());
                }
            }
            newTrack.updateBlockInfo();
        }
    } //addTrackSegment

    /**
     * Add a Level Crossing
     */
    public void addLevelXing() {
        //get unique name
        String name = finder.uniqueName("X", numLevelXings++);

        //create object
        LevelXing o = new LevelXing(name, currentPoint, this);

        //if (o!=null) {
        xingList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //check on layout block
        String newName = blockIDComboBox.getDisplayName();
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlockAC(b);
            o.setLayoutBlockBD(b);

            //check on occupancy sensor
            String sensorName = blockSensorComboBox.getDisplayName();

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.setText(b.getOccupancySensorName());
                }
            }
        }
    } //addLevelXing

    /**
     * Add a LayoutSlip
     */
    public void addLayoutSlip(int type) {
        //get the rotation entry
        double rot = 0.0;
        String s = rotationComboBox.getEditor().getItem().toString();
        s = (null != s) ? s.trim() : "";

        if (s.isEmpty()) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("Error3") + " "
                        + e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //get unique name
        String name = finder.uniqueName("SL", numLayoutSlips++);

        //create object
        LayoutSlip o = new LayoutSlip(name, currentPoint, rot, this, type);
        slipList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //check on layout block
        String newName = blockIDComboBox.getDisplayName();
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlock(b);

            //check on occupancy sensor
            String sensorName = blockSensorComboBox.getDisplayName();

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.setText(b.getOccupancySensorName());
                }
            }
        }

        String turnoutName = turnoutNameComboBox.getDisplayName();

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
                turnoutNameComboBox.setText(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnout("");
            turnoutNameComboBox.setText("");
            turnoutNameComboBox.setSelectedIndex(-1);
        }
        turnoutName = extraTurnoutNameComboBox.getDisplayName();

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnoutB(turnoutName);

            if (o.getTurnoutB().getSystemName().equals(turnoutName.toUpperCase())) {
                extraTurnoutNameComboBox.setText(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnoutB("");
            extraTurnoutNameComboBox.setText("");
            extraTurnoutNameComboBox.setSelectedIndex(-1);
        }
    } //addLayoutSlip

    /**
     * Add a Layout Turnout
     */
    public void addLayoutTurnout(int type) {
        //get the rotation entry
        double rot = 0.0;
        String s = rotationComboBox.getEditor().getItem().toString();
        s = (null != s) ? s.trim() : "";

        if (s.isEmpty()) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("Error3") + " "
                        + e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //get unique name
        String name = finder.uniqueName("TO", numLayoutTurnouts++);

        //create object
        LayoutTurnout o = new LayoutTurnout(name, type, currentPoint, rot, xScale, yScale, this);
        turnoutList.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();

        //check on layout block
        String newName = blockIDComboBox.getDisplayName();
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlock(b);

            //check on occupancy sensor
            String sensorName = blockSensorComboBox.getDisplayName();

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.setText(b.getOccupancySensorName());
                }
            }
        }

        //set default continuing route Turnout State
        o.setContinuingSense(Turnout.CLOSED);

        //check on a physical turnout
        String turnoutName = turnoutNameComboBox.getDisplayName();

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
                turnoutNameComboBox.setText(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnout("");
            turnoutNameComboBox.setText("");
            turnoutNameComboBox.setSelectedIndex(-1);
        }
    } //addLayoutTurnout

    /**
     * Validates that a physical turnout exists and is unique among Layout
     * Turnouts Returns true if valid turnout was entered, false otherwise
     *
     * @param inTurnoutName the (system or user) name of the turnout
     * @param inOpenPane    the pane over which to show dialogs (null to
     *                      suppress dialogs)
     * @return true if valid
     */
    public boolean validatePhysicalTurnout(@Nonnull String inTurnoutName,
            @Nullable Component inOpenPane) {
        //check if turnout name was entered
        if (inTurnoutName.isEmpty()) {
            //no turnout entered
            return false;
        }

        //check that the unique turnout name corresponds to a defined physical turnout
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutName);
        if (t == null) {
            //There is no turnout corresponding to this name
            if (inOpenPane != null) {
                JOptionPane.showMessageDialog(inOpenPane,
                        java.text.MessageFormat.format(Bundle.getMessage("Error8"),
                                new Object[]{inTurnoutName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        log.debug("validatePhysicalTurnout('{}')", inTurnoutName);

        //ensure that this turnout is unique among Layout Turnouts
        for (LayoutTurnout lt : turnoutList) {
            t = lt.getTurnout();
            if (t != null) {
                String sname = t.getSystemName();
                String uname = t.getUserName();
                log.debug("{}: Turnout tested '{}' and '{}'.", lt.getName(), sname, uname);

                if ((sname.equals(inTurnoutName.toUpperCase()))
                        || ((uname != null) && (uname.equals(inTurnoutName)))) {
                    if (inOpenPane != null) {
                        JOptionPane.showMessageDialog(inOpenPane,
                                java.text.MessageFormat.format(Bundle.getMessage("Error4"),
                                        new Object[]{inTurnoutName}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                    return false;
                }
            }

            // Only check for the second turnout if the type is a double cross over
            // otherwise the second turnout is used to throw an additional turnout at
            // the same time.
            if (lt.getTurnoutType() >= LayoutTurnout.DOUBLE_XOVER) {
                t = lt.getSecondTurnout();
                if (t != null) {
                    String sname = t.getSystemName();
                    String uname = t.getUserName();
                    log.debug("{}: 2nd Turnout tested '{}' and '{}'.", lt.getName(), sname, uname);

                    if ((sname.equals(inTurnoutName.toUpperCase()))
                            || ((uname != null) && (uname.equals(inTurnoutName)))) {
                        if (inOpenPane != null) {
                            JOptionPane.showMessageDialog(inOpenPane,
                                    java.text.MessageFormat.format(Bundle.getMessage("Error4"),
                                            new Object[]{inTurnoutName}),
                                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        }
                        return false;
                    }
                }
            }
        }

        for (LayoutSlip sl : slipList) {
            t = sl.getTurnout();
            if (t != null) {
                String sname = t.getSystemName();
                String uname = t.getUserName();
                log.debug("{}: slip Turnout tested '{}' and '{}'.", sl.getName(), sname, uname);

                if ((sname.equals(inTurnoutName.toUpperCase()))
                        || ((uname != null) && (uname.equals(inTurnoutName)))) {
                    if (inOpenPane != null) {
                        JOptionPane.showMessageDialog(inOpenPane,
                                java.text.MessageFormat.format(Bundle.getMessage("Error4"),
                                        new Object[]{inTurnoutName}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                    return false;
                }
            }

            t = sl.getTurnoutB();
            if (t != null) {
                String sname = t.getSystemName();
                String uname = t.getUserName();
                log.debug("{}: slip Turnout B tested '{}' and '{}'.", sl.getName(), sname, uname);

                if ((sname.equals(inTurnoutName.toUpperCase()))
                        || ((uname != null) && (uname.equals(inTurnoutName)))) {
                    if (inOpenPane != null) {
                        JOptionPane.showMessageDialog(inOpenPane,
                                java.text.MessageFormat.format(Bundle.getMessage("Error4"),
                                        new Object[]{inTurnoutName}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                    return false;
                }
            }
        }
        return true;
    } //validatePhysicalTurnout

    /**
     * Adds a link in the 'to' object to the 'from' object
     */
    private void setLink(@Nonnull Object fromObject, int fromPointType,
            @Nonnull Object toObject, int toPointType) {
        switch (toPointType) {
            case LayoutTrack.POS_POINT: {
                if (fromPointType == LayoutTrack.TRACK) {
                    ((PositionablePoint) toObject).setTrackConnection(
                            (TrackSegment) fromObject);
                } else {
                    log.error("Attempt to set a non-TRACK connection to a Positionable Point");
                }
                break;
            }

            case LayoutTrack.TURNOUT_A: {
                ((LayoutTurnout) toObject).setConnectA(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.TURNOUT_B: {
                ((LayoutTurnout) toObject).setConnectB(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.TURNOUT_C: {
                ((LayoutTurnout) toObject).setConnectC(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.TURNOUT_D: {
                ((LayoutTurnout) toObject).setConnectD(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.LEVEL_XING_A: {
                ((LevelXing) toObject).setConnectA(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.LEVEL_XING_B: {
                ((LevelXing) toObject).setConnectB(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.LEVEL_XING_C: {
                ((LevelXing) toObject).setConnectC(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.LEVEL_XING_D: {
                ((LevelXing) toObject).setConnectD(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.SLIP_A: {
                ((LayoutSlip) toObject).setConnectA(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.SLIP_B: {
                ((LayoutSlip) toObject).setConnectB(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.SLIP_C: {
                ((LayoutSlip) toObject).setConnectC(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.SLIP_D: {
                ((LayoutSlip) toObject).setConnectD(fromObject, fromPointType);
                break;
            }

            case LayoutTrack.TRACK: {
                //should never happen, Track Segment links are set in ctor
                log.error("Illegal request to set a Track Segment link");
                break;
            }

            default: {
                if ((toPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) && (fromPointType == LayoutTrack.TRACK)) {
                    ((LayoutTurntable) toObject).setRayConnect((TrackSegment) fromObject,
                            toPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
                break;
            }
        } //switch
    } //setLink

    /**
     * Return a layout block with the entered name, creating a new one if
     * needed. Note that the entered name becomes the user name of the
     * LayoutBlock, and a system name is automatically created by
     * LayoutBlockManager if needed.
     */
    public LayoutBlock provideLayoutBlock(@Nonnull String inBlockName) {
        //log.debug("provideLayoutBlock :: '{}'", inBlockName);
        LayoutBlock result = null, newBlk = null; //assume failure (pessimist!)

        if (inBlockName.isEmpty()) {
            //nothing entered, try autoAssign
            if (autoAssignBlocks) {
                newBlk = InstanceManager.getDefault(LayoutBlockManager.class
                ).createNewLayoutBlock();

                if (null == newBlk) {
                    log.error("Failure to auto-assign LayoutBlock '{}'.", inBlockName);

                }
            }
        } else {
            //check if this Layout Block already exists
            result = InstanceManager.getDefault(LayoutBlockManager.class
            ).getByUserName(inBlockName);

            if (null == result) { //(no)
                newBlk = InstanceManager.getDefault(LayoutBlockManager.class
                ).createNewLayoutBlock(null, inBlockName);

                if (null == newBlk) {
                    log.error("Failure to create new LayoutBlock '{}'.", inBlockName);
                }
            }
        }

        //if we created a new block
        if (newBlk != null) {
            //initialize the new block
            //log.debug("provideLayoutBlock :: Init new block {}", inBlockName);
            newBlk.initializeLayoutBlock();
            newBlk.initializeLayoutBlockRouting();
            newBlk.setBlockTrackColor(defaultTrackColor);
            newBlk.setBlockOccupiedColor(defaultOccupiedTrackColor);
            newBlk.setBlockExtraColor(defaultAlternativeTrackColor);
            result = newBlk;
        }

        if (null != result) {
            //set both new and previously existing block
            result.addLayoutEditor(this);
            result.incrementUse();
            setDirty();
        }
        return result;
    } //provideLayoutBlock

    /**
     * Validates that the supplied occupancy sensor name corresponds to an
     * existing sensor and is unique among all blocks. If valid, returns true
     * and sets the block sensor name in the block. Else returns false, and does
     * nothing to the block.
     */
    public boolean validateSensor(@Nonnull String sensorName,
            @Nonnull LayoutBlock blk,
            @Nonnull Component openFrame) {
        boolean result = false; //assume failure (pessimist!)

        //check if anything entered
        if (!sensorName.isEmpty()) {
            //get a validated sensor corresponding to this name and assigned to block
            Sensor s = blk.validateSensor(sensorName, openFrame);
            result = (null != s); //if sensor returned result is true.
        }
        return result;
    } //validateSensor

    /**
     * Return a layout block with the given name if one exists. Registers this
     * LayoutEditor with the layout block. This method is designed to be used
     * when a panel is loaded. The calling method must handle whether the use
     * count should be incremented.
     */
    public LayoutBlock getLayoutBlock(@Nonnull String blockID) {
        //check if this Layout Block already exists
        LayoutBlock blk = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(blockID);
        if (blk == null) {
            log.error("LayoutBlock '{}' not found when panel loaded", blockID);
            return null;
        }
        blk.addLayoutEditor(this);
        return blk;
    } //getLayoutBlock

    /**
     * Remove object from all Layout Editor temporary lists of items not part of
     * track schematic
     */
    protected boolean remove(@Nonnull Object s) {
        boolean found = false;

        if (sensorImage.contains(s)) {
            sensorImage.remove(s);
            found = true;
        }
        if (sensorList.contains(s)) {
            sensorList.remove(s);
            found = true;
        }
        if (backgroundImage.contains(s)) {
            backgroundImage.remove(s);
            found = true;
        }
        if (memoryLabelList.contains(s)) {
            memoryLabelList.remove(s);
            found = true;
        }
        if (blockContentsLabelList.contains(s)) {
            blockContentsLabelList.remove(s);
            found = true;
        }
        if (signalList.contains(s)) {
            signalList.remove(s);
            found = true;
        }
        if (multiSensors.contains(s)) {
            multiSensors.remove(s);
            found = true;
        }
        if (clocks.contains(s)) {
            clocks.remove(s);
            found = true;
        }
        if (signalHeadImage.contains(s)) {
            signalHeadImage.remove(s);
            found = true;
        }
        if (labelImage.contains(s)) {
            labelImage.remove(s);
            found = true;
        }
        for (int i = 0; i < signalMastList.size(); i++) {
            if (s == signalMastList.get(i)) {
                if (removeSignalMast((SignalMastIcon) s)) {
                    signalMastList.remove(i);
                    found = true;
                    break;
                } else {
                    return false;
                }
            }
        }

        super.removeFromContents((Positionable) s);

        if (found) {
            setDirty();
            redrawPanel();
        }
        return found;
    } //remove

    @Override
    public boolean removeFromContents(@Nonnull Positionable l) {
        return remove(l);
    }

    private String findBeanUsage(@Nonnull NamedBean sm) {
        PositionablePoint pe;
        PositionablePoint pw;
        LayoutTurnout lt;
        LevelXing lx;
        LayoutSlip ls;
        boolean found = false;
        StringBuilder sb = new StringBuilder();

        sb.append("This ");

        if (sm instanceof SignalMast) {
            sb.append("Signal Mast"); //TODO I18N using Bundle.getMessage("BeanNameSignalMast");
            sb.append(" is linked to the following items<br> do you want to remove those references");

            if (InstanceManager.getDefault(jmri.SignalMastLogicManager.class
            ).isSignalMastUsed((SignalMast) sm)) {
                jmri.SignalMastLogic sml
                        = InstanceManager.getDefault(jmri.SignalMastLogicManager.class
                        ).getSignalMastLogic((SignalMast) sm);

                //jmri.SignalMastLogic sml =
                //InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic((SignalMast)sm);
                if ((sml != null) && sml.useLayoutEditor(sml.getDestinationList().get(0))) {
                    sb.append(" and any SignalMast Logic associated with it");
                }
            }
        } else if (sm instanceof Sensor) {
            sb.append("Sensor"); //TODO I18N using Bundle.getMessage("BeanNameSensor");
            sb.append(" is linked to the following items<br> do you want to remove those references");
        } else if (sm instanceof SignalHead) {
            sb.append("SignalHead"); //TODO I18N using Bundle.getMessage("BeanNameSignalHead");
            sb.append(" is linked to the following items<br> do you want to remove those references");
        }

        if ((pw = finder.findPositionablePointByWestBoundBean(sm)) != null) {
            sb.append("<br>Point of ");
            TrackSegment t = pw.getConnect1();

            if (t != null) {
                sb.append(t.getBlockName()).append(" and ");
            }
            t = pw.getConnect2();

            if (t != null) {
                sb.append(t.getBlockName());
            }
            found = true;
        }

        if ((pe = finder.findPositionablePointByEastBoundBean(sm)) != null) {
            sb.append("<br>Point of ");
            TrackSegment t = pe.getConnect1();

            if (t != null) {
                sb.append(t.getBlockName()).append(" and ");
            }
            t = pe.getConnect2();

            if (t != null) {
                sb.append(t.getBlockName());
            }
            found = true;
        }

        if ((lt = finder.findLayoutTurnoutByBean(sm)) != null) {
            sb.append("<br>Turnout ").append(lt.getTurnoutName()); //I18N using Bundle.getMessage("BeanNameTurnout");
            found = true;
        }

        if ((lx = finder.findLevelXingByBean(sm)) != null) {
            sb.append("<br>Level Crossing ").append(lx.getId());
            found = true;
        }

        if ((ls = finder.findLayoutSlipByBean(sm)) != null) {
            sb.append("<br>Slip ").append(ls.getTurnoutName());
            found = true;
        }

        if (!found) {
            return null;
        }
        return sb.toString();
    } //findBeanUsage

    private boolean removeSignalMast(@Nonnull SignalMastIcon si) {
        SignalMast sm = si.getSignalMast();
        String usage = findBeanUsage(sm);

        if (usage != null) {
            usage = String.format("<html>%s</html>", usage);
            int selectedValue = JOptionPane.showOptionDialog(this,
                    usage, Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonCancel")},
                    Bundle.getMessage("ButtonYes"));

            if (selectedValue == 1) {
                return true; //return leaving the references in place but allow the icon to be deleted.
            }

            if (selectedValue == 2) {
                return false; //do not delete the item
            }
            removeBeanRefs(sm);
        }
        return true;
    } //removeSignalMast

    private void removeBeanRefs(@Nonnull NamedBean sm) {
        PositionablePoint pe;
        PositionablePoint pw;
        LayoutTurnout lt;
        LevelXing lx;
        LayoutSlip ls;

        if ((pw = finder.findPositionablePointByWestBoundBean(sm)) != null) {
            pw.removeBeanReference(sm);
        }

        if ((pe = finder.findPositionablePointByEastBoundBean(sm)) != null) {
            pe.removeBeanReference(sm);
        }

        if ((lt = finder.findLayoutTurnoutByBean(sm)) != null) {
            lt.removeBeanReference(sm);
        }

        if ((lx = finder.findLevelXingByBean(sm)) != null) {
            lx.removeBeanReference(sm);
        }

        if ((ls = finder.findLayoutSlipByBean(sm)) != null) {
            ls.removeBeanReference(sm);
        }
    } //removeBeanRefs

    boolean noWarnPositionablePoint = false;

    /**
     * Remove a PositionablePoint -- an Anchor or an End Bumper.
     */
    protected boolean removePositionablePoint(@Nonnull PositionablePoint o) {
        //First verify with the user that this is really wanted, only show message if there is a bit of track connected
        if ((o.getConnect1() != null) || (o.getConnect2() != null)) {
            if (!noWarnPositionablePoint) {
                int selectedValue = JOptionPane.showOptionDialog(this,
                        Bundle.getMessage("Question2"), Bundle.getMessage(
                        "WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"),
                            Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonYesPlus")},
                        Bundle.getMessage("ButtonNo"));

                if (selectedValue == 1) {
                    return false; //return without creating if "No" response
                }

                if (selectedValue == 2) {
                    //Suppress future warnings, and continue
                    noWarnPositionablePoint = true;
                }
            }

            //remove from selection information
            if (selectedObject == o) {
                selectedObject = null;
            }

            if (prevSelectedObject == o) {
                prevSelectedObject = null;
            }

            //remove connections if any
            TrackSegment t = o.getConnect1();

            if (t != null) {
                removeTrackSegment(t);
            }
            t = o.getConnect2();

            if (t != null) {
                removeTrackSegment(t);
            }

            //delete from array
        }

        for (int i = 0; i < pointList.size(); i++) {
            PositionablePoint p = pointList.get(i);

            if (p == o) {
                //found object
                pointList.remove(i);
                setDirty();
                redrawPanel();

                return true;
            }
        }
        return false;
    } //removePositionablePoint

    boolean noWarnLayoutTurnout = false;

    /**
     * Remove a LayoutTurnout
     */
    protected boolean removeLayoutTurnout(@Nonnull LayoutTurnout o) {
        //First verify with the user that this is really wanted
        if (!noWarnLayoutTurnout) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question1r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false; //return without removing if "No" response
            }

            if (selectedValue == 2) {
                //Suppress future warnings, and continue
                noWarnLayoutTurnout = true;
            }
        }

        //remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        //remove connections if any
        TrackSegment t = (TrackSegment) o.getConnectA();

        if (t != null) {
            substituteAnchor(o.getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(o.getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(o.getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(o.getCoordsD(), o, t);
        }

        //decrement Block use count(s)
        LayoutBlock b = o.getLayoutBlock();

        if (b != null) {
            b.decrementUse();
        }

        if ((o.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                || (o.getTurnoutType() == LayoutTurnout.RH_XOVER)
                || (o.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
            LayoutBlock b2 = o.getLayoutBlockB();

            if ((b2 != null) && (b2 != b)) {
                b2.decrementUse();
            }
            LayoutBlock b3 = o.getLayoutBlockC();

            if ((b3 != null) && (b3 != b) && (b3 != b2)) {
                b3.decrementUse();
            }
            LayoutBlock b4 = o.getLayoutBlockD();

            if ((b4 != null) && (b4 != b)
                    && (b4 != b2) && (b4 != b3)) {
                b4.decrementUse();
            }
        }

        //delete from array
        if (turnoutList.contains(o)) {
            turnoutList.remove(o);
            setDirty();
            redrawPanel();
            return true;
        }
        return false;
    } //removeLayoutTurnout

    private void substituteAnchor(@Nonnull Point2D loc,
            @Nonnull Object o, @Nonnull TrackSegment t) {
        PositionablePoint p = addAnchor(loc);

        if (t.getConnect1() == o) {
            t.setNewConnect1(p, LayoutTrack.POS_POINT);
        }

        if (t.getConnect2() == o) {
            t.setNewConnect2(p, LayoutTrack.POS_POINT);
        }
        p.setTrackConnection(t);
    } //substituteAnchor

    boolean noWarnLevelXing = false;

    /**
     * Remove a Level Crossing
     */
    protected boolean removeLevelXing(@Nonnull LevelXing o) {
        //First verify with the user that this is really wanted
        if (!noWarnLevelXing) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question3r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false; //return without creating if "No" response
            }

            if (selectedValue == 2) {
                //Suppress future warnings, and continue
                noWarnLevelXing = true;
            }
        }

        //remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        //remove connections if any
        TrackSegment t = (TrackSegment) o.getConnectA();

        if (t != null) {
            substituteAnchor(o.getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(o.getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(o.getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(o.getCoordsD(), o, t);
        }

        //decrement block use count if any blocks in use
        LayoutBlock lb = o.getLayoutBlockAC();

        if (lb != null) {
            lb.decrementUse();
        }
        LayoutBlock lbx = o.getLayoutBlockBD();

        if ((lbx != null) && (lb != null) && (lbx != lb)) {
            lb.decrementUse();
        }

        //delete from array
        if (xingList.contains(o)) {
            xingList.remove(o);
            o.remove();
            setDirty();
            redrawPanel();
            return true;
        }
        return false;
    } //removeLevelXing

    boolean noWarnSlip = false;

    protected boolean removeLayoutSlip(@Nonnull LayoutTurnout o) {
        if (!(o instanceof LayoutSlip)) {
            return false;
        }

        //First verify with the user that this is really wanted
        if (!noWarnSlip) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question5r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false; //return without creating if "No" response
            }

            if (selectedValue == 2) {
                //Suppress future warnings, and continue
                noWarnSlip = true;
            }
        }

        //remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        //remove connections if any
        TrackSegment t = (TrackSegment) o.getConnectA();

        if (t != null) {
            substituteAnchor(o.getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(o.getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(o.getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(o.getCoordsD(), o, t);
        }

        //decrement block use count if any blocks in use
        LayoutBlock lb = o.getLayoutBlock();

        if (lb != null) {
            lb.decrementUse();
        }

        //delete from array
        if (slipList.contains(o)) {
            slipList.remove(o);
            o.remove();
            setDirty();
            redrawPanel();
            return true;
        }
        return false;
    } //removeLayoutSlip

    boolean noWarnTurntable = false;

    /**
     * Remove a Layout Turntable
     */
    protected boolean removeTurntable(@Nonnull LayoutTurntable o) {
        //First verify with the user that this is really wanted
        if (!noWarnTurntable) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question4r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false; //return without creating if "No" response
            }

            if (selectedValue == 2) {
                //Suppress future warnings, and continue
                noWarnTurntable = true;
            }
        }

        //remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        //remove connections if any
        for (int j = 0; j < o.getNumberRays(); j++) {
            TrackSegment t = o.getRayConnectOrdered(j);

            if (t != null) {
                substituteAnchor(o.getRayCoordsIndexed(j), o, t);
            }
        }

        //delete from array
        if (turntableList.contains(o)) {
            turntableList.remove(o);
            o.remove();
            setDirty();
            redrawPanel();
            return true;
        }
        return false;
    } //removeTurntable

    /**
     * Remove a Track Segment
     */
    protected void removeTrackSegment(@Nonnull TrackSegment o) {
        //save affected blocks
        LayoutBlock block1 = null;
        LayoutBlock block2 = null;
        LayoutBlock block = o.getLayoutBlock();

        //remove any connections
        int type = o.getType1();

        if (type == LayoutTrack.POS_POINT) {
            PositionablePoint p = (PositionablePoint) (o.getConnect1());

            if (p != null) {
                p.removeTrackConnection(o);

                if (p.getConnect1() != null) {
                    block1 = p.getConnect1().getLayoutBlock();
                } else if (p.getConnect2() != null) {
                    block1 = p.getConnect2().getLayoutBlock();
                }
            }
        } else {
            block1 = getAffectedBlock(o.getConnect1(), type);
            disconnect(o.getConnect1(), type);
        }
        type = o.getType2();

        if (type == LayoutTrack.POS_POINT) {
            PositionablePoint p = (PositionablePoint) (o.getConnect2());

            if (p != null) {
                p.removeTrackConnection(o);

                if (p.getConnect1() != null) {
                    block2 = p.getConnect1().getLayoutBlock();
                } else if (p.getConnect2() != null) {
                    block2 = p.getConnect2().getLayoutBlock();
                }
            }
        } else {
            block2 = getAffectedBlock(o.getConnect2(), type);
            disconnect(o.getConnect2(), type);
        }

        //delete from array
        for (int i = 0; i < trackList.size(); i++) {
            TrackSegment t = trackList.get(i);

            if (t == o) {
                //found object
                trackList.remove(i);
            }
        }

        //update affected blocks
        if (block != null) {
            //decrement Block use count
            block.decrementUse();
            auxTools.setBlockConnectivityChanged();
            block.updatePaths();
        }

        if ((block1 != null) && (block1 != block)) {
            block1.updatePaths();
        }

        if ((block2 != null) && (block2 != block) && (block2 != block1)) {
            block2.updatePaths();
        }

        //
        setDirty();
        redrawPanel();
    } //removeTrackSegment

    private void disconnect(@Nonnull Object o, int type) {
        if (o == null) {
            return;
        }

        switch (type) {
            case LayoutTrack.TURNOUT_A: {
                ((LayoutTurnout) o).setConnectA(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.TURNOUT_B: {
                ((LayoutTurnout) o).setConnectB(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.TURNOUT_C: {
                ((LayoutTurnout) o).setConnectC(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.TURNOUT_D: {
                ((LayoutTurnout) o).setConnectD(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.LEVEL_XING_A: {
                ((LevelXing) o).setConnectA(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.LEVEL_XING_B: {
                ((LevelXing) o).setConnectB(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.LEVEL_XING_C: {
                ((LevelXing) o).setConnectC(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.LEVEL_XING_D: {
                ((LevelXing) o).setConnectD(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.SLIP_A: {
                ((LayoutSlip) o).setConnectA(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.SLIP_B: {
                ((LayoutSlip) o).setConnectB(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.SLIP_C: {
                ((LayoutSlip) o).setConnectC(null, LayoutTrack.NONE);
                break;
            }

            case LayoutTrack.SLIP_D: {
                ((LayoutSlip) o).setConnectD(null, LayoutTrack.NONE);
                break;
            }

            default: {
                if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                    ((LayoutTurntable) o).setRayConnect(null, type - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
                break;
            }
        } //switch
    } //disconnect

    public LayoutBlock getAffectedBlock(@Nonnull Object o, int type) {
        LayoutBlock result = null;
        if (o != null) {
            switch (type) {
                case LayoutTrack.TURNOUT_A: {
                    result = ((LayoutTurnout) o).getLayoutBlock();
                    break;
                }

                case LayoutTrack.TURNOUT_B: {
                    result = ((LayoutTurnout) o).getLayoutBlockB();
                    break;
                }

                case LayoutTrack.TURNOUT_C: {
                    result = ((LayoutTurnout) o).getLayoutBlockC();
                    break;
                }

                case LayoutTrack.TURNOUT_D: {
                    result = ((LayoutTurnout) o).getLayoutBlockD();
                    break;
                }

                case LayoutTrack.LEVEL_XING_A: {
                    result = ((LevelXing) o).getLayoutBlockAC();
                    break;
                }

                case LayoutTrack.LEVEL_XING_B: {
                    result = ((LevelXing) o).getLayoutBlockBD();
                    break;
                }

                case LayoutTrack.LEVEL_XING_C: {
                    result = ((LevelXing) o).getLayoutBlockAC();
                    break;
                }

                case LayoutTrack.LEVEL_XING_D: {
                    result = ((LevelXing) o).getLayoutBlockBD();
                    break;
                }

                case LayoutTrack.SLIP_A:
                case LayoutTrack.SLIP_B:
                case LayoutTrack.SLIP_C:
                case LayoutTrack.SLIP_D: {
                    result = ((LayoutSlip) o).getLayoutBlock();
                    break;
                }

                case LayoutTrack.TRACK: {
                    result = ((TrackSegment) o).getLayoutBlock();
                    break;
                }
                default: {
                    log.warn("Unhandled track type: {}", type);
                    break;
                }
            } //switch
        }
        return result;
    } //getAffectedBlock

    /**
     * Add a sensor indicator to the Draw Panel
     */
    void addSensor() {
        String newName = sensorComboBox.getDisplayName();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error10"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);

//l.setActiveIcon(sensorIconEditor.getIcon(0));
//l.setInactiveIcon(sensorIconEditor.getIcon(1));
//l.setInconsistentIcon(sensorIconEditor.getIcon(2));
//l.setUnknownIcon(sensorIconEditor.getIcon(3));
        l.setIcon("SensorStateActive", sensorIconEditor.getIcon(0));
        l.setIcon("SensorStateInactive", sensorIconEditor.getIcon(1));
        l.setIcon("BeanStateInconsistent", sensorIconEditor.getIcon(2));
        l.setIcon("BeanStateUnknown", sensorIconEditor.getIcon(3));
        l.setSensor(newName);
        l.setDisplayLevel(Editor.SENSORS);

        //Sensor xSensor = l.getSensor();
        //(Note: I don't see the point of this section of code because...
        if (l.getSensor() != null) {
            if ((l.getNamedSensor().getName() == null)
                    || (!(l.getNamedSensor().getName().equals(newName)))) {
                sensorComboBox.setText(l.getNamedSensor().getName());
            }
        }

        //...because this is called regardless of the code above
        sensorComboBox.setText(l.getNamedSensor().getName());
        setNextLocation(l);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //addSensor

    public void putSensor(@Nonnull SensorIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SENSORS);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //putSensor

    /**
     * Add a signal head to the Panel
     */
    void addSignalHead() {
        //check for valid signal head entry
        String newName = signalHeadComboBox.getDisplayName();
        SignalHead mHead = null;

        if (!newName.isEmpty()) {
            mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class
            ).getSignalHead(newName);

            /*if (mHead == null)
 mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(newName);
 else */
            signalHeadComboBox.setText(newName);
        }

        if (mHead == null) {
            //There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(thisPanel,
                    java.text.MessageFormat.format(Bundle.getMessage("Error9"),
                            new Object[]{newName}),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        //create and set up signal icon
        SignalHeadIcon l = new SignalHeadIcon(this);
        l.setSignalHead(newName);
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
        unionToPanelBounds(l.getBounds());
        setNextLocation(l);
        setDirty();
        putSignal(l);
    } //addSignalHead

    public void putSignal(@Nonnull SignalHeadIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SIGNALS);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //putSignal

    SignalHead getSignalHead(@Nonnull String name) {
        SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class
        ).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(jmri.SignalHeadManager.class
            ).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalHead named {}", name);
        }
        return sh;
    } //getSignalHead

    public boolean containsSignalHead(@Nonnull SignalHead head) {
        for (SignalHeadIcon h : signalList) {
            if (h.getSignalHead() == head) {
                return true;
            }
        }
        return false;
    } //containsSignalHead

    public void removeSignalHead(@Nonnull SignalHead head) {
        SignalHeadIcon h = null;
        int index = -1;

        for (int i = 0; (i < signalList.size()) && (index == -1); i++) {
            h = signalList.get(i);

            if (h.getSignalHead() == head) {
                index = i;
                break;
            }
        }

        if (index != (-1)) {
            signalList.remove(index);

            if (h != null) {
                h.remove();
                h.dispose();
            }
            setDirty();
            redrawPanel();
        }
    } //removeSignalHead

    void addSignalMast() {
        //check for valid signal head entry
        String newName = signalMastComboBox.getDisplayName();
        SignalMast mMast = null;

        if (!newName.isEmpty()) {
            mMast = InstanceManager.getDefault(jmri.SignalMastManager.class
            ).getSignalMast(newName);
            signalMastComboBox.setText(newName);
        }

        if (mMast == null) {
            //There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(thisPanel,
                    java.text.MessageFormat.format(Bundle.getMessage("Error9"),
                            new Object[]{newName}),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        //create and set up signal icon
        SignalMastIcon l = new SignalMastIcon(this);
        l.setSignalMast(newName);
        unionToPanelBounds(l.getBounds());
        setNextLocation(l);
        setDirty();
        putSignalMast(l);
    } //addSignalMast

    public void putSignalMast(@Nonnull SignalMastIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SIGNALS);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //putSignalMast

    SignalMast getSignalMast(@Nonnull String name) {
        SignalMast sh = InstanceManager.getDefault(jmri.SignalMastManager.class
        ).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(jmri.SignalMastManager.class
            ).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalMast named {}", name);
        }
        return sh;
    } //getSignalMast

    public boolean containsSignalMast(@Nonnull SignalMast mast) {
        for (SignalMastIcon h : signalMastList) {
            if (h.getSignalMast() == mast) {
                return true;
            }
        }
        return false;
    } //containsSignalMast

    /**
     * Add a label to the Draw Panel
     */
    void addLabel() {
        String labelText = textLabelTextField.getText();
        labelText = (null != labelText) ? labelText.trim() : "";

        if (labelText.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        PositionableLabel l = super.addLabel(labelText);
        unionToPanelBounds(l.getBounds());
        setDirty();
        l.setForeground(defaultTextColor);
    } //addLabel

    @Override
    public void putItem(@Nonnull Positionable l) {
        super.putItem(l);

        if (l instanceof SensorIcon) {
            sensorImage.add((SensorIcon) l);
            sensorList.add((SensorIcon) l);
        } else if (l instanceof LocoIcon) {
            markerImage.add((LocoIcon) l);
        } else if (l instanceof SignalHeadIcon) {
            signalHeadImage.add((SignalHeadIcon) l);
            signalList.add((SignalHeadIcon) l);
        } else if (l instanceof SignalMastIcon) {
            signalMastList.add((SignalMastIcon) l);
        } else if (l instanceof MemoryIcon) {
            memoryLabelList.add((MemoryIcon) l);
        } else if (l instanceof BlockContentsIcon) {
            blockContentsLabelList.add((BlockContentsIcon) l);
        } else if (l instanceof AnalogClock2Display) {
            clocks.add((AnalogClock2Display) l);
        } else if (l instanceof MultiSensorIcon) {
            multiSensors.add((MultiSensorIcon) l);
        }

        if (l instanceof PositionableLabel) {
            if (((PositionableLabel) l).isBackground()) {
                backgroundImage.add((PositionableLabel) l);
            } else {
                labelImage.add((PositionableLabel) l);
            }
        }
        unionToPanelBounds(l.getBounds(new Rectangle()));
        setDirty();
    } //putItem

    /**
     * Add a memory label to the Draw Panel
     */
    void addMemory() {
        String memoryName = textMemoryComboBox.getDisplayName();

        if (memoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11a"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        MemoryIcon l = new MemoryIcon(" ", this);
        l.setMemory(memoryName);
        Memory xMemory = l.getMemory();

        if (xMemory != null) {
            String uname = xMemory.getDisplayName();
            if ((uname == null) || (!(uname.equals(memoryName)))) {
                //put the system name in the memory field
                textMemoryComboBox.setText(xMemory.getSystemName());
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        l.setForeground(defaultTextColor);
        unionToPanelBounds(l.getBounds());
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //addMemory

    void addBlockContents() {
        String newName = blockContentsComboBox.getDisplayName();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11b"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        BlockContentsIcon l = new BlockContentsIcon(" ", this);
        l.setBlock(newName);
        jmri.Block xMemory = l.getBlock();

        if (xMemory != null) {
            String uname = xMemory.getDisplayName();
            if ((uname == null) || (!(uname.equals(newName)))) {
                //put the system name in the memory field
                blockContentsComboBox.setText(xMemory.getSystemName());
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        l.setForeground(defaultTextColor);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //addBlockContents

    /**
     * Add a Reporter Icon to the panel
     */
    void addReporter(@Nonnull String textReporter, int xx, int yy) {
        ReporterIcon l = new ReporterIcon(this);
        l.setReporter(textReporter);
        l.setLocation(xx, yy);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        unionToPanelBounds(l.getBounds());
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //addReporter

    /**
     * Add an icon to the target
     */
    void addIcon() {
        PositionableLabel l = new PositionableLabel(iconEditor.getIcon(0), this);
        setNextLocation(l);
        l.setDisplayLevel(Editor.ICONS);
        unionToPanelBounds(l.getBounds());
        l.updateSize();
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
    } //addIcon

    /**
     * Add a loco marker to the target
     */
    @Override
    public LocoIcon addLocoIcon(@Nonnull String name) {
        LocoIcon l = new LocoIcon(this);
        Point2D pt = windowCenter();
        l.setLocation((int) pt.getX(), (int) pt.getY());
        putLocoIcon(l, name);
        l.setPositionable(true);
        unionToPanelBounds(l.getBounds());
        return l;
    } //addLocoIcon

    @Override
    public void putLocoIcon(@Nonnull LocoIcon l, @Nonnull String name) {
        super.putLocoIcon(l, name);
        markerImage.add(l);
        unionToPanelBounds(l.getBounds());
    }

    JFileChooser inputFileChooser;

    /**
     * Add a background image
     */
    public void addBackground() {
        if (inputFileChooser == null) {
            inputFileChooser = new JFileChooser(
                    String.format("%s%sresources%sicons",
                            System.getProperty("user.dir"),
                            java.io.File.separator,
                            java.io.File.separator));
            if (false) {
                // TODO: Discuss with jmri-developers
                // this filter will allow any images supported by the current
                // operating system. This may not be desirable because it will
                // allow images that may not be supported by operating systems
                // other than the current one.
                FileFilter filt = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
                inputFileChooser.setFileFilter(filt);
            } else {
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
                filt.addExtension("gif");
                filt.addExtension("jpg");
                //TODO: discuss with jmri-developers - support png image files?
                filt.addExtension("png");
                inputFileChooser.setFileFilter(filt);
            }
        }
        inputFileChooser.rescanCurrentDirectory();

        int retVal = inputFileChooser.showOpenDialog(this);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; //give up if no file selected
        }

        //NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
        //inputFileChooser.getSelectedFile().getPath());
        String name = inputFileChooser.getSelectedFile().getPath();

        //convert to portable path
        name = jmri.util.FileUtil.getPortableFilename(name);

        //setup icon
        PositionableLabel o = super.setUpBackground(name);
        backgroundImage.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();
    } //addBackground

    /**
     * Remove a background image from the list of background images
     */
    protected void removeBackground(@Nonnull PositionableLabel b) {
        if (backgroundImage.contains(b)) {
            backgroundImage.remove(b);
            setDirty();
            return;
        }
    } //removeBackground

    /**
     * Invoke a window to allow you to add a MultiSensor indicator to the target
     */
    private int multiLocX;
    private int multiLocY;

    void startMultiSensor() {
        multiLocX = xLoc;
        multiLocY = yLoc;

        if (multiSensorFrame == null) {
            //create a common edit frame
            multiSensorFrame = new MultiSensorIconFrame(this);
            multiSensorFrame.initComponents();
            multiSensorFrame.pack();
        }
        multiSensorFrame.setVisible(true);
    } //startMultiSensor

    //Invoked when window has new multi-sensor ready
    public void addMultiSensor(@Nonnull MultiSensorIcon l) {
        l.setLocation(multiLocX, multiLocY);
        putItem(l); // note: this calls unionToPanelBounds & setDirty()
        multiSensorFrame.dispose();
        multiSensorFrame = null;
    } //addMultiSensor

    /**
     * Set object location and size for icon and label object as it is created.
     * Size comes from the preferredSize; location comes from the fields where
     * the user can spec it.
     */
    @Override
    protected void setNextLocation(@Nonnull Positionable obj) {
        obj.setLocation(xLoc, yLoc);
    }

    public ConnectivityUtil getConnectivityUtil() {
        if (conTools == null) {
            conTools = new ConnectivityUtil(thisPanel);
        }
        return conTools;
    } //getConnectivityUtil

    public LayoutEditorTools getLETools() {
        if (tools == null) {
            tools = new LayoutEditorTools(thisPanel);
        }
        return tools;
    } //getLETools

    /**
     * Invoked by DeletePanel menu item Validate user intent before deleting
     */
    @Override
    public boolean deletePanel() {
        //verify deletion
        if (!super.deletePanel()) {
            return false; //return without deleting if "No" response
        }
        turnoutList.clear();
        trackList.clear();
        pointList.clear();
        xingList.clear();
        slipList.clear();
        turntableList.clear();

        return true;
    } //deletePanel

    /**
     * Control whether target panel items are editable. Does this by invoking
     * the {@link Editor#setAllEditable} function of the parent class. This also
     * controls the relevant pop-up menu items (which are the primary way that
     * items are edited).
     *
     * @param editable true for editable.
     */
    @Override
    public void setAllEditable(boolean editable) {
        int restoreScroll = _scrollState;

        super.setAllEditable(editable);

        if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
            if (editable) {
                createFloatingEditToolBox();
            } else {
                deleteFloatingEditToolBox();
            }
        } else {
            editToolBarContainer.setVisible(editable);
        }
        setShowHidden(editable);

        if (editable) {
            setScroll(Editor.SCROLL_BOTH);
            _scrollState = restoreScroll;
        } else {
            setScroll(_scrollState);
        }

        //these may not be set up yet...
        if (helpBarPanel != null) {
            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(editable && getShowHelpBar());
            } else {
                helpBarPanel.setVisible(editable && getShowHelpBar());
            }
        }
        awaitingIconChange = false;
        editModeCheckBoxMenuItem.setSelected(editable);
        redrawPanel();
    }

    /**
     * Control whether panel items are positionable. Markers are always
     * positionable.
     *
     * @param state true for positionable.
     */
    @Override
    public void setAllPositionable(boolean state) {
        super.setAllPositionable(state);

        for (Positionable p : markerImage) {
            p.setPositionable(true);
        }
    } //setAllPositionable

    /**
     * Control whether target panel items are controlling layout items. Does
     * this by invoke the {@link Positionable#setControlling} function of each
     * item on the target panel. This also controls the relevant pop-up menu
     * items.
     *
     * @param state true for controlling.
     */
    public void setTurnoutAnimation(boolean state) {
        if (animationCheckBoxMenuItem.isSelected() != state) {
            animationCheckBoxMenuItem.setSelected(state);
        }

        if (animatingLayout != state) {
            animatingLayout = state;
            redrawPanel();
        }
    } //setTurnoutAnimation

    public boolean isAnimating() {
        return animatingLayout;
    }

    public int getLayoutWidth() {
        return panelWidth;
    }

    public int getLayoutHeight() {
        return panelHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getUpperLeftX() {
        return upperLeftX;
    }

    public int getUpperLeftY() {
        return upperLeftY;
    }

    public boolean getScroll() {
        //deprecated but kept to allow opening files
        //on version 2.5.1 and earlier
        if (_scrollState == Editor.SCROLL_NONE) {
            return false;
        } else {
            return true;
        }
    } //getScroll

    public int setGridSize(int newSize) {
        gridSize1st = newSize;
        return gridSize1st;
    } //setGridSize

    public int getGridSize() {
        return gridSize1st;
    } //getGridSize

    public int setGridSize2nd(int newSize) {
        gridSize2nd = newSize;
        return gridSize2nd;
    } //setGridSize

    public int getGridSize2nd() {
        return gridSize2nd;
    } //getGridSize

    public int getMainlineTrackWidth() {
        return (int) mainlineTrackWidth;
    } //getMainlineTrackWidth

    public int getSideTrackWidth() {
        return (int) sideTrackWidth;
    } //getSideTrackWidth

    public double getXScale() {
        return xScale;
    }

    public double getYScale() {
        return yScale;
    }

    public String getDefaultTrackColor() {
        return ColorUtil.colorToString(defaultTrackColor);
    }

    public String getDefaultOccupiedTrackColor() {
        return ColorUtil.colorToString(defaultOccupiedTrackColor);
    }

    public String getDefaultAlternativeTrackColor() {
        return ColorUtil.colorToString(defaultAlternativeTrackColor);
    }

    public String getDefaultTextColor() {
        return ColorUtil.colorToString(defaultTextColor);
    }

    public String getTurnoutCircleColor() {
        return ColorUtil.colorToString(turnoutCircleColor);
    }

    public int getTurnoutCircleSize() {
        return turnoutCircleSize;
    }

    public boolean getTurnoutDrawUnselectedLeg() {
        return turnoutDrawUnselectedLeg;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public boolean getShowHelpBar() {
        return showHelpBar;
    }

    public boolean getDrawGrid() {
        return drawGrid;
    }

    public boolean getSnapOnAdd() {
        return snapToGridOnAdd;
    }

    public boolean getSnapOnMove() {
        return snapToGridOnMove;
    }

    public boolean getAntialiasingOn() {
        return antialiasingOn;
    }

    public boolean getHighlightSelectedBlock() {
        return highlightSelectedBlockFlag;
    }

    public boolean getTurnoutCircles() {
        return turnoutCirclesWithoutEditMode;
    }

    public boolean getTooltipsNotEdit() {
        return tooltipsWithoutEditMode;
    }

    public boolean getTooltipsInEdit() {
        return tooltipsInEditMode;
    }

    public boolean getAutoBlockAssignment() {
        return autoAssignBlocks;
    }

    public void setLayoutDimensions(int windowWidth, int windowHeight, int windowX, int windowY, int panelWidth, int panelHeight) {
        setLayoutDimensions(windowWidth, windowHeight, windowX, windowY, panelWidth, panelHeight, false);
    }

    public void setLayoutDimensions(int windowWidth, int windowHeight, int windowX, int windowY, int panelWidth, int panelHeight, boolean merge) {
        upperLeftX = windowX;
        upperLeftY = windowY;
        setLocation(upperLeftX, upperLeftY);

        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        setSize(windowWidth, windowHeight);

        Rectangle2D panelBounds = new Rectangle2D.Double(0.0, 0.0, panelWidth, panelHeight);

        if (merge) {
            panelBounds.add(calculateMinimumLayoutBounds());
        }
        setPanelBounds(panelBounds);
    } //setLayoutDimensions

    public Rectangle2D getPanelBounds() {
        return new Rectangle2D.Double(0.0, 0.0, panelWidth, panelHeight);
    }

    public void setPanelBounds(Rectangle2D newBounds) {
        //TODO: dead-code strip this
        // note: upperLeft (X&Y) are WINDOW locations
//        upperLeftX = (int) newRectangle.getX();
//        upperLeftY = (int) newRectangle.getY();
//        setLocation(upperLeftX, upperLeftY);
//        if (upperLeftX < 0 || upperLeftY < 0) {
//            log.error("negative upperLeft X or Y");
//        }

        // make sure the origin is at {0, 0}
        newBounds = MathUtil.offset(newBounds, -newBounds.getX(), -newBounds.getY());

        panelWidth = (int) newBounds.getWidth();
        panelHeight = (int) newBounds.getHeight();
        setTargetPanelSize(panelWidth, panelHeight);

        log.debug("setPanelBounds(({})", newBounds);
    }

    // this will grow the panel bounds based on items added to the layout
    public Rectangle2D unionToPanelBounds(Rectangle2D bounds) {
        Rectangle2D result = getPanelBounds();
        result.add(bounds);
        setPanelBounds(result);
        return result;
    }

    public void setMainlineTrackWidth(int w) {
        mainlineTrackWidth = w;
    }

    public void setSideTrackWidth(int w) {
        sideTrackWidth = w;
    }

    public void setDefaultTrackColor(@Nonnull String colorName) {
        defaultTrackColor = ColorUtil.stringToColor(colorName);
        setOptionMenuTrackColor();
    }

    public void setDefaultOccupiedTrackColor(@Nonnull String colorName) {
        defaultOccupiedTrackColor = ColorUtil.stringToColor(colorName);
        setOptionMenuTrackColor();
    }

    public void setDefaultAlternativeTrackColor(@Nonnull String colorName) {
        defaultAlternativeTrackColor = ColorUtil.stringToColor(colorName);
        setOptionMenuTrackColor();
    }

    public void setTurnoutCircleColor(@Nonnull String colorName) {
        if (colorName.equals("track")) {
            colorName = getDefaultTrackColor();
        }
        turnoutCircleColor = ColorUtil.stringToColor(colorName);
        setOptionMenuTurnoutCircleColor();
    }

    public void setTurnoutCircleSize(int size) {
        //this is an int
        turnoutCircleSize = size;

        //these are doubles
        circleRadius = SIZE * size;
        circleDiameter = 2.0 * circleRadius;

        setOptionMenuTurnoutCircleSize();
    } //setTurnoutCircleSize

    public void setTurnoutDrawUnselectedLeg(boolean state) {
        if (turnoutDrawUnselectedLeg != state) {
            turnoutDrawUnselectedLeg = state;
            turnoutDrawUnselectedLegCheckBoxMenuItem.setSelected(turnoutDrawUnselectedLeg);
        }
    } //setTurnoutDrawUnselectedLeg

    public void setDefaultTextColor(@Nonnull String colorName) {
        defaultTextColor = ColorUtil.stringToColor(colorName);
        setOptionMenuTextColor();
    }

    public void setDefaultBackgroundColor(@Nonnull String colorName) {
        defaultBackgroundColor = ColorUtil.stringToColor(colorName);
        setOptionMenuBackgroundColor();
    }

    public void setXScale(double xSc) {
        xScale = xSc;
    }

    public void setYScale(double ySc) {
        yScale = ySc;
    }

    public void setLayoutName(@Nonnull String name) {
        layoutName = name;
    }

    public void setShowHelpBar(boolean state) {
        if (showHelpBar != state) {
            showHelpBar = state;

            //these may not be set up yet...
            if (showHelpCheckBoxMenuItem != null) {
                showHelpCheckBoxMenuItem.setSelected(showHelpBar);
            }

            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                if (floatEditHelpPanel != null) {
                    floatEditHelpPanel.setVisible(isEditable() && showHelpBar);
                }
            } else {
                if (helpBarPanel != null) {
                    helpBarPanel.setVisible(isEditable() && showHelpBar);

                }
            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".showHelpBar", showHelpBar);
            });
        }
    } //setShowHelpBar

    public void setDrawGrid(boolean state) {
        if (drawGrid != state) {
            drawGrid = state;
            showGridCheckBoxMenuItem.setSelected(drawGrid);
        }
    } //setDrawGrid

    public void setSnapOnAdd(boolean state) {
        if (snapToGridOnAdd != state) {
            snapToGridOnAdd = state;
            snapToGridOnAddCheckBoxMenuItem.setSelected(snapToGridOnAdd);
        }
    } //setSnapOnAdd

    public void setSnapOnMove(boolean state) {
        if (snapToGridOnMove != state) {
            snapToGridOnMove = state;
            snapToGridOnMoveCheckBoxMenuItem.setSelected(snapToGridOnMove);
        }
    } //setSnapOnMove

    public void setAntialiasingOn(boolean state) {
        if (antialiasingOn != state) {
            antialiasingOn = state;

            //this may not be set up yet...
            if (antialiasingOnCheckBoxMenuItem != null) {
                antialiasingOnCheckBoxMenuItem.setSelected(antialiasingOn);

            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class
            ).ifPresent((prefsMgr) -> {
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".antialiasingOn", antialiasingOn);
            });
        }
    } //setAntialiasingOn

    //enable/disable using the "Extra" color to highlight the selected block
    public void setHighlightSelectedBlock(boolean state) {
        if (highlightSelectedBlockFlag != state) {
            highlightSelectedBlockFlag = state;

            //this may not be set up yet...
            if (highlightSelectedBlockCheckBoxMenuItem != null) {
                highlightSelectedBlockCheckBoxMenuItem.setSelected(highlightSelectedBlockFlag);

            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class
            ).ifPresent((prefsMgr) -> {
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".highlightSelectedBlock", highlightSelectedBlockFlag);
            });

            if (highlightSelectedBlockFlag) {
                //use the "Extra" color to highlight the selected block
                if (!highlightBlockInComboBox(blockIDComboBox)) {
                    highlightBlockInComboBox(blockContentsComboBox);
                }
            } else {
                //undo using the "Extra" color to highlight the selected block
                highlightBlock(null);
            }
        }
    } //setHighlightSelectedBlock

    //
    //highlight the block selected by the specified combo Box
    //
    private boolean highlightBlockInComboBox(@Nonnull JmriBeanComboBox inComboBox) {
        boolean result = false;

        if (null != inComboBox) {
            jmri.Block b = (jmri.Block) inComboBox.getNamedBean();
            result = highlightBlock(b);
        }
        return result;
    } //highlightBlockInComboBox

    //
    //
    //
    private boolean highlightBlock(@Nullable jmri.Block inBlock) {
        boolean result = false; //assume failure (pessimist!)

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);

        jmri.Manager m = blockIDComboBox.getManager();
        List<NamedBean> l = m.getNamedBeanList();

        for (NamedBean nb : l) {
            jmri.Block b = (jmri.Block) nb;
            LayoutBlock lb = lbm.getLayoutBlock(b);

            if (lb != null) {
                boolean enable = ((null != inBlock) && b.equals(inBlock));
                lb.setUseExtraColor(enable);
                result |= enable;
            }
        }
        return result;
    } //highlightBlock

    public void setTurnoutCircles(boolean state) {
        if (turnoutCirclesWithoutEditMode != state) {
            turnoutCirclesWithoutEditMode = state;
            if (turnoutCirclesOnCheckBoxMenuItem != null) {
                turnoutCirclesOnCheckBoxMenuItem.setSelected(turnoutCirclesWithoutEditMode);
            }
        }
    } //setTurnoutCircles

    public void setAutoBlockAssignment(boolean boo) {
        if (autoAssignBlocks != boo) {
            autoAssignBlocks = boo;
            if (autoAssignBlocksCheckBoxMenuItem != null) {
                autoAssignBlocksCheckBoxMenuItem.setSelected(autoAssignBlocks);
            }
        }
    } //setAutoBlockAssignment

    public void setTooltipsNotEdit(boolean state) {
        if (tooltipsWithoutEditMode != state) {
            tooltipsWithoutEditMode = state;
            setTooltipSubMenu();
        }
    } //setTooltipsNotEdit

    public void setTooltipsInEdit(boolean state) {
        if (tooltipsInEditMode != state) {
            tooltipsInEditMode = state;
            setTooltipSubMenu();
        }
    } //setTooltipsInEdit

    private void setTooltipSubMenu() {
        if (tooltipNone != null) {
            tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
            tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
            tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
            tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        }
    } //setTooltipSubMenu

    //accessor routines for turnout size parameters
    public void setTurnoutBX(double bx) {
        turnoutBX = bx;
        setDirty();
    }

    public double getTurnoutBX() {
        return turnoutBX;
    }

    public void setTurnoutCX(double cx) {
        turnoutCX = cx;
        setDirty();
    }

    public double getTurnoutCX() {
        return turnoutCX;
    }

    public void setTurnoutWid(double wid) {
        turnoutWid = wid;
        setDirty();
    }

    public double getTurnoutWid() {
        return turnoutWid;
    }

    public void setXOverLong(double lg) {
        xOverLong = lg;
        setDirty();
    }

    public double getXOverLong() {
        return xOverLong;
    }

    public void setXOverHWid(double hwid) {
        xOverHWid = hwid;
        setDirty();
    }

    public double getXOverHWid() {
        return xOverHWid;
    }

    public void setXOverShort(double sh) {
        xOverShort = sh;
        setDirty();
    }

    public double getXOverShort() {
        return xOverShort;
    }

    //reset turnout sizes to program defaults
    private void resetTurnoutSize() {
        turnoutBX = LayoutTurnout.turnoutBXDefault;
        turnoutCX = LayoutTurnout.turnoutCXDefault;
        turnoutWid = LayoutTurnout.turnoutWidDefault;
        xOverLong = LayoutTurnout.xOverLongDefault;
        xOverHWid = LayoutTurnout.xOverHWidDefault;
        xOverShort = LayoutTurnout.xOverShortDefault;
        setDirty();
    } //resetTurnoutSize

    public void setDirectTurnoutControl(boolean boo) {
        useDirectTurnoutControl = boo;
        useDirectTurnoutControlCheckBoxMenuItem.setSelected(useDirectTurnoutControl);
    }

    public boolean getDirectTurnoutControl() {
        return useDirectTurnoutControl;
    }

    //final initialization routine for loading a LayoutEditor
    public void setConnections() {
        //initialize TrackSegments if any
        for (TrackSegment ts : trackList) {
            ts.setObjects(this);
        }

        //initialize PositionablePoints if any
        for (PositionablePoint p : pointList) {
            p.setObjects(this);
        }

        //initialize LevelXings if any
        for (LevelXing x : xingList) {
            x.setObjects(this);
        }

        //initialize LayoutSlip if any
        for (LayoutSlip sl : slipList) {
            sl.setObjects(this);
        }

        //initialize LayoutTurntables if any
        for (LayoutTurntable t : turntableList) {
            t.setObjects(this);
        }

        //initialize LayoutTurnouts if any
        for (LayoutTurnout l : turnoutList) {
            l.setObjects(this);
        }
        auxTools.initializeBlockConnectivity();
        log.debug("Initializing Block Connectivity for {}", layoutName);

        //reset the panel changed bit
        resetDirty();
    } //setConnections

    //these are convenience methods to return rectangles
    //to do point-in-rect (hit) testing
    //compute the control point rect at inPoint
    public Rectangle2D trackControlPointRectAt(@Nonnull Point2D inPoint) {
        return new Rectangle2D.Double(
                inPoint.getX() - LayoutTrack.controlPointSize,
                inPoint.getY() - LayoutTrack.controlPointSize,
                LayoutTrack.controlPointSize2, LayoutTrack.controlPointSize2);
    } //controlPointRectAt

    //compute the turnout circle rect at inPoint
    public Rectangle2D trackControlCircleRectAt(@Nonnull Point2D inPoint) {
        return new Rectangle2D.Double(inPoint.getX() - circleRadius,
                inPoint.getY() - circleRadius, circleDiameter, circleDiameter);
    }

    //compute the turnout circle at inPoint (used for drawing)
    public Ellipse2D trackControlCircleAt(@Nonnull Point2D inPoint) {
        return new Ellipse2D.Double(inPoint.getX() - circleRadius,
                inPoint.getY() - circleRadius, circleDiameter, circleDiameter);
    }

    /**
     * Special internal class to allow drawing of layout to a JLayeredPane This
     * is the 'target' pane where the layout is displayed
     */
    @Override
    protected void paintTargetPanel(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //drawPositionableLabelBorder(g2);
        //Optional antialising, to eliminate (reduce) staircase on diagonal lines
        if (antialiasingOn) {
            g2.setRenderingHints(antialiasing);
        }

        if (isEditable()) {
            if (getDrawGrid()) {
                drawPanelGrid(g2);
            }
            drawHiddenTrackSegments(g2);
        }

        drawDashedTrackSegments(g2, false); //non-mainline
        drawDashedTrackSegments(g2, true); //mainline

        drawSolidTrackSegments(g2, false); //non-mainline
        drawSolidTrackSegments(g2, true); //mainline
        drawPoints(g2);

        drawTurnouts(g2);
        drawXings(g2);
        drawSlips(g2);
        drawTurntables(g2);

        drawTrackInProgress(g2);

        // things that only get drawn in edit mode
        if (isEditable()) {
            drawPointsEditControls(g2);

            drawTurnoutEditControls(g2);
            drawXingEditControls(g2);
            drawSlipEditControls(g2);
            drawTurntableEditControls(g2);
            drawTrackSegmentEditControls(g2);

            drawSelectionRect(g2);

            drawMemoryRects(g2);
            drawBlockContentsRects(g2);

            if (allControlling()) {
                drawTurnoutControls(g2);
                drawSlipControls(g2);
                drawTurntableControls(g2);
            }
            highLightSelection(g2);
        } else if (turnoutCirclesWithoutEditMode) {
            if (allControlling()) {
                drawTurnoutControls(g2);
                drawSlipControls(g2);
                drawTurntableControls(g2);
            }
        }
    } //paintTargetPanel

    boolean main = true;
    float trackWidth = sideTrackWidth;

    //had to make this protected so the LayoutTrack classes could access it
    //also returned the current value of trackWidth for the callers to use
    protected float setTrackStrokeWidth(@Nonnull Graphics2D g2, boolean need) {
        if (main != need) {
            main = need;

            //change track stroke width
            trackWidth = main ? mainlineTrackWidth : sideTrackWidth;
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        }
        return trackWidth;
    } //setTrackStrokeWidth

    private void drawTurnouts(@Nonnull Graphics2D g2) {
        // loop over all turnouts
        for (LayoutTurnout t : turnoutList) {
            if (!t.isHidden() || isEditable()) {
                t.draw(g2);
            }
        }
    } //drawTurnouts

    private void drawXings(@Nonnull Graphics2D g2) {
        // loop over all level crossings
        for (LevelXing x : xingList) {
            if (!(x.isHidden() && !isEditable())) {
                x.draw(g2);
            }
        }
    } //drawXings

    private void drawSlips(@Nonnull Graphics2D g2) {
        for (LayoutSlip sl : slipList) {
            sl.draw(g2);
        }
    } //drawSlips

    private void drawTurnoutControls(@Nonnull Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(turnoutCircleColor);
        // loop over all turnouts
        boolean editable = isEditable();
        for (LayoutTurnout t : turnoutList) {
            if (editable || !(t.isHidden() || t.isDisabled())) {
                t.drawControls(g2);
            }
        }
    } //drawTurnoutControls

    private void drawSlipControls(@Nonnull Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(turnoutCircleColor);
        // loop over all slips
        boolean editable = isEditable();
        for (LayoutSlip sl : slipList) {
            if (editable || !(sl.isHidden() || sl.isDisabled())) {
                sl.drawControls(g2);
            }
        }
    } //drawSlipControls

    private void drawTurnoutEditControls(@Nonnull Graphics2D g2) {
        // loop over all turnouts
        for (LayoutTurnout t : turnoutList) {
            g2.setColor(turnoutCircleColor);
            t.drawEditControls(g2);
        }
    } //drawTurnoutEditControls

    private void drawTurntables(@Nonnull Graphics2D g2) {
        // loop over all layout turntables
        for (LayoutTurntable lt : turntableList) {
            lt.draw(g2);
        }
    } //drawTurntables

    private void drawTurntableControls(@Nonnull Graphics2D g2) {
        // loop over all layout turntables
        for (LayoutTurntable lt : turntableList) {
            lt.drawControls(g2);
        }
    } //drawTurntableControls

    private void drawXingEditControls(@Nonnull Graphics2D g2) {
        // loop over all level crossings
        for (LevelXing x : xingList) {
            x.drawEditControls(g2);
        }
    } //drawXingEditControls

    private void drawSlipEditControls(@Nonnull Graphics2D g2) {
        // loop over all slips
        for (LayoutSlip sl : slipList) {
            if (!(sl.isHidden() && !isEditable())) {
                g2.setColor(turnoutCircleColor);
                sl.drawEditControls(g2);
            }
        }
    } //drawSlipEditControls

    private void drawTurntableEditControls(@Nonnull Graphics2D g2) {
        // loop over all turntables
        for (LayoutTurntable x : turntableList) {
            x.drawEditControls(g2);
        }
    } //drawTurntableEditControls

    private void drawHiddenTrackSegments(@Nonnull Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        setTrackStrokeWidth(g2, false);
        for (TrackSegment ts : trackList) {
            if (ts.isHidden()) {
                ts.drawHidden(g2);
            }
        }
    } //drawHiddenTrackSegments

    private void drawDashedTrackSegments(@Nonnull Graphics2D g2, boolean mainline) {
        setTrackStrokeWidth(g2, mainline);
        for (TrackSegment ts : trackList) {
            ts.drawDashed(g2, mainline);
        }
    } //drawDashedTrackSegments

    // drawHidden all track segments which are not hidden, not dashed, and that match the mainline parm
    private void drawSolidTrackSegments(@Nonnull Graphics2D g2, boolean mainline) {
        setTrackStrokeWidth(g2, mainline);
        for (TrackSegment ts : trackList) {
            ts.drawSolid(g2, mainline);
        }
    } //drawSolidTrackSegments

    private void drawTrackInProgress(@Nonnull Graphics2D g2) {
        //check for segment in progress
        if (isEditable() && (beginObject != null) && trackButton.isSelected()) {
            g2.setColor(defaultTrackColor);
            setTrackStrokeWidth(g2, false);
            g2.draw(new Line2D.Double(beginLocation, currentLocation));
        }
    } //drawTrackInProgress

    private void drawTrackSegmentEditControls(@Nonnull Graphics2D g2) {
        // loop over all track segments
        for (TrackSegment ts : trackList) {
            ts.drawEditControls(g2);
        }
    } //drawTrackSegmentEditControls

    private void drawPoints(@Nonnull Graphics2D g2) {
        for (PositionablePoint p : pointList) {
            p.draw(g2);
        } // for (PositionablePoint p : pointList)
    } //drawPoints

    private void drawPointsEditControls(@Nonnull Graphics2D g2) {
        for (PositionablePoint p : pointList) {
            p.drawControls(g2);
        } // for (PositionablePoint p : pointList)
    } //drawPointsEditControls

    private Rectangle2D getSelectionRect() {
        double selX = Math.min(selectionX, selectionX + selectionWidth);
        double selY = Math.min(selectionY, selectionY + selectionHeight);
        Rectangle2D result = new Rectangle2D.Double(selX, selY,
                Math.abs(selectionWidth), Math.abs(selectionHeight));
        return result;
    }

    public void setSelectionRect(Rectangle2D selectionRect) {
        selectionX = selectionRect.getX();
        selectionY = selectionRect.getY();
        selectionWidth = selectionRect.getWidth();
        selectionHeight = selectionRect.getHeight();

        clearSelectionGroups();
        createSelectionGroups();
        selectionActive = true;
        redrawPanel();
    }

    private void drawSelectionRect(@Nonnull Graphics2D g2) {
        if (selectionActive && (selectionWidth != 0.0) && (selectionHeight != 0.0)) {
            java.awt.Stroke stroke = g2.getStroke();
            Color color = g2.getColor();

            g2.setColor(new Color(204, 207, 88));
            g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

            g2.draw(getSelectionRect());

            g2.setColor(color);
            g2.setStroke(stroke);
        }
    } //drawSelectionRect

    private void drawMemoryRects(@Nonnull Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        for (MemoryIcon l : memoryLabelList) {
            g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height));
        }
    } //drawMemoryRects

    private void drawBlockContentsRects(@Nonnull Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        for (BlockContentsIcon l : blockContentsLabelList) {
            g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height));
        }
    } //drawBlockContentsRects

    private void drawPanelGrid(@Nonnull Graphics2D g2) {
        int wideMod = gridSize1st * gridSize2nd;
        int wideMin = gridSize1st / 2;

        // granulize puts these on gridSize1st increments
        double minX = MathUtil.granulize(upperLeftX, gridSize1st);
        double minY = MathUtil.granulize(upperLeftY, gridSize1st);
        double maxX = MathUtil.granulize(panelWidth + upperLeftX, gridSize1st);
        double maxY = MathUtil.granulize(panelHeight + upperLeftY, gridSize1st);

        Point2D startPt = new Point2D.Double(0.0, gridSize1st);
        Point2D stopPt = new Point2D.Double(maxX, gridSize1st);
        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2.setColor(Color.gray);
        g2.setStroke(narrow);

        //draw horizontal lines
        double pix = gridSize1st;

        while (pix < maxY) {
            startPt.setLocation(0.0, pix);
            stopPt.setLocation(maxX, pix);

            if ((((int) pix) % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
            pix += gridSize1st;
        }

        //draw vertical lines
        pix = gridSize1st;

        while (pix < maxX) {
            startPt.setLocation(pix, 0.0);
            stopPt.setLocation(pix, maxY);

            if ((((int) pix) % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
            pix += gridSize1st;
        }
    } //drawPanelGrid

    @Override
    protected boolean showAlignPopup(@Nonnull Positionable l) {
        return false;
    }

    @Override
    public void showToolTip(@Nonnull Positionable selection, @Nonnull MouseEvent event) {
        ToolTip tip = selection.getToolTip();
        tip.setLocation(selection.getX() + selection.getWidth() / 2, selection.getY() + selection.getHeight());
        tip.setText(selection.getNameString());
        setToolTip(tip);
    } //showToolTip

    @Override
    public void addToPopUpMenu(@Nonnull NamedBean nb, @Nonnull JMenuItem item, int menu) {
        if ((nb == null) || (item == null)) {
            return;
        }

        if (nb instanceof Sensor) {
            for (SensorIcon si : sensorList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si.getPopupUtility().addEditPopUpMenu(item);
                            si.getPopupUtility().addViewPopUpMenu(item);
                    } //switch
                }
            }
        } else if (nb instanceof SignalHead) {
            for (SignalHeadIcon si : signalList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si.getPopupUtility().addEditPopUpMenu(item);
                            si.getPopupUtility().addViewPopUpMenu(item);
                    } //switch
                }
            }
        } else if (nb instanceof SignalMast) {
            for (SignalMastIcon si : signalMastList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si.getPopupUtility().addEditPopUpMenu(item);
                            si.getPopupUtility().addViewPopUpMenu(item);
                    } //switch
                }
            }
        } else if (nb instanceof jmri.Block) {
            for (BlockContentsIcon si : blockContentsLabelList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si.getPopupUtility().addEditPopUpMenu(item);
                            si.getPopupUtility().addViewPopUpMenu(item);
                    } //switch
                }
            }
        } else if (nb instanceof Memory) {
            for (MemoryIcon si : memoryLabelList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si.getPopupUtility().addEditPopUpMenu(item);
                            si.getPopupUtility().addViewPopUpMenu(item);
                    } //switch
                }
            }
        } else if (nb instanceof Turnout) {
            for (LayoutTurnout ti : turnoutList) {
                if (ti.getTurnout().equals(nb)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            ti.addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            ti.addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            ti.addEditPopUpMenu(item);
                            ti.addViewPopUpMenu(item);
                    } //switch
                    break;
                }
            }

            for (LayoutSlip sl : slipList) {
                if ((sl.getTurnout() == nb) || (sl.getTurnoutB() == nb)) {
                    switch (menu) {
                        case Editor.VIEWPOPUPONLY: {
                            sl.addViewPopUpMenu(item);
                            break;
                        }

                        case Editor.EDITPOPUPONLY: {
                            sl.addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            sl.addEditPopUpMenu(item);
                            sl.addViewPopUpMenu(item);
                    } //switch
                    break;
                }
            }
        }
    } //addToPopUpMenu

    @Override
    public String toString() {
        return String.format("LayoutEditor: %s", getLayoutName());
    }

    @Override
    public void vetoableChange(@Nonnull java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();

        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoInUseLayoutEditorHeader", toString())); //IN18N
            message.append("<ul>");
            boolean found = false;

            if (nb instanceof SignalHead) {
                if (containsSignalHead((SignalHead) nb)) {
                    found = true;
                    message.append("<li>");
                    message.append(Bundle.getMessage("VetoSignalHeadIconFound"));
                    message.append("</li>");
                }
                LayoutTurnout lt = finder.findLayoutTurnoutByBean(nb);

                if (lt != null) {
                    message.append("<li>");
                    message.append(Bundle.getMessage("VetoSignalHeadAssignedToTurnout", lt.getTurnoutName()));
                    message.append("</li>");
                }
                PositionablePoint p = finder.findPositionablePointByBean(nb);

                if (p != null) {
                    message.append("<li>");
                    // Need to expand to get the names of blocks
                    message.append(Bundle.getMessage("VetoSignalHeadAssignedToPoint"));
                    message.append("</li>");
                }
                LevelXing lx = finder.findLevelXingByBean(nb);

                if (lx != null) {
                    message.append("<li>");
                    //Need to expand to get the names of blocks
                    message.append(Bundle.getMessage("VetoSignalHeadAssignedToLevelXing"));
                    message.append("</li>");
                }
                LayoutSlip ls = finder.findLayoutSlipByBean(nb);

                if (ls != null) {
                    message.append("<li>");
                    message.append(Bundle.getMessage("VetoSignalHeadAssignedToLayoutSlip", ls.getTurnoutName()));
                    message.append("</li>");
                }
            } else if (nb instanceof Turnout) {
                LayoutTurnout lt = finder.findLayoutTurnoutByBean(nb);

                if (lt != null) {
                    found = true;
                    message.append("<li>");
                    message.append(Bundle.getMessage("VetoTurnoutIconFound"));
                    message.append("</li>");
                }

                for (LayoutTurnout t : turnoutList) {
                    if (t.getLinkedTurnoutName() != null) {
                        String uname = nb.getUserName();

                        if (nb.getSystemName().equals(t.getLinkedTurnoutName())
                                || ((uname != null) && uname.equals(t.getLinkedTurnoutName()))) {
                            found = true;
                            message.append("<li>");
                            message.append(Bundle.getMessage("VetoLinkedTurnout", t.getTurnoutName()));
                            message.append("</li>");
                        }
                    }

                    if (nb.equals(t.getSecondTurnout())) {
                        found = true;
                        message.append("<li>");
                        message.append(Bundle.getMessage("VetoSecondTurnout", t.getTurnoutName()));
                        message.append("</li>");
                    }
                }
                LayoutSlip ls = finder.findLayoutSlipByBean(nb);

                if (ls != null) {
                    found = true;
                    message.append("<li>");
                    message.append(Bundle.getMessage("VetoSlipIconFound", ls.getDisplayName()));
                    message.append("</li>");
                }

                for (LayoutTurntable lx : turntableList) {
                    if (lx.isTurnoutControlled()) {
                        for (int i = 0; i < lx.getNumberRays(); i++) {
                            if (nb.equals(lx.getRayTurnout(i))) {
                                found = true;
                                message.append("<li>");
                                message.append(Bundle.getMessage("VetoRayTurntableControl", lx.getId()));
                                message.append("</li>");
                                break;
                            }
                        }
                    }
                }
            }

            if (nb instanceof SignalMast) {
                if (containsSignalMast((SignalMast) nb)) {
                    message.append("<li>");
                    message.append("As an Icon");
                    message.append("</li>");
                    found = true;
                }
                String foundelsewhere = findBeanUsage(nb);

                if (foundelsewhere != null) {
                    message.append(foundelsewhere);
                    found = true;
                }
            }

            if (nb instanceof Sensor) {
                int count = 0;

                for (SensorIcon si : sensorList) {
                    if (nb.equals(si.getNamedBean())) {
                        count++;
                        found = true;
                    }
                }

                if (count > 0) {
                    message.append("<li>");
                    message.append(String.format("As an Icon %s times", count));
                    message.append("</li>");
                }
                String foundelsewhere = findBeanUsage(nb);

                if (foundelsewhere != null) {
                    message.append(foundelsewhere);
                    found = true;
                }
            }

            if (nb instanceof Memory) {
                for (MemoryIcon si : memoryLabelList) {
                    if (nb.equals(si.getMemory())) {
                        found = true;
                        message.append("<li>");
                        message.append(Bundle.getMessage("VetoMemoryIconFound"));
                        message.append("</li>");
                    }
                }
            }

            if (found) {
                message.append("</ul>");
                message.append(Bundle.getMessage("VetoReferencesWillBeRemoved")); //IN18N
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { //IN18N
            if (nb instanceof SignalHead) {
                removeSignalHead((SignalHead) nb);
                removeBeanRefs(nb);
            }

            if (nb instanceof Turnout) {
                LayoutTurnout lt = finder.findLayoutTurnoutByBean(nb);

                if (lt != null) {
                    lt.setTurnout(null);
                }

                for (LayoutTurnout t : turnoutList) {
                    if (t.getLinkedTurnoutName() != null) {
                        if (t.getLinkedTurnoutName().equals(nb.getSystemName())
                                || ((nb.getUserName() != null) && t.getLinkedTurnoutName().equals(nb.getUserName()))) {
                            t.setLinkedTurnoutName(null);
                        }
                    }

                    if (nb.equals(t.getSecondTurnout())) {
                        t.setSecondTurnout(null);
                    }
                }

                for (LayoutSlip sl : slipList) {
                    if (nb.equals(sl.getTurnout())) {
                        sl.setTurnout(null);
                    }

                    if (nb.equals(sl.getTurnoutB())) {
                        sl.setTurnoutB(null);
                    }
                }

                for (LayoutTurntable lx : turntableList) {
                    if (lx.isTurnoutControlled()) {
                        for (int i = 0; i < lx.getNumberRays(); i++) {
                            if (nb.equals(lx.getRayTurnout(i))) {
                                lx.setRayTurnout(i, null, NamedBean.UNKNOWN);
                            }
                        }
                    }
                }
            }

            if (nb instanceof SignalMast) {
                removeBeanRefs(nb);

                if (containsSignalMast((SignalMast) nb)) {
                    Iterator<SignalMastIcon> icon = signalMastList.iterator();

                    while (icon.hasNext()) {
                        SignalMastIcon i = icon.next();

                        if (i.getSignalMast().equals(nb)) {
                            icon.remove();
                            super.removeFromContents(i);
                        }
                    }
                    setDirty();
                    redrawPanel();
                }
            }

            if (nb instanceof Sensor) {
                removeBeanRefs(nb);
                Iterator<SensorIcon> icon = sensorImage.iterator();

                while (icon.hasNext()) {
                    SensorIcon i = icon.next();

                    if (nb.equals(i.getSensor())) {
                        icon.remove();
                        super.removeFromContents(i);
                    }
                }
                setDirty();
                redrawPanel();
            }

            if (nb instanceof Memory) {
                Iterator<MemoryIcon> icon = memoryLabelList.iterator();

                while (icon.hasNext()) {
                    MemoryIcon i = icon.next();

                    if (nb.equals(i.getMemory())) {
                        icon.remove();
                        super.removeFromContents(i);
                    }
                }
            }
        }
    } //vetoableChange

//    protected void rename(String inFrom, String inTo) {
//        
//    }
    @Override
    public void dispose() {
        if (sensorFrame != null) {
            sensorFrame.dispose();
            sensorFrame = null;
        }
        if (signalFrame != null) {
            signalFrame.dispose();
            signalFrame = null;
        }
        if (iconFrame != null) {
            iconFrame.dispose();
            iconFrame = null;
        }
        super.dispose();
    }

    //initialize logging
    private final static Logger log = LoggerFactory.getLogger(LayoutEditor.class);
}
