package jmri.managers.configurexml;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

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
    public Element store(Object o) {
        // long numCond = 0;
        // long numStateVars = 0;
        Element conditionals = new Element("conditionals");  // NOI18N
        setStoreElementClass(conditionals);
        ConditionalManager cm = (ConditionalManager) o;
        if (cm != null) {
            SortedSet<Conditional> condList = cm.getNamedBeanSet();
            // don't return an element if there are no conditionals to include
            if (condList.isEmpty()) {
                return null;
            }
            for (Conditional c : condList) {
                // store the conditionals
                // numCond++;
                // long condTime = System.currentTimeMillis();
                String cName = c.getSystemName();
                log.debug("conditional system name is {}", cName);  // NOI18N

                Element elem = new Element("conditional");  // NOI18N

                // As a work-around for backward compatibility, store systemName and userName as attributes.
                // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                elem.setAttribute("systemName", cName);  // NOI18N
                String uName = c.getUserName();
                if (uName != null && !uName.isEmpty()) {
                    elem.setAttribute("userName", uName);  // NOI18N
                }

                elem.addContent(new Element("systemName").addContent(cName));

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
                List<ConditionalVariable> variableList = ((DefaultConditional) c).getStateVariableList();
                /*                numStateVars += variableList.size();
                 if (numCond>1190) {
                 partTime = System.currentTimeMillis() - partTime;
                 System.out.println("time to for getCopyOfStateVariables "+partTime+"ms. total stateVariable= "+numStateVars);
                 }*/
                for (ConditionalVariable variable : variableList) {
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
                for (ConditionalAction action : actionList) {
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
                /* condTime = System.currentTimeMillis() - condTime;
                 if (condTime>1) {
                 System.out.println(numCond+"th Conditional \""+sName+"\" took "+condTime+"ms to store.");
                 }*/
            }
        }
        // System.out.println("Elapsed time to store "+numCond+" Conditional "+(System.currentTimeMillis()-time)+"ms.");
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
     * additional info needed for a specific Logix type, invoke this with the
     * parent of the set of Logix elements.
     *
     * @param conditionals Element containing the Logix elements to load.
     */
    public void loadConditionals(Element conditionals) {
        List<Element> conditionalList = conditionals.getChildren("conditional");  // NOI18N
        log.debug("Found {} conditionals", conditionalList.size());  // NOI18N
        ConditionalManager cm = InstanceManager.getDefault(jmri.ConditionalManager.class);

        for (Element condElem : conditionalList) {
            String sysName = getSystemName(condElem);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", condElem);  // NOI18N
                break;
            }

            // omitted username is treated as empty, not null
            String userName = getUserName(condElem);
            if (userName == null) {
                userName = "";
            }

            log.debug("create conditional: ({})({})", sysName, userName);  // NOI18N

            // Try getting the conditional.  This should fail
            Conditional c = cm.getBySystemName(sysName);
            if (c == null) {
                // Check for parent Logix
                Logix x = cm.getParentLogix(sysName);
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
                c = cm.createNewConditional(sysName, userName);
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
                log.warn("No state variables found for conditional {}", sysName);  // NOI18N
            }
            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            for (Element cvar : conditionalVarList) {
                ConditionalVariable variable = new ConditionalVariable();
                if (cvar.getAttribute("operator") == null) {    // NOI18N
                    log.warn("unexpected null in operator {} {}", cvar, // NOI18N
                            cvar.getAttributes());
                } else {
                    int oper = Integer.parseInt(cvar
                            .getAttribute("operator").getValue());  // NOI18N
                    // Adjust old, lt 4.13.4, xml content
                    if (oper == 2) oper = 4;
                    if (oper == 3) oper = 1;
                    if (oper == 6) oper = 5;
                    Conditional.Operator operator = Conditional.Operator.getOperatorFromIntValue(oper);
                    variable.setOpern(operator);
                }
                if (cvar.getAttribute("negated") != null) {  // NOI18N
                    if ("yes".equals(cvar
                            .getAttribute("negated").getValue())) {  // NOI18N
                        variable.setNegation(true);
                    } else {
                        variable.setNegation(false);
                    }
                }
                variable.setType(Conditional.Type.getOperatorFromIntValue(
                        Integer.parseInt(cvar.getAttribute("type").getValue())));  // NOI18N
                variable.setName(cvar
                        .getAttribute("systemName").getValue());  // NOI18N
                if (cvar.getAttribute("dataString") != null) {  // NOI18N
                    variable.setDataString(cvar
                            .getAttribute("dataString").getValue());  // NOI18N
                }
                if (cvar.getAttribute("num1") != null) {  // NOI18N
                    variable.setNum1(Integer.parseInt(cvar
                            .getAttribute("num1").getValue()));  // NOI18N
                }
                if (cvar.getAttribute("num2") != null) {  // NOI18N
                    variable.setNum2(Integer.parseInt(cvar
                            .getAttribute("num2").getValue()));  // NOI18N
                }
                variable.setTriggerActions(true);
                if (cvar.getAttribute("triggersCalc") != null) {  // NOI18N
                    if ("no".equals(cvar
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
            //    log.warn("No actions found for conditional {}", sysName);
            //}
            List<ConditionalAction> actionList = ((DefaultConditional)c).getActionList();
            org.jdom2.Attribute attr = null;
            for (Element cact : conditionalActionList) {
                ConditionalAction action = new DefaultConditionalAction();
                attr = cact.getAttribute("option");  // NOI18N
                if (attr != null) {
                    action.setOption(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in option {} {}", cact,  // NOI18N
                            cact.getAttributes());
                }
                // actionDelay is removed.  delay data is stored as a String to allow
                // such data be referenced by internal memory.
                // For backward compatibility, set delay "int" as a string
                attr = cact.getAttribute("delay");  // NOI18N
                if (attr != null) {
                    action.setActionString(attr.getValue());
                }
                attr = cact.getAttribute("type");  // NOI18N
                if (attr != null) {
                    action.setType(Conditional.Action.getOperatorFromIntValue(Integer.parseInt(attr.getValue())));
                } else {
                    log.warn("unexpected null in type {} {}", cact,
                            cact.getAttributes()); // NOI18N
                }
                attr = cact.getAttribute("systemName");  // NOI18N
                if (attr != null) {
                    action.setDeviceName(attr.getValue());
                } else {
                    log.warn("unexpected null in systemName {} {}", cact,  // NOI18N
                            cact.getAttributes());
                }
                attr = cact.getAttribute("data");  // NOI18N
                if (attr != null) {
                    action.setActionData(Integer.parseInt(attr.getValue()));
                } else {
                    log.warn("unexpected null in action data {} {}", cact,  // NOI18N
                            cact.getAttributes());
                }
                attr = cact.getAttribute("string");  // NOI18N
                if (attr != null) {
                    action.setActionString(attr.getValue());
                } else {
                    log.warn("unexpected null in action string {} {}", cact,  // NOI18N
                            cact.getAttributes());
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
        DefaultConditionalManager pManager = InstanceManager.getDefault(DefaultConditionalManager.class);
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
