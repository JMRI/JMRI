package jmri.jmrit.ctc.editor.code;

import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateEpilogue;
import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateProlog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CreateGUIObjectsXMLFile {
    private static final int START_OFFSET = 12;
    private static final int GIF_HORIZONTAL_SIZE = 65;

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not write anything if it fails.")
//  By doing this, it prevents me from accidentally accessing "_mCodeButtonHandlerDataArrayList" and screwing it up!
//  I just have to make sure that I don't reference "passedCodeButtonHandlerDataArrayList" anywhere else in the code!
    public static void writeGUIObjects(String directoryToCreateThemIn, OtherData otherData, ArrayList <CodeButtonHandlerData> passedCodeButtonHandlerDataArrayList) {
//  Create a DEEP COPY of the array list, so that we can sort it independently of the outside data:
//  In the process, reject any <= 0 entries, or one(s) that have been generated before, as they won't be generated:
        ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayListDeepCopy = new ArrayList <>();
        for (CodeButtonHandlerData codeButtonHandlerData : passedCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mGUIColumnNumber > 0 && codeButtonHandlerData._mGUIGeneratedAtLeastOnceAlready == false)
                codeButtonHandlerDataArrayListDeepCopy.add(codeButtonHandlerData.deepCopy());
            codeButtonHandlerData._mGUIGeneratedAtLeastOnceAlready = true;  // BUT mark it as being done already.
        }
//  Sort by columnm number:
        Collections.sort(codeButtonHandlerDataArrayListDeepCopy);
//  Reject any duplicate column numbers:
        if (codeButtonHandlerDataArrayListDeepCopy.size() > 1) {
            for (int index = codeButtonHandlerDataArrayListDeepCopy.size() - 2; index >= 0; index--) { // End -1 to first
                if (codeButtonHandlerDataArrayListDeepCopy.get(index)._mGUIColumnNumber == codeButtonHandlerDataArrayListDeepCopy.get(index+1)._mGUIColumnNumber) {
                    codeButtonHandlerDataArrayListDeepCopy.remove(index + 1);
                }
            }
        }

        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter(directoryToCreateThemIn + "GUIObjects.xml"));  // NOI18N
        } catch (IOException e) { return; }
        generateProlog(printWriter);
        printWriter.println("  <paneleditor class=\"jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml\" name=\"Panel \" x=\"857\" y=\"437\" height=\"437\" width=\"527\" editable=\"yes\" positionable=\"no\" showtooltips=\"yes\" controlling=\"yes\" hide=\"yes\" panelmenu=\"yes\" scrollable=\"both\" redBackground=\"255\" greenBackground=\"255\" blueBackground=\"255\">"); // NOI18N

//  Create "one of" objects:

        if (otherData._mGUIDesign_BuilderPlate) {
            generateBuilderPlate(printWriter);
        }

        int oneOfItemsBottomEdge;
        switch(otherData._mGUIDesign_VerticalSize) {
            default:
            case SMALL:
                oneOfItemsBottomEdge = 718;
                break;
            case MEDIUM:
                oneOfItemsBottomEdge = 850;
                break;
            case LARGE:
                oneOfItemsBottomEdge = 900;
                break;
        }

        if (otherData._mGUIDesign_AnalogClockEtc) {
            printWriter.println("    <fastclock x=\"26\" y=\"" + (oneOfItemsBottomEdge - 186) + "\" scale=\"1.0\" color=\"black\" class=\"jmri.jmrit.display.configurexml.AnalogClock2DisplayXml\" />");    // NOI18N
            generateToggle(96, oneOfItemsBottomEdge - 223, "ISCLOCKRUNNING", printWriter);  // NOI18N
            generateTextPositionableLabel(87, oneOfItemsBottomEdge - 244, Bundle.getMessage("CreateGUIObjectsXMLFileClockOn"), printWriter);    // NOI18N
        }
        if (otherData._mGUIDesign_FleetingToggleSwitch) {
            if (!ProjectsCommonSubs.isNullOrEmptyString(otherData._mFleetingToggleInternalSensor)) {
                generateToggle(226, oneOfItemsBottomEdge - 36, otherData._mFleetingToggleInternalSensor, printWriter);
                generateTextPositionableLabel(215, oneOfItemsBottomEdge - 54, Bundle.getMessage("CreateGUIObjectsXMLFileFleetingOn"), printWriter); // NOI18N
            }
        }
        if (otherData._mGUIDesign_ReloadCTCSystemButton) {
            if (!ProjectsCommonSubs.isNullOrEmptyString(otherData._mCTCDebugSystemReloadInternalSensor)) {
                generatePushButton(291, oneOfItemsBottomEdge - 36, otherData._mCTCDebugSystemReloadInternalSensor, printWriter);
                generateTextPositionableLabel(277, oneOfItemsBottomEdge - 54, Bundle.getMessage("CreateGUIObjectsXMLFileReloadCTC"), printWriter);  // NOI18N
            }
        }
        if (otherData._mGUIDesign_CTCDebugOnToggle) {
            if (!ProjectsCommonSubs.isNullOrEmptyString(otherData._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor)) {
                generateToggle(358, oneOfItemsBottomEdge - 36, otherData._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor, printWriter);
                generateTextPositionableLabel(339, oneOfItemsBottomEdge - 54, Bundle.getMessage("CreateGUIObjectsXMLFileCTCDebugOn"), printWriter); // NOI18N
            }
        }

//  Create all GUI objects:
        if (!codeButtonHandlerDataArrayListDeepCopy.isEmpty()) {
            generatePanel(0, 0, otherData._mGUIDesign_VerticalSize, "Panel-left", printWriter); // NOI18N
            int lastHorizontalPosition = START_OFFSET;        // Where we are now.
            int thisObjectHorizontalPosition = START_OFFSET;
            for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayListDeepCopy) {
                thisObjectHorizontalPosition = (codeButtonHandlerData._mGUIColumnNumber - 1) * GIF_HORIZONTAL_SIZE + START_OFFSET;
//  Put in possible blank panels between where we left off last and the next one:
                for ( ; lastHorizontalPosition < thisObjectHorizontalPosition; lastHorizontalPosition += GIF_HORIZONTAL_SIZE) {
                    generatePanel(lastHorizontalPosition, 0, otherData._mGUIDesign_VerticalSize, "Panel-blank", printWriter);   // NOI18N
                }
                lastHorizontalPosition = thisObjectHorizontalPosition + GIF_HORIZONTAL_SIZE;

//  Put appropriate type of panel in:
                boolean generateSwitch = (codeButtonHandlerData._mSWDI_Enabled || codeButtonHandlerData._mSWDL_Enabled);
                boolean generateSignal = (codeButtonHandlerData._mSIDI_Enabled || codeButtonHandlerData._mSIDL_Enabled);
//  4 possibilities: Blank again, switch, signal, switch and signal:
                String filename = "Panel-blank";  // Default if not next 3 below:
                if (generateSwitch && generateSignal) filename = "Panel-sw-sig";    // NOI18N
                else if (!generateSwitch && generateSignal) filename = "Panel-signal";  // NOI18N
                else if (generateSwitch && !generateSignal) filename = "Panel-switch";  // NOI18N
                generatePanel(thisObjectHorizontalPosition, 0, otherData._mGUIDesign_VerticalSize, filename, printWriter);
//  CB:
                if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mCodeButtonInternalSensor)) { // Always exists, but for safety:
                    if (codeButtonHandlerData._mCodeButtonDelayTime == 0) { // Only if real one is supposed to be present:
                        generatePushButton(thisObjectHorizontalPosition + 21, adjustCodeButtonYBySize(632, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mCodeButtonInternalSensor, printWriter);
                    }
                }
//  O.S. occupancy sensor:
                if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mOSSectionOccupiedExternalSensor)) {
                    generateSensorIndicator(thisObjectHorizontalPosition + 21, 78, codeButtonHandlerData._mOSSectionOccupiedExternalSensor, "Red", otherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink, printWriter);    // NOI18N
                }
                if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mOSSectionOccupiedExternalSensor2)) {
                    generateSensorIndicator(thisObjectHorizontalPosition + 21, 108, codeButtonHandlerData._mOSSectionOccupiedExternalSensor2, "Red", otherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink, printWriter);  // NOI18N
                }
//  SWDI:
                if (codeButtonHandlerData._mSWDI_Enabled) { // Switch Indicators:
                    int y = adjustSwitchItemsYBySize(340, otherData._mGUIDesign_VerticalSize);
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_NormalInternalSensor)) {
                        generateSensorIndicator(thisObjectHorizontalPosition + 4, y, codeButtonHandlerData._mSWDI_NormalInternalSensor, "green", false, printWriter);   // NOI18N
                    }
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_ReversedInternalSensor)) {
                        generateSensorIndicator(thisObjectHorizontalPosition + 38, y, codeButtonHandlerData._mSWDI_ReversedInternalSensor, "amber", false, printWriter);    // NOI18N
                    }
                    if (otherData._mGUIDesign_TurnoutsOnPanel) {
                        if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_ExternalTurnout)) {
                            switch (codeButtonHandlerData._mSWDI_GUITurnoutType) {
                                case TURNOUT:
                                    generateTurnoutIcon(thisObjectHorizontalPosition + 9, 80, codeButtonHandlerData._mSWDI_ExternalTurnout, codeButtonHandlerData._mSWDI_GUITurnoutLeftHand, printWriter);
                                    break;
                                case CROSSOVER:
                                    if (codeButtonHandlerData._mSWDI_GUITurnoutLeftHand == codeButtonHandlerData._mSWDI_GUICrossoverLeftHand) {
                                        generateTurnoutCrossoverIcon(thisObjectHorizontalPosition + 20, 40, codeButtonHandlerData._mSWDI_ExternalTurnout, false, codeButtonHandlerData._mSWDI_GUICrossoverLeftHand, printWriter);
                                    } else {
                                        generateTurnoutIcon(thisObjectHorizontalPosition + 9, 80, codeButtonHandlerData._mSWDI_ExternalTurnout, codeButtonHandlerData._mSWDI_GUITurnoutLeftHand, printWriter);
                                        generateTurnoutIcon(thisObjectHorizontalPosition + 9, 135, codeButtonHandlerData._mSWDI_ExternalTurnout, codeButtonHandlerData._mSWDI_GUICrossoverLeftHand, printWriter);
                                    }
                                    break;
                                case DOUBLE_CROSSOVER:
                                    generateTurnoutCrossoverIcon(thisObjectHorizontalPosition + 20, 80, codeButtonHandlerData._mSWDI_ExternalTurnout, true, codeButtonHandlerData._mSWDI_GUICrossoverLeftHand, printWriter);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
//  SWDL:
                if (codeButtonHandlerData._mSWDL_Enabled) { // Switch Lever:
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDL_InternalSensor)) {
                        generateTurnoutLever(thisObjectHorizontalPosition + 8, adjustSwitchItemsYBySize(379, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mSWDL_InternalSensor, printWriter);
                        generateTextPositionableLabelCentered(thisObjectHorizontalPosition + 32, adjustSwitchItemsYBySize(356, otherData._mGUIDesign_VerticalSize), Integer.toString(codeButtonHandlerData._mSwitchNumber), printWriter);
                    }
                }
//  SIDI:
                if (codeButtonHandlerData._mSIDI_Enabled) { // Signal Indicators:
                    int y = adjustSignalItemsYBySize(454, otherData._mGUIDesign_VerticalSize);
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_LeftInternalSensor)) {
                        generateSensorIndicator(thisObjectHorizontalPosition + 4, y, codeButtonHandlerData._mSIDI_LeftInternalSensor, "green", false, printWriter); // NOI18N
                    }
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_NormalInternalSensor)) { // Should always be present, but for safety:
                        generateSensorIndicator(thisObjectHorizontalPosition + 22, adjustSignalItemsYBySize(440, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mSIDI_NormalInternalSensor, "red", false, printWriter);
                    }
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_RightInternalSensor)) {
                        generateSensorIndicator(thisObjectHorizontalPosition + 38, y, codeButtonHandlerData._mSIDI_RightInternalSensor, "green", false, printWriter);   // NOI18N
                    }
                    if (otherData._mGUIDesign_SignalsOnPanel == OtherData.SIGNALS_ON_PANEL.ALL) {
                        ArrayList<String> signalsArrayListLR = ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList);
                        int x;
                        x = thisObjectHorizontalPosition + 10;
                        for (String signal : signalsArrayListLR) {
                            switch(otherData._mSignalSystemType) {
                                case SIGNALHEAD:
                                    generateSignalHead(x, 120, signal, false, printWriter);
                                    break;
                                case SIGNALMAST:
                                    generateSignalMast(x, 120, signal, false, printWriter);
                                    break;
                                default:
                                    break;
                            }
                            x -= 11;
                        }
                        ArrayList<String> signalsArrayListRL = ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList);
                        x = thisObjectHorizontalPosition + 20;
                        for (String signal : signalsArrayListRL) {
                            switch(otherData._mSignalSystemType) {
                                case SIGNALHEAD:
                                    generateSignalHead(x, 70, signal, true, printWriter);
                                    break;
                                case SIGNALMAST:
                                    generateSignalMast(x, 70, signal, true, printWriter);
                                    break;
                                default:
                                    break;
                            }
                            x += 11;
                        }
//  SpotBugs whines about "useless control flow", so I commented this out (not the comment on the next line):
                    } /*else if (otherData._mGUIDesign_SignalsOnPanel == OtherData.SIGNALS_ON_PANEL.GREEN_OFF) {  // Future someday, as of 10/30/18 user CANNOT select this!
                    }*/
                }
//  SIDL:
                if (codeButtonHandlerData._mSIDL_Enabled) { // Signal Lever:
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDL_NormalInternalSensor)) {
                        generateSignalLever(thisObjectHorizontalPosition + 8, adjustSignalItemsYBySize(492, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mSIDL_LeftInternalSensor, codeButtonHandlerData._mSIDL_NormalInternalSensor, codeButtonHandlerData._mSIDL_RightInternalSensor, printWriter);
                        generateTextPositionableLabelCentered(thisObjectHorizontalPosition + 32, adjustSignalItemsYBySize(470, otherData._mGUIDesign_VerticalSize), Integer.toString(codeButtonHandlerData._mSignalEtcNumber), printWriter);
                    }
                }
//  CO:
                if (codeButtonHandlerData._mCO_Enabled) { // Call On:
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mCO_CallOnToggleInternalSensor)) {
                        generateToggle(thisObjectHorizontalPosition + 21, adjustCallOnItemsYBySize(582, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mCO_CallOnToggleInternalSensor, printWriter);
                        generateTextPositionableLabel(thisObjectHorizontalPosition + 48, adjustCallOnItemsYBySize(590, otherData._mGUIDesign_VerticalSize), Bundle.getMessage("CreateGUIObjectsXMLFileCallOn"), printWriter);   // NOI18N
                    }
                }
//  TUL:
                if (codeButtonHandlerData._mTUL_Enabled) { // Turnout Locking:
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle)) {
                        generateToggle(thisObjectHorizontalPosition + 21, adjustLockedItemsYBySize(541, otherData._mGUIDesign_VerticalSize), codeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle, printWriter);
                        generateTextPositionableLabel(thisObjectHorizontalPosition + 48, adjustLockedItemsYBySize(536, otherData._mGUIDesign_VerticalSize), Bundle.getMessage("CreateGUIObjectsXMLFileLocal"), printWriter);    // NOI18N
                        generateTextPositionableLabel(thisObjectHorizontalPosition + 48, adjustLockedItemsYBySize(560, otherData._mGUIDesign_VerticalSize), Bundle.getMessage("CreateGUIObjectsXMLFileLocked"), printWriter);   // NOI18N
                    }
                    if (!ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator)) {
                        generateSensorIndicator(thisObjectHorizontalPosition + 22, 200, codeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator, "red", false, printWriter);  // NOI18N
                        generateTextPositionableLabel(thisObjectHorizontalPosition + 9, 230, Bundle.getMessage("CreateGUIObjectsXMLFileUnlocked"), printWriter);    // NOI18N
                    }
                }
                thisObjectHorizontalPosition += GIF_HORIZONTAL_SIZE;   // In case this is the LAST object, and we fall out of the loop to the next line:
            }

            for (int i = 0; i < otherData._mGUIDesign_NumberOfEmptyColumnsAtEnd; i++) {
                generatePanel(thisObjectHorizontalPosition, 0, otherData._mGUIDesign_VerticalSize, "Panel-blank", printWriter); // NOI18N
                thisObjectHorizontalPosition += GIF_HORIZONTAL_SIZE;
            }
            generatePanel(thisObjectHorizontalPosition, 0, otherData._mGUIDesign_VerticalSize, "Panel-right", printWriter); // NOI18N
        }

        printWriter.println("  </paneleditor>");    // NOI18N
        generateEpilogue(printWriter);
        printWriter.close();
    }

/*
    <positionablelabel x="55" y="323" level="3" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" icon="yes" class="jmri.jmrit.display.configurexml.PositionableLabelXml">
      <tooltip>Icon</tooltip>
      <icon url="program:resources/icons/USS/plate/base-plates/misc/USS-plate.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </icon>
    </positionablelabel>

*/
    private static void generateBuilderPlate(PrintWriter printWriter) {
        printWriter.println("    <positionablelabel x=\"55\" y=\"323\" level=\"3\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.PositionableLabelXml\">"); // NOI18N
        printWriter.println("      <tooltip>Icon</tooltip>");   // NOI18N
        printWriter.println("      <icon url=\"program:resources/icons/USS/plate/base-plates/misc/USS-plate.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </icon>");   // NOI18N
        printWriter.println("    </positionablelabel>");    // NOI18N
    }

/*  As of 4.13.4ish:
    <positionablelabel x="966" y="559" level="4" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" text="Lock" fontname="Dialog.plain" size="11" style="0" red="255" green="255" blue="255" hasBackground="no" justification="centre" class="jmri.jmrit.display.configurexml.PositionableLabelXml">
      <tooltip>Text Label</tooltip>
    </positionablelabel>
*/
    private static final String DIALOG_USED = "Dialog.plain";   // NOI18N
    private static void generateTextPositionableLabel(int x, int y, String text, PrintWriter printWriter) {
        printWriter.println("    <positionablelabel x=\"" + x + "\" y=\"" + y + "\" level=\"4\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" text=\"" + text + "\" fontname=\"" + DIALOG_USED + "\" size=\"11\" style=\"0\" red=\"255\" green=\"255\" blue=\"255\" hasBackground=\"no\" justification=\"centre\" class=\"jmri.jmrit.display.configurexml.PositionableLabelXml\">"); // NOI18N
        printWriter.println("      <tooltip>Text Label</tooltip>");         // NOI18N
        printWriter.println("    </positionablelabel>");        // NOI18N
    }
    private static void generateTextPositionableLabelCentered(int x, int y, String text, PrintWriter printWriter) {
        generateTextPositionableLabel(centerText(x, text), y, text, printWriter);
    }
    private static int centerText(int originalValue, String text) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        Font font = new Font(DIALOG_USED, Font.PLAIN, 11);     // Found Dialog.plain in the latest .xml files.  Probably the default
        int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
        return originalValue - (textwidth / 2);
    }

/*  As of 4.13.4ish:
    <positionablelabel x="0" y="0" level="3" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" icon="yes" class="jmri.jmrit.display.configurexml.PositionableLabelXml">
      <tooltip>Icon</tooltip>
      <icon url="program:resources/icons/USS/background/Panel-blank-9.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </icon>
    </positionablelabel>
*/
    private static void generatePanel(int x, int y, OtherData.VERTICAL_SIZE verticalSize, String resourceFilename, PrintWriter printWriter) {
//      I use other defaults here for both positionable and showtooltip = false:
        switch(verticalSize) {
            default:
            case SMALL:
                resourceFilename += "-7.gif";   // NOI18N
                break;
            case MEDIUM:
                resourceFilename += "-8.gif";   // NOI18N
                break;
            case LARGE:
                resourceFilename += "-9.gif";   // NOI18N
                break;
        }
        printWriter.println("    <positionablelabel x=\"" + x + "\" y=\"" + y + "\" level=\"1\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"false\" showtooltip=\"false\" editable=\"true\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.PositionableLabelXml\">");  // NOI18N
//      I don't use Tooltip, since it's false anyways...
        printWriter.println("      <icon url=\"program:resources/icons/USS/background/" + resourceFilename + "\" degrees=\"0\" scale=\"1.0\">");    // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </icon>");   // NOI18N
        printWriter.println("    </positionablelabel>");    // NOI18N
    }

/*  As of 4.13.4ish:
    <sensoricon sensor="IS3:SWNI" x="0" y="0" level="10" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" momentary="false" icon="yes" class="jmri.jmrit.display.configurexml.SensorIconXml">
      <tooltip>IS2:CB</tooltip>
      <active url="program:resources/icons/USS/sensor/green-on.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </active>
      <inactive url="program:resources/icons/USS/sensor/green-off.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inactive>
      <unknown url="program:resources/icons/USS/sensor/s-unknown.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </unknown>
      <inconsistent url="program:resources/icons/USS/sensor/s-inconsistent.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inconsistent>
      <iconmaps />
    </sensoricon>
*/
    private static void generateSensorIndicator(int x, int y, String sensor, String color, boolean unknownInconsistentRedBlink, PrintWriter printWriter) {
        String unknown = unknownInconsistentRedBlink ? "blink/red-b" : "s-unknown"; // NOI18N
        String inconsistent = unknownInconsistentRedBlink ? "blink/red-b" : "s-inconsistent";   // NOI18N
        printWriter.println("    <sensoricon sensor=\"" + sensor + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"10\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" momentary=\"false\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\">");   // NOI18N
        printWriter.println("      <tooltip>" + sensor + "</tooltip>"); // NOI18N
        printWriter.println("      <active url=\"program:resources/icons/USS/sensor/" + color + "-on.gif\" degrees=\"0\" scale=\"1.0\">");  // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </active>"); // NOI18N
        printWriter.println("      <inactive url=\"program:resources/icons/USS/sensor/" + color + "-off.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </inactive>");   // NOI18N
        printWriter.println("      <unknown url=\"program:resources/icons/USS/sensor/" + unknown + ".gif\" degrees=\"0\" scale=\"1.0\">");  // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </unknown>");    // NOI18N
        printWriter.println("      <inconsistent url=\"program:resources/icons/USS/sensor/" + inconsistent + ".gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </inconsistent>");   // NOI18N
        printWriter.println("      <iconmaps />");  // NOI18N
        printWriter.println("    </sensoricon>");   // NOI18N
    }

/*  As of 4.13.4ish:
Left:
    <turnouticon turnout="LT47" x="486" y="40" level="7" forcecontroloff="true" hidden="no" positionable="true" showtooltip="false" editable="true" tristate="false" momentary="false" directControl="false" class="jmri.jmrit.display.configurexml.TurnoutIconXml">
      <icons>
        <closed url="program:resources/icons/USS/track/turnout/left/west/os-l-w-closed.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </closed>
        <thrown url="program:resources/icons/USS/track/turnout/left/west/os-l-w-thrown.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </thrown>
        <unknown url="program:resources/icons/USS/track/turnout/left/west/os-l-w-unknown.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </unknown>
        <inconsistent url="program:resources/icons/USS/track/turnout/left/west/os-l-w-inconsistent.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </inconsistent>
      </icons>
      <iconmaps />
    </turnouticon>
Right:
        <closed url="program:resources/icons/USS/track/turnout/right/east/os-r-e-closed.gif" degrees="0" scale="1.0">
*/
    private static void generateTurnoutIcon(int x, int y, String turnout, boolean isTurnoutLeftHanded, PrintWriter printWriter) {
        String partialFilename = isTurnoutLeftHanded ? "left/west/os-l-w" : "right/east/os-r-e";    // NOI18N
        printWriter.println("    <turnouticon turnout=\"" + turnout + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"7\" forcecontroloff=\"true\" hidden=\"no\" positionable=\"true\" showtooltip=\"false\" editable=\"true\" tristate=\"false\" momentary=\"false\" directControl=\"false\" class=\"jmri.jmrit.display.configurexml.TurnoutIconXml\">");  // NOI18N
        printWriter.println("      <icons>");   // NOI18N
        printWriter.println("        <closed url=\"program:resources/icons/USS/track/turnout/" + partialFilename + "-closed.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </closed>");   // NOI18N
        printWriter.println("        <thrown url=\"program:resources/icons/USS/track/turnout/" + partialFilename + "-thrown.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </thrown>");   // NOI18N
        printWriter.println("        <unknown url=\"program:resources/icons/USS/track/turnout/" + partialFilename + "-unknown.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </unknown>");  // NOI18N
        printWriter.println("        <inconsistent url=\"program:resources/icons/USS/track/turnout/" + partialFilename + "-inconsistent.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </inconsistent>"); // NOI18N
        printWriter.println("      </icons>");  // NOI18N
        printWriter.println("      <iconmaps />");  // NOI18N
        printWriter.println("    </turnouticon>");  // NOI18N
    }

/*  As of 4.13.4ish:
    <turnouticon turnout="LT48" x="807" y="40" level="7" forcecontroloff="true" hidden="no" positionable="true" showtooltip="false" editable="true" tristate="false" momentary="false" directControl="false" class="jmri.jmrit.display.configurexml.TurnoutIconXml">
      <icons>
        <closed url="program:resources/icons/USS/track/crossover/left/os-l-sc-closed.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </closed>
        <thrown url="program:resources/icons/USS/track/crossover/left/os-l-sc-thrown.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </thrown>
        <unknown url="program:resources/icons/USS/track/crossover/left/os-l-sc-unknown.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </unknown>
        <inconsistent url="program:resources/icons/USS/track/crossover/left/os-l-sc-inconsistent.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </inconsistent>
      </icons>
      <iconmaps />
    </turnouticon>
*/
    private static void generateTurnoutCrossoverIcon(int x, int y, String turnout, boolean isDoubleCrossover, boolean isTurnoutLeftHanded, PrintWriter printWriter) {
        String partialFilename;
        if (isDoubleCrossover) { // No left or right
            partialFilename = "double/os-dc";   // NOI18N
        } else {
            partialFilename = isTurnoutLeftHanded ? "left/os-l-sc" : "right/os-r-sc";   // NOI18N
        }
        printWriter.println("    <turnouticon turnout=\"" + turnout + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"7\" forcecontroloff=\"true\" hidden=\"no\" positionable=\"true\" showtooltip=\"false\" editable=\"true\" tristate=\"false\" momentary=\"false\" directControl=\"false\" class=\"jmri.jmrit.display.configurexml.TurnoutIconXml\">");  // NOI18N
        printWriter.println("      <icons>");   // NOI18N
        printWriter.println("        <closed url=\"program:resources/icons/USS/track/crossover/" + partialFilename + "-closed.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </closed>");   // NOI18N
        printWriter.println("        <thrown url=\"program:resources/icons/USS/track/crossover/" + partialFilename + "-thrown.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </thrown>");   // NOI18N
        printWriter.println("        <unknown url=\"program:resources/icons/USS/track/crossover/" + partialFilename + "-unknown.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </unknown>");  // NOI18N
        printWriter.println("        <inconsistent url=\"program:resources/icons/USS/track/crossover/" + partialFilename + "-inconsistent.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println("        </inconsistent>"); // NOI18N
        printWriter.println("      </icons>");  // NOI18N
        printWriter.println("      <iconmaps />");  // NOI18N
        printWriter.println("    </turnouticon>");  // NOI18N
    }

/*
    <signalheadicon signalhead="LH441" x="645" y="31" level="9" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" clickmode="3" litmode="false" class="jmri.jmrit.display.configurexml.SignalHeadIconXml">
      <tooltip>LH441</tooltip>
      <icons>
        <held url="program:resources/icons/smallschematics/searchlights/left-held-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </held>
        <dark url="program:resources/icons/smallschematics/searchlights/left-dark-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </dark>
        <red url="program:resources/icons/smallschematics/searchlights/left-red-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </red>
        <yellow url="program:resources/icons/smallschematics/searchlights/left-yellow-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </yellow>
        <green url="program:resources/icons/smallschematics/searchlights/left-green-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </green>
        <flashred url="program:resources/icons/smallschematics/searchlights/left-flashred-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </flashred>
        <flashyellow url="program:resources/icons/smallschematics/searchlights/left-flashyellow-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </flashyellow>
        <flashgreen url="program:resources/icons/smallschematics/searchlights/left-flashgreen-short.gif" degrees="0" scale="1.0">
          <rotation>0</rotation>
        </flashgreen>
      </icons>
      <iconmaps />
    </signalheadicon>
*/
    private static void generateSignalHead(int x, int y, String signalHead, boolean isRightToLeft, PrintWriter printWriter) {
        String direction = isRightToLeft ? "left" : "right";    // NOI18N
        printWriter.println("    <signalheadicon signalhead=\"" + signalHead + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"9\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" clickmode=\"3\" litmode=\"false\" class=\"jmri.jmrit.display.configurexml.SignalHeadIconXml\">");   // NOI18N
        printWriter.println("      <tooltip>" + signalHead + "</tooltip>"); // NOI18N
        printWriter.println("      <icons>");   // NOI18N
        generateSignalBlock(direction, "held", printWriter);    // NOI18N
        generateSignalBlock(direction, "dark", printWriter);    // NOI18N
        generateSignalBlock(direction, "red", printWriter); // NOI18N
        generateSignalBlock(direction, "yellow", printWriter);  // NOI18N
        generateSignalBlock(direction, "green", printWriter);   // NOI18N
        generateSignalBlock(direction, "flashred", printWriter);    // NOI18N
        generateSignalBlock(direction, "flashyellow", printWriter); // NOI18N
        generateSignalBlock(direction, "flashgreen", printWriter);  // NOI18N
        printWriter.println("      </icons>");  // NOI18N
        printWriter.println("      <iconmaps />");  // NOI18N
        printWriter.println("    </signalheadicon>");   // NOI18N
    }
    private static void generateSignalBlock(String direction, String color, PrintWriter printWriter) {
        printWriter.println(generateSignalLineStart(direction, color));
        printWriter.println("          <rotation>0</rotation>");    // NOI18N
        printWriter.println(generateSignalLineEnd(color));
    }
    private static String generateSignalLineStart(String direction, String color) {
        return "        <" + color + " url=\"program:resources/icons/smallschematics/searchlights/" + direction + "-" + color + "-short.gif\" degrees=\"0\" scale=\"1.0\">";    // NOI18N
    }
    private static String generateSignalLineEnd(String color) {
        return "        </" + color + ">";
    }

/*
    <signalmasticon signalmast="SM-CS10ME" x="461" y="17" level="9" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" clickmode="0" litmode="false" degrees="0" scale="1.0" imageset="default" class="jmri.jmrit.display.configurexml.SignalMastIconXml">
      <tooltip>SM-CS10ME (LF$dsm:SW-1968:SL-2(722))</tooltip>
    </signalmasticon>
*/
    private static void generateSignalMast(int x, int y, String signalMast, boolean isRightToLeft, PrintWriter printWriter) {
        String degrees = isRightToLeft ? "180" : "0";   // NOI18N
        printWriter.println("    <signalmasticon signalmast=\"" + signalMast + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"9\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" clickmode=\"0\" litmode=\"false\" degrees=\"" + degrees + "\" scale=\"1.0\" imageset=\"default\" class=\"jmri.jmrit.display.configurexml.SignalMastIconXml\">");    // NOI18N
        printWriter.println("      <tooltip>" + signalMast + "</tooltip>"); // NOI18N
        printWriter.println("    </signalmasticon>");   // NOI18N
    }

/*  As of 4.13.4ish:
    <sensoricon sensor="IS27:LEVER" x="1826" y="310" level="10" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" momentary="false" icon="yes" class="jmri.jmrit.display.configurexml.SensorIconXml">
      <tooltip>IS27:LEVER</tooltip>
      <active url="program:resources/icons/USS/plate/levers/l-left.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </active>
      <inactive url="program:resources/icons/USS/plate/levers/l-right.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inactive>
      <unknown url="program:resources/icons/USS/plate/levers/l-unknown.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </unknown>
      <inconsistent url="program:resources/icons/USS/plate/levers/l-inconsistent.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inconsistent>
      <iconmaps />
    </sensoricon>
*/
    public static void generateTurnoutLever(int x, int y, String lever, PrintWriter printWriter) {
        printWriter.println("    <sensoricon sensor=\"" + lever + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"10\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" momentary=\"false\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\">");    // NOI18N
        printWriter.println("      <tooltip>" + lever + "</tooltip>");  // NOI18N
        printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/lever-left-wide.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </active>"); // NOI18N
        printWriter.println("      <inactive url=\"program:resources/icons/USS/plate/levers/lever-right-wide.gif\" degrees=\"0\" scale=\"1.0\">");  // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </inactive>");   // NOI18N
        printWriter.println("      <unknown url=\"program:resources/icons/USS/plate/levers/lever-unknown-wide.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </unknown>");    // NOI18N
        printWriter.println("      <inconsistent url=\"program:resources/icons/USS/plate/levers/lever-inconsistent-wide.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </inconsistent>");   // NOI18N
        printWriter.println("      <iconmaps />");  // NOI18N
        printWriter.println("    </sensoricon>");   // NOI18N
    }

/*  As of 4.13.4ish:
    <multisensoricon x="1826" y="423" level="10" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" updown="false" class="jmri.jmrit.display.configurexml.MultiSensorIconXml">
      <tooltip>IS28:LDGL,IS28:NGL,IS28:RDGL</tooltip>
      <active url="program:resources/icons/USS/plate/levers/l-left.gif" degrees="0" scale="1.0" sensor="IS28:LDGL">
        <rotation>0</rotation>
      </active>
      <active url="program:resources/icons/USS/plate/levers/l-vertical.gif" degrees="0" scale="1.0" sensor="IS28:NGL">
        <rotation>0</rotation>
      </active>
      <active url="program:resources/icons/USS/plate/levers/l-right.gif" degrees="0" scale="1.0" sensor="IS28:RDGL">
        <rotation>0</rotation>
      </active>
      <inactive url="program:resources/icons/USS/plate/levers/l-inactive.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inactive>
      <unknown url="program:resources/icons/USS/plate/levers/l-unknown.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </unknown>
      <inconsistent url="program:resources/icons/USS/plate/levers/l-inconsistent.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inconsistent>
    </multisensoricon>
*/
    public static void generateSignalLever(int x, int y, String left, String vertical, String right, PrintWriter printWriter ) {
        printWriter.println("    <multisensoricon x=\"" + x + "\" y=\"" + y + "\" level=\"10\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" updown=\"false\" class=\"jmri.jmrit.display.configurexml.MultiSensorIconXml\">");   // NOI18N
        printWriter.println("      <tooltip>" + left + "," + vertical + "," + right + "</tooltip>");// NOI18N
        if (!left.trim().isEmpty()) {
            printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/lever-left-wide.gif\" degrees=\"0\" scale=\"1.0\" sensor=\"" + left + "\">");// NOI18N
            printWriter.println("        <rotation>0</rotation>");  // NOI18N
            printWriter.println("      </active>"); // NOI18N
        }
        printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/lever-vertical-wide.gif\" degrees=\"0\" scale=\"1.0\" sensor=\"" + vertical + "\">"); // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </active>"); // NOI18N
        if (!right.trim().isEmpty()) {
            printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/lever-right-wide.gif\" degrees=\"0\" scale=\"1.0\" sensor=\"" + right + "\">");// NOI18N
            printWriter.println("        <rotation>0</rotation>");// NOI18N
            printWriter.println("      </active>");// NOI18N
        }
        printWriter.println("      <inactive url=\"program:resources/icons/USS/plate/levers/lever-inactive-wide.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inactive>");// NOI18N
        printWriter.println("      <unknown url=\"program:resources/icons/USS/plate/levers/lever-unknown-wide.gif\" degrees=\"0\" scale=\"1.0\">"); // NOI18N
        printWriter.println("        <rotation>0</rotation>");  // NOI18N
        printWriter.println("      </unknown>");    // NOI18N
        printWriter.println("      <inconsistent url=\"program:resources/icons/USS/plate/levers/lever-inconsistent-wide.gif\" degrees=\"0\" scale=\"1.0\">");   // NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inconsistent>");// NOI18N
        printWriter.println("    </multisensoricon>");// NOI18N
    }

/*  As of 4.13.4ish:
    <sensoricon sensor="IS58:LOCKTOGGLE" x="3063" y="551" level="10" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" momentary="false" icon="yes" class="jmri.jmrit.display.configurexml.SensorIconXml">
      <tooltip>IS:SAV_PDC_LOCKLEVER</tooltip>
      <active url="program:resources/icons/USS/plate/levers/switch-on.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </active>
      <inactive url="program:resources/icons/USS/plate/levers/switch-off.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inactive>
      <unknown url="program:resources/icons/USS/plate/levers/switch-unknown.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </unknown>
      <inconsistent url="program:resources/icons/USS/plate/levers/switch-inconsistent.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inconsistent>
      <iconmaps />
    </sensoricon>
*/
    public static void generateToggle(int x, int y, String sensor, PrintWriter printWriter) {
        printWriter.println("    <sensoricon sensor=\"" + sensor + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"10\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" momentary=\"false\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\">");   // NOI18N
        printWriter.println("      <tooltip>" + sensor + "</tooltip>");// NOI18N
        printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/switch-on.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </active>");// NOI18N
        printWriter.println("      <inactive url=\"program:resources/icons/USS/plate/levers/switch-off.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inactive>");// NOI18N
        printWriter.println("      <unknown url=\"program:resources/icons/USS/plate/levers/switch-unknown.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </unknown>");// NOI18N
        printWriter.println("      <inconsistent url=\"program:resources/icons/USS/plate/levers/switch-inconsistent.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inconsistent>");// NOI18N
        printWriter.println("      <iconmaps />");// NOI18N
        printWriter.println("    </sensoricon>");// NOI18N
    }

/*  As of 4.13.4ish:
    <sensoricon sensor="IS80:CB" x="3388" y="632" level="10" forcecontroloff="false" hidden="no" positionable="true" showtooltip="true" editable="true" momentary="true" icon="yes" class="jmri.jmrit.display.configurexml.SensorIconXml">
      <tooltip>IS80:CB</tooltip>
      <active url="program:resources/icons/USS/plate/levers/code-press.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </active>
      <inactive url="program:resources/icons/USS/plate/levers/code.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inactive>
      <unknown url="program:resources/icons/USS/plate/levers/code-unknown.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </unknown>
      <inconsistent url="program:resources/icons/USS/plate/levers/code-inconsistent.gif" degrees="0" scale="1.0">
        <rotation>0</rotation>
      </inconsistent>
      <iconmaps />
    </sensoricon>
*/
    public static void generatePushButton(int x, int y, String sensor, PrintWriter printWriter) {
        printWriter.println("    <sensoricon sensor=\"" + sensor + "\" x=\"" + x + "\" y=\"" + y + "\" level=\"10\" forcecontroloff=\"false\" hidden=\"no\" positionable=\"true\" showtooltip=\"true\" editable=\"true\" momentary=\"true\" icon=\"yes\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\">");// NOI18N
        printWriter.println("      <tooltip>" + sensor + "</tooltip>");// NOI18N
        printWriter.println("      <active url=\"program:resources/icons/USS/plate/levers/code-press.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </active>");// NOI18N
        printWriter.println("      <inactive url=\"program:resources/icons/USS/plate/levers/code.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inactive>");// NOI18N
        printWriter.println("      <unknown url=\"program:resources/icons/USS/plate/levers/code-unknown.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </unknown>");// NOI18N
        printWriter.println("      <inconsistent url=\"program:resources/icons/USS/plate/levers/code-inconsistent.gif\" degrees=\"0\" scale=\"1.0\">");// NOI18N
        printWriter.println("        <rotation>0</rotation>");// NOI18N
        printWriter.println("      </inconsistent>");// NOI18N
        printWriter.println("      <iconmaps />");// NOI18N
        printWriter.println("    </sensoricon>");// NOI18N
    }

    private static int adjustSwitchItemsYBySize(int y, OtherData.VERTICAL_SIZE verticalSize) {
        switch(verticalSize) {
            default:
            case SMALL:
                return y;
            case MEDIUM:
                return y + 6;
            case LARGE:
                return y + 18;
        }
    }

    private static int adjustSignalItemsYBySize(int y, OtherData.VERTICAL_SIZE verticalSize) {
        switch(verticalSize) {
            default:
            case SMALL:
                return y;
            case MEDIUM:
                return y + 12;
            case LARGE:
                return y + 33;
        }
    }

    private static int adjustLockedItemsYBySize(int y, OtherData.VERTICAL_SIZE verticalSize) {
        switch(verticalSize) {
            default:
            case SMALL:
                return y;
            case MEDIUM:
                return y + 38;
            case LARGE:
                return y + 82;
        }
    }

    private static int adjustCallOnItemsYBySize(int y, OtherData.VERTICAL_SIZE verticalSize) {
        switch(verticalSize) {
            default:
            case SMALL:
                return y;
            case MEDIUM:
                return y + 85;
            case LARGE:
                return y + 134;
        }
    }

    private static int adjustCodeButtonYBySize(int y, OtherData.VERTICAL_SIZE verticalSize) {
        switch(verticalSize) {
            default:
            case SMALL:
                return y;
            case MEDIUM:
                return y + 120;
            case LARGE:
                return y + 180;
        }
    }
}
