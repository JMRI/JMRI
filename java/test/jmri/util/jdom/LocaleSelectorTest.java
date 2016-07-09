package jmri.util.jdom;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Tests for the jmri.util.LocaleSelector class.
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class LocaleSelectorTest extends TestCase {

    public void testFindDefault() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Namespace xml = Namespace.XML_NAMESPACE;
        Element el = new Element("foo")
                .setAttribute("temp", "a")
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "hh", xml)
                        .addContent("b")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "a", result);
    }

    public void testFindFullCode() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Namespace xml = Namespace.XML_NAMESPACE;
        Element el = new Element("foo")
                .setAttribute("temp", "a")
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "aa_BB", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl_KL", xml)
                        .addContent("c")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "c", result);
    }

    public void testFindPartialCode() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Namespace xml = Namespace.XML_NAMESPACE;
        Element el = new Element("foo")
                .setAttribute("temp", "a")
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "aa_BB", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl", xml)
                        .addContent("c")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl_AA", xml)
                        .addContent("d")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "c", result);
    }

    public void testDefaultAttribute() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Element el = new Element("foo")
                .setAttribute("temp", "a");

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "a", result);
    }

    public void testDefaultElement() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Element el = new Element("foo")
                .addContent(
                        new Element("temp")
                        .addContent("b")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "b", result);
    }

    public void testFindFullCodeNoAttribute() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Namespace xml = Namespace.XML_NAMESPACE;
        Element el = new Element("foo")
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "aa_BB", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl_KL", xml)
                        .addContent("c")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "c", result);
    }

    public void testFindPartialCodeNoAttribute() {
        LocaleSelector.suffixes
                = new String[]{
                    "kl_KL", "kl"
                };

        Namespace xml = Namespace.XML_NAMESPACE;
        Element el = new Element("foo")
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "aa_BB", xml)
                        .addContent("b")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl", xml)
                        .addContent("c")
                )
                .addContent(
                        new Element("temp")
                        .setAttribute("lang", "kl_AA", xml)
                        .addContent("d")
                );

        String result = LocaleSelector.getAttribute(el, "temp");
        Assert.assertEquals("find default", "c", result);
    }

    // from here down is testing infrastructure
    public LocaleSelectorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocaleSelectorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocaleSelectorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
