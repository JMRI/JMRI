package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: Z21PacketizerTest </p>
 * <p>
 *
 * @author Bob Jacobsen Copyrgiht (C) 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        RocoZ21CommandStation lcs = new RocoZ21CommandStation();
        tc = new Z21XNetPacketizer(lcs) {
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

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
    }

}
