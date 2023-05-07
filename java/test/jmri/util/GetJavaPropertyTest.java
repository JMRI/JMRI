package jmri.util;

import java.io.*;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GetJavaPropertyTest {

    // no testCtor as test class only supplies static methods

    @Test
    public void testNotAProperty() {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bos, true);
        PrintStream oldStream = System.out;
        
        try {
            System.setOut(printStream);
            Assertions.assertDoesNotThrow(() -> {
                GetJavaProperty.main(new String[]{"jmri.shutdownmanager"});
            });
            bos.flush();
        } catch(IOException ex) {
            Assertions.fail("Could not flush temporary stream", ex);
        } finally {
            System.setOut(oldStream);
        }

        Assertions.assertEquals(System.getProperty("jmri.shutdownmanager")
            + System.lineSeparator(), bos.toString());
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(GetJavaPropertyTest.class);

}
