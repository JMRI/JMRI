package jmri.util.junit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.netbeans.jemmy.Timeouts;

/**
 * Jemmy timeouts that can be shared between threads.
 * <p>
 * Jemmy's {@link Timeouts} is not thread safe: {@link #setTimeout} removes
 * the old value before it puts the new one, and {@link #getTimeout} reads a
 * value twice. Jemmy operators keep the current timeouts they are given
 * rather than a copy, and some of them set timeouts in it, while every new
 * operator copies the current timeouts with {@link #cloneThis}. When a test
 * closes a modal dialog from another thread, as
 * {@link jmri.util.swing.JemmyUtil#createModalDialogOperatorThread} does,
 * the copy can read a timeout in the moment it is missing, and the thread
 * fails with a NullPointerException in Timeouts.getTimeout. The dialog is
 * then never closed, and the test fails or waits for ever.
 * <p>
 * This version does each of those operations as one step, so the current
 * timeouts can be used from several threads.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ThreadSafeTimeouts extends Timeouts {

    @Override
    public synchronized boolean contains(String name) {
        return super.contains(name);
    }

    @Override
    public synchronized long setTimeout(String name, long newValue) {
        return super.setTimeout(name, newValue);
    }

    @Override
    public synchronized long getTimeout(String name) {
        return super.getTimeout(name);
    }

    @Override
    public synchronized long initTimeout(String name, long newValue) {
        return super.initTimeout(name, newValue);
    }

    @Override
    public synchronized Timeouts cloneThis() {
        return super.cloneThis();
    }

    @Override
    public synchronized void print(PrintWriter pw) {
        super.print(pw);
    }

    @Override
    public synchronized void load(InputStream stream) throws IOException {
        super.load(stream);
    }
}
