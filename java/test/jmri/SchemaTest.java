package jmri;

import java.io.File;
import jmri.configurexml.SchemaTestBase;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Checks of JMRI ml/sample files; here because where else would you put it?
 *
 * @author Bob Jacobsen Copyright 2009, 2016
 * @since 4.3.3
 */
@RunWith(Parameterized.class)
public class SchemaTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        // the following are just tested for schema pass/fail, not load/store
        // could recurse, but xml/samples/javaone/Throttles.xml fails
        // (was not tested prior to 4.7.1)
        return getFiles(new File("xml/samples"), false, true);
    }

    public SchemaTest(File file, boolean pass) {
        super(file, pass);
    }

}
