package jmri.jmrit.display;

import java.io.File;
import java.util.ArrayList;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Checks of JMRI XML Schema
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 */
@RunWith(Parameterized.class)
public class SchemaTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        ArrayList<Object[]> files = new ArrayList<>();
        // the following are just tested for schema pass/fail, not load/store
        files.addAll(getFiles(new File("java/test/jmri/jmrit/display/verify"), true, true));
        // also tested for load/store
        files.addAll(getFiles(new File("java/test/jmri/jmrit/display/load"), true, true));
        return files;
    }

    public SchemaTest(File file, boolean pass) {
        super(file, pass);
    }
}
