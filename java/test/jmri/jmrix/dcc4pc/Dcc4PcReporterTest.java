package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcReporterTest extends jmri.implementation.AbstractRailComReporterTest {

    @BeforeEach
    @Override
    public void setUp() {
        r = new Dcc4PcReporter("DR1","test");
        JUnitUtil.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcReporterTest.class);

}
