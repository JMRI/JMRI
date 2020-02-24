package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SignalHeadWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalHeadWhereUsedTest {

    @Test
    public void testSignalHeadWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        SignalHeadWhereUsed ctor = new SignalHeadWhereUsed();
        Assert.assertNotNull("exists", ctor);

//         SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("Left-B");
//         JTextArea result = SignalHeadWhereUsed.getWhereUsed(signalHead);
//         Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
//         JUnitUtil.resetProfileManager();
//         JUnitUtil.initRosterConfigManager();
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
//         java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
//         cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadWhereUsedTest.class);
}
