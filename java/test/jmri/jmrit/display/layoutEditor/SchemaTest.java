package jmri.jmrit.display.layoutEditor;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks of JMRI XML Schema
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        return setTestFilesBelowThisPath("java/test/jmri/jmrit/display/layoutEditor");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
