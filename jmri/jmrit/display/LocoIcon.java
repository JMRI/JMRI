package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.RosterEntry;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * An icon that displays the position of a loco on a panel.<P>
 * The icon can always be repositioned and its popup menu is
 * always active.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.10 $
 */

public class LocoIcon extends PositionableLabel {

    private static final String WHITE = "White";		//loco background colors
    private static final String GREEN = "Green";
    private static final String GRAY = "Gray";
    private static final String RED = "Red";
    private static final String BLUE = "Blue";
    private static final String YELLOW = "Yellow";
	
	public LocoIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
    	super(new NamedIcon("resources/icons/markers/loco-white.gif",
                            "resources/icons/markers/loco-white.gif"), editor);
        setDisplayLevel(Editor.MARKERS);
        setShowTooltip(false);
        _text = true;	//Markers are an icon with text
    }

    // Markers are always positionable 
    public void setPositionable(boolean enabled) {super.setPositionable(true);}
    
    // Markers always have a popup menu
    public void setEditable(boolean enabled) {super.setEditable(true);}
    
    jmri.jmrit.throttle.ThrottleFrame tf = null;
    
    public void doMouseReleased(MouseEvent e) {
    	if (e.isPopupTrigger()){
    		if(log.isDebugEnabled())
    			log.debug("mouse released create mini locoicon popup menu");
    		JPopupMenu popup = new JPopupMenu();
    		showPopUp(popup);
    		setRemoveMenu(popup);
            popup.show(this, this.getWidth()/2, this.getHeight()/2);
    	}  	
    }
    
    /**
     * Pop-up only if right click and not dragged 
     */
    public void showPopUp(JPopupMenu popup) {
        super.showPopUp(popup);

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
	}
    
    ButtonGroup locoButtonGroup = null;
    
    protected JMenu makeLocoIconMenu(){
    	JMenu iconMenu = new JMenu(rb.getString("LocoColor"));
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
    
    public static String[] getLocoColors(){
    	String[] colors = {WHITE,GREEN,GRAY,RED,BLUE,YELLOW};
    	return colors;
    }
    
    private void setRemoveMenu(JPopupMenu popup) {
    	popup.add(new AbstractAction(rb.getString("Remove")) {
    		public void actionPerformed(ActionEvent e) { 
    			remove();
    		}
    	});
    }
                  
    protected RosterEntry entry = null;
    
    public void setRosterEntry (RosterEntry entry){
    	this.entry = entry;
    }
    
    public RosterEntry getRosterEntry (){
    	return entry;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIcon.class.getName());
}
