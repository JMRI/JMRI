package jmri.jmrix.loconet.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.string.actions.AbstractStringAction;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends an OPC_PEER message on the LocoNet
 * 
 * There doesn't seem to be a LocoNet command for sending strings. The strings
 * are sent with the OPC_PEER LocoNet command, using "SV Programming Message
 * Formats Version 13", using "SV programming format 2".
 * 
 * This LocoNet command has four data bytes, D1, D2, D3, and D4. The strings
 * are sent three characters at a time, encoded as ISO-8859-1 aka Latin1 in
 * D1, D2 and D3. The lower 7 bits of byte D4 has the index in the string,
 * there these three bytes are located. The highest bit in D4 is 1 if this
 * is the last package, or 0 if there is more data to send.
 * 
 * If the length of the string is not a multiple of three, spaces are added so
 * that the length is a multiple of three. The longest string that can be sent
 * is 129 characters.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class StringActionLocoNetOpcPeer extends AbstractStringAction {
//        implements VetoableChangeListener {

//    private NamedBeanHandle<Memory> _memoryHandle;
    
    // When sending OPC_PEER messages on the LocoNet, an unique manufacturer ID
    // is needed, OR, a unique developer ID while using NMRA DIY DCC ManufacturerId of 13.
    // Please do not use developer ID of 17 if you develop your own LocoNet device.
    // Register your own developer ID at https://groups.io/g/LocoNet-Hackers under
    // "Files" in the file LocoNet Hackers DeveloperId List_v??.html
    private int _manufacturerID = 13;   // Default to NMRA DIY DCC ManufacturerId of 13
    private int _developerID = 17;      // Default to the developer ID of Daniel Bergqvist, 17.
    
    // FIX THIS LATER!!! IT'S HARDCODED NOW.
    private LocoNetSystemConnectionMemo lm;
    private LnTrafficController tc;
    
    private String _stringToSend;
    private int _index = -1;    // Index in string to send. -1 if not sending.
    private int _sourceAddress = 0x00;
    private int _destAddress = 200;         // FIX LATER !!!
    private int _sv_address = 100;          // FIX LATER !!!
    private byte[] _dataToSend;
    private int _numCharsToSend = 8;        // This MUST be a multiple of 3.
    
    public StringActionLocoNetOpcPeer(String sys, String user) {
        super(sys, user);
    }
    
    public void setManufacturerID(int id) {
        _manufacturerID = id;
    }
    
    public int getManufacturerID() {
        return _manufacturerID;
    }
    
    public void setDeveloperID(int id) {
        _developerID = id;
    }
    
    public int getDeveloperID() {
        return _developerID;
    }
    
    public void setSourceAddress(int address) {
        _sourceAddress = address;
    }
    
    public int getSourceAddress() {
        return _sourceAddress;
    }
    
    public void setDestAddress(int address) {
        _destAddress = address;
    }
    
    public int getDestAddress() {
        return _destAddress;
    }
    
    public void set_SV_Address(int address) {
        _sv_address = address;
    }
    
    public int get_SV_Address() {
        return _sv_address;
    }
    
/*    
    public void setMemory(String memoryName) {
        if (memoryName != null) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
            if (memory != null) {
                _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            } else {
                log.warn("memory '{}' does not exists", memoryName);
            }
        } else {
            _memoryHandle = null;
        }
    }
    
    public void setMemory(NamedBeanHandle<Memory> handle) {
        _memoryHandle = handle;
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
        } else {
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
*/    
    // This method is protected to be able to test it from the
    // StringActionLocoNet_OPC_PEERTest class
    protected int getTopBitsByte(long destAddr, long svAddr) {
        return (int)
                (
                    ((svAddr & 0x8000) >> (15-3))
                    + ((svAddr & 0x0080) >> (7-2))
                    + ((destAddr & 0x8000) >> (15-1))
                    + ((destAddr & 0x0080) >> (7-0))
                );
    }
    
    // This method is protected to be able to test it from the
    // StringActionLocoNet_OPC_PEERTest class
    protected int getTopBitsByte(int D4, int D3, int D2, int D1) {
        return  (
                    ((D4 & 0x80) >> (7-3))
                    + ((D3 & 0x80) >> (7-2))
                    + ((D2 & 0x80) >> (7-1))
                    + ((D1 & 0x80) >> (7-0))
                );
    }
    
    private void sendString() {
        if (_dataToSend != null && _index < _dataToSend.length) {
            
            int lengthByte = _index;
            
            // If this is the last part to send, set the highest bit in the
            // byte to 1 to mark that this is the last part of the string.
            if (_index+3 >= _dataToSend.length) lengthByte += 0x80;
            
            LocoNetMessage l = LnSv2MessageContents.createSv2Message(
                    _sourceAddress,
                    LnSv2MessageContents.SV_CMD_WRITE_FOUR,
                    _destAddress,
                    _sv_address,
                    _dataToSend[_index],
                    _dataToSend[_index+1],
                    _dataToSend[_index+2],
                    lengthByte
            );
            
            tc.sendLocoNetMessage(l);
            _index += 4;
            if (_index >= _numCharsToSend) _index = -1;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String value) throws UnsupportedEncodingException {
        // We cannot send the string if we don't have any LocoNet connection
        if (tc == null) return;
        
        // The length of the string to send must be a multiple of 3
        while ((value.length() % 3) != 0) value += ' ';
        
        _dataToSend = value.getBytes("ISO-8859-1");
        _index = 0;
        sendString();
//        if (_memoryHandle != null) {
//            _memoryHandle.getBean().setValue(value);
//        }
    }
/*
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
    }
*/    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringActionLocoNet_OPC_PEER_Short", "none");
/*        
        if (_memoryHandle != null) {
            return Bundle.getMessage(locale, "StringActionMemory1", _memoryHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "StringActionMemory1", "none");
        }
*/        
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return getShortDescription(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        List<LocoNetSystemConnectionMemo> list = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        
        // Return if we don't have any LocoNet connection
        if (list.isEmpty()) return;
        
        // FIX THIS LATER !!!
        lm = list.get(0);
        
        tc = lm.getLnTrafficController();
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
//    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNetOpcPeer.class);

}
