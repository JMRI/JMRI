package jmri.util;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractFrameActionTest {

    @Test
    public void testCtor() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        assertNotNull( t, "exists");
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testAction() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event"));
        // this test creates a JmriJFrame with no title.  find that
        javax.swing.JFrame f = JFrameOperator.waitJFrame("", true, true);
        assertNotNull( f, "found output frame");
        // then close the frame.
        JFrameOperator fo = new JFrameOperator(f);
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

}
