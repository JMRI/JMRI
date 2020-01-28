package jmri.jmrit.progsupport;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


/**
 * Invokes complete set of tests in the jmri.jmrit.progsupport tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.jmriti.progsupport")
public class PackageTest {
}
