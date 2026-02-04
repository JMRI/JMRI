package jmri.implementation;

import jmri.*;

/**
 * An addressed programmer which supports permissions.
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class PermissionAddressedProgrammer extends PermissionProgrammer implements jmri.AddressedProgrammer {

    public PermissionAddressedProgrammer(AddressedProgrammer programmer) {
        super(programmer);
    }

    @Override
    protected Permission getPermission() {
        return PermissionsProgrammer.PERMISSION_PROGRAMMING_ON_MAIN;
    }

    @Override
    public boolean getLongAddress() {
        return ((AddressedProgrammer)_programmer).getLongAddress();
    }

    @Override
    public int getAddressNumber() {
        return ((AddressedProgrammer)_programmer).getAddressNumber();
    }

    @Override
    public String getAddress() {
        return ((AddressedProgrammer)_programmer).getAddress();
    }

}
