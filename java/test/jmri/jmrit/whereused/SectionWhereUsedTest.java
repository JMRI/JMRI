package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SectionWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SectionWhereUsedTest {

    @Test
    public void testSectionWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        SectionWhereUsed ctor = new SectionWhereUsed();
        Assert.assertNotNull("exists", ctor);
        Section section = InstanceManager.getDefault(jmri.SectionManager.class).getSection("LeftTO to Main");
        JTextArea result = SectionWhereUsed.getWhereUsed(section);
        Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
        cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SectionWhereUsedTest.class);
}
