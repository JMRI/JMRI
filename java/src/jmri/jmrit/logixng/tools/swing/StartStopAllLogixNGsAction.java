package jmri.jmrit.logixng.tools.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG_Manager;

/**
 * Swing action to create and register a TimeDiagram object.
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class StartStopAllLogixNGsAction extends AbstractAction {

    private final boolean _start;
    
    public StartStopAllLogixNGsAction(String s, boolean start) {
        super(s);
        _start = start;
    }

    public StartStopAllLogixNGsAction(boolean start) {
        this(Bundle.getMessage("MenuTimeDiagram"), start);  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_start) {
            InstanceManager.getDefault(LogixNG_Manager.class).activateAllLogixNGs();
        } else {
            InstanceManager.getDefault(LogixNG_Manager.class).deActivateAllLogixNGs();
        }
    }
    
}
