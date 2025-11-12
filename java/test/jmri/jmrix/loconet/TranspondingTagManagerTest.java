package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.managers.ProxyIdTagManager;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.managers.TranspondingTagManager class.
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class TranspondingTagManagerTest extends jmri.managers.DefaultIdTagManagerTest {

    @Test
    @Override
    public void testIdTagCreation() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t = m.createNewIdTag("LD0413276BC1", "Test Tag");

        assertNotNull( t, "TranspondingTag is not null");
    }

    @Test
    @Override
    public void testIdTagNames() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t = m.createNewIdTag("LD0413276BC1", "Test Tag");

        assertEquals( "LD0413276BC1", t.getSystemName(), "TranspondingTag system name is 'LD0413276BC1'");
        assertEquals( "Test Tag", t.getUserName(), "TranspondingTag user name is 'Test Tag'");
        assertEquals( "0413276BC1", t.getTagID(), "TranspondingTag tag id is '0413276BC1'");
    }

    @Test
    @Override
    public void testIdTagSingleRetrieval() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t = (TranspondingTag) m.newIdTag("LD0413276BC1", "Test Tag");

        assertNotNull( t, "Returned TranspondingTag is not null");

        assertNotNull( m.getBySystemName("LD0413276BC1"), "Get by system name is not null");
        assertNotNull( m.getByUserName("Test Tag"), "Get by user name is not null");
        assertNotNull( m.getByTagID("0413276BC1"), "Get by tag id is not null");

        assertNotNull( m.getIdTag("LD0413276BC1"), "Get TranspondingTag using system name is not null");
        assertNotNull( m.getIdTag("Test Tag"), "Get TranspondingTag using user name is not null");
        assertNotNull( m.getIdTag("0413276BC1"), "Get TranspondingTag using tag id is not null");

        var bySysName = m.getBySystemName("LD0413276BC1");
        assertNotNull(bySysName);
        assertTrue( t.getSystemName().equals(bySysName.getSystemName()),
            "Matching TranspondingTag returned from manager by system name");
        var byUsrName = m.getByUserName("Test Tag");
        assertNotNull(byUsrName);
        assertTrue( t.getUserName().equals(byUsrName.getUserName()),
            "Matching TranspondingTag returned from manager by user name");
        var byTagId = m.getByTagID("0413276BC1");
        assertNotNull(byTagId);
        assertTrue( t.getTagID().equals(byTagId.getTagID()),
            "Matching TranspondingTag returned from manager by tag id");

        assertNull( m.getBySystemName("LD99999999"), "Null Object returned from manager by system name");
        assertNull( m.getBySystemName("This doesn't exist"), "Null Object returned from manager by user name");
        assertNull( m.getBySystemName("XXXXXXXXXX"), "Null Object returned from manager by tagID");
    }

    @Test
    @Override
    public void testIdTagMultiRetrieval() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t1 = (TranspondingTag) m.newIdTag("LD0413276BC1", "Test Tag 1");
        TranspondingTag t2 = (TranspondingTag)m.newIdTag("LD0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created TranspondingTags are different");

        assertTrue( t1.equals(m.getBySystemName("LD0413276BC1")),
            "Matching TranspondingTag returned from manager by system name");
        assertTrue( t1.equals(m.getByUserName("Test Tag 1")),
            "Matching TranspondingTag returned from manager by user name");
        assertTrue( t1.equals(m.getByTagID("0413276BC1")),
            "Matching TranspondingTag returned from manager by tag id");

        assertTrue( t1.equals(m.getIdTag("LD0413276BC1")),
            "Matching TranspondingTag returned from manager via getRfidTag using system name");
        assertTrue( t1.equals(m.getIdTag("Test Tag 1")),
            "Matching TranspondingTag returned from manager via getRfidTag using user name");
        assertTrue( t1.equals(m.getIdTag("0413276BC1")),
            "Matching TranspondingTag returned from manager via getRfidTag using tag id");

        assertFalse( t2.equals(m.getBySystemName("LD0413276BC1")),
            "Non-matching TranspondingTag returned from manager by system name");
        assertFalse( t2.equals(m.getByUserName("Test Tag 1")),
            "Non-matching TranspondingTag returned from manager by user name");
        assertFalse( t2.equals(m.getByTagID("0413276BC1")),
            "Non-matching TranspondingTag returned from manager by tag id");

        assertFalse( t2.equals(m.getIdTag("LD0413276BC1")),
            "Non-matching TranspondingTag returned from manager via getRfidTag using system name");
        assertFalse( t2.equals(m.getIdTag("Test Tag 1")),
            "Non-matching TranspondingTag returned from manager via getRfidTag using user name");
        assertFalse( t2.equals(m.getIdTag("0413276BC1")),
            "Non-matching TranspondingTag returned from manager via getRfidTag using tag id");
    }

    @Test
    @Override
    public void testIdTagProviderCreate() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t = (TranspondingTag) m.provideIdTag("0413276BC1");

        assertNotNull( t, "TranspondingTag is not null");
        assertEquals( "LD0413276BC1", t.getSystemName(),
            "TranspondingTag System Name is 'LD0413276BC1'");
        assertEquals( "LD0413276BC1", t.getDisplayName(),
            "TranspondingTag display name is system name");
        assertEquals( "0413276BC1", t.getTagID(),
            "TranspondingTag tag ID is 0413276BC1");
        assertNull( t.getUserName(), "TranspondingTag user name is blank");

        t.setUserName("Test Tag");

        assertNotNull( t.getUserName(), "TranspondingTag user name is not blank");
        assertEquals( "Test Tag", t.getDisplayName(), "TranspondingTag display name is user name");
    }

    @Test
    @Override
    public void testIdTagProviderGet() {
        TranspondingTagManager m = (TranspondingTagManager)l;
        TranspondingTag t1 = (TranspondingTag)m.newIdTag("LD0413276BC1", "Test Tag 1");
        TranspondingTag t2 = (TranspondingTag)m.newIdTag("LD0413275FCA", "Test Tag 2");

        assertFalse( t1.equals(t2), "Created TranspondingTags are different");

        assertTrue( t1.equals(m.provideIdTag("LD0413276BC1")),
            "Matching TranspondingTag returned via provideTag by system name");
        assertTrue( t1.equals(m.provideIdTag("Test Tag 1")),
            "Matching TranspondingTag returned via provideTag by user name");
        assertTrue( t1.equals(m.provideIdTag("0413276BC1")),
            "Matching TranspondingTag returned via provideTag by tag ID");

        assertFalse( t1.equals(m.provideIdTag("LD0413275FCA")),
            "Non-matching TranspondingTag returned via provideTag by system name");
        assertFalse( t1.equals(m.provideIdTag("Test Tag 2")),
            "Non-matching TranspondingTag returned via provideTag by user name");
        assertFalse( t1.equals(m.provideIdTag("0413275FCA")),
            "Non-matching TranspondingTag returned via provideTag by tag ID");
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
        JUnitUtil.tearDown();
    }

    // Override init method so as not to load file
    // nor register shutdown task during tests.
    @Override
    protected TranspondingTagManager getManager() {
        return new TranspondingTagManager() {
            @Override
            public void init() {
            }
        };
    }

}
