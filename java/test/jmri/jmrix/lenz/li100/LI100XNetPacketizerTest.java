package jmri.jmrix.lenz.li100;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: LI100XNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LI100XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new LI100XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
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
