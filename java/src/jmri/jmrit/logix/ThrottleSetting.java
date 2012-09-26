package jmri.jmrit.logix;


public class ThrottleSetting {

    long    _time;
    String  _command;
    String  _value;
    String  _blockName;

    public ThrottleSetting() {
    }

    public ThrottleSetting(long time, String command, String value, String blockName) {
        _time = time;
        _command = command;
        _value = value;
        _blockName = blockName;
    }
    public ThrottleSetting(ThrottleSetting ts) {
        this(ts.getTime(), ts.getCommand(), ts.getValue(), ts.getBlockName());
    }

    /**
    * Time is an object so that a "synch to block entry" notation can be used 
    * rather than elapsed time.
    */
    public void setTime(long time) { _time = time; }
    public long getTime() { return _time; }

    public void setCommand(String command) { _command = command; }
    public String getCommand() { return _command; }

    public void setValue(String value) { _value = value; }
    public String getValue() { return _value; }

    public void setBlockName(String blockName) { _blockName = blockName; }
    public String getBlockName() { return _blockName; }

    public String toString() {
        return "ThrottleSetting: wait "+_time+"ms for "+_command+"= "+_value+" at block "+_blockName; 
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleSetting.class.getName());
}

