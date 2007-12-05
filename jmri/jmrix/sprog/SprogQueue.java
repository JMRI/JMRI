package jmri.jmrix.sprog;

import jmri.jmrix.sprog.SprogSlot;

// DccLocoAddress.java

/**
 * DCC Command Station Queue for use with soft command station implementations.
 *
 * @author			Andrew Crosland Copyright (C) 2006
 * @version			$Revision: 1.1 $
 */

public class SprogQueue {

  /**
   * Create a default length queue
   */
  public SprogQueue() {
      length = SprogConstants.MAX_SLOTS;
      q = new SprogSlot[length];
      for (int i = 0; i < length; i++) {
        q[i] = new SprogSlot(i);
      }
    }

    private int length;
    private SprogSlot[] q;

    /**
     * Return contents of Queue slot i
     * @param i int
     * @return SprogSlot
     */
    public SprogSlot slot(int i) {
      return q[i];
    }

    /**
     * Add a new packet to the queue, returns slot number or negative if queue
     * is full, else
     *
     *
     * @param address int
     * @param payload byte[]
     * @param repeat int
     * @return int
     */
    public int add(int address, byte [] payload, int repeat) {
      int i = findFree();
      if (i >=0) {
        q[i].set(address, payload, repeat);
      }
      return i;
    }

    /**
     * Add a new packet to the queue, replacing any existing packet that has the
     * same address. Returns slot number or negative if the queue is full
     *
     * @param address int
     * @param spd int
     * @param isForward boolean
     * @param payload byte[]
     * @param repeat int
     * @return int
     */
    public int findReplace(int address, int spd,
                               boolean isForward, byte [] payload, int repeat) {
      int i = findAddress(address);
      if (i < 0) {
        i = findFree();
      }
      if (i>=0) {
        q[i].set(address, spd, isForward, payload, repeat);
      }
      return i;
    }

    // Public access methods
    public int getLength() { return length; }

    /**
     * Remove a queue entry by index, return false if index out of range
     * @param index int
     * @return boolean
     */
    public boolean removeIndex(int index) {
      if (index < 0 || index >= length) { return false; }
      q[index].clear();
      return true;
    }

    /**
     * Remove a queue entry for a particular address
     * Return slot number or negative if the address is not in the queue
     *
     * @param a int
     * @return int
     */
    public int release(int a) {
      int i = findAddress(a);
      if (i>=0) {
        q[i].clear();
      }
      return i;
    }

    /**
     * Notify packet sent
     * Update the repeat and free the queue entry if it finished with
     *
     * @param i int
     */
/*
    public void notifySent(int i) {
      int r = q[i].getRepeat();
      if (r > 1) {
        q[i].setRepeat(r-1);
      }
      if (r == 1) {
        removeIndex(r);
      }
    }
*/

    /**
     * Clear the queue
     */
    private void clear() {
      for (int i = 0; i < length; i++) {
        q[i].clear();
      }
    }

    /**
     * Find a free queue entry, beginning the search from the last known
     * free entry. Return -1 if the queue is full
     *
     * @return int
     */
    private int findFree() {
      int i, j;
      for (i=0; i<length; i++) {
        if (q[i].isFree()) {
          return i;
        }
      }
      return (-1);
    }

    /**
     * Find a queue entry matching the address
     * Return the index or -1 if the address is not in the queue
     *
     * @param a int
     * @return int
     */
    private int findAddress(int a) {
      for (int i=0; i<length; i++) {
        if (q[i].getAddr() == a) { return i;}
      }
      return (-1);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogQueue.class.getName());

}


/* @(#)SprogQueue.java */
