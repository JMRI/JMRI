package jmri.jmrix.qsi.qsimon;

import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import jmri.jmrix.qsi.QsiTrafficControlScaffold;
import jmri.jmrix.qsi.QsiTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class QsiMonActionTest {

    @Test
    public void testCTor() {
        QsiTrafficController tc = new QsiTrafficControlScaffold();
        QsiSystemConnectionMemo memo = new QsiSystemConnectionMemo(tc);
        QsiMonAction t = new QsiMonAction(memo);
        Assertions.assertNotNull(t, "exists");
        memo.dispose();
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testQsiMonActionPerformed() {
        QsiTrafficController tc = new QsiTrafficControlScaffold();
        QsiSystemConnectionMemo memo = new QsiSystemConnectionMemo(tc);
        QsiMonAction t = new QsiMonAction(memo);
        Assertions.assertNotNull(t, "exists");
        
        jmri.util.ThreadingUtil.runOnGUI(() -> t.actionPerformed(null));
        
        JFrameOperator jfo = new JFrameOperator("QSI Command Monitor");
        Assertions.assertNotNull(jfo);
        jfo.requestClose();
        jfo.waitClosed();
        
        memo.dispose();
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
