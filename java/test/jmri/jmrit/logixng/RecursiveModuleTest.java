package jmri.jmrit.logixng;

import jmri.*;
import jmri.jmrit.logixng.digital.actions.*;
import jmri.jmrit.logixng.digital.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.jmrit.logixng.digital.expressions.ExpressionLocalVariable;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test that a module can be used recursive by calculating the Fibonacci numbers.
 * 
 * @author Daniel Bergqvist 2020
 */
public class RecursiveModuleTest {
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Memory n;
    private Memory result;
    
    
    @Test
    public void testFibonacci() {
        System.out.format("%n%n%nStart test%n%n");
        
        InstanceManager.getDefault(LogixNG_Manager.class).setSymbolTable(null);
        Stack stack = InstanceManager.getDefault(LogixNG_Manager.class).getStack();
        while (stack.getCount() > 0) stack.pop();
        
        n.setValue(0);
        Assert.assertEquals(Long.valueOf(1), result.getValue());
        if (1==1) return;
        
        n.setValue(1);
        Assert.assertEquals(Long.valueOf(1), result.getValue());
        
        n.setValue(2);
        Assert.assertEquals(Long.valueOf(2), result.getValue());
        
        n.setValue(3);
        Assert.assertEquals(Long.valueOf(3), result.getValue());
        
        n.setValue(4);
        Assert.assertEquals(Long.valueOf(5), result.getValue());
        
        n.setValue(5);
        Assert.assertEquals(Long.valueOf(8), result.getValue());
        
        n.setValue(6);
        Assert.assertEquals(Long.valueOf(13), result.getValue());
        
        n.setValue(7);
        Assert.assertEquals(Long.valueOf(21), result.getValue());
        
        n.setValue(8);
        Assert.assertEquals(Long.valueOf(34), result.getValue());
        
        n.setValue(9);
        Assert.assertEquals(Long.valueOf(55), result.getValue());
        
        n.setValue(10);
        Assert.assertEquals(Long.valueOf(89), result.getValue());
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
//        Map<String, FemaleSocketManager.SocketType> socketTypes = InstanceManager.getDefault(FemaleSocketManager.class).getSocketTypes();
        
//        for (FemaleSocketManager.SocketType socketType : socketTypes.values()) {
//            System.out.format("Socket: %s, %s, %s%n", socketType.getName(), socketType.getDescr(), socketType.getClass());
//        }
        
//        InstanceManager.getDefault(NamedTableManager.class).newStack("IQT1", null);
        
        
        
        
        n = InstanceManager.getDefault(MemoryManager.class).provide("IMN");
        n.setValue(1);
        
        result = InstanceManager.getDefault(MemoryManager.class).provide("IMRESULT");
        result.setValue("Hello");
        
        Module module = InstanceManager.getDefault(ModuleManager.class).createModule("IQM1", null);
        
        module.addParameter("n", true, false);
        module.addParameter("result", false, true);
        module.addLocalVariable("temp", SymbolTable.InitialValueType.None, "");
        
        module.setRootSocketType(InstanceManager.getDefault(FemaleSocketManager.class)
                .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));
        
        Many many901 = new Many("IQDA901", null);
        MaleSocket manySocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many901);
        module.getRootSocket().connect(manySocket901);
        
        IfThenElse ifThenElse912 = new IfThenElse("IQDA912", null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse912.setType(IfThenElse.Type.CONTINOUS_ACTION);
        MaleSocket ifThenElseSocket912 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse912);
        manySocket901.getChild(0).connect(ifThenElseSocket912);
        
        
        ExpressionLocalVariable expressionLocalVariable913 = new ExpressionLocalVariable("IQDE913", null);
        expressionLocalVariable913.setVariable("n");
        expressionLocalVariable913.setConstantValue("0");
        expressionLocalVariable913.setCompareTo(ExpressionLocalVariable.CompareTo.VALUE);
        expressionLocalVariable913.setVariableOperation(ExpressionLocalVariable.VariableOperation.EQUAL);
        MaleSocket maleSocket913 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLocalVariable913);
        ifThenElseSocket912.getChild(0).connect(maleSocket913);
        
        ActionLocalVariable actionLocalVariable914 = new ActionLocalVariable("IQDA914", null);
        actionLocalVariable914.setVariable("result");
        actionLocalVariable914.setData("1");   // Since this is a formula, it's the number 1, not the string "1"
        actionLocalVariable914.setVariableOperation(ActionLocalVariable.VariableOperation.FORMULA);
        MaleSocket maleSocket914 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLocalVariable914);
        ifThenElseSocket912.getChild(1).connect(maleSocket914);
        
        
        IfThenElse ifThenElse915 = new IfThenElse("IQDA915", null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse915.setType(IfThenElse.Type.CONTINOUS_ACTION);
        MaleSocket ifThenElseSocket915 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse915);
        ifThenElseSocket912.getChild(2).connect(ifThenElseSocket915);
        
        ExpressionLocalVariable expressionLocalVariable916 = new ExpressionLocalVariable("IQDE916", null);
        expressionLocalVariable916.setVariable("n");
        expressionLocalVariable916.setConstantValue("1");
        expressionLocalVariable916.setCompareTo(ExpressionLocalVariable.CompareTo.VALUE);
        expressionLocalVariable916.setVariableOperation(ExpressionLocalVariable.VariableOperation.EQUAL);
        MaleSocket maleSocket916 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLocalVariable916);
        ifThenElse915.getChild(0).connect(maleSocket916);
        
        ActionLocalVariable actionLocalVariable917 = new ActionLocalVariable("IQDA917", null);
        actionLocalVariable917.setVariable("result");
        actionLocalVariable917.setData("1");   // Since this is a formula, it's the number 1, not the string "1"
        actionLocalVariable917.setVariableOperation(ActionLocalVariable.VariableOperation.FORMULA);
        MaleSocket maleSocket917 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLocalVariable917);
        ifThenElse915.getChild(1).connect(maleSocket917);
        
        
        
        Many many921 = new Many("IQDA921", null);
        MaleSocket manySocket921 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many921);
        ifThenElse915.getChild(2).connect(manySocket921);
        
        // Call the module for n-1
        ModuleDigitalAction moduleDigitalAction925 = new ModuleDigitalAction("IQDA925", null);
        moduleDigitalAction925.setModule("IQM1");
        moduleDigitalAction925.addParameter("n", SymbolTable.InitialValueType.Formula, "n - 1");
        MaleSocket maleSocket925 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction925);
        manySocket921.getChild(0).connect(maleSocket925);
        
        // Call the module
        ModuleDigitalAction moduleDigitalAction932 = new ModuleDigitalAction("IQDA932", null);
        moduleDigitalAction932.setModule("IQM1");
        moduleDigitalAction932.addParameter("n", SymbolTable.InitialValueType.Formula, "n - 2");
        MaleSocket maleSocket932 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction932);
        manySocket921.getChild(1).connect(maleSocket932);
        
        
        
        
        
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        
//        conditionalNG.addLocalVariable("n", SymbolTable.InitialValueType.Memory, "IMN");
        
        Many many = new Many("IQDA1", null);
        MaleSocket manySocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        conditionalNG.getChild(0).connect(manySocket);
        
        ActionListenOnBeans listenOnBeans = new ActionListenOnBeans("IQDA2", null);
        listenOnBeans.addReference(new NamedBeanReference("IMN", ActionListenOnBeans.NamedBeanType.MEMORY));
//        listenOnBeans.addReference("Turnoaut:IT1");
//        listenOnBeans.addReference("Turnout:IT1xx");
//        listenOnBeans.addReference("senSorIS1");
        MaleSocket listenSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        manySocket.getChild(0).connect(listenSocket);
        
        ModuleDigitalAction moduleDigitalAction = new ModuleDigitalAction("IQDA4", null);
        moduleDigitalAction.setModule("IQM1");
//        moduleDigitalAction.addParameter("n", SymbolTable.InitialValueType.LocalVariable, "n");
        moduleDigitalAction.addParameter("n", SymbolTable.InitialValueType.Memory, "IMN");
        moduleDigitalAction.addParameter("result", SymbolTable.InitialValueType.None, "");
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction);
        manySocket.getChild(1).connect(maleSocket2);
/*        
        // Store the sum in IMRESULT
        ActionMemory actionMemory = new ActionMemory("IQDA5", null);
        actionMemory.setMemory("IMRESULT");
        actionMemory.setData("result");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.COPY_VARIABLE_TO_MEMORY);
        MaleSocket maleSocket3 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory);
        manySocket.getChild(2).connect(maleSocket3);
*/        
        
        
        
        
        
        
        final String treeIndent = "   ";
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter printWriter = new java.io.PrintWriter(stringWriter);
        logixNG.printTree(java.util.Locale.ENGLISH, printWriter, treeIndent);
        System.out.println(stringWriter.toString());
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        
        stringWriter = new java.io.StringWriter();
        printWriter = new java.io.PrintWriter(stringWriter);
        module.printTree(java.util.Locale.ENGLISH, printWriter, treeIndent);
        System.out.println(stringWriter.toString());
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
