package jmri.jmrit.display;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.*;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.DirectorySearcher;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.controlPanelEditor.shape.PositionableShape;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.tools.swing.DeleteBean;
import jmri.jmrit.logixng.tools.swing.LogixNGEditor;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.util.DnDStringImportHandler;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriColorChooser;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.JmriMouseMotionListener;

/**
 * This is the Model and a Controller for panel editor Views. (Panel Editor,
 * Layout Editor or any subsequent editors) The Model is simply a list of
 * Positionable objects added to a "target panel". Control of the display
 * attributes of the Positionable objects is done here. However, control of
 * mouse events is passed to the editor views, so control is also done by the
 * editor views.
 * <p>
 * The "contents" List keeps track of all the objects added to the target frame
 * for later manipulation. This class only locates and moves "target panel"
 * items, and does not control their appearance - that is left for the editor
 * views.
 * <p>
 * The Editor has tri-state "flags" to control the display of Positionable
 * object attributes globally - i.e. "on" or "off" for all - or as a third
 * state, permits the display control "locally" by corresponding flags in each
 * Positionable object
 * <p>
 * The title of the target and the editor panel are kept consistent via the
 * {#setTitle} method.
 * <p>
 * Mouse events are initial handled here, rather than in the individual
 * displayed objects, so that selection boxes for moving multiple objects can be
 * provided.
 * <p>
 * This class also implements an effective ToolTipManager replacement, because
 * the standard Swing one can't deal with the coordinate changes used to zoom a
 * panel. It works by controlling the contents of the _tooltip instance
 * variable, and triggering repaint of the target window when the tooltip
 * changes. The window painting then explicitly draws the tooltip for the
 * underlying object.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2003, 2007
 * @author Dennis Miller 2004
 * @author Howard G. Penny Copyright: Copyright (c) 2005
 * @author Matthew Harris Copyright: Copyright (c) 2009
 * @author Pete Cressman Copyright: Copyright (c) 2009, 2010, 2011
 *
 */
abstract public class Editor extends JmriJFrameWithPermissions
        implements JmriMouseListener, JmriMouseMotionListener, ActionListener,
                KeyListener, VetoableChangeListener {

    public static final int BKG = 1;
    public static final int TEMP = 2;
    public static final int ICONS = 3;
    public static final int LABELS = 4;
    public static final int MEMORIES = 5;
    public static final int REPORTERS = 5;
    public static final int SECURITY = 6;
    public static final int TURNOUTS = 7;
    public static final int LIGHTS = 8;
    public static final int SIGNALS = 9;
    public static final int SENSORS = 10;
    public static final int CLOCK = 10;
    public static final int MARKERS = 10;
    public static final int NUM_LEVELS = 10;

    public static final int SCROLL_NONE = 0;
    public static final int SCROLL_BOTH = 1;
    public static final int SCROLL_HORIZONTAL = 2;
    public static final int SCROLL_VERTICAL = 3;

    public static final Color HIGHLIGHT_COLOR = new Color(204, 207, 88);

    public static final String POSITIONABLE_FLAVOR = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.jmrit.display.Positionable";

    private boolean _loadFailed = false;

    private ArrayList<Positionable> _contents = new ArrayList<>();
    private Map<String, Positionable> _idContents = new HashMap<>();
    private Map<String, Set<Positionable>> _classContents = new HashMap<>();
    protected JLayeredPane _targetPanel;
    private JFrame _targetFrame;
    private JScrollPane _panelScrollPane;

    // Option menu items
    protected int _scrollState = SCROLL_NONE;
    protected boolean _editable = true;
    private boolean _positionable = true;
    private boolean _controlLayout = true;
    private boolean _showHidden = true;
    private boolean _showToolTip = true;
//    private boolean _showCoordinates = true;

    final public static int OPTION_POSITION = 1;
    final public static int OPTION_CONTROLS = 2;
    final public static int OPTION_HIDDEN = 3;
    final public static int OPTION_TOOLTIP = 4;
//    final public static int OPTION_COORDS = 5;

    private boolean _globalSetsLocal = true;    // pre 2.9.6 behavior
    private boolean _useGlobalFlag = false;     // pre 2.9.6 behavior

    // mouse methods variables
    protected int _lastX;
    protected int _lastY;
    BasicStroke DASHED_LINE = new BasicStroke(1f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL,
            10f, new float[]{10f, 10f}, 0f);

    protected Rectangle _selectRect = null;
    protected Rectangle _highlightcomponent = null;
    protected boolean _dragging = false;
    protected ArrayList<Positionable> _selectionGroup = null;  // items gathered inside fence

    protected Positionable _currentSelection;
    private ToolTip _defaultToolTip;
    private ToolTip _tooltip = null;

    // Accessible to editor views
    protected int xLoc = 0;     // x coord of selected Positionable
    protected int yLoc = 0;     // y coord of selected Positionable
    protected int _anchorX;     // x coord when mousePressed
    protected int _anchorY;     // y coord when mousePressed

//    private boolean delayedPopupTrigger = false; // Used to delay the request of a popup, on a mouse press as this may conflict with a drag event
    protected double _paintScale = 1.0;   // scale for _targetPanel drawing

    protected Color defaultBackgroundColor = Color.lightGray;
    protected boolean _pastePending = false;

    // map of icon editor frames (incl, icon editor) keyed by name
    protected HashMap<String, JFrameItem> _iconEditorFrame = new HashMap<>();

    // store panelMenu state so preference is retained on headless systems
    private boolean panelMenuIsVisible = true;

    private boolean _inEditInlineLogixNGMode = false;
    private LogixNGEditor _inlineLogixNGEdit;

    public Editor() {
    }

    public Editor(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        setName(name);
        _defaultToolTip = new ToolTip(null, 0, 0, null);
        setVisible(false);
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
        InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(EditorManager.class).add(this);
    }

    public Editor(String name) {
        this(name, true, true);
    }

    /**
     * Set <strong>white</strong> as the default background color for panels created using the <strong>New Panel</strong> menu item.
     * Overriden by LE to use a different default background color and set other initial defaults.
     */
    public void newPanelDefaults() {
        setBackgroundColor(Color.WHITE);
    }

    public void loadFailed() {
        _loadFailed = true;
    }

    NamedIcon _newIcon;
    boolean _ignore = false;
    boolean _delete;
    HashMap<String, String> _urlMap = new HashMap<>();

    public NamedIcon loadFailed(String msg, String url) {
        log.debug("loadFailed _ignore= {} {}", _ignore, msg);
        if (_urlMap == null) {
            _urlMap = new HashMap<>();
        }
        String goodUrl = _urlMap.get(url);
        if (goodUrl != null) {
            return NamedIcon.getIconByName(goodUrl);
        }
        if (_ignore) {
            _loadFailed = true;
            return NamedIcon.getIconByName(url);
        }
        _newIcon = null;
        _delete = false;
        (new UrlErrorDialog(msg, url)).setVisible(true);

        if (_delete) {
            return null;
        }
        if (_newIcon == null) {
            _loadFailed = true;
            _newIcon = NamedIcon.getIconByName(url);
        }
        return _newIcon;
    }

    public class UrlErrorDialog extends JDialog {

        private final JTextField _urlField;
        private final CatalogPanel _catalog;
        private final String _badUrl;

        UrlErrorDialog(String msg, String url) {
            super(_targetFrame, Bundle.getMessage("BadIcon"), true);
            _badUrl = url;
            JPanel content = new JPanel();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalStrut(10));
            panel.add(new JLabel(MessageFormat.format(Bundle.getMessage("IconUrlError"), msg)));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt1")));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt1A")));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt1B")));
            panel.add(Box.createVerticalStrut(10));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt2", Bundle.getMessage("ButtonContinue"))));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt3", Bundle.getMessage("ButtonDelete"))));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt3A")));
            panel.add(Box.createVerticalStrut(10));
            panel.add(new JLabel(Bundle.getMessage("UrlErrorPrompt4", Bundle.getMessage("ButtonIgnore"))));
            panel.add(Box.createVerticalStrut(10));
            _urlField = new JTextField(url);
            _urlField.setDragEnabled(true);
            _urlField.setTransferHandler(new DnDStringImportHandler());
            panel.add(_urlField);
            panel.add(makeDoneButtonPanel());
            _urlField.setToolTipText(Bundle.getMessage("TooltipFixUrl"));
            panel.setToolTipText(Bundle.getMessage("TooltipFixUrl"));
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIconToText"));
            panel.add(_catalog);
            content.add(panel);
            setContentPane(content);
            setLocation(200, 100);
            pack();
        }

        private JPanel makeDoneButtonPanel() {
            JPanel result = new JPanel();
            result.setLayout(new FlowLayout());
            JButton doneButton = new JButton(Bundle.getMessage("ButtonContinue"));
            doneButton.addActionListener(a -> {
                _newIcon = NamedIcon.getIconByName(_urlField.getText());
                if (_newIcon != null) {
                    _urlMap.put(_badUrl, _urlField.getText());
                }
                UrlErrorDialog.this.dispose();
            });
            doneButton.setToolTipText(Bundle.getMessage("TooltipContinue"));
            result.add(doneButton);

            JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
            deleteButton.addActionListener(a -> {
                _delete = true;
                UrlErrorDialog.this.dispose();
            });
            result.add(deleteButton);
            deleteButton.setToolTipText(Bundle.getMessage("TooltipDelete"));

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonIgnore"));
            cancelButton.addActionListener(a -> {
                _ignore = true;
                UrlErrorDialog.this.dispose();
            });
            result.add(cancelButton);
            cancelButton.setToolTipText(Bundle.getMessage("TooltipIgnore"));
            return result;
        }
    }

    public void disposeLoadData() {
        _urlMap = null;
    }

    public boolean loadOK() {
        return !_loadFailed;
    }

    public List<Positionable> getContents() {
        return Collections.unmodifiableList(_contents);
    }

    public Map<String, Positionable> getIdContents() {
        return Collections.unmodifiableMap(_idContents);
    }

    public Set<String> getClassNames() {
        return Collections.unmodifiableSet(_classContents.keySet());
    }

    public Set<Positionable> getPositionablesByClassName(String className) {
        Set<Positionable> set = _classContents.get(className);
        if (set == null) {
            return null;
        }
        return Collections.unmodifiableSet(set);
    }

    public void setDefaultToolTip(ToolTip dtt) {
        _defaultToolTip = dtt;
    }

    //
    // *************** setting the main panel and frame ***************
    //
    /**
     * Set the target panel.
     * <p>
     * An Editor may or may not choose to use 'this' as its frame or the
     * interior class 'TargetPane' for its targetPanel.
     *
     * @param targetPanel the panel to be edited
     * @param frame       the frame to embed the panel in
     */
    protected void setTargetPanel(JLayeredPane targetPanel, JmriJFrame frame) {
        if (targetPanel == null) {
            _targetPanel = new TargetPane();
        } else {
            _targetPanel = targetPanel;
        }
        // If on a headless system, set heavyweight components to null
        // and don't attach mouse and keyboard listeners to the panel
        if (GraphicsEnvironment.isHeadless()) {
            _panelScrollPane = null;
            _targetFrame = null;
            return;
        }
        if (frame == null) {
            _targetFrame = this;
        } else {
            _targetFrame = frame;
        }
        _targetFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        _panelScrollPane = new JScrollPane(_targetPanel);
        Container contentPane = _targetFrame.getContentPane();
        contentPane.add(_panelScrollPane);
        _targetFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                targetWindowClosingEvent(e);
            }
        });
        _targetPanel.addMouseListener(JmriMouseListener.adapt(this));
        _targetPanel.addMouseMotionListener(JmriMouseMotionListener.adapt(this));
        _targetPanel.setFocusable(true);
        _targetPanel.addKeyListener(this);
        //_targetFrame.pack();
    }

    protected void setTargetPanelSize(int w, int h) {
//        log.debug("setTargetPanelSize now w={}, h={}", w, h);
        _targetPanel.setSize(w, h);
        _targetPanel.invalidate();
    }

    protected Dimension getTargetPanelSize() {
        return _targetPanel.getSize();
    }

    /**
     * Allow public access to the target (content) panel for external
     * modification, particularly from scripts.
     *
     * @return the target panel
     */
    public final JComponent getTargetPanel() {
        return _targetPanel;
    }

    /**
     * Allow public access to the scroll pane for external control of position,
     * particularly from scripts.
     *
     * @return the scroll pane containing the target panel
     */
    public final JScrollPane getPanelScrollPane() {
        return _panelScrollPane;
    }

    public final JFrame getTargetFrame() {
        return _targetFrame;
    }

    public Color getBackgroundColor() {
        if (_targetPanel instanceof TargetPane) {
            TargetPane tmp = (TargetPane) _targetPanel;
            return tmp.getBackgroundColor();
        } else {
            return null;
        }
    }

    public void setBackgroundColor(Color col) {
        if (_targetPanel instanceof TargetPane) {
            TargetPane tmp = (TargetPane) _targetPanel;
            tmp.setBackgroundColor(col);
        }
        JmriColorChooser.addRecentColor(col);
    }

    public void clearBackgroundColor() {
        if (_targetPanel instanceof TargetPane) {
            TargetPane tmp = (TargetPane) _targetPanel;
            tmp.clearBackgroundColor();
        }
    }

    /**
     * Get scale for TargetPane drawing.
     *
     * @return the scale
     */
    public final double getPaintScale() {
        return _paintScale;
    }

    protected final void setPaintScale(double newScale) {
        double ratio = newScale / _paintScale;
        _paintScale = newScale;
        setScrollbarScale(ratio);
    }

    private ToolTipTimer _tooltipTimer;

    protected void setToolTip(ToolTip tt) {
        if (tt != null) {
            var pos = tt.getPositionable();
            if (pos != null) {  // LE turnout tooltips do not have a Positionable
                if (pos.isHidden() && !isEditable()) {
                    // Skip hidden objects
                    return;
                }
            }
        }

        if (tt == null) {
            _tooltip = null;
            if (_tooltipTimer != null) {
                _tooltipTimer.stop();
                _tooltipTimer = null;
                _targetPanel.repaint();
            }

        } else if (_tooltip == null && _tooltipTimer == null) {
            log.debug("start :: tt = {}, tooltip = {}, timer = {}", tt, _tooltip, _tooltipTimer);
            _tooltipTimer = new ToolTipTimer(TOOLTIPSHOWDELAY, this, tt);
            _tooltipTimer.setRepeats(false);
            _tooltipTimer.start();
        }
    }

    static int TOOLTIPSHOWDELAY = 1000; // msec
    static int TOOLTIPDISMISSDELAY = 4000;  // msec

    /*
     * Wait TOOLTIPSHOWDELAY then show tooltip. Wait TOOLTIPDISMISSDELAY and
     * disappear.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        //log.debug("_tooltipTimer actionPerformed: Timer on= {}", (_tooltipTimer!=null));
        if (_tooltipTimer != null) {
            _tooltip = _tooltipTimer.getToolTip();
            _tooltipTimer.stop();
        }
        if (_tooltip != null) {
            _tooltipTimer = new ToolTipTimer(TOOLTIPDISMISSDELAY, this, null);
            _tooltipTimer.setRepeats(false);
            _tooltipTimer.start();
        } else {
            _tooltipTimer = null;
        }
        _targetPanel.repaint();
    }

    static class ToolTipTimer extends Timer {

        private final ToolTip tooltip;

        ToolTipTimer(int delay, ActionListener listener, ToolTip tip) {
            super(delay, listener);
            tooltip = tip;
        }

        ToolTip getToolTip() {
            return tooltip;
        }
    }

    /**
     * Special internal class to allow drawing of layout to a JLayeredPane. This
     * is the 'target' pane where the layout is displayed.
     */
    public class TargetPane extends JLayeredPane {

        private int h = 100;
        private int w = 150;

        public TargetPane() {
            setLayout(null);
        }

        @Override
        public void setSize(int width, int height) {
//            log.debug("size now w={}, h={}", width, height);
            this.h = height;
            this.w = width;
            super.setSize(width, height);
        }

        @Override
        public Dimension getSize() {
            return new Dimension(w, h);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(w, h);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Component add(@Nonnull Component c, int i) {
            int hnew = Math.max(this.h, c.getLocation().y + c.getSize().height);
            int wnew = Math.max(this.w, c.getLocation().x + c.getSize().width);
            if (hnew > h || wnew > w) {
//                log.debug("size was {},{} - i ={}", w, h, i);
                setSize(wnew, hnew);
            }
            return super.add(c, i);
        }

        @Override
        public void add(@Nonnull Component c, Object o) {
            super.add(c, o);
            int hnew = Math.max(h, c.getLocation().y + c.getSize().height);
            int wnew = Math.max(w, c.getLocation().x + c.getSize().width);
            if (hnew > h || wnew > w) {
                // log.debug("adding of {} with Object - i=", c.getSize(), o);
                setSize(wnew, hnew);
            }
        }

        private Color _highlightColor = HIGHLIGHT_COLOR;
        private Color _selectGroupColor = HIGHLIGHT_COLOR;
        private Color _selectRectColor = Color.red;
        private transient Stroke _selectRectStroke = DASHED_LINE;

        public void setHighlightColor(Color color) {
            _highlightColor = color;
        }

        public Color getHighlightColor() {
            return _highlightColor;
        }

        public void setSelectGroupColor(Color color) {
            _selectGroupColor = color;
        }

        public void setSelectRectColor(Color color) {
            _selectRectColor = color;
        }

        public void setSelectRectStroke(Stroke stroke) {
            _selectRectStroke = stroke;
        }

        public void setDefaultColors() {
            _highlightColor = HIGHLIGHT_COLOR;
            _selectGroupColor = HIGHLIGHT_COLOR;
            _selectRectColor = Color.red;
            _selectRectStroke = DASHED_LINE;
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = null;
            if (g instanceof Graphics2D) {
                g2d = (Graphics2D) g;
                g2d.scale(_paintScale, _paintScale);
            }
            super.paint(g);

            Stroke stroke = new BasicStroke();
            if (g2d != null) {
                stroke = g2d.getStroke();
            }
            Color color = g.getColor();
            if (_selectRect != null) {
                //Draw a rectangle on top of the image.
                if (g2d != null) {
                    g2d.setStroke(_selectRectStroke);
                }
                g.setColor(_selectRectColor);
                g.drawRect(_selectRect.x, _selectRect.y, _selectRect.width, _selectRect.height);
            }
            if (_selectionGroup != null) {
                g.setColor(_selectGroupColor);
                if (g2d != null) {
                    g2d.setStroke(new BasicStroke(2.0f));
                }
                for (Positionable p : _selectionGroup) {
                    if (p != null) {
                        if (!(p instanceof PositionableShape)) {
                            g.drawRect(p.getX(), p.getY(), p.maxWidth(), p.maxHeight());
                        } else {
                            PositionableShape s = (PositionableShape) p;
                            s.drawHandles();
                        }
                    }
                }
            }
            //Draws a border around the highlighted component
            if (_highlightcomponent != null) {
                g.setColor(_highlightColor);
                if (g2d != null) {
                    g2d.setStroke(new BasicStroke(2.0f));
                }
                g.drawRect(_highlightcomponent.x, _highlightcomponent.y,
                        _highlightcomponent.width, _highlightcomponent.height);
            }
            paintTargetPanel(g);

            g.setColor(color);
            if (g2d != null) {
                g2d.setStroke(stroke);
            }
            if (_tooltip != null) {
                _tooltip.paint(g2d, _paintScale);
            }
        }

        public void setBackgroundColor(Color col) {
            setBackground(col);
            setOpaque(true);
            JmriColorChooser.addRecentColor(col);
        }

        public void clearBackgroundColor() {
            setOpaque(false);
        }

        public Color getBackgroundColor() {
            if (isOpaque()) {
                return getBackground();
            }
            return null;
        }
    }

    private void setScrollbarScale(double ratio) {
        //resize the panel to reflect scaling
        Dimension dim = _targetPanel.getSize();
        int tpWidth = (int) ((dim.width) * ratio);
        int tpHeight = (int) ((dim.height) * ratio);
        _targetPanel.setSize(tpWidth, tpHeight);
        log.debug("setScrollbarScale: ratio= {}, tpWidth= {}, tpHeight= {}", ratio, tpWidth, tpHeight);
        // compute new scroll bar positions to keep upper left same
        JScrollBar horScroll = _panelScrollPane.getHorizontalScrollBar();
        JScrollBar vertScroll = _panelScrollPane.getVerticalScrollBar();
        int hScroll = (int) (horScroll.getValue() * ratio);
        int vScroll = (int) (vertScroll.getValue() * ratio);
        // set scrollbars maximum range (otherwise setValue may fail);
        horScroll.setMaximum((int) ((horScroll.getMaximum()) * ratio));
        vertScroll.setMaximum((int) ((vertScroll.getMaximum()) * ratio));
        // set scroll bar positions
        horScroll.setValue(hScroll);
        vertScroll.setValue(vScroll);
    }

    /*
     * ********************** Options setup *********************
     */
    /**
     * Control whether target panel items are editable. Does this by invoke the
     * {@link Positionable#setEditable(boolean)} function of each item on the
     * target panel. This also controls the relevant pop-up menu items (which
     * are the primary way that items are edited).
     *
     * @param state true for editable.
     */
    public void setAllEditable(boolean state) {
        _editable = state;
        for (Positionable _content : _contents) {
            _content.setEditable(state);
        }
        if (!_editable) {
            _highlightcomponent = null;
            deselectSelectionGroup();
        }
    }

    public void deselectSelectionGroup() {
        if (_selectionGroup == null) {
            return;
        }
        for (Positionable p : _selectionGroup) {
            if (p instanceof PositionableShape) {
                PositionableShape s = (PositionableShape) p;
                s.removeHandles();
            }
        }
        _selectionGroup = null;
    }

    // accessor routines for persistent information
    public boolean isEditable() {
        return _editable;
    }

    /**
     * Set which flag should be used, global or local for Positioning and
     * Control of individual items. Items call getFlag() to return the
     * appropriate flag it should use.
     *
     * @param set True if global flags should be used for positioning.
     */
    public void setUseGlobalFlag(boolean set) {
        _useGlobalFlag = set;
    }

    public boolean useGlobalFlag() {
        return _useGlobalFlag;
    }

    /**
     * Get the setting for the specified option.
     *
     * @param whichOption The option to get
     * @param localFlag   is the current setting of the item
     * @return The setting for the option
     */
    public boolean getFlag(int whichOption, boolean localFlag) {
        //log.debug("getFlag Option= {}, _useGlobalFlag={} localFlag={}", whichOption, _useGlobalFlag, localFlag);
        if (_useGlobalFlag) {
            switch (whichOption) {
                case OPTION_POSITION:
                    return _positionable;
                case OPTION_CONTROLS:
                    return _controlLayout;
                case OPTION_HIDDEN:
                    return _showHidden;
                case OPTION_TOOLTIP:
                    return _showToolTip;
//                case OPTION_COORDS:
//                    return _showCoordinates;
                default:
                    log.warn("Unhandled which option code: {}", whichOption);
                    break;
            }
        }
        return localFlag;
    }

    /**
     * Set if {@link #setAllControlling(boolean)} and
     * {@link #setAllPositionable(boolean)} are set for existing as well as new
     * items.
     *
     * @param set true if setAllControlling() and setAllPositionable() are set
     *            for existing items
     */
    public void setGlobalSetsLocalFlag(boolean set) {
        _globalSetsLocal = set;
    }

    /**
     * Control whether panel items can be positioned. Markers can always be
     * positioned.
     *
     * @param state true to set all items positionable; false otherwise
     */
    public void setAllPositionable(boolean state) {
        _positionable = state;
        if (_globalSetsLocal) {
            for (Positionable p : _contents) {
                // don't allow backgrounds to be set positionable by global flag
                if (!state || p.getDisplayLevel() != BKG) {
                    p.setPositionable(state);
                }
            }
        }
    }

    public boolean allPositionable() {
        return _positionable;
    }

    /**
     * Control whether target panel items are controlling layout items.
     * <p>
     * Does this by invoking the {@link Positionable#setControlling} function of
     * each item on the target panel. This also controls the relevant pop-up
     * menu items.
     *
     * @param state true for controlling.
     */
    public void setAllControlling(boolean state) {
        _controlLayout = state;
        if (_globalSetsLocal) {
            for (Positionable _content : _contents) {
                _content.setControlling(state);
            }
        }
    }

    public boolean allControlling() {
        return _controlLayout;
    }

    /**
     * Control whether target panel hidden items are visible or not. Does this
     * by invoke the {@link Positionable#setHidden} function of each item on the
     * target panel.
     *
     * @param state true for Visible.
     */
    public void setShowHidden(boolean state) {
        _showHidden = state;
        if (_showHidden) {
            for (Positionable _content : _contents) {
                _content.setVisible(true);
            }
        } else {
            for (Positionable _content : _contents) {
                _content.showHidden();
            }
        }
    }

    public boolean showHidden() {
        return _showHidden;
    }

    public void setAllShowToolTip(boolean state) {
        _showToolTip = state;
        for (Positionable _content : _contents) {
            _content.setShowToolTip(state);
        }
    }

    public boolean showToolTip() {
        return _showToolTip;
    }

    /*
     * Control whether target panel items will show their coordinates in their
     * popup menu.
     *
     * @param state true for show coordinates.
     */
 /*
     public void setShowCoordinates(boolean state) {
     _showCoordinates = state;
     for (int i = 0; i<_contents.size(); i++) {
     _contents.get(i).setViewCoordinates(state);
     }
     }
     public boolean showCoordinates() {
     return _showCoordinates;
     }
     */

    /**
     * Hide or show menus on the target panel.
     *
     * @param state true to show menus; false to hide menus
     * @since 3.9.5
     */
    public void setPanelMenuVisible(boolean state) {
        this.panelMenuIsVisible = state;
        if (!GraphicsEnvironment.isHeadless() && this._targetFrame != null) {
            _targetFrame.getJMenuBar().setVisible(state);
            this.revalidate();
        }
    }

    /**
     * Is the menu on the target panel shown?
     *
     * @return true if menu is visible
     * @since 3.9.5
     */
    public boolean isPanelMenuVisible() {
        if (!GraphicsEnvironment.isHeadless() && this._targetFrame != null) {
            this.panelMenuIsVisible = _targetFrame.getJMenuBar().isVisible();
        }
        return this.panelMenuIsVisible;
    }

    protected void setScroll(int state) {
        log.debug("setScroll {}", state);
        switch (state) {
            case SCROLL_NONE:
                _panelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                _panelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                break;
            case SCROLL_BOTH:
                _panelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                _panelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                break;
            case SCROLL_HORIZONTAL:
                _panelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                _panelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                break;
            case SCROLL_VERTICAL:
                _panelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                _panelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                break;
            default:
                log.warn("Unexpected  setScroll state of {}", state);
                break;
        }
        _scrollState = state;
    }

    public void setScroll(String strState) {
        int state = SCROLL_BOTH;
        if (strState.equalsIgnoreCase("none") || strState.equalsIgnoreCase("no")) {
            state = SCROLL_NONE;
        } else if (strState.equals("horizontal")) {
            state = SCROLL_HORIZONTAL;
        } else if (strState.equals("vertical")) {
            state = SCROLL_VERTICAL;
        }
        log.debug("setScroll: strState= {}, state= {}", strState, state);
        setScroll(state);
    }

    public String getScrollable() {
        String value = "";
        switch (_scrollState) {
            case SCROLL_NONE:
                value = "none";
                break;
            case SCROLL_BOTH:
                value = "both";
                break;
            case SCROLL_HORIZONTAL:
                value = "horizontal";
                break;
            case SCROLL_VERTICAL:
                value = "vertical";
                break;
            default:
                log.warn("Unexpected _scrollState of {}", _scrollState);
                break;
        }
        return value;
    }
    /*
     * *********************** end Options setup **********************
     */
    /*
     * Handle closing (actually hiding due to HIDE_ON_CLOSE) the target window.
     * <p>
     * The target window has been requested to close, don't delete it at this
     * time. Deletion must be accomplished via the Delete this panel menu item.
     */
    protected void targetWindowClosing() {
        String name = _targetFrame.getTitle();
        if (!InstanceManager.getDefault(ShutDownManager.class).isShuttingDown()) {
            InstanceManager.getDefault(UserPreferencesManager.class).showInfoMessage(
                    Bundle.getMessage("PanelHideTitle"), Bundle.getMessage("PanelHideNotice", name),  // NOI18N
                    "jmri.jmrit.display.EditorManager", "skipHideDialog"); // NOI18N
            InstanceManager.getDefault(UserPreferencesManager.class).setPreferenceItemDetails(
                    "jmri.jmrit.display.EditorManager", "skipHideDialog", Bundle.getMessage("PanelHideSkip"));  // NOI18N
        }
    }

    protected Editor changeView(String className) {
        JFrame frame = getTargetFrame();

        try {
            Editor ed = (Editor) Class.forName(className).getDeclaredConstructor().newInstance();

            ed.setName(getName());
            ed.init(getName());

            ed._contents = new ArrayList<>(_contents);
            ed._idContents = new HashMap<>(_idContents);
            ed._classContents = new HashMap<>(_classContents);

            for (Positionable p : _contents) {
                p.setEditor(ed);
                ed.addToTarget(p);
                if (log.isDebugEnabled()) {
                    log.debug("changeView: {} addToTarget class= {}", p.getNameString(), p.getClass().getName());
                }
            }
            ed.setAllEditable(isEditable());
            //ed.setAllPositionable(allPositionable());
            //ed.setShowCoordinates(showCoordinates());
            ed.setAllShowToolTip(showToolTip());
            //ed.setAllControlling(allControlling());
            ed.setShowHidden(isVisible());
            ed.setPanelMenuVisible(frame.getJMenuBar().isVisible());
            ed.setScroll(getScrollable());
            ed.setTitle();
            ed.setBackgroundColor(getBackgroundColor());
            ed.getTargetFrame().setLocation(frame.getLocation());
            ed.getTargetFrame().setSize(frame.getSize());
            ed.setSize(getSize());
//            ed.pack();
            ed.setVisible(true);
            dispose();
            return ed;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException cnfe) {
            log.error("changeView exception {}", cnfe.toString());
        }
        return null;
    }

    /*
     * *********************** Popup Item Methods **********************
     *
     * These methods are to be called from the editor view's showPopUp method
     */
    /**
     * Add a checkbox to lock the position of the Positionable item.
     *
     * @param p     the item
     * @param popup the menu to add the lock menu item to
     */
    public void setPositionableMenu(Positionable p, JPopupMenu popup) {
        JCheckBoxMenuItem lockItem = new JCheckBoxMenuItem(Bundle.getMessage("LockPosition"));
        lockItem.setSelected(!p.isPositionable());
        lockItem.addActionListener(new ActionListener() {
            private Positionable comp;
            private JCheckBoxMenuItem checkBox;

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.setPositionable(!checkBox.isSelected());
                setSelectionsPositionable(!checkBox.isSelected(), comp);
            }

            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb;
                return this;
            }
        }.init(p, lockItem));
        popup.add(lockItem);
    }

    /**
     * Display the {@literal X & Y} coordinates of the Positionable item and
     * provide a dialog menu item to edit them.
     *
     * @param p     The item to add the menu item to
     * @param popup The menu item to add the action to
     * @return always returns true
     */
    public boolean setShowCoordinatesMenu(Positionable p, JPopupMenu popup) {

        JMenuItem edit;
        if ((p instanceof MemoryOrGVIcon) && (p.getPopupUtility().getFixedWidth() == 0)) {
            MemoryOrGVIcon pm = (MemoryOrGVIcon) p;

            edit = new JMenuItem(Bundle.getMessage(
                "EditLocationXY", pm.getOriginalX(), pm.getOriginalY()));

            edit.addActionListener(MemoryIconCoordinateEdit.getCoordinateEditAction(pm));
        } else {
            edit = new JMenuItem(Bundle.getMessage(
                "EditLocationXY", p.getX(), p.getY()));
            edit.addActionListener(CoordinateEdit.getCoordinateEditAction(p));
        }
        popup.add(edit);
        return true;
    }

    /**
     * Offer actions to align the selected Positionable items either
     * Horizontally (at average y coordinates) or Vertically (at average x
     * coordinates).
     *
     * @param p     The positionable item
     * @param popup The menu to add entries to
     * @return true if entries added to menu
     */
    public boolean setShowAlignmentMenu(Positionable p, JPopupMenu popup) {
        if (showAlignPopup(p)) {
            JMenu edit = new JMenu(Bundle.getMessage("EditAlignment"));
            edit.add(new AbstractAction(Bundle.getMessage("AlignX")) {
                private int _x;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(_x, comp.getY());
                    }
                }

                AbstractAction init(int x) {
                    _x = x;
                    return this;
                }
            }.init(p.getX()));
            edit.add(new AbstractAction(Bundle.getMessage("AlignMiddleX")) {
                private int _x;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(_x - comp.getWidth() / 2, comp.getY());
                    }
                }

                AbstractAction init(int x) {
                    _x = x;
                    return this;
                }
            }.init(p.getX() + p.getWidth() / 2));
            edit.add(new AbstractAction(Bundle.getMessage("AlignOtherX")) {
                private int _x;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(_x - comp.getWidth(), comp.getY());
                    }
                }

                AbstractAction init(int x) {
                    _x = x;
                    return this;
                }
            }.init(p.getX() + p.getWidth()));
            edit.add(new AbstractAction(Bundle.getMessage("AlignY")) {
                private int _y;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(comp.getX(), _y);
                    }
                }

                AbstractAction init(int y) {
                    _y = y;
                    return this;
                }
            }.init(p.getY()));
            edit.add(new AbstractAction(Bundle.getMessage("AlignMiddleY")) {
                private int _y;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(comp.getX(), _y - comp.getHeight() / 2);
                    }
                }

                AbstractAction init(int y) {
                    _y = y;
                    return this;
                }
            }.init(p.getY() + p.getHeight() / 2));
            edit.add(new AbstractAction(Bundle.getMessage("AlignOtherY")) {
                private int _y;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    for (Positionable comp : _selectionGroup) {
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(comp.getX(), _y - comp.getHeight());
                    }
                }

                AbstractAction init(int y) {
                    _y = y;
                    return this;
                }
            }.init(p.getY() + p.getHeight()));
            edit.add(new AbstractAction(Bundle.getMessage("AlignXFirst")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    int x = _selectionGroup.get(0).getX();
                    for (int i = 1; i < _selectionGroup.size(); i++) {
                        Positionable comp = _selectionGroup.get(i);
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(x, comp.getY());
                    }
                }
            });
            edit.add(new AbstractAction(Bundle.getMessage("AlignYFirst")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_selectionGroup == null) {
                        return;
                    }
                    int y = _selectionGroup.get(0).getX();
                    for (int i = 1; i < _selectionGroup.size(); i++) {
                        Positionable comp = _selectionGroup.get(i);
                        if (!getFlag(OPTION_POSITION, comp.isPositionable())) {
                            continue;
                        }
                        comp.setLocation(comp.getX(), y);
                    }
                }
            });
            popup.add(edit);
            return true;
        }
        return false;
    }

    /**
     * Display 'z' level of the Positionable item and provide a dialog
     * menu item to edit it.
     *
     * @param p     The item
     * @param popup the menu to add entries to
     */
    public void setDisplayLevelMenu(Positionable p, JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("EditLevel_", p.getDisplayLevel()));
        edit.addActionListener(CoordinateEdit.getLevelEditAction(p));
        popup.add(edit);
    }

    /**
     * Add a menu entry to set visibility of the Positionable item
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setHiddenMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }
        JCheckBoxMenuItem hideItem = new JCheckBoxMenuItem(Bundle.getMessage("SetHidden"));
        hideItem.setSelected(p.isHidden());
        hideItem.addActionListener(new ActionListener() {
            private Positionable comp;
            private JCheckBoxMenuItem checkBox;

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.setHidden(checkBox.isSelected());
                setSelectionsHidden(checkBox.isSelected(), comp);
            }

            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb;
                return this;
            }
        }.init(p, hideItem));
        popup.add(hideItem);
    }

    /**
     * Add a menu entry to set visibility of the Positionable item based on the presence of contents.
     * If the value is null or empty, the icon is not visible.
     * This is applicable to memory,  block content and LogixNG global variable labels.
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setEmptyHiddenMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }
        if (p instanceof BlockContentsIcon || p instanceof MemoryIcon || p instanceof GlobalVariableIcon) {
            JCheckBoxMenuItem hideEmptyItem = new JCheckBoxMenuItem(Bundle.getMessage("SetEmptyHidden"));
            hideEmptyItem.setSelected(p.isEmptyHidden());
            hideEmptyItem.addActionListener(new ActionListener() {
                private Positionable comp;
                private JCheckBoxMenuItem checkBox;

                @Override
                public void actionPerformed(ActionEvent e) {
                    comp.setEmptyHidden(checkBox.isSelected());
                }

                ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                    comp = pos;
                    checkBox = cb;
                    return this;
                }
            }.init(p, hideEmptyItem));
            popup.add(hideEmptyItem);
        }
    }

    /**
     * Add a menu entry to disable double click value edits.  This applies when not in panel edit mode.
     * This is applicable to memory,  block content and LogixNG global variable labels.
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setValueEditDisabledMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }
        if (p instanceof BlockContentsIcon || p instanceof MemoryIcon || p instanceof GlobalVariableIcon) {
            JCheckBoxMenuItem valueEditDisableItem = new JCheckBoxMenuItem(Bundle.getMessage("SetValueEditDisabled"));
            valueEditDisableItem.setSelected(p.isValueEditDisabled());
            valueEditDisableItem.addActionListener(new ActionListener() {
                private Positionable comp;
                private JCheckBoxMenuItem checkBox;

                @Override
                public void actionPerformed(ActionEvent e) {
                    comp.setValueEditDisabled(checkBox.isSelected());
                }

                ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                    comp = pos;
                    checkBox = cb;
                    return this;
                }
            }.init(p, valueEditDisableItem));
            popup.add(valueEditDisableItem);
        }
    }

    /**
     * Add a menu entry to edit Id of the Positionable item
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setEditIdMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }

        popup.add(CoordinateEdit.getIdEditAction(p, "EditId", this));
    }

    /**
     * Add a menu entry to edit Classes of the Positionable item
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setEditClassesMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }

        popup.add(CoordinateEdit.getClassesEditAction(p, "EditClasses", this));
    }

    /**
     * Check if edit of a conditional is in progress.
     *
     * @return true if this is the case, after showing dialog to user
     */
    private boolean checkEditConditionalNG() {
        if (_inEditInlineLogixNGMode) {
            // Already editing a LogixNG, ask for completion of that edit
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("Error_InlineLogixNGInEditMode"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JmriJOptionPane.ERROR_MESSAGE);
            _inlineLogixNGEdit.bringToFront();
            return true;
        }
        return false;
    }

    /**
     * Add a menu entry to edit Id of the Positionable item
     *
     * @param p     the item
     * @param popup the menu to add the entry to
     */
    public void setLogixNGPositionableMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }

        JMenu logixNG_Menu = new JMenu("LogixNG");
        popup.add(logixNG_Menu);

        logixNG_Menu.add(new AbstractAction(Bundle.getMessage("LogixNG_Inline")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkEditConditionalNG()) {
                    return;
                }

                if (p.getLogixNG() == null) {
                    LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                            .createLogixNG(null, true);
                    logixNG.setInlineLogixNG(p);
                    logixNG.activate();
                    logixNG.setEnabled(true);
                    logixNG.clearStartup();
                    p.setLogixNG(logixNG);
                }
                LogixNGEditor logixNGEditor = new LogixNGEditor(null, p.getLogixNG().getSystemName());
                logixNGEditor.addEditorEventListener((HashMap<String, String> data) -> {
                    _inEditInlineLogixNGMode = false;
                    data.forEach((key, value) -> {
                        if (key.equals("Finish")) {                  // NOI18N
                            _inlineLogixNGEdit = null;
                            _inEditInlineLogixNGMode = false;
                        } else if (key.equals("Delete")) {           // NOI18N
                            _inEditInlineLogixNGMode = false;
                            deleteLogixNG(p.getLogixNG());
                        } else if (key.equals("chgUname")) {         // NOI18N
                            p.getLogixNG().setUserName(value);
                        }
                    });
                    if (p.getLogixNG() != null && p.getLogixNG().getNumConditionalNGs() == 0) {
                        deleteLogixNG_Internal(p.getLogixNG());
                    }
                });
                logixNGEditor.bringToFront();
                _inEditInlineLogixNGMode = true;
                _inlineLogixNGEdit = logixNGEditor;
            }
        });
    }

    private void deleteLogixNG(LogixNG logixNG) {
        DeleteBean<LogixNG> deleteBean = new DeleteBean<>(
                InstanceManager.getDefault(LogixNG_Manager.class));

        boolean hasChildren = logixNG.getNumConditionalNGs() > 0;

        deleteBean.delete(logixNG, hasChildren, t -> deleteLogixNG_Internal(t),
                (t,list) -> logixNG.getListenerRefsIncludingChildren(list),
                jmri.jmrit.logixng.LogixNG_UserPreferences.class.getName());
    }

    private void deleteLogixNG_Internal(LogixNG logixNG) {
        logixNG.setEnabled(false);
        try {
            InstanceManager.getDefault(LogixNG_Manager.class).deleteBean(logixNG, "DoDelete");
            logixNG.getInlineLogixNG().setLogixNG(null);
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error("{} : Could not Delete.", e.getMessage());
        }
    }

    /**
     * Check if it's possible to change the id of the Positionable to the
     * desired string.
     * @param p the Positionable
     * @param newId the desired new id
     * @throws jmri.jmrit.display.Positionable.DuplicateIdException if another
     *         Positionable in the editor already has this id
     */
    public void positionalIdChange(Positionable p, String newId)
            throws Positionable.DuplicateIdException {

        if (Objects.equals(newId, p.getId())) {
            return;
        }

        if ((newId != null) && (_idContents.containsKey(newId))) {
            throw new Positionable.DuplicateIdException();
        }

        if (p.getId() != null) {
            _idContents.remove(p.getId());
        }
        if (newId != null) {
            _idContents.put(newId, p);
        }
    }

    /**
     * Add a class name to the Positionable
     * @param p the Positionable
     * @param className the class name
     * @throws IllegalArgumentException if the name contains a comma
     */
    public void positionalAddClass(Positionable p, String className) {

        if (className == null) {
            throw new IllegalArgumentException("Class name must not be null");
        }
        if (className.isBlank()) {
            throw new IllegalArgumentException("Class name must not be blank");
        }
        if (className.contains(",")) {
            throw new IllegalArgumentException("Class name must not contain a comma");
        }

        if (p.getClasses().contains(className)) {
            return;
        }

        _classContents.computeIfAbsent(className, o -> new HashSet<>()).add(p);
    }

    /**
     * Removes a class name from the Positionable
     * @param p the Positionable
     * @param className the class name
     */
    public void positionalRemoveClass(Positionable p, String className) {

        if (p.getClasses().contains(className)) {
            return;
        }
        _classContents.get(className).remove(p);
    }

    /**
     * Add a checkbox to display a tooltip for the Positionable item and if
     * showable, provide a dialog menu to edit it.
     *
     * @param p     the item to set the menu for
     * @param popup the menu to add for p
     */
    public void setShowToolTipMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }

        JMenu edit = new JMenu(Bundle.getMessage("EditTooltip"));

        JCheckBoxMenuItem showToolTipItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowTooltip"));
        showToolTipItem.setSelected(p.showToolTip());
        showToolTipItem.addActionListener(new ActionListener() {
            private Positionable comp;
            private JCheckBoxMenuItem checkBox;

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.setShowToolTip(checkBox.isSelected());
            }

            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb;
                return this;
            }
        }.init(p, showToolTipItem));
        edit.add(showToolTipItem);

        edit.add(CoordinateEdit.getToolTipEditAction(p));

        JCheckBoxMenuItem prependToolTipWithDisplayNameItem =
            new JCheckBoxMenuItem(Bundle.getMessage("PrependTooltipWithDisplayName"));
        prependToolTipWithDisplayNameItem.setSelected(p.getToolTip().getPrependToolTipWithDisplayName());
        prependToolTipWithDisplayNameItem.addActionListener(new ActionListener() {
            private Positionable comp;
            private JCheckBoxMenuItem checkBox;

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.getToolTip().setPrependToolTipWithDisplayName(checkBox.isSelected());
            }

            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb;
                return this;
            }
        }.init(p, prependToolTipWithDisplayNameItem));
        edit.add(prependToolTipWithDisplayNameItem);

        popup.add(edit);
    }

    /**
     * Add an action to remove the Positionable item.
     *
     * @param p     the item to set the menu for
     * @param popup the menu to add for p
     */
    public void setRemoveMenu(Positionable p, JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("Remove")) {
            private Positionable comp;

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.remove();
                removeSelections(comp);
            }

            AbstractAction init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
    }

    /*
     * *********************** End Popup Methods **********************
     */
 /*
     * ****************** Marker Menu ***************************
     */
    protected void locoMarkerFromRoster() {
        final JmriJFrame locoRosterFrame = new JmriJFrame();
        locoRosterFrame.getContentPane().setLayout(new FlowLayout());
        locoRosterFrame.setTitle(Bundle.getMessage("LocoFromRoster"));
        JLabel mtext = new JLabel();
        mtext.setText(Bundle.getMessage("SelectLoco") + ":");
        locoRosterFrame.getContentPane().add(mtext);
        final RosterEntrySelectorPanel rosterBox = new RosterEntrySelectorPanel();
        rosterBox.addPropertyChangeListener("selectedRosterEntries", pce -> {
            if (rosterBox.getSelectedRosterEntries().length != 0) {
                selectLoco(rosterBox.getSelectedRosterEntries()[0]);
            }
        });
        locoRosterFrame.getContentPane().add(rosterBox);
        locoRosterFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                locoRosterFrame.dispose();
            }
        });
        locoRosterFrame.pack();
        locoRosterFrame.setVisible(true);
    }

    protected LocoIcon selectLoco(String rosterEntryTitle) {
        if ("".equals(rosterEntryTitle)) {
            return null;
        }
        return selectLoco(Roster.getDefault().entryFromTitle(rosterEntryTitle));
    }

    protected LocoIcon selectLoco(RosterEntry entry) {
        LocoIcon l = null;
        if (entry == null) {
            return null;
        }
        // try getting road number, else use DCC address
        String rn = entry.getRoadNumber();
        if ((rn == null) || rn.equals("")) {
            rn = entry.getDccAddress();
        }
        if (rn != null) {
            l = addLocoIcon(rn);
            l.setRosterEntry(entry);
        }
        return l;
    }

    protected void locoMarkerFromInput() {
        final JmriJFrame locoFrame = new JmriJFrame();
        locoFrame.getContentPane().setLayout(new FlowLayout());
        locoFrame.setTitle(Bundle.getMessage("EnterLocoMarker"));

        JLabel textId = new JLabel();
        textId.setText(Bundle.getMessage("LocoID") + ":");
        locoFrame.getContentPane().add(textId);

        final JTextField locoId = new JTextField(7);
        locoFrame.getContentPane().add(locoId);
        locoId.setText("");
        locoId.setToolTipText(Bundle.getMessage("EnterLocoID"));
        JButton okay = new JButton();
        okay.setText(Bundle.getMessage("ButtonOK"));
        okay.addActionListener(e -> {
            String nameID = locoId.getText();
            if ((nameID != null) && !(nameID.trim().equals(""))) {
                addLocoIcon(nameID.trim());
            } else {
                JmriJOptionPane.showMessageDialog(locoFrame, Bundle.getMessage("ErrorEnterLocoID"),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        });
        locoFrame.getContentPane().add(okay);
        locoFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                locoFrame.dispose();
            }
        });
        locoFrame.pack();
        if (_targetFrame != null) {
            locoFrame.setLocation(_targetFrame.getLocation());
        }
        locoFrame.setVisible(true);
    }

    /**
     * Remove marker icons from panel
     */
    protected void removeMarkers() {
        log.debug("Remove markers");
        for (int i = _contents.size() - 1; i >= 0; i--) {
            Positionable il = _contents.get(i);
            if (il instanceof LocoIcon) {
                il.remove();
                if (il.getId() != null) {
                    _idContents.remove(il.getId());
                }
                for (String className : il.getClasses()) {
                    _classContents.get(className).remove(il);
                }
            }
        }
    }

    /*
     * *********************** End Marker Menu Methods **********************
     */
 /*
     * ************ Adding content to the panel **********************
     */
    public PositionableLabel setUpBackground(String name) {
        NamedIcon icon = NamedIcon.getIconByName(name);
        PositionableLabel l = new PositionableLabel(icon, this);
        l.setPopupUtility(null);        // no text
        l.setPositionable(false);
        l.setShowToolTip(false);
        l.setSize(icon.getIconWidth(), icon.getIconHeight());
        l.setDisplayLevel(BKG);
        l.setLocation(getNextBackgroundLeft(), 0);
        try {
            putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return l;
    }

    protected PositionableLabel addLabel(String text) {
        PositionableLabel l = new PositionableLabel(text, this);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        setNextLocation(l);
        try {
            putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return l;
    }

    /**
     * Determine right side x of furthest right background
     */
    private int getNextBackgroundLeft() {
        int left = 0;
        // place to right of background images, if any
        for (Positionable p : _contents) {
            if (p instanceof PositionableLabel) {
                PositionableLabel l = (PositionableLabel) p;
                if (l.isBackground()) {
                    int test = l.getX() + l.maxWidth();
                    if (test > left) {
                        left = test;
                    }
                }
            }
        }
        return left;
    }

    /* Positionable has set a new level.  Editor must change it in the target panel.
     */
    public void displayLevelChange(Positionable l) {
        removeFromTarget(l);
        addToTarget(l);
    }

    public TrainIcon addTrainIcon(String name) {
        TrainIcon l = new TrainIcon(this);
        putLocoIcon(l, name);
        return l;
    }

    public LocoIcon addLocoIcon(String name) {
        LocoIcon l = new LocoIcon(this);
        putLocoIcon(l, name);
        return l;
    }

    public void putLocoIcon(LocoIcon l, String name) {
        l.setText(name);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setEditable(isEditable());    // match popup mode to editor mode
        try {
            putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
    }

    public void putItem(Positionable l) throws Positionable.DuplicateIdException {
        l.invalidate();
        l.setPositionable(true);
        l.setVisible(true);
        if (l.getToolTip() == null) {
            l.setToolTip(new ToolTip(_defaultToolTip, l));
        }
        addToTarget(l);
        if (!_contents.add(l)) {
            log.error("Unable to add {} to _contents", l.getNameString());
        }
        if (l.getId() != null) {
            if (_idContents.containsKey(l.getId())) {
                throw new Positionable.DuplicateIdException();
            }
            _idContents.put(l.getId(), l);
        }
        for (String className : l.getClasses()) {
            _classContents.get(className).add(l);
        }
        if (log.isDebugEnabled()) {
            log.debug("putItem {} to _contents. level= {}", l.getNameString(), l.getDisplayLevel());
        }
    }

    protected void addToTarget(Positionable l) {
        JComponent c = (JComponent) l;
        c.invalidate();
        _targetPanel.remove(c);
        _targetPanel.add(c, Integer.valueOf(l.getDisplayLevel()));
        _targetPanel.moveToFront(c);
        c.repaint();
        _targetPanel.revalidate();
    }

    /*
     * ************ Icon editors for adding content ***********
     */
    static final String[] ICON_EDITORS = {"Sensor", "RightTurnout", "LeftTurnout",
        "SlipTOEditor", "SignalHead", "SignalMast", "Memory", "Light",
        "Reporter", "Background", "MultiSensor", "Icon", "Text", "Block Contents"};

    /**
     * Create editor for a given item type.
     * Paths to default icons are fixed in code. Compare to respective icon package,
     * eg. {@link #addSensorEditor()} and {@link SensorIcon}
     *
     * @param name Icon editor's name
     * @return a window
     */
    public JFrameItem getIconFrame(String name) {
        JFrameItem frame = _iconEditorFrame.get(name);
        if (frame == null) {
            if ("Sensor".equals(name)) {
                addSensorEditor();
            } else if ("RightTurnout".equals(name)) {
                addRightTOEditor();
            } else if ("LeftTurnout".equals(name)) {
                addLeftTOEditor();
            } else if ("SlipTOEditor".equals(name)) {
                addSlipTOEditor();
            } else if ("SignalHead".equals(name)) {
                addSignalHeadEditor();
            } else if ("SignalMast".equals(name)) {
                addSignalMastEditor();
            } else if ("Memory".equals(name)) {
                addMemoryEditor();
            } else if ("GlobalVariable".equals(name)) {
                addGlobalVariableEditor();
            } else if ("Reporter".equals(name)) {
                addReporterEditor();
            } else if ("Light".equals(name)) {
                addLightEditor();
            } else if ("Background".equals(name)) {
                addBackgroundEditor();
            } else if ("MultiSensor".equals(name)) {
                addMultiSensorEditor();
            } else if ("Icon".equals(name)) {
                addIconEditor();
            } else if ("Text".equals(name)) {
                addTextEditor();
            } else if ("BlockLabel".equals(name)) {
                addBlockContentsEditor();
            } else if ("Audio".equals(name)) {
                addAudioEditor();
            } else if ("LogixNG".equals(name)) {
                addLogixNGEditor();
            } else {
                // log.error("No such Icon Editor \"{}\"", name);
                return null;
            }
            // frame added in the above switch
            frame = _iconEditorFrame.get(name);

            if (frame == null) { // addTextEditor does not create a usable frame
                return null;
            }
            //frame.setLocationRelativeTo(this);
            frame.setLocation(frameLocationX, frameLocationY);
            frameLocationX += DELTA;
            frameLocationY += DELTA;
        }
        frame.setVisible(true);
        return frame;
    }
    public int frameLocationX = 0;
    public int frameLocationY = 0;
    static final int DELTA = 20;

    public IconAdder getIconEditor(String name) {
        return _iconEditorFrame.get(name).getEditor();
    }

    /**
     * Add a label to the target.
     */
    protected void addTextEditor() {
        String newLabel = JmriJOptionPane.showInputDialog(this, Bundle.getMessage("PromptNewLabel"),"");
        if (newLabel == null) {
            return;  // canceled
        }
        PositionableLabel l = addLabel(newLabel);
        // always allow new items to be moved
        l.setPositionable(true);
    }

    protected void addRightTOEditor() {
        IconAdder editor = new IconAdder("RightTurnout");
        editor.setIcon(3, "TurnoutStateClosed",
                "resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
        editor.setIcon(2, "TurnoutStateThrown",
                "resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");

        JFrameItem frame = makeAddIconFrame("RightTurnout", true, true, editor);
        _iconEditorFrame.put("RightTurnout", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = a -> addTurnoutR();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLeftTOEditor() {
        IconAdder editor = new IconAdder("LeftTurnout");
        editor.setIcon(3, "TurnoutStateClosed",
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
        editor.setIcon(2, "TurnoutStateThrown",
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

        JFrameItem frame = makeAddIconFrame("LeftTurnout", true, true, editor);
        _iconEditorFrame.put("LeftTurnout", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = a -> addTurnoutL();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addSlipTOEditor() {
        SlipIconAdder editor = new SlipIconAdder("SlipTOEditor");
        editor.setIcon(3, "LowerWestToUpperEast",
                "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif");
        editor.setIcon(2, "UpperWestToLowerEast",
                "resources/icons/smallschematics/tracksegments/os-slip-upper-west-lower-east.gif");
        editor.setIcon(4, "LowerWestToLowerEast",
                "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif");
        editor.setIcon(5, "UpperWestToUpperEast",
                "resources/icons/smallschematics/tracksegments/os-slip-upper-west-upper-east.gif");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/smallschematics/tracksegments/os-slip-error-full.gif");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/smallschematics/tracksegments/os-slip-unknown-full.gif");
        editor.setTurnoutType(SlipTurnoutIcon.DOUBLESLIP);
        JFrameItem frame = makeAddIconFrame("SlipTOEditor", true, true, editor);
        _iconEditorFrame.put("SlipTOEditor", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = a -> addSlip();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.SlipTurnoutIcon", true);
    }

    protected void addSensorEditor() {
        IconAdder editor = new IconAdder("Sensor");
        editor.setIcon(3, "SensorStateActive",
                "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        editor.setIcon(2, "SensorStateInactive",
                "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");

        JFrameItem frame = makeAddIconFrame("Sensor", true, true, editor);
        _iconEditorFrame.put("Sensor", frame);
        editor.setPickList(PickListModel.sensorPickModelInstance());

        ActionListener addIconAction = a -> putSensor();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addSignalHeadEditor() {
        IconAdder editor = getSignalHeadEditor();
        JFrameItem frame = makeAddIconFrame("SignalHead", true, true, editor);
        _iconEditorFrame.put("SignalHead", frame);
        editor.setPickList(PickListModel.signalHeadPickModelInstance());

        ActionListener addIconAction = a -> putSignalHead();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected IconAdder getSignalHeadEditor() {
        // note that all these icons will be refreshed when user clicks a specific signal head in the table
        IconAdder editor = new IconAdder("SignalHead");
        editor.setIcon(0, "SignalHeadStateRed",
                "resources/icons/smallschematics/searchlights/left-red-marker.gif");
        editor.setIcon(1, "SignalHeadStateYellow",
                "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
        editor.setIcon(2, "SignalHeadStateGreen",
                "resources/icons/smallschematics/searchlights/left-green-marker.gif");
        editor.setIcon(3, "SignalHeadStateDark",
                "resources/icons/smallschematics/searchlights/left-dark-marker.gif");
        editor.setIcon(4, "SignalHeadStateHeld",
                "resources/icons/smallschematics/searchlights/left-held-marker.gif");
        editor.setIcon(5, "SignalHeadStateLunar",
                "resources/icons/smallschematics/searchlights/left-lunar-marker.gif");
        editor.setIcon(6, "SignalHeadStateFlashingRed",
                "resources/icons/smallschematics/searchlights/left-flashred-marker.gif");
        editor.setIcon(7, "SignalHeadStateFlashingYellow",
                "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
        editor.setIcon(8, "SignalHeadStateFlashingGreen",
                "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif");
        editor.setIcon(9, "SignalHeadStateFlashingLunar",
                "resources/icons/smallschematics/searchlights/left-flashlunar-marker.gif");
        return editor;
    }

    protected void addSignalMastEditor() {
        IconAdder editor = new IconAdder("SignalMast");

        JFrameItem frame = makeAddIconFrame("SignalMast", true, true, editor);
        _iconEditorFrame.put("SignalMast", frame);
        editor.setPickList(PickListModel.signalMastPickModelInstance());

        ActionListener addIconAction = a -> putSignalMast();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    private final SpinnerNumberModel _spinCols = new SpinnerNumberModel(3, 1, 100, 1);

    protected void addMemoryEditor() {
        IconAdder editor = new IconAdder("Memory") {
            final JButton bSpin = new JButton(Bundle.getMessage("AddSpinner"));
            final JButton bBox = new JButton(Bundle.getMessage("AddInputBox"));
            final JSpinner spinner = new JSpinner(_spinCols);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                bSpin.addActionListener(a -> addMemorySpinner());
                JPanel p1 = new JPanel();
                //p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
                bBox.addActionListener(a -> addMemoryInputBox());
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
                JPanel p2 = new JPanel();
                p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                p2.add(spinner);
                p1.add(p2);
                p1.add(bBox);
                p.add(p1);
                p1 = new JPanel();
                p1.add(bSpin);
                p.add(p1);
            }

            @Override
            public void valueChanged(ListSelectionEvent e) {
                super.valueChanged(e);
                bSpin.setEnabled(addIconIsEnabled());
                bBox.setEnabled(addIconIsEnabled());
            }
        };
        ActionListener addIconAction = a -> putMemory();
        JFrameItem frame = makeAddIconFrame("Memory", true, true, editor);
        _iconEditorFrame.put("Memory", frame);
        editor.setPickList(PickListModel.memoryPickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addGlobalVariableEditor() {
        IconAdder editor = new IconAdder("GlobalVariable") {
            final JButton bSpin = new JButton(Bundle.getMessage("AddSpinner"));
            final JButton bBox = new JButton(Bundle.getMessage("AddInputBox"));
            final JSpinner spinner = new JSpinner(_spinCols);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                bSpin.addActionListener(a -> addGlobalVariableSpinner());
                JPanel p1 = new JPanel();
                //p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
                bBox.addActionListener(a -> addGlobalVariableInputBox());
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
                JPanel p2 = new JPanel();
                p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                p2.add(spinner);
                p1.add(p2);
                p1.add(bBox);
                p.add(p1);
                p1 = new JPanel();
                p1.add(bSpin);
                p.add(p1);
            }

            @Override
            public void valueChanged(ListSelectionEvent e) {
                super.valueChanged(e);
                bSpin.setEnabled(addIconIsEnabled());
                bBox.setEnabled(addIconIsEnabled());
            }
        };
        ActionListener addIconAction = a -> putGlobalVariable();
        JFrameItem frame = makeAddIconFrame("GlobalVariable", true, true, editor);
        _iconEditorFrame.put("GlobalVariable", frame);
        editor.setPickList(PickListModel.globalVariablePickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addBlockContentsEditor() {
        IconAdder editor = new IconAdder("Block Contents");
        ActionListener addIconAction = a -> putBlockContents();
        JFrameItem frame = makeAddIconFrame("BlockLabel", true, true, editor);
        _iconEditorFrame.put("BlockLabel", frame);
        editor.setPickList(PickListModel.blockPickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addReporterEditor() {
        IconAdder editor = new IconAdder("Reporter");
        ActionListener addIconAction = a -> addReporter();
        JFrameItem frame = makeAddIconFrame("Reporter", true, true, editor);
        _iconEditorFrame.put("Reporter", frame);
        editor.setPickList(PickListModel.reporterPickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLightEditor() {
        IconAdder editor = new IconAdder("Light");
        editor.setIcon(3, "StateOff",
                "resources/icons/smallschematics/lights/cross-on.png");
        editor.setIcon(2, "StateOn",
                "resources/icons/smallschematics/lights/cross-off.png");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/smallschematics/lights/cross-inconsistent.png");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/smallschematics/lights/cross-unknown.png");

        JFrameItem frame = makeAddIconFrame("Light", true, true, editor);
        _iconEditorFrame.put("Light", frame);
        editor.setPickList(PickListModel.lightPickModelInstance());

        ActionListener addIconAction = a -> addLight();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addBackgroundEditor() {
        IconAdder editor = new IconAdder("Background");
        editor.setIcon(0, "background", "resources/PanelPro.gif");

        JFrameItem frame = makeAddIconFrame("Background", true, false, editor);
        _iconEditorFrame.put("Background", frame);

        ActionListener addIconAction = a -> putBackground();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected JFrameItem addMultiSensorEditor() {
        MultiSensorIconAdder editor = new MultiSensorIconAdder("MultiSensor");
        editor.setIcon(0, "BeanStateInconsistent",
                "resources/icons/USS/plate/levers/l-inconsistent.gif");
        editor.setIcon(1, "BeanStateUnknown",
                "resources/icons/USS/plate/levers/l-unknown.gif");
        editor.setIcon(2, "SensorStateInactive",
                "resources/icons/USS/plate/levers/l-inactive.gif");
        editor.setIcon(3, "MultiSensorPosition 0",
                "resources/icons/USS/plate/levers/l-left.gif");
        editor.setIcon(4, "MultiSensorPosition 1",
                "resources/icons/USS/plate/levers/l-vertical.gif");
        editor.setIcon(5, "MultiSensorPosition 2",
                "resources/icons/USS/plate/levers/l-right.gif");

        JFrameItem frame = makeAddIconFrame("MultiSensor", true, false, editor);
        _iconEditorFrame.put("MultiSensor", frame);
        frame.addHelpMenu("package.jmri.jmrit.display.MultiSensorIconAdder", true);

        editor.setPickList(PickListModel.sensorPickModelInstance());

        ActionListener addIconAction = a -> addMultiSensor();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        return frame;
    }

    protected void addIconEditor() {
        IconAdder editor = new IconAdder("Icon");
        editor.setIcon(0, "plainIcon", "resources/icons/smallschematics/tracksegments/block.gif");
        JFrameItem frame = makeAddIconFrame("Icon", true, false, editor);
        _iconEditorFrame.put("Icon", frame);

        ActionListener addIconAction = a -> putIcon();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addAudioEditor() {
        IconAdder editor = new IconAdder("Audio");
        editor.setIcon(0, "plainIcon", "resources/icons/audio_icon.gif");
        JFrameItem frame = makeAddIconFrame("Audio", true, false, editor);
        _iconEditorFrame.put("Audio", frame);
        editor.setPickList(PickListModel.audioPickModelInstance());

        ActionListener addIconAction = a -> putAudio();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLogixNGEditor() {
        IconAdder editor = new IconAdder("LogixNG");
        editor.setIcon(0, "plainIcon", "resources/icons/logixng/logixng_icon.gif");
        JFrameItem frame = makeAddIconFrame("LogixNG", true, false, editor);
        _iconEditorFrame.put("LogixNG", frame);

        ActionListener addIconAction = a -> putLogixNG();
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    /*
     * ************** add content items from Icon Editors *******************
     */
    /**
     * Add a sensor indicator to the target.
     *
     * @return The sensor that was added to the panel.
     */
    protected SensorIcon putSensor() {
        SensorIcon result = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);
        IconAdder editor = getIconEditor("Sensor");
        Hashtable<String, NamedIcon> map = editor.getIconMap();
        Enumeration<String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            result.setIcon(key, map.get(key));
        }
//        l.setActiveIcon(editor.getIcon("SensorStateActive"));
//        l.setInactiveIcon(editor.getIcon("SensorStateInactive"));
//        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
//        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        NamedBean b = editor.getTableSelection();
        if (b != null) {
            result.setSensor(b.getDisplayName());
        }
        result.setDisplayLevel(SENSORS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException ex) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", ex);
        }
        return result;
    }

    /**
     * Add a turnout indicator to the target
     */
    void addTurnoutR() {
        IconAdder editor = getIconEditor("RightTurnout");
        addTurnout(editor);
    }

    void addTurnoutL() {
        IconAdder editor = getIconEditor("LeftTurnout");
        addTurnout(editor);
    }

    protected TurnoutIcon addTurnout(IconAdder editor) {
        TurnoutIcon result = new TurnoutIcon(this);
        result.setTurnout(editor.getTableSelection().getDisplayName());
        Hashtable<String, NamedIcon> map = editor.getIconMap();
        Enumeration<String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            result.setIcon(key, map.get(key));
        }
        result.setDisplayLevel(TURNOUTS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException ex) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", ex);
        }
        return result;
    }

    @SuppressFBWarnings(value="BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification="iconEditor requested as exact type")
    SlipTurnoutIcon addSlip() {
        SlipTurnoutIcon result = new SlipTurnoutIcon(this);
        SlipIconAdder editor = (SlipIconAdder) getIconEditor("SlipTOEditor");
        result.setSingleSlipRoute(editor.getSingleSlipRoute());

        switch (editor.getTurnoutType()) {
            case SlipTurnoutIcon.DOUBLESLIP:
                result.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                result.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                result.setLowerWestToLowerEastIcon(editor.getIcon("LowerWestToLowerEast"));
                result.setUpperWestToUpperEastIcon(editor.getIcon("UpperWestToUpperEast"));
                break;
            case SlipTurnoutIcon.SINGLESLIP:
                result.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                result.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                result.setLowerWestToLowerEastIcon(editor.getIcon("Slip"));
                result.setSingleSlipRoute(editor.getSingleSlipRoute());
                break;
            case SlipTurnoutIcon.THREEWAY:
                result.setLowerWestToUpperEastIcon(editor.getIcon("Upper"));
                result.setUpperWestToLowerEastIcon(editor.getIcon("Middle"));
                result.setLowerWestToLowerEastIcon(editor.getIcon("Lower"));
                result.setSingleSlipRoute(editor.getSingleSlipRoute());
                break;
            case SlipTurnoutIcon.SCISSOR: //Scissor is the same as a Double for icon storing.
                result.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                result.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                result.setLowerWestToLowerEastIcon(editor.getIcon("LowerWestToLowerEast"));
                //l.setUpperWestToUpperEastIcon(editor.getIcon("UpperWestToUpperEast"));
                break;
            default:
                log.warn("Unexpected addSlip editor.getTurnoutType() of {}", editor.getTurnoutType());
                break;
        }

        if ((editor.getTurnoutType() == SlipTurnoutIcon.SCISSOR) && (!editor.getSingleSlipRoute())) {
            result.setTurnout(editor.getTurnout("lowerwest").getName(), SlipTurnoutIcon.LOWERWEST);
            result.setTurnout(editor.getTurnout("lowereast").getName(), SlipTurnoutIcon.LOWEREAST);
        }
        result.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        result.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        result.setTurnoutType(editor.getTurnoutType());
        result.setTurnout(editor.getTurnout("west").getName(), SlipTurnoutIcon.WEST);
        result.setTurnout(editor.getTurnout("east").getName(), SlipTurnoutIcon.EAST);
        result.setDisplayLevel(TURNOUTS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    /**
     * Add a signal head to the target.
     *
     * @return The signal head that was added to the target.
     */
    protected SignalHeadIcon putSignalHead() {
        SignalHeadIcon result = new SignalHeadIcon(this);
        IconAdder editor = getIconEditor("SignalHead");
        result.setSignalHead(editor.getTableSelection().getDisplayName());
        Hashtable<String, NamedIcon> map = editor.getIconMap();
        Enumeration<String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            result.setIcon(key, map.get(key));
        }
        result.setDisplayLevel(SIGNALS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException ex) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", ex);
        }
        return result;
    }

    /**
     * Add a signal mast to the target.
     *
     * @return The signal mast that was added to the target.
     */
    protected SignalMastIcon putSignalMast() {
        SignalMastIcon result = new SignalMastIcon(this);
        IconAdder editor = _iconEditorFrame.get("SignalMast").getEditor();
        result.setSignalMast(editor.getTableSelection().getDisplayName());
        result.setDisplayLevel(SIGNALS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected MemoryIcon putMemory() {
        MemoryIcon result = new MemoryIcon(new NamedIcon("resources/icons/misc/X-red.gif",
                "resources/icons/misc/X-red.gif"), this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        result.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected MemorySpinnerIcon addMemorySpinner() {
        MemorySpinnerIcon result = new MemorySpinnerIcon(this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        result.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected MemoryInputIcon addMemoryInputBox() {
        MemoryInputIcon result = new MemoryInputIcon(_spinCols.getNumber().intValue(), this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        result.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected GlobalVariableIcon putGlobalVariable() {
        GlobalVariableIcon result = new GlobalVariableIcon(new NamedIcon("resources/icons/misc/X-red.gif",
                "resources/icons/misc/X-red.gif"), this);
        IconAdder globalVariableIconEditor = getIconEditor("GlobalVariable");
        result.setGlobalVariable(globalVariableIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected GlobalVariableSpinnerIcon addGlobalVariableSpinner() {
        GlobalVariableSpinnerIcon result = new GlobalVariableSpinnerIcon(this);
        IconAdder globalVariableIconEditor = getIconEditor("GlobalVariable");
        result.setGlobalVariable(globalVariableIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected GlobalVariableInputIcon addGlobalVariableInputBox() {
        GlobalVariableInputIcon result = new GlobalVariableInputIcon(_spinCols.getNumber().intValue(), this);
        IconAdder globalVariableIconEditor = getIconEditor("GlobalVariable");
        result.setGlobalVariable(globalVariableIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected BlockContentsIcon putBlockContents() {
        BlockContentsIcon result = new BlockContentsIcon(new NamedIcon("resources/icons/misc/X-red.gif",
                "resources/icons/misc/X-red.gif"), this);
        IconAdder blockIconEditor = getIconEditor("BlockLabel");
        result.setBlock(blockIconEditor.getTableSelection().getDisplayName());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(MEMORIES);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    /**
     * Add a Light indicator to the target
     *
     * @return The light indicator that was added to the target.
     */
    protected LightIcon addLight() {
        LightIcon result = new LightIcon(this);
        IconAdder editor = getIconEditor("Light");
        result.setOffIcon(editor.getIcon("StateOff"));
        result.setOnIcon(editor.getIcon("StateOn"));
        result.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        result.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        result.setLight((Light) editor.getTableSelection());
        result.setDisplayLevel(LIGHTS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected ReporterIcon addReporter() {
        ReporterIcon result = new ReporterIcon(this);
        IconAdder reporterIconEditor = getIconEditor("Reporter");
        result.setReporter((Reporter) reporterIconEditor.getTableSelection());
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(REPORTERS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void putBackground() {
        // most likely the image is scaled.  get full size from URL
        IconAdder bkgrndEditor = getIconEditor("Background");
        String url = bkgrndEditor.getIcon("background").getURL();
        setUpBackground(url);
    }

    /**
     * Add an icon to the target.
     *
     * @return The icon that was added to the target.
     */
    protected Positionable putIcon() {
        IconAdder iconEditor = getIconEditor("Icon");
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        if (log.isDebugEnabled()) {
            log.debug("putIcon: {} url= {}", (icon == null ? "null" : "icon"), url);
        }
        PositionableLabel result = new PositionableLabel(icon, this);
//        l.setPopupUtility(null);        // no text
        result.setDisplayLevel(ICONS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        result.updateSize();
        return result;
    }

    /**
     * Add a LogixNG icon to the target.
     *
     * @return The LogixNG icon that was added to the target.
     */
    protected Positionable putAudio() {
        IconAdder iconEditor = getIconEditor("Audio");
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        if (log.isDebugEnabled()) {
            log.debug("putAudio: {} url= {}", (icon == null ? "null" : "icon"), url);
        }
        AudioIcon result = new AudioIcon(icon, this);
        NamedBean b = iconEditor.getTableSelection();
        if (b != null) {
            result.setAudio(b.getDisplayName());
        }
//        l.setPopupUtility(null);        // no text
        result.setDisplayLevel(ICONS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putAudio() with null id has thrown DuplicateIdException", e);
        }
        result.updateSize();
        return result;
    }

    /**
     * Add a LogixNG icon to the target.
     *
     * @return The LogixNG icon that was added to the target.
     */
    protected Positionable putLogixNG() {
        IconAdder iconEditor = getIconEditor("LogixNG");
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        if (log.isDebugEnabled()) {
            log.debug("putLogixNG: {} url= {}", (icon == null ? "null" : "icon"), url);
        }
        LogixNGIcon result = new LogixNGIcon(icon, this);
//        l.setPopupUtility(null);        // no text
        result.setDisplayLevel(ICONS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putLogixNG() with null id has thrown DuplicateIdException", e);
        }
        result.updateSize();
        return result;
    }

    @SuppressFBWarnings(value="BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification="iconEditor requested as exact type")
    public MultiSensorIcon addMultiSensor() {
        MultiSensorIcon result = new MultiSensorIcon(this);
        MultiSensorIconAdder editor = (MultiSensorIconAdder) getIconEditor("MultiSensor");
        result.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        result.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        result.setInactiveIcon(editor.getIcon("SensorStateInactive"));
        int numPositions = editor.getNumIcons();
        for (int i = 3; i < numPositions; i++) {
            NamedIcon icon = editor.getIcon(i);
            String sensor = editor.getSensor(i).getName();
            result.addEntry(sensor, icon);
        }
        result.setUpDown(editor.getUpDown());
        result.setDisplayLevel(SENSORS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected AnalogClock2Display addClock() {
        AnalogClock2Display result = new AnalogClock2Display(this);
        result.setOpaque(false);
        result.update();
        result.setDisplayLevel(CLOCK);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    protected RpsPositionIcon addRpsReporter() {
        RpsPositionIcon result = new RpsPositionIcon(this);
        result.setSize(result.getPreferredSize().width, result.getPreferredSize().height);
        result.setDisplayLevel(SENSORS);
        setNextLocation(result);
        try {
            putItem(result);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        return result;
    }

    /*
     * ****************** end adding content ********************
     */
 /*
     * ********************* Icon Editors utils ***************************
     */
    public static class JFrameItem extends JmriJFrame {

        private final IconAdder _editor;

        JFrameItem(String name, IconAdder editor) {
            super(name);
            _editor = editor;
            setName(name);
        }

        public IconAdder getEditor() {
            return _editor;
        }

        @Override
        public String toString() {
            return this.getName();
        }
    }

    public void setTitle() {
        String name = _targetFrame.getTitle();
        if (name == null || name.equals("")) {
            super.setTitle(Bundle.getMessage("LabelEditor"));
        } else {
            super.setTitle(name + " " + Bundle.getMessage("LabelEditor"));
        }
        for (JFrameItem frame : _iconEditorFrame.values()) {
            frame.setTitle(frame.getName() + " (" + name + ")");
        }
        setName(name);
    }

    /**
     * Create a frame showing all images in the set used for an icon.
     * Opened when editItemInPanel button is clicked in the Edit Icon Panel,
     * shown after icon's context menu Edit Icon... item is selected.
     *
     * @param name bean type name
     * @param add true when used to add a new item on panel, false when used to edit an item already on the panel
     * @param table true for bean types presented as table instead of icons
     * @param editor parent frame of the image frame
     * @return JFrame connected to the editor,  to be filled with icons
     */
    protected JFrameItem makeAddIconFrame(String name, boolean add, boolean table, IconAdder editor) {
        log.debug("makeAddIconFrame for {}, add= {}, table= {}", name, add, table);
        String txt;
        String bundleName;
        JFrameItem frame = new JFrameItem(name, editor);
        // use NamedBeanBundle property for basic beans like "Turnout" I18N
        if ("Sensor".equals(name)) {
            bundleName = "BeanNameSensor";
        } else if ("SignalHead".equals(name)) {
            bundleName = "BeanNameSignalHead";
        } else if ("SignalMast".equals(name)) {
            bundleName = "BeanNameSignalMast";
        } else if ("Memory".equals(name)) {
            bundleName = "BeanNameMemory";
        } else if ("Reporter".equals(name)) {
            bundleName = "BeanNameReporter";
        } else if ("Light".equals(name)) {
            bundleName = "BeanNameLight";
        } else if ("Turnout".equals(name)) {
            bundleName = "BeanNameTurnout"; // called by RightTurnout and LeftTurnout objects in TurnoutIcon.java edit() method
        } else if ("Block".equals(name)) {
            bundleName = "BeanNameBlock";
        } else if ("GlobalVariable".equals(name)) {
            bundleName = "BeanNameGlobalVariable";
        } else if ("Audio".equals(name)) {
            bundleName = "BeanNameAudio";
        } else {
            bundleName = name;
        }
        if (editor != null) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            if (add) {
                txt = MessageFormat.format(Bundle.getMessage("addItemToPanel"), Bundle.getMessage(bundleName));
            } else {
                txt = MessageFormat.format(Bundle.getMessage("editItemInPanel"), Bundle.getMessage(bundleName));
            }
            p.add(new JLabel(txt));
            if (table) {
                txt = MessageFormat.format(Bundle.getMessage("TableSelect"), Bundle.getMessage(bundleName),
                        (add ? Bundle.getMessage("ButtonAddIcon") : Bundle.getMessage("ButtonUpdateIcon")));
            } else {
                if ("MultiSensor".equals(name)) {
                    txt = MessageFormat.format(Bundle.getMessage("SelectMultiSensor", Bundle.getMessage("ButtonAddIcon")),
                            (add ? Bundle.getMessage("ButtonAddIcon") : Bundle.getMessage("ButtonUpdateIcon")));
                } else {
                    txt = MessageFormat.format(Bundle.getMessage("IconSelect"), Bundle.getMessage(bundleName),
                            (add ? Bundle.getMessage("ButtonAddIcon") : Bundle.getMessage("ButtonUpdateIcon")));
                }
            }
            p.add(new JLabel(txt));
            p.add(new JLabel("    ")); // add a bit of space on pane above icons
            frame.getContentPane().add(p, BorderLayout.NORTH);
            frame.getContentPane().add(editor);

            JMenuBar menuBar = new JMenuBar();
            JMenu findIcon = new JMenu(Bundle.getMessage("findIconMenu"));
            menuBar.add(findIcon);

            JMenuItem editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
            editItem.addActionListener(e -> {
                ImageIndexEditor ii = InstanceManager.getDefault(ImageIndexEditor.class);
                ii.pack();
                ii.setVisible(true);
            });
            findIcon.add(editItem);
            findIcon.addSeparator();

            JMenuItem searchItem = new JMenuItem(Bundle.getMessage("searchFSMenu"));
            searchItem.addActionListener(new ActionListener() {
                private IconAdder ea;

                @Override
                public void actionPerformed(ActionEvent e) {
                    InstanceManager.getDefault(DirectorySearcher.class).searchFS();
                    ea.addDirectoryToCatalog();
                }

                ActionListener init(IconAdder ed) {
                    ea = ed;
                    return this;
                }
            }.init(editor));

            findIcon.add(searchItem);
            frame.setJMenuBar(menuBar);
            editor.setParent(frame);
            // when this window closes, check for saving
            if (add) {
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                        if (log.isDebugEnabled()) {
                            log.debug("windowClosing: HIDE {}", toString());
                        }
                    }
                });
            }
        } else {
            log.error("No icon editor specified for {}", name); // NOI18N
        }
        if (add) {
            txt = MessageFormat.format(Bundle.getMessage("AddItem"), Bundle.getMessage(bundleName));
            _iconEditorFrame.put(name, frame);
        } else {
            txt = MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage(bundleName));
        }
        frame.setTitle(txt + " (" + getTitle() + ")");
        frame.pack();
        return frame;
    }

    /*
     * ******************* cleanup ************************
     */
    protected void removeFromTarget(Positionable l) {
        _targetPanel.remove((Component) l);
        _highlightcomponent = null;
        Point p = l.getLocation();
        int w = l.getWidth();
        int h = l.getHeight();
        _targetPanel.revalidate();
        _targetPanel.repaint(p.x, p.y, w, h);
    }

    public boolean removeFromContents(Positionable l) {
        removeFromTarget(l);
        //todo check that parent == _targetPanel
        //Container parent = this.getParent();
        // force redisplay
        if (l.getId() != null) {
            _idContents.remove(l.getId());
        }
        for (String className : l.getClasses()) {
            _classContents.get(className).remove(l);
        }
        return _contents.remove(l);
    }

    /**
     * Ask user if panel should be deleted. The caller should dispose the panel
     * to delete it.
     *
     * @return true if panel should be deleted.
     */
    public boolean deletePanel() {
        log.debug("deletePanel");
        // verify deletion
        int selectedValue = JmriJOptionPane.showOptionDialog(_targetPanel,
                Bundle.getMessage("QuestionA") + "\n" + Bundle.getMessage("QuestionA2",
                    Bundle.getMessage("FileMenuItemStore")),
                Bundle.getMessage("DeleteVerifyTitle"), JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonYesDelete"), Bundle.getMessage("ButtonCancel")},
                Bundle.getMessage("ButtonCancel"));
        // return without deleting if "Cancel" or Cancel Dialog response
        return (selectedValue == 0 ); // array position 0 = Yes, Delete.
    }

    /**
     * Dispose of the editor.
     */
    @Override
    public void dispose() {
        for (JFrameItem frame : _iconEditorFrame.values()) {
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.dispose();
        }
        // delete panel - deregister the panel for saving
        ConfigureManager cm = InstanceManager.getNullableDefault(ConfigureManager.class);
        if (cm != null) {
            cm.deregister(this);
        }
        InstanceManager.getDefault(EditorManager.class).remove(this);
        setVisible(false);
        _contents.clear();
        _idContents.clear();
        for (var list : _classContents.values()) {
            list.clear();
        }
        _classContents.clear();
        removeAll();
        super.dispose();
    }

    /*
     * **************** Mouse Methods **********************
     */
    public void showToolTip(Positionable selection, JmriMouseEvent event) {
        ToolTip tip = selection.getToolTip();
        tip.setLocation(selection.getX() + selection.getWidth() / 2, selection.getY() + selection.getHeight());
        setToolTip(tip);
    }

    protected int getItemX(Positionable p, int deltaX) {
        if ((p instanceof MemoryOrGVIcon) && (p.getPopupUtility().getFixedWidth() == 0)) {
            MemoryOrGVIcon pm = (MemoryOrGVIcon) p;
            return pm.getOriginalX() + (int) Math.round(deltaX / getPaintScale());
        } else {
            return p.getX() + (int) Math.round(deltaX / getPaintScale());
        }
    }

    protected int getItemY(Positionable p, int deltaY) {
        if ((p instanceof MemoryOrGVIcon) && (p.getPopupUtility().getFixedWidth() == 0)) {
            MemoryOrGVIcon pm = (MemoryOrGVIcon) p;
            return pm.getOriginalY() + (int) Math.round(deltaY / getPaintScale());
        } else {
            return p.getY() + (int) Math.round(deltaY / getPaintScale());
        }
    }

    /**
     * Provide a method for external code to add items to context menus.
     *
     * @param nb   The namedBean associated with the postionable item.
     * @param item The entry to add to the menu.
     * @param menu The menu to add the entry to.
     */
    public void addToPopUpMenu(NamedBean nb, JMenuItem item, int menu) {
        if (nb == null || item == null) {
            return;
        }
        for (Positionable pos : _contents) {
            if (pos.getNamedBean() == nb && pos.getPopupUtility() != null) {
                addToPopUpMenu( pos, item, menu);
                return;
            } else if (pos instanceof SlipTurnoutIcon) {
                if (pos.getPopupUtility() != null) {
                    SlipTurnoutIcon sti = (SlipTurnoutIcon) pos;
                    if ( sti.getTurnout(SlipTurnoutIcon.EAST) == nb || sti.getTurnout(SlipTurnoutIcon.WEST) == nb
                        || sti.getTurnout(SlipTurnoutIcon.LOWEREAST) == nb
                        || sti.getTurnout(SlipTurnoutIcon.LOWERWEST) == nb) {
                        addToPopUpMenu( pos, item, menu);
                        return;
                    }
                }
            } else if ( pos instanceof MultiSensorIcon && pos.getPopupUtility() != null) {
                MultiSensorIcon msi = (MultiSensorIcon) pos;
                for (int i = 0; i < msi.getNumEntries(); i++) {
                    if ( msi.getSensorName(i).equals(nb.getUserName())
                        || msi.getSensorName(i).equals(nb.getSystemName())) {
                        addToPopUpMenu( pos, item, menu);
                        return;
                    }
                }
            }
        }
    }

    private void addToPopUpMenu( Positionable pos, JMenuItem item, int menu ) {
        switch (menu) {
            case VIEWPOPUPONLY:
                pos.getPopupUtility().addViewPopUpMenu(item);
                break;
            case EDITPOPUPONLY:
                pos.getPopupUtility().addEditPopUpMenu(item);
                break;
            default:
                pos.getPopupUtility().addEditPopUpMenu(item);
                pos.getPopupUtility().addViewPopUpMenu(item);
        }
    }

    public static final int VIEWPOPUPONLY = 0x00;
    public static final int EDITPOPUPONLY = 0x01;
    public static final int BOTHPOPUPS = 0x02;

    /**
     * Relocate item.
     * <p>
     * Note that items can not be moved past the left or top edges of the panel.
     *
     * @param p      The item to move.
     * @param deltaX The horizontal displacement.
     * @param deltaY The vertical displacement.
     */
    public void moveItem(Positionable p, int deltaX, int deltaY) {
        //log.debug("moveItem at ({},{}) delta ({},{})", p.getX(), p.getY(), deltaX, deltaY);
        if (getFlag(OPTION_POSITION, p.isPositionable())) {
            int xObj = getItemX(p, deltaX);
            int yObj = getItemY(p, deltaY);
            // don't allow negative placement, icon can become unreachable
            if (xObj < 0) {
                xObj = 0;
            }
            if (yObj < 0) {
                yObj = 0;
            }
            p.setLocation(xObj, yObj);
            // and show!
            p.repaint();
        }
    }

    /**
     * Return a List of all items whose bounding rectangle contain the mouse
     * position. ordered from top level to bottom
     *
     * @param event contains the mouse position.
     * @return a list of positionable items or an empty list.
     */
    protected List<Positionable> getSelectedItems(JmriMouseEvent event) {
        Rectangle rect = new Rectangle();
        ArrayList<Positionable> selections = new ArrayList<>();
        for (Positionable p : _contents) {
            double x = event.getX();
            double y = event.getY();
            rect = p.getBounds(rect);
            if (p instanceof jmri.jmrit.display.controlPanelEditor.shape.PositionableShape
                    && p.getDegrees() != 0) {
                double rad = p.getDegrees() * Math.PI / 180.0;
                java.awt.geom.AffineTransform t = java.awt.geom.AffineTransform.getRotateInstance(-rad);
                double[] pt = new double[2];
                // bit shift to avoid SpotBugs paranoia
                pt[0] = x - rect.x - (rect.width >>> 1);
                pt[1] = y - rect.y - (rect.height >>> 1);
                t.transform(pt, 0, pt, 0, 1);
                x = pt[0] + rect.x + (rect.width >>> 1);
                y = pt[1] + rect.y + (rect.height >>> 1);
            }
            Rectangle2D.Double rect2D = new Rectangle2D.Double(rect.x * _paintScale,
                    rect.y * _paintScale,
                    rect.width * _paintScale,
                    rect.height * _paintScale);
            if (rect2D.contains(x, y) && (p.getDisplayLevel() > BKG || event.isControlDown())) {
                boolean added = false;
                int level = p.getDisplayLevel();
                for (int k = 0; k < selections.size(); k++) {
                    if (level >= selections.get(k).getDisplayLevel()) {
                        selections.add(k, p);
                        added = true;       // OK to lie in the case of background icon
                        break;
                    }
                }
                if (!added) {
                    selections.add(p);
                }
            }
        }
        //log.debug("getSelectedItems at ({},{}) {} found,", x, y, selections.size());
        return selections;
    }

    /*
     * Gather all items inside _selectRect
     * Keep old group if Control key is down
     */
    protected void makeSelectionGroup(JmriMouseEvent event) {
        if (!event.isControlDown() || _selectionGroup == null) {
            _selectionGroup = new ArrayList<>();
        }
        Rectangle test = new Rectangle();
        List<Positionable> list = getContents();
        if (event.isShiftDown()) {
            for (Positionable comp : list) {
                if (_selectRect.intersects(comp.getBounds(test))
                        && (event.isControlDown() || comp.getDisplayLevel() > BKG)) {
                    _selectionGroup.add(comp);
                    //log.debug("makeSelectionGroup: selection: {}, class= {}", comp.getNameString(), comp.getClass().getName());
                }
            }
        } else {
            for (Positionable comp : list) {
                if (_selectRect.contains(comp.getBounds(test))
                        && (event.isControlDown() || comp.getDisplayLevel() > BKG)) {
                    _selectionGroup.add(comp);
                    //log.debug("makeSelectionGroup: selection: {}, class= {}", comp.getNameString(), comp.getClass().getName());
                }
            }
        }
        log.debug("makeSelectionGroup: {} selected.", _selectionGroup.size());
        if (_selectionGroup.size() < 1) {
            _selectRect = null;
            deselectSelectionGroup();
        }
    }

    /*
     * For the param, selection, Add to or delete from _selectionGroup.
     * If not there, add.
     * If there, delete.
     * make new group if Cntl key is not held down
     */
    protected void modifySelectionGroup(Positionable selection, JmriMouseEvent event) {
        if (!event.isControlDown() || _selectionGroup == null) {
            _selectionGroup = new ArrayList<>();
        }
        boolean removed = false;
        if (event.isControlDown()) {
            if (selection.getDisplayLevel() > BKG) {
                if (_selectionGroup.contains(selection)) {
                    removed = _selectionGroup.remove(selection);
                } else {
                    _selectionGroup.add(selection);
                }
            } else if (event.isShiftDown()) {
                if (_selectionGroup.contains(selection)) {
                    removed = _selectionGroup.remove(selection);
                } else {
                    _selectionGroup.add(selection);
                }
            }
        }
        log.debug("modifySelectionGroup: size= {}, selection {}",
            _selectionGroup.size(), (removed ? "removed" : "added"));
    }

    /**
     * Set attributes of a Positionable.
     *
     * @param newUtil helper from which to get attributes
     * @param p       the item to set attributes of
     *
     */
    public void setAttributes(PositionablePopupUtil newUtil, Positionable p) {
        p.setPopupUtility(newUtil.clone(p, p.getTextComponent()));
        int mar = newUtil.getMargin();
        int bor = newUtil.getBorderSize();
        Border outlineBorder;
        if (bor == 0) {
            outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        } else {
            outlineBorder = new LineBorder(newUtil.getBorderColor(), bor);
        }
        Border borderMargin;
        if (newUtil.hasBackground()) {
            borderMargin = new LineBorder(p.getBackground(), mar);
        } else {
            borderMargin = BorderFactory.createEmptyBorder(mar, mar, mar, mar);
        }
        p.setBorder(new CompoundBorder(outlineBorder, borderMargin));

        if (p instanceof PositionableLabel) {
            PositionableLabel pos = (PositionableLabel) p;
            if (pos.isText()) {
                int deg = pos.getDegrees();
                pos.rotate(0);
                if (deg == 0) {
                    p.setOpaque(newUtil.hasBackground());
                } else {
                    pos.rotate(deg);
                }
            }
        } else if (p instanceof PositionableJPanel) {
            p.setOpaque(newUtil.hasBackground());
            p.getTextComponent().setOpaque(newUtil.hasBackground());
        }
        p.updateSize();
        p.repaint();
        if (p instanceof PositionableIcon) {
            NamedBean bean = p.getNamedBean();
            if (bean != null) {
                ((PositionableIcon) p).displayState(bean.getState());
            }
        }
    }

    protected void setSelectionsAttributes(PositionablePopupUtil util, Positionable pos) {
        if (_selectionGroup != null && _selectionGroup.contains(pos)) {
            for (Positionable p : _selectionGroup) {
                if (p instanceof PositionableLabel) {
                    setAttributes(util, p);
                }
            }
        }
    }

    protected void setSelectionsHidden(boolean enabled, Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.setHidden(enabled);
            }
        }
    }

    protected boolean setSelectionsPositionable(boolean enabled, Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.setPositionable(enabled);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void removeSelections(Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.remove();
            }
            deselectSelectionGroup();
        }
    }

    protected void setSelectionsScale(double s, Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.setScale(s);
            }
        } else {
            p.setScale(s);
        }
    }

    protected void setSelectionsRotation(int k, Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.rotate(k);
            }
        } else {
            p.rotate(k);
        }
    }

    protected void setSelectionsDisplayLevel(int k, Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable comp : _selectionGroup) {
                comp.setDisplayLevel(k);
            }
        } else {
            p.setDisplayLevel(k);
        }
    }

    protected void setSelectionsDockingLocation(Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable pos : _selectionGroup) {
                if (pos instanceof LocoIcon) {
                    ((LocoIcon) pos).setDockingLocation(pos.getX(), pos.getY());
                }
            }
        } else if (p instanceof LocoIcon) {
            ((LocoIcon) p).setDockingLocation(p.getX(), p.getY());
        }
    }

    protected void dockSelections(Positionable p) {
        if (_selectionGroup != null && _selectionGroup.contains(p)) {
            for (Positionable pos : _selectionGroup) {
                if (pos instanceof LocoIcon) {
                    ((LocoIcon) pos).dock();
                }
            }
        } else if (p instanceof LocoIcon) {
            ((LocoIcon) p).dock();
        }
    }

    protected boolean showAlignPopup(Positionable p) {
        return _selectionGroup != null && _selectionGroup.contains(p);
    }

    public Rectangle getSelectRect() {
        return _selectRect;
    }

    public void drawSelectRect(int x, int y) {
        int aX = getAnchorX();
        int aY = getAnchorY();
        int w = x - aX;
        int h = y - aY;
        if (x < aX) {
            aX = x;
            w = -w;
        }
        if (y < aY) {
            aY = y;
            h = -h;
        }
        _selectRect = new Rectangle((int) Math.round(aX / _paintScale), (int) Math.round(aY / _paintScale),
                (int) Math.round(w / _paintScale), (int) Math.round(h / _paintScale));
    }

    public final int getAnchorX() {
        return _anchorX;
    }

    public final int getAnchorY() {
        return _anchorY;
    }

    public final int getLastX() {
        return _lastX;
    }

    public final int getLastY() {
        return _lastY;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (_selectionGroup == null) {
            return;
        }
        int x = 0;
        int y = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                y = -1;
                break;
            case KeyEvent.VK_DOWN:
                y = 1;
                break;
            case KeyEvent.VK_LEFT:
                x = -1;
                break;
            case KeyEvent.VK_RIGHT:
                x = 1;
                break;
            default:
                log.warn("Unexpected e.getKeyCode() of {}", e.getKeyCode());
                break;
        }
        //A cheat if the shift key isn't pressed then we move 5 pixels at a time.
        if (!e.isShiftDown()) {
            y *= 5;
            x *= 5;
        }
        for (Positionable comp : _selectionGroup) {
            moveItem(comp, x, y);
        }
        _targetPanel.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoInUseEditorHeader", getName())); // NOI18N
            message.append("<br>");
            boolean found = false;
            int count = 0;
            for (Positionable p : _contents) {
                if (nb.equals(p.getNamedBean())) {
                    found = true;
                    count++;
                }
            }
            if (found) {
                message.append(Bundle.getMessage("VetoFoundInPanel", count));
                message.append("<br>");
                message.append(Bundle.getMessage("VetoReferencesWillBeRemoved")); // NOI18N
                message.append("<br>");
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // NOI18N
            ArrayList<Positionable> toDelete = new ArrayList<>();
            for (Positionable p : _contents) {
                if (nb.equals(p.getNamedBean())) {
                    toDelete.add(p);
                }
            }
            for (Positionable p : toDelete) {
                removeFromContents(p);
                _targetPanel.repaint();
            }
        }
    }

    /*
     * ********************* Abstract Methods ***********************
     */
    @Override
    public abstract void mousePressed(JmriMouseEvent event);

    @Override
    public abstract void mouseReleased(JmriMouseEvent event);

    @Override
    public abstract void mouseClicked(JmriMouseEvent event);

    @Override
    public abstract void mouseDragged(JmriMouseEvent event);

    @Override
    public abstract void mouseMoved(JmriMouseEvent event);

    @Override
    public abstract void mouseEntered(JmriMouseEvent event);

    @Override
    public abstract void mouseExited(JmriMouseEvent event);

    /*
     * set up target panel, frame etc.
     */
    protected abstract void init(String name);

    /*
     * Closing of Target frame window.
     */
    protected abstract void targetWindowClosingEvent(WindowEvent e);

    /**
     * Called from TargetPanel's paint method for additional drawing by editor
     * view.
     *
     * @param g the context to paint within
     */
    protected abstract void paintTargetPanel(Graphics g);

    /**
     * Set an object's location when it is created.
     *
     * @param obj the object to locate
     */
    protected abstract void setNextLocation(Positionable obj);

    /**
     * After construction, initialize all the widgets to their saved config
     * settings.
     */
    protected abstract void initView();

    /**
     * Set up item(s) to be copied by paste.
     *
     * @param p the item to copy
     */
    protected abstract void copyItem(Positionable p);

    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            getContents().forEach( pos -> {
                String data = getUsageData(pos);
                if (pos instanceof MultiSensorIcon) {
                    MultiSensorIcon multi = (MultiSensorIcon) pos;
                    multi.getSensors().forEach( sensor -> {
                        if (bean.equals(sensor)) {
                            report.add(new NamedBeanUsageReport("PositionalIcon", data));
                        }
                    });

                } else if (pos instanceof SlipTurnoutIcon) {
                    SlipTurnoutIcon slip3Scissor = (SlipTurnoutIcon) pos;
                    if (bean.equals(slip3Scissor.getTurnout(SlipTurnoutIcon.EAST))) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }
                    if (bean.equals(slip3Scissor.getTurnout(SlipTurnoutIcon.WEST))) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }
                    if ( slip3Scissor.getNamedTurnout(SlipTurnoutIcon.LOWEREAST) != null
                        && bean.equals(slip3Scissor.getTurnout(SlipTurnoutIcon.LOWEREAST))) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }
                    if ( slip3Scissor.getNamedTurnout(SlipTurnoutIcon.LOWERWEST) != null
                        && bean.equals(slip3Scissor.getTurnout(SlipTurnoutIcon.LOWERWEST))) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }

                } else if (pos instanceof LightIcon) {
                    LightIcon icon = (LightIcon) pos;
                    if (bean.equals(icon.getLight())) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }

                } else if (pos instanceof ReporterIcon) {
                    ReporterIcon icon = (ReporterIcon) pos;
                    if (bean.equals(icon.getReporter())) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }

                } else if (pos instanceof AudioIcon) {
                    AudioIcon icon = (AudioIcon) pos;
                    if (bean.equals(icon.getAudio())) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }

                } else {
                    if ( bean.equals(pos.getNamedBean())) {
                        report.add(new NamedBeanUsageReport("PositionalIcon", data));
                    }
               }
            });
        }
        return report;
    }

    String getUsageData(Positionable pos) {
        Point point = pos.getLocation();
        return String.format("%s :: x=%d, y=%d",
                pos.getClass().getSimpleName(),
                Math.round(point.getX()),
                Math.round(point.getY()));
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Editor.class);

}
