package jmri.jmrit.symbolicprog.tabbedframe;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007, 2008
 * @see jmri.jmrit.XmlFile
 */
public class SchemaTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("xml/programmers"), true, true);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void schemaTest(File file, boolean pass) {
        super.validate(file, pass);
    }
}
