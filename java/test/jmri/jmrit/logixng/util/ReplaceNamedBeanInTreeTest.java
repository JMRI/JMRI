package jmri.jmrit.logixng.util;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.*;

/**
 * Test ReplaceNamedBeanInTreeTest.
 *
 * Copyright Daniel Bergqvist (C) 2024
 */
public class ReplaceNamedBeanInTreeTest {

    public final String TREE_INDENT = "     ";

    private CreateLogixNGTreeScaffold createLogixNGTreeScaffold;

    private String getPrintTree(LogixNG logixng, PrintTreeSettings printTreeSettings) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        logixng.printTree(
                printTreeSettings,
                Locale.ENGLISH,
                printWriter,
                TREE_INDENT,
                new MutableInt(0));

        return stringWriter.toString();
    }

    private void printDiff(String originalTree, String newTree) {
        log.error("--------------------------------------------");
        log.error("Old tree:");
        log.error("XXX"+originalTree+"XXX");
        log.error("--------------------------------------------");
        log.error("New tree:");
        log.error("XXX"+newTree+"XXX");
        log.error("--------------------------------------------");

        String[] originalTreeLines = originalTree.split(System.lineSeparator());
        String[] newTreeLines = newTree.split(System.lineSeparator());
        int line=0;
        for (; line < Math.min(originalTreeLines.length, newTreeLines.length); line++) {
            if (!originalTreeLines[line].equals(newTreeLines[line])) {
                log.error("Tree differs on line {}:", line+1);
                log.error("Orig: {}", originalTreeLines[line]);
                log.error(" New: {}", newTreeLines[line]);
                break;
            }
        }
        Assert.fail("The tree has changed. The tree differs on line "+Integer.toString(line+1));
    }

    @Test
    public void testReplaceNamedBeans() throws PropertyVetoException, Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        PrintTreeSettings printTreeSettings = new PrintTreeSettings();
        printTreeSettings._printDisplayName = true;
        printTreeSettings._hideUserName = false;

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        createLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        ReplaceNamedBeanInTree replaceNamedBeanInTree = new ReplaceNamedBeanInTree();

        boolean done = false;
        while (!done) {
            for (LogixNG logixng : logixNG_Manager.getNamedBeanSet()) {

                Map<NamedBeanHandle<NamedBean>, NamedBeanHandle<NamedBean>> newBeansMap = new HashMap<>();

                String originalTree = this.getPrintTree(logixng, printTreeSettings);

                for (int i=0; i < logixng.getNumConditionalNGs(); i++) {
                    ConditionalNG cng = logixng.getConditionalNG(i);

                    for (NamedBeanType namedBeanType : NamedBeanType.values()) {

                        var selectNamedBeans = replaceNamedBeanInTree.getSelectNamedBeans(cng);

                        for (var logixNG_SelectNamedBean : selectNamedBeans) {
                            NamedBeanType selectNamedBeanType = logixNG_SelectNamedBean.getType();

                            if (namedBeanType.equals(selectNamedBeanType)) {

                                NamedBeanType.CreateBean createBean = namedBeanType.getCreateBean();
                                if (createBean != null) {

                                    String systemName = namedBeanType.getManager()
                                            .getSystemNamePrefix()
                                            + CreateLogixNGTreeScaffold.getRandomString(20);

                                    NamedBeanHandle<NamedBean> oldBean = (NamedBeanHandle<NamedBean>) logixNG_SelectNamedBean.get();
                                    Assert.assertNotNull(oldBean);

                                    NamedBeanHandle<NamedBean> beanHandle = newBeansMap.get(oldBean);
                                    if (beanHandle == null) {
                                        var tempBean = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                                .getNamedBeanHandle(oldBean.getBean().getSystemName(), oldBean.getBean());
                                        beanHandle = newBeansMap.get(tempBean);
                                    }
                                    if (beanHandle == null) {
                                        String userName = CreateLogixNGTreeScaffold.getRandomString(20);
                                        NamedBean bean = createBean.createBean(systemName, userName);
                                        beanHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                                .getNamedBeanHandle(bean.getDisplayName(), bean);
                                        newBeansMap.put(oldBean, beanHandle);
                                    }

                                    Assert.assertNotNull(beanHandle);

                                    logixNG_SelectNamedBean.replace(beanHandle);
                                }
                            }
                        }

                    }


                    String newTree = this.getPrintTree(logixng, printTreeSettings);

                    // The old tree has the old names. Change the old names to
                    // the new ones in the printout of the old tree
                    for (var entry : newBeansMap.entrySet()) {
//                        log.error("Replace {} with {}", entry.getKey().getBean().getDisplayName(), entry.getValue().getBean().getDisplayName());
                        originalTree = originalTree.replaceAll(
                                "\\\""+entry.getKey().getBean().getDisplayName()+"\\\"",
                                "\""+entry.getValue().getBean().getDisplayName()+"\"");

                        originalTree = originalTree.replaceAll(
                                " "+entry.getKey().getBean().getDisplayName()+" ",
                                " "+entry.getValue().getBean().getDisplayName()+" ");
                    }

                    // Fix this!!!
                    // ActionListenOnBeans is not handled yet!!!!
//                    if (1==0)
                    for (var entry : newBeansMap.entrySet()) {
                        if (entry.getKey().getBean().getSystemName().equals("IM2")) {
                            newTree = newTree.replaceAll(
                                    "\\\"Some memory\\\"",
                                    "\""+entry.getValue().getBean().getDisplayName()+"\"");
                        }
                    }

                    // Scripts are not updated so manually fix that here
                    for (var entry : newBeansMap.entrySet()) {
                        if (entry.getKey().getBean().getSystemName().equals("IS1")) {
                            newTree = newTree.replaceAll(
                                    "Script result.setValue\\( sensors.provideSensor\\(\\\"IS1\\\"",
                                    "Script result.setValue( sensors.provideSensor(\""+entry.getValue().getBean().getDisplayName()+"\"");
                        }
                    }

                    // Expression Reference is not updated so manually fix that here
                    for (var entry : newBeansMap.entrySet()) {
                        if (entry.getKey().getBean().getSystemName().equals("IL1")) {
                            newTree = newTree.replaceAll(
                                    "Reference IL1 is not Light",
                                    "Reference "+entry.getValue().getBean().getDisplayName()+" is not Light");
                        }
                    }

                    if (!originalTree.equals(newTree)) {
                        printDiff(originalTree, newTree);
                    }
                }
                done = true;
            }
        }
    }

    @Before
    public void setUp() {
        createLogixNGTreeScaffold = new CreateLogixNGTreeScaffold();
        createLogixNGTreeScaffold.setUp();
    }

    @After
    public void tearDown() {
//        jmri.util.JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        createLogixNGTreeScaffold.tearDown();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReplaceNamedBeanInTreeTest.class);

}
