package jmri;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks of JMRI ml/sample files; here because where else would you put it?
 *
 * @author Bob Jacobsen Copyright 2009, 2016
 * @since 4.3.3
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        // the following are just tested for schema pass/fail, not load/store
        // could recurse, but xml/samples/javaone/Throttles.xml fails
        // (was not tested prior to 4.7.1)
        return getFiles(new File("xml/samples"), false, true);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }

}
