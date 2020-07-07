package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import org.junit.Assert;
import org.junit.jupiter.api.*;


/**
 * <p>
 * Title: ZTC611XNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class ZTC611XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new ZTC611XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
        try {
            port = new XNetPortControllerScaffold();
        } catch (Exception e) {
            Assert.fail("Error creating test port");
        }
    }

}
