package jmri.script;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriScriptEngineManagerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Test
    public void testCTor() {
        JmriScriptEngineManager t = new JmriScriptEngineManager();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriScriptEngineManagerTest.class);
    
    private Path copyDataFile(String relative) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(relative)) {
            Path p = tempFolder.newFile().toPath();
            Files.copy(is, p, StandardCopyOption.REPLACE_EXISTING);
            return p;
        }
    }
    
    private String loadReaderContents(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int l;
        while ((l = r.read(buf)) >= 0) {
            sb.append(buf, 0, l);
        }
        return sb.toString();
    }
    
    private void testScriptReadsUnchanged(String relative) throws IOException {
        Path golden = copyDataFile(relative);
        String goldenScript = new String(Files.readAllBytes(golden), "UTF-8");

        String script;
        try(Reader r = JmriScriptEngineManager.pythonEncodingFileReader(golden.toFile())) {
            script = loadReaderContents(r);
        }
        assertEquals(goldenScript, script);
    }
    
    /**
     * Loads script without any encoding directive. The Reader contents must match the
     * (UTF8) file's contents.
     */
    @Test
    public void testReadScriptWithoutDirective() throws Exception {
        testScriptReadsUnchanged("script_utf8.py");
    }
    
    /**
     * Reads a script that contains an encoding directive. The directive must
     * be removed from the script, so the script will match the golden one
     * plus an extra line. Number of lines must be the same as in the original
     * content.
     * 
     * @throws Exception 
     */
    @Test
    public void testReadScriptWithEncoding() throws Exception {
        Path golden = copyDataFile("script_iso_8859-2_golden.py");
        String goldenScript = new String(Files.readAllBytes(golden), "iso-8859-2");
        Path czech = copyDataFile("script_iso_8859-2.py");
        
        String script;
        try(Reader r = JmriScriptEngineManager.pythonEncodingFileReader(czech.toFile())) {
            script = loadReaderContents(r);
        }
        assertEquals(goldenScript, script);
        
        String[] l1 = script.split("\n");
        List<String> gl = Files.readAllLines(czech, Charset.forName("iso-8859-2"));
        assertEquals(gl.size(), l1.length);
    }
    
    /**
     * Checks that if the directive appears late in the file, it is ignored. In this case,
     * the script should be read using UTF8 and match exactly the resource contents.
     * 
     * @throws Exception 
     */
    @Test
    public void testReadTooLinesBeforeCoding() throws Exception {
        testScriptReadsUnchanged("script_toomanylines.py");
    }
     
    /**
     * Checks that if preceded by a long line, the coding directive is ignored.
     * The file should read unchanged.
     * @throws Exception 
     */
    @Test
    public void testReadTooLargeLine() throws Exception {
        testScriptReadsUnchanged("script_toolong.py");
    }
}
