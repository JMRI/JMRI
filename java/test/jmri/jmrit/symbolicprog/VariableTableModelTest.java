package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JLabel;

import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.*;

/**
 * Test VariableTableModel table methods.
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class VariableTableModelTest {

    private ProgDebugger p = new ProgDebugger();

    @Test
    public void testStart() {
        // create one with some dummy arguments
        VariableTableModel t = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p)
        );
        Assertions.assertNotNull(t);
    }

    // Can we create a table?
    @Test
    public void testVarTableCreate() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, null);  // CvTableModel ref is null for this test
        Assertions.assertNotNull(t, "exists");
    }

    // Check column count member fn, column names
    @Test
    public void testVarTableColumnCount() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, null);
        Assertions.assertEquals(2, t.getColumnCount());
        Assertions.assertEquals(t.getColumnName(1), Bundle.getMessage("Name")); // allow for I18N
    }

    // Check loading two columns, three rows
    @Test
    public void testVarTableLoad_2_3() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0, el1;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "1")
                                .setAttribute("label", "one")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("item", "really two")
                                .setAttribute("readOnly", "no")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                        .setAttribute("min", "1")
                                )
                        )
                        .addContent(el1 = new Element("variable")
                                .setAttribute("CV", "4")
                                .setAttribute("readOnly", "no")
                                .setAttribute("mask", "XXXVVVVX")
                                .setAttribute("label", "two")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                        .setAttribute("min", "1")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        // fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}
        // and test reading this
        t.setRow(0, el0);
        Assertions.assertEquals("1", t.getValueAt(0, 0));
        Assertions.assertEquals("one", t.getValueAt(0, 1));

        // check that the variable names were set right
        Assertions.assertEquals("one", t.getLabel(0), "check loaded label ");
        Assertions.assertEquals("really two", t.getItem(0), "check loaded item ");

        t.setRow(1, el1);
        Assertions.assertEquals("4", t.getValueAt(1, 0));
        Assertions.assertEquals("two", t.getValueAt(1, 1));

        Assertions.assertEquals(2, t.getRowCount());

        // check finding
        Assertions.assertEquals(1, t.findVarIndex("two"), "find variable two ");
        Assertions.assertEquals(-1, t.findVarIndex("not there, eh?"), "find nonexistant variable ");

        // check reverse finding
        Assertions.assertEquals(1, t.findVarIndex("two", true), "find variable two ");
        Assertions.assertEquals(0, t.findVarIndex("really two", true), "find variable one ");
        Assertions.assertEquals(-1, t.findVarIndex("not there, eh?", true), "find nonexistant variable ");
    }

    // Check creating a longaddr type, walk through its programming
    @Test
    public void testVarTableLoadLongAddr() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "17")
                                .setAttribute("readOnly", "no")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("label", "long")
                                .addContent(new Element("longAddressVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        // fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}
        // and test reading this
        t.setRow(0, el0);
        Assertions.assertEquals("17", t.getValueAt(0, 0));
        Assertions.assertEquals("long", t.getValueAt(0, 1));

        Assertions.assertEquals(1, t.getRowCount());

    }

    // Check creating a speed table, then finding it by name
    @Test
    public void testVarSpeedTable() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "67")
                                .setAttribute("label", "Speed Table")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "")
                                .addContent(new Element("speedTableVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check finding
        Assertions.assertEquals(1, t.getRowCount(), "length of variable list ");
        Assertions.assertEquals("Speed Table", t.getLabel(0), "name of 1st variable ");
        Assertions.assertEquals(0, t.findVarIndex("Speed Table"), "find Speed Table ");
        Assertions.assertEquals(-1, t.findVarIndex("not there, eh?"), "find nonexistant variable ");

    }

    // Check creating a splitVal variable with no default value
    @Test
    public void testVarSplitValnoDefault() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "1,9")
                                .setAttribute("label", "SplitVal no default")
                                .addContent(new Element("splitVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check finding
        Assertions.assertEquals(1, t.getRowCount(), "length of variable list ");
        Assertions.assertEquals("SplitVal no default", t.getLabel(0), "name of 1st variable ");
        Assertions.assertEquals(0, t.findVarIndex("SplitVal no default"), "find variable by name ");
        Assertions.assertEquals(-1, t.findVarIndex("not there, eh?"), "find nonexistant variable ");

    }

    // Check creating a splitVal variable with default value
    @Test
    public void testVarSplitValwithDefault() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "1,9")
                                .setAttribute("label", "SplitVal with default")
                                .setAttribute("default", "32700")
                                .addContent(new Element("splitVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check finding
        Assertions.assertEquals(1, t.getRowCount(), "length of variable list ");
        Assertions.assertEquals("SplitVal with default", t.getLabel(0), "name of 1st variable ");
        Assertions.assertEquals(0, t.findVarIndex("SplitVal with default"), "find variable by name ");
        SplitVariableValue sv = (SplitVariableValue) t.getVariable(t.findVarIndex("SplitVal with default"));
        Assertions.assertEquals(32700, sv.getLongValue(), "find value of variable ");
    }

    // Check creating a splitVal variable with big default value
    @Test
    public void testVarSplitValwithBigDefault() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "1:8")
                                .setAttribute("label", "SplitVal with big default")
                                .setAttribute("default", "35184372088832")
                                .addContent(new Element("splitVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check finding
        Assertions.assertEquals(1, t.getRowCount(), "length of variable list ");
        Assertions.assertEquals("SplitVal with big default", t.getLabel(0), "name of 1st variable ");
        Assertions.assertEquals(0, t.findVarIndex("SplitVal with big default"), "find variable by name ");
        SplitVariableValue sv = (SplitVariableValue) t.getVariable(t.findVarIndex("SplitVal with big default"));
        Assertions.assertEquals(35184372088832L, sv.getLongValue(), "find value of variable ");

    }

    // Check creating an enumvar with various groupings
    @Test
    public void testVarEnumVar() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "99")
                                .setAttribute("label", "Enum Sample")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "")
                                .addContent(new Element("enumVal")
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V0")
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V1")
                                        )
                                        .addContent(new Element("enumChoiceGroup")
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V2")
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V3")
                                                )
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V4")
                                        )
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check
        Assertions.assertEquals("Enum Sample", t.getLabel(0), "name of variable 1");
        EnumVariableValue ev = (EnumVariableValue) t.getVariable(t.findVarIndex("Enum Sample"));
        ev.setValue(1);
        Assertions.assertEquals("V1", ev.getTextValue(), "value 1");
        ev.setValue(2);
        Assertions.assertEquals("V2", ev.getTextValue(), "value 2");
        ev.setValue(3);
        Assertions.assertEquals("V3", ev.getTextValue(), "value 3");
        ev.setValue(4);
        Assertions.assertEquals("V4", ev.getTextValue(), "value 4");
    }

    @Test
    public void testVarDecVarMaxMask() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0, el1, el2, el3, el4;
        root.addContent(new Element("decoder")
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "1")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                )
                        )
                        .addContent(el1 = new Element("variable")
                                .setAttribute("CV", "2")
                                .setAttribute("mask", "VVVV")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "3")
                                )
                        )
                        .addContent(el2 = new Element("variable")
                                .setAttribute("CV", "3")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "9999")
                                )
                        )
                        .addContent(el3 = new Element("variable")
                                .setAttribute("CV", "4")
                                .addContent(new Element("hexVal")
                                        .setAttribute("max", "9")
                                )
                        )
                        .addContent(el4 = new Element("variable")
                                .setAttribute("CV", "5")
                                .addContent(new Element("hexVal")
                                        .setAttribute("max", "F99")
                                )
                        )
                ) // variables element
        ) // decoder element
        ; // end of adding contents

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("p", "m", "f");
        t.setRow(0, el0, _df);
        Assertions.assertNull(el0.getAttribute("mask"), "mask");
        t.setRow(1, el1, _df);
        Assertions.assertEquals("[Attribute: mask=\"VVVV\"]", el1.getAttribute("mask").toString(), "mask");
        t.setRow(2, el2, _df);
        Assertions.assertNull(el2.getAttribute("mask"), "mask");
        t.setRow(3, el3, _df);
        Assertions.assertNull(el3.getAttribute("mask"), "mask");
        t.setRow(4, el4, _df);
        Assertions.assertNull(el4.getAttribute("mask"), "mask");

        // now read the rows from the tablemodel
        // decval
        Assertions.assertEquals("VVVVVVVV", t.getVariable(0).getMask(), "default mask"); // default mask applied
        Assertions.assertEquals("VVVV", t.getVariable(1).getMask(), "xml mask"); // from xml definition
        Assertions.assertEquals("VVVVVVVVVVVVVV", t.getVariable(2).getMask(), ">256 mask"); // autogenerated
        // hexval
        Assertions.assertEquals("VVVVVVVV", t.getVariable(3).getMask(), "hex default mask"); // default
        Assertions.assertEquals("VVVVVVVVVVVV", t.getVariable(4).getMask(), "hex max mask"); // autogenerated on hex
    }

    @Test
    public void testSetDefault() {
        // set up a decoder to match against
        DecoderFile df = createDecoderFile("p", "m", "f");

        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p)) {
            { _df = df; }
        };
        HashMap<String, CvValue> map = new HashMap<>();
        map.put("17", new CvValue("17", null));

        VariableValue v = new DecVariableValue("label17", "comment17", "", false, false, false, false, "17", "VVVVVVVV", 0, 255, map, null, null);

        Assertions.assertEquals("0", v.getValueString(), "default start");

        // set default with normal element
        Element el0 = new Element("variable")
                                .setAttribute("default", "21");

        t.setDefaultValue(el0, v);
        Assertions.assertEquals("21", v.getValueString(), "default start");

        // set default with defaultItem elements
        Element el1 = new Element("variable")
                                .addContent(new Element("defaultItem")
                                        .setAttribute("default", "31")
                                        .setAttribute("include", "x")
                                )
                                .addContent(new Element("defaultItem")
                                        .setAttribute("default", "32")
                                        .setAttribute("include", "m")
                                )
                                .setAttribute("default", "21");

        t.setDefaultValue(el1, v);
        Assertions.assertEquals("32", v.getValueString(), "default start");
    }
    
    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude0() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("p", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("f", "", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line

        // specify expected included elements here
        Integer[] included = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude1() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("p", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("m", "", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line
        exclude[2] = "p";

        // specify expected included elements here
        Integer[] included = {0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude2() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("p", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("o,p,q", "", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line
        exclude[8] = "p";

        // specify expected included elements here
        Integer[] included = {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude3() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("o,p,q", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("q", "", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line
        exclude[11] = "o";

        // specify expected included elements here
        Integer[] included = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude4() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("o,p,q", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("m", "", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line
        exclude[2] = "p";
        excludeGroup[9] = "m";
        exclude[14] = "f";

        // specify expected included elements here
        Integer[] included = {0, 1, 3, 4, 5, 6, 7, 8, 12, 13, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude5() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("o,p,q", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("f", "m", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line

        // specify expected included elements here
        Integer[] included = {0, 1, 3, 4, 5, 6, 7, 12, 13};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Check creating an enumvar with various groupings and element-based includes/excludes
    @Test
    public void testVarEnumVarIncludeExclude6() {
        //set up the test
        int itemCount = 30;
        String[] include = new String[itemCount];
        String[] exclude = new String[itemCount];
        String[] includeGroup = new String[itemCount];
        String[] excludeGroup = new String[itemCount];

        // set up a decoder to match against
        DecoderFile _df = createDecoderFile("o,p,q", "m", "f");
        // set up default includes to first arg & default excludes to second arg
        setupEnumVarIncludeExcludeDefaults("f", "no match", include, exclude, includeGroup, excludeGroup);
        //set custom includes/excludes below this line

        // specify expected included elements here
        Integer[] included = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        // perform the test
        doEnumVarIncludeExclude(included, itemCount, include, exclude, includeGroup, excludeGroup, _df);
    }

    // Begin common setup routines for testVarEnumVarIncludeExclude tests
    // create a test enumVal element
    Element setupEnumVarIncludeExcludeElement(String[] include, String[] exclude,
            String[] includeGroup, String[] excludeGroup) {
        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements and content
        Element enumVarElement;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(enumVarElement = new Element("variable")
                                .setAttribute("CV", "99")
                                .setAttribute("label", "Enum Sample")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "")
                                .addContent(new Element("enumVal")
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V0")
                                                .setAttribute("value", "0")
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V1")
                                                .setAttribute("value", "1")
                                                .setAttribute("include", include[1])
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V2")
                                                .setAttribute("value", "2")
                                                .setAttribute("exclude", exclude[2])
                                        )
                                        .addContent(new Element("enumChoiceGroup")
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V3")
                                                        .setAttribute("value", "3")
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V4")
                                                        .setAttribute("value", "4")
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V5")
                                                        .setAttribute("value", "5")
                                                )
                                        )
                                        .addContent(new Element("enumChoiceGroup")
                                                .setAttribute("include", includeGroup[6])
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V6")
                                                        .setAttribute("value", "6")
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V7")
                                                        .setAttribute("value", "7")
                                                        .setAttribute("include", include[7])
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V8")
                                                        .setAttribute("value", "8")
                                                        .setAttribute("exclude", exclude[8])
                                                )
                                        )
                                        .addContent(new Element("enumChoiceGroup")
                                                .setAttribute("exclude", excludeGroup[9])
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V9")
                                                        .setAttribute("value", "9")
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V10")
                                                        .setAttribute("value", "10")
                                                        .setAttribute("include", include[10])
                                                )
                                                .addContent(new Element("enumChoice")
                                                        .setAttribute("choice", "V11")
                                                        .setAttribute("value", "11")
                                                        .setAttribute("exclude", exclude[11])
                                                )
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V12")
                                                .setAttribute("value", "12")
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V13")
                                                .setAttribute("value", "13")
                                                .setAttribute("include", include[13])
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V14")
                                                .setAttribute("value", "14")
                                                .setAttribute("exclude", exclude[14])
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "V15")
                                                .setAttribute("value", "15")
                                                .setAttribute("include", include[15])
                                                .setAttribute("exclude", exclude[15])
                                        )
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents
        return enumVarElement;
    }

    /**
     * Set up default includes and excludes.
     *
     * @param defaultInclude the default include string
     * @param defaultExclude the default exclude string
     * @param include        the include array for items
     * @param exclude        the exclude array for items
     * @param includeGroup   the include array for group items
     * @param excludeGroup   the include array for group items
     */
    void setupEnumVarIncludeExcludeDefaults(String defaultInclude, String defaultExclude, String[] include, String[] exclude,
            String[] includeGroup, String[] excludeGroup) {
        for (int i = 0; i < include.length; i++) {
            include[i] = defaultInclude;
            exclude[i] = defaultExclude;
        }
        for (int i = 0; i < includeGroup.length; i++) {
            includeGroup[i] = defaultInclude;
            excludeGroup[i] = defaultExclude;
        }
    }

    DecoderFile createDecoderFile(String productID, String model, String family) {
        String _productID = productID;
        String _model = model;
        String _family = family;
        DecoderFile ret = new DecoderFile() {
            @Override
            public String getProductID() {
                return _productID;
            }

            @Override
            public String getModel() {
                return _model;
            }

            @Override
            public String getFamily() {
                return _family;
            }
        };
        return ret;
    }

    // perform the EnumVarIncludeExclude test
    void doEnumVarIncludeExclude(Integer[] included, int itemCount, String[] include, String[] exclude, String[] includeGroup, String[] excludeGroup, DecoderFile _df) {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));
        // create the EnumVar and get a reference to it
        Element el0 = setupEnumVarIncludeExcludeElement(include, exclude, includeGroup, excludeGroup);
        t.setRow(0, el0, _df);
        Assertions.assertEquals("Enum Sample", t.getLabel(0), "name of variable 1");
        EnumVariableValue ev = (EnumVariableValue) t.getVariable(t.findVarIndex("Enum Sample"));

        ArrayList<Integer> includedList = new ArrayList<>(Arrays.asList(included));
        for (int i = 0; i < itemCount; i++) {
            ev.setValue(i);
            if (includedList.contains(i)) {
                Assertions.assertEquals("V" + i, ev.getTextValue(), "Value " + i + " is included");
            } else {
                Assertions.assertEquals("Reserved value " + i, ev.getTextValue(), "Value " + i + " is excluded");
            }
        }
    }
    // End common setup routines for testVarEnumVarIncludeExclude tests

    // Check creating bogus XML (unknown variable type)
    @Test
    public void testVarTableLoadBogus() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p)) {
            @Override
            void reportBogus() {
            }
        };

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el0 = new Element("variable")
                                .setAttribute("CV", "17")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "no")
                                .setAttribute("label", "long")
                                .addContent(new Element("bogusVal")
                                )
                        )
                ) // variables element
        ) // decoder element
                ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        // fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}
        // and test reading this
        t.setRow(0, el0);
        Assertions.assertEquals(0, t.getRowCount());
    }

    // Check can read simple file
    @Test
    public void testVarTableLoadFileSimple() throws Exception {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree from file
        XmlFile file = new XmlFile() {
        };
        Element root = file.rootFromName("xml/decoders/0NMRA.xml");

        // add the contents
        Element el0 = root.getChild("decoder").getChild("variables");
        int i = 0;
        for (Element v : el0.getChildren("variable")) {
            t.setRow(i++, v);
        }
        // fault is failure to reach the end, e.g. throw message or exception

    }

    // Check can read complex file
    @Test
    public void testVarTableLoadFileComplex() throws Exception {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p));

        // create a JDOM tree from file
        XmlFile file = new XmlFile() {
        };
        Element root = file.rootFromName("xml/decoders//QSI_ver9.xml");

        // add the contents
        Element el0 = root.getChild("decoder").getChild("variables");
        int i = 0;
        for (Element v : el0.getChildren("variable")) {
            t.setRow(i++, v);
        }
        // fault is failure to reach the end, e.g. throw message or exception
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
