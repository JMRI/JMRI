package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
        File f = new File(FileUtil.getUserFilesPath() + "temp" + File.separator + "CsvImporter.test.xml");
        // recreate it
        if (f.exists()) {
            Assertions.assertTrue(f.delete());
        }
        try (PrintStream p = new PrintStream(new FileOutputStream(f))) {
            p.print(contents);
        }

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
        
        // following messages don't seem to always happen in AppVeyor and Travis CI
        jmri.util.JUnitAppender.suppressWarnMessage("CV1 was in import file, but not defined by the decoder definition");
        jmri.util.JUnitAppender.suppressWarnMessage("CV2 was in import file, but not defined by the decoder definition");
        
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CsvImporterTest.class);

}
