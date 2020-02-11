package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the WhereUsedFrame Class
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedFrameTest {

    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WhereUsedFrame frame = new WhereUsedFrame();
        Assert.assertNotNull(frame);
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleWhereUsed"));  // NOI18N
        Assert.assertNotNull(jfo);

        // Select Sensor
        new JComboBoxOperator(jfo, 0).selectItem("Sensor");  // NOI18N
        new JComboBoxOperator(jfo, 1).selectItem(7);   // S-Main
        Assert.assertTrue(frame._textArea.getLineCount() > 5);
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
        cm.load(f);
   }

    @After
    public  void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedFrameTest.class);
}
