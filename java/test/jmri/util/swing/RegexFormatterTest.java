package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class RegexFormatterTest {

    @Test
    public void testCTor() {
        RegexFormatter t = new RegexFormatter();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringCTor() {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        Assert.assertNotNull("exists",t);
    }

   
    @Test
    public void testPatternCTor() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter(p);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetPattern() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter(p);
        Assert.assertEquals("patterns match",p,t.getPattern());
    }

    @Test
    public void testSetAndGetPattern() {
        Pattern p = Pattern.compile("[A-Za-z]\\d*");
        RegexFormatter t = new RegexFormatter();
        t.setPattern(p);
        Assert.assertEquals("patterns match",p,t.getPattern());
    }

    @Test
    public void testStringToValue() throws ParseException {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        Assert.assertNotNull("exists",t.stringToValue("A1234"));
    }

    @Test(expected=ParseException.class)
    public void testStringToValueFailure() throws ParseException {
        RegexFormatter t = new RegexFormatter("[A-Za-z]\\d*");
        Assert.assertNotNull("exists",t.stringToValue("AB1234"));
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

    // private final static Logger log = LoggerFactory.getLogger(RegexFormaterTest.class);

}
