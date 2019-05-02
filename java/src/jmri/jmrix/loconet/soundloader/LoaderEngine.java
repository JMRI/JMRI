package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.spjfile.SpjFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the actual LocoNet transfers to download sounds into a Digitrax SFX
 * decoder.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class LoaderEngine {

    static final int CMD_START = 0x04;
    static final int CMD_ADD = 0x08;

    static final int TYPE_SDF = 0x01;
    static final int TYPE_WAV = 0x00;

    static final int SENDPAGESIZE = 256;
    static final int SENDDATASIZE = 128;

    SpjFile spjFile;

    public LoaderEngine(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * Send the complete sequence to download to a decoder.
     *
     * Intended to be run in a separate thread. Uses "notify" method for status
     * updates; overload that to redirect the messages
     */
    public void runDownload(SpjFile file) {
        this.spjFile = file;

        initController();

        // use a try-catch to handle aborts from below
        try {
            // erase flash
            notify(Bundle.getMessage("EngineEraseFlash"));
            controller.sendLocoNetMessage(getEraseMessage());
            protectedWait(1000);
            notify(Bundle.getMessage("EngineEraseWait"));
            protectedWait(20000);

            // start
            notify(Bundle.getMessage("EngineSendInit"));
            controller.sendLocoNetMessage(getInitMessage());
            protectedWait(250);

            // send SDF info
            sendSDF();

            // send all WAV subfiles
            sendAllWAV();

            // end
            controller.sendLocoNetMessage(getExitMessage());
            notify(Bundle.getMessage("EngineDone"));
        } catch (DelayException e) {
            notify(Bundle.getMessage("EngineAbortDelay"));
        }

    }

    void sendSDF() throws DelayException {
        notify(Bundle.getMessage("EngineSendSdf"));

        // get control info, data
        SpjFile.Header header = spjFile.findSdfHeader();
        int handle = header.getHandle();
        String name = header.getName();
        byte[] contents = header.getByteArray();

        // transfer
        LocoNetMessage m;

        m = initTransfer(TYPE_SDF, handle, name, contents);
        controller.sendLocoNetMessage(m);
        throttleOutbound(m);

        while ((m = nextTransfer()) != null) {
            controller.sendLocoNetMessage(m);
            throttleOutbound(m);
        }
    }

    void sendAllWAV() throws DelayException {
        notify(Bundle.getMessage("EngineSendWav"));
        for (int i = 1; i < spjFile.numHeaders(); i++) {
            // see if WAV
            if (spjFile.getHeader(i).isWAV()) {
                sendOneWav(i);
            }
        }
    }

    public void sendOneWav(int index) throws DelayException {
        notify(Bundle.getMessage("EngineSendWavBlock", index));
        // get control info, data
        SpjFile.Header header = spjFile.getHeader(index);
        int handle = header.getHandle();
        String name = header.getName();
        byte[] buffer = header.getByteArray();

        // that byte array is the "record", not "data";
        // recopy in offset
        int offset = header.getDataStart() - header.getRecordStart();
        int len = header.getDataLength();
        byte[] contents = new byte[len];
        for (int i = 0; i < len; i++) {
            contents[i] = buffer[i + offset];
        }

        // transfer
        LocoNetMessage m;

        m = initTransfer(TYPE_WAV, handle, name, contents);
        controller.sendLocoNetMessage(m);
        throttleOutbound(m);

        while ((m = nextTransfer()) != null) {
            controller.sendLocoNetMessage(m);
            throttleOutbound(m);
        }
    }

    /**
     * Nofify of status of download.
     *
     * This implementation doesn't do much, but this is provided as a separate
     * method to allow easy overloading
     */
    public void notify(String message) {
        log.debug(message);
    }

    /**
     * Delay to prevent too much data being sent down.
     *
     * Works with the controller to ensure that too much data doesn't back up.
     */
    void throttleOutbound(LocoNetMessage m) throws DelayException {
        protectedWait(50);  // minimum wait to clear

        // wait up to 1 sec in 10mSec chunks for isXmtBusy to clear
        for (int i = 1; i < 100; i++) {
            if (!controller.isXmtBusy()) {
                return; // done, so return
            }            // wait a while, and then try again
            protectedWait(10);
        }
        throw new DelayException("Ran out of time after sending " + m.toString()); // NOI18N
    }

    static class DelayException extends Exception {
        DelayException(String s) {
            super(s);
        }
    }

    /**
     * Provide a simple object wait. This handles interrupts, synchonization,
     * etc
     */
    public void protectedWait(int millis) {
        synchronized (this) {
            try {
                wait(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }
    }

    /**
     * Start a sequence to download a specific type of data.
     *
     * This returns the message to start the process. You then loop calling
     * nextWavTransfer() until it says it's complete.
     *
     * @param type Either TYPE_SDF or TYPE_WAV for the data type
     */
    LocoNetMessage initTransfer(int type, int handle, String name, byte[] contents) {
        transferType = type;
        transferStart = true;
        transferHandle = handle;
        transferName = name;
        transferContents = contents;

        return getStartDataMessage(transferType, handle, contents.length);
    }

    private boolean transferStart;
    private int transferType;
    private int transferHandle;
    private String transferName;
    private byte[] transferContents;
    private int transferIndex;

    /**
     * Get the next message for an ongoing WAV download.
     *
     * You loop calling nextWavTransfer() until it says it's complete by
     * returning null.
     */
    public LocoNetMessage nextTransfer() {
        if (transferStart) {

            transferStart = false;
            transferIndex = 0;

            // first transfer, send DataHeader info            
            byte[] header = new byte[40];
            header[0] = (byte) transferHandle;
            header[1] = (byte) (transferContents.length & 0xFF);
            header[2] = (byte) ((transferContents.length / 256) & 0xFF);
            header[3] = (byte) ((transferContents.length / 256 / 256) & 0xFF);
            header[4] = 0; // hdroffset
            header[5] = 0; // wavemode1
            header[6] = 0; // wavemode2
            header[7] = 0; // spare1

            for (int i = 8; i < 40; i++) {
                header[i] = 0;
            }
            if (transferName.length() > 32) {
                log.error("name {} is too long, truncated", transferName);
            }
            for (int i = 0; i < Math.min(32, transferName.length()); i++) {
                header[i + 8] = (byte) transferName.charAt(i);
            }

            return getSendDataMessage(transferType, transferHandle, header);

        } else {
            // subsequent transfers, send what data you can.
            // calculate remaining bytes
            int remaining = transferContents.length - transferIndex;
            if (remaining < 0) {
                log.error("Did not expect to find length {} and index {}", transferContents.length, transferIndex);
            }
            if (remaining <= 0) {
                return null; // transfer complete
            }
            // set up a buffer for this transfer
            int sendSize = remaining;
            if (remaining > SENDDATASIZE) {
                sendSize = SENDDATASIZE;
            }
            byte[] buffer = new byte[sendSize];
            for (int i = 0; i < sendSize; i++) {
                buffer[i] = transferContents[transferIndex + i];
            }

            // update for next time
            transferIndex = transferIndex + sendSize;

            // and return the message
            return getSendDataMessage(transferType, transferHandle, buffer);
        }
    }

    /**
     * Get a message to start the download of data
     *
     * @param handle Handle number for the following data
     * @param length Total length of the WAV data to load
     */
    LocoNetMessage getStartDataMessage(int type, int handle, int length) {
        int pagecount = length / SENDPAGESIZE;
        int remainder = length - pagecount * SENDPAGESIZE;
        if (remainder != 0) {
            pagecount++;
        }

        if (log.isDebugEnabled()) {
            log.debug("getStartDataMessage: {},{},{};{},{}", type, handle, length, pagecount, remainder);
        }

        LocoNetMessage m = new LocoNetMessage(new int[]{0xD3, (type | CMD_START), handle, pagecount & 0x7F,
            (pagecount / 128), 0});
        m.setParity();
        return m;
    }

    /**
     * Get a message to tell the PR2 to store length bytes of data (following)
     *
     * @param handle   Handle number for the following data
     * @param contents Data to download
     */
    LocoNetMessage getSendDataMessage(int type, int handle, byte[] contents) {

        int length = contents.length;

        LocoNetMessage m = new LocoNetMessage(length + 7);
        m.setElement(0, 0xD3);
        m.setElement(1, type | CMD_ADD);
        m.setElement(2, handle);
        m.setElement(3, length & 0x7F);
        m.setElement(4, (length / 128));
        m.setElement(5, 0x00);  // 1st checksum

        for (int i = 0; i < length; i++) {
            m.setElement(6 + i, contents[i]);
        }

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
     * Get a message to initialize the load sequence
     */
    LocoNetMessage getInitMessage() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD3, 0x01, 0x00, 0x00, 0x00, 0x2D});
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

    LocoNetSystemConnectionMemo memo;

    void initController() {
        if (controller == null) {
            controller = memo.getLnTrafficController();
        }
    }

    LnTrafficController controller = null;

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(LoaderEngine.class);

}
