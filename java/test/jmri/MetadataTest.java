package jmri;

import java.util.Arrays;

import jmri.profile.ProfileManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MetadataTest {

    /**
     * Test of getBySystemName method, of class Metadata.
     */
    @Test
    public void testGetBySystemName() {
        // non-existant properties should return null
        assertNull( Metadata.getBySystemName("non-existant-property"), "Non-existant property");
        // Java System properties are returned if in proper case
        assertEquals( System.getProperty("path.separator"), Metadata.getBySystemName("path.separator"), "Java System property in correct case");
        assertNull( Metadata.getBySystemName("Path.Separator"), "Java System property in wrong case");
        // Named properties are case insenitive
        assertEquals( Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("COPYRIGHT"), "COPYRIGHT");
        assertEquals( Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("copyright"), "copyright");
        assertEquals( Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("CopyRight"), "CopyRight");
        assertEquals( Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.getBySystemName("cOPYrIGHT"), "cOPYrIGHT");
        // Test that every named property returns an expected result
        assertEquals( jmri.Version.name(), Metadata.getBySystemName(Metadata.JMRIVERSION), Metadata.JMRIVERSION);
        assertEquals( jmri.Version.getCanonicalVersion(), Metadata.getBySystemName(Metadata.JMRIVERCANON), Metadata.JMRIVERCANON);
        assertEquals( Integer.toString(jmri.Version.major), Metadata.getBySystemName(Metadata.JMRIVERMAJOR), Metadata.JMRIVERMAJOR);
        assertEquals( Integer.toString(jmri.Version.minor), Metadata.getBySystemName(Metadata.JMRIVERMINOR), Metadata.JMRIVERMINOR);
        assertEquals( Integer.toString(jmri.Version.test), Metadata.getBySystemName(Metadata.JMRIVERTEST), Metadata.JMRIVERTEST);
        assertEquals( System.getProperty("java.version", "<unknown>"), Metadata.getBySystemName(Metadata.JVMVERSION), Metadata.JVMVERSION);
        assertEquals( System.getProperty("java.vendor", "<unknown>"), Metadata.getBySystemName(Metadata.JVMVENDOR), Metadata.JVMVENDOR);
        assertEquals( ProfileManager.getDefault().getActiveProfileName(), Metadata.getBySystemName(Metadata.ACTIVEPROFILE), Metadata.ACTIVEPROFILE);
        assertEquals( jmri.Version.getCopyright(), Metadata.getBySystemName(Metadata.COPYRIGHT), Metadata.COPYRIGHT);
    }

    /**
     * Test of getSystemNameArray method, of class Metadata.
     */
    @Test
    public void testGetSystemNameArray() {
        assertArrayEquals( new String[]{Metadata.JMRIVERSION,
            Metadata.JMRIVERCANON,
            Metadata.JMRIVERMAJOR,
            Metadata.JMRIVERMINOR,
            Metadata.JMRIVERTEST,
            Metadata.JVMVERSION,
            Metadata.JVMVENDOR,
            Metadata.ACTIVEPROFILE,
            Metadata.COPYRIGHT},
                Metadata.getSystemNameArray(),
            "Known property names");
    }

    /**
     * Test of getSystemNameList method, of class Metadata.
     */
    @Test
    public void testGetSystemNameList() {
        assertEquals( Arrays.asList(new String[]{Metadata.JMRIVERSION,
            Metadata.JMRIVERCANON,
            Metadata.JMRIVERMAJOR,
            Metadata.JMRIVERMINOR,
            Metadata.JMRIVERTEST,
            Metadata.JVMVERSION,
            Metadata.JVMVENDOR,
            Metadata.ACTIVEPROFILE,
            Metadata.COPYRIGHT}),
                Metadata.getSystemNameList(),
            "Known property names");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
