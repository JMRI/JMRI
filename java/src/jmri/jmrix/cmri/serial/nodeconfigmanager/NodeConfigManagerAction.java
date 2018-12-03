package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger log = LoggerFactory.getLogger(NodeConfigManagerAction.class);

}
