package jmri.util.junit;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.util.junit")

/**
 * Invokes complete set of tests in the jmri.util.junit tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest  {
}
