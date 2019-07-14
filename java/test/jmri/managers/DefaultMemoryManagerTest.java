package jmri.managers;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultMemoryManagerTest extends AbstractProvidingManagerTestBase<jmri.MemoryManager,jmri.Memory> {

    @Test
    public void testIMthrows() {
        try {
            l.provideMemory("IM");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException e) {
            // nothing to do
        }
        JUnitAppender.assertErrorMessage("Invalid system name for memory: \"IM\" but needed IM followed by a suffix");
    }

    @Test
    public void testBlankThrows() {
        try {
            l.provideMemory("");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException e) {
            // nothing to do
        }
        JUnitAppender.assertErrorMessage("Invalid system name for Memory: \"\" needed non-empty suffix to follow IM");
    }

    @Test
    public void testCreatesiM() {
        jmri.Memory im = l.provideMemory("iM");
        Assert.assertNotNull("iM created",im);
        Assert.assertEquals("correct system name","IMiM",im.getSystemName());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultMemoryManagerTest.class);

}
