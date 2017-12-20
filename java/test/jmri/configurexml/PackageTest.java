package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SchemaTest.class,
        LoadAndCheckTest.class,
        LoadAndStoreTest.class,
        ConfigXmlManagerTest.class,
        BlockManagerXmlTest.class,
        SectionManagerXmlTest.class,
        TransitManagerXmlTest.class,
        DefaultJavaBeanConfigXMLTest.class,
        BundleTest.class,
        DccLocoAddressXmlTest.class,
        JmriConfigureXmlExceptionTest.class,
        jmri.configurexml.turnoutoperations.PackageTest.class,
        jmri.configurexml.swing.PackageTest.class,
        ErrorHandlerTest.class,
        LoadXmlConfigActionTest.class,
        LoadXmlUserActionTest.class,
        LocoAddressXmlTest.class,
        SaveMenuTest.class,
        StoreXmlAllActionTest.class,
        StoreXmlConfigActionTest.class,
        StoreXmlUserActionTest.class,
        TurnoutOperationManagerXmlTest.class,
        ErrorMemoTest.class,
        ClassMigrationManagerTest.class,
        DefaultClassMigrationTest.class,
})

/**
 * Test the jmri.configxml package.
 *
 * @author	Bob Jacobsen
 */
public class PackageTest {
}

