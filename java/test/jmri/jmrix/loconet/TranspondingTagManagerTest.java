package jmri.jmrix.loconet;

import jmri.IdTag;
import jmri.IdTagManager;
import org.junit.*;

/**
 * Tests for the jmri.managers.TranspondingTagManager class.
 *
 * @author	Matthew Harris Copyright (C) 2011
 */
public class TranspondingTagManagerTest {

    @Test
    public void testTranspondingTagCreation() {
        TranspondingTagManager m = getManager();
        TranspondingTag t = m.createNewIdTag("LD0413276BC1", "Test Tag");

        Assert.assertNotNull("TranspondingTag is not null", t);
    }

    @Test
    public void testTranspondingTagNames() {
        TranspondingTagManager m = getManager();
        TranspondingTag t = m.createNewIdTag("LD0413276BC1", "Test Tag");

        Assert.assertEquals("TranspondingTag system name is 'LD0413276BC1'", "LD0413276BC1", t.getSystemName());
        Assert.assertEquals("TranspondingTag user name is 'Test Tag'", "Test Tag", t.getUserName());
        Assert.assertEquals("TranspondingTag tag id is '0413276BC1'", "0413276BC1", t.getTagID());
    }

    @Test
    public void testTranspondingTagSingleRetrieval() {
        TranspondingTagManager m = getManager();
        TranspondingTag t = (TranspondingTag) m.newIdTag("LD0413276BC1", "Test Tag");

        Assert.assertNotNull("Returned TranspondingTag is not null", t);

        Assert.assertNotNull("Get by system name is not null", m.getBySystemName("LD0413276BC1"));
        Assert.assertNotNull("Get by user name is not null", m.getByUserName("Test Tag"));
        Assert.assertNotNull("Get by tag id is not null", m.getByTagID("0413276BC1"));

        Assert.assertNotNull("Get TranspondingTag using system name is not null", m.getIdTag("LD0413276BC1"));
        Assert.assertNotNull("Get TranspondingTag using user name is not null", m.getIdTag("Test Tag"));
        Assert.assertNotNull("Get TranspondingTag using tag id is not null", m.getIdTag("0413276BC1"));

        Assert.assertTrue("Matching TranspondingTag returned from manager by system name", t.getSystemName().equals(m.getBySystemName("LD0413276BC1").getSystemName()));
        Assert.assertTrue("Matching TranspondingTag returned from manager by user name", t.getUserName().equals(m.getByUserName("Test Tag").getUserName()));
        Assert.assertTrue("Matching TranspondingTag returned from manager by tag id", t.getTagID().equals(m.getByTagID("0413276BC1").getTagID()));

        Assert.assertNull("Null Object returned from manager by system name", m.getBySystemName("LD99999999"));
        Assert.assertNull("Null Object returned from manager by user name", m.getBySystemName("This doesn't exist"));
        Assert.assertNull("Null Object returned from manager by tagID", m.getBySystemName("XXXXXXXXXX"));
    }

    @Test
    public void testTranspondingTagMultiRetrieval() {
        TranspondingTagManager m = getManager();
        TranspondingTag t1 = (TranspondingTag) m.newIdTag("LD0413276BC1", "Test Tag 1");
        TranspondingTag t2 = (TranspondingTag)m.newIdTag("LD0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created TranspondingTags are different", t1.equals(t2));

        Assert.assertTrue("Matching TranspondingTag returned from manager by system name", t1.equals(m.getBySystemName("LD0413276BC1")));
        Assert.assertTrue("Matching TranspondingTag returned from manager by user name", t1.equals(m.getByUserName("Test Tag 1")));
        Assert.assertTrue("Matching TranspondingTag returned from manager by tag id", t1.equals(m.getByTagID("0413276BC1")));

        Assert.assertTrue("Matching TranspondingTag returned from manager via getRfidTag using system name", t1.equals(m.getIdTag("LD0413276BC1")));
        Assert.assertTrue("Matching TranspondingTag returned from manager via getRfidTag using user name", t1.equals(m.getIdTag("Test Tag 1")));
        Assert.assertTrue("Matching TranspondingTag returned from manager via getRfidTag using tag id", t1.equals(m.getIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching TranspondingTag returned from manager by system name", t2.equals(m.getBySystemName("LD0413276BC1")));
        Assert.assertFalse("Non-matching TranspondingTag returned from manager by user name", t2.equals(m.getByUserName("Test Tag 1")));
        Assert.assertFalse("Non-matching TranspondingTag returned from manager by tag id", t2.equals(m.getByTagID("0413276BC1")));

        Assert.assertFalse("Non-matching TranspondingTag returned from manager via getRfidTag using system name", t2.equals(m.getIdTag("LD0413276BC1")));
        Assert.assertFalse("Non-matching TranspondingTag returned from manager via getRfidTag using user name", t2.equals(m.getIdTag("Test Tag 1")));
        Assert.assertFalse("Non-matching TranspondingTag returned from manager via getRfidTag using tag id", t2.equals(m.getIdTag("0413276BC1")));
    }

    @Test
    public void testTranspondingTagProviderCreate() {
        TranspondingTagManager m = getManager();
        TranspondingTag t = (TranspondingTag) m.provideIdTag("0413276BC1");

        Assert.assertNotNull("TranspondingTag is not null", t);
        Assert.assertEquals("TranspondingTag System Name is 'LD0413276BC1'", "LD0413276BC1", t.getSystemName());
        Assert.assertEquals("TranspondingTag display name is system name", "LD0413276BC1", t.getDisplayName());
        Assert.assertEquals("TranspondingTag tag ID is 0413276BC1", "0413276BC1", t.getTagID());
        Assert.assertNull("TranspondingTag user name is blank", t.getUserName());

        t.setUserName("Test Tag");

        Assert.assertNotNull("TranspondingTag user name is not blank", t.getUserName());
        Assert.assertEquals("TranspondingTag display name is user name", "Test Tag", t.getDisplayName());
    }

    @Test
    public void testTranspondingTagProviderGet() {
        TranspondingTagManager m = getManager();
        TranspondingTag t1 = (TranspondingTag)m.newIdTag("LD0413276BC1", "Test Tag 1");
        TranspondingTag t2 = (TranspondingTag)m.newIdTag("LD0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created TranspondingTags are different", t1.equals(t2));

        Assert.assertTrue("Matching TranspondingTag returned via provideTag by system name", t1.equals(m.provideIdTag("LD0413276BC1")));
        Assert.assertTrue("Matching TranspondingTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 1")));
        Assert.assertTrue("Matching TranspondingTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching TranspondingTag returned via provideTag by system name", t1.equals(m.provideIdTag("LD0413275FCA")));
        Assert.assertFalse("Non-matching TranspondingTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 2")));
        Assert.assertFalse("Non-matching TranspondingTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413275FCA")));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    // Override init method so as not to load file
    // nor register shutdown task during tests.
    protected TranspondingTagManager getManager() {
        return new TranspondingTagManager() {
            @Override
            public void init() {
            }
        };
    }

}
