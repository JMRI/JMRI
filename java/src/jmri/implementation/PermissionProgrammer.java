package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.util.List;

import jmri.*;

/**
 * A programmer which supports permissions.
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class PermissionProgrammer implements jmri.Programmer {

    /**
     * The minimum time to pass between requests to trigger an user request.
     * During programming, JMRI might do thousands write requests, and to
     * prevent that the user has to answer on each of these, this time needs
     * to pass between two request to result in an user request to tell the
     * user that permissions are denied.
     */
    private static final long THROTTLE_USER_REQUEST_TIME = 3000;  // time in milliseconds

    private long _throttleUserRequestTime = 0L;

    protected final Programmer _programmer;

    public PermissionProgrammer(Programmer programmer) {
        this._programmer = programmer;
    }

    protected boolean throttleUserRequest() {
        long oldTime = _throttleUserRequestTime;
        _throttleUserRequestTime = System.currentTimeMillis();
        return (_throttleUserRequestTime - oldTime) > THROTTLE_USER_REQUEST_TIME;
    }

    protected Permission getPermission() {
        return PermissionsProgrammer.PERMISSION_PROGRAMMING_TRACK;
    }

    private boolean hasPermission() {
        // Does the user has permission?
        boolean hasPerm = InstanceManager.getDefault(PermissionManager.class)
                .hasAtLeastPermission(getPermission(),
                        BooleanPermission.BooleanValue.TRUE);

        if (!hasPerm && throttleUserRequest()) {
            // Notify the user about lack of permission
            InstanceManager.getDefault(PermissionManager.class)
                    .ensureAtLeastPermission(getPermission(),
                            BooleanPermission.BooleanValue.TRUE);

            // Reset the time for throttle user request
            throttleUserRequest();
        }
        return hasPerm;
    }

    /** {@inheritDoc} */
    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        if (hasPermission()) {
            _programmer.writeCV(CV, val, p);
        } else {
            notifyProgListenerEnd(p, 0, ProgListener.UnknownError);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        if (hasPermission()) {
            _programmer.readCV(CV, p);
        } else {
            notifyProgListenerEnd(p, 0, ProgListener.UnknownError);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void readCV(String CVname, ProgListener p, int startVal) throws jmri.ProgrammerException {
        if (hasPermission()) {
            _programmer.readCV(CVname, p, startVal);
        } else {
            notifyProgListenerEnd(p, 0, ProgListener.UnknownError);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        if (hasPermission()) {
            _programmer.confirmCV(CV, val, p);
        } else {
            notifyProgListenerEnd(p, 0, ProgListener.UnknownError);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        return _programmer.getSupportedModes();
    }

    /** {@inheritDoc} */
    @Override
    public void setMode(ProgrammingMode p) {
        _programmer.setMode(p);
    }

    /** {@inheritDoc} */
    @Override
    public ProgrammingMode getMode() {
        return _programmer.getMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getCanRead() {
        return _programmer.getCanRead();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getCanRead(String addr) {
        return _programmer.getCanRead(addr);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getCanWrite() {
        return _programmer.getCanWrite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getCanWrite(String addr) {
        return _programmer.getCanWrite(addr);
    }

    /** {@inheritDoc} */
    @Override
    public WriteConfirmMode getWriteConfirmMode(String addr) {
        return _programmer.getWriteConfirmMode(addr);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        _programmer.addPropertyChangeListener(p);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        _programmer.removePropertyChangeListener(p);
    }

    /** {@inheritDoc} */
    @Override
    public String decodeErrorCode(int i) {
        return _programmer.decodeErrorCode(i);
    }

    /** {@inheritDoc} */
    @Override
    public Configurator getConfigurator() {
        return _programmer.getConfigurator();
    }
}
