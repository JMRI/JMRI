package jmri.progdebugger;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.progdebugger")

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <p>
 * Due to existing package and class names, this is both the test suite for the
 * package, but also contains some tests for the ProgDebugger class.
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002
 */
public class PackageTest  {
}
