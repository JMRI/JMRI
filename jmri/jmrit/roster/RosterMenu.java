// RosterMenu.java

package jmri.jmrit.roster;

import java.awt.*;
import javax.swing.*;

/**
 * Provides a context-specific menu for handling the Roster.
 * <P>
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.4 $
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class RosterMenu extends JMenu {

    /**
     * Ctor argument defining that the menu object will be
     * used as part of the main menu of the program, away from
     * any GUI that can select or use a RosterEntry.
     */
    static public final int MAINMENU = 1;

    /**
     * Ctor argument defining that the menu object will be
     * used as a menu on a GUI object that can select
     * a RosterEntry.
     */
    static public final int SELECTMENU =2;

    /**
     * Ctor argument defining that the menu object will
     * be used as a menu on a GUI object that is dealing with
     * a single RosterEntry.
     */
    static public final int ENTRYMENU = 3;

    /**
     * Create a
     * @param pMenuName Name for the menu
     * @param pMenuType Select where the menu will be used, hence the
     *                  right set of items to be enabled.
     * @param pWho      The Component using this menu, used to ensure that
     *                  dialog boxes will pop in the right place.
     */
    public RosterMenu(String pMenuName, int pMenuType, Component pWho) {
        super(pMenuName);

        // create the menu

        AbstractAction importAction = new ImportRosterItemAction("Import ...", pWho);
        importAction.setEnabled(false);

        AbstractAction exportAction = new ExportRosterItemAction("Export ...", pWho);
        exportAction.setEnabled(false);

        AbstractAction copyAction = new CopyRosterItemAction("Copy ...", pWho);
        copyAction.setEnabled(false);

        AbstractAction deleteAction = new DeleteRosterItemAction("Delete ...", pWho);
        deleteAction.setEnabled(false);

        // Need a frame here, but are not passed one
        Frame newFrame = new Frame();
        AbstractAction printAction = new PrintRosterAction("Print ...", newFrame);
        printAction.setEnabled(false);

        add(copyAction);
        add(importAction);
        add(exportAction);
        add(deleteAction);
        add(printAction);

        // activate the right items
        switch (pMenuType) {
            case MAINMENU:
                deleteAction.setEnabled(true);
                importAction.setEnabled(true);
                exportAction.setEnabled(true);
                copyAction.setEnabled(true);
                printAction.setEnabled(true);
                break;
            case SELECTMENU:
                printAction.setEnabled(true);
                break;
            case ENTRYMENU:
                printAction.setEnabled(true);
                break;
            default:
                log.error("RosterMenu constructed without a valid menuType parameter: "
                            +pMenuType);
            }
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterMenu.class.getName());

}
