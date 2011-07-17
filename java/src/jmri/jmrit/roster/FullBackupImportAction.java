package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class FullBackupImportAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private Component _who;

	/**
	 * @param s
	 *            Name of this action, e.g. in menus
	 * @param who
	 *            Component that action is associated with, used to ensure
	 *            proper position in of dialog boxes
	 */
	public FullBackupImportAction(String s, Component who) {
		super(s);
		//_who = who;
	}

	public void actionPerformed(ActionEvent e) {
	}
}
