package jmri.jmrit.decoderdefn;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks of JMRI XML Schema for decoder definition files.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        ArrayList<Arguments> files = new ArrayList<>();
        // check that name list file is valid
        files.addAll(getFiles(new File("xml/names.xml"), true, true).collect(Collectors.toList()));
        // check that the schema passes useful constructs
        files.addAll(getFiles(new File("java/test/jmri/jmrit/decoderdefn/pass"), true, true).collect(Collectors.toList()));
        // check that the schema detects errors
        files.addAll(getFiles(new File("java/test/jmri/jmrit/decoderdefn/fail"), true, false).collect(Collectors.toList()));
        // check that decoder definitions are valid
        files.addAll(getFiles(new File("xml/decoders/"), true, true).collect(Collectors.toList()));
        // check that decoderIndex is valid
        files.addAll(getFiles(new File("xml/decoderIndex.xml"), true, true).collect(Collectors.toList()));
        return files.stream();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
