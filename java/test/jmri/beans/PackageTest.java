package jmri.beans;

import jmri.util.JUnitUtil;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeansTest.class,
        UnboundBeanTest.class,
        ArbitraryPropertySupportTest.class,
        UnboundArbitraryBeanTest.class,
	    ConstrainedArbitraryBeanTest.class,
})

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class PackageTest {
}

