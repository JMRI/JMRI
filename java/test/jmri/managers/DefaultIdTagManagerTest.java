package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.managers.DefaultIdTagManager class.
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultIdTagManagerTest extends AbstractProvidingManagerTestBase<IdTagManager,IdTag> {

    @Test
    public void testIdTagCreation() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("ID0413276BC1", "Test Tag");

        assertNotNull( t, "IdTag is not null");
    }

    @Test
    public void testIdTagNames() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("ID0413276BC1", "Test Tag");

        assertEquals( "ID0413276BC1", t.getSystemName(), "IdTag system name is 'ID0413276BC1'");
        assertEquals( "Test Tag", t.getUserName(), "IdTag user name is 'Test Tag'");
        assertEquals( "0413276BC1", t.getTagID(), "IdTag tag id is '0413276BC1'");
    }

    @Test
    public void testIdTagSingleRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.newIdTag("ID0413276BC1", "Test Tag");

        assertNotNull( t, "Returned IdTag is not null");

        IdTag getBySystemNameID0413276BC1 = m.getBySystemName("ID0413276BC1");
        assertNotNull( getBySystemNameID0413276BC1, "Get by system name is not null");

        IdTag getByUserNameTestTag = m.getByUserName("Test Tag");
        assertNotNull( getByUserNameTestTag, "Get by user name is not null");
        assertNotNull( m.getByTagID("0413276BC1"), "Get by tag id is not null");

        IdTag getIdTagID0413276BC1 = m.getIdTag("ID0413276BC1");
        assertNotNull( getIdTagID0413276BC1, "Get IdTag using system name is not null");
        assertNotNull( m.getIdTag("Test Tag"), "Get IdTag using user name is not null");
        assertNotNull( m.getIdTag("0413276BC1"), "Get IdTag using tag id is not null");

        assertEquals( t.getSystemName(), getBySystemNameID0413276BC1.getSystemName(),
                "Matching IdTag returned from manager by system name");
        assertEquals( t.getUserName(), getByUserNameTestTag.getUserName(),
                "Matching IdTag returned from manager by user name");
        assertEquals( t.getTagID(), getIdTagID0413276BC1.getTagID(),
                "Matching IdTag returned from manager by tag id");

        assertNull( m.getBySystemName("ID99999999"), "Null Object returned from manager by system name");
        assertNull( m.getBySystemName("This doesn't exist"), "Null Object returned from manager by user name");
        assertNull( m.getBySystemName("XXXXXXXXXX"), "Null Object returned from manager by tagID");
    }

    @Test
    public void testIdTagMultiRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("ID0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("ID0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created IdTags are different");

        assertTrue( t1.equals(m.getBySystemName("ID0413276BC1")),
            "Matching IdTag returned from manager by system name");
        assertTrue( t1.equals(m.getByUserName("Test Tag 1")),
            "Matching IdTag returned from manager by user name");
        assertTrue( t1.equals(m.getByTagID("0413276BC1")),
            "Matching IdTag returned from manager by tag id");

        assertTrue( t1.equals(m.getIdTag("ID0413276BC1")),
            "Matching IdTag returned from manager via getRfidTag using system name");
        assertTrue( t1.equals(m.getIdTag("Test Tag 1")),
            "Matching IdTag returned from manager via getRfidTag using user name");
        assertTrue( t1.equals(m.getIdTag("0413276BC1")),
            "Matching IdTag returned from manager via getRfidTag using tag id");

        assertFalse( t2.equals(m.getBySystemName("ID0413276BC1")),
            "Non-matching IdTag returned from manager by system name");
        assertFalse( t2.equals(m.getByUserName("Test Tag 1")),
            "Non-matching IdTag returned from manager by user name");
        assertFalse( t2.equals(m.getByTagID("0413276BC1")),
            "Non-matching IdTag returned from manager by tag id");

        assertFalse( t2.equals(m.getIdTag("ID0413276BC1")),
            "Non-matching IdTag returned from manager via getRfidTag using system name");
        assertFalse( t2.equals(m.getIdTag("Test Tag 1")),
            "Non-matching IdTag returned from manager via getRfidTag using user name");
        assertFalse( t2.equals(m.getIdTag("0413276BC1")),
            "Non-matching IdTag returned from manager via getRfidTag using tag id");
    }

    @Test
    public void testIdTagProviderCreate() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.provideIdTag("0413276BC1");

        assertNotNull( t, "IdTag is not null");
        assertEquals( "ID0413276BC1", t.getSystemName(), "IdTag System Name is 'ID0413276BC1'");
        assertEquals( "ID0413276BC1", t.getDisplayName(), "IdTag display name is system name");
        assertEquals( "0413276BC1", t.getTagID(), "IdTag tag ID is 0413276BC1");
        assertNull( t.getUserName(), "IdTag user name is blank");

        t.setUserName("Test Tag");

        assertNotNull( t.getUserName(), "IdTag user name is not blank");
        assertEquals( "Test Tag", t.getDisplayName(), "IdTag display name is user name");
    }

    @Test
    public void testIdTagProviderGet() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("ID0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("ID0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created IdTags are different");

        assertTrue( t1.equals(m.provideIdTag("ID0413276BC1")),
            "Matching IdTag returned via provideTag by system name");
        assertTrue( t1.equals(m.provideIdTag("Test Tag 1")),
            "Matching IdTag returned via provideTag by user name");
        assertTrue( t1.equals(m.provideIdTag("0413276BC1")),
            "Matching IdTag returned via provideTag by tag ID");

        assertFalse( t1.equals(m.provideIdTag("ID0413275FCA")),
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
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
        l = getManager();
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // Override init method so as not to load file
    // nor register shutdown task during tests.
    protected DefaultIdTagManager getManager() {
        return new DefaultIdTagManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)) {
            @Override
            public void init() {
            }
        };
    }

}
