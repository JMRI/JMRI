package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.RosterEntry;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

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
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.4 $
 */

public class LocoIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    private static final String WHITE = "White";		//loco background colors
    private static final String GREEN = "Green";
    private static final String GRAY = "Gray";
    private static final String RED = "Red";
    private static final String BLUE = "Blue";
    private static final String YELLOW = "Yellow";
	
	public LocoIcon() {
        // super ctor call to make sure this is an icon label
    	super(new NamedIcon("resources/icons/markers/loco-white.gif",
                            "resources/icons/markers/loco-white.gif"));
        setDisplayLevel(PanelEditor.MARKERS);
        icon = true;
        text = true;
    }
 
    // update icon as state of marker changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getText() + " " + e.getPropertyName() + " is now "
					+ e.getNewValue());
	}


    boolean enablePopUp = true;
    jmri.jmrit.throttle.ThrottleFrame tf = null;
    /**
     * Pop-up only if right click and not dragged 
     */
    protected void showPopUp(MouseEvent e) {
		ours = this;
		if (enablePopUp) {
			popup = new JPopupMenu();
			if (entry != null) {
				popup.add(new AbstractAction("Throttle") {
					public void actionPerformed(ActionEvent e) {
						tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
						tf.notifyAddressChosen(entry.getDccLocoAddress().getNumber(), entry.getDccLocoAddress().isLongAddress());
						tf.setVisible(true);
					}
				});
			}
			popup.add(makeLocoIconMenu());
			popup.add(makeFontSizeMenu());
			popup.add(makeFontStyleMenu());
			popup.add(makeFontColorMenu());

			popup.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					remove();
					dispose();
				}
			});

			// end creation of pop-up menu

			popup.show(e.getComponent(), e.getX(), e.getY());
		} else
			enablePopUp = true;
	}
    
    ButtonGroup locoButtonGroup = null;
    
    protected JMenu makeLocoIconMenu(){
    	JMenu iconMenu = new JMenu("Loco color");
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
    		super.setIcon (white);
    		setForeground (Color.black);
    	}
    	if(color.equals(GREEN)){
    		super.setIcon (green);
    		setForeground (Color.black);
    	}
    	if(color.equals(GRAY)){
    		super.setIcon (gray);
    		setForeground (Color.white);
    	}
    	if(color.equals(RED)){
    		super.setIcon (red);
    		setForeground (Color.white);
    	}
    	if(color.equals(BLUE)){
    		super.setIcon (blue);
    		setForeground (Color.white);
    	}
    	if(color.equals(YELLOW)){
    		super.setIcon (yellow);
    		setForeground (Color.black);
    	}
    }
    
    public String[] getLocoColors(){
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
    
    public void mouseDragged(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse dragged event
		if (layoutPanel!=null) {
			layoutPanel.handleMouseDragged(e,getX(),getY());
			return;
		}
    	enablePopUp = false;
    	super.setPositionable(true);
    	super.mouseDragged(e);
    }
 
    /**
     * Update the marker when the icon is clicked
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        log.debug("No loco connection, can't process click");
     }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIcon.class.getName());
}
