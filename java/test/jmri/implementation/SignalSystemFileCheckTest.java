package jmri.implementation;

import java.io.File;
import jmri.configurexml.SchemaTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests of the signal system definition files.
 * <p>
 * Checks all files in the distribution directory
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
@RunWith(Parameterized.class)
public class SignalSystemFileCheckTest extends SchemaTestBase {

    @Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        return getFiles(new File("xml/signals"), true, true);
    }

    public SignalSystemFileCheckTest(File file, boolean pass) {
        super(file, pass);
    }
}
