package apps;

import java.io.File;
import java.util.stream.Stream;

import jmri.configurexml.SchemaTestBase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test upper level loading of config files
 *
 * @author Bob Jacobsen Copyright 2012
 * @since 2.5.5
 */
public class ValidateConfigFilesTest extends SchemaTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("xml/config"), true, true);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void validateConfigFiles(File file, boolean pass) {
        super.validate(file, pass);
    }
}
