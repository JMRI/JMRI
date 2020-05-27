package jmri.jmrit.operations.rollingstock.engines;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.rollingstock.engines.tools.*;

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
        AbstractAction importRosterAction = new ImportRosterEngineAction();
        AbstractAction exportAction = new ExportEngineRosterAction();
        AbstractAction importAction = new ImportEngineAction();
        AbstractAction deleteAction = new DeleteEngineRosterAction();
        AbstractAction resetMovesAction = new ResetEngineMovesAction();
        AbstractAction printAction = new PrintEngineRosterAction(false, pWho);
        AbstractAction previewAction = new PrintEngineRosterAction(true, pWho);

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
                log.error("RosterMenu constructed without a valid menuType parameter: {}", pMenuType);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EngineRosterMenu.class.getName());

}
