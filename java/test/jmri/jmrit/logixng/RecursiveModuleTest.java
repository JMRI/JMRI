package jmri.jmrit.logixng;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.DigitalCallModule;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.ActionListenOnBeans;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.jmrit.logixng.Module.ReturnValueType;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
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
        n.setValue(0);
        Assert.assertEquals(Long.valueOf(1), result.getValue());
        
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
        NamedBeanType.reset();
        
        n = InstanceManager.getDefault(MemoryManager.class).provide("IMN");
        n.setValue(1);
        
        result = InstanceManager.getDefault(MemoryManager.class).provide("IMRESULT");
        result.setValue("Hello");
        
        Module module = InstanceManager.getDefault(ModuleManager.class).createModule("IQM1", null,
                InstanceManager.getDefault(FemaleSocketManager.class)
                        .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));
        
        module.addParameter("n", true, false);
        module.addParameter("result", false, true);
        module.addLocalVariable("temp1", InitialValueType.None, null);
        module.addLocalVariable("temp2", InitialValueType.None, null);
        
        DigitalMany many901 = new DigitalMany("IQDA901", null);
        MaleSocket manySocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many901);
        module.getRootSocket().connect(manySocket901);
        
        IfThenElse ifThenElse912 = new IfThenElse("IQDA912", null);
        ifThenElse912.setType(IfThenElse.Type.AlwaysExecute);
        MaleSocket ifThenElseSocket912 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse912);
        manySocket901.getChild(0).connect(ifThenElseSocket912);
        
        
        ExpressionLocalVariable expressionLocalVariable913 = new ExpressionLocalVariable("IQDE913", null);
        expressionLocalVariable913.setLocalVariable("n");
        expressionLocalVariable913.setConstantValue("0");
        expressionLocalVariable913.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable913.setVariableOperation(ExpressionLocalVariable.VariableOperation.Equal);
        MaleSocket maleSocket913 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLocalVariable913);
        ifThenElseSocket912.getChild(0).connect(maleSocket913);
        
        ActionLocalVariable actionLocalVariable914 = new ActionLocalVariable("IQDA914", null);
        actionLocalVariable914.setLocalVariable("result");
        actionLocalVariable914.setFormula("1");   // Since this is a formula, it's the number 1, not the string "1"
        actionLocalVariable914.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        MaleSocket maleSocket914 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLocalVariable914);
        ifThenElseSocket912.getChild(1).connect(maleSocket914);
        
        
        IfThenElse ifThenElse915 = new IfThenElse("IQDA915", null);
        ifThenElse915.setType(IfThenElse.Type.AlwaysExecute);
        MaleSocket ifThenElseSocket915 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse915);
        ifThenElseSocket912.getChild(2).connect(ifThenElseSocket915);
        
        ExpressionLocalVariable expressionLocalVariable916 = new ExpressionLocalVariable("IQDE916", null);
        expressionLocalVariable916.setLocalVariable("n");
        expressionLocalVariable916.setConstantValue("1");
        expressionLocalVariable916.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable916.setVariableOperation(ExpressionLocalVariable.VariableOperation.Equal);
        MaleSocket maleSocket916 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLocalVariable916);
        ifThenElse915.getChild(0).connect(maleSocket916);
        
        ActionLocalVariable actionLocalVariable917 = new ActionLocalVariable("IQDA917", null);
        actionLocalVariable917.setLocalVariable("result");
        actionLocalVariable917.setFormula("1");   // Since this is a formula, it's the number 1, not the string "1"
        actionLocalVariable917.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        MaleSocket maleSocket917 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLocalVariable917);
        ifThenElse915.getChild(1).connect(maleSocket917);
        
        
        
        DigitalMany many921 = new DigitalMany("IQDA921", null);
        MaleSocket manySocket921 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many921);
        ifThenElse915.getChild(2).connect(manySocket921);
        
        // Call the module for n-1
        DigitalCallModule moduleDigitalAction925 = new DigitalCallModule("IQDA925", null);
        moduleDigitalAction925.setModule("IQM1");
        moduleDigitalAction925.addParameter("n", InitialValueType.Formula, "n - 1", ReturnValueType.None, null);
        moduleDigitalAction925.addParameter("result", InitialValueType.None, "", ReturnValueType.LocalVariable, "temp1");
        MaleSocket maleSocket925 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction925);
        manySocket921.getChild(0).connect(maleSocket925);
        
        // Call the module
        DigitalCallModule moduleDigitalAction932 = new DigitalCallModule("IQDA932", null);
        moduleDigitalAction932.setModule("IQM1");
        moduleDigitalAction932.addParameter("n", InitialValueType.Formula, "n - 2", ReturnValueType.None, null);
        moduleDigitalAction932.addParameter("result", InitialValueType.None, "", ReturnValueType.LocalVariable, "temp2");
        MaleSocket maleSocket932 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction932);
        manySocket921.getChild(1).connect(maleSocket932);
        
        ActionLocalVariable actionLocalVariable933 = new ActionLocalVariable("IQDA933", null);
        actionLocalVariable933.setLocalVariable("result");
        actionLocalVariable933.setFormula("temp1 + temp2");
        actionLocalVariable933.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        MaleSocket maleSocket933 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLocalVariable933);
        manySocket921.getChild(2).connect(maleSocket933);
        
        
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        
        DigitalMany many = new DigitalMany("IQDA1", null);
        MaleSocket manySocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        conditionalNG.getChild(0).connect(manySocket);
        
        ActionListenOnBeans listenOnBeans = new ActionListenOnBeans("IQDA2", null);
        listenOnBeans.addReference(new NamedBeanReference("IMN", NamedBeanType.Memory, false));
//        listenOnBeans.addReference("Turnoaut:IT1");
//        listenOnBeans.addReference("Turnout:IT1xx");
//        listenOnBeans.addReference("senSorIS1");
        MaleSocket listenSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        manySocket.getChild(0).connect(listenSocket);
        
        DigitalCallModule moduleDigitalAction = new DigitalCallModule("IQDA4", null);
        moduleDigitalAction.setModule("IQM1");
//        moduleDigitalAction.addParameter("n", InitialValueType.LocalVariable, "n");
        moduleDigitalAction.addParameter("n", InitialValueType.Memory, "IMN", ReturnValueType.None, null);
        moduleDigitalAction.addParameter("result", InitialValueType.None, "", ReturnValueType.Memory, "IMRESULT");
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction);
        manySocket.getChild(1).connect(maleSocket2);
/*        
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
*/        
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }
    
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
