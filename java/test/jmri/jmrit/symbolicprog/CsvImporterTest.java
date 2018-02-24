package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import jmri.util.FileUtil;
import javax.swing.JLabel;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CsvImporterTest {

    public File makeTempFile(String contents) throws IOException {
        // create a file
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File f = new java.io.File(FileUtil.getUserFilesPath() + "temp" + File.separator + "CsvImporter.test.xml");
        // recreate it
        if (f.exists()) {
            f.delete();
        }
        PrintStream p = new PrintStream(new FileOutputStream(f));
        p.print(contents);
        p.close();

        return f;
    }

    @Test
    public void testCTor() throws IOException {
        // create a file
        String s = "CV1,0\n"
                + "CV2,1\n";
        File f = makeTempFile(s);
        CvTableModel tm = new CvTableModel(new JLabel(), null);
        CsvImporter t = new CsvImporter(f,tm);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CsvImporterTest.class);

}
