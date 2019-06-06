package jmri.jmrit.audio;

/**
 * Base implementation of all common thread code for use by threads in the
 * various Audio classes.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudioThread extends Thread implements AudioThread {

    /**
     * True while the thread is running.
     */
    private boolean alive = true;

    /**
     * True when thread should die.
     */
    private boolean die = false;

    /**
     * Simple counter to ensure that each created thread has a unique name.
     */
    private static long counter;

    /**
     * Default constructor that gives this thread a unique name based on the
     * value of the static counter
     */
    public AbstractAudioThread() {
        this.setName("audio-" + AbstractAudioThread.nextCounter());
    }

    private synchronized static long nextCounter() {
        return counter++;
    }

    /**
     * Perform necessary cleanup routines before shutting down.
     */
    protected void cleanup() {

        // Thread is to shutdown
        die(SET, true);

        // End of thread
        alive(SET, false);
    }

    @Override
    public boolean alive() {
        return alive(GET, NA);
    }

    @Override
    public void die() {
        die(SET, true);
    }

    /**
     * Checks if the thread is in the process of shutting down.
     *
     * @return true, if thread should die
     */
    protected boolean dying() {
        return die(GET, NA);
    }

    /**
     * Based on the 'action' parameter, sets or returns if the thread is running.
     *
     * @param action GET or SET
     * @param value  for action==SET, new value; for action==GET, NA
     * @return true, when thread is alive
     */
    private synchronized boolean alive(boolean action, boolean value) {
        if (action == SET) {
            alive = value;
        }
        return alive;
    }

    /**
     * Based on the 'action' parameter, sets or returns if the thread should die.
     *
     * @param action GET or SET
     * @param value  for action==SET, new value; for action==GET, NA
     * @return true, when thread should die
     */
    private synchronized boolean die(boolean action, boolean value) {
        if (action == SET) {
            die = value;
        }
        return die;
    }

    /**
     * Sleep for the specified number of milliseconds.
     * <p>
     * (Avoids cluttering the main code with the try-catch construct)
     *
     * @param ms number of milliseconds to sleep for
     */
    protected static void snooze(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

}
