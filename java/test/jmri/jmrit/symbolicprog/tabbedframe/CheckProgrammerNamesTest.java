package jmri.jmrit.symbolicprog.tabbedframe;

import java.io.File;
import org.junit.*;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007, 2008
 * @see jmri.jmrit.XmlFile
 */
public class CheckProgrammerNamesTest {

    @Test
    public void testAdvanced() {
        checkAgainstNames(new File("xml/programmers/Advanced.xml"));
    }

    @Test
    public void testComprehensive() {
        checkAgainstNames(new File("xml/programmers/Comprehensive.xml"));
    }

    @Test
    public void testBasic() {
        checkAgainstNames(new File("xml/programmers/Basic.xml"));
    }

    @Test
    public void testTrainShowBasic() {
        checkAgainstNames(new File("xml/programmers/TrainShowBasic.xml"));
    }

    @Test
    public void testSampleClub() {
        checkAgainstNames(new File("xml/programmers/Sample Club.xml"));
    }

    @Test
    public void testCustom() {
        checkAgainstNames(new File("xml/programmers/Custom.xml"));
    }

    @Test
    public void testTutorial() {
        checkAgainstNames(new File("xml/programmers/Tutorial.xml"));
    }

    @Test
    public void testRegisters() {
        checkAgainstNames(new File("xml/programmers/Registers.xml"));
    }

    @Test
    @Ignore("Preexisting failing condition")
    public void testESU() {
        checkAgainstNames(new File("xml/programmers/ESU.xml"));
    }

    @Test
    @Ignore("Preexisting failing condition")
    public void testZimo() {
        checkAgainstNames(new File("xml/programmers/Zimo.xml"));
    }

    @Test
    public void testComprehensiveComplete() {
        checkComplete(new File("xml/programmers/Comprehensive.xml"));
    }

    @Test
    public void testAdvancedComplete() {
        checkComplete(new File("xml/programmers/Advanced.xml"));
    }

    // utilities
    public void checkAgainstNames(File file) {
        String result = ProgCheckAction.checkMissingNames(file);
        if (!result.equals("")) {
            Assert.fail(result);
        }
    }

    public void checkComplete(File file) {
        String result = ProgCheckAction.checkIncompleteComprehensive(file);
        if (!result.equals("")) {
            Assert.fail(result);
        }
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
