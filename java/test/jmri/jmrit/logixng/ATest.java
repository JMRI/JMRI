package jmri.jmrit.logixng;

import java.io.*;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG
 *
 * @author Daniel Bergqvist 2018
 */
public class ATest {

    @Test
    public void testA() throws FileNotFoundException, IOException {
        boolean foundTest = false;
        int count = 0;
        String lastLine = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/daniel_data/Jigsaw_Puzzle/job-logs.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.substring("2024-09-20T18:34:05.9933458Z ".length());
                if ("[INFO]  T E S T S".equals(line.trim())) {
                    foundTest = true;
                    continue;
                }
                if (!foundTest) continue;
                if ("[INFO] -------------------------------------------------------".equals(line)) continue;
                if (!line.startsWith("WARN  - Found remnant thread ")) continue;
                if (line.equals(lastLine)) continue;
                lastLine = line;

                if (line.startsWith("WARN  - Found remnant thread \"DCC4PC Sensor Poll\" in group \"main\" ")) continue;

                log.error(line);
                if (++count >= 10000) break;
            }
        }
        log.error("Count: {}", count);
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

     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ATest.class);

}
