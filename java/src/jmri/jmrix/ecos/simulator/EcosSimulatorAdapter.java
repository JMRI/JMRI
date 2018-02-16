package jmri.jmrix.ecos.simulator;

import java.io.IOException;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.networkdriver.NetworkDriverAdapter;
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
public class EcosSimulatorAdapter extends NetworkDriverAdapter {

    private EcosSimulatorServer server = null;

    public EcosSimulatorAdapter() {
        super(new EcosSystemConnectionMemo("U", "ECoS Simulator")); // pass customized user name
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
     * {@inheritDoc}
     * <p>
     * This implementation sets the port to the port used by the simulation
     * server before connecting.
     */
    @Override
    public void connect() throws IOException {
        this.setPort(server.getPort());
        super.connect();
    }

    /**
     * Set up all of the other objects to operate with an EcosSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        this.configure(new EcosSimulatorTrafficController(this.getSystemConnectionMemo()));
    }

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorAdapter.class);

}
