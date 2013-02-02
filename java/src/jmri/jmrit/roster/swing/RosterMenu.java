// RosterMenu.java

package jmri.jmrit.roster.swing;

import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import jmri.jmrit.roster.CopyRosterItemAction;
import jmri.jmrit.roster.DeleteRosterItemAction;
import jmri.jmrit.roster.ExportRosterItemAction;
import jmri.jmrit.roster.FullBackupExportAction;
import jmri.jmrit.roster.ImportRosterItemAction;
import jmri.jmrit.roster.PrintRosterAction;

/**
 * Provides a context-specific menu for handling the Roster.
 * <P>
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2008
 * @author  Dennis Miller  Copyright (C) 2005
 * @version	$Revision$
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
     * Create a menu of Roster tools.
     * @param pMenuName Name for the menu
     * @param pMenuType Select where the menu will be used, hence the
     *                  right set of items to be enabled.
     * @param pWho      The Component using this menu, used to ensure that
     *                  dialog boxes will pop in the right place.
     */
    public RosterMenu(String pMenuName, int pMenuType, Component pWho) {
        super(pMenuName);

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

        // create the menu

        AbstractAction dp3Action = new jmri.jmrit.roster.swing.RosterFrameAction(rb.getString("MenuItemRoster"), false);
        dp3Action.setEnabled(true);

        AbstractAction createAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneNewProgAction(rb.getString("MenuItemCreate"));
        createAction.setEnabled(false);

        AbstractAction editAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneEditAction(rb.getString("MenuItemEdit"));
        editAction.setEnabled(false);

        AbstractAction importAction = new ImportRosterItemAction(rb.getString("MenuItemImport"), pWho);
        importAction.setEnabled(false);

        AbstractAction exportAction = new ExportRosterItemAction(rb.getString("MenuItemExport"), pWho);
        exportAction.setEnabled(false);

        AbstractAction copyAction = new CopyRosterItemAction(rb.getString("MenuItemCopy"), pWho);
        copyAction.setEnabled(false);

        AbstractAction deleteAction = new DeleteRosterItemAction(rb.getString("MenuItemDelete"), pWho);
        deleteAction.setEnabled(false);
        
        AbstractAction deleteGroupAction = new DeleteRosterGroupAction(rb.getString("MenuGroupDelete"), pWho);
        deleteGroupAction.setEnabled(false);
        
        AbstractAction createGroupAction = new CreateRosterGroupAction(rb.getString("MenuGroupCreate"), pWho);
        createGroupAction.setEnabled(false);
        
        AbstractAction rosterEntryToGroupAction = new RosterEntryToGroupAction(rb.getString("MenuGroupAssociate"), pWho);
        rosterEntryToGroupAction.setEnabled(false);
        
        AbstractAction removeRosterEntryToGroupAction = new RemoveRosterEntryToGroupAction(rb.getString("MenuGroupDisassociate"), pWho);
        removeRosterEntryToGroupAction.setEnabled(false);

        AbstractAction rosterGroupTableAction = new jmri.jmrit.roster.swing.rostergroup.RosterGroupTableAction(rb.getString("MenuGroupTable"));
        rosterGroupTableAction.setEnabled(false); 
       
        AbstractAction rosterExportAction = new FullBackupExportAction(rb.getString("MenuFullExport"), pWho);
        rosterExportAction.setEnabled(false);

        // Need a frame here, but are not passed one
        Frame newFrame = new Frame();
        AbstractAction printAction = new PrintRosterAction(rb.getString("MenuItemPrint"), newFrame, false);
        printAction.setEnabled(false);
        AbstractAction previewAction = new PrintRosterAction(rb.getString("MenuItemPreview"), newFrame, true);
        printAction.setEnabled(false);
        
        JMenu groupMenu = new JMenu(rb.getString("MenuRosterGroups"));
        groupMenu.add(createGroupAction);
        groupMenu.add(deleteGroupAction);
        groupMenu.add(rosterGroupTableAction);
        groupMenu.add(rosterEntryToGroupAction);
        groupMenu.add(removeRosterEntryToGroupAction);

        
        add(dp3Action);
        addSeparator();
        add(createAction);
        add(editAction);
        add(copyAction);
        add(importAction);
        add(exportAction);
        add(deleteAction);
        add(printAction);
        add(previewAction);
        addSeparator();
        add(groupMenu);
        addSeparator();
        add(rosterExportAction);

        // activate the right items
        switch (pMenuType) {
            case MAINMENU:
                createAction.setEnabled(true);
                editAction.setEnabled(true);
                deleteAction.setEnabled(true);
                importAction.setEnabled(true);
                exportAction.setEnabled(true);
                copyAction.setEnabled(true);
                deleteGroupAction.setEnabled(true);
                createGroupAction.setEnabled(true);
                rosterEntryToGroupAction.setEnabled(true);
                removeRosterEntryToGroupAction.setEnabled(true);
                printAction.setEnabled(true);
                previewAction.setEnabled(true);
                rosterGroupTableAction.setEnabled(true);
                rosterExportAction.setEnabled(true);
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
    static Logger log = Logger.getLogger(RosterMenu.class.getName());

}
