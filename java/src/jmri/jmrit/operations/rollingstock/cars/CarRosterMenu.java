package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import jmri.jmrit.operations.rollingstock.cars.tools.DeleteCarRosterAction;
import jmri.jmrit.operations.rollingstock.cars.tools.ExportCarRosterAction;
import jmri.jmrit.operations.rollingstock.cars.tools.ImportCarRosterAction;
import jmri.jmrit.operations.rollingstock.cars.tools.PrintCarRosterAction;
import jmri.jmrit.operations.rollingstock.cars.tools.ResetCarMovesAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a context-specific menu for handling the Roster.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007, 2012, 2016
 *
 */
public class CarRosterMenu extends JMenu {

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
     *            of items to be enabled.
     * @param carsTableFrame The Component using this menu, used to ensure that
     *            dialog boxes will pop in the right place.
     */
    public CarRosterMenu(String pMenuName, int pMenuType, CarsTableFrame carsTableFrame) {
        super(pMenuName);

        // create the menu
        AbstractAction importAction = new ImportCarRosterAction(Bundle.getMessage("MenuItemImport"),
                carsTableFrame);
        importAction.setEnabled(false);
        AbstractAction exportAction = new ExportCarRosterAction(Bundle.getMessage("MenuItemExport"),
                carsTableFrame);
        exportAction.setEnabled(false);
        AbstractAction deleteAction = new DeleteCarRosterAction(carsTableFrame);
        deleteAction.setEnabled(false);
        AbstractAction resetMovesAction = new ResetCarMovesAction(
                Bundle.getMessage("MenuItemResetMoves"), carsTableFrame);
        resetMovesAction.setEnabled(false);

        AbstractAction printAction = new PrintCarRosterAction(Bundle.getMessage("MenuItemPrint"),
                false, carsTableFrame);
        printAction.setEnabled(false);
        AbstractAction previewAction = new PrintCarRosterAction(
                Bundle.getMessage("MenuItemPreview"), true, carsTableFrame);
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
            case ENTRYMENU:
                printAction.setEnabled(true);
                previewAction.setEnabled(true);
                break;
            default:
                log.error("RosterMenu constructed without a valid menuType parameter: " + pMenuType);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CarRosterMenu.class
            .getName());

}
