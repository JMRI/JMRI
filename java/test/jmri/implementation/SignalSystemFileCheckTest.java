package jmri.implementation;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of the signal system definition files.
 * <p>
 * Checks all files in the distribution directory
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class SignalSystemFileCheckTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("xml/signals"), true, true);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
