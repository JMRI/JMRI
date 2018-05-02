/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.script package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.script;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        JmriScriptEngineManagerTest.class,
        ScriptFileChooserTest.class,
        ScriptOutputTest.class,
})

public class PackageTest  {
}
