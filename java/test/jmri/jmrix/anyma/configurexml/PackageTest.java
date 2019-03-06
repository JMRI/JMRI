package jmri.jmrix.anyma.configurexml;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AnymaDMX_ConnectionConfigXmlTest.class,
    UsbLightManagerXmlTest.class,})

/**
 * Tests for the jmri.jmrix.acela.configurexml package.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class PackageTest {
}
