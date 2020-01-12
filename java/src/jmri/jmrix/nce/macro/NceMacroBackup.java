package jmri.jmrix.nce.macro;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import jmri.util.swing.TextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backups NCE Macros to a text file format defined by NCE.
 * <p>
 * NCE "Backup macros" dumps the macros into a text file. Each line contains the
 * contents of one macro. The first macro, 0 starts at address xC800. The last
 * macro 255 is at address xDBEC.
 * <p>
 * NCE file format:
 * <p>
 * :C800 (macro 0: 20 hex chars representing 10 accessories) :C814 (macro 1: 20
 * hex chars representing 10 accessories) :C828 (macro 2: 20 hex chars
 * representing 10 accessories) . . :DBEC (macro 255: 20 hex chars representing
 * 10 accessories) :0000
 * <p>
 * Macro data byte:
 * <p>
 * bit 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0 _ _ _ _ 1 0 A A A A A A 1 A A A C D
 * D D addr bit 7 6 5 4 3 2 10 9 8 1 0 turnout T
 * <p>
 * By convention, MSB address bits 10 - 8 are one's complement. NCE macros
 * always set the C bit to 1. The LSB "D" (0) determines if the accessory is to
 * be thrown (0) or closed (1). The next two bits "D D" are the LSBs of the
 * accessory address. Note that NCE display addresses are 1 greater than NMRA
 * DCC. Note that address bit 2 isn't supposed to be inverted, but it is the way
 * NCE implemented their macros.
 * <p>
 * Examples:
 * <p>
 * 81F8 = accessory 1 thrown 9FFC = accessory 123 thrown B5FD = accessory 211
 * close BF8F = accessory 2044 close
 * <p>
 * FF10 = link macro 16
 * <p>
 * This backup routine uses the same macro data format as NCE.
 *
 * @author Dan Boudreau Copyright (C) 2007
 */
public class NceMacroBackup extends Thread implements jmri.jmrix.nce.NceListener {

    private static final int CS_MACRO_MEM = 0xC800; // start of NCE CS Macro memory
    private static final int NUM_MACRO = 256;  // there are 256 possible macros
    private static final int MACRO_LNTH = 20;  // 20 bytes per macro
    private static final int REPLY_16 = 16;   // reply length of 16 byte expected
    private static int replyLen = 0;    // expected byte length
    private int waiting = 0;      // to catch responses not intended for this module
    private boolean secondRead = false;    // when true, another 16 byte read expected
    private boolean fileValid = false;    // used to flag backup status messages

    private static final byte[] NCE_MACRO_DATA = new byte[MACRO_LNTH];

    javax.swing.JLabel textMacro = new javax.swing.JLabel();
    javax.swing.JLabel macroNumber = new javax.swing.JLabel();

    private NceTrafficController tc = null;

    public NceMacroBackup(NceTrafficController t) {
        super();
        this.tc = t;
    }

    @Override
    public void run() {

        // get file to write to
        JFileChooser fc = new JFileChooser(FileUtil.getUserFilesPath());
        fc.addChoosableFileFilter(new TextFilter());

        File fs = new File("NCE macro backup.txt");
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
            if (JOptionPane.showConfirmDialog(null, "File "
                    + f.getName() + " already exists, overwrite it?",
                    "Overwrite file?", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
        }

        try (PrintWriter fileOut = new PrintWriter(new BufferedWriter(new FileWriter(f)), true)) {
            if (JOptionPane.showConfirmDialog(null,
                    "Backup can take over a minute, continue?", "NCE Macro Backup",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                fileOut.close();
                return;
            }     

            // create a status frame
            JPanel ps = new JPanel();
            jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Macro Backup");
            fstatus.setLocationRelativeTo(null);
            fstatus.setSize(200, 100);
            fstatus.getContentPane().add(ps);

            ps.add(textMacro);
            ps.add(macroNumber);

            textMacro.setText("Macro number:");
            textMacro.setVisible(true);
            macroNumber.setVisible(true);

            // now read NCE CS macro memory and write to file
            waiting = 0;   // reset in case there was a previous error
            fileValid = true;  // assume we're going to succeed

            for (int macroNum = 0; macroNum < NUM_MACRO; macroNum++) {

                macroNumber.setText(Integer.toString(macroNum));
                fstatus.setVisible(true);

                getNceMacro(macroNum);

                if (!fileValid) {
                    macroNum = NUM_MACRO;  // break out of for loop
                }
                if (fileValid) {
                    StringBuilder buf = new StringBuilder();
                    buf.append(":").append(Integer.toHexString(CS_MACRO_MEM + (macroNum * MACRO_LNTH)));

                    for (int i = 0; i < MACRO_LNTH; i++) {
                        buf.append(" ").append(StringUtil.twoHexFromInt(NCE_MACRO_DATA[i++]));
                        buf.append(StringUtil.twoHexFromInt(NCE_MACRO_DATA[i]));
                    }

                    log.debug("macro {}", buf);

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
                JOptionPane.showMessageDialog(null, "Successful Backup!",
                        "NCE Macro", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Backup failed", "NCE Macro",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            return;
        }

    }

    // Read 20 bytes of NCE CS memory
    private void getNceMacro(int mN) {

        NceMessage m = readMacroMemory(mN, false);
        tc.sendNceMessage(m, this);
        // wait for read to complete, flag determines if 1st or 2nd read
        if (!readWait()) {
            return;
        }

        NceMessage m2 = readMacroMemory(mN, true);
        tc.sendNceMessage(m2, this);
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

    // Reads 16 bytes of NCE macro memory, and adjusts for second read
    private NceMessage readMacroMemory(int macroNum, boolean second) {
        secondRead = second;   // set flag for receive
        int nceMacroAddr = (macroNum * MACRO_LNTH) + CS_MACRO_MEM;
        if (second) {
            nceMacroAddr += REPLY_16;  // adjust for second memory read
        }
        replyLen = REPLY_16;    // Expect 16 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_16);
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

        // first read 16 bytes, second read only 4 bytes needed
        int offset = 0;
        int numBytes = REPLY_16;
        if (secondRead) {
            offset = REPLY_16;
            numBytes = 4;
        }

        for (int i = 0; i < numBytes; i++) {
            NCE_MACRO_DATA[i + offset] = (byte) r.getElement(i);
        }
        waiting--;

        // wake up backup thread
        synchronized (this) {
            notify();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceMacroBackup.class);
}
