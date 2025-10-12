package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import java.text.ParseException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class RegexFormatterTest {

    @Test
    public void testCTor() {
        RegexFormatter t = new RegexFormatter();
        assertNotNull( t, "exists");
    }

    @Test
    public void testStringCTor() {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        assertNotNull( t, "exists");
    }

   
    @Test
    public void testPatternCTor() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter(p);
        assertNotNull( t, "exists");
    }

    @Test
    public void testGetPattern() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter(p);
        assertEquals( p, t.getPattern(), "patterns match");
    }

    @Test
    public void testSetAndGetPattern() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter();
        t.setPattern(p);
        assertEquals( p, t.getPattern(), "patterns match");
    }

    @Test
    public void testStringToValue() throws ParseException {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        assertNotNull( t.stringToValue("A1234"), "exists");
    }

    @Test
    public void testStringToValueFailure() throws ParseException {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        Exception ex = assertThrows(ParseException.class, () -> t.stringToValue("AB1234"));
        assertNotNull(ex);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RegexFormaterTest.class);

}
