//TrainsScheduleEditAction.java

package jmri.jmrit.operations.trains;


import java.awt.event.*;

import javax.swing.*;


/**
 * Action to edit timetable
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision$
 */
public class TrainsScheduleEditAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1796097519377642811L;

	public TrainsScheduleEditAction(){
		super(Bundle.getMessage("MenuItemEditSchedule"));
	}
	
	 public void actionPerformed(ActionEvent e) {
		new TrainsScheduleEditFrame();
	 }
	
}