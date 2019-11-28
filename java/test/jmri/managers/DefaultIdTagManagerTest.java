package jmri.managers;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.junit.*;

/**
 * Tests for the jmri.managers.DefaultIdTagManager class.
 *
 * @author	Matthew Harris Copyright (C) 2011
 */
public class DefaultIdTagManagerTest extends AbstractProvidingManagerTestBase<IdTagManager,IdTag> {

    @Test
    public void testIdTagCreation() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("ID0413276BC1", "Test Tag");

        Assert.assertNotNull("IdTag is not null", t);
    }

    @Test
    public void testIdTagNames() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("ID0413276BC1", "Test Tag");

        Assert.assertEquals("IdTag system name is 'ID0413276BC1'", "ID0413276BC1", t.getSystemName());
        Assert.assertEquals("IdTag user name is 'Test Tag'", "Test Tag", t.getUserName());
        Assert.assertEquals("IdTag tag id is '0413276BC1'", "0413276BC1", t.getTagID());
    }

    @Test
    public void testIdTagSingleRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.newIdTag("ID0413276BC1", "Test Tag");

        Assert.assertNotNull("Returned IdTag is not null", t);

        Assert.assertNotNull("Get by system name is not null", m.getBySystemName("ID0413276BC1"));
        Assert.assertNotNull("Get by user name is not null", m.getByUserName("Test Tag"));
        Assert.assertNotNull("Get by tag id is not null", m.getByTagID("0413276BC1"));

        Assert.assertNotNull("Get IdTag using system name is not null", m.getIdTag("ID0413276BC1"));
        Assert.assertNotNull("Get IdTag using user name is not null", m.getIdTag("Test Tag"));
        Assert.assertNotNull("Get IdTag using tag id is not null", m.getIdTag("0413276BC1"));

        Assert.assertTrue("Matching IdTag returned from manager by system name", t.getSystemName().equals(m.getBySystemName("ID0413276BC1").getSystemName()));
        Assert.assertTrue("Matching IdTag returned from manager by user name", t.getUserName().equals(m.getByUserName("Test Tag").getUserName()));
        Assert.assertTrue("Matching IdTag returned from manager by tag id", t.getTagID().equals(m.getByTagID("0413276BC1").getTagID()));

        Assert.assertNull("Null Object returned from manager by system name", m.getBySystemName("ID99999999"));
        Assert.assertNull("Null Object returned from manager by user name", m.getBySystemName("This doesn't exist"));
        Assert.assertNull("Null Object returned from manager by tagID", m.getBySystemName("XXXXXXXXXX"));
    }

    @Test
    public void testIdTagMultiRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("ID0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("ID0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created IdTags are different", t1.equals(t2));

        Assert.assertTrue("Matching IdTag returned from manager by system name", t1.equals(m.getBySystemName("ID0413276BC1")));
        Assert.assertTrue("Matching IdTag returned from manager by user name", t1.equals(m.getByUserName("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned from manager by tag id", t1.equals(m.getByTagID("0413276BC1")));

        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using system name", t1.equals(m.getIdTag("ID0413276BC1")));
        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using user name", t1.equals(m.getIdTag("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using tag id", t1.equals(m.getIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned from manager by system name", t2.equals(m.getBySystemName("ID0413276BC1")));
        Assert.assertFalse("Non-matching IdTag returned from manager by user name", t2.equals(m.getByUserName("Test Tag 1")));
        Assert.assertFalse("Non-matching IdTag returned from manager by tag id", t2.equals(m.getByTagID("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using system name", t2.equals(m.getIdTag("ID0413276BC1")));
        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using user name", t2.equals(m.getIdTag("Test Tag 1")));
        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using tag id", t2.equals(m.getIdTag("0413276BC1")));
    }

    @Test
    public void testIdTagProviderCreate() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.provideIdTag("0413276BC1");

        Assert.assertNotNull("IdTag is not null", t);
        Assert.assertEquals("IdTag System Name is 'ID0413276BC1'", "ID0413276BC1", t.getSystemName());
        Assert.assertEquals("IdTag display name is system name", "ID0413276BC1", t.getDisplayName());
        Assert.assertEquals("IdTag tag ID is 0413276BC1", "0413276BC1", t.getTagID());
        Assert.assertNull("IdTag user name is blank", t.getUserName());

        t.setUserName("Test Tag");

        Assert.assertNotNull("IdTag user name is not blank", t.getUserName());
        Assert.assertEquals("IdTag display name is user name", "Test Tag", t.getDisplayName());
    }

    @Test
    public void testIdTagProviderGet() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("ID0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("ID0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created IdTags are different", t1.equals(t2));

        Assert.assertTrue("Matching IdTag returned via provideTag by system name", t1.equals(m.provideIdTag("ID0413276BC1")));
        Assert.assertTrue("Matching IdTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned via provideTag by system name", t1.equals(m.provideIdTag("ID0413275FCA")));
        Assert.assertFalse("Non-matching IdTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 2")));
        Assert.assertFalse("Non-matching IdTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413275FCA")));
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
        l = getManager();
    }

    @After
    public void tearDown() throws Exception {
        l = null;
        jmri.util.JUnitUtil.tearDown();
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
