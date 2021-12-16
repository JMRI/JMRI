package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.*;
import jmri.configurexml.StoreXmlUserAction;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.dispatcher.DispatcherAction;
import jmri.jmrit.dispatcher.DispatcherFrame;
import jmri.jmrit.display.*;
import jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.*;
import jmri.jmrit.display.layoutEditor.LayoutEditorToolBarPanel.LocationFormat;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.entryexit.AddEntryExitPairAction;
import jmri.swing.NamedBeanComboBox;
import jmri.util.*;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriColorChooser;

/**
 * Provides a scrollable Layout Panel and editor toolbars (that can be hidden)
 * <p>
 * This module serves as a manager for the LayoutTurnout, Layout Block,
 * PositionablePoint, Track Segment, LayoutSlip and LevelXing objects which are
 * integral subparts of the LayoutEditor class.
 * <p>
 * All created objects are put on specific levels depending on their type
 * (higher levels are in front): Note that higher numbers appear behind lower
 * numbers.
 * <p>
 * The "contents" List keeps track of all text and icon label objects added to
 * the target frame for later manipulation. Other Lists keep track of drawn
 * items.
 * <p>
 * Based in part on PanelEditor.java (Bob Jacobsen (c) 2002, 2003). In
 * particular, text and icon label items are copied from Panel editor, as well
 * as some of the control design.
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 * @author George Warner Copyright: (c) 2017-2019
 */
final public class LayoutEditor extends PanelEditor implements MouseWheelListener, LayoutModels {

    // Operational instance variables - not saved to disk
    private JmriJFrame floatingEditToolBoxFrame = null;
    private JScrollPane floatingEditContentScrollPane = null;
    private JPanel floatEditHelpPanel = null;

    private JPanel editToolBarContainerPanel = null;
    private JScrollPane editToolBarScrollPane = null;

    private JPanel helpBarPanel = null;
    private final JPanel helpBar = new JPanel();

    private final boolean editorUseOldLocSize;

    private LayoutEditorToolBarPanel leToolBarPanel = null;

    @Nonnull
    public LayoutEditorToolBarPanel getLayoutEditorToolBarPanel() {
        return leToolBarPanel;
    }

    // end of main panel controls
    private boolean delayedPopupTrigger = false;
    private Point2D currentPoint = new Point2D.Double(100.0, 100.0);
    private Point2D dLoc = new Point2D.Double(0.0, 0.0);

    private int toolbarHeight = 100;
    private int toolbarWidth = 100;

    private TrackSegment newTrack = null;
    private boolean panelChanged = false;

    // size of point boxes
    public static final double SIZE = 3.0;
    public static final double SIZE2 = SIZE * 2.; // must be twice SIZE

    public Color turnoutCircleColor = Color.black; // matches earlier versions
    public Color turnoutCircleThrownColor = Color.black;
    private boolean turnoutFillControlCircles = false;
    private int turnoutCircleSize = 4; // matches earlier versions

    // use turnoutCircleSize when you need an int and these when you need a double
    // note: these only change when setTurnoutCircleSize is called
    // using these avoids having to call getTurnoutCircleSize() and
    // the multiply (x2) and the int -> double conversion overhead
    public double circleRadius = SIZE * getTurnoutCircleSize();
    public double circleDiameter = 2.0 * circleRadius;

    // selection variables
    public boolean selectionActive = false;
    private double selectionX = 0.0;
    private double selectionY = 0.0;
    public double selectionWidth = 0.0;
    public double selectionHeight = 0.0;

    // Option menu items
    private JCheckBoxMenuItem editModeCheckBoxMenuItem = null;

    private JRadioButtonMenuItem toolBarSideTopButton = null;
    private JRadioButtonMenuItem toolBarSideLeftButton = null;
    private JRadioButtonMenuItem toolBarSideBottomButton = null;
    private JRadioButtonMenuItem toolBarSideRightButton = null;
    private JRadioButtonMenuItem toolBarSideFloatButton = null;

    private final JCheckBoxMenuItem wideToolBarCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ToolBarWide"));

    private JCheckBoxMenuItem positionableCheckBoxMenuItem = null;
    private JCheckBoxMenuItem controlCheckBoxMenuItem = null;
    private JCheckBoxMenuItem animationCheckBoxMenuItem = null;
    private JCheckBoxMenuItem showHelpCheckBoxMenuItem = null;
    private JCheckBoxMenuItem showGridCheckBoxMenuItem = null;
    private JCheckBoxMenuItem autoAssignBlocksCheckBoxMenuItem = null;
    private JMenu scrollMenu = null;
    private JRadioButtonMenuItem scrollBothMenuItem = null;
    private JRadioButtonMenuItem scrollNoneMenuItem = null;
    private JRadioButtonMenuItem scrollHorizontalMenuItem = null;
    private JRadioButtonMenuItem scrollVerticalMenuItem = null;
    private JMenu tooltipMenu = null;
    private JRadioButtonMenuItem tooltipAlwaysMenuItem = null;
    private JRadioButtonMenuItem tooltipNoneMenuItem = null;
    private JRadioButtonMenuItem tooltipInEditMenuItem = null;
    private JRadioButtonMenuItem tooltipNotInEditMenuItem = null;

    private JCheckBoxMenuItem pixelsCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Pixels"));
    private JCheckBoxMenuItem metricCMCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("MetricCM"));
    private JCheckBoxMenuItem englishFeetInchesCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EnglishFeetInches"));

    private JCheckBoxMenuItem snapToGridOnAddCheckBoxMenuItem = null;
    private JCheckBoxMenuItem snapToGridOnMoveCheckBoxMenuItem = null;
    private JCheckBoxMenuItem antialiasingOnCheckBoxMenuItem = null;
    private JCheckBoxMenuItem drawLayoutTracksLabelCheckBoxMenuItem = null;
    private JCheckBoxMenuItem turnoutCirclesOnCheckBoxMenuItem = null;
    private JCheckBoxMenuItem turnoutDrawUnselectedLegCheckBoxMenuItem = null;
    private JCheckBoxMenuItem turnoutFillControlCirclesCheckBoxMenuItem = null;
    private JCheckBoxMenuItem hideTrackSegmentConstructionLinesCheckBoxMenuItem = null;
    private JCheckBoxMenuItem useDirectTurnoutControlCheckBoxMenuItem = null;
    private ButtonGroup turnoutCircleSizeButtonGroup = null;

    private boolean turnoutDrawUnselectedLeg = true;
    private boolean autoAssignBlocks = false;

    // Tools menu items
    private final JMenu zoomMenu = new JMenu(Bundle.getMessage("MenuZoom"));
    private final JRadioButtonMenuItem zoom025Item = new JRadioButtonMenuItem("x 0.25");
    private final JRadioButtonMenuItem zoom05Item = new JRadioButtonMenuItem("x 0.5");
    private final JRadioButtonMenuItem zoom075Item = new JRadioButtonMenuItem("x 0.75");
    private final JRadioButtonMenuItem noZoomItem = new JRadioButtonMenuItem(Bundle.getMessage("NoZoom"));
    private final JRadioButtonMenuItem zoom15Item = new JRadioButtonMenuItem("x 1.5");
    private final JRadioButtonMenuItem zoom20Item = new JRadioButtonMenuItem("x 2.0");
    private final JRadioButtonMenuItem zoom30Item = new JRadioButtonMenuItem("x 3.0");
    private final JRadioButtonMenuItem zoom40Item = new JRadioButtonMenuItem("x 4.0");
    private final JRadioButtonMenuItem zoom50Item = new JRadioButtonMenuItem("x 5.0");
    private final JRadioButtonMenuItem zoom60Item = new JRadioButtonMenuItem("x 6.0");
    private final JRadioButtonMenuItem zoom70Item = new JRadioButtonMenuItem("x 7.0");
    private final JRadioButtonMenuItem zoom80Item = new JRadioButtonMenuItem("x 8.0");

    private final JMenuItem undoTranslateSelectionMenuItem = new JMenuItem(Bundle.getMessage("UndoTranslateSelection"));
    private final JMenuItem assignBlockToSelectionMenuItem = new JMenuItem(Bundle.getMessage("AssignBlockToSelectionTitle") + "...");

    // Selected point information
    private Point2D startDelta = new Point2D.Double(0.0, 0.0); // starting delta coordinates
    public Object selectedObject = null;       // selected object, null if nothing selected
    public Object prevSelectedObject = null;   // previous selected object, for undo
    private HitPointType selectedHitPointType = HitPointType.NONE;         // hit point type within the selected object

    public LayoutTrack foundTrack = null;      // found object, null if nothing found
    public LayoutTrackView foundTrackView = null;                 // found view object, null if nothing found
    private Point2D foundLocation = new Point2D.Double(0.0, 0.0); // location of found object
    public HitPointType foundHitPointType = HitPointType.NONE;          // connection type within the found object

    public LayoutTrack beginTrack = null;      // begin track segment connection object, null if none
    public Point2D beginLocation = new Point2D.Double(0.0, 0.0); // location of begin object
    private HitPointType beginHitPointType = HitPointType.NONE; // connection type within begin connection object

    public Point2D currentLocation = new Point2D.Double(0.0, 0.0); // current location

    // Lists of items that describe the Layout, and allow it to be drawn
    // Each of the items must be saved to disk over sessions
    private List<AnalogClock2Display> clocks = new ArrayList<>();           // fast clocks
    private List<LocoIcon> markerImage = new ArrayList<>();                 // marker images
    private List<MultiSensorIcon> multiSensors = new ArrayList<>();         // multi-sensor images
    private List<PositionableLabel> backgroundImage = new ArrayList<>();    // background images
    private List<PositionableLabel> labelImage = new ArrayList<>();         // positionable label images
    private List<SensorIcon> sensorImage = new ArrayList<>();               // sensor images
    private List<SignalHeadIcon> signalHeadImage = new ArrayList<>();       // signal head images

    // PositionableLabel's
    private List<BlockContentsIcon> blockContentsLabelList = new ArrayList<>(); // BlockContentsIcon Label List
    private List<MemoryIcon> memoryLabelList = new ArrayList<>();               // Memory Label List
    private List<SensorIcon> sensorList = new ArrayList<>();                    // Sensor Icons
    private List<SignalHeadIcon> signalList = new ArrayList<>();                // Signal Head Icons
    private List<SignalMastIcon> signalMastList = new ArrayList<>();            // Signal Mast Icons

    public final LayoutEditorViewContext gContext = new LayoutEditorViewContext(); // public for now, as things work access changes

    @Nonnull
    public List<SensorIcon> getSensorList() {
        return sensorList;
    }

    @Nonnull
    public List<PositionableLabel> getLabelImageList()  {
        return labelImage;
    }

    @Nonnull
    public List<BlockContentsIcon> getBlockContentsLabelList() {
        return blockContentsLabelList;
    }

    @Nonnull
    public List<MemoryIcon> getMemoryLabelList() {
        return memoryLabelList;
    }

    @Nonnull
    public List<SignalHeadIcon> getSignalList() {
        return signalList;
    }

    @Nonnull
    public List<SignalMastIcon> getSignalMastList() {
        return signalMastList;
    }

    private final List<LayoutShape> layoutShapes = new ArrayList<>();               // LayoutShap list

    // counts used to determine unique internal names
    private int numAnchors = 0;
    private int numEndBumpers = 0;
    private int numEdgeConnectors = 0;
    private int numTrackSegments = 0;
    private int numLevelXings = 0;
    private int numLayoutSlips = 0;
    private int numLayoutTurnouts = 0;
    private int numLayoutTurntables = 0;
    private int numShapes = 0;

    private LayoutEditorFindItems finder = new LayoutEditorFindItems(this);

    @Nonnull
    public LayoutEditorFindItems getFinder() {
        return finder;
    }

    private Color mainlineTrackColor = Color.DARK_GRAY;
    private Color sidelineTrackColor = Color.DARK_GRAY;
    public Color defaultTrackColor = Color.DARK_GRAY;
    private Color defaultOccupiedTrackColor = Color.red;
    private Color defaultAlternativeTrackColor = Color.white;
    private Color defaultTextColor = Color.black;

    private String layoutName = "";
    private boolean animatingLayout = true;
    private boolean showHelpBar = true;
    private boolean drawGrid = true;

    private boolean snapToGridOnAdd = false;
    private boolean snapToGridOnMove = false;
    private boolean snapToGridInvert = false;

    private boolean antialiasingOn = false;
    private boolean drawLayoutTracksLabel = false;
    private boolean highlightSelectedBlockFlag = false;

    private boolean turnoutCirclesWithoutEditMode = false;
    private boolean tooltipsWithoutEditMode = false;
    private boolean tooltipsInEditMode = true;

    // turnout size parameters - saved with panel
    private double turnoutBX = LayoutTurnout.turnoutBXDefault; // RH, LH, WYE
    private double turnoutCX = LayoutTurnout.turnoutCXDefault;
    private double turnoutWid = LayoutTurnout.turnoutWidDefault;
    private double xOverLong = LayoutTurnout.xOverLongDefault; // DOUBLE_XOVER, RH_XOVER, LH_XOVER
    private double xOverHWid = LayoutTurnout.xOverHWidDefault;
    private double xOverShort = LayoutTurnout.xOverShortDefault;
    private boolean useDirectTurnoutControl = false; // Uses Left click for closing points, Right click for throwing.

    // saved state of options when panel was loaded or created
    private boolean savedEditMode = true;
    private boolean savedPositionable = true;
    private boolean savedControlLayout = true;
    private boolean savedAnimatingLayout = true;
    private boolean savedShowHelpBar = true;

    // zoom
    private double minZoom = 0.25;
    private final double maxZoom = 8.0;

    // A hash to store string -> KeyEvent constants, used to set keyboard shortcuts per locale
    private HashMap<String, Integer> stringsToVTCodes = new HashMap<>();

    /*==============*\
    |* Toolbar side *|
    \*==============*/
    private enum ToolBarSide {
        eTOP("top"),
        eLEFT("left"),
        eBOTTOM("bottom"),
        eRIGHT("right"),
        eFLOAT("float");

        private final String name;
        private static final Map<String, ToolBarSide> ENUM_MAP;

        ToolBarSide(String name) {
            this.name = name;
        }

        // Build an immutable map of String name to enum pairs.
        static {
            Map<String, ToolBarSide> map = new ConcurrentHashMap<>();

            for (ToolBarSide instance : ToolBarSide.values()) {
                map.put(instance.getName(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static ToolBarSide getName(@CheckForNull String name) {
            return ENUM_MAP.get(name);
        }

        public String getName() {
            return name;
        }
    }

    private ToolBarSide toolBarSide = ToolBarSide.eTOP;

    public LayoutEditor() {
        this("My Layout");
    }

    public LayoutEditor(@Nonnull String name) {
        super(name);
        setSaveSize(true);
        layoutName = name;

        editorUseOldLocSize = InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize();

        // initialise keycode map
        initStringsToVTCodes();

        setupToolBar();
        setupMenuBar();

        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("SansSerif", Font.PLAIN, 12),
                Color.black, new Color(215, 225, 255), Color.black));

        // setup help bar
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

            case SystemType.WINDOWS:
            case SystemType.LINUX: {
                helpText3 = Bundle.getMessage("Help3Win");
                break;
            }

            default:
                helpText3 = Bundle.getMessage("Help3");
        }

        JTextArea helpTextArea3 = new JTextArea(helpText3);
        helpBar.add(helpTextArea3);

        // set to full screen
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        gContext.setWindowWidth(screenDim.width - 20);
        gContext.setWindowHeight(screenDim.height - 120);

        // Let Editor make target, and use this frame
        super.setTargetPanel(null, null);
        super.setTargetPanelSize(gContext.getWindowWidth(), gContext.getWindowHeight());
        setSize(screenDim.width, screenDim.height);

        // register the resulting panel for later configuration
        InstanceManager.getOptionalDefault(ConfigureManager.class)
                .ifPresent(cm -> cm.registerUser(this));

        // confirm that panel hasn't already been loaded
        if (!this.equals(InstanceManager.getDefault(EditorManager.class).get(name))) {
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
        }
        setFocusable(true);
        addKeyListener(this);
        resetDirty();

        // establish link to LayoutEditor Tools
        auxTools = getLEAuxTools();

        SwingUtilities.invokeLater(() -> {
            // initialize preferences
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                String windowFrameRef = getWindowFrameRef();

                Object prefsProp = prefsMgr.getProperty(windowFrameRef, "toolBarSide");
                // log.debug("{}.toolBarSide is {}", windowFrameRef, prefsProp);
                if (prefsProp != null) {
                    ToolBarSide newToolBarSide = ToolBarSide.getName((String) prefsProp);
                    setToolBarSide(newToolBarSide);
                }

                // Note: since prefs default to false and we want wide to be the default
                // we invert it and save it as thin
                boolean prefsToolBarIsWide = prefsMgr.getSimplePreferenceState(windowFrameRef + ".toolBarThin");

                log.debug("{}.toolBarThin is {}", windowFrameRef, prefsProp);
                setToolBarWide(prefsToolBarIsWide);

                boolean prefsShowHelpBar = prefsMgr.getSimplePreferenceState(windowFrameRef + ".showHelpBar");
                // log.debug("{}.showHelpBar is {}", windowFrameRef, prefsShowHelpBar);

                setShowHelpBar(prefsShowHelpBar);

                boolean prefsAntialiasingOn = prefsMgr.getSimplePreferenceState(windowFrameRef + ".antialiasingOn");
                // log.debug("{}.antialiasingOn is {}", windowFrameRef, prefsAntialiasingOn);

                setAntialiasingOn(prefsAntialiasingOn);

                boolean prefsDrawLayoutTracksLabel = prefsMgr.getSimplePreferenceState(windowFrameRef + ".drawLayoutTracksLabel");
                // log.debug("{}.drawLayoutTracksLabel is {}", windowFrameRef, prefsDrawLayoutTracksLabel);
                setDrawLayoutTracksLabel(prefsDrawLayoutTracksLabel);

                boolean prefsHighlightSelectedBlockFlag
                        = prefsMgr.getSimplePreferenceState(windowFrameRef + ".highlightSelectedBlock");
                // log.debug("{}.highlightSelectedBlock is {}", windowFrameRef, prefsHighlightSelectedBlockFlag);

                setHighlightSelectedBlock(prefsHighlightSelectedBlockFlag);
            }); // InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr)

            // make sure that the layoutEditorComponent is in the _targetPanel components
            List<Component> componentList = Arrays.asList(_targetPanel.getComponents());
            if (!componentList.contains(layoutEditorComponent)) {
                try {
                    _targetPanel.remove(layoutEditorComponent);
                    _targetPanel.add(layoutEditorComponent, Integer.valueOf(3));
                    _targetPanel.moveToFront(layoutEditorComponent);
                } catch (Exception e) {
                    log.warn("paintTargetPanelBefore: ", e);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")  // getMenuShortcutKeyMask()
    private void setupMenuBar() {
        // initialize menu bar
        JMenuBar menuBar = new JMenuBar();

        // set up File menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuFileMnemonic")));
        menuBar.add(fileMenu);
        StoreXmlUserAction store = new StoreXmlUserAction(Bundle.getMessage("FileMenuItemStore"));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        store.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("MenuItemStoreAccelerator")), primary_modifier));
        fileMenu.add(store);
        fileMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent event) -> {
            if (deletePanel()) {
                dispose();
            }
        });
        setJMenuBar(menuBar);

        // setup Options menu
        setupOptionMenu(menuBar);

        // setup Tools menu
        setupToolsMenu(menuBar);

        // setup Zoom menu
        setupZoomMenu(menuBar);

        // setup marker menu
        setupMarkerMenu(menuBar);

        // Setup Dispatcher window
        setupDispatcherMenu(menuBar);

        // setup Help menu
        addHelpMenu("package.jmri.jmrit.display.LayoutEditor", true);
    }

    @Override
    public void newPanelDefaults() {
        getLayoutTrackDrawingOptions().setMainRailWidth(2);
        getLayoutTrackDrawingOptions().setSideRailWidth(1);
        setBackgroundColor(defaultBackgroundColor);
        JmriColorChooser.addRecentColor(defaultTrackColor);
        JmriColorChooser.addRecentColor(defaultOccupiedTrackColor);
        JmriColorChooser.addRecentColor(defaultAlternativeTrackColor);
        JmriColorChooser.addRecentColor(defaultBackgroundColor);
        JmriColorChooser.addRecentColor(defaultTextColor);
    }

    private final LayoutEditorComponent layoutEditorComponent = new LayoutEditorComponent(this);

    private void setupToolBar() {
        // Initial setup for both horizontal and vertical
        Container contentPane = getContentPane();

        // remove these (if present) so we can add them back (without duplicates)
        if (editToolBarContainerPanel != null) {
            editToolBarContainerPanel.setVisible(false);
            contentPane.remove(editToolBarContainerPanel);
        }

        if (helpBarPanel != null) {
            contentPane.remove(helpBarPanel);
        }

        deletefloatingEditToolBoxFrame();
        if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
            createfloatingEditToolBoxFrame();
            createFloatingHelpPanel();
            return;
        }

        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        boolean toolBarIsVertical = (toolBarSide.equals(ToolBarSide.eRIGHT) || toolBarSide.equals(ToolBarSide.eLEFT));
        if (toolBarIsVertical) {
            leToolBarPanel = new LayoutEditorVerticalToolBarPanel(this);
            editToolBarScrollPane = new JScrollPane(leToolBarPanel);
            toolbarWidth = editToolBarScrollPane.getPreferredSize().width;
            toolbarHeight = screenDim.height;
        } else {
            leToolBarPanel = new LayoutEditorHorizontalToolBarPanel(this);
            editToolBarScrollPane = new JScrollPane(leToolBarPanel);
            toolbarWidth = screenDim.width;
            toolbarHeight = editToolBarScrollPane.getPreferredSize().height;
        }

        editToolBarContainerPanel = new JPanel();
        editToolBarContainerPanel.setLayout(new BoxLayout(editToolBarContainerPanel, BoxLayout.PAGE_AXIS));
        editToolBarContainerPanel.add(editToolBarScrollPane);

        // setup notification for when horizontal scrollbar changes visibility
        // editToolBarScroll.getViewport().addChangeListener(e -> {
        // log.warn("scrollbars visible: " + editToolBarScroll.getHorizontalScrollBar().isVisible());
        //});
        editToolBarContainerPanel.setMinimumSize(new Dimension(toolbarWidth, toolbarHeight));
        editToolBarContainerPanel.setPreferredSize(new Dimension(toolbarWidth, toolbarHeight));

        helpBarPanel = new JPanel();
        helpBarPanel.add(helpBar);

        for (Component c : helpBar.getComponents()) {
            if (c instanceof JTextArea) {
                JTextArea j = (JTextArea) c;
                j.setSize(new Dimension(toolbarWidth, j.getSize().height));
                j.setLineWrap(toolBarIsVertical);
                j.setWrapStyleWord(toolBarIsVertical);
            }
        }
        contentPane.setLayout(new BoxLayout(contentPane, toolBarIsVertical ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS));

        switch (toolBarSide) {
            case eTOP:
            case eLEFT:
                contentPane.add(editToolBarContainerPanel, 0);
                break;

            case eBOTTOM:
            case eRIGHT:
                contentPane.add(editToolBarContainerPanel);
                break;

            default:
                // fall through
                break;
        }

        if (toolBarIsVertical) {
            editToolBarContainerPanel.add(helpBarPanel);
        } else {
            contentPane.add(helpBarPanel);
        }
        helpBarPanel.setVisible(isEditable() && getShowHelpBar());
        editToolBarContainerPanel.setVisible(isEditable());
    }

    private void createfloatingEditToolBoxFrame() {
        if (isEditable() && floatingEditToolBoxFrame == null) {
            // Create a scroll pane to hold the window content.
            leToolBarPanel = new LayoutEditorFloatingToolBarPanel(this);
            floatingEditContentScrollPane = new JScrollPane(leToolBarPanel);
            floatingEditContentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            floatingEditContentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            // Create the window and add the toolbox content
            floatingEditToolBoxFrame = new JmriJFrame(Bundle.getMessage("ToolBox", getLayoutName()));
            floatingEditToolBoxFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            floatingEditToolBoxFrame.setContentPane(floatingEditContentScrollPane);
            floatingEditToolBoxFrame.pack();
            floatingEditToolBoxFrame.setAlwaysOnTop(true);
            floatingEditToolBoxFrame.setVisible(true);
        }
    }

    private void deletefloatingEditToolBoxFrame() {
        if (floatingEditContentScrollPane != null) {
            floatingEditContentScrollPane.removeAll();
            floatingEditContentScrollPane = null;
        }
        if (floatingEditToolBoxFrame != null) {
            floatingEditToolBoxFrame.dispose();
            floatingEditToolBoxFrame = null;
        }
    }

    private void createFloatingHelpPanel() {

        if (leToolBarPanel instanceof LayoutEditorFloatingToolBarPanel) {
            LayoutEditorFloatingToolBarPanel leftbp = (LayoutEditorFloatingToolBarPanel) leToolBarPanel;
            floatEditHelpPanel = new JPanel();
            leToolBarPanel.add(floatEditHelpPanel);

            // Notice: End tree structure indenting
            // Force the help panel width to the same as the tabs section
            int tabSectionWidth = (int) leftbp.getPreferredSize().getWidth();

            // Change the textarea settings
            for (Component c : helpBar.getComponents()) {
                if (c instanceof JTextArea) {
                    JTextArea j = (JTextArea) c;
                    j.setSize(new Dimension(tabSectionWidth, j.getSize().height));
                    j.setLineWrap(true);
                    j.setWrapStyleWord(true);
                }
            }

            // Change the width of the help panel section
            floatEditHelpPanel.setMaximumSize(new Dimension(tabSectionWidth, Integer.MAX_VALUE));
            floatEditHelpPanel.add(helpBar);
            floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
        }
    }

    @Override
    public void init(String name) {
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

        scrollNoneMenuItem.setSelected(_scrollState == Editor.SCROLL_NONE);
        scrollBothMenuItem.setSelected(_scrollState == Editor.SCROLL_BOTH);
        scrollHorizontalMenuItem.setSelected(_scrollState == Editor.SCROLL_HORIZONTAL);
        scrollVerticalMenuItem.setSelected(_scrollState == Editor.SCROLL_VERTICAL);
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    @Override
    public void targetWindowClosingEvent(WindowEvent e) {
        boolean save = (isDirty() || (savedEditMode != isEditable())
                || (savedPositionable != allPositionable())
                || (savedControlLayout != allControlling())
                || (savedAnimatingLayout != isAnimating())
                || (savedShowHelpBar != getShowHelpBar()));

        log.trace("Temp fix to disable CI errors: save = {}", save);
        targetWindowClosing();
    }

    /**
     * Set up NamedBeanComboBox
     *
     * @param inComboBox     the NamedBeanComboBox to set up
     * @param inValidateMode true to validate typed inputs; false otherwise
     * @param inEnable       boolean to enable / disable the NamedBeanComboBox
     * @param inEditable     boolean to make the NamedBeanComboBox editable
     */
    public static void setupComboBox(@Nonnull NamedBeanComboBox<?> inComboBox, boolean inValidateMode, boolean inEnable, boolean inEditable) {
        log.debug("LE setupComboBox called");
        assert inComboBox != null;

        inComboBox.setEnabled(inEnable);
        inComboBox.setEditable(inEditable);
        inComboBox.setValidatingInput(inValidateMode);
        inComboBox.setSelectedIndex(-1);

        // This has to be set before calling setupComboBoxMaxRows
        // (otherwise if inFirstBlank then the  number of rows will be wrong)
        inComboBox.setAllowNull(!inValidateMode);

        // set the max number of rows that will fit onscreen
        JComboBoxUtil.setupComboBoxMaxRows(inComboBox);

        inComboBox.setSelectedIndex(-1);
    }

    /**
     * Grabs a subset of the possible KeyEvent constants and puts them into a
     * hash for fast lookups later. These lookups are used to enable bundles to
     * specify keyboard shortcuts on a per-locale basis.
     */
    private void initStringsToVTCodes() {
        Field[] fields = KeyEvent.class
                .getFields();

        for (Field field : fields) {
            String name = field.getName();

            if (name.startsWith("VK")) {
                int code = 0;
                try {
                    code = field.getInt(null);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    // exceptions make me throw up...
                }

                String key = name.substring(3);

                // log.debug("VTCode[{}]:'{}'", key, code);
                stringsToVTCodes.put(key, code);
            }
        }
    }

    /**
     * The Java run times for 11 and 12 running on macOS have a bug that causes double events for
     * JCheckBoxMenuItem when invoked by an accelerator key combination.
     * <p>
     * The java.version property is parsed to determine the run time version.  If the event occurs
     * on macOS and Java 11 or 12 and a modifier key was active, true is returned.  The five affected
     * action events will drop the event and process the second occurrence.
     * @aparam event The action event.
     * @return true if the event is affected, otherwise return false.
     */
    private boolean fixMacBugOn11(ActionEvent event) {
        boolean result = false;
        if (SystemType.isMacOSX()) {
            if (event.getModifiers() != 0) {
                // MacOSX and modifier key, test Java version
                String version = System.getProperty("java.version");
                if (version.startsWith("1.")) {
                    version = version.substring(2, 3);
                } else {
                    int dot = version.indexOf(".");
                    if (dot != -1) {
                        version = version.substring(0, dot);
                    }
                }
                int vers = Integer.parseInt(version);
                result = (vers == 11 || vers == 12);
            }
        }
        return result;
     }

    /**
     * Set up the Option menu.
     *
     * @param menuBar to add the option menu to
     * @return option menu that was added
     */
    @SuppressWarnings("deprecation")  // getMenuShortcutKeyMask()
    private JMenu setupOptionMenu(@Nonnull JMenuBar menuBar) {
        assert menuBar != null;

        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));

        optionMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("OptionsMnemonic")));
        menuBar.add(optionMenu);

        //
        //  edit mode
        //
        editModeCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EditMode"));
        optionMenu.add(editModeCheckBoxMenuItem);
        editModeCheckBoxMenuItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("EditModeMnemonic")));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        editModeCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("EditModeAccelerator")), primary_modifier));
        editModeCheckBoxMenuItem.addActionListener((ActionEvent event) -> {

            if (fixMacBugOn11(event)) {
                editModeCheckBoxMenuItem.setSelected(!editModeCheckBoxMenuItem.isSelected());
                return;
            }

            setAllEditable(editModeCheckBoxMenuItem.isSelected());

            // show/hide the help bar
            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                if (floatEditHelpPanel != null) {
                    floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
                }
            } else {
                helpBarPanel.setVisible(isEditable() && getShowHelpBar());
            }

            if (isEditable()) {
                setAllShowToolTip(tooltipsInEditMode);

                // redo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    if (!highlightBlockInComboBox(leToolBarPanel.blockIDComboBox)) {
                        highlightBlockInComboBox(leToolBarPanel.blockContentsComboBox);
                    }
                }
            } else {
                setAllShowToolTip(tooltipsWithoutEditMode);

                // undo using the "Extra" color to highlight the selected block
                if (highlightSelectedBlockFlag) {
                    highlightBlock(null);
                }
            }
            awaitingIconChange = false;
        });
        editModeCheckBoxMenuItem.setSelected(isEditable());

        //
        // toolbar
        //
        JMenu toolBarMenu = new JMenu(Bundle.getMessage("ToolBar")); // used for ToolBar SubMenu
        optionMenu.add(toolBarMenu);

        JMenu toolBarSideMenu = new JMenu(Bundle.getMessage("ToolBarSide"));
        ButtonGroup toolBarSideGroup = new ButtonGroup();

        //
        // create toolbar side menu items: (top, left, bottom, right)
        //
        toolBarSideTopButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideTop"));
        toolBarSideTopButton.addActionListener((ActionEvent event) -> setToolBarSide(ToolBarSide.eTOP));
        toolBarSideTopButton.setSelected(toolBarSide.equals(ToolBarSide.eTOP));
        toolBarSideMenu.add(toolBarSideTopButton);
        toolBarSideGroup.add(toolBarSideTopButton);

        toolBarSideLeftButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideLeft"));
        toolBarSideLeftButton.addActionListener((ActionEvent event) -> setToolBarSide(ToolBarSide.eLEFT));
        toolBarSideLeftButton.setSelected(toolBarSide.equals(ToolBarSide.eLEFT));
        toolBarSideMenu.add(toolBarSideLeftButton);
        toolBarSideGroup.add(toolBarSideLeftButton);

        toolBarSideBottomButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideBottom"));
        toolBarSideBottomButton.addActionListener((ActionEvent event) -> setToolBarSide(ToolBarSide.eBOTTOM));
        toolBarSideBottomButton.setSelected(toolBarSide.equals(ToolBarSide.eBOTTOM));
        toolBarSideMenu.add(toolBarSideBottomButton);
        toolBarSideGroup.add(toolBarSideBottomButton);

        toolBarSideRightButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideRight"));
        toolBarSideRightButton.addActionListener((ActionEvent event) -> setToolBarSide(ToolBarSide.eRIGHT));
        toolBarSideRightButton.setSelected(toolBarSide.equals(ToolBarSide.eRIGHT));
        toolBarSideMenu.add(toolBarSideRightButton);
        toolBarSideGroup.add(toolBarSideRightButton);

        toolBarSideFloatButton = new JRadioButtonMenuItem(Bundle.getMessage("ToolBarSideFloat"));
        toolBarSideFloatButton.addActionListener((ActionEvent event) -> setToolBarSide(ToolBarSide.eFLOAT));
        toolBarSideFloatButton.setSelected(toolBarSide.equals(ToolBarSide.eFLOAT));
        toolBarSideMenu.add(toolBarSideFloatButton);
        toolBarSideGroup.add(toolBarSideFloatButton);

        toolBarMenu.add(toolBarSideMenu);

        //
        // toolbar wide menu
        //
        toolBarMenu.add(wideToolBarCheckBoxMenuItem);
        wideToolBarCheckBoxMenuItem.addActionListener((ActionEvent event) -> setToolBarWide(wideToolBarCheckBoxMenuItem.isSelected()));
        wideToolBarCheckBoxMenuItem.setSelected(leToolBarPanel.toolBarIsWide);
        wideToolBarCheckBoxMenuItem.setEnabled(toolBarSide.equals(ToolBarSide.eTOP) || toolBarSide.equals(ToolBarSide.eBOTTOM));

        //
        // Scroll Bars
        //
        scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable")); // used for ScrollBarsSubMenu
        optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollBothMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
        scrollGroup.add(scrollBothMenuItem);
        scrollMenu.add(scrollBothMenuItem);
        scrollBothMenuItem.setSelected(_scrollState == Editor.SCROLL_BOTH);
        scrollBothMenuItem.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_BOTH;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollNoneMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
        scrollGroup.add(scrollNoneMenuItem);
        scrollMenu.add(scrollNoneMenuItem);
        scrollNoneMenuItem.setSelected(_scrollState == Editor.SCROLL_NONE);
        scrollNoneMenuItem.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_NONE;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollHorizontalMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
        scrollGroup.add(scrollHorizontalMenuItem);
        scrollMenu.add(scrollHorizontalMenuItem);
        scrollHorizontalMenuItem.setSelected(_scrollState == Editor.SCROLL_HORIZONTAL);
        scrollHorizontalMenuItem.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_HORIZONTAL;
            setScroll(_scrollState);
            redrawPanel();
        });
        scrollVerticalMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));
        scrollGroup.add(scrollVerticalMenuItem);
        scrollMenu.add(scrollVerticalMenuItem);
        scrollVerticalMenuItem.setSelected(_scrollState == Editor.SCROLL_VERTICAL);
        scrollVerticalMenuItem.addActionListener((ActionEvent event) -> {
            _scrollState = Editor.SCROLL_VERTICAL;
            setScroll(_scrollState);
            redrawPanel();
        });

        //
        // Tooltips
        //
        tooltipMenu = new JMenu(Bundle.getMessage("TooltipSubMenu"));
        optionMenu.add(tooltipMenu);
        ButtonGroup tooltipGroup = new ButtonGroup();
        tooltipNoneMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("TooltipNone"));
        tooltipGroup.add(tooltipNoneMenuItem);
        tooltipMenu.add(tooltipNoneMenuItem);
        tooltipNoneMenuItem.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipNoneMenuItem.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = false;
            setAllShowToolTip(false);
        });
        tooltipAlwaysMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("TooltipAlways"));
        tooltipGroup.add(tooltipAlwaysMenuItem);
        tooltipMenu.add(tooltipAlwaysMenuItem);
        tooltipAlwaysMenuItem.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipAlwaysMenuItem.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = true;
            setAllShowToolTip(true);
        });
        tooltipInEditMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("TooltipEdit"));
        tooltipGroup.add(tooltipInEditMenuItem);
        tooltipMenu.add(tooltipInEditMenuItem);
        tooltipInEditMenuItem.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
        tooltipInEditMenuItem.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = true;
            tooltipsWithoutEditMode = false;
            setAllShowToolTip(isEditable());
        });
        tooltipNotInEditMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("TooltipNotEdit"));
        tooltipGroup.add(tooltipNotInEditMenuItem);
        tooltipMenu.add(tooltipNotInEditMenuItem);
        tooltipNotInEditMenuItem.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        tooltipNotInEditMenuItem.addActionListener((ActionEvent event) -> {
            tooltipsInEditMode = false;
            tooltipsWithoutEditMode = true;
            setAllShowToolTip(!isEditable());
        });

        //
        // show edit help
        //
        showHelpCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowEditHelp"));
        optionMenu.add(showHelpCheckBoxMenuItem);
        showHelpCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            boolean newShowHelpBar = showHelpCheckBoxMenuItem.isSelected();
            setShowHelpBar(newShowHelpBar);
        });
        showHelpCheckBoxMenuItem.setSelected(getShowHelpBar());

        //
        // Allow Repositioning
        //
        positionableCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowRepositioning"));
        optionMenu.add(positionableCheckBoxMenuItem);
        positionableCheckBoxMenuItem.addActionListener((ActionEvent event) -> setAllPositionable(positionableCheckBoxMenuItem.isSelected()));
        positionableCheckBoxMenuItem.setSelected(allPositionable());

        //
        // Allow Layout Control
        //
        controlCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowLayoutControl"));
        optionMenu.add(controlCheckBoxMenuItem);
        controlCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAllControlling(controlCheckBoxMenuItem.isSelected());
            redrawPanel();
        });
        controlCheckBoxMenuItem.setSelected(allControlling());

        //
        // use direct turnout control
        //
        useDirectTurnoutControlCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("UseDirectTurnoutControl")); // NOI18N
        optionMenu.add(useDirectTurnoutControlCheckBoxMenuItem);
        useDirectTurnoutControlCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setDirectTurnoutControl(useDirectTurnoutControlCheckBoxMenuItem.isSelected());
        });
        useDirectTurnoutControlCheckBoxMenuItem.setSelected(useDirectTurnoutControl);

        //
        // antialiasing
        //
        antialiasingOnCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AntialiasingOn"));
        optionMenu.add(antialiasingOnCheckBoxMenuItem);
        antialiasingOnCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAntialiasingOn(antialiasingOnCheckBoxMenuItem.isSelected());
            redrawPanel();
        });
        antialiasingOnCheckBoxMenuItem.setSelected(antialiasingOn);

        //
        // drawLayoutTracksLabel
        //
        drawLayoutTracksLabelCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("DrawLayoutTracksLabel"));
        optionMenu.add(drawLayoutTracksLabelCheckBoxMenuItem);
        drawLayoutTracksLabelCheckBoxMenuItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("DrawLayoutTracksMnemonic")));
        drawLayoutTracksLabelCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("DrawLayoutTracksAccelerator")), primary_modifier));
        drawLayoutTracksLabelCheckBoxMenuItem.addActionListener((ActionEvent event) -> {

            if (fixMacBugOn11(event)) {
                drawLayoutTracksLabelCheckBoxMenuItem.setSelected(!drawLayoutTracksLabelCheckBoxMenuItem.isSelected());
                return;
            }

            setDrawLayoutTracksLabel(drawLayoutTracksLabelCheckBoxMenuItem.isSelected());
            redrawPanel();
        });
        drawLayoutTracksLabelCheckBoxMenuItem.setSelected(drawLayoutTracksLabel);

        //
        // edit title
        //
        optionMenu.addSeparator();
        JMenuItem titleItem = new JMenuItem(Bundle.getMessage("EditTitle") + "...");
        optionMenu.add(titleItem);
        titleItem.addActionListener((ActionEvent event) -> {
            // prompt for name
            String newName = (String) JOptionPane.showInputDialog(getTargetFrame(),
                    Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterTitle")),
                    Bundle.getMessage("EditTitleMessageTitle"),
                    JOptionPane.PLAIN_MESSAGE, null, null, getLayoutName());

            if (newName != null) {
                if (!newName.equals(getLayoutName())) {
                    if (InstanceManager.getDefault(EditorManager.class).contains(newName)) {
                        JOptionPane.showMessageDialog(
                                null, Bundle.getMessage("CanNotRename"), Bundle.getMessage("PanelExist"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        setTitle(newName);
                        setLayoutName(newName);
                        getLayoutTrackDrawingOptions().setName(newName);
                        setDirty();

                        if (toolBarSide.equals(ToolBarSide.eFLOAT) && isEditable()) {
                            // Rebuild the toolbox after a name change.
                            deletefloatingEditToolBoxFrame();
                            createfloatingEditToolBoxFrame();
                            createFloatingHelpPanel();
                        }
                    }
                }
            }
        });

        //
        // set background color
        //
        JMenuItem backgroundColorMenuItem = new JMenuItem(Bundle.getMessage("SetBackgroundColor", "..."));
        optionMenu.add(backgroundColorMenuItem);
        backgroundColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("SetBackgroundColor", ""),
                    defaultBackgroundColor);
            if (desiredColor != null && !defaultBackgroundColor.equals(desiredColor)) {
                defaultBackgroundColor = desiredColor;
                setBackgroundColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });

        //
        // set default text color
        //
        JMenuItem textColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultTextColor", "..."));
        optionMenu.add(textColorMenuItem);
        textColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("DefaultTextColor", ""),
                    defaultTextColor);
            if (desiredColor != null && !defaultTextColor.equals(desiredColor)) {
                setDefaultTextColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });

        if (editorUseOldLocSize) {
            //
            //  save location and size
            //
            JMenuItem locationItem = new JMenuItem(Bundle.getMessage("SetLocation"));
            optionMenu.add(locationItem);
            locationItem.addActionListener((ActionEvent event) -> {
                setCurrentPositionAndSize();
                log.debug("Bounds:{}, {}, {}, {}, {}, {}",
                        gContext.getUpperLeftX(), gContext.getUpperLeftY(),
                        gContext.getWindowWidth(), gContext.getWindowHeight(),
                        gContext.getLayoutWidth(), gContext.getLayoutHeight());
            });
        }

        //
        // Add Options
        //
        JMenu optionsAddMenu = new JMenu(Bundle.getMessage("AddMenuTitle"));
        optionMenu.add(optionsAddMenu);

        // add background image
        JMenuItem backgroundItem = new JMenuItem(Bundle.getMessage("AddBackground") + "...");
        optionsAddMenu.add(backgroundItem);
        backgroundItem.addActionListener((ActionEvent event) -> {
            addBackground();
            // note: panel resized in addBackground
            setDirty();
            redrawPanel();
        });

        // add fast clock
        JMenuItem clockItem = new JMenuItem(Bundle.getMessage("AddItem", Bundle.getMessage("FastClock")));
        optionsAddMenu.add(clockItem);
        clockItem.addActionListener((ActionEvent event) -> {
            AnalogClock2Display c = addClock();
            unionToPanelBounds(c.getBounds());
            setDirty();
            redrawPanel();
        });

        // add turntable
        JMenuItem turntableItem = new JMenuItem(Bundle.getMessage("AddTurntable"));
        optionsAddMenu.add(turntableItem);
        turntableItem.addActionListener((ActionEvent event) -> {
            Point2D pt = windowCenter();
            if (selectionActive) {
                pt = MathUtil.midPoint(getSelectionRect());
            }
            addTurntable(pt);
            // note: panel resized in addTurntable
            setDirty();
            redrawPanel();
        });

        // add reporter
        JMenuItem reporterItem = new JMenuItem(Bundle.getMessage("AddReporter") + "...");
        optionsAddMenu.add(reporterItem);
        reporterItem.addActionListener((ActionEvent event) -> {
            Point2D pt = windowCenter();
            if (selectionActive) {
                pt = MathUtil.midPoint(getSelectionRect());
            }
            EnterReporterDialog d = new EnterReporterDialog(this);
            d.enterReporter((int) pt.getX(), (int) pt.getY());
            // note: panel resized in enterReporter
            setDirty();
            redrawPanel();
        });

        //
        // location coordinates format menu
        //
        JMenu locationMenu = new JMenu(Bundle.getMessage("LocationMenuTitle")); // used for location format SubMenu
        optionMenu.add(locationMenu);

        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
            String windowFrameRef = getWindowFrameRef();
            Object prefsProp = prefsMgr.getProperty(windowFrameRef, "LocationFormat");
            // log.debug("{}.LocationFormat is {}", windowFrameRef, prefsProp);
            if (prefsProp != null) {
                getLayoutEditorToolBarPanel().setLocationFormat(LocationFormat.valueOf((String) prefsProp));
            }
        });

        // pixels (jmri classic)
        locationMenu.add(pixelsCheckBoxMenuItem);
        pixelsCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            getLayoutEditorToolBarPanel().setLocationFormat(LocationFormat.ePIXELS);
            selectLocationFormatCheckBoxMenuItem();
            redrawPanel();
        });

        // metric cm's
        locationMenu.add(metricCMCheckBoxMenuItem);
        metricCMCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            getLayoutEditorToolBarPanel().setLocationFormat(LocationFormat.eMETRIC_CM);
            selectLocationFormatCheckBoxMenuItem();
            redrawPanel();
        });

        // english feet/inches/16th's
        locationMenu.add(englishFeetInchesCheckBoxMenuItem);
        englishFeetInchesCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            getLayoutEditorToolBarPanel().setLocationFormat(LocationFormat.eENGLISH_FEET_INCHES);
            selectLocationFormatCheckBoxMenuItem();
            redrawPanel();
        });
        selectLocationFormatCheckBoxMenuItem();

        //
        // grid menu
        //
        JMenu gridMenu = new JMenu(Bundle.getMessage("GridMenuTitle")); // used for Grid SubMenu
        optionMenu.add(gridMenu);

        // show grid
        showGridCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowEditGrid"));
        showGridCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("ShowEditGridAccelerator")), primary_modifier));
        gridMenu.add(showGridCheckBoxMenuItem);
        showGridCheckBoxMenuItem.addActionListener((ActionEvent event) -> {

            if (fixMacBugOn11(event)) {
                showGridCheckBoxMenuItem.setSelected(!showGridCheckBoxMenuItem.isSelected());
                return;
            }

            drawGrid = showGridCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        showGridCheckBoxMenuItem.setSelected(getDrawGrid());

        // snap to grid on add
        snapToGridOnAddCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnAdd"));
        snapToGridOnAddCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnAddAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        gridMenu.add(snapToGridOnAddCheckBoxMenuItem);
        snapToGridOnAddCheckBoxMenuItem.addActionListener((ActionEvent event) -> {

            if (fixMacBugOn11(event)) {
                snapToGridOnAddCheckBoxMenuItem.setSelected(!snapToGridOnAddCheckBoxMenuItem.isSelected());
                return;
            }

            snapToGridOnAdd = snapToGridOnAddCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        snapToGridOnAddCheckBoxMenuItem.setSelected(snapToGridOnAdd);

        // snap to grid on move
        snapToGridOnMoveCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnMove"));
        snapToGridOnMoveCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnMoveAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        gridMenu.add(snapToGridOnMoveCheckBoxMenuItem);
        snapToGridOnMoveCheckBoxMenuItem.addActionListener((ActionEvent event) -> {

            if (fixMacBugOn11(event)) {
                snapToGridOnMoveCheckBoxMenuItem.setSelected(!snapToGridOnMoveCheckBoxMenuItem.isSelected());
                return;
            }

            snapToGridOnMove = snapToGridOnMoveCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        snapToGridOnMoveCheckBoxMenuItem.setSelected(snapToGridOnMove);

        // specify grid square size
        JMenuItem gridSizeItem = new JMenuItem(Bundle.getMessage("SetGridSizes") + "...");
        gridMenu.add(gridSizeItem);
        gridSizeItem.addActionListener((ActionEvent event) -> {
            EnterGridSizesDialog d = new EnterGridSizesDialog(this);
            d.enterGridSizes();
        });

        //
        // track menu
        //
        JMenu trackMenu = new JMenu(Bundle.getMessage("TrackMenuTitle"));
        optionMenu.add(trackMenu);

        // set track drawing options menu item
        JMenuItem jmi = new JMenuItem(Bundle.getMessage("SetTrackDrawingOptions"));
        trackMenu.add(jmi);
        jmi.setToolTipText(Bundle.getMessage("SetTrackDrawingOptionsToolTip"));
        jmi.addActionListener((ActionEvent event) -> {
            LayoutTrackDrawingOptionsDialog ltdod
                    = new LayoutTrackDrawingOptionsDialog(
                            this, true, getLayoutTrackDrawingOptions());
            ltdod.setVisible(true);
        });

        // track colors item menu item
        JMenu trkColourMenu = new JMenu(Bundle.getMessage("TrackColorSubMenu"));
        trackMenu.add(trkColourMenu);

        JMenuItem trackColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultTrackColor"));
        trkColourMenu.add(trackColorMenuItem);
        trackColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("DefaultTrackColor"),
                    defaultTrackColor);
            if (desiredColor != null && !defaultTrackColor.equals(desiredColor)) {
                setDefaultTrackColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });

        JMenuItem trackOccupiedColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultOccupiedTrackColor"));
        trkColourMenu.add(trackOccupiedColorMenuItem);
        trackOccupiedColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("DefaultOccupiedTrackColor"),
                    defaultOccupiedTrackColor);
            if (desiredColor != null && !defaultOccupiedTrackColor.equals(desiredColor)) {
                setDefaultOccupiedTrackColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });

        JMenuItem trackAlternativeColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultAlternativeTrackColor"));
        trkColourMenu.add(trackAlternativeColorMenuItem);
        trackAlternativeColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("DefaultAlternativeTrackColor"),
                    defaultAlternativeTrackColor);
            if (desiredColor != null && !defaultAlternativeTrackColor.equals(desiredColor)) {
                setDefaultAlternativeTrackColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });

        // Set All Tracks To Default Colors
        JMenuItem setAllTracksToDefaultColorsMenuItem = new JMenuItem(Bundle.getMessage("SetAllTracksToDefaultColors"));
        trkColourMenu.add(setAllTracksToDefaultColorsMenuItem);
        setAllTracksToDefaultColorsMenuItem.addActionListener((ActionEvent event) -> {
            if (setAllTracksToDefaultColors() > 0) {
                setDirty();
                redrawPanel();
            }
        });

        // Automatically Assign Blocks to Track
        autoAssignBlocksCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AutoAssignBlock"));
        trackMenu.add(autoAssignBlocksCheckBoxMenuItem);
        autoAssignBlocksCheckBoxMenuItem.addActionListener((ActionEvent event) -> autoAssignBlocks = autoAssignBlocksCheckBoxMenuItem.isSelected());
        autoAssignBlocksCheckBoxMenuItem.setSelected(autoAssignBlocks);

        // add hideTrackSegmentConstructionLines menu item
        hideTrackSegmentConstructionLinesCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("HideTrackConLines"));
        trackMenu.add(hideTrackSegmentConstructionLinesCheckBoxMenuItem);
        hideTrackSegmentConstructionLinesCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            int show = TrackSegmentView.SHOWCON;

            if (hideTrackSegmentConstructionLinesCheckBoxMenuItem.isSelected()) {
                show = TrackSegmentView.HIDECONALL;
            }

            for (TrackSegmentView tsv : getTrackSegmentViews()) {
                tsv.hideConstructionLines(show);
            }
            redrawPanel();
        });
        hideTrackSegmentConstructionLinesCheckBoxMenuItem.setSelected(autoAssignBlocks);

        //
        // add turnout options submenu
        //
        JMenu turnoutOptionsMenu = new JMenu(Bundle.getMessage("TurnoutOptions"));
        optionMenu.add(turnoutOptionsMenu);

        // animation item
        animationCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("AllowTurnoutAnimation"));
        turnoutOptionsMenu.add(animationCheckBoxMenuItem);
        animationCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            boolean mode = animationCheckBoxMenuItem.isSelected();
            setTurnoutAnimation(mode);
        });
        animationCheckBoxMenuItem.setSelected(true);

        // circle on Turnouts
        turnoutCirclesOnCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("TurnoutCirclesOn"));
        turnoutOptionsMenu.add(turnoutCirclesOnCheckBoxMenuItem);
        turnoutCirclesOnCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            turnoutCirclesWithoutEditMode = turnoutCirclesOnCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        turnoutCirclesOnCheckBoxMenuItem.setSelected(turnoutCirclesWithoutEditMode);

        // select turnout circle color
        JMenuItem turnoutCircleColorMenuItem = new JMenuItem(Bundle.getMessage("TurnoutCircleColor"));
        turnoutCircleColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("TurnoutCircleColor"),
                    turnoutCircleColor);
            if (desiredColor != null && !turnoutCircleColor.equals(desiredColor)) {
                setTurnoutCircleColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });
        turnoutOptionsMenu.add(turnoutCircleColorMenuItem);

        // select turnout circle thrown color
        JMenuItem turnoutCircleThrownColorMenuItem = new JMenuItem(Bundle.getMessage("TurnoutCircleThrownColor"));
        turnoutCircleThrownColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("TurnoutCircleThrownColor"),
                    turnoutCircleThrownColor);
            if (desiredColor != null && !turnoutCircleThrownColor.equals(desiredColor)) {
                setTurnoutCircleThrownColor(desiredColor);
                setDirty();
                redrawPanel();
            }
        });
        turnoutOptionsMenu.add(turnoutCircleThrownColorMenuItem);

        turnoutFillControlCirclesCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("TurnoutFillControlCircles"));
        turnoutOptionsMenu.add(turnoutFillControlCirclesCheckBoxMenuItem);
        turnoutFillControlCirclesCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            turnoutFillControlCircles = turnoutFillControlCirclesCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        turnoutFillControlCirclesCheckBoxMenuItem.setSelected(turnoutFillControlCircles);

        // select turnout circle size
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

        // add "enable drawing of unselected leg " menu item (helps when diverging angle is small)
        turnoutDrawUnselectedLegCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("TurnoutDrawUnselectedLeg"));
        turnoutOptionsMenu.add(turnoutDrawUnselectedLegCheckBoxMenuItem);
        turnoutDrawUnselectedLegCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            turnoutDrawUnselectedLeg = turnoutDrawUnselectedLegCheckBoxMenuItem.isSelected();
            redrawPanel();
        });
        turnoutDrawUnselectedLegCheckBoxMenuItem.setSelected(turnoutDrawUnselectedLeg);

        return optionMenu;
    }

    private void selectLocationFormatCheckBoxMenuItem() {
        pixelsCheckBoxMenuItem.setSelected(getLayoutEditorToolBarPanel().getLocationFormat() == LocationFormat.ePIXELS);
        metricCMCheckBoxMenuItem.setSelected(getLayoutEditorToolBarPanel().getLocationFormat() == LocationFormat.eMETRIC_CM);
        englishFeetInchesCheckBoxMenuItem.setSelected(getLayoutEditorToolBarPanel().getLocationFormat() == LocationFormat.eENGLISH_FEET_INCHES);
    }

    /*============================================*\
    |* LayoutTrackDrawingOptions accessor methods *|
    \*============================================*/
    private LayoutTrackDrawingOptions layoutTrackDrawingOptions = null;

    /**
     *
     * Getter Layout Track Drawing Options. since 4.15.6 split variable
     * defaultTrackColor and mainlineTrackColor/sidelineTrackColor <br>
     * blockDefaultColor, blockOccupiedColor and blockAlternativeColor added to
     * LayoutTrackDrawingOptions <br>
     *
     * @return LayoutTrackDrawingOptions object
     */
    @Nonnull
    public LayoutTrackDrawingOptions getLayoutTrackDrawingOptions() {
        if (layoutTrackDrawingOptions == null) {
            layoutTrackDrawingOptions = new LayoutTrackDrawingOptions(getLayoutName());
            // integrate LayoutEditor drawing options with previous drawing options
            layoutTrackDrawingOptions.setMainBlockLineWidth(gContext.getMainlineTrackWidth());
            layoutTrackDrawingOptions.setSideBlockLineWidth(gContext.getSidelineTrackWidth());
            layoutTrackDrawingOptions.setMainRailWidth(gContext.getMainlineTrackWidth());
            layoutTrackDrawingOptions.setSideRailWidth(gContext.getSidelineTrackWidth());
            layoutTrackDrawingOptions.setMainRailColor(mainlineTrackColor);
            layoutTrackDrawingOptions.setSideRailColor(sidelineTrackColor);
            layoutTrackDrawingOptions.setBlockDefaultColor(defaultTrackColor);
            layoutTrackDrawingOptions.setBlockOccupiedColor(defaultOccupiedTrackColor);
            layoutTrackDrawingOptions.setBlockAlternativeColor(defaultAlternativeTrackColor);
        }
        return layoutTrackDrawingOptions;
    }

    /**
     * since 4.15.6 split variable defaultTrackColor and
     * mainlineTrackColor/sidelineTrackColor
     *
     * @param ltdo LayoutTrackDrawingOptions object
     */
    public void setLayoutTrackDrawingOptions(LayoutTrackDrawingOptions ltdo) {
        layoutTrackDrawingOptions = ltdo;

        // copy main/side line block widths
        gContext.setMainlineBlockWidth(layoutTrackDrawingOptions.getMainBlockLineWidth());
        gContext.setSidelineBlockWidth(layoutTrackDrawingOptions.getSideBlockLineWidth());

        // copy main/side line track (rail) widths
        gContext.setMainlineTrackWidth(layoutTrackDrawingOptions.getMainRailWidth());
        gContext.setSidelineTrackWidth(layoutTrackDrawingOptions.getSideRailWidth());

        mainlineTrackColor = layoutTrackDrawingOptions.getMainRailColor();
        sidelineTrackColor = layoutTrackDrawingOptions.getSideRailColor();
        redrawPanel();
    }

    private JCheckBoxMenuItem skipTurnoutCheckBoxMenuItem = null;
    private AddEntryExitPairAction addEntryExitPairAction = null;

    /**
     * setup the Layout Editor Tools menu
     *
     * @param menuBar the menu bar to add the Tools menu to
     */
    private void setupToolsMenu(@Nonnull JMenuBar menuBar) {
        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));

        toolsMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuToolsMnemonic")));
        menuBar.add(toolsMenu);

        // setup checks menu
        getLEChecks().setupChecksMenu(toolsMenu);

        // assign blocks to selection
        assignBlockToSelectionMenuItem.setToolTipText(Bundle.getMessage("AssignBlockToSelectionToolTip"));
        toolsMenu.add(assignBlockToSelectionMenuItem);
        assignBlockToSelectionMenuItem.addActionListener((ActionEvent event) -> {
            // bring up scale track diagram dialog
            assignBlockToSelection();
        });
        assignBlockToSelectionMenuItem.setEnabled(_layoutTrackSelection.size() > 0);

        // scale track diagram
        JMenuItem jmi = new JMenuItem(Bundle.getMessage("ScaleTrackDiagram") + "...");
        jmi.setToolTipText(Bundle.getMessage("ScaleTrackDiagramToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up scale track diagram dialog
            ScaleTrackDiagramDialog d = new ScaleTrackDiagramDialog(this);
            d.scaleTrackDiagram();
        });

        // translate selection
        jmi = new JMenuItem(Bundle.getMessage("TranslateSelection") + "...");
        jmi.setToolTipText(Bundle.getMessage("TranslateSelectionToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up translate selection dialog
            if (!selectionActive || (selectionWidth == 0.0) || (selectionHeight == 0.0)) {
                // no selection has been made - nothing to move
                JOptionPane.showMessageDialog(this, Bundle.getMessage("Error12"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            } else {
                // bring up move selection dialog
                MoveSelectionDialog d = new MoveSelectionDialog(this);
                d.moveSelection();
            }
        });

        // undo translate selection
        undoTranslateSelectionMenuItem.setToolTipText(Bundle.getMessage("UndoTranslateSelectionToolTip"));
        toolsMenu.add(undoTranslateSelectionMenuItem);
        undoTranslateSelectionMenuItem.addActionListener((ActionEvent event) -> {
            // undo previous move selection
            undoMoveSelection();
        });
        undoTranslateSelectionMenuItem.setEnabled(canUndoMoveSelection);

        // rotate selection
        jmi = new JMenuItem(Bundle.getMessage("RotateSelection90MenuItemTitle"));
        jmi.setToolTipText(Bundle.getMessage("RotateSelection90MenuItemToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> rotateSelection90());

        // rotate entire layout
        jmi = new JMenuItem(Bundle.getMessage("RotateLayout90MenuItemTitle"));
        jmi.setToolTipText(Bundle.getMessage("RotateLayout90MenuItemToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> rotateLayout90());

        // align layout to grid
        jmi = new JMenuItem(Bundle.getMessage("AlignLayoutToGridMenuItemTitle") + "...");
        jmi.setToolTipText(Bundle.getMessage("AlignLayoutToGridMenuItemToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> alignLayoutToGrid());

        // align selection to grid
        jmi = new JMenuItem(Bundle.getMessage("AlignSelectionToGridMenuItemTitle") + "...");
        jmi.setToolTipText(Bundle.getMessage("AlignSelectionToGridMenuItemToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> alignSelectionToGrid());

        // reset turnout size to program defaults
        jmi = new JMenuItem(Bundle.getMessage("ResetTurnoutSize"));
        jmi.setToolTipText(Bundle.getMessage("ResetTurnoutSizeToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // undo previous move selection
            resetTurnoutSize();
        });
        toolsMenu.addSeparator();

        // skip turnout
        skipTurnoutCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SkipInternalTurnout"));
        skipTurnoutCheckBoxMenuItem.setToolTipText(Bundle.getMessage("SkipInternalTurnoutToolTip"));
        toolsMenu.add(skipTurnoutCheckBoxMenuItem);
        skipTurnoutCheckBoxMenuItem.addActionListener((ActionEvent event) -> setIncludedTurnoutSkipped(skipTurnoutCheckBoxMenuItem.isSelected()));
        skipTurnoutCheckBoxMenuItem.setSelected(isIncludedTurnoutSkipped());

        // set signals at turnout
        jmi = new JMenuItem(Bundle.getMessage("SignalsAtTurnout") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtTurnoutToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at turnout tool dialog
            getLETools().setSignalsAtTurnout(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        // set signals at block boundary
        jmi = new JMenuItem(Bundle.getMessage("SignalsAtBoundary") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtBoundaryToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at block boundary tool dialog
            getLETools().setSignalsAtBlockBoundary(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        // set signals at crossover turnout
        jmi = new JMenuItem(Bundle.getMessage("SignalsAtXoverTurnout") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtXoverTurnoutToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at crossover tool dialog
            getLETools().setSignalsAtXoverTurnout(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        // set signals at level crossing
        jmi = new JMenuItem(Bundle.getMessage("SignalsAtLevelXing") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtLevelXingToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at level crossing tool dialog
            getLETools().setSignalsAtLevelXing(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        // set signals at throat-to-throat turnouts
        jmi = new JMenuItem(Bundle.getMessage("SignalsAtTToTTurnout") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtTToTTurnoutToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at throat-to-throat turnouts tool dialog
            getLETools().setSignalsAtThroatToThroatTurnouts(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        // set signals at 3-way turnout
        jmi = new JMenuItem(Bundle.getMessage("SignalsAt3WayTurnout") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAt3WayTurnoutToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at 3-way turnout tool dialog
            getLETools().setSignalsAt3WayTurnout(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        jmi = new JMenuItem(Bundle.getMessage("SignalsAtSlip") + "...");
        jmi.setToolTipText(Bundle.getMessage("SignalsAtSlipToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            // bring up signals at throat-to-throat turnouts tool dialog
            getLETools().setSignalsAtSlip(leToolBarPanel.signalIconEditor, leToolBarPanel.signalFrame);
        });

        jmi = new JMenuItem(Bundle.getMessage("EntryExitTitle") + "...");
        jmi.setToolTipText(Bundle.getMessage("EntryExitToolTip"));
        toolsMenu.add(jmi);
        jmi.addActionListener((ActionEvent event) -> {
            if (addEntryExitPairAction == null) {
                addEntryExitPairAction = new AddEntryExitPairAction("ENTRY EXIT", LayoutEditor.this);
            }
            addEntryExitPairAction.actionPerformed(event);
        });
//        if (true) {   // TODO: disable for production
//            jmi = new JMenuItem("GEORGE");
//            toolsMenu.add(jmi);
//            jmi.addActionListener((ActionEvent event) -> {
//                // do GEORGE stuff here!
//            });
//        }
    }   // setupToolsMenu

    /**
     * get the toolbar side
     *
     * @return the side where to put the tool bar
     */
    public ToolBarSide getToolBarSide() {
        return toolBarSide;
    }

    /**
     * set the tool bar side
     *
     * @param newToolBarSide on which side to put the toolbar
     */
    public void setToolBarSide(ToolBarSide newToolBarSide) {
        // null if edit toolbar is not setup yet...
        if (!newToolBarSide.equals(toolBarSide)) {
            toolBarSide = newToolBarSide;
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setProperty(getWindowFrameRef(), "toolBarSide", toolBarSide.getName()));
            toolBarSideTopButton.setSelected(toolBarSide.equals(ToolBarSide.eTOP));
            toolBarSideLeftButton.setSelected(toolBarSide.equals(ToolBarSide.eLEFT));
            toolBarSideBottomButton.setSelected(toolBarSide.equals(ToolBarSide.eBOTTOM));
            toolBarSideRightButton.setSelected(toolBarSide.equals(ToolBarSide.eRIGHT));
            toolBarSideFloatButton.setSelected(toolBarSide.equals(ToolBarSide.eFLOAT));

            setupToolBar(); // re-layout all the toolbar items

            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                if (editToolBarContainerPanel != null) {
                    editToolBarContainerPanel.setVisible(false);
                }
                if (floatEditHelpPanel != null) {
                    floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
                }
            } else {
                if (floatingEditToolBoxFrame != null) {
                    deletefloatingEditToolBoxFrame();
                }
                editToolBarContainerPanel.setVisible(isEditable());
                if (getShowHelpBar()) {
                    helpBarPanel.setVisible(isEditable());
                    // not sure why... but this is the only way I could
                    // get everything to layout correctly
                    // when the helpbar is visible...
                    boolean editMode = isEditable();
                    setAllEditable(!editMode);
                    setAllEditable(editMode);
                }
            }
            wideToolBarCheckBoxMenuItem.setEnabled(
                    toolBarSide.equals(ToolBarSide.eTOP)
                    || toolBarSide.equals(ToolBarSide.eBOTTOM));
        }
    }   // setToolBarSide

    //
    //
    //
    private void setToolBarWide(boolean newToolBarIsWide) {
        // null if edit toolbar not setup yet...
        if (leToolBarPanel.toolBarIsWide != newToolBarIsWide) {
            leToolBarPanel.toolBarIsWide = newToolBarIsWide;

            wideToolBarCheckBoxMenuItem.setSelected(leToolBarPanel.toolBarIsWide);

            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                // Note: since prefs default to false and we want wide to be the default
                // we invert it and save it as thin
                prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".toolBarThin", !leToolBarPanel.toolBarIsWide);
            });

            setupToolBar(); // re-layout all the toolbar items

            if (getShowHelpBar()) {
                // not sure why, but this is the only way I could
                // get everything to layout correctly
                // when the helpbar is visible...
                boolean editMode = isEditable();
                setAllEditable(!editMode);
                setAllEditable(editMode);
            } else {
                helpBarPanel.setVisible(isEditable() && getShowHelpBar());
            }
        }
    }   // setToolBarWide

    //
    //
    //
    @SuppressWarnings("deprecation")  // getMenuShortcutKeyMask()
    private void setupZoomMenu(@Nonnull JMenuBar menuBar) {
        zoomMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuZoomMnemonic")));
        menuBar.add(zoomMenu);
        ButtonGroup zoomButtonGroup = new ButtonGroup();

        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // add zoom choices to menu
        JMenuItem zoomInItem = new JMenuItem(Bundle.getMessage("ZoomIn"));
        zoomInItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("zoomInMnemonic")));
        String zoomInAccelerator = Bundle.getMessage("zoomInAccelerator");
        // log.debug("zoomInAccelerator: " + zoomInAccelerator);
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomInAccelerator), primary_modifier));
        zoomMenu.add(zoomInItem);
        zoomInItem.addActionListener((ActionEvent event) -> setZoom(getZoom() * 1.1));

        JMenuItem zoomOutItem = new JMenuItem(Bundle.getMessage("ZoomOut"));
        zoomOutItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("zoomOutMnemonic")));
        String zoomOutAccelerator = Bundle.getMessage("zoomOutAccelerator");
        // log.debug("zoomOutAccelerator: " + zoomOutAccelerator);
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomOutAccelerator), primary_modifier));
        zoomMenu.add(zoomOutItem);
        zoomOutItem.addActionListener((ActionEvent event) -> setZoom(getZoom() / 1.1));

        JMenuItem zoomFitItem = new JMenuItem(Bundle.getMessage("ZoomToFit"));
        zoomMenu.add(zoomFitItem);
        zoomFitItem.addActionListener((ActionEvent event) -> zoomToFit());
        zoomMenu.addSeparator();

        // add zoom choices to menu
        zoomMenu.add(zoom025Item);
        zoom025Item.addActionListener((ActionEvent event) -> setZoom(0.25));
        zoomButtonGroup.add(zoom025Item);

        zoomMenu.add(zoom05Item);
        zoom05Item.addActionListener((ActionEvent event) -> setZoom(0.5));
        zoomButtonGroup.add(zoom05Item);

        zoomMenu.add(zoom075Item);
        zoom075Item.addActionListener((ActionEvent event) -> setZoom(0.75));
        zoomButtonGroup.add(zoom075Item);

        String zoomNoneAccelerator = Bundle.getMessage("zoomNoneAccelerator");
        // log.debug("zoomNoneAccelerator: " + zoomNoneAccelerator);
        noZoomItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(zoomNoneAccelerator), primary_modifier));

        zoomMenu.add(noZoomItem);
        noZoomItem.addActionListener((ActionEvent event) -> setZoom(1.0));
        zoomButtonGroup.add(noZoomItem);

        zoomMenu.add(zoom15Item);
        zoom15Item.addActionListener((ActionEvent event) -> setZoom(1.5));
        zoomButtonGroup.add(zoom15Item);

        zoomMenu.add(zoom20Item);
        zoom20Item.addActionListener((ActionEvent event) -> setZoom(2.0));
        zoomButtonGroup.add(zoom20Item);

        zoomMenu.add(zoom30Item);
        zoom30Item.addActionListener((ActionEvent event) -> setZoom(3.0));
        zoomButtonGroup.add(zoom30Item);

        zoomMenu.add(zoom40Item);
        zoom40Item.addActionListener((ActionEvent event) -> setZoom(4.0));
        zoomButtonGroup.add(zoom40Item);

        zoomMenu.add(zoom50Item);
        zoom50Item.addActionListener((ActionEvent event) -> setZoom(5.0));
        zoomButtonGroup.add(zoom50Item);

        zoomMenu.add(zoom60Item);
        zoom60Item.addActionListener((ActionEvent event) -> setZoom(6.0));
        zoomButtonGroup.add(zoom60Item);

        zoomMenu.add(zoom70Item);
        zoom70Item.addActionListener((ActionEvent event) -> setZoom(7.0));
        zoomButtonGroup.add(zoom70Item);

        zoomMenu.add(zoom80Item);
        zoom80Item.addActionListener((ActionEvent event) -> setZoom(8.0));
        zoomButtonGroup.add(zoom80Item);

        // note: because this LayoutEditor object was just instantiated its
        // zoom attribute is 1.0; if it's being instantiated from an XML file
        // that has a zoom attribute for this object then setZoom will be
        // called after this method returns and we'll select the appropriate
        // menu item then.
        noZoomItem.setSelected(true);

        // Note: We have to invoke this stuff later because _targetPanel is not setup yet
        SwingUtilities.invokeLater(() -> {
            // get the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                Object zoomProp = prefsMgr.getProperty(getWindowFrameRef(), "zoom");

                log.debug(
                        "{} zoom is {}", getWindowFrameRef(), zoomProp);

                if (zoomProp
                        != null) {
                    setZoom((Double) zoomProp);
                }
            }
            );

            // get the scroll bars from the scroll pane
            JScrollPane scrollPane = getPanelScrollPane();
            if (scrollPane != null) {
                JScrollBar hsb = scrollPane.getHorizontalScrollBar();
                JScrollBar vsb = scrollPane.getVerticalScrollBar();

                // Increase scroll bar unit increments!!!
                vsb.setUnitIncrement(gContext.getGridSize());
                hsb.setUnitIncrement(gContext.getGridSize());

                // add scroll bar adjustment listeners
                vsb.addAdjustmentListener(this::scrollBarAdjusted);
                hsb.addAdjustmentListener(this::scrollBarAdjusted);

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
    }   // setupZoomMenu

    private MouseWheelListener[] mouseWheelListeners;

    // scroll bar listener to update x & y coordinates in toolbar on scroll
    public void scrollBarAdjusted(AdjustmentEvent event) {
        // log.warn("scrollBarAdjusted");
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
            dLoc = new Point2D.Double(xLoc, yLoc);

            leToolBarPanel.setLocationText(dLoc);
        }
        adjustClip();
    }

    private void adjustScrollBars() {
        // log.info("adjustScrollBars()");

        // This is the bounds of what's on the screen
        JScrollPane scrollPane = getPanelScrollPane();
        Rectangle scrollBounds = scrollPane.getViewportBorderBounds();
        // log.info("  getViewportBorderBounds: {}", MathUtil.rectangle2DToString(scrollBounds));

        // this is the size of the entire scaled layout panel
        Dimension targetPanelSize = getTargetPanelSize();
        // log.info("  getTargetPanelSize: {}", MathUtil.dimensionToString(targetPanelSize));

        // double scale = getZoom();
        // determine the relative position of the current horizontal scrollbar
        JScrollBar horScroll = scrollPane.getHorizontalScrollBar();
        double oldX = horScroll.getValue();
        double oldMaxX = horScroll.getMaximum();
        double ratioX = (oldMaxX < 1) ? 0 : oldX / oldMaxX;

        // calculate the new X maximum and value
        int panelWidth = (int) (targetPanelSize.getWidth());
        int scrollWidth = (int) scrollBounds.getWidth();
        int newMaxX = Math.max(panelWidth - scrollWidth, 0);
        int newX = (int) (newMaxX * ratioX);
        horScroll.setMaximum(newMaxX);
        horScroll.setValue(newX);

        // determine the relative position of the current vertical scrollbar
        JScrollBar vertScroll = scrollPane.getVerticalScrollBar();
        double oldY = vertScroll.getValue();
        double oldMaxY = vertScroll.getMaximum();
        double ratioY = (oldMaxY < 1) ? 0 : oldY / oldMaxY;

        // calculate the new X maximum and value
        int tempPanelHeight = (int) (targetPanelSize.getHeight());
        int tempScrollHeight = (int) scrollBounds.getHeight();
        int newMaxY = Math.max(tempPanelHeight - tempScrollHeight, 0);
        int newY = (int) (newMaxY * ratioY);
        vertScroll.setMaximum(newMaxY);
        vertScroll.setValue(newY);

//        log.info("w: {}, x: {}, h: {}, y: {}", "" + newMaxX, "" + newX, "" + newMaxY, "" + newY);
        adjustClip();
    }

    private void adjustClip() {
        // log.info("adjustClip()");

        // This is the bounds of what's on the screen
        JScrollPane scrollPane = getPanelScrollPane();
        Rectangle scrollBounds = scrollPane.getViewportBorderBounds();
        // log.info("  ViewportBorderBounds: {}", MathUtil.rectangle2DToString(scrollBounds));

        JScrollBar horScroll = scrollPane.getHorizontalScrollBar();
        int scrollX = horScroll.getValue();
        JScrollBar vertScroll = scrollPane.getVerticalScrollBar();
        int scrollY = vertScroll.getValue();

        Rectangle2D newClipRect = MathUtil.offset(
                scrollBounds,
                scrollX - scrollBounds.getMinX(),
                scrollY - scrollBounds.getMinY());
        newClipRect = MathUtil.scale(newClipRect, 1.0 / getZoom());
        newClipRect = MathUtil.granulize(newClipRect, 1.0); // round to nearest pixel
        layoutEditorComponent.setClip(newClipRect);

        redrawPanel();
    }

    @Override
    public void mouseWheelMoved(@Nonnull MouseWheelEvent event) {
        // log.warn("mouseWheelMoved");
        if (event.isAltDown()) {
            // get the mouse position from the event and convert to target panel coordinates
            Component component = (Component) event.getSource();
            Point eventPoint = event.getPoint();
            JComponent targetPanel = getTargetPanel();
            Point2D mousePoint = SwingUtilities.convertPoint(component, eventPoint, targetPanel);

            // get the old view port position
            JScrollPane scrollPane = getPanelScrollPane();
            JViewport viewPort = scrollPane.getViewport();
            Point2D viewPosition = viewPort.getViewPosition();

            // convert from oldZoom (scaled) coordinates to image coordinates
            double zoom = getZoom();
            Point2D imageMousePoint = MathUtil.divide(mousePoint, zoom);
            Point2D imageViewPosition = MathUtil.divide(viewPosition, zoom);
            // compute the delta (in image coordinates)
            Point2D imageDelta = MathUtil.subtract(imageMousePoint, imageViewPosition);

            // compute how much to change zoom
            double amount = Math.pow(1.1, event.getScrollAmount());
            if (event.getWheelRotation() < 0.0) {
                // reciprocal for zoom out
                amount = 1.0 / amount;
            }
            // set the new zoom
            double newZoom = setZoom(zoom * amount);
            // recalulate the amount (in case setZoom didn't zoom as much as we wanted)
            amount = newZoom / zoom;

            // convert the old delta to the new
            Point2D newImageDelta = MathUtil.divide(imageDelta, amount);
            // calculate the new view position (in image coordinates)
            Point2D newImageViewPosition = MathUtil.subtract(imageMousePoint, newImageDelta);
            // convert from image coordinates to newZoom (scaled) coordinates
            Point2D newViewPosition = MathUtil.multiply(newImageViewPosition, newZoom);

            // don't let origin go negative
            newViewPosition = MathUtil.max(newViewPosition, MathUtil.zeroPoint2D);
            // log.info("mouseWheelMoved: newViewPos2D: {}", newViewPosition);

            // set new view position
            viewPort.setViewPosition(MathUtil.point2DToPoint(newViewPosition));
        } else {
            JScrollPane scrollPane = getPanelScrollPane();
            if (scrollPane != null) {
                if (scrollPane.getVerticalScrollBar().isVisible()) {
                    // Redispatch the event to the original MouseWheelListeners
                    for (MouseWheelListener mwl : mouseWheelListeners) {
                        mwl.mouseWheelMoved(event);
                    }
                } else {
                    // proprogate event to ancestor
                    Component ancestor = SwingUtilities.getAncestorOfClass(JScrollPane.class,
                            scrollPane);
                    if (ancestor != null) {
                        MouseWheelEvent mwe = new MouseWheelEvent(
                                ancestor,
                                event.getID(),
                                event.getWhen(),
                                event.getModifiersEx(),
                                event.getX(),
                                event.getY(),
                                event.getXOnScreen(),
                                event.getYOnScreen(),
                                event.getClickCount(),
                                event.isPopupTrigger(),
                                event.getScrollType(),
                                event.getScrollAmount(),
                                event.getWheelRotation());

                        ancestor.dispatchEvent(mwe);
                    }
                }
            }
        }
    }

    //
    // select the apropreate zoom menu item based on the zoomFactor
    //
    private void selectZoomMenuItem(double zoomFactor) {
        // this will put zoomFactor on 100% increments
        //(so it will more likely match one of these values)
        int newZoomFactor = (int) MathUtil.granulize(zoomFactor, 100);
        // int newZoomFactor = ((int) Math.round(zoomFactor)) * 100;
        noZoomItem.setSelected(newZoomFactor == 100);
        zoom20Item.setSelected(newZoomFactor == 200);
        zoom30Item.setSelected(newZoomFactor == 300);
        zoom40Item.setSelected(newZoomFactor == 400);
        zoom50Item.setSelected(newZoomFactor == 500);
        zoom60Item.setSelected(newZoomFactor == 600);
        zoom70Item.setSelected(newZoomFactor == 700);
        zoom80Item.setSelected(newZoomFactor == 800);

        // this will put zoomFactor on 50% increments
        //(so it will more likely match one of these values)
        // newZoomFactor = ((int) (zoomFactor * 2)) * 50;
        newZoomFactor = (int) MathUtil.granulize(zoomFactor, 50);
        zoom05Item.setSelected(newZoomFactor == 50);
        zoom15Item.setSelected(newZoomFactor == 150);

        // this will put zoomFactor on 25% increments
        //(so it will more likely match one of these values)
        // newZoomFactor = ((int) (zoomFactor * 4)) * 25;
        newZoomFactor = (int) MathUtil.granulize(zoomFactor, 25);
        zoom025Item.setSelected(newZoomFactor == 25);
        zoom075Item.setSelected(newZoomFactor == 75);
    }

    /**
     * setZoom
     *
     * @param zoomFactor the amount to scale
     * @return the new scale amount (not necessarily the same as zoomFactor)
     */
    public double setZoom(double zoomFactor) {
        double newZoom = MathUtil.pin(zoomFactor, minZoom, maxZoom);
        selectZoomMenuItem(newZoom);

        if (!MathUtil.equals(newZoom, getPaintScale())) {
            log.debug("zoom: {}", zoomFactor);
            // setPaintScale(newZoom);   //<<== don't call; messes up scrollbars
            _paintScale = newZoom;      // just set paint scale directly
            resetTargetSize();          // calculate new target panel size
            adjustScrollBars();         // and adjust the scrollbars ourselves
            // adjustClip();

            leToolBarPanel.zoomLabel.setText(String.format("x%1$,.2f", newZoom));

            // save the window specific saved zoom user preference
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setProperty(getWindowFrameRef(), "zoom", zoomFactor));
        }
        return getPaintScale();
    }

    /**
     * getZoom
     *
     * @return the zooming scale
     */
    public double getZoom() {
        return getPaintScale();
    }

    /**
     * getMinZoom
     *
     * @return the minimum zoom scale
     */
    public double getMinZoom() {
        return minZoom;
    }

    /**
     * getMaxZoom
     *
     * @return the maximum zoom scale
     */
    public double getMaxZoom() {
        return maxZoom;
    }

    //
    // TODO: make this public? (might be useful!)
    //
    private Rectangle2D calculateMinimumLayoutBounds() {
        // calculate a union of the bounds of everything on the layout
        Rectangle2D result = new Rectangle2D.Double();

        // combine all (onscreen) Components into a list of list of Components
        List<List<? extends Component>> listOfListsOfComponents = new ArrayList<>();
        listOfListsOfComponents.add(backgroundImage);
        listOfListsOfComponents.add(sensorImage);
        listOfListsOfComponents.add(signalHeadImage);
        listOfListsOfComponents.add(markerImage);
        listOfListsOfComponents.add(labelImage);
        listOfListsOfComponents.add(clocks);
        listOfListsOfComponents.add(multiSensors);
        listOfListsOfComponents.add(signalList);
        listOfListsOfComponents.add(memoryLabelList);
        listOfListsOfComponents.add(blockContentsLabelList);
        listOfListsOfComponents.add(sensorList);
        listOfListsOfComponents.add(signalMastList);
        // combine their bounds
        for (List<? extends Component> listOfComponents : listOfListsOfComponents) {
            for (Component o : listOfComponents) {
                if (result.isEmpty()) {
                    result = o.getBounds();
                } else {
                    result = result.createUnion(o.getBounds());
                }
            }
        }

        for (LayoutTrackView ov : getLayoutTrackViews()) {
            if (result.isEmpty()) {
                result = ov.getBounds();
            } else {
                result = result.createUnion(ov.getBounds());
            }
        }

        for (LayoutShape o : layoutShapes) {
            if (result.isEmpty()) {
                result = o.getBounds();
            } else {
                result = result.createUnion(o.getBounds());
            }
        }

        // put a grid size margin around it
        result = MathUtil.inset(result, gContext.getGridSize() * gContext.getGridSize2nd() / -2.0);

        return result;
    }

    /**
     * resize panel bounds
     *
     * @param forceFlag if false only grow bigger
     * @return the new (?) panel bounds
     */
    private Rectangle2D resizePanelBounds(boolean forceFlag) {
        Rectangle2D panelBounds = getPanelBounds();
        Rectangle2D layoutBounds = calculateMinimumLayoutBounds();

        // make sure it includes the origin
        layoutBounds.add(MathUtil.zeroPoint2D);

        if (forceFlag) {
            panelBounds = layoutBounds;
        } else {
            panelBounds.add(layoutBounds);
        }

        // don't let origin go negative
        panelBounds = panelBounds.createIntersection(MathUtil.zeroToInfinityRectangle2D);

        // log.info("resizePanelBounds: {}", MathUtil.rectangle2DToString(panelBounds));
        setPanelBounds(panelBounds);

        return panelBounds;
    }

    private double zoomToFit() {
        Rectangle2D layoutBounds = resizePanelBounds(true);

        // calculate the bounds for the scroll pane
        JScrollPane scrollPane = getPanelScrollPane();
        Rectangle2D scrollBounds = scrollPane.getViewportBorderBounds();

        // don't let origin go negative
        scrollBounds = scrollBounds.createIntersection(MathUtil.zeroToInfinityRectangle2D);

        // calculate the horzontial and vertical scales
        double scaleWidth = scrollPane.getWidth() / layoutBounds.getWidth();
        double scaleHeight = scrollPane.getHeight() / layoutBounds.getHeight();

        // set the new zoom to the smallest of the two
        double result = setZoom(Math.min(scaleWidth, scaleHeight));

        // set the new zoom (return value may be different)
        result = setZoom(result);

        // calculate new scroll bounds
        scrollBounds = MathUtil.scale(layoutBounds, result);

        // don't let origin go negative
        scrollBounds = scrollBounds.createIntersection(MathUtil.zeroToInfinityRectangle2D);

        // make sure it includes the origin
        scrollBounds.add(MathUtil.zeroPoint2D);

        // and scroll to it
        scrollPane.scrollRectToVisible(MathUtil.rectangle2DToRectangle(scrollBounds));

        return result;
    }

    private Point2D windowCenter() {
        // Returns window's center coordinates converted to layout space
        // Used for initial setup of turntables and reporters
        return MathUtil.divide(MathUtil.center(getBounds()), getZoom());
    }

    private void setupMarkerMenu(@Nonnull JMenuBar menuBar) {
        JMenu markerMenu = new JMenu(Bundle.getMessage("MenuMarker"));

        markerMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuMarkerMnemonic")));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLoco") + "...") {
            @Override
            public void actionPerformed(ActionEvent event) {
                locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLocoRoster") + "...") {
            @Override
            public void actionPerformed(ActionEvent event) {
                locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("RemoveMarkers")) {
            @Override
            public void actionPerformed(ActionEvent event) {
                removeMarkers();
            }
        });
    }

    private void setupDispatcherMenu(@Nonnull JMenuBar menuBar) {
        JMenu dispMenu = new JMenu(Bundle.getMessage("MenuDispatcher"));

        dispMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuDispatcherMnemonic")));
        dispMenu.add(new JMenuItem(new DispatcherAction(Bundle.getMessage("MenuItemOpen"))));
        menuBar.add(dispMenu);
        JMenuItem newTrainItem = new JMenuItem(Bundle.getMessage("MenuItemNewTrain"));
        dispMenu.add(newTrainItem);
        newTrainItem.addActionListener((ActionEvent event) -> {
            if (InstanceManager.getDefault(TransitManager.class).getNamedBeanSet().size() <= 0) {
                // Inform the user that there are no Transits available, and don't open the window
                JOptionPane.showMessageDialog(
                        null,
                        ResourceBundle.getBundle("jmri.jmrit.dispatcher.DispatcherBundle").
                                getString("NoTransitsMessage"));
            } else {
                DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class
                );
                if (!df.getNewTrainActive()) {
                    df.getActiveTrainFrame().initiateTrain(event, null, null);
                    df.setNewTrainActive(true);
                } else {
                    df.getActiveTrainFrame().showActivateFrame(null);
                }
            }
        });
        menuBar.add(dispMenu);
    }

    private boolean includedTurnoutSkipped = false;

    public boolean isIncludedTurnoutSkipped() {
        return includedTurnoutSkipped;
    }

    public void setIncludedTurnoutSkipped(Boolean boo) {
        includedTurnoutSkipped = boo;
    }

    boolean openDispatcherOnLoad = false;

    // TODO: Java standard pattern for boolean getters is "isOpenDispatcherOnLoad()"
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
    public void removeMarkers() {
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
    }

    /**
     * Assign the block from the toolbar to all selected layout tracks
     */
    private void assignBlockToSelection() {
        String newName = leToolBarPanel.blockIDComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        LayoutBlock b = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(newName);
        _layoutTrackSelection.forEach((lt) -> lt.setAllLayoutBlocks(b));
    }

    public boolean translateTrack(float xDel, float yDel) {
        Point2D delta = new Point2D.Double(xDel, yDel);
        getLayoutTrackViews().forEach((ltv) -> ltv.setCoordsCenter(MathUtil.add(ltv.getCoordsCenter(), delta)));
        resizePanelBounds(true);
        return true;
    }

    /**
     * scale all LayoutTracks coordinates by the x and y factors.
     *
     * @param xFactor the amount to scale X coordinates.
     * @param yFactor the amount to scale Y coordinates.
     * @return true when complete.
     */
    public boolean scaleTrack(float xFactor, float yFactor) {
        getLayoutTrackViews().forEach((ltv) -> ltv.scaleCoords(xFactor, yFactor));

        // update the overall scale factors
        gContext.setXScale(gContext.getXScale() * xFactor);
        gContext.setYScale(gContext.getYScale() * yFactor);

        resizePanelBounds(true);
        return true;
    }

    /**
     * loop through all LayoutBlocks and set colors to the default colors from
     * this LayoutEditor
     *
     * @return count of changed blocks
     */
    public int setAllTracksToDefaultColors() {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class
        );
        SortedSet<LayoutBlock> lBList = lbm.getNamedBeanSet();
        int changed = 0;
        for (LayoutBlock lb : lBList) {
            lb.setBlockTrackColor(this.getDefaultTrackColorColor());
            lb.setBlockOccupiedColor(this.getDefaultOccupiedTrackColorColor());
            lb.setBlockExtraColor(this.getDefaultAlternativeTrackColorColor());
            changed++;
        }
        log.info("Track Colors set to default values for {} layoutBlocks.", changed);
        return changed;
    }

    private Rectangle2D undoRect;
    private boolean canUndoMoveSelection = false;
    private Point2D undoDelta = MathUtil.zeroPoint2D;

    /**
     * Translate entire layout by x and y amounts.
     *
     * @param xTranslation horizontal (X) translation value
     * @param yTranslation vertical (Y) translation value
     */
    public void translate(float xTranslation, float yTranslation) {
        // here when all numbers read in - translation if entered
        if ((xTranslation != 0.0F) || (yTranslation != 0.0F)) {
            Point2D delta = new Point2D.Double(xTranslation, yTranslation);
            Rectangle2D selectionRect = getSelectionRect();

            // set up undo information
            undoRect = MathUtil.offset(selectionRect, delta);
            undoDelta = MathUtil.subtract(MathUtil.zeroPoint2D, delta);
            canUndoMoveSelection = true;
            undoTranslateSelectionMenuItem.setEnabled(canUndoMoveSelection);

            // apply translation to icon items within the selection
            for (Positionable c : _positionableSelection) {
                Point2D newPoint = MathUtil.add(c.getLocation(), delta);
                c.setLocation((int) newPoint.getX(), (int) newPoint.getY());
            }

            for (LayoutTrack lt : _layoutTrackSelection) {
                LayoutTrackView ltv = getLayoutTrackView(lt);
                ltv.setCoordsCenter(MathUtil.add(ltv.getCoordsCenter(), delta));
            }

            for (LayoutShape ls : _layoutShapeSelection) {
                ls.setCoordsCenter(MathUtil.add(ls.getCoordsCenter(), delta));
            }

            selectionX = undoRect.getX();
            selectionY = undoRect.getY();
            selectionWidth = undoRect.getWidth();
            selectionHeight = undoRect.getHeight();
            resizePanelBounds(false);
            setDirty();
            redrawPanel();
        }
    }

    /**
     * undo the move selection
     */
    void undoMoveSelection() {
        if (canUndoMoveSelection) {
            _positionableSelection.forEach((c) -> {
                Point2D newPoint = MathUtil.add(c.getLocation(), undoDelta);
                c.setLocation((int) newPoint.getX(), (int) newPoint.getY());
            });

            _layoutTrackSelection.forEach(
                    (lt) -> {
                        LayoutTrackView ltv = getLayoutTrackView(lt);
                        ltv.setCoordsCenter(MathUtil.add(ltv.getCoordsCenter(), undoDelta));
                    }
            );

            _layoutShapeSelection.forEach((ls) -> ls.setCoordsCenter(MathUtil.add(ls.getCoordsCenter(), undoDelta)));

            undoRect = MathUtil.offset(undoRect, undoDelta);
            selectionX = undoRect.getX();
            selectionY = undoRect.getY();
            selectionWidth = undoRect.getWidth();
            selectionHeight = undoRect.getHeight();

            resizePanelBounds(false);
            redrawPanel();

            canUndoMoveSelection = false;
            undoTranslateSelectionMenuItem.setEnabled(canUndoMoveSelection);
        }
    }

    /**
     * Rotate selection by 90 degrees clockwise.
     */
    public void rotateSelection90() {
        Rectangle2D bounds = getSelectionRect();
        Point2D center = MathUtil.midPoint(bounds);

        for (Positionable positionable : _positionableSelection) {
            Rectangle2D cBounds = positionable.getBounds(new Rectangle());
            Point2D oldBottomLeft = new Point2D.Double(cBounds.getMinX(), cBounds.getMaxY());
            Point2D newTopLeft = MathUtil.rotateDEG(oldBottomLeft, center, 90);
            boolean rotateFlag = true;
            if (positionable instanceof PositionableLabel) {
                PositionableLabel positionableLabel = (PositionableLabel) positionable;
                if (positionableLabel.isBackground()) {
                    rotateFlag = false;
                }
            }
            if (rotateFlag) {
                positionable.rotate(positionable.getDegrees() + 90);
                positionable.setLocation((int) newTopLeft.getX(), (int) newTopLeft.getY());
            }
        }

        for (LayoutTrack lt : _layoutTrackSelection) {
            LayoutTrackView ltv = getLayoutTrackView(lt);
            ltv.setCoordsCenter(MathUtil.rotateDEG(ltv.getCoordsCenter(), center, 90));
            ltv.rotateCoords(90);
        }

        for (LayoutShape ls : _layoutShapeSelection) {
            ls.setCoordsCenter(MathUtil.rotateDEG(ls.getCoordsCenter(), center, 90));
            ls.rotateCoords(90);
        }

        resizePanelBounds(true);
        setDirty();
        redrawPanel();
    }

    /**
     * Rotate the entire layout by 90 degrees clockwise.
     */
    public void rotateLayout90() {
        List<Positionable> positionables = new ArrayList<>(getContents());
        positionables.addAll(backgroundImage);
        positionables.addAll(blockContentsLabelList);
        positionables.addAll(labelImage);
        positionables.addAll(memoryLabelList);
        positionables.addAll(sensorImage);
        positionables.addAll(sensorList);
        positionables.addAll(signalHeadImage);
        positionables.addAll(signalList);
        positionables.addAll(signalMastList);

        // do this to remove duplicates that may be in more than one list
        positionables = positionables.stream().distinct().collect(Collectors.toList());

        Rectangle2D bounds = getPanelBounds();
        Point2D lowerLeft = new Point2D.Double(bounds.getMinX(), bounds.getMaxY());

        for (Positionable positionable : positionables) {
            Rectangle2D cBounds = positionable.getBounds(new Rectangle());
            Point2D newTopLeft = MathUtil.subtract(MathUtil.rotateDEG(positionable.getLocation(), lowerLeft, 90), lowerLeft);
            boolean reLocateFlag = true;
            if (positionable instanceof PositionableLabel) {
                try {
                    PositionableLabel positionableLabel = (PositionableLabel) positionable;
                    if (positionableLabel.isBackground()) {
                        reLocateFlag = false;
                    }
                    positionableLabel.rotate(positionableLabel.getDegrees() + 90);
                } catch (NullPointerException ex) {
                    log.warn("previously-ignored NPE", ex);
                }
            }
            if (reLocateFlag) {
                try {
                    positionable.setLocation((int) (newTopLeft.getX() - cBounds.getHeight()), (int) newTopLeft.getY());
                } catch (NullPointerException ex) {
                    log.warn("previously-ignored NPE", ex);
                }
            }
        }

        for (LayoutTrackView ltv : getLayoutTrackViews()) {
            try {
                Point2D newPoint = MathUtil.subtract(MathUtil.rotateDEG(ltv.getCoordsCenter(), lowerLeft, 90), lowerLeft);
                ltv.setCoordsCenter(newPoint);
                ltv.rotateCoords(90);
            } catch (NullPointerException ex) {
                log.warn("previously-ignored NPE", ex);
            }
        }

        for (LayoutShape ls : layoutShapes) {
            Point2D newPoint = MathUtil.subtract(MathUtil.rotateDEG(ls.getCoordsCenter(), lowerLeft, 90), lowerLeft);
            ls.setCoordsCenter(newPoint);
            ls.rotateCoords(90);
        }

        resizePanelBounds(true);
        setDirty();
        redrawPanel();
    }

    /**
     * align the layout to grid
     */
    public void alignLayoutToGrid() {
        // align to grid
        List<Positionable> positionables = new ArrayList<>(getContents());
        positionables.addAll(backgroundImage);
        positionables.addAll(blockContentsLabelList);
        positionables.addAll(labelImage);
        positionables.addAll(memoryLabelList);
        positionables.addAll(sensorImage);
        positionables.addAll(sensorList);
        positionables.addAll(signalHeadImage);
        positionables.addAll(signalList);
        positionables.addAll(signalMastList);

        // do this to remove duplicates that may be in more than one list
        positionables = positionables.stream().distinct().collect(Collectors.toList());
        alignToGrid(positionables, getLayoutTracks(), layoutShapes);
    }

    /**
     * align selection to grid
     */
    public void alignSelectionToGrid() {
        alignToGrid(_positionableSelection, _layoutTrackSelection, _layoutShapeSelection);
    }

    private void alignToGrid(List<Positionable> positionables, List<LayoutTrack> tracks, List<LayoutShape> shapes) {
        for (Positionable positionable : positionables) {
            Point2D newLocation = MathUtil.granulize(positionable.getLocation(), gContext.getGridSize());
            positionable.setLocation((int) (newLocation.getX()), (int) newLocation.getY());
        }
        for (LayoutTrack lt : tracks) {
            LayoutTrackView ltv = getLayoutTrackView(lt);
            ltv.setCoordsCenter(MathUtil.granulize(ltv.getCoordsCenter(), gContext.getGridSize()));
            if (lt instanceof LayoutTurntable) {
                LayoutTurntable tt = (LayoutTurntable) lt;
                LayoutTurntableView ttv = getLayoutTurntableView(tt);
                for (LayoutTurntable.RayTrack rt : tt.getRayTrackList()) {
                    int rayIndex = rt.getConnectionIndex();
                    ttv.setRayCoordsIndexed(MathUtil.granulize(ttv.getRayCoordsIndexed(rayIndex), gContext.getGridSize()), rayIndex);
                }
            }
        }
        for (LayoutShape ls : shapes) {
            ls.setCoordsCenter(MathUtil.granulize(ls.getCoordsCenter(), gContext.getGridSize()));
            for (int idx = 0; idx < ls.getNumberPoints(); idx++) {
                ls.setPoint(idx, MathUtil.granulize(ls.getPoint(idx), gContext.getGridSize()));
            }
        }

        resizePanelBounds(true);
        setDirty();
        redrawPanel();
    }

    public void setCurrentPositionAndSize() {
        // save current panel location and size
        Dimension dim = getSize();

        // Compute window size based on LayoutEditor size
        gContext.setWindowHeight(dim.height);
        gContext.setWindowWidth(dim.width);

        // Compute layout size based on LayoutPane size
        dim = getTargetPanelSize();
        gContext.setLayoutWidth((int) (dim.width / getZoom()));
        gContext.setLayoutHeight((int) (dim.height / getZoom()));
        adjustScrollBars();

        Point pt = getLocationOnScreen();
        gContext.setUpperLeftY(pt.x);
        gContext.setUpperLeftY(pt.y);

        log.debug("setCurrentPositionAndSize Position - {},{} WindowSize - {},{} PanelSize - {},{}", gContext.getUpperLeftX(), gContext.getUpperLeftY(), gContext.getWindowWidth(), gContext.getWindowHeight(), gContext.getLayoutWidth(), gContext.getLayoutHeight());
        setDirty();
    }

    private JRadioButtonMenuItem addButtonGroupMenuEntry(
            @Nonnull JMenu inMenu,
            ButtonGroup inButtonGroup,
            final String inName,
            boolean inSelected,
            ActionListener inActionListener) {
        JRadioButtonMenuItem result = new JRadioButtonMenuItem(inName);
        if (inActionListener != null) {
            result.addActionListener(inActionListener);
        }
        if (inButtonGroup != null) {
            inButtonGroup.add(result);
        }
        result.setSelected(inSelected);

        inMenu.add(result);

        return result;
    }

    private void addTurnoutCircleSizeMenuEntry(
            @Nonnull JMenu inMenu,
            @Nonnull String inName,
            final int inSize) {
        ActionListener a = (ActionEvent event) -> {
            if (getTurnoutCircleSize() != inSize) {
                setTurnoutCircleSize(inSize);
                setDirty();
                redrawPanel();
            }
        };
        addButtonGroupMenuEntry(inMenu,
                turnoutCircleSizeButtonGroup, inName,
                getTurnoutCircleSize() == inSize, a);
    }

    private void setOptionMenuTurnoutCircleSize() {
        String tcs = Integer.toString(getTurnoutCircleSize());
        Enumeration<AbstractButton> e = turnoutCircleSizeButtonGroup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton button = e.nextElement();
            String buttonName = button.getText();
            button.setSelected(buttonName.equals(tcs));
        }
    }

    @Override
    public void setScroll(int state) {
        if (isEditable()) {
            // In edit mode the scroll bars are always displayed, however we will want to set the scroll for when we exit edit mode
            super.setScroll(Editor.SCROLL_BOTH);
            _scrollState = state;
        } else {
            super.setScroll(state);
        }
    }

    /**
     * The LE xml load uses the string version of setScroll which went directly to
     * Editor.  The string version has been added here so that LE can set the scroll
     * selection.
     * @param value The new scroll value.
     */
    @Override
    public void setScroll(String value) {
        if (value != null) super.setScroll(value);
        scrollNoneMenuItem.setSelected(_scrollState == Editor.SCROLL_NONE);
        scrollBothMenuItem.setSelected(_scrollState == Editor.SCROLL_BOTH);
        scrollHorizontalMenuItem.setSelected(_scrollState == Editor.SCROLL_HORIZONTAL);
        scrollVerticalMenuItem.setSelected(_scrollState == Editor.SCROLL_VERTICAL);
    }

    /**
     * Add a layout turntable at location specified
     *
     * @param pt x,y placement for turntable
     */
    public void addTurntable(@Nonnull Point2D pt) {
        // get unique name
        String name = finder.uniqueName("TUR", ++numLayoutTurntables);
        LayoutTurntable lt = new LayoutTurntable(name, this);
        LayoutTurntableView ltv = new LayoutTurntableView(lt, pt, this);

        addLayoutTrack(lt, ltv);

        lt.addRay(0.0);
        lt.addRay(90.0);
        lt.addRay(180.0);
        lt.addRay(270.0);
        setDirty();

    }

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
    }

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
     * Check the dirty state.
     *
     * @return true if panel has changed
     */
    public boolean isDirty() {
        return panelChanged;
    }

    /*
    * Get mouse coordinates and adjust for zoom.
    * <p>
    * Side effects on xLoc, yLoc and dLoc
     */
    @Nonnull
    private Point2D calcLocation(MouseEvent event, int dX, int dY) {
        xLoc = (int) ((event.getX() + dX) / getZoom());
        yLoc = (int) ((event.getY() + dY) / getZoom());
        dLoc = new Point2D.Double(xLoc, yLoc);
        return dLoc;
    }

    private Point2D calcLocation(MouseEvent event) {
        return calcLocation(event, 0, 0);
    }

    /**
     * Handle a mouse pressed event
     * <p>
     * Side-effects on _anchorX, _anchorY,_lastX, _lastY, xLoc, yLoc, dLoc,
     * selectionActive, xLabel, yLabel
     *
     * @param event the MouseEvent
     */
    @Override
    public void mousePressed(MouseEvent event) {
        // initialize cursor position
        _anchorX = xLoc;
        _anchorY = yLoc;
        _lastX = _anchorX;
        _lastY = _anchorY;
        calcLocation(event);

        // TODO: Add command-click on nothing to pan view?
        if (isEditable()) {
            boolean prevSelectionActive = selectionActive;
            selectionActive = false;
            leToolBarPanel.setLocationText(dLoc);

            if (event.isPopupTrigger()) {
                if (isMetaDown(event) || event.isAltDown()) {
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    // no possible conflict with moving, display the popup now
                    showEditPopUps(event);
                }
            }

            if (isMetaDown(event) || event.isAltDown()) {
                // if dragging an item, identify the item for mouseDragging
                selectedObject = null;
                selectedHitPointType = HitPointType.NONE;

                if (findLayoutTracksHitPoint(dLoc)) {
                    selectedObject = foundTrack;
                    selectedHitPointType = foundHitPointType;
                    startDelta = MathUtil.subtract(foundLocation, dLoc);
                    foundTrack = null;
                    foundTrackView = null;
                } else {
                    selectedObject = checkMarkerPopUps(dLoc);
                    if (selectedObject != null) {
                        selectedHitPointType = HitPointType.MARKER;
                        startDelta = MathUtil.subtract(((LocoIcon) selectedObject).getLocation(), dLoc);
                    } else {
                        selectedObject = checkClockPopUps(dLoc);
                        if (selectedObject != null) {
                            selectedHitPointType = HitPointType.LAYOUT_POS_JCOMP;
                            startDelta = MathUtil.subtract(((PositionableJComponent) selectedObject).getLocation(), dLoc);
                        } else {
                            selectedObject = checkMultiSensorPopUps(dLoc);
                            if (selectedObject != null) {
                                selectedHitPointType = HitPointType.MULTI_SENSOR;
                                startDelta = MathUtil.subtract(((MultiSensorIcon) selectedObject).getLocation(), dLoc);
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
                            selectedHitPointType = HitPointType.LAYOUT_POS_LABEL;
                            startDelta = MathUtil.subtract(((PositionableLabel) selectedObject).getLocation(), dLoc);
                            if (selectedObject instanceof MemoryIcon) {
                                MemoryIcon pm = (MemoryIcon) selectedObject;

                                if (pm.getPopupUtility().getFixedWidth() == 0) {
                                    startDelta = new Point2D.Double((pm.getOriginalX() - dLoc.getX()),
                                            (pm.getOriginalY() - dLoc.getY()));
                                }
                            }
                        } else {
                            selectedObject = checkBackgroundPopUps(dLoc);

                            if (selectedObject != null) {
                                selectedHitPointType = HitPointType.LAYOUT_POS_LABEL;
                                startDelta = MathUtil.subtract(((PositionableLabel) selectedObject).getLocation(), dLoc);
                            } else {
                                // dragging a shape?
                                ListIterator<LayoutShape> listIterator = layoutShapes.listIterator(layoutShapes.size());
                                // hit test in front to back order (reverse order of list)
                                while (listIterator.hasPrevious()) {
                                    LayoutShape ls = listIterator.previous();
                                    selectedHitPointType = ls.findHitPointType(dLoc, true);
                                    if (LayoutShape.isShapeHitPointType(selectedHitPointType)) {
                                        // log.warn("drag selectedObject: ", lt);
                                        selectedObject = ls;    // found one!
                                        beginLocation = dLoc;
                                        currentLocation = beginLocation;
                                        startDelta = MathUtil.zeroPoint2D;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (event.isShiftDown() && leToolBarPanel.trackButton.isSelected() && !event.isPopupTrigger()) {
                // starting a Track Segment, check for free connection point
                selectedObject = null;

                if (findLayoutTracksHitPoint(dLoc, true)) {
                    // match to a free connection point
                    beginTrack = foundTrack;
                    beginHitPointType = foundHitPointType;
                    beginLocation = foundLocation;
                    // BUGFIX: prevents initial drawTrackSegmentInProgress to {0, 0}
                    currentLocation = beginLocation;
                } else {
                    // TODO: auto-add anchor point?
                    beginTrack = null;
                }
            } else if (event.isShiftDown() && leToolBarPanel.shapeButton.isSelected() && !event.isPopupTrigger()) {
                // adding or extending a shape
                selectedObject = null;  // assume we're adding...
                for (LayoutShape ls : layoutShapes) {
                    selectedHitPointType = ls.findHitPointType(dLoc, true);
                    if (HitPointType.isShapePointOffsetHitPointType(selectedHitPointType)) {
                        // log.warn("extend selectedObject: ", lt);
                        selectedObject = ls;    // nope, we're extending
                        beginLocation = dLoc;
                        currentLocation = beginLocation;
                        break;
                    }
                }
            } else if (!event.isShiftDown() && !event.isControlDown() && !event.isPopupTrigger()) {
                // check if controlling a turnout in edit mode
                selectedObject = null;

                if (allControlling()) {
                    checkControls(false);
                }
                // initialize starting selection - cancel any previous selection rectangle
                selectionActive = true;
                selectionX = dLoc.getX();
                selectionY = dLoc.getY();
                selectionWidth = 0.0;
                selectionHeight = 0.0;
            }

            if (prevSelectionActive) {
                redrawPanel();
            }
        } else if (allControlling()
                && !isMetaDown(event) && !event.isPopupTrigger()
                && !event.isAltDown() && !event.isShiftDown() && !event.isControlDown()) {
            // not in edit mode - check if mouse is on a turnout (using wider search range)
            selectedObject = null;
            checkControls(true);
        } else if ((isMetaDown(event) || event.isAltDown())
                && !event.isShiftDown() && !event.isControlDown()) {
            // not in edit mode - check if moving a marker if there are any
            selectedObject = checkMarkerPopUps(dLoc);
            if (selectedObject != null) {
                selectedHitPointType = HitPointType.MARKER;
                startDelta = MathUtil.subtract(((LocoIcon) selectedObject).getLocation(), dLoc);
            }
        } else if (event.isPopupTrigger() && !event.isShiftDown()) {
            // not in edit mode - check if a marker popup menu is being requested
            LocoIcon lo = checkMarkerPopUps(dLoc);
            if (lo != null) {
                delayedPopupTrigger = true;
            }
        }

        if (!event.isPopupTrigger()) {
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                selections.get(0).doMousePressed(event);
            }
        }

        requestFocusInWindow();
    }   // mousePressed

// this is a method to iterate over a list of lists of items
// calling the predicate tester.test on each one
// all matching items are then added to the resulting List
// note: currently unused; commented out to avoid findbugs warning
// private static List testEachItemInListOfLists(
//        @Nonnull List<List> listOfListsOfObjects,
//        @Nonnull Predicate<Object> tester) {
//    List result = new ArrayList<>();
//    for (List<Object> listOfObjects : listOfListsOfObjects) {
//        List<Object> l = listOfObjects.stream().filter(o -> tester.test(o)).collect(Collectors.toList());
//        result.addAll(l);
//    }
//    return result;
//}
// this is a method to iterate over a list of lists of items
// calling the predicate tester.test on each one
// and return the first one that matches
// TODO: make this public? (it is useful! ;-)
// note: currently unused; commented out to avoid findbugs warning
// private static Object findFirstMatchingItemInListOfLists(
//        @Nonnull List<List> listOfListsOfObjects,
//        @Nonnull Predicate<Object> tester) {
//    Object result = null;
//    for (List listOfObjects : listOfListsOfObjects) {
//        Optional<Object> opt = listOfObjects.stream().filter(o -> tester.test(o)).findFirst();
//        if (opt.isPresent()) {
//            result = opt.get();
//            break;
//        }
//    }
//    return result;
//}
    /**
     * Called by {@link #mousePressed} to determine if the mouse click was in a
     * turnout control location. If so, update selectedHitPointType and
     * selectedObject for use by {@link #mouseReleased}.
     * <p>
     * If there's no match, selectedObject is set to null and
     * selectedHitPointType is left referring to the results of the checking the
     * last track on the list.
     * <p>
     * Refers to the current value of {@link #getLayoutTracks()} and
     * {@link #dLoc}.
     *
     * @param useRectangles set true to use rectangle; false for circles.
     */
    private void checkControls(boolean useRectangles) {
        selectedObject = null;  // deliberate side-effect
        for (LayoutTrackView theTrackView : getLayoutTrackViews()) {
            selectedHitPointType = theTrackView.findHitPointType(dLoc, useRectangles); // deliberate side-effect
            if (HitPointType.isControlHitType(selectedHitPointType)) {
                selectedObject = theTrackView.getLayoutTrack(); // deliberate side-effect
                return;
            }
        }
    }

    // This is a geometric search, and should be done with views.
    // Hence this form is inevitably temporary.
    //
    private boolean findLayoutTracksHitPoint(
            @Nonnull Point2D loc, boolean requireUnconnected) {
        return findLayoutTracksHitPoint(loc, requireUnconnected, null);
    }

    // This is a geometric search, and should be done with views.
    // Hence this form is inevitably temporary.
    //
    // optional parameter requireUnconnected
    private boolean findLayoutTracksHitPoint(@Nonnull Point2D loc) {
        return findLayoutTracksHitPoint(loc, false, null);
    }

    /**
     * Internal (private) method to find the track closest to a point, with some
     * modifiers to the search. The {@link #foundTrack} and
     * {@link #foundHitPointType} members are set from the search.
     * <p>
     * This is a geometric search, and should be done with views. Hence this
     * form is inevitably temporary.
     *
     * @param loc                Point to search from
     * @param requireUnconnected forwarded to {@link #getLayoutTrackView}; if
     *                           true, return only free connections
     * @param avoid              Don't return this track, keep searching. Note
     *                           that {@Link #selectedObject} is also always
     *                           avoided automatically
     * @returns true if values of {@link #foundTrack} and
     * {@link #foundHitPointType} correct; note they may have changed even if
     * false is returned.
     */
    private boolean findLayoutTracksHitPoint(@Nonnull Point2D loc,
            boolean requireUnconnected, @CheckForNull LayoutTrack avoid) {
        boolean result = false; // assume failure (pessimist!)

        foundTrack = null;
        foundTrackView = null;
        foundHitPointType = HitPointType.NONE;

        Optional<LayoutTrack> opt = getLayoutTracks().stream().filter(layoutTrack -> {  // != means can't (yet) loop over Views
            if ((layoutTrack != avoid) && (layoutTrack != selectedObject)) {
                foundHitPointType = getLayoutTrackView(layoutTrack).findHitPointType(loc, false, requireUnconnected);
            }
            return (HitPointType.NONE != foundHitPointType);
        }).findFirst();

        LayoutTrack layoutTrack = null;
        if (opt.isPresent()) {
            layoutTrack = opt.get();
        }

        if (layoutTrack != null) {
            foundTrack = layoutTrack;
            foundTrackView = this.getLayoutTrackView(layoutTrack);

            // get screen coordinates
            foundLocation = foundTrackView.getCoordsForConnectionType(foundHitPointType);
            /// foundNeedsConnect = isDisconnected(foundHitPointType);
            result = true;
        }
        return result;
    }

    private TrackSegment checkTrackSegmentPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        TrackSegment result = null;

        // NOTE: Rather than calculate all the hit rectangles for all
        // the points below and test if this location is in any of those
        // rectangles just create a hit rectangle for the location and
        // see if any of the points below are in it instead...
        Rectangle2D r = layoutEditorControlCircleRectAt(loc);

        // check Track Segments, if any
        for (TrackSegmentView tsv : getTrackSegmentViews()) {
            if (r.contains(tsv.getCentreSeg())) {
                result = tsv.getTrackSegment();
                break;
            }
        }
        return result;
    }

    private PositionableLabel checkBackgroundPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        PositionableLabel result = null;
        // check background images, if any
        for (int i = backgroundImage.size() - 1; i >= 0; i--) {
            PositionableLabel b = backgroundImage.get(i);
            Rectangle2D r = b.getBounds();
            if (r.contains(loc)) {
                result = b;
                break;
            }
        }
        return result;
    }

    private SensorIcon checkSensorIconPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        SensorIcon result = null;
        // check sensor images, if any
        for (int i = sensorImage.size() - 1; i >= 0; i--) {
            SensorIcon s = sensorImage.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
            }
        }
        return result;
    }

    private SignalHeadIcon checkSignalHeadIconPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        SignalHeadIcon result = null;
        // check signal head images, if any
        for (int i = signalHeadImage.size() - 1; i >= 0; i--) {
            SignalHeadIcon s = signalHeadImage.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private SignalMastIcon checkSignalMastIconPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        SignalMastIcon result = null;
        // check signal head images, if any
        for (int i = signalMastList.size() - 1; i >= 0; i--) {
            SignalMastIcon s = signalMastList.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private PositionableLabel checkLabelImagePopUps(@Nonnull Point2D loc) {
        assert loc != null;

        PositionableLabel result = null;
        int level = 0;

        for (int i = labelImage.size() - 1; i >= 0; i--) {
            PositionableLabel s = labelImage.get(i);
            double x = s.getX();
            double y = s.getY();
            double w = 10.0;
            double h = 5.0;

            if (s.isIcon() || s.isRotated() || s.getPopupUtility().getOrientation() != PositionablePopupUtil.HORIZONTAL) {
                w = s.maxWidth();
                h = s.maxHeight();
            } else if (s.isText()) {
                h = s.getFont().getSize();
                w = (h * 2 * (s.getText().length())) / 3;
            }

            Rectangle2D r = new Rectangle2D.Double(x, y, w, h);
            if (r.contains(loc)) {
                if (s.getDisplayLevel() >= level) {
                    // Check to make sure that we are returning the highest level label.
                    result = s;
                    level = s.getDisplayLevel();
                }
            }
        }
        return result;
    }

    private AnalogClock2Display checkClockPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        AnalogClock2Display result = null;
        // check clocks, if any
        for (int i = clocks.size() - 1; i >= 0; i--) {
            AnalogClock2Display s = clocks.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private MultiSensorIcon checkMultiSensorPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        MultiSensorIcon result = null;
        // check multi sensor icons, if any
        for (int i = multiSensors.size() - 1; i >= 0; i--) {
            MultiSensorIcon s = multiSensors.get(i);
            Rectangle2D r = s.getBounds();
            if (r.contains(loc)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private LocoIcon checkMarkerPopUps(@Nonnull Point2D loc) {
        assert loc != null;

        LocoIcon result = null;
        // check marker icons, if any
        for (int i = markerImage.size() - 1; i >= 0; i--) {
            LocoIcon l = markerImage.get(i);
            Rectangle2D r = l.getBounds();
            if (r.contains(loc)) {
                // mouse was pressed in marker icon
                result = l;
                break;
            }
        }
        return result;
    }

    private LayoutShape checkLayoutShapePopUps(@Nonnull Point2D loc) {
        assert loc != null;

        LayoutShape result = null;
        for (LayoutShape ls : layoutShapes) {
            selectedHitPointType = ls.findHitPointType(loc, true);
            if (LayoutShape.isShapeHitPointType(selectedHitPointType)) {
                result = ls;
                break;
            }
        }
        return result;
    }

    /**
     * Get the coordinates for the connection type of the specified LayoutTrack
     * or subtype.
     * <p>
     * This uses the current LayoutEditor object to map a LayoutTrack (no
     * coordinates) object to _a_ specific LayoutTrackView object in the current
     * LayoutEditor i.e. window. This allows the same model object in two
     * windows, but not twice in a single window.
     * <p>
     * This is temporary, and needs to go away as the LayoutTrack doesn't
     * logically have position; just the LayoutTrackView does, and multiple
     * LayoutTrackViews can refer to one specific LayoutTrack.
     *
     * @param trk            the object (LayoutTrack subclass)
     * @param connectionType the type of connection
     * @return the coordinates for the connection type of the specified object
     */
    @Nonnull
    public Point2D getCoords(@Nonnull LayoutTrack trk, HitPointType connectionType) {
        assert trk != null;

        return getCoords(getLayoutTrackView(trk), connectionType);
    }

    /**
     * Get the coordinates for the connection type of the specified
     * LayoutTrackView or subtype.
     *
     * @param trkv           the object (LayoutTrackView subclass)
     * @param connectionType the type of connection
     * @return the coordinates for the connection type of the specified object
     */
    @Nonnull
    public Point2D getCoords(@Nonnull LayoutTrackView trkv, HitPointType connectionType) {
        assert trkv != null;

        return trkv.getCoordsForConnectionType(connectionType);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        super.setToolTip(null);

        // initialize mouse position
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if (isEditable()) {
            leToolBarPanel.setLocationText(dLoc);

            // released the mouse with shift down... see what we're adding
            if (!event.isPopupTrigger() && !isMetaDown(event) && event.isShiftDown()) {

                currentPoint = new Point2D.Double(xLoc, yLoc);

                if (snapToGridOnAdd != snapToGridInvert) {
                    // this snaps the current point to the grid
                    currentPoint = MathUtil.granulize(currentPoint, gContext.getGridSize());
                    xLoc = (int) currentPoint.getX();
                    yLoc = (int) currentPoint.getY();
                    leToolBarPanel.setLocationText(currentPoint);
                }

                if (leToolBarPanel.turnoutRHButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.RH_TURNOUT);
                } else if (leToolBarPanel.turnoutLHButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.LH_TURNOUT);
                } else if (leToolBarPanel.turnoutWYEButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.WYE_TURNOUT);
                } else if (leToolBarPanel.doubleXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.DOUBLE_XOVER);
                } else if (leToolBarPanel.rhXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.RH_XOVER);
                } else if (leToolBarPanel.lhXoverButton.isSelected()) {
                    addLayoutTurnout(LayoutTurnout.TurnoutType.LH_XOVER);
                } else if (leToolBarPanel.levelXingButton.isSelected()) {
                    addLevelXing();
                } else if (leToolBarPanel.layoutSingleSlipButton.isSelected()) {
                    addLayoutSlip(LayoutSlip.TurnoutType.SINGLE_SLIP);
                } else if (leToolBarPanel.layoutDoubleSlipButton.isSelected()) {
                    addLayoutSlip(LayoutSlip.TurnoutType.DOUBLE_SLIP);
                } else if (leToolBarPanel.endBumperButton.isSelected()) {
                    addEndBumper();
                } else if (leToolBarPanel.anchorButton.isSelected()) {
                    addAnchor();
                } else if (leToolBarPanel.edgeButton.isSelected()) {
                    addEdgeConnector();
                } else if (leToolBarPanel.trackButton.isSelected()) {
                    if ((beginTrack != null) && (foundTrack != null)
                            && (beginTrack != foundTrack)) {
                        addTrackSegment();
                        _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                    beginTrack = null;
                    foundTrack = null;
                    foundTrackView = null;
                } else if (leToolBarPanel.multiSensorButton.isSelected()) {
                    startMultiSensor();
                } else if (leToolBarPanel.sensorButton.isSelected()) {
                    addSensor();
                } else if (leToolBarPanel.signalButton.isSelected()) {
                    addSignalHead();
                } else if (leToolBarPanel.textLabelButton.isSelected()) {
                    addLabel();
                } else if (leToolBarPanel.memoryButton.isSelected()) {
                    addMemory();
                } else if (leToolBarPanel.blockContentsButton.isSelected()) {
                    addBlockContents();
                } else if (leToolBarPanel.iconLabelButton.isSelected()) {
                    addIcon();
                } else if (leToolBarPanel.shapeButton.isSelected()) {
                    if (selectedObject == null) {
                        addLayoutShape(currentPoint);
                        _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    } else {
                        LayoutShape ls = (LayoutShape) selectedObject;
                        ls.addPoint(currentPoint, selectedHitPointType.shapePointIndex());
                    }
                } else if (leToolBarPanel.signalMastButton.isSelected()) {
                    addSignalMast();
                } else {
                    log.warn("No item selected in panel edit mode");
                }
                // resizePanelBounds(false);
                selectedObject = null;
                redrawPanel();
            } else if ((event.isPopupTrigger() || delayedPopupTrigger) && !isDragging) {
                selectedObject = null;
                selectedHitPointType = HitPointType.NONE;
                whenReleased = event.getWhen();
                showEditPopUps(event);
            } else if ((selectedObject != null) && (selectedHitPointType == HitPointType.TURNOUT_CENTER)
                    && allControlling() && (!isMetaDown(event) && !event.isAltDown()) && !event.isPopupTrigger()
                    && !event.isShiftDown() && !event.isControlDown()) {
                // controlling turnouts, in edit mode
                LayoutTurnout t = (LayoutTurnout) selectedObject;
                t.toggleTurnout();
            } else if ((selectedObject != null) && ((selectedHitPointType == HitPointType.SLIP_LEFT)
                    || (selectedHitPointType == HitPointType.SLIP_RIGHT))
                    && allControlling() && (!isMetaDown(event) && !event.isAltDown()) && !event.isPopupTrigger()
                    && !event.isShiftDown() && !event.isControlDown()) {
                // controlling slips, in edit mode
                LayoutSlip sl = (LayoutSlip) selectedObject;
                sl.toggleState(selectedHitPointType);
            } else if ((selectedObject != null) && (HitPointType.isTurntableRayHitType(selectedHitPointType))
                    && allControlling() && (!isMetaDown(event) && !event.isAltDown()) && !event.isPopupTrigger()
                    && !event.isShiftDown() && !event.isControlDown()) {
                // controlling turntable, in edit mode
                LayoutTurntable t = (LayoutTurntable) selectedObject;
                t.setPosition(selectedHitPointType.turntableTrackIndex());
            } else if ((selectedObject != null) && ((selectedHitPointType == HitPointType.TURNOUT_CENTER)
                    || (selectedHitPointType == HitPointType.SLIP_CENTER)
                    || (selectedHitPointType == HitPointType.SLIP_LEFT)
                    || (selectedHitPointType == HitPointType.SLIP_RIGHT))
                    && allControlling() && (isMetaDown(event) && !event.isAltDown())
                    && !event.isShiftDown() && !event.isControlDown() && isDragging) {
                // We just dropped a turnout (or slip)... see if it will connect to anything
                hitPointCheckLayoutTurnouts((LayoutTurnout) selectedObject);
            } else if ((selectedObject != null) && (selectedHitPointType == HitPointType.POS_POINT)
                    && allControlling() && (isMetaDown(event))
                    && !event.isShiftDown() && !event.isControlDown() && isDragging) {
                // We just dropped a PositionablePoint... see if it will connect to anything
                PositionablePoint p = (PositionablePoint) selectedObject;
                if ((p.getConnect1() == null) || (p.getConnect2() == null)) {
                    checkPointOfPositionable(p);
                }
            }

            if ((leToolBarPanel.trackButton.isSelected()) && (beginTrack != null) && (foundTrack != null)) {
                // user let up shift key before releasing the mouse when creating a track segment
                _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                beginTrack = null;
                foundTrack = null;
                foundTrackView = null;
                redrawPanel();
            }
            createSelectionGroups();
        } else if ((selectedObject != null) && (selectedHitPointType == HitPointType.TURNOUT_CENTER)
                && allControlling() && !isMetaDown(event) && !event.isAltDown() && !event.isPopupTrigger()
                && !event.isShiftDown() && (!delayedPopupTrigger)) {
            // controlling turnout out of edit mode
            LayoutTurnout t = (LayoutTurnout) selectedObject;
            if (useDirectTurnoutControl) {
                t.setState(Turnout.CLOSED);
            } else {
                t.toggleTurnout();
            }
        } else if ((selectedObject != null) && ((selectedHitPointType == HitPointType.SLIP_LEFT)
                || (selectedHitPointType == HitPointType.SLIP_RIGHT))
                && allControlling() && !isMetaDown(event) && !event.isAltDown() && !event.isPopupTrigger()
                && !event.isShiftDown() && (!delayedPopupTrigger)) {
            // controlling slip out of edit mode
            LayoutSlip sl = (LayoutSlip) selectedObject;
            sl.toggleState(selectedHitPointType);
        } else if ((selectedObject != null) && (HitPointType.isTurntableRayHitType(selectedHitPointType))
                && allControlling() && !isMetaDown(event) && !event.isAltDown() && !event.isPopupTrigger()
                && !event.isShiftDown() && (!delayedPopupTrigger)) {
            // controlling turntable out of edit mode
            LayoutTurntable t = (LayoutTurntable) selectedObject;
            t.setPosition(selectedHitPointType.turntableTrackIndex());
        } else if ((event.isPopupTrigger() || delayedPopupTrigger) && (!isDragging)) {
            // requesting marker popup out of edit mode
            LocoIcon lo = checkMarkerPopUps(dLoc);
            if (lo != null) {
                showPopUp(lo, event);
            } else {
                if (findLayoutTracksHitPoint(dLoc)) {
                    // show popup menu
                    switch (foundHitPointType) {
                        case TURNOUT_CENTER: {
                            if (useDirectTurnoutControl) {
                                LayoutTurnout t = (LayoutTurnout) foundTrack;
                                t.setState(Turnout.THROWN);
                            } else {
                                foundTrackView.showPopup(event);
                            }
                            break;
                        }

                        case LEVEL_XING_CENTER:
                        case SLIP_RIGHT:
                        case SLIP_LEFT: {
                            foundTrackView.showPopup(event);
                            break;
                        }

                        default: {
                            break;
                        }
                    }
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
                selections.get(0).doMouseReleased(event);
                whenReleased = event.getWhen();
            }
        }

        // train icon needs to know when moved
        if (event.isPopupTrigger() && isDragging) {
            List<Positionable> selections = getSelectedItems(event);
            if (selections.size() > 0) {
                selections.get(0).doMouseDragged(event);
            }
        }

        if (selectedObject != null) {
            // An object was selected, deselect it
            prevSelectedObject = selectedObject;
            selectedObject = null;
        }

        // clear these
        beginTrack = null;
        foundTrack = null;
        foundTrackView = null;

        delayedPopupTrigger = false;

        if (isDragging) {
            resizePanelBounds(true);
            isDragging = false;
        }

        requestFocusInWindow();
    }   // mouseReleased

    private void showEditPopUps(@Nonnull MouseEvent event) {
        if (findLayoutTracksHitPoint(dLoc)) {
            if (HitPointType.isBezierHitType(foundHitPointType)) {
                getTrackSegmentView((TrackSegment) foundTrack).showBezierPopUp(event, foundHitPointType);
            } else if (HitPointType.isTurntableRayHitType(foundHitPointType)) {
                LayoutTurntable t = (LayoutTurntable) foundTrack;
                if (t.isTurnoutControlled()) {
                    LayoutTurntableView ltview = getLayoutTurntableView((LayoutTurntable) foundTrack);
                    ltview.showRayPopUp(event, foundHitPointType.turntableTrackIndex());
                }
            } else if (HitPointType.isPopupHitType(foundHitPointType)) {
                foundTrackView.showPopup(event);
            } else if (HitPointType.isTurnoutHitType(foundHitPointType)) {
                // don't curently have edit popup for these
            } else {
                log.warn("Unknown foundPointType:{}", foundHitPointType);
            }
        } else {
            do {
                TrackSegment ts = checkTrackSegmentPopUps(dLoc);
                if (ts != null) {
                    TrackSegmentView tsv = getTrackSegmentView(ts);
                    tsv.showPopup(event);
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
                LayoutShape ls = checkLayoutShapePopUps(dLoc);
                if (ls != null) {
                    ls.showShapePopUp(event, selectedHitPointType);
                    break;
                }
            } while (false);
        }
    }

    /**
     * Select the menu items to display for the Positionable's popup.
     */
    @Override
    public void showPopUp(@Nonnull Positionable p, @Nonnull MouseEvent event) {
        assert p != null;

        if (!((Component) p).isVisible()) {
            return; // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            JMenuItem jmi;

            if (showAlignPopup()) {
                setShowAlignmentMenu(popup);
                popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        deleteSelectedItems();
                    }
                });
            } else {
                if (p.doViemMenu()) {
                    String objectType = p.getClass().getName();
                    objectType = objectType.substring(objectType.lastIndexOf('.') + 1);
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
                popupSet |= p.setEditIconMenu(popup);
                popupSet |= p.setTextEditMenu(popup);

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
                    // popupSet = false;
                }
                p.setDisableControlMenu(popup);
                setShowAlignmentMenu(popup);

                // for Positionables with unique settings
                p.showPopUp(popup);
                setShowToolTipMenu(p, popup);

                setRemoveMenu(p, popup);

                if (p.doViemMenu()) {
                    setHiddenMenu(p, popup);
                    setEditIdMenu(p, popup);
                }
            }
        } else {
            p.showPopUp(popup);
            PositionablePopupUtil util = p.getPopupUtility();

            if (util != null) {
                util.setAdditionalViewPopUpMenu(popup);
            }
        }
        popup.show((Component) p, p.getWidth() / 2 + (int) ((getZoom() - 1.0) * p.getX()),
                p.getHeight() / 2 + (int) ((getZoom() - 1.0) * p.getY()));

        /*popup.show((Component)pt, event.getX(), event.getY());*/
    }

    private long whenReleased = 0; // used to identify event that was popup trigger
    private boolean awaitingIconChange = false;

    @Override
    public void mouseClicked(@Nonnull MouseEvent event) {
        // initialize mouse position
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if (!isMetaDown(event) && !event.isPopupTrigger() && !event.isAltDown()
                && !awaitingIconChange && !event.isShiftDown() && !event.isControlDown()) {
            List<Positionable> selections = getSelectedItems(event);

            if (selections.size() > 0) {
                selections.get(0).doMouseClicked(event);
            }
        } else if (event.isPopupTrigger() && (whenReleased != event.getWhen())) {

            if (isEditable()) {
                selectedObject = null;
                selectedHitPointType = HitPointType.NONE;
                showEditPopUps(event);
            } else {
                LocoIcon lo = checkMarkerPopUps(dLoc);

                if (lo != null) {
                    showPopUp(lo, event);
                }
            }
        }

        if (event.isControlDown() && !event.isPopupTrigger()) {
            if (findLayoutTracksHitPoint(dLoc)) {
                switch (foundHitPointType) {
                    case POS_POINT:
                    case TURNOUT_CENTER:
                    case LEVEL_XING_CENTER:
                    case SLIP_LEFT:
                    case SLIP_RIGHT:
                    case TURNTABLE_CENTER: {
                        amendSelectionGroup(foundTrack);
                        break;
                    }

                    default: {
                        break;
                    }
                }
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
                                    } else {
                                        LayoutShape ls = checkLayoutShapePopUps(dLoc);
                                        if (ls != null) {
                                            amendSelectionGroup(ls);
                                        }
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
        requestFocusInWindow();
    }

    private void checkPointOfPositionable(@Nonnull PositionablePoint p) {
        assert p != null;

        TrackSegment t = p.getConnect1();

        if (t == null) {
            t = p.getConnect2();
        }

        // Nothing connected to this bit of track so ignore
        if (t == null) {
            return;
        }
        beginTrack = p;
        beginHitPointType = HitPointType.POS_POINT;
        PositionablePointView pv = getPositionablePointView(p);
        Point2D loc = pv.getCoordsCenter();

        if (findLayoutTracksHitPoint(loc, true, p)) {
            switch (foundHitPointType) {
                case POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundTrack;

                    if ((p2.getType() == PositionablePoint.PointType.ANCHOR) && p2.setTrackConnection(t)) {
                        if (t.getConnect1() == p) {
                            t.setNewConnect1(p2, foundHitPointType);
                        } else {
                            t.setNewConnect2(p2, foundHitPointType);
                        }
                        p.removeTrackConnection(t);

                        if ((p.getConnect1() == null) && (p.getConnect2() == null)) {
                            removePositionablePoint(p);
                        }
                    }
                    break;
                }
                case TURNOUT_A:
                case TURNOUT_B:
                case TURNOUT_C:
                case TURNOUT_D:
                case SLIP_A:
                case SLIP_B:
                case SLIP_C:
                case SLIP_D:
                case LEVEL_XING_A:
                case LEVEL_XING_B:
                case LEVEL_XING_C:
                case LEVEL_XING_D: {
                    try {
                        if (foundTrack.getConnection(foundHitPointType) == null) {
                            foundTrack.setConnection(foundHitPointType, t, HitPointType.TRACK);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(foundTrack, foundHitPointType);
                            } else {
                                t.setNewConnect2(foundTrack, foundHitPointType);
                            }
                            p.removeTrackConnection(t);

                            if ((p.getConnect1() == null) && (p.getConnect2() == null)) {
                                removePositionablePoint(p);
                            }
                        }
                    } catch (JmriException e) {
                        log.debug("Unable to set location");
                    }
                    break;
                }

                default: {
                    if (HitPointType.isTurntableRayHitType(foundHitPointType)) {
                        LayoutTurntable tt = (LayoutTurntable) foundTrack;
                        int ray = foundHitPointType.turntableTrackIndex();

                        if (tt.getRayConnectIndexed(ray) == null) {
                            tt.setRayConnect(t, ray);

                            if (t.getConnect1() == p) {
                                t.setNewConnect1(tt, foundHitPointType);
                            } else {
                                t.setNewConnect2(tt, foundHitPointType);
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
            }
            redrawPanel();

            if (t.getLayoutBlock() != null) {
                getLEAuxTools().setBlockConnectivityChanged();
            }
        }
        beginTrack = null;
    }

    // We just dropped a turnout... see if it will connect to anything
    private void hitPointCheckLayoutTurnouts(@Nonnull LayoutTurnout lt) {
        beginTrack = lt;

        LayoutTurnoutView ltv = getLayoutTurnoutView(lt);

        if (lt.getConnectA() == null) {
            if (lt instanceof LayoutSlip) {
                beginHitPointType = HitPointType.SLIP_A;
            } else {
                beginHitPointType = HitPointType.TURNOUT_A;
            }
            dLoc = ltv.getCoordsA();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if (lt.getConnectB() == null) {
            if (lt instanceof LayoutSlip) {
                beginHitPointType = HitPointType.SLIP_B;
            } else {
                beginHitPointType = HitPointType.TURNOUT_B;
            }
            dLoc = ltv.getCoordsB();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if (lt.getConnectC() == null) {
            if (lt instanceof LayoutSlip) {
                beginHitPointType = HitPointType.SLIP_C;
            } else {
                beginHitPointType = HitPointType.TURNOUT_C;
            }
            dLoc = ltv.getCoordsC();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }

        if ((lt.getConnectD() == null) && (lt.isTurnoutTypeXover() || lt.isTurnoutTypeSlip())) {
            if (lt instanceof LayoutSlip) {
                beginHitPointType = HitPointType.SLIP_D;
            } else {
                beginHitPointType = HitPointType.TURNOUT_D;
            }
            dLoc = ltv.getCoordsD();
            hitPointCheckLayoutTurnoutSubs(dLoc);
        }
        beginTrack = null;
        foundTrack = null;
        foundTrackView = null;
    }

    private void hitPointCheckLayoutTurnoutSubs(@Nonnull Point2D dLoc) {
        assert dLoc != null;

        if (findLayoutTracksHitPoint(dLoc, true)) {
            switch (foundHitPointType) {
                case POS_POINT: {
                    PositionablePoint p2 = (PositionablePoint) foundTrack;

                    if (((p2.getConnect1() == null) && (p2.getConnect2() != null))
                            || ((p2.getConnect1() != null) && (p2.getConnect2() == null))) {
                        TrackSegment t = p2.getConnect1();

                        if (t == null) {
                            t = p2.getConnect2();
                        }

                        if (t == null) {
                            return;
                        }
                        LayoutTurnout lt = (LayoutTurnout) beginTrack;
                        try {
                            if (lt.getConnection(beginHitPointType) == null) {
                                lt.setConnection(beginHitPointType, t, HitPointType.TRACK);
                                p2.removeTrackConnection(t);

                                if (t.getConnect1() == p2) {
                                    t.setNewConnect1(lt, beginHitPointType);
                                } else {
                                    t.setNewConnect2(lt, beginHitPointType);
                                }
                                removePositionablePoint(p2);
                            }

                            if (t.getLayoutBlock() != null) {
                                getLEAuxTools().setBlockConnectivityChanged();
                            }
                        } catch (JmriException e) {
                            log.debug("Unable to set location");
                        }
                    }
                    break;
                }

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
                    ((LayoutTurnoutView) foundTrackView).setTrackSegmentBlock(foundHitPointType, true);
                    break;
                }

                default: {
                    log.warn("Unexpected foundPointType {} in hitPointCheckLayoutTurnoutSubs", foundHitPointType);
                    break;
                }
            }
        }
    }

    private void rotateTurnout(@Nonnull LayoutTurnout t) {
        assert t != null;

        LayoutTurnoutView tv = getLayoutTurnoutView(t);

        LayoutTurnout be = (LayoutTurnout) beginTrack;
        LayoutTurnoutView bev = getLayoutTurnoutView(be);

        if (((beginHitPointType == HitPointType.TURNOUT_A) && ((be.getConnectB() != null) || (be.getConnectC() != null)))
                || ((beginHitPointType == HitPointType.TURNOUT_B) && ((be.getConnectA() != null) || (be.getConnectC() != null)))
                || ((beginHitPointType == HitPointType.TURNOUT_C) && ((be.getConnectB() != null) || (be.getConnectA() != null)))) {
            return;
        }

        if ((be.getTurnoutType() != LayoutTurnout.TurnoutType.RH_TURNOUT) && (be.getTurnoutType() != LayoutTurnout.TurnoutType.LH_TURNOUT)) {
            return;
        }

        Point2D c, diverg, xy2;

        if ((foundHitPointType == HitPointType.TURNOUT_C) && (beginHitPointType == HitPointType.TURNOUT_C)) {
            c = tv.getCoordsA();
            diverg = tv.getCoordsB();
            xy2 = MathUtil.subtract(c, diverg);
        } else if ((foundHitPointType == HitPointType.TURNOUT_C)
                && ((beginHitPointType == HitPointType.TURNOUT_A) || (beginHitPointType == HitPointType.TURNOUT_B))) {

            c = tv.getCoordsCenter();
            diverg = tv.getCoordsC();

            if (beginHitPointType == HitPointType.TURNOUT_A) {
                xy2 = MathUtil.subtract(bev.getCoordsB(), bev.getCoordsA());
            } else {
                xy2 = MathUtil.subtract(bev.getCoordsA(), bev.getCoordsB());
            }
        } else if (foundHitPointType == HitPointType.TURNOUT_B) {
            c = tv.getCoordsA();
            diverg = tv.getCoordsB();

            switch (beginHitPointType) {
                case TURNOUT_B:
                    xy2 = MathUtil.subtract(bev.getCoordsA(), bev.getCoordsB());
                    break;
                case TURNOUT_A:
                    xy2 = MathUtil.subtract(bev.getCoordsB(), bev.getCoordsA());
                    break;
                case TURNOUT_C:
                default:
                    xy2 = MathUtil.subtract(bev.getCoordsCenter(), bev.getCoordsC());
                    break;
            }
        } else if (foundHitPointType == HitPointType.TURNOUT_A) {
            c = tv.getCoordsA();
            diverg = tv.getCoordsB();

            switch (beginHitPointType) {
                case TURNOUT_A:
                    xy2 = MathUtil.subtract(bev.getCoordsA(), bev.getCoordsB());
                    break;
                case TURNOUT_B:
                    xy2 = MathUtil.subtract(bev.getCoordsB(), bev.getCoordsA());
                    break;
                case TURNOUT_C:
                default:
                    xy2 = MathUtil.subtract(bev.getCoordsC(), bev.getCoordsCenter());
                    break;
            }
        } else {
            return;
        }
        Point2D xy = MathUtil.subtract(diverg, c);
        double radius = Math.toDegrees(Math.atan2(xy.getY(), xy.getX()));
        double eRadius = Math.toDegrees(Math.atan2(xy2.getY(), xy2.getX()));
        bev.rotateCoords(radius - eRadius);

        Point2D conCord = bev.getCoordsA();
        Point2D tCord = tv.getCoordsC();

        if (foundHitPointType == HitPointType.TURNOUT_B) {
            tCord = tv.getCoordsB();
        }

        if (foundHitPointType == HitPointType.TURNOUT_A) {
            tCord = tv.getCoordsA();
        }

        switch (beginHitPointType) {
            case TURNOUT_A:
                conCord = bev.getCoordsA();
                break;
            case TURNOUT_B:
                conCord = bev.getCoordsB();
                break;
            case TURNOUT_C:
                conCord = bev.getCoordsC();
                break;
            default:
                break;
        }
        xy = MathUtil.subtract(conCord, tCord);
        Point2D offset = MathUtil.subtract(bev.getCoordsCenter(), xy);
        bev.setCoordsCenter(offset);
    }

    public List<Positionable> _positionableSelection = new ArrayList<>();
    public List<LayoutTrack> _layoutTrackSelection = new ArrayList<>();
    public List<LayoutShape> _layoutShapeSelection = new ArrayList<>();

    @Nonnull
    public List<Positionable> getPositionalSelection() {
        return _positionableSelection;
    }

    @Nonnull
    public List<LayoutTrack> getLayoutTrackSelection() {
        return _layoutTrackSelection;
    }

    @Nonnull
    public List<LayoutShape> getLayoutShapeSelection() {
        return _layoutShapeSelection;
    }

    private void createSelectionGroups() {
        Rectangle2D selectionRect = getSelectionRect();

        getContents().forEach((o) -> {
            if (selectionRect.contains(o.getLocation())) {

                log.trace("found item o of class {}", o.getClass());
                if (!_positionableSelection.contains(o)) {
                    _positionableSelection.add(o);
                }
            }
        });

        getLayoutTracks().forEach((lt) -> {
            LayoutTrackView ltv = getLayoutTrackView(lt);
            Point2D center = ltv.getCoordsCenter();
            if (selectionRect.contains(center)) {
                if (!_layoutTrackSelection.contains(lt)) {
                    _layoutTrackSelection.add(lt);
                }
            }
        });
        assignBlockToSelectionMenuItem.setEnabled(_layoutTrackSelection.size() > 0);

        layoutShapes.forEach((ls) -> {
            if (selectionRect.intersects(ls.getBounds())) {
                if (!_layoutShapeSelection.contains(ls)) {
                    _layoutShapeSelection.add(ls);
                }
            }
        });
        redrawPanel();
    }

    public void clearSelectionGroups() {
        selectionActive = false;
        _positionableSelection.clear();
        _layoutTrackSelection.clear();
        assignBlockToSelectionMenuItem.setEnabled(false);
        _layoutShapeSelection.clear();
    }

    private boolean noWarnGlobalDelete = false;

    private void deleteSelectedItems() {
        if (!noWarnGlobalDelete) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question6"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return; // return without creating if "No" response
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                // Suppress future warnings, and continue
                noWarnGlobalDelete = true;
            }
        }

        _positionableSelection.forEach(this::remove);

        _layoutTrackSelection.forEach((lt) -> {
            if (lt instanceof PositionablePoint) {
                boolean oldWarning = noWarnPositionablePoint;
                noWarnPositionablePoint = true;
                removePositionablePoint((PositionablePoint) lt);
                noWarnPositionablePoint = oldWarning;
            } else if (lt instanceof LevelXing) {
                boolean oldWarning = noWarnLevelXing;
                noWarnLevelXing = true;
                removeLevelXing((LevelXing) lt);
                noWarnLevelXing = oldWarning;
            } else if (lt instanceof LayoutSlip) {
                boolean oldWarning = noWarnSlip;
                noWarnSlip = true;
                removeLayoutSlip((LayoutSlip) lt);
                noWarnSlip = oldWarning;
            } else if (lt instanceof LayoutTurntable) {
                boolean oldWarning = noWarnTurntable;
                noWarnTurntable = true;
                removeTurntable((LayoutTurntable) lt);
                noWarnTurntable = oldWarning;
            } else if (lt instanceof LayoutTurnout) {  //<== this includes LayoutSlips
                boolean oldWarning = noWarnLayoutTurnout;
                noWarnLayoutTurnout = true;
                removeLayoutTurnout((LayoutTurnout) lt);
                noWarnLayoutTurnout = oldWarning;
            }
        });

        layoutShapes.removeAll(_layoutShapeSelection);

        clearSelectionGroups();
        redrawPanel();
    }

    private void amendSelectionGroup(@Nonnull Positionable p) {
        assert p != null;

        if (_positionableSelection.contains(p)) {
            _positionableSelection.remove(p);
        } else {
            _positionableSelection.add(p);
        }
        redrawPanel();
    }

    public void amendSelectionGroup(@Nonnull LayoutTrack p) {
        assert p != null;

        if (_layoutTrackSelection.contains(p)) {
            _layoutTrackSelection.remove(p);
        } else {
            _layoutTrackSelection.add(p);
        }
        assignBlockToSelectionMenuItem.setEnabled(_layoutTrackSelection.size() > 0);
        redrawPanel();
    }

    public void amendSelectionGroup(@Nonnull LayoutShape ls) {
        assert ls != null;

        if (_layoutShapeSelection.contains(ls)) {
            _layoutShapeSelection.remove(ls);
        } else {
            _layoutShapeSelection.add(ls);
        }
        redrawPanel();
    }

    public void alignSelection(boolean alignX) {
        Point2D minPoint = MathUtil.infinityPoint2D;
        Point2D maxPoint = MathUtil.zeroPoint2D;
        Point2D sumPoint = MathUtil.zeroPoint2D;
        int cnt = 0;

        for (Positionable comp : _positionableSelection) {
            if (!getFlag(Editor.OPTION_POSITION, comp.isPositionable())) {
                continue;   // skip non-positionables
            }
            Point2D p = MathUtil.pointToPoint2D(comp.getLocation());
            minPoint = MathUtil.min(minPoint, p);
            maxPoint = MathUtil.max(maxPoint, p);
            sumPoint = MathUtil.add(sumPoint, p);
            cnt++;
        }

        for (LayoutTrack lt : _layoutTrackSelection) {
            LayoutTrackView ltv = getLayoutTrackView(lt);
            Point2D p = ltv.getCoordsCenter();
            minPoint = MathUtil.min(minPoint, p);
            maxPoint = MathUtil.max(maxPoint, p);
            sumPoint = MathUtil.add(sumPoint, p);
            cnt++;
        }

        for (LayoutShape ls : _layoutShapeSelection) {
            Point2D p = ls.getCoordsCenter();
            minPoint = MathUtil.min(minPoint, p);
            maxPoint = MathUtil.max(maxPoint, p);
            sumPoint = MathUtil.add(sumPoint, p);
            cnt++;
        }

        Point2D avePoint = MathUtil.divide(sumPoint, cnt);
        int aveX = (int) avePoint.getX();
        int aveY = (int) avePoint.getY();

        for (Positionable comp : _positionableSelection) {
            if (!getFlag(Editor.OPTION_POSITION, comp.isPositionable())) {
                continue;   // skip non-positionables
            }

            if (alignX) {
                comp.setLocation(aveX, comp.getY());
            } else {
                comp.setLocation(comp.getX(), aveY);
            }
        }

        _layoutTrackSelection.forEach((lt) -> {
            LayoutTrackView ltv = getLayoutTrackView(lt);
            if (alignX) {
                ltv.setCoordsCenter(new Point2D.Double(aveX, ltv.getCoordsCenter().getY()));
            } else {
                ltv.setCoordsCenter(new Point2D.Double(ltv.getCoordsCenter().getX(), aveY));
            }
        });

        _layoutShapeSelection.forEach((ls) -> {
            if (alignX) {
                ls.setCoordsCenter(new Point2D.Double(aveX, ls.getCoordsCenter().getY()));
            } else {
                ls.setCoordsCenter(new Point2D.Double(ls.getCoordsCenter().getX(), aveY));
            }
        });

        redrawPanel();
    }

    private boolean showAlignPopup() {
        return ((_positionableSelection.size() > 0)
                || (_layoutTrackSelection.size() > 0)
                || (_layoutShapeSelection.size() > 0));
    }

    /**
     * Offer actions to align the selected Positionable items either
     * Horizontally (at average y coord) or Vertically (at average x coord).
     *
     * @param popup the JPopupMenu to add alignment menu to
     * @return true if alignment menu added
     */
    public boolean setShowAlignmentMenu(@Nonnull JPopupMenu popup) {
        if (showAlignPopup()) {
            JMenu edit = new JMenu(Bundle.getMessage("EditAlignment"));
            edit.add(new AbstractAction(Bundle.getMessage("AlignX")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    alignSelection(true);
                }
            });
            edit.add(new AbstractAction(Bundle.getMessage("AlignY")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    alignSelection(false);
                }
            });
            popup.add(edit);

            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(@Nonnull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedItems();
            return;
        }

        double deltaX = returnDeltaPositionX(event);
        double deltaY = returnDeltaPositionY(event);

        if ((deltaX != 0) || (deltaY != 0)) {
            selectionX += deltaX;
            selectionY += deltaY;

            Point2D delta = new Point2D.Double(deltaX, deltaY);
            _positionableSelection.forEach((c) -> {
                Point2D newPoint = c.getLocation();
                if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth() == 0)) {
                    MemoryIcon pm = (MemoryIcon) c;
                    newPoint = new Point2D.Double(pm.getOriginalX(), pm.getOriginalY());
                }
                newPoint = MathUtil.add(newPoint, delta);
                newPoint = MathUtil.max(MathUtil.zeroPoint2D, newPoint);
                c.setLocation(MathUtil.point2DToPoint(newPoint));
            });

            _layoutTrackSelection.forEach((lt) -> {
                LayoutTrackView ltv = getLayoutTrackView(lt);
                Point2D newPoint = MathUtil.add(ltv.getCoordsCenter(), delta);
                newPoint = MathUtil.max(MathUtil.zeroPoint2D, newPoint);
                getLayoutTrackView(lt).setCoordsCenter(newPoint);
            });

            _layoutShapeSelection.forEach((ls) -> {
                Point2D newPoint = MathUtil.add(ls.getCoordsCenter(), delta);
                newPoint = MathUtil.max(MathUtil.zeroPoint2D, newPoint);
                ls.setCoordsCenter(newPoint);
            });
            redrawPanel();
            return;
        }
        getLayoutEditorToolBarPanel().keyPressed(event);
    }

    private double returnDeltaPositionX(@Nonnull KeyEvent event) {
        double result = 0.0;
        double amount = event.isShiftDown() ? 5.0 : 1.0;

        switch (event.getKeyCode()) {
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
        }
        return result;
    }

    private double returnDeltaPositionY(@Nonnull KeyEvent event) {
        double result = 0.0;
        double amount = event.isShiftDown() ? 5.0 : 1.0;

        switch (event.getKeyCode()) {
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
        }
        return result;
    }

    int _prevNumSel = 0;

    @Override
    public void mouseMoved(@Nonnull MouseEvent event) {
        // initialize mouse position
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        if (isEditable()) {
            leToolBarPanel.setLocationText(dLoc);
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

        if (findLayoutTracksHitPoint(dLoc)) {
            // log.debug("foundTrack: {}", foundTrack);
            if (HitPointType.isControlHitType(foundHitPointType)) {
                _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
            foundTrack = null;
            foundHitPointType = HitPointType.NONE;
        } else {
            _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }   // mouseMoved

    private boolean isDragging = false;

    @Override
    public void mouseDragged(@Nonnull MouseEvent event) {
        // initialize mouse position
        calcLocation(event);

        // ignore this event if still at the original point
        if ((!isDragging) && (xLoc == getAnchorX()) && (yLoc == getAnchorY())) {
            return;
        }

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        // process this mouse dragged event
        if (isEditable()) {
            leToolBarPanel.setLocationText(dLoc);
        }
        currentPoint = MathUtil.add(dLoc, startDelta);
        // don't allow negative placement, objects could become unreachable
        currentPoint = MathUtil.max(currentPoint, MathUtil.zeroPoint2D);

        if ((selectedObject != null) && (isMetaDown(event) || event.isAltDown())
                && (selectedHitPointType == HitPointType.MARKER)) {
            // marker moves regardless of editMode or positionable
            PositionableLabel pl = (PositionableLabel) selectedObject;
            pl.setLocation((int) currentPoint.getX(), (int) currentPoint.getY());
            isDragging = true;
            redrawPanel();
            return;
        }

        if (isEditable()) {
            if ((selectedObject != null) && isMetaDown(event) && allPositionable()) {
                if (snapToGridOnMove != snapToGridInvert) {
                    // this snaps currentPoint to the grid
                    currentPoint = MathUtil.granulize(currentPoint, gContext.getGridSize());
                    xLoc = (int) currentPoint.getX();
                    yLoc = (int) currentPoint.getY();
                    leToolBarPanel.setLocationText(currentPoint);
                }

                if ((_positionableSelection.size() > 0)
                        || (_layoutTrackSelection.size() > 0)
                        || (_layoutShapeSelection.size() > 0)) {
                    Point2D lastPoint = new Point2D.Double(_lastX, _lastY);
                    Point2D offset = MathUtil.subtract(currentPoint, lastPoint);
                    Point2D newPoint;

                    for (Positionable c : _positionableSelection) {
                        if ((c instanceof MemoryIcon) && (c.getPopupUtility().getFixedWidth() == 0)) {
                            MemoryIcon pm = (MemoryIcon) c;
                            newPoint = new Point2D.Double(pm.getOriginalX(), pm.getOriginalY());
                        } else {
                            newPoint = c.getLocation();
                        }
                        newPoint = MathUtil.add(newPoint, offset);
                        // don't allow negative placement, objects could become unreachable
                        newPoint = MathUtil.max(newPoint, MathUtil.zeroPoint2D);
                        c.setLocation(MathUtil.point2DToPoint(newPoint));
                    }

                    for (LayoutTrack lt : _layoutTrackSelection) {
                        LayoutTrackView ltv = getLayoutTrackView(lt);
                        Point2D center = ltv.getCoordsCenter();
                        newPoint = MathUtil.add(center, offset);
                        // don't allow negative placement, objects could become unreachable
                        newPoint = MathUtil.max(newPoint, MathUtil.zeroPoint2D);
                        getLayoutTrackView(lt).setCoordsCenter(newPoint);
                    }

                    for (LayoutShape ls : _layoutShapeSelection) {
                        Point2D center = ls.getCoordsCenter();
                        newPoint = MathUtil.add(center, offset);
                        // don't allow negative placement, objects could become unreachable
                        newPoint = MathUtil.max(newPoint, MathUtil.zeroPoint2D);
                        ls.setCoordsCenter(newPoint);
                    }

                    _lastX = xLoc;
                    _lastY = yLoc;
                } else {
                    switch (selectedHitPointType) {
                        case POS_POINT:
                        case TURNOUT_CENTER:
                        case LEVEL_XING_CENTER:
                        case SLIP_LEFT:
                        case SLIP_RIGHT:
                        case TURNTABLE_CENTER: {
                            getLayoutTrackView((LayoutTrack) selectedObject).setCoordsCenter(currentPoint);
                            isDragging = true;
                            break;
                        }

                        case TURNOUT_A: {
                            getLayoutTurnoutView((LayoutTurnout) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case TURNOUT_B: {
                            getLayoutTurnoutView((LayoutTurnout) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case TURNOUT_C: {
                            getLayoutTurnoutView((LayoutTurnout) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case TURNOUT_D: {
                            getLayoutTurnoutView((LayoutTurnout) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case LEVEL_XING_A: {
                            getLevelXingView((LevelXing) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case LEVEL_XING_B: {
                            getLevelXingView((LevelXing) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case LEVEL_XING_C: {
                            getLevelXingView((LevelXing) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case LEVEL_XING_D: {
                            getLevelXingView((LevelXing) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case SLIP_A: {
                            getLayoutSlipView((LayoutSlip) selectedObject).setCoordsA(currentPoint);
                            break;
                        }

                        case SLIP_B: {
                            getLayoutSlipView((LayoutSlip) selectedObject).setCoordsB(currentPoint);
                            break;
                        }

                        case SLIP_C: {
                            getLayoutSlipView((LayoutSlip) selectedObject).setCoordsC(currentPoint);
                            break;
                        }

                        case SLIP_D: {
                            getLayoutSlipView((LayoutSlip) selectedObject).setCoordsD(currentPoint);
                            break;
                        }

                        case LAYOUT_POS_LABEL:
                        case MULTI_SENSOR: {
                            PositionableLabel pl = (PositionableLabel) selectedObject;

                            if (pl.isPositionable()) {
                                pl.setLocation((int) currentPoint.getX(), (int) currentPoint.getY());
                                isDragging = true;
                            }
                            break;
                        }

                        case LAYOUT_POS_JCOMP: {
                            PositionableJComponent c = (PositionableJComponent) selectedObject;

                            if (c.isPositionable()) {
                                c.setLocation((int) currentPoint.getX(), (int) currentPoint.getY());
                                isDragging = true;
                            }
                            break;
                        }

                        case TRACK_CIRCLE_CENTRE: {
                            TrackSegmentView tv = getTrackSegmentView((TrackSegment) selectedObject);
                            tv.reCalculateTrackSegmentAngle(currentPoint.getX(), currentPoint.getY());
                            break;
                        }

                        default: {
                            if (HitPointType.isBezierHitType(foundHitPointType)) {
                                int index = selectedHitPointType.bezierPointIndex();
                                getTrackSegmentView((TrackSegment) selectedObject).setBezierControlPoint(currentPoint, index);
                            } else if ((selectedHitPointType == HitPointType.SHAPE_CENTER)) {
                                ((LayoutShape) selectedObject).setCoordsCenter(currentPoint);
                            } else if (HitPointType.isShapePointOffsetHitPointType(selectedHitPointType)) {
                                int index = selectedHitPointType.shapePointIndex();
                                ((LayoutShape) selectedObject).setPoint(index, currentPoint);
                            } else if (HitPointType.isTurntableRayHitType(selectedHitPointType)) {
                                LayoutTurntable turn = (LayoutTurntable) selectedObject;
                                LayoutTurntableView turnView = getLayoutTurntableView(turn);
                                turnView.setRayCoordsIndexed(currentPoint.getX(), currentPoint.getY(),
                                        selectedHitPointType.turntableTrackIndex());
                            }
                            break;
                        }
                    }
                }
            } else if ((beginTrack != null)
                    && event.isShiftDown()
                    && leToolBarPanel.trackButton.isSelected()) {
                // dragging from first end of Track Segment
                currentLocation = new Point2D.Double(xLoc, yLoc);
                boolean needResetCursor = (foundTrack != null);

                if (findLayoutTracksHitPoint(currentLocation, true)) {
                    // have match to free connection point, change cursor
                    _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if (needResetCursor) {
                    _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else if (event.isShiftDown()
                    && leToolBarPanel.shapeButton.isSelected() && (selectedObject != null)) {
                // dragging from end of shape
                currentLocation = new Point2D.Double(xLoc, yLoc);
            } else if (selectionActive && !event.isShiftDown() && !isMetaDown(event)) {
                selectionWidth = xLoc - selectionX;
                selectionHeight = yLoc - selectionY;
            }
            redrawPanel();
        } else {
            Rectangle r = new Rectangle(event.getX(), event.getY(), 1, 1);
            ((JComponent) event.getSource()).scrollRectToVisible(r);
        }   // if (isEditable())
    }   // mouseDragged

    @Override
    public void mouseEntered(@Nonnull MouseEvent event) {
        _targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Add an Anchor point.
     */
    public void addAnchor() {
        addAnchor(currentPoint);
    }

    @Nonnull
    public PositionablePoint addAnchor(@Nonnull Point2D p) {
        assert p != null;

        // get unique name
        String name = finder.uniqueName("A", ++numAnchors);

        // create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.PointType.ANCHOR, this);
        PositionablePointView pv = new PositionablePointView(o, p, this);
        addLayoutTrack(o, pv);

        setDirty();

        return o;
    }

    /**
     * Add an End Bumper point.
     */
    public void addEndBumper() {
        // get unique name
        String name = finder.uniqueName("EB", ++numEndBumpers);

        // create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.PointType.END_BUMPER, this);
        PositionablePointView pv = new PositionablePointView(o, currentPoint, this);
        addLayoutTrack(o, pv);

        setDirty();
    }

    /**
     * Add an Edge Connector point.
     */
    public void addEdgeConnector() {
        // get unique name
        String name = finder.uniqueName("EC", ++numEdgeConnectors);

        // create object
        PositionablePoint o = new PositionablePoint(name,
                PositionablePoint.PointType.EDGE_CONNECTOR, this);
        PositionablePointView pv = new PositionablePointView(o, currentPoint, this);
        addLayoutTrack(o, pv);

        setDirty();
    }

    /**
     * Add a Track Segment
     */
    public void addTrackSegment() {
        // get unique name
        String name = finder.uniqueName("T", ++numTrackSegments);

        // create object
        newTrack = new TrackSegment(name, beginTrack, beginHitPointType,
                foundTrack, foundHitPointType,
                leToolBarPanel.mainlineTrack.isSelected(), this);

        TrackSegmentView tsv = new TrackSegmentView(
                newTrack,
                this
        );
        addLayoutTrack(newTrack, tsv);

        setDirty();

        // link to connected objects
        setLink(beginTrack, beginHitPointType, newTrack, HitPointType.TRACK);
        setLink(foundTrack, foundHitPointType, newTrack, HitPointType.TRACK);

        // check on layout block
        String newName = leToolBarPanel.blockIDComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            newTrack.setLayoutBlock(b);
            getLEAuxTools().setBlockConnectivityChanged();

            // check on occupancy sensor
            String sensorName = leToolBarPanel.blockSensorComboBox.getSelectedItemDisplayName();
            if (sensorName == null) {
                sensorName = "";
            }

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    leToolBarPanel.blockSensorComboBox.setSelectedItem(b.getOccupancySensor());
                }
            }
            newTrack.updateBlockInfo();
        }
    }

    /**
     * Add a Level Crossing
     */
    public void addLevelXing() {
        // get unique name
        String name = finder.uniqueName("X", ++numLevelXings);

        // create object
        LevelXing o = new LevelXing(name, this);
        LevelXingView ov = new LevelXingView(o, currentPoint, this);
        addLayoutTrack(o, ov);

        setDirty();

        // check on layout block
        String newName = leToolBarPanel.blockIDComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            o.setLayoutBlockAC(b);
            o.setLayoutBlockBD(b);

            // check on occupancy sensor
            String sensorName = leToolBarPanel.blockSensorComboBox.getSelectedItemDisplayName();
            if (sensorName == null) {
                sensorName = "";
            }

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    leToolBarPanel.blockSensorComboBox.setSelectedItem(b.getOccupancySensor());
                }
            }
        }
    }

    /**
     * Add a LayoutSlip
     *
     * @param type the slip type
     */
    public void addLayoutSlip(LayoutTurnout.TurnoutType type) {
        // get the rotation entry
        double rot = 0.0;
        String s = leToolBarPanel.rotationComboBox.getEditor().getItem().toString().trim();

        if (s.isEmpty()) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("Error3") + " "
                        + e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        // get unique name
        String name = finder.uniqueName("SL", ++numLayoutSlips);

        // create object
        LayoutSlip o;
        LayoutSlipView ov;

        switch (type) {
            case DOUBLE_SLIP:
                LayoutDoubleSlip lds = new LayoutDoubleSlip(name, this);
                o = lds;
                ov = new LayoutDoubleSlipView(lds, currentPoint, rot, this);
                break;
            case SINGLE_SLIP:
                LayoutSingleSlip lss = new LayoutSingleSlip(name, this);
                o = lss;
                ov = new LayoutSingleSlipView(lss, currentPoint, rot, this);
                break;
            default:
                log.error("can't create slip {} with type {}", name, type);
                return; // without creating
        }

        addLayoutTrack(o, ov);

        setDirty();

        // check on layout block
        String newName = leToolBarPanel.blockIDComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            ov.setLayoutBlock(b);

            // check on occupancy sensor
            String sensorName = leToolBarPanel.blockSensorComboBox.getSelectedItemDisplayName();
            if (sensorName == null) {
                sensorName = "";
            }

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    leToolBarPanel.blockSensorComboBox.setSelectedItem(b.getOccupancySensor());
                }
            }
        }

        String turnoutName = leToolBarPanel.turnoutNameComboBox.getSelectedItemDisplayName();
        if (turnoutName == null) {
            turnoutName = "";
        }

        if (validatePhysicalTurnout(turnoutName, this)) {
            // turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName)) {
                leToolBarPanel.turnoutNameComboBox.setSelectedItem(o.getTurnout());
            }
        } else {
            o.setTurnout("");
            leToolBarPanel.turnoutNameComboBox.setSelectedItem(null);
            leToolBarPanel.turnoutNameComboBox.setSelectedIndex(-1);
        }
        turnoutName = leToolBarPanel.extraTurnoutNameComboBox.getSelectedItemDisplayName();
        if (turnoutName == null) {
            turnoutName = "";
        }

        if (validatePhysicalTurnout(turnoutName, this)) {
            // turnout is valid and unique.
            o.setTurnoutB(turnoutName);

            if (o.getTurnoutB().getSystemName().equals(turnoutName)) {
                leToolBarPanel.extraTurnoutNameComboBox.setSelectedItem(o.getTurnoutB());
            }
        } else {
            o.setTurnoutB("");
            leToolBarPanel.extraTurnoutNameComboBox.setSelectedItem(null);
            leToolBarPanel.extraTurnoutNameComboBox.setSelectedIndex(-1);
        }
    }

    /**
     * Add a Layout Turnout
     *
     * @param type the turnout type
     */
    public void addLayoutTurnout(LayoutTurnout.TurnoutType type) {
        // get the rotation entry
        double rot = 0.0;
        String s = leToolBarPanel.rotationComboBox.getEditor().getItem().toString().trim();

        if (s.isEmpty()) {
            rot = 0.0;
        } else {
            try {
                rot = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("Error3") + " "
                        + e, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        // get unique name
        String name = finder.uniqueName("TO", ++numLayoutTurnouts);

        // create object - check all types, although not clear all actually reach here
        LayoutTurnout o;
        LayoutTurnoutView ov;

        switch (type) {

            case RH_TURNOUT:
                LayoutRHTurnout lrht = new LayoutRHTurnout(name, this);
                o = lrht;
                ov = new LayoutRHTurnoutView(lrht, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;
            case LH_TURNOUT:
                LayoutLHTurnout llht = new LayoutLHTurnout(name, this);
                o = llht;
                ov = new LayoutLHTurnoutView(llht, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;
            case WYE_TURNOUT:
                LayoutWye lw = new LayoutWye(name, this);
                o = lw;
                ov = new LayoutWyeView(lw, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;
            case DOUBLE_XOVER:
                LayoutDoubleXOver ldx = new LayoutDoubleXOver(name, this);
                o = ldx;
                ov = new LayoutDoubleXOverView(ldx, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;
            case RH_XOVER:
                LayoutRHXOver lrx = new LayoutRHXOver(name, this);
                o = lrx;
                ov = new LayoutRHXOverView(lrx, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;
            case LH_XOVER:
                LayoutLHXOver llx = new LayoutLHXOver(name, this);
                o = llx;
                ov = new LayoutLHXOverView(llx, currentPoint, rot, gContext.getXScale(), gContext.getYScale(), this);
                break;

            case DOUBLE_SLIP:
                LayoutDoubleSlip lds = new LayoutDoubleSlip(name, this);
                o = lds;
                ov = new LayoutDoubleSlipView(lds, currentPoint, rot, this);
                log.error("Found SINGLE_SLIP in addLayoutTurnout for element {}", name);
                break;
            case SINGLE_SLIP:
                LayoutSingleSlip lss = new LayoutSingleSlip(name, this);
                o = lss;
                ov = new LayoutSingleSlipView(lss, currentPoint, rot, this);
                log.error("Found SINGLE_SLIP in addLayoutTurnout for element {}", name);
                break;

            default:
                log.error("can't create LayoutTrack {} with type {}", name, type);
                return; // without creating
        }

        addLayoutTrack(o, ov);

        setDirty();

        // check on layout block
        String newName = leToolBarPanel.blockIDComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        LayoutBlock b = provideLayoutBlock(newName);

        if (b != null) {
            ov.setLayoutBlock(b);

            // check on occupancy sensor
            String sensorName = leToolBarPanel.blockSensorComboBox.getSelectedItemDisplayName();
            if (sensorName == null) {
                sensorName = "";
            }

            if (!sensorName.isEmpty()) {
                if (!validateSensor(sensorName, b, this)) {
                    b.setOccupancySensorName("");
                } else {
                    leToolBarPanel.blockSensorComboBox.setSelectedItem(b.getOccupancySensor());
                }
            }
        }

        // set default continuing route Turnout State
        o.setContinuingSense(Turnout.CLOSED);

        // check on a physical turnout
        String turnoutName = leToolBarPanel.turnoutNameComboBox.getSelectedItemDisplayName();
        if (turnoutName == null) {
            turnoutName = "";
        }

        if (validatePhysicalTurnout(turnoutName, this)) {
            // turnout is valid and unique.
            o.setTurnout(turnoutName);

            if (o.getTurnout().getSystemName().equals(turnoutName)) {
                leToolBarPanel.turnoutNameComboBox.setSelectedItem(o.getTurnout());
            }
        } else {
            o.setTurnout("");
            leToolBarPanel.turnoutNameComboBox.setSelectedItem(null);
            leToolBarPanel.turnoutNameComboBox.setSelectedIndex(-1);
        }
    }

    /**
     * Validates that a physical turnout exists and is unique among Layout
     * Turnouts Returns true if valid turnout was entered, false otherwise
     *
     * @param inTurnoutName the (system or user) name of the turnout
     * @param inOpenPane    the pane over which to show dialogs (null to
     *                      suppress dialogs)
     * @return true if valid
     */
    public boolean validatePhysicalTurnout(
            @Nonnull String inTurnoutName,
            @CheckForNull Component inOpenPane) {
        // check if turnout name was entered
        if (inTurnoutName.isEmpty()) {
            // no turnout entered
            return false;
        }

        // check that the unique turnout name corresponds to a defined physical turnout
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutName);
        if (t == null) {
            // There is no turnout corresponding to this name
            if (inOpenPane != null) {
                JOptionPane.showMessageDialog(inOpenPane,
                        MessageFormat.format(Bundle.getMessage("Error8"), inTurnoutName),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        log.debug("validatePhysicalTurnout('{}')", inTurnoutName);
        boolean result = true;  // assume success (optimist!)

        // ensure that this turnout is unique among Layout Turnouts in this Layout
        for (LayoutTurnout lt : getLayoutTurnouts()) {
            t = lt.getTurnout();
            if (t != null) {
                String sname = t.getSystemName();
                String uname = t.getUserName();
                log.debug("{}: Turnout tested '{}' and '{}'.", lt.getName(), sname, uname);
                if ((sname.equals(inTurnoutName))
                        || ((uname != null) && (uname.equals(inTurnoutName)))) {
                    result = false;
                    break;
                }
            }

            // Only check for the second turnout if the type is a double cross over
            // otherwise the second turnout is used to throw an additional turnout at
            // the same time.
            if (lt.isTurnoutTypeXover()) {
                t = lt.getSecondTurnout();
                if (t != null) {
                    String sname = t.getSystemName();
                    String uname = t.getUserName();
                    log.debug("{}: 2nd Turnout tested '{}' and '{}'.", lt.getName(), sname, uname);
                    if ((sname.equals(inTurnoutName))
                            || ((uname != null) && (uname.equals(inTurnoutName)))) {
                        result = false;
                        break;
                    }
                }
            }
        }

        if (result) {   // only need to test slips if we haven't failed yet...
            // ensure that this turnout is unique among Layout slips in this Layout
            for (LayoutSlip sl : getLayoutSlips()) {
                t = sl.getTurnout();
                if (t != null) {
                    String sname = t.getSystemName();
                    String uname = t.getUserName();
                    log.debug("{}: slip Turnout tested '{}' and '{}'.", sl.getName(), sname, uname);
                    if ((sname.equals(inTurnoutName))
                            || ((uname != null) && (uname.equals(inTurnoutName)))) {
                        result = false;
                        break;
                    }
                }

                t = sl.getTurnoutB();
                if (t != null) {
                    String sname = t.getSystemName();
                    String uname = t.getUserName();
                    log.debug("{}: slip Turnout B tested '{}' and '{}'.", sl.getName(), sname, uname);
                    if ((sname.equals(inTurnoutName))
                            || ((uname != null) && (uname.equals(inTurnoutName)))) {
                        result = false;
                        break;
                    }
                }
            }
        }

        if (result) {   // only need to test Turntable turnouts if we haven't failed yet...
            // ensure that this turntable turnout is unique among turnouts in this Layout
            for (LayoutTurntable tt : getLayoutTurntables()) {
                for (LayoutTurntable.RayTrack ray : tt.getRayTrackList()) {
                    t = ray.getTurnout();
                    if (t != null) {
                        String sname = t.getSystemName();
                        String uname = t.getUserName();
                        log.debug("{}: Turntable turnout tested '{}' and '{}'.", ray.getTurnoutName(), sname, uname);
                        if ((sname.equals(inTurnoutName))
                                || ((uname != null) && (uname.equals(inTurnoutName)))) {
                            result = false;
                            break;
                        }
                    }
                }
            }
        }

        if (!result && (inOpenPane != null)) {
            JOptionPane.showMessageDialog(inOpenPane,
                    MessageFormat.format(Bundle.getMessage("Error4"), inTurnoutName),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
        return result;
    }

    /**
     * link the 'from' object and type to the 'to' object and type
     *
     * @param fromObject    the object to link from
     * @param fromPointType the object type to link from
     * @param toObject      the object to link to
     * @param toPointType   the object type to link to
     */
    public void setLink(@Nonnull LayoutTrack fromObject, HitPointType fromPointType,
            @Nonnull LayoutTrack toObject, HitPointType toPointType) {
        switch (fromPointType) {
            case POS_POINT: {
                if ((toPointType == HitPointType.TRACK) && (fromObject instanceof PositionablePoint)) {
                    ((PositionablePoint) fromObject).setTrackConnection((TrackSegment) toObject);
                } else {
                    log.error("Attempt to link a non-TRACK connection ('{}')to a Positionable Point ('{}')",
                            toObject.getName(), fromObject.getName());
                }
                break;
            }

            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D: {
                try {
                    fromObject.setConnection(fromPointType, toObject, toPointType);
                } catch (JmriException e) {
                    // ignore (log.error in setConnection method)
                }
                break;
            }

            case TRACK: {
                // should never happen, Track Segment links are set in ctor
                log.error("Illegal request to set a Track Segment link");
                break;
            }

            default: {
                if (HitPointType.isTurntableRayHitType(fromPointType) && (fromObject instanceof LayoutTurntable)) {
                    if (toObject instanceof TrackSegment) {
                        ((LayoutTurntable) fromObject).setRayConnect((TrackSegment) toObject,
                                fromPointType.turntableTrackIndex());
                    } else {
                        log.warn("setLink found expected toObject type {} with fromPointType {} fromObject type {}",
                                toObject.getClass(), fromPointType, fromObject.getClass(), new Exception("traceback"));
                    }
                } else {
                    log.warn("setLink found expected fromObject type {} with fromPointType {} toObject type {}",
                            fromObject.getClass(), fromPointType, toObject.getClass(), new Exception("traceback"));
                }
                break;
            }
        }
    }

    /**
     * Return a layout block with the entered name, creating a new one if
     * needed. Note that the entered name becomes the user name of the
     * LayoutBlock, and a system name is automatically created by
     * LayoutBlockManager if needed.
     * <p>
     * If the block name is a system name, then the user will have to supply a
     * user name for the block.
     * <p>
     * Some, but not all, errors pop a Swing error dialog in addition to
     * logging.
     *
     * @param inBlockName the entered name
     * @return the provided LayoutBlock
     */
    public LayoutBlock provideLayoutBlock(@Nonnull String inBlockName) {
        LayoutBlock result = null; // assume failure (pessimist!)
        LayoutBlock newBlk = null; // assume failure (pessimist!)

        if (inBlockName.isEmpty()) {
            // nothing entered, try autoAssign
            if (autoAssignBlocks) {
                newBlk = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock();
                if (null == newBlk) {
                    log.error("provideLayoutBlock: Failure to auto-assign for empty LayoutBlock name");
                }
            } else {
                log.debug("provideLayoutBlock: no name given and not assigning auto block names");
            }
        } else {
            // check if this Layout Block already exists
            result = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(inBlockName);
            if (result == null) { //(no)
                // The combo box name can be either a block system name or a block user name
                Block checkBlock = InstanceManager.getDefault(BlockManager.class).getBlock(inBlockName);
                if (checkBlock == null) {
                    log.error("provideLayoutBlock: The block name '{}' does not return a block.", inBlockName);
                } else {
                    String checkUserName = checkBlock.getUserName();
                    if (checkUserName != null && checkUserName.equals(inBlockName)) {
                        // Go ahead and use the name for the layout block
                        newBlk = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, inBlockName);
                        if (newBlk == null) {
                            log.error("provideLayoutBlock: Failure to create new LayoutBlock '{}'.", inBlockName);
                        }
                    } else {
                        // Appears to be a system name, request a user name
                        String blkUserName = JOptionPane.showInputDialog(getTargetFrame(),
                                Bundle.getMessage("BlkUserNameMsg"),
                                Bundle.getMessage("BlkUserNameTitle"),
                                JOptionPane.PLAIN_MESSAGE);
                        if (blkUserName != null && !blkUserName.isEmpty()) {
                            // Verify the user name
                            Block checkDuplicate = InstanceManager.getDefault(BlockManager.class).getByUserName(blkUserName);
                            if (checkDuplicate != null) {
                                JOptionPane.showMessageDialog(getTargetFrame(),
                                        Bundle.getMessage("BlkUserNameInUse", blkUserName),
                                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                            } else {
                                // OK to use as a block user name
                                checkBlock.setUserName(blkUserName);
                                newBlk = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, blkUserName);
                                if (newBlk == null) {
                                    log.error("provideLayoutBlock: Failure to create new LayoutBlock '{}' with a new user name.", blkUserName);
                                }
                            }
                        }
                    }
                }
            }
        }

        // if we created a new block
        if (newBlk != null) {
            // initialize the new block
            // log.debug("provideLayoutBlock :: Init new block {}", inBlockName);
            newBlk.initializeLayoutBlock();
            newBlk.initializeLayoutBlockRouting();
            newBlk.setBlockTrackColor(defaultTrackColor);
            newBlk.setBlockOccupiedColor(defaultOccupiedTrackColor);
            newBlk.setBlockExtraColor(defaultAlternativeTrackColor);
            result = newBlk;
        }

        if (result != null) {
            // set both new and previously existing block
            result.addLayoutEditor(this);
            result.incrementUse();
            setDirty();
        }
        return result;
    }

    /**
     * Validates that the supplied occupancy sensor name corresponds to an
     * existing sensor and is unique among all blocks. If valid, returns true
     * and sets the block sensor name in the block. Else returns false, and does
     * nothing to the block.
     *
     * @param sensorName the sensor name to validate
     * @param blk        the LayoutBlock in which to set it
     * @param openFrame  the frame (Component) it is in
     * @return true if sensor is valid
     */
    public boolean validateSensor(
            @Nonnull String sensorName,
            @Nonnull LayoutBlock blk,
            @Nonnull Component openFrame) {
        boolean result = false; // assume failure (pessimist!)

        // check if anything entered
        if (!sensorName.isEmpty()) {
            // get a validated sensor corresponding to this name and assigned to block
            if (blk.getOccupancySensorName().equals(sensorName)) {
                result = true;
            } else {
                Sensor s = blk.validateSensor(sensorName, openFrame);
                result = (s != null); // if sensor returned result is true.
            }
        }
        return result;
    }

    /**
     * Return a layout block with the given name if one exists. Registers this
     * LayoutEditor with the layout block. This method is designed to be used
     * when a panel is loaded. The calling method must handle whether the use
     * count should be incremented.
     *
     * @param blockID the given name
     * @return null if blockID does not already exist
     */
    public LayoutBlock getLayoutBlock(@Nonnull String blockID) {
        // check if this Layout Block already exists
        LayoutBlock blk = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(blockID);
        if (blk == null) {
            log.error("LayoutBlock '{}' not found when panel loaded", blockID);
            return null;
        }
        blk.addLayoutEditor(this);
        return blk;
    }

    /**
     * Remove object from all Layout Editor temporary lists of items not part of
     * track schematic
     *
     * @param s the object to remove
     * @return true if found
     */
    private boolean remove(@Nonnull Object s) {
        boolean found = false;

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
        if (multiSensors.contains(s)) {
            multiSensors.remove(s);
            found = true;
        }
        if (clocks.contains(s)) {
            clocks.remove(s);
            found = true;
        }
        if (labelImage.contains(s)) {
            labelImage.remove(s);
            found = true;
        }

        if (sensorImage.contains(s) || sensorList.contains(s)) {
            Sensor sensor = ((SensorIcon) s).getSensor();
            if (sensor != null) {
                if (removeAttachedBean((sensor))) {
                    sensorImage.remove(s);
                    sensorList.remove(s);
                    found = true;
                } else {
                    return false;
                }
            }
        }

        if (signalHeadImage.contains(s) || signalList.contains(s)) {
            SignalHead head = ((SignalHeadIcon) s).getSignalHead();
            if (head != null) {
                if (removeAttachedBean((head))) {
                    signalHeadImage.remove(s);
                    signalList.remove(s);
                    found = true;
                } else {
                    return false;
                }
            }
        }

        if (signalMastList.contains(s)) {
            SignalMast mast = ((SignalMastIcon) s).getSignalMast();
            if (mast != null) {
                if (removeAttachedBean((mast))) {
                    signalMastList.remove(s);
                    found = true;
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
    }

    @Override
    public boolean removeFromContents(@Nonnull Positionable l) {
        return remove(l);
    }

    private String findBeanUsage(@Nonnull NamedBean bean) {
        PositionablePoint pe;
        PositionablePoint pw;
        LayoutTurnout lt;
        LevelXing lx;
        LayoutSlip ls;
        boolean found = false;
        StringBuilder sb = new StringBuilder();
        String msgKey = "DeleteReference";  // NOI18N
        String beanKey = "None";  // NOI18N
        String beanValue = bean.getDisplayName();

        if (bean instanceof SignalMast) {
            beanKey = "BeanNameSignalMast";  // NOI18N

            if (InstanceManager.getDefault(SignalMastLogicManager.class).isSignalMastUsed((SignalMast) bean)) {
                SignalMastLogic sml = InstanceManager.getDefault(
                        SignalMastLogicManager.class).getSignalMastLogic((SignalMast) bean);
                if ((sml != null) && sml.useLayoutEditor(sml.getDestinationList().get(0))) {
                    msgKey = "DeleteSmlReference";  // NOI18N
                }
            }
        } else if (bean instanceof Sensor) {
            beanKey = "BeanNameSensor";  // NOI18N
        } else if (bean instanceof SignalHead) {
            beanKey = "BeanNameSignalHead";  // NOI18N
        }
        if (!beanKey.equals("None")) {  // NOI18N
            sb.append(Bundle.getMessage(msgKey, Bundle.getMessage(beanKey), beanValue));
        }

        if ((pw = finder.findPositionablePointByWestBoundBean(bean)) != null) {
            TrackSegment t1 = pw.getConnect1();
            TrackSegment t2 = pw.getConnect2();
            if (t1 != null) {
                if (t2 != null) {
                    sb.append(Bundle.getMessage("DeleteAtPoint1", t1.getBlockName()));  // NOI18N
                    sb.append(Bundle.getMessage("DeleteAtPoint2", t2.getBlockName()));  // NOI18N
                } else {
                    sb.append(Bundle.getMessage("DeleteAtPoint1", t1.getBlockName()));  // NOI18N
                }
            }
            found = true;
        }

        if ((pe = finder.findPositionablePointByEastBoundBean(bean)) != null) {
            TrackSegment t1 = pe.getConnect1();
            TrackSegment t2 = pe.getConnect2();

            if (t1 != null) {
                if (t2 != null) {
                    sb.append(Bundle.getMessage("DeleteAtPoint1", t1.getBlockName()));  // NOI18N
                    sb.append(Bundle.getMessage("DeleteAtPoint2", t2.getBlockName()));  // NOI18N
                } else {
                    sb.append(Bundle.getMessage("DeleteAtPoint1", t1.getBlockName()));  // NOI18N
                }
            }
            found = true;
        }

        if ((lt = finder.findLayoutTurnoutByBean(bean)) != null) {
            sb.append(Bundle.getMessage("DeleteAtOther", Bundle.getMessage("BeanNameTurnout"), lt.getTurnoutName()));   // NOI18N
            found = true;
        }

        if ((lx = finder.findLevelXingByBean(bean)) != null) {
            sb.append(Bundle.getMessage("DeleteAtOther", Bundle.getMessage("LevelCrossing"), lx.getId()));   // NOI18N
            found = true;
        }

        if ((ls = finder.findLayoutSlipByBean(bean)) != null) {
            sb.append(Bundle.getMessage("DeleteAtOther", Bundle.getMessage("Slip"), ls.getTurnoutName()));   // NOI18N
            found = true;
        }

        if (!found) {
            return null;
        }
        return sb.toString();
    }

    /**
     * NX Sensors, Signal Heads and Signal Masts can be attached to positional
     * points, turnouts and level crossings. If an attachment exists, present an
     * option to cancel the remove action, remove the attachement or retain the
     * attachment.
     *
     * @param bean The named bean to be removed.
     * @return true if OK to remove the related icon.
     */
    private boolean removeAttachedBean(@Nonnull NamedBean bean) {
        String usage = findBeanUsage(bean);

        if (usage != null) {
            usage = String.format("<html>%s</html>", usage);
            int selectedValue = JOptionPane.showOptionDialog(this,
                    usage, Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonCancel")},
                    Bundle.getMessage("ButtonYes"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return true; // return leaving the references in place but allow the icon to be deleted.
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                return false; // do not delete the item
            }
            if (bean instanceof Sensor) {
                // Additional actions for NX sensor pairs
                return getLETools().removeSensorAssignment((Sensor) bean);
            } else {
                removeBeanRefs(bean);
            }
        }
        return true;
    }

    private void removeBeanRefs(@Nonnull NamedBean bean) {
        PositionablePoint pe;
        PositionablePoint pw;
        LayoutTurnout lt;
        LevelXing lx;
        LayoutSlip ls;

        if ((pw = finder.findPositionablePointByWestBoundBean(bean)) != null) {
            pw.removeBeanReference(bean);
        }

        if ((pe = finder.findPositionablePointByEastBoundBean(bean)) != null) {
            pe.removeBeanReference(bean);
        }

        if ((lt = finder.findLayoutTurnoutByBean(bean)) != null) {
            lt.removeBeanReference(bean);
        }

        if ((lx = finder.findLevelXingByBean(bean)) != null) {
            lx.removeBeanReference(bean);
        }

        if ((ls = finder.findLayoutSlipByBean(bean)) != null) {
            ls.removeBeanReference(bean);
        }
    }

    private boolean noWarnPositionablePoint = false;

    /**
     * Remove a PositionablePoint -- an Anchor or an End Bumper.
     *
     * @param o the PositionablePoint to remove
     * @return true if removed
     */
    public boolean removePositionablePoint(@Nonnull PositionablePoint o) {
        // First verify with the user that this is really wanted, only show message if there is a bit of track connected
        if ((o.getConnect1() != null) || (o.getConnect2() != null)) {
            if (!noWarnPositionablePoint) {
                int selectedValue = JOptionPane.showOptionDialog(this,
                        Bundle.getMessage("Question2"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"),
                            Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonYesPlus")},
                        Bundle.getMessage("ButtonNo"));

                if (selectedValue == JOptionPane.NO_OPTION) {
                    return false; // return without creating if "No" response
                }

                if (selectedValue == JOptionPane.CANCEL_OPTION) {
                    // Suppress future warnings, and continue
                    noWarnPositionablePoint = true;
                }
            }

            // remove from selection information
            if (selectedObject == o) {
                selectedObject = null;
            }

            if (prevSelectedObject == o) {
                prevSelectedObject = null;
            }

            // remove connections if any
            TrackSegment t1 = o.getConnect1();
            TrackSegment t2 = o.getConnect2();

            if (t1 != null) {
                removeTrackSegment(t1);
            }

            if (t2 != null) {
                removeTrackSegment(t2);
            }

            // delete from array
        }

        return removeLayoutTrackAndRedraw(o);
    }

    private boolean noWarnLayoutTurnout = false;

    /**
     * Remove a LayoutTurnout
     *
     * @param o the LayoutTurnout to remove
     * @return true if removed
     */
    public boolean removeLayoutTurnout(@Nonnull LayoutTurnout o) {
        // First verify with the user that this is really wanted
        if (!noWarnLayoutTurnout) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question1r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return false; // return without removing if "No" response
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                // Suppress future warnings, and continue
                noWarnLayoutTurnout = true;
            }
        }

        // remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        // remove connections if any
        TrackSegment t = (TrackSegment) o.getConnectA();

        if (t != null) {
            substituteAnchor(getLayoutTurnoutView(o).getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(getLayoutTurnoutView(o).getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(getLayoutTurnoutView(o).getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(getLayoutTurnoutView(o).getCoordsD(), o, t);
        }

        // decrement Block use count(s)
        LayoutBlock b = o.getLayoutBlock();

        if (b != null) {
            b.decrementUse();
        }

        if (o.isTurnoutTypeXover() || o.isTurnoutTypeSlip()) {
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

        return removeLayoutTrackAndRedraw(o);
    }

    private void substituteAnchor(@Nonnull Point2D loc,
            @Nonnull LayoutTrack o, @Nonnull TrackSegment t) {
        PositionablePoint p = addAnchor(loc);

        if (t.getConnect1() == o) {
            t.setNewConnect1(p, HitPointType.POS_POINT);
        }

        if (t.getConnect2() == o) {
            t.setNewConnect2(p, HitPointType.POS_POINT);
        }
        p.setTrackConnection(t);
    }

    private boolean noWarnLevelXing = false;

    /**
     * Remove a Level Crossing
     *
     * @param o the LevelXing to remove
     * @return true if removed
     */
    public boolean removeLevelXing(@Nonnull LevelXing o) {
        // First verify with the user that this is really wanted
        if (!noWarnLevelXing) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question3r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return false; // return without creating if "No" response
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                // Suppress future warnings, and continue
                noWarnLevelXing = true;
            }
        }

        // remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        // remove connections if any
        LevelXingView ov = getLevelXingView(o);

        TrackSegment t = (TrackSegment) o.getConnectA();
        if (t != null) {
            substituteAnchor(ov.getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(ov.getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(ov.getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(ov.getCoordsD(), o, t);
        }

        // decrement block use count if any blocks in use
        LayoutBlock lb = o.getLayoutBlockAC();

        if (lb != null) {
            lb.decrementUse();
        }
        LayoutBlock lbx = o.getLayoutBlockBD();

        if ((lbx != null) && (lb != null) && (lbx != lb)) {
            lb.decrementUse();
        }

        return removeLayoutTrackAndRedraw(o);
    }

    private boolean noWarnSlip = false;

    /**
     * Remove a slip
     *
     * @param o the LayoutSlip to remove
     * @return true if removed
     */
    public boolean removeLayoutSlip(@Nonnull LayoutTurnout o) {
        if (!(o instanceof LayoutSlip)) {
            return false;
        }

        // First verify with the user that this is really wanted
        if (!noWarnSlip) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question5r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return false; // return without creating if "No" response
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                // Suppress future warnings, and continue
                noWarnSlip = true;
            }
        }

        LayoutTurnoutView ov = getLayoutTurnoutView(o);

        // remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        // remove connections if any
        TrackSegment t = (TrackSegment) o.getConnectA();

        if (t != null) {
            substituteAnchor(ov.getCoordsA(), o, t);
        }
        t = (TrackSegment) o.getConnectB();

        if (t != null) {
            substituteAnchor(ov.getCoordsB(), o, t);
        }
        t = (TrackSegment) o.getConnectC();

        if (t != null) {
            substituteAnchor(ov.getCoordsC(), o, t);
        }
        t = (TrackSegment) o.getConnectD();

        if (t != null) {
            substituteAnchor(ov.getCoordsD(), o, t);
        }

        // decrement block use count if any blocks in use
        LayoutBlock lb = o.getLayoutBlock();

        if (lb != null) {
            lb.decrementUse();
        }

        return removeLayoutTrackAndRedraw(o);
    }

    private boolean noWarnTurntable = false;

    /**
     * Remove a Layout Turntable
     *
     * @param o the LayoutTurntable to remove
     * @return true if removed
     */
    public boolean removeTurntable(@Nonnull LayoutTurntable o) {
        // First verify with the user that this is really wanted
        if (!noWarnTurntable) {
            int selectedValue = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("Question4r"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonNo"),
                        Bundle.getMessage("ButtonYesPlus")},
                    Bundle.getMessage("ButtonNo"));

            if (selectedValue == JOptionPane.NO_OPTION) {
                return false; // return without creating if "No" response
            }

            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                // Suppress future warnings, and continue
                noWarnTurntable = true;
            }
        }

        // remove from selection information
        if (selectedObject == o) {
            selectedObject = null;
        }

        if (prevSelectedObject == o) {
            prevSelectedObject = null;
        }

        // remove connections if any
        LayoutTurntableView ov = getLayoutTurntableView(o);
        for (int j = 0; j < o.getNumberRays(); j++) {
            TrackSegment t = ov.getRayConnectOrdered(j);

            if (t != null) {
                substituteAnchor(ov.getRayCoordsIndexed(j), o, t);
            }
        }

        return removeLayoutTrackAndRedraw(o);
    }

    /**
     * Remove a Track Segment
     *
     * @param o the TrackSegment to remove
     */
    public void removeTrackSegment(@Nonnull TrackSegment o) {
        // save affected blocks
        LayoutBlock block1 = null;
        LayoutBlock block2 = null;
        LayoutBlock block = o.getLayoutBlock();

        // remove any connections
        HitPointType type = o.getType1();

        if (type == HitPointType.POS_POINT) {
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

        if (type == HitPointType.POS_POINT) {
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

        // delete from array
        removeLayoutTrack(o);

        // update affected blocks
        if (block != null) {
            // decrement Block use count
            block.decrementUse();
            getLEAuxTools().setBlockConnectivityChanged();
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
    }

    private void disconnect(@Nonnull LayoutTrack o, HitPointType type) {
        switch (type) {
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D: {
                try {
                    o.setConnection(type, null, HitPointType.NONE);
                } catch (JmriException e) {
                    // ignore (log.error in setConnection method)
                }
                break;
            }

            default: {
                if (HitPointType.isTurntableRayHitType(type)) {
                    ((LayoutTurntable) o).setRayConnect(null, type.turntableTrackIndex());
                }
                break;
            }
        }
    }

    /**
     * Depending on the given type, and the real class of the given LayoutTrack,
     * determine the connected LayoutTrack. This provides a variable-indirect
     * form of e.g. trk.getLayoutBlockC() for example. Perhaps "Connected Block"
     * captures the idea better, but that method name is being used for
     * something else.
     *
     *
     * @param track The track who's connected blocks are being examined
     * @param type  This point to check for connected blocks, i.e. TURNOUT_B
     * @return The block at a particular point on the track object, or null if
     *         none.
     */
    // Temporary - this should certainly be a LayoutTrack method.
    public LayoutBlock getAffectedBlock(@Nonnull LayoutTrack track, HitPointType type) {
        LayoutBlock result = null;

        switch (type) {
            case TURNOUT_A:
            case SLIP_A: {
                if (track instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) track;
                    result = lt.getLayoutBlock();
                }
                break;
            }

            case TURNOUT_B:
            case SLIP_B: {
                if (track instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) track;
                    result = lt.getLayoutBlockB();
                }
                break;
            }

            case TURNOUT_C:
            case SLIP_C: {
                if (track instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) track;
                    result = lt.getLayoutBlockC();
                }
                break;
            }

            case TURNOUT_D:
            case SLIP_D: {
                if (track instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) track;
                    result = lt.getLayoutBlockD();
                }
                break;
            }

            case LEVEL_XING_A:
            case LEVEL_XING_C: {
                if (track instanceof LevelXing) {
                    LevelXing lx = (LevelXing) track;
                    result = lx.getLayoutBlockAC();
                }
                break;
            }

            case LEVEL_XING_B:
            case LEVEL_XING_D: {
                if (track instanceof LevelXing) {
                    LevelXing lx = (LevelXing) track;
                    result = lx.getLayoutBlockBD();
                }
                break;
            }

            case TRACK: {
                if (track instanceof TrackSegment) {
                    TrackSegment ts = (TrackSegment) track;
                    result = ts.getLayoutBlock();
                }
                break;
            }
            default: {
                log.warn("Unhandled track type: {}", type);
                break;
            }
        }
        return result;
    }

    /**
     * Add a sensor indicator to the Draw Panel
     */
    void addSensor() {
        String newName = leToolBarPanel.sensorComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error10"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);

        l.setIcon("SensorStateActive", leToolBarPanel.sensorIconEditor.getIcon(0));
        l.setIcon("SensorStateInactive", leToolBarPanel.sensorIconEditor.getIcon(1));
        l.setIcon("BeanStateInconsistent", leToolBarPanel.sensorIconEditor.getIcon(2));
        l.setIcon("BeanStateUnknown", leToolBarPanel.sensorIconEditor.getIcon(3));
        l.setSensor(newName);
        l.setDisplayLevel(Editor.SENSORS);

        leToolBarPanel.sensorComboBox.setSelectedItem(l.getSensor());
        setNextLocation(l);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    public void putSensor(@Nonnull SensorIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SENSORS);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    /**
     * Add a signal head to the Panel
     */
    void addSignalHead() {
        // check for valid signal head entry
        String newName = leToolBarPanel.signalHeadComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        SignalHead mHead = null;

        if (!newName.isEmpty()) {
            mHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(newName);

            /*if (mHead == null)
            mHead = InstanceManager.getDefault(SignalHeadManager.class).getByUserName(newName);
            else */
            leToolBarPanel.signalHeadComboBox.setSelectedItem(mHead);
        }

        if (mHead == null) {
            // There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(this,
                    MessageFormat.format(Bundle.getMessage("Error9"), newName),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create and set up signal icon
        SignalHeadIcon l = new SignalHeadIcon(this);
        l.setSignalHead(newName);
        l.setIcon("SignalHeadStateRed", leToolBarPanel.signalIconEditor.getIcon(0));
        l.setIcon("SignalHeadStateFlashingRed", leToolBarPanel.signalIconEditor.getIcon(1));
        l.setIcon("SignalHeadStateYellow", leToolBarPanel.signalIconEditor.getIcon(2));
        l.setIcon("SignalHeadStateFlashingYellow", leToolBarPanel.signalIconEditor.getIcon(3));
        l.setIcon("SignalHeadStateGreen", leToolBarPanel.signalIconEditor.getIcon(4));
        l.setIcon("SignalHeadStateFlashingGreen", leToolBarPanel.signalIconEditor.getIcon(5));
        l.setIcon("SignalHeadStateDark", leToolBarPanel.signalIconEditor.getIcon(6));
        l.setIcon("SignalHeadStateHeld", leToolBarPanel.signalIconEditor.getIcon(7));
        l.setIcon("SignalHeadStateLunar", leToolBarPanel.signalIconEditor.getIcon(8));
        l.setIcon("SignalHeadStateFlashingLunar", leToolBarPanel.signalIconEditor.getIcon(9));
        unionToPanelBounds(l.getBounds());
        setNextLocation(l);
        setDirty();
        putSignal(l);
    }

    public void putSignal(@Nonnull SignalHeadIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SIGNALS);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    @CheckForNull
    SignalHead getSignalHead(@Nonnull String name) {
        SignalHead sh = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(SignalHeadManager.class).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalHead named {}", name);
        }
        return sh;
    }

    public boolean containsSignalHead(@CheckForNull SignalHead head) {
        if (head != null) {
            for (SignalHeadIcon h : signalList) {
                if (h.getSignalHead() == head) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeSignalHead(@CheckForNull SignalHead head) {
        if (head != null) {
            for (SignalHeadIcon h : signalList) {
                if (h.getSignalHead() == head) {
                    signalList.remove(h);
                    h.remove();
                    h.dispose();
                    setDirty();
                    redrawPanel();
                    break;
                }
            }
        }
    }

    void addSignalMast() {
        // check for valid signal head entry
        String newName = leToolBarPanel.signalMastComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        SignalMast mMast = null;

        if (!newName.isEmpty()) {
            mMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(newName);
            leToolBarPanel.signalMastComboBox.setSelectedItem(mMast);
        }

        if (mMast == null) {
            // There is no signal head corresponding to this name
            JOptionPane.showMessageDialog(this,
                    MessageFormat.format(Bundle.getMessage("Error9"), newName),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        // create and set up signal icon
        SignalMastIcon l = new SignalMastIcon(this);
        l.setSignalMast(newName);
        unionToPanelBounds(l.getBounds());
        setNextLocation(l);
        setDirty();
        putSignalMast(l);
    }

    public void putSignalMast(@Nonnull SignalMastIcon l) {
        l.updateSize();
        l.setDisplayLevel(Editor.SIGNALS);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    SignalMast getSignalMast(@Nonnull String name) {
        SignalMast sh = InstanceManager.getDefault(SignalMastManager.class).getBySystemName(name);

        if (sh == null) {
            sh = InstanceManager.getDefault(SignalMastManager.class).getByUserName(name);
        }

        if (sh == null) {
            log.warn("did not find a SignalMast named {}", name);
        }
        return sh;
    }

    public boolean containsSignalMast(@Nonnull SignalMast mast) {
        for (SignalMastIcon h : signalMastList) {
            if (h.getSignalMast() == mast) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a label to the Draw Panel
     */
    void addLabel() {
        String labelText = leToolBarPanel.textLabelTextField.getText();
        labelText = (labelText != null) ? labelText.trim() : "";

        if (labelText.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        PositionableLabel l = super.addLabel(labelText);
        unionToPanelBounds(l.getBounds());
        setDirty();
        l.setForeground(defaultTextColor);
    }

    @Override
    public void putItem(@Nonnull Positionable l) throws Positionable.DuplicateIdException {
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
    }

    /**
     * Add a memory label to the Draw Panel
     */
    void addMemory() {
        String memoryName = leToolBarPanel.textMemoryComboBox.getSelectedItemDisplayName();
        if (memoryName == null) {
            memoryName = "";
        }

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
            if (!uname.equals(memoryName)) {
                // put the system name in the memory field
                leToolBarPanel.textMemoryComboBox.setSelectedItem(xMemory);
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        l.setForeground(defaultTextColor);
        unionToPanelBounds(l.getBounds());
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    void addBlockContents() {
        String newName = leToolBarPanel.blockContentsComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11b"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        BlockContentsIcon l = new BlockContentsIcon(" ", this);
        l.setBlock(newName);
        Block xMemory = l.getBlock();

        if (xMemory != null) {
            String uname = xMemory.getDisplayName();
            if (!uname.equals(newName)) {
                // put the system name in the memory field
                leToolBarPanel.blockContentsComboBox.setSelectedItem(xMemory);
            }
        }
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        l.setForeground(defaultTextColor);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    /**
     * Add a Reporter Icon to the panel.
     *
     * @param reporter the reporter icon to add.
     * @param xx       the horizontal location.
     * @param yy       the vertical location.
     */
    public void addReporter(@Nonnull Reporter reporter, int xx, int yy) {
        ReporterIcon l = new ReporterIcon(this);
        l.setReporter(reporter);
        l.setLocation(xx, yy);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.LABELS);
        unionToPanelBounds(l.getBounds());
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    /**
     * Add an icon to the target
     */
    void addIcon() {
        PositionableLabel l = new PositionableLabel(leToolBarPanel.iconEditor.getIcon(0), this);
        setNextLocation(l);
        l.setDisplayLevel(Editor.ICONS);
        unionToPanelBounds(l.getBounds());
        l.updateSize();
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    /**
     * Add a loco marker to the target
     */
    @Override
    public LocoIcon addLocoIcon(@Nonnull String name) {
        LocoIcon l = new LocoIcon(this);
        Point2D pt = windowCenter();
        if (selectionActive) {
            pt = MathUtil.midPoint(getSelectionRect());
        }
        l.setLocation((int) pt.getX(), (int) pt.getY());
        putLocoIcon(l, name);
        l.setPositionable(true);
        unionToPanelBounds(l.getBounds());
        return l;
    }

    @Override
    public void putLocoIcon(@Nonnull LocoIcon l, @Nonnull String name) {
        super.putLocoIcon(l, name);
        markerImage.add(l);
        unionToPanelBounds(l.getBounds());
    }

    private JFileChooser inputFileChooser = null;

    /**
     * Add a background image
     */
    public void addBackground() {
        if (inputFileChooser == null) {
            inputFileChooser = new JFileChooser(
                    String.format("%s%sresources%sicons",
                            System.getProperty("user.dir"),
                            File.separator,
                            File.separator));

            inputFileChooser.setFileFilter(new FileNameExtensionFilter("Graphics Files", "gif", "jpg", "png"));
        }
        inputFileChooser.rescanCurrentDirectory();

        int retVal = inputFileChooser.showOpenDialog(this);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; // give up if no file selected
        }

        // NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
        // inputFileChooser.getSelectedFile().getPath());
        String name = inputFileChooser.getSelectedFile().getPath();

        // convert to portable path
        name = FileUtil.getPortableFilename(name);

        // setup icon
        PositionableLabel o = super.setUpBackground(name);
        backgroundImage.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();
    }

    // there is no way to call this; could that
    //    private boolean remove(@Nonnull Object s)
    // is being used instead.
    //
    ///**
    // * Remove a background image from the list of background images
    // *
    // * @param b PositionableLabel to remove
    // */
    //private void removeBackground(@Nonnull PositionableLabel b) {
    //    if (backgroundImage.contains(b)) {
    //        backgroundImage.remove(b);
    //        setDirty();
    //    }
    //}
    /**
     * add a layout shape to the list of layout shapes
     *
     * @param p Point2D where the shape should be
     * @return the LayoutShape
     */
    @Nonnull
    private LayoutShape addLayoutShape(@Nonnull Point2D p) {
        // get unique name
        String name = finder.uniqueName("S", ++numShapes);

        // create object
        LayoutShape o = new LayoutShape(name, p, this);
        layoutShapes.add(o);
        unionToPanelBounds(o.getBounds());
        setDirty();
        return o;
    }

    /**
     * Remove a layout shape from the list of layout shapes
     *
     * @param s the LayoutShape to add
     * @return true if added
     */
    public boolean removeLayoutShape(@Nonnull LayoutShape s) {
        boolean result = false;
        if (layoutShapes.contains(s)) {
            layoutShapes.remove(s);
            setDirty();
            result = true;
            redrawPanel();
        }
        return result;
    }

    /**
     * Invoke a window to allow you to add a MultiSensor indicator to the target
     */
    private int multiLocX;
    private int multiLocY;

    void startMultiSensor() {
        multiLocX = xLoc;
        multiLocY = yLoc;

        if (leToolBarPanel.multiSensorFrame == null) {
            // create a common edit frame
            leToolBarPanel.multiSensorFrame = new MultiSensorIconFrame(this);
            leToolBarPanel.multiSensorFrame.initComponents();
            leToolBarPanel.multiSensorFrame.pack();
        }
        leToolBarPanel.multiSensorFrame.setVisible(true);
    }

    // Invoked when window has new multi-sensor ready
    public void addMultiSensor(@Nonnull MultiSensorIcon l) {
        l.setLocation(multiLocX, multiLocY);
        try {
            putItem(l); // note: this calls unionToPanelBounds & setDirty()
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        leToolBarPanel.multiSensorFrame.dispose();
        leToolBarPanel.multiSensorFrame = null;
    }

    /**
     * Set object location and size for icon and label object as it is created.
     * Size comes from the preferredSize; location comes from the fields where
     * the user can spec it.
     *
     * @param obj the positionable object.
     */
    @Override
    public void setNextLocation(@Nonnull Positionable obj) {
        obj.setLocation(xLoc, yLoc);
    }

    //
    // singleton (one per-LayoutEditor) accessors
    //
    private ConnectivityUtil conTools = null;

    @Nonnull
    public ConnectivityUtil getConnectivityUtil() {
        if (conTools == null) {
            conTools = new ConnectivityUtil(this);
        }
        return conTools;
    }

    private LayoutEditorTools tools = null;

    @Nonnull
    public LayoutEditorTools getLETools() {
        if (tools == null) {
            tools = new LayoutEditorTools(this);
        }
        return tools;
    }

    private LayoutEditorAuxTools auxTools = null;

    @Nonnull
    public LayoutEditorAuxTools getLEAuxTools() {
        if (auxTools == null) {
            auxTools = new LayoutEditorAuxTools(this);
        }
        return auxTools;
    }

    private LayoutEditorChecks layoutEditorChecks = null;

    @Nonnull
    public LayoutEditorChecks getLEChecks() {
        if (layoutEditorChecks == null) {
            layoutEditorChecks = new LayoutEditorChecks(this);
        }
        return layoutEditorChecks;
    }

    /**
     * Invoked by DeletePanel menu item Validate user intent before deleting
     */
    @Override
    public boolean deletePanel() {
        // verify deletion
        if (!super.deletePanel()) {
            return false; // return without deleting if "No" response
        }
        clearLayoutTracks();
        return true;
    }

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
                createfloatingEditToolBoxFrame();
                createFloatingHelpPanel();
            } else {
                deletefloatingEditToolBoxFrame();
            }
        } else {
            editToolBarContainerPanel.setVisible(editable);
        }
        setShowHidden(editable);

        if (editable) {
            setScroll(Editor.SCROLL_BOTH);
            _scrollState = restoreScroll;
        } else {
            setScroll(_scrollState);
        }

        // these may not be set up yet...
        if (helpBarPanel != null) {
            if (toolBarSide.equals(ToolBarSide.eFLOAT)) {
                if (floatEditHelpPanel != null) {
                    floatEditHelpPanel.setVisible(isEditable() && getShowHelpBar());
                }
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

        markerImage.forEach((p) -> p.setPositionable(true));
    }

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
    }

    public boolean isAnimating() {
        return animatingLayout;
    }

    public boolean getScroll() {
        // deprecated but kept to allow opening files
        // on version 2.5.1 and earlier
        return _scrollState != Editor.SCROLL_NONE;
    }

//    public Color getDefaultBackgroundColor() {
//        return defaultBackgroundColor;
//    }
    public String getDefaultTrackColor() {
        return ColorUtil.colorToColorName(defaultTrackColor);
    }

    /**
     *
     * Getter defaultTrackColor.
     *
     * @return block default color as Color
     */
    @Nonnull
    public Color getDefaultTrackColorColor() {
        return defaultTrackColor;
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "coloToColorName only returns null if null passed to it")
    public String getDefaultOccupiedTrackColor() {
        return ColorUtil.colorToColorName(defaultOccupiedTrackColor);
    }

    /**
     *
     * Getter defaultOccupiedTrackColor.
     *
     * @return block default occupied color as Color
     */
    @Nonnull
    public Color getDefaultOccupiedTrackColorColor() {
        return defaultOccupiedTrackColor;
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "coloToColorName only returns null if null passed to it")
    public String getDefaultAlternativeTrackColor() {
        return ColorUtil.colorToColorName(defaultAlternativeTrackColor);
    }

    /**
     *
     * Getter defaultAlternativeTrackColor.
     *
     * @return block default alternative color as Color
     */
    @Nonnull
    public Color getDefaultAlternativeTrackColorColor() {
        return defaultAlternativeTrackColor;
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "coloToColorName only returns null if null passed to it")
    public String getDefaultTextColor() {
        return ColorUtil.colorToColorName(defaultTextColor);
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "coloToColorName only returns null if null passed to it")
    public String getTurnoutCircleColor() {
        return ColorUtil.colorToColorName(turnoutCircleColor);
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "coloToColorName only returns null if null passed to it")
    public String getTurnoutCircleThrownColor() {
        return ColorUtil.colorToColorName(turnoutCircleThrownColor);
    }

    public boolean isTurnoutFillControlCircles() {
        return turnoutFillControlCircles;
    }

    public int getTurnoutCircleSize() {
        return turnoutCircleSize;
    }

    public boolean isTurnoutDrawUnselectedLeg() {
        return turnoutDrawUnselectedLeg;
    }

    public String getLayoutName() {
        return layoutName;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getShowHelpBar() {
        return showHelpBar;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getDrawGrid() {
        return drawGrid;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getSnapOnAdd() {
        return snapToGridOnAdd;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getSnapOnMove() {
        return snapToGridOnMove;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getAntialiasingOn() {
        return antialiasingOn;
    }

    public boolean isDrawLayoutTracksLabel() {
        return drawLayoutTracksLabel;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getHighlightSelectedBlock() {
        return highlightSelectedBlockFlag;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getTurnoutCircles() {
        return turnoutCirclesWithoutEditMode;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getTooltipsNotEdit() {
        return tooltipsWithoutEditMode;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getTooltipsInEdit() {
        return tooltipsInEditMode;
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getAutoBlockAssignment() {
        return autoAssignBlocks;
    }

    public void setLayoutDimensions(int windowWidth, int windowHeight, int windowX, int windowY, int panelWidth, int panelHeight) {
        setLayoutDimensions(windowWidth, windowHeight, windowX, windowY, panelWidth, panelHeight, false);
    }

    public void setLayoutDimensions(int windowWidth, int windowHeight, int windowX, int windowY, int panelWidth, int panelHeight, boolean merge) {

        gContext.setUpperLeftX(windowX);
        gContext.setUpperLeftY(windowY);
        setLocation(gContext.getUpperLeftX(), gContext.getUpperLeftY());

        gContext.setWindowWidth(windowWidth);
        gContext.setWindowHeight(windowHeight);
        setSize(windowWidth, windowHeight);

        Rectangle2D panelBounds = new Rectangle2D.Double(0.0, 0.0, panelWidth, panelHeight);

        if (merge) {
            panelBounds.add(calculateMinimumLayoutBounds());
        }
        setPanelBounds(panelBounds);
    }

    @Nonnull
    public Rectangle2D getPanelBounds() {
        return new Rectangle2D.Double(0.0, 0.0, gContext.getLayoutWidth(), gContext.getLayoutHeight());
    }

    public void setPanelBounds(@Nonnull Rectangle2D newBounds) {
        // don't let origin go negative
        newBounds = newBounds.createIntersection(MathUtil.zeroToInfinityRectangle2D);

        if (!getPanelBounds().equals(newBounds)) {
            gContext.setLayoutWidth((int) newBounds.getWidth());
            gContext.setLayoutHeight((int) newBounds.getHeight());
            resetTargetSize();
        }
        log.debug("setPanelBounds(({})", newBounds);
    }

    private void resetTargetSize() {
        int newTargetWidth = (int) (gContext.getLayoutWidth() * getZoom());
        int newTargetHeight = (int) (gContext.getLayoutHeight() * getZoom());

        Dimension targetPanelSize = getTargetPanelSize();
        int oldTargetWidth = (int) targetPanelSize.getWidth();
        int oldTargetHeight = (int) targetPanelSize.getHeight();

        if ((newTargetWidth != oldTargetWidth) || (newTargetHeight != oldTargetHeight)) {
            setTargetPanelSize(newTargetWidth, newTargetHeight);
            adjustScrollBars();
        }
    }

    // this will grow the panel bounds based on items added to the layout
    @Nonnull
    public Rectangle2D unionToPanelBounds(@Nonnull Rectangle2D bounds) {
        Rectangle2D result = getPanelBounds();

        // make room to expand
        Rectangle2D b = MathUtil.inset(bounds, gContext.getGridSize() * gContext.getGridSize2nd() / -2.0);

        // don't let origin go negative
        b = b.createIntersection(MathUtil.zeroToInfinityRectangle2D);

        result.add(b);

        setPanelBounds(result);
        return result;
    }

    /**
     * @param color value to set the default track color to.
     */
    public void setDefaultTrackColor(@Nonnull Color color) {
        defaultTrackColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /**
     * @param color value to set the default occupied track color to.
     */
    public void setDefaultOccupiedTrackColor(@Nonnull Color color) {
        defaultOccupiedTrackColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /**
     * @param color value to set the default alternate track color to.
     */
    public void setDefaultAlternativeTrackColor(@Nonnull Color color) {
        defaultAlternativeTrackColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /**
     * @param color new color for turnout circle.
     */
    public void setTurnoutCircleColor(@CheckForNull Color color) {
        if (color == null) {
            turnoutCircleColor = getDefaultTrackColorColor();
        } else {
            turnoutCircleColor = color;
            JmriColorChooser.addRecentColor(color);
        }
    }

    /**
     * @param color new color for turnout circle.
     */
    public void setTurnoutCircleThrownColor(@CheckForNull Color color) {
        if (color == null) {
            turnoutCircleThrownColor = getDefaultTrackColorColor();
        } else {
            turnoutCircleThrownColor = color;
            JmriColorChooser.addRecentColor(color);
        }
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to fill in turnout control circles, else false.
     */
    @InvokeOnGuiThread
    public void setTurnoutFillControlCircles(boolean state) {
        if (turnoutFillControlCircles != state) {
            turnoutFillControlCircles = state;
            turnoutFillControlCirclesCheckBoxMenuItem.setSelected(turnoutFillControlCircles);
        }
    }

    public void setTurnoutCircleSize(int size) {
        // this is an int
        turnoutCircleSize = size;

        // these are doubles
        circleRadius = SIZE * size;
        circleDiameter = 2.0 * circleRadius;

        setOptionMenuTurnoutCircleSize();
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to draw unselected legs, else false.
     */
    @InvokeOnGuiThread
    public void setTurnoutDrawUnselectedLeg(boolean state) {
        if (turnoutDrawUnselectedLeg != state) {
            turnoutDrawUnselectedLeg = state;
            turnoutDrawUnselectedLegCheckBoxMenuItem.setSelected(turnoutDrawUnselectedLeg);
        }
    }

    /**
     * @param color value to set the default text color to.
     */
    public void setDefaultTextColor(@Nonnull Color color) {
        defaultTextColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /**
     * @param color value to set the panel background to.
     */
    public void setDefaultBackgroundColor(@Nonnull Color color) {
        defaultBackgroundColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    public void setLayoutName(@Nonnull String name) {
        layoutName = name;
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to show the help bar, else false.
     */
    @InvokeOnGuiThread  // due to the setSelected call on a possibly-visible item
    public void setShowHelpBar(boolean state) {
        if (showHelpBar != state) {
            showHelpBar = state;

            // these may not be set up yet...
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
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".showHelpBar", showHelpBar));
        }
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to show the draw grid, else false.
     */
    @InvokeOnGuiThread
    public void setDrawGrid(boolean state) {
        if (drawGrid != state) {
            drawGrid = state;
            showGridCheckBoxMenuItem.setSelected(drawGrid);
        }
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to set snap to grid on add, else false.
     */
    @InvokeOnGuiThread
    public void setSnapOnAdd(boolean state) {
        if (snapToGridOnAdd != state) {
            snapToGridOnAdd = state;
            snapToGridOnAddCheckBoxMenuItem.setSelected(snapToGridOnAdd);
        }
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to set snap on move, else false.
     */
    @InvokeOnGuiThread
    public void setSnapOnMove(boolean state) {
        if (snapToGridOnMove != state) {
            snapToGridOnMove = state;
            snapToGridOnMoveCheckBoxMenuItem.setSelected(snapToGridOnMove);
        }
    }

    /**
     * Should only be invoked on the GUI (Swing) thread.
     *
     * @param state true to set anti-aliasing flag on, else false.
     */
    @InvokeOnGuiThread
    public void setAntialiasingOn(boolean state) {
        if (antialiasingOn != state) {
            antialiasingOn = state;

            // this may not be set up yet...
            if (antialiasingOnCheckBoxMenuItem != null) {
                antialiasingOnCheckBoxMenuItem.setSelected(antialiasingOn);

            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".antialiasingOn", antialiasingOn));
        }
    }

    /**
     *
     * @param state true to set anti-aliasing flag on, else false.
     */
    public void setDrawLayoutTracksLabel(boolean state) {
        if (drawLayoutTracksLabel != state) {
            drawLayoutTracksLabel = state;

            // this may not be set up yet...
            if (drawLayoutTracksLabelCheckBoxMenuItem != null) {
                drawLayoutTracksLabelCheckBoxMenuItem.setSelected(drawLayoutTracksLabel);

            }
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".drawLayoutTracksLabel", drawLayoutTracksLabel));
        }
    }

    // enable/disable using the "Extra" color to highlight the selected block
    public void setHighlightSelectedBlock(boolean state) {
        if (highlightSelectedBlockFlag != state) {
            highlightSelectedBlockFlag = state;

            // this may not be set up yet...
            if (leToolBarPanel.highlightBlockCheckBox != null) {
                leToolBarPanel.highlightBlockCheckBox.setSelected(highlightSelectedBlockFlag);

            }

            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> prefsMgr.setSimplePreferenceState(getWindowFrameRef() + ".highlightSelectedBlock", highlightSelectedBlockFlag));

            // thread this so it won't break the AppVeyor checks
            ThreadingUtil.newThread(() -> {
                if (highlightSelectedBlockFlag) {
                    // use the "Extra" color to highlight the selected block
                    if (!highlightBlockInComboBox(leToolBarPanel.blockIDComboBox)) {
                        highlightBlockInComboBox(leToolBarPanel.blockContentsComboBox);
                    }
                } else {
                    // undo using the "Extra" color to highlight the selected block
                    Block block = leToolBarPanel.blockIDComboBox.getSelectedItem();
                    highlightBlock(null);
                    leToolBarPanel.blockIDComboBox.setSelectedItem(block);
                }
            }).start();
        }
    }

    //
    // highlight the block selected by the specified combo Box
    //
    public boolean highlightBlockInComboBox(@Nonnull NamedBeanComboBox<Block> inComboBox) {
        return highlightBlock(inComboBox.getSelectedItem());
    }

    /**
     * highlight the specified block
     *
     * @param inBlock the block
     * @return true if block was highlighted
     */
    public boolean highlightBlock(@CheckForNull Block inBlock) {
        boolean result = false; // assume failure (pessimist!)

        if (leToolBarPanel.blockIDComboBox.getSelectedItem() != inBlock) {
            leToolBarPanel.blockIDComboBox.setSelectedItem(inBlock);
        }

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class
        );
        Set<Block> l = leToolBarPanel.blockIDComboBox.getManager().getNamedBeanSet();
        for (Block b : l) {
            LayoutBlock lb = lbm.getLayoutBlock(b);
            if (lb != null) {
                boolean enable = ((inBlock != null) && b.equals(inBlock));
                lb.setUseExtraColor(enable);
                result |= enable;
            }
        }
        return result;
    }

    /**
     * highlight the specified layout block
     *
     * @param inLayoutBlock the layout block
     * @return true if layout block was highlighted
     */
    public boolean highlightLayoutBlock(@Nonnull LayoutBlock inLayoutBlock) {
        return highlightBlock(inLayoutBlock.getBlock());
    }

    public void setTurnoutCircles(boolean state) {
        if (turnoutCirclesWithoutEditMode != state) {
            turnoutCirclesWithoutEditMode = state;
            if (turnoutCirclesOnCheckBoxMenuItem != null) {
                turnoutCirclesOnCheckBoxMenuItem.setSelected(turnoutCirclesWithoutEditMode);
            }
        }
    }

    public void setAutoBlockAssignment(boolean boo) {
        if (autoAssignBlocks != boo) {
            autoAssignBlocks = boo;
            if (autoAssignBlocksCheckBoxMenuItem != null) {
                autoAssignBlocksCheckBoxMenuItem.setSelected(autoAssignBlocks);
            }
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
        if (tooltipNoneMenuItem != null) {
            tooltipNoneMenuItem.setSelected((!tooltipsInEditMode) && (!tooltipsWithoutEditMode));
            tooltipAlwaysMenuItem.setSelected((tooltipsInEditMode) && (tooltipsWithoutEditMode));
            tooltipInEditMenuItem.setSelected((tooltipsInEditMode) && (!tooltipsWithoutEditMode));
            tooltipNotInEditMenuItem.setSelected((!tooltipsInEditMode) && (tooltipsWithoutEditMode));
        }
    }

    // accessor routines for turnout size parameters
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

    // reset turnout sizes to program defaults
    private void resetTurnoutSize() {
        turnoutBX = LayoutTurnout.turnoutBXDefault;
        turnoutCX = LayoutTurnout.turnoutCXDefault;
        turnoutWid = LayoutTurnout.turnoutWidDefault;
        xOverLong = LayoutTurnout.xOverLongDefault;
        xOverHWid = LayoutTurnout.xOverHWidDefault;
        xOverShort = LayoutTurnout.xOverShortDefault;
        setDirty();
    }

    public void setDirectTurnoutControl(boolean boo) {
        useDirectTurnoutControl = boo;
        useDirectTurnoutControlCheckBoxMenuItem.setSelected(useDirectTurnoutControl);
    }

    // TODO: Java standard pattern for boolean getters is "isShowHelpBar()"
    public boolean getDirectTurnoutControl() {
        return useDirectTurnoutControl;
    }

    // final initialization routine for loading a LayoutEditor
    public void setConnections() {
        getLayoutTracks().forEach((lt) -> lt.setObjects(this));
        getLEAuxTools().initializeBlockConnectivity();
        log.debug("Initializing Block Connectivity for {}", getLayoutName());

        // reset the panel changed bit
        resetDirty();
    }

    // these are convenience methods to return rectangles
    // to use when (hit point-in-rect testing
    //
    // compute the control point rect at inPoint
    public @Nonnull
    Rectangle2D layoutEditorControlRectAt(@Nonnull Point2D inPoint) {
        return new Rectangle2D.Double(inPoint.getX() - SIZE,
                inPoint.getY() - SIZE, SIZE2, SIZE2);
    }

    // compute the turnout circle control rect at inPoint
    public @Nonnull
    Rectangle2D layoutEditorControlCircleRectAt(@Nonnull Point2D inPoint) {
        return new Rectangle2D.Double(inPoint.getX() - circleRadius,
                inPoint.getY() - circleRadius, circleDiameter, circleDiameter);
    }

    /**
     * Special internal class to allow drawing of layout to a JLayeredPane This
     * is the 'target' pane where the layout is displayed
     */
    @Override
    public void paintTargetPanel(@Nonnull Graphics g) {
        // Nothing to do here
        // All drawing has been moved into LayoutEditorComponent
        // which calls draw.
        // This is so the layout is drawn at level three
        // (above or below the Positionables)
    }

    // get selection rectangle
    @Nonnull
    public Rectangle2D getSelectionRect() {
        double selX = Math.min(selectionX, selectionX + selectionWidth);
        double selY = Math.min(selectionY, selectionY + selectionHeight);
        return new Rectangle2D.Double(selX, selY,
                Math.abs(selectionWidth), Math.abs(selectionHeight));
    }

    // set selection rectangle
    public void setSelectionRect(@Nonnull Rectangle2D selectionRect) {
        // selectionRect = selectionRect.createIntersection(MathUtil.zeroToInfinityRectangle2D);
        selectionX = selectionRect.getX();
        selectionY = selectionRect.getY();
        selectionWidth = selectionRect.getWidth();
        selectionHeight = selectionRect.getHeight();

        // There's already code in the super class (Editor) to draw
        // the selection rect... We just have to set _selectRect
        _selectRect = MathUtil.rectangle2DToRectangle(selectionRect);

        selectionRect = MathUtil.scale(selectionRect, getZoom());

        JComponent targetPanel = getTargetPanel();
        Rectangle targetRect = targetPanel.getVisibleRect();
        // this will make it the size of the targetRect
        // (effectively centering it onscreen)
        Rectangle2D selRect2D = MathUtil.inset(selectionRect,
                (selectionRect.getWidth() - targetRect.getWidth()) / 2.0,
                (selectionRect.getHeight() - targetRect.getHeight()) / 2.0);
        // don't let the origin go negative
        selRect2D = selRect2D.createIntersection(MathUtil.zeroToInfinityRectangle2D);
        Rectangle selRect = MathUtil.rectangle2DToRectangle(selRect2D);
        if (!targetRect.contains(selRect)) {
            targetPanel.scrollRectToVisible(selRect);
        }

        clearSelectionGroups();
        selectionActive = true;
        createSelectionGroups();
        // redrawPanel(); // createSelectionGroups already calls this
    }

    public void setSelectRect(Rectangle rectangle) {
        _selectRect = rectangle;
    }

    /*
    // TODO: This compiles but I can't get the syntax correct to pass the (sub-)class
    public List<LayoutTrack> getLayoutTracksOfClass(@Nonnull Class<LayoutTrack> layoutTrackClass) {
    return getLayoutTracks().stream()
    .filter(item -> item instanceof PositionablePoint)
    .filter(layoutTrackClass::isInstance)
    //.map(layoutTrackClass::cast)  // TODO: Do we need this? if not dead-code-strip
    .collect(Collectors.toList());
    }

    // TODO: This compiles but I can't get the syntax correct to pass the array of (sub-)classes
    public List<LayoutTrack> getLayoutTracksOfClasses(@Nonnull List<Class<? extends LayoutTrack>> layoutTrackClasses) {
    return getLayoutTracks().stream()
    .filter(o -> layoutTrackClasses.contains(o.getClass()))
    .collect(Collectors.toList());
    }

    // TODO: This compiles but I can't get the syntax correct to pass the (sub-)class
    public List<LayoutTrack> getLayoutTracksOfClass(@Nonnull Class<? extends LayoutTrack> layoutTrackClass) {
    return getLayoutTracksOfClasses(new ArrayList<>(Arrays.asList(layoutTrackClass)));
    }

    public List<PositionablePoint> getPositionablePoints() {
    return getLayoutTracksOfClass(PositionablePoint);
    }
     */
    public @Nonnull
    Stream<LayoutTrack> getLayoutTracksOfClass(Class<? extends LayoutTrack> layoutTrackClass) {
        return getLayoutTracks().stream()
                .filter(layoutTrackClass::isInstance)
                .map(layoutTrackClass::cast);
    }

    public @Nonnull
    Stream<LayoutTrackView> getLayoutTrackViewsOfClass(Class<? extends LayoutTrackView> layoutTrackViewClass) {
        return getLayoutTrackViews().stream()
                .filter(layoutTrackViewClass::isInstance)
                .map(layoutTrackViewClass::cast);
    }

    public @Nonnull
    List<PositionablePointView> getPositionablePointViews() {
        return getLayoutTrackViewsOfClass(PositionablePointView.class)
                .map(PositionablePointView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<PositionablePoint> getPositionablePoints() {
        return getLayoutTracksOfClass(PositionablePoint.class)
                .map(PositionablePoint.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutSlipView> getLayoutSlipViews() {
        return getLayoutTrackViewsOfClass(LayoutSlipView.class)
                .map(LayoutSlipView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutSlip> getLayoutSlips() {
        return getLayoutTracksOfClass(LayoutSlip.class)
                .map(LayoutSlip.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<TrackSegmentView> getTrackSegmentViews() {
        return getLayoutTrackViewsOfClass(TrackSegmentView.class)
                .map(TrackSegmentView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<TrackSegment> getTrackSegments() {
        return getLayoutTracksOfClass(TrackSegment.class)
                .map(TrackSegment.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurnoutView> getLayoutTurnoutViews() { // this specifically does not include slips
        return getLayoutTrackViews().stream() // next line excludes LayoutSlips
                .filter((o) -> (!(o instanceof LayoutSlipView) && (o instanceof LayoutTurnoutView)))
                .map(LayoutTurnoutView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurnout> getLayoutTurnouts() { // this specifically does not include slips
        return getLayoutTracks().stream() // next line excludes LayoutSlips
                .filter((o) -> (!(o instanceof LayoutSlip) && (o instanceof LayoutTurnout)))
                .map(LayoutTurnout.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurntable> getLayoutTurntables() {
        return getLayoutTracksOfClass(LayoutTurntable.class)
                .map(LayoutTurntable.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurntableView> getLayoutTurntableViews() {
        return getLayoutTrackViewsOfClass(LayoutTurntableView.class)
                .map(LayoutTurntableView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LevelXing> getLevelXings() {
        return getLayoutTracksOfClass(LevelXing.class)
                .map(LevelXing.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LevelXingView> getLevelXingViews() {
        return getLayoutTrackViewsOfClass(LevelXingView.class)
                .map(LevelXingView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Read-only access to the list of LayoutTrack family objects. The returned
     * list will throw UnsupportedOperationException if you attempt to modify
     * it.
     *
     * @return unmodifiable copy of layout track list.
     */
    @Nonnull
    final public List<LayoutTrack> getLayoutTracks() {
        return Collections.unmodifiableList(layoutTrackList);
    }

    public @Nonnull
    List<LayoutTurnoutView> getLayoutTurnoutAndSlipViews() {
        return getLayoutTrackViewsOfClass(LayoutTurnoutView.class
        )
                .map(LayoutTurnoutView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurnout> getLayoutTurnoutsAndSlips() {
        return getLayoutTracksOfClass(LayoutTurnout.class
        )
                .map(LayoutTurnout.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Read-only access to the list of LayoutTrackView family objects. The
     * returned list will throw UnsupportedOperationException if you attempt to
     * modify it.
     *
     * @return unmodifiable copy of track views.
     */
    @Nonnull
    final public List<LayoutTrackView> getLayoutTrackViews() {
        return Collections.unmodifiableList(layoutTrackViewList);
    }

    private final List<LayoutTrack> layoutTrackList = new ArrayList<>();
    private final List<LayoutTrackView> layoutTrackViewList = new ArrayList<>();
    private final Map<LayoutTrack, LayoutTrackView> trkToView = new HashMap<>();
    private final Map<LayoutTrackView, LayoutTrack> viewToTrk = new HashMap<>();

    // temporary
    final public LayoutTrackView getLayoutTrackView(LayoutTrack trk) {
        LayoutTrackView lv = trkToView.get(trk);
        if (lv == null) {
            log.warn("No View found for {} class {}", trk, trk.getClass());
            throw new IllegalArgumentException("No View found: " + trk.getClass());
        }
        return lv;
    }

    // temporary
    final public LevelXingView getLevelXingView(LevelXing xing) {
        LayoutTrackView lv = trkToView.get(xing);
        if (lv == null) {
            log.warn("No View found for {} class {}", xing, xing.getClass());
            throw new IllegalArgumentException("No View found: " + xing.getClass());
        }
        if (lv instanceof LevelXingView) {
            return (LevelXingView) lv;
        } else {
            log.error("wrong type {} {} found {}", xing, xing.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + xing.getClass());
    }

    // temporary
    final public LayoutTurnoutView getLayoutTurnoutView(LayoutTurnout to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No View found: " + to);
        }
        if (lv instanceof LayoutTurnoutView) {
            return (LayoutTurnoutView) lv;
        } else {
            log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + to.getClass());
    }

    // temporary
    final public LayoutTurntableView getLayoutTurntableView(LayoutTurntable to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: " + to);
        }
        if (lv instanceof LayoutTurntableView) {
            return (LayoutTurntableView) lv;
        } else {
            log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + to.getClass());
    }

    // temporary
    final public LayoutSlipView getLayoutSlipView(LayoutSlip to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: " + to);
        }
        if (lv instanceof LayoutSlipView) {
            return (LayoutSlipView) lv;
        } else {
            log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + to.getClass());
    }

    // temporary
    final public TrackSegmentView getTrackSegmentView(TrackSegment to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: " + to);
        }
        if (lv instanceof TrackSegmentView) {
            return (TrackSegmentView) lv;
        } else {
            log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + to.getClass());
    }

    // temporary
    final public PositionablePointView getPositionablePointView(PositionablePoint to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: " + to);
        }
        if (lv instanceof PositionablePointView) {
            return (PositionablePointView) lv;
        } else {
            log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        }
        throw new IllegalArgumentException("Wrong type: " + to.getClass());
    }

    /**
     * Add a LayoutTrack and LayoutTrackView to the list of LayoutTrack family
     * objects.
     *
     * @param trk the layout track to add.
     */
    final public void addLayoutTrack(@Nonnull LayoutTrack trk, @Nonnull LayoutTrackView v) {
        log.trace("addLayoutTrack {}", trk);
        if (layoutTrackList.contains(trk)) {
            log.warn("LayoutTrack {} already being maintained", trk.getName());
        }

        layoutTrackList.add(trk);
        layoutTrackViewList.add(v);
        trkToView.put(trk, v);
        viewToTrk.put(v, trk);

        unionToPanelBounds(v.getBounds()); // temporary - this should probably _not_ be in the topological part
    }

    /**
     * If item present, delete from the list of LayoutTracks and force a dirty
     * redraw.
     *
     * @param trk the layout track to remove and redraw.
     * @return true is item was deleted and a redraw done.
     */
    final public boolean removeLayoutTrackAndRedraw(@Nonnull LayoutTrack trk) {
        log.trace("removeLayoutTrackAndRedraw {}", trk);
        if (layoutTrackList.contains(trk)) {
            removeLayoutTrack(trk);
            setDirty();
            redrawPanel();
            log.trace("removeLayoutTrackAndRedraw present {}", trk);
            return true;
        }
        log.trace("removeLayoutTrackAndRedraw absent {}", trk);
        return false;
    }

    /**
     * If item present, delete from the list of LayoutTracks and force a dirty
     * redraw.
     *
     * @param trk the layout track to remove.
     */
    final public void removeLayoutTrack(@Nonnull LayoutTrack trk) {
        log.trace("removeLayoutTrack {}", trk);
        layoutTrackList.remove(trk);
        LayoutTrackView v = trkToView.get(trk);
        layoutTrackViewList.remove(v);
        trkToView.remove(trk);
        viewToTrk.remove(v);
    }

    /**
     * Clear the list of layout tracks. Not intended for general use.
     * <p>
     */
    private void clearLayoutTracks() {
        layoutTrackList.clear();
        layoutTrackViewList.clear();
        trkToView.clear();
        viewToTrk.clear();
    }

    public @Nonnull
    List<LayoutShape> getLayoutShapes() {
        return layoutShapes;
    }

    public void sortLayoutShapesByLevel() {
        layoutShapes.sort((lhs, rhs) -> {
            // -1 == less than, 0 == equal, +1 == greater than
            return Integer.signum(lhs.getLevel() - rhs.getLevel());
        });
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is temporary, using the on-screen points from the
     * LayoutTrackViews via @{link LayoutEditor#getCoords}.
     */
    @Override
    public int computeDirection(LayoutTrack trk1, HitPointType h1, LayoutTrack trk2, HitPointType h2) {
        return Path.computeDirection(
                getCoords(trk1, h1),
                getCoords(trk2, h2)
        );
    }

    @Override
    public int computeDirectionToCenter(@Nonnull LayoutTrack trk1, @Nonnull HitPointType h1, @Nonnull PositionablePoint p) {
        return Path.computeDirection(
                getCoords(trk1, h1),
                getPositionablePointView(p).getCoordsCenter()
        );
    }

    @Override
    public int computeDirectionFromCenter(@Nonnull PositionablePoint p, @Nonnull LayoutTrack trk1, @Nonnull HitPointType h1) {
        return Path.computeDirection(
                getPositionablePointView(p).getCoordsCenter(),
                getCoords(trk1, h1)
        );
    }

    @Override
    public boolean showAlignPopup(@Nonnull Positionable l) {
        return false;
    }

    @Override
    public void showToolTip(
            @Nonnull Positionable selection,
            @Nonnull MouseEvent event) {
        ToolTip tip = selection.getToolTip();
        String txt = tip.getText();
        if ((txt != null) && !txt.isEmpty()) {
            tip.setLocation(selection.getX() + selection.getWidth() / 2, selection.getY() + selection.getHeight());
            setToolTip(tip);
        }
    }

    @Override
    public void addToPopUpMenu(
            @Nonnull NamedBean nb,
            @Nonnull JMenuItem item,
            int menu) {
        if ((nb == null) || (item == null)) {
            return;
        }

        List<?> theList = null;

        if (nb instanceof Sensor) {
            theList = sensorList;
        } else if (nb instanceof SignalHead) {
            theList = signalList;
        } else if (nb instanceof SignalMast) {
            theList = signalMastList;
        } else if (nb instanceof Block) {
            theList = blockContentsLabelList;
        } else if (nb instanceof Memory) {
            theList = memoryLabelList;
        }
        if (theList != null) {
            for (Object o : theList) {
                PositionableLabel si = (PositionableLabel) o;
                if ((si.getNamedBean() == nb) && (si.getPopupUtility() != null)) {
                    if (menu != Editor.VIEWPOPUPONLY) {
                        si.getPopupUtility().addEditPopUpMenu(item);
                    }
                    if (menu != Editor.EDITPOPUPONLY) {
                        si.getPopupUtility().addViewPopUpMenu(item);
                    }
                }
            }
        } else if (nb instanceof Turnout) {
            for (LayoutTurnoutView ltv : getLayoutTurnoutAndSlipViews()) {
                if (ltv.getTurnout().equals(nb)) {
                    if (menu != Editor.VIEWPOPUPONLY) {
                        ltv.addEditPopUpMenu(item);
                    }
                    if (menu != Editor.EDITPOPUPONLY) {
                        ltv.addViewPopUpMenu(item);
                    }
                }
            }
        }
    }

    @Override
    public @Nonnull
    String toString() {
        return String.format("LayoutEditor: %s", getLayoutName());
    }

    @Override
    public void vetoableChange(
            @Nonnull PropertyChangeEvent evt)
            throws PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();

        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoInUseLayoutEditorHeader", toString())); // NOI18N
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
                    // Need to expand to get the names of blocks
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

                for (LayoutTurnout t : getLayoutTurnouts()) {
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

                for (LayoutTurntable lx : getLayoutTurntables()) {
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
                message.append(Bundle.getMessage("VetoReferencesWillBeRemoved")); // NOI18N
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // NOI18N
            if (nb instanceof SignalHead) {
                removeSignalHead((SignalHead) nb);
                removeBeanRefs(nb);
            }

            if (nb instanceof Turnout) {
                LayoutTurnout lt = finder.findLayoutTurnoutByBean(nb);

                if (lt != null) {
                    lt.setTurnout("");
                }

                for (LayoutTurnout t : getLayoutTurnouts()) {
                    if (t.getLinkedTurnoutName() != null) {
                        if (t.getLinkedTurnoutName().equals(nb.getSystemName())
                                || ((nb.getUserName() != null) && t.getLinkedTurnoutName().equals(nb.getUserName()))) {
                            t.setLinkedTurnoutName("");
                        }
                    }

                    if (nb.equals(t.getSecondTurnout())) {
                        t.setSecondTurnout("");
                    }
                }

                for (LayoutSlip sl : getLayoutSlips()) {
                    if (nb.equals(sl.getTurnout())) {
                        sl.setTurnout("");
                    }

                    if (nb.equals(sl.getTurnoutB())) {
                        sl.setTurnoutB("");
                    }
                }

                for (LayoutTurntable lx : getLayoutTurntables()) {
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
    }

//    private void rename(String inFrom, String inTo) {
//
//    }
    @Override
    public void dispose() {
        if (leToolBarPanel.sensorFrame != null) {
            leToolBarPanel.sensorFrame.dispose();
            leToolBarPanel.sensorFrame = null;
        }
        if (leToolBarPanel.signalFrame != null) {
            leToolBarPanel.signalFrame.dispose();
            leToolBarPanel.signalFrame = null;
        }
        if (leToolBarPanel.iconFrame != null) {
            leToolBarPanel.iconFrame.dispose();
            leToolBarPanel.iconFrame = null;
        }
        super.dispose();

    }

    // package protected
    class TurnoutComboBoxPopupMenuListener implements PopupMenuListener {

        private final NamedBeanComboBox<Turnout> comboBox;
        private final List<Turnout> currentTurnouts;

        public TurnoutComboBoxPopupMenuListener(NamedBeanComboBox<Turnout> comboBox, List<Turnout> currentTurnouts) {
            this.comboBox = comboBox;
            this.currentTurnouts = currentTurnouts;
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            // This method is called before the popup menu becomes visible.
            log.debug("PopupMenuWillBecomeVisible");
            Set<Turnout> l = new HashSet<>();
            comboBox.getManager().getNamedBeanSet().forEach((turnout) -> {
                if (!currentTurnouts.contains(turnout)) {
                    if (!validatePhysicalTurnout(turnout.getDisplayName(), null)) {
                        l.add(turnout);
                    }
                }
            });
            comboBox.setExcludedItems(l);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
            // This method is called before the popup menu becomes invisible
            log.debug("PopupMenuWillBecomeInvisible");
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent event) {
            // This method is called when the popup menu is canceled
            log.debug("PopupMenuCanceled");
        }
    }

    /**
     * Create a listener that will exclude turnouts that are present in the
     * current panel.
     *
     * @param comboBox The NamedBeanComboBox that contains the turnout list.
     * @return A PopupMenuListener
     */
    public TurnoutComboBoxPopupMenuListener newTurnoutComboBoxPopupMenuListener(NamedBeanComboBox<Turnout> comboBox) {
        return new TurnoutComboBoxPopupMenuListener(comboBox, new ArrayList<>());
    }

    /**
     * Create a listener that will exclude turnouts that are present in the
     * current panel. The list of current turnouts are not excluded.
     *
     * @param comboBox        The NamedBeanComboBox that contains the turnout
     *                        list.
     * @param currentTurnouts The turnouts to be left in the turnout list.
     * @return A PopupMenuListener
     */
    public TurnoutComboBoxPopupMenuListener newTurnoutComboBoxPopupMenuListener(NamedBeanComboBox<Turnout> comboBox, List<Turnout> currentTurnouts) {
        return new TurnoutComboBoxPopupMenuListener(comboBox, currentTurnouts);
    }

    List<NamedBeanUsageReport> usageReport;

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        usageReport = new ArrayList<>();
        if (bean != null) {
            usageReport = super.getUsageReport(bean);

            // LE Specific checks
            // Turnouts
            findTurnoutUsage(bean);

            // Check A, EB, EC for sensors, masts, heads
            findPositionalUsage(bean);

            // Level Crossings
            findXingWhereUsed(bean);

            // Track segments
            findSegmentWhereUsed(bean);
        }
        return usageReport;
    }

    void findTurnoutUsage(NamedBean bean) {
        for (LayoutTurnout turnout : getLayoutTurnoutsAndSlips()) {
            String data = getUsageData(turnout);

            if (bean.equals(turnout.getTurnout())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnout", data));
            }
            if (bean.equals(turnout.getSecondTurnout())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnout2", data));
            }

            if (isLBLockUsed(bean, turnout.getLayoutBlock())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutBlock", data));
            }
            if (turnout.hasEnteringDoubleTrack()) {
                if (isLBLockUsed(bean, turnout.getLayoutBlockB())) {
                    usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutBlock", data));
                }
                if (isLBLockUsed(bean, turnout.getLayoutBlockC())) {
                    usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutBlock", data));
                }
                if (isLBLockUsed(bean, turnout.getLayoutBlockD())) {
                    usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutBlock", data));
                }
            }

            if (bean.equals(turnout.getSensorA())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSensor", data));
            }
            if (bean.equals(turnout.getSensorB())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSensor", data));
            }
            if (bean.equals(turnout.getSensorC())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSensor", data));
            }
            if (bean.equals(turnout.getSensorD())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSensor", data));
            }

            if (bean.equals(turnout.getSignalAMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalMast", data));
            }
            if (bean.equals(turnout.getSignalBMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalMast", data));
            }
            if (bean.equals(turnout.getSignalCMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalMast", data));
            }
            if (bean.equals(turnout.getSignalDMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalMast", data));
            }

            if (bean.equals(turnout.getSignalA1())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalA2())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalA3())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalB1())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalB2())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalC1())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalC2())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalD1())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
            if (bean.equals(turnout.getSignalD2())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorTurnoutSignalHead", data));
            }
        }
    }

    void findPositionalUsage(NamedBean bean) {
        for (PositionablePoint point : getPositionablePoints()) {
            String data = getUsageData(point);
            if (bean.equals(point.getEastBoundSensor())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSensor", data));
            }
            if (bean.equals(point.getWestBoundSensor())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSensor", data));
            }
            if (bean.equals(point.getEastBoundSignalHead())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSignalHead", data));
            }
            if (bean.equals(point.getWestBoundSignalHead())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSignalHead", data));
            }
            if (bean.equals(point.getEastBoundSignalMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSignalMast", data));
            }
            if (bean.equals(point.getWestBoundSignalMast())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorPointSignalMast", data));
            }
        }
    }

    void findSegmentWhereUsed(NamedBean bean) {
        for (TrackSegment segment : getTrackSegments()) {
            if (isLBLockUsed(bean, segment.getLayoutBlock())) {
                String data = getUsageData(segment);
                usageReport.add(new NamedBeanUsageReport("LayoutEditorSegmentBlock", data));
            }
        }
    }

    void findXingWhereUsed(NamedBean bean) {
        for (LevelXing xing : getLevelXings()) {
            String data = getUsageData(xing);
            if (isLBLockUsed(bean, xing.getLayoutBlockAC())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingBlock", data));
            }
            if (isLBLockUsed(bean, xing.getLayoutBlockBD())) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingBlock", data));
            }
            if (isUsedInXing(bean, xing, LevelXing.Geometry.POINTA)) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingOther", data));
            }
            if (isUsedInXing(bean, xing, LevelXing.Geometry.POINTB)) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingOther", data));
            }
            if (isUsedInXing(bean, xing, LevelXing.Geometry.POINTC)) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingOther", data));
            }
            if (isUsedInXing(bean, xing, LevelXing.Geometry.POINTD)) {
                usageReport.add(new NamedBeanUsageReport("LayoutEditorXingOther", data));
            }
        }
    }

    String getUsageData(LayoutTrack track) {
        LayoutTrackView trackView = getLayoutTrackView(track);
        Point2D point = trackView.getCoordsCenter();
        if (trackView instanceof TrackSegmentView) {
            TrackSegmentView segmentView = (TrackSegmentView) trackView;
            point = new Point2D.Double(segmentView.getCentreSegX(), segmentView.getCentreSegY());
        }
        return String.format("%s :: x=%d, y=%d",
                track.getClass().getSimpleName(),
                Math.round(point.getX()),
                Math.round(point.getY()));
    }

    boolean isLBLockUsed(NamedBean bean, LayoutBlock lblock) {
        boolean result = false;
        if (lblock != null) {
            if (bean.equals(lblock.getBlock())) {
                result = true;
            }
        }
        return result;
    }

    boolean isUsedInXing(NamedBean bean, LevelXing xing, LevelXing.Geometry point) {
        boolean result = false;
        if (bean.equals(xing.getSensor(point))) {
            result = true;
        }
        if (bean.equals(xing.getSignalHead(point))) {
            result = true;
        }
        if (bean.equals(xing.getSignalMast(point))) {
            result = true;
        }
        return result;
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditor.class);
}
