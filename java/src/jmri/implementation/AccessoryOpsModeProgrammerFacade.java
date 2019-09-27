package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammerFacade;
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
 * <p>
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
// @ToDo("read handling needs to be aligned with other ops mode programmers")
// @ToDo("make sure jmri/jmrit/progsupport/ProgServiceModePane shows the modes, and that DP/DP3 displays them as it configures a decoder")
public class AccessoryOpsModeProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * Programmer facade for access to Accessory Decoder Ops Mode programming.
     *
     * @param prog     The (possibly already decorated) programmer we are
     *                 piggybacking on.
     * @param addrType A string. "accessory" or "output" causes the address to
     *                 be interpreted as an 11 bit accessory output address.
     *                 "decoder" causes the address to be interpreted as a 9 bit
     *                 accessory decoder address "signal" causes the address to
     *                 be interpreted as an 11 bit signal decoder address.
     * @param delay    A string representing the desired delay between
     *                 programming operations, in milliseconds.
     * @param baseProg The underlying undecorated Ops Mode Programmer we are
     *                 piggybacking on.
     */
    @SuppressFBWarnings(value = "DM_CONVERT_CASE",
            justification = "parameter value is never localised")  // NOI18N
    public AccessoryOpsModeProgrammerFacade(Programmer prog, @Nonnull String addrType, int delay, AddressedProgrammer baseProg) {
        super(prog);
        log.debug("Constructing AccessoryOpsModeProgrammerFacade");
        this._usingProgrammer = null;
        this.mode = prog.getMode();
        this.aprog = prog;
        this._addrType = (addrType == null) ? "" : addrType.toLowerCase(); // NOI18N
        this._delay = delay;
        this._baseProg = baseProg;
    }

    // ops accessory mode can't read locally
    ProgrammingMode mode;

    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.OPSACCBYTEMODE);
        ret.add(ProgrammingMode.OPSACCBITMODE);
        ret.add(ProgrammingMode.OPSACCEXTBYTEMODE);
        ret.add(ProgrammingMode.OPSACCEXTBITMODE);
        return ret;
    }

    /**
     * Don't pass this mode through, as the underlying doesn't have it (although
     * we should check).
     *
     * @param p The desired programming mode
     */
    @Override
    public void setMode(ProgrammingMode p) {
    }

    Programmer aprog;

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
    int _val;                       // remember the value being read/written for confirmative reply
    String _cv;                     // remember the cv number being read/written
    String _addrType;               // remember the address type: ("decoder" or null) or ("accessory" or "output")
    int _delay;                     // remember the programming delay, in milliseconds
    AddressedProgrammer _baseProg;   // remember the underlying programmer

    // programming interface
    @Override
    public synchronized void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
        log.debug("writeCV entry: ProgListener p is {}", p);
        _val = val;
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        byte[] b;

        // Send DCC commands to implement prog.writeCV(cv, val, this);
        switch (_addrType) {
            case "accessory":
            case "output":
                // interpret address as accessory address
                log.debug("Send an accDecoderPktOpsMode: address={}, cv={}, value={}",
                        _baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                b = NmraPacket.accDecoderPktOpsMode(_baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                break;
            case "signal":
                // interpret address as signal address
                log.debug("Send an accSignalDecoderPktOpsMode: address={}, cv={}, value={}",
                        _baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                b = NmraPacket.accSignalDecoderPktOpsMode(_baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                break;
            case "altsignal":
                // interpret address as signal address using the alternative interpretation of S-9.2.1
                log.debug("Send an altAccSignalDecoderPktOpsMode: address={}, cv={}, value={}",
                        _baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                b = NmraPacket.altAccSignalDecoderPktOpsMode(_baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                break;
            case "decoder":
                // interpet address as decoder address
                log.debug("Send an accDecPktOpsMode: address={}, cv={}, value={}",
                        _baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                b = NmraPacket.accDecPktOpsMode(_baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                break;
            case "legacy":
                // interpet address as decoder address and send legacy packet
                log.debug("Send an accDecPktOpsModeLegacy: address={}, cv={}, value={}",
                        _baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                b = NmraPacket.accDecPktOpsModeLegacy(_baseProg.getAddressNumber(), Integer.parseInt(cv), val);
                break;
            default:
                log.error("Unknown Address Type \"{}\"", _addrType);
                programmingOpReply(val, ProgListener.UnknownError);
                return;
        }
        boolean ret = InstanceManager.getDefault(CommandStation.class).sendPacket(b, 2); // send two packets
        if (!ret) {
                log.error("Unable to program cv={}, value={}: Operation not implemented in command station", Integer.parseInt(cv), val);
                programmingOpReply(val, ProgListener.NotImplemented);
                return;
        }

        // set up a delayed completion reply
        log.debug("delaying {} milliseconds for cv={}, value={}", _delay, Integer.parseInt(cv), val);
        jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
            log.debug("            delay elapsed for cv={}, value={}", Integer.parseInt(cv), val);
            programmingOpReply(val, ProgListener.OK);
        }, _delay);
    }

    @Override
    public synchronized void readCV(String cv, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        prog.readCV(cv, this);
    }

    @Override
    public synchronized void confirmCV(String cv, int val, ProgListener p) throws ProgrammerException {
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        prog.confirmCV(cv, val, this);
    }

    private transient volatile jmri.ProgListener _usingProgrammer;

    /**
     * Internal method to remember who's using the programmer.
     *
     *
     * @param p the programmer
     * @throws ProgrammerException if p is already in use
     */
    protected synchronized void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        log.debug("useProgrammer entry: _usingProgrammer is {}", _usingProgrammer);
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
        log.debug("useProgrammer exit: _usingProgrammer is {}", _usingProgrammer);
    }

    enum ProgState {

        PROGRAMMING, NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public synchronized void programmingOpReply(int value, int status) {
        log.debug("notifyProgListenerEnd value={}, status={}", value, status);

        if (status != OK) {
            // pass abort up
            log.debug("Reset and pass abort up");
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            temp.programmingOpReply(value, status);
            return;
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }

        switch (state) {
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                log.debug("going NOTPROGRAMMING after value {}, status={}", value, status);
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

    private final static Logger log = LoggerFactory.getLogger(AccessoryOpsModeProgrammerFacade.class);

}
