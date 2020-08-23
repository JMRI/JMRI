package jmri.jmrix.openlcb.configurexml;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks of JMRI XML Schema for OpenLCB files.
 *
 * @author Bob Jacobsen Copyright 2018
 * @since 2.9.3
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        return setTestFilesBelowThisPath("java/test/jmri/jmrix/openlcb/configurexml");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
