package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the MemoryWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class MemoryWhereUsedTest {

    @Test
    public void testMemoryWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MemoryWhereUsed ctor = new MemoryWhereUsed();
        Assert.assertNotNull("exists", ctor);

//         Memory memory = InstanceManager.getDefault(jmri.MemoryManager.class).getMemory("BlockMemory");
//         JTextArea result = MemoryWhereUsed.getWhereUsed(memory);
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

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryWhereUsedTest.class);
}
