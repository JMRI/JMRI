package jmri.util.swing;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BeanSelectCreatePanelTest {

    @Test
    public void testCTor() {
        jmri.TurnoutManager manager = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class);
        jmri.Turnout turnout = manager.provideTurnout("IT1");
        BeanSelectCreatePanel<jmri.Turnout> t = new BeanSelectCreatePanel<>(manager,turnout);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BeanSelectCreatePanelTest.class.getName());

}
