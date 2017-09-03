package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of operation parameters
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 */
public class OperationsSetupFrame extends OperationsFrame {

    public OperationsSetupFrame() {
        super(Bundle.getMessage("TitleOperationsSetup"), new OperationsSetupPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();
        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new OptionAction(Bundle.getMessage("TitleOptions")));
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
        toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
        toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
        toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
        toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));

        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight500));
    }

//    private final static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class);
}
