package jmri.jmrix.nce.consist;

import java.awt.Component;
import java.awt.Frame;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a context-specific menu for handling the Roster.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007
 * @see NceConsistRosterEntry
 * @see NceConsistRoster
 */
public class NceConsistRosterMenu extends JMenu {

    /**
     * Ctor argument defining that the menu object will be used as part of the
     * main menu of the program, away from any GUI that can select or use a
     * RosterEntry.
     */
    static public final int MAINMENU = 1;

    /**
     * Ctor argument defining that the menu object will be used as a menu on a
     * GUI object that can select a RosterEntry.
     */
    static public final int SELECTMENU = 2;

    /**
     * Ctor argument defining that the menu object will be used as a menu on a
     * GUI object that is dealing with a single RosterEntry.
     */
    static public final int ENTRYMENU = 3;

    /**
     * Create a
     *
     * @param pMenuName Name for the menu
     * @param pMenuType Select where the menu will be used, hence the right set
     *                  of items to be enabled.
     * @param pWho      The Component using this menu, used to ensure that
     *                  dialog boxes will pop in the right place.
     */
    public NceConsistRosterMenu(String pMenuName, int pMenuType, Component pWho) {
        super(pMenuName);

        // create the menu
        //       AbstractAction importAction = new ImportRosterItemAction(Bundle.getMessage("MenuItemImport"), pWho);
        //       importAction.setEnabled(false);
        //       AbstractAction exportAction = new ExportRosterItemAction(Bundle.getMessage("MenuItemExport"), pWho);
        //       exportAction.setEnabled(false);
        //       AbstractAction copyAction = new CopyRosterItemAction(Bundle.getMessage("MenuItemCopy"), pWho);
        //       copyAction.setEnabled(false);
        //       AbstractAction deleteAction = new DeleteRosterItemAction(Bundle.getMessage("MenuItemDelete"), pWho);
        //       deleteAction.setEnabled(false);
        // Need a frame here, but are not passed one
        Frame newFrame = new Frame();
        AbstractAction printAction = new PrintNceConsistRosterAction(Bundle.getMessage("MenuItemPrintSummary"), newFrame, false);
        printAction.setEnabled(false);
        AbstractAction previewAction = new PrintNceConsistRosterAction(Bundle.getMessage("MenuItemPreviewSummary"), newFrame, true);
        printAction.setEnabled(false);
//        add(copyAction);
//        add(importAction);
//        add(exportAction);
//        add(deleteAction);
        add(printAction);
        add(previewAction);

        // activate the correct items (currently all identical)
        switch (pMenuType) {
            case MAINMENU:
//                deleteAction.setEnabled(true);
//                importAction.setEnabled(true);
//                exportAction.setEnabled(true);
//                copyAction.setEnabled(true);
            case SELECTMENU:
            case ENTRYMENU:
                printAction.setEnabled(true);
                previewAction.setEnabled(true);
                break;
            default:
                log.error("RosterMenu constructed without a valid menuType parameter: {}", pMenuType);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NceConsistRosterMenu.class);

}
