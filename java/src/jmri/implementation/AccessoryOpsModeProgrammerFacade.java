package jmri.implementation;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammerFacade;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade for access to Accessory Decoder Ops Mode programming
 * <p>
 * (Eventually implements four modes, passing all others to underlying
 * programmer:
 * <ul>
 * <li>OPSACCBYTEMODE
 * <li>OPSACCBITMODE
 * <li>OPSACCEXTBYTEMODE
 * <li>OPSACCEXTBITMODE
 * </ul>
 * <P>
 * Used through the String write/read/confirm interface. Accepts integers as
 * addresses, but then emits NMRA DCC packets through the default CommandStation
 * interface (which must be present)
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
// @ToDo("transform to annotations requires e.g. http://alchemy.grimoire.ca/m2/sites/ca.grimoire/todo-annotations/")
// @ToDo("get address from underlyng programmer (which might require adding a new subclass structure to Programmer)")
// @ToDo("finish mode handling; what gets passed through?")
// @ToDo("write almost certainly needs a delay")
// @ToDo("read handling needs to be aligned with other ops mode programmers")
// @ToDo("make sure jmri/jmrit/progsupport/ProgServiceModePane shows the modes, and that DP/DP3 displays them as it configures a decoder")
public class AccessoryOpsModeProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * Programmer facade for access to Accessory Decoder Ops Mode programming
     *
     * @param prog     The Ops Mode Programmer we are piggybacking on.
     * @param addrType A string. "accessory" forces the current decoder address
     *                 to be interpreted as a 14 bit accessory address.
     *                 "decoder" current decoder address to be interpreted as a
     *                 9 bit decoder address
     */
    public AccessoryOpsModeProgrammerFacade(AddressedProgrammer prog, String addrType) {
        super(prog);
        this.mode = prog.getMode();
        this.aprog = prog;
        this._addrType = addrType;
    }

    // ops accessory mode can't read locally
    ProgrammingMode mode;

    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(DefaultProgrammerManager.OPSACCBYTEMODE);
        ret.add(DefaultProgrammerManager.OPSACCBITMODE);
        ret.add(DefaultProgrammerManager.OPSACCEXTBYTEMODE);
        ret.add(DefaultProgrammerManager.OPSACCEXTBITMODE);
        return ret;
    }

    /**
     * Don't pass this mode through, as the underlying doesn't have it (although
     * we should check)
     */
    @Override
    public void setMode(ProgrammingMode p) {
    }

    AddressedProgrammer aprog;

    @Override
    public boolean getCanRead() {
        return prog.getCanRead();
    }

    @Override
    public boolean getCanRead(String addr) {
        return prog.getCanRead(addr);
    }

    @Override
    public boolean getCanWrite() {
        return prog.getCanWrite();
    }

    @Override
    public boolean getCanWrite(String addr) {
        return prog.getCanWrite(addr);
    }

    // members for handling the programmer interface
    int _val;           // remember the value being read/written for confirmative reply
    String _cv;         // remember the cv number being read/written
    String _addrType;	// remember the address type: "decoder" or "accessory"

    // programming interface
    @Override
    synchronized public void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
        _val = val;
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        byte[] b;

        // Send DCC commands to implement prog.writeCV(cv, val, this);
        if ((_addrType != null) && _addrType.equalsIgnoreCase("accessory")) {  // interpret address as accessory address
            // Send a basic ops mode accessory CV programming packet for newer decoders
            b = NmraPacket.accDecoderPktOpsMode(aprog.getAddressNumber(), Integer.parseInt(cv), val);
            InstanceManager.getDefault(CommandStation.class).sendPacket(b, 1);
        } else {  // interpet address as decoder address
            // Send a legacy ops mode accessory CV programming packet for compatibility with older decoders
            // (Sending both packet types was also observed to benefit timing considerations - spacing effect)
            b = NmraPacket.accDecPktOpsModeLegacy(aprog.getAddressNumber(), Integer.parseInt(cv), val);
            InstanceManager.getDefault(CommandStation.class).sendPacket(b, 1);

            // Send a basic ops mode accessory CV programming packet for newer decoders
            b = NmraPacket.accDecPktOpsMode(aprog.getAddressNumber(), Integer.parseInt(cv), val);
            InstanceManager.getDefault(CommandStation.class).sendPacket(b, 1);
        }
        // and reply done
        this.programmingOpReply(val, ProgListener.OK);
    }

    @Override
    synchronized public void confirmCV(String cv, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(cv, p);
    }

    @Override
    synchronized public void readCV(String cv, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        prog.readCV(cv, this);
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    enum ProgState {

        PROGRAMMING, NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify");
        }

        switch (state) {
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
                break;
            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;

        }
    }

    private final static Logger log = LoggerFactory.getLogger(AccessoryOpsModeProgrammerFacade.class.getName());

}
