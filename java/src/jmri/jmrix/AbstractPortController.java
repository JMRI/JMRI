// AbstractPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Hashtable;


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
    
    protected String option1Name = "1";
    protected String option2Name = "2";
    protected String option3Name = "3";
    protected String option4Name = "4";

    abstract public String getCurrentPortName();
    
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
    
    public Hashtable<String, Option> getOptionList(){
        return options;
    }
    
    public void setOptionState(String option, String value){
        if(options.containsKey(option)){
            options.get(option).configure(value);
        }
    }
    
    public String getOptionState(String option){
        if(options.containsKey(option)){
            return options.get(option).getCurrent();
        }
        return null;
    }
    
    public String[] getOptionChoices(String option){
        if(options.containsKey(option)){
            return options.get(option).getOptions();
        }
        return null;
    }
    
    protected Hashtable<String, Option> options = new Hashtable<String, Option>();
    
    static public class Option{
        
        String currentValue = null;
        String name;
        String displayText;
        String[] options;
        Boolean advancedOption = true;
        
        public Option(String name, String displayText, String[] options, boolean advanced){
            this(name, displayText, options);
            this.advancedOption = advanced;
        }
        
        public Option(String name, String displayText, String[] options){
            this.name = name;
            this.displayText = displayText;
            this.options = options;
        }
        
        public void configure(String value){
            currentValue = value;
        }
        
        public String getCurrent(){
            if(currentValue==null) return options[0];
            return currentValue;
        }
        
        public String[] getOptions(){
            return options;
        }
        
        public String getName(){
            return name;
        }
        
        public String getDisplayText(){
            return name;
        }
        
        public boolean isAdvanced() {
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
