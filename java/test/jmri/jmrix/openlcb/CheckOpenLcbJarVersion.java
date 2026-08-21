package jmri.jmrix.openlcb;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check the version of the openlcb.jar file.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class CheckOpenLcbJarVersion {

    @Test
    public void testAddressOK() {

        String cp = System.getProperty("java.class.path");
        String[] cpSorted = cp.split(":");
        Arrays.sort(cpSorted);
        for (var s : cpSorted) {
            System.out.format("Classpath: %s%n", s);
        }
        System.out.format("Version: %s%n", org.openlcb.Version.libVersion());

        var packages = ClassLoader.getSystemClassLoader().getDefinedPackages();
        Arrays.sort(packages, (Package t1, Package t2) -> t1.getName().compareTo(t2.getName()));
        for (var p : packages) {
            System.out.format("Package: %s%n", p.getName());
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
