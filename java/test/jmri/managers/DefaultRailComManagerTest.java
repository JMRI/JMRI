package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultRailComManagerTest extends DefaultIdTagManagerTest {

    @Override
    @Test
    public void testIdTagCreation() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("RD0413276BC1", "Test Tag");

        assertNotNull( t, "IdTag is not null");
    }

    @Override
    @Test
    public void testIdTagNames() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("RD0413276BC1", "Test Tag");

        assertEquals( "RD0413276BC1", t.getSystemName(),
            "IdTag system name is 'RD0413276BC1'");
        assertEquals( "Test Tag", t.getUserName(), "IdTag user name is 'Test Tag'");
        assertEquals( "0413276BC1", t.getTagID(), "IdTag tag id is '0413276BC1'");
    }

    @Override
    @Test
    public void testIdTagSingleRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.newIdTag("RD0413276BC1", "Test Tag");

        assertNotNull( t, "Returned IdTag is not null");

        assertNotNull( m.getBySystemName("RD0413276BC1"), "Get by system name is not null");
        assertNotNull( m.getByUserName("Test Tag"), "Get by user name is not null");
        assertNotNull( m.getByTagID("0413276BC1"), "Get by tag id is not null");

        assertNotNull( m.getIdTag("RD0413276BC1"), "Get IdTag using system name is not null");
        assertNotNull( m.getIdTag("Test Tag"), "Get IdTag using user name is not null");
        assertNotNull( m.getIdTag("0413276BC1"), "Get IdTag using tag id is not null");

        var tagBySysName = m.getBySystemName("RD0413276BC1");
        assertNotNull(tagBySysName);
        assertTrue( t.getSystemName().equals(tagBySysName.getSystemName()),
            "Matching IdTag returned from manager by system name");
        var tagByUserName = m.getByUserName("Test Tag");
        assertNotNull(tagByUserName);
        var tagUserName = t.getUserName();
        assertNotNull(tagUserName);
        assertTrue( tagUserName.equals(tagByUserName.getUserName()),
            "Matching IdTag returned from manager by user name");
        var tagById = m.getByTagID("0413276BC1");
        assertNotNull(tagById);
        assertTrue( t.getTagID().equals(tagById.getTagID()),
            "Matching IdTag returned from manager by tag id");

        assertNull( m.getBySystemName("RD99999999"),
            "Null Object returned from manager by system name");
        assertNull( m.getBySystemName("This doesn't exist"),
            "Null Object returned from manager by user name");
        assertNull( m.getBySystemName("XXXXXXXXXX"),
            "Null Object returned from manager by tagID");
    }

    @Override
    @Test
    public void testIdTagMultiRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("RD0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("RD0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created IdTags are different");

        assertTrue( t1.equals(m.getBySystemName("RD0413276BC1")),
            "Matching IdTag returned from manager by system name");
        assertTrue( t1.equals(m.getByUserName("Test Tag 1")),
            "Matching IdTag returned from manager by user name");
        assertTrue( t1.equals(m.getByTagID("0413276BC1")),
            "Matching IdTag returned from manager by tag id");

        assertTrue( t1.equals(m.getIdTag("RD0413276BC1")),
            "Matching IdTag returned from manager via getRfidTag using system name");
        assertTrue( t1.equals(m.getIdTag("Test Tag 1")),
            "Matching IdTag returned from manager via getRfidTag using user name");
        assertTrue( t1.equals(m.getIdTag("0413276BC1")),
            "Matching IdTag returned from manager via getRfidTag using tag id");

        assertFalse( t2.equals(m.getBySystemName("RD0413276BC1")),
            "Non-matching IdTag returned from manager by system name");
        assertFalse( t2.equals(m.getByUserName("Test Tag 1")),
            "Non-matching IdTag returned from manager by user name");
        assertFalse( t2.equals(m.getByTagID("0413276BC1")),
            "Non-matching IdTag returned from manager by tag id");

        assertFalse( t2.equals(m.getIdTag("RD0413276BC1")),
            "Non-matching IdTag returned from manager via getRfidTag using system name");
        assertFalse( t2.equals(m.getIdTag("Test Tag 1")),
            "Non-matching IdTag returned from manager via getRfidTag using user name");
        assertFalse( t2.equals(m.getIdTag("0413276BC1")),
            "Non-matching IdTag returned from manager via getRfidTag using tag id");
    }

    @Override
    @Test
    public void testIdTagProviderCreate() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.provideIdTag("0413276BC1");

        assertNotNull( t, "IdTag is not null");
        assertEquals( "RD0413276BC1", t.getSystemName(), "IdTag System Name is 'RD0413276BC1'");
        assertEquals( "RD0413276BC1", t.getDisplayName(), "IdTag display name is system name");
        assertEquals( "0413276BC1", t.getTagID(), "IdTag tag ID is 0413276BC1");
        assertNull( t.getUserName(), "IdTag user name is blank");

        t.setUserName("Test Tag");

        assertNotNull( t.getUserName(), "IdTag user name is not blank");
        assertEquals( "Test Tag", t.getDisplayName(), "IdTag display name is user name");
    }

    @Test
    @Override
    public void testIdTagProviderGet() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("RD0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("RD0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created IdTags are different");

        assertTrue( t1.equals(m.provideIdTag("RD0413276BC1")),
            "Matching IdTag returned via provideTag by system name");
        assertTrue( t1.equals(m.provideIdTag("Test Tag 1")),
            "Matching IdTag returned via provideTag by user name");
        assertTrue( t1.equals(m.provideIdTag("0413276BC1")),
            "Matching IdTag returned via provideTag by tag ID");

        assertFalse( t1.equals(m.provideIdTag("RD0413275FCA")),
            "Non-matching IdTag returned via provideTag by system name");
        assertFalse( t1.equals(m.provideIdTag("Test Tag 2")),
            "Non-matching IdTag returned via provideTag by user name");
        assertFalse( t1.equals(m.provideIdTag("0413275FCA")),
            "Non-matching IdTag returned via provideTag by tag ID");
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
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        InstanceManager.setDefault(jmri.IdTagManager.class,new ProxyIdTagManager());
        l = getManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        l = null;
        InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // Override init method so as not to load file
    // nor register shutdown task during tests.
    @Override
    protected DefaultIdTagManager getManager() {
        return new DefaultRailComManager() {
            @Override
            public void init() {
            }
        };
    }

}
