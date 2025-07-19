package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractFrameActionTest {

    @Test
    public void testCtor() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        Assert.assertNotNull("exists",t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testAction() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event"));
        // this test creates a JmriJFrame with no title.  find that
        javax.swing.JFrame f = JFrameOperator.waitJFrame("", true, true);
        Assert.assertNotNull("found output frame", f);
        // then close the frame.
        JFrameOperator fo = new JFrameOperator(f);
        fo.requestClose();
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
