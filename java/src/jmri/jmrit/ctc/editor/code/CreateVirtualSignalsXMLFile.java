package code;

import static code.CreateXMLFiles.generateEpilogue;
import static code.CreateXMLFiles.generateProlog;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import jmri.jmrit.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctcserialdata.OtherData;
import jmri.jmrit.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CreateVirtualSignalsXMLFile {
    public static void writeVirtualSignals(String directoryToCreateThemIn, OtherData otherData, ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter(directoryToCreateThemIn + "VirtualSignals.xml"));
        } catch (IOException e) { return; }
        generateProlog(printWriter);
        printWriter.println("  <signalheads class=\"jmri.managers.configurexml.AbstractSignalHeadManagerXml\">");
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
        printWriter.println("  </signalheads>");
        generateEpilogue(printWriter);
        printWriter.close();
    }
    
    private static void generateSignalHead(String signalHead, PrintWriter printWriter) {
        printWriter.println("    <signalhead class=\"jmri.implementation.configurexml.VirtualSignalHeadXml\">");
        printWriter.println("      <systemName>" + signalHead + "</systemName>");
        printWriter.println("    </signalhead>");
    }
}
