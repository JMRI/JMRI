package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   CanSprogConnectionConfigTest.class,
   CanisbConnectionConfigTest.class,
   CanisbSerialDriverAdapterTest.class,
   PiSprog3ConnectionConfigTest.class,
   Sprog3PlusConnectionConfigTest.class,
   Sprog3PlusSerialDriverAdapterTest.class,
   jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver package.
 *
 * @author Andrew Crosland (C) 2020
 */
public class PackageTest {
    
}
