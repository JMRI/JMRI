package jmri.jmrit.display.layoutEditor;

import static jmri.util.MathUtil.lerp;
import static jmri.util.MathUtil.midpoint;

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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.JTextComponent;
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
import jmri.jmrit.display.AnalogClock2Display;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.MultiSensorIcon;
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
 * PositionablePoint, Track Segment, and LevelXing objects which are integral
 * subparts of the LayoutEditor class.
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
public class LayoutEditor extends jmri.jmrit.display.panelEditor.PanelEditor implements java.beans.VetoableChangeListener {
    //Defined text resource
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    //Operational instance variables - not saved to disk
    //private jmri.TurnoutManager tm = null;
    private LayoutEditor thisPanel = null;

    //dashed line parameters
    //private static int minNumDashes = 3;
    //private static double maxDashLength = 10;
    private JmriJFrame floatingEditToolBox = null;
    private JScrollPane floatingEditContent = null;
    private JPanel floatEditHelpPanel = null;
    private JPanel editToolBarPanel = null;
    private JScrollPane editToolBarScroll = null;
    private JPanel editToolBarContainer = null;
    private JPanel helpBarPanel = null;
    private JPanel helpBar = new JPanel();

    protected boolean skipIncludedTurnout = false;

    public ArrayList<PositionableLabel> backgroundImage = new ArrayList<PositionableLabel>();   //background images
    public ArrayList<SensorIcon> sensorImage = new ArrayList<SensorIcon>();                     //sensor images
    public ArrayList<SignalHeadIcon> signalHeadImage = new ArrayList<SignalHeadIcon>();         //signal head images
    public ArrayList<LocoIcon> markerImage = new ArrayList<LocoIcon>();                         //marker images
    public ArrayList<PositionableLabel> labelImage = new ArrayList<PositionableLabel>();        //layout positionable label images
    public ArrayList<AnalogClock2Display> clocks = new ArrayList<AnalogClock2Display>();        //fast clocks
    public ArrayList<MultiSensorIcon> multiSensors = new ArrayList<MultiSensorIcon>();          //multi-sensor images

    public LayoutEditorAuxTools auxTools = null;
    private ConnectivityUtil conTools = null;

    private Font toolBarFont = null;

    private ButtonGroup itemGroup = null;

    //top row of radio buttons
    private JLabel turnoutLabel = new JLabel();
    private JRadioButton turnoutRHButton = new JRadioButton(rb.getString("RightHandAbbreviation"));
    private JRadioButton turnoutLHButton = new JRadioButton(rb.getString("LeftHandAbbreviation"));
    private JRadioButton turnoutWYEButton = new JRadioButton(rb.getString("WYEAbbreviation"));
    private JRadioButton doubleXoverButton = new JRadioButton(rb.getString("DoubleCrossOverAbbreviation"));
    private JRadioButton rhXoverButton = new JRadioButton(Bundle.getMessage("RightCrossOver")); //key is also used by Control Panel
                                                                                                //Editor, placed in DisplayBundle
    private JRadioButton lhXoverButton = new JRadioButton(Bundle.getMessage("LeftCrossOver"));  //idem
    private JRadioButton layoutSingleSlipButton = new JRadioButton(rb.getString("LayoutSingleSlip"));
    private JRadioButton layoutDoubleSlipButton = new JRadioButton(rb.getString("LayoutDoubleSlip"));

    //Default flow layout definiitons for JPanels
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
    private JRadioButton levelXingButton = new JRadioButton(rb.getString("LevelCrossing"));
    private JRadioButton trackButton = new JRadioButton(rb.getString("TrackSegment"));

    //2nd row of check boxes
    private JPanel trackSegmentPropertiesPanel = new JPanel(leftRowLayout);
    private JCheckBox mainlineTrack = new JCheckBox(rb.getString("MainlineBox"));
    private JCheckBox dashedLine = new JCheckBox(rb.getString("Dashed"));

    private JLabel blockNameLabel = new JLabel();
    private JmriBeanComboBox blockIDComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JLabel blockSensorNameLabel = new JLabel();
    private JLabel blockSensorLabel = new JLabel(Bundle.getMessage("BeanNameSensor"));
    private JmriBeanComboBox blockSensorComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    //3rd row of radio buttons (and any associated text fields)
    private JLabel nodesLabel = new JLabel();
    private JRadioButton endBumperButton = new JRadioButton(rb.getString("EndBumper"));
    private JRadioButton anchorButton = new JRadioButton(rb.getString("Anchor"));
    private JRadioButton edgeButton = new JRadioButton(rb.getString("EdgeConnector"));

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

    private JRadioButton signalMastButton = new JRadioButton(rb.getString("SignalMastIcon"));
    private JmriBeanComboBox signalMastComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalMastManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton sensorButton = new JRadioButton(rb.getString("SensorIcon"));
    private JmriBeanComboBox sensorComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton signalButton = new JRadioButton(rb.getString("SignalIcon"));
    private JmriBeanComboBox signalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JRadioButton iconLabelButton = new JRadioButton(rb.getString("IconLabel"));

    private JButton changeIconsButton = new JButton(rb.getString("ChangeIcons") + "...");

    public MultiIconEditor sensorIconEditor = null;
    public JFrame sensorFrame;

    public MultiIconEditor signalIconEditor = null;
    public JFrame signalFrame;

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
    private JRadioButtonMenuItem noZoomItem = new JRadioButtonMenuItem(rb.getString("NoZoom"));
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
    private int gridSize = 10;

    //size of point boxes
    private static final double SIZE = 3.0;
    private static final double SIZE2 = SIZE * 2.;  //must be twice SIZE

    //note: although these have been moved to the LayoutTurnout class I'm leaving a copy of them here so
    //that any external use of these won't break. At some point in the future these should be @Deprecated.
    //All JMRI sources have been updated to use the ones in the LayoutTurnout class.
    //defined constants - turnout types
    public static final int RH_TURNOUT = LayoutTurnout.RH_TURNOUT;
    public static final int LH_TURNOUT = LayoutTurnout.LH_TURNOUT;
    public static final int WYE_TURNOUT = LayoutTurnout.WYE_TURNOUT;
    public static final int DOUBLE_XOVER = LayoutTurnout.DOUBLE_XOVER;
    public static final int RH_XOVER = LayoutTurnout.RH_XOVER;
    public static final int LH_XOVER = LayoutTurnout.LH_XOVER;
    public static final int SINGLE_SLIP = LayoutTurnout.SINGLE_SLIP;
    public static final int DOUBLE_SLIP = LayoutTurnout.DOUBLE_SLIP;

    //connection types (see note above)
    public static final int NONE = LayoutTrack.NONE;
    public static final int POS_POINT = LayoutTrack.POS_POINT;
    public static final int TURNOUT_A = LayoutTrack.TURNOUT_A;  //throat for RH, LH, and WYE turnouts
    public static final int TURNOUT_B = LayoutTrack.TURNOUT_B;  //continuing route for RH or LH turnouts
    public static final int TURNOUT_C = LayoutTrack.TURNOUT_C;  //diverging route for RH or LH turnouts
    public static final int TURNOUT_D = LayoutTrack.TURNOUT_D;  //double-crossover or single crossover only
    public static final int LEVEL_XING_A = LayoutTrack.LEVEL_XING_A;
    public static final int LEVEL_XING_B = LayoutTrack.LEVEL_XING_B;
    public static final int LEVEL_XING_C = LayoutTrack.LEVEL_XING_C;
    public static final int LEVEL_XING_D = LayoutTrack.LEVEL_XING_D;
    public static final int TRACK = LayoutTrack.TRACK;
    public static final int TURNOUT_CENTER = LayoutTrack.TURNOUT_CENTER;    //non-connection points should be last
    public static final int LEVEL_XING_CENTER = LayoutTrack.LEVEL_XING_CENTER;
    public static final int TURNTABLE_CENTER = LayoutTrack.TURNTABLE_CENTER;
    public static final int LAYOUT_POS_LABEL = LayoutTrack.LAYOUT_POS_LABEL;
    public static final int LAYOUT_POS_JCOMP = LayoutTrack.LAYOUT_POS_JCOMP;
    public static final int MULTI_SENSOR = LayoutTrack.MULTI_SENSOR;
    public static final int MARKER = LayoutTrack.MARKER;
    public static final int TRACK_CIRCLE_CENTRE = LayoutTrack.TRACK_CIRCLE_CENTRE;
    public static final int SLIP_CENTER = LayoutTrack.SLIP_CENTER;  //
    public static final int SLIP_A = LayoutTrack.SLIP_A;            //offset for slip connection points
    public static final int SLIP_B = LayoutTrack.SLIP_B;            //offset for slip connection points
    public static final int SLIP_C = LayoutTrack.SLIP_C;            //offset for slip connection points
    public static final int SLIP_D = LayoutTrack.SLIP_D;            //offset for slip connection points
    public static final int SLIP_LEFT = LayoutTrack.SLIP_LEFT;
    public static final int SLIP_RIGHT = LayoutTrack.SLIP_RIGHT;
    public static final int TURNTABLE_RAY_OFFSET = LayoutTrack.TURNTABLE_RAY_OFFSET;    //offset for turntable connection points

    protected Color turnoutCircleColor = Color.black;   //matches earlier versions
    protected int turnoutCircleSize = 4;                //matches earlier versions

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
    private JCheckBoxMenuItem editModeItem = null;

    private JRadioButtonMenuItem toolBarSideTopButton = null;
    private JRadioButtonMenuItem toolBarSideLeftButton = null;
    private JRadioButtonMenuItem toolBarSideBottomButton = null;
    private JRadioButtonMenuItem toolBarSideRightButton = null;
    private JRadioButtonMenuItem toolBarSideFloatButton = null;

    private JMenu toolBarFontSizeMenu = new JMenu(Bundle.getMessage("FontSize"));
    private JCheckBoxMenuItem toolBarIsWideItem = new JCheckBoxMenuItem(rb.getString("ToolBarWide"));
    private JMenu dropDownListsDisplayOrderMenu = new JMenu(Bundle.getMessage("DropDownListsDisplayOrder"));

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
    private JCheckBoxMenuItem highlightSelectedBlockItem = null;

    private JCheckBoxMenuItem turnoutCirclesOnItem = null;
    private JCheckBoxMenuItem skipTurnoutItem = null;
    private JCheckBoxMenuItem turnoutDrawUnselectedLegItem = null;
    private JCheckBoxMenuItem hideTrackSegmentConstructionLines = null;
    private JCheckBoxMenuItem useDirectTurnoutControlItem = null;
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
    private int[] turnoutCircleSizes = new int[10];

    private JRadioButtonMenuItem[] trackColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] trackOccupiedColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] trackAlternativeColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] backgroundColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] textColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] turnoutCircleColorMenuItems = new JRadioButtonMenuItem[14];
    private JRadioButtonMenuItem[] turnoutCircleSizeMenuItems = new JRadioButtonMenuItem[10];

    private int trackColorCount = 0;
    private int trackOccupiedColorCount = 0;
    private int trackAlternativeColorCount = 0;
    private int textColorCount = 0;
    private int turnoutCircleColorCount = 0;
    private int turnoutCircleSizeCount = 0;
    private boolean turnoutDrawUnselectedLeg = true;
    private int backgroundColorCount = 0;
    private boolean autoAssignBlocks = false;

    //Selected point information
    private transient Point2D startDel = new Point2D.Double(0.0, 0.0);  //starting delta coordinates
    private Object selectedObject = null;                               //selected object, null if nothing selected
    private Object prevSelectedObject = null;                           //previous selected object, for undo
    private int selectedPointType = 0;                                  //connection type within the selected object
    //private boolean selectedNeedsConnect = false; //true if selected object is unconnected

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED")  //no Serializable support at present
    private Object foundObject = null;                              //found object, null if nothing found

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED")          //no Serializable support at present
    private transient Point2D foundLocation = new Point2D.Double(0.0, 0.0); //location of found object

    private int foundPointType = 0; //connection type within the found object

    @SuppressWarnings("unused")
    private boolean foundNeedsConnect = false;                                  //true if found point needs a connection
    private Object beginObject = null;                                          //begin track segment connection object, null if
                                                                                //none
    private transient Point2D beginLocation = new Point2D.Double(0.0, 0.0);     //location of begin object
    private int beginPointType = 0;                                             //connection type within begin connection object
    private transient Point2D currentLocation = new Point2D.Double(0.0, 0.0);   //current location

    //Lists of items that describe the Layout, and allow it to be drawn
    //Each of the items must be saved to disk over sessions
    public ArrayList<LayoutTurnout> turnoutList = new ArrayList<LayoutTurnout>();       //LayoutTurnouts
    public ArrayList<TrackSegment> trackList = new ArrayList<TrackSegment>();           //TrackSegment list
    public ArrayList<PositionablePoint> pointList = new ArrayList<PositionablePoint>(); //PositionablePoint list
    public ArrayList<LevelXing> xingList = new ArrayList<LevelXing>();                  //LevelXing list
    public ArrayList<LayoutSlip> slipList = new ArrayList<LayoutSlip>();                //Layout slip list
    public ArrayList<LayoutTurntable> turntableList = new ArrayList<LayoutTurntable>(); //Turntable list

    //counts used to determine unique internal names
    private int numAnchors = 0;
    private int numEndBumpers = 0;
    private int numEdgeConnectors = 0;
    private int numTrackSegments = 0;
    private int numLevelXings = 0;
    private int numLayoutSlips = 0;
    private int numLayoutTurnouts = 0;
    private int numLayoutTurntables = 0;

    //Lists of items that facilitate tools and drawings
    public ArrayList<SignalHeadIcon> signalList = new ArrayList<SignalHeadIcon>();                      //Signal Head Icons
    public ArrayList<MemoryIcon> memoryLabelList = new ArrayList<MemoryIcon>();                         //Memory Label List
    public ArrayList<BlockContentsIcon> blockContentsLabelList = new ArrayList<BlockContentsIcon>();    //BlockContentsIcon Label
                                                                                                        //List
    public ArrayList<SensorIcon> sensorList = new ArrayList<SensorIcon>();                              //Sensor Icons
    public ArrayList<SignalMastIcon> signalMastList = new ArrayList<SignalMastIcon>();                  //Signal Head Icons
    public LayoutEditorFindItems finder = new LayoutEditorFindItems(this);

    public LayoutEditorFindItems getFinder() {
        return finder;
    }

    //persistent instance variables - saved to disk with Save Panel
    private int windowWidth = 0;
    private int windowHeight = 0;

    private int panelWidth = 0;
    private int panelHeight = 0;

    private int upperLeftX = 0;
    private int upperLeftY = 0;
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
    private boolean drawGrid = false;
    private boolean snapToGridOnAdd = false;
    private boolean snapToGridOnMove = false;
    private boolean antialiasingOn = false;
    private boolean highlightSelectedBlockFlag = false;

    private boolean turnoutCirclesWithoutEditMode = false;
    private boolean tooltipsWithoutEditMode = false;
    private boolean tooltipsInEditMode = true;

    //turnout size parameters - saved with panel
    private double turnoutBX = LayoutTurnout.turnoutBXDefault;  //RH, LH, WYE
    private double turnoutCX = LayoutTurnout.turnoutCXDefault;
    private double turnoutWid = LayoutTurnout.turnoutWidDefault;
    private double xOverLong = LayoutTurnout.xOverLongDefault;  //DOUBLE_XOVER, RH_XOVER, LH_XOVER
    private double xOverHWid = LayoutTurnout.xOverHWidDefault;
    private double xOverShort = LayoutTurnout.xOverShortDefault;
    private boolean useDirectTurnoutControl = false;    //Uses Left click for closing points, Right click for throwing.

    //saved state of options when panel was loaded or created
    private boolean savedEditMode = true;
    private boolean savedPositionable = true;
    private boolean savedControlLayout = true;
    private boolean savedAnimatingLayout = true;
    private boolean savedShowHelpBar = false;

    //zoom
    private double maxZoom = 6.0;
    private double minZoom = 0.25;
    private double stepUnderOne = 0.25; //when the zoom is less than 1
    private double stepOverOne = 0.5;   //when the zoom is greater than 1
    private double stepOverTwo = 1.0;   //when the zoom is greater than 2

    //Special sub group for color treatment when active
    JPanel blockPropertiesPanel = null;

    //A hash to store string -> KeyEvent constants, used to set keyboard shortcuts per locale
    private HashMap<String, Integer> stringsToVTCodes = new HashMap<String, Integer>();

    //Antialiasing rendering
    private static final RenderingHints antialiasing = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    private enum eToolBarSide {
        eTOP("top"),
        eLEFT("left"),
        eBOTTOM("bottom"),
        eRIGHT("right"),
        eFLOAT("float");

        private String name;
        private static final Map<String, eToolBarSide> ENUM_MAP;

        eToolBarSide(String name) {
            this.name = name;
        }

        //Build an immutable map of String name to enum pairs.
        static {
            Map<String, eToolBarSide> map = new ConcurrentHashMap<String, eToolBarSide>();

            for (eToolBarSide instance : eToolBarSide.values()) {
                map.put(instance.getName(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static eToolBarSide getName(String name) {
            return ENUM_MAP.get(name);
        }

        public String getName() {
            return this.name;
        }
    }

    private eToolBarSide toolBarSide = eToolBarSide.eTOP;
    private boolean toolBarIsWide = true;

    public LayoutEditor() {
        this("My Layout");
    }

    public LayoutEditor(String name) {
        super(name);
        layoutName = name;

        //initialise keycode map
        initStringsToVTCodes();

        //initialize menu bar
        JMenuBar menuBar = new JMenuBar();

        //set up File menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.setMnemonic(stringsToVTCodes.get(rb.getString("MenuFileMnemonic")));
        menuBar.add(fileMenu);
        jmri.configurexml.StoreXmlUserAction store = new jmri.configurexml.StoreXmlUserAction(rbx.getString("MenuItemStore"));
        int primary_modifier = SystemType.isMacOSX() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
        store.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                           stringsToVTCodes.get(rbx.getString("MenuItemStoreAccelerator")), primary_modifier));
        fileMenu.add(store);
        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rbx.getString("DeletePanel"));
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
        itemGroup.  add(turnoutRHButton);
        itemGroup.  add(turnoutLHButton);
        itemGroup.  add(turnoutWYEButton);
        itemGroup.  add(doubleXoverButton);
        itemGroup.  add(rhXoverButton);
        itemGroup.  add(lhXoverButton);
        itemGroup.  add(levelXingButton);
        itemGroup.  add(layoutSingleSlipButton);
        itemGroup.  add(layoutDoubleSlipButton);
        itemGroup.  add(endBumperButton);
        itemGroup.  add(anchorButton);
        itemGroup.  add(edgeButton);
        itemGroup.  add(trackButton);
        itemGroup.  add(multiSensorButton);
        itemGroup.  add(sensorButton);
        itemGroup.  add(signalButton);
        itemGroup.  add(signalMastButton);
        itemGroup.  add(textLabelButton);
        itemGroup.  add(memoryButton);
        itemGroup.  add(blockContentsButton);
        itemGroup.  add(iconLabelButton);

        //This is used to enable/disable property controls depending on which (radio) button is selected
        ActionListener selectionListAction = (ActionEvent a) -> {
                    //turnout properties
                    boolean e = (turnoutRHButton.isSelected() ||
                                             turnoutLHButton.isSelected() ||
                                             turnoutWYEButton.isSelected() ||
                                             doubleXoverButton.isSelected() ||
                                             rhXoverButton.isSelected() ||
                                             lhXoverButton.isSelected() ||
                                             layoutSingleSlipButton.isSelected() ||
                                             layoutDoubleSlipButton.isSelected());
                    log.debug("turnoutPropertiesPanel is " + (e ? "enabled" : "disabled"));
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
                    log.debug("extraTurnoutPanel is " + (e ? "enabled" : "disabled"));

                    for (Component i : extraTurnoutPanel.getComponents()) {
                            i.setEnabled(e);
                    }

                    //track Segment properties
                    e = trackButton.isSelected();
                    log.debug("trackSegmentPropertiesPanel is " + (e ? "enabled" : "disabled"));

                    for (Component i : trackSegmentPropertiesPanel.getComponents()) {
                            i.setEnabled(e);
                    }

                    //block properties
                    e = (turnoutRHButton.isSelected() ||
                             turnoutLHButton.isSelected() ||
                             turnoutWYEButton.isSelected() ||
                             doubleXoverButton.isSelected() ||
                             rhXoverButton.isSelected() ||
                             lhXoverButton.isSelected() ||
                             layoutSingleSlipButton.isSelected() ||
                             layoutDoubleSlipButton.isSelected() ||
                             levelXingButton.isSelected() ||
                             trackButton.isSelected());
                    log.debug("blockPanel is " + (e ? "enabled" : "disabled"));

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
                    e = (sensorButton.isSelected() ||
                             signalButton.isSelected() ||
                             iconLabelButton.isSelected());
                    log.debug("changeIconsButton is " + (e ? "enabled" : "disabled"));
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
        turnoutRHButton.setToolTipText(rb.getString("RHToolTip"));
        turnoutLHButton.setToolTipText(rb.getString("LHToolTip"));
        turnoutWYEButton.setToolTipText(rb.getString("WYEToolTip"));
        doubleXoverButton.setToolTipText(rb.getString("DoubleCrossOverToolTip"));
        rhXoverButton.setToolTipText(rb.getString("RHCrossOverToolTip"));
        lhXoverButton.setToolTipText(rb.getString("LHCrossOverToolTip"));
        layoutSingleSlipButton.setToolTipText(rb.getString("SingleSlipToolTip"));
        layoutDoubleSlipButton.setToolTipText(rb.getString("DoubleSlipToolTip"));

        String turnoutNameString = Bundle.getMessage("Name");
        JLabel turnoutNameLabel = new JLabel(turnoutNameString);
        turnoutNamePanel.add(turnoutNameLabel);

        setupComboBox(turnoutNameComboBox, false, true);
        turnoutNameComboBox.setToolTipText(rb.getString("TurnoutNameToolTip"));
        turnoutNamePanel.add(turnoutNameComboBox);

        setupComboBox(extraTurnoutNameComboBox, false, true);
        extraTurnoutNameComboBox.setToolTipText(rb.getString("SecondTurnoutNameToolTip"));

        //this is enabled/disabled via selectionListAction above
        JLabel extraTurnoutLabel = new JLabel(rb.getString("SecondName"));
        extraTurnoutLabel.setEnabled(false);
        extraTurnoutPanel.  add(extraTurnoutLabel);
        extraTurnoutPanel.  add(extraTurnoutNameComboBox);
        extraTurnoutPanel.setEnabled(false);

        String[] angleStrings = {"-180", "-135", "-90", "-45", "0", "+45", "+90", "+135", "+180"};
        rotationComboBox = new JComboBox<>(angleStrings);
        rotationComboBox.setEditable(true);
        rotationComboBox.setSelectedIndex(4);
        rotationComboBox.setToolTipText(rb.getString("RotationToolTip"));

        JLabel rotationLabel = new JLabel(rb.getString("Rotation"));
        rotationPanel.  add(rotationLabel);
        rotationPanel.  add(rotationComboBox);

        zoomPanel.  add(new JLabel(rb.getString("ZoomLabel") + ":"));
        zoomPanel.  add(zoomLabel);

        Dimension coordSize = xLabel.getPreferredSize();
        coordSize.width *= 2;
        xLabel.setPreferredSize(coordSize);
        yLabel.setPreferredSize(coordSize);

//locationPanel.add(new JLabel("    " + rb.getString("Location") + ":"));
        locationPanel.  add(new JLabel(rb.getString("Location") + ":"));
        locationPanel.  add(new JLabel("{x:"));
        locationPanel.  add(xLabel);
        locationPanel.  add(new JLabel(", y:"));
        locationPanel.  add(yLabel);
        locationPanel.  add(new JLabel("}    "));

        //second row of edit tool bar items
        levelXingButton.setToolTipText(rb.getString("LevelCrossingToolTip"));
        trackButton.setToolTipText(rb.getString("TrackSegmentToolTip"));

        //this is enabled/disabled via selectionListAction above
        trackSegmentPropertiesPanel.add(mainlineTrack);

        mainlineTrack.setSelected(false);
        mainlineTrack.setEnabled(false);
        mainlineTrack.setToolTipText(rb.getString("MainlineCheckBoxTip"));

        trackSegmentPropertiesPanel.add(dashedLine);
        dashedLine.setSelected(false);
        dashedLine.setEnabled(false);
        dashedLine.setToolTipText(rb.getString("DashedCheckBoxTip"));

        //the blockPanel is enabled/disabled via selectionListAction above
        setupComboBox(blockIDComboBox, false, true);
        blockIDComboBox.setToolTipText(rb.getString("BlockIDToolTip"));

        //change the block name
        blockIDComboBox.addActionListener((ActionEvent a) -> {
            //use the "Extra" color to highlight the selected block
            if (highlightSelectedBlockFlag) {
                highlightBlockInComboBox(blockIDComboBox);
            }
            String newName = getUserNameForComboBox(blockIDComboBox);
            LayoutBlock b = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(newName);

            if (b != null) {
    //if there is an occupancy sensor assigned already
                String sensorName = b.getOccupancySensorName();

                if (sensorName.length() > 0) {
    //update the block sensor ComboBox
                    blockSensorComboBox.getEditor().setItem(sensorName);
                } else {
                    blockSensorComboBox.getEditor().setItem("");
                }
            }
        });

        setupComboBox(blockSensorComboBox, true, true);
        blockSensorComboBox.setToolTipText(rb.getString("OccupancySensorToolTip"));

        //third row of edit tool bar items
        endBumperButton.setToolTipText(rb.getString("EndBumperToolTip"));
        anchorButton.setToolTipText(rb.getString("AnchorToolTip"));
        edgeButton.setToolTipText(rb.getString("EdgeConnectorToolTip"));
        textLabelButton.setToolTipText(rb.getString("TextLabelToolTip"));

        textLabelTextField.setToolTipText(rb.getString("TextToolTip"));
        textLabelTextField.setEnabled(false);

        memoryButton.setToolTipText(Bundle.getMessage("MemoryButtonToolTip", Bundle.getMessage("Memory")));

        setupComboBox(textMemoryComboBox, true, false);
        textMemoryComboBox.setToolTipText(rb.getString("MemoryToolTip"));

        blockContentsButton.setToolTipText(rb.getString("BlockContentsButtonToolTip"));

        setupComboBox(blockContentsComboBox, true, false);
        blockContentsComboBox.setToolTipText(rb.getString("BlockContentsButtonToolTip"));

        blockContentsComboBox.addActionListener((ActionEvent a) -> {
            //use the "Extra" color to highlight the selected block
            if (highlightSelectedBlockFlag) {
                highlightBlockInComboBox(blockContentsComboBox);
            }
        });

        //fourth row of edit tool bar items
        //multi sensor…
        multiSensorButton.setToolTipText(rb.getString("MultiSensorToolTip"));

        //Signal Mast & text
        signalMastButton.setToolTipText(rb.getString("SignalMastButtonToolTip"));
        setupComboBox(signalMastComboBox, true, false);

        //sensor icon & text
        sensorButton.setToolTipText(rb.getString("SensorButtonToolTip"));

        setupComboBox(sensorComboBox, true, false);
        sensorComboBox.setToolTipText(rb.getString("SensorIconToolTip"));

        sensorIconEditor = new MultiIconEditor(4);
        sensorIconEditor.   setIcon(0,  Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateActive")),
                                    "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        sensorIconEditor.   setIcon(1,  Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateInactive")),
                                    "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        sensorIconEditor.   setIcon(2,  Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateInconsistent")),
                                    "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.   setIcon(3,  Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateUnknown")),
                                    "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.complete();

        //Signal icon & text
        signalButton.setToolTipText(rb.getString("SignalButtonToolTip"));

        setupComboBox(signalHeadComboBox, true, false);
        signalHeadComboBox.setToolTipText(rb.getString("SignalIconToolTip"));

        signalIconEditor = new MultiIconEditor(10);
        signalIconEditor.   setIcon(0,  "Red:",         "resources/icons/smallschematics/searchlights/left-red-short.gif");
        signalIconEditor.   setIcon(1,  "Flash red:",   "resources/icons/smallschematics/searchlights/left-flashred-short.gif");
        signalIconEditor.   setIcon(2,  "Yellow:",      "resources/icons/smallschematics/searchlights/left-yellow-short.gif");
        signalIconEditor.   setIcon(3,
                                    "Flash yellow:",
                                    "resources/icons/smallschematics/searchlights/left-flashyellow-short.gif");
        signalIconEditor.   setIcon(4,  "Green:",   "resources/icons/smallschematics/searchlights/left-green-short.gif");
        signalIconEditor.   setIcon(5,  "Flash green:",
                                    "resources/icons/smallschematics/searchlights/left-flashgreen-short.gif");
        signalIconEditor.   setIcon(6,  "Dark:",    "resources/icons/smallschematics/searchlights/left-dark-short.gif");
        signalIconEditor.   setIcon(7,  "Held:",    "resources/icons/smallschematics/searchlights/left-held-short.gif");
        signalIconEditor.   setIcon(8,
                                    "Lunar",
                                    "resources/icons/smallschematics/searchlights/left-lunar-short-marker.gif");
        signalIconEditor.   setIcon(9,
                                    "Flash Lunar",
                                    "resources/icons/smallschematics/searchlights/left-flashlunar-short-marker.gif");
        signalIconEditor.complete();

        sensorFrame = new JFrame(rb.getString("EditSensorIcons"));
        sensorFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        sensorFrame.getContentPane().add(sensorIconEditor);
        sensorFrame.pack();

        signalFrame = new JFrame(rb.getString("EditSignalIcons"));
        signalFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);  //no spaces around
                                                                                                                //Label as that
                                                                                                                //breaks html
                                                                                                                //formatting
        signalFrame.getContentPane().add(signalIconEditor);
        signalFrame.pack();
        signalFrame.setVisible(false);

        //icon label
        iconLabelButton.setToolTipText(rb.getString("IconLabelToolTip"));

        //change icons…
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
                JOptionPane.showMessageDialog(null, rb.getString("ChangeIconNotApplied"),
                                              rb.getString("ChangeIcons"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        changeIconsButton.setToolTipText(rb.getString("ChangeIconToolTip"));
        changeIconsButton.setEnabled(false);

        //??
        iconEditor = new MultiIconEditor(1);
        iconEditor.setIcon(0, "", "resources/icons/smallschematics/tracksegments/block.gif");
        iconEditor.complete();
        iconFrame = new JFrame(rb.getString("EditIcon"));
        iconFrame.getContentPane().add(iconEditor);
        iconFrame.pack();

        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("SansSerif", Font.PLAIN, 12),
                                            Color.black, new Color(215, 225, 255), Color.black));

        //setup help bar
        helpBar.setLayout(new BoxLayout(helpBar, BoxLayout.PAGE_AXIS));
        JTextArea helpTextArea1 = new JTextArea(rb.getString("Help1"));
        helpBar.add(helpTextArea1);
        JTextArea helpTextArea2 = new JTextArea(rb.getString("Help2"));
        helpBar.add(helpTextArea2);

        String helpText3 = "";

        switch (SystemType.getType()) {
            case SystemType.MACOSX: {
                helpText3 = rb.getString("Help3Mac");
                break;
            }

            case SystemType.WINDOWS: {
                helpText3 = rb.getString("Help3Win");
                break;
            }

            case SystemType.LINUX: {
                helpText3 = rb.getString("Help3Win");
                break;
            }

            default:
                helpText3 = rb.getString("Help3");
        }   //switch

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
        if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
            log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
        }
        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(this);
        thisPanel = this;
        thisPanel.setFocusable(true);
        thisPanel.addKeyListener(this);
        resetDirty();

        //establish link to LayoutEditorAuxTools
        auxTools = new LayoutEditorAuxTools(thisPanel);

        //Note: We have to invoke this later because everything's not really setup yet
        SwingUtilities.invokeLater(() -> {
            //initialize preferences
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                String windowFrameRef = getWindowFrameRef();

                Object prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarSide");

                //log.info("{}.toolBarSide is {}", windowFrameRef, prefsProp);
                if (prefsProp != null) {
                    eToolBarSide newToolBarSide = eToolBarSide.getName((String) prefsProp);
                    setToolBarSide(newToolBarSide);
                }

                //Note: since prefs default to false and we want wide to be the default
                //we invert it and save it as thin
                boolean prefsToolBarIsWide = prefsMgr.getSimplePreferenceState(windowFrameRef + ".toolBarThin");
                log.info("{}.toolBarThin is {}", windowFrameRef, prefsProp);
                setToolBarWide(prefsToolBarIsWide);

                boolean prefsShowHelpBar = prefsMgr.getSimplePreferenceState(windowFrameRef + ".showHelpBar");

                //log.info("{}.showHelpBar is {}", windowFrameRef, prefsShowHelpBar);
                setShowHelpBar(prefsShowHelpBar);

                boolean prefsAntialiasingOn = prefsMgr.getSimplePreferenceState(windowFrameRef + ".antialiasingOn");

                //log.info("{}.antialiasingOn is {}", windowFrameRef, prefsAntialiasingOn);
                setAntialiasingOn(prefsAntialiasingOn);

                boolean prefsHighlightSelectedBlockFlag =
                    prefsMgr.getSimplePreferenceState(windowFrameRef + ".highlightSelectedBlock");

                //log.info("{}.highlightSelectedBlock is {}", windowFrameRef, prefsHighlightSelectedBlockFlag);
                setHighlightSelectedBlock(prefsHighlightSelectedBlockFlag);

                prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarFontSize");

                //log.info("{} prefsProp toolBarFontSize is {}", windowFrameRef, prefsProp);

                if (null != prefsProp) {
                    float toolBarFontSize = Float.parseFloat(prefsProp.toString());
                    //setupToolBarFontSizes(toolBarFontSize);
                }
                updateAllComboBoxesDropDownListDisplayOrderFromPrefs();

                //this doesn't work as expected (1st one called messes up 2nd?)
                Point prefsWindowLocation = prefsMgr.getWindowLocation(windowFrameRef);
                Dimension prefsWindowSize = prefsMgr.getWindowSize(windowFrameRef);
                log.info("prefsMgr.prefsWindowLocation({}) is {}",   windowFrameRef, prefsWindowLocation);
                log.info("prefsMgr.prefsWindowSize is({}) {}",       windowFrameRef, prefsWindowSize);

                //Point prefsWindowLocation = null;
                //Dimension prefsWindowSize = null;

                //use this instead?
                if (true) { //(Nope, it's not working ether: prefsProp always comes back null)
                    prefsProp = prefsMgr.getProperty(windowFrameRef, "windowRectangle2D");
                    log.info("prefsMgr.getProperty({}, \"windowRectangle2D\") is {}", windowFrameRef, prefsProp);

                    if (null != prefsProp) {
                        Rectangle2D windowRectangle2D = (Rectangle2D) prefsProp;
                        prefsWindowLocation.setLocation(windowRectangle2D.getX(), windowRectangle2D.getY());
                        prefsWindowSize.setSize(windowRectangle2D.getWidth(), windowRectangle2D.getHeight());
                    }
                }

                if ((prefsWindowLocation != null) && (prefsWindowSize != null) &&
                    (prefsWindowSize.width >= 640) && (prefsWindowSize.height >= 480)) {
    //note: panel width & height comes from the saved (xml) panel (file) on disk
                    setLayoutDimensions(prefsWindowSize.width, prefsWindowSize.height,
                                        prefsWindowLocation.x, prefsWindowLocation.y,
                                        panelWidth, panelHeight);
                }
            }); //InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr)
        });     //SwingUtilities.invokeLater
    }           //LayoutEditor (constructor)

    private void createFloatingEditToolBox() {
        if (floatingEditToolBox == null) {
            if (floatingEditContent == null) {
                // Create the window content on the first call
                createFloatingEditContent();
            }

            if (isEditable() && floatingEditToolBox == null) {
                //Create the window and add the toolbox content
                floatingEditToolBox = new JmriJFrame("ToolBox: " + layoutName); // TODO - Bundle
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
        log.info("#### createFloatingEditContent");  // DIAG
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

        FlowLayout floatContentLayout = new FlowLayout(FlowLayout.CENTER, 5, 2);    //5 pixel gap between items, 2 vertical gap

        //Contains the block and sensor combo boxes.
        //It is moved to the appropriate detail pane when the tab changes.
        blockPropertiesPanel = new JPanel(floatContentLayout);
        String blockNameString = rb.getString("BlockID");
        blockSensorNameLabel = new JLabel(blockNameString);
        blockPropertiesPanel.   add(blockNameLabel);
        blockPropertiesPanel.   add(blockIDComboBox);
        blockPropertiesPanel.   add(blockSensorLabel);
        blockPropertiesPanel.   add(blockSensorComboBox);

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
        turnoutGroup1.  add(turnoutRHButton);
        turnoutGroup1.  add(turnoutLHButton);
        turnoutGroup1.  add(turnoutWYEButton);
        floatEditTurnout.add(turnoutGroup1);

        JPanel turnoutGroup2 = new JPanel(floatContentLayout);
        turnoutGroup2.  add(doubleXoverButton);
        turnoutGroup2.  add(rhXoverButton);
        turnoutGroup2.  add(lhXoverButton);
        floatEditTurnout.add(turnoutGroup2);

        JPanel turnoutGroup3 = new JPanel(floatContentLayout);
        turnoutGroup3.  add(layoutSingleSlipButton);
        turnoutGroup3.  add(layoutDoubleSlipButton);
        floatEditTurnout.add(turnoutGroup3);

        JPanel turnoutGroup4 = new JPanel(floatContentLayout);
        turnoutGroup4.  add(turnoutNamePanel);
        turnoutGroup4.  add(extraTurnoutPanel);
        floatEditTurnout.add(turnoutGroup4);

        JPanel turnoutGroup5 = new JPanel(floatContentLayout);
        turnoutGroup5.add(rotationPanel);
        floatEditTurnout.   add(turnoutGroup5);

        floatEditTurnout.   add(blockPropertiesPanel);
        floatEditTabsPane.addTab("Turnouts", null, floatEditTurnout, null); //TODO - Bundle

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

        floatEditTabsPane.addTab("Track", null, floatEditTrack, null);  //TODO - Bundle

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

        floatEditTabsPane.addTab("Labels", null, floatEditLabel, null); //TODO - Bundle

        //Tab 3 - Icons
        JPanel floatEditIcon = new JPanel();
        floatEditIcon.setLayout(new BoxLayout(floatEditIcon, BoxLayout.Y_AXIS));

        JPanel iconGroup1 = new JPanel(floatContentLayout);
        iconGroup1. add(multiSensorButton);
        iconGroup1. add(changeIconsButton);
        floatEditIcon.add(iconGroup1);

        JPanel iconGroup2 = new JPanel(floatContentLayout);
        iconGroup2. add(sensorButton);
        iconGroup2. add(sensorComboBox);
        floatEditIcon.add(iconGroup2);

        JPanel iconGroup3 = new JPanel(floatContentLayout);
        iconGroup3. add(signalMastButton);
        iconGroup3. add(signalMastComboBox);
        floatEditIcon.add(iconGroup3);

        JPanel iconGroup4 = new JPanel(floatContentLayout);
        iconGroup4. add(signalButton);
        iconGroup4. add(signalHeadComboBox);
        floatEditIcon.add(iconGroup4);

        JPanel iconGroup5 = new JPanel(floatContentLayout);
        iconGroup5.add(iconLabelButton);
        floatEditIcon.add(iconGroup5);

        floatEditTabsPane.addTab("Icons", null, floatEditIcon, null);   //TODO - Bundle
        floatEditTabsPanel.add(floatEditTabsPane);
        floatingEditPanel.add(floatEditTabsPanel);

        //End the tabs structure

        //The next 3 groups reside under the tab secton
        JPanel floatEditLocationPanel = new JPanel();
        floatEditLocationPanel. add(zoomPanel);
        floatEditLocationPanel. add(locationPanel);
        floatingEditPanel.add(floatEditLocationPanel);

//      JPanel floatEditActionPanel = new JPanel();
//      floatEditActionPanel.add(new JLabel("floatEditActionPanel", JLabel.CENTER));
//      floatingEditPanel.add(floatEditActionPanel);

        floatEditHelpPanel = new JPanel();
        floatingEditPanel.add(floatEditHelpPanel);

        //Notice:  End tree structure indenting
        //Create a scroll pane to hold the window content.
        floatingEditContent = new JScrollPane(floatingEditPanel);
        floatingEditContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        floatingEditContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        SwingUtilities.invokeLater(() -> {
            //Deferred action -- Force the help panel width to the same as the tabs section
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
            floatEditHelpPanel.setVisible(isEditable() && showHelpBar);
        });

        floatEditTabsPane.addChangeListener((e) -> {
            //Move the block group between the turnouts and track tabs
            int selIndex = floatEditTabsPane.getSelectedIndex();

            if (selIndex == 0) {
                floatEditTurnout.add(blockPropertiesPanel);
            } else if (selIndex == 1) {
                floatEditTrack.add(blockPropertiesPanel);
            }
        });
    }   //createFloatingEditContent

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
        if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
            createFloatingEditToolBox();
            return;
        }

        boolean toolBarIsVertical = (toolBarSide.equals(eToolBarSide.eRIGHT) || toolBarSide.equals(eToolBarSide.eLEFT));

        editToolBarPanel = new JPanel();
        editToolBarPanel.setLayout(new BoxLayout(editToolBarPanel, BoxLayout.PAGE_AXIS));

        JPanel outerBorderPanel = editToolBarPanel;
        JPanel innerBorderPanel = editToolBarPanel;

        Border blacklineBorder = BorderFactory.createLineBorder(Color.black);

        boolean useBorders = !(toolBarSide.equals(eToolBarSide.eTOP) || toolBarSide.equals(eToolBarSide.eBOTTOM));

        if (useBorders) {
            outerBorderPanel = new JPanel();
            outerBorderPanel.setLayout(new BoxLayout(outerBorderPanel, BoxLayout.PAGE_AXIS));
            TitledBorder outerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, rb.getString("Track"));
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

        String blockNameString = rb.getString("BlockID");

        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        if (toolBarIsVertical) {
            FlowLayout verticalTitleLayout = new FlowLayout(FlowLayout.CENTER, 5, 5);   //5 pixel gap between items, 5 vertical gap
            FlowLayout verticalContentLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);   //5 pixel gap between items, 2 vertical gap

            turnoutLabel = new JLabel("-- " + Bundle.getMessage("BeanNameTurnout") + " --");

            if (!useBorders) {
                JPanel vTop1TitlePanel = new JPanel(verticalTitleLayout);
                vTop1TitlePanel.add(turnoutLabel);
                vTop1TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1TitlePanel.getPreferredSize().height));
                innerBorderPanel.add(vTop1TitlePanel);
            }

            JPanel vTop1Panel = new JPanel(verticalContentLayout);
            vTop1Panel. add(turnoutLHButton);
            vTop1Panel. add(turnoutRHButton);
            vTop1Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop1Panel);

            JPanel vTop2Panel = new JPanel(verticalContentLayout);
            vTop2Panel. add(turnoutWYEButton);
            vTop2Panel. add(doubleXoverButton);
            vTop2Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop2Panel.getPreferredSize().height * 2));
            innerBorderPanel.add(vTop2Panel);

            JPanel vTop3Panel = new JPanel(verticalContentLayout);
            vTop3Panel. add(lhXoverButton);
            vTop3Panel. add(rhXoverButton);
            vTop3Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop3Panel.getPreferredSize().height));
            innerBorderPanel.add(vTop3Panel);

            JPanel vTop4Panel = new JPanel(verticalContentLayout);
            vTop4Panel. add(layoutSingleSlipButton);
            vTop4Panel. add(layoutDoubleSlipButton);
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
            trackLabel = new JLabel("-- " + rb.getString("Track") + " --");

            if (!useBorders) {
                JPanel vTop8TitlePanel = new JPanel(verticalTitleLayout);
                vTop8TitlePanel.add(trackLabel);
                vTop8TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop8TitlePanel.getPreferredSize().height));
                outerBorderPanel.add(vTop8TitlePanel);
            }

            JPanel vTop8Panel = new JPanel(verticalContentLayout);
            vTop8Panel. add(levelXingButton);
            vTop8Panel. add(trackButton);
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
            nodesLabel = new JLabel("-- " + rb.getString("Nodes") + " --");

            if (useBorders) {
                nodesBorderPanel = new JPanel();
                nodesBorderPanel.setLayout(new BoxLayout(nodesBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, rb.getString("Nodes"));
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
            labelsLabel = new JLabel("-- " + rb.getString("Labels") + " --");

            if (useBorders) {
                labelsBorderPanel = new JPanel();
                labelsBorderPanel.setLayout(new BoxLayout(labelsBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, rb.getString("Labels"));
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
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, rb.getString("IconsTitle"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                iconsBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop17TitlePanel = new JPanel(verticalTitleLayout);
                JLabel iconsLabel = new JLabel("-- " + rb.getString("IconsTitle") + " --");
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
            turnoutLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + ":  ");

            if (!useBorders) {
                hTop1Left.add(turnoutLabel);
            }
            hTop1Left.  add(turnoutRHButton);
            hTop1Left.  add(turnoutLHButton);
            hTop1Left.  add(turnoutWYEButton);
            hTop1Left.  add(doubleXoverButton);
            hTop1Left.  add(rhXoverButton);
            hTop1Left.  add(lhXoverButton);
            hTop1Left.  add(layoutSingleSlipButton);
            hTop1Left.  add(layoutDoubleSlipButton);
            hTop1Panel.add(hTop1Left);

            if (toolBarIsWide) {
                hTop1Panel.add(Box.createHorizontalGlue());

                JPanel hTop1Right = new JPanel(rightRowLayout);
                hTop1Right. add(turnoutNamePanel);
                hTop1Right. add(extraTurnoutPanel);
                hTop1Right. add(rotationPanel);
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
            trackLabel = new JLabel(rb.getString("Track") + ":  ");

            if (!useBorders) {
                hTop3Left.add(trackLabel);
            }
            hTop3Left.  add(levelXingButton);
            hTop3Left.  add(trackButton);
            hTop3Left.  add(trackSegmentPropertiesPanel);

            hTop3Panel. add(hTop3Left);
            hTop3Panel. add(Box.createHorizontalGlue());

            //Row 3 : Center Components
            JPanel hTop3Center = new JPanel(centerRowLayout);
            blockNameLabel = new JLabel(blockNameString);
            hTop3Center.add(blockNameLabel);
            hTop3Center.add(blockIDComboBox);
            hTop3Center.add(blockSensorLabel);
            hTop3Center.add(blockSensorComboBox);

            hTop3Panel. add(hTop3Center);
            hTop3Panel. add(Box.createHorizontalGlue());

            if (toolBarIsWide) {
                //row 3 : Right Components
                JPanel hTop3Right = new JPanel(rightRowLayout);
                hTop3Right. add(zoomPanel);
                hTop3Right. add(locationPanel);
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
            nodesLabel = new JLabel(rb.getString("Nodes") + ":  ");
            hTop4Left.  add(nodesLabel);
            hTop4Left.  add(endBumperButton);
            hTop4Left.  add(anchorButton);
            hTop4Left.  add(edgeButton);
            hTop4Panel. add(hTop4Left);
            hTop4Panel. add(Box.createHorizontalGlue());

            if (!toolBarIsWide) {
                //Row 4 : Right Components
                JPanel hTop4Right = new JPanel(rightRowLayout);
                hTop4Right. add(zoomPanel);
                hTop4Right. add(locationPanel);
                hTop4Panel.add(hTop4Right);
            }
            editToolBarPanel.add(hTop4Panel);

            //Row 5 Components (wide 4-center)
            labelsLabel = new JLabel(rb.getString("Labels") + ":  ");

            if (toolBarIsWide) {
                JPanel hTop4Center = new JPanel(centerRowLayout);
                hTop4Center.add(labelsLabel);
                hTop4Center.add(textLabelButton);
                hTop4Center.add(textLabelTextField);
                hTop4Center.add(memoryButton);
                hTop4Center.add(textMemoryComboBox);
                hTop4Center.add(blockContentsButton);
                hTop4Center.add(blockContentsComboBox);
                hTop4Panel. add(hTop4Center);
                hTop4Panel. add(Box.createHorizontalGlue());
                editToolBarPanel.add(hTop4Panel);
            } else {
                editToolBarPanel.add(hTop4Panel);

                JPanel hTop5Left = new JPanel(leftRowLayout);
                hTop5Left.  add(labelsLabel);
                hTop5Left.  add(textLabelButton);
                hTop5Left.  add(textLabelTextField);
                hTop5Left.  add(memoryButton);
                hTop5Left.  add(textMemoryComboBox);
                hTop5Left.  add(blockContentsButton);
                hTop5Left.  add(blockContentsComboBox);
                hTop5Left.  add(Box.createHorizontalGlue());
                editToolBarPanel.add(hTop5Left);
            }

            //Row 6
            JPanel hTop6Panel = new JPanel();
            hTop6Panel.setLayout(new BoxLayout(hTop6Panel, BoxLayout.LINE_AXIS));

            //Row 6 : Left Components
            //JPanel hTop6Left = new JPanel(centerRowLayout);
            JPanel hTop6Left = new JPanel(leftRowLayout);
            hTop6Left.  add(multiSensorButton);
            hTop6Left.  add(changeIconsButton);
            hTop6Left.  add(sensorButton);
            hTop6Left.  add(sensorComboBox);
            hTop6Left.  add(signalMastButton);
            hTop6Left.  add(signalMastComboBox);
            hTop6Left.  add(signalButton);
            hTop6Left.  add(signalHeadComboBox);
            hTop6Left.  add(new JLabel("      "));
            hTop6Left.  add(iconLabelButton);

            hTop6Panel.add(hTop6Left);
            editToolBarPanel.add(hTop6Panel);
        }
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
            case eLEFT: {
                contentPane.add(editToolBarContainer, 0);
                break;
            }

            case eBOTTOM:
            case eRIGHT: {
                contentPane.add(editToolBarContainer);
                break;
            }
        }   //switch

        if (toolBarIsVertical) {
            editToolBarContainer.add(helpBarPanel);
        } else {
            contentPane.add(helpBarPanel);
        }
        helpBarPanel.setVisible(isEditable() && showHelpBar);
        editToolBarContainer.setVisible(isEditable());

        //Note: We have to invoke this later because everything's not really setup yet
        SwingUtilities.invokeLater(() -> {
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
                    newToolBarFontSize = (float) MathUtil.pin(newToolBarFontSize, 9.0, 12.0);   //keep it between 9 & 12

                    String windowFrameRef = getWindowFrameRef();
                    Object prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarFontSize");

                    //log.debug("{} prefsProp is {}", windowFrameRef, prefsProp);
                    if (prefsProp != null) {    //(yes)
                        newToolBarFontSize = Float.parseFloat(prefsProp.toString());
                    } else {    //(no)
                                //use the GuiLafPreferencesManager value
                        GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
                        newToolBarFontSize = manager.getFontSize();

    //save it in user preferences for the panel
                        prefsMgr.setProperty(windowFrameRef, "toolBarFontSize", newToolBarFontSize);
                    }
                    setupToolBarFontSizes(newToolBarFontSize);
                });
            }
        }); //SwingUtilities.invokeLater
    }       //setupToolBar()

    //
    //recursive routine to walk a container hierarchy and set all conponent fonts
    //
    private void recursiveSetFont(Container inContainer, Font inFont) {
        if (false) {    //<<== disabled as per <https://github.com/JMRI/JMRI/pull/3145#issuecomment-283940658>
            for (Component c : inContainer.getComponents()) {
                c.setFont(inFont);

                if (c instanceof Container) {
                    recursiveSetFont((Container) c, toolBarFont);
                }
            }
        }
    }   //recursiveSetFont

    //
    //set the font sizes for all toolbar objects
    //
    private float toolBarFontSize = 12.f;

    private void setupToolBarFontSizes(float newToolBarFontSize) {
        if (toolBarFontSize != newToolBarFontSize) {
            toolBarFontSize = newToolBarFontSize;

            log.info("Font size: " + newToolBarFontSize);

            toolBarFont = zoomLabel.getFont();
            toolBarFont = toolBarFont.deriveFont(newToolBarFontSize);

            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
                recursiveSetFont(floatingEditContent, toolBarFont);
            } else {
                recursiveSetFont(editToolBarPanel, toolBarFont);
            }
        }
    }   //setupToolBarFontSizes

    @Override
    protected void init(String name) {}

    @Override
    public void initView() {
        editModeItem.setSelected(isEditable());

        positionableItem.setSelected(allPositionable());
        controlItem.setSelected(allControlling());

        if (isEditable()) {
            setAllShowTooltip(tooltipsInEditMode);
        } else {
            setAllShowTooltip(tooltipsWithoutEditMode);
        }

        switch (_scrollState) {
            case SCROLL_NONE: {
                scrollNone.setSelected(true);
                break;
            }

            case SCROLL_BOTH: {
                scrollBoth.setSelected(true);
                break;
            }

            case SCROLL_HORIZONTAL: {
                scrollHorizontal.setSelected(true);
                break;
            }

            case SCROLL_VERTICAL: {
                scrollVertical.setSelected(true);
                break;
            }

            default: {
                break;
            }
        }   //switch
    }       //initView

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        log.info("Frame size: {w:" + width + ", h:" + height + "}");
    }

    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        boolean save = (isDirty() || (savedEditMode != isEditable()) ||
                        (savedPositionable != allPositionable()) ||
                        (savedControlLayout != allControlling()) ||
                        (savedAnimatingLayout != animatingLayout) ||
                        (savedShowHelpBar != showHelpBar));

        targetWindowClosing(save);
    }   //targetWindowClosingEvent

    //
    //setup editable JmriBeanComboBoxes
    //
    //note: inValidateMode if true valid == green, invalid == red background
    //if false valid == green, invalid == yellow background
    //
    public void setupComboBox(JmriBeanComboBox inComboBox, boolean inValidateMode, boolean inEnable) {
        inComboBox.setEditable(true);
        inComboBox.getEditor().setItem("");
        inComboBox.setSelectedIndex(-1);
        inComboBox.setEnabled(inEnable);

        //fires when drop down list item is selected
        inComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    JmriBeanComboBox cb = (JmriBeanComboBox) event.getSource();
                    validateComboBox(cb, inValidateMode);
                }
            }
        });

        //fires when key is released while typing in combox editor
        inComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                JTextComponent c = (JTextComponent) event.getSource();
                JmriBeanComboBox cb = (JmriBeanComboBox) c.getParent();
                validateComboBox(cb, inValidateMode);
            }
        });
    }   //setupComboBox

    //
    //
    //
    private void validateComboBox(JmriBeanComboBox inComboBox, boolean inValidateMode) {
        ComboBoxEditor cbe = inComboBox.getEditor();
        JTextComponent c = (JTextComponent) cbe.getEditorComponent();
        String comboBoxText = cbe.getItem().toString();

        if (!comboBoxText.isEmpty()) {
            if (validateComboBoxEntry(inComboBox)) {
                c.setBackground(new Color(0xBDECB6));   //pastel green
            } else {
                if (inValidateMode) {
                    c.setBackground(new Color(0xFFC0C0));   //pastel red
                } else {
                    c.setBackground(new Color(0xFDFD96));   //pastel yellow
                }
            }
        } else {
            c.setBackground(new Color(0xFFFFFF));   //white
        }
    }   //validateComboBox

    //
    //
    //
    private NamedBean getBeanForComboBox(JmriBeanComboBox inComboBox) {
        NamedBean result = null;

        jmri.Manager uDaManager = inComboBox.getManager();

        String comboBoxText = inComboBox.getEditor().getItem().toString();
        comboBoxText = (null != comboBoxText) ? comboBoxText.trim() : "";

        //try user name
        result = uDaManager.getBeanByUserName(comboBoxText);

        if (null == result) {
            //try system name
            //note: don't use getBeanBySystemName here
            //throws an IllegalArgumentException if text is invalid
            result = uDaManager.getNamedBean(comboBoxText);
        }

        if (null == result) {
            //quick search to see if text matches anything in the drop down list
            String[] displayList = inComboBox.getDisplayList();
            boolean found = false;  //assume failure (pessimist!)

            for (String item : displayList) {
                if (item.equals(comboBoxText)) {
                    found = true;
                    break;
                }
            }

            if (found) {    //if we found it there then…
                //walk the namedBeanList…
                List<NamedBean> namedBeanList = uDaManager.getNamedBeanList();

                for (NamedBean namedBean : namedBeanList) {
                    //checking to see if it matches "<sname> - <uname>" or "<uname> - <sname>"
                    String uname = namedBean.getUserName();
                    String sname = namedBean.getSystemName();

                    if ((null != uname) && (null != sname)) {
                        String usname = uname + " - " + sname;
                        String suname = sname + " - " + uname;

                        if (comboBoxText.equals(usname) || comboBoxText.equals(suname)) {
                            result = namedBean;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }   //getBeanForComboBox

    //
    //is the combo box text valid?
    //
    private boolean validateComboBoxEntry(JmriBeanComboBox inComboBox) {
        return null != getBeanForComboBox(inComboBox);
    }

    //
    //return the user name for this combobox
    //
    private String getUserNameForComboBox(JmriBeanComboBox inComboBox) {
        String result = inComboBox.getEditor().getItem().toString();
        result = (null != result) ? result.trim() : "";

        NamedBean b = getBeanForComboBox(inComboBox);

        if (null != b) {
            result = b.getUserName();
        }
        return result;
    }   //getUserNameForComboBox

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
                    //exceptions make me throw up…
                    log.error("This error message, which nobody will ever see, shuts my IDE up.");
                }

                String key = name.substring(3);

                //log.info("VTCode[{}]:'{}'", key, code);
                stringsToVTCodes.put(key, code);
            }
        }
    }   //initStringsToVTCodes

    LayoutEditorTools tools = null;
    jmri.jmrit.signalling.AddEntryExitPairAction entryExit = null;

    void setupToolsMenu(JMenuBar menuBar) {
        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));

        toolsMenu.setMnemonic(stringsToVTCodes.get(rb.getString("MenuToolsMnemonic")));
        menuBar.add(toolsMenu);

        //scale track diagram
        JMenuItem scaleItem = new JMenuItem(rb.getString("ScaleTrackDiagram") + "...");
        toolsMenu.add(scaleItem);
        scaleItem.addActionListener((ActionEvent event) -> {
            //bring up scale track diagram dialog
            scaleTrackDiagram();
        });

        //translate selection
        JMenuItem moveItem = new JMenuItem(rb.getString("TranslateSelection") + "...");
        toolsMenu.add(moveItem);
        moveItem.addActionListener((ActionEvent event) -> {
            //bring up translate selection dialog
            moveSelection();
        });

        //undo translate selection
        JMenuItem undoMoveItem = new JMenuItem(rb.getString("UndoTranslateSelection"));
        toolsMenu.add(undoMoveItem);
        undoMoveItem.addActionListener((ActionEvent event) -> {
            //undo previous move selection
            undoMoveSelection();
        });

        //reset turnout size to program defaults
        JMenuItem undoTurnoutSize = new JMenuItem(rb.getString("ResetTurnoutSize"));
        toolsMenu.add(undoTurnoutSize);
        undoTurnoutSize.addActionListener((ActionEvent event) -> {
            //undo previous move selection
            resetTurnoutSize();
        });
        toolsMenu.addSeparator();

        //skip turnout
        skipTurnoutItem = new JCheckBoxMenuItem(rb.getString("SkipInternalTurnout"));
        toolsMenu.add(skipTurnoutItem);
        skipTurnoutItem.addActionListener((ActionEvent event) -> {
            skipIncludedTurnout = skipTurnoutItem.isSelected();
        });
        skipTurnoutItem.setSelected(skipIncludedTurnout);

        //set signals at turnout
        JMenuItem turnoutItem = new JMenuItem(rb.getString("SignalsAtTurnout") + "...");
        toolsMenu.add(turnoutItem);
        turnoutItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at turnout tool dialog
            tools.setSignalsAtTurnout(signalIconEditor, signalFrame);
        });

        //set signals at block boundary
        JMenuItem boundaryItem = new JMenuItem(rb.getString("SignalsAtBoundary") + "...");
        toolsMenu.add(boundaryItem);
        boundaryItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at block boundary tool dialog
            tools.setSignalsAtBlockBoundary(signalIconEditor, signalFrame);
        });

        //set signals at crossover turnout
        JMenuItem xoverItem = new JMenuItem(rb.getString("SignalsAtXoverTurnout") + "...");
        toolsMenu.add(xoverItem);
        xoverItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at double crossover tool dialog
            tools.setSignalsAtXoverTurnout(signalIconEditor, signalFrame);
        });

        //set signals at level crossing
        JMenuItem xingItem = new JMenuItem(rb.getString("SignalsAtLevelXing") + "...");
        toolsMenu.add(xingItem);
        xingItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at level crossing tool dialog
            tools.setSignalsAtLevelXing(signalIconEditor, signalFrame);
        });

        //set signals at throat-to-throat turnouts
        JMenuItem tToTItem = new JMenuItem(rb.getString("SignalsAtTToTTurnout") + "...");
        toolsMenu.add(tToTItem);
        tToTItem.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at throat-to-throat turnouts tool dialog
            tools.setSignalsAtTToTTurnouts(signalIconEditor, signalFrame);
        });

        //set signals at 3-way turnout
        JMenuItem way3Item = new JMenuItem(rb.getString("SignalsAt3WayTurnout") + "...");
        toolsMenu.add(way3Item);
        way3Item.addActionListener((ActionEvent event) -> {
            if (tools == null) {
                tools = new LayoutEditorTools(thisPanel);
            }

            //bring up signals at 3-way turnout tool dialog
            tools.setSignalsAt3WayTurnout(signalIconEditor, signalFrame);
        });

        JMenuItem slipItem = new JMenuItem(rb.getString("SignalsAtSlip") + "...");
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
    }   //setupToolsMenu

    //
    //setup option menu
    //
    protected JMenu setupOptionMenu(JMenuBar menuBar) {
        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));

        optionMenu.setMnemonic(stringsToVTCodes.get(rb.getString("OptionsMnemonic")));
        menuBar.add(optionMenu);

        //edit mode item
        editModeItem = new JCheckBoxMenuItem(rb.getString("EditMode"));
        optionMenu.add(editModeItem);
        editModeItem.setMnemonic(stringsToVTCodes.get(rb.getString("EditModeMnemonic")));
        int primary_modifier = SystemType.isMacOSX() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
        editModeItem.setAccelerator(KeyStroke.getKeyStroke(
                                        stringsToVTCodes.get(rb.getString("EditModeAccelerator")), primary_modifier));
        editModeItem.addActionListener((ActionEvent event) -> {
            setAllEditable(editModeItem.isSelected());

            //show/hide the help bar
            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(isEditable() && showHelpBar);
            } else {
                helpBarPanel.setVisible(isEditable() && showHelpBar);
            }

            if (isEditable()) {
                setAllShowTooltip(tooltipsInEditMode);

    //redo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    if (!highlightBlockInComboBox(blockIDComboBox)) {
                        highlightBlockInComboBox(blockContentsComboBox);
                    }
                }
            } else {
                setAllShowTooltip(tooltipsWithoutEditMode);

    //undo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    highlightBlock(null);
                }
            }
            awaitingIconChange = false;
        });
        editModeItem.setSelected(isEditable());

        //
        //create our (top) toolbar menu
        //
        JMenu toolBarMenu = new JMenu(Bundle.getMessage("ToolBar"));    //used for ToolBar SubMenu
        optionMenu.add(toolBarMenu);

        //
        //create toolbar side menu items: (top, left, bottom, right)
        //
        toolBarSideTopButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideTop"));
        toolBarSideTopButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(eToolBarSide.eTOP);
        });
        toolBarSideTopButton.setSelected(toolBarSide.equals(eToolBarSide.eTOP));

        toolBarSideLeftButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideLeft"));
        toolBarSideLeftButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(eToolBarSide.eLEFT);
        });
        toolBarSideLeftButton.setSelected(toolBarSide.equals(eToolBarSide.eLEFT));

        toolBarSideBottomButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideBottom"));
        toolBarSideBottomButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(eToolBarSide.eBOTTOM);
        });
        toolBarSideBottomButton.setSelected(toolBarSide.equals(eToolBarSide.eBOTTOM));

        toolBarSideRightButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideRight"));
        toolBarSideRightButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(eToolBarSide.eRIGHT);
        });
        toolBarSideRightButton.setSelected(toolBarSide.equals(eToolBarSide.eRIGHT));

        toolBarSideFloatButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideFloat"));
        toolBarSideFloatButton.addActionListener((ActionEvent event) -> {
            setToolBarSide(eToolBarSide.eFLOAT);
        });
        toolBarSideFloatButton.setSelected(toolBarSide.equals(eToolBarSide.eFLOAT));

        JMenu toolBarSideMenu = new JMenu(Bundle.getMessage("ToolBarSide"));    //used for ScrollBarsSubMenu
        toolBarSideMenu.add(toolBarSideTopButton);
        toolBarSideMenu.add(toolBarSideLeftButton);
        toolBarSideMenu.add(toolBarSideBottomButton);
        toolBarSideMenu.add(toolBarSideRightButton);
        toolBarSideMenu.add(toolBarSideFloatButton);

        ButtonGroup toolBarSideGroup = new ButtonGroup();
        toolBarSideGroup.   add(toolBarSideTopButton);
        toolBarSideGroup.   add(toolBarSideLeftButton);
        toolBarSideGroup.   add(toolBarSideBottomButton);
        toolBarSideGroup.   add(toolBarSideRightButton);
        toolBarSideGroup.   add(toolBarSideFloatButton);
        toolBarMenu.add(toolBarSideMenu);

        //
        //toolbar wide menu
        //
        toolBarMenu.add(toolBarIsWideItem);
        toolBarIsWideItem.addActionListener((ActionEvent event) -> {
            boolean newToolBarIsWide = toolBarIsWideItem.isSelected();
            setToolBarWide(newToolBarIsWide);
        });
        toolBarIsWideItem.setSelected(toolBarIsWide);

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
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuCanceled(MenuEvent e) {}
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
                int ddldoInt = parentMenu.getComponentZOrder(ddldoMenuItem);
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
                            ddldoPrefName = ddldoPrefName + "." + ttt;
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
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        toolBarMenu.add(dropDownListsDisplayOrderMenu);

        //
        //positionable item
        //
        positionableItem = new JCheckBoxMenuItem(rb.getString("AllowRepositioning"));
        optionMenu.add(positionableItem);
        positionableItem.addActionListener((ActionEvent event) -> {
            setAllPositionable(positionableItem.isSelected());
        });
        positionableItem.setSelected(allPositionable());

        //
        //controlable item
        //
        controlItem = new JCheckBoxMenuItem(rb.getString("AllowLayoutControl"));
        optionMenu.add(controlItem);
        controlItem.addActionListener((ActionEvent event) -> {
            setAllControlling(controlItem.isSelected());
        });
        controlItem.setSelected(allControlling());

        //
        //animation item
        //
        animationItem = new JCheckBoxMenuItem(rb.getString("AllowTurnoutAnimation"));
        optionMenu.add(animationItem);
        animationItem.addActionListener((ActionEvent event) -> {
            boolean mode = animationItem.isSelected();
            setTurnoutAnimation(mode);
        });
        animationItem.setSelected(true);

        //
        //show help item
        //
        showHelpItem = new JCheckBoxMenuItem(rb.getString("ShowEditHelp"));
        optionMenu.add(showHelpItem);
        showHelpItem.addActionListener((ActionEvent event) -> {
            boolean newShowHelpBar = showHelpItem.isSelected();
            setShowHelpBar(newShowHelpBar);
        });
        showHelpItem.setSelected(showHelpBar);

        //
        //show grid item
        //
        showGridItem = new JCheckBoxMenuItem(rb.getString("ShowEditGrid"));
        showGridItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                                                               rb.getString("ShowEditGridAccelerator")), primary_modifier));
        optionMenu.add(showGridItem);
        showGridItem.addActionListener((ActionEvent event) -> {
            drawGrid = showGridItem.isSelected();
            repaint();
        });
        showGridItem.setSelected(drawGrid);

        //
        //snap to grid on add item
        //
        snapToGridOnAddItem = new JCheckBoxMenuItem(rb.getString("SnapToGridOnAdd"));
        snapToGridOnAddItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                                                                      rb.getString("SnapToGridOnAddAccelerator")),
                                                                  primary_modifier | ActionEvent.SHIFT_MASK));
        optionMenu.add(snapToGridOnAddItem);
        snapToGridOnAddItem.addActionListener((ActionEvent event) -> {
            snapToGridOnAdd = snapToGridOnAddItem.isSelected();
            repaint();
        });
        snapToGridOnAddItem.setSelected(snapToGridOnAdd);

        //
        //snap to grid on move item
        //
        snapToGridOnMoveItem = new JCheckBoxMenuItem(rb.getString("SnapToGridOnMove"));
        snapToGridOnMoveItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                                                                       rb.getString("SnapToGridOnMoveAccelerator")),
                                                                   primary_modifier | ActionEvent.SHIFT_MASK));
        optionMenu.add(snapToGridOnMoveItem);
        snapToGridOnMoveItem.addActionListener((ActionEvent event) -> {
            snapToGridOnMove = snapToGridOnMoveItem.isSelected();
            repaint();
        });
        snapToGridOnMoveItem.setSelected(snapToGridOnMove);

        //
        //specify grid square size
        //
        JMenuItem gridSizeItem = new JMenuItem(rb.getString("EditGridSize") + "...");
        optionMenu.add(gridSizeItem);
        gridSizeItem.addActionListener((ActionEvent event) -> {
            //prompt for new size
            String newSize = (String) JOptionPane.showInputDialog(getTargetFrame(),
                                                                  rb.getString("NewGridSize") + ":",
                                                                  rb.getString("EditGridsizeMessageTitle"),
                                                                  JOptionPane.PLAIN_MESSAGE, null, null, String.valueOf(gridSize));

            if (newSize == null) {
                return; //cancelled
            }
            int gSize = Integer.parseInt(newSize);

            if (gSize == gridSize) {
                return; //no change
            }

            if ((gSize < 5) || (gSize > 100)) {
                JOptionPane.showMessageDialog(null, rb.getString("GridSizeInvalid"), rb.getString("CannotEditGridSize"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }
            setGridSize(gSize);
            setDirty(true);
            repaint();
        });

        //
        //Show/Hide Scroll Bars
        //
        scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable"));    //used for ScrollBarsSubMenu
        optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.setSelected(_scrollState == SCROLL_BOTH);
        scrollBoth.addActionListener((ActionEvent event) -> {
            _scrollState = SCROLL_BOTH;
            setScroll(_scrollState);
            repaint();
        });
        scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.setSelected(_scrollState == SCROLL_NONE);
        scrollNone.addActionListener((ActionEvent event) -> {
            _scrollState = SCROLL_NONE;
            setScroll(_scrollState);
            repaint();
        });
        scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.setSelected(_scrollState == SCROLL_HORIZONTAL);
        scrollHorizontal.addActionListener((ActionEvent event) -> {
            _scrollState = SCROLL_HORIZONTAL;
            setScroll(_scrollState);
            repaint();
        });
        scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.setSelected(_scrollState == SCROLL_VERTICAL);
        scrollVertical.addActionListener((ActionEvent event) -> {
            _scrollState = SCROLL_VERTICAL;
            setScroll(_scrollState);
            repaint();
        });

        //
        //Tooltip options
        //
        tooltipMenu = new JMenu(rb.getString("TooltipSubMenu"));
        optionMenu.add(tooltipMenu);
        ButtonGroup tooltipGroup = new ButtonGroup();
        tooltipNone = new JRadioButtonMenuItem(rb.getString("TooltipNone"));
        tooltipGroup.add(tooltipNone);
        tooltipMenu.add(tooltipNone);
        tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNone.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = false;
            setAllShowTooltip(false);
        });
        tooltipAlways = new JRadioButtonMenuItem(rb.getString("TooltipAlways"));
        tooltipGroup.add(tooltipAlways);
        tooltipMenu.add(tooltipAlways);
        tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipAlways.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = true;
            setAllShowTooltip(true);
        });
        tooltipInEdit = new JRadioButtonMenuItem(rb.getString("TooltipEdit"));
        tooltipGroup.add(tooltipInEdit);
        tooltipMenu.add(tooltipInEdit);
        tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipInEdit.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = false;
            setAllShowTooltip(isEditable());
        });
        tooltipNotInEdit = new JRadioButtonMenuItem(rb.getString("TooltipNotEdit"));
        tooltipGroup.add(tooltipNotInEdit);
        tooltipMenu.add(tooltipNotInEdit);
        tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipNotInEdit.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = true;
            setAllShowTooltip(!isEditable());
        });

        //
        //antialiasing
        //
        antialiasingOnItem = new JCheckBoxMenuItem(rb.getString("AntialiasingOn"));
        optionMenu.add(antialiasingOnItem);
        antialiasingOnItem.addActionListener((ActionEvent event) -> {
            antialiasingOn = antialiasingOnItem.isSelected();
            repaint();
        });
        antialiasingOnItem.setSelected(antialiasingOn);

        //
        //Highlight Selected Block
        //
        highlightSelectedBlockItem = new JCheckBoxMenuItem(rb.getString("HighlightSelectedBlock"));
        optionMenu.add(highlightSelectedBlockItem);
        highlightSelectedBlockItem.addActionListener((ActionEvent event) -> {
            setHighlightSelectedBlock(highlightSelectedBlockItem.isSelected());
        });
        highlightSelectedBlockItem.setSelected(highlightSelectedBlockFlag);

        //
        //edit title item
        //
        optionMenu.addSeparator();
        JMenuItem titleItem = new JMenuItem(rb.getString("EditTitle") + "...");
        optionMenu.add(titleItem);
        titleItem.addActionListener((ActionEvent event) -> {
            //prompt for name
            String newName = (String) JOptionPane.showInputDialog(getTargetFrame(),
                                                                  rb.getString("EnterTitle") + ":",
                                                                  rb.getString("EditTitleMessageTitle"),
                                                                  JOptionPane.PLAIN_MESSAGE, null, null, layoutName);

            if (newName == null) {
                return; //cancelled
            }

            if (newName.equals(layoutName)) {
                return;
            }

            if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(newName)) {
                JOptionPane.showMessageDialog(null, rb.getString("CanNotRename"), rb.getString("PanelExist"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }
            setTitle(newName);
            layoutName = newName;
            jmri.jmrit.display.PanelMenu.instance().renameEditorPanel(thisPanel);
            setDirty(true);

            if (toolBarSide.equals(eToolBarSide.eFLOAT) && isEditable()) {
                deleteFloatingEditToolBox();
                createFloatingEditToolBox();
            }
        });

        //
        //background image menu item
        //
        JMenuItem backgroundItem = new JMenuItem(rb.getString("AddBackground") + "...");
        optionMenu.add(backgroundItem);
        backgroundItem.addActionListener((ActionEvent event) -> {
            addBackground();
            setDirty(true);
            repaint();
        });

        //
        //background color menu item
        //
        JMenu backgroundColorMenu = new JMenu(rb.getString("SetBackgroundColor"));
        backgroundColorButtonGroup = new ButtonGroup();
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Black"),     Color.black);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("DarkGray"),  Color.darkGray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Gray"),      Color.gray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("LightGray"), Color.lightGray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("White"),     Color.white);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Red"),       Color.red);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Pink"),      Color.pink);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Orange"),    Color.orange);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Yellow"),    Color.yellow);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Green"),     Color.green);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Blue"),      Color.blue);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Magenta"),   Color.magenta);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Cyan"),      Color.cyan);
        optionMenu. add(backgroundColorMenu);

        //
        //add fast clock menu item
        //
        JMenuItem clockItem = new JMenuItem(Bundle.getMessage("AddItem", Bundle.getMessage("FastClock")));
        optionMenu. add(clockItem);
        clockItem.addActionListener((ActionEvent event) -> {
            addClock();
            setDirty(true);
            repaint();
        });

        //
        //add turntable menu item
        //
        JMenuItem turntableItem = new JMenuItem(rb.getString("AddTurntable"));
        optionMenu.add(turntableItem);
        turntableItem.addActionListener((ActionEvent event) -> {
            addTurntable(windowCenter());
            setDirty(true);
            repaint();
        });

        //
        //add reporter menu item
        //
        JMenuItem reporterItem = new JMenuItem(rb.getString("AddReporter") + "...");
        optionMenu.add(reporterItem);
        reporterItem.addActionListener((ActionEvent event) -> {
            Point2D pt = windowCenter();
            enterReporter((int) pt.getX(), (int) pt.getY());
            setDirty(true);
            repaint();
        });

        //
        //set location and size menu item
        //
        JMenuItem locationItem = new JMenuItem(rb.getString("SetLocation"));
        optionMenu.add(locationItem);
        locationItem.addActionListener((ActionEvent event) -> {
            setCurrentPositionAndSize();
            log.debug("Bounds:" + upperLeftX + ", " + upperLeftY + ", " + windowWidth + ", " + windowHeight + ", " + panelWidth +
                      ", " + panelHeight);
        });

        //
        //set track width menu item
        //
        JMenuItem widthItem = new JMenuItem(rb.getString("SetTrackWidth") + "...");
        optionMenu.add(widthItem);
        widthItem.addActionListener((ActionEvent event) -> {
            //bring up enter track width dialog
            enterTrackWidth();
        });

        //
        //track colors item menu item
        //
        JMenu trkColourMenu = new JMenu(rb.getString("TrackColorSubMenu"));
        optionMenu.add(trkColourMenu);

        JMenu trackColorMenu = new JMenu(rb.getString("DefaultTrackColor"));
        trackColorButtonGroup = new ButtonGroup();
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Black"),     Color.black);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("DarkGray"),  Color.darkGray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Gray"),      Color.gray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("White"),     Color.white);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Red"),       Color.red);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Pink"),      Color.pink);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Orange"),    Color.orange);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Yellow"),    Color.yellow);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Green"),     Color.green);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Blue"),      Color.blue);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Magenta"),   Color.magenta);
        addTrackColorMenuEntry(trackColorMenu, Bundle.getMessage("Cyan"),      Color.cyan);
        trkColourMenu.add(trackColorMenu);

        JMenu trackOccupiedColorMenu = new JMenu(rb.getString("DefaultOccupiedTrackColor"));
        trackOccupiedColorButtonGroup = new ButtonGroup();
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Black"),     Color.black);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("DarkGray"),  Color.darkGray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Gray"),      Color.gray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("White"),     Color.white);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Red"),       Color.red);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Pink"),      Color.pink);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Orange"),    Color.orange);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Yellow"),    Color.yellow);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Green"),     Color.green);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Blue"),      Color.blue);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Magenta"),   Color.magenta);
        addTrackOccupiedColorMenuEntry(trackOccupiedColorMenu, Bundle.getMessage("Cyan"),      Color.cyan);
        trkColourMenu.add(trackOccupiedColorMenu);

        JMenu trackAlternativeColorMenu = new JMenu(rb.getString("DefaultAlternativeTrackColor"));
        trackAlternativeColorButtonGroup = new ButtonGroup();
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Black"),     Color.black);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("DarkGray"),  Color.darkGray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Gray"),      Color.gray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("LightGray"), Color.lightGray);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("White"),     Color.white);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Red"),       Color.red);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Pink"),      Color.pink);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Orange"),    Color.orange);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Yellow"),    Color.yellow);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Green"),     Color.green);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Blue"),      Color.blue);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Magenta"),   Color.magenta);
        addTrackAlternativeColorMenuEntry(trackAlternativeColorMenu,  Bundle.getMessage("Cyan"),      Color.cyan);
        trkColourMenu.add(trackAlternativeColorMenu);

        //
        //add text color menu item
        //
        JMenu textColorMenu = new JMenu(rb.getString("DefaultTextColor"));
        textColorButtonGroup = new ButtonGroup();
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Black"),     Color.black);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("DarkGray"),  Color.darkGray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Gray"),      Color.gray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("LightGray"), Color.lightGray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("White"),     Color.white);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Red"),       Color.red);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Pink"),      Color.pink);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Orange"),    Color.orange);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Yellow"),    Color.yellow);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Green"),     Color.green);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Blue"),      Color.blue);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Magenta"),   Color.magenta);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Cyan"),      Color.cyan);
        optionMenu. add(textColorMenu);

        //
        //add turnout options submenu
        //
        JMenu turnoutOptionsMenu = new JMenu(rb.getString("TurnoutOptions"));
        optionMenu. add(turnoutOptionsMenu);

        //circle on Turnouts
        turnoutCirclesOnItem = new JCheckBoxMenuItem(rb.getString("TurnoutCirclesOn"));
        turnoutOptionsMenu.add(turnoutCirclesOnItem);
        turnoutCirclesOnItem.addActionListener((ActionEvent event) -> {
            turnoutCirclesWithoutEditMode = turnoutCirclesOnItem.isSelected();
            repaint();
        });
        turnoutCirclesOnItem.setSelected(turnoutCirclesWithoutEditMode);

        //select turnout circle color
        JMenu turnoutCircleColorMenu = new JMenu(rb.getString("TurnoutCircleColor"));
        turnoutCircleColorButtonGroup = new ButtonGroup();
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("UseDefaultTrackColor"),  null);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Black"),                 Color.black);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("DarkGray"),              Color.darkGray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Gray"),                  Color.gray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("LightGray"),             Color.lightGray);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("White"),                 Color.white);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Red"),                   Color.red);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Pink"),                  Color.pink);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Orange"),                Color.orange);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Yellow"),                Color.yellow);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Green"),                 Color.green);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Blue"),                  Color.blue);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Magenta"),               Color.magenta);
        addTurnoutCircleColorMenuEntry(turnoutCircleColorMenu, Bundle.getMessage("Cyan"),                  Color.cyan);
        turnoutOptionsMenu.add(turnoutCircleColorMenu);

        //select turnout circle size
        JMenu turnoutCircleSizeMenu = new JMenu(rb.getString("TurnoutCircleSize"));
        turnoutCircleSizeButtonGroup = new ButtonGroup();
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "1",    1);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "2",    2);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "3",    3);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "4",    4);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "5",    5);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "6",    6);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "7",    7);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "8",    8);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "9",    9);
        addTurnoutCircleSizeMenuEntry(turnoutCircleSizeMenu,  "10",   10);
        turnoutOptionsMenu. add(turnoutCircleSizeMenu);

        //
        //add "enable drawing of unselected leg " menu item (helps when diverging angle is small)
        //
        turnoutDrawUnselectedLegItem = new JCheckBoxMenuItem(rb.getString("TurnoutDrawUnselectedLeg"));
        turnoutOptionsMenu. add(turnoutDrawUnselectedLegItem);
        turnoutDrawUnselectedLegItem.addActionListener((ActionEvent event) -> {
            turnoutDrawUnselectedLeg = turnoutDrawUnselectedLegItem.isSelected();
            repaint();
        });
        turnoutDrawUnselectedLegItem.setSelected(turnoutDrawUnselectedLeg);

        //add show grid menu item
        autoAssignBlocksItem = new JCheckBoxMenuItem(rb.getString("AutoAssignBlock"));
        optionMenu.add(autoAssignBlocksItem);
        autoAssignBlocksItem.addActionListener((ActionEvent event) -> {
            autoAssignBlocks = autoAssignBlocksItem.isSelected();
        });
        autoAssignBlocksItem.setSelected(autoAssignBlocks);

        //
        //add hideTrackSegmentConstructionLines menu item
        //
        hideTrackSegmentConstructionLines = new JCheckBoxMenuItem(rb.getString("HideTrackConLines"));
        optionMenu.add(hideTrackSegmentConstructionLines);
        hideTrackSegmentConstructionLines.addActionListener((ActionEvent event) -> {
            int show = TrackSegment.SHOWCON;

            if (hideTrackSegmentConstructionLines.isSelected()) {
                show = TrackSegment.HIDECONALL;
            }

            for (TrackSegment t : trackList) {
                t.hideConstructionLines(show);
            }
            repaint();
        });
        hideTrackSegmentConstructionLines.setSelected(autoAssignBlocks);

        //
        //add "use direct turnout control" menu item
        //
        useDirectTurnoutControlItem = new JCheckBoxMenuItem(rb.getString("UseDirectTurnoutControl"));   //IN18N
        optionMenu.add(useDirectTurnoutControlItem);
        useDirectTurnoutControlItem.addActionListener((ActionEvent event) -> {
            useDirectTurnoutControl = false;

            if (useDirectTurnoutControlItem.isSelected()) {
                useDirectTurnoutControl = true;
            }
        });
        useDirectTurnoutControlItem.setSelected(useDirectTurnoutControl);

        return optionMenu;
    }   //setupOptionMenu

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
    }   //updateDropDownMenuDisplayOrderMenu

    //
    //update drop down menu display order for all combo boxes (from prefs)
    //
    private void updateAllComboBoxesDropDownListDisplayOrderFromPrefs() {
        //1st call the recursive funtion starting from the edit toolbar container
        updateComboBoxDropDownListDisplayOrderFromPrefs(editToolBarContainer);
        updateComboBoxDropDownListDisplayOrderFromPrefs(floatingEditContent);

        //and now that that's done update the drop down menu display order menu
        updateDropDownMenuDisplayOrderMenu();
    }   //updateAllComboBoxesDropDownListDisplayOrderFromPrefs

    //
    //update drop down menu display order for all combo boxes (from prefs)
    //note: recursive function that walks down the component / container tree
    //
    private void updateComboBoxDropDownListDisplayOrderFromPrefs(Component inComponent) {
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
                    String ttt = jbcb.getToolTipText();

                    if (null != ttt) {
    //change the name of the preference based on the tool tip text
                        ddldoPrefName = ddldoPrefName + "." + ttt;

                        //try to get the preference
                        ddldoProp = prefsMgr.getProperty(getWindowFrameRef(), ddldoPrefName);

                        if (null != ddldoProp) {                //if we found it…
                            ddldoPref = ddldoProp.toString();   //get it's (string value
                        } else {                                //…otherwise…
                                                                //save it in the users preferences
                            prefsMgr.setProperty(windowFrameRef, ddldoPrefName, ddldoPref);
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
                        //must be a bogus value… lets re-set everything to DISPLAYNAME
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
                //nothing to do here… move along…
            }
        }   //if (null != inComponent) {
    }       //updateComboBoxDropDownListDisplayOrderFromPrefs

    //
    //
    //
    private void setToolBarSide(eToolBarSide newToolBarSide) {
        // null if edit toolbar is not setup yet…
        if ((editModeItem != null) && !newToolBarSide.equals(toolBarSide)) {
            toolBarSide = newToolBarSide;
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setProperty(getWindowFrameRef(), "toolBarSide", toolBarSide.getName());
            });

            setupToolBar(); //re-layout all the toolbar items

            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
                createFloatingEditToolBox();
                if (null != editToolBarContainer) {
                    editToolBarContainer.setVisible(false);
                }
            } else {
                if (null != floatingEditToolBox) {
                    deleteFloatingEditToolBox();
                }
                editToolBarContainer.setVisible(isEditable());
            }
            toolBarSideTopButton.setSelected(toolBarSide.equals(eToolBarSide.eTOP));
            toolBarSideLeftButton.setSelected(toolBarSide.equals(eToolBarSide.eLEFT));
            toolBarSideBottomButton.setSelected(toolBarSide.equals(eToolBarSide.eBOTTOM));
            toolBarSideRightButton.setSelected(toolBarSide.equals(eToolBarSide.eRIGHT));
            toolBarSideFloatButton.setSelected(toolBarSide.equals(eToolBarSide.eFLOAT));

            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(isEditable() && showHelpBar);
            } else if (showHelpBar) {
                //not sure why… but this is the only way I could
                //get everything to layout correctly
                //when the helpbar is visible…
                boolean editMode = editModeItem.isSelected();
                setAllEditable(!editMode);
                setAllEditable(editMode);
            } else {
                helpBarPanel.setVisible(isEditable() && showHelpBar);
            }
        }
    }   //setToolBarSide

    //
    //
    //
    private void setToolBarWide(boolean newToolBarIsWide) {
        //null if edit toolbar not setup yet…
        if ((editModeItem != null) && (toolBarIsWide != newToolBarIsWide)) {
            toolBarIsWide = newToolBarIsWide;

            toolBarIsWideItem.setSelected(toolBarIsWide);

            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                //Note: since prefs default to false and we want wide to be the default
                //we invert it and save it as thin
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".toolBarThin", !toolBarIsWide);
            });

            setupToolBar(); //re-layout all the toolbar items

            if (showHelpBar) {
                //not sure why… but this is the only way I could
                //get everything to layout correctly
                //when the helpbar is visible…
                boolean editMode = editModeItem.isSelected();
                setAllEditable(!editMode);
                setAllEditable(editMode);
            } else {
                helpBarPanel.setVisible(isEditable() && showHelpBar);
            }
        }
    }   //setToolBarWide

    //
    //
    //
    private void setupZoomMenu(JMenuBar menuBar) {
        zoomMenu.setMnemonic(stringsToVTCodes.get(rb.getString("MenuZoomMnemonic")));
        menuBar.add(zoomMenu);
        ButtonGroup zoomButtonGroup = new ButtonGroup();

        //add zoom choices to menu
        JMenuItem zoomInItem = new JMenuItem(rb.getString("ZoomIn"));
        zoomInItem.setMnemonic(stringsToVTCodes.get(rb.getString("zoomInMnemonic")));
        int primary_modifier = SystemType.isMacOSX() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
        String zoomInAccelerator = rb.getString("zoomInAccelerator");

        //log.info("zoomInAccelerator: " + zoomInAccelerator);
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomInAccelerator), primary_modifier));
        zoomMenu.add(zoomInItem);
        ActionListener pressedZoomInActionListener = (ActionEvent event) -> {
            zoomIn();
        };
        zoomInItem.addActionListener(pressedZoomInActionListener);

        JMenuItem zoomOutItem = new JMenuItem(rb.getString("ZoomOut"));
        zoomOutItem.setMnemonic(stringsToVTCodes.get(rb.getString("zoomOutMnemonic")));
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                                                              rb.getString("zoomOutAccelerator")), primary_modifier));
        zoomMenu.add(zoomOutItem);
        zoomOutItem.addActionListener((ActionEvent event) -> {
            zoomOut();
        });

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
        //zoom attribute is 1.0… if it's being instantiated from an XML file
        //that has a zoom attribute for this object then setZoom will be
        //called after this method returns and we'll select the appropriate
        //menu item then.
        noZoomItem.setSelected(true);

        //Note: We have to invoke this later because everything's not setup yet
        SwingUtilities.invokeLater(() -> {
            //get the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                Object zoomProp = prefsMgr.getProperty(getWindowFrameRef(), "zoom");
                log.debug("{} zoom is {}", getWindowFrameRef(), zoomProp);

                if (zoomProp != null) {
                    setZoom((Double) zoomProp);
                }
            });
        });
    }   //setupZoomMenu

    //
    //
    //
    private void selectZoomMenuItem(double zoomFactor) {
        //this will put zoomFactor on 25% increments
        //(so it will more likely match one of these values)
        int newZoomFactor = ((int) (zoomFactor * 4)) * 25;

        zoom025Item.setSelected(newZoomFactor == 25);
        zoom05Item.setSelected(newZoomFactor == 50);
        zoom075Item.setSelected(newZoomFactor == 75);
        noZoomItem.setSelected(newZoomFactor == 100);
        zoom15Item.setSelected(newZoomFactor == 150);
        zoom20Item.setSelected(newZoomFactor == 200);
        zoom30Item.setSelected(newZoomFactor == 300);
        zoom40Item.setSelected(newZoomFactor == 400);
        zoom50Item.setSelected(newZoomFactor == 500);
        zoom60Item.setSelected(newZoomFactor == 600);
    }   //selectZoomMenuItem

    //
    //
    //
    public double setZoom(double zoomFactor) {
        double newZoom = Math.min(Math.max(zoomFactor, minZoom), maxZoom);

        if (newZoom != getPaintScale()) {
            log.debug("zoom: " + zoomFactor);
            setPaintScale(newZoom);
            zoomLabel.setText(String.format("x%1$,.2f", newZoom));
            selectZoomMenuItem(newZoom);

            //save the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setProperty(getWindowFrameRef(), "zoom", zoomFactor);
            });
        }
        return getPaintScale();
    }   //setZoom

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

        if (true) {
            newScale *= 1.1;
        } else if (_paintScale < 1.0) {
            newScale = _paintScale + stepUnderOne;
        } else if (_paintScale < 2.0) {
            newScale = _paintScale + stepOverOne;
        } else {
            newScale = _paintScale + stepOverTwo;
        }
        return setZoom(newScale);
    }   //zoomIn

    //
    //
    //
    private double zoomOut() {
        double newScale = _paintScale;

        if (true) {
            newScale /= 1.1;
        } else if (_paintScale > 2.0) {
            newScale = _paintScale - stepOverTwo;
        } else if (_paintScale > 1.0) {
            newScale = _paintScale - stepOverOne;
        } else {
            newScale = _paintScale - stepUnderOne;
        }
        return setZoom(newScale);
    }   //zoomOut

    //
    //
    //
    private Point2D windowCenter() {
        //Returns window's center coordinates converted to layout space
        //Used for initial setup of turntables and reporters
        //First of all compute center of window in screen coordinates
        Point pt = getLocationOnScreen();
        Dimension dim = getSize();

        pt.x += dim.width / 2;
        pt.y += dim.height / 2 + 40;    //40 = approx. difference between upper and lower menu areas
        //Now convert to layout space
        SwingUtilities.convertPointFromScreen(pt, getTargetPanel());
        pt.x /= getPaintScale();
        pt.y /= getPaintScale();

        return pt;
    }   //windowCenter

    //
    //
    //
    private void setupMarkerMenu(JMenuBar menuBar) {
        JMenu markerMenu = new JMenu(rbx.getString("MenuMarker"));

        markerMenu.setMnemonic(stringsToVTCodes.get(rbx.getString("MenuMarkerMnemonic")));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(rbx.getString("AddLoco") + "...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(rbx.getString("AddLocoRoster") + "...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(rbx.getString("RemoveMarkers")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeMarkers();
            }
        });
    }   //setupMarkerMenu

    //
    //
    //
    private void setupDispatcherMenu(JMenuBar menuBar) {
        JMenu dispMenu = new JMenu(Bundle.getMessage("MenuDispatcher"));

        dispMenu.setMnemonic(stringsToVTCodes.get(rb.getString("MenuDispatcherMnemonic")));
        dispMenu.add(new JMenuItem(new jmri.jmrit.dispatcher.DispatcherAction(Bundle.getMessage("MenuItemOpen"))));
        menuBar.add(dispMenu);
        JMenuItem newTrainItem = new JMenuItem(Bundle.getMessage("MenuItemNewTrain"));
        dispMenu.add(newTrainItem);
        newTrainItem.addActionListener((ActionEvent event) -> {
            if (jmri.InstanceManager.getDefault(jmri.TransitManager.class).getSystemNameList().size() <= 0) {
    //Inform the user that there are no Transits available, and don't open the window
                javax.swing.JOptionPane.showMessageDialog(null,
                                                          ResourceBundle.getBundle("jmri.jmrit.dispatcher.DispatcherBundle").
                                                          getString("NoTransitsMessage"));

                return;
            }
            jmri.jmrit.dispatcher.DispatcherFrame df = jmri.jmrit.dispatcher.DispatcherFrame.instance();

            if (!df.getNewTrainActive()) {
                df.getActiveTrainFrame().initiateTrain(event, null, null);
                df.setNewTrainActive(true);
            } else {
                df.getActiveTrainFrame().showActivateFrame(null);
            }
        });
        menuBar.add(dispMenu);
    }   //setupDispatcherMenu

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
                setDirty(true);
            }
        }
        super.removeMarkers();
        repaint();
    }   //removeMarkers

    //operational variables for enter track width pane
    private JmriJFrame enterTrackWidthFrame = null;
    private boolean enterWidthOpen = false;
    private boolean trackWidthChange = false;
    private JTextField sideWidthField = new JTextField(6);
    private JTextField mainlineWidthField = new JTextField(6);
    private JButton trackWidthDone;
    private JButton trackWidthCancel;

    //display dialog for entering track widths
    protected void enterTrackWidth() {
        if (enterWidthOpen) {
            enterTrackWidthFrame.setVisible(true);

            return;
        }

        //Initialize if needed
        if (enterTrackWidthFrame == null) {
            enterTrackWidthFrame = new JmriJFrame(rb.getString("SetTrackWidth"));
            enterTrackWidthFrame.addHelpMenu("package.jmri.jmrit.display.EnterTrackWidth", true);
            enterTrackWidthFrame.setLocation(70, 30);
            Container theContentPane = enterTrackWidthFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup mainline track width (placed above side track for clarity, name 'panel3' kept)
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel mainlineWidthLabel = new JLabel(rb.getString("MainlineTrackWidth"));
            panel3. add(mainlineWidthLabel);
            panel3. add(mainlineWidthField);
            mainlineWidthField.setToolTipText(rb.getString("MainlineTrackWidthHint"));
            theContentPane.add(panel3);

            //setup side track width
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel sideWidthLabel = new JLabel(rb.getString("SideTrackWidth"));
            panel2. add(sideWidthLabel);
            panel2. add(sideWidthField);
            sideWidthField.setToolTipText(rb.getString("SideTrackWidthHint"));
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
        mainlineWidthField.setText("" + getMainlineTrackWidth());
        sideWidthField.setText("" + getSideTrackWidth());
        enterTrackWidthFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                trackWidthCancelPressed(null);
            }
        });
        enterTrackWidthFrame.pack();
        enterTrackWidthFrame.setVisible(true);
        trackWidthChange = false;
        enterWidthOpen = true;
    }   //enterTrackWidth

    void trackWidthDonePressed(ActionEvent a) {
        String newWidth = "";
        float wid = 0.0F;

        //get side track width
        newWidth = sideWidthField.getText().trim();
        try {
            wid = Float.parseFloat(newWidth);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((wid <= 0.99) || (wid > 10.0)) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                                          java.text.MessageFormat.format(rb.getString("Error2"),
                                                                         new Object[] {" " + wid + " "}),
                                          Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (sideTrackWidth != wid) {
            sideTrackWidth = wid;
            trackWidthChange = true;
        }

        //get mainline track width
        newWidth = mainlineWidthField.getText().trim();
        try {
            wid = Float.parseFloat(newWidth);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame, rb.getString("EntryError") + ": " +
                                          e + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((wid <= 0.99) || (wid > 10.0)) {
            JOptionPane.showMessageDialog(enterTrackWidthFrame,
                                          java.text.MessageFormat.format(rb.getString("Error2"),
                                                                         new Object[] {" " + wid + " "}),
                                          Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (mainlineTrackWidth != wid) {
            mainlineTrackWidth = wid;
            trackWidthChange = true;
        }

        //success - hide dialog and repaint if needed
        enterWidthOpen = false;
        enterTrackWidthFrame.setVisible(false);
        enterTrackWidthFrame.dispose();
        enterTrackWidthFrame = null;

        if (trackWidthChange) {
            repaint();
            setDirty(true);
        }
    }   //trackWidthDonePressed

    void trackWidthCancelPressed(ActionEvent a) {
        enterWidthOpen = false;
        enterTrackWidthFrame.setVisible(false);
        enterTrackWidthFrame.dispose();
        enterTrackWidthFrame = null;

        if (trackWidthChange) {
            repaint();
            setDirty(true);
        }
    }   //trackWidthCancelPressed

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
            enterReporterFrame = new JmriJFrame(rb.getString("AddReporter"));

//enterReporterFrame.addHelpMenu("package.jmri.jmrit.display.AddReporterLabel", true);
            enterReporterFrame.setLocation(70, 30);
            Container theContentPane = enterReporterFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup reporter entry
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel reporterLabel = new JLabel(rb.getString("ReporterName"));
            panel2. add(reporterLabel);
            panel2. add(reporterNameField);
            reporterNameField.setToolTipText(rb.getString("ReporterNameHint"));
            theContentPane.add(panel2);

            //setup coordinates entry
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel xCoordLabel = new JLabel(rb.getString("ReporterLocationX"));
            panel3. add(xCoordLabel);
            panel3. add(xPositionField);
            xPositionField.setToolTipText(rb.getString("ReporterLocationXHint"));
            JLabel yCoordLabel = new JLabel(rb.getString("ReporterLocationY"));
            panel3. add(yCoordLabel);
            panel3. add(yPositionField);
            yPositionField.setToolTipText(rb.getString("ReporterLocationYHint"));
            theContentPane.add(panel3);

            //set up Add and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(reporterDone = new JButton(rb.getString("AddNewLabel")));
            reporterDone.addActionListener((ActionEvent event) -> {
                reporterDonePressed(event);
            });
            reporterDone.setToolTipText(rb.getString("ReporterDoneHint"));

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
        xPositionField.setText("" + defaultX);
        yPositionField.setText("" + defaultY);
        enterReporterFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                reporterCancelPressed(null);
            }
        });
        enterReporterFrame.pack();
        enterReporterFrame.setVisible(true);
        reporterOpen = true;
    }   //enterReporter

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
            JOptionPane.showMessageDialog(enterReporterFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((xx <= 0) || (xx > dim.width)) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                                          java.text.MessageFormat.format(rb.getString("Error2a"),
                                                                         new Object[] {" " + xx + " "}),
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
            JOptionPane.showMessageDialog(enterReporterFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((yy <= 0) || (yy > dim.height)) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                                          java.text.MessageFormat.format(rb.getString("Error2a"),
                                                                         new Object[] {" " + yy + " "}),
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
                                              java.text.MessageFormat.format(rb.getString("Error18"),
                                                                             new Object[] {rName}), Bundle.getMessage("ErrorTitle"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }

            if (!rName.equals(reporter.getUserName())) {
                rName = rName.toUpperCase();
            }
        } else {
            JOptionPane.showMessageDialog(enterReporterFrame,
                                          rb.getString("Error17"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //add the reporter icon
        addReporter(rName, xx, yy);

        //success - repaint the panel
        repaint();
        enterReporterFrame.setVisible(true);
    }   //reporterDonePressed

    void reporterCancelPressed(ActionEvent a) {
        reporterOpen = false;
        enterReporterFrame.setVisible(false);
        enterReporterFrame.dispose();
        enterReporterFrame = null;
        repaint();
    }   //reporterCancelPressed

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
            scaleTrackDiagramFrame = new JmriJFrame(rb.getString("ScaleTrackDiagram"));
            scaleTrackDiagramFrame.addHelpMenu("package.jmri.jmrit.display.ScaleTrackDiagram", true);
            scaleTrackDiagramFrame.setLocation(70, 30);
            Container theContentPane = scaleTrackDiagramFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xTranslateLabel = new JLabel(rb.getString("XTranslateLabel"));
            panel31.add(xTranslateLabel);
            panel31.add(xTranslateField);
            xTranslateField.setToolTipText(rb.getString("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yTranslateLabel = new JLabel(rb.getString("YTranslateLabel"));
            panel32.add(yTranslateLabel);
            panel32.add(yTranslateField);
            yTranslateField.setToolTipText(rb.getString("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message 1
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(rb.getString("Message1Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //setup x factor
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            JLabel xFactorLabel = new JLabel(rb.getString("XFactorLabel"));
            panel21.add(xFactorLabel);
            panel21.add(xFactorField);
            xFactorField.setToolTipText(rb.getString("FactorHint"));
            theContentPane.add(panel21);

            //setup y factor
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
            JLabel yFactorLabel = new JLabel(rb.getString("YFactorLabel"));
            panel22.add(yFactorLabel);
            panel22.add(yFactorField);
            yFactorField.setToolTipText(rb.getString("FactorHint"));
            theContentPane.add(panel22);

            //setup information message 2
            JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
            JLabel message2Label = new JLabel(rb.getString("Message2Label"));
            panel23.add(message2Label);
            theContentPane.add(panel23);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(scaleTrackDiagramDone = new JButton(rb.getString("ScaleTranslate")));
            scaleTrackDiagramDone.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramDonePressed(event);
            });
            scaleTrackDiagramDone.setToolTipText(rb.getString("ScaleTranslateHint"));

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
    }   //scaleTrackDiagram

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
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y translation
        newText = yTranslateField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get x factor
        newText = xFactorField.getText().trim();
        try {
            xFactor = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y factor
        newText = yFactorField.getText().trim();
        try {
            yFactor = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(scaleTrackDiagramFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
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
            repaint();
            setDirty(true);
        }
    }   //scaleTrackDiagramDonePressed

    void scaleTrackDiagramCancelPressed(ActionEvent a) {
        scaleTrackDiagramOpen = false;
        scaleTrackDiagramFrame.setVisible(false);
        scaleTrackDiagramFrame.dispose();
        scaleTrackDiagramFrame = null;
    }   //scaleTrackDiagramCancelPressed

    boolean translateTrack(float xDel, float yDel) {
        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            Point2D center = t.getCoordsCenter();
            t.setCoordsCenter(new Point2D.Double(center.getX() + xDel, center.getY() + yDel));
        }

        //loop over all defined level crossings
        for (LevelXing x : xingList) {
            Point2D center = x.getCoordsCenter();
            x.setCoordsCenter(new Point2D.Double(center.getX() + xDel, center.getY() + yDel));
        }

        //loop over all defined slips
        for (LayoutSlip sl : slipList) {
            Point2D center = sl.getCoordsCenter();
            sl.setCoordsCenter(new Point2D.Double(center.getX() + xDel, center.getY() + yDel));
        }

        //loop over all defined turntables
        for (LayoutTurntable x : turntableList) {
            Point2D center = x.getCoordsCenter();
            x.setCoordsCenter(new Point2D.Double(center.getX() + xDel, center.getY() + yDel));
        }

        //loop over all defined Anchor Points and End Bumpers
        for (PositionablePoint p : pointList) {
            Point2D coord = p.getCoords();
            p.setCoords(new Point2D.Double(coord.getX() + xDel, coord.getY() + yDel));
        }
        return true;
    }   //translateTrack

    boolean scaleTrack(float xFactor, float yFactor) {
        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            t.scaleCoords(xFactor, yFactor);
        }

        //loop over all defined level crossings
        for (LevelXing x : xingList) {
            x.scaleCoords(xFactor, yFactor);
        }

        //loop over all defined slips
        for (LayoutSlip sl : slipList) {
            sl.scaleCoords(xFactor, yFactor);
        }

        //loop over all defined turntables
        for (LayoutTurntable x : turntableList) {
            x.scaleCoords(xFactor, yFactor);
        }

        //loop over all defined Anchor Points and End Bumpers
        for (PositionablePoint p : pointList) {
            Point2D coord = p.getCoords();
            p.setCoords(new Point2D.Double(Math.round(coord.getX() * xFactor),
                                           Math.round(coord.getY() * yFactor)));
        }

        //update the overall scale factors
        xScale = xScale * xFactor;
        yScale = yScale * yFactor;

        return true;
    }   //scaleTrack

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
            JOptionPane.showMessageDialog(this, rb.getString("Error12"),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (moveSelectionOpen) {
            moveSelectionFrame.setVisible(true);

            return;
        }

        //Initialize if needed
        if (moveSelectionFrame == null) {
            moveSelectionFrame = new JmriJFrame(rb.getString("TranslateSelection"));
            moveSelectionFrame.addHelpMenu("package.jmri.jmrit.display.TranslateSelection", true);
            moveSelectionFrame.setLocation(70, 30);
            Container theContentPane = moveSelectionFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xMoveLabel = new JLabel(rb.getString("XTranslateLabel"));
            panel31.add(xMoveLabel);
            panel31.add(xMoveField);
            xMoveField.setToolTipText(rb.getString("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yMoveLabel = new JLabel(rb.getString("YTranslateLabel"));
            panel32.add(yMoveLabel);
            panel32.add(yMoveField);
            yMoveField.setToolTipText(rb.getString("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(rb.getString("Message3Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(moveSelectionDone = new JButton(rb.getString("MoveSelection")));
            moveSelectionDone.addActionListener((ActionEvent event) -> {
                moveSelectionDonePressed(event);
            });
            moveSelectionDone.setToolTipText(rb.getString("MoveSelectionHint"));

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
    }   //moveSelection

    void moveSelectionDonePressed(ActionEvent a) {
        String newText = "";
        float xTranslation = 0.0F;
        float yTranslation = 0.0F;

        //get x translation
        newText = xMoveField.getText().trim();
        try {
            xTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(moveSelectionFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //get y translation
        newText = yMoveField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(moveSelectionFrame, rb.getString("EntryError") + ": " +
                                          e + " " + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                          JOptionPane.ERROR_MESSAGE);

            return;
        }

        //here when all numbers read in - translation if entered
        if ((xTranslation != 0.0F) || (yTranslation != 0.0F)) {
            //set up selection rectangle
            Rectangle2D selectRect = new Rectangle2D.Double(selectionX, selectionY,
                                                            selectionWidth, selectionHeight);

            //set up undo information
            undoRect = new Rectangle2D.Double(selectionX + xTranslation, selectionY + yTranslation,
                                              selectionWidth, selectionHeight);
            undoDeltaX = -xTranslation;
            undoDeltaY = -yTranslation;
            canUndoMoveSelection = true;

            //apply translation to icon items within the selection
            List<Positionable> contents = getContents();

            for (Positionable c : contents) {
                Point2D upperLeft = c.getLocation();

                if (selectRect.contains(upperLeft)) {
                    int xNew = (int) (upperLeft.getX() + xTranslation);
                    int yNew = (int) (upperLeft.getY() + yTranslation);
                    c.setLocation(xNew, yNew);
                }
            }

            //loop over all defined turnouts
            for (LayoutTurnout t : turnoutList) {
                if ((t.getVersion() == 2) && ((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                                              (t.getTurnoutType() == LayoutTurnout.LH_XOVER) ||
                                              (t.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
                    if (selectRect.contains(t.getCoordsA())) {
                        Point2D coord = t.getCoordsA();
                        t.setCoordsA(new Point2D.Double(coord.getX() + xTranslation,
                                                        coord.getY() + yTranslation));
                    }

                    if (selectRect.contains(t.getCoordsB())) {
                        Point2D coord = t.getCoordsB();
                        t.setCoordsB(new Point2D.Double(coord.getX() + xTranslation,
                                                        coord.getY() + yTranslation));
                    }

                    if (selectRect.contains(t.getCoordsC())) {
                        Point2D coord = t.getCoordsC();
                        t.setCoordsC(new Point2D.Double(coord.getX() + xTranslation,
                                                        coord.getY() + yTranslation));
                    }

                    if (selectRect.contains(t.getCoordsD())) {
                        Point2D coord = t.getCoordsD();
                        t.setCoordsD(new Point2D.Double(coord.getX() + xTranslation,
                                                        coord.getY() + yTranslation));
                    }
                } else {
                    Point2D center = t.getCoordsCenter();

                    if (selectRect.contains(center)) {
                        t.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                                                             center.getY() + yTranslation));
                    }
                }
            }

            //loop over all defined level crossings
            for (LevelXing x : xingList) {
                Point2D center = x.getCoordsCenter();

                if (selectRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                                                         center.getY() + yTranslation));
                }
            }

            //loop over all defined slips
            for (LayoutSlip sl : slipList) {
                Point2D center = sl.getCoordsCenter();

                if (selectRect.contains(center)) {
                    sl.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                                                          center.getY() + yTranslation));
                }
            }

            //loop over all defined turntables
            for (LayoutTurntable x : turntableList) {
                Point2D center = x.getCoordsCenter();

                if (selectRect.contains(center)) {
                    x.setCoordsCenter(new Point2D.Double(center.getX() + xTranslation,
                                                         center.getY() + yTranslation));
                }
            }

            //loop over all defined Anchor Points and End Bumpers
            for (PositionablePoint p : pointList) {
                Point2D coord = p.getCoords();

                if (selectRect.contains(coord)) {
                    p.setCoords(new Point2D.Double(coord.getX() + xTranslation,
                                                   coord.getY() + yTranslation));
                }
            }
            repaint();
            setDirty(true);
        }

        //success - hide dialog
        moveSelectionOpen = false;
        moveSelectionFrame.setVisible(false);
        moveSelectionFrame.dispose();
        moveSelectionFrame = null;
    }   //moveSelectionDonePressed

    void moveSelectionCancelPressed(ActionEvent a) {
        moveSelectionOpen = false;
        moveSelectionFrame.setVisible(false);
        moveSelectionFrame.dispose();
        moveSelectionFrame = null;
    }   //moveSelectionCancelPressed

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
                if ((t.getVersion() == 2) && ((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                                              (t.getTurnoutType() == LayoutTurnout.LH_XOVER) ||
                                              (t.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
                    if (undoRect.contains(t.getCoordsA())) {
                        Point2D coord = t.getCoordsA();
                        t.setCoordsA(new Point2D.Double(coord.getX() + undoDeltaX,
                                                        coord.getY() + undoDeltaY));
                    }

                    if (undoRect.contains(t.getCoordsB())) {
                        Point2D coord = t.getCoordsB();
                        t.setCoordsB(new Point2D.Double(coord.getX() + undoDeltaX,
                                                        coord.getY() + undoDeltaY));
                    }

                    if (undoRect.contains(t.getCoordsC())) {
                        Point2D coord = t.getCoordsC();
                        t.setCoordsC(new Point2D.Double(coord.getX() + undoDeltaX,
                                                        coord.getY() + undoDeltaY));
                    }

                    if (undoRect.contains(t.getCoordsD())) {
                        Point2D coord = t.getCoordsD();
                        t.setCoordsD(new Point2D.Double(coord.getX() + undoDeltaX,
                                                        coord.getY() + undoDeltaY));
                    }
                } else {
                    Point2D center = t.getCoordsCenter();

                    if (undoRect.contains(center)) {
                        t.setCoordsCenter(new Point2D.Double(center.getX() + undoDeltaX,
                                                             center.getY() + undoDeltaY));
                    }
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
                Point2D coord = p.getCoords();

                if (undoRect.contains(coord)) {
                    p.setCoords(new Point2D.Double(coord.getX() + undoDeltaX,
                                                   coord.getY() + undoDeltaY));
                }
            }
            repaint();
            canUndoMoveSelection = false;
        }
    }   //undoMoveSelection

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

        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
            String windowFrameRef = getWindowFrameRef();

            //the restore code for this isn't working…
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
                log.info("testing prefsProp: " + prefsProp);
            }
        });

        log.debug(
            "setCurrentPositionAndSize Position - " + upperLeftX + "," + upperLeftY + " WindowSize - " + windowWidth + "," + windowHeight + " PanelSize - " + panelWidth + "," +
            panelHeight);
        setDirty(true);
    }   //setCurrentPositionAndSize()

    void addBackgroundColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultBackgroundColor.equals(desiredColor)) {
                    defaultBackgroundColor = desiredColor;
                    setBackgroundColor(desiredColor);
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        backgroundColorButtonGroup.add(r);

        if (defaultBackgroundColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        backgroundColorMenuItems[backgroundColorCount] = r;
        backgroundColors[backgroundColorCount] = color;
        backgroundColorCount++;
    }   //addBackgroundColorMenuEntry

    void addTrackColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultTrackColor.equals(desiredColor)) {
                    LayoutTrack.setDefaultTrackColor(desiredColor);
                    defaultTrackColor = desiredColor;
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        trackColorButtonGroup.add(r);

        if (defaultTrackColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        trackColorMenuItems[trackColorCount] = r;
        trackColors[trackColorCount] = color;
        trackColorCount++;
    }   //addTrackColorMenuEntry

    void addTrackOccupiedColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultOccupiedTrackColor.equals(desiredColor)) {
                    defaultOccupiedTrackColor = desiredColor;
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        trackOccupiedColorButtonGroup.add(r);

        if (defaultOccupiedTrackColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        trackOccupiedColorMenuItems[trackOccupiedColorCount] = r;
        trackOccupiedColors[trackOccupiedColorCount] = color;
        trackOccupiedColorCount++;
    }   //addTrackOccupiedColorMenuEntry

    void addTrackAlternativeColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultAlternativeTrackColor.equals(desiredColor)) {
                    defaultAlternativeTrackColor = desiredColor;
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        trackAlternativeColorButtonGroup.add(r);

        if (defaultAlternativeTrackColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        trackAlternativeColorMenuItems[trackAlternativeColorCount] = r;
        trackAlternativeColors[trackAlternativeColorCount] = color;
        trackAlternativeColorCount++;
    }   //addTrackAlternativeColorMenuEntry

    protected void setOptionMenuTrackColor() {
        for (int i = 0; i < trackColorCount; i++) {
            trackColorMenuItems[i].setSelected(trackColors[i].equals(defaultTrackColor));
        }

        for (int i = 0; i < trackOccupiedColorCount; i++) {
            trackOccupiedColorMenuItems[i].setSelected(trackOccupiedColors[i].equals(defaultOccupiedTrackColor));
        }

        for (int i = 0; i < trackAlternativeColorCount; i++) {
            trackAlternativeColorMenuItems[i].setSelected(trackAlternativeColors[i].equals(defaultAlternativeTrackColor));
        }
    }   //setOptionMenuTrackColor

    void addTextColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultTextColor.equals(desiredColor)) {
                    defaultTextColor = desiredColor;
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        textColorButtonGroup.add(r);

        if (defaultTextColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        textColorMenuItems[textColorCount] = r;
        textColors[textColorCount] = color;
        textColorCount++;
    }   //addTextColorMenuEntry

    void addTurnoutCircleColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                setTurnoutCircleColor(ColorUtil.colorToString(desiredColor));
                setDirty(true);
                repaint();
            }   //actionPerformed
        };

        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        turnoutCircleColorButtonGroup.add(r);

        if (turnoutCircleColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        turnoutCircleColorMenuItems[turnoutCircleColorCount] = r;
        turnoutCircleColors[turnoutCircleColorCount] = color;
        turnoutCircleColorCount++;
    }   //addTurnoutCircleColorMenuEntry

    void addTurnoutCircleSizeMenuEntry(JMenu menu, final String name, final int size) {
        ActionListener a = new ActionListener() {
            final int desiredSize = size;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getTurnoutCircleSize() != desiredSize) {
                    setTurnoutCircleSize(desiredSize);
                    setDirty(true);
                    repaint();
                }
            }   //actionPerformed
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        turnoutCircleSizeButtonGroup.add(r);

        if (getTurnoutCircleSize() == size) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        turnoutCircleSizeMenuItems[turnoutCircleSizeCount] = r;
        turnoutCircleSizes[turnoutCircleSizeCount] = size;
        turnoutCircleSizeCount++;
    }   //addTurnoutCircleSizeMenuEntry

    protected void setOptionMenuTurnoutCircleColor() {
        for (int i = 0; i < turnoutCircleColorCount; i++) {
            if ((turnoutCircleColors[i] == null) && (turnoutCircleColor == null)) {
                turnoutCircleColorMenuItems[i].setSelected(true);
            } else if ((turnoutCircleColors[i] != null) && turnoutCircleColors[i].equals(turnoutCircleColor)) {
                turnoutCircleColorMenuItems[i].setSelected(true);
            } else {
                turnoutCircleColorMenuItems[i].setSelected(false);
            }
        }
    }   //setOptionMenuTurnoutCircleColor

    protected void setOptionMenuTurnoutCircleSize() {
        for (int i = 0; i < turnoutCircleSizeCount; i++) {
            if (turnoutCircleSizes[i] == getTurnoutCircleSize()) {
                turnoutCircleSizeMenuItems[i].setSelected(true);
            } else {
                turnoutCircleSizeMenuItems[i].setSelected(false);
            }
        }
    }   //setOptionMenuTurnoutCircleSize

    protected void setOptionMenuTextColor() {
        for (int i = 0; i < textColorCount; i++) {
            if (textColors[i].equals(defaultTextColor)) {
                textColorMenuItems[i].setSelected(true);
            } else {
                textColorMenuItems[i].setSelected(false);
            }
        }
    }   //setOptionMenuTextColor

    protected void setOptionMenuBackgroundColor() {
        for (int i = 0; i < backgroundColorCount; i++) {
            if (backgroundColors[i].equals(defaultBackgroundColor)) {
                backgroundColorMenuItems[i].setSelected(true);
            } else {
                backgroundColorMenuItems[i].setSelected(false);
            }
        }
    }   //setOptionMenuBackgroundColor

    @Override
    public void setScroll(int state) {
        if (isEditable()) {
            //In edit mode the scroll bars are always displayed, however we will want to set the scroll for when we exit edit mode
            super.setScroll(SCROLL_BOTH);
            _scrollState = state;
        } else {
            super.setScroll(state);
        }
    }   //setScroll

    /**
     * Add a layout turntable at location specified
     */
    public void addTurntable(Point2D pt) {
        numLayoutTurntables++;
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "TUR" + numLayoutTurntables;

            if (finder.findLayoutTurntableByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numLayoutTurntables++;
            }
        }
        LayoutTurntable x = new LayoutTurntable(name, pt, this);

        //if (x != null) {
        turntableList.add(x);
        setDirty(true);

        //}
        x.  addRay(0.0);
        x.  addRay(90.0);
        x.  addRay(180.0);
        x.  addRay(270.0);
    }   //addTurntable

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
    }   //resetDirty

    /**
     * Allow external set of dirty bit
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
    private void calcLocation(MouseEvent event, int dX, int dY) {
        xLoc = (int) ((event.getX() + dX) / getPaintScale());
        yLoc = (int) ((event.getY() + dY) / getPaintScale());
        dLoc.setLocation(xLoc, yLoc);
    }   //calcLocation

    /**
     * Handle a mouse pressed event
     */
    @Override
    public void mousePressed(MouseEvent event) {
        //initialize cursor position
        _anchorX = xLoc;
        _anchorY = yLoc;
        _lastX = _anchorX;
        _lastY = _anchorY;
        calcLocation(event, 0, 0);

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
                    checkPopUp(event);
                }
            }

            if (event.isMetaDown() || event.isAltDown()) {
                //if moving an item, identify the item for mouseDragging
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;

                if (checkSelect(dLoc, false)) {
                    selectedObject = foundObject;
                    selectedPointType = foundPointType;

                    //selectedNeedsConnect = foundNeedsConnect;
                    startDel.setLocation(foundLocation.getX() - dLoc.getX(), foundLocation.getY() - dLoc.getY());
                    foundObject = null;
                } else {
                    selectedObject = checkMarkers(dLoc);

                    if (selectedObject != null) {
                        selectedPointType = LayoutTrack.MARKER;
                        startDel.setLocation((((LocoIcon) selectedObject).getX() - dLoc.getX()),
                                             (((LocoIcon) selectedObject).getY() - dLoc.getY()));

                        //selectedNeedsConnect = false;
                    } else {
                        selectedObject = checkClocks(dLoc);

                        if (selectedObject != null) {
                            selectedPointType = LayoutTrack.LAYOUT_POS_JCOMP;
                            startDel.setLocation((((PositionableJComponent) selectedObject).getX() - dLoc.getX()),
                                                 (((PositionableJComponent) selectedObject).getY() - dLoc.getY()));

                            //selectedNeedsConnect = false;
                        } else {
                            selectedObject = checkMultiSensors(dLoc);

                            if (selectedObject != null) {
                                selectedPointType = LayoutTrack.MULTI_SENSOR;
                                startDel.setLocation((((MultiSensorIcon) selectedObject).getX() - dLoc.getX()),
                                                     (((MultiSensorIcon) selectedObject).getY() - dLoc.getY()));

                                //selectedNeedsConnect = false;
                            }
                        }
                    }

                    if (selectedObject == null) {
                        selectedObject = checkSensorIcons(dLoc);

                        if (selectedObject == null) {
                            selectedObject = checkSignalHeadIcons(dLoc);

                            if (selectedObject == null) {
                                selectedObject = checkLabelImages(dLoc);

                                if (selectedObject == null) {
                                    selectedObject = checkSignalMastIcons(dLoc);
                                }
                            }
                        }

                        if (selectedObject != null) {
                            selectedPointType = LayoutTrack.LAYOUT_POS_LABEL;
                            startDel.setLocation((((PositionableLabel) selectedObject).getX() - dLoc.getX()),
                                                 (((PositionableLabel) selectedObject).getY() - dLoc.getY()));

                            if (selectedObject instanceof MemoryIcon) {
                                MemoryIcon pm = (MemoryIcon) selectedObject;

                                if (pm.getPopupUtility().getFixedWidth() == 0) {
                                    startDel.setLocation((pm.getOriginalX() - dLoc.getX()),
                                                         (pm.getOriginalY() - dLoc.getY()));
                                }
                            }

                            //selectedNeedsConnect = false;
                        } else {
                            selectedObject = checkBackgrounds(dLoc);

                            if (selectedObject != null) {
                                selectedPointType = LayoutTrack.LAYOUT_POS_LABEL;
                                startDel.setLocation((((PositionableLabel) selectedObject).getX() - dLoc.getX()),
                                                     (((PositionableLabel) selectedObject).getY() - dLoc.getY()));

                                //selectedNeedsConnect = false;
                            }
                        }
                    }
                }
            } else if (event.isShiftDown() && trackButton.isSelected() && (!event.isPopupTrigger())) {
                //starting a Track Segment, check for free connection point
                selectedObject = null;

                if (checkSelect(dLoc, true)) {
                    //match to a free connection point
                    beginObject = foundObject;
                    beginPointType = foundPointType;
                    beginLocation = foundLocation;
                } else {    //TODO: auto-add anchor point?
                    foundObject = null;
                    beginObject = null;
                }
            } else if ((!event.isShiftDown()) && (!event.isControlDown()) && (!event.isPopupTrigger())) {
                //check if controlling a turnout in edit mode
                selectedObject = null;

                if (allControlling()) {
                    //check if mouse is on a turnout
                    selectedObject = null;

                    for (LayoutTurnout t : turnoutList) {
                        //check the center point
                        Point2D pt = t.getCoordsCenter();
                        Double distance = dLoc.distance(pt);

                        if (distance <= circleRadius) {
                            //mouse was pressed on this turnout
                            selectedObject = t;
                            selectedPointType = LayoutTrack.TURNOUT_CENTER;
                            break;
                        }
                    }

                    for (LayoutSlip sl : slipList) {
                        //check east/west turnout (control) circles?
                        Point2D pt = sl.getCoordsCenter();

                        Point2D leftCenter = midpoint(sl.getCoordsA(), sl.getCoordsB());
                        Double leftFract = circleRadius / pt.distance(leftCenter);
                        Point2D leftCircleCenter = lerp(pt, leftCenter, leftFract);
                        Double leftDistance = dLoc.distance(leftCircleCenter);

                        Point2D rightCenter = midpoint(sl.getCoordsC(), sl.getCoordsD());
                        Double rightFract = circleRadius / pt.distance(rightCenter);
                        Point2D rightCircleCenter = lerp(pt, rightCenter, rightFract);
                        Double rightDistance = dLoc.distance(rightCircleCenter);

                        if ((leftDistance <= circleRadius) || (rightDistance <= circleRadius)) {
                            //mouse was pressed on this turnout
                            selectedObject = sl;
                            selectedPointType = (leftDistance < rightDistance) ? LayoutTrack.SLIP_LEFT : LayoutTrack.SLIP_RIGHT;
                            break;
                        }
                    }

                    for (LayoutTurntable x : turntableList) {
                        for (int k = 0; k < x.getNumberRays(); k++) {
                            if (x.getRayConnectOrdered(k) != null) {
                                //check the A connection point
                                Point2D pt = x.getRayCoordsOrdered(k);
                                Rectangle2D r = controlPointRectAt(pt);

                                if (r.contains(dLoc)) {
                                    //mouse was pressed on this connection point
                                    selectedObject = x;
                                    selectedPointType = LayoutTrack.TURNTABLE_RAY_OFFSET + x.getRayIndex(k);
                                    break;
                                }
                            }
                        }
                    }
                }   //if (allControlling())
                //initialize starting selection - cancel any previous selection rectangle
                selectionActive = true;
                selectionX = dLoc.getX();
                selectionY = dLoc.getY();
                selectionWidth = 0.0;
                selectionHeight = 0.0;
            }

            if (prevSelectionActive) {
                repaint();
            }
        } else if (allControlling() && (!event.isMetaDown()) && (!event.isPopupTrigger()) &&
                   (!event.isAltDown()) && (!event.isShiftDown()) && (!event.isControlDown())) {
            //not in edit mode - check if mouse is on a turnout (using wider search range)
            selectedObject = null;

            for (LayoutTurnout t : turnoutList) {
                Point2D pt = t.getCoordsCenter();
                Rectangle2D r = turnoutCircleRectAt(pt);

                if (r.contains(dLoc)) {
                    //mouse was pressed on this turnout
                    selectedObject = t;
                    selectedPointType = LayoutTrack.TURNOUT_CENTER;
                    break;
                }
            }

            for (LayoutSlip sl : slipList) {
                //check east/west turnout (control) circles?
                Point2D pt = sl.getCoordsCenter();

                Point2D leftCenter = midpoint(sl.getCoordsA(), sl.getCoordsB());
                Double leftFract = circleRadius / pt.distance(leftCenter);
                Point2D leftCircleCenter = lerp(pt, leftCenter, leftFract);
                Rectangle2D leftRectangle = turnoutCircleRectAt(leftCircleCenter);

                if (leftRectangle.contains(dLoc)) {
                    //mouse was pressed on this turnout
                    selectedObject = sl;
                    selectedPointType = LayoutTrack.SLIP_LEFT;
                    break;
                }

                Point2D rightCenter = midpoint(sl.getCoordsC(), sl.getCoordsD());
                Double rightFract = circleRadius / pt.distance(rightCenter);
                Point2D rightCircleCenter = lerp(pt, rightCenter, rightFract);
                Rectangle2D rightRectangle = turnoutCircleRectAt(rightCircleCenter);

                if (rightRectangle.contains(dLoc)) {
                    //mouse was pressed on this turnout
                    selectedObject = sl;
                    selectedPointType = LayoutTrack.SLIP_RIGHT;
                    break;
                }
            }

            for (LayoutTurntable x : turntableList) {
                for (int k = 0; k < x.getNumberRays(); k++) {
                    if (x.getRayConnectOrdered(k) != null) {
                        //check the A connection point
                        Point2D pt = x.getRayCoordsOrdered(k);
                        Rectangle2D r = controlPointRectAt(pt);

                        if (r.contains(dLoc)) {
                            //mouse was pressed on this connection point
                            selectedObject = x;
                            selectedPointType = LayoutTrack.TURNTABLE_RAY_OFFSET + x.getRayIndex(k);
                            break;
                        }
                    }
                }
            }
        } else if ((event.isMetaDown() || event.isAltDown()) &&
                   (!event.isShiftDown()) && (!event.isControlDown())) {
            //not in edit mode - check if moving a marker if there are any
            selectedObject = checkMarkers(dLoc);

            if (selectedObject != null) {
                selectedPointType = LayoutTrack.MARKER;
                startDel.setLocation((((LocoIcon) selectedObject).getX() - dLoc.getX()),
                                     (((LocoIcon) selectedObject).getY() - dLoc.getY()));

                //selectedNeedsConnect = false;
            }
        } else if (event.isPopupTrigger() && (!event.isShiftDown())) {
            //not in edit mode - check if a marker popup menu is being requested
            LocoIcon lo = checkMarkers(dLoc);

            if (lo != null) {
                delayedPopupTrigger = true;
            }
        }

        if (!event.isPopupTrigger() && !isDragging) {
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                selections.get(0).doMousePressed(event);
            }
        }
        thisPanel.requestFocusInWindow();
    }   //mousePressed

    private boolean checkSelect(Point2D loc, boolean requireUnconnected) {
        return checkSelect(loc, requireUnconnected, null);
    }

    private boolean checkSelect(Point2D loc, boolean requireUnconnected, Object avoid) {
        //check positionable points, if any
        for (PositionablePoint p : pointList) {
            if (p != avoid) {
                if ((p != selectedObject) && !requireUnconnected ||
                    (p.getConnect1() == null) ||
                    ((p.getType() == PositionablePoint.ANCHOR) &&
                     (p.getConnect2() == null))) {
                    Point2D pt = p.getCoords();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = p;
                        foundPointType = LayoutTrack.POS_POINT;
                        foundNeedsConnect = ((p.getConnect1() == null) || (p.getConnect2() == null));

                        return true;
                    }
                }
            }
        }

        //check turnouts, if any
        for (LayoutTurnout t : turnoutList) {
            if (t != selectedObject) {
                if (!requireUnconnected) {
                    //check the center point
                    Point2D pt = t.getCoordsCenter();
                    Rectangle2D r = turnoutCircleRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = t;
                        foundPointType = LayoutTrack.TURNOUT_CENTER;
                        foundNeedsConnect = false;

                        return true;
                    }
                }

                if (!requireUnconnected || (t.getConnectA() == null)) {
                    //check the A connection point
                    Point2D pt = t.getCoordsA();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = t;
                        foundPointType = LayoutTrack.TURNOUT_A;
                        foundNeedsConnect = (t.getConnectA() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (t.getConnectB() == null)) {
                    //check the B connection point
                    Point2D pt = t.getCoordsB();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = t;
                        foundPointType = LayoutTrack.TURNOUT_B;
                        foundNeedsConnect = (t.getConnectB() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (t.getConnectC() == null)) {
                    //check the C connection point
                    Point2D pt = t.getCoordsC();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = t;
                        foundPointType = LayoutTrack.TURNOUT_C;
                        foundNeedsConnect = (t.getConnectC() == null);

                        return true;
                    }
                }

                if (((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                     (t.getTurnoutType() == LayoutTurnout.RH_XOVER) ||
                     (t.getTurnoutType() == LayoutTurnout.LH_XOVER)) && (!requireUnconnected || (t.getConnectD() == null))) {
                    //check the D connection point, double crossover turnouts only
                    Point2D pt = t.getCoordsD();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = t;
                        foundPointType = LayoutTrack.TURNOUT_D;
                        foundNeedsConnect = (t.getConnectD() == null);

                        return true;
                    }
                }
            }
        }

        //check level Xings, if any
        for (LevelXing x : xingList) {
            if (x != selectedObject) {
                if (!requireUnconnected) {
                    //check the center point
                    Point2D pt = x.getCoordsCenter();
                    Rectangle2D r = turnoutCircleRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.LEVEL_XING_CENTER;
                        foundNeedsConnect = false;

                        return true;
                    }
                }

                if (!requireUnconnected || (x.getConnectA() == null)) {
                    //check the A connection point
                    Point2D pt = x.getCoordsA();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.LEVEL_XING_A;
                        foundNeedsConnect = (x.getConnectA() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (x.getConnectB() == null)) {
                    //check the B connection point
                    Point2D pt = x.getCoordsB();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.LEVEL_XING_B;
                        foundNeedsConnect = (x.getConnectB() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (x.getConnectC() == null)) {
                    //check the C connection point
                    Point2D pt = x.getCoordsC();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.LEVEL_XING_C;
                        foundNeedsConnect = (x.getConnectC() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (x.getConnectD() == null)) {
                    //check the D connection point
                    Point2D pt = x.getCoordsD();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.LEVEL_XING_D;
                        foundNeedsConnect = (x.getConnectD() == null);

                        return true;
                    }
                }
            }
        }

        //check slips, if any
        for (LayoutSlip sl : slipList) {
            if (sl != selectedObject) {
                if (!requireUnconnected) {
                    Point2D pt = sl.getCoordsCenter();

                    Point2D leftCenter = midpoint(sl.getCoordsA(), sl.getCoordsB());
                    Double leftFract = circleRadius / pt.distance(leftCenter);
                    Point2D leftCircleCenter = lerp(pt, leftCenter, leftFract);
                    Double leftDistance = dLoc.distance(leftCircleCenter);

                    Point2D rightCenter = midpoint(sl.getCoordsC(), sl.getCoordsD());
                    Double rightFract = circleRadius / pt.distance(rightCenter);
                    Point2D rightCircleCenter = lerp(pt, rightCenter, rightFract);
                    Double rightDistance = dLoc.distance(rightCircleCenter);

                    if ((leftDistance <= circleRadius) || (rightDistance <= circleRadius)) {
                        //mouse was pressed on this turnout
                        foundLocation = pt;
                        foundObject = sl;
                        foundPointType = (leftDistance < rightDistance) ? LayoutTrack.SLIP_LEFT : LayoutTrack.SLIP_RIGHT;
                        foundNeedsConnect = false;

                        return true;
                    }
                }

                if (!requireUnconnected || (sl.getConnectA() == null)) {
                    //check the A connection point
                    Point2D pt = sl.getCoordsA();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = sl;
                        foundPointType = LayoutTrack.SLIP_A;
                        foundNeedsConnect = (sl.getConnectA() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (sl.getConnectB() == null)) {
                    //check the B connection point
                    Point2D pt = sl.getCoordsB();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = sl;
                        foundPointType = LayoutTrack.SLIP_B;
                        foundNeedsConnect = (sl.getConnectB() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (sl.getConnectC() == null)) {
                    //check the C connection point
                    Point2D pt = sl.getCoordsC();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = sl;
                        foundPointType = LayoutTrack.SLIP_C;
                        foundNeedsConnect = (sl.getConnectC() == null);

                        return true;
                    }
                }

                if (!requireUnconnected || (sl.getConnectD() == null)) {
                    //check the D connection point
                    Point2D pt = sl.getCoordsD();
                    Rectangle2D r = controlPointRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this connection point
                        foundLocation = pt;
                        foundObject = sl;
                        foundPointType = LayoutTrack.SLIP_D;
                        foundNeedsConnect = (sl.getConnectD() == null);

                        return true;
                    }
                }
            }
        }

        //check turntables, if any
        for (LayoutTurntable x : turntableList) {
            if (x != selectedObject) {
                if (!requireUnconnected) {
                    //check the center point
                    Point2D pt = x.getCoordsCenter();
                    Rectangle2D r = turnoutCircleRectAt(pt);

                    if (r.contains(loc)) {
                        //mouse was pressed on this center point
                        foundLocation = pt;
                        foundObject = x;
                        foundPointType = LayoutTrack.TURNTABLE_CENTER;
                        foundNeedsConnect = false;

                        return true;
                    }
                }

                for (int k = 0; k < x.getNumberRays(); k++) {
                    if (!requireUnconnected || (x.getRayConnectOrdered(k) == null)) {
                        Point2D pt = x.getRayCoordsOrdered(k);
                        Rectangle2D r = controlPointRectAt(pt);

                        if (r.contains(loc)) {
                            //mouse was pressed on this connection point
                            foundLocation = pt;
                            foundObject = x;
                            foundPointType = LayoutTrack.TURNTABLE_RAY_OFFSET + x.getRayIndex(k);
                            foundNeedsConnect = (x.getRayConnectOrdered(k) == null);

                            return true;
                        }
                    }
                }
            }
        }

        for (TrackSegment t : trackList) {
            if (t.getCircle()) {
                Point2D pt = t.getCoordsCenterCircle();
                Rectangle2D r = controlPointRectAt(pt);

                if (r.contains(loc)) {
                    //mouse was pressed on this connection point
                    foundLocation = pt;
                    foundObject = t;
                    foundPointType = LayoutTrack.TRACK_CIRCLE_CENTRE;
                    foundNeedsConnect = false;

                    return true;
                }
            }
        }

        //no connection point found
        foundObject = null;

        return false;
    }   //checkSelect

    private TrackSegment checkTrackSegments(Point2D loc) {
        //check Track Segments, if any
        for (TrackSegment tr : trackList) {
            Object o = tr.getConnect1();
            int type = tr.getType1();

            if (tr.getCircle()) {
                Rectangle2D r = turnoutCircleRectAt(
                    new Point2D.Double(tr.getCentreSegX(), tr.getCentreSegY()));

                //Test this detection rectangle
                if (r.contains(loc)) {  //mouse was pressed in detection rectangle
                    return tr;
                }
            } else {
                //get coordinates of first end point
                Point2D pt1 = getEndCoords(o, type);
                o = tr.getConnect2();
                type = tr.getType2();

                //get coordinates of second end point
                Point2D pt2 = getEndCoords(o, type);

                //construct a detection rectangle
                double cX = (pt1.getX() + pt2.getX()) / 2.0D;
                double cY = (pt1.getY() + pt2.getY()) / 2.0D;
                Rectangle2D r = turnoutCircleRectAt(new Point2D.Double(cX, cY));

                //Test this detection rectangle
                if (r.contains(loc)) {

                    //mouse was pressed in detection rectangle
                    return tr;
                }
            }
        }
        return null;
    }   //checkTrackSegments

    private PositionableLabel checkBackgrounds(Point2D loc) {
        //check background images, if any
        for (int i = backgroundImage.size() - 1; i >= 0; i--) {
            PositionableLabel b = backgroundImage.get(i);
            double x = b.getX();
            double y = b.getY();
            double w = b.maxWidth();
            double h = b.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in background image
                return b;
            }
        }
        return null;
    }   //checkBackgrounds

    private SensorIcon checkSensorIcons(Point2D loc) {
        //check sensor images, if any
        for (int i = sensorImage.size() - 1; i >= 0; i--) {
            SensorIcon s = sensorImage.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = s.maxWidth();
            double h = s.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in sensor icon image
                return s;
            }
        }
        return null;
    }   //checkSensorIcons

    private SignalHeadIcon checkSignalHeadIcons(Point2D loc) {
        //check signal head images, if any
        for (int i = signalHeadImage.size() - 1; i >= 0; i--) {
            SignalHeadIcon s = signalHeadImage.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = s.maxWidth();
            double h = s.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in signal head image
                return s;
            }
        }
        return null;
    }   //checkSignalHeadIcons

    private SignalMastIcon checkSignalMastIcons(Point2D loc) {
        //check signal head images, if any
        for (int i = signalMastList.size() - 1; i >= 0; i--) {
            SignalMastIcon s = signalMastList.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = s.maxWidth();
            double h = s.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in signal head image
                return s;
            }
        }
        return null;
    }   //checkSignalMastIcons

    private PositionableLabel checkLabelImages(Point2D loc) {
        PositionableLabel l = null;
        int level = 0;

        for (int i = labelImage.size() - 1; i >= 0; i--) {
            PositionableLabel s = labelImage.get(i);
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

            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {
                //mouse was pressed in label image
                if (s.getDisplayLevel() >= level) {
                    //Check to make sure that we are returning the highest level label.
                    l = s;
                    level = s.getDisplayLevel();
                }
            }
        }
        return l;
    }   //checkLabelImages

    private AnalogClock2Display checkClocks(Point2D loc) {
        //check clocks, if any
        for (int i = clocks.size() - 1; i >= 0; i--) {
            AnalogClock2Display s = clocks.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = s.getFaceWidth();
            double h = s.getFaceHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in clock image
                return s;
            }
        }
        return null;
    }   //checkClocks

    private MultiSensorIcon checkMultiSensors(Point2D loc) {
        //check multi sensor icons, if any
        for (int i = multiSensors.size() - 1; i >= 0; i--) {
            MultiSensorIcon s = multiSensors.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = s.maxWidth();
            double h = s.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in multi sensor image
                return s;
            }
        }
        return null;
    }   //checkMultiSensors

    private LocoIcon checkMarkers(Point2D loc) {
        //check marker icons, if any
        for (int i = markerImage.size() - 1; i >= 0; i--) {
            LocoIcon l = markerImage.get(i);
            double x = l.getX();
            double y = l.getY();
            double w = l.maxWidth();
            double h = l.maxHeight();
            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);

            //Test this detection rectangle
            if (r.contains(loc)) {

                //mouse was pressed in marker icon
                return l;
            }
        }
        return null;
    }   //checkMarkers

    public Point2D getEndCoords(Object o, int type) {
        if (o != null) {
            switch (type) {
                case LayoutTrack.POS_POINT: {
                    return ((PositionablePoint) o).getCoords();
                }

                case LayoutTrack.TURNOUT_A: {
                    return ((LayoutTurnout) o).getCoordsA();
                }

                case LayoutTrack.TURNOUT_B: {
                    return ((LayoutTurnout) o).getCoordsB();
                }

                case LayoutTrack.TURNOUT_C: {
                    return ((LayoutTurnout) o).getCoordsC();
                }

                case LayoutTrack.TURNOUT_D: {
                    return ((LayoutTurnout) o).getCoordsD();
                }

                case LayoutTrack.LEVEL_XING_A: {
                    return ((LevelXing) o).getCoordsA();
                }

                case LayoutTrack.LEVEL_XING_B: {
                    return ((LevelXing) o).getCoordsB();
                }

                case LayoutTrack.LEVEL_XING_C: {
                    return ((LevelXing) o).getCoordsC();
                }

                case LayoutTrack.LEVEL_XING_D: {
                    return ((LevelXing) o).getCoordsD();
                }

                case LayoutTrack.SLIP_A: {
                    return ((LayoutSlip) o).getCoordsA();
                }

                case LayoutTrack.SLIP_B: {
                    return ((LayoutSlip) o).getCoordsB();
                }

                case LayoutTrack.SLIP_C: {
                    return ((LayoutSlip) o).getCoordsC();
                }

                case LayoutTrack.SLIP_D: {
                    return ((LayoutSlip) o).getCoordsD();
                }

                default:

                    if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                        return ((LayoutTurntable) o).getRayCoordsIndexed(type - LayoutTrack.TURNTABLE_RAY_OFFSET);
                    }
            }   //switch
        }
        return new Point2D.Double(0.0, 0.0);
    }   //getEndCoords

    @Override
    public void mouseReleased(MouseEvent event) {
        super.setToolTip(null);

        //initialize mouse position
        calcLocation(event, 0, 0);

        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));

            if ((!event.isPopupTrigger()) && (!event.isMetaDown()) && (!event.isAltDown()) &&
                event.isShiftDown()) {
                currentPoint = new Point2D.Double(xLoc, yLoc);

                if (snapToGridOnAdd) {
                    xLoc = ((xLoc + (gridSize / 2)) / gridSize) * gridSize;
                    yLoc = ((yLoc + (gridSize / 2)) / gridSize) * gridSize;
                    currentPoint.setLocation(xLoc, yLoc);
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
                    if ((beginObject != null) && (foundObject != null) &&
                        (beginObject != foundObject)) {
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
                selectedObject = null;
                repaint();
            } else if ((event.isPopupTrigger() || delayedPopupTrigger) && !isDragging) {
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;
                whenReleased = event.getWhen();
                checkPopUp(event);
            }   //check if controlling turnouts
            else if ((selectedObject != null) && (selectedPointType == LayoutTrack.TURNOUT_CENTER) &&
                     allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                     (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling layout, in edit mode
                LayoutTurnout t = (LayoutTurnout) selectedObject;
                t.toggleTurnout();
            } else if ((selectedObject != null) && ((selectedPointType == LayoutTrack.SLIP_CENTER) ||
                                                    (selectedPointType == LayoutTrack.SLIP_LEFT) ||
                                                    (selectedPointType == LayoutTrack.SLIP_RIGHT)) &&
                       allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                       (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling layout, in edit mode
                LayoutSlip sl = (LayoutSlip) selectedObject;
                sl.toggleState(selectedPointType);
            } else if ((selectedObject != null) && (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) &&
                       allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                       (!event.isShiftDown()) && (!event.isControlDown())) {
                //controlling layout, in edit mode
                LayoutTurntable t = (LayoutTurntable) selectedObject;
                t.setPosition(selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
            } else if ((selectedObject != null) && (selectedPointType == LayoutTrack.TURNOUT_CENTER) &&
                       allControlling() && (event.isMetaDown()) && (!event.isAltDown()) &&
                       (!event.isShiftDown()) && (!event.isControlDown()) && isDragging) {
                //controlling layout, in edit mode
                checkPointsOfTurnout((LayoutTurnout) selectedObject);
            } else if ((selectedObject != null) && (selectedPointType == LayoutTrack.POS_POINT) &&
                       allControlling() && (event.isMetaDown()) && (!event.isAltDown()) &&
                       (!event.isShiftDown()) && (!event.isControlDown()) && isDragging) {
                PositionablePoint p = (PositionablePoint) selectedObject;

                if ((p.getConnect1() == null) || (p.getConnect2() == null)) {
                    checkPointOfPositionable(p);
                }
            }

            if ((trackButton.isSelected()) && (beginObject != null) && (foundObject != null)) {
                //user let up shift key before releasing the mouse when creating a track segment
                setCursor(Cursor.getDefaultCursor());
                beginObject = null;
                foundObject = null;
                repaint();
            }
            createSelectionGroups();
        }   //check if controlling turnouts out of edit mode
        else if ((selectedObject != null) && (selectedPointType == LayoutTrack.TURNOUT_CENTER) &&
                 allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                 (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            //controlling layout, not in edit mode
            if (useDirectTurnoutControl) {
                LayoutTurnout t = (LayoutTurnout) selectedObject;
                t.setState(jmri.Turnout.CLOSED);
            } else {
                LayoutTurnout t = (LayoutTurnout) selectedObject;
                t.toggleTurnout();
            }
        }   //check if controlling turnouts out of edit mode
        else if ((selectedObject != null) && ((selectedPointType == LayoutTrack.SLIP_CENTER) ||
                                              (selectedPointType == LayoutTrack.SLIP_LEFT) ||
                                              (selectedPointType == LayoutTrack.SLIP_RIGHT)) &&
                 allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                 (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            //controlling layout, not in edit mode
            LayoutSlip sl = (LayoutSlip) selectedObject;
            sl.toggleState(selectedPointType);
        } else if ((selectedObject != null) && (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) &&
                   allControlling() && (!event.isMetaDown()) && (!event.isAltDown()) && (!event.isPopupTrigger()) &&
                   (!event.isShiftDown()) && (!delayedPopupTrigger)) {
            LayoutTurntable t = (LayoutTurntable) selectedObject;
            t.setPosition(selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
        }   //check if requesting marker popup out of edit mode
        else if ((event.isPopupTrigger() || delayedPopupTrigger) && (!isDragging)) {
            LocoIcon lo = checkMarkers(dLoc);

            if (lo != null) {
                showPopUp(lo, event);
            } else {
                if (checkSelect(dLoc, false)) {
                    //show popup menu
                    switch (foundPointType) {
                        case LayoutTrack.TURNOUT_CENTER: {
                            if (useDirectTurnoutControl) {
                                LayoutTurnout t = (LayoutTurnout) foundObject;
                                t.setState(jmri.Turnout.THROWN);
                            } else {
                                ((LayoutTurnout) foundObject).showPopUp(event, isEditable());
                            }
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_CENTER: {
                            ((LevelXing) foundObject).showPopUp(event, isEditable());
                            break;
                        }

                        case LayoutTrack.SLIP_CENTER:
                        case LayoutTrack.SLIP_RIGHT:
                        case LayoutTrack.SLIP_LEFT: {
                            ((LayoutSlip) foundObject).showPopUp(event, isEditable());
                            break;
                        }

                        default: {
                            break;
                        }
                    }   //switch
                }
                AnalogClock2Display c = checkClocks(dLoc);

                if (c != null) {
                    showPopUp(c, event);
                } else {
                    SignalMastIcon sm = checkSignalMastIcons(dLoc);

                    if (sm != null) {
                        showPopUp(sm, event);
                    } else {
                        PositionableLabel im = checkLabelImages(dLoc);

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
                selections.get(0).doMouseReleased(event);
                whenReleased = event.getWhen();
            }
        }

        //train icon needs to know when moved
        if (event.isPopupTrigger() && isDragging) {
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
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
    }   //mouseReleased

    private void checkPopUp(MouseEvent event) {
        if (checkSelect(dLoc, false)) {
            //show popup menu
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    ((PositionablePoint) foundObject).showPopUp(event);
                    break;
                }

                case LayoutTrack.TURNOUT_CENTER: {
                    ((LayoutTurnout) foundObject).showPopUp(event, isEditable());
                    break;
                }

                case LayoutTrack.LEVEL_XING_CENTER: {
                    ((LevelXing) foundObject).showPopUp(event, isEditable());
                    break;
                }

                case LayoutTrack.SLIP_CENTER:
                case LayoutTrack.SLIP_LEFT:
                case LayoutTrack.SLIP_RIGHT: {
                    ((LayoutSlip) foundObject).showPopUp(event, isEditable());
                    break;
                }

                case LayoutTrack.TURNTABLE_CENTER: {
                    ((LayoutTurntable) foundObject).showPopUp(event);
                    break;
                }

                default: {
                    break;
                }
            }   //switch

            if (foundPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                LayoutTurntable t = (LayoutTurntable) foundObject;

                if (t.isTurnoutControlled()) {
                    ((LayoutTurntable) foundObject).showRayPopUp(event, foundPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
            }
        } else {
            TrackSegment tr = checkTrackSegments(dLoc);

            if (tr != null) {
                tr.showPopUp(event);
            } else {
                SensorIcon s = checkSensorIcons(dLoc);

                if (s != null) {
                    showPopUp(s, event);
                } else {
                    LocoIcon lo = checkMarkers(dLoc);

                    if (lo != null) {
                        showPopUp(lo, event);
                    } else {
                        SignalHeadIcon sh = checkSignalHeadIcons(dLoc);

                        if (sh != null) {
                            showPopUp(sh, event);
                        } else {
                            AnalogClock2Display c = checkClocks(dLoc);

                            if (c != null) {
                                showPopUp(c, event);
                            } else {
                                MultiSensorIcon ms = checkMultiSensors(dLoc);

                                if (ms != null) {
                                    showPopUp(ms, event);
                                } else {
                                    PositionableLabel lb = checkLabelImages(dLoc);

                                    if (lb != null) {
                                        showPopUp(lb, event);
                                    } else {
                                        PositionableLabel b = checkBackgrounds(dLoc);

                                        if (b != null) {
                                            showPopUp(b, event);
                                        } else {
                                            SignalMastIcon sm = checkSignalMastIcons(dLoc);

                                            if (sm != null) {
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
    }   //checkPopUp

    /**
     * Select the menu items to display for the Positionable's popup
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent) p).isVisible()) {
            return; //component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
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
                    popup.add(p.getNameString());

                    if (p.isPositionable()) {
                        setShowCoordinatesMenu(p, popup);
                    }
                    setDisplayLevelMenu(p, popup);
                    setPositionableMenu(p, popup);
                }

                boolean popupSet = false;
                popupSet = p.setRotateOrthogonalMenu(popup);
                popupSet = p.setRotateMenu(popup);

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
                setShowTooltipMenu(p, popup);

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
    }   //showPopUp()

    private long whenReleased = 0;  //used to identify event that was popup trigger
    private boolean awaitingIconChange = false;

    @Override
    public void mouseClicked(MouseEvent event) {
        if ((!event.isMetaDown()) && (!event.isPopupTrigger()) && (!event.isAltDown()) &&
            (!awaitingIconChange) && (!event.isShiftDown()) && (!event.isControlDown())) {
            calcLocation(event, 0, 0);
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                selections.get(0).doMouseClicked(event);
            }
        } else if (event.isPopupTrigger() && (whenReleased != event.getWhen())) {
            calcLocation(event, 0, 0);

            if (isEditable()) {
                selectedObject = null;
                selectedPointType = LayoutTrack.NONE;
                checkPopUp(event);
            } else {
                LocoIcon lo = checkMarkers(dLoc);

                if (lo != null) {
                    showPopUp(lo, event);
                }
            }
        }

        if (event.isControlDown() && !event.isPopupTrigger()) {
            if (checkSelect(dLoc, false)) {
                //show popup menu
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

                    case LayoutTrack.SLIP_CENTER:
                    case LayoutTrack.SLIP_LEFT:
                    case LayoutTrack.SLIP_RIGHT: {
                        amendSelectionGroup((LayoutSlip) foundObject);
                        break;
                    }

                    case LayoutTrack.TURNTABLE_CENTER: {
                        amendSelectionGroup((LayoutTurntable) foundObject);
                        break;
                    }

                    case LayoutTrack.TURNOUT_A:
                    case LayoutTrack.TURNOUT_B:
                    case LayoutTrack.TURNOUT_C:
                    case LayoutTrack.TURNOUT_D: {
                        LayoutTurnout t = (LayoutTurnout) foundObject;

                        if ((t.getVersion() == 2) && ((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                                                      (t.getTurnoutType() == LayoutTurnout.LH_XOVER) ||
                                                      (t.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
                            amendSelectionGroup((LayoutTurnout) foundObject, dLoc);
                        }

                        //$FALL-THROUGH$
                    }

                    default: {
                        break;
                    }
                }   //switch
            } else {
                PositionableLabel s = checkSensorIcons(dLoc);

                if (s != null) {
                    amendSelectionGroup(s);
                } else {
                    PositionableLabel sh = checkSignalHeadIcons(dLoc);

                    if (sh != null) {
                        amendSelectionGroup(sh);
                    } else {
                        PositionableLabel ms = checkMultiSensors(dLoc);

                        if (ms != null) {
                            amendSelectionGroup(ms);
                        } else {
                            PositionableLabel lb = checkLabelImages(dLoc);

                            if (lb != null) {
                                amendSelectionGroup(lb);
                            } else {
                                PositionableLabel b = checkBackgrounds(dLoc);

                                if (b != null) {
                                    amendSelectionGroup(b);
                                } else {
                                    PositionableLabel sm = checkSignalMastIcons(dLoc);

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
    }   //mouseClicked

    private void checkPointOfPositionable(PositionablePoint p) {
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
        Point2D loc = p.getCoords();

        if (checkSelect(loc, true, p)) {
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundObject;

                    if ((p2.getType() == PositionablePoint.ANCHOR) && p2.setTrackConnection(t)) {
                        if (t.getConnect1() == p) {
                            t.setNewConnect1(p2, LayoutTrack.POS_POINT);
                        } else {
                            t.setNewConnect2(p2, LayoutTrack.POS_POINT);
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
                case LayoutTrack.TURNOUT_D: {
                    LayoutTurnout lt = (LayoutTurnout) foundObject;
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

                case LayoutTrack.LEVEL_XING_A:
                case LayoutTrack.LEVEL_XING_B:
                case LayoutTrack.LEVEL_XING_C:
                case LayoutTrack.LEVEL_XING_D: {
                    LevelXing lx = (LevelXing) foundObject;
                    try {
                        if (lx.getConnection(foundPointType) == null) {
                            lx.setConnection(foundPointType, t, LayoutTrack.TRACK);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(lx, foundPointType);
                            } else {
                                t.setNewConnect2(lx, foundPointType);
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

                case LayoutTrack.SLIP_A:
                case LayoutTrack.SLIP_B:
                case LayoutTrack.SLIP_C:
                case LayoutTrack.SLIP_D: {
                    LayoutSlip ls = (LayoutSlip) foundObject;
                    try {
                        if (ls.getConnection(foundPointType) == null) {
                            ls.setConnection(foundPointType, t, LayoutTrack.TRACK);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(ls, foundPointType);
                            } else {
                                t.setNewConnect2(ls, foundPointType);
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

                default:

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
            }   //switch
            repaint();

            if (t.getLayoutBlock() != null) {
                auxTools.setBlockConnectivityChanged();
            }
        }
        beginObject = null;
        foundObject = null;
    }   //checkPointOfPositionable

    private void checkPointsOfTurnout(LayoutTurnout lt) {
        beginObject = lt;

        if (lt.getConnectA() == null) {
            beginPointType = LayoutTrack.TURNOUT_A;
            dLoc = lt.getCoordsA();
            checkPointsOfTurnoutSub(lt.getCoordsA());
        }

        if (lt.getConnectB() == null) {
            beginPointType = LayoutTrack.TURNOUT_B;
            dLoc = lt.getCoordsB();
            checkPointsOfTurnoutSub(lt.getCoordsB());
        }

        if (lt.getConnectC() == null) {
            beginPointType = LayoutTrack.TURNOUT_C;
            dLoc = lt.getCoordsC();
            checkPointsOfTurnoutSub(lt.getCoordsC());
        }

        if ((lt.getConnectD() == null) && ((lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                                           (lt.getTurnoutType() == LayoutTurnout.LH_XOVER) ||
                                           (lt.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
            beginPointType = LayoutTrack.TURNOUT_D;
            dLoc = lt.getCoordsD();
            checkPointsOfTurnoutSub(lt.getCoordsD());
        }
        beginObject = null;
        foundObject = null;
    }   //checkPointsOfTurnout

    private void checkPointsOfTurnoutSub(Point2D dLoc) {
        if (checkSelect(dLoc, true)) {
            switch (foundPointType) {
                case LayoutTrack.POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundObject;

                    if (((p2.getConnect1() == null) && (p2.getConnect2() != null)) ||
                        ((p2.getConnect1() != null) && (p2.getConnect2() == null))) {
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
                case LayoutTrack.TURNOUT_D: {
                    LayoutTurnout ft = (LayoutTurnout) foundObject;
                    addTrackSegment();

                    if ((ft.getTurnoutType() == LayoutTurnout.RH_TURNOUT) || (ft.getTurnoutType() == LayoutTurnout.LH_TURNOUT)) {
                        rotateTurnout(ft);
                    }
                    break;
                }

                default: {
                    log.warn("Unexpected foundPointType {}  in checkPointsOfTurnoutSub", foundPointType);
                    break;
                }
            }   //switch
        }
    }   //checkPointsOfTurnoutSub

    private void rotateTurnout(LayoutTurnout t) {
        LayoutTurnout be = (LayoutTurnout) beginObject;

        if (((beginPointType == LayoutTrack.TURNOUT_A) && ((be.getConnectB() != null) || (be.getConnectC() != null))) ||
            ((beginPointType == LayoutTrack.TURNOUT_B) && ((be.getConnectA() != null) || (be.getConnectC() != null))) ||
            ((beginPointType == LayoutTrack.TURNOUT_C) && ((be.getConnectB() != null) || (be.getConnectA() != null)))) {
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
        } else if ((foundPointType == LayoutTrack.TURNOUT_C) &&
                   ((beginPointType == LayoutTrack.TURNOUT_A) || (beginPointType == LayoutTrack.TURNOUT_B))) {
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
            } else {    //(beginPointType==TURNOUT_C){
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
            } else {    //(beginPointType==TURNOUT_C){
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
    }   //rotateTurnout

    //private ArrayList<LayoutTurnout> _turnoutSelection = null; //LayoutTurnouts
    private java.util.HashMap<LayoutTurnout, TurnoutSelection> _turnoutSelection = null;

    static class TurnoutSelection {
        boolean pointA = false;
        boolean pointB = false;
        boolean pointC = false;
        boolean pointD = false;

        TurnoutSelection() {}

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

    private ArrayList<PositionablePoint> _pointSelection = null;    //new ArrayList<PositionablePoint>();  //PositionablePoint list
    private ArrayList<LevelXing> _xingSelection = null;             //new ArrayList<LevelXing>();  //LevelXing list
    private ArrayList<LayoutSlip> _slipSelection = null;            //new ArrayList<LevelXing>();  //LayoutSlip list
    private ArrayList<LayoutTurntable> _turntableSelection = null;  //new ArrayList<LayoutTurntable>(); //Turntable list
    private ArrayList<Positionable> _positionableSelection = null;

    private void highLightSelection(Graphics2D g) {
        java.awt.Stroke stroke = g.getStroke();
        Color color = g.getColor();
        g.setColor(new Color(204, 207, 88));
        g.setStroke(new java.awt.BasicStroke(2.0f));

        if (_positionableSelection != null) {
            for (Positionable c : _positionableSelection) {
                g.drawRect(c.getX(), c.getY(), c.maxWidth(), c.maxHeight());
            }
        }

        //loop over all defined turnouts
        if (_turnoutSelection != null) {
            for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                LayoutTurnout t = entry.getKey();
                int ttype = t.getTurnoutType();

                if ((t.getVersion() == 2) && ((ttype == LayoutTurnout.DOUBLE_XOVER) ||
                                              (ttype == LayoutTurnout.LH_XOVER) ||
                                              (ttype == LayoutTurnout.RH_XOVER))) {
                    TurnoutSelection ts = entry.getValue();

                    if (ts.getPointA()) {
                        Point2D coord = t.getCoordsA();
                        g.drawRect((int) coord.getX() - 4, (int) coord.getY() - 4, 9, 9);
                    }

                    if (ts.getPointB()) {
                        Point2D coord = t.getCoordsB();
                        g.drawRect((int) coord.getX() - 4, (int) coord.getY() - 4, 9, 9);
                    }

                    if (ts.getPointC()) {
                        Point2D coord = t.getCoordsC();
                        g.drawRect((int) coord.getX() - 4, (int) coord.getY() - 4, 9, 9);
                    }

                    if (ts.getPointD()) {
                        Point2D coord = t.getCoordsD();
                        g.drawRect((int) coord.getX() - 4, (int) coord.getY() - 4, 9, 9);
                    }
                } else {
                    int minx =
                        (int) Math.min(Math.min(t.getCoordsA().getX(), t.getCoordsB().getX()),
                                       Math.min(t.getCoordsC().getX(), t.getCoordsD().getX()));
                    int miny =
                        (int) Math.min(Math.min(t.getCoordsA().getY(), t.getCoordsB().getY()),
                                       Math.min(t.getCoordsC().getY(), t.getCoordsD().getY()));
                    int maxx =
                        (int) Math.max(Math.max(t.getCoordsA().getX(), t.getCoordsB().getX()),
                                       Math.max(t.getCoordsC().getX(), t.getCoordsD().getX()));
                    int maxy =
                        (int) Math.max(Math.max(t.getCoordsA().getY(), t.getCoordsB().getY()),
                                       Math.max(t.getCoordsC().getY(), t.getCoordsD().getY()));
                    int width = maxx - minx;
                    int height = maxy - miny;
                    int x = (int) t.getCoordsCenter().getX() - (width / 2);
                    int y = (int) t.getCoordsCenter().getY() - (height / 2);
                    g.drawRect(x, y, width, height);
                }
            }
        }

        if (_xingSelection != null) {
            //loop over all defined level crossings
            for (LevelXing xing : _xingSelection) {
                int minx =
                    (int) Math.min(Math.min(xing.getCoordsA().getX(), xing.getCoordsB().getX()),
                                   Math.min(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int miny =
                    (int) Math.min(Math.min(xing.getCoordsA().getY(), xing.getCoordsB().getY()),
                                   Math.min(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int maxx =
                    (int) Math.max(Math.max(xing.getCoordsA().getX(), xing.getCoordsB().getX()),
                                   Math.max(xing.getCoordsC().getX(), xing.getCoordsD().getX()));
                int maxy =
                    (int) Math.max(Math.max(xing.getCoordsA().getY(), xing.getCoordsB().getY()),
                                   Math.max(xing.getCoordsC().getY(), xing.getCoordsD().getY()));
                int width = maxx - minx;
                int height = maxy - miny;
                int x = (int) xing.getCoordsCenter().getX() - (width / 2);
                int y = (int) xing.getCoordsCenter().getY() - (height / 2);
                g.drawRect(x, y, width, height);
            }
        }

        if (_slipSelection != null) {
            //loop over all defined slips
            for (LayoutSlip sl : _slipSelection) {
                int minx =
                    (int) Math.min(Math.min(sl.getCoordsA().getX(), sl.getCoordsB().getX()),
                                   Math.min(sl.getCoordsC().getX(), sl.getCoordsD().getX()));
                int miny =
                    (int) Math.min(Math.min(sl.getCoordsA().getY(), sl.getCoordsB().getY()),
                                   Math.min(sl.getCoordsC().getY(), sl.getCoordsD().getY()));
                int maxx =
                    (int) Math.max(Math.max(sl.getCoordsA().getX(), sl.getCoordsB().getX()),
                                   Math.max(sl.getCoordsC().getX(), sl.getCoordsD().getX()));
                int maxy =
                    (int) Math.max(Math.max(sl.getCoordsA().getY(), sl.getCoordsB().getY()),
                                   Math.max(sl.getCoordsC().getY(), sl.getCoordsD().getY()));
                int width = maxx - minx;
                int height = maxy - miny;
                int x = (int) sl.getCoordsCenter().getX() - (width / 2);
                int y = (int) sl.getCoordsCenter().getY() - (height / 2);
                g.drawRect(x, y, width, height);
            }
        }

        //loop over all defined turntables
        if (_turntableSelection != null) {
            for (LayoutTurntable tt : _turntableSelection) {
                Point2D center = tt.getCoordsCenter();
                int x = (int) center.getX() - (int) tt.getRadius();
                int y = (int) center.getY() - (int) tt.getRadius();
                g.drawRect(x, y, ((int) tt.getRadius() * 2), ((int) tt.getRadius() * 2));
            }
        }

        //loop over all defined Anchor Points and End Bumpers
        if (_pointSelection != null) {
            for (PositionablePoint p : _pointSelection) {
                Point2D coord = p.getCoords();
                g.drawRect((int) coord.getX() - 4, (int) coord.getY() - 4, 9, 9);
            }
        }
        g.setColor(color);
        g.setStroke(stroke);
    }   //highLightSelection

    private void createSelectionGroups() {
        List<Positionable> contents = getContents();
        Rectangle2D selectRect = new Rectangle2D.Double(selectionX, selectionY,
                                                        selectionWidth, selectionHeight);

        for (Positionable c : contents) {
            Point2D upperLeft = c.getLocation();

            if (selectRect.contains(upperLeft)) {
                if (_positionableSelection == null) {
                    _positionableSelection = new ArrayList<Positionable>();
                }

                if (!_positionableSelection.contains(c)) {
                    _positionableSelection.add(c);
                }
            }
        }

        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            int ttype = t.getTurnoutType();

            if ((t.getVersion() == 2) && ((ttype == LayoutTurnout.DOUBLE_XOVER) ||
                                          (ttype == LayoutTurnout.LH_XOVER) ||
                                          (ttype == LayoutTurnout.RH_XOVER))) {
                if (selectRect.contains(t.getCoordsA())) {
                    if (_turnoutSelection == null) {
                        _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
                    }
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(t)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(t, ts);
                    } else {
                        ts = _turnoutSelection.get(t);
                    }
                    ts.setPointA(true);
                }

                if (selectRect.contains(t.getCoordsB())) {
                    if (_turnoutSelection == null) {
                        _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
                    }
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(t)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(t, ts);
                    } else {
                        ts = _turnoutSelection.get(t);
                    }
                    ts.setPointB(true);
                }

                if (selectRect.contains(t.getCoordsC())) {
                    if (_turnoutSelection == null) {
                        _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
                    }
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(t)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(t, ts);
                    } else {
                        ts = _turnoutSelection.get(t);
                    }
                    ts.setPointC(true);
                }

                if (selectRect.contains(t.getCoordsD())) {
                    if (_turnoutSelection == null) {
                        _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
                    }
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(t)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(t, ts);
                    } else {
                        ts = _turnoutSelection.get(t);
                    }
                    ts.setPointD(true);
                }
            } else {
                Point2D center = t.getCoordsCenter();

                if (selectRect.contains(center)) {
                    if (_turnoutSelection == null) {
                        _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
                    }

                    if (!_turnoutSelection.containsKey(t)) {
                        _turnoutSelection.put(t, new TurnoutSelection());
                    }
                }
            }
        }

        //loop over all defined level crossings
        for (LevelXing x : xingList) {
            Point2D center = x.getCoordsCenter();

            if (selectRect.contains(center)) {
                if (_xingSelection == null) {
                    _xingSelection = new ArrayList<LevelXing>();
                }

                if (!_xingSelection.contains(x)) {
                    _xingSelection.add(x);
                }
            }
        }

        //loop over all defined slips
        for (LayoutSlip sl : slipList) {
            Point2D center = sl.getCoordsCenter();

            if (selectRect.contains(center)) {
                if (_slipSelection == null) {
                    _slipSelection = new ArrayList<LayoutSlip>();
                }

                if (!_slipSelection.contains(sl)) {
                    _slipSelection.add(sl);
                }
            }
        }

        //loop over all defined turntables
        for (LayoutTurntable x : turntableList) {
            Point2D center = x.getCoordsCenter();

            if (selectRect.contains(center)) {
                if (_turntableSelection == null) {
                    _turntableSelection = new ArrayList<LayoutTurntable>();
                }

                if (!_turntableSelection.contains(x)) {
                    _turntableSelection.add(x);
                }
            }
        }

        //loop over all defined Anchor Points and End Bumpers
        for (PositionablePoint p : pointList) {
            Point2D coord = p.getCoords();

            if (selectRect.contains(coord)) {
                if (_pointSelection == null) {
                    _pointSelection = new ArrayList<PositionablePoint>();
                }

                if (!_pointSelection.contains(p)) {
                    _pointSelection.add(p);
                }
            }
        }
        repaint();
    }   //createSelectionGroups

    private void clearSelectionGroups() {
        _pointSelection = null;
        _turntableSelection = null;
        _xingSelection = null;
        _slipSelection = null;
        _turnoutSelection = null;
        _positionableSelection = null;
    }   //clearSelectionGroups

    boolean noWarnGlobalDelete = false;

    private void deleteSelectedItems() {
        if (!noWarnGlobalDelete) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             rb.getString("Question6"), Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           rb.getString("ButtonYesPlus")},
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

            for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                removeLayoutTurnout(entry.getKey());
            }
            noWarnLayoutTurnout = oldTurnout;
        }
        selectionActive = false;
        clearSelectionGroups();
        repaint();
    }   //deleteSelectedItems

    private void amendSelectionGroup(Positionable p) {
        if (_positionableSelection == null) {
            _positionableSelection = new ArrayList<Positionable>();
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
        repaint();
    }   //amendSelectionGroup

    private void amendSelectionGroup(LayoutTurnout p, Point2D dLoc) {
        if (_turnoutSelection == null) {
            _turnoutSelection = new HashMap<LayoutTurnout, TurnoutSelection>();
        }

        boolean removed = false;

        for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
            LayoutTurnout t = entry.getKey();

            if (t == p) {
                int ttype = t.getTurnoutType();

                if ((t.getVersion() == 2) && ((ttype == LayoutTurnout.DOUBLE_XOVER) ||
                                              (ttype == LayoutTurnout.LH_XOVER) ||
                                              (ttype == LayoutTurnout.RH_XOVER))) {
                    TurnoutSelection ts = entry.getValue();
                    Rectangle2D r = controlPointRectAt(dLoc);

                    if (ts.getPointA()) {
                        if (r.contains(t.getCoordsA())) {
                            ts.setPointA(false);
                            removed = true;
                        }
                    }

                    if (ts.getPointB()) {
                        if (r.contains(t.getCoordsB())) {
                            ts.setPointB(false);
                            removed = true;
                        }
                    }

                    if (ts.getPointC()) {
                        if (r.contains(t.getCoordsC())) {
                            ts.setPointC(false);
                            removed = true;
                        }
                    }

                    if (ts.getPointD()) {
                        if (r.contains(t.getCoordsD())) {
                            ts.setPointD(false);
                            removed = true;
                        }
                    }

                    if (!ts.getPointA() && !ts.getPointB() && !ts.getPointC() && !ts.getPointD()) {
                        _turnoutSelection.remove(t);
                        removed = true;
                        break;
                    }
                } else {
                    _turnoutSelection.remove(t);
                    removed = true;
                    break;
                }
            }
        }

        if (!removed) {
            if ((p.getVersion() == 2) && ((p.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
                                          (p.getTurnoutType() == LayoutTurnout.LH_XOVER) ||
                                          (p.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
                Rectangle2D r = controlPointRectAt(dLoc);

                if (r.contains(p.getCoordsA())) {
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(p)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(p, ts);
                    } else {
                        ts = _turnoutSelection.get(p);
                    }
                    ts.setPointA(true);
                }

                if (r.contains(p.getCoordsB())) {
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(p)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(p, ts);
                    } else {
                        ts = _turnoutSelection.get(p);
                    }
                    ts.setPointB(true);
                }

                if (r.contains(p.getCoordsC())) {
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(p)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(p, ts);
                    } else {
                        ts = _turnoutSelection.get(p);
                    }
                    ts.setPointC(true);
                }

                if (r.contains(p.getCoordsD())) {
                    TurnoutSelection ts;

                    if (!_turnoutSelection.containsKey(p)) {
                        ts = new TurnoutSelection();
                        _turnoutSelection.put(p, ts);
                    } else {
                        ts = _turnoutSelection.get(p);
                    }
                    ts.setPointD(true);
                }
            } else {
                _turnoutSelection.put(p, new TurnoutSelection());
            }
        }

        if (_turnoutSelection.isEmpty()) {
            _turnoutSelection = null;
        }
        repaint();
    }   //amendSelectionGroup

    private void amendSelectionGroup(PositionablePoint p) {
        if (_pointSelection == null) {
            _pointSelection = new ArrayList<PositionablePoint>();
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
        repaint();
    }   //amendSelectionGroup

    private void amendSelectionGroup(LevelXing p) {
        if (_xingSelection == null) {
            _xingSelection = new ArrayList<LevelXing>();
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
        repaint();
    }   //amendSelectionGroup

    private void amendSelectionGroup(LayoutSlip p) {
        if (_slipSelection == null) {
            _slipSelection = new ArrayList<LayoutSlip>();
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
        repaint();
    }   //amendSelectionGroup

    private void amendSelectionGroup(LayoutTurntable p) {
        if (_turntableSelection == null) {
            _turntableSelection = new ArrayList<LayoutTurntable>();
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
        repaint();
    }   //amendSelectionGroup

    public void alignSelection(boolean alignX) {
        int sum = 0;
        int cnt = 0;

        if (_positionableSelection != null) {
            for (Positionable comp : _positionableSelection) {
                if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                    continue;
                }

                if (alignX) {
                    sum += comp.getX();
                } else {
                    sum += comp.getY();
                }
                cnt++;
            }
        }

        if (_pointSelection != null) {
            for (PositionablePoint comp : _pointSelection) {
                if (alignX) {
                    sum += comp.getCoords().getX();
                } else {
                    sum += comp.getCoords().getY();
                }
                cnt++;
            }
        }

        if (_turnoutSelection != null) {
            for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                LayoutTurnout comp = entry.getKey();

                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }

        if (_xingSelection != null) {
            for (LevelXing comp : _xingSelection) {
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }

        if (_slipSelection != null) {
            for (LayoutSlip comp : _slipSelection) {
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }

        if (_turntableSelection != null) {
            for (LayoutTurntable comp : _turntableSelection) {
                if (alignX) {
                    sum += comp.getCoordsCenter().getX();
                } else {
                    sum += comp.getCoordsCenter().getY();
                }
                cnt++;
            }
        }

        int ave = Math.round((float) sum / cnt);

        if (_positionableSelection != null) {
            for (Positionable comp : _positionableSelection) {
                if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                    continue;
                }

                if (alignX) {
                    comp.setLocation(ave, comp.getY());
                } else {
                    comp.setLocation(comp.getX(), ave);
                }
            }
        }

        if (_pointSelection != null) {
            for (PositionablePoint comp : _pointSelection) {
                if (alignX) {
                    comp.setCoords(new Point2D.Double(ave, comp.getCoords().getY()));
                } else {
                    comp.setCoords(new Point2D.Double(comp.getCoords().getX(), ave));
                }
            }
        }

        if (_turnoutSelection != null) {
            for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                LayoutTurnout comp = entry.getKey();

                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }

        if (_xingSelection != null) {
            for (LevelXing comp : _xingSelection) {
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }

        if (_slipSelection != null) {
            for (LayoutSlip comp : _slipSelection) {
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }

        if (_turntableSelection != null) {
            for (LayoutTurntable comp : _turntableSelection) {
                if (alignX) {
                    comp.setCoordsCenter(new Point2D.Double(ave, comp.getCoordsCenter().getY()));
                } else {
                    comp.setCoordsCenter(new Point2D.Double(comp.getCoordsCenter().getX(), ave));
                }
            }
        }
        repaint();
    }   //alignSelection

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
    }   //showAlignPopup

    /**
     * Offer actions to align the selected Positionable items either
     * Horizontally (at avearage y coord) or Vertically (at avearage x coord).
     */
    public boolean setShowAlignmentMenu(JPopupMenu popup) {
        if (showAlignPopup()) {
            JMenu edit = new JMenu(rb.getString("EditAlignment"));
            edit.add(new AbstractAction(rb.getString("AlignX")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alignSelection(true);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignY")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alignSelection(false);
                }
            });
            popup.add(edit);

            return true;
        }
        return false;
    }   //setShowAlignmentMenu

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedItems();

            return;
        }

        if (_positionableSelection != null) {
            for (Positionable c : _positionableSelection) {
                int xNew;
                int yNew;

                if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth() == 0)) {
                    MemoryIcon pm = (MemoryIcon) c;
                    xNew = (int) (returnNewXPostition(e, pm.getOriginalX()));
                    yNew = (int) (returnNewYPostition(e, pm.getOriginalY()));
                } else {
                    Point2D upperLeft = c.getLocation();
                    xNew = (int) (returnNewXPostition(e, upperLeft.getX()));
                    yNew = (int) (returnNewYPostition(e, upperLeft.getY()));
                }
                c.setLocation(xNew, yNew);
            }
        }

        //loop over all defined turnouts
        if (_turnoutSelection != null) {
            for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                LayoutTurnout t = entry.getKey();
                int ttype = t.getTurnoutType();

                if ((t.getVersion() == 2) && ((ttype == LayoutTurnout.DOUBLE_XOVER) ||
                                              (ttype == LayoutTurnout.LH_XOVER) ||
                                              (ttype == LayoutTurnout.RH_XOVER))) {
                    TurnoutSelection ts = entry.getValue();

                    if (ts.getPointA()) {
                        Point2D coord = t.getCoordsA();
                        t.setCoordsA(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                                        returnNewYPostition(e, coord.getY())));
                    }

                    if (ts.getPointB()) {
                        Point2D coord = t.getCoordsB();
                        t.setCoordsB(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                                        returnNewYPostition(e, coord.getY())));
                    }

                    if (ts.getPointC()) {
                        Point2D coord = t.getCoordsC();
                        t.setCoordsC(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                                        returnNewYPostition(e, coord.getY())));
                    }

                    if (ts.getPointD()) {
                        Point2D coord = t.getCoordsD();
                        t.setCoordsD(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                                        returnNewYPostition(e, coord.getY())));
                    }
                } else {
                    Point2D center = t.getCoordsCenter();
                    t.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                         returnNewYPostition(e, center.getY())));
                }
            }
        }

        if (_xingSelection != null) {
            //loop over all defined level crossings
            for (LevelXing x : _xingSelection) {
                Point2D center = x.getCoordsCenter();
                x.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                     returnNewYPostition(e, center.getY())));
            }
        }

        if (_slipSelection != null) {
            //loop over all defined slips
            for (LayoutSlip sl : _slipSelection) {
                Point2D center = sl.getCoordsCenter();
                sl.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                      returnNewYPostition(e, center.getY())));
            }
        }

        //loop over all defined turntables
        if (_turntableSelection != null) {
            for (LayoutTurntable x : _turntableSelection) {
                Point2D center = x.getCoordsCenter();
                x.setCoordsCenter(new Point2D.Double(returnNewXPostition(e, center.getX()),
                                                     returnNewYPostition(e, center.getY())));
            }
        }

        //loop over all defined Anchor Points and End Bumpers
        if (_pointSelection != null) {
            for (PositionablePoint p : _pointSelection) {
                Point2D coord = p.getCoords();
                p.setCoords(new Point2D.Double(returnNewXPostition(e, coord.getX()),
                                               returnNewYPostition(e, coord.getY())));
            }
        }
        repaint();
    }   //keyPressed

    private double returnNewXPostition(KeyEvent e, double val) {
        if (e.isShiftDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: {
                    val = val - 1;
                    break;
                }

                case KeyEvent.VK_RIGHT: {
                    val = val + 1;
                    break;
                }

                default: {
                    break;
                }
            }   //switch
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: {
                    val = val - 5;
                    break;
                }

                case KeyEvent.VK_RIGHT: {
                    val = val + 5;
                    break;
                }

                default: {
                    break;
                }
            }   //switch
        }

        if (val < 0) {
            val = 0;
        }
        return val;
    }   //returnNewXPostition

    private double returnNewYPostition(KeyEvent e, double val) {
        if (e.isShiftDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP: {
                    val = val - 1;
                    break;
                }

                case KeyEvent.VK_DOWN: {
                    val = val + 1;
                    break;
                }

                default: {
                    log.warn("Unexpected key code {}  in returnNewYPosition", e.getKeyCode());
                    break;
                }
            }   //switch
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP: {
                    val = val - 5;
                    break;
                }

                case KeyEvent.VK_DOWN: {
                    val = val + 5;
                    break;
                }

                default: {
                    log.warn("Unexpected key code {}  in returnNewYPosition", e.getKeyCode());
                    break;
                }
            }   //switch
        }

        if (val < 0) {
            val = 0;
        }
        return val;
    }   //returnNewYPostition

    int _prevNumSel = 0;

    @Override
    public void mouseMoved(MouseEvent event) {
        calcLocation(event, 0, 0);

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

        if ((selection != null) && (selection.getDisplayLevel() > BKG) && selection.showTooltip()) {
            showToolTip(selection, event);
        } else {
            super.setToolTip(null);
        }

        if (numSel != _prevNumSel) {
            repaint();
            _prevNumSel = numSel;
        }
    }   //mouseMoved

    private boolean isDragging = false;

    @Override
    public void mouseDragged(MouseEvent event) {
        //initialize mouse position
        calcLocation(event, 0, 0);

        //ignore this event if still at the original point
        if ((!isDragging) && (xLoc == getAnchorX()) && (yLoc == getAnchorY())) {
            return;
        }

        //process this mouse dragged event
        if (isEditable()) {
            xLabel.setText(Integer.toString(xLoc));
            yLabel.setText(Integer.toString(yLoc));
        }
        Point2D newPos = new Point2D.Double(dLoc.getX() + startDel.getX(),
                                            dLoc.getY() + startDel.getY());

        if ((selectedObject != null) && (event.isMetaDown() || event.isAltDown()) && (selectedPointType == LayoutTrack.MARKER)) {
            //marker moves regardless of editMode or positionable
            PositionableLabel pl = (PositionableLabel) selectedObject;
            int xint = (int) newPos.getX();
            int yint = (int) newPos.getY();

            //don't allow negative placement, object could become unreachable
            if (xint < 0) {
                xint = 0;
            }

            if (yint < 0) {
                yint = 0;
            }
            pl.setLocation(xint, yint);
            isDragging = true;
            repaint();

            return;
        }

        if (isEditable()) {
            if ((selectedObject != null) && (event.isMetaDown() || event.isAltDown()) && allPositionable()) {
                //moving a point
                if (snapToGridOnMove) {
                    int xx = (((int) newPos.getX() + (gridSize / 2)) / gridSize) * gridSize;
                    int yy = (((int) newPos.getY() + (gridSize / 2)) / gridSize) * gridSize;
                    newPos.setLocation(xx, yy);
                }

                if ((_pointSelection != null) || (_turntableSelection != null) || (_xingSelection != null) ||
                    (_turnoutSelection != null) || (_positionableSelection != null)) {
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

                            if (xNew < 0) {
                                xNew = 0;
                            }

                            if (yNew < 0) {
                                yNew = 0;
                            }
                            c.setLocation(xNew, yNew);
                        }
                    }

                    if (_turnoutSelection != null) {
                        for (Map.Entry<LayoutTurnout, TurnoutSelection> entry : _turnoutSelection.entrySet()) {
                            LayoutTurnout t = entry.getKey();
                            int ttype = t.getTurnoutType();

                            if ((t.getVersion() == 2) && ((ttype == LayoutTurnout.DOUBLE_XOVER) ||
                                                          (ttype == LayoutTurnout.LH_XOVER) ||
                                                          (ttype == LayoutTurnout.RH_XOVER))) {
                                TurnoutSelection ts = entry.getValue();

                                if (ts.getPointA()) {
                                    Point2D coord = t.getCoordsA();
                                    xNew = (int) coord.getX() + offsetx;
                                    yNew = (int) coord.getY() + offsety;

                                    if (xNew < 0) {
                                        xNew = 0;
                                    }

                                    if (yNew < 0) {
                                        yNew = 0;
                                    }
                                    t.setCoordsA(new Point2D.Double(xNew, yNew));
                                }

                                if (ts.getPointB()) {
                                    Point2D coord = t.getCoordsB();
                                    xNew = (int) coord.getX() + offsetx;
                                    yNew = (int) coord.getY() + offsety;

                                    if (xNew < 0) {
                                        xNew = 0;
                                    }

                                    if (yNew < 0) {
                                        yNew = 0;
                                    }
                                    t.setCoordsB(new Point2D.Double(xNew, yNew));
                                }

                                if (ts.getPointC()) {
                                    Point2D coord = t.getCoordsC();
                                    xNew = (int) coord.getX() + offsetx;
                                    yNew = (int) coord.getY() + offsety;

                                    if (xNew < 0) {
                                        xNew = 0;
                                    }

                                    if (yNew < 0) {
                                        yNew = 0;
                                    }
                                    t.setCoordsC(new Point2D.Double(xNew, yNew));
                                }

                                if (ts.getPointD()) {
                                    Point2D coord = t.getCoordsD();
                                    xNew = (int) coord.getX() + offsetx;
                                    yNew = (int) coord.getY() + offsety;

                                    if (xNew < 0) {
                                        xNew = 0;
                                    }

                                    if (yNew < 0) {
                                        yNew = 0;
                                    }
                                    t.setCoordsD(new Point2D.Double(xNew, yNew));
                                }
                            } else {
                                Point2D center = t.getCoordsCenter();
                                xNew = (int) center.getX() + offsetx;
                                yNew = (int) center.getY() + offsety;

                                if (xNew < 0) {
                                    xNew = 0;
                                }

                                if (yNew < 0) {
                                    yNew = 0;
                                }
                                t.setCoordsCenter(new Point2D.Double(xNew, yNew));
                            }
                        }
                    }

                    if (_xingSelection != null) {
                        //loop over all defined level crossings
                        for (LevelXing x : _xingSelection) {
                            Point2D center = x.getCoordsCenter();
                            xNew = (int) center.getX() + offsetx;
                            yNew = (int) center.getY() + offsety;

                            if (xNew < 0) {
                                xNew = 0;
                            }

                            if (yNew < 0) {
                                yNew = 0;
                            }
                            x.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }

                    if (_slipSelection != null) {
                        //loop over all defined slips
                        for (LayoutSlip sl : _slipSelection) {
                            Point2D center = sl.getCoordsCenter();
                            xNew = (int) center.getX() + offsetx;
                            yNew = (int) center.getY() + offsety;

                            if (xNew < 0) {
                                xNew = 0;
                            }

                            if (yNew < 0) {
                                yNew = 0;
                            }
                            sl.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }

                    //loop over all defined turntables
                    if (_turntableSelection != null) {
                        for (LayoutTurntable x : _turntableSelection) {
                            Point2D center = x.getCoordsCenter();
                            xNew = (int) center.getX() + offsetx;
                            yNew = (int) center.getY() + offsety;

                            if (xNew < 0) {
                                xNew = 0;
                            }

                            if (yNew < 0) {
                                yNew = 0;
                            }
                            x.setCoordsCenter(new Point2D.Double(xNew, yNew));
                        }
                    }

                    //loop over all defined Anchor Points and End Bumpers
                    if (_pointSelection != null) {
                        for (PositionablePoint p : _pointSelection) {
                            Point2D coord = p.getCoords();
                            xNew = (int) coord.getX() + offsetx;
                            yNew = (int) coord.getY() + offsety;

                            if (xNew < 0) {
                                xNew = 0;
                            }

                            if (yNew < 0) {
                                yNew = 0;
                            }
                            p.setCoords(new Point2D.Double(xNew, yNew));
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
                            ((PositionablePoint) selectedObject).setCoords(newPos);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.TURNOUT_CENTER: {
                            ((LayoutTurnout) selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.TURNOUT_A: {
                            o = (LayoutTurnout) selectedObject;
                            o.setCoordsA(newPos);
                            break;
                        }

                        case LayoutTrack.TURNOUT_B: {
                            o = (LayoutTurnout) selectedObject;
                            o.setCoordsB(newPos);
                            break;
                        }

                        case LayoutTrack.TURNOUT_C: {
                            o = (LayoutTurnout) selectedObject;
                            o.setCoordsC(newPos);
                            break;
                        }

                        case LayoutTrack.TURNOUT_D: {
                            o = (LayoutTurnout) selectedObject;
                            o.setCoordsD(newPos);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_CENTER: {
                            ((LevelXing) selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_A: {
                            x = (LevelXing) selectedObject;
                            x.setCoordsA(newPos);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_B: {
                            x = (LevelXing) selectedObject;
                            x.setCoordsB(newPos);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_C: {
                            x = (LevelXing) selectedObject;
                            x.setCoordsC(newPos);
                            break;
                        }

                        case LayoutTrack.LEVEL_XING_D: {
                            x = (LevelXing) selectedObject;
                            x.setCoordsD(newPos);
                            break;
                        }

                        case LayoutTrack.SLIP_CENTER:
                        case LayoutTrack.SLIP_LEFT:
                        case LayoutTrack.SLIP_RIGHT: {
                            ((LayoutSlip) selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.SLIP_A: {
                            sl = (LayoutSlip) selectedObject;
                            sl.setCoordsA(newPos);
                            break;
                        }

                        case LayoutTrack.SLIP_B: {
                            sl = (LayoutSlip) selectedObject;
                            sl.setCoordsB(newPos);
                            break;
                        }

                        case LayoutTrack.SLIP_C: {
                            sl = (LayoutSlip) selectedObject;
                            sl.setCoordsC(newPos);
                            break;
                        }

                        case LayoutTrack.SLIP_D: {
                            sl = (LayoutSlip) selectedObject;
                            sl.setCoordsD(newPos);
                            break;
                        }

                        case LayoutTrack.TURNTABLE_CENTER: {
                            ((LayoutTurntable) selectedObject).setCoordsCenter(newPos);
                            isDragging = true;
                            break;
                        }

                        case LayoutTrack.LAYOUT_POS_LABEL: {
                            PositionableLabel l = (PositionableLabel) selectedObject;

                            if (l.isPositionable()) {
                                int xint = (int) newPos.getX();
                                int yint = (int) newPos.getY();

                                //don't allow negative placement, object could become unreachable
                                if (xint < 0) {
                                    xint = 0;
                                }

                                if (yint < 0) {
                                    yint = 0;
                                }
                                l.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.LAYOUT_POS_JCOMP: {
                            PositionableJComponent c = (PositionableJComponent) selectedObject;

                            if (c.isPositionable()) {
                                int xint = (int) newPos.getX();
                                int yint = (int) newPos.getY();

                                //don't allow negative placement, object could become unreachable
                                if (xint < 0) {
                                    xint = 0;
                                }

                                if (yint < 0) {
                                    yint = 0;
                                }
                                c.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.MULTI_SENSOR: {
                            PositionableLabel pl = (PositionableLabel) selectedObject;

                            if (pl.isPositionable()) {
                                int xint = (int) newPos.getX();
                                int yint = (int) newPos.getY();

                                //don't allow negative placement, object could become unreachable
                                if (xint < 0) {
                                    xint = 0;
                                }

                                if (yint < 0) {
                                    yint = 0;
                                }
                                pl.setLocation(xint, yint);
                                isDragging = true;
                            }
                            break;
                        }

                        case LayoutTrack.TRACK_CIRCLE_CENTRE: {
                            TrackSegment t = (TrackSegment) selectedObject;
                            t.reCalculateTrackSegmentAngle(newPos.getX(), newPos.getY());
                            break;
                        }

                        default:

                            if (selectedPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                                LayoutTurntable turn = (LayoutTurntable) selectedObject;
                                turn.setRayCoordsIndexed(newPos.getX(), newPos.getY(),
                                                         selectedPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                            }
                    }   //switch
                }
                repaint();
            } else if ((beginObject != null) && event.isShiftDown() &&
                       trackButton.isSelected()) {
                //dragging from first end of Track Segment
                currentLocation.setLocation(xLoc, yLoc);
                boolean needResetCursor = (foundObject != null);

                if (checkSelect(currentLocation, true)) {
                    //have match to free connection point, change cursor
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if (needResetCursor) {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            } else if (selectionActive && (!event.isShiftDown()) && (!event.isAltDown()) && (!event.isMetaDown())) {
                selectionWidth = xLoc - selectionX;
                selectionHeight = yLoc - selectionY;
                repaint();
            }
        } else {
            Rectangle r = new Rectangle(event.getX(), event.getY(), 1, 1);
            ((JComponent) event.getSource()).scrollRectToVisible(r);
        }
    }   //mouseDragged

    @SuppressWarnings("unused")
    private void updateLocation(Object o, int pointType, Point2D newPos) {
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

                if (pointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                    LayoutTurntable turn = (LayoutTurntable) o;
                    turn.setRayCoordsIndexed(newPos.getX(), newPos.getY(),
                                             pointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
        }   //switch
        setDirty(true);
    }   //updateLocation

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
    }   //setLoc

    /**
     * Add an Anchor point.
     */
    public void addAnchor() {
        addAnchor(currentPoint);
    }

    private PositionablePoint addAnchor(Point2D p) {
        numAnchors++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "A" + numAnchors;

            if (finder.findPositionablePointByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numAnchors++;
            }
        }

        //create object
        PositionablePoint o = new PositionablePoint(name,
                                                    PositionablePoint.ANCHOR, p, this);

        //if (o!=null) {
        pointList.add(o);
        setDirty(true);

        //}
        return o;
    }   //addAnchor

    /**
     * Add an End Bumper point.
     */
    public void addEndBumper() {
        numEndBumpers++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "EB" + numEndBumpers;

            if (finder.findPositionablePointByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numEndBumpers++;
            }
        }

        //create object
        PositionablePoint o = new PositionablePoint(name,
                                                    PositionablePoint.END_BUMPER, currentPoint, this);

        //if (o!=null) {
        pointList.add(o);
        setDirty(true);

        //}
    }   //addEndBumper

    /**
     * Add an Edge Connector point.
     */
    public void addEdgeConnector() {
        numEdgeConnectors++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "EC" + numEdgeConnectors;

            if (finder.findPositionablePointByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numEdgeConnectors++;
            }
        }

        //create object
        PositionablePoint o = new PositionablePoint(name,
                                                    PositionablePoint.EDGE_CONNECTOR, currentPoint, this);

        //if (o!=null) {
        pointList.add(o);
        setDirty(true);

        //}
    }   //addEdgeConnector

    /**
     * Add a Track Segment
     */
    public void addTrackSegment() {
        numTrackSegments++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "T" + numTrackSegments;

            if (finder.findTrackSegmentByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numTrackSegments++;
            }
        }

        //create object
        newTrack = new TrackSegment(name, beginObject, beginPointType,
                                    foundObject, foundPointType, dashedLine.isSelected(),
                                    mainlineTrack.isSelected(), this);

        trackList.add(newTrack);
        setDirty(true);

        //link to connected objects
        setLink(newTrack,   LayoutTrack.TRACK,  beginObject,    beginPointType);
        setLink(newTrack,   LayoutTrack.TRACK,  foundObject,    foundPointType);

        //check on layout block
        String newName = getUserNameForComboBox(blockIDComboBox);
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            newTrack.setLayoutBlock(b);
            auxTools.setBlockConnectivityChanged();

            //check on occupancy sensor
            String sensorName = getUserNameForComboBox(blockSensorComboBox);

            if (sensorName.length() > 0) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.getEditor().setItem(b.getOccupancySensorName());
                }
            }
            newTrack.updateBlockInfo();
        }
    }   //addTrackSegment

    /**
     * Add a Level Crossing
     */
    public void addLevelXing() {
        numLevelXings++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "X" + numLevelXings;

            if (finder.findLevelXingByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numLevelXings++;
            }
        }

        //create object
        LevelXing o = new LevelXing(name, currentPoint, this);

        //if (o!=null) {
        xingList.add(o);
        setDirty(true);

        //check on layout block
        String newName = getUserNameForComboBox(blockIDComboBox);
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlockAC(b);
            o.setLayoutBlockBD(b);

            //check on occupancy sensor
            String sensorName = getUserNameForComboBox(blockSensorComboBox);

            if (sensorName.length() > 0) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.getEditor().setItem(b.getOccupancySensorName());
                }
            }
        }

        //}
    }   //addLevelXing

    /**
     * Add a LayoutSlip
     */
    public void addLayoutSlip(int type) {
        //get the rotation entry
        double rot = 0.0;
        String s = rotationComboBox.getEditor().getItem().toString();
        s = (null != s) ? s.trim() : "";

        if (s.length() < 1) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, rb.getString("Error3") + " " +
                                              e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }
        numLayoutSlips++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "SL" + numLayoutSlips;

            if (finder.findLayoutSlipByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numLayoutSlips++;
            }
        }

        //create object
        LayoutSlip o = new LayoutSlip(name, currentPoint, rot, this, type);
        slipList.add(o);
        setDirty(true);

        //check on layout block
        String newName = getUserNameForComboBox(blockIDComboBox);
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlock(b);

            //check on occupancy sensor
            String sensorName = getUserNameForComboBox(blockSensorComboBox);

            if (sensorName.length() > 0) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.getEditor().setItem(b.getOccupancySensorName());
                }
            }
        }

        String turnoutName = getUserNameForComboBox(turnoutNameComboBox);

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
                turnoutNameComboBox.getEditor().setItem(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnout("");
            turnoutNameComboBox.getEditor().setItem("");
            turnoutNameComboBox.setSelectedIndex(-1);
        }
        turnoutName = getUserNameForComboBox(extraTurnoutNameComboBox);

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnoutB(turnoutName);

            if (o.getTurnoutB().getSystemName().equals(turnoutName.toUpperCase())) {
                extraTurnoutNameComboBox.getEditor().setItem(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnoutB("");
            extraTurnoutNameComboBox.getEditor().setItem("");
            extraTurnoutNameComboBox.setSelectedIndex(-1);
        }
    }   //addLayoutSlip

    /**
     * Add a Layout Turnout
     */
    public void addLayoutTurnout(int type) {
        //get the rotation entry
        double rot = 0.0;
        String s = rotationComboBox.getEditor().getItem().toString();
        s = (null != s) ? s.trim() : "";

        if (s.length() < 1) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, rb.getString("Error3") + " " +
                                              e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }
        numLayoutTurnouts++;

        //get unique name
        String name = "";
        boolean duplicate = true;

        while (duplicate) {
            name = "TO" + numLayoutTurnouts;

            if (finder.findLayoutTurnoutByName(name) == null) {
                duplicate = false;
            }

            if (duplicate) {
                numLayoutTurnouts++;
            }
        }

        //create object
        LayoutTurnout o = new LayoutTurnout(name, type, currentPoint, rot, xScale, yScale, this);
        turnoutList.add(o);
        setDirty(true);

        //check on layout block
        String newName = getUserNameForComboBox(blockIDComboBox);
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlock(b);

            //check on occupancy sensor
            String sensorName = getUserNameForComboBox(blockSensorComboBox);

            if (sensorName.length() > 0) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    blockSensorComboBox.getEditor().setItem(b.getOccupancySensorName());
                }
            }
        }

        //set default continuing route Turnout State
        o.setContinuingSense(Turnout.CLOSED);

        //check on a physical turnout
        String turnoutName = getUserNameForComboBox(turnoutNameComboBox);

        if (validatePhysicalTurnout(turnoutName, this)) {
            //turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName.toUpperCase())) {
                turnoutNameComboBox.getEditor().setItem(turnoutName.toUpperCase());
            }
        } else {
            o.setTurnout("");
            turnoutNameComboBox.getEditor().setItem("");
            turnoutNameComboBox.setSelectedIndex(-1);
        }

        //}
    }   //addLayoutTurnout

    /**
     * Validates that a physical turnout exists and is unique among Layout
     * Turnouts Returns true if valid turnout was entered, false otherwise
     */
    public boolean validatePhysicalTurnout(String turnoutName, Component openPane) {
        //check if turnout name was entered
        if (turnoutName.length() < 1) {
            //no turnout entered
            return false;
        }

        //ensure that this turnout is unique among Layout Turnouts
        for (LayoutTurnout t : turnoutList) {
            log.debug("LT '" + t.getName() + "', Turnout tested '" + t.getTurnoutName() + "' ");
            Turnout to = t.getTurnout();

            if (to != null) {
                String uname = to.getUserName();

                if ((to.getSystemName().equals(turnoutName.toUpperCase())) ||
                    ((uname != null) && (uname.equals(turnoutName)))) {
                    JOptionPane.showMessageDialog(openPane,
                                                  java.text.MessageFormat.format(rb.getString("Error4"),
                                                                                 new Object[] {turnoutName}),
                                                  Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                    return false;
                }
            }

            /*Only check for the second turnout if the type is a double cross over
               otherwise the second turnout is used to throw an additional turnout at
               the same time.*/
            if (t.getTurnoutType() >= LayoutTurnout.DOUBLE_XOVER) {
                Turnout to2 = t.getSecondTurnout();

                if (to2 != null) {
                    String uname = to2.getUserName();

                    if ((to2.getSystemName().equals(turnoutName.toUpperCase())) ||
                        ((uname != null) && (uname.equals(turnoutName)))) {
                        JOptionPane.showMessageDialog(openPane,
                                                      java.text.MessageFormat.format(rb.getString("Error4"),
                                                                                     new Object[] {turnoutName}),
                                                      Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                        return false;
                    }
                }
            }
        }

        for (LayoutSlip sl : slipList) {
            Turnout to = sl.getTurnout();

            if (to != null) {
                String uname = to.getUserName();

                if (to.getSystemName().equals(turnoutName) || ((uname != null) && uname.equals(turnoutName))) {
                    JOptionPane.showMessageDialog(openPane,
                                                  java.text.MessageFormat.format(rb.getString("Error4"),
                                                                                 new Object[] {turnoutName}),
                                                  Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                    return false;
                }
            }
            to = sl.getTurnoutB();

            if (to != null) {
                String uname = to.getUserName();

                if (to.getSystemName().equals(turnoutName) || ((uname != null) && uname.equals(turnoutName))) {
                    JOptionPane.showMessageDialog(openPane,
                                                  java.text.MessageFormat.format(rb.getString("Error4"),
                                                                                 new Object[] {turnoutName}),
                                                  Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                    return false;
                }
            }
        }

        //check that the unique turnout name corresponds to a defined physical turnout
        Turnout to = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);

        if (to == null) {
            //There is no turnout corresponding to this name
            JOptionPane.showMessageDialog(openPane,
                                          java.text.MessageFormat.format(rb.getString("Error8"),
                                                                         new Object[] {turnoutName}),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return false;
        }
        return true;
    }   //validatePhysicalTurnout

    /**
     * Adds a link in the 'to' object to the 'from' object
     */
    private void setLink(Object fromObject, int fromPointType, Object toObject, int toPointType) {
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

            default:

                if ((toPointType >= LayoutTrack.TURNTABLE_RAY_OFFSET) && (fromPointType == LayoutTrack.TRACK)) {
                    ((LayoutTurntable) toObject).setRayConnect((TrackSegment) fromObject,
                                                               toPointType - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
        }   //switch
    }       //setLink

    /**
     * Return a layout block with the entered name, creating a new one if
     * needed. Note that the entered name becomes the user name of the
     * LayoutBlock, and a system name is automatically created by
     * LayoutBlockManager if needed.
     */
    public LayoutBlock provideLayoutBlock(String inBlockName) {
        //log.info("provideLayoutBlock :: '{}'", inBlockName);
        LayoutBlock result = null, newBlk = null;   //assume failure (pessimist!)

        if (inBlockName.length() < 1) {
            //nothing entered, try autoAssign
            if (autoAssignBlocks) {
                newBlk = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock();

                if (null == newBlk) {
                    log.error("Failure to auto-assign LayoutBlock '{}'.", inBlockName);
                }
            }
        } else {
            //check if this Layout Block already exists
            result = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(inBlockName);

            if (null == result) {   //(no)
                newBlk = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, inBlockName);

                if (null == newBlk) {
                    log.error("Failure to create new LayoutBlock '{}'.", inBlockName);
                }
            }
        }

        //if we created a new block
        if (newBlk != null) {
            //initialize the new block
            //log.info("provideLayoutBlock :: Init new block {}", inBlockName);
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
            setDirty(true);
        }
        return result;
    }   //provideLayoutBlock

    /**
     * Validates that the supplied occupancy sensor name corresponds to an
     * existing sensor and is unique among all blocks. If valid, returns true
     * and sets the block sensor name in the block. Else returns false, and does
     * nothing to the block.
     */
    public boolean validateSensor(String sensorName, LayoutBlock blk, Component openFrame) {
        boolean result = false; //assume failure (pessimist!)

        //check if anything entered
        if (sensorName.length() > 0) {
            //get a validated sensor corresponding to this name and assigned to block
            Sensor s = blk.validateSensor(sensorName, openFrame);
            result = (null != s);   //if sensor returned result is true.
        }
        return result;
    }   //validateSensor

    /**
     * Return a layout block with the given name if one exists. Registers this
     * LayoutEditor with the layout block. This method is designed to be used
     * when a panel is loaded. The calling method must handle whether the use
     * count should be incremented.
     */
    public LayoutBlock getLayoutBlock(String blockID) {
        //check if this Layout Block already exists
        LayoutBlock blk = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(blockID);

        if (blk == null) {
            log.error("LayoutBlock '" + blockID + "' not found when panel loaded");

            return null;
        }
        blk.addLayoutEditor(this);

        return blk;
    }   //getLayoutBlock

    /**
     * Remove object from all Layout Editor temmporary lists of items not part
     * of track schematic
     */
    protected boolean remove(Object s) {
        boolean found = false;

        for (int i = 0; i < sensorImage.size(); i++) {
            if (s == sensorImage.get(i)) {
                sensorImage.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < sensorList.size(); i++) {
            if (s == sensorList.get(i)) {
                sensorList.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < backgroundImage.size(); i++) {
            if (s == backgroundImage.get(i)) {
                backgroundImage.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < memoryLabelList.size(); i++) {
            if (s == memoryLabelList.get(i)) {
                memoryLabelList.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < blockContentsLabelList.size(); i++) {
            if (s == blockContentsLabelList.get(i)) {
                blockContentsLabelList.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < signalList.size(); i++) {
            if (s == signalList.get(i)) {
                signalList.remove(i);
                found = true;
                break;
            }
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

        for (int i = 0; i < multiSensors.size(); i++) {
            if (s == multiSensors.get(i)) {
                multiSensors.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < clocks.size(); i++) {
            if (s == clocks.get(i)) {
                clocks.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < signalHeadImage.size(); i++) {
            if (s == signalHeadImage.get(i)) {
                signalHeadImage.remove(i);
                found = true;
                break;
            }
        }

        for (int i = 0; i < labelImage.size(); i++) {
            if (s == labelImage.get(i)) {
                labelImage.remove(i);
                found = true;
                break;
            }
        }
        super.removeFromContents((Positionable) s);

        if (found) {
            setDirty(true);
            repaint();
        }
        return found;
    }   //remove

    @Override
    public boolean removeFromContents(Positionable l) {
        return remove(l);
    }

    private String findBeanUsage(NamedBean sm) {
        PositionablePoint pe;
        PositionablePoint pw;
        LayoutTurnout lt;
        LevelXing lx;
        LayoutSlip ls;
        boolean found = false;
        StringBuilder sb = new StringBuilder();

        sb.append("This ");

        if (sm instanceof SignalMast) {
            sb. append("Signal Mast");  //TODO I18N using Bundle.getMessage("BeanNameSignalMast");
            sb. append(" is linked to the following items<br> do you want to remove those references");

            if (InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed((SignalMast) sm)) {
                jmri.SignalMastLogic sml =
                    InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic((SignalMast) sm);

                //jmri.SignalMastLogic sml =
                //InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic((SignalMast)sm);
                if ((sml != null) && sml.useLayoutEditor(sml.getDestinationList().get(0))) {
                    sb.append(" and any SignalMast Logic associated with it");
                }
            }
        } else if (sm instanceof Sensor) {
            sb. append("Sensor");   //TODO I18N using Bundle.getMessage("BeanNameSensor");
            sb. append(" is linked to the following items<br> do you want to remove those references");
        } else if (sm instanceof SignalHead) {
            sb. append("SignalHead");   //TODO I18N using Bundle.getMessage("BeanNameSignalHead");
            sb. append(" is linked to the following items<br> do you want to remove those references");
        }

        if ((pw = finder.findPositionablePointByWestBoundBean(sm)) != null) {
            sb.append("<br>Point of ");
            TrackSegment t = pw.getConnect1();

            if (t != null) {
                sb.append(t.getBlockName() + " and ");
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
                sb.append(t.getBlockName() + " and ");
            }
            t = pe.getConnect2();

            if (t != null) {
                sb.append(t.getBlockName());
            }
            found = true;
        }

        if ((lt = finder.findLayoutTurnoutByBean(sm)) != null) {
            sb.append("<br>Turnout " + lt.getTurnoutName());    //I18N using Bundle.getMessage("BeanNameTurnout");
            found = true;
        }

        if ((lx = finder.findLevelXingByBean(sm)) != null) {
            sb.append("<br>Level Crossing " + lx.getID());
            found = true;
        }

        if ((ls = finder.findLayoutSlipByBean(sm)) != null) {
            sb.append("<br>Slip " + ls.getTurnoutName());
            found = true;
        }

        if (!found) {
            return null;
        }
        return sb.toString();
    }   //findBeanUsage

    private boolean removeSignalMast(SignalMastIcon si) {
        SignalMast sm = si.getSignalMast();
        String usage = findBeanUsage(sm);

        if (usage != null) {
            usage = "<html>" + usage + "</html>";
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             usage, Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           Bundle.getMessage("ButtonCancel")},
                                                             Bundle.getMessage("ButtonYes"));

            if (selectedValue == 1) {
                return true;    //return leaving the references in place but allow the icon to be deleted.
            }

            if (selectedValue == 2) {
                return false;   //do not delete the item
            }
            removeBeanRefs(sm);
        }
        return true;
    }   //removeSignalMast

    private void removeBeanRefs(NamedBean sm) {
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
    }   //removeBeanRefs

    boolean noWarnPositionablePoint = false;

    /**
     * Remove a PositionablePoint -- an Anchor or an End Bumper.
     */
    protected boolean removePositionablePoint(PositionablePoint o) {
        //First verify with the user that this is really wanted, only show message if there is a bit of track connected
        if ((o.getConnect1() != null) || (o.getConnect2() != null)) {
            if (!noWarnPositionablePoint) {
                int selectedValue = JOptionPane.showOptionDialog(this,
                                                                 rb.getString("Question2"), Bundle.getMessage(
                                                                     "WarningTitle"),
                                                                 JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                                 new Object[] {Bundle.getMessage("ButtonYes"),
                                                                               Bundle.getMessage("ButtonNo"),
                                                                               rb.getString("ButtonYesPlus")},
                                                                 Bundle.getMessage("ButtonNo"));

                if (selectedValue == 1) {
                    return false;   //return without creating if "No" response
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
                setDirty(true);
                repaint();

                return true;
            }
        }
        return false;
    }   //removePositionablePoint

    boolean noWarnLayoutTurnout = false;

    /**
     * Remove a LayoutTurnout
     */
    protected boolean removeLayoutTurnout(LayoutTurnout o) {
        //First verify with the user that this is really wanted
        if (!noWarnLayoutTurnout) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             rb.getString("Question1r"), Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           rb.getString("ButtonYesPlus")},
                                                             Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false;   //return without removing if "No" response
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

        if ((o.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) ||
            (o.getTurnoutType() == LayoutTurnout.RH_XOVER) ||
            (o.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
            LayoutBlock b2 = o.getLayoutBlockB();

            if ((b2 != null) && (b2 != b)) {
                b2.decrementUse();
            }
            LayoutBlock b3 = o.getLayoutBlockC();

            if ((b3 != null) && (b3 != b) && (b3 != b2)) {
                b3.decrementUse();
            }
            LayoutBlock b4 = o.getLayoutBlockD();

            if ((b4 != null) && (b4 != b) &&
                (b4 != b2) && (b4 != b3)) {
                b4.decrementUse();
            }
        }

        //delete from array
        for (int i = 0; i < turnoutList.size(); i++) {
            LayoutTurnout lt = turnoutList.get(i);

            if (lt == o) {
                //found object
                turnoutList.remove(i);
                setDirty(true);
                repaint();

                return true;
            }
        }
        return false;
    }   //removeLayoutTurnout

    private void substituteAnchor(Point2D loc, Object o, TrackSegment t) {
        PositionablePoint p = addAnchor(loc);

        if (t.getConnect1() == o) {
            t.setNewConnect1(p, LayoutTrack.POS_POINT);
        }

        if (t.getConnect2() == o) {
            t.setNewConnect2(p, LayoutTrack.POS_POINT);
        }
        p.setTrackConnection(t);
    }   //substituteAnchor

    boolean noWarnLevelXing = false;

    /**
     * Remove a Level Crossing
     */
    protected boolean removeLevelXing(LevelXing o) {
        //First verify with the user that this is really wanted
        if (!noWarnLevelXing) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             rb.getString("Question3r"), Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           rb.getString("ButtonYesPlus")},
                                                             Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false;   //return without creating if "No" response
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
        for (int i = 0; i < xingList.size(); i++) {
            LevelXing lx = xingList.get(i);

            if (lx == o) {
                //found object
                xingList.remove(i);
                o.remove();
                setDirty(true);
                repaint();

                return true;
            }
        }
        return false;
    }   //removeLevelXing

    boolean noWarnSlip = false;

    protected boolean removeLayoutSlip(LayoutTurnout o) {
        if (!(o instanceof LayoutSlip)) {
            return false;
        }

        //First verify with the user that this is really wanted
        if (!noWarnSlip) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             rb.getString("Question5r"), Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           rb.getString("ButtonYesPlus")},
                                                             Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false;   //return without creating if "No" response
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
        for (int i = 0; i < slipList.size(); i++) {
            LayoutSlip lx = slipList.get(i);

            if (lx == o) {
                //found object
                slipList.remove(i);
                o.remove();
                setDirty(true);
                repaint();

                return true;
            }
        }
        return false;
    }   //removeLayoutSlip

    boolean noWarnTurntable = false;

    /**
     * Remove a Layout Turntable
     */
    protected boolean removeTurntable(LayoutTurntable o) {
        //First verify with the user that this is really wanted
        if (!noWarnTurntable) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                                                             rb.getString("Question4r"), Bundle.getMessage("WarningTitle"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                             new Object[] {Bundle.getMessage("ButtonYes"),
                                                                           Bundle.getMessage("ButtonNo"),
                                                                           rb.getString("ButtonYesPlus")},
                                                             Bundle.getMessage("ButtonNo"));

            if (selectedValue == 1) {
                return false;   //return without creating if "No" response
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
        for (int i = 0; i < turntableList.size(); i++) {
            LayoutTurntable lx = turntableList.get(i);

            if (lx == o) {
                //found object
                turntableList.remove(i);
                o.remove();
                setDirty(true);
                repaint();

                return true;
            }
        }
        return false;
    }   //removeTurntable

    /**
     * Remove a Track Segment
     */
    protected void removeTrackSegment(TrackSegment o) {
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
        setDirty(true);
        repaint();
    }   //removeTrackSegment

    private void disconnect(Object o, int type) {
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

            default:

                if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                    ((LayoutTurntable) o).setRayConnect(null, type - LayoutTrack.TURNTABLE_RAY_OFFSET);
                }
        }   //switch
    }       //disconnect

    public LayoutBlock getAffectedBlock(Object o, int type) {
        if (o == null) {
            return null;
        }

        switch (type) {
            case LayoutTrack.TURNOUT_A: {
                return ((LayoutTurnout) o).getLayoutBlock();
            }

            case LayoutTrack.TURNOUT_B: {
                return ((LayoutTurnout) o).getLayoutBlockB();
            }

            case LayoutTrack.TURNOUT_C: {
                return ((LayoutTurnout) o).getLayoutBlockC();
            }

            case LayoutTrack.TURNOUT_D: {
                return ((LayoutTurnout) o).getLayoutBlockD();
            }

            case LayoutTrack.LEVEL_XING_A: {
                return ((LevelXing) o).getLayoutBlockAC();
            }

            case LayoutTrack.LEVEL_XING_B: {
                return ((LevelXing) o).getLayoutBlockBD();
            }

            case LayoutTrack.LEVEL_XING_C: {
                return ((LevelXing) o).getLayoutBlockAC();
            }

            case LayoutTrack.LEVEL_XING_D: {
                return ((LevelXing) o).getLayoutBlockBD();
            }

            case LayoutTrack.SLIP_A:
            case LayoutTrack.SLIP_B:
            case LayoutTrack.SLIP_C:
            case LayoutTrack.SLIP_D: {
                return ((LayoutSlip) o).getLayoutBlock();
            }

            case LayoutTrack.TRACK:

                return ((TrackSegment) o).getLayoutBlock();
        }   //switch
        return null;
    }   //getAffectedBlock

    /**
     * Add a sensor indicator to the Draw Panel
     */
    void addSensor() {
        String newName = getUserNameForComboBox(sensorComboBox);

        if (newName.length() <= 0) {
            JOptionPane.showMessageDialog(this, rb.getString("Error10"),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                                                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);

//l.setActiveIcon(sensorIconEditor.getIcon(0));
//l.setInactiveIcon(sensorIconEditor.getIcon(1));
//l.setInconsistentIcon(sensorIconEditor.getIcon(2));
//l.setUnknownIcon(sensorIconEditor.getIcon(3));
        l.  setIcon("SensorStateActive",        sensorIconEditor.getIcon(0));
        l.  setIcon("SensorStateInactive",      sensorIconEditor.getIcon(1));
        l.  setIcon("BeanStateInconsistent",    sensorIconEditor.getIcon(2));
        l.  setIcon("BeanStateUnknown",         sensorIconEditor.getIcon(3));
        l.setSensor(newName);
        l.setDisplayLevel(SENSORS);

        //Sensor xSensor = l.getSensor();
        //(Note: I don't see the point of this section of code because…
        if (l.getSensor() != null) {
            if ((l.getNamedSensor().getName() == null) ||
                (!(l.getNamedSensor().getName().equals(newName)))) {
                sensorComboBox.getEditor().setItem(l.getNamedSensor().getName());
            }
        }

        //…because this is called regardless of the code above?!?
        sensorComboBox.getEditor().setItem(l.getNamedSensor().getName());

        setNextLocation(l);
        setDirty(true);
        putItem(l);
    }   //addSensor

    public void putSensor(SensorIcon l) {
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SENSORS);
    }   //putSensor

    /**
     * Add a signal head to the Panel
     */
    void addSignalHead() {
        //check for valid signal head entry
        String newName = getUserNameForComboBox(signalHeadComboBox);
        SignalHead mHead = null;

        if (!newName.equals("")) {
            mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(newName);

            /*if (mHead == null)
               mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(newName);
               else */
            signalHeadComboBox.getEditor().setItem(newName);
        }

        if (mHead == null) {
            //There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(thisPanel,
                                          java.text.MessageFormat.format(rb.getString("Error9"),
                                                                         new Object[] {newName}),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        //create and set up signal icon
        SignalHeadIcon l = new SignalHeadIcon(this);
        l.setSignalHead(newName);
        l.  setIcon(rbean.getString("SignalHeadStateRed"),              signalIconEditor.getIcon(0));
        l.  setIcon(rbean.getString("SignalHeadStateFlashingRed"),      signalIconEditor.getIcon(1));
        l.  setIcon(rbean.getString("SignalHeadStateYellow"),           signalIconEditor.getIcon(2));
        l.  setIcon(rbean.getString("SignalHeadStateFlashingYellow"),   signalIconEditor.getIcon(3));
        l.  setIcon(rbean.getString("SignalHeadStateGreen"),            signalIconEditor.getIcon(4));
        l.  setIcon(rbean.getString("SignalHeadStateFlashingGreen"),    signalIconEditor.getIcon(5));
        l.  setIcon(rbean.getString("SignalHeadStateDark"),             signalIconEditor.getIcon(6));
        l.  setIcon(rbean.getString("SignalHeadStateHeld"),             signalIconEditor.getIcon(7));
        l.  setIcon(rbean.getString("SignalHeadStateLunar"),            signalIconEditor.getIcon(8));
        l.  setIcon(rbean.getString("SignalHeadStateFlashingLunar"),    signalIconEditor.getIcon(9));
        setNextLocation(l);
        setDirty(true);
        putSignal(l);
    }   //addSignalHead

    public void putSignal(SignalHeadIcon l) {
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SIGNALS);
    }   //putSignal

    SignalHead getSignalHead(String name) {
        SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalHead named " + name);
        }
        return sh;
    }   //getSignalHead

    public boolean containsSignalHead(SignalHead head) {
        for (SignalHeadIcon h : signalList) {
            if (h.getSignalHead() == head) {
                return true;
            }
        }
        return false;
    }   //containsSignalHead

    public void removeSignalHead(SignalHead head) {
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
            setDirty(true);
            repaint();
        }
    }   //removeSignalHead

    void addSignalMast() {
        //check for valid signal head entry
        String newName = getUserNameForComboBox(signalMastComboBox);
        SignalMast mMast = null;

        if (!newName.equals("")) {
            mMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(newName);
            signalMastComboBox.getEditor().setItem(newName);
        }

        if (mMast == null) {
            //There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(thisPanel,
                                          java.text.MessageFormat.format(rb.getString("Error9"),
                                                                         new Object[] {newName}),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        //create and set up signal icon
        SignalMastIcon l = new SignalMastIcon(this);
        l.setSignalMast(newName);
        setNextLocation(l);
        setDirty(true);
        putSignalMast(l);
    }   //addSignalMast

    public void putSignalMast(SignalMastIcon l) {
        putItem(l);
        l.updateSize();
        l.setDisplayLevel(SIGNALS);
    }   //putSignalMast

    SignalMast getSignalMast(String name) {
        SignalMast sh = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalMast named " + name);
        }
        return sh;
    }   //getSignalMast

    public boolean containsSignalMast(SignalMast mast) {
        for (SignalMastIcon h : signalMastList) {
            if (h.getSignalMast() == mast) {
                return true;
            }
        }
        return false;
    }   //containsSignalMast

    /**
     * Add a label to the Draw Panel
     */
    void addLabel() {
        String labelText = textLabelTextField.getText();
        labelText = (null != labelText) ? labelText.trim() : "";

        if (labelText.length() <= 0) {
            JOptionPane.showMessageDialog(this, rb.getString("Error11"),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }
        PositionableLabel l = super.addLabel(labelText);
        setDirty(true);
        l.setForeground(defaultTextColor);
    }   //addLabel

    @Override
    public void putItem(Positionable l) {
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
            if (!(((PositionableLabel) l).isBackground())) {
                labelImage.add((PositionableLabel) l);
            } else {
                backgroundImage.add((PositionableLabel) l);
            }
        }
    }   //putItem

    /**
     * Add a memory label to the Draw Panel
     */
    void addMemory() {
        String memoryName = getUserNameForComboBox(textMemoryComboBox);

        if (memoryName.length() <= 0) {
            JOptionPane.showMessageDialog(this, rb.getString("Error11a"),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }
        MemoryIcon l = new MemoryIcon("   ", this);
        l.setMemory(memoryName);
        Memory xMemory = l.getMemory();

        if (xMemory != null) {
            String uname = xMemory.getUserName();

            if ((uname == null) || (!(uname.equals(memoryName)))) {
                //put the system name in the memory field
                textMemoryComboBox.getEditor().setItem(xMemory.getSystemName());
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        l.setForeground(defaultTextColor);
        setDirty(true);
        putItem(l);
    }   //addMemory

    void addBlockContents() {
        String newName = getUserNameForComboBox(blockContentsComboBox);

        if (newName.length() <= 0) {
            JOptionPane.showMessageDialog(this, rb.getString("Error11b"),
                                          Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }
        BlockContentsIcon l = new BlockContentsIcon("   ", this);
        l.setBlock(newName);
        jmri.Block xMemory = l.getBlock();

        if (xMemory != null) {
            String uname = xMemory.getUserName();

            if ((uname == null) || (!(uname.equals(newName)))) {
                //put the system name in the memory field
                blockContentsComboBox.getEditor().setItem(xMemory.getSystemName());
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        l.setForeground(defaultTextColor);
        setDirty(true);
        putItem(l);
    }   //addBlockContents

    /**
     * Add a Reporter Icon to the panel
     */
    void addReporter(String textReporter, int xx, int yy) {
        ReporterIcon l = new ReporterIcon(this);

        l.setReporter(textReporter);
        l.setLocation(xx, yy);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        setDirty(true);
        putItem(l);
    }   //addReporter

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
    }   //addIcon

    /**
     * Add a loco marker to the target
     */
    @Override
    public LocoIcon addLocoIcon(String name) {
        LocoIcon l = new LocoIcon(this);
        Point2D pt = windowCenter();

        l.setLocation((int) pt.getX(), (int) pt.getY());
        putLocoIcon(l, name);
        l.setPositionable(true);

        return l;
    }   //addLocoIcon

    @Override
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
            inputFileChooser = new JFileChooser(System.getProperty(
                                                    "user.dir") + java.io.File.separator + "resources" + java.io.File.separator +
                                                "icons");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            filt.   addExtension("gif");
            filt.   addExtension("jpg");
            inputFileChooser.setFileFilter(filt);
        }
        inputFileChooser.rescanCurrentDirectory();

        int retVal = inputFileChooser.showOpenDialog(this);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; //give up if no file selected
        }//NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
//inputFileChooser.getSelectedFile().getPath());

        String name = inputFileChooser.getSelectedFile().getPath();

        //convert to portable path
        name = jmri.util.FileUtil.getPortableFilename(name);

        //setup icon
        backgroundImage.add(super.setUpBackground(name));
    }   //addBackground

    /**
     * Remove a background image from the list of background images
     */
    protected void removeBackground(PositionableLabel b) {
        for (int i = 0; i < backgroundImage.size(); i++) {
            if (b == backgroundImage.get(i)) {
                backgroundImage.remove(i);
                setDirty(true);

                return;
            }
        }
    }   //removeBackground

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
    }   //startMultiSensor

    //Invoked when window has new multi-sensor ready
    public void addMultiSensor(MultiSensorIcon l) {
        l.setLocation(multiLocX, multiLocY);
        setDirty(true);
        putItem(l);
        multiSensorFrame = null;
    }   //addMultiSensor

    /**
     * Set object location and size for icon and label object as it is created.
     * Size comes from the preferredSize; location comes from the fields where
     * the user can spec it.
     */
    @Override
    protected void setNextLocation(Positionable obj) {
        obj.setLocation(xLoc, yLoc);
    }

    public ConnectivityUtil getConnectivityUtil() {
        if (conTools == null) {
            conTools = new ConnectivityUtil(thisPanel);
        }
        return conTools;
    }   //getConnectivityUtil

    public LayoutEditorTools getLETools() {
        if (tools == null) {
            tools = new LayoutEditorTools(thisPanel);
        }
        return tools;
    }   //getLETools

    /**
     * Invoked by DeletePanel menu item Validate user intent before deleting
     */
    @Override
    public boolean deletePanel() {
        //verify deletion
        if (!super.deletePanel()) {
            return false;   //return without deleting if "No" response
        }
        turnoutList.clear();
        trackList.clear();
        pointList.clear();
        xingList.clear();
        slipList.clear();
        turntableList.clear();

        return true;
    }   //deletePanel

    /**
     * Control whether target panel items are editable. Does this by invoke the
     * {@link Editor#setAllEditable} function of the parent class. This also
     * controls the relevant pop-up menu items (which are the primary way that
     * items are edited).
     *
     * @param editable true for editable.
     */
    @Override
    public void setAllEditable(boolean editable) {
        int restoreScroll = _scrollState;

        super.setAllEditable(editable);

        if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
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
            setScroll(SCROLL_BOTH);
            _scrollState = restoreScroll;
        } else {
            setScroll(_scrollState);
        }

        //these may not be setup yet…
        if (helpBarPanel != null) {
            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
                floatEditHelpPanel.setVisible(editable && showHelpBar);
            } else {
                helpBarPanel.setVisible(editable && showHelpBar);
            }
        }
        awaitingIconChange = false;
        editModeItem.setSelected(editable);
        repaint();
    }   //setAllEditable

    /**
     * Control whether panel items are positionable. Markers are always
     * positionable.
     *
     * @param state true for positionable.
     */
    @Override
    public void setAllPositionable(boolean state) {
        super.setAllPositionable(state);

        for (int i = 0; i < markerImage.size(); i++) {
            ((Positionable) markerImage.get(i)).setPositionable(true);
        }
    }   //setAllPositionable

    /**
     * Control whether target panel items are controlling layout items. Does
     * this by invoke the {@link Positionable#setControlling} function of each
     * item on the target panel. This also controls the relevant pop-up menu
     * items.
     *
     * @param state true for controlling.
     */
    public void setTurnoutAnimation(boolean state) {
        if (animationItem.isSelected() != state) {
            animationItem.setSelected(state);
        }

        if (animatingLayout != state) {
            animatingLayout = state;
            repaint();
        }
    }   //setTurnoutAnimation

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
        if (_scrollState == SCROLL_NONE) {
            return false;
        } else {
            return true;
        }
    }   //getScroll

    public int setGridSize(int newSize) {
        gridSize = newSize;

        return gridSize;
    }   //setGridSize

    public int getGridSize() {
        int gs = gridSize;

        return gs;
    }   //getGridSize

    public int getMainlineTrackWidth() {
        int wid = (int) mainlineTrackWidth;

        return wid;
    }   //getMainlineTrackWidth

    public int getSideTrackWidth() {
        int wid = (int) sideTrackWidth;

        return wid;
    }   //getSideTrackWidth

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

    public void setLayoutDimensions(int windowW, int windowH, int x, int y, int panelW, int panelH) {
        upperLeftX = x;
        upperLeftY = y;
        windowWidth = windowW;
        windowHeight = windowH;
        panelWidth = panelW;
        panelHeight = panelH;
        setTargetPanelSize(panelWidth, panelHeight);
        setLocation(upperLeftX, upperLeftY);
        setSize(windowWidth, windowHeight);
        log.debug(
            "setLayoutDimensions Position - " + upperLeftX + "," + upperLeftY + " windowSize - " + windowWidth + "," + windowHeight + " panelSize - " + panelWidth + "," +
            panelHeight);
    }   //setLayoutDimensions

    public void setMainlineTrackWidth(int w) {
        mainlineTrackWidth = w;
    }

    public void setSideTrackWidth(int w) {
        sideTrackWidth = w;
    }

    public void setDefaultTrackColor(String color) {
        defaultTrackColor = ColorUtil.stringToColor(color);
        setOptionMenuTrackColor();
    }

    public void setDefaultOccupiedTrackColor(String color) {
        defaultOccupiedTrackColor = ColorUtil.stringToColor(color);
        setOptionMenuTrackColor();
    }

    public void setDefaultAlternativeTrackColor(String color) {
        defaultAlternativeTrackColor = ColorUtil.stringToColor(color);
        setOptionMenuTrackColor();
    }

    public void setTurnoutCircleColor(String newColor) {
        turnoutCircleColor = ColorUtil.stringToColor(newColor);
        setOptionMenuTurnoutCircleColor();
    }

    public void setTurnoutCircleSize(int size) {
        //this is an int
        turnoutCircleSize = size;

        //these are doubles
        circleRadius = SIZE * size;
        circleDiameter = 2.0 * circleRadius;

        setOptionMenuTurnoutCircleSize();
    }   //setTurnoutCircleSize

    public void setTurnoutDrawUnselectedLeg(boolean state) {
        if (turnoutDrawUnselectedLeg != state) {
            turnoutDrawUnselectedLeg = state;
            turnoutDrawUnselectedLegItem.setSelected(turnoutDrawUnselectedLeg);
        }
    }   //setTurnoutDrawUnselectedLeg

    public void setDefaultTextColor(String color) {
        defaultTextColor = ColorUtil.stringToColor(color);
        setOptionMenuTextColor();
    }

    public void setDefaultBackgroundColor(String color) {
        defaultBackgroundColor = ColorUtil.stringToColor(color);
        setOptionMenuBackgroundColor();
    }

    public void setXScale(double xSc) {
        xScale = xSc;
    }

    public void setYScale(double ySc) {
        yScale = ySc;
    }

    public void setLayoutName(String name) {
        layoutName = name;
    }

    public void setShowHelpBar(boolean state) {
        if (showHelpBar != state) {
            showHelpBar = state;

            //these may not be setup yet…
            if (showHelpItem != null) {
                showHelpItem.setSelected(showHelpBar);
            }

            if (toolBarSide.equals(eToolBarSide.eFLOAT)) {
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
    }   //setShowHelpBar

    public void setDrawGrid(boolean state) {
        if (drawGrid != state) {
            drawGrid = state;
            showGridItem.setSelected(drawGrid);
        }
    }   //setDrawGrid

    public void setSnapOnAdd(boolean state) {
        if (snapToGridOnAdd != state) {
            snapToGridOnAdd = state;
            snapToGridOnAddItem.setSelected(snapToGridOnAdd);
        }
    }   //setSnapOnAdd

    public void setSnapOnMove(boolean state) {
        if (snapToGridOnMove != state) {
            snapToGridOnMove = state;
            snapToGridOnMoveItem.setSelected(snapToGridOnMove);
        }
    }   //setSnapOnMove

    public void setAntialiasingOn(boolean state) {
        if (antialiasingOn != state) {
            antialiasingOn = state;

            //this may not be setup yet…
            if (antialiasingOnItem != null) {
                antialiasingOnItem.setSelected(antialiasingOn);
            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".antialiasingOn", antialiasingOn);
            });
        }
    }   //setAntialiasingOn

    //enable/disable using the "Extra" color to highlight the selected block
    public void setHighlightSelectedBlock(boolean state) {
        if (highlightSelectedBlockFlag != state) {
            highlightSelectedBlockFlag = state;

            //this may not be setup yet…
            if (highlightSelectedBlockItem != null) {
                highlightSelectedBlockItem.setSelected(highlightSelectedBlockFlag);
            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
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
    }   //setHighlightSelectedBlock

    //
    //highlight the block selected by the specified combo Box
    //
    private boolean highlightBlockInComboBox(JmriBeanComboBox inComboBox) {
        boolean result = false;

        if (null != inComboBox) {
            jmri.Block b = (jmri.Block)getBeanForComboBox(inComboBox);
            result = highlightBlock(b);
        }
        return result;
    }   //highlightBlockInComboBox

    //
    //
    //
    private boolean highlightBlock(jmri.Block inBlock) {
        boolean result = false; //assume failure (pessimist!)

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);

        jmri.Manager m = blockIDComboBox.getManager();
        List<NamedBean> l = m.getNamedBeanList();

        for (NamedBean nb : l) {
            jmri.Block b = (jmri.Block)nb;
            LayoutBlock lb = lbm.getLayoutBlock(b);

            if (null != lb) {
                boolean enable = ((null != inBlock) && b.equals(inBlock));
                lb.setUseExtraColor(enable);
                result |= enable;
            }
        }
        return result;
    }   //highlightBlock

    public void setTurnoutCircles(boolean state) {
        if (turnoutCirclesWithoutEditMode != state) {
            turnoutCirclesWithoutEditMode = state;
            turnoutCirclesOnItem.setSelected(turnoutCirclesWithoutEditMode);
        }
    }   //setTurnoutCircles

    public void setAutoBlockAssignment(boolean boo) {
        if (autoAssignBlocks != boo) {
            autoAssignBlocks = boo;
            autoAssignBlocksItem.setSelected(autoAssignBlocks);
        }
    }   //setAutoBlockAssignment

    public void setTooltipsNotEdit(boolean state) {
        if (tooltipsWithoutEditMode != state) {
            tooltipsWithoutEditMode = state;
            setTooltipSubMenu();
        }
    }   //setTooltipsNotEdit

    public void setTooltipsInEdit(boolean state) {
        if (tooltipsInEditMode != state) {
            tooltipsInEditMode = state;
            setTooltipSubMenu();
        }
    }   //setTooltipsInEdit

    private void setTooltipSubMenu() {
        tooltipNone.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipAlways.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipInEdit.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNotInEdit.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
    }   //setTooltipSubMenu

    //accessor routines for turnout size parameters
    public void setTurnoutBX(double bx) {
        turnoutBX = bx;
        setDirty(true);
    }

    public double getTurnoutBX() {
        return turnoutBX;
    }

    public void setTurnoutCX(double cx) {
        turnoutCX = cx;
        setDirty(true);
    }

    public double getTurnoutCX() {
        return turnoutCX;
    }

    public void setTurnoutWid(double wid) {
        turnoutWid = wid;
        setDirty(true);
    }

    public double getTurnoutWid() {
        return turnoutWid;
    }

    public void setXOverLong(double lg) {
        xOverLong = lg;
        setDirty(true);
    }

    public double getXOverLong() {
        return xOverLong;
    }

    public void setXOverHWid(double hwid) {
        xOverHWid = hwid;
        setDirty(true);
    }

    public double getXOverHWid() {
        return xOverHWid;
    }

    public void setXOverShort(double sh) {
        xOverShort = sh;
        setDirty(true);
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
        setDirty(true);
    }   //resetTurnoutSize

    public void setDirectTurnoutControl(boolean boo) {
        useDirectTurnoutControl = boo;
        useDirectTurnoutControlItem.setSelected(useDirectTurnoutControl);
    }

    public boolean getDirectTurnoutControl() {
        return useDirectTurnoutControl;
    }

    //final initialization routine for loading a LayoutEditor
    public void setConnections() {
        //initialize TrackSegments if any
        for (TrackSegment t : trackList) {
            t.setObjects(this);
        }

        //initialize PositionablePoints if any
        for (PositionablePoint p : pointList) {
            p.setObjects(this);
        }

        //initialize LevelXings if any
        for (LevelXing x : xingList) {
            x.setObjects(this);
        }

        //initialize LevelXings if any
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
        log.debug("Initializing Block Connectivity for " + layoutName);

        //reset the panel changed bit
        resetDirty();
    }   //setConnections

    //these are convenience methods to return rectangles
    //to do point-in-rect (hit) testing
    //compute the control point rect at inPoint
    public Rectangle2D controlPointRectAt(Point2D inPoint) {
        return new Rectangle2D.Double(
            inPoint.getX() - LayoutTrack.controlPointSize,
            inPoint.getY() - LayoutTrack.controlPointSize,
            LayoutTrack.controlPointSize2, LayoutTrack.controlPointSize2);
    }   //controlPointRectAt

    //compute the turnout circle rect at inPoint
    public Rectangle2D turnoutCircleRectAt(Point2D inPoint) {
        return new Rectangle2D.Double(inPoint.getX() - circleRadius,
                                      inPoint.getY() - circleRadius, circleDiameter, circleDiameter);
    }

    //compute the turnout circle at inPoint (used for drawing)
    public Ellipse2D turnoutCircleAt(Point2D inPoint) {
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

        if (isEditable() && drawGrid) {
            drawPanelGrid(g2);
        }
        g2.setColor(defaultTrackColor);
        main = false;
        g2.setStroke(new BasicStroke(sideTrackWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        drawHiddenTrack(g2);
        drawDashedTrack(g2, false); //non-mainline
        drawDashedTrack(g2, true);  //mainline
        drawSolidTrack(g2, false);  //non-mainline
        drawSolidTrack(g2, true);   //mainline
        drawTurnouts(g2);
        drawXings(g2);
        drawSlips(g2);
        drawTurntables(g2);
        drawTrackInProgress(g2);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPoints(g2);

        if (isEditable()) {
            drawTurnoutRects(g2);
            drawXingRects(g2);
            drawSlipRects(g2);
            drawTrackOvals(g2);
            drawSelectionRect(g2);
            drawTurntableRects(g2);
            drawMemoryRects(g2);
            drawBlockContentsRects(g2);
            drawTrackCircleCentre(g2);
            drawTurnoutCircles(g2);
            highLightSelection(g2);
        } else if (turnoutCirclesWithoutEditMode) {
            drawTurnoutCircles(g2);
            drawSlipCircles(g2);
        }
    }   //paintTargetPanel

    boolean main = true;
    float trackWidth = sideTrackWidth;

    //had to make this public so the LayoutTrack classes could access it
    //also returned the current value of trackWidth for the callers to use
    public float setTrackStrokeWidth(Graphics2D g2, boolean need) {
        if (main != need) {
            main = need;

            //change track stroke width
            trackWidth = main ? mainlineTrackWidth : sideTrackWidth;
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        }
        return trackWidth;
    }   //setTrackStrokeWidth

    protected void drawTurnouts(Graphics2D g2) {
        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            if (!t.isHidden() || isEditable()) {
                t.draw(g2);
            }
        }
    }   //drawTurnouts

    private void drawXings(Graphics2D g2) {
        //loop over all defined level crossings
        for (LevelXing x : xingList) {
            if (!(x.isHidden() && !isEditable())) {
                x.draw(g2);
            }
        }
    }   //drawXings

    private void drawSlips(Graphics2D g2) {
        for (LayoutSlip sl : slipList) {
            sl.draw(g2);
        }
    }   //drawSlips

    private void drawTurnoutCircles(Graphics2D g2) {
        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            g2.setColor(turnoutCircleColor);

            if (!(t.isHidden() && !isEditable())) {
                t.drawTurnoutCircle(g2);
            }
        }
    }   //drawTurnoutCircles

    private void drawSlipCircles(Graphics2D g2) {
        //loop over all defined slips
        g2.setColor(turnoutCircleColor);

        for (LayoutSlip sl : slipList) {
            if (!(sl.isHidden() && !isEditable())) {
                sl.drawSlipCircles(g2);
            }
        }
    }   //drawSlipCircles

    private void drawTurnoutRects(Graphics2D g2) {
        //loop over all defined turnouts
        for (LayoutTurnout t : turnoutList) {
            g2.setColor(turnoutCircleColor);
            t.drawTurnoutRect(g2);
        }
    }   //drawTurnoutRects

    private void drawTurntables(Graphics2D g2) {
        //loop over all defined layout turntables
        for (LayoutTurntable x : turntableList) {
            //draw turntable circle - default track color, side track width
            setTrackStrokeWidth(g2, false);
            Point2D c = x.getCoordsCenter();
            double r = x.getRadius();
            double d = r + r;
            g2.setColor(defaultTrackColor);
            g2.draw(new Ellipse2D.Double(c.getX() - r, c.getY() - r, d, d));

            //draw ray tracks
            for (int j = 0; j < x.getNumberRays(); j++) {
                Point2D pt = x.getRayCoordsOrdered(j);
                TrackSegment t = x.getRayConnectOrdered(j);

                if (t != null) {
                    setTrackStrokeWidth(g2, t.getMainline());
                    LayoutBlock b = t.getLayoutBlock();

                    if (b != null) {
                        g2.setColor(b.getBlockColor());
                    } else {
                        g2.setColor(defaultTrackColor);
                    }
                } else {
                    setTrackStrokeWidth(g2, false);
                    g2.setColor(defaultTrackColor);
                }
                g2.draw(new Line2D.Double(new Point2D.Double(
                                              pt.getX() - ((pt.getX() - c.getX()) * 0.2),
                                              pt.getY() - ((pt.getY() - c.getY()) * 0.2)), pt));
            }

            if (x.isTurnoutControlled() && (x.getPosition() != -1)) {
                Point2D pt = x.getRayCoordsIndexed(x.getPosition());
                g2.draw(new Line2D.Double(new Point2D.Double(
                                              pt.getX() - ((pt.getX() - c.getX()) * 1.8 /*2*/),
                                              pt.getY() - ((pt.getY() - c.getY()) * 1.8 /**
                                                                                         * 2
                                                                                         */
                                                        )), pt));
            }
        }
    }   //drawTurntables

    private void drawXingRects(Graphics2D g2) {
        //loop over all defined level crossings
        for (LevelXing x : xingList) {
            Point2D pt = x.getCoordsCenter();
            g2.setColor(defaultTrackColor);
            g2.draw(controlPointRectAt(pt));
            pt = x.getCoordsA();

            if (x.getConnectA() == null) {
                g2.setColor(Color.magenta);
            } else {
                g2.setColor(Color.blue);
            }
            g2.draw(controlPointRectAt(pt));
            pt = x.getCoordsB();

            if (x.getConnectB() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(controlPointRectAt(pt));
            pt = x.getCoordsC();

            if (x.getConnectC() == null) {
                g2.setColor(Color.magenta);
            } else {
                g2.setColor(Color.blue);
            }
            g2.draw(controlPointRectAt(pt));
            pt = x.getCoordsD();

            if (x.getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(controlPointRectAt(pt));
        }
    }   //drawXingRects

    private void drawSlipRects(Graphics2D g2) {
        //loop over all defined slips
        for (LayoutSlip sl : slipList) {
            if (!(sl.isHidden() && !isEditable())) {
                g2.setColor(turnoutCircleColor);
                sl.drawSlipRect(g2);
            }
        }
    }   //drawSlipRects

    private void drawTurntableRects(Graphics2D g2) {
        //loop over all defined turntables
        for (LayoutTurntable x : turntableList) {
            Point2D pt = x.getCoordsCenter();
            g2.setColor(defaultTrackColor);
            g2.draw(controlPointRectAt(pt));

            for (int j = 0; j < x.getNumberRays(); j++) {
                pt = x.getRayCoordsOrdered(j);

                if (x.getRayConnectOrdered(j) == null) {
                    g2.setColor(Color.red);
                } else {
                    g2.setColor(Color.green);
                }
                g2.draw(controlPointRectAt(pt));
            }
        }
    }   //drawTurntableRects

    private void drawHiddenTrack(Graphics2D g2) {
        for (TrackSegment t : trackList) {
            if (isEditable() && t.isHidden()) {
                t.draw(g2);
                setTrackStrokeWidth(g2, !main);
            }
        }
    }   //drawHiddenTrack

    private void drawDashedTrack(Graphics2D g2, boolean mainline) {
        for (TrackSegment t : trackList) {
            t.drawDashed(g2, mainline);
        }
    }   //drawDashedTrack

    /* draw all track segments which are not hidden, not dashed, and that match the isMainline parm */
    private void drawSolidTrack(Graphics2D g2, boolean isMainline) {
        for (TrackSegment t : trackList) {
            setTrackStrokeWidth(g2, isMainline);

            if ((!t.isHidden()) && (!t.getDashed()) && (isMainline == t.getMainline())) {
                t.drawSolid(g2, isMainline);
            }
        }
    }   //drawSolidTrack

    /*
     * Draws a square at the circles centre, that then allows the user to dynamically change
     * the angle by dragging the mouse.
     */
    private void drawTrackCircleCentre(Graphics2D g2) {
        //loop over all defined track segments
        for (TrackSegment t : trackList) {
            g2.setColor(Color.black);

            if (t.getCircle() && t.showConstructionLinesLE()) {
                Point2D pt = t.getCoordsCenterCircle();
                g2.draw(turnoutCircleRectAt(pt));
            }
        }
    }   //drawTrackCircleCentre

    private void drawTrackInProgress(Graphics2D g2) {
        //check for segment in progress
        if (isEditable() && (beginObject != null) && trackButton.isSelected()) {
            g2.setColor(defaultTrackColor);
            setTrackStrokeWidth(g2, false);
            g2.draw(new Line2D.Double(beginLocation, currentLocation));
        }
    }   //drawTrackInProgress

    private void drawTrackOvals(Graphics2D g2) {
        //loop over all defined track segments
        g2.setColor(defaultTrackColor);

        for (TrackSegment t : trackList) {
            t.drawOvals(g2);
        }
    }   //drawTrackOvals

    private void drawPoints(Graphics2D g2) {
        for (PositionablePoint p : pointList) {
            switch (p.getType()) {
                case PositionablePoint.ANCHOR: {
                    //nothing to draw unless in edit mode
                    if (isEditable()) {
                        //in edit mode, draw locater rectangle
                        Point2D pt = p.getCoords();

                        if ((p.getConnect1() == null) || (p.getConnect2() == null)) {
                            g2.setColor(Color.red);
                        } else {
                            g2.setColor(Color.green);
                        }
                        g2.draw(controlPointRectAt(pt));
                    }
                    break;
                }

                case PositionablePoint.END_BUMPER: {
                    //nothing to draw unless in edit mode
                    if (isEditable()) {
                        //in edit mode, draw locater rectangle
                        Point2D pt = p.getCoords();

                        if (p.getConnect1() == null) {
                            g2.setColor(Color.red);
                        } else {
                            g2.setColor(Color.green);
                        }
                        g2.draw(controlPointRectAt(pt));
                    }
                    break;
                }

                case PositionablePoint.EDGE_CONNECTOR: {
                    //nothing to draw unless in edit mode
                    if (isEditable()) {
                        //in edit mode, draw locater rectangle
                        Point2D pt = p.getCoords();

                        if (p.getConnect1() == null) {
                            g2.setColor(Color.red);
                        } else if (p.getConnect2() == null) {
                            g2.setColor(Color.blue);
                        } else {
                            g2.setColor(Color.green);
                        }
                        g2.draw(controlPointRectAt(pt));
                    }
                    break;
                }

                default:
                    log.error("Illegal type of Positionable Point");
            }   //switch
        }
    }   //drawPoints

    private void drawSelectionRect(Graphics2D g2) {
        if (selectionActive && (selectionWidth != 0.0) && (selectionHeight != 0.0)) {
            g2.setColor(defaultTrackColor);
            g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(new Rectangle2D.Double(selectionX, selectionY, selectionWidth, selectionHeight));
        }
    }   //drawSelectionRect

    private void drawMemoryRects(Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        for (MemoryIcon l : memoryLabelList) {
            g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height));
        }
    }   //drawMemoryRects

    private void drawBlockContentsRects(Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        for (BlockContentsIcon l : blockContentsLabelList) {
            g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height));
        }
    }   //drawBlockContentsRects

    private void drawPanelGrid(Graphics2D g2) {
        int wideMod = gridSize * 10;
        int wideMin = gridSize / 2;

        Dimension dim = getSize();
        double maxX = dim.width;
        double maxY = dim.height;

        Point2D startPt = new Point2D.Double(0.0, gridSize);
        Point2D stopPt = new Point2D.Double(maxX, gridSize);
        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2.setColor(Color.gray);
        g2.setStroke(narrow);

        //draw horizontal lines
        double pix = gridSize;

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
            pix += gridSize;
        }

        //draw vertical lines
        pix = gridSize;

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
            pix += gridSize;
        }
    }   //drawPanelGrid

    protected Point2D getCoords(Object o, int type) {
        if (o != null) {
            switch (type) {
                case LayoutTrack.POS_POINT: {
                    return ((PositionablePoint) o).getCoords();
                }

                case LayoutTrack.TURNOUT_A: {
                    return ((LayoutTurnout) o).getCoordsA();
                }

                case LayoutTrack.TURNOUT_B: {
                    return ((LayoutTurnout) o).getCoordsB();
                }

                case LayoutTrack.TURNOUT_C: {
                    return ((LayoutTurnout) o).getCoordsC();
                }

                case LayoutTrack.TURNOUT_D: {
                    return ((LayoutTurnout) o).getCoordsD();
                }

                case LayoutTrack.LEVEL_XING_A: {
                    return ((LevelXing) o).getCoordsA();
                }

                case LayoutTrack.LEVEL_XING_B: {
                    return ((LevelXing) o).getCoordsB();
                }

                case LayoutTrack.LEVEL_XING_C: {
                    return ((LevelXing) o).getCoordsC();
                }

                case LayoutTrack.LEVEL_XING_D: {
                    return ((LevelXing) o).getCoordsD();
                }

                case LayoutTrack.SLIP_A: {
                    return ((LayoutSlip) o).getCoordsA();
                }

                case LayoutTrack.SLIP_B: {
                    return ((LayoutSlip) o).getCoordsB();
                }

                case LayoutTrack.SLIP_C: {
                    return ((LayoutSlip) o).getCoordsC();
                }

                case LayoutTrack.SLIP_D: {
                    return ((LayoutSlip) o).getCoordsD();
                }

                default:

                    if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                        return ((LayoutTurntable) o).getRayCoordsIndexed(type - LayoutTrack.TURNTABLE_RAY_OFFSET);
                    }
            }   //switch
        } else {
            log.error("Null connection point of type " + type + " " + getLayoutName());
        }
        return new Point2D.Double(0.0, 0.0);
    }   //getCoords

    @Override
    protected boolean showAlignPopup(Positionable l) {
        return false;
    }

    @Override
    public void showToolTip(Positionable selection, MouseEvent event) {
        ToolTip tip = selection.getTooltip();

        tip.setLocation(selection.getX() + selection.getWidth() / 2, selection.getY() + selection.getHeight());
        tip.setText(selection.getNameString());
        setToolTip(tip);
    }   //showToolTip

    @Override
    public void addToPopUpMenu(NamedBean nb, JMenuItem item, int menu) {
        if ((nb == null) || (item == null)) {
            return;
        }

        if (nb instanceof Sensor) {
            for (SensorIcon si : sensorList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si. getPopupUtility().addEditPopUpMenu(item);
                            si. getPopupUtility().addViewPopUpMenu(item);
                    }   //switch
                }
            }
        } else if (nb instanceof SignalHead) {
            for (SignalHeadIcon si : signalList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si. getPopupUtility().addEditPopUpMenu(item);
                            si. getPopupUtility().addViewPopUpMenu(item);
                    }   //switch
                }
            }
        } else if (nb instanceof SignalMast) {
            for (SignalMastIcon si : signalMastList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si. getPopupUtility().addEditPopUpMenu(item);
                            si. getPopupUtility().addViewPopUpMenu(item);
                    }   //switch
                }
            }
        } else if (nb instanceof jmri.Block) {
            for (BlockContentsIcon si : blockContentsLabelList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si. getPopupUtility().addEditPopUpMenu(item);
                            si. getPopupUtility().addViewPopUpMenu(item);
                    }   //switch
                }
            }
        } else if (nb instanceof Memory) {
            for (MemoryIcon si : memoryLabelList) {
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            si.getPopupUtility().addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            si.getPopupUtility().addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            si. getPopupUtility().addEditPopUpMenu(item);
                            si. getPopupUtility().addViewPopUpMenu(item);
                    }   //switch
                }
            }
        } else if (nb instanceof Turnout) {
            for (LayoutTurnout ti : turnoutList) {
                if (ti.getTurnout().equals(nb)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            ti.addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            ti.addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            ti.addEditPopUpMenu(item);
                            ti.addViewPopUpMenu(item);
                    }   //switch
                    break;
                }
            }

            for (LayoutSlip sl : slipList) {
                if ((sl.getTurnout() == nb) || (sl.getTurnoutB() == nb)) {
                    switch (menu) {
                        case VIEWPOPUPONLY: {
                            sl.addViewPopUpMenu(item);
                            break;
                        }

                        case EDITPOPUPONLY: {
                            sl.addEditPopUpMenu(item);
                            break;
                        }

                        default:
                            sl.addEditPopUpMenu(item);
                            sl.addViewPopUpMenu(item);
                    }   //switch
                    break;
                }
            }
        }
    }   //addToPopUpMenu

    @Override
    public String toString() {
        return getLayoutName();
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();

        if ("CanDelete".equals(evt.getPropertyName())) {    //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoInUseLayoutEditorHeader", toString()));   //IN18N
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

                        if (nb.getSystemName().equals(t.getLinkedTurnoutName()) ||
                            ((uname != null) && uname.equals(t.getLinkedTurnoutName()))) {
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
                                message.append(Bundle.getMessage("VetoRayTurntableControl", lx.getID()));
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
                    message.append("As an Icon " + count + " times");
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
                message.append(Bundle.getMessage("VetoReferencesWillBeRemoved"));   //IN18N
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) {  //IN18N
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
                        if (t.getLinkedTurnoutName().equals(nb.getSystemName()) ||
                            ((nb.getUserName() != null) && t.getLinkedTurnoutName().equals(nb.getUserName()))) {
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
                    setDirty(true);
                    repaint();
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
                setDirty(true);
                repaint();
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
    }   //vetoableChange

    //initialize logging
    private final static Logger log = LoggerFactory.getLogger(LayoutEditor.class.getName());
}

