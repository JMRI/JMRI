package jmri.jmrix.nce.macro;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
 * Restores NCE Macros from a text file defined by NCE.
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
 * The restore routine checks that each line of the file begins with the
 * appropriate macro address.
 *
 * @author Dan Boudreau Copyright (C) 2007
 */
public class NceMacroRestore extends Thread implements jmri.jmrix.nce.NceListener {

    private static final int CS_MACRO_MEM = 0xC800; // start of NCE CS Macro memory
    private static final int MACRO_LNTH = 20;  // 20 bytes per macro
    private static final int REPLY_1 = 1;   // reply length of 1 byte expected
    private int replyLen = 0;    // expected byte length
    private int waiting = 0;     // to catch responses not intended for this module
    private boolean fileValid = false;  // used to flag status messages

    javax.swing.JLabel textMacro = new javax.swing.JLabel();
    javax.swing.JLabel macroNumber = new javax.swing.JLabel();

    private NceTrafficController tc = null;

    public NceMacroRestore(NceTrafficController t) {
        super();
        this.tc = t;
    }

    @Override
    public void run() {

        // Get file to read from
        JFileChooser fc = new JFileChooser(FileUtil.getUserFilesPath());
        fc.addChoosableFileFilter(new TextFilter());
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; // Canceled
        }
        if (fc.getSelectedFile() == null) {
            return; // Canceled
        }
        File f = fc.getSelectedFile();
        
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {

            // create a status frame
            JPanel ps = new JPanel();
            jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Macro Restore");
            fstatus.setLocationRelativeTo(null);
            fstatus.setSize(200, 100);
            fstatus.getContentPane().add(ps);

            ps.add(textMacro);
            ps.add(macroNumber);

            textMacro.setText("Macro number:");
            textMacro.setVisible(true);
            macroNumber.setVisible(true);

            // Now read the file and check the macro address
            waiting = 0;
            fileValid = false;     // in case we break out early
            int macroNum = 0;     // for user status messages
            int curMacro = CS_MACRO_MEM;  // load the start address of the NCE macro memory
            byte[] macroAccy = new byte[20];  // NCE Macro data
            String line;

            while (true) {
                try {
                    line = in.readLine();
                } catch (IOException e) {
                    break;
                }

                macroNumber.setText(Integer.toString(macroNum++));

                if (line == null) {    // while loop does not break out quick enough
                    log.error("NCE macro file terminator :0000 not found");
                    break;
                }
                if (log.isDebugEnabled()) {
                    log.debug("macro " + line);
                }
                // check that each line contains the NCE memory address of the macro
                String macroAddr = ":" + Integer.toHexString(curMacro);
                String[] macroLine = line.split(" ");

                // check for end of macro terminator
                if (macroLine[0].equalsIgnoreCase(":0000")) {
                    fileValid = true; // success!
                    break;
                }

                if (!macroAddr.equalsIgnoreCase(macroLine[0])) {
                    log.error("Restore file selected is not a vaild backup file");
                    log.error("Macro addr in restore file should be {} Macro addr read {}", macroAddr, macroLine[0]);
                    break;
                }

                // macro file found, give the user the choice to continue
                if (curMacro == CS_MACRO_MEM) {
                    if (JOptionPane
                            .showConfirmDialog(
                                    null,
                                    "Restore file found!  Restore can take over a minute, continue?",
                                    "NCE Macro Restore", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        break;
                    }
                }

                fstatus.setVisible(true);

                // now read the entire line from the file and create NCE messages
                for (int i = 0; i < 10; i++) {
                    int j = i << 1;    // i = word index, j = byte index

                    byte b[] = StringUtil.bytesFromHexString(macroLine[i + 1]);

                    macroAccy[j] = b[0];
                    macroAccy[j + 1] = b[1];
                }

                NceMessage m = writeNceMacroMemory(curMacro, macroAccy, false);
                tc.sendNceMessage(m, this);
                m = writeNceMacroMemory(curMacro, macroAccy, true);
                tc.sendNceMessage(m, this);

                curMacro += MACRO_LNTH;

                // wait for writes to NCE CS to complete
                if (waiting > 0) {
                    synchronized (this) {
                        try {
                            wait(20000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                }
                // failed
                if (waiting > 0) {
                    log.error("timeout waiting for reply");
                    break;
                }
            }

            in.close();
            
            // kill status panel
            fstatus.dispose();

            if (fileValid) {
                JOptionPane.showMessageDialog(null, "Successful Restore!",
                        "NCE Macro", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Restore failed. Check console for error messages. \r\n"
                        + "If operating at 19,200 baud, try 9600 baud.",
                        "NCE Macro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            return;
        }
    }

    // writes 20 bytes of NCE macro memory, and adjusts for second write
    private NceMessage writeNceMacroMemory(int curMacro, byte[] b,
            boolean second) {

        replyLen = REPLY_1; // Expect 1 byte response
        waiting++;
        byte[] bl;

        if (second) {
            // write next 4 bytes
            curMacro += 16; // adjust memory address for second memory write
            byte[] data = new byte[4];
            for (int i = 0; i < 4; i++) {
                data[i] = b[i + 16];
            }
            bl = NceBinaryCommand.accMemoryWrite4(curMacro, data);

        } else {
            // write first 16 bytes
            byte[] data = new byte[16];
            for (int i = 0; i < 16; i++) {
                data[i] = b[i];
            }
            bl = NceBinaryCommand.accMemoryWriteN(curMacro, data);
        }
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
        return m;
    }

    @Override
    public void message(NceMessage m) {
    } // ignore replies

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY")
    @Override
    public void reply(NceReply r) {
        if (log.isDebugEnabled()) {
            log.debug("waiting for " + waiting + " responses ");
        }
        if (waiting <= 0) {
            log.error("unexpected response");
            return;
        }
        waiting--;
        if (r.getNumDataElements() != replyLen) {
            log.error("reply length incorrect");
            return;
        }
        if (replyLen == REPLY_1) {
            // Looking for proper response
            int recChar = r.getElement(0);
            if (recChar != '!') {
                log.error("reply incorrect");
            }
        }

        // wake up restore thread
        if (waiting == 0) {
            synchronized (this) {
                notify();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceMacroRestore.class);

}
