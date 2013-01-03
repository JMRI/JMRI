// CarRosterMenu.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Component;
import java.awt.Frame;
import javax.swing.AbstractAction;
import javax.swing.JMenu;

/**
 * Provides a context-specific menu for handling the Roster.
 * <P>
 * 
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007, 2012
 * @version $Revision$
 * 
 */
public class CarRosterMenu extends JMenu {

	/**
	 * Ctor argument defining that the menu object will be used as part of the main menu of the program, away from any
	 * GUI that can select or use a RosterEntry.
	 */
	static public final int MAINMENU = 1;

	/**
	 * Ctor argument defining that the menu object will be used as a menu on a GUI object that can select a RosterEntry.
	 */
	static public final int SELECTMENU = 2;

	/**
	 * Ctor argument defining that the menu object will be used as a menu on a GUI object that is dealing with a single
	 * RosterEntry.
	 */
	static public final int ENTRYMENU = 3;

	/**
	 * Create a
	 * 
	 * @param pMenuName
	 *            Name for the menu
	 * @param pMenuType
	 *            Select where the menu will be used, hence the right set of items to be enabled.
	 * @param pWho
	 *            The Component using this menu, used to ensure that dialog boxes will pop in the right place.
	 */
	public CarRosterMenu(String pMenuName, int pMenuType, Component pWho) {
		super(pMenuName);

		// create the menu

		AbstractAction importAction = new ImportCarRosterAction(Bundle.getMessage("MenuItemImport"),
				pWho);
		importAction.setEnabled(false);
		AbstractAction exportAction = new ExportCarRosterAction(Bundle.getMessage("MenuItemExport"),
				pWho);
		exportAction.setEnabled(false);
		AbstractAction deleteAction = new DeleteCarRosterAction(Bundle.getMessage("MenuItemDelete"),
				pWho);
		deleteAction.setEnabled(false);
		AbstractAction resetMovesAction = new ResetCarMovesAction(
				Bundle.getMessage("MenuItemResetMoves"), pWho);
		resetMovesAction.setEnabled(false);

		// Need a frame here, but are not passed one
		Frame newFrame = new Frame();
		AbstractAction printAction = new PrintCarRosterAction(Bundle.getMessage("MenuItemPrint"),
				newFrame, false, pWho);
		printAction.setEnabled(false);
		AbstractAction previewAction = new PrintCarRosterAction(
				Bundle.getMessage("MenuItemPreview"), newFrame, true, pWho);
		previewAction.setEnabled(false);
		add(importAction);
		add(exportAction);
		add(deleteAction);
		add(resetMovesAction);
		add(printAction);
		add(previewAction);

		// activate the right items
		switch (pMenuType) {
		case MAINMENU:
			importAction.setEnabled(true);
			exportAction.setEnabled(true);
			deleteAction.setEnabled(true);
			resetMovesAction.setEnabled(true);
			printAction.setEnabled(true);
			previewAction.setEnabled(true);
			break;
		case SELECTMENU:
			printAction.setEnabled(true);
			previewAction.setEnabled(true);
			break;
		case ENTRYMENU:
			printAction.setEnabled(true);
			previewAction.setEnabled(true);
			break;
		default:
			log.error("RosterMenu constructed without a valid menuType parameter: " + pMenuType);
		}
	}

	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarRosterMenu.class
			.getName());

}
