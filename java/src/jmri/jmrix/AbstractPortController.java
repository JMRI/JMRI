// AbstractPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance.
 * SerialPortAdapter is an Interface, and its implementing
 * classes also inherit from various PortController types.  But we
 * want some common behaviors for those, so we put them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
abstract public class AbstractPortController implements PortAdapter {

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public boolean status() {return opened;}
    
    protected boolean opened = false;
    
    protected void setOpened() {opened = true; }
    protected void setClosed() {opened = false; }
    
    //These are to support the old legacy files.
    protected String option1Name = "1";
    protected String option2Name = "2";
    protected String option3Name = "3";
    protected String option4Name = "4";

    abstract public String getCurrentPortName();
    /*
        The next set of configureOptions are to support the old configuration files.
    */
    public void configureOption1(String value) { 
        if(options.containsKey(option1Name)){
            options.get(option1Name).configure(value);
        }
    }

    public void configureOption2(String value) { 
        if(options.containsKey(option2Name)){
            options.get(option2Name).configure(value);
        }
    }
    
    public void configureOption3(String value) { 
        if(options.containsKey(option3Name)){
            options.get(option3Name).configure(value);
        }
    }

    public void configureOption4(String value) { 
        if(options.containsKey(option4Name)){
            options.get(option4Name).configure(value);
        }
    }

        /*
        The next set of getOption Names are to support legacy configuration files
    */
    public String getOption1Name(){
        return option1Name;
    }
    
    public String getOption2Name(){
        return option2Name;
    }
    
    public String getOption3Name(){
        return option3Name;
    }

    public String getOption4Name(){
        return option4Name;
    }

    /**
    * Get a list of all the options configured against this adapter.
    */
    public String[] getOptions(){
        String[] arr = new String[options.size()];
        Enumeration<String> en = options.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    
    }

    /**
    * Set the value of an option
    */
    public void setOptionState(String option, String value){
        if(options.containsKey(option)){
            options.get(option).configure(value);
        }
    }
    
    /**
    *  Get the value of a specific option
    */
    public String getOptionState(String option){
        if(options.containsKey(option)){
            return options.get(option).getCurrent();
        }
        return null;
    }
    
    /**
    *   return a list of the various choices allowed with an option.
    */
    public String[] getOptionChoices(String option){
        if(options.containsKey(option)){
            return options.get(option).getOptions();
        }
        return null;
    }
    
    public String getOptionDisplayName(String option){
        if(options.containsKey(option)){
            return options.get(option).getDisplayText();
        }
        return null;
    }
    
    public boolean isOptionAdvanced(String option){
        if(options.containsKey(option)){
            return options.get(option).isAdvanced();
        }
        return false;
    }
    
    protected Hashtable<String, Option> options = new Hashtable<String, Option>();
    
    static protected class Option {
        
        String currentValue = null;
        String displayText;
        String[] options;
        Boolean advancedOption = true;
        
        public Option(String displayText, String[] options, boolean advanced){
            this(displayText, options);
            this.advancedOption = advanced;
        }
        
        public Option(String displayText, String[] options){
            this.displayText = displayText;
            this.options = new String[options.length];
            System.arraycopy( options, 0, this.options, 0, options.length );
        }
        
        void configure(String value){
            currentValue = value;
        }
        
        String getCurrent(){
            if(currentValue==null) return options[0];
            return currentValue;
        }
        
        String[] getOptions(){
            return options;
        }
        
        String getDisplayText(){
            return displayText;
        }
        
        boolean isAdvanced() {
            return advancedOption;
        }
    }
    
    /**
    * Get and set of the Manufacturer for network (TCP/IP) based
    * connections is handled by the ConnectionConfig code in each
    * connector.  this is here as we implement the serialdriveradpter.
    */
    public String getManufacturer() { return mManufacturer; }
    public void setManufacturer(String manufacturer) { 
        log.debug("update manufacturer from "+mManufacturer+" to "+manufacturer);
        mManufacturer = manufacturer; 
    }
    protected String mManufacturer = null;
    
    public boolean getDisabled() { return mDisabled; }
   
    /* The set disabled is handled within the local port controller for each system
    this is because it needs to also needs to set a disabled flag in the system connection memo*/
    
    abstract public void setDisabled(boolean disabled);
    protected boolean mDisabled = false;
    
    abstract public SystemConnectionMemo getSystemConnectionMemo();

    protected boolean allowConnectionRecovery = false;

    abstract public void recover();

    protected int reconnectinterval = 1000;
    protected int retryAttempts = 10;

    protected static void safeSleep(long milliseconds, String s) {
          try {
             Thread.sleep(milliseconds);
          }
          catch (InterruptedException e) {
             log.error("Sleep Exception raised during reconnection attempt" +s);
          }
    }

    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPortController.class.getName());

}
