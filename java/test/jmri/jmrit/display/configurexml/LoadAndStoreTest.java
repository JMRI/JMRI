// LoadAndStoreTest.java
package jmri.jmrit.display.configurexml;

import jmri.configurexml.LoadAndStoreTestBase;
import junit.framework.Test;

/**
 * Test that configuration files can be read and then stored again consistently.
 * When done across various versions of schema, this checks ability to read
 * older files in newer versions; completeness of reading code; etc.
 * <p>
 * Functional checks, that e.g. check the details of a specific type are being
 * read properly, should go into another type-specific test class.
 * <p>
 * The functionality comes from the common base class, this is just here to
 * insert the test suite into the JUnit hierarchy at the right place.
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTest extends jmri.configurexml.LoadAndStoreTestBase {

    // from here down is testing infrastructure
    // Note setup() and teardown are provided from base class, and 
    // need to be invoked if you add methods here
    public LoadAndStoreTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LoadAndStoreTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return LoadAndStoreTestBase.makeSuite("java/test/jmri/jmrit/display/configurexml/");
    }
}
