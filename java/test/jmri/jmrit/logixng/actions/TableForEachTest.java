package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.TableRowOrColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test TableForEach
 * 
 * @author Daniel Bergqvist 2019
 */
public class TableForEachTest extends AbstractDigitalActionTestBase {

    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
    TableForEach _tableForEach;
    MaleSocket _maleSocket;
    private final List<String> _cells = new ArrayList<>();
    
    @Override
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return _logixNG;
    }
    
    @Override
    public MaleSocket getConnectableChild() {
        DigitalMany action = new DigitalMany("IQDA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Table: For each column of row \"\" in table \"\" set variable \"\" and execute action A1 ::: Use default%n" +
                "   ! A1%n" +
                "      MyAction ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Table: For each column of row \"\" in table \"\" set variable \"\" and execute action A1 ::: Use default%n" +
                "            ! A1%n" +
                "               MyAction ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new TableForEach(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        TableForEach t = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists",t);
        t = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
/* DISABLE FOR NOW    
    @Test
    public void testCtorAndSetup1() {
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName(null);
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);
        
        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));
        
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertTrue("action female socket is connected",
                action.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                action.getChild(0).getConnectedSocket());
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        action.setup();
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
    }
*/    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == _tableForEach.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                _tableForEach.getChild(0));
        
        boolean hasThrown = false;
        try {
            _tableForEach.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testDescription() {
        TableForEach a1 = new TableForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "Table: For each", a1.getShortDescription());
        TableForEach a2 = new TableForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "Table: For each column of row \"\" in table \"\" set variable \"\" and execute action A1", a2.getLongDescription());
    }
    
    @Test
    public void testExecute()
            throws IOException {
        
        _maleSocket.addLocalVariable("MyVariable", SymbolTable.InitialValueType.None, null);
        
        _logixNG.setEnabled(false);
        
        // Load table turnout_and_signals.csv
        NamedTable csvTable =
                InstanceManager.getDefault(NamedTableManager.class)
                        .loadTableFromCSV("IQT1", null, "program:java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv");
        
        _tableForEach.setTable(csvTable);
        _tableForEach.setTableRowOrColumn(TableRowOrColumn.Column);
        _tableForEach.setRowOrColumnName("1");
        _tableForEach.setLocalVariableName("MyVariable");
        _logixNG.setEnabled(true);
        
        Assert.assertEquals("IT1 :::  ::: IH1 :::  :::  ::: IT1 ::: IT3 ::: IH1" +
                " ::: IH6 :::  ::: IH4 ::: IH6 ::: IT1 ::: IH1 ::: IH3 ::: IH4" +
                " ::: IH6 ::: IT1 ::: IT3 :::  :::  ::: ",
                String.join(" ::: ", _cells));
    }
    
    @Test
    @Override
    public void testIsActive() {
        _logixNG.setEnabled(true);
        super.testIsActive();
    }
    
    @Test
    @Override
    public void testMaleSocketIsActive() {
        _logixNG.setEnabled(true);
        super.testMaleSocketIsActive();
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);
        _tableForEach = new TableForEach("IQDA321", null);
        _maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_tableForEach);
        _conditionalNG.getChild(0).connect(_maleSocket);
        _base = _tableForEach;
        _baseMaleSocket = _maleSocket;
        
        _tableForEach.getChild(0).connect(InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(new MyAction("IQDA999", null)));
        
        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        _logixNG.setEnabled(false);
    }

    @After
    public void tearDown() {
        JUnitAppender.suppressErrorMessage("tableHandle is null");
        _logixNG.setEnabled(false);
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
        _category = null;
        _logixNG = null;
        _conditionalNG = null;
        _tableForEach = null;
        _base = null;
        _baseMaleSocket = null;
        _maleSocket = null;
    }
    
    
    private class MyAction extends AbstractDigitalAction {

        public MyAction(String sys, String user) throws BadUserNameException, BadSystemNameException {
            super(sys, user);
        }

        @Override
        protected void registerListenersForThisClass() {
            // Do nothing
        }

        @Override
        protected void unregisterListenersForThisClass() {
            // Do nothing
        }

        @Override
        protected void disposeMe() {
            // Do nothing
        }

        @Override
        public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
            DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
            String sysName = systemNames.get(getSystemName());
            String userName = userNames.get(getSystemName());
            if (sysName == null) sysName = manager.getAutoSystemName();
            MyAction copy = new MyAction(sysName, userName);
            copy.setComment(getComment());
            return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
        }

        @Override
        public String getShortDescription(Locale locale) {
            return "MyAction";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "MyAction";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void execute() throws JmriException {
            SymbolTable symbolTable = this.getConditionalNG().getSymbolTable();
            _cells.add(symbolTable.getValue("MyVariable").toString());
        }
        
    }
    
}
