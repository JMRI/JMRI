package jmri.util.junit.annotations;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.util.junit.annotations")

/**
 * Invokes complete set of tests in the jmri.util.junit.annotations tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest  {
}
