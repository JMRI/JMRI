package jmri.jmrix.ecos.simulator;

import java.io.IOException;
import jmri.jmrix.ecos.EcosConnectionTypeList;
import jmri.jmrix.ecos.EcosPortController;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated ECoS system.
 * <p>
 * Currently, the ECoS EcosSimulatorAdapter reacts to commands sent from the
 * user interface with messages an appropriate reply message. Based on
 * jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter
 * 2017 support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2017
 * @author Randall Wood Copyright 2017
 */
public class EcosSimulatorAdapter extends EcosPortController {

    private EcosSimulatorServer server = null;

    public EcosSimulatorAdapter() {
        super(new EcosSystemConnectionMemo("U", "ECoS Simulator")); // pass customized user name
        setManufacturer(EcosConnectionTypeList.ESU);
        this.setHostAddress("127.0.0.1");
        this.setHostName("localhost");
        try {
            server = new EcosSimulatorServer();
            server.start();
            log.info("Using simulated server on port {}", server.getPort());
            this.setPort(server.getPort());
        } catch (IOException ex) {
            log.error("Unable to create simulated server");
        }
    }

    /**
     * Set up all of the other objects to operate with an EcosSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        EcosTrafficController control = new EcosTrafficController(getSystemConnectionMemo());
        //compare with: XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        control.connectPort(this);
        this.getSystemConnectionMemo().setEcosTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();
    }

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorAdapter.class);

}
