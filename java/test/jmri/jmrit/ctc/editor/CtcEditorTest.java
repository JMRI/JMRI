package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

/**
 * Tests for the CtcEditor Class
 * @author Dave Sand Copyright (C) 2018
 */
public class CtcEditorTest {
    JFrameOperator _jfo = null;

    @Test
    // test creation
    public void testCreate() {
         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
       new CtcEditor();
        CtcEditor f = new CtcEditor();
        f.setVisible(true);
        Assert.assertNotNull(f);
        _jfo = new JFrameOperator(Bundle.getMessage("TitleCtcEditor"));  // NOI18N
        Assert.assertNotNull(_jfo);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDone")).doClick();  // NOI18N
// new EventTool().waitNoEvent(10000);

    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorTest.class);
}