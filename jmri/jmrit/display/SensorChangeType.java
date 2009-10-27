//LayoutSensorChangeType
package jmri.jmrit.display;

import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
//import jmri.jmrit.display.LayoutEditor;

public class SensorChangeType {

    public SensorChangeType() {}
    
    public SensorChangeType(SensorIcon l, LayoutEditor p){
    
        //LayoutEditor p = LayoutEditor();
        SensorIcon nl;
        int x = l.getX();
        int y = l.getY();
        String s = l.getSensor().getSystemName().toString();
        int level = l.getDisplayLevel();
        boolean momentary = l.getMomentary();
        boolean forceControl = l.getForceControlOff();
        boolean icon = l.isIcon();
        

        if(!icon) {
            //Changeto Icon
            nl = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
            nl.setForceControlOff(forceControl);
            nl.setMomentary(momentary);
            nl.setSensor(s);
            nl.setLocation(x,y);
            nl.setDisplayLevel(level);
            p.putSensor(nl);
            
        } else {
            nl = new SensorIcon(new String("  "));
            // We must be changing to text
            nl.setForceControlOff(forceControl);
            nl.setMomentary(momentary);
            nl.setSensor(s);
            nl.setLocation(x,y);
            nl.setDisplayLevel(level);
            p.putSensor(nl);
        }
        
        //We shall remove the old ones once we have created the new
        l.remove();
        l.dispose();
    
    }
    
    public SensorChangeType(SensorIcon l, PanelEditor p){
    
        //LayoutEditor p = LayoutEditor();
        SensorIcon nl;
        int x = l.getX();
        int y = l.getY();
        String s = l.getSensor().getSystemName().toString();
        int level = l.getDisplayLevel();
        boolean momentary = l.getMomentary();
        boolean forceControl = l.getForceControlOff();
        boolean icon = l.isIcon();
        

        if(!icon) {
            //Changeto Icon
            nl = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
            nl.setForceControlOff(forceControl);
            nl.setMomentary(momentary);
            nl.setSensor(s);
            nl.setLocation(x,y);
            nl.setDisplayLevel(level);
            p.putLabel(nl);
            
        } else {
            nl = new SensorIcon(new String("  "));
            // We must be changing to text
            nl.setForceControlOff(forceControl);
            nl.setMomentary(momentary);
            nl.setSensor(s);
            nl.setLocation(x,y);
            nl.setDisplayLevel(level);
            p.putLabel(nl);
        }
        
        //We shall remove the old ones once we have created the new
        l.remove();
        l.dispose();
    
    }



}