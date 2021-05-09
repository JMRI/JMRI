package jmri.jmrit.logixng.tools.swing;

import java.awt.Dimension;

import jmri.util.JmriJFrame;

/**
 * Table for LogixNG initialization.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class LogixNGInitializationTable extends JmriJFrame {
    
    private static final int panelWidth700 = 700;
    private static final int panelHeight500 = 500;
    
    
    @Override
    public void initComponents() {
        super.initComponents();
        // build menu
//        JMenuBar menuBar = new JMenuBar();
//        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
//        toolMenu.add(new CreateNewLogixNGAction("Create a LogixNG"));
/*        
        toolMenu.add(new CreateNewLogixNGAction(Bundle.getMessage("TitleOptions")));
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
        toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
        toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
        toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
        toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));
*/      
//        menuBar.add(toolMenu);
//        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        
//        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N
        
        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
    }
    
    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }
    
}
