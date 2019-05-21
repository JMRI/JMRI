package jmri.jmrix.tams;

/**
 * Encodes a message to a Tams MasterControl command station.
 * <p>
 * The {@link TamsReply} class handles the response from the command station.
 * <p>
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @author	Jan Boen
 */
public class TamsMessage extends jmri.jmrix.AbstractMRMessage {

    static private final int TamsProgrammingTimeout = 5000;//ms

    //The oneByteReply is used to tell TamsReply if one or more bytes are expected
    //The lastByteReply is gives the value of the last byte to be expected, for sensors this is always 0x00
    //Has been set to sensor value as we expect this to the most common binary type of reply
    //Is used in conjunction with isBinary() = true
    //When receiving an ASCII reply we scan for ] which indicates end of reply. Anything after this can be ignored
    
    // accessor to get one element of the TamsMessage
    @Override
    public int getElement(int n) {
        return _dataChars[n];
    }

    //Extend the class with extra Tams Specific variables
	private char _replyType = 'X';//C(ommand Station), S(ensor), T(urnout), P(ower), L(oco), X(Undefined), M(anual) via PacketGen

    public char getReplyType() {
        return _replyType;
    }

    public void setReplyType(char rt) {
        _replyType = rt;
    }
    
    private boolean _replyOneByte = true;//Will it be a single byte reply?

    public boolean getReplyOneByte() {
        return _replyOneByte;
    }

    public void setReplyOneByte(boolean rob) {
        _replyOneByte = rob;
    }
    
	private int _replyLastByte = TamsConstants.EOM00;//What will be the last byte of a multi byte reply?

    public int getReplyLastByte() {
        return _replyLastByte;
    }

    public void setReplyLastByte(int rlb) {
    	_replyLastByte = rlb;
    }
    
    public TamsMessage() {
        super();
    }

    // create a new one
    public TamsMessage(int i) {
        super(i);
    }

    // copy one
    public TamsMessage(TamsMessage m) {
        super(m);
    }

    // from String
    public TamsMessage(String m) {
        super(m);
    	setBinary(false);
    }

    // from binary
    public TamsMessage(int[] m) {//an array of int will be interpreted as binary
        this((m.length));
        //int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet
        // add each byte of the input message
        for (j = 0; j < m.length; j++) {
            this.setElement(j, m[j]);//changed i to j
            //i++;
        }
        setBinary(true);//Is a binary reply
        setReplyOneByte(false);//By default we set false and then check if we must change
        if ((this.getElement(1) == (TamsConstants.XSTATUS & TamsConstants.MASKFF)) || (this.getElement(1) == (TamsConstants.XEVENT & TamsConstants.MASKFF))){
            setReplyOneByte(true);
        }
        setReplyLastByte(TamsConstants.EOM00);//By default we set 0x00 and then check if we must change
        if (this.getElement(1) == (TamsConstants.XEVTLOK & TamsConstants.MASKFF)){
            setReplyLastByte(TamsConstants.EOM80);
        }
        //log.info(jmri.util.StringUtil.appendTwoHexFromInt(this.getElement(1),""));
        setRetries(5);
    	//log.info("Binary reply will be: one byte= " + getReplyOneByte() + ", last byte= " + getReplyLastByte());
    }

    static public final int POLLTIMEOUT = 100;

    // static methods to return a formatted message
    //Binary messages

    //Set power OFF via XPwrOff (0xA6)
    static public TamsMessage setXPwrOff() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XPWROFF & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(true);
        m.setReplyType('P');
        //log.info("Preformatted Tams message = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        return m;
    }
    
    //Set power ON via XPwrOn (0xA7)
    static public TamsMessage setXPwrOn() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XPWRON & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(true);
        m.setReplyType('P');
        //log.info("Preformatted Tams message = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        return m;
    }
    
    //Get power status via XStatus (0xA2)
    static public TamsMessage getXStatus() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XSTATUS & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(true);
        m.setReplyType('P');
        //log.info("Preformatted Tams getXStatus = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        //log.info("isBinary= " + m.isBinary() + ", one byte reply " + m.getReplyOneByte() +  ", reply type " + m.getReplyType());
        return m;
    }
    
    //Get sensor status via XEvtSen (0xCB)
    //Only reports changes since last poll
    static public TamsMessage getXEvtSen() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XEVTSEN & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyLastByte(TamsConstants.EOM00);//No more sensor data is following
        m.setReplyType('S');
        //log.info("Preformatted Tams message = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        return m;
    }
    
    //Get loco changes via XEvtLok (0xC9)
    //Only reports changes which have not been initiated from PC
    static public TamsMessage getXEvtLok() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XEVTLOK & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyLastByte(TamsConstants.EOM80);//No more loco data is following
        m.setReplyType('L');
        //log.info("Preformatted Tams message = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        return m;
    }
    
    //Get turnout changes via XEvtTrn (0xCA)
    //Only reports changes which have not been initiated from PC
    static public TamsMessage getXEvtTrn() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & 0xFF);
        m.setElement(1, TamsConstants.XEVTTRN & 0xFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyType('T');
        //log.info("Preformatted Tams message = " + Integer.toHexString(m.getElement(0)) + " " + Integer.toHexString(m.getElement(1)));
        return m;
    }
    
    //Set Tams MC to report only sensors which have been changed on polling
    static public TamsMessage setXSR() {
        TamsMessage m = new TamsMessage("xSR 1");
        m.setBinary(false);
        m.setReplyOneByte(false);
        m.setReplyType('S');
        return m;
    }
    
    //Set Tams MC so that a sensor module with at least 1 bit set is reporting its status
    static public TamsMessage setXSensOff() {
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.LEADINGX & 0xFF);
        m.setElement(1, TamsConstants.XSENSOFF & 0xFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyType('S');
        return m;
    }
    
    //Command Station messages
    static public TamsMessage getReadPagedCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRP " + cv);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWP " + cv + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getReadRegister(int reg) { //Vx
        TamsMessage m = new TamsMessage("xPTRR " + reg);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWriteRegister(int reg, int val) { //Sx xxx
        TamsMessage m = new TamsMessage("xPTWR " + reg + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getReadDirectByteCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRD " + cv);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWriteDirectByteCV(int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWD " + cv + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getReadDirectBitCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRB " + cv);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWriteDirectBitCV(int cv, int bit, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWB " + cv + ", " + bit + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWriteOpsModeCVMsg(int adr, int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPD " + adr + ", " + cv + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }

    static public TamsMessage getWriteOpsModeAccCVMsg(int adr, int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPA " + adr + ", " + cv + ", " + val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        m.setBinary(false);
        m.setReplyType('C');
        return m;
    }
}
