// NodeConfigurationMgr.java

package jmri.jmrix.cmri.serial.nodeconfigmanager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a
 * NodeConfigManagerAction object
 * Derived from the original NodeConfig class
 *
 * @author	Chuck Catania    Copyright (C) 2014,2017
 */

public class NodeConfigManagerAction extends AbstractAction {
    
    CMRISystemConnectionMemo _memo = null;

    public NodeConfigManagerAction(String s, CMRISystemConnectionMemo memo) { 
        super(s);
        _memo = memo;
    }

    public NodeConfigManagerAction(CMRISystemConnectionMemo memo) {
        this(Bundle.getMessage("WindowTitle"), memo);
    }
    
  @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigManagerFrame f = new NodeConfigManagerFrame(_memo);
        try {
              f.initComponents();
            }
        catch (Exception ex) {
            log.error("NodeConfigManagerAction Exception-C2: "+ex.toString());
            }
        f.setLocation(20,40);
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(NodeConfigManagerAction.class.getName());

}


/* @(#)NodeConfigurationMgr.java */
