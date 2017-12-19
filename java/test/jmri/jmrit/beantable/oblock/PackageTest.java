package jmri.jmrit.beantable.oblock;

import jmri.util.JUnitUtil;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        BlockPathTableModelTest.class,
        PathTurnoutTableModelTest.class,
        TableFramesTest.class,
        OBlockTableModelTest.class,
        BlockPortalTableModelTest.class,
        PortalTableModelTest.class,
        SignalTableModelTest.class,
        DnDJTableTest.class,
})

/**
 * Tests for classes in the jmri.jmrit.beantable.oblock package
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class PackageTest{
}
