// DefaultConditionalManagerXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.ConditionalManager;
import jmri.DefaultConditionalManager;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * Provides the functionality for
 * configuring ConditionalManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.8 $
 */
public class DefaultConditionalManagerXml extends AbstractNamedBeanManagerConfigXML {

    public DefaultConditionalManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * ConditionalManager
     * @param o Object to store, of type ConditionalManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element conditionals = new Element("conditionals");
        setStoreElementClass(conditionals);
        ConditionalManager tm = (ConditionalManager) o;
        if (tm!=null) {
            java.util.Iterator iter = tm.getSystemNameList().iterator();
            
            // don't return an element if there are not conditionals to include
            if (!iter.hasNext()) return null;
            
            // store the conditionals
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("conditional system name is "+sname);
                Conditional c = tm.getBySystemName(sname);
                Element elem = new Element("conditional").setAttribute("systemName", sname);
                
                // store common parts
                storeCommon(c, elem);
                elem.setAttribute("antecedent", c.getAntecedentExpression());
                elem.setAttribute("logicType", Integer.toString(c.getLogicType()));

				// save child state variables
                ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
                for (int k=0; k < variableList.size(); k++) {
                    ConditionalVariable variable = variableList.get(k); 
                    Element vElem = new Element("conditionalStateVariable");
                    int oper = variable.getOpern();
                    if (oper == Conditional.OPERATOR_AND && variable.isNegated()) {
                        oper = Conditional.OPERATOR_AND_NOT;    // backward compatibility
                    } else if (oper == Conditional.OPERATOR_NONE && variable.isNegated()) {
                        oper = Conditional.OPERATOR_NOT;        // backward compatibility
                    }
                    vElem.setAttribute("operator",Integer.toString(oper));
                    if (variable.isNegated())
                        vElem.setAttribute("negated","yes");
                    else
                        vElem.setAttribute("negated","no");
                    vElem.setAttribute("type",Integer.toString(variable.getType()));
                    vElem.setAttribute("systemName", variable.getName());
                    vElem.setAttribute("dataString", variable.getDataString());
                    vElem.setAttribute("num1",Integer.toString(variable.getNum1()));
                    vElem.setAttribute("num2",Integer.toString(variable.getNum2()));
                    if (variable.doCalculation()) 
                        vElem.setAttribute("triggersCalc","yes");
                    else
                        vElem.setAttribute("triggersCalc","no");						
                    elem.addContent(vElem);
                }
				// save action information
                ArrayList actionList = c.getCopyOfActions();
				for (int k=0; k < actionList.size(); k++) {
                    ConditionalAction action = (ConditionalAction)actionList.get(k);
					Element aElem = new Element("conditionalAction");
					aElem.setAttribute("option",Integer.toString(action.getOption()));
					aElem.setAttribute("type",Integer.toString(action.getType()));
					aElem.setAttribute("systemName",action.getDeviceName());
					aElem.setAttribute("data",Integer.toString(action.getActionData()));
                    // To allow regression of config files back to previous releases
                    // add "delay" attribute 
                    try {
                        Integer.parseInt(action.getActionString());
                        aElem.setAttribute("delay",action.getActionString());
                    } catch (NumberFormatException nfe) {
                        aElem.setAttribute("delay","0");
                    }
					aElem.setAttribute("string",action.getActionString());
					elem.addContent(aElem);
				}
				conditionals.addContent(elem);
			}			
		}
		return (conditionals);	
	}
	
    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param conditionals The top-level element being created
     */
    public void setStoreElementClass(Element conditionals) {
        conditionals.setAttribute("class","jmri.configurexml.DefaultConditionalManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a ConditionalManager object of the correct class, then
     * register and fill it.
     * @param conditionals Top level Element to unpack.
     */
    public void load(Element conditionals) {
        // create the master object
        replaceConditionalManager();
        // load individual logixs
        loadConditionals(conditionals);
    }

    /**
     * Utility method to load the individual Logix objects.
     * If there's no additional info needed for a specific logix type,
     * invoke this with the parent of the set of Logix elements.
     * @param conditionals Element containing the Logix elements to load.
     */
    public void loadConditionals(Element conditionals) {
		List conditionalList = conditionals.getChildren("conditional");
        if (log.isDebugEnabled()) log.debug("Found "+conditionalList.size()+" conditionals");
        ConditionalManager tm = InstanceManager.conditionalManagerInstance();

        for (int i=0; i<conditionalList.size(); i++) {
            if ( ((Element)(conditionalList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(conditionalList.get(i)))+
									" "+((Element)(conditionalList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(conditionalList.get(i))).getAttribute("systemName").
																				getValue();
            String userName = null;
            if ( ((Element)(conditionalList.get(i))).getAttribute("userName") != null)
                userName = ((Element)(conditionalList.get(i))).getAttribute("userName").
																				getValue();
				
			if (log.isDebugEnabled()) log.debug("create conditional: ("+sysName+")("+
										(userName==null?"<null>":userName)+")");
            Conditional c = tm.createNewConditional(sysName, userName);
            if (c!=null) {
                // load common parts
                loadCommon(c, (Element)(conditionalList.get(i)));

                String ant = "";
                int logicType = Conditional.ALL_AND;
                if (((Element)(conditionalList.get(i))).getAttribute("antecedent") != null)
                {
                    ant = ((Element)(conditionalList.get(i))).getAttribute("antecedent").getValue();
                }
                if (((Element)(conditionalList.get(i))).getAttribute("logicType") != null)
                {
                    logicType = Integer.parseInt(
                        ((Element)(conditionalList.get(i))).getAttribute("logicType").getValue());
                }
                c.setLogicType(logicType, ant);
                
				// load state variables, if there are any
                List conditionalVarList = ((Element)(conditionalList.get(i))).
												getChildren("conditionalStateVariable");

				if (conditionalVarList.size() == 0) {
                    log.error("No state variables found for conditional "+sysName);
                }
                ArrayList <ConditionalVariable> variableList = new ArrayList <ConditionalVariable> ();
                for (int n=0; n<conditionalVarList.size(); n++)
                {
                    ConditionalVariable variable = new ConditionalVariable();
                    if ( ((Element)(conditionalVarList.get(n))).getAttribute("operator") == null) {
                        log.warn("unexpected null in operator "+((Element)(conditionalVarList.get(n)))+
                            " "+((Element)(conditionalVarList.get(n))).getAttributes());
                    } else {
                        int oper = Integer.parseInt(((Element)(conditionalVarList.get(n)))
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
                    if ( ((Element)(conditionalVarList.get(n))).getAttribute("negated") != null) {
                        if ("yes".equals(((Element)(conditionalVarList.get(n)))
                                                .getAttribute("negated").getValue()))
                            variable.setNegation(true);
                        else
                            variable.setNegation(false);
                     }
                    variable.setType(Integer.parseInt(((Element)(conditionalVarList.get(n)))
                                                    .getAttribute("type").getValue()));
                    variable.setName(((Element)(conditionalVarList.get(n)))
                                                    .getAttribute("systemName").getValue());
                    if (((Element)(conditionalVarList.get(n))).getAttribute("dataString") != null) {
                        variable.setDataString(((Element)(conditionalVarList.get(n)))
                                                        .getAttribute("dataString").getValue());
                    }
                    if (((Element)(conditionalVarList.get(n))).getAttribute("num1") != null) {
                        variable.setNum1(Integer.parseInt(((Element)(conditionalVarList.get(n)))
                                                        .getAttribute("num1").getValue()));
                    }
                    if (((Element)(conditionalVarList.get(n))).getAttribute("num2") != null) {
                        variable.setNum2(Integer.parseInt(((Element)(conditionalVarList.get(n)))
                                                        .getAttribute("num2").getValue()));
                    }
                    variable.setTriggerCalculation(true);
                    if (((Element)(conditionalVarList.get(n))).getAttribute("triggersCalc") != null) {
                        if ("no".equals(((Element)(conditionalVarList.get(n)))
                                                .getAttribute("triggersCalc").getValue()))
                            variable.setTriggerCalculation(false);
                    }
                    variableList.add(variable);
				}
                c.setStateVariables(variableList);

				// load actions - there better be some
                List conditionalActionList = ((Element)(conditionalList.get(i))).
												getChildren("conditionalAction");

				if (conditionalActionList.size() == 0) {
                    log.warn("No actions found for conditional "+sysName);
                }
                ArrayList <ConditionalAction> actionList = new ArrayList <ConditionalAction> ();
                org.jdom.Attribute attr = null;
                for (int n=0; n<conditionalActionList.size(); n++)
                {
                    ConditionalAction action = new ConditionalAction();
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("option");
                    if ( attr != null) {
                        action.setOption(Integer.parseInt(attr.getValue()));
                    }
                    else {
                        log.warn("unexpected null in option "+((Element)(conditionalActionList.get(n)))+
                            " "+((Element)(conditionalActionList.get(n))).getAttributes());
                    }
                    // actionDelay is removed.  delay data is stored as a String to allow
                    // such data be referenced by internal memory.
                    // For backward compatibility, set delay "int" as a string
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("delay");
                    if (attr != null)
                    {
                        action.setActionString(attr.getValue());
                    }
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("type");
                    if ( attr != null) {
                        action.setType(Integer.parseInt(attr.getValue()));
                    }
                    else {
                        log.warn("unexpected null in type "+((Element)(conditionalActionList.get(n)))+
                            " "+((Element)(conditionalActionList.get(n))).getAttributes());
                    }
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("systemName");
                    if ( attr != null) {
                        action.setDeviceName(attr.getValue());
                    }
                    else {
                        log.warn("unexpected null in systemName "+((Element)(conditionalActionList.get(n)))+
                            " "+((Element)(conditionalActionList.get(n))).getAttributes());
                    }
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("data");
                    if ( attr != null) {
                        action.setActionData(Integer.parseInt(attr.getValue()));
                    }
                    else {
                        log.warn("unexpected null in action data "+((Element)(conditionalActionList.get(n)))+
                            " "+((Element)(conditionalActionList.get(n))).getAttributes());
                    }
                    attr = ((Element)(conditionalActionList.get(n))).getAttribute("string");
                    if ( attr != null) {
                        String str = attr.getValue().trim();
                        if (str.length() > 0 )
                        {
                            action.setActionString(str);
                        }
                    }
                    else {
                        log.warn("unexpected null in action string "+((Element)(conditionalActionList.get(n)))+
                            " "+((Element)(conditionalActionList.get(n))).getAttributes());
                    }
                    actionList.add(action);
                }
			    c.setAction(actionList);
            } else {
                log.error("createNewConditional failed for " + sysName + ", " +userName);
            }
        }
	}

    /**
     * Replace the current ConditionalManager, if there is one, with
     * one newly created during a load operation. This is skipped
     * if they are of the same absolute type.
     */
    protected void replaceConditionalManager() {
        if (InstanceManager.conditionalManagerInstance().getClass().getName()
                .equals(DefaultConditionalManager.class.getName()))
            return;
        // if old manager exists, remove it from configuration process
        if (InstanceManager.conditionalManagerInstance() != null)
            InstanceManager.configureManagerInstance().deregister(
                InstanceManager.conditionalManagerInstance() );
        // register new one with InstanceManager
        DefaultConditionalManager pManager = DefaultConditionalManager.instance();
        InstanceManager.setConditionalManager(pManager);
        // register new one for configuration
        InstanceManager.configureManagerInstance().registerConfig(pManager);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultConditionalManagerXml.class.getName());
}
