package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.TrackerTableAction;
import jmri.jmrit.roster.RosterEntry;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
//import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * An icon that displays the position of a loco on a panel.<P>
 * The icon can always be repositioned and its popup menu is
 * always active.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */

public class LocoIcon extends PositionableLabel {

    public static final String WHITE = "White";		//loco background colors
    public static final String GREEN = "Green";
    public static final String GRAY = "Gray";
    public static final String RED = "Red";
    public static final String BLUE = "Blue";
    public static final String YELLOW = "Yellow";
    
    private int _dockX = 0;
    private int _dockY = 0;
    private Color _locoColor;
	
	public LocoIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
    	super(new NamedIcon("resources/icons/markers/loco-white.gif",
                            "resources/icons/markers/loco-white.gif"), editor);
    	_locoColor = Color.WHITE;
        setDisplayLevel(Editor.MARKERS);
        setShowTooltip(false);
        //setEditable(false);
        _text = true;	//Markers are an icon with text
        setPopupUtility(new PositionablePopupUtil(this, this) {       // need this class for Font Edit
            public void setFixedTextMenu(JPopupMenu popup) {}
            public void setTextMarginMenu(JPopupMenu popup) {}
            public void setTextBorderMenu(JPopupMenu popup) {}
            public void setTextJustificationMenu(JPopupMenu popup) {}
        });
    }
	
    public Positionable deepClone() {
        LocoIcon pos = new LocoIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        LocoIcon pos = (LocoIcon)p;
        if (entry!=null) {
            pos.setRosterEntry(getRosterEntry());       	
        }
    	pos.setText(getText());
        return super.finishClone(pos);
    }

	// Marker tool tips are always disabled
	public void setShowTooltip(boolean set){super.setShowTooltip(false);}

    // Markers are always positionable 
    public void setPositionable(boolean enabled) {super.setPositionable(true);}
    
    // Markers always have a popup menu
    public boolean doViemMenu() {
        return false;
    }
    
    jmri.jmrit.throttle.ThrottleFrame tf = null;
    
    /**
     * Pop-up only if right click and not dragged 
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (entry != null) {
            popup.add(new AbstractAction("Throttle") {
                public void actionPerformed(ActionEvent e) {
                    tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
                    tf.getAddressPanel().setRosterEntry(entry);
                    tf.toFront();
                }
            });
        }
        popup.add(makeLocoIconMenu());
        if (isEditable()) {
            getEditor().setShowAlignmentMenu(this, popup);
            getEditor().setShowCoordinatesMenu(this, popup);
            popup.add(makeDockingMenu());
            popup.add(makeDockMenu());
            getPopupUtility().setTextFontMenu(popup);
        } else {
        	setRotateMenu(popup);
            if (entry==null) {
                setTextEditMenu(popup);
            }
            popup.add(makeDockMenu());
            getPopupUtility().setTextFontMenu(popup);
            getEditor().setRemoveMenu(this, popup);
        }
        return true;
	}
    
    ButtonGroup locoButtonGroup = null;
    
    protected JMenu makeLocoIconMenu(){
    	JMenu iconMenu = new JMenu(Bundle.getMessage("LocoColor"));
    	locoButtonGroup = new ButtonGroup();
    	String[] colors = getLocoColors();
    	for (int i=0; i<colors.length; i++){
    		addLocoMenuEntry(iconMenu, colors[i]);
    	}
     	return iconMenu;
    }
    
    // loco icons
    NamedIcon white = new NamedIcon("resources/icons/markers/loco-white.gif",
                                     "resources/icons/markers/loco-white.gif");
    NamedIcon green = new NamedIcon("resources/icons/markers/loco-green.gif",
    								"resources/icons/markers/loco-green.gif");
    NamedIcon gray = new NamedIcon("resources/icons/markers/loco-gray.gif",
    								"resources/icons/markers/loco-gray.gif");
    NamedIcon red = new NamedIcon("resources/icons/markers/loco-red.gif",
    								"resources/icons/markers/loco-red.gif");
    NamedIcon blue = new NamedIcon("resources/icons/markers/loco-blue.gif",
									"resources/icons/markers/loco-blue.gif");
    NamedIcon yellow = new NamedIcon("resources/icons/markers/loco-yellow.gif",
									"resources/icons/markers/loco-yellow.gif");
    
    
    public void addLocoMenuEntry (JMenu iconMenu, final String color){
    	JRadioButtonMenuItem r = new JRadioButtonMenuItem(color);
    	r.addActionListener(new ActionListener() {
    		final String desiredColor = color;
            public void actionPerformed(ActionEvent e) { setLocoColor(desiredColor); }
        });
    	locoButtonGroup.add(r);
    	iconMenu.add(r);
    }
    
    public void setLocoColor(String color){
    	log.debug("Set loco color to " + color);
    	if(color.equals(WHITE)){
    		super.updateIcon (white);
        	_locoColor = Color.WHITE;   	
    		setForeground (Color.black);
    	}
    	if(color.equals(GREEN)){
    		super.updateIcon (green);
        	_locoColor = Color.GREEN;   	
    		setForeground (Color.black);
    	}
    	if(color.equals(GRAY)){
    		super.updateIcon (gray);
        	_locoColor = Color.GRAY;   	
    		setForeground (Color.white);
    	}
    	if(color.equals(RED)){
    		super.updateIcon (red);
        	_locoColor = Color.RED;   	
    		setForeground (Color.white);
    	}
    	if(color.equals(BLUE)){
    		super.updateIcon (blue);
        	_locoColor = Color.BLUE;   	
    		setForeground (Color.white);
    	}
    	if(color.equals(YELLOW)){
    		super.updateIcon (yellow);
        	_locoColor = Color.YELLOW;   	
    		setForeground (Color.black);
    	}
    }
    
    public static String[] getLocoColors(){
    	String[] colors = {WHITE,GREEN,GRAY,RED,BLUE,YELLOW};
    	return colors;
    }
    
    protected RosterEntry entry = null;
    
    public void setRosterEntry (RosterEntry entry){
    	this.entry = entry;
    }
    
    public RosterEntry getRosterEntry (){
    	return entry;
    }
    
    protected JMenuItem makeDockingMenu() {
    	JMenuItem dockingMenu = new JMenuItem(Bundle.getMessage("setDockingLocation"));
    	dockingMenu.addActionListener(new ActionListener() {
    		Editor ed;
    		LocoIcon loco;
    		ActionListener init(Editor e, LocoIcon l) {
    			ed = e;
    			loco =l;
    			return this;
    		}
            public void actionPerformed(ActionEvent e) {
            	ed.setSelectionsDockingLocation(loco); 
            }
        }.init(getEditor(), this));
	return dockingMenu;
}
    public void setDockingLocation(int x, int y) {
       	_dockX = x;
       	_dockY = y;
    }
    
    public int getDockX() {
    	return _dockX;
    }
    public int getDockY() {
    	return _dockY;
    }
    
    public void dock() {
    	setLocation(_dockX, _dockY);
    }

    protected JMenuItem makeDockMenu() {
    	JMenuItem dockMenu = new JMenuItem(Bundle.getMessage("dockIcon"));
    	dockMenu.addActionListener(new ActionListener() {
    		Editor ed;
    		LocoIcon loco;
    		ActionListener init(Editor e, LocoIcon l) {
    			ed = e;
    			loco =l;
    			return this;
    		}
           public void actionPerformed(ActionEvent e) {
            	ed.dockSelections(loco); }
        }.init(getEditor(), this));
    	return dockMenu;
    }

    public void doMouseReleased(MouseEvent event) {
    	List <Positionable> selections = _editor.getSelectedItems(event);
    	if (selections==null) {
    		return;
    	}
    	for (int i=0; i<selections.size(); i++) {
    		if (selections.get(i) instanceof IndicatorTrack) {
    			IndicatorTrack t = (IndicatorTrack)selections.get(i);
    			jmri.jmrit.logix.OBlock block = t.getOccBlock();
    			if (block!=null) {
    				block.setMarkerForeground(getForeground());
    				block.setMarkerBackground(_locoColor);
        			if (TrackerTableAction.markNewTracker(block, getText())) {
        				dock();
//        				remove();    				
        			}
        		}
    			break;
    		}
    	}
    }

    static Logger log = LoggerFactory.getLogger(LocoIcon.class.getName());
}
