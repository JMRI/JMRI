package jmri.jmrit.logixng.ztest;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.implementation.DefaultLogixNGManager;
import jmri.util.swing.JmriAbstractAction;

/**
 * Swing action to create and register a TimeTableFrame
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class LogixNG_StartupAction extends JmriAbstractAction {

    public LogixNG_StartupAction(String s) {
        super(s);
    }

    public LogixNG_StartupAction() {
        this("LogixNG_StartupAction");  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
/*        
        try {
            ((DefaultLogixNGManager)InstanceManager.getDefault(LogixNG_Manager.class)).testLogixNGs();
//        } catch (PropertyVetoException ex) {
        } catch (Exception ex) {
            Logger.getLogger(LogixNG_StartupAction.class.getName()).log(Level.SEVERE, "exception thrown", ex);
        }
        /*
            if (jmri.InstanceManager.getNullableDefault(TimeTableFrame.class) != null) {
            // Prevent duplicate copies
            return;
            }
            TimeTableFrame f = new TimeTableFrame("");
            f.setVisible(true);
        */
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }
    
}
