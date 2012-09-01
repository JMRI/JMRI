package jmri.jmrit.display;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.*;
import javax.swing.Timer;  // disambiguate java.util.Timer
import javax.swing.event.ListSelectionEvent;

import jmri.CatalogTree;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Reporter;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.picker.PickListModel;

import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.util.JmriJFrame;
//import jmri.configurexml.*;

/**
 * This is the Model and a Controller for panel editor Views. 
 * (Panel Editor, Layout Editor or any subsequent editors)
 * The Model is simply a list of Positionable objects added to a
 * "target panel". Control of the display attributes of the
 * Positionable objects is done here.  However, control of mouse
 * events is passed to the editor views, so control is also
 * done by the editor views.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.  This class only locates and moves
 * "target panel" items, and does not control their appearance - that
 * is left for the editor views. 
 * <P>
 * The Editor has tri-state "flags" to control the display of 
 * Positionable object attributes globally - i.e. "on" or "off" for
 * all - or as a third state, permits the display control "locally" 
 * by corresponding flags in each Positionable object 
 * <P>
 * The title of the target and the editor panel are kept
 * consistent via the {#setTitle} method.
 *
 * <p>
 * Mouse events are initially handled here, rather than in the 
 * individual displayed objects, so that selection boxes for 
 * moving multiple objects can be provided. 
 *
 * <p>
 * This class also implements an effective ToolTipManager replacement,
 * because the standard Swing one can't deal with the coordinate 
 * changes used to zoom a panel.  It works by controlling the contents 
 * of the _tooltip instance variable, and triggering repaint of the 
 * target window when the tooltip changes.  The window painting then
 * explicitly draws the tooltip for the underlying object.
 *
 * @author  Bob Jacobsen  Copyright: Copyright (c) 2002, 2003, 2007
 * @author  Dennis Miller 2004
 * @author  Howard G. Penny Copyright: Copyright (c) 2005
 * @author  Matthew Harris Copyright: Copyright (c) 2009
 * @author  Pete Cressman Copyright: Copyright (c) 2009, 2010, 2011
 * @version			$Revision 1.0 $
 * 
 */

abstract public class Editor extends JmriJFrame implements MouseListener, MouseMotionListener,
                                ActionListener, KeyListener {

    final public static int BKG       = 1;
    final public static int TEMP      = 2;
    final public static int ICONS     = 3;
    final public static int LABELS    = 4;
    final public static int MEMORIES  = 5;
    final public static int REPORTERS = 5;
    final public static int SECURITY  = 6;
    final public static int TURNOUTS  = 7;
    final public static int LIGHTS    = 8;
    final public static int SIGNALS   = 9;
    final public static int SENSORS   = 10;
    final public static int CLOCK     = 10;
    final public static int MARKERS   = 10;
    final public static int NUM_LEVELS= 10;

    final public static int SCROLL_NONE       = 0;
    final public static int SCROLL_BOTH       = 1;
    final public static int SCROLL_HORIZONTAL = 2;
    final public static int SCROLL_VERTICAL   = 3;

    static final public ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public static final String POSITIONABLE_FLAVOR = java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType +
               ";class=jmri.jmrit.display.Positionable";

    private boolean _debug = false;
    private boolean _loadFailed = false;

    boolean showCloseInfoMessage = true;	//display info message when closing panel
    
    protected ArrayList <Positionable> _contents = new ArrayList<Positionable>();
    protected JLayeredPane _targetPanel;
    private JFrame      _targetFrame;
	private JScrollPane _panelScrollPane;
	
    // Option menu items 
	protected int _scrollState = SCROLL_NONE;
    protected boolean _editable = true;
    private boolean _positionable = true;
    private boolean _controlLayout = true;
    private boolean _showHidden = true;
    private boolean _showTooltip = true;
//    private boolean _showCoordinates = true;

    final public static int OPTION_POSITION = 1;
    final public static int OPTION_CONTROLS = 2;
    final public static int OPTION_HIDDEN = 3;
    final public static int OPTION_TOOLTIP= 4;
//    final public static int OPTION_COORDS = 5;

    private boolean _globalSetsLocal = true;    // pre 2.9.6 behavior
    private boolean _useGlobalFlag = false;     // pre 2.9.6 behavior

    // mouse methods variables
    protected int _lastX;
    protected int _lastY;
    BasicStroke DASHED_LINE = new BasicStroke(1f, BasicStroke.CAP_BUTT, 
                                    BasicStroke.JOIN_BEVEL,
                                    10f, new float[] {10f, 10f}, 0f);

    protected Rectangle _selectRect = null;
    protected Rectangle _highlightcomponent = null;
    protected boolean _dragging = false;
    protected ArrayList <Positionable> _selectionGroup = null;  // items gathered inside fence

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
    HashMap <String, JFrameItem> _iconEditorFrame = new HashMap <String, JFrameItem>();

    public Editor() {
        _debug = log.isDebugEnabled();
    }

    public Editor(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        setName(name);
        _debug = log.isDebugEnabled();
        _defaultToolTip = new ToolTip(null, 0, 0);
        setVisible(false);
    }
    
    public Editor(String name) {
        this(name, true, true);
    }

    public void loadFailed() {
        _loadFailed = true;
    }
    /**
    *
    */
    NamedIcon _newIcon;
    boolean _ignore = false;
    boolean _delete;
    HashMap<String, String> _urlMap = new HashMap<String, String>(); 
    public NamedIcon loadFailed(String msg, String url) {
        if (_debug) log.debug("loadFailed _ignore= "+_ignore);
        String goodUrl = _urlMap.get(url);
        if (goodUrl!=null) {
            return NamedIcon.getIconByName(goodUrl);
        }
        if (_ignore) {
            _loadFailed = true;
            return new NamedIcon(url, url);
        }
        _newIcon = null;
        _delete = false;
        new UrlErrorDialog(msg, url);

        if (_delete) {
            if (_debug) log.debug("loadFailed _delete= "+_delete);
            return null;
        }
        if (_newIcon==null) {
            _loadFailed = true;
            _newIcon =new NamedIcon(url, url);
        }
        if (_debug) log.debug("loadFailed icon null= "+(_newIcon==null));
        return _newIcon;
    }
    class UrlErrorDialog extends JDialog {
        JTextField _urlField;
        CatalogPanel  _catalog;
        String _badUrl;
        UrlErrorDialog(String msg, String url) {
            super(_targetFrame, rb.getString("BadIcon"), true);
            _badUrl = url;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(java.text.MessageFormat.format(rb.getString("IconUrlError"), msg)));
            panel.add(new JLabel(rb.getString("UrlErrorPrompt1")));
            panel.add(javax.swing.Box.createVerticalStrut(10));
            panel.add(new JLabel(rb.getString("UrlErrorPrompt2")));
            panel.add(new JLabel(rb.getString("UrlErrorPrompt3")));
            panel.add(new JLabel(rb.getString("UrlErrorPrompt4")));
            _urlField = new JTextField(url);
            _urlField.setDragEnabled(true);
            _urlField.setTransferHandler(new jmri.util.DnDStringImportHandler());
            panel.add(_urlField);
            panel.add(makeDoneButtonPanel());
            _urlField.setToolTipText(rb.getString("TooltipFixUrl"));
            panel.setToolTipText(rb.getString("TooltipFixUrl"));
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setToolTipText(rb.getString("ToolTipDragIconToText"));
            panel.add(_catalog);
            setContentPane(panel);
            setLocation(200, 100);
            pack();
            setVisible(true);
        }
        protected JPanel makeDoneButtonPanel() {
            JPanel panel0 = new JPanel();
            panel0.setLayout(new FlowLayout());
            JButton doneButton = new JButton(rb.getString("ButtonContinue"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        _newIcon = NamedIcon.getIconByName(_urlField.getText());
                        if (_newIcon!=null) {
                            _urlMap.put(_badUrl, _urlField.getText());
                        }
                        dispose();
                    }
            });
            doneButton.setToolTipText(rb.getString("TooltipContinue"));
            panel0.add(doneButton);

            JButton deleteButton = new JButton(rb.getString("ButtonDeleteIcon"));
            deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        _delete = true;
                        dispose();
                    }
            });
            panel0.add(deleteButton);
            deleteButton.setToolTipText(rb.getString("TooltipDelete"));

            JButton cancelButton = new JButton(rb.getString("ButtonIgnore"));
            cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        _ignore = true;
                        dispose();
                    }
            });
            panel0.add(cancelButton);
            cancelButton.setToolTipText(rb.getString("TooltipIgnore"));
            return panel0;
        }
    }

    public void disposeLoadData() {
        _urlMap = null;
    }
    
    public boolean loadOK() {
        return !_loadFailed;
    }
    
    public List <Positionable> getContents() {
        return _contents;
    }

    public void setDefaultToolTip(ToolTip dtt) {
        _defaultToolTip = dtt;
    }
    
    /***************** setting the main panel and frame ****************/

    /**
    * An Editor may or may not choose to use 'this' as its frame or
    * the interior class 'TargetPane' for its targetPanel
    */
    protected void setTargetPanel(JLayeredPane targetPanel, JmriJFrame frame) {
        if (targetPanel == null){
            _targetPanel = new TargetPane();
        } else {
            _targetPanel = targetPanel;
        }
        if (frame == null) {
            _targetFrame = this;
        } else {
            _targetFrame = frame;
        }
        _targetFrame.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        _panelScrollPane = new JScrollPane(_targetPanel);
        Container contentPane = _targetFrame.getContentPane();
        contentPane.add(_panelScrollPane);
        _targetFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                targetWindowClosingEvent(e);
            }
        });
        _targetPanel.addMouseListener(this);
        _targetPanel.addMouseMotionListener(this);
        _targetPanel.setFocusable(true);
        _targetPanel.addKeyListener(this);
        //_targetFrame.pack();
    }

    protected void setTargetPanelSize(int w, int h) {
//        if (_debug) log.debug("setTargetPanelSize now w="+w+", h="+h);
        _targetPanel.setSize(w, h);
        _targetPanel.invalidate();
    }

    protected Dimension getTargetPanelSize() {
        return _targetPanel.getSize();
    }
     
    public final JComponent getTargetPanel() {
        return _targetPanel;
    }
    protected final JScrollPane getPanelScrollPane() {
        return _panelScrollPane;
    }

    public final JFrame getTargetFrame() {
        return _targetFrame;
    }
    
    public Color getBackgroundColor(){
        TargetPane tmp = (TargetPane) _targetPanel;
        return tmp.getBackgroundColor();
    }
    
    public void setBackgroundColor(Color col){
        TargetPane tmp = (TargetPane) _targetPanel;
        tmp.setBackgroundColor(col);
    }
    
    public void clearBackgroundColor(){
        TargetPane tmp = (TargetPane) _targetPanel;
        tmp.clearBackgroundColor();
    }

    /**
     * Get/Set scale for TargetPane drawing
     */
    public final double getPaintScale() {
        return _paintScale;
    }
    protected final void setPaintScale(double newScale) {
		double ratio = newScale/_paintScale;
        _paintScale = newScale;
        setScrollbarScale(ratio);
    }
    
    ToolTipTimer _tooltipTimer;
    protected void setToolTip(ToolTip tt) {
        if (tt==null) {
            _tooltip = null;
            if (_tooltipTimer != null) {
                _tooltipTimer.stop();
                _tooltipTimer = null;
            }

        } else if (_tooltip==null && _tooltipTimer==null) {
            _tooltipTimer = new ToolTipTimer(TOOLTIPSHOWDELAY, this, tt);
            _tooltipTimer.setRepeats(false);
            _tooltipTimer.start();
        }
    }

    static int TOOLTIPSHOWDELAY = 1000; // msec
    static int TOOLTIPDISMISSDELAY = 4000;  // msec

    /**
    * Wait TOOLTIPSHOWDELAY then show tooltip.  Wait TOOLTIPDISMISSDELAY and disaappear
    */
    public void actionPerformed(ActionEvent event) {
        //if (_debug) log.debug("_tooltipTimer actionPerformed: Timer on= "+(_tooltipTimer!=null));
        if (_tooltipTimer!=null) {
            _tooltip = _tooltipTimer.getTooltip();
            _tooltipTimer.stop();
        }
        if (_tooltip!=null) {
            _tooltipTimer = new ToolTipTimer(TOOLTIPDISMISSDELAY, this, null);
            _tooltipTimer.setRepeats(false);
            _tooltipTimer.start();
        } else {
            _tooltipTimer = null;
        }
        _targetPanel.repaint();
    }

    static class ToolTipTimer extends Timer {
        ToolTip tooltip;
        ToolTipTimer(int delay, ActionListener listener, ToolTip tip) {
            super(delay, listener);
            tooltip = tip;
        }
        ToolTip getTooltip() {
            return tooltip;
        }
    }

    /**
     *  Special internal class to allow drawing of layout to a JLayeredPane
     *  This is the 'target' pane where the layout is displayed
     */
    public class TargetPane extends JLayeredPane 
    {
        int h = 100;
        int w = 150;
        public TargetPane() {
            setLayout(null);
        }
        
        public void setSize(int width, int height) {
//            if (_debug) log.debug("size now w="+width+", h="+height);
            this.h = height;
            this.w = width;
            super.setSize(width, height);
        }
        public Dimension getSize() {
            return new Dimension(w,h);
        }
        public Dimension getPreferredSize() {
            return new Dimension(w,h);
        }
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
        public Component add(Component c, int i) {
            int hnew = Math.max(this.h, c.getLocation().y+c.getSize().height);
            int wnew = Math.max(this.w, c.getLocation().x+c.getSize().width);
            if (hnew>h || wnew>w) {
//                if (_debug) log.debug("size was "+w+","+h+" - i="+i);
                setSize(wnew,hnew);
            }
            return super.add(c, i);
        }
        public void add(Component c, Object o) {
            super.add(c, o);
            int hnew = Math.max(h, c.getLocation().y+c.getSize().height);
            int wnew = Math.max(w, c.getLocation().x+c.getSize().width);
            if (hnew>h || wnew>w) {
//                if (_debug) log.debug("adding of "+c.getSize()+" with Object - i="+o);
                setSize(wnew,hnew);
            }
        }

        private Color _highlightColor = new Color(204, 207, 88);
        private Color _selectGroupColor = new Color(204, 207, 88);
        private Color _selectRectColor = Color.red;
        private Stroke _selectRectStroke = DASHED_LINE;
        public void setHighlightColor(Color color) {
             _highlightColor = color;
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
            _highlightColor = new Color(204, 207, 88);
            _selectGroupColor = new Color(204, 207, 88);
            _selectRectColor = Color.red;
            _selectRectStroke = DASHED_LINE;
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.scale(_paintScale, _paintScale);
            super.paint(g);
            paintTargetPanel(g);
            java.awt.Stroke stroke = g2d.getStroke();
            Color color = g2d.getColor();
            if (_selectRect != null) {
                //Draw a rectangle on top of the image.
                g2d.setStroke(_selectRectStroke);
                g2d.setColor(_selectRectColor);
                g.drawRect(_selectRect.x, _selectRect.y, _selectRect.width, _selectRect.height);
            }
            if (_selectionGroup!=null){
                g2d.setColor(_selectGroupColor);
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                if (_selectionGroup!=null){
                    for(int i=0; i<_selectionGroup.size();i++){
                        g.drawRect(_selectionGroup.get(i).getX(), _selectionGroup.get(i).getY(), 
                                   _selectionGroup.get(i).maxWidth(), _selectionGroup.get(i).maxHeight());
                    }
                }
            }
            //Draws a border around the highlighted component
            if (_highlightcomponent!=null) {
                g2d.setColor(_highlightColor);
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g.drawRect(_highlightcomponent.x, _highlightcomponent.y, 
                           _highlightcomponent.width, _highlightcomponent.height);
            }
            g2d.setColor(color);
            g2d.setStroke(stroke);
            if (_tooltip != null) {
                _tooltip.paint(g2d, _paintScale);
            }
        }
        
        public void setBackgroundColor(Color col){
            setBackground(col);
            setOpaque(true);
        }
        
        public void clearBackgroundColor(){
            setOpaque(false);
        }
        
        public Color getBackgroundColor(){
            if (isOpaque())
                return getBackground();
            return null;
        }
    }
    
    private void setScrollbarScale(double ratio) {
		Dimension dim = _targetPanel.getSize();
        //Dimension dim = _targetPanel.getPreferredSize();
        //Dimension dim = _targetPanel.getMaximumSize();
		int tpWidth = (int)((dim.width)*ratio);
		int tpHeight = (int)((dim.height)*ratio);
        _targetPanel.setSize(tpWidth,tpHeight);
        if (_debug) log.debug("setScrollbarScale: ratio= "+ratio+", tpWidth= "+tpWidth+", tpHeight= "+tpHeight);
		// compute new scroll bar positions in order to keep image centered
        JScrollBar horScroll = _panelScrollPane.getHorizontalScrollBar();
        JScrollBar vertScroll = _panelScrollPane.getVerticalScrollBar();
		int hScroll = horScroll.getVisibleAmount()/2;
		hScroll = (int)((horScroll.getValue() + hScroll) * ratio) - hScroll;
		int vScroll = vertScroll.getVisibleAmount()/2;
		vScroll = (int)((vertScroll.getValue() + vScroll) * ratio) - vScroll;
		// set scrollbars maximum range (otherwise setValue may fail);
		horScroll.setMaximum((int)((horScroll.getMaximum())*ratio));
		vertScroll.setMaximum((int)((vertScroll.getMaximum())*ratio));
		// set scroll bar positions
		horScroll.setValue(hScroll);
		vertScroll.setValue(vScroll);
		repaint();
    }
     
    /************************ Options setup **********************/
    /**
     *  Control whether target panel items are editable.
     *  Does this by invoke the {@link Positionable#setEditable(boolean)} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items
     *  (which are the primary way that items are edited).
     * @param state true for editable.
     */
    public void setAllEditable(boolean state) {
		_editable = state;
        for (int i = 0; i<_contents.size(); i++) {
            _contents.get(i).setEditable(state);
        }
        if (!_editable) {
            _highlightcomponent = null;
            _selectionGroup = null;
        }
    }
    
	// accessor routines for persistent information
    public boolean isEditable() {
        return _editable;
    }

    /**
    * Set which flag should be used, global or local for Positioning
    * and Control of individual items.  Items call getFlag() to return
    * the appropriate flag it should use.
    */
    public void setUseGlobalFlag(boolean set) {
        _useGlobalFlag = set;      
    }
    public boolean useGlobalFlag() {
        return _useGlobalFlag;
    }

    /**
    * @param localFlag is the current setting of the item
    */
    public boolean getFlag(int whichOption, boolean localFlag) {
        //if (_debug)  log.debug("getFlag Option= "+whichOption+", _useGlobalFlag="+_useGlobalFlag+" localFlag="+localFlag);
        if (_useGlobalFlag) {
            switch (whichOption) {
                case OPTION_POSITION:
                    return _positionable;
                case OPTION_CONTROLS:
                    return _controlLayout;
                case OPTION_HIDDEN:
                    return _showHidden;
                case OPTION_TOOLTIP:
                    return _showTooltip;
//                case OPTION_COORDS:
//                    return _showCoordinates;
            }
        }
        return localFlag;
    }
    /**
    * Does global flag sets Positionable and Control for individual items
    */
    public void setGlobalSetsLocalFlag(boolean set) {
        _globalSetsLocal = set;
    }

    /**
     *  Control whether panel items are positionable.
	 *  Markers are always positionable.
     * @param state true for all items positionable.
     */
    public void setAllPositionable(boolean state) {
        _positionable = state;
        if (_globalSetsLocal) {
            for (int i = 0; i<_contents.size(); i++) {
                Positionable p = _contents.get(i);
                // don't allow backgrounds to be set positionable by global flag
                if (!state || p.getDisplayLevel()!=BKG) {
                    p.setPositionable(state);
                }
            }
        }
    }
    public boolean allPositionable() {
        return _positionable;
    }

    /**
     *  Control whether target panel items are controlling layout items.
     *  Does this by invoke the {@link Positionable#setControlling} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items.
     * @param state true for controlling.
     */
    public void setAllControlling(boolean state) {
        _controlLayout = state;
        if (_globalSetsLocal) {
            for (int i = 0; i<_contents.size(); i++) {
                _contents.get(i).setControlling(state);
            }
        }
    }
    public boolean allControlling() {
        return _controlLayout;
    }

    /**
     *  Control whether target panel hidden items are visible or not.
     *  Does this by invoke the {@link Positionable#setHidden} function of
     *  each item on the target panel.
     * @param state true for Visible.
     */
    public void setShowHidden(boolean state) {
        _showHidden = state;
        if (_showHidden) {
            for (int i = 0; i<_contents.size(); i++) {
                _contents.get(i).setVisible(true);
            }
        } else {
            for (int i = 0; i<_contents.size(); i++) {
                _contents.get(i).showHidden();
            }
        }
    }

    public boolean showHidden() {
        return _showHidden;
    }

    public void setAllShowTooltip(boolean state) {
        _showTooltip = state;
        for (int i = 0; i<_contents.size(); i++) {
            _contents.get(i).setShowTooltip(state);
        }
    }
    public boolean showTooltip() {
        return _showTooltip;
    }

  
    /**
     *  Control whether target panel items will show their
     *  coordinates in their popup memu. 
     * @param state true for show coodinates.
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
     *  Control whether target panel shows a menu
     * @param state true for controlling.
     */
    public void setPanelMenu(boolean state) {
        _targetFrame.getJMenuBar().setVisible(state);
        validate();
    }

    protected void setScroll(int state) {
        if (_debug) log.debug("setScroll "+state);
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
        }
        _scrollState = state;
	}

    public void setScroll(String strState) {
        int state = SCROLL_BOTH;
        if (strState.equalsIgnoreCase("none") || strState.equalsIgnoreCase("no")) state = SCROLL_NONE;
        else if (strState.equals("horizontal")) state = SCROLL_HORIZONTAL;
        else if (strState.equals("vertical")) state = SCROLL_VERTICAL;
        if (_debug) log.debug("setScroll: strState= "+strState+", state= "+state);
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
        }
        return value;
    }
    /************************* end Options setup ***********************/

    /**
     * The target window has been requested to close, don't delete it at this
	 *   time.  Deletion must be accomplished via the Delete this panel menu item.
     */
    protected void targetWindowClosing(boolean save) {
        //this.setVisible(false);   // doesn't remove the editor!
		// display info message on panel close
		if (showCloseInfoMessage) {
			String name = "Panel";
            String message = null;
            if(save) {
                message = rb.getString("Reminder1")+" "+rb.getString("Reminder2")+
								"\n"+rb.getString("Reminder3");
            } else {
                message = rb.getString("PanelCloseQuestion") +"\n" +
                                rb.getString("PanelCloseHelp");
            }
			if (_targetPanel.getTopLevelAncestor() != null)
				name = ((JFrame) _targetPanel.getTopLevelAncestor()).getTitle();
			int selectedValue = JOptionPane.showOptionDialog(_targetPanel, 
					java.text.MessageFormat.format(message, 
							new Object[] { name }), rb.getString("ReminderTitle"),
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, new Object[] { rb.getString("ButtonHide"), rb.getString("ButtonDelete"),
				            rb.getString("ButtonDontShow") }, rb.getString("ButtonHide"));
            switch (selectedValue) {
                case 0:
                    _targetFrame.setVisible(false);   // doesn't remove the editor!
                    jmri.jmrit.display.PanelMenu.instance().updateEditorPanel(this);
                    break;
                case 1:
                    if (deletePanel() ) { // disposes everything
                        dispose(true);
                    }
                    break;
                case 2:
                    showCloseInfoMessage = false;
                    _targetFrame.setVisible(false);   // doesn't remove the editor!
                    jmri.jmrit.display.PanelMenu.instance().updateEditorPanel(this);
                    break;
                default:    // dialog closed - do nothing
                    _targetFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            }
            if (_debug) 
                log.debug("targetWindowClosing: selectedValue= "+selectedValue);
		} else {
            _targetFrame.setVisible(false);   // doesn't remove the editor!
            jmri.jmrit.display.PanelMenu.instance().updateEditorPanel(this);
        }
    }

    protected Editor changeView(String className) {

        JFrame frame = getTargetFrame();

        try {
            Editor ed = (Editor)Class.forName(className).newInstance();

            ed.setName(getName());
            ed.init(getName());

            ed._contents = _contents;
            for (int i = 0; i<_contents.size(); i++) {
                Positionable p = _contents.get(i);
                p.setEditor(ed);
                ed.addToTarget(p);
                if (_debug) log.debug("changeView: "+p.getNameString()+" addToTarget class= "
                                      +p.getClass().getName());
            }
            ed.setAllEditable(isEditable());
            ed.setAllPositionable(allPositionable());
            //ed.setShowCoordinates(showCoordinates());
            ed.setAllShowTooltip(showTooltip());
            ed.setAllControlling(allControlling());
            ed.setShowHidden(isVisible());
            ed.setPanelMenu(frame.getJMenuBar().isVisible());
            ed.setScroll(getScrollable());
            ed.setTitle();
            ed.setBackgroundColor(getBackgroundColor());
            ed.getTargetFrame().setLocation(frame.getLocation());
            ed.getTargetFrame().setSize(frame.getSize());
            ed.setSize(getSize());
//            ed.pack();
            ed.setVisible(true);
            jmri.jmrit.display.PanelMenu.instance().addEditorPanel(ed);
            dispose(false);
            return ed;
        } catch (ClassNotFoundException cnfe) {
            log.error("changeView exception "+cnfe.toString());
        } catch (InstantiationException ie) {
            log.error("changeView exception "+ie.toString());
        } catch (IllegalAccessException iae) {
            log.error("changeView exception "+iae.toString());
        }
        return null;
    }

    /************************* Popup Item Methods ***********************/
    /**
    * These methods are to be called from the editor view's showPopUp method
    */
    /**
    * Add a checkbox to lock the position of the Positionable item
    */
    public void setPositionableMenu(Positionable p, JPopupMenu popup) {
        JCheckBoxMenuItem lockItem = new JCheckBoxMenuItem(rb.getString("LockPosition"));
        lockItem.setSelected(!p.isPositionable());
        lockItem.addActionListener(new ActionListener(){
            Positionable comp;
            JCheckBoxMenuItem checkBox;
            public void actionPerformed(java.awt.event.ActionEvent e) {
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
    * Display the X & Y coordinates of the Positionable item and provide a
    * dialog memu item to edit them.
    */
    public boolean setShowCoordinatesMenu(Positionable p, JPopupMenu popup) {
        //if (showCoordinates()) {
            JMenu edit = new JMenu(rb.getString("EditLocation"));
            if ((p instanceof MemoryIcon) && (p.getPopupUtility().getFixedWidth()==0)) {
                MemoryIcon pm = (MemoryIcon) p;
                edit.add("x= " + pm.getOriginalX());
                edit.add("y= " + pm.getOriginalY());
                edit.add(MemoryIconCoordinateEdit.getCoordinateEditAction(pm));
            } else {
                edit.add("x= " + p.getX());
                edit.add("y= " + p.getY());
                edit.add(CoordinateEdit.getCoordinateEditAction(p));
            }
            popup.add(edit);
           return true;
        //}
        //return false;
    }

    /**
    * Offer actions to align the selected Positionable items either
    * Horizontally (at avearage y coord) or Vertically (at avearage x coord).
    */
    public boolean setShowAlignmentMenu(Positionable p, JPopupMenu popup) {
        if (showAlignPopup(p)) {
            JMenu edit = new JMenu(rb.getString("EditAlignment"));
            edit.add(new AbstractAction(rb.getString("AlignX")) {
                public void actionPerformed(ActionEvent e) {
                    alignGroup(true,false);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignY")) {
                public void actionPerformed(ActionEvent e) {
                    alignGroup(false,false);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignXFirst")) {
                public void actionPerformed(ActionEvent e) {
                    alignGroup(true,true);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignYFirst")) {
                public void actionPerformed(ActionEvent e) {
                    alignGroup(false,true);
                }
            });
            popup.add(edit);
            return true;
        }
        return false;
    }
        
    /**
    * Display display 'z' level of the Positionable item and provide a
    * dialog memu item to edit it.
    */
    public void setDisplayLevelMenu(Positionable p, JPopupMenu popup) {
        JMenu edit = new JMenu(rb.getString("EditLevel"));
        edit.add("level= " + p.getDisplayLevel());
        edit.add(CoordinateEdit.getLevelEditAction(p));
        popup.add(edit);
    }

    /**
    * Add a checkbox to set visibility of the Positionable item
    */
    public void setHiddenMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }
        JCheckBoxMenuItem hideItem = new JCheckBoxMenuItem(rb.getString("SetHidden"));
        hideItem.setSelected(p.isHidden());
        hideItem.addActionListener(new ActionListener(){
            Positionable comp;
            JCheckBoxMenuItem checkBox;
            public void actionPerformed(java.awt.event.ActionEvent e) {
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
    * Add a checkbox to display a tooltip for the Positionable item and
    * if showable, provide a dialog menu to edit it.
    */
    public void setShowTooltipMenu(Positionable p, JPopupMenu popup) {
        if (p.getDisplayLevel() == BKG) {
            return;
        }
        JMenu edit = new JMenu(rb.getString("EditTooltip"));
        JCheckBoxMenuItem showTooltipItem = new JCheckBoxMenuItem(rb.getString("ShowTooltip"));
        showTooltipItem.setSelected(p.showTooltip());
        showTooltipItem.addActionListener(new ActionListener(){
            Positionable comp;
            JCheckBoxMenuItem checkBox;
            public void actionPerformed(java.awt.event.ActionEvent e) {
                comp.setShowTooltip(checkBox.isSelected());
            }
            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb; 
                return this;
            }
        }.init(p, showTooltipItem));
        edit.add(showTooltipItem);
        edit.add(CoordinateEdit.getTooltipEditAction(p));
        popup.add(edit);
    }

    /**
    * Add an action to remove the Positionable item.
    */
    public void setRemoveMenu(Positionable p, JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("Remove")) {
            Positionable comp;
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

    /************************* End Popup Methods ***********************/
    /******************** Marker Menu ****************************/

    protected void locoMarkerFromRoster() {
        final JmriJFrame locoRosterFrame = new JmriJFrame();
        locoRosterFrame.getContentPane().setLayout(new FlowLayout());
        locoRosterFrame.setTitle(rb.getString("LocoFromRoster"));
        javax.swing.JLabel mtext = new javax.swing.JLabel();
        mtext.setText(rb.getString("SelectLoco")+":");
        locoRosterFrame.getContentPane().add(mtext);
        final RosterEntrySelectorPanel rosterBox = new RosterEntrySelectorPanel();
        rosterBox.addPropertyChangeListener("selectedRosterEntries", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (rosterBox.getSelectedRosterEntries().length != 0) {
                    selectLoco(rosterBox.getSelectedRosterEntries()[0]);
                }
            }
        });
        locoRosterFrame.getContentPane().add(rosterBox);
        locoRosterFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    locoRosterFrame.dispose();
        }
            });
        locoRosterFrame.pack();
    	locoRosterFrame.setVisible(true);
    }

    protected LocoIcon selectLoco(String rosterEntryTitle){
		if ("".equals(rosterEntryTitle))
			return null;
		return selectLoco(Roster.instance().entryFromTitle(rosterEntryTitle));
    }

    protected LocoIcon selectLoco(RosterEntry entry) {
        LocoIcon l = null;
        if (entry==null) {
            return null;
        }
		// try getting road number, else use DCC address
		String rn = entry.getRoadNumber();
		if ((rn==null) || rn.equals("")) 
			rn = entry.getDccAddress();
		if (rn != null){
			l = addLocoIcon(rn);
			l.setRosterEntry(entry);
		}
        return l;
	}
    
    protected void locoMarkerFromInput() {
        final JmriJFrame locoFrame = new JmriJFrame();
        locoFrame.getContentPane().setLayout(new FlowLayout());
        locoFrame.setTitle(rb.getString("EnterLocoMarker"));
        javax.swing.JLabel textId = new javax.swing.JLabel();
        textId.setText(rb.getString("LocoID")+":");
        locoFrame.getContentPane().add(textId);
        final javax.swing.JTextField locoId = new javax.swing.JTextField(7);
        locoFrame.getContentPane().add(locoId);
        locoId.setText("");
        locoId.setToolTipText(rb.getString("EnterLocoID"));
        javax.swing.JButton okay = new javax.swing.JButton();
        okay.setText(rb.getString("OK"));
        okay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String nameID = locoId.getText();
                if ( (nameID!=null) && !(nameID.trim().equals("")) ) {			
                    addLocoIcon(nameID.trim());
                }
                else {
                    JOptionPane.showMessageDialog(locoFrame,rb.getString("ErrorEnterLocoID"),
                                    rb.getString("errorTitle"),JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        locoFrame.getContentPane().add(okay);
        locoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    locoFrame.dispose();
                }
            });			
        locoFrame.pack();
        if(_targetFrame != null)
        	locoFrame.setLocation(_targetFrame.getLocation());
		locoFrame.setVisible(true);
    }    

    /**
     * Remove marker icons from panel
     */
    protected void removeMarkers() {
		if (_debug) log.debug("Remove markers");
		for (int i=_contents.size()-1; i>=0; i--) {
            Positionable il = _contents.get(i);
            if (il instanceof LocoIcon) {
                ((LocoIcon)il).remove();
            }
		}
	}
    
    /************************* End Marker Menu Methods ***********************/
    
    /************** Adding content to the panel ***********************/

    public PositionableLabel setUpBackground(String name) {
        NamedIcon icon = NamedIcon.getIconByName(name);									   
        PositionableLabel l = new PositionableLabel(icon, this);
        l.setPopupUtility(null);        // no text
        l.setPositionable(false);
        l.setShowTooltip(false);
        l.setSize(icon.getIconWidth(), icon.getIconHeight());
        l.setDisplayLevel(BKG);
		l.setLocation(getNextBackgroundLeft(),0);
        putItem(l);
        return l;
    }

    
    protected PositionableLabel addLabel(String text) {
        PositionableLabel l = new PositionableLabel(text, this);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        setNextLocation(l);
        putItem(l);
        return l;
    }
	
	/**
	 * Determine right side x of furthest right background
	 */
	private int getNextBackgroundLeft() {
		int left = 0;
		// place to right of background images, if any
		for (int i=0; i<_contents.size(); i++) {
			Positionable p = _contents.get(i);
            if (p instanceof PositionableLabel) {
                PositionableLabel l = (PositionableLabel)p;
                if (l.isBackground()) {
                    int test = l.getX() + l.maxWidth();
                    if (test>left) left = test;
                }
            }
		}
		return left;
	}

    /* Positionable has set a new level.  Editor must change it in the target panel.
    */
    public void displayLevelChange(Positionable l){
    	removeFromTarget(l);
    	addToTarget(l);
    }
    
    public TrainIcon addTrainIcon (String name){
    	TrainIcon l = new TrainIcon(this);
        putLocoIcon(l, name);
    	return l;
    }
    
    public LocoIcon addLocoIcon (String name){
    	LocoIcon l = new LocoIcon(this);       
        putLocoIcon(l, name);
        return l;
    }
	
    public void putLocoIcon(LocoIcon l, String name) {
    	l.setText(name);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
    	l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setEditable(isEditable());    // match popup mode to editor mode
    	putItem(l);
    }

    public void putItem(Positionable l) {
        l.invalidate();
        l.setPositionable(true);
        l.setVisible(true);
        if (l.getTooltip()==null) {
            l.setTooltip(new ToolTip(_defaultToolTip, l));
        }
        addToTarget(l);
        if (!_contents.add(l)) {
            log.error("Unable to add "+l.getNameString()+" to _contents");
        }
        if (_debug) log.debug("putItem "+l.getNameString()+" to _contents. level= "+l.getDisplayLevel());
    }
    
    protected void addToTarget(Positionable l) {
        JComponent c = (JComponent)l;
        c.invalidate();
    	_targetPanel.remove(c);
        _targetPanel.add(c, Integer.valueOf(l.getDisplayLevel()));
        _targetPanel.moveToFront(c);
		c.repaint();
        _targetPanel.validate();
    }

    /************** Icon editors for adding content ************/

    static final public String[] ICON_EDITORS = {"Sensor", "RightTurnout", "LeftTurnout",
                        "SlipTOEditor", "SignalHead", "SignalMast", "Memory", "Light", 
                        "Reporter", "Background", "MultiSensor", "Icon", "Text"};
    /**
    * @param name Icon editor's name
    */
    public JFrameItem getIconFrame(String name) {
        JFrameItem frame = _iconEditorFrame.get(name);
        if (frame==null) {
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
            } else {
//                log.error("No such Icon Editor \""+name+"\"");
                return null;
            }
            // frame added in the above switch 
            frame = _iconEditorFrame.get(name);

            if (frame==null) { // addTextEditor does not create a usable frame
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
     * Add a label to the target
     */
    protected void addTextEditor() {
        String newLabel = JOptionPane.showInputDialog(this, rb.getString("PromptNewLabel"));
        if (newLabel==null) return;  // canceled
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

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addTurnoutR();
            }
        };
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

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addTurnoutL();
            }
        };
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

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addSlip();
            }
        };
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

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putSensor();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected IconAdder getSignalHeadEditor() {
        IconAdder editor = new IconAdder("SignalHead");
        editor.setIcon(0, rbean.getString("SignalHeadStateRed"),
            "resources/icons/smallschematics/searchlights/left-red-marker.gif");
        editor.setIcon(1, rbean.getString("SignalHeadStateYellow"), 
            "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
        editor.setIcon(2, rbean.getString("SignalHeadStateGreen"),
            "resources/icons/smallschematics/searchlights/left-green-marker.gif");
        editor.setIcon(3, rbean.getString("SignalHeadStateDark"),
            "resources/icons/smallschematics/searchlights/left-dark-marker.gif");
        editor.setIcon(4, rbean.getString("SignalHeadStateHeld"),
            "resources/icons/smallschematics/searchlights/left-held-marker.gif");
        editor.setIcon(5, rbean.getString("SignalHeadStateLunar"),
            "resources/icons/smallschematics/searchlights/left-lunar-marker.gif");
        editor.setIcon(6, rbean.getString("SignalHeadStateFlashingRed"), 
            "resources/icons/smallschematics/searchlights/left-flashred-marker.gif");
        editor.setIcon(7, rbean.getString("SignalHeadStateFlashingYellow"), 
            "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
        editor.setIcon(8, rbean.getString("SignalHeadStateFlashingGreen"),
            "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif");
        editor.setIcon(9, rbean.getString("SignalHeadStateFlashingLunar"),
            "resources/icons/smallschematics/searchlights/left-flashlunar-marker.gif");
        return editor;
    }

    protected void addSignalHeadEditor() {
        IconAdder editor = getSignalHeadEditor();
        JFrameItem frame = makeAddIconFrame("SignalHead", true, true, editor);
        _iconEditorFrame.put("SignalHead", frame);
        editor.setPickList(PickListModel.signalHeadPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putSignalHead();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }


    protected void addSignalMastEditor() {
        IconAdder editor = new IconAdder("SignalMast");

        JFrameItem frame = makeAddIconFrame("SignalMast", true, true, editor);
        _iconEditorFrame.put("SignalMast", frame);
        editor.setPickList(PickListModel.signalMastPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putSignalMast();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    SpinnerNumberModel _spinCols = new SpinnerNumberModel(3,1,100,1);

    protected void addMemoryEditor() {
        IconAdder editor = new IconAdder("Memory") {
                JButton b = new JButton(rb.getString("AddSpinner"));
                JButton bBox = new JButton(rb.getString("AddInputBox"));
                JSpinner spinner = new JSpinner(_spinCols);
                protected void addAdditionalButtons(JPanel p) {
                    b.addActionListener( new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                        addMemorySpinner();
                        }
                    });
                    JPanel p1 = new JPanel();
                    p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
                    bBox.addActionListener( new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                        addMemoryInputBox();
                        }
                    });
                    ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(2);
                    spinner.setMaximumSize(spinner.getPreferredSize());
                    p1.add(bBox);
                    JPanel p2 = new JPanel();
                    //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                    p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                    p2.add(new JLabel(rb.getString("NumColsLabel")));
                    p2.add(spinner);
                    p1.add(bBox);
                    p1.add(p2);
                    p.add(p1);
                    p.add(b);
                }

                public void valueChanged(ListSelectionEvent e) {
                    super.valueChanged(e);
                    b.setEnabled(addIconIsEnabled());
                    bBox.setEnabled(addIconIsEnabled());
                }
        };
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putMemory();
            }
        };
        JFrameItem frame = makeAddIconFrame("Memory", true, true, editor);
        _iconEditorFrame.put("Memory", frame);
        editor.setPickList(PickListModel.memoryPickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addReporterEditor() {
        IconAdder editor = new IconAdder("Reporter");
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addReporter();
            }
        };
        JFrameItem frame = makeAddIconFrame("Reporter", true, true, editor);
        _iconEditorFrame.put("Reporter", frame);
        editor.setPickList(PickListModel.reporterPickModelInstance());
        editor.makeIconPanel(true);
        editor.complete(addIconAction, false, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLightEditor() {
        IconAdder editor = new IconAdder("Light");
        editor.setIcon(3, "LightStateOff",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
        editor.setIcon(2, "LightStateOn", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

        JFrameItem frame = makeAddIconFrame("Light", true, true, editor);
        _iconEditorFrame.put("Light", frame);
        editor.setPickList(PickListModel.lightPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addLight();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addBackgroundEditor() {
        IconAdder editor = new IconAdder("Background");
        editor.setIcon(0, "background","resources/PanelPro.gif");

        JFrameItem frame = makeAddIconFrame("Background", true, false, editor);
        _iconEditorFrame.put("Background", frame);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putBackground();
            }
        };
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

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addMultiSensor();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, true, false);
        return frame;
    }

    protected void addIconEditor() {
        IconAdder editor = new IconAdder("Icon");
        editor.setIcon(0, "plainIcon","resources/icons/smallschematics/tracksegments/block.gif");
        JFrameItem frame = makeAddIconFrame("Icon", true, false, editor);
        _iconEditorFrame.put("Icon", frame);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putIcon();
            }
        };
        editor.makeIconPanel(true);
        editor.complete(addIconAction, true, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }
    
    /**************** add content items from Icon Editors ********************/
    /**
     * Add a sensor indicator to the target
     */
    protected SensorIcon putSensor() {
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);
        IconAdder editor = getIconEditor("Sensor");
        Hashtable <String, NamedIcon> map = editor.getIconMap();
        Enumeration <String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            l.setIcon(key, map.get(key));
        }
//        l.setActiveIcon(editor.getIcon("SensorStateActive"));
//        l.setInactiveIcon(editor.getIcon("SensorStateInactive"));
//        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
//        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        jmri.NamedBean b = editor.getTableSelection();
        if (b!=null) {
            l.setSensor(b.getDisplayName());
        }
        l.setDisplayLevel(SENSORS);
        setNextLocation(l);
        putItem(l);
        return l;
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
    
    protected TurnoutIcon  addTurnout(IconAdder editor){
    	TurnoutIcon l = new TurnoutIcon(this);
        l.setTurnout(editor.getTableSelection().getDisplayName());
        Hashtable <String, NamedIcon> map = editor.getIconMap();
        Enumeration <String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            l.setIcon(key, map.get(key));
        }
        l.setDisplayLevel(TURNOUTS);
        setNextLocation(l);
        putItem(l);
        return l;
    }
    
    void addSlip(){
        SlipIconAdder editor = (SlipIconAdder)getIconEditor("SlipTOEditor");
    	SlipTurnoutIcon l = new SlipTurnoutIcon(this);
        l.setSingleSlipRoute(editor.getSingleSlipRoute());
        switch(editor.getTurnoutType()){
            case SlipTurnoutIcon.DOUBLESLIP : 
                l.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                l.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                l.setLowerWestToLowerEastIcon(editor.getIcon("LowerWestToLowerEast"));
                l.setUpperWestToUpperEastIcon(editor.getIcon("UpperWestToUpperEast"));
                break;
            case SlipTurnoutIcon.SINGLESLIP:
                l.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                l.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                l.setLowerWestToLowerEastIcon(editor.getIcon("Slip"));
                l.setSingleSlipRoute(editor.getSingleSlipRoute());
                break;
            case SlipTurnoutIcon.THREEWAY:
                l.setLowerWestToUpperEastIcon(editor.getIcon("Upper"));
                l.setUpperWestToLowerEastIcon(editor.getIcon("Middle"));
                l.setLowerWestToLowerEastIcon(editor.getIcon("Lower"));
                l.setSingleSlipRoute(editor.getSingleSlipRoute());
                break;
            case SlipTurnoutIcon.SCISSOR: //Scissor is the same as a Double for icon storing.
                l.setLowerWestToUpperEastIcon(editor.getIcon("LowerWestToUpperEast"));
                l.setUpperWestToLowerEastIcon(editor.getIcon("UpperWestToLowerEast"));
                l.setLowerWestToLowerEastIcon(editor.getIcon("LowerWestToLowerEast"));
                //l.setUpperWestToUpperEastIcon(editor.getIcon("UpperWestToUpperEast"));
                break;
        }
        
        if((editor.getTurnoutType()==SlipTurnoutIcon.SCISSOR)&&(!editor.getSingleSlipRoute())){
            l.setTurnout(editor.getTurnout("lowerwest").getName(), SlipTurnoutIcon.LOWERWEST);
            l.setTurnout(editor.getTurnout("lowereast").getName(), SlipTurnoutIcon.LOWEREAST);
        }
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setTurnoutType(editor.getTurnoutType());
        l.setTurnout(editor.getTurnout("west").getName(), SlipTurnoutIcon.WEST);
        l.setTurnout(editor.getTurnout("east").getName(), SlipTurnoutIcon.EAST);
        l.setDisplayLevel(TURNOUTS);
        setNextLocation(l);
        putItem(l);
    }
    
    /**
     * Add a signal head to the target
     */
    protected SignalHeadIcon putSignalHead() {
        SignalHeadIcon l = new SignalHeadIcon(this);
        IconAdder editor = getIconEditor("SignalHead");
        l.setSignalHead(editor.getTableSelection().getDisplayName());
        Hashtable <String, NamedIcon> map = editor.getIconMap();
        Enumeration <String> e = map.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            l.setIcon(key, map.get(key));
        }
        l.setDisplayLevel(SIGNALS);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    /**
     * Add a signal mast to the target
     */
   protected SignalMastIcon putSignalMast() {
        SignalMastIcon l = new SignalMastIcon(this);
        IconAdder editor = _iconEditorFrame.get("SignalMast").getEditor();
        l.setSignalMast(editor.getTableSelection().getDisplayName());
        l.setDisplayLevel(SIGNALS);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    protected MemoryIcon putMemory() {
        MemoryIcon l = new MemoryIcon(new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif"), this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
        return l;
    }
    
    protected MemorySpinnerIcon addMemorySpinner() {
        MemorySpinnerIcon l = new MemorySpinnerIcon(this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    protected MemoryInputIcon addMemoryInputBox() {
        MemoryInputIcon l = new MemoryInputIcon(_spinCols.getNumber().intValue(), this);
        IconAdder memoryIconEditor = getIconEditor("Memory");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
        return l;
    }
    
    /**
     * Add a Light indicator to the target
     */
    protected LightIcon addLight() {
        LightIcon l = new LightIcon(this);
        IconAdder editor = getIconEditor("Light");
        l.setOffIcon(editor.getIcon("LightStateOff"));
        l.setOnIcon(editor.getIcon("LightStateOn"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setLight((Light)editor.getTableSelection());
        l.setDisplayLevel(LIGHTS);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    protected ReporterIcon addReporter() {
        ReporterIcon l = new ReporterIcon(this);
        IconAdder reporterIconEditor = getIconEditor("Reporter");
        l.setReporter((Reporter)reporterIconEditor.getTableSelection());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(REPORTERS);
        setNextLocation(l);
        putItem(l);
        return l;
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
     * Add an icon to the target
     */    
    protected Positionable putIcon() {
        IconAdder iconEditor = getIconEditor("Icon");
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        if (_debug) log.debug("putIcon: "+(icon==null?"null":"icon")+" url= "+url);
        PositionableLabel l = new PositionableLabel(icon, this);
        l.setPopupUtility(null);        // no text 
        l.setDisplayLevel(ICONS);
        setNextLocation(l);
        putItem(l);
        l.updateSize();
        return l;
    }

    public MultiSensorIcon addMultiSensor() {
        MultiSensorIcon m = new MultiSensorIcon(this);
        MultiSensorIconAdder editor = (MultiSensorIconAdder)getIconEditor("MultiSensor");
        m.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        m.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        m.setInactiveIcon(editor.getIcon("SensorStateInactive"));
        int numPositions = editor.getNumIcons();
        for (int i=3; i<numPositions; i++) {
            NamedIcon icon = editor.getIcon(i);
            String sensor = editor.getSensor(i).getName();
            m.addEntry(sensor, icon);
        }
        m.setUpDown(editor.getUpDown());
        m.setDisplayLevel(SENSORS);
        setNextLocation(m);
        putItem(m);
        return m;
    }

    protected void addClock(){
        AnalogClock2Display l = new AnalogClock2Display(this);
        l.setOpaque(false);
        l.update();
        l.setDisplayLevel(CLOCK);
        setNextLocation(l);
        putItem(l);
    }

    protected void addRpsReporter() {
        RpsPositionIcon l = new RpsPositionIcon(this);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(SENSORS);
        setNextLocation(l);
        putItem(l);
    }
    
    /******************** end adding content *********************/

    /*********************** Icon Editors utils ****************************/
    public List <IconAdder> getIconEditors() {
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        ArrayList <IconAdder> list = new ArrayList <IconAdder>();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            IconAdder ed = frame.getEditor();
            if (ed != null){
                list.add(ed);
            }
        }
        return list;
    }

    /**
    * Called by ImageIndexEditor after it has stored new image files and 
    * new default icons.
    */
    public void addTreeToEditors(CatalogTree tree) {
        List <IconAdder> list = getIconEditors();
        for (int i=0; i<list.size(); i++){
            IconAdder ed = list.get(i);
            ed.addTreeToCatalog(tree);
            ed.initDefaultIcons();
        }
    }
    
    public static class JFrameItem extends JmriJFrame {
        IconAdder _editor;
        JFrameItem (String name, IconAdder editor) {
            super(name);
            _editor = editor;
            setName(name);
        }
        public IconAdder getEditor() {
            return _editor;
        }
        public String toString() {
            return this.getName();
        }
    }

    public void setTitle() {
        String name = "";
        if (_targetPanel.getTopLevelAncestor()!=null) {
            name=((JFrame)_targetPanel.getTopLevelAncestor()).getTitle();
        }
        if (name==null || name.equals("")) super.setTitle(rb.getString("LabelEditor"));
        else super.setTitle(name+" "+rb.getString("LabelEditor"));
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            frame.setTitle(frame.getName()+" ("+name+")");
        }
        setName(name);
    }

    protected JFrameItem makeAddIconFrame(String name, boolean add, boolean table, IconAdder editor) {
        if (_debug) log.debug("makeAddIconFrame for "+name+", add= "+add+", table= "+table);
        String txt;
        JFrameItem frame = new JFrameItem(name, editor);
        if (editor != null) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            if (add) {
                txt = java.text.MessageFormat.format(rb.getString("addItenToPanel"), rb.getString(name));
            } else {
                txt = java.text.MessageFormat.format(rb.getString("editItenInPanel"), rb.getString(name));
            }
            p.add(new JLabel(txt));
            if (table) {
                txt = java.text.MessageFormat.format(rb.getString("TableSelect"), rb.getString(name), 
                                     (add ? rb.getString("ButtonAddIcon") : rb.getString("ButtonUpdateIcon")));
            } else {
                if ("MultiSensor".equals(name)) {
                    txt = java.text.MessageFormat.format(rb.getString("SelectMultiSensor"), 
                                         (add ? rb.getString("ButtonAddIcon") : rb.getString("ButtonUpdateIcon")));
                } else {
                    txt = java.text.MessageFormat.format(rb.getString("IconSelect"), rb.getString(name), 
                                         (add ? rb.getString("ButtonAddIcon") : rb.getString("ButtonUpdateIcon")));
                }
            }
            p.add(new JLabel(txt));
            frame.getContentPane().add(p,BorderLayout.NORTH);
            frame.getContentPane().add(editor);

            JMenuBar menuBar = new JMenuBar();
            JMenu findIcon = new JMenu(rb.getString("findIconMenu"));
            menuBar.add(findIcon);

            JMenuItem editItem = new JMenuItem(rb.getString("editIndexMenu"));
            editItem.addActionListener(new ActionListener() {
                    Editor editor;
                    public void actionPerformed(ActionEvent e) {
                        ImageIndexEditor ii = ImageIndexEditor.instance(editor);
                        ii.pack();
                        ii.setVisible(true);
                    }
                    ActionListener init(Editor ed) {
                        editor = ed;
                        return this;
                    }
                }.init(this));
            findIcon.add(editItem);
            findIcon.addSeparator();
            
            JMenuItem searchItem = new JMenuItem(rb.getString("searchFSMenu"));
            searchItem.addActionListener(new ActionListener() {
                    IconAdder ea;
                    public void actionPerformed(ActionEvent e) {
                        File dir = jmri.jmrit.catalog.DirectorySearcher.instance().searchFS();
                        if (dir != null) {
                            ea.addDirectoryToCatalog(dir);
                        }
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
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                        Editor editor;
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(editor);
                            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                            if (_debug) log.debug("windowClosing: HIDE "+toString());
                        }
                        java.awt.event.WindowAdapter init(Editor ed) {
                            editor = ed;
                            return this;
                        }
                }.init(this));
            }
        } else {
            log.error("No icon editor specified for "+name);
        }
        if (add) {
            txt = java.text.MessageFormat.format(rb.getString("AddItem"), rb.getString(name));
            _iconEditorFrame.put(name, frame);
        } else {
            txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString(name));
        }
        frame.setTitle(txt+" ("+getTitle()+")");
        frame.pack();
        return frame;
    }

    /********************* cleanup *************************/

    protected void removeFromTarget(Positionable l) {
        _targetPanel.remove((Component)l);
        _highlightcomponent = null;
    }

    public boolean removeFromContents(Positionable l) {
        removeFromTarget(l);
        Point p = l.getLocation();
        int w = l.getWidth();
        int h = l.getHeight();
        //todo check that parent == _targetPanel
        //Container parent = this.getParent();   
        // force redisplay
        _targetPanel.validate();
        _targetPanel.repaint(p.x,p.y,w,h);
        return _contents.remove(l);
    }

    /**
    * On return of 'true', caller should call dispose()
    */
	public boolean deletePanel() {
        if (_debug) log.debug("deletePanel");
		// verify deletion
		int selectedValue = JOptionPane.showOptionDialog(_targetPanel,
				rb.getString("QuestionA")+"\n"+rb.getString("QuestionB"),
				rb.getString("DeleteVerifyTitle"),JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,null,
				new Object[]{rb.getString("ButtonYesDelete"),rb.getString("ButtonNoCancel")},
				rb.getString("ButtonNoCancel"));
        // return without deleting if "No" response
		return (selectedValue == JOptionPane.YES_OPTION);
    }

    public void dispose(boolean clear) {		
        if (_debug) log.debug("Editor delete and dispose done. clear= "+clear);
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.dispose();
        }
		// delete panel - deregister the panel for saving 
        InstanceManager.configureManagerInstance().deregister(this);
		jmri.jmrit.display.PanelMenu.instance().deletePanel(this);
		setVisible(false);
        if (clear) {
            _contents.clear();
        }
        removeAll();
        super.dispose();
    }


    /****************** Mouse Methods ***********************/

    public void showToolTip(Positionable selection, MouseEvent event) {
        ToolTip tip = selection.getTooltip();
        String txt = tip.getText();
        if (txt==null) {
            tip.setText(selection.getNameString());
        }
        tip.setLocation(selection.getX()+selection.getWidth()/2, selection.getY()+selection.getHeight());
        setToolTip(tip);
    }

    protected int getItemX(Positionable p, int deltaX) {
        if ((p instanceof MemoryIcon) && (p.getPopupUtility().getFixedWidth()==0)) {
            MemoryIcon pm = (MemoryIcon) p;
            return pm.getOriginalX() + (int)Math.round(deltaX/getPaintScale());
        } else {
            return p.getX() + (int)Math.round(deltaX/getPaintScale());
        }
    }
    protected int getItemY(Positionable p, int deltaY) {
        if ((p instanceof MemoryIcon) && (p.getPopupUtility().getFixedWidth()==0)) {
            MemoryIcon pm = (MemoryIcon) p;
            return pm.getOriginalY() + (int)Math.round(deltaY/getPaintScale());
        } else {
            return p.getY() + (int)Math.round(deltaY/getPaintScale());
        }
    }
    
    
    /**
    * Provide a method for external code to add items in popup menus
    */
    
    public void addToPopUpMenu(jmri.NamedBean nb, JMenuItem item, int menu){
        if(nb==null || item==null){
            return;
        }
        for(Positionable pos:_contents){
            if(pos.getNamedBean()==nb && pos.getPopupUtility()!=null){
                switch(menu){
                    case VIEWPOPUPONLY : pos.getPopupUtility().addViewPopUpMenu(item); break;
                    case EDITPOPUPONLY : pos.getPopupUtility().addEditPopUpMenu(item); break;
                    default: pos.getPopupUtility().addEditPopUpMenu(item);
                             pos.getPopupUtility().addViewPopUpMenu(item);
                }
                return;
            } else if (pos instanceof SlipTurnoutIcon) {
                if(pos.getPopupUtility()!=null){
                    SlipTurnoutIcon sti = (SlipTurnoutIcon)pos;
                    if(sti.getTurnout(SlipTurnoutIcon.EAST)==nb || sti.getTurnout(SlipTurnoutIcon.WEST)==nb ||
                        sti.getTurnout(SlipTurnoutIcon.LOWEREAST)==nb || sti.getTurnout(SlipTurnoutIcon.LOWERWEST)==nb) {
                        switch(menu){
                            case VIEWPOPUPONLY : pos.getPopupUtility().addViewPopUpMenu(item); break;
                            case EDITPOPUPONLY : pos.getPopupUtility().addEditPopUpMenu(item); break;
                            default: pos.getPopupUtility().addEditPopUpMenu(item);
                                     pos.getPopupUtility().addViewPopUpMenu(item);
                        }
                        return;
                    }
                }
            } else if (pos instanceof MultiSensorIcon) {
                if(pos.getPopupUtility()!=null){
                    MultiSensorIcon msi = (MultiSensorIcon)pos;
                    boolean match = false;
                    for(int i = 0; i<msi.getNumEntries(); i++){
                        if(msi.getSensorName(i).equals(nb.getUserName())){
                            match = true;
                            break;
                        } else if (msi.getSensorName(i).equals(nb.getSystemName())){
                            match = true;
                            break;
                        }
                    }
                    if(match){
                        switch(menu){
                            case VIEWPOPUPONLY : pos.getPopupUtility().addViewPopUpMenu(item); break;
                            case EDITPOPUPONLY : pos.getPopupUtility().addEditPopUpMenu(item); break;
                            default: pos.getPopupUtility().addEditPopUpMenu(item);
                                     pos.getPopupUtility().addViewPopUpMenu(item);
                        }
                        return;
                    }
                }
            }
        }
    }
    
    public final static int VIEWPOPUPONLY = 0x00;
    public final static int EDITPOPUPONLY = 0x01;
    public final static int BOTHPOPUPS = 0x02;

    /**
    * Relocate item
    */
    protected void moveItem(Positionable p, int deltaX, int deltaY) {
        //if (_debug) log.debug("moveItem at ("+p.getX()+","+p.getY()+") delta ("+deltaX+", "+deltaY+")");
        if (getFlag(OPTION_POSITION, p.isPositionable())) {
            int xObj = getItemX( p, deltaX);
            int yObj = getItemY( p, deltaY);
            // don't allow negative placement, icon can become unreachable 
            if (xObj < 0) xObj = 0;
            if (yObj < 0) yObj = 0;
            p.setLocation(xObj, yObj);
            // and show!
            p.repaint();
        }
    }

    /**
    * Return a List of all items whose bounding rectangle contain the mouse position.
    * ordered from top level to bottom
    */
    protected List <Positionable> getSelectedItems(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        Rectangle rect = new Rectangle();
        ArrayList <Positionable> selections = new ArrayList <Positionable>();
        for (int i=0; i<_contents.size(); i++) {
            Positionable p = _contents.get(i);
            rect= p.getBounds(rect);
            //if (_debug && !_dragging) log.debug("getSelectedItems: rect= ("+rect.x+","+rect.y+
            //                      ") width= "+rect.width+", height= "+rect.height+
            //                                    " isPositionable= "+p.isPositionable());
            Rectangle2D.Double rect2D = new Rectangle2D.Double(rect.x*_paintScale,
                                                               rect.y*_paintScale,
                                                               rect.width*_paintScale,
                                                               rect.height*_paintScale);
            if (rect2D.contains(x, y) && (p.getDisplayLevel()>BKG || event.isControlDown())) {
                boolean added =false;
                int level = p.getDisplayLevel();
                for (int k=0; k<selections.size(); k++) {
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
        //if (_debug)  log.debug("getSelectedItems at ("+x+","+y+") "+selections.size()+" found,");
        return selections;
    }

    /*
    * Gather all items inside _selectRect
    * Keep old group if Control key is down
    */
    protected void makeSelectionGroup(MouseEvent event) {
        if (!event.isControlDown() || _selectionGroup==null) {
            _selectionGroup = new ArrayList <Positionable>();
        }
        Rectangle test = new Rectangle();
        List <Positionable> list = getContents();
        if (event.isShiftDown()) {
            for (int i=0; i < list.size(); i++) {
                Positionable comp = list.get(i);
                if (_selectRect.intersects(comp.getBounds(test)) && 
                                (event.isControlDown() || comp.getDisplayLevel()>BKG)) {
                    _selectionGroup.add(comp);
                  /*  if (_debug) {
                        log.debug("makeSelectionGroup: selection: "+ comp.getNameString()+
                                    ", class= "+comp.getClass().getName());
                    } */
                }
            }
        } else {
            for (int i=0; i < list.size(); i++) {
                Positionable comp = list.get(i);
                if (_selectRect.contains(comp.getBounds(test)) && 
                                (event.isControlDown() || comp.getDisplayLevel()>BKG)) {
                    _selectionGroup.add(comp);
                  /*  if (_debug) {
                        log.debug("makeSelectionGroup: selection: "+ comp.getNameString()+
                                    ", class= "+comp.getClass().getName());
                    } */
                }
            }
        }
        if (_debug) log.debug("makeSelectionGroup: "+_selectionGroup.size()+" selected.");
        if (_selectionGroup.size() < 1) {
            _selectRect = null;
            _selectionGroup = null;
        }
    }

    /*
    * For the param, selection, Add to or delete from _selectionGroup. 
    * If not there, add.
    * If there, delete.
    * make new group if Cntl key is not held down
    */
    protected void modifySelectionGroup(Positionable selection, MouseEvent event) {
        if (!event.isControlDown() || _selectionGroup==null) {
            _selectionGroup = new ArrayList <Positionable>();
        }
        boolean removed = false;
        if (selection.getDisplayLevel()>BKG || event.isControlDown()) {
            if (_selectionGroup.contains(selection)) {
                removed = _selectionGroup.remove(selection);
            } else {
                _selectionGroup.add(selection);
            }
        }
        if (_debug) {
            log.debug("modifySelectionGroup: size= "+_selectionGroup.size()+", selection "+ 
                      (removed ? "removed" : "added"));
        }
    }

    protected boolean setTextAttributes(PositionableLabel p, JPopupMenu popup) {
        if (p.getPopupUtility()==null) {
            return false;
        }
        popup.add(new AbstractAction(rb.getString("TextAttributes")){
        	PositionableLabel comp;
            public void actionPerformed(java.awt.event.ActionEvent e) {
                new TextAttrDialog(comp);
            }
            AbstractAction init(PositionableLabel pos) {
                comp = pos;
                return this;
            }
        }.init(p));
        return true;
    }
	
    class TextAttrDialog extends JDialog {
    	PositionableLabel _pos;
        jmri.jmrit.display.palette.DecoratorPanel _decorator;
        TextAttrDialog(PositionableLabel p) {
            super(_targetFrame, rb.getString("TextAttributes"), true);
            _pos = p;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            _decorator = new jmri.jmrit.display.palette.DecoratorPanel(_pos.getEditor());
            _decorator.initDecoratorPanel(_pos);
            panel.add(_decorator);
            panel.add(makeDoneButtonPanel());
            setContentPane(panel);
            pack();
            setLocationRelativeTo((java.awt.Component)_pos);
            setVisible(true);
        }
        protected JPanel makeDoneButtonPanel() {
            JPanel panel0 = new JPanel();
            panel0.setLayout(new FlowLayout());
            JButton doneButton = new JButton(rb.getString("Done"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        PositionablePopupUtil util = _decorator.getPositionablePopupUtil();
                        _decorator.getText(_pos);
                        setAttributes(util, _pos, _decorator.isOpaque());
                        setSelectionsAttributes(util, _pos, _decorator.isOpaque());
                        dispose();
                    }
            });
            panel0.add(doneButton);

            JButton cancelButton = new JButton(rb.getString("Cancel"));
            cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        dispose();
                    }
            });
            panel0.add(cancelButton);
            return panel0;
        }
    }
    
    protected void setAttributes(PositionablePopupUtil newUtil, PositionableLabel pos, boolean isOpaque) {
    	if (!pos.isText() || (pos.isText() && pos.isIcon())) {
    		return;
    	}
		pos.saveOpaque(isOpaque);
        pos.setPopupUtility(newUtil.clone(pos));
		pos.setOpaque(isOpaque);
        int deg = pos.getDegrees();
        if (deg!=0) {
            pos.rotate(0);
    		pos.setOpaque(isOpaque);
            pos.rotate(deg);        	
        }
//		PositionablePopupUtil u = pos.getPopupUtility();
//		u.setMargin(u.getMargin());
		pos.updateSize();
        if (pos instanceof PositionableIcon) {
        	jmri.NamedBean bean = pos.getNamedBean();
        	if (bean!=null) {
            	((PositionableIcon)pos).displayState(bean.getState());                            		
        	}
        }
    }

    protected void setSelectionsAttributes(PositionablePopupUtil util, Positionable pos, boolean isOpaque) { 
        if (_selectionGroup!=null && _selectionGroup.contains(pos)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
            	Positionable p = _selectionGroup.get(i);
            	if ( p instanceof PositionableLabel ) {
                    setAttributes(util, (PositionableLabel)p, isOpaque);           		
            	}
             }
        }
    }

    protected void setSelectionsHidden(boolean enabled, Positionable p) { 
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).setHidden(enabled);
            }
        }
    }
        
    protected boolean setSelectionsPositionable(boolean enabled, Positionable p) { 
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).setPositionable(enabled);
            }
            return true;
        } else {
            return false; 
        }
    }

    protected void removeSelections(Positionable p) { 
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).remove();
            }
            _selectionGroup = null;
        }
    }

    protected void setSelectionsScale(double s, Positionable p) { 
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).setScale(s);
            }
        } else {
            p.setScale(s);
        }
    }
        
    protected void setSelectionsRotation(int k, Positionable p) { 
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).rotate(k);
            }
        } else {
            p.rotate(k);
        }
    }
        
    protected void setSelectionsDockingLocation(Positionable p) {
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
            	Positionable pos = _selectionGroup.get(i);
            	if (pos instanceof LocoIcon) {
            		((LocoIcon)pos).setDockingLocation(pos.getX(), pos.getY());
            	}
            }
        }
        else if (p instanceof LocoIcon) {
        		((LocoIcon)p).setDockingLocation(p.getX(), p.getY());
        }
    }
        
    protected void dockSelections(Positionable p) {
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            for (int i=0; i<_selectionGroup.size(); i++) {
            	Positionable pos = _selectionGroup.get(i);
            	if (pos instanceof LocoIcon) {
            		((LocoIcon)pos).dock();
            	}
            }
        }
        else if (p instanceof LocoIcon) {
        		((LocoIcon)p).dock();
        	}       	
    }
        
    protected boolean showAlignPopup(Positionable p) {
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            return true;
        } else {
            return false;
        }
    }

    protected void alignGroup(boolean alignX, boolean alignToFirstSelected) {
        if (_selectionGroup==null) {
            return;
        }
        int sum = 0;
        int cnt = 0;
        int ave = 0;
        
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable comp = _selectionGroup.get(i);
            if (!getFlag(OPTION_POSITION, comp.isPositionable()))  { continue; }
            if (alignToFirstSelected) {
                if (alignX) {
                        ave = comp.getX();
                    } else {
                        ave = comp.getY();
                    }
                    break;
                } else {
                    if (alignX) {
                    sum += comp.getX();
                } else {
                    sum += comp.getY();
                }
            cnt++;
            }
        }

        if (!alignToFirstSelected) {
            ave = Math.round((float) sum / cnt);
        }

        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable comp = _selectionGroup.get(i);
            if (!getFlag(OPTION_POSITION, comp.isPositionable()))  { continue; }
            if (alignX) {
                comp.setLocation(ave, comp.getY());
            } else {
                comp.setLocation(comp.getX(), ave);
            }
        }
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
        _selectRect = new Rectangle(aX, aY, w, h);
    }

    public final int getAnchorX() {
        return _anchorX;
    }

    public final int getAnchorY() {
        return _anchorY;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (_selectionGroup==null) return;
        int x = 0;
        int y = 0;
        switch (e.getKeyCode()){
            case KeyEvent.VK_UP: y=-1;
                                break;
            case KeyEvent.VK_DOWN: y=1;
                                break;
            case KeyEvent.VK_LEFT: x=-1;
                                break;
            case KeyEvent.VK_RIGHT: x=1;
                                    break;
        }
        //A cheat if the shift key isn't pressed then we move 5 pixels at a time.
        if(!e.isShiftDown()){
            y=y*5;
            x=x*5;
        }
        for (int i=0; i<_selectionGroup.size(); i++) {
            moveItem(_selectionGroup.get(i), x, y);
        }
        _targetPanel.repaint();
    }

    public void keyReleased(KeyEvent e) {
    }
    
    /*********************** Abstract Methods ************************/

    abstract public void mousePressed(MouseEvent event);

    abstract public void mouseReleased(MouseEvent event);

    abstract public void mouseClicked(MouseEvent event);

    abstract public void mouseDragged(MouseEvent event);

    abstract public void mouseMoved(MouseEvent event);
    
    abstract public void mouseEntered(MouseEvent event);

    abstract public void mouseExited(MouseEvent event);
    
    /*
     * set up target panel, frame etc.
     */
    abstract protected void init(String name);
    /*
     * Closing of Target frame window.
     */
    abstract protected void targetWindowClosingEvent(java.awt.event.WindowEvent e);
    /**
     * Called from TargetPanel's paint method for additional drawing by editor view
     */
    abstract protected void paintTargetPanel(Graphics g);
    /**
     * Set an object's location when it is created.
     */
    abstract protected void setNextLocation(Positionable obj);
    /**
     * Editor Views should make calls to this class (Editor) to set popup menu items.
     * See 'Popup Item Methods' above for the calls.
     */
    abstract protected void showPopUp(Positionable p, MouseEvent event);
    /**
     * After construction, initialize all the widgets to their saved config settings.
     */
    abstract public void initView();
    
    /**
    * set up item(s) to be copied by paste
    */
    abstract protected void copyItem(Positionable p);

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Editor.class.getName());
}
