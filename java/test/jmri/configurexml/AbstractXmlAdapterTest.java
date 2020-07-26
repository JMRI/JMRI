package jmri.configurexml;

import java.util.HashMap;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.*;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the AbstractXmlAdapter class itself
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class AbstractXmlAdapterTest{

    @Test
    public void testGetAttributeBooleanValue() {
        AbstractXmlAdapter adapter  = new AbstractXmlAdapter(){
            public Element store(Object o) {return null;}
            public void load(Element e, Object o) {}
        };
        
        Element testEl = new Element("foo");
        
        Assert.assertTrue(adapter.getAttributeBooleanValue(testEl, "att", true));
        Assert.assertFalse(adapter.getAttributeBooleanValue(testEl, "att", false));
                
        testEl.setAttribute("t", "true");
        testEl.setAttribute("f", "false");
        testEl.setAttribute("y", "yes");
        testEl.setAttribute("n", "no");

        Assert.assertTrue(adapter.getAttributeBooleanValue(testEl, "t", true));
        Assert.assertTrue(adapter.getAttributeBooleanValue(testEl, "t", false));
                
        Assert.assertFalse(adapter.getAttributeBooleanValue(testEl, "f", true));
        Assert.assertFalse(adapter.getAttributeBooleanValue(testEl, "f", false));
                
        Assert.assertTrue(adapter.getAttributeBooleanValue(testEl, "y", true));
        Assert.assertTrue(adapter.getAttributeBooleanValue(testEl, "y", false));
                
        Assert.assertFalse(adapter.getAttributeBooleanValue(testEl, "n", true));
        Assert.assertFalse(adapter.getAttributeBooleanValue(testEl, "n", false));
                
        
    }
    
    @Test
    public void testGetAttributeIintegerValue() {
        AbstractXmlAdapter adapter  = new AbstractXmlAdapter(){
            public Element store(Object o) {return null;}
            public void load(Element e, Object o) {}
        };
        
        Element testEl = new Element("foo");
        
        Assert.assertEquals(12, adapter.getAttributeIntegerValue(testEl, "att", 12));
                
        testEl.setAttribute("t21", "21");
        
        Assert.assertEquals(21, adapter.getAttributeIntegerValue(testEl, "t21", 12));
        
        // check error handling
        testEl.setAttribute("bar", "bar");
        Assert.assertEquals(21, adapter.getAttributeIntegerValue(testEl, "bar", 21));
        
        JUnitAppender.assertErrorMessageStartsWith("element: foo System name \"attribute: bar\" User name \"value: bar\" while getAttributeIntegerValue threw exception in adaptor of type jmri.configurexml.AbstractXmlAdapterTest");
    }
        
    @Test
    public void testGetAttributeDoubleValue() {
        AbstractXmlAdapter adapter  = new AbstractXmlAdapter(){
            public Element store(Object o) {return null;}
            public void load(Element e, Object o) {}
        };
        
        Element testEl = new Element("foo");
        
        Assert.assertEquals(12., adapter.getAttributeDoubleValue(testEl, "att", 12.), 0.001);
                
        testEl.setAttribute("t21", "21.");
        
        Assert.assertEquals(21., adapter.getAttributeDoubleValue(testEl, "t21", 12.), 0.001);
    }
        
    @Test
    public void testGetAttributeFloatValue() {
        AbstractXmlAdapter adapter  = new AbstractXmlAdapter(){
            public Element store(Object o) {return null;}
            public void load(Element e, Object o) {}
        };
        
        Element testEl = new Element("foo");
        
        Assert.assertEquals(12., adapter.getAttributeFloatValue(testEl, "att", 12.f), 0.001);
                
        testEl.setAttribute("t21", "21.");
        
        Assert.assertEquals(21., adapter.getAttributeFloatValue(testEl, "t21", 12.f), 0.001);
    }
        
    enum testEnum {
        Foo,
        Bar,
        Biff;
        
        // The purpose of this method is to ensure that the EnumIO method
        // doesn't use the method toString() since it could return a different
        // value than the name of the enum, for example a localized name.
        @Override
        public String toString() {
            return "A string";
        }
    }
    
    @Test
    public void testEnumIoOrdinals() {
        AbstractXmlAdapter.EnumIO<testEnum> map = new AbstractXmlAdapter.EnumIoOrdinals<>(testEnum.class);
        
        Assert.assertEquals("0", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("0"));
        Assert.assertEquals("2", map.outputFromEnum(testEnum.Biff));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("2"));
        
        Assert.assertEquals(testEnum.Foo, map.inputFromString("FooBar"));
        JUnitAppender.assertErrorMessage("from String FooBar get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
        
        Assert.assertEquals(testEnum.Foo, map.inputFromString(null));
        JUnitAppender.assertErrorMessage("from String null get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
    }

    @Test
    public void testEnumIoNames() {
        AbstractXmlAdapter.EnumIO<testEnum> map = new AbstractXmlAdapter.EnumIoNames<>(testEnum.class);
        
        Assert.assertEquals("Foo", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("Biff"));
        
        Assert.assertEquals(testEnum.Foo, map.inputFromString("FooBar"));
        JUnitAppender.assertErrorMessage("from String FooBar get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
        
        Assert.assertEquals(testEnum.Foo, map.inputFromString(null));
        JUnitAppender.assertErrorMessage("from String null get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
    }

    @Test
    public void testEnumIoMapped1() {
        AbstractXmlAdapter.EnumIO<testEnum> map 
            = new AbstractXmlAdapter.EnumIoMapped<>(testEnum.class,
                                        new HashMap<String, testEnum>(){{
                                            put("4", testEnum.Foo);
                                            put("5", testEnum.Bar);
                                            put("6", testEnum.Biff);
                                            put("foo", testEnum.Foo);
                                            put("bar", testEnum.Bar);
                                            put("biff", testEnum.Biff);
                                        }}
                                   );
        
        Assert.assertEquals("Foo", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("biff"));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("6"));

        Assert.assertEquals("Foo", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("foo"));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("4"));

        Assert.assertEquals(testEnum.Foo, map.inputFromString("FooBar"));
        JUnitAppender.assertErrorMessage("from String FooBar get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");

        Assert.assertEquals(testEnum.Foo, map.inputFromString(null));
        JUnitAppender.assertErrorMessage("from String null get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
    }

    @Test
    public void testEnumIoMapped2() {
        AbstractXmlAdapter.EnumIO<testEnum> map 
            = new AbstractXmlAdapter.EnumIoMapped<>(testEnum.class,
                                        new HashMap<String, testEnum>(){{
                                            put("4", testEnum.Foo);
                                            put("5", testEnum.Bar);
                                            put("6", testEnum.Biff);
                                            put("foo", testEnum.Foo);
                                            put("bar", testEnum.Bar);
                                            put("biff", testEnum.Biff);
                                        }},
                                        new HashMap<testEnum, String>(){{
                                            put(testEnum.Foo, "FOO");
                                            put(testEnum.Bar, "BAR");
                                            put(testEnum.Biff, "BIFF");
                                        }}
                                );
        
        Assert.assertEquals("BIFF", map.outputFromEnum(testEnum.Biff));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("biff"));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("6"));

        Assert.assertEquals("FOO", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("foo"));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("4"));

        Assert.assertEquals(testEnum.Foo, map.inputFromString("FooBar"));
        JUnitAppender.assertErrorMessage("from String FooBar get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");

        Assert.assertEquals(testEnum.Foo, map.inputFromString(null));
        JUnitAppender.assertErrorMessage("from String null get Foo for class jmri.configurexml.AbstractXmlAdapterTest$testEnum");
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
