package jmri.util.junit.rules;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.util.junit.rules")

/**
 * Invokes complete set of tests in the jmri.util.junit.rules tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest  {
}
