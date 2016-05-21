// NodeConfigAction.java

package jmri.jmrix.ieee802154.swing.nodeconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeConfigFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision$
 */
public class NodeConfigAction extends AbstractAction {

    private jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo icm=null;

    public NodeConfigAction(String s, jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo cm) { 
       super(s);
       if(cm==null) 
       {
          // find the first registered memo.
          icm=(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo)(jmri.InstanceManager.
               getList(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo.class).get(0));
       }
       else icm=cm;
    }

    public NodeConfigAction() {
        this("Configure IEEE802154 Nodes",null);
    }

    public NodeConfigAction(String s) {
        this(s,null);
    }

    public NodeConfigAction(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo cm)    {
       this("Configure IEEE802154 Nodes",cm);
    }


    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(icm.getTrafficController());
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
