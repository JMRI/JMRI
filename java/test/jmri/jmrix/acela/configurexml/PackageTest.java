package jmri.jmrix.acela.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   AcelaLightManagerXmlTest.class,
   AcelaSensorManagerXmlTest.class,
   AcelaSignalHeadXmlTest.class,
   AcelaTurnoutManagerXmlTest.class
})
/**
 * Tests for the jmri.jmrix.acela.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
