package jmri.jmrix.xpa.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.xpa.swing.xpamon.PackageTest.class,
    jmri.jmrix.xpa.swing.xpaconfig.PackageTest.class,
    jmri.jmrix.xpa.swing.packetgen.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.xpa.swing package.
 *
 * @author Paul Bender Copyright 2016
 */
public class PackageTest {

}
