package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.configtool package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class ConfigToolActionTest {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load dummy TrafficController
        TrafficControllerScaffold tcs = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        //f.initComponents(memo);
        ConfigToolPane pane = new ConfigToolPane();
        Assert.assertNotNull("exists", pane);
        
        memo.dispose();
        tcs.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
