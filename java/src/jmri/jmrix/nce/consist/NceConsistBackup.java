package jmri.jmrix.nce.consist;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import jmri.util.swing.TextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backups NCE Consists to a text file format defined by NCE.
 * <p>
 * NCE "Backup consists" dumps the consists into a text file. The consists data
 * are stored in the NCE CS starting at xF500 and ending at xFAFF.
 * <p>
 * NCE file format:
 * <p>
 * :F500 (16 bytes per line, grouped as 8 words with space delimiters) :F510 . .
 * :FAF0 :0000
 * <p>
 * Consist data byte:
 * <p>
 * bit 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0
 * <p>
 * This backup routine uses the same consist data format as NCE.
 *
 * @author Dan Boudreau Copyright (C) 2007
 */
public class NceConsistBackup extends Thread implements jmri.jmrix.nce.NceListener {

    private static final int CONSIST_LNTH = 16; // 16 bytes per line
    private int replyLen = 0; // expected byte length
    private int waiting = 0; // to catch responses not intended for this module
    private boolean fileValid = false; // used to flag backup status messages

    private final byte[] nceConsistData = new byte[CONSIST_LNTH];

    JLabel textConsist = new JLabel();
    JLabel consistNumber = new JLabel();

    private NceTrafficController tc = null;
    private int workingNumConsists = -1;

    public NceConsistBackup(NceTrafficController t) {
        tc = t;
        workingNumConsists = NceCmdStationMemory.CabMemorySerial.NUM_CONSIST;
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            workingNumConsists = NceCmdStationMemory.CabMemoryUsb.NUM_CONSIST;
        }
    }

    @Override
    public void run() {

        // get file to write to
        JFileChooser fc = new JFileChooser(FileUtil.getUserFilesPath());
        fc.addChoosableFileFilter(new TextFilter());

        File fs = new File("NCE consist backup.txt");
        fc.setSelectedFile(fs);

        int retVal = fc.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; // Canceled
        }
        if (fc.getSelectedFile() == null) {
            return; // Canceled
        }
        File f = fc.getSelectedFile();
        if (fc.getFileFilter() != fc.getAcceptAllFileFilter()) {
            // append .txt to file name if needed
            String fileName = f.getAbsolutePath();
            String fileNameLC = fileName.toLowerCase();
            if (!fileNameLC.endsWith(".txt")) {
                fileName = fileName + ".txt";
                f = new File(fileName);
            }
        }
        if (f.exists()) {
            if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("FileExists"), new Object[] {f.getName()}),
                    Bundle.getMessage("OverwriteFile"), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
        }

        try (PrintWriter fileOut = new PrintWriter(new BufferedWriter(new FileWriter(f)),
                true)) {

            if (JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("BackupTakesAwhile"), Bundle.getMessage("NceConsistBackup"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                fileOut.close();
                return;
            }

            // create a status frame
            JPanel ps = new JPanel();
            jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame(Bundle.getMessage("NceConsistBackup"));
            fstatus.setLocationRelativeTo(null);
            fstatus.setSize(300, 100);
            fstatus.getContentPane().add(ps);

            ps.add(textConsist);
            ps.add(consistNumber);

            textConsist.setText(Bundle.getMessage("ConsistLineNumber"));
            textConsist.setVisible(true);
            consistNumber.setVisible(true);

            // now read NCE CS consist memory and write to file
            waiting = 0; // reset in case there was a previous error
            fileValid = true; // assume we're going to succeed
            // output string to file

            for (int consistNum = 0; consistNum < workingNumConsists; consistNum++) {

                consistNumber.setText(Integer.toString(consistNum));
                fstatus.setVisible(true);

                getNceConsist(consistNum);

                if (!fileValid) {
                    consistNum = workingNumConsists; // break out of for loop
                }
                if (fileValid) {
                    StringBuilder buf = new StringBuilder();
                    buf.append(":").append(Integer.toHexString(
                            NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM + (consistNum * CONSIST_LNTH)));

                    for (int i = 0; i < CONSIST_LNTH; i++) {
                        buf.append(" ").append(StringUtil.twoHexFromInt(nceConsistData[i++]));
                        buf.append(StringUtil.twoHexFromInt(nceConsistData[i]));
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("consist " + buf.toString());
                    }

                    fileOut.println(buf.toString());
                }
            }

            if (fileValid) {
                // NCE file terminator
                String line = ":0000";
                fileOut.println(line);
            }

            // Write to disk and close file
            fileOut.flush();
            fileOut.close();

            // kill status panel
            fstatus.dispose();

            if (fileValid) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SuccessfulBackup"),
                        Bundle.getMessage("NceConsistBackup"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("BackupFailed"),
                        Bundle.getMessage("NceConsistBackup"), JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            // this is the end of the try-with-resources that opens fileOut.
            return;
        }

    }

    // Read 16 bytes of NCE CS memory
    private void getNceConsist(int cN) {

        NceMessage m = readConsistMemory(cN);
        tc.sendNceMessage(m, this);
        // wait for read to complete
        readWait();
    }

    // wait up to 30 sec per read
    private boolean readWait() {
        int waitcount = 30;
        while (waiting > 0) {
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
            }
            if (waitcount-- < 0) {
                log.error("read timeout");
                fileValid = false; // need to quit
                return false;
            }
        }
        return true;
    }

    // Reads 16 bytes of NCE consist memory
    private NceMessage readConsistMemory(int consistNum) {

        int nceConsistAddr = (consistNum * CONSIST_LNTH) + NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
        replyLen = NceMessage.REPLY_16; // Expect 16 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryRead(nceConsistAddr);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_16);
        return m;
    }

    @Override
    public void message(NceMessage m) {
    } // ignore replies

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY")
    // this reply always expects two consecutive reads
    @Override
    public void reply(NceReply r) {

        if (waiting <= 0) {
            log.error("unexpected response");
            return;
        }
        if (r.getNumDataElements() != replyLen) {
            log.error("reply length incorrect");
            return;
        }

        // load data buffer
        for (int i = 0; i < NceMessage.REPLY_16; i++) {
            nceConsistData[i] = (byte) r.getElement(i);
        }
        waiting--;

        // wake up backup thread
        synchronized (this) {
            notify();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceConsistBackup.class);
}
