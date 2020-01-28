package jmri.swing;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

import org.junit.runners.Suite.SuiteClasses;

import jmri.util.swing.DedupingPropertyChangeListenerTest;

/**
 * Invokes complete set of tests in the jmri.swing tree
 *
 * @author	Bob Jacobsen Copyright 2014
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.swing")
public class PackageTest {
}
