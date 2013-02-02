package jmri.managers.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.TurnoutManager;
import jmri.Turnout;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;
import jmri.configurexml.TurnoutOperationManagerXml;
import jmri.NamedBeanHandle;

import java.util.List;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Provides the abstract base and store functionality for
 * configuring TurnoutManagers, working with
 * AbstractTurnoutManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element turnouts)
 * class, relying on implementation here to load the individual turnouts.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Turnout or AbstractTurnout subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public abstract class AbstractTurnoutManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractTurnoutManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutManager and associated TurnoutOperation's
     * @param o Object to store, of type TurnoutManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element turnouts = new Element("turnouts");
        setStoreElementClass(turnouts);
        TurnoutManager tm = (TurnoutManager) o;
        if (tm!=null) {
        	TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
        	Element opElem = tomx.store(TurnoutOperationManager.getInstance());
        	turnouts.addContent(opElem);
            java.util.Iterator<String> iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not turnouts to include
            if (!iter.hasNext()) return null;

            String defaultclosed = tm.getDefaultClosedSpeed();
            String defaultthrown = tm.getDefaultThrownSpeed();
            turnouts.addContent(new Element("defaultclosedspeed").addContent(defaultclosed));
            turnouts.addContent(new Element("defaultthrownspeed").addContent(defaultthrown));

            // store the turnouts
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Turnout t = tm.getBySystemName(sname);
                Element elem = new Element("turnout")
                            .setAttribute("systemName", sname); // deprecated for 2.9.* series
                elem.addContent(new Element("systemName").addContent(sname));
                log.debug("store turnout "+sname);

                storeCommon(t, elem);
                                
                // include feedback info
                elem.setAttribute("feedback", t.getFeedbackModeName());
                NamedBeanHandle<Sensor> s;
                s = t.getFirstNamedSensor();
                if (s!=null) elem.setAttribute("sensor1", s.getName());
                s = t.getSecondNamedSensor();
                if (s!=null) elem.setAttribute("sensor2", s.getName());
                
                // include turnout inverted
                elem.setAttribute("inverted", t.getInverted()?"true":"false");
                
                if (t.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT)){
                    // include turnout locked
                    elem.setAttribute("locked", t.getLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT)?"true":"false");          
                 	// include turnout lock mode
                	String lockOpr;
                	if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                		lockOpr = "both"; 
                	} else if (t.canLock(Turnout.CABLOCKOUT)){
                		lockOpr = "cab";
                	} else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                		lockOpr = "pushbutton";
                	} else {
                		lockOpr = "none";
                	}
                	elem.setAttribute("lockMode", lockOpr);          
                	// include turnout decoder
                	elem.setAttribute("decoder", t.getDecoderName());
                }
                
				// include number of control bits, if different from one
				int iNum = t.getNumberOutputBits();
				if (iNum!=1) elem.setAttribute("numBits",""+iNum);
				
				// include turnout control type, if different from 0
				int iType = t.getControlType();
				if (iType!=0) elem.setAttribute("controlType",""+iType);

                // add operation stuff
                String opstr = null;
                TurnoutOperation op = t.getTurnoutOperation();
                if (t.getInhibitOperation()) {
                	opstr = "Off";
                } else if (op==null) {
                	opstr = "Default";
                } else if (op.isNonce()) {	// nonce operation appears as subelement
        			TurnoutOperationXml adapter = TurnoutOperationXml.getAdapter(op);
        			if (adapter != null) {
        				Element nonceOpElem = adapter.store(op);
        				if (opElem != null) {
        					elem.addContent(nonceOpElem);
        				}
        			}
                } else {
                	opstr = op.getName();
                }
                if (opstr != null) {
                	elem.setAttribute("automate", opstr);
                }
                if((t.getDivergingSpeed()!=null) && (!t.getDivergingSpeed().equals("")) && !t.getDivergingSpeed().contains("Global")){
                    elem.addContent(new Element("divergingSpeed").addContent(t.getDivergingSpeed()));
                }
                if((t.getStraightSpeed()!=null) && (!t.getStraightSpeed().equals("")) && !t.getStraightSpeed().contains("Global")){
                    elem.addContent(new Element("straightSpeed").addContent(t.getStraightSpeed()));
                }
                
                // add element
                turnouts.addContent(elem);

            }
        }
        return turnouts;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param turnouts The top-level element being created
     */
    abstract public void setStoreElementClass(Element turnouts);

    /**
     * Create a TurnoutManager object of the correct class, then
     * register and fill it.
     * @param turnouts Top level Element to unpack.
     */
    abstract public boolean load(Element turnouts);

    /**
     * Utility method to load the individual Turnout objects.
     * If there's no additional info needed for a specific turnout type,
     * invoke this with the parent of the set of Turnout elements.
     * @param turnouts Element containing the Turnout elements to load.
     * @return true if succeeded
     */
    @SuppressWarnings("unchecked")
	public boolean loadTurnouts(Element turnouts) {
    	boolean result = true;
    	List<Element> operationList = turnouts.getChildren("operations");
    	if (operationList.size()>1) {
    		log.warn("unexpected extra elements found in turnout operations list");
    		result = false;
    	}
    	if (operationList.size()>0) {
    		TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
    		tomx.load(operationList.get(0));
    	}
    	List<Element> turnoutList = turnouts.getChildren("turnout");
    	if (log.isDebugEnabled()) log.debug("Found "+turnoutList.size()+" turnouts");
    	TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        
        try {
            if (turnouts.getChild("defaultclosedspeed")!=null){
                String closedSpeed = turnouts.getChild("defaultclosedspeed").getText();
                if (closedSpeed!=null && !closedSpeed.equals("")){
                    tm.setDefaultClosedSpeed(closedSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }
        
        try {
            if (turnouts.getChild("defaultthrownspeed")!=null){
                String thrownSpeed = turnouts.getChild("defaultthrownspeed").getText();
                if (thrownSpeed!=null && !thrownSpeed.equals("")){
                    tm.setDefaultThrownSpeed(thrownSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }

        for (int i=0; i<turnoutList.size(); i++) {
            Element elem = turnoutList.get(i);
            String sysName = getSystemName(elem);
            if ( sysName == null ) {
                log.error("unexpected null in systemName "+elem);
                result = false;
                break;
            }
            String userName = getUserName(elem);
            if (log.isDebugEnabled()) log.debug("create turnout: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            Turnout t = tm.getBySystemName(sysName);
            if (t==null){
                    t = tm.newTurnout(sysName, userName);
                    //Nothing is logged in the console window as the newTurnoutFunction already does this.
                    //log.error("Could not create turnout: '"+sysName+"' user name: '"+(userName==null?"":userName)+"'");
                    if (t==null){
                        result = false;
                        continue;
                    }
                    //result = false;
                    //continue;
            } else if (userName!=null)
                t.setUserName(userName);
            
            // Load common parts
            loadCommon(t, elem);
            
            // now add feedback if needed
            Attribute a;
            a = elem.getAttribute("feedback");
            if (a!=null) {
            	try{
            		t.setFeedbackMode(a.getValue());
            	}catch (IllegalArgumentException e){
            		log.error("Can not set feedback mode: '"+a.getValue()+"' for turnout: '"+sysName+"' user name: '"+(userName==null?"":userName)+"'");
            		result = false;
            	}
            }
            a = elem.getAttribute("sensor1");
            if (a!=null) { 
                try {
                    t.provideFirstFeedbackSensor(a.getValue());
                } catch (jmri.JmriException e){
                    result = false;
                }
            }
            a = elem.getAttribute("sensor2");
            if (a!=null) {
                try {
                    t.provideSecondFeedbackSensor(a.getValue());
                } catch (jmri.JmriException e){
                    result = false;
                }
            }
            
            // check for turnout inverted
            t.setInverted(getAttributeBool(elem, "inverted", false));
             
            // check for turnout decoder
            a = turnoutList.get(i).getAttribute("decoder");
            if (a!=null) { 
            	t.setDecoderName(a.getValue());
            }
            
            // check for turnout lock mode
			a = turnoutList.get(i).getAttribute("lockMode");
			if (a != null) {
				if (a.getValue().equals("both"))
					t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
				if (a.getValue().equals("cab")) {
					t.enableLockOperation(Turnout.CABLOCKOUT, true);
					t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
				}
				if (a.getValue().equals("pushbutton")) {
					t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
					t.enableLockOperation(Turnout.CABLOCKOUT, false);
				}
			}
            
            // check for turnout locked
            a = turnoutList.get(i).getAttribute("locked");
            if (a!=null) { 
            	t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, a.getValue().equals("true"));
            }

 			
			// number of bits, if present - if not, defaults to 1
			a = turnoutList.get(i).getAttribute("numBits");
			if (a==null) {
				t.setNumberOutputBits(1);
			}
			else {
				int iNum = Integer.parseInt(a.getValue());
				if ( (iNum==1) || (iNum==2) ) {
					t.setNumberOutputBits(iNum);
				}
				else {
					log.warn("illegal number of output bits for control of turnout "+sysName);
					t.setNumberOutputBits(1);
					result = false;
				}
			}
			
			// control type, if present - if not, defaults to 0
			a = turnoutList.get(i).getAttribute("controlType");
			if (a==null) {
				t.setControlType(0);
			}
			else {
				int iType = Integer.parseInt(a.getValue());
				if (iType>=0) {
					t.setControlType(iType);
				}
				else {
					log.warn("illegal control type for control of turnout "+sysName);
					t.setControlType(0);
					result = false;
				}
			}
			
            // operation stuff
            List<Element> myOpList = turnoutList.get(i).getChildren("operation");
            if (myOpList.size()>0) {
            	if (myOpList.size()>1) {
            		log.warn("unexpected extra elements found in turnout-specific operations");
            		result = false;
            	}
            	TurnoutOperation toper = TurnoutOperationXml.loadOperation(myOpList.get(0));
        		t.setTurnoutOperation(toper);
            } else {
            	a = turnoutList.get(i).getAttribute("automate");
            	if (a!=null) {
            		String str = a.getValue();
            		if (str.equals("Off")) {
            			t.setInhibitOperation(true);
            		} else if (!str.equals("Default")) {
            			TurnoutOperation toper =
            				TurnoutOperationManager.getInstance().getOperation(str);
            			t.setTurnoutOperation(toper);
            		}
            	}
            }
			
			//  set initial state from sensor feedback if appropriate
			t.setInitialKnownStateFromFeedback();
            try {
                t.setDivergingSpeed("Global");
                if (elem.getChild("divergingSpeed")!=null){
                    String speed = elem.getChild("divergingSpeed").getText();
                    if (speed!=null && !speed.equals("") && !speed.contains("Global")){
                        t.setDivergingSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
            }

            try {
                t.setStraightSpeed("Global");
                if (elem.getChild("straightSpeed")!=null){
                    String speed = elem.getChild("straightSpeed").getText();
                    if (speed!=null && !speed.equals("") && !speed.contains("Global")){
                        t.setStraightSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
            }
        }
       return result;
    }
    
    public int loadOrder(){
        return InstanceManager.turnoutManagerInstance().getXMLOrder();
    }

    static Logger log = Logger.getLogger(AbstractTurnoutManagerConfigXML.class.getName());
}
