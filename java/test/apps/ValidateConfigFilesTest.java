package apps;

import java.io.File;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test upper level loading of config files
 *
 * @author Bob Jacobsen Copyright 2012
 * @since 2.5.5
 */
@RunWith(Parameterized.class)
public class ValidateConfigFilesTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        return getFiles(new File("xml/config"), true, true);
    }

    public ValidateConfigFilesTest(File file, boolean pass) {
        super(file, pass);
    }
}
