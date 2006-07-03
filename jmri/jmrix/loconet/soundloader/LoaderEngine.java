// LoaderEngine.java

package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnTrafficController;

/**
 * Controls the actual LocoNet transfers to download sounds
 * into a Digitrax SFX decoder.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2006
 * @version	    $Revision: 1.1 $
 */
public class LoaderEngine {

    public void doEraseAll() {
        initController();
    }

    /**
     * Start a WAV download.
     * 
     * This returns the message to start the process.
     * You then loop calling nextWavTransfer() until it says it's complete.
     */
    public LocoNetMessage initWavTransfer(int handle, String name, byte[] contents) {
        transferStart = true;
        transferHandle = handle;
        transferName = name;
        transferContents = contents;
        
        return getStartWavMessage(handle, contents.length);
    }
    
    private boolean transferStart;
    private int transferHandle;
    private String transferName;
    private byte[] transferContents;
    private int transferIndex;
    
    /**
     * Get the next message for an ongoing WAV download.
     * 
     * You loop calling nextWavTransfer() until it says it's complete
     * by returning null.
     */
    public LocoNetMessage nextWavTransfer() {
        if (transferStart) {

            transferStart = false;
            transferIndex = 0;

            // first transfer, send DataHeader info            
            byte[] header = new byte[40];
            header[0] = (byte) transferHandle;
            header[1] = (byte) (transferContents.length & 0xFF);
            header[2] = (byte) ((transferContents.length/256) & 0xFF);
            header[3] = (byte) ((transferContents.length/256/256) & 0xFF);
            header[4] = 0; // hdroffset
            header[5] = 0; // wavemode1
            header[6] = 0; // wavemode2
            header[7] = 0; // spare1

            for (int i=8; i<40; i++) header[i] = 0;
            for (int i=0; i<transferName.length(); i++) header[i+8] = (byte) transferName.charAt(i);

            return getSendWavDataMessage(transferHandle, header);
            
        } else {
            // subsequent transfers, send what data you can.
            // calculate remaining bytes
            int remaining = transferContents.length - transferIndex;
            if (remaining < 0) log.error("Did not expect to find length "+transferContents.length+" and index "+transferIndex);
            if (remaining <= 0) return null; // transfer complete
            
            // set up a buffer for this transfer
            int sendSize = remaining;
            if (remaining > WAVPAGESIZE) sendSize = WAVPAGESIZE;
            byte[] buffer = new byte[sendSize];
            for (int i=0; i<sendSize; i++) buffer[i] = transferContents[transferIndex+i];
            
            // update for next time
            transferIndex = transferIndex + sendSize;
            
            // and return the message
            return getSendWavDataMessage(transferHandle, buffer);
            
        }
    }

    static final int WAVPAGESIZE = 256;

    /**
     * Get a message to start the download of WAV data
     * @param handle Handle number for the following data
     * @param length Total length of the WAV data to load
     */
     LocoNetMessage getStartWavMessage(int handle, int length) {
        
        int pagecount = length/WAVPAGESIZE;
        int remainder = length - pagecount*WAVPAGESIZE;
        if (remainder != 0) pagecount++;
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD3, 0x04, handle, pagecount&0x7F, 
                                    (pagecount/128), 0});
        m.setParity();
        return m;
    }

    /**
     * Get a message to tell the PR2 to store length bytes of
     * data (following)
     * @param handle Handle number for the following data
     * @param contents Data to download
     */
     LocoNetMessage getSendWavDataMessage(int handle, byte[] contents) {
        
        int length = contents.length;
        
        LocoNetMessage m = new LocoNetMessage(length + 7);
        m.setElement(0, 0xD3);
        m.setElement(1, 0x08);
        m.setElement(2, handle);
        m.setElement(3, length&0x7F);
        m.setElement(4, (length/128));
        m.setElement(5, 0x00);  // 1st checksum

        for (int i=0; i<length; i++) m.setElement(6+i, contents[i]);
        
        m.setParity();
        return m;
    }

    /**
     * Get a message to erase the non-volatile sound memory
     */
    LocoNetMessage getEraseMessage() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD3, 0x02, 0x01, 0x7F, 0x00, 0x50});
        m.setParity();
        return m;
    }
    
    /**
     * Get a message to exit the download process
     */
    LocoNetMessage getExitMessage() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD3, 0x00, 0x00, 0x00, 0x00, 0x2C});
        m.setParity();
        return m;
    }
    
    void initController() {
        if (controller == null) controller = LnTrafficController.instance();
    }
    
    LnTrafficController controller = null;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoaderEngine.class.getName());

}
