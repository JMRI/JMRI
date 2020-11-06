package jmri.jmrit.logixng.tools.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jmri.InstanceManager;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.util.JmriJFrame;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalActionBean;

/**
 *
 */
public class TimeDiagram extends JmriJFrame {
    
    private static final int panelWidth700 = 700;
    private static final int panelHeight500 = 500;
    
    
    public TimeDiagram() {
        
    }

    @Override
    public void initComponents() {
        super.initComponents();
        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CreateNewLogixNGAction("Create a LogixNG"));
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
        menuBar.add(toolMenu);
//        menuBar.add(new jmri.jmrit.operations.OperationsMenu());

        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }
    
    
    /**
     * Swing action to load the New LogixNG frame.
     *
     * @author Daniel Bergqvist Copyright (C) 2019
     */
    public static class CreateNewLogixNGAction extends AbstractAction {

        public CreateNewLogixNGAction(String s) {
            super(s);
        }

//        OptionFrame f = null;

        @Override
        public void actionPerformed(ActionEvent e) {
/*            
            String systemName;
            LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test in TimeDiagram");  // NOI18N
            System.out.format("logixNG: %s%n", logixNG);
            systemName = InstanceManager.getDefault(DigitalExpressionManager.class).getNewSystemName(logixNG);
            DigitalExpressionBean expression = new ExpressionTurnout(systemName, "An expression for test");  // NOI18N
            InstanceManager.getDefault(DigitalExpressionManager.class).register(expression);
//            InstanceManager.getDefault(jmri.DigitalExpressionManager.class).addExpression(new ExpressionTurnout(systemName, "LogixNG 102, DigitalExpressionBean 26"));  // NOI18N
            systemName = InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(logixNG);
            DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
            InstanceManager.getDefault(DigitalActionManager.class).register(action);
/*            
            if (f == null || !f.isVisible()) {
                f = new OptionFrame();
                f.initComponents();
            }
            f.setExtendedState(Frame.NORMAL);
            f.setVisible(true); // this also brings the frame into focus
*/
        }
    }

}
