// OsIndicator.java

package jmri.jmrit.ussctc;

import jmri.*;
/**
 * Provide bean-like access to the collection of Logix, Routes, Memories,
 * etc that make up a OsIndicator.
 *<P>
 * An OS Indicator drives the lamp on the panel for a particular OS.
 * Honors a separate lock/unlocked indication by showing occupied if the
 * associated turnout has been unlocked.
 *
 * @author	Bob Jacobsen    Copyright (C) 2007
 * @version     $Revision: 1.1 $
 */
public class OsIndicator implements Constants {

    final static String namePrefix = commonNamePrefix+"OsIndicator"+commonNameSuffix;
    /**
     * Nobody can build anonymous object
     */
    private OsIndicator() {}
    
    
    /**
     * Create one from scratch
     * @param output Output turnout to be driven
     * @param osSensor Sensor checking for OS occupancy
     * @param lock Lock NamedBean (type to be decided)
     */
    public OsIndicator(String output, String osSensor, String lock) {
        this.lock = lock;
        this.osSensor = osSensor;
        this.output = output;
    }

    /**
     * Create the underlying objects that implement this
     */
    public void instantiate() {
        // find/create Logix
        String nameP = namePrefix+output;
        Logix l = InstanceManager.logixManagerInstance().
            getLogix(nameP);
        if (l==null) l = InstanceManager.logixManagerInstance().
            createNewLogix(nameP,"");
        l.deActivateLogix();
        // Find/create conditional and add
        Conditional c = InstanceManager.conditionalManagerInstance()
            .getConditional(l,nameP+"C1");
        if (c==null) {
            c = InstanceManager.conditionalManagerInstance()
                .createNewConditional(nameP+"C1", "");
            l.addConditional(nameP+"C1",-1);
        }
        
        // Load variable into the Conditional
        // Omit lock term if no lock specified
        if (!lock.equals("")) {
            int[] opern = new int[]{Conditional.OPERATOR_NONE, Conditional.OPERATOR_AND};
            int[] type = new int[]{Conditional.TYPE_SENSOR_INACTIVE,Conditional.TYPE_SENSOR_INACTIVE};
            String[] name = new String[]{osSensor,lock};
            String[] data = new String[]{"N/A","N/A"};
            int[] num1 = new int[]{0,0};
            int[] num2 = new int[]{0,0};
            c.setStateVariables(opern, type, name, data, num1, num2, opern.length);
        } else {
            int[] opern = new int[]{Conditional.OPERATOR_NONE};
            int[] type = new int[]{Conditional.TYPE_SENSOR_INACTIVE};
            String[] name = new String[]{osSensor};
            String[] data = new String[]{"N/A"};
            int[] num1 = new int[]{0};
            int[] num2 = new int[]{0};
            c.setStateVariables(opern, type, name, data, num1, num2, opern.length);
        }
                
        // and put it back in operation
        l.activateLogix();
        
    }

    /**
     * Create an object to represent an existing OsIndicator.
     * @param outputName name of output Turnout that drives the indicator
     * @throws JmriException if no such OsIndicator exists, or some problem found
     */
    public OsIndicator(String outputName) throws jmri.JmriException { 
        this.output = outputName;

        // findLogix
        String nameP = namePrefix+output;
        Logix l = InstanceManager.logixManagerInstance().
            getLogix(nameP);
        if (l==null) throw new jmri.JmriException("Logix does not exist");
            
        // Find/create conditional and add
        Conditional c = InstanceManager.conditionalManagerInstance()
            .getConditional(l,nameP+"C1");
        if (c==null) throw new jmri.JmriException("Conditional does not exist");
        
        // Load variables from the Conditional
        int length = c.getNumStateVariables();
        int[] opern = new int[length];
        int[] type = new int[length];
        String[] name = new String[length];
        String[] data = new String[length];
        int[] num1 = new int[length];
        int[] num2 = new int[length];
        c.getStateVariables(opern, type, name, data, num1, num2);
        
        // and load internals
        osSensor = name[0];
        lock = "";
        if (length>1) 
            lock = name[1];
        
    }

    public String getOutputName() {
        return output;
    }
    public String getOsSensorName() {
        return osSensor;
    }
    public String getLockName() {
        return lock;
    }
    
    String output;
    String osSensor;
    String lock;
    
}

/* @(#)OsIndicator.java */
