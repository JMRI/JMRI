package jmri.jmrit.beantable.beanedit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    BeanItemPanelTest.class,
    BlockEditActionTest.class,
    SensorDebounceEditActionTest.class,
    SensorEditActionTest.class,
    TurnoutEditActionTest.class,
    SensorPullUpEditActionTest.class,
    BeanEditItemTest.class
})

/**
 * Tests for classes in the jmri.jmrit.beantable.beanedit package
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class PackageTest {
}
