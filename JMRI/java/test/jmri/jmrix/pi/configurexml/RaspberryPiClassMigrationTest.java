package jmri.jmrix.pi.configurexml;

import apps.tests.Log4JFixture;
import java.util.HashMap;
import java.util.Map;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class RaspberryPiClassMigrationTest {

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

    @Test
    public void testGetMigrations() {
        RaspberryPiClassMigration instance = new RaspberryPiClassMigration();
        Map<String, String> expResult = new HashMap<>();
        expResult.put("jmri.jmrix.pi.configurexml.ConnectionConfigXml", "jmri.jmrix.pi.configurexml.RaspberryPiConnectionConfigXml");
        Assert.assertEquals(expResult, instance.getMigrations());
    }

}
