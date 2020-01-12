package jmri.server.json.operations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    JsonOperationsTest.class,
    JsonOperationsHttpServiceTest.class,
    JsonOperationsSocketServiceTest.class
})
/**
 * Tests for the jmri.server.json.operations package
 *
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
