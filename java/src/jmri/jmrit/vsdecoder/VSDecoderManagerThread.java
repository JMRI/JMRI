package jmri.jmrit.vsdecoder;

/**
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <p>
 *
 * @author Mark Underwood Copyright (C) 2011
 */
class VSDecoderManagerThread extends Thread {

    private volatile static VSDecoderManagerThread instance = null;
    private static VSDecoderManager manager = null;
    boolean is_running;

    private VSDecoderManagerThread() {
        super();
        is_running = false;
    }

    public static VSDecoderManagerThread instance(Boolean create) {
        manager = new VSDecoderManager();
        return instance();
    }

    public static VSDecoderManagerThread instance() {
        if (instance == null) {
            VSDecoderManagerThread temp = new VSDecoderManagerThread();
            temp.setName("VSDecoderManagerThread");
            temp.start();
            instance = temp; // don't allow escape of VSDecoderManagerThread object until running
        }
        return instance;
    }

    public static VSDecoderManager manager() {
        return VSDecoderManagerThread.manager;
    }

    @Override
    public void run() {
        is_running = true;
        while (is_running) {
            // just nap.
            try {
                sleep(20);
            } catch (InterruptedException e) {
            }
        }
        // all done.
    }

    public void kill() {
        is_running = false;
    }
}
