package jmri.jmrix.loconet;

/**
 * Provide an ALM that lives on the LocoNet.
 * <P>ALMs work in terms of numbered (indexed) Args, each 14 bits
 * long.  These are typically in the SW1, SW2 format, but don't have
 * to be.
 * <P>
 * LocoNet ALM messages showing a argument value N will show in the throttle
 * editor as N+1.  Here, we refer to these as "arguments" and "throttle values".
 * Similarly, the addresses are "addresses" in the ALM, based on zero, and
 * "entries" in the throttle, based on 1.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author Bob Jacobsen     Copyright 2002
 * @version $Revision: 1.5 $
 */

public abstract class AbstractAlmImplementation implements LocoNetListener {

    // constants for the ATASK value
    final int INTERROGATE   = 0;
    final int READ          = 2;
    final int WRITE         = 3;

    /**
     * What ALM number does this object respond to?
     */
    protected int mNumber;
    /**
     * Does this ALM reply on the LocoNet, or just
     * form an image of an implementation elsewhere?
     * <P>True for image, false for primary implementation on LocoNet
     */
    protected boolean mImage;

    public AbstractAlmImplementation(int pNumber, boolean pImage) {
        mNumber = pNumber;
        mImage  = pImage;
        initData();
	LnTrafficController.instance().addLocoNetListener(~0, this);
    }

    /**
     * Interpret LocoNet traffic.
     *
     * @param msg Input message
     */
    public void message(LocoNetMessage msg) {
        // sort on opcode and ALM number
        if (msg.getOpCode()==0xEE && msg.getElement(2)==mNumber)
            writeMsg(msg);
        else if (msg.getOpCode()==0xE6 && msg.getElement(2)==mNumber)
            readMsg(msg);
    }

    /**
     * Handle ALM_WR_ACCESS message
     * @param msg
     */
    void writeMsg(LocoNetMessage msg) {
        // sort out the ATASK
        switch (msg.getElement(3)) {
        case INTERROGATE: {
            // if primary implementation
            if (!mImage) {
                // send canned reply
                LocoNetMessage l = new LocoNetMessage(16);
                l.setElement( 0, 0xE6);
                l.setElement( 1, 0x10);
                l.setElement( 2, mNumber);
                l.setElement( 3, INTERROGATE);
                l.setElement( 4, 0x40);
                l.setElement( 5, 0x00);
                l.setElement( 6, 0x03);
                l.setElement( 7, 0x02);
                l.setElement( 8, 0x08);
                l.setElement( 9, 0x7F);
                l.setElement(10, 0x00);
                l.setElement(11, 0x00);
                l.setElement(12, 0x00);
                l.setElement(13, 0x00);
                l.setElement(14, 0x00);
                l.setElement(15, 0x00);
                LnTrafficController.instance().sendLocoNetMessage(l);
            }
            return;
        }
        case READ: {
            int block = msg.getElement(5)*128+msg.getElement(4);
            // if primary implementation
            if (!mImage) {
                // send information in reply
                // find address
                int arg1 = retrieve(block,0);
                int arg2 = retrieve(block,1);
                int arg3 = retrieve(block,2);
                int arg4 = retrieve(block,3);

                // format message and send
                LocoNetMessage l = new LocoNetMessage(16);
                l.setElement( 0, 0xE6);
                l.setElement( 1, 0x10);
                l.setElement( 2, mNumber);
                l.setElement( 3, READ);
                l.setElement( 4, msg.getElement(4));
                l.setElement( 5, msg.getElement(5));
                l.setElement( 6, 0x03);
                l.setElement( 7, arg1&0x7F);
                l.setElement( 8, arg1/128);
                l.setElement( 9, arg2&0x7F);
                l.setElement(10, arg2/128);
                l.setElement(11, arg3&0x7F);
                l.setElement(12, arg3/128);
                l.setElement(13, arg4&0x7F);
                l.setElement(14, arg4/128);
                l.setElement(15, 0x00);
                LnTrafficController.instance().sendLocoNetMessage(l);
            }
            
            noteRead(block);
            
            return;
        }
        case WRITE:
            // find address
            int block = msg.getElement(5)*128+msg.getElement(4);
            // get and save data
            int arg1 = msg.getElement(8)*128+msg.getElement(7);
            store(block, 0, arg1);
            int arg2 = msg.getElement(10)*128+msg.getElement(9);
            store(block, 1, arg2);
            int arg3 = msg.getElement(12)*128+msg.getElement(11);
            store(block, 2, arg3);
            int arg4 = msg.getElement(14)*128+msg.getElement(13);
            store(block, 3, arg4);
            
            noteChanged(block);
            
            // if primary implementation
            if (!mImage) {
                // send LACK
                LocoNetMessage l = new LocoNetMessage(4);
                l.setElement( 0, 0xB4);
                l.setElement( 2, 0x66);  // E6 without high bit
                l.setElement( 3, 0x7F);
                LnTrafficController.instance().sendLocoNetMessage(l);
            }
            return;
        default:
            log.warn("Unexpected ATASK: "+msg.getElement(3));
            return;
        }
    }

    /**
     * Notify possible subclass that a block has changed.
     */
    public void noteChanged(int block) {}

    /**
     * Notify possible subclass that a read is being handled
     */
    public void noteRead(int block) {}
    
    /**
     * Handle ALM_RD_ACCESS message.
     * <P>
     * This is somewhat odd, as only we should be sending this; it's really
     * a reply to an ALM_WR_ACCESS message.  It could be the echo of our
     * own message, however, so it's not treated as an error now.
     * @param msg
     */
    void readMsg(LocoNetMessage msg) {
    }

    /**
     * Create and send the LocoNet message to read a 
     * particular block.
     * <P>
     * The results will return later.
     */
    void sendRead(int block) {
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement( 0, 0xEE);
        l.setElement( 1, 0x10);
        l.setElement( 2, mNumber);
        l.setElement( 3, 2);    // read
        l.setElement( 4, block&0x7F);    // blockl
        l.setElement( 5, block/128);    // blockh
        l.setElement( 6, 0x03);
        l.setElement( 7, 0x02);
        l.setElement( 8, 0x08);
        l.setElement( 9, 0x7F);
        l.setElement(10, 0x00);
        l.setElement(11, 0x00);
        l.setElement(12, 0x00);
        l.setElement(13, 0x00);
        l.setElement(14, 0x00);
        l.setElement(15, 0x00);
        LnTrafficController.instance().sendLocoNetMessage(l);
    }

    /**
     * Create and send the LocoNet message to read a 
     * particular block.
     * <P>
     * The results will return later.
     */
    void sendWrite(int block) {
        int arg1 = retrieve(block, 0);
        int arg2 = retrieve(block, 1);
        int arg3 = retrieve(block, 2);
        int arg4 = retrieve(block, 3);

        // format message and send
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement( 0, 0xEE);
        l.setElement( 1, 0x10);
        l.setElement( 2, mNumber);
        l.setElement( 3, 3);    // write
        l.setElement( 4, block&0x7F);  // blockl
        l.setElement( 5, block/128);  // blockh
        l.setElement( 6, 0x03);
        l.setElement( 7, arg1&0x7F);
        l.setElement( 8, arg1/128);
        l.setElement( 9, arg2&0x7F);
        l.setElement(10, arg2/128);
        l.setElement(11, arg3&0x7F);
        l.setElement(12, arg3/128);
        l.setElement(13, arg4&0x7F);
        l.setElement(14, arg4/128);
        l.setElement(15, 0x00);
        LnTrafficController.instance().sendLocoNetMessage(l);
        return;
    }
    
    /**
     * Abstract method invoked to save a new value.
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item Item number within the block, as 0,1,2,3
     * @param value The integer argument value to store
     */
    abstract void store(int block, int item, int value);

    /**
     * Abstract method invoked to retrieve a value.
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item Item number within the block, as 0,1,2,3
     * @return The integer argument value
     */
    abstract int retrieve(int block, int item);

    /**
     * Abstract method to initialize the local storage
     */
    abstract void initData();

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractAlmImplementation.class.getName());
}