package jmri.jmrix.loconet;

/**
 * ALM implementation for the SE8.
 * <P>
 * LocoNet ALM messages showing a argument value N will show in the throttle
 * editor as N+1.  Here, we refer to these as "arguments" and "throttle values".
 * Similarly, the addresses are "addresses" in the ALM, based on zero, and
 * "entries" in the throttle, based on 1.
 * <P>
 * This provides 'MAXPAGES' pages,
 * accessed through entries 1, 17, etc. The page number appears as
 * throttle value of 1 through 8.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author Bob Jacobsen     Copyright 2002
 * @version $Revision: 1.2 $
 */

public class Se8AlmImplementation extends AbstractAlmImplementation {

    final int MAXPAGES = 8;  // number of accessible pages
    final int ENTRYSIZE = 64; // number of arguments per entry, must be power of two

    public Se8AlmImplementation(int pNumber, boolean pImage) {
        super(pNumber, pImage);
        initData();
	LnTrafficController.instance().addLocoNetListener(~0, this);
    }

    /**
     * Internal method to save a new value
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item Item number within the block, as 0,1,2,3
     * @param value The integer argument value to store
     */
    void store(int block, int item, int value) {
        contents[page(block, item)][block*4+item] = value;

        // message the throttle if this is a write to the menu number
        if (item == 0 && (block&(ENTRYSIZE-1))==0)
            LnMessageManager.instance().sendMessage("ABCDdcba");
    }

    /**
     * Internal method to retrieve a value
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item Item number within the block, as 0,1,2,3
     * @returns The integer argument value
     */
    int retrieve(int block, int item) {
        return contents[page(block, item)][block*4+item];
    }

    /**
     * Local storage for the values in this ALM
     */
    int contents[][];

    /**
     * Initialize the local storage
     */
    void initData() {
        final int length = 512*4;
        contents = new int[MAXPAGES][length];
        for (int i=0; i<length; i++)
            for (int j=0; j<MAXPAGES; j++)
                contents[j][i]=0x3FFF;
    }

    /**
     * Figure out the page index for a particular request.
     * <P>
     * Note that access to the arguments setting the page numbers
     * (1, 17, 33, etc) always go to page 0.
     * Pages are numbered 0 to MAXPAGE-1 internally, are are stored
     * in the array that way, but because of the way the throttle-resident
     * editor works, are visible to the user as the human-readable 1 to MAXPAGE.
     * @returns 0-7
     */
    int page(int block, int item) {
        // if you're accessing the page value, it's on internal page 0
        if (item==0 && (block&(ENTRYSIZE-1))==0) return 0;
        // else find the right page
        int page = contents[0][(block&(~(ENTRYSIZE-1)))*4+0];
        if (page < 0) return 0;
        if (page >= MAXPAGES) return 0;   // puts default value as page 1
        return page;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Se8AlmImplementation.class.getName());
}