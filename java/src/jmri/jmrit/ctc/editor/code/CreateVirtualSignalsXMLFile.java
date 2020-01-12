package jmri.jmrit.ctc.editor.code;

import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateEpilogue;
import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateProlog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CreateVirtualSignalsXMLFile {
    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not write anything if it fails.")
    public static void writeVirtualSignals(String directoryToCreateThemIn, OtherData otherData, ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter(directoryToCreateThemIn + "VirtualSignals.xml"));  // NOI18N
        } catch (IOException e) { return; }
        generateProlog(printWriter);
        printWriter.println("  <signalheads class=\"jmri.managers.configurexml.AbstractSignalHeadManagerXml\">");   // NOI18N
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mSIDI_Enabled) { // Signal Indicators:
                if (otherData._mGUIDesign_SignalsOnPanel == OtherData.SIGNALS_ON_PANEL.ALL) {
                    ArrayList<String> signalsArrayListLR = ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList);
                    for (String signalHead : signalsArrayListLR) {
                        generateSignalHead(signalHead, printWriter);
                    }
                    ArrayList<String> signalsArrayListRL = ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList);
                    for (String signalHead : signalsArrayListRL) {
                        generateSignalHead(signalHead, printWriter);
                    }
                }
            }
        }
        printWriter.println("  </signalheads>");    // NOI18N
        generateEpilogue(printWriter);
        printWriter.close();
    }
    
    private static void generateSignalHead(String signalHead, PrintWriter printWriter) {
        printWriter.println("    <signalhead class=\"jmri.implementation.configurexml.VirtualSignalHeadXml\">");    // NOI18N
        printWriter.println("      <systemName>" + signalHead + "</systemName>");   // NOI18N
        printWriter.println("    </signalhead>");   // NOI18N
    }
}
