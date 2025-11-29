package jmri.tracktiles;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks of JMRI XML Schema for track definition files.
 *
 * @author Bob Jacobsen Copyright 2025
 * @since 5.17.8
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        ArrayList<Arguments> files = new ArrayList<>();
        // check that the schema passes useful constructs
        files.addAll(getFiles(new File("java/test/jmri/tracktiles/pass"), true, true).collect(Collectors.toList()));
        // check that the schema detects errors
        files.addAll(getFiles(new File("java/test/jmri/tracktiles/fail"), true, false).collect(Collectors.toList()));
        // check that decoder definitions are valid
        files.addAll(getFiles(new File("xml/tracktiles/"), true, true).collect(Collectors.toList()));
        return files.stream();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
