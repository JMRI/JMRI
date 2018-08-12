// PackageTest
package jmri.util.docbook;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RevHistoryTest.class,
        jmri.util.docbook.configurexml.PackageTest.class,
        RevisionTest.class,
})

/**
 * Tests for the jmri.util.docbook package
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class PackageTest  {
}
