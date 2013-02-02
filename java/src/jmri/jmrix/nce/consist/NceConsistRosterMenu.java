// NceConsistRosterMenu.java

package jmri.jmrix.nce.consist;

import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

/**
 * Provides a context-specific menu for handling the Roster.
 * <P>
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007
 * @version	$Revision$
 * @see NceConsistRosterEntry
 * @see NceConsistRoster
 */
public class NceConsistRosterMenu extends JMenu {

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
    public NceConsistRosterMenu(String pMenuName, int pMenuType, Component pWho) {
        super(pMenuName);

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

        // create the menu

 //       AbstractAction importAction = new ImportRosterItemAction(rb.getString("MenuItemImport"), pWho);
 //       importAction.setEnabled(false);

 //       AbstractAction exportAction = new ExportRosterItemAction(rb.getString("MenuItemExport"), pWho);
 //       exportAction.setEnabled(false);

 //       AbstractAction copyAction = new CopyRosterItemAction(rb.getString("MenuItemCopy"), pWho);
 //       copyAction.setEnabled(false);

 //       AbstractAction deleteAction = new DeleteRosterItemAction(rb.getString("MenuItemDelete"), pWho);
 //       deleteAction.setEnabled(false);

        // Need a frame here, but are not passed one
        Frame newFrame = new Frame();
        AbstractAction printAction = new PrintNceConsistRosterAction(rb.getString("MenuItemPrint"), newFrame, false);
        printAction.setEnabled(false);
        AbstractAction previewAction = new PrintNceConsistRosterAction(rb.getString("MenuItemPreview"), newFrame, true);
        printAction.setEnabled(false);
//        add(copyAction);
//        add(importAction);
//        add(exportAction);
//        add(deleteAction);
        add(printAction);
        add(previewAction);

        // activate the right items
        switch (pMenuType) {
            case MAINMENU:
//                deleteAction.setEnabled(true);
//                importAction.setEnabled(true);
//                exportAction.setEnabled(true);
//                copyAction.setEnabled(true);
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
                log.error("RosterMenu constructed without a valid menuType parameter: "
                            +pMenuType);
            }
    }

	// initialize logging
    static Logger log = Logger.getLogger(NceConsistRosterMenu.class.getName());

}
