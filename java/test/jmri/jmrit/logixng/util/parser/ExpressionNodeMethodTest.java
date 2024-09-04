package jmri.jmrit.logixng.util.parser;

import java.io.*;
import java.util.*;

import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test ExpressionNodeMethod.
 * This method is also used to create the ExpressionNodeMethodPrimTest.java source file.
 *
 * @author Daniel Bergqvist 2024
 */
public class ExpressionNodeMethodTest {

    public void testCall(Object object, String method, Object expectedResult, Object[] params)
            throws FunctionNotExistsException, JmriException {

        Map<String, Variable> variables = new HashMap<>();
        List<ExpressionNode> parameterList = new ArrayList<>();
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        for (Object p : params) {
            parameterList.add(new MyExpressionNode(p));
        }

        ExpressionNodeMethod t = new ExpressionNodeMethod(method, variables, parameterList);
        Object result = t.calculate(object, symbolTable);

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testExpressionNodeMethod() throws JmriException {
        testCall("Hello", "substring", "ell", new Object[]{1,4});
    }

    @Test
    public void testMapEntry() throws JmriException {
        // HashMap.entrySet() returns a set of classes that are private.
        // We cannot use reflection to call methods on a class that's private.
        Map<String, String> map = new HashMap<>();
        map.put("Hello", "World");
        var entry = map.entrySet().iterator().next();
        testCall(entry, "getKey", "Hello", new Object[]{});
        testCall(entry, "getValue", "World", new Object[]{});
        testCall(entry, "toString", "Hello=World", new Object[]{});
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testMethodWithBooleanParameter() throws JmriException {
        var editor = new jmri.jmrit.display.layoutEditor.LayoutEditor("My layout");
        var trackSegment = new TrackSegment("Id", "A", HitPointType.TRACK, "B", HitPointType.TRACK, false, editor);
        var trackSegmentView = new TrackSegmentView(trackSegment, editor);
        editor.addLayoutTrack(trackSegment, trackSegmentView);
        var segments = editor.getTrackSegmentViews();
        var segment = segments.get(0);
        segment.setHidden(false);
        Assertions.assertFalse(segment.isHidden());
        testCall(segment, "setHidden", null, new Object[]{true});
        Assertions.assertTrue(segment.isHidden());
        editor.dispose();
    }

    /**
     * This method creates the ExpressionNodeMethodPrimTest.java source file.
     * Uncomment the line "@org.junit.Ignore" and run this test to create the file.
     * @throws IOException if an I/O exception occurs.
     */
    @org.junit.Ignore
    @Test
    public void testCreateTestMethods() throws IOException {

        String filename = "java/test/jmri/jmrit/logixng/util/parser/ExpressionNodeMethodPrimTest.java";

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {

            writer.format(
                    "package jmri.jmrit.logixng.util.parser;\n" +
                    "\n" +
                    "/*\n" +
                    "    Important!!!\n" +
                    "    This file is created by ExpressionNodeMethodTest.testCreateTestMethods()\n" +
                    "    Comment @org.junit.Ignore if you want to regenerate this file.\n" +
                    "*/\n" +
                    "\n" +
                    "import java.util.*;\n" +
                    "\n" +
                    "import jmri.JmriException;\n" +
                    "import jmri.jmrit.logixng.SymbolTable;\n" +
                    "import jmri.jmrit.logixng.implementation.DefaultConditionalNG;\n" +
                    "import jmri.jmrit.logixng.implementation.DefaultSymbolTable;\n" +
                    "import jmri.util.JUnitUtil;\n" +
                    "\n" +
                    "import org.junit.After;\n" +
                    "import org.junit.Assert;\n" +
                    "import org.junit.Before;\n" +
                    "import org.junit.Test;\n" +
                    "\n" +
                    "/**\n" +
                    " * Test ParsedExpression\n" +
                    " *\n" +
                    " * @author Daniel Bergqvist 2024\n" +
                    " */\n" +
                    "public class ExpressionNodeMethodPrimTest {\n" +
                    "\n" +
                    "    private final static TestClass tc = new TestClass();\n" +
                    "\n" +
                    "    public void testCall(Object object, String method, Object expectedResult, Object[] params)\n" +
                    "            throws FunctionNotExistsException, JmriException {\n" +
                    "\n" +
                    "        Map<String, Variable> variables = new HashMap<>();\n" +
                    "        List<ExpressionNode> parameterList = new ArrayList<>();\n" +
                    "        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG(\"IQC1\", null));\n" +
                    "\n" +
                    "        for (Object p : params) {\n" +
                    "            parameterList.add(new MyExpressionNode(p));\n" +
                    "        }\n" +
                    "\n" +
                    "        ExpressionNodeMethod t = new ExpressionNodeMethod(method, variables, parameterList);\n" +
                    "        Object result = t.calculate(object, symbolTable);\n" +
                    "\n" +
                    "        Assert.assertEquals(expectedResult, result);\n" +
                    "    }\n" +
                    "\n" +
                    "");

            String[] classes = new String[]{"Byte", "Short", "Integer", "Long", "Float", "Double"};
            String[] types = new String[]{"byte", "short", "int", "long", "float", "double"};

            for (int i=0; i < classes.length; i++) {

                if (i > 0) {
                    writer.format(
                            "    }\n" +
                            "\n");
                }

                writer.format(
                        "    @Test\n" +
                        "    public void test%s() throws FunctionNotExistsException, JmriException {\n" +
                        "", classes[i]);

                for (int j=0; j < classes.length; j++) {
                    for (int k=0; k < classes.length; k++) {
                        for (int l=0; l < classes.length; l++) {
                            String result = "8L";
                            if ("Float".equals(classes[i])) result = "8D";
                            if ("Double".equals(classes[i])) result = "8D";
                            if ("Float".equals(classes[j])) result = "8D";
                            if ("Double".equals(classes[j])) result = "8D";

                            if (("float".equals(types[k]) || "double".equals(types[k]))
                                    && (!"Float".equals(classes[i]) && !"Double".equals(classes[i]))) continue;

                            if (("float".equals(types[l]) || "double".equals(types[l]))
                                    && (!"Float".equals(classes[j]) && !"Double".equals(classes[j]))) continue;

                            writer.format("        testCall(tc, \"test%s%s\", null, new Object[]{(%s)2,(%s)4});%n", classes[i], classes[j], types[k], types[l]);
                            writer.format("        testCall(tc, \"test%s%sResult\", %s, new Object[]{(%s)2,(%s)4});%n", classes[i], classes[j], result, types[k], types[l]);
                        }
                    }
                }
            }

            writer.format("    }%n");

            writer.format(
                    "    // The minimal setup for log4J\n" +
                    "    @Before\n" +
                    "    public void setUp() {\n" +
                    "        JUnitUtil.setUp();\n" +
                    "    }\n" +
                    "\n" +
                    "    @After\n" +
                    "    public void tearDown() {\n" +
                    "        JUnitUtil.tearDown();\n" +
                    "    }\n" +
                    "\n" +
                    "\n" +
                    "    private static class MyExpressionNode implements ExpressionNode {\n" +
                    "\n" +
                    "        private final Object _value;\n" +
                    "\n" +
                    "        private MyExpressionNode(Object value) {\n" +
                    "            _value = value;\n" +
                    "        }\n" +
                    "\n" +
                    "        @Override\n" +
                    "        public Object calculate(SymbolTable symbolTable) throws JmriException {\n" +
                    "            return _value;\n" +
                    "        }\n" +
                    "\n" +
                    "        @Override\n" +
                    "        public String getDefinitionString() {\n" +
                    "            throw new UnsupportedOperationException(\"Not supported yet.\");\n" +
                    "        }\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "\n" +
                    "    public static class TestClass {\n" +
                    "\n");


            for (int i=0; i < classes.length; i++) {
                for (int j=0; j < classes.length; j++) {
                    String result = "long";
                    if ("Float".equals(classes[i])) result = "double";
                    if ("Double".equals(classes[i])) result = "double";
                    if ("Float".equals(classes[j])) result = "double";
                    if ("Double".equals(classes[j])) result = "double";
                    writer.format("        public void test%s%s(%s a, %s b) { }%n", classes[i], classes[j], types[i], types[j]);
                    writer.format("        public %s test%s%sResult(%s a, %s b) { return a*b; }%n", result, classes[i], classes[j], types[i], types[j]);
                }
            }

            writer.format("    }%n");
            writer.format("%n");
            writer.format("}%n");
        }
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }


    private static class MyExpressionNode implements ExpressionNode {

        private final Object _value;

        private MyExpressionNode(Object value) {
            _value = value;
        }

        @Override
        public Object calculate(SymbolTable symbolTable) throws JmriException {
            return _value;
        }

        @Override
        public String getDefinitionString() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
