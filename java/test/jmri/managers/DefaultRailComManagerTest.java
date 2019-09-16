package jmri.managers;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        Assert.assertNotNull("IdTag is not null", t);
    }

    @Override
    @Test
    public void testIdTagNames() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.createNewIdTag("RD0413276BC1", "Test Tag");

        Assert.assertEquals("IdTag system name is 'RD0413276BC1'", "RD0413276BC1", t.getSystemName());
        Assert.assertEquals("IdTag user name is 'Test Tag'", "Test Tag", t.getUserName());
        Assert.assertEquals("IdTag tag id is '0413276BC1'", "0413276BC1", t.getTagID());
    }

    @Override
    @Test
    public void testIdTagSingleRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.newIdTag("RD0413276BC1", "Test Tag");

        Assert.assertNotNull("Returned IdTag is not null", t);

        Assert.assertNotNull("Get by system name is not null", m.getBySystemName("RD0413276BC1"));
        Assert.assertNotNull("Get by user name is not null", m.getByUserName("Test Tag"));
        Assert.assertNotNull("Get by tag id is not null", m.getByTagID("0413276BC1"));

        Assert.assertNotNull("Get IdTag using system name is not null", m.getIdTag("RD0413276BC1"));
        Assert.assertNotNull("Get IdTag using user name is not null", m.getIdTag("Test Tag"));
        Assert.assertNotNull("Get IdTag using tag id is not null", m.getIdTag("0413276BC1"));

        Assert.assertTrue("Matching IdTag returned from manager by system name", t.getSystemName().equals(m.getBySystemName("RD0413276BC1").getSystemName()));
        Assert.assertTrue("Matching IdTag returned from manager by user name", t.getUserName().equals(m.getByUserName("Test Tag").getUserName()));
        Assert.assertTrue("Matching IdTag returned from manager by tag id", t.getTagID().equals(m.getByTagID("0413276BC1").getTagID()));

        Assert.assertNull("Null Object returned from manager by system name", m.getBySystemName("RD99999999"));
        Assert.assertNull("Null Object returned from manager by user name", m.getBySystemName("This doesn't exist"));
        Assert.assertNull("Null Object returned from manager by tagID", m.getBySystemName("XXXXXXXXXX"));
    }

    @Override
    @Test
    public void testIdTagMultiRetrieval() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("RD0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("RD0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created IdTags are different", t1.equals(t2));

        Assert.assertTrue("Matching IdTag returned from manager by system name", t1.equals(m.getBySystemName("RD0413276BC1")));
        Assert.assertTrue("Matching IdTag returned from manager by user name", t1.equals(m.getByUserName("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned from manager by tag id", t1.equals(m.getByTagID("0413276BC1")));

        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using system name", t1.equals(m.getIdTag("RD0413276BC1")));
        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using user name", t1.equals(m.getIdTag("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned from manager via getRfidTag using tag id", t1.equals(m.getIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned from manager by system name", t2.equals(m.getBySystemName("RD0413276BC1")));
        Assert.assertFalse("Non-matching IdTag returned from manager by user name", t2.equals(m.getByUserName("Test Tag 1")));
        Assert.assertFalse("Non-matching IdTag returned from manager by tag id", t2.equals(m.getByTagID("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using system name", t2.equals(m.getIdTag("RD0413276BC1")));
        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using user name", t2.equals(m.getIdTag("Test Tag 1")));
        Assert.assertFalse("Non-matching IdTag returned from manager via getRfidTag using tag id", t2.equals(m.getIdTag("0413276BC1")));
    }

    @Override
    @Test
    public void testIdTagProviderCreate() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t = m.provideIdTag("0413276BC1");

        Assert.assertNotNull("IdTag is not null", t);
        Assert.assertEquals("IdTag System Name is 'RD0413276BC1'", "RD0413276BC1", t.getSystemName());
        Assert.assertEquals("IdTag display name is system name", "RD0413276BC1", t.getDisplayName());
        Assert.assertEquals("IdTag tag ID is 0413276BC1", "0413276BC1", t.getTagID());
        Assert.assertNull("IdTag user name is blank", t.getUserName());

        t.setUserName("Test Tag");

        Assert.assertNotNull("IdTag user name is not blank", t.getUserName());
        Assert.assertEquals("IdTag display name is user name", "Test Tag", t.getDisplayName());
    }

    @Test
    @Override
    public void testIdTagProviderGet() {
        DefaultIdTagManager m = (DefaultIdTagManager)l;
        IdTag t1 = m.newIdTag("RD0413276BC1", "Test Tag 1");
        IdTag t2 = m.newIdTag("RD0413275FCA", "Test Tag 2");

        Assert.assertFalse("Created IdTags are different", t1.equals(t2));

        Assert.assertTrue("Matching IdTag returned via provideTag by system name", t1.equals(m.provideIdTag("RD0413276BC1")));
        Assert.assertTrue("Matching IdTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 1")));
        Assert.assertTrue("Matching IdTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413276BC1")));

        Assert.assertFalse("Non-matching IdTag returned via provideTag by system name", t1.equals(m.provideIdTag("RD0413275FCA")));
        Assert.assertFalse("Non-matching IdTag returned via provideTag by user name", t1.equals(m.provideIdTag("Test Tag 2")));
        Assert.assertFalse("Non-matching IdTag returned via provideTag by tag ID", t1.equals(m.provideIdTag("0413275FCA")));
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        jmri.InstanceManager.setDefault(jmri.IdTagManager.class,new ProxyIdTagManager());
        l = getManager();
    }

    @After
    @Override
    public void tearDown() {
        l = null;
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
