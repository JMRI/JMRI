package jmri.managers.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalGroup;
import jmri.SignalGroupManager;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for a DefaultSignalGroupManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class DefaultSignalGroupManagerXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalGroupManagerXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalGroupManager
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SignalGroupManager m = (SignalGroupManager)o;

        Element element = new Element("signalgroups");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        List<String> names = m.getSystemNameList();
        for (int i = 0; i < names.size(); i++) {
            Element e = new Element("signalgroup");
            SignalGroup p = m.getSignalGroup(names.get(i));
            e.setAttribute("systemName", p.getSystemName()); // deprecated for 2.9.* series
            e.addContent(new Element("systemName").addContent(p.getSystemName()));
            e.setAttribute("userName", p.getUserName());
            //storeCommon(p, e);
            element.addContent(e);
            for (int x=0; x<p.getNumSignalMastAppearances(); x++){
                Element app = new Element("appearance").setAttribute("valid", p.getSignalMastAppearanceByIndex(x));
                e.addContent(app);
            }
            e.setAttribute("signalMast", p.getSignalMastName());
            
            for (int x=0; x<p.getNumSignalHeadItems(); x++){
            
                storeSignalHead(e, p, x);
                
            }
        }
        return element;
    }
    
    private void storeSignalHead(Element element, SignalGroup _group, int x){
        Element group = new Element("signalHead");
        String name = _group.getSignalHeadItemNameByIndex(x);
        group.setAttribute("name", name);
        group.setAttribute("onAppearance", getSignalColour(_group.getSignalHeadOnStateByIndex(x)));
        group.setAttribute("offAppearance", getSignalColour(_group.getSignalHeadOffStateByIndex(x)));
        if(_group.getSensorTurnoutOperByIndex(x))
            group.setAttribute("sensorTurnoutLogic", "AND");
        else
            group.setAttribute("sensorTurnoutLogic", "OR");

        for (int i = 0; i<_group.getNumSignalHeadTurnoutsByIndex(x); i++){
            storeTurnout(group, _group, x, i);
        }
        for (int i = 0; i<_group.getNumSignalHeadSensorsByIndex(x); i++){
            storeSensor(group, _group, x, i);
        }
        
        element.addContent(group);
    }
    
    private void storeTurnout(Element element, SignalGroup _group, int x, int turn){
        Element turnout = new Element("turnout").setAttribute("name", _group.getTurnoutNameByIndex(x, turn));
        turnout.setAttribute("state", ""+_group.getTurnoutStateByIndex(x, turn));
        element.addContent(turnout);
    }
    
    private void storeSensor(Element element, SignalGroup _group, int x, int sensor){
        Element Sensor = new Element("sensor").setAttribute("name", _group.getSensorNameByIndex(x, sensor));
        Sensor.setAttribute("state", ""+_group.getSensorStateByIndex(x, sensor));
        element.addContent(Sensor);
    }

    public void setStoreElementClass(Element signalGroup) {
        signalGroup.setAttribute("class",this.getClass().getName());
    }
    
    private String getSignalColour(int mAppearance){
        switch(mAppearance){
            case SignalHead.RED:
                    return "RED";
        	case SignalHead.FLASHRED:
                    return "FLASHRED";
        	case SignalHead.YELLOW:
                    return "YELLOW";
        	case SignalHead.FLASHYELLOW:
                    return "FLASHYELLOW";
        	case SignalHead.GREEN:
                    return "GREEN";
        	case SignalHead.FLASHGREEN:
                    return "FLASHGREEN";
            case SignalHead.LUNAR:
                    return "LUNAR";
            case SignalHead.FLASHLUNAR:
                    return "FLASHLUNAR";
        	case SignalHead.DARK:
                    return "DARK";
        	default:
                    log.warn("Unexpected appearance: "+mAppearance);
                // go dark
                    return "DARK";
        }
    }
    /**
     * Create a DefaultSignalGroupManager
     * @param element Top level Element to unpack.
     * @return true if successful
     */
     @SuppressWarnings("unchecked")
    public boolean load(Element element) {
        // loop over contained signalgroup elements
        List<Element> list = element.getChildren("signalgroup");

        SignalGroupManager sgm = InstanceManager.signalGroupManagerInstance();

        for (int i = 0; i < list.size(); i++) {
            SignalGroup m;
            Element e = list.get(i);
            String primary;
            String yesno;
            boolean inverse =false;
            int state =0x00;

            String sys = getSystemName(e);
            
            m = sgm.newSignalGroup(sys);
            
            if (getUserName(e) != null)
                m.setUserName(getUserName(e));
                
            primary = e.getAttribute("signalMast").getValue();
            m.setSignalMast(primary);
            
            List<Element> appList = e.getChildren("appearance");
            for(int y = 0; y<appList.size(); y++){
                String value = appList.get(y).getAttribute("valid").getValue();
                m.addSignalMastAppearance(value);
            }
            //loadCommon(m, e);
            List<Element> signalHeadList = list.get(i).getChildren("signalHead");
            if (signalHeadList.size() > 0) {
                for (int y = 0; y<signalHeadList.size(); y++){
                    String head = signalHeadList.get(y).getAttribute("name").getValue();
                    SignalHead sigHead = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(head);
                    m.addSignalHead(sigHead);
                    yesno = signalHeadList.get(y).getAttribute("sensorTurnoutLogic").getValue();
                    inverse = false;
                    if ( (yesno!=null) && (!yesno.equals("")) ) {
                        if (yesno.equals("AND")) inverse=true;
                        else if (yesno.equals("OR")) inverse=false;
                    }
                    m.setSensorTurnoutOper(sigHead, inverse);
                    
                    try {
                        m.setSignalHeadOnState(sigHead, getIntFromColour(signalHeadList.get(y).getAttribute("onAppearance").getValue()));
                    }
                    catch ( NullPointerException ex) {  // considered normal if the attributes are not present
                    }
                    try {
                        m.setSignalHeadOffState(sigHead,getIntFromColour(signalHeadList.get(y).getAttribute("offAppearance").getValue()));
                    }
                    catch ( NullPointerException ex) {  // considered normal if the attributes are not present
                    }
                    List<Element> signalTurnoutList = signalHeadList.get(y).getChildren("turnout");
                    if (signalTurnoutList.size() > 0){
                        for(int k = 0; k<signalTurnoutList.size(); k++){
                            String tName = signalTurnoutList.get(k).getAttribute("name").getValue();
                            jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(tName);
                            state = 0;
                            try {
                                state = signalTurnoutList.get(k).getAttribute("state").getIntValue();
                            } catch (org.jdom.DataConversionException ex) {
                                log.warn("invalid state attribute value");
                            }
                            m.setSignalHeadAlignTurnout(sigHead, turnout, state);
                        }
                    }
                     List<Element> signalSensorList = signalHeadList.get(y).getChildren("sensor");
                    if (signalSensorList.size() > 0){
                        for(int k = 0; k<signalSensorList.size(); k++){
                            String sName = signalSensorList.get(k).getAttribute("name").getValue();
                            jmri.Sensor sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(sName);
                            state = 0;
                            try {
                                state = signalSensorList.get(k).getAttribute("state").getIntValue();
                            } catch (org.jdom.DataConversionException ex) {
                                log.warn("invalid style attribute value");
                            }
                            m.setSignalHeadAlignSensor(sigHead, sensor, state);
                        }
                    }
                }
            
            
            }

        }
        
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    private int getIntFromColour(String colour){
        if (colour.equals("RED")) return SignalHead.RED;
        else if (colour.equals("YELLOW")) return SignalHead.YELLOW;
        else if (colour.equals("GREEN")) return SignalHead.GREEN;
        else if (colour.equals("LUNAR")) return SignalHead.LUNAR;
        else if (colour.equals("DARK")) return SignalHead.DARK;
        else if (colour.equals("FLASHRED")) return SignalHead.FLASHRED;
        else if (colour.equals("FLASHYELLOW")) return SignalHead.FLASHYELLOW;
        else if (colour.equals("FLASHGREEN")) return SignalHead.FLASHGREEN;
        else if (colour.equals("FLASHLUNAR")) return SignalHead.FLASHLUNAR;
        else log.warn("Unexpected appearance: "+colour);
        return SignalHead.DARK;
    }
    
    public int loadOrder(){
        return InstanceManager.signalGroupManagerInstance().getXMLOrder();
    }

    static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManagerXml.class.getName());
}
