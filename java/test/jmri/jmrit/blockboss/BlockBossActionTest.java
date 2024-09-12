package jmri.jmrit.blockboss;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BlockBossActionTest {

    @Test
    public void testCtor() {
        BlockBossAction t = new BlockBossAction();
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testAction() {
        BlockBossAction t = new BlockBossAction();
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event"));

        JFrameOperator fo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));
        JUnitUtil.dispose(fo.getWindow());
        fo.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockBossActionTest.class);

}
