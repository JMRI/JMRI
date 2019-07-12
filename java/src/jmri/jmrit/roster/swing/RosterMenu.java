package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.Frame;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import jmri.jmrit.roster.CopyRosterItemAction;
import jmri.jmrit.roster.DeleteRosterItemAction;
import jmri.jmrit.roster.ExportRosterItemAction;
import jmri.jmrit.roster.FullBackupExportAction;
import jmri.jmrit.roster.FullBackupImportAction;
import jmri.jmrit.roster.ImportRosterItemAction;
import jmri.jmrit.roster.PrintListAction;
import jmri.jmrit.roster.PrintRosterAction;
import jmri.jmrit.roster.swing.speedprofile.SpeedProfileAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a context-specific menu for handling the Roster.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008
 * @author Dennis Miller Copyright (C) 2005
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class RosterMenu extends JMenu {

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
     * Create a menu of Roster tools.
     *
     * @param pMenuName Name for the menu
     * @param pMenuType Select where the menu will be used, hence the right set
     *                  of items to be enabled.
     * @param pWho      The Component using this menu, used to ensure that
     *                  dialog boxes will pop in the right place.
     */
    public RosterMenu(String pMenuName, int pMenuType, Component pWho) {
        super(pMenuName);

        // create the menu
        AbstractAction dp3Action = new jmri.jmrit.roster.swing.RosterFrameAction(Bundle.getMessage("MenuItemRoster"), false);
        dp3Action.setEnabled(true);

        AbstractAction createAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneNewProgAction(Bundle.getMessage("MenuItemCreate"));
        createAction.setEnabled(false);

        AbstractAction editAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneEditAction(Bundle.getMessage("MenuItemEdit"));
        editAction.setEnabled(false);

        AbstractAction exportAction = new ExportRosterItemAction(Bundle.getMessage("MenuItemExport"), pWho);
        exportAction.setEnabled(false);

        AbstractAction importAction = new ImportRosterItemAction(Bundle.getMessage("MenuItemImport"), pWho);
        importAction.setEnabled(false);

        AbstractAction copyAction = new CopyRosterItemAction(Bundle.getMessage("MenuItemCopy"), pWho);
        copyAction.setEnabled(false);

        AbstractAction deleteAction = new DeleteRosterItemAction(Bundle.getMessage("MenuItemDelete"), pWho);
        deleteAction.setEnabled(false);

        AbstractAction deleteGroupAction = new DeleteRosterGroupAction(Bundle.getMessage("MenuGroupDelete"), pWho);
        deleteGroupAction.setEnabled(false);

        AbstractAction createGroupAction = new CreateRosterGroupAction(Bundle.getMessage("MenuGroupCreate"), pWho);
        createGroupAction.setEnabled(false);

        AbstractAction rosterEntryToGroupAction = new RosterEntryToGroupAction(Bundle.getMessage("MenuGroupAssociate"), pWho);
        rosterEntryToGroupAction.setEnabled(false);

        AbstractAction removeRosterEntryToGroupAction = new RemoveRosterEntryToGroupAction(Bundle.getMessage("MenuGroupDisassociate"), pWho);
        removeRosterEntryToGroupAction.setEnabled(false);

        AbstractAction rosterGroupTableAction = new jmri.jmrit.roster.swing.rostergroup.RosterGroupTableAction(Bundle.getMessage("MenuGroupTable"));
        rosterGroupTableAction.setEnabled(false);

        AbstractAction rosterExportAction = new FullBackupExportAction(Bundle.getMessage("MenuFullExport"), pWho);
        rosterExportAction.setEnabled(false);

        AbstractAction rosterImportAction = new FullBackupImportAction(Bundle.getMessage("MenuFullImport"), pWho);
        rosterImportAction.setEnabled(false);

        AbstractAction speedProfileAction = new SpeedProfileAction(Bundle.getMessage("MenuSpeedProfile"));
        speedProfileAction.setEnabled(false);

        // Need a frame here, but are not passed one
        Frame newFrame = new Frame();
        AbstractAction printAction = new PrintRosterAction(Bundle.getMessage("MenuItemPrintSummary"), newFrame, false);
        printAction.setEnabled(false);
        AbstractAction listAction = new PrintListAction(Bundle.getMessage("MenuItemPrintList"), newFrame, false);
        listAction.setEnabled(false);
        AbstractAction previewAction = new PrintRosterAction(Bundle.getMessage("MenuItemPreviewSummary"), newFrame, true);
        previewAction.setEnabled(false);
        AbstractAction previewListAction = new PrintListAction(Bundle.getMessage("MenuItemPreviewList"), newFrame, true);
        previewListAction.setEnabled(false);

        JMenu groupMenu = new JMenu(Bundle.getMessage("MenuRosterGroups"));
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
        add(listAction);
        add(previewAction);
        add(previewListAction);
        addSeparator();
        add(groupMenu);
        addSeparator();
        add(rosterExportAction);
        add(rosterImportAction);
        add(speedProfileAction);

        // activate the correct items
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
                listAction.setEnabled(true);
                previewAction.setEnabled(true);
                previewListAction.setEnabled(true);
                rosterGroupTableAction.setEnabled(true);
                rosterExportAction.setEnabled(true);
                rosterImportAction.setEnabled(true);
                speedProfileAction.setEnabled(true);
                break;
            case SELECTMENU:
            case ENTRYMENU:
                printAction.setEnabled(true);
                previewAction.setEnabled(true);
                break;
            default:
                log.error("RosterMenu constructed without a valid menuType parameter: "
                        + pMenuType);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RosterMenu.class);

}
