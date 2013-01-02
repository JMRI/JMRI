// NceConsistEngineAction.java

package jmri.jmrit.operations.rollingstock.engines;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrix.nce.ActiveFlag;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;


/**
 * Starts the NceConsistEngine thread
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */


public class NceConsistEngineAction extends AbstractAction {
	
	NceTrafficController tc;
    
	public NceConsistEngineAction(String actionName, Component frame) {
        super(actionName);
        // only enable if connected to an NCE system
        setEnabled(false);
        // disable if NCE USB selected
		// get NceTrafficContoller if there's one
		List<Object> memos = InstanceManager.getList(NceSystemConnectionMemo.class);
		if (memos != null){
			// find NceConnection that is serial
			for (int i=0; i<memos.size(); i++){
				NceSystemConnectionMemo memo = (NceSystemConnectionMemo)memos.get(i);
				if (memo.getNceUSB() == NceTrafficController.USB_SYSTEM_NONE){
					tc = memo.getNceTrafficController();
				}
			}
		}
        if(ActiveFlag.isActive() && tc != null)
			setEnabled(true);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new NceConsistEngines(tc);
		mb.setName("Nce Consist Sync Engines"); // NOI18N
		mb.start();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(NceConsistEngineAction.class.getName());
}
