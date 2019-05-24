package jmri.managers.configurexml;

import java.util.ArrayList;
import java.util.List;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultConditionalAction;
import jmri.managers.DefaultConditionalManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for configuring ConditionalManagers.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Pete Cressman Copyright (C) 2009, 2011
 */
public class DefaultConditionalManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultConditionalManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a ConditionalManager
     *
     * @param o Object to store, of type ConditionalManager
     * @return Element containing the complete info
     */
    @Override
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    public Element store(Object o) {
//    	long numCond = 0;
//    	long numStateVars = 0;
        Element conditionals = new Element("conditionals");  // NOI18N
        setStoreElementClass(conditionals);
        ConditionalManager tm = (ConditionalManager) o;
        if (tm != null) {
            java.util.Iterator<String> iter = tm.getSystemNameList().iterator();

            // don't return an element if there are not conditionals to include
            if (!iter.hasNext()) {
                return null;
            }

            // store the conditionals
            while (iter.hasNext()) {
//            	numCond++;
//            	long condTime = System.currentTimeMillis();
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during store");  // NOI18N
                }
                log.debug("conditional system name is " + sname);  // NOI18N
                Conditional c = tm.getBySystemName(sname);
                if (c == null) {
                    log.error("Unable to save '{}' to the XML file", sname);  // NOI18N
                    continue;
                }
                Element elem = new Element("conditional");  // NOI18N

                // As a work-around for backward compatibility, store systemName and username as attribute.
                // Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                elem.setAttribute("systemName", sname);  // NOI18N
                String uName = c.getUserName();
                if (uName != null && !uName.isEmpty()) {
                    elem.setAttribute("userName", uName);  // NOI18N
                }

                elem.addContent(new Element("systemName").addContent(sname));

                // store common parts
                storeCommon(c, elem);
                elem.setAttribute("antecedent", c.getAntecedentExpression());  // NOI18N
                elem.setAttribute("logicType", Integer.toString(c.getLogicType().getIntValue()));  // NOI18N
                if (c.getTriggerOnChange()) {
                    elem.setAttribute("triggerOnChange", "yes");  // NOI18N
                } else {
                    elem.setAttribute("triggerOnChange", "no");  // NOI18N
                }

                // save child state variables
                // Creating StateVariables gets very slow when more than c10,000 exist.
                // creation time goes from less than 1ms to more than 5000ms.
                // Don't need a clone for read-only use.
//                List <ConditionalVariable> variableList = c.getCopyOfStateVariables();
                List<ConditionalVariable> variableList = ((jmri.implementation.DefaultConditional) c).getStateVariableList();
                /*                numStateVars += variableList.size();
                 if (numCond>1190) {
                 partTime = System.currentTimeMillis() - partTime;
                 System.out.println("time to for getCopyOfStateVariables "+partTime+"ms. total stateVariable= "+numStateVars);
                 }*/
                for (int k = 0; k < variableList.size(); k++) {
                    ConditionalVariable variable = variableList.get(k);
                    Element vElem = new Element("conditionalStateVariable");  // NOI18N
                    int oper = variable.getOpern().getIntValue();
                    vElem.setAttribute("operator", Integer.toString(oper));  // NOI18N
                    if (variable.isNegated()) {
                        vElem.setAttribute("negated", "yes");  // NOI18N
                    } else {
                        vElem.setAttribute("negated", "no");  // NOI18N
                    }
                    vElem.setAttribute("type", Integer.toString(variable.getType().getIntValue()));  // NOI18N
                    vElem.setAttribute("systemName", variable.getName());  // NOI18N
                    vElem.setAttribute("dataString", variable.getDataString());  // NOI18N
                    vElem.setAttribute("num1", Integer.toString(variable.getNum1()));  // NOI18N
                    vElem.setAttribute("num2", Integer.toString(variable.getNum2()));  // NOI18N
                    if (variable.doTriggerActions()) {
                        vElem.setAttribute("triggersCalc", "yes");  // NOI18N
                    } else {
                        vElem.setAttribute("triggersCalc", "no");  // NOI18N
                    }
                    elem.addContent(vElem);
                }
                // save action information
                List<ConditionalAction> actionList = c.getCopyOfActions();
                /*               	if (numCond>1190) {
                 partTime = System.currentTimeMillis() - partTime;
                 System.out.println("time to for getCopyOfActions "+partTime+"ms. numActions= "+actionList.size());
                 }*/
                for (int k = 0; k < actionList.size(); k++) {
                    ConditionalAction action = actionList.get(k);
                    Element aElem = new Element("conditionalAction");  // NOI18N
                    aElem.setAttribute("option", Integer.toString(action.getOption()));  // NOI18N
                    aElem.setAttribute("type", Integer.toString(action.getType().getIntValue()));  // NOI18N
                    aElem.setAttribute("systemName", action.getDeviceName());  // NOI18N
                    aElem.setAttribute("data", Integer.toString(action.getActionData()));  // NOI18N
                    // To allow regression of config files back to previous releases
                    // add "delay" attribute
                    try {
                        Integer.parseInt(action.getActionString());
                        aElem.setAttribute("delay", action.getActionString());  // NOI18N
                    } catch (NumberFormatException nfe) {
                        aElem.setAttribute("delay", "0");  // NOI18N
                    }
                    aElem.setAttribute("string", action.getActionString());  // NOI18N
                    elem.addContent(aElem);
                }
                conditionals.addContent(elem);
                /*				condTime = System.currentTimeMillis() - condTime;
                 if (condTime>1) {
                 System.out.println(numCond+"th Conditional \""+sname+"\" took "+condTime+"ms to store.");
                 }*/
            }
        }
//        System.out.println("Elapsed time to store "+numCond+" Conditional "+(System.currentTimeMillis()-time)+"ms.");
        return (conditionals);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param conditionals The top-level element being created
     */
    public void setStoreElementClass(Element conditionals) {
        conditionals.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");  // NOI18N
    }

    /**
     * Create a ConditionalManager object of the correct class, then register
     * and fill it.
     *
     * @param sharedConditionals  Shared top level Element to unpack.
     * @param perNodeConditionals Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedConditionals, Element perNodeConditionals) {
        // create the master object
        replaceConditionalManager();
        // load individual logixs
        loadConditionals(sharedConditionals);
        return true;
    }

    /**
     * Utility method to load the individual Logix objects. If there's no
     * additional info needed for a specific logix type, invoke this with the
     * parent of the set of Logix elements.
     *
     * @param conditionals Element containing the Logix elements to load.
     */
    public void loadConditionals(Element conditionals) {
        List<Element> conditionalList = conditionals.getChildren("conditional");  // NOI18N
        if (log.isDebugEnabled()) {
            log.debug("Found " + conditionalList.size() + " conditionals");  // NOI18N
        }
        ConditionalManager tm = InstanceManager.getDefault(jmri.ConditionalManager.class);

        for (int i = 0; i < conditionalList.size(); i++) {
            Element condElem = conditionalList.get(i);
            String sysName = getSystemName(condElem);
            if (sysName == null) {
                log.warn("unexpected null in systemName " + condElem);  // NOI18N
                break;
            }

            // omitted username is treated as empty, not null
            String userName = getUserName(condElem);
            if (userName == null) {
                userName = "";
            }

            if (log.isDebugEnabled()) {
                log.debug("create conditional: ({})({})", sysName, userName);  // NOI18N
            }

            // Try getting the conditional.  This should fail
            Conditional c = tm.getBySystemName(sysName);
            if (c == null) {
                // Check for parent Logix
                Logix x = tm.getParentLogix(sysName);
                if (x == null) {
                    log.warn("Conditional '{}' has no parent Logix", sysName);  // NOI18N
                    continue;
                }

                // Found a potential parent Logix, check the Logix index
                boolean inIndex = false;
                for (int j = 0; j < x.getNumConditionals(); j++) {
                    String cName = x.getConditionalByNumberOrder(j);
                    if (sysName.equals(cName)) {
                        inIndex = true;
                        break;
                    }
                }
                if (!inIndex) {
                    log.warn("Conditional '{}' is not in the Logix index", sysName);  // NOI18N
                    continue;
                }

                // Create the condtional
                c = tm.createNewConditional(sysName, userName);
            }

            if (c == null) {
                // Should never get here
                log.error("Conditional '{}' cannot be created", sysName);  // NOI18N
                continue;
            }

            // conditional already exists
            // load common parts
            loadCommon(c, condElem);

            String ant = "";
            int logicType = Conditional.ALL_AND;
            if (condElem.getAttribute("antecedent") != null) {  // NOI18N
                String antTemp = condElem.getAttribute("antecedent").getValue();  // NOI18N
                ant = jmri.jmrit.conditional.ConditionalEditBase.translateAntecedent(antTemp, true);
            }
            if (condElem.getAttribute("logicType") != null) {  // NOI18N
                logicType = Integer.parseInt(
                        condElem.getAttribute("logicType").getValue());  // NOI18N
            }
            c.setLogicType(Conditional.AntecedentOperator.getOperatorFromIntValue(logicType), ant);

            // load state variables, if there are any
            List<Element> conditionalVarList = condElem.getChildren("conditionalStateVariable");  // NOI18N

            // Note: Because things like (R1 or R2) and R3) return to positions in the
            // list of state variables, we can't just append when re-reading a conditional;
            // we have to drop any existing ConditionalStateVariables and create a clean, new list.

            if (conditionalVarList.size() == 0) {
                log.warn("No state variables found for conditional " + sysName);  // NOI18N
            }
            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            for (int n = 0; n < conditionalVarList.size(); n++) {
                ConditionalVariable variable = new ConditionalVariable();
                if (conditionalVarList.get(n).getAttribute("operator") == null) {  // NOI18N
                    log.warn("unexpected null in operator " + conditionalVarList.get(n)  // NOI18N
                            + " " + conditionalVarList.get(n).getAttributes());
                } else {
                    int oper = Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("operator").getValue());  // NOI18N
                    Conditional.Operator operator = Conditional.Operator.getOperatorFromIntValue(oper);
                    variable.setOpern(operator);
                }
                if (conditionalVarList.get(n).getAttribute("negated") != null) {  // NOI18N
                    if ("yes".equals(conditionalVarList.get(n)
                            .getAttribute("negated").getValue())) {  // NOI18N
                        variable.setNegation(true);
                    } else {
                        variable.setNegation(false);
                    }
                }
                variable.setType(Conditional.Type.getOperatorFromIntValue(
                        Integer.parseInt(conditionalVarList.get(n).getAttribute("type").getValue())));  // NOI18N
                variable.setName(conditionalVarList.get(n)
                        .getAttribute("systemName").getValue());  // NOI18N
                if (conditionalVarList.get(n).getAttribute("dataString") != null) {  // NOI18N
                    variable.setDataString(conditionalVarList.get(n)
                            .getAttribute("dataString").getValue());  // NOI18N
                }
                if (conditionalVarList.get(n).getAttribute("num1") != null) {  // NOI18N
                    variable.setNum1(Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("num1").getValue()));  // NOI18N
                }
                if (conditionalVarList.get(n).getAttribute("num2") != null) {  // NOI18N
                    variable.setNum2(Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("num2").getValue()));  // NOI18N
                }
                variable.setTriggerActions(true);
                if (conditionalVarList.get(n).getAttribute("triggersCalc") != null) {  // NOI18N
                    if ("no".equals(conditionalVarList.get(n)
                            .getAttribute("triggersCalc").getValue())) {  // NOI18N
                        variable.setTriggerActions(false);
                    }
                }
                variableList.add(variable);
            }
            c.setStateVariables(variableList);

            // load actions - there better be some
            List<Element> conditionalActionList = condElem.getChildren("conditionalAction");  // NOI18N

            // Really OK, since a user may use such conditionals to define a reusable
            // expression of state variables.  These conditions are then used as a
            // state variable in other conditionals.  (pwc)
            //if (conditionalActionList.size() == 0) {
            //    log.warn("No actions found for conditional "+sysName);
            //}
            List<ConditionalAction> actionList = ((DefaultConditional)c).getActionList();
            org.jdom2.Attribute attr = null;
            for (int n = 0; n < conditionalActionList.size(); n++) {
                ConditionalAction action = new DefaultConditionalAction();
                attr = conditionalActionList.get(n).getAttribute("option");  // NOI18N
                if (attr != null) {
                    action.setOption(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in option " + conditionalActionList.get(n)  // NOI18N
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                // actionDelay is removed.  delay data is stored as a String to allow
                // such data be referenced by internal memory.
                // For backward compatibility, set delay "int" as a string
                attr = conditionalActionList.get(n).getAttribute("delay");  // NOI18N
                if (attr != null) {
                    action.setActionString(attr.getValue());
                }
                attr = conditionalActionList.get(n).getAttribute("type");  // NOI18N
                if (attr != null) {
                    action.setType(Conditional.Action.getOperatorFromIntValue(Integer.parseInt(attr.getValue())));
                } else {
                    log.warn("unexpected null in type " + conditionalActionList.get(n)  // NOI18N
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("systemName");  // NOI18N
                if (attr != null) {
                    action.setDeviceName(attr.getValue());
                } else {
                    log.warn("unexpected null in systemName " + conditionalActionList.get(n)  // NOI18N
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("data");  // NOI18N
                if (attr != null) {
                    action.setActionData(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in action data " + conditionalActionList.get(n)  // NOI18N
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("string");  // NOI18N
                if (attr != null) {
                    action.setActionString(attr.getValue());
                } else {
                    log.warn("unexpected null in action string " + conditionalActionList.get(n)  // NOI18N
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                if (!actionList.contains(action)) actionList.add(action);
            }
            c.setAction(actionList);

            // 1/16/2011 - trigger for execution of the action list changed to execute each
            // time state is computed.  Formerly execution of the action list was done only
            // when state changes.  All conditionals are upgraded to this new policy.
            // However, for conditionals with actions that toggle on change of state
            // the old policy should be used.
            boolean triggerOnChange = false;
            if (condElem.getAttribute("triggerOnChange") != null) {  // NOI18N
                if ("yes".equals(condElem.getAttribute("triggerOnChange").getValue())) {  // NOI18N
                    triggerOnChange = true;
                }
            } else {
                /* Don't upgrade -Let old be as is
                 for (int k=0; k<actionList.size(); k++){
                 ConditionalAction action = actionList.get(k);
                 if (action.getOption()==Conditional.ACTION_OPTION_ON_CHANGE){
                 triggerOnChange = true;
                 break;
                 }
                 }
                 */
                triggerOnChange = true;
            }
            c.setTriggerOnChange(triggerOnChange);
        }
    }

    /**
     * Replace the current ConditionalManager, if there is one, with one newly
     * created during a load operation. This is skipped if they are of the same
     * absolute type.
     */
    protected void replaceConditionalManager() {
        if (InstanceManager.getDefault(jmri.ConditionalManager.class).getClass().getName()
                .equals(DefaultConditionalManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.ConditionalManager.class) != null) {
            InstanceManager.getDefault(jmri.ConfigureManager.class).deregister(
                    InstanceManager.getDefault(jmri.ConditionalManager.class));
        }
        // register new one with InstanceManager
        DefaultConditionalManager pManager = DefaultConditionalManager.instance();
        InstanceManager.store(pManager, ConditionalManager.class);
        InstanceManager.setDefault(ConditionalManager.class, pManager);
        // register new one for configuration
        InstanceManager.getDefault(jmri.ConfigureManager.class).registerConfig(pManager, jmri.Manager.CONDITIONALS);
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.ConditionalManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultConditionalManagerXml.class);
}
