package jmri;

import java.util.Arrays;
import jmri.profile.ProfileManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MetadataTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getBySystemName method, of class Metadata.
     */
    @Test
    public void testGetBySystemName() {
        // non-existant properties should return null
        Assert.assertNull("Non-existant property", Metadata.getBySystemName("non-existant-property"));
        // Java System properties are returned if in proper case
        Assert.assertEquals("Java System property in correct case", System.getProperty("path.separator"), Metadata.getBySystemName("path.separator"));
        Assert.assertNull("Java System property in wrong case", Metadata.getBySystemName("Path.Separator"));
        // Named properties are case insenitive
        Assert.assertEquals("COPYRIGHT", Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("COPYRIGHT"));
        Assert.assertEquals("copyright", Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("copyright"));
        Assert.assertEquals("CopyRight", Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("CopyRight"));
        Assert.assertEquals("cOPYrIGHT", Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("cOPYrIGHT"));
        // Test that every named property returns an expected result
        Assert.assertEquals(Metadata.JMRIVERSION, jmri.Version.name(), Metadata.getBySystemName(Metadata.JMRIVERSION));
        Assert.assertEquals(Metadata.JMRIVERCANON, jmri.Version.getCanonicalVersion(), Metadata.getBySystemName(Metadata.JMRIVERCANON));
        Assert.assertEquals(Metadata.JMRIVERMAJOR, Integer.toString(jmri.Version.major), Metadata.getBySystemName(Metadata.JMRIVERMAJOR));
        Assert.assertEquals(Metadata.JMRIVERMINOR, Integer.toString(jmri.Version.minor), Metadata.getBySystemName(Metadata.JMRIVERMINOR));
        Assert.assertEquals(Metadata.JMRIVERTEST, Integer.toString(jmri.Version.test), Metadata.getBySystemName(Metadata.JMRIVERTEST));
        Assert.assertEquals(Metadata.JVMVERSION, System.getProperty("java.version", "<unknown>"), Metadata.getBySystemName(Metadata.JVMVERSION));
        Assert.assertEquals(Metadata.JVMVENDOR, System.getProperty("java.vendor", "<unknown>"), Metadata.getBySystemName(Metadata.JVMVENDOR));
        Assert.assertEquals(Metadata.ACTIVEPROFILE, ProfileManager.getDefault().getActiveProfileName(), Metadata.getBySystemName(Metadata.ACTIVEPROFILE));
        Assert.assertEquals(Metadata.COPYRIGHT, jmri.Version.getCopyright(), Metadata.getBySystemName(Metadata.COPYRIGHT));
    }

    /**
     * Test of getSystemNameArray method, of class Metadata.
     */
    @Test
    public void testGetSystemNameArray() {
        Assert.assertArrayEquals("Known property names", new String[]{Metadata.JMRIVERSION,
            Metadata.JMRIVERCANON,
            Metadata.JMRIVERMAJOR,
            Metadata.JMRIVERMINOR,
            Metadata.JMRIVERTEST,
            Metadata.JVMVERSION,
            Metadata.JVMVENDOR,
            Metadata.ACTIVEPROFILE,
            Metadata.COPYRIGHT}, Metadata.getSystemNameArray());
    }

    /**
     * Test of getSystemNameList method, of class Metadata.
     */
    @Test
    public void testGetSystemNameList() {
        Assert.assertEquals("Known property names", Arrays.asList(new String[]{Metadata.JMRIVERSION,
            Metadata.JMRIVERCANON,
            Metadata.JMRIVERMAJOR,
            Metadata.JMRIVERMINOR,
            Metadata.JMRIVERTEST,
            Metadata.JVMVERSION,
            Metadata.JVMVENDOR,
            Metadata.ACTIVEPROFILE,
            Metadata.COPYRIGHT}), Metadata.getSystemNameList());
    }

}
