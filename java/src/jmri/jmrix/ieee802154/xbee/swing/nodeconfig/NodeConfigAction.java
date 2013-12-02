// NodeConfigAction.java

package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeConfigFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision$
 */
public class NodeConfigAction extends jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigAction {

    private jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo xcm=null;

    public NodeConfigAction(String s, jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo cm) { 
       super(s,cm);
       if(cm==null) 
       {
          // find the first registered memo.
          xcm=(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo)(jmri.InstanceManager.
               getList(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo.class).get(0));
       }
       else xcm=cm;
    }

    public NodeConfigAction() {
        this("Configure XBee Nodes",null);
    }

    public NodeConfigAction(String s) {
        this(s,null);
    }

    public NodeConfigAction(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo cm)    {
       this("Configure XBee Nodes",cm);
    }


    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame((jmri.jmrix.ieee802154.xbee.XBeeTrafficController)xcm.getTrafficController());
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setLocation(100,30);
        f.setVisible(true);
    }

   static Logger log = LoggerFactory.getLogger(NodeConfigAction.class.getName());
}


/* @(#)NodeConfigAction.java */
