package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.Memory;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultMemoryManagerTest extends AbstractProvidingManagerTestBase<jmri.MemoryManager,Memory> {

    @Test
    public void testIMthrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            l.provideMemory("IM"),
            "Expected exception not thrown");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Memory: System name \"" + l.getSystemNamePrefix() + "\" is missing suffix.");
    }

    @Test
    public void testBlankThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            l.provideMemory(""),
            "Expected exception not thrown");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Memory: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testCreatesiM() {
        Memory im = l.provideMemory("iM");
        assertNotNull( im, "iM created");
        assertEquals( "IMiM", im.getSystemName(), "correct system name");
    }
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultMemoryManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultMemoryManagerTest.class);

}
