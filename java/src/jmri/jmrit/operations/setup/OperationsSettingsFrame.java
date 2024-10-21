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
public class OperationsSettingsFrame extends OperationsFrame {

    public OperationsSettingsFrame() {
        super(Bundle.getMessage("TitleOperationsSetup"), new OperationsSettingsPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();
        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new OptionAction());
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.addSeparator();
        toolMenu.add(new BackupFilesAction());
        toolMenu.add(new RestoreFilesAction());
        toolMenu.addSeparator();
        toolMenu.add(new LoadDemoAction());
        toolMenu.add(new ResetAction());
        toolMenu.add(new ManageBackupsAction());

        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight500));
    }

//    private final static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class);
}
