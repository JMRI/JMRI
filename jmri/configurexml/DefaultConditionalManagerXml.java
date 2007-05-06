// DefaultConditionalManagerXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.DefaultConditionalManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the functionality for
 * configuring ConditionalManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.4 $
 */
public class DefaultConditionalManagerXml implements XmlAdapter {

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
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("conditional system name is "+sname);
                Conditional c = tm.getBySystemName(sname);
                String uname = c.getUserName();
                Element elem = new Element("conditional")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
				// save child state variables
				int numStateVariables = c.getNumStateVariables();
				if (numStateVariables>0) {
					Element cElem = null;
					for (int k = 0;k<numStateVariables;k++) {
						cElem = new Element("conditionalStateVariable");
						cElem.addAttribute("operator",Integer.toString(
								c.getStateVariableOperator(k)));
						cElem.addAttribute("type",Integer.toString(
								c.getStateVariableType(k)));
						cElem.addAttribute("systemName",
								c.getStateVariableName(k));
						cElem.addAttribute("dataString",
								c.getStateVariableDataString(k));
						cElem.addAttribute("num1",Integer.toString(
								c.getStateVariableNum1(k)));
						cElem.addAttribute("num2",Integer.toString(
								c.getStateVariableNum2(k)));
						if (c.getStateVariableTriggersCalculation(k)) 
							cElem.addAttribute("triggersCalc","yes");
						else
							cElem.addAttribute("triggersCalc","no");						
						elem.addContent(cElem);
					}
				}
				// save action information
				int[] opt = {0,0};
				int[] delay = {0,0};
				int[] type = {0,0};
				String[] name = {" "," "};
				int[] data = {0,0};
				String[] dataString = {" "," "};
				c.getAction(opt,delay,type,name,data,dataString);
				Element aElem = null;
				for (int k = 0;k<2;k++) {
					aElem = new Element("conditionalAction");
					aElem.addAttribute("option",Integer.toString(opt[k]));
					aElem.addAttribute("delay",Integer.toString(delay[k]));
					aElem.addAttribute("type",Integer.toString(type[k]));
					aElem.addAttribute("systemName",name[k]);
					aElem.addAttribute("data",Integer.toString(data[k]));
					aElem.addAttribute("string",dataString[k]);
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
        conditionals.addAttribute("class","jmri.configurexml.DefaultConditionalManagerXml");
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
				// load state variables, if there are any
                List conditionalVarList = ((Element)(conditionalList.get(i))).
												getChildren("conditionalStateVariable");
				if (conditionalVarList.size()>0) {
					// load state variables
					int[] svOperator = new int[Conditional.MAX_STATE_VARIABLES];
					int[] svType = new int[Conditional.MAX_STATE_VARIABLES];
					String[] svName = new String[Conditional.MAX_STATE_VARIABLES];
					String[] svDataString = new String[Conditional.MAX_STATE_VARIABLES];
					int[] svNum1 = new int[Conditional.MAX_STATE_VARIABLES];
					int[] svNum2 = new int[Conditional.MAX_STATE_VARIABLES];
					boolean[] svTriggersCalc = new boolean[Conditional.MAX_STATE_VARIABLES];
					int numVariables = conditionalVarList.size();
					// check if number is reasonable
					if (numVariables>Conditional.MAX_STATE_VARIABLES) {
						log.error("too many state variables found for conditional "+sysName);
						numVariables = Conditional.MAX_STATE_VARIABLES;
					}
                    for (int n=0; n<numVariables; n++) {
                        if ( ((Element)(conditionalVarList.get(n))).getAttribute("operator") == null) {
                            log.warn("unexpected null in operator "+((Element)(conditionalVarList.get(n)))+
								" "+((Element)(conditionalVarList.get(n))).getAttributes());
                            break;
                        }
						svOperator[n] = Integer.parseInt(((Element)(conditionalVarList.get(n)))
														.getAttribute("operator").getValue());
						svType[n] = Integer.parseInt(((Element)(conditionalVarList.get(n)))
														.getAttribute("type").getValue());
                        svName[n] = ((Element)(conditionalVarList.get(n)))
														.getAttribute("systemName").getValue();
                        svDataString[n] = ((Element)(conditionalVarList.get(n)))
														.getAttribute("dataString").getValue();
						svNum1[n] = Integer.parseInt(((Element)(conditionalVarList.get(n)))
														.getAttribute("num1").getValue());
						svNum2[n] = Integer.parseInt(((Element)(conditionalVarList.get(n)))
														.getAttribute("num2").getValue());
						svTriggersCalc[n] = true;
						if (((Element)(conditionalVarList.get(n))).
													getAttribute("triggersCalc") != null) {
							if ("no".equals(((Element)(conditionalVarList.get(n)))
													.getAttribute("triggersCalc").getValue()))
								svTriggersCalc[n] = false;
						}
					}
					// add state variables to conditional
					c.setStateVariables(svOperator,svType,svName,svDataString,svNum1,
															svNum2,svTriggersCalc,numVariables);
				}
				// load actions - there better be some
                List conditionalActionList = ((Element)(conditionalList.get(i))).
												getChildren("conditionalAction");
				if (conditionalActionList.size()>0) {
					// load actions
					int[] opt = {0,0};
					int[] delay = {0,0};
					int[] type = {Conditional.ACTION_NONE,Conditional.ACTION_NONE};
					String[] aName = {" "," "};
					int[] data = {0,0};
					String[] dataString = {" "," "};						
					int num = conditionalActionList.size();
					// check if number is within limit of 2					
					if (num>2) {
						log.error("more than 2 actions found for conditional "+sysName);
						num = 2;
					}
					for (int n = 0;n<num;n++) {
                        if ( ((Element)(conditionalActionList.get(n))).getAttribute("option") == null) {
                            log.warn("unexpected null in option "+((Element)(conditionalActionList.get(n)))+
								" "+((Element)(conditionalActionList.get(n))).getAttributes());
                            break;
                        }
						opt[n] = Integer.parseInt(((Element)(conditionalActionList.get(n)))
														.getAttribute("option").getValue());
						delay[n] = Integer.parseInt(((Element)(conditionalActionList.get(n)))
														.getAttribute("delay").getValue());
						type[n] = Integer.parseInt(((Element)(conditionalActionList.get(n)))
														.getAttribute("type").getValue());
						aName[n] = ((Element)(conditionalActionList.get(n)))
														.getAttribute("systemName").getValue();
						data[n] = Integer.parseInt(((Element)(conditionalActionList.get(n)))
														.getAttribute("data").getValue());
						dataString[n] = ((Element)(conditionalActionList.get(n)))
														.getAttribute("string").getValue();
					}
					// set actions in conditional
					c.setAction(opt,delay,type,aName,data,dataString);
				}
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
