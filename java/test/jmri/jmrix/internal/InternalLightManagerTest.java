package jmri.jmrix.internal;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.internal.InternalLightManager class.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class InternalLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IL" + i;
    }

    @Test
    public void testAsAbstractFactory() {
        // create and register the manager object
        InternalLightManager alm = new InternalLightManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setLightManager(alm);

        // ask for a Light, and check type
        LightManager lm = InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("IL21", "my name");

        assertNotNull( tl);

        // make sure loaded into tables

        assertNotNull( lm.getBySystemName("IL21"));
        assertNotNull( lm.getByUserName("my name"));

        assertSame( tl, lm.getBySystemName("IL21") );
        assertSame( tl, lm.getByUserName("my name") );

    }

    @Test
    public void testIsVariableLight() {
        // ask for a Light, and check type
        LightManager lm = InstanceManager.lightManagerInstance();
        assertInstanceOf( VariableLight.class,
            lm.newLight("IL21", "my name"));

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
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
        // create and register the manager object
        l = new InternalLightManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setLightManager(l);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static import org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalLightManagerTest.class);
}
