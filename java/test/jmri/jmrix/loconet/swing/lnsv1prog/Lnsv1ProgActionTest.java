package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class Lnsv1ProgActionTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Lnsv1ProgAction t = new Lnsv1ProgAction();
        assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
