//LayoutSensorChangeType
package jmri.jmrit.display;

import java.awt.Color;

import jmri.jmrit.catalog.NamedIcon;

public class SensorChangeType {

    public SensorChangeType() {}
    
    public SensorChangeType(SensorIcon l, LayoutEditor p){
    
        getOldValues(l);
        

        if(!icon) {
            nl = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        } else {
            nl = new SensorIcon(new String("   "));
        }
        p.putSensor(nl);
        putValuesIntoNew();
        
        
        //We shall remove the old ones once we have created the new
        l.remove();
        l.dispose();
    
    }
    
    public SensorChangeType(SensorIcon l, PanelEditor p){
        getOldValues(l);
        //LayoutEditor p = LayoutEditor();

        
        if(!icon) {
            nl = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        } else {
            nl = new SensorIcon(new String("   "));
        }
        
         p.putLabel(nl);
         putValuesIntoNew();
        //We shall remove the old ones once we have created the new
        l.remove();
        l.dispose();
    
    }
    
        SensorIcon nl;
        int x;
        int y;
        String s;
        int level;
        boolean momentary;
        boolean forceControl;
        boolean icon;
        boolean text;
        NamedIcon active;
        NamedIcon inconsistent;
        NamedIcon inactive;
        NamedIcon unknown;
        String strActive;
        String strInactive;
        String strInconsistent;
        String strUnknown;
        String strText;
        Color clrActiveText;
        Color clrActiveBack;
        Color clrInActiveText;
        Color clrInActiveBack;
        Color clrUnknownText;
        Color clrUnknownBack;
        Color clrInconsistentText;
        Color clrInconsistentBack;
        boolean opaque = false;

        
    void getOldValues(SensorIcon l){
        x = l.getX();
        y = l.getY();
        s = l.getSensor().getSystemName().toString();
        level = l.getDisplayLevel();
        opaque = l.isOpaque();
        momentary = l.getMomentary();
        forceControl = l.getForceControlOff();
        icon = l.isIcon();
        text = l.isText();
        if ((l.getOriginalText()==null) && (l.isIcon())&&(l.isText()))
            strText = l.getText();
        else
            strText = l.getOriginalText();
        active = l.getActiveIcon();
        inconsistent = l.getInconsistentIcon();
        inactive = l.getInactiveIcon();
        unknown = l.getUnknownIcon();
        strActive = l.getActiveText();
        strInactive = l.getInactiveText();
        strInconsistent = l.getInconsistentText();
        strUnknown = l.getUnknownText();
        clrActiveText = l.getTextActive();
        clrActiveBack = l.getBackgroundActive();
        clrInActiveText = l.getTextInActive();
        clrInActiveBack  = l.getBackgroundInActive();
        clrUnknownText = l.getTextUnknown();
        clrUnknownBack  = l.getBackgroundActive();
        clrInconsistentText = l.getTextInconsistent();
        clrInconsistentBack  = l.getBackgroundUnknown();
    }
    
    void putValuesIntoNew(){
        nl.setForceControlOff(forceControl);
        nl.setMomentary(momentary);
        nl.setSensor(s);
        nl.setLocation(x,y);
        nl.setDisplayLevel(level);
        nl.setActiveIcon(active);
        nl.setInconsistentIcon(inconsistent);
        nl.setInactiveIcon(inactive);
        nl.setUnknownIcon(unknown);
        nl.setOpaque(opaque);
        //nl.setText(strText);
        if (strText!=null){
            nl.setText(strText);
            nl.setOriginalText(strText);
        }
        if (strActive!=null)
            nl.setActiveText(strActive);
        if (strInactive!=null)
            nl.setInactiveText(strInactive);
        if (strInconsistent!=null)
            nl.setInconsistentText(strInconsistent);
        if (strUnknown!=null)
            nl.setUnknownText(strUnknown);
        if (clrActiveText!=null)
            nl.setTextActive(clrActiveText);
        if (clrActiveBack!=null)
            nl.setBackgroundActive(clrActiveBack);
        if (clrInActiveText!=null)
            nl.setTextInActive(clrInActiveText);
        if (clrInActiveBack!=null)
            nl.setBackgroundInActive(clrInActiveBack);
        if (clrUnknownText!=null)
            nl.setTextUnknown(clrUnknownText);
        if (clrUnknownBack!=null)
            nl.setBackgroundActive(clrUnknownBack);
        if (clrInconsistentText!=null)
            nl.setTextInconsistent(clrInconsistentText);
        if (clrInconsistentBack!=null)
            nl.setBackgroundUnknown(clrInconsistentBack);
    }


}