package jmri.jmrit.display;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
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
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.picker.PickListModel;
import org.jdom.Element;

import jmri.util.JmriJFrame;
import jmri.configurexml.*;

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
 * @author  Pete Cressman Copyright: Copyright (c) 2009, 2010
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
    private boolean _debug = false;

    boolean showCloseInfoMessage = true;	//display info message when closing panel
    
    protected ArrayList <Positionable> _contents = new ArrayList<Positionable>();
    private JLayeredPane _targetPanel;
    private JFrame      _targetFrame;
	private JScrollPane _panelScrollPane;
	
    // Option menu items 
	protected int _scrollState = SCROLL_NONE;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _controlLayout = true;
    private boolean _showHidden = true;
    private boolean _showTooltip = true;
    private boolean _showCoordinates = true;

    final public static int OPTION_POSITION = 1;
    final public static int OPTION_CONTROLS = 2;
    final public static int OPTION_HIDDEN = 3;
    final public static int OPTION_TOOLTIP= 4;
    final public static int OPTION_COORDS = 5;

    private boolean _globalSetsLocal = true;    // pre 2.9.6 behavior
    private boolean _useGlobalFlag = false;     // pre 2.9.6 behavior

    // mouse methods variables
    protected int _lastX;
    protected int _lastY;
    BasicStroke DASHED_LINE = new BasicStroke(1f, BasicStroke.CAP_BUTT, 
                                    BasicStroke.JOIN_BEVEL,
                                    10f, new float[] {10f, 10f}, 0f);

    protected Rectangle _selectRect = null;
    Rectangle _highlightcomponent = null;
    protected boolean _dragging = false;
    protected ArrayList <Positionable> _selectionGroup = null;  // items gathered inside fence

    private Positionable _currentSelection;
    private ToolTip _defaultToolTip;
    private ToolTip _tooltip = null;

    // Accessible to editor views
    protected int xLoc = 0;     // x coord of selected Positionable
    protected int yLoc = 0;     // y coord of selected Positionable
    protected int _anchorX;     // x coord when mousePressed
    protected int _anchorY;     // y coord when mousePressed

    private boolean delayedPopupTrigger = false; // Used to delay the request of a popup, on a mouse press as this may conflict with a drag event

    private double _paintScale = 1.0;   // scale for _targetPanel drawing
    
    private Color defaultBackgroundColor = Color.lightGray;

    // map of icon editor frames (incl, icon editor) keyed by name
    HashMap <String, JFrameItem> _iconEditorFrame = new HashMap <String, JFrameItem>();

    public Editor() {
        _debug = log.isDebugEnabled();
    }

    public Editor(String name) {
        super(name);
        setName(name);
        _debug = log.isDebugEnabled();
        _defaultToolTip = new ToolTip(null, 0, 0);
        setVisible(false);
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
        if (_debug) log.debug("setTargetPanelSize now w="+w+", h="+h);
        _targetPanel.setSize(w, h);
        _targetPanel.invalidate();
        //_targetFrame.setSize(w, h);
        //_targetFrame.pack();
    }

    protected Dimension getTargetPanelSize() {
        return _targetPanel.getSize();
    }
     
    protected final JComponent getTargetPanel() {
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

    static public int TOOLTIPSHOWDELAY = 1000; // msec
    static public int TOOLTIPDISMISSDELAY = 4000;  // msec

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

    class ToolTipTimer extends Timer {
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
            if (_debug) log.debug("size now w="+width+", h="+height);
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
                if (_debug) log.debug("size was "+w+","+h+" - i="+i);
                setSize(wnew,hnew);
            }
            return super.add(c, i);
        }
        public void add(Component c, Object o) {
            super.add(c, o);
            int hnew = Math.max(h, c.getLocation().y+c.getSize().height);
            int wnew = Math.max(w, c.getLocation().x+c.getSize().width);
            if (hnew>h || wnew>w) {
                if (_debug) log.debug("adding of "+c.getSize()+" with Object - i="+o);
                setSize(wnew,hnew);
            }
        }
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.scale(_paintScale, _paintScale);
            super.paint(g);
            paintTargetPanel(g);
            if (_selectRect != null) {
                //Draw a rectangle on top of the image.
                java.awt.Stroke stroke = g2d.getStroke();
                Color color = g2d.getColor();
                g2d.setStroke(DASHED_LINE);
                g2d.setColor(Color.red);
                g.drawRect(_selectRect.x, _selectRect.y, _selectRect.width, _selectRect.height);
                g2d.setStroke(stroke);
                g2d.setColor(color);
            }
            if (_highlightcomponent!=null || _selectionGroup!=null){
                java.awt.Stroke stroke = g2d.getStroke();
                Color color = g2d.getColor();
                g2d.setColor(new Color(204, 207, 88));
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                if (_selectionGroup!=null){
                    for(int i=0; i<_selectionGroup.size();i++){
                        g.drawRect(_selectionGroup.get(i).getX(), _selectionGroup.get(i).getY(), _selectionGroup.get(i).maxWidth(), _selectionGroup.get(i).maxHeight());
                    }
                }
                if (_highlightcomponent!=null)
                    g.drawRect(_highlightcomponent.x, _highlightcomponent.y, _highlightcomponent.width, _highlightcomponent.height);
                //Draws a border around the highlighted component
                g2d.setColor(color);
                g2d.setStroke(stroke);
            }
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
        if (_debug)  log.debug("getFlag Option= "+whichOption+", _useGlobalFlag="+_useGlobalFlag+" localFlag="+localFlag);
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
                case OPTION_COORDS:
                    return _showCoordinates;
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
    public void setShowCoordinates(boolean state) {
        _showCoordinates = state;
        for (int i = 0; i<_contents.size(); i++) {
            _contents.get(i).setViewCoordinates(state);
        } 
    }
    public boolean showCoordinates() {
        return _showCoordinates;
    }

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
        String value = new String();
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

    protected void changeView(String className) {

        JFrame frame = getTargetFrame();
        Dimension size = frame.getSize();
        Point posn = frame.getLocation();

        try {
            Editor ed = (Editor)Class.forName(className).newInstance();
            //ed.setEditable(false);
            ed.setName(getName());
            ed.init(getName());
            //ed.getTargetFrame().setTitle(frame.getTitle());
            ed.getTargetFrame().setLocation(posn.x,posn.y);
            ed.getTargetFrame().setSize(size.width,size.height);
            ed.setAllEditable(isEditable());
            ed.setAllPositionable(allPositionable());
            ed.setShowCoordinates(showCoordinates());
            ed.setAllShowTooltip(showTooltip());
            ed.setAllControlling(allControlling());
            ed.setShowHidden(isVisible());
            ed.setPanelMenu(frame.getJMenuBar().isVisible());
            ed.setScroll(getScrollable());
            ed.setTitle();
            ed._contents = _contents;
            for (int i = 0; i<_contents.size(); i++) {
                Positionable p = _contents.get(i);
                p.setEditor(ed);
                ed.addToTarget(p);
            }
            ed.pack();
            ed.setVisible(true);
            jmri.jmrit.display.PanelMenu.instance().addEditorPanel(ed);
        } catch (ClassNotFoundException cnfe) {
            log.error("changeView exception "+cnfe.toString());
        } catch (InstantiationException ie) {
            log.error("changeView exception "+ie.toString());
        } catch (IllegalAccessException iae) {
            log.error("changeView exception "+iae.toString());
        }
        dispose(false);
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
        if (showCoordinates()) {
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
        }
        return false;
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
                    alignGroup(true);
                }
            });
            edit.add(new AbstractAction(rb.getString("AlignY")) {
                public void actionPerformed(ActionEvent e) {
                    alignGroup(false);
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
        if (p.getDisplayLevel() == BKG) {
            return;
        }
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
                if (_selectionGroup==null)
                    comp.remove();
                else
                    removeMultiItems();
            }
            AbstractAction init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
    }
    
    private void removeMultiItems(){
        boolean itemsInCopy = false;
        if (_selectionGroup==_multiItemCopyGroup){
            itemsInCopy=true;
        }
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable comp = _selectionGroup.get(i);
            comp.remove();
        }
        //As we have removed all the items from the panel we can remove the group.
        _selectionGroup = null;
        //If the items in the selection group and copy group are the same we need to
        //clear the copy group as the originals no longer exist.
        if (itemsInCopy)
            _multiItemCopyGroup = null;
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
        final JComboBox rosterBox = Roster.instance().fullRosterComboBox();
        rosterBox.insertItemAt("", 0);
        rosterBox.setSelectedIndex(0);
        rosterBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectLoco(rosterBox.getSelectedItem().toString());
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
    protected void selectLoco(String rosterEntryTitle){
		if (rosterEntryTitle == "")
			return;
		RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		// try getting road number, else use DCC address
		String rn = entry.getRoadNumber();
		if ((rn==null) || rn.equals("")) 
			rn = entry.getDccAddress();
		if (rn != null){
			LocoIcon l = addLocoIcon(rn);
			l.setRosterEntry(entry);
		}
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
		log.debug("Remove markers");
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
        l.setTooltip(new ToolTip(_defaultToolTip, l));
        addToTarget(l);
        _contents.add(l);
        /*This allows us to catch any new items that are being pasted into the panel
        and add them to the selection group, so that the user can instantly move them around*/
        if (pasteItem){
            amendSelectionGroup(l);
        }
    }
    
    protected void addToTarget(Positionable l) {
        JComponent c = (JComponent)l;
        c.invalidate();
    	_targetPanel.remove(c);
        _targetPanel.add(c, new Integer(l.getDisplayLevel()));
        _targetPanel.moveToFront(c);
		c.repaint();
        _targetPanel.validate();
    }

    /************** Icon editors for adding content ************/

    static final public String[] ICON_EDITORS = {"SensorEditor", "RightTOEditor", "LeftTOEditor",
                        "SlipTOEditor", "SignalHeadEditor", "SignalMastEditor", "MemoryEditor", 
                        "ReporterEditor", "BackgroundEditor", "MultiSensorEditor", "IconEditor"};
    /**
    * @param name Icon editor's name
    */
    public JFrameItem getIconFrame(String name) {
        JFrameItem frame = _iconEditorFrame.get(name);
        if (frame==null) {
            if ("SensorEditor".equals(name)) {
                addSensorEditor();
            } else if ("RightTOEditor".equals(name)) {
                addRightTOEditor();
            } else if ("LeftTOEditor".equals(name)) {
                addLeftTOEditor();
            } else if ("SlipTOEditor".equals(name)) {
                addSlipTOEditor();
            } else if ("SignalHeadEditor".equals(name)) {
                addSignalHeadEditor();
            } else if ("SignalMastEditor".equals(name)) {
                addSignalMastEditor();
            } else if ("MemoryEditor".equals(name)) {
                addMemoryEditor();
            } else if ("ReporterEditor".equals(name)) {
                addReporterEditor();
            } else if ("LightEditor".equals(name)) {
                addLightEditor();
            } else if ("BackgroundEditor".equals(name)) {
                addBackgroundEditor();
            } else if ("MultiSensorEditor".equals(name)) {
                addMultiSensorEditor();
            } else if ("IconEditor".equals(name)) {
                addIconEditor();
            } else {
                log.error("No such Icon Editor \""+name+"\"");
                return null;
            }
            // frame added in the above switch 
            frame = _iconEditorFrame.get(name);
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
        IconAdder editor = new IconAdder("RightTOEditor");
        editor.setIcon(3, "TurnoutStateClosed",
            "resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
        editor.setIcon(2, "TurnoutStateThrown", 
            "resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent", 
            "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
            "resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");

        JFrameItem frame = makeAddIconFrame("RightTOEditor", "addIconsToPanel", "SelectTO", editor);
        _iconEditorFrame.put("RightTOEditor", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addTurnoutR();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("RightTOEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLeftTOEditor() {
        IconAdder editor = new IconAdder("LeftTOEditor");
        editor.setIcon(3, "TurnoutStateClosed",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
        editor.setIcon(2, "TurnoutStateThrown", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

        JFrameItem frame = makeAddIconFrame("LeftTOEditor", "addIconsToPanel", "SelectTO", editor);
        _iconEditorFrame.put("LeftTOEditor", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addTurnoutL();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("LeftTOEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
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
        JFrameItem frame = makeAddIconFrame("SlipTOEditor", "addIconsToPanel", "SelectTO", editor);
        _iconEditorFrame.put("SlipTOEditor", frame);
        editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addSlip();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("SlipTOEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.SlipTurnoutIcon", true);
    }

    protected void addSensorEditor() {
        IconAdder editor = new IconAdder("SensorEditor");
        editor.setIcon(3, "SensorStateActive",
            "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        editor.setIcon(2, "SensorStateInactive", 
            "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        editor.setIcon(0, "BeanStateInconsistent", 
            "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
            "resources/icons/smallschematics/tracksegments/circuit-error.gif");

        JFrameItem frame = makeAddIconFrame("SensorEditor", "addIconsToPanel", 
                                           "SelectSensor", editor);
        _iconEditorFrame.put("SensorEditor", frame);
        editor.setPickList(PickListModel.sensorPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putSensor();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("SensorEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addSignalHeadEditor() {
        IconAdder editor = new IconAdder("SignalHeadEditor");
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

        JFrameItem frame = makeAddIconFrame("SignalHeadEditor", "addIconsToPanel", 
                                                       "SelectSignalHead", editor);
        _iconEditorFrame.put("SignalHeadEditor", frame);
        editor.setPickList(PickListModel.signalHeadPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putSignalHead();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("SignalHeadEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }


    protected void addSignalMastEditor() {
        IconAdder editor = new IconAdder("SignalMastEditor");

        JFrameItem frame = makeAddIconFrame("SignalMastEditor", "addIconsToPanel", 
                                           "SelectSignalMast", editor);
        _iconEditorFrame.put("SignalMastEditor", frame);
        editor.setPickList(PickListModel.signalMastPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addSignalMast();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("SignalMastEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    SpinnerNumberModel _spinCols = new SpinnerNumberModel(0,0,100,1);

    protected void addMemoryEditor() {
        IconAdder editor = new IconAdder("MemoryEditor") {
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
        JFrameItem frame = makeAddIconFrame("MemoryEditor", "addMemValueToPanel", "SelectMemory", editor);
        _iconEditorFrame.put("MemoryEditor", frame);
        editor.setPickList(PickListModel.memoryPickModelInstance());
        editor.makeIconPanel();
        editor.complete(addIconAction, null, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addReporterEditor() {
        IconAdder editor = new IconAdder("ReporterEditor");
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addReporter();
            }
        };
        JFrameItem frame = makeAddIconFrame("ReporterEditor", "addReportValueToPanel","SelectReporter", editor);
        _iconEditorFrame.put("ReporterEditor", frame);
        editor.setPickList(PickListModel.reporterPickModelInstance());
        editor.makeIconPanel();
        editor.complete(addIconAction, null, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addLightEditor() {
        IconAdder editor = new IconAdder("LightEditor");
        editor.setIcon(3, "LightStateOff",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
        editor.setIcon(2, "LightStateOn", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
        editor.setIcon(0, "BeanStateInconsistent", 
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
        editor.setIcon(1, "BeanStateUnknown",
            "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

        JFrameItem frame = makeAddIconFrame("LightEditor", "addIconsToPanel", 
                                           "SelectLight", editor);
        _iconEditorFrame.put("LightEditor", frame);
        editor.setPickList(PickListModel.lightPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addLight();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("LightEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected void addBackgroundEditor() {
        IconAdder editor = new IconAdder("BackgroundEditor");
        editor.setIcon(0, "background","resources/PanelPro.gif");

        JFrameItem frame = makeAddIconFrame("BackgroundEditor","addBackgroundToPanel", "pressAdd", editor);
        _iconEditorFrame.put("BackgroundEditor", frame);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putBackground();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("BackgroundEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }

    protected JFrameItem addMultiSensorEditor() {
        MultiSensorIconAdder editor = new MultiSensorIconAdder("MultiSensorEditor");
        editor.setIcon(0, "BeanStateInconsistent",
                                  "resources/icons/USS/plate/levers/l-inconsistent.gif");
        editor.setIcon(1, "BeanStateUnknown",
                                  "resources/icons/USS/plate/levers/l-unknown.gif");
        editor.setIcon(2, "SensorStateInactive",
                                  "resources/icons/USS/plate/levers/l-inactive.gif");
        editor.setIcon(3, "MultiSensorPosition",
                                  "resources/icons/USS/plate/levers/l-left.gif");
        editor.setIcon(4, "MultiSensorPosition",
                                  "resources/icons/USS/plate/levers/l-vertical.gif");
        editor.setIcon(5, "MultiSensorPosition",
                                  "resources/icons/USS/plate/levers/l-right.gif");

        JFrameItem frame = makeAddIconFrame("MultiSensorEditor", "addIconsToPanel","SelectMultiSensor", editor);
        _iconEditorFrame.put("MultiSensorEditor", frame);
        frame.addHelpMenu("package.jmri.jmrit.display.MultiSensorIconAdder", true);

        editor.setPickList(PickListModel.sensorPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addMultiSensor();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("MultiSensorEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, true, false);
        return frame;
    }

    protected void addIconEditor() {
        IconAdder editor = new IconAdder("IconEditor");
        editor.setIcon(0, "plainIcon","resources/icons/smallschematics/tracksegments/block.gif");
        JFrameItem frame = makeAddIconFrame("IconEditor", "addIconToPanel", "pressAdd", editor);
        _iconEditorFrame.put("IconEditor", frame);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                putIcon();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    JFrameItem frame = _iconEditorFrame.get("IconEditor");
                    frame.getEditor().addCatalog();
                    frame.pack();
                }
        };
        editor.makeIconPanel();
        editor.complete(addIconAction, changeIconAction, false, false);
        frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
    }
    
    /**************** add content items from Icon Editors ********************/
    /**
     * Add a sensor indicator to the target
     */
    protected SensorIcon putSensor() {
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), this);
        IconAdder editor = getIconEditor("SensorEditor");
        l.setActiveIcon(editor.getIcon("SensorStateActive"));
        l.setInactiveIcon(editor.getIcon("SensorStateInactive"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
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
        IconAdder editor = getIconEditor("RightTOEditor");
        addTurnout(editor);
    }
    
    void addTurnoutL() {      
        IconAdder editor = getIconEditor("LeftTOEditor");
        addTurnout(editor);
    }
    
    void addTurnout(IconAdder editor){
    	TurnoutIcon l = new TurnoutIcon(this);
        l.setClosedIcon(editor.getIcon("TurnoutStateClosed"));
        l.setThrownIcon(editor.getIcon("TurnoutStateThrown"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setTurnout(editor.getTableSelection().getDisplayName());
        l.setDisplayLevel(TURNOUTS);
        setNextLocation(l);
        putItem(l);
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
        IconAdder editor = getIconEditor("SignalHeadEditor");
        l.setRedIcon(editor.getIcon("SignalHeadStateRed"));
        l.setFlashRedIcon(editor.getIcon("SignalHeadStateFlashingRed"));
        l.setYellowIcon(editor.getIcon("SignalHeadStateYellow"));
        l.setFlashYellowIcon(editor.getIcon("SignalHeadStateFlashingYellow"));
        l.setGreenIcon(editor.getIcon("SignalHeadStateGreen"));
        l.setFlashGreenIcon(editor.getIcon("SignalHeadStateFlashingGreen"));
        l.setDarkIcon(editor.getIcon("SignalHeadStateDark"));
        l.setHeldIcon(editor.getIcon("SignalHeadStateHeld"));
        l.setDarkIcon(editor.getIcon("SignalHeadStateLunar"));
        l.setHeldIcon(editor.getIcon("SignalHeadStateFlashingLunar"));
        l.setSignalHead(editor.getTableSelection().getDisplayName());
        l.setDisplayLevel(SIGNALS);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    /**
     * Add a signal mast to the target
     */
    void addSignalMast() {
        SignalMastIcon l = new SignalMastIcon(this);
        IconAdder editor = _iconEditorFrame.get("SignalMastEditor").getEditor();
        l.setSignalMast(editor.getTableSelection().getDisplayName());
        l.setDisplayLevel(SIGNALS);
        setNextLocation(l);
        putItem(l);
    }

    void putMemory() {
        MemoryIcon l = new MemoryIcon(new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif"), this);
        IconAdder memoryIconEditor = getIconEditor("MemoryEditor");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
    }
    
    void addMemorySpinner() {
        MemorySpinnerIcon l = new MemorySpinnerIcon(this);
        IconAdder memoryIconEditor = getIconEditor("MemoryEditor");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
    }

    void addMemoryInputBox() {
        MemoryInputIcon l = new MemoryInputIcon(_spinCols.getNumber().intValue(), this);
        IconAdder memoryIconEditor = getIconEditor("MemoryEditor");
        l.setMemory(memoryIconEditor.getTableSelection().getDisplayName());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        setNextLocation(l);
        putItem(l);
    }
    
    /**
     * Add a Light indicator to the target
     */
    void addLight() {
        LightIcon l = new LightIcon(this);
        IconAdder editor = getIconEditor("LightEditor");
        l.setOffIcon(editor.getIcon("LightStateOff"));
        l.setOnIcon(editor.getIcon("LightStateOn"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setLight((Light)editor.getTableSelection());
        l.setDisplayLevel(LIGHTS);
        setNextLocation(l);
        putItem(l);
    }

    void addReporter() {
        ReporterIcon l = new ReporterIcon(this);
        IconAdder reporterIconEditor = getIconEditor("ReporterEditor");
        l.setReporter((Reporter)reporterIconEditor.getTableSelection());
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(REPORTERS);
        setNextLocation(l);
        putItem(l);
    }
    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void putBackground() {
        // most likely the image is scaled.  get full size from URL
        IconAdder bkgrndEditor = getIconEditor("BackgroundEditor");
        String url = bkgrndEditor.getIcon("background").getURL();
        setUpBackground(url);
    }

    
    /**
     * Add an icon to the target
     */    
    protected Positionable putIcon() {
        IconAdder iconEditor = getIconEditor("IconEditor");
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        PositionableLabel l = new PositionableLabel(icon, this);
        l.setPopupUtility(null);        // no text 
        l.setDisplayLevel(ICONS);
        setNextLocation(l);
        putItem(l);
        return l;
    }

    public MultiSensorIcon addMultiSensor() {
        MultiSensorIcon m = new MultiSensorIcon(this);
        MultiSensorIconAdder editor = (MultiSensorIconAdder)getIconEditor("MultiSensorEditor");
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
    
    public class JFrameItem extends JmriJFrame {
        IconAdder _editor;
        JFrameItem (String title, IconAdder editor) {
            super(title);
            _editor = editor;
            setName(title);
        }
        public IconAdder getEditor() {
            return _editor;
        }
        public String toString() {
            return this.getName();
        }
        public void windowClosing(java.awt.event.WindowEvent e) {
            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            if (_debug) log.debug("windowClosing: "+toString());
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

    protected JFrameItem makeAddIconFrame(String title, String select1, String select2, 
                                IconAdder editor) {
        JFrameItem frame = new JFrameItem(rb.getString(title), editor);
        if (editor != null) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(rb.getString(select1)));
            p.add(new JLabel(rb.getString(select2)));
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
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    Editor editor;
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(editor);
                    }
                    java.awt.event.WindowAdapter init(Editor ed) {
                        editor = ed;
                        return this;
                    }
            }.init(this));
        }
        _iconEditorFrame.put(title, frame);
        String name = getTitle();
        frame.setTitle(frame.getName()+" ("+name+")");
        frame.pack();
        return frame;
    }

    /********************* cleanup *************************/

    private void removeFromTarget(Positionable l) {
        _targetPanel.remove((Component)l);
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
        if (_debug) log.debug("Editor delete and dispose done.");
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
            int system = jmri.util.SystemType.getType();
            if (system==jmri.util.SystemType.MACOSX) {
                txt = java.text.MessageFormat.format(rb.getString("ToolTipGenericMac"), selection.getNameString());
            }
            else if (system==jmri.util.SystemType.WINDOWS) {
                txt = java.text.MessageFormat.format(rb.getString("ToolTipGenericWin"), selection.getNameString());
            }
            else {
                txt = java.text.MessageFormat.format(rb.getString("ToolTipGeneric"), selection.getNameString());
            }
            tip.setText(txt);
        }
        tip.setLocation(selection.getX()+selection.getWidth()/2, selection.getY()+selection.getHeight());
        setToolTip(tip);
    }

    /**
    * Relocate item
    */
    protected void moveItem(Positionable p, int deltaX, int deltaY) {
        //if (_debug) log.debug("moveItem at ("+p.getX()+","+p.getY()+") delta ("+deltaX+", "+deltaY+")");
        if (getFlag(OPTION_POSITION, p.isPositionable())) {
            int xObj;
            int yObj;
            if ((p instanceof MemoryIcon) && (p.getPopupUtility().getFixedWidth()==0)) {
                MemoryIcon pm = (MemoryIcon) p;
                xObj = pm.getOriginalX() + (int)Math.round(deltaX/getPaintScale());
                yObj = pm.getOriginalY() + (int)Math.round(deltaY/getPaintScale());
            } else {
                xObj = p.getX() + (int)Math.round(deltaX/getPaintScale());
                yObj = p.getY() + (int)Math.round(deltaY/getPaintScale());
            }
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
            //if (_debug) log.debug("getSelectedItems: rect= ("+rect.x+","+rect.y+
            //                      ") width= "+rect.width+", height= "+rect.height);
            Rectangle2D.Double rect2D = new Rectangle2D.Double(rect.x*_paintScale,
                                                               rect.y*_paintScale,
                                                               rect.width*_paintScale,
                                                               rect.height*_paintScale);
            if (rect2D.contains(x, y)) {
                boolean added =false;
                int level = p.getDisplayLevel();
                for (int k=0; k<selections.size(); k++) {
                    if (level > selections.get(k).getDisplayLevel()) {
                        selections.add(k, p);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    selections.add(p);
                }
            }
        }
        if (_debug)  log.debug("getSelectedItems at ("+x+","+y+") "+selections.size()+" found,");
        return selections;
    }

    /*
    * Gather all items inside _selectRect
    */
    protected void makeSelectionGroup() {
        _selectionGroup = new ArrayList <Positionable>();
        Rectangle test = new Rectangle();
        List <Positionable> list = getContents();
		for (int i=0; i < list.size(); i++) {
			Positionable comp = list.get(i);
            if (_selectRect.contains(comp.getBounds(test))) {
                _selectionGroup.add(comp);
              /*  if (_debug) {
                    log.debug("makeSelectionGroup: selection: "+ comp.getNameString()+
                                ", class= "+comp.getClass().getName());
                } */
            }
		}
        if (_debug) {
            log.debug("makeSelectionGroup:"+_selectionGroup.size()+" selected.");
        }
        if (_selectionGroup.size() < 1) {
            _selectRect = null;
            _selectionGroup = null;
        }
    }
    
    private void amendSelectionGroup(Positionable p){
        if (p==null) return;
        if (_selectionGroup==null){
            _selectionGroup = new ArrayList <Positionable>();
        }
        boolean removed = false;
        for(int i=0; i<_selectionGroup.size();i++){
            if (_selectionGroup.get(i)==p){
                _selectionGroup.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _selectionGroup.add(p);
        else if (removed && _selectionGroup.isEmpty())
            _selectionGroup=null;
        _targetPanel.repaint();
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
    protected boolean showAlignPopup(Positionable p) {
        if (_selectionGroup!=null && _selectionGroup.contains(p)) {
            return true;
        } else {
            return false;
        }
    }

    protected void lockSelections(boolean lock) {
        if (_selectionGroup!=null) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).setPositionable(!lock);
            }
        } else {
            _currentSelection.setPositionable(!lock);
        }
    }

    protected void alignGroup(boolean alignX) {
        if (_selectionGroup==null) {
            return;
        }
        int sum = 0;
        int cnt = 0;
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable comp = _selectionGroup.get(i);
            if (!getFlag(OPTION_POSITION, comp.isPositionable()))  { continue; }
            if (alignX) {
                sum += comp.getX();
            } else {
                sum += comp.getY();
            }
            cnt++;
        }
        int ave = Math.round((float)sum/cnt);
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

    private void moveSelectRect(int deltaX, int deltaY) {
        if (_selectRect != null) {
            _selectRect = new Rectangle(_selectRect.x+deltaX, _selectRect.y+deltaY,
                                        _selectRect.width, _selectRect.height);
        }
    }

    private void drawSelectRect(int x, int y) {
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

    /**
    * The "selects" List in the following methods contains all the Content items whose bounding
    * rectangle contains the Point (event.getX(), event.getY()). The list is ordered by
    * ascending level - i.e. the top-most item is at index 0.
    */

    public void mousePressed(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mousePressed at ("+event.getX()+","+event.getY()+") _dragging="+_dragging);
        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;
        List <Positionable> selections = getSelectedItems(event);
        if (_dragging) {
            return;
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
            if (event.isPopupTrigger()) {
                if (_debug) log.debug("mousePressed calls showPopUp");
                if (event.isMetaDown() || event.isAltDown()){
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    // no possible conflict with moving, display the popup now
                    if (_selectionGroup!=null){
                        //Will show the copy option only
                        showMultiSelectPopUp(event, _currentSelection);
                    } else {
                        showPopUp(_currentSelection, event);
                    }
                }
            } else if (!event.isControlDown()){ 
                _currentSelection.doMousePressed(event);
                if (_selectRect==null)
                    _selectionGroup = null;
            }
        } else {
            if (event.isPopupTrigger()) {
                if (event.isMetaDown() || event.isAltDown()){
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    if (_multiItemCopyGroup!=null){
                        pasteItemPopUp(event);
                    } else if (_selectionGroup!=null) {
                        showMultiSelectPopUp(event, _currentSelection);
                    } else {
                        backgroundPopUp(event);
                        _currentSelection = null;
                    }
                }
            } else {
                _currentSelection = null;
            }
        }
        //if ((event.isControlDown() || _selectionGroup!=null) && _currentSelection!=null){
        if ((event.isControlDown()) || event.isMetaDown() || event.isAltDown()){
            //Don't want to do anything, just want to catch it, so that the next two else ifs are not
            //executed
        } else if (_currentSelection==null || 
                    (_selectRect!=null && !_selectRect.contains(_anchorX, _anchorY))){
                _selectRect = new Rectangle(_anchorX, _anchorY, 0, 0);
                _selectionGroup = null;
        } else {
            _selectRect = null;
            _selectionGroup = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseReleased(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseReleased at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" selectRect is "+(_selectRect==null? "null":"not null"));
        List <Positionable> selections = getSelectedItems(event);

        if (_dragging) {
            mouseDragged(event);
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
        } else {
            if ((event.isPopupTrigger()||delayedPopupTrigger) && !_dragging){
                if (_multiItemCopyGroup!=null)
                    pasteItemPopUp(event);
                else {
                    backgroundPopUp(event);
                    _currentSelection = null;
                }
            } 
            else{
                _currentSelection = null;
                
            }
        }
        /*if (event.isControlDown() && _currentSelection!=null && !event.isPopupTrigger()){
            amendSelectionGroup(_currentSelection, event);*/
        if ((event.isPopupTrigger() || delayedPopupTrigger) && _currentSelection != null && !_dragging) {
            if (_selectionGroup!=null){
                //Will show the copy option only
                showMultiSelectPopUp(event, _currentSelection);
                
            } else {
                showPopUp(_currentSelection, event);
            }
        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseReleased(event);
            }
            if (allPositionable() && _selectRect!=null) {
                if (_selectionGroup==null && _dragging) {
                    makeSelectionGroup();
                }
            }
        }
        delayedPopupTrigger = false;
        _dragging = false;
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseDragged(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if ((event.isPopupTrigger()) || (!event.isMetaDown() && !event.isAltDown())) {
            if (_currentSelection!=null) {
                List <Positionable> selections = getSelectedItems(event);
                if (selections.size() > 0) {
                    if (selections.get(0)!=_currentSelection) {
                        _currentSelection.doMouseReleased(event);
                    }
                } else {
                    _currentSelection.doMouseReleased(event);
                }
            }
            return; 
        }
        if (_currentSelection!=null || _selectionGroup!=null) {
            if (!getFlag(OPTION_POSITION, _currentSelection.isPositionable())) { return; }
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
            if (_selectionGroup!=null) {
                for (int i=0; i<_selectionGroup.size(); i++){
                    moveItem(_selectionGroup.get(i), deltaX, deltaY);
                }
                moveSelectRect(deltaX, deltaY);
                _highlightcomponent = null;
            } else {
                moveItem(_currentSelection, deltaX, deltaY);
                _highlightcomponent = new Rectangle(_currentSelection.getX(), _currentSelection.getY(), _currentSelection.maxWidth(), _currentSelection.maxHeight());
            }
        } else {
            if (allPositionable() && _selectionGroup==null) {
                drawSelectRect(event.getX(), event.getY());
            }
        }
        _dragging = true;
        _lastX = event.getX();
        _lastY = event.getY();
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseMoved(MouseEvent event) {
        //if (_debug) log.debug("mouseMoved at ("+event.getX()+","+event.getY()+")"); 
        if (_dragging || event.isPopupTrigger()) { return; }

        List <Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                selection = selections.get(1); 
            } else {
                selection = selections.get(0); 
            }
        }
        if (isEditable() && selection!=null && selection.getDisplayLevel()>BKG){
            _highlightcomponent = new Rectangle(selection.getX(), selection.getY(), selection.maxWidth(), selection.maxHeight());
            _targetPanel.repaint();
        } else {
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
        if (selection!=null && selection.getDisplayLevel()>BKG && selection.showTooltip()) {
            showToolTip(selection, event);
            //selection.highlightlabel(true);
            _targetPanel.repaint();
        } else {
            setToolTip(null);
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
    }

    public void mouseClicked(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseClicked at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" selectRect is "+(_selectRect==null? "null":"not null"));
        List <Positionable> selections = getSelectedItems(event);

        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
        } else {
            _currentSelection = null;
            if (event.isPopupTrigger()){
                if (_multiItemCopyGroup==null)
                    pasteItemPopUp(event);
                else
                    backgroundPopUp(event);
            }
        }
        if (event.isPopupTrigger() && _currentSelection != null && !_dragging) {
            if (_selectionGroup!=null)
                showMultiSelectPopUp(event, _currentSelection);
            else
                showPopUp(_currentSelection, event);
            //_selectionGroup = null; //Show popup only works for a single item
            
        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseClicked(event);
            }
        }
        _targetPanel.repaint(); // needed for ToolTip
        if (event.isControlDown() && _currentSelection!=null && !event.isPopupTrigger()){
            amendSelectionGroup(_currentSelection);
        }
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint();  // needed for ToolTip
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (_selectionGroup==null) return;
        int x = 0;
        int y = 0;
        for (int i=0; i<_selectionGroup.size(); i++) {
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
            
            moveItem(_selectionGroup.get(i), x, y);
        }
        moveSelectRect(x, y);
        _targetPanel.repaint();
    }

    public void keyReleased(KeyEvent e) {
    }
    
    protected ArrayList <Positionable> _multiItemCopyGroup = null;  // items gathered inside fence
    
    protected void copyItem(Positionable p){
        _multiItemCopyGroup = new ArrayList <Positionable>();
        _multiItemCopyGroup.add(p);
    }
    
    protected void pasteItemPopUp(final MouseEvent event){
        if (!isEditable())
            return;
        if (_multiItemCopyGroup==null)
            return;
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Paste");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { pasteItem(event); }
        });
        setBackgroundMenu(popup);
        popup.add(edit);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    
    protected void backgroundPopUp(MouseEvent event){
        if (!isEditable())
            return;
        JPopupMenu popup = new JPopupMenu();
        setBackgroundMenu(popup);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    
    protected void showMultiSelectPopUp(final MouseEvent event, Positionable p){
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Copy");
        if (p.isPositionable()) {
            setShowAlignmentMenu(p, popup);
        }
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                _multiItemCopyGroup = new ArrayList <Positionable>();
                _multiItemCopyGroup = _selectionGroup;
            }
        });
        setRemoveMenu(p, popup);
        popup.add(edit);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    
    protected boolean pasteItem = false;
    
    protected void pasteItem(MouseEvent e){
        pasteItem = true;
        XmlAdapter adapter;
        String className;
        int x;
        int y;
        int xOrig;
        int yOrig;
        if (_multiItemCopyGroup!=null) {
            JComponent copied;
            int xoffset;
            int yoffset;
            x = _multiItemCopyGroup.get(0).getX();
            y = _multiItemCopyGroup.get(0).getY();
            xoffset=e.getX()-x;
            yoffset=e.getY()-y;
            for(int i = 0; i<_multiItemCopyGroup.size(); i++){
                copied = (JComponent)_multiItemCopyGroup.get(i);
                xOrig = copied.getX();
                yOrig = copied.getY();
                x = xOrig+xoffset;
                y = yOrig+yoffset;
                if (x<0) x=1;
                if (y<0) y=1;
                className=ConfigXmlManager.adapterName(copied);
                copied.setLocation(x, y);
                try{
                    adapter = (XmlAdapter)Class.forName(className).newInstance();
                    Element el = adapter.store(copied);
                    adapter.load(el, this);
                } catch (Exception ex) {
                    log.debug(ex);
                }
                copied.setLocation(xOrig, yOrig);
            }
        }
        pasteItem = false;
        _targetPanel.repaint();
    }
    
    public void setBackgroundMenu(JPopupMenu popup){
        JMenu edit = new JMenu(rb.getString("FontBackgroundColor"));
        makeColorMenu(edit);
        popup.add(edit);
    
    }
        
    protected void makeColorMenu(JMenu colorMenu) {
        ButtonGroup buttonGrp = new ButtonGroup();
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Black"), Color.black);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("DarkGray"),Color.darkGray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Gray"),Color.gray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("LightGray"),Color.lightGray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("White"),Color.white);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Red"),Color.red);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Orange"),Color.orange);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Yellow"),Color.yellow);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Green"),Color.green);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Blue"),Color.blue);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Magenta"),Color.magenta);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Clear"), null);
    }
    
    protected void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
                           final String name, Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            Color desiredColor;
            public void actionPerformed(ActionEvent e) {
                if(desiredColor!=null)
                    setBackgroundColor(desiredColor);
                else
                    clearBackgroundColor();
            }
            ActionListener init (Color c) {
                desiredColor = c;
                return this;
            }
        }.init(color);
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        if (color==null) { color = defaultBackgroundColor; }
        setColorButton(getBackgroundColor(), color, r);
        colorButtonGroup.add(r);
        menu.add(r);
    }
    
    protected void setColorButton(Color color, Color buttonColor, JRadioButtonMenuItem r) {
        if (_debug)
            log.debug("setColorButton: color="+color+" (RGB= "+(color==null?"":color.getRGB())+
                      ") buttonColor= "+buttonColor+" (RGB= "+(buttonColor==null?"":buttonColor.getRGB())+")");
        if (buttonColor!=null) {
            if (color!=null && buttonColor.getRGB() == color.getRGB()) {
                 r.setSelected(true);
            } else r.setSelected(false);
        } else {
            if (color==null)  r.setSelected(true);
            else  r.setSelected(false);
        }
    }
    /*********************** Abstract Methods ************************/

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
    


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Editor.class.getName());
}
