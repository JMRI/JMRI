package jmri.managers.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AbstractNamedBeanManagerConfigXMLTest.class,
        AbstractSignalHeadManagerXmlTest.class,
	DefaultConditionalManagerXmlTest.class,
	DefaultLogixManagerXmlTest.class,
	DefaultMemoryManagerXmlTest.class,
	DefaultRouteManagerXmlTest.class,
	DefaultSignalGroupManagerXmlTest.class,
	DefaultSignalMastLogicManagerXmlTest.class,
	DefaultSignalMastManagerXmlTest.class,
	DefaultUserMessagePreferencesXmlTest.class,
	ManagerDefaultSelectorXmlTest.class,
	ProxyTurnoutManagerXmlTest.class
})


/**
 * Invoke complete set of tests for the jmri.managers.configurexml package
 *
 * @author	Bob Jacobsen, Copyright (C) 2009, 2010
 */
public class PackageTest {
}
