package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an ALM that lives on the LocoNet.
 * <p>
 * ALMs work in terms of numbered (indexed) Args, each 14 bits long. These are
 * typically in the SW1, SW2 format, but don't have to be.
 * <p>
 * This ALM can operate in one of two modes:
 * <ul>
 *   <li>"image" - This ALM shadows one that really exists somewhere else. This
 *   implementation keeps values that are being written, but doesn't reply to read
 *   or write commands on the LocoNet.
 *   <li>"not image" - This is the only existing implementation of this particular
 *   ALM, so it replies to read and write commands.
 * </ul>
 * LocoNet ALM messages showing a argument value N will show in the throttle
 * editor as N+1. Here, we refer to these as "arguments" and "throttle values".
 * Similarly, device addresses are "addresses" in the ALM, based on zero, and
 * "entries" in the throttle, based on 1.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright 2002
 * @deprecated as of JMRI 2.13.5 - keep for a bit until DCS240 situation is resolved (note added June 2016)
 */
@Deprecated
public abstract class AbstractAlmImplementation implements LocoNetListener {

    // constants for the ATASK value
    final int INTERROGATE = 0;
    final int READ = 2;
    final int WRITE = 3;

    /**
     * What ALM number does this object respond to?
     */
    protected int mNumber;
    /**
     * Does this ALM reply on the LocoNet, or just form an image of an
     * implementation elsewhere?
     * <p>
     * True for image, false for primary implementation on LocoNet.
     */
    protected boolean mImage;

    public AbstractAlmImplementation(int pNumber, boolean pImage) {
        mNumber = pNumber;
        mImage = pImage;
        initData();
        adapterMemo.getLnTrafficController().addLocoNetListener(~0, this);
    }

    /**
     * Interpret LocoNet traffic.
     *
     * @param msg Input message
     */
    @Override
    public void message(LocoNetMessage msg) {
        // sort on opcode and ALM number
        if ((msg.getOpCode() == LnConstants.OPC_IMM_PACKET_2) && msg.getElement(2) == mNumber) {
            writeMsg(msg);
        } else if ((msg.getOpCode() == LnConstants.OPC_ALM_READ) && msg.getElement(2) == mNumber) {
            readMsg(msg);
        } else if ((msg.getOpCode() == LnConstants.OPC_LONG_ACK) && msg.getElement(1) == 0x6E) {
            lackMsg(msg);
        }
    }

    boolean handleNextLACK = false;
    boolean waitWriteMessage = false;
    int lastWriteBlock;
    LocoNetSystemConnectionMemo adapterMemo;

    /**
     * Handle LACK message.
     * <p>
     * If we're waiting for this, it indicates successful end of a write ALM
     * sequence.
     *
     */
    void lackMsg(LocoNetMessage msg) {
        if (handleNextLACK) {
            handleNextLACK = false;
            noteWriteComplete(lastWriteBlock);
        }
    }

    /**
     * Handle ALM_WR_ACCESS message.
     * <p>
     * If we're an image, just record information from a WRITE.
     * <p>
     * If we're not an image, reply to all commands
     *
     */
    void writeMsg(LocoNetMessage msg) {
        // sort out the ATASK
        switch (msg.getElement(3)) {
            case INTERROGATE: {
                // if primary implementation
                if (!mImage) {
                    // send canned reply
                    LocoNetMessage l = new LocoNetMessage(16);
                    l.setElement(0, 0xE6);
                    l.setElement(1, 0x10);
                    l.setElement(2, mNumber);
                    l.setElement(3, INTERROGATE);
                    l.setElement(4, 0x40);
                    l.setElement(5, 0x00);
                    l.setElement(6, 0x03);
                    l.setElement(7, 0x02);
                    l.setElement(8, 0x08);
                    l.setElement(9, 0x7F);
                    l.setElement(10, 0x00);
                    l.setElement(11, 0x00);
                    l.setElement(12, 0x00);
                    l.setElement(13, 0x00);
                    l.setElement(14, 0x00);
                    l.setElement(15, 0x00);
                    adapterMemo.getLnTrafficController().sendLocoNetMessage(l);
                }
                return;
            }
            case READ: {
                int block = msg.getElement(5) * 128 + msg.getElement(4);
                // if primary implementation
                if (!mImage) {
                    // send information in reply
                    // find address
                    int arg1 = retrieve(block, 0);
                    int arg2 = retrieve(block, 1);
                    int arg3 = retrieve(block, 2);
                    int arg4 = retrieve(block, 3);

                    // format message and send
                    LocoNetMessage l = new LocoNetMessage(16);
                    l.setElement(0, 0xE6);
                    l.setElement(1, 0x10);
                    l.setElement(2, mNumber);
                    l.setElement(3, READ);
                    l.setElement(4, msg.getElement(4));
                    l.setElement(5, msg.getElement(5));
                    l.setElement(6, 0x03);
                    l.setElement(7, arg1 & 0x7F);
                    l.setElement(8, arg1 / 128);
                    l.setElement(9, arg2 & 0x7F);
                    l.setElement(10, arg2 / 128);
                    l.setElement(11, arg3 & 0x7F);
                    l.setElement(12, arg3 / 128);
                    l.setElement(13, arg4 & 0x7F);
                    l.setElement(14, arg4 / 128);
                    l.setElement(15, 0x00);
                    adapterMemo.getLnTrafficController().sendLocoNetMessage(l);
                }

                noteReadCmd(block);

                return;
            }
            case WRITE:
                // find address
                int block = msg.getElement(5) * 128 + msg.getElement(4);
                // get and save data
                int arg1 = msg.getElement(8) * 128 + msg.getElement(7);
                store(block, 0, arg1);
                int arg2 = msg.getElement(10) * 128 + msg.getElement(9);
                store(block, 1, arg2);
                int arg3 = msg.getElement(12) * 128 + msg.getElement(11);
                store(block, 2, arg3);
                int arg4 = msg.getElement(14) * 128 + msg.getElement(13);
                store(block, 3, arg4);

                noteChanged(block);

                // is this a message we sent?
                if (waitWriteMessage) {
                    waitWriteMessage = false;
                    handleNextLACK = true;
                }

                // if primary implementation
                if (!mImage) {
                    // send LACK
                    LocoNetMessage l = new LocoNetMessage(4);
                    l.setElement(0, 0xB4);
                    l.setElement(1, 0x6E);  // EE without high bit
                    l.setElement(2, 0x7F);
                    adapterMemo.getLnTrafficController().sendLocoNetMessage(l);
                }
                return;
            default:
                log.warn("Unexpected ATASK: " + msg.getElement(3));
                return;
        }
    }

    /**
     * Notify possible subclass that a block has changed.
     * @param block  something about a block
     */
    public void noteChanged(int block) {
    }

    /**
     * Notify possible subclass that a read cmd is being handled
     * @param block  something about a block
     */
    public void noteReadCmd(int block) {
    }

    /**
     * Notify possible subclass that a read reply is being handled
     * @param block  something about a block
     */
    public void noteReadReply(int block) {
    }

    /**
     * Notify possible subclass that a write operation is complete
     * @param block  something about a block
     */
    public void noteWriteComplete(int block) {
    }

    /**
     * Handle ALM_RD_ACCESS message.
     * <p>
     * If we're an image, this came from the real implementation and reflects
     * the correct values; capture them.
     * <p>
     * If we're not an image, we just sent this, so we'll ignore it.
     *
     * @param msg a LocoNet message
     */
    void readMsg(LocoNetMessage msg) {
        // sort out the ATASK
        switch (msg.getElement(3)) {
            case READ:
                if (!mImage) {
                    return;
                }

                // image, so capture the data
                // find address
                int block = msg.getElement(5) * 128 + msg.getElement(4);
                // get and save data
                int arg1 = msg.getElement(8) * 128 + msg.getElement(7);
                store(block, 0, arg1);
                int arg2 = msg.getElement(10) * 128 + msg.getElement(9);
                store(block, 1, arg2);
                int arg3 = msg.getElement(12) * 128 + msg.getElement(11);
                store(block, 2, arg3);
                int arg4 = msg.getElement(14) * 128 + msg.getElement(13);
                store(block, 3, arg4);

                noteReadReply(block);
                return;

            default:
                return;
        }
    }

    /**
     * Create and send the LocoNet message to read a particular block.
     * <p>
     * The results will return later.
     */
    void sendRead(int block) {
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement(0, 0xEE);
        l.setElement(1, 0x10);
        l.setElement(2, mNumber);
        l.setElement(3, 2);    // read
        l.setElement(4, block & 0x7F);    // blockl
        l.setElement(5, block / 128);    // blockh
        l.setElement(6, 0x03);
        l.setElement(7, 0x02);
        l.setElement(8, 0x08);
        l.setElement(9, 0x7F);
        l.setElement(10, 0x00);
        l.setElement(11, 0x00);
        l.setElement(12, 0x00);
        l.setElement(13, 0x00);
        l.setElement(14, 0x00);
        l.setElement(15, 0x00);
        adapterMemo.getLnTrafficController().sendLocoNetMessage(l);
    }

    /**
     * Create and send the LocoNet message to read a particular block.
     * <p>
     * The waitWriteMessage and handleNextLACK boolean variables are used to
     * control processong of the LACK message that will come from this.
     * <p>
     * For a single write, that's not needed, but if you want to know when it is
     * complete you need to:
     * <ol>
     *   <li>Recognize when your write command has come back from the LocoNet
     *   <li>Wait for the next LACK
     * </ol>
     * Hopefully this ALM writer is unique, so there won't be two writes to the
     * same ALM going on at the same time; the LocoNet is not well synchronized
     * against that.
     */
    void sendWrite(int block) {
        int arg1 = retrieve(block, 0);
        int arg2 = retrieve(block, 1);
        int arg3 = retrieve(block, 2);
        int arg4 = retrieve(block, 3);

        // format message and send
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement(0, 0xEE);
        l.setElement(1, 0x10);
        l.setElement(2, mNumber);
        l.setElement(3, 3);    // write
        l.setElement(4, block & 0x7F);  // blockl
        l.setElement(5, block / 128);  // blockh
        l.setElement(6, 0x03);
        l.setElement(7, arg1 & 0x7F);
        l.setElement(8, arg1 / 128);
        l.setElement(9, arg2 & 0x7F);
        l.setElement(10, arg2 / 128);
        l.setElement(11, arg3 & 0x7F);
        l.setElement(12, arg3 / 128);
        l.setElement(13, arg4 & 0x7F);
        l.setElement(14, arg4 / 128);
        l.setElement(15, 0x00);

        lastWriteBlock = block;
        waitWriteMessage = true;

        adapterMemo.getLnTrafficController().sendLocoNetMessage(l);
        return;
    }

    /**
     * Abstract method invoked to save a new value.
     * <p>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item  Item number within the block, as 0,1,2,3
     * @param value The integer argument value to store
     */
    abstract void store(int block, int item, int value);

    /**
     * Abstract method invoked to retrieve a value.
     * <p>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item  Item number within the block, as 0,1,2,3
     * @return The integer argument value
     */
    abstract int retrieve(int block, int item);

    /**
     * Abstract method to initialize the local storage
     */
    abstract void initData();

    private final static Logger log = LoggerFactory.getLogger(AbstractAlmImplementation.class);
}
