package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the WhereUsedAction Class
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedActionTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        WhereUsedAction action = new WhereUsedAction();
        Assertions.assertNotNull( action, "exists");
        action.actionPerformed(null);

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleWhereUsed"));
        Assertions.assertNotNull(jfo);
        JUnitUtil.dispose(jfo.getWindow());

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
