package jmri.jmrix.loconet.locormi;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

/**
 * Start a LnMessageServer that will listen for clients wanting to
 * use the LocoNet connection on this machine.
 * Copyright:    Copyright (c) 2002
 * @author      Alex Shepherd
 * @version $Revision: 1.6 $
 */
public class LnMessageServerAction extends AbstractAction {

    public LnMessageServerAction( String s ) {
        super( s ) ;
    }

    public LnMessageServerAction() {
        super( "Start LocoNet server" ) ;
    }

    public void actionPerformed( ActionEvent e) {
        try {
            LnMessageServer server = LnMessageServer.getInstance() ;
            server.enable();
        } catch( RemoteException ex ) {
            log.warn( "LnMessageServerAction Exception: " + ex );
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageServerAction.class.getName());
}
