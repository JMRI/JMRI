package jmri.jmrix.loconet.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends an OPC_PEER message on the LocoNet
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class StringActionLocoNet_OPC_PEER extends AbstractStringAction {
//        implements VetoableChangeListener {

//    private NamedBeanHandle<Memory> _memoryHandle;
    
    // When sending OPC_PEER messages on the LocoNet, an unique manufacturer ID
    // is needed, OR, a unique developer ID while using NMRA DIY DCC ManufacturerId of 13.
    // Please do not use developer ID of 17 if you develop your own LocoNet device.
    // Register your own developer ID at https://groups.io/g/LocoNet-Hackers under
    // "Files" in the file LocoNet Hackers DeveloperId List_v??.html
    private int _manufacturerID = 13;   // Default to NMRA DIY DCC ManufacturerId of 13
    private int _developerID = 17;      // Default to the developer ID of Daniel Bergqvist, 17.
    
    private String _stringToSend;
    private int _index = -1;    // Index in string to send. -1 if not sending.
    private int _sourceAddress = 0x00;
    private long _destAddress;
    private long _start_SV_address;
    private int _numCharsToSend = 8;    // This MUST be a multiple of 4.
    
    public StringActionLocoNet_OPC_PEER(String sys, String user) {
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
    
    public void setDestAddress(long address) {
        _destAddress = address;
    }
    
    public long getDestAddress() {
        return _destAddress;
    }
    
    public void set_SV_Address(long address) {
        _start_SV_address = address;
    }
    
    public long get_SV_Address() {
        return _start_SV_address;
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
        List<LocoNetSystemConnectionMemo> list = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        
        // Return if we don't have any LocoNet connection
        if (list.isEmpty()) return;
        
        LocoNetSystemConnectionMemo lm = list.get(0);
//        LocoNetSystemConnectionMemo lm2 = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class).get(1);
//        lm.getSystemPrefix();
//        lm.getUserName();
        LnTrafficController tc = lm.getLnTrafficController();
        if (_index >= 0) {
            LocoNetMessage l = new LocoNetMessage(16);
            String localText;
            if (_stringToSend.length() > _index) {
                localText = _stringToSend.substring(_index) + "    ";  // ensure at least 4 characters
            } else {
                localText = "    "; // 4 characters
            }
            long svAddr = _start_SV_address + _index;
            l.setOpCode(LnConstants.OPC_PEER_XFER);
            l.setElement(1, 0x10);
            l.setElement(2, _sourceAddress & 0x7F);     // source address
            l.setElement(3, 0x05);      // SV_CMD: 0x05 = SV write 4 bytes: write 4 bytes of data from D1..D4
            l.setElement(4, 0x02);      // SV_TYPE: 
            l.setElement(5, getTopBitsByte(_destAddress, svAddr)); // SVX1
            l.setElement(6, (int)(_destAddress & 0x7F));  // DST_L
            l.setElement(7, (int)(_destAddress / 256));  // DST_H
            l.setElement(8, (int)(svAddr & 0x7F));  // SV_ADRL
            l.setElement(9, (int)(svAddr / 256));  // SV_ADRH
            l.setElement(10, getTopBitsByte(localText.charAt(3), localText.charAt(2), localText.charAt(1), localText.charAt(0)));    // SVX2
            l.setElement(11, localText.charAt(0));  // D1
            l.setElement(12, localText.charAt(1));  // D2
            l.setElement(13, localText.charAt(2));  // D3
            l.setElement(14, localText.charAt(3));  // D4
            tc.sendLocoNetMessage(l);
            _index += 4;
            if (_index >= _numCharsToSend) _index = -1;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String value) {
        _stringToSend = value;
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
        return Bundle.getMessage(locale, "StringActionMemory1", "none");
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
        // Do nothing
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
    
//    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNet_OPC_PEER.class);

}
