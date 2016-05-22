package jmri.jmrix.loconet.locormi;

import javax.swing.AbstractAction;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

import org.slf4j.LoggerFactory;

import jmri.util.zeroconf.ZeroConfService;

/**
 * Start a LnMessageServer that will listen for clients wanting to
 * use the LocoNet connection on this machine.
 * Copyright:    Copyright (c) 2002
 * @author      Alex Shepherd
 * @version $Revision$
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
            // start server
            LnMessageServer server = LnMessageServer.getInstance() ;
            server.enable();
            // advertise under zeroconf
            ZeroConfService.create("_jmri-locormi._tcp.local.", 1099).publish();
            // disable action, as already run
            setEnabled(false);
        } catch( RemoteException ex ) {
            LoggerFactory.getLogger(LnMessageServerAction.class.getName()).warn( "LnMessageServerAction Exception: " + ex );
        }
    }

}
