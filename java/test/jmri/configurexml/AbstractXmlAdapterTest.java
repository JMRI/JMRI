package jmri.configurexml;

import java.util.HashMap;
import org.jdom2.*;
import org.junit.*;

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
    }
        
    enum testEnum {Foo, Bar, Biff}
    
    @Test
    public void testEnumIoOrdinals() {
        AbstractXmlAdapter.EnumIO<testEnum> map = new AbstractXmlAdapter.EnumIoOrdinals<>(testEnum.class);
        
        Assert.assertEquals("0", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Foo, map.inputFromString("0"));
        Assert.assertEquals("2", map.outputFromEnum(testEnum.Biff));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("2"));
    }

    @Test
    public void testEnumIoNames() {
        AbstractXmlAdapter.EnumIO<testEnum> map = new AbstractXmlAdapter.EnumIoNames<>(testEnum.class);
        
        Assert.assertEquals("Foo", map.outputFromEnum(testEnum.Foo));
        Assert.assertEquals(testEnum.Biff, map.inputFromString("Biff"));
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
    }


    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
