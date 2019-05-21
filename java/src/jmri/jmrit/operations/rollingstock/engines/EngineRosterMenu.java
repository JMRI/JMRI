package jmri.jmrit.operations.rollingstock.engines;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import jmri.jmrit.operations.rollingstock.engines.tools.DeleteEngineRosterAction;
import jmri.jmrit.operations.rollingstock.engines.tools.ExportEngineRosterAction;
import jmri.jmrit.operations.rollingstock.engines.tools.ImportEngineAction;
import jmri.jmrit.operations.rollingstock.engines.tools.ImportRosterEngineAction;
import jmri.jmrit.operations.rollingstock.engines.tools.PrintEngineRosterAction;
import jmri.jmrit.operations.rollingstock.engines.tools.ResetEngineMovesAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a context-specific menu for handling the Roster.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007, 2012
 */
public class EngineRosterMenu extends JMenu {

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
     * Creates a roster menu for locomotives.
     *
     * @param pMenuName Name for the menu
     * @param pMenuType Select where the menu will be used, hence the right set
     *            of items to be enabled.
     * @param pWho The Component using this menu, used to ensure that dialog
     *            boxes will pop in the right place.
     */
    public EngineRosterMenu(String pMenuName, int pMenuType, EnginesTableFrame pWho) {
        super(pMenuName);

        // create the menu
        AbstractAction importRosterAction = new ImportRosterEngineAction(Bundle.getMessage("MenuItemImportRoster"));
        AbstractAction exportAction = new ExportEngineRosterAction(Bundle.getMessage("MenuItemExport"));
        AbstractAction importAction = new ImportEngineAction(Bundle.getMessage("MenuItemImport"));
        AbstractAction deleteAction = new DeleteEngineRosterAction(Bundle.getMessage("MenuItemDelete"));
        AbstractAction resetMovesAction = new ResetEngineMovesAction(Bundle.getMessage("MenuItemResetMoves"));
        AbstractAction printAction = new PrintEngineRosterAction(Bundle.getMessage("MenuItemPrint"), false, pWho);
        AbstractAction previewAction = new PrintEngineRosterAction(Bundle.getMessage("MenuItemPreview"), true, pWho);

        add(importRosterAction);
        add(importAction);
        add(exportAction);
        add(deleteAction);
        add(resetMovesAction);
        add(printAction);
        add(previewAction);

        // activate the right items
        switch (pMenuType) {
            case MAINMENU:
            case SELECTMENU:
            case ENTRYMENU:
                break;
            default:
                log.error("RosterMenu constructed without a valid menuType parameter: " + pMenuType);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EngineRosterMenu.class.getName());

}
