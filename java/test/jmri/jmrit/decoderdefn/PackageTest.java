package jmri.jmrit.decoderdefn;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IdentifyDecoderTest.class,
        DecoderIndexFileTest.class,
        DecoderFileTest.class,
        SchemaTest.class,
        DecoderIndexBuilderTest.class,
        NameCheckActionTest.class,
        DecoderIndexCreateActionTest.class,
        InstallDecoderFileActionTest.class,
        InstallDecoderURLActionTest.class,
        PrintDecoderListActionTest.class,
        BundleTest.class,
        // Disabled until #2601 is resolved
        // DuplicateTest.class,
})

/**
 * Tests for the jmrit.decoderdefn package
 *
 * @author	Bob Jacobsen
  */
public class PackageTest {

}
