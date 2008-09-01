package jmri.jmrit.operations.trains;

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

import jmri.jmrit.display.LocoIcon;

/**
 * An icon that displays the position of a loco on a panel.<P>
 * The icon can always be repositioned and its popup menu is
 * always active.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */

public class TrainIcon extends LocoIcon {

 
	public TrainIcon() {
        // super ctor call to make sure this is an icon label
    	super(); 
    }
 
    boolean enablePopUp = true;
    jmri.jmrit.throttle.ThrottleFrame tf = null;
    /**
     * Pop-up only if right click and not dragged 
     */
    protected void showPopUp(MouseEvent e) {
		//ours = this;
		if (enablePopUp) {
			JPopupMenu popup = new JPopupMenu();
			if (train != null){
				popup.add(new AbstractAction("Move") {
					public void actionPerformed(ActionEvent e) {
						train.move();
					}
				});
				popup.add(new AbstractAction("Set X&Y") {
					public void actionPerformed(ActionEvent e) {
						train.setTrainIconCordinates();
					}
				});
			}
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
    
   
    
    Train train = null;
    
    public void setTrain (Train train){
    	this.train = train;
    }
    
    public Train getTrain (){
    	return train;
    }
    
 
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIcon.class.getName());
}
