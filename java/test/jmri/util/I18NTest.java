/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.i18nchecker.I18nChecker;

/**
 *
 * @author rhwood
 */
public class I18NTest extends TestCase {

    static Logger log = Logger.getLogger(I18NTest.class);

    public void testI18N() throws IOException {
        File suiteDir = new File(System.getProperty("user.dir")); // root of SVN checkout
        suiteDir = suiteDir.getCanonicalFile();
        String topDirs = "."; // use "." since null or "" cause tests to fail
        Boolean allProperties = true; // test all .properties files, not just <package>/Bundle.properties
        String result = I18nChecker.runAsTest(suiteDir, topDirs, this.getUnfinishedI18NModules(), allProperties);
        if (!result.isEmpty()) {
            fail(result);
        }
    }

    public static Test suite() {
        return new TestSuite(I18NTest.class);
    }

    private Map<String, Integer> getUnfinishedI18NModules() throws IOException {
        Properties props = new Properties();
        InputStream is = new java.io.FileInputStream(new java.io.File("java/test/jmri/util/i18n_known_errors.properties"));
        try {
            props.load(is);
        } finally {
            is.close();
        }
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String key : props.stringPropertyNames()) {
            System.out.println("prop: "+key+" "+props.getProperty(key, "0"));
            int val = Integer.parseInt(props.getProperty(key, "0"));
            log.info("I18NTest expected results from file: " + key + "=" + props.getProperty(key));
            log.info("I18NTest expected results as used: " + key + "=" + val);
            result.put(key, val);
        }
        return result;
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", I18NTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

}
