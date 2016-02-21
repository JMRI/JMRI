// DefaultConditionalManagerXML.java
package jmri.managers.configurexml;

import java.util.ArrayList;
import java.util.List;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultConditionalAction;
import jmri.managers.DefaultConditionalManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for configuring ConditionalManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Pete Cressman Copyright (C) 2009, 2011
 * @version $Revision$
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
    public Element store(Object o) {
//    	long numCond = 0;
//    	long numStateVars = 0;
        Element conditionals = new Element("conditionals");
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
                    log.error("System name null during store");
                }
                log.debug("conditional system name is " + sname);
                Conditional c = tm.getBySystemName(sname);
                Element elem = new Element("conditional").setAttribute("systemName", sname);
                elem.addContent(new Element("systemName").addContent(sname));

                // store common parts
                storeCommon(c, elem);
                elem.setAttribute("antecedent", c.getAntecedentExpression());
                elem.setAttribute("logicType", Integer.toString(c.getLogicType()));
                if (c.getTriggerOnChange()) {
                    elem.setAttribute("triggerOnChange", "yes");
                } else {
                    elem.setAttribute("triggerOnChange", "no");
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
                    Element vElem = new Element("conditionalStateVariable");
                    int oper = variable.getOpern();
                    if (oper == Conditional.OPERATOR_AND && variable.isNegated()) {
                        oper = Conditional.OPERATOR_AND_NOT;    // backward compatibility
                    } else if (oper == Conditional.OPERATOR_NONE && variable.isNegated()) {
                        oper = Conditional.OPERATOR_NOT;        // backward compatibility
                    }
                    vElem.setAttribute("operator", Integer.toString(oper));
                    if (variable.isNegated()) {
                        vElem.setAttribute("negated", "yes");
                    } else {
                        vElem.setAttribute("negated", "no");
                    }
                    vElem.setAttribute("type", Integer.toString(variable.getType()));
                    vElem.setAttribute("systemName", variable.getName());
                    vElem.setAttribute("dataString", variable.getDataString());
                    vElem.setAttribute("num1", Integer.toString(variable.getNum1()));
                    vElem.setAttribute("num2", Integer.toString(variable.getNum2()));
                    if (variable.doTriggerActions()) {
                        vElem.setAttribute("triggersCalc", "yes");
                    } else {
                        vElem.setAttribute("triggersCalc", "no");
                    }
                    elem.addContent(vElem);
                }
                // save action information
                ArrayList<ConditionalAction> actionList = c.getCopyOfActions();
                /*               	if (numCond>1190) {
                 partTime = System.currentTimeMillis() - partTime;
                 System.out.println("time to for getCopyOfActions "+partTime+"ms. numActions= "+actionList.size());
                 }*/
                for (int k = 0; k < actionList.size(); k++) {
                    ConditionalAction action = actionList.get(k);
                    Element aElem = new Element("conditionalAction");
                    aElem.setAttribute("option", Integer.toString(action.getOption()));
                    aElem.setAttribute("type", Integer.toString(action.getType()));
                    aElem.setAttribute("systemName", action.getDeviceName());
                    aElem.setAttribute("data", Integer.toString(action.getActionData()));
                    // To allow regression of config files back to previous releases
                    // add "delay" attribute 
                    try {
                        Integer.parseInt(action.getActionString());
                        aElem.setAttribute("delay", action.getActionString());
                    } catch (NumberFormatException nfe) {
                        aElem.setAttribute("delay", "0");
                    }
                    aElem.setAttribute("string", action.getActionString());
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
        conditionals.setAttribute("class", this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a ConditionalManager object of the correct class, then register
     * and fill it.
     *
     * @param sharedConditionals Top level Element to unpack.
     * @param perNodeConditionals
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
        List<Element> conditionalList = conditionals.getChildren("conditional");
        if (log.isDebugEnabled()) {
            log.debug("Found " + conditionalList.size() + " conditionals");
        }
        ConditionalManager tm = InstanceManager.conditionalManagerInstance();

        for (int i = 0; i < conditionalList.size(); i++) {
            Element condElem = conditionalList.get(i);
            String sysName = getSystemName(condElem);
            if (sysName == null) {
                log.warn("unexpected null in systemName " + condElem);
                break;
            }

            String userName = "";  // omitted username is treated as empty, not null
            if (condElem.getAttribute("userName") != null) {
                userName = condElem.getAttribute("userName").getValue();
            }

            if (log.isDebugEnabled()) {
                log.debug("create conditional: (" + sysName + ")("
                        + (userName == null ? "<null>" : userName) + ")");
            }
            Conditional c = tm.getBySystemName(sysName);
            if (c == null) c = tm.createNewConditional(sysName, userName);

            // conditional already exists
            // load common parts
            loadCommon(c, condElem);

            String ant = "";
            int logicType = Conditional.ALL_AND;
            if (condElem.getAttribute("antecedent") != null) {
                ant = condElem.getAttribute("antecedent").getValue();
            }
            if (condElem.getAttribute("logicType") != null) {
                logicType = Integer.parseInt(
                        condElem.getAttribute("logicType").getValue());
            }
            c.setLogicType(logicType, ant);

            // load state variables, if there are any
            List<Element> conditionalVarList = condElem.getChildren("conditionalStateVariable");

            if (conditionalVarList.size() == 0) {
                log.warn("No state variables found for conditional " + sysName);
            }
            ArrayList<ConditionalVariable> variableList = ((DefaultConditional)c).getStateVariableList();
            for (int n = 0; n < conditionalVarList.size(); n++) {
                ConditionalVariable variable = new ConditionalVariable();
                if (conditionalVarList.get(n).getAttribute("operator") == null) {
                    log.warn("unexpected null in operator " + conditionalVarList.get(n)
                            + " " + conditionalVarList.get(n).getAttributes());
                } else {
                    int oper = Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("operator").getValue());
                    if (oper == Conditional.OPERATOR_AND_NOT) {
                        variable.setNegation(true);
                        oper = Conditional.OPERATOR_AND;
                    } else if (oper == Conditional.OPERATOR_NOT) {
                        variable.setNegation(true);
                        oper = Conditional.OPERATOR_NONE;
                    }
                    variable.setOpern(oper);
                }
                if (conditionalVarList.get(n).getAttribute("negated") != null) {
                    if ("yes".equals(conditionalVarList.get(n)
                            .getAttribute("negated").getValue())) {
                        variable.setNegation(true);
                    } else {
                        variable.setNegation(false);
                    }
                }
                variable.setType(Integer.parseInt(conditionalVarList.get(n)
                        .getAttribute("type").getValue()));
                variable.setName(conditionalVarList.get(n)
                        .getAttribute("systemName").getValue());
                if (conditionalVarList.get(n).getAttribute("dataString") != null) {
                    variable.setDataString(conditionalVarList.get(n)
                            .getAttribute("dataString").getValue());
                }
                if (conditionalVarList.get(n).getAttribute("num1") != null) {
                    variable.setNum1(Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("num1").getValue()));
                }
                if (conditionalVarList.get(n).getAttribute("num2") != null) {
                    variable.setNum2(Integer.parseInt(conditionalVarList.get(n)
                            .getAttribute("num2").getValue()));
                }
                variable.setTriggerActions(true);
                if (conditionalVarList.get(n).getAttribute("triggersCalc") != null) {
                    if ("no".equals(conditionalVarList.get(n)
                            .getAttribute("triggersCalc").getValue())) {
                        variable.setTriggerActions(false);
                    }
                }
                if (! variableList.contains(variable) ) variableList.add(variable);
            }
            c.setStateVariables(variableList);

            // load actions - there better be some
            List<Element> conditionalActionList = condElem.getChildren("conditionalAction");

            // Really OK, since a user may use such conditionals to define a reusable
            // expression of state variables.  These conditions are then used as a 
            // state variable in other conditionals.  (pwc)
            //if (conditionalActionList.size() == 0) {
            //    log.warn("No actions found for conditional "+sysName);
            //}
            ArrayList<ConditionalAction> actionList = ((DefaultConditional)c).getActionList();
            org.jdom2.Attribute attr = null;
            for (int n = 0; n < conditionalActionList.size(); n++) {
                ConditionalAction action = new DefaultConditionalAction();
                attr = conditionalActionList.get(n).getAttribute("option");
                if (attr != null) {
                    action.setOption(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in option " + conditionalActionList.get(n)
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                // actionDelay is removed.  delay data is stored as a String to allow
                // such data be referenced by internal memory.
                // For backward compatibility, set delay "int" as a string
                attr = conditionalActionList.get(n).getAttribute("delay");
                if (attr != null) {
                    action.setActionString(attr.getValue());
                }
                attr = conditionalActionList.get(n).getAttribute("type");
                if (attr != null) {
                    action.setType(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in type " + conditionalActionList.get(n)
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("systemName");
                if (attr != null) {
                    action.setDeviceName(attr.getValue());
                } else {
                    log.warn("unexpected null in systemName " + conditionalActionList.get(n)
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("data");
                if (attr != null) {
                    action.setActionData(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in action data " + conditionalActionList.get(n)
                            + " " + conditionalActionList.get(n).getAttributes());
                }
                attr = conditionalActionList.get(n).getAttribute("string");
                if (attr != null) {
                    action.setActionString(attr.getValue());
                } else {
                    log.warn("unexpected null in action string " + conditionalActionList.get(n)
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
            if (condElem.getAttribute("triggerOnChange") != null) {
                if ("yes".equals(condElem.getAttribute("triggerOnChange").getValue())) {
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
        if (InstanceManager.conditionalManagerInstance().getClass().getName()
                .equals(DefaultConditionalManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.conditionalManagerInstance() != null) {
            InstanceManager.configureManagerInstance().deregister(
                    InstanceManager.conditionalManagerInstance());
        }
        // register new one with InstanceManager
        DefaultConditionalManager pManager = DefaultConditionalManager.instance();
        InstanceManager.store(pManager, ConditionalManager.class);
        InstanceManager.setDefault(ConditionalManager.class, pManager);
        // register new one for configuration
        InstanceManager.getDefault(jmri.ConfigureManager.class).registerConfig(pManager, jmri.Manager.CONDITIONALS);
    }

    public int loadOrder() {
        return InstanceManager.conditionalManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultConditionalManagerXml.class.getName());
}
