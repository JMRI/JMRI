from java.awt import GridLayout
from javax.swing import BoxLayout
from javax.swing import ButtonGroup
from javax.swing import JButton
from javax.swing import JCheckBox
from javax.swing import JComboBox
from javax.swing import JFileChooser
from javax.swing import JLabel
from javax.swing import JOptionPane
from javax.swing import JPanel
from javax.swing import JRadioButton
from javax.swing import JScrollPane
from javax.swing import JTextArea
from javax.swing import JTextField
from javax.swing.filechooser import FileFilter
from jmri import SignalHead
from AD2UIUtil import *

# USER INTERFACE CLASSES ============== Long and boring :-)

    # Main window =================
    
class ADspeedsFrame (AdScrollFrame):
    def __init__(self):
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Speed Levels",
                               AutoDispatcher.centerLabel(
                               "List of supported speed levels (minimum speed listed first)"))

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Name"))
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(ADsettings.speedsList), 2))
        if self.firstTime:
            self.speedNamesSwing = []
            self.speedDefaultSwing = []
            self.speedGroup = ButtonGroup()
        ind = 0
        for s in ADsettings.speedsList:
            if self.firstTime:
                self.speedNamesSwing.append(JTextField(s, 20))
            self.detail.add(self.speedNamesSwing[ind])
            if ind == 0:
                self.detail.add(JLabel(""))
            else:
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                self.detail.add(deleteButton)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Speeds window =================

    # define what Cancel button in Speeds Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.speedsFrame = None

    # define what Add button in Speeds Window does when clicked
    def whenAddClicked(self, event):
        ADsettings.speedsList.append("")
        self.speedNamesSwing.append(JTextField("", 20))
        radioButton = JRadioButton("Default speed")
        self.speedDefaultSwing.append(radioButton)
        self.speedGroup.add(radioButton)
        self.speedsChanged()

    # define what Delete button in Speeds Window does when clicked
    def whenDeleteClicked(self, event):
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove speed level \""
            + ADsettings.speedsList[ind] + "\"?", "Confirmation",
            JOptionPane.YES_NO_OPTION) == 1):
            return
        ADsettings.speedsList.pop(ind)
        self.speedsChanged()

    # define what Apply button in Speeds Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0
        for s in ADsettings.speedsList:
            ADsettings.speedsList[ind] = self.speedNamesSwing[ind].text
            ind += 1 
        self.speedsChanged()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Speed names changed")

    def speedsChanged(self):
        if AutoDispatcher.blocksFrame != None:
            AutoDispatcher.blocksFrame.reDisplay()
        if AutoDispatcher.indicationsFrame != None:
            AutoDispatcher.indicationsFrame.reDisplay()
        if AutoDispatcher.signalEditFrame != None:
            AutoDispatcher.signalEditFrame.reDisplay()
        if AutoDispatcher.locosFrame != None:
            AutoDispatcher.locosFrame.reDisplay()
        if AutoDispatcher.trainDetailFrame != None:
            AutoDispatcher.trainDetailFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()
    
    # Indications window =================
    
class ADindicationsFrame (AdScrollFrame):
    def __init__(self):
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal Indications",
                               AutoDispatcher.centerLabel("List of supported signal indications"))

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 5))
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Indication"))
        header1.add(AutoDispatcher.centerLabel("Next section"))
        header1.add(AutoDispatcher.centerLabel("Turnouts ahead"))
        self.header.add(header1)
        self.header.add(AutoDispatcher.centerLabel("Next signal indication"))
        self.header.add(AutoDispatcher.centerLabel("Speed"))
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        indicationNames = ["-"]
        for a in ADsettings.indicationsList:
            indicationNames.append(a.name)
        speedNames = []
        for s in ADsettings.speedsList:
            speedNames.append(s)
        maxSpeeds = len(speedNames)
        self.detail.setLayout(GridLayout(len(ADsettings.indicationsList), 5))
        ind = 0
        for a in ADsettings.indicationsList:
            self.detail.add(a.nameSwing)
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, 2))
            if ind == 0:
                temppane1.add(AutoDispatcher.centerLabel("Occupied"))
                temppane1.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(temppane1)
                self.detail.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(AutoDispatcher.centerLabel("Stop"))
                self.detail.add(JLabel(""))
            elif ind == 1:
                temppane1.add(AutoDispatcher.centerLabel("Available"))
                temppane1.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(temppane1)
                self.detail.add(AutoDispatcher.centerLabel("-"))
                a.speedSwing = JComboBox(speedNames)
                if a.speed > maxSpeeds:
                    a.speedSwing.setSelectedIndex(maxSpeeds-1)
                else:
                    a.speedSwing.setSelectedIndex(a.speed-1)
                self.detail.add(a.speedSwing)
                self.detail.add(JLabel(""))
            else:
                temppane1.add(AutoDispatcher.centerLabel("Available"))
                temppane1.add(a.nextTurnoutSwing)
                self.detail.add(temppane1)
                a.nextIndicationSwing = JComboBox(indicationNames)
                a.nextIndicationSwing.setSelectedIndex(a.nextIndication + 1)
                self.detail.add(a.nextIndicationSwing)
                a.speedSwing = JComboBox(speedNames)
                if a.speed > maxSpeeds:
                    a.speedSwing.setSelectedIndex(maxSpeeds-1)
                else:
                    a.speedSwing.setSelectedIndex(a.speed-1)
                self.detail.add(a.speedSwing)
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteIndicationClicked
                self.detail.add(deleteButton)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Indications window =================

    # define what Cancel button in Indications Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.indicationsFrame = None

    # define what Add button in Indications Window does when clicked
    def whenAddClicked(self, event):
        ADsettings.indicationsList.append(ADindication("New indication", -1, -1,
                                          len(ADsettings.speedsList)))
        ADsignalType.adjust()
        self.indicationsChanged()

    # define what Delete button in Indications Window does when clicked
    def whenDeleteIndicationClicked(self, event):
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove signal indication \""
            + ADsettings.indicationsList[ind].name + "\"?", "Confirmation",
            JOptionPane.YES_NO_OPTION) == 1):
            return
        ADsettings.indicationsList.pop(ind)
        for a in ADsettings.indicationsList:
            if a.nextIndication == ind:
                a.nextIndication = -1
            elif a.nextIndication > ind:
                a.nextIndication -= 1
        AutoDispatcher.setPreferencesDirty()
        self.indicationsChanged()

    # define what Apply button in Indications Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0
        for a in ADsettings.indicationsList:
            a.name = a.nameSwing.text
            if ind > 0:
                a.speed = a.speedSwing.getSelectedIndex() + 1
                if ind > 1:
                    a.nextIndication = a.nextIndicationSwing.getSelectedIndex()-1
                    a.nextTurnout = a.nextTurnoutSwing.getSelectedIndex()-1
            ind += 1 
        self.indicationsChanged()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Signal indications changed")

    def indicationsChanged(self):
        if AutoDispatcher.signalEditFrame != None:
            AutoDispatcher.signalEditFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Signal Types window =================
    
class ADsignalTypesFrame (AdScrollFrame):
    def __init__(self):
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal Types",
                               AutoDispatcher.centerLabel("List of supported signal types"))

    def createHeader(self):
        # Fill contents of Header
        self.header = None
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(ADsettings.signalTypes), 2))
        ind = 0
        for s in ADsettings.signalTypes:
            self.detail.add(JLabel(s.name))
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, 4))
            if s.headsNumber == 1:
                temppane1.add(AutoDispatcher.centerLabel("1 Head"))
            else:
                temppane1.add(AutoDispatcher.centerLabel(str(s.headsNumber)
                              + " Heads"))
            editButton = JButton("Edit")
            editButton.setActionCommand(str(ind))
            editButton.actionPerformed = self.whenEditClicked
            temppane1.add(editButton)
            duplicateButton = JButton("Duplicate")
            duplicateButton.setActionCommand(str(ind))
            duplicateButton.actionPerformed = self.whenDuplicateClicked
            temppane1.add(duplicateButton)
            if ind == 0:
                temppane1.add(JLabel(""))
            else:
                if s.inUse > 0:
                    temppane1.add(AutoDispatcher.centerLabel("In use"))
                else:
                    deleteButton = JButton("Delete")
                    deleteButton.setActionCommand(str(ind))
                    deleteButton.actionPerformed = self.whenDeleteClicked
                    temppane1.add(deleteButton)
            self.detail.add(temppane1)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

    # Buttons of Signal Types window =================

    # define what Cancel button in Signal Types Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.signalTypesFrame = None
        if AutoDispatcher.signalEditFrame != None:
            AutoDispatcher.signalEditFrame.dispose()
            AutoDispatcher.signalEditFrame = None
                        
    # define what Edit button in Signal Types Window does when clicked
    def whenEditClicked(self, event):
        ind = int(event.getActionCommand())
        if AutoDispatcher.signalEditFrame != None:
            AutoDispatcher.signalEditFrame.show()
            if (ADsettings.signalTypes[ind]
                == AutoDispatcher.signalEditFrame.editSignal):
                return
            if (JOptionPane.showConfirmDialog(None,
                "Save changes to signal type \""
                + AutoDispatcher.signalEditFrame.editSignal.name
                + "\" before editing signal type \""
                + ADsettings.signalTypes[ind].name
                + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) != 1):
                AutoDispatcher.signalEditFrame.whenApplyClicked(None)
            AutoDispatcher.signalEditFrame.dispose()
        AutoDispatcher.signalEditFrame = (
                                          ADsignalEditFrame(ADsettings.signalTypes[ind]))

    # define what Add button in Signal Types Window does when clicked
    def whenAddClicked(self, event):
        ADsettings.signalTypes.append(ADsignalType(
                                      "New signal type", [], []))
        self.signalTypesChanged()

    # define what Delete button in Signal Types Window does when clicked
    def whenDeleteClicked(self, event):
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove signal type \""
            + ADsettings.signalTypes[ind].name
            + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1):
            return
        ADsettings.signalTypes.pop(ind)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Signal type \"" + ADsettings.signalTypes[ind].name + " removed")
        self.signalTypesChanged()

    # define what Duplicate button in Signal Types Window does when clicked
    def whenDuplicateClicked(self, event):
        ind = int(event.getActionCommand())
        ADsettings.signalTypes.append(
                                      ADsignalType(ADsettings.signalTypes[ind].name + 
                                      " copy", ADsettings.signalTypes[ind].aspects,
                                      ADsettings.signalTypes[ind].speeds))
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Signal type \"" + ADsettings.signalTypes[ind].name + " duplicated")
        self.signalTypesChanged()

    def signalTypesChanged(self):
        if AutoDispatcher.signalMastsFrame != None:
            AutoDispatcher.signalMastsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Signal Edit Window =================

class ADsignalEditFrame (AdScrollFrame):
    def __init__(self, signal):
        # Create and display Speeds window
        self.editSignal = signal
        first = JPanel()
        first.setLayout(BoxLayout(first, BoxLayout.X_AXIS))
        first.add(AutoDispatcher.centerLabel("Name: "))
        first.add(self.editSignal.nameSwing)
        first.add(AutoDispatcher.centerLabel("Heads: "))
        self.headsSwing = JComboBox(["1", "2", "3", "4", "5"])
        self.headsSwing.setSelectedIndex(self.editSignal.headsNumber -1)
        if self.editSignal == ADsettings.signalTypes[0]:
            self.headsSwing.enabled = False
        first.add(self.headsSwing)
        # super.init
        AdScrollFrame.__init__(self, "Edit signal type", first)

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 3 + self.editSignal.headsNumber))
        self.header.add(AutoDispatcher.centerLabel("Signal indication"))
        for i in range(self.editSignal.headsNumber):
            self.header.add(AutoDispatcher.centerLabel("Head" + str(i + 1)))
        self.header.add(AutoDispatcher.centerLabel("Speed"))
        self.header.add(AutoDispatcher.centerLabel("Override speed"))
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(self.editSignal.aspectsNumber, 3
                              + self.editSignal.headsNumber))
        speedNames = ["No"]
        for s in ADsettings.speedsList:
            speedNames.append(s)
        self.signalSpeedSwing = []
        self.signalAspectsSwing = []
        ind = 0
        headsAspects = AutoDispatcher.headsAspects.keys()
        headsAspects.sort()
        for a in self.editSignal.aspects:
            if ind >= len(ADsettings.indicationsList):
                # self.detail.add(JLabel("Not defined"))
                break
            # else :
            self.detail.add(JLabel(ADsettings.indicationsList[ind].name))
            aspectLine = []
            for aa in a:
                aspectSwing = JComboBox(headsAspects)
                aspectSwing.setSelectedItem(AutoDispatcher.inverseAspects[aa])
                aspectLine.append(aspectSwing)
                self.detail.add(aspectSwing)
            self.signalAspectsSwing.append(aspectLine)
            self.detail.add(AutoDispatcher.centerLabel(
                            ADsettings.getSpeedName(ADsettings.indicationsList[ind].speed)))
            speedSwing = JComboBox(speedNames)
            speedIndex = self.editSignal.speeds[ind] + 1
            if speedIndex >= len(speedNames):
                speedIndex = len(speedNames) -1
            speedSwing.setSelectedIndex(speedIndex)
            if ind == 0:
                speedSwing.enabled = False
            self.signalSpeedSwing.append(speedSwing)
            self.detail.add(speedSwing)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Signal Edit window =================

    # define what Cancel button in Signal Edit Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.signalEditFrame = None

    # define what Apply button in Signal Edit Window does when clicked
    def whenApplyClicked(self, event):
        if self.editSignal != None:
            self.editSignal.name = self.editSignal.nameSwing.text
            for i in range(self.editSignal.aspectsNumber):
                for j in range(self.editSignal.headsNumber):
                    self.editSignal.aspects[i][j] = AutoDispatcher.headsAspects[
                        self.signalAspectsSwing[i][j].getSelectedItem()]
                self.editSignal.speeds[i] = self.signalSpeedSwing[
                    i].getSelectedIndex()-1
            newHeads = self.headsSwing.getSelectedIndex() + 1
            if newHeads != self.editSignal.headsNumber:
                diff = newHeads - self.editSignal.headsNumber
                if diff > 0:
                    for i in range(diff):
                        self.editSignal.aspects[0].append(SignalHead.RED)
                    for i in range(len(self.editSignal.aspects) -1):
                        for j in range(diff):
                            self.editSignal.aspects[i + 1].append(
                                                                  SignalHead.GREEN)
                else:
                    for a in self.editSignal.aspects:
                        for i in range(-diff):
                            a.pop()
                self.editSignal.headsNumber = (
                                               self.headsSwing.getSelectedIndex() + 1)
            if AutoDispatcher.signalTypesFrame != None:
                AutoDispatcher.signalTypesFrame.reDisplay()
            if AutoDispatcher.signalMastsFrame != None:
                AutoDispatcher.signalMastsFrame.reDisplay()
            AutoDispatcher.setPreferencesDirty()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Signal type \"" + self.editSignal.name + " modified")
            self.reDisplay()

    # Signal Types window =================
    
class ADsignalMastsFrame (AdScrollFrame):
    def __init__(self):
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal masts", None)

    def createHeader(self):
        # Fill contents of Header
        if self.firstTime:
            names = ADsignalMast.getNames()
            names.sort()
            self.signals = []
            self.maxHeads = 1
            for name in names:
                s = ADsignalMast.getByName(name)
                # Shouldn't happen
                if s != None:
                    if s.headsNumber > self.maxHeads:
                        self.maxHeads = s.headsNumber
                    self.signals.append(s)
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Signal name"))
        self.header.add(AutoDispatcher.centerLabel("Signal type"))
        header1 = JPanel()
        header1.setLayout(GridLayout(1, self.maxHeads + 1))
        for i in range(self.maxHeads):
            header1.add(AutoDispatcher.centerLabel("Head" + str(i + 1)))
        header1.add(JLabel(""))
        self.header.add(header1)
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(self.signals), 3))
        self.namesSwing = []
        self.typesSwing = []
        self.headsSwing = []
        types = []
        for t in ADsettings.signalTypes:
            types.append(t.name)
        ind = 0
        for s in self.signals:
            nameSwing = JTextField(s.name, 20)
            self.detail.add(nameSwing)
            self.namesSwing.append(nameSwing)
            typeSwing = JComboBox(types)
            typeSwing.setSelectedItem(s.signalType.name)
            self.detail.add(typeSwing)
            self.typesSwing.append(typeSwing)
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, self.maxHeads))
            i = 0
            headsLineSwing = []
            for h in s.signalHeads:
                headSwing = JComboBox(AutoDispatcher.signalHeadNames)
                if i < self.maxHeads:
                    if h != None:
                        headSwing.setSelectedItem(h.name)
                    temppane1.add(headSwing)
                    headsLineSwing.append(headSwing)
                    i += 1
            self.headsSwing.append(headsLineSwing)
            while i < self.maxHeads:
                temppane1.add(JLabel(""))
                i += 1
            if s.inUse > 0:
                temppane1.add(AutoDispatcher.centerLabel("In use"))
            else:
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                temppane1.add(deleteButton)
            self.detail.add(temppane1)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Signal Types window =================

    # define what Cancel button in Signal Masts Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.signalMastsFrame = None
                        
    # define what Add button in Signal Masts Window does when clicked
    def whenAddClicked(self, event):
        newName = "New signal"
        i = 1
        while ADsignalMast.signalsList.has_key(newName):
            i += 1
            newName = "New signal " + str(i)
        self.signals.append(ADsignalMast.provideSignal(newName))
        self.reDisplay()

    # define what Apply button in Signal Masts Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0
        for s in self.signals:
            newName = self.namesSwing[ind].text
            if newName != s.name:
                if ADsignalMast.signalsList.has_key(newName):
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                            "Duplicate signal masts name \""
                                            + newName + "\" - ignored")
                    self.namesSwing[ind].text = s.name
                else:
                    s.name = newName
            typeName = self.typesSwing[ind].getSelectedItem()
            newType = s.signalType
            for t in ADsettings.signalTypes:
                if t.name == typeName:
                    newType = t
                    break
            if newType != s.signalType:
                s.signalType.changeUse(-1)
                while newType.headsNumber > s.headsNumber:
                    s.signalHeads.append(None)
                    s.headsNumber += 1
                s.signalType = newType
                s.signalType.changeUse(1)
                if newType.headsNumber > self.maxHeads:
                    self.maxHeads = newType.headsNumber
            i = 0
            for h in self.headsSwing[ind]:
                headName = h.getSelectedItem()
                if headName == "":
                    s.signalHeads[i] = None
                else:
                    s.signalHeads[i] = ADsignalHead(headName)
                i += 1
            ind += 1
        newDic = {}
        for s in ADsignalMast.getList():
            newDic[s.name] = s
        ADsignalMast.signalsList = newDic           
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Signal masts updated")
        self.signalMastsChanged()

    # define what Delete button in Signal Masts Window does when clicked
    def whenDeleteClicked(self, event):
        ind = int(event.getActionCommand())
        name = self.signals[ind].name
        if (JOptionPane.showConfirmDialog(None, "Remove signal mast \"" + name
            + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1):
            return
        removed = self.signals.pop(ind)
        removed.changeUse(-1)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Signal mast \"" + name + " removed")
        self.signalMastsChanged()
        
    def signalMastsChanged(self):
        if AutoDispatcher.signalTypesFrame != None:
            AutoDispatcher.signalTypesFrame.reDisplay()
        if AutoDispatcher.sectionsFrame != None:
            AutoDispatcher.sectionsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Sections Window =================
    
class ADsectionsFrame (AdScrollFrame):
    def __init__(self):
        # Create Sections window
        # super.init
        AdScrollFrame.__init__(self, "Sections", None)

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 8))
        self.header.add(AutoDispatcher.centerLabel("Section"))
        self.header.add(AutoDispatcher.centerLabel("One-Way"))
        self.header.add(AutoDispatcher.centerLabel("Transit-Only"))
        self.header.add(AutoDispatcher.centerLabel(
                        ADsettings.directionNames[0] + " signal"))
        self.header.add(AutoDispatcher.centerLabel(
                        ADsettings.directionNames[0] + " stop at beginning"))
        self.header.add(AutoDispatcher.centerLabel(
                        ADsettings.directionNames[1] + " signal"))
        self.header.add(AutoDispatcher.centerLabel(
                        ADsettings.directionNames[1] + " stop at beginning"))
        self.header.add(AutoDispatcher.centerLabel("Man. sensor"))
        
    def createDetail(self):
        # Fill contents of scroll area
        sections = ADsection.getSectionsTable()
        self.detail.setLayout(GridLayout(len(sections), 6))
        both = (ADsettings.directionNames[0] + "-"
                + ADsettings.directionNames[1])
        signalMastList = ADsignalMast.getNames()
        signalMastList.sort()
        signalPopUp = [""]
        for s in signalMastList:
            signalPopUp.append("s: " + s)
        for s in AutoDispatcher.signalHeadNames:
            if not s in signalMastList and s.strip() != "":
                signalPopUp.append("h: " + s)        
        for s in sections:
            self.detail.add(AutoDispatcher.centerLabel(s[0]))
            ss = ADsection.getByName(s[0])
            ss.oneWaySwing.removeAllItems()
            ss.oneWaySwing.addItem("")
            ss.oneWaySwing.addItem(ADsettings.directionNames[0])
            ss.oneWaySwing.addItem(ADsettings.directionNames[1])
            self.detail.add(ss.oneWaySwing)
            ss.oneWaySwing.setSelectedItem(s[1])
            ss.transitOnlySwing.removeAllItems()
            ss.transitOnlySwing.addItem("")
            ss.transitOnlySwing.addItem(both)
            ss.transitOnlySwing.addItem(both + "+")
            ss.transitOnlySwing.addItem(ADsettings.directionNames[0])
            ss.transitOnlySwing.addItem(ADsettings.directionNames[0]
                                        + "+")
            ss.transitOnlySwing.addItem(ADsettings.directionNames[1])
            ss.transitOnlySwing.addItem(ADsettings.directionNames[1]
                                        + "+")
            self.detail.add(ss.transitOnlySwing)
            ss.transitOnlySwing.setSelectedItem(s[2])
            j = ADsettings.ccw
            for k in range(2):
                ss.signalSwing[j] = JComboBox(signalPopUp)
                self.detail.add(ss.signalSwing[j])
                if ss.signal[j] != None:
                    ss.signalSwing[j].setSelectedItem("s: " + ss.signal[j].name)
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                if ss.stopAtBeginning[j] >= 0:
                    ss.stopAtBeginningSwing[j].selected = True
                    ss.stopAtBeginningDelay[j].text = str(ss.stopAtBeginning[j])
                else:
                    ss.stopAtBeginningSwing[j].selected = False
                    ss.stopAtBeginningDelay[j].text = "0.0"
                temppane.add(ss.stopAtBeginningSwing[j])
                temppane.add(ss.stopAtBeginningDelay[j])
                self.detail.add(temppane)
                j = 1-j
            ss.manualSensorSwing = JComboBox(ADsection.sensorNames)
            if s[6] in ADsection.sensorNames:
                ss.manualSensorSwing.setSelectedItem(s[6])
            self.detail.add(ss.manualSensorSwing)

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Sections window =================
    
    # define what Cancel button in Sections Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.sectionsFrame = None

    # define what Apply button in Sections Window does when clicked
    def whenApplyClicked(self, event):
        for s in ADsection.getList():
            s.updateFromSwing()
        if AutoDispatcher.signalMastsFrame != None:
            AutoDispatcher.signalMastsFrame.reDisplay()
        ADgridGroup.create()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Sections changes applied")

    # Blocks Window =================
    
class ADblocksFrame (AdScrollFrame):
    def __init__(self):
        # Create Blocks window
        # super.init
        AdScrollFrame.__init__(self, "Blocks", None)

    def createHeader(self):
        # Fill contents of Header
        # Retrieve section names
        self.sectionNames = ADsection.getNames()
        self.sectionNames.sort()
            
        # Check if the direction of any section can be reversed
        self.columns = 10
        self.canBeReversed = []
        self.inversionPoints = []
        for sectionName in self.sectionNames:
            section = ADsection.getByName(sectionName)
            canBe = False
            # Direction can be reversed only if all entry points
            # in one direction connect with sections in opposite direction
            # (i.e. the section is at the end of a reversing loop)
            invPoints = ["Inv. Points"]
            for direction in [False, True]:
                entries = section.getEntries(direction)
                if len(entries) > 0:
                    canBe1 = True
                    invPoints1 = []
                    for entry in entries:
                        if not entry.getDirectionChange():
                            canBe1 = False
                            break
                        invPoints1.append(entry.getExternalSection().getName())
                    if canBe1:
                        canBe = True
                        invPoints.extend(invPoints1)
            # Keep track of the condition
            self.canBeReversed.append(canBe)
            if canBe:
                self.columns = 11
            else:
                invPoints = []
            self.inversionPoints.append(invPoints)
                
        self.header.setLayout(GridLayout(2, self.columns))
        self.header.add(AutoDispatcher.centerLabel("Section"))
        self.header.add(AutoDispatcher.centerLabel("Block"))
        for i in range(2):
            for j in range(2):
                header1 = JPanel()
                header1.setLayout(GridLayout(1, 2))
                for k in range(2):
                    header1.add(AutoDispatcher.centerLabel(
                                ADsettings.directionNames[i]))
                self.header.add(header1)
            self.header.add(AutoDispatcher.centerLabel(
                            ADsettings.directionNames[i]))
            self.header.add(AutoDispatcher.centerLabel(
                            ADsettings.directionNames[i]))
        if self.columns > 10:
            self.header.add(JLabel(""))
        self.header.add(JLabel(""))
        self.header.add(JLabel(""))
        for i in range(2):
            header1 = JPanel()
            header1.setLayout(GridLayout(1, 2))
            header1.add(AutoDispatcher.centerLabel("alloc."))
            header1.add(AutoDispatcher.centerLabel("safe"))
            self.header.add(header1)
            header1 = JPanel()
            header1.setLayout(GridLayout(1, 2))
            header1.add(AutoDispatcher.centerLabel("stop"))
            header1.add(AutoDispatcher.centerLabel("brake"))
            self.header.add(header1)
            self.header.add(AutoDispatcher.centerLabel("speed"))
            self.header.add(AutoDispatcher.centerLabel("action"))

        if self.columns > 10:
            self.header.add(AutoDispatcher.centerLabel("Reverse"))

    def createDetail(self):
        # Fill contents of scroll area
        # Compute total number of lines
        linesNumber = len(self.sectionNames) * 2
        ind = 0
        for sectionName in self.sectionNames:
            s = ADsection.getByName(sectionName)
            lines = len(s.getBlocks(True))
            if lines < len(self.inversionPoints[ind]):
                lines = len(self.inversionPoints[ind])
            linesNumber += lines
            ind += 1
        self.detail.setLayout(GridLayout(linesNumber, self.columns))
        # Prepare list for speeds ComboBox
        speeds = [""]
        speeds.extend(ADsettings.speedsList)
        for sectionName in self.sectionNames:
            canBe = self.canBeReversed.pop(0)
            invPoints = self.inversionPoints.pop(0)
            section = ADsection.getByName(sectionName)
            for block in section.getBlocks(ADsettings.ccw):
                self.detail.add(AutoDispatcher.centerLabel(sectionName))
                self.detail.add(AutoDispatcher.centerLabel(block.getName()))
                j = ADsettings.ccw
                for k in range(2):
                    temppane = JPanel()
                    temppane.setLayout(GridLayout(1, 2))
                    block.allocationSwing[j].setSelected(block == (
                                                         section.allocationPoint[j]))
                    temppane.add(block.allocationSwing[j])
 
                    block.safeSwing[j].setSelected(block == (
                                                   section.safePoint[j]))
                    temppane.add(block.safeSwing[j])
                    self.detail.add(temppane)
                    
                    temppane = JPanel()
                    temppane.setLayout(GridLayout(1, 2))
                    block.stopSwing[j].setSelected(block == (
                                                   section.stopBlock[j]))
                    temppane.add(block.stopSwing[j])

                    block.brakeSwing[j].setSelected(block == (
                                                    section.brakeBlock[j]))
                    temppane.add(block.brakeSwing[j])
                    self.detail.add(temppane)

                    block.speedSwing[j] = JComboBox(speeds)
                    block.speedSwing[j].setSelectedIndex(block.getSpeed(j))
                    self.detail.add(block.speedSwing[j])
                    block.actionSwing[j].text = block.action[j]
                    self.detail.add(block.actionSwing[j])
                    
                    j = 1 - j
                if self.columns > 10:
                    if canBe and sectionName != "":
                        reverseButton = JButton("Reverse")
                        reverseButton.setActionCommand(sectionName)
                        reverseButton.actionPerformed = self.whenReverseClicked
                        self.detail.add(reverseButton)
                    else:
                        if len(invPoints) > 0:
                            self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                        else:
                            self.detail.add(JLabel(""))
                sectionName = ""
            # Add a "None" line
            self.detail.add(JLabel(""))
            self.detail.add(AutoDispatcher.centerLabel("None"))
            j = ADsettings.ccw
            for k in range(2):
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                temppane.add(JLabel(""))
                section.safeNoneSwing[j].setSelected(
                                                     section.safePoint[j] == None)
                temppane.add(section.safeNoneSwing[j])
                self.detail.add(temppane)
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                temppane.add(JLabel(""))
                section.brakeNoneSwing[j].setSelected(
                                                      section.brakeBlock[j] == None)
                temppane.add(section.brakeNoneSwing[j])
                self.detail.add(temppane)
                self.detail.add(JLabel(""))
                self.detail.add(JLabel(""))
                j = 1 - j
            if self.columns > 10:
                if len(invPoints) > 0:
                    self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                    while len(invPoints) > 0:
                        for i in range(self.columns - 1):
                            self.detail.add(JLabel(""))
                        self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                else:
                    self.detail.add(JLabel(""))
            # Add an empty line between sections
            for i in range(self.columns):
                self.detail.add(JLabel(""))

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Blocks window =================
    
    # define what Cancel button in Blocks Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.blocksFrame = None

    # define what Reverse button in Blocks Window does when clicked
    def whenReverseClicked(self, event):
        sectionName = event.getActionCommand()
        section = ADsection.getByName(sectionName)
        # Reverse blocks order
        section.manuallyFlip()
        # Adjust entry points
        AutoDispatcher.instance.findTransitPoints()
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Direction of section " + sectionName + " reversed")
        return

    # define what Apply button in Blocks Window does when clicked
    def whenApplyClicked(self, event):
        for section in ADsection.getList():
            section.safePoint[0] = section.safePoint[1] = None
            for block in section.getBlocks(True):
                for j in range(2):
                    if block.allocationSwing[j].isSelected():
                        section.allocationPoint[j] = block
                    if block.safeSwing[j].isSelected():
                        section.safePoint[j] = block
                    if block.stopSwing[j].isSelected():
                        section.stopBlock[j] = block
                    if block.brakeSwing[j].isSelected():
                        section.brakeBlock[j] = block
                    block.speed[j] = block.speedSwing[j].getSelectedIndex()
                    block.action[j] = block.actionSwing[j].text
            for j in range(2):
                if section.safeNoneSwing[j].isSelected():
                    section.safePoint[j] = None
                if section.brakeNoneSwing[j].isSelected():
                    section.brakeBlock[j] = None
        self.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Blocks changes applied")
          
    # Locations Window =================
    
class ADlocationsFrame (AdScrollFrame):
    def __init__(self):
        # Create Locations window
        # Retrieve location names
        # super.init
        AdScrollFrame.__init__(self, "Locations", JLabel("Define the list"
                               + " of sections corresponding to each Operations' location"))
        frameSize = self.getMinimumSize()
        if frameSize.width < 800:
            frameSize.width = 800
            self.setMinimumSize(frameSize)
            self.pack()
    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Location"))
        self.header.add(AutoDispatcher.centerLabel("Sections"))

    def createDetail(self):
        # Fill contents of scroll area
        # Compute total number of lines
        self.locationNames = ADlocation.getNames()
        self.locationNames.sort()
        self.detail.setLayout(GridLayout(len(self.locationNames), 2))
        self.valuesSwing = []
        for locationName in self.locationNames:
            location = ADlocation.getByName(locationName)
            if location.opLocation == None:
                locationName += " (Unknown)"
            self.detail.add(JLabel(locationName))
            value = location.text
            value = JTextField(value, 20)
            self.detail.add(value)
            self.valuesSwing.append(value)

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Locations window =================
    
    # define what Cancel button in Locations Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.locationsFrame = None

    # define what Apply button in Locations Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0 
        for locationName in self.locationNames:
            location = ADlocation.getByName(locationName)
            location.setSections(self.valuesSwing[ind].text)
            ind += 1
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Locations changes applied")

    # Preferences Window =================   
   
class ADpreferencesFrame (AdScrollFrame):
    def __init__(self):
        # Create Preferences window
        # super.init
        AdScrollFrame.__init__(self, "Preferences", None)

    def createHeader(self):
        self.header = None

    def createDetail(self):
        self.detail.setLayout(GridLayout(43, 2))
        self.detail.add(JLabel("COMMON SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Verbose output:"))
        self.verboseSwing = JCheckBox("", ADsettings.verbose)
        self.detail.add(self.verboseSwing)
        self.detail.add(JLabel("Ring bell for main events:"))
        self.ringBellSwing = JCheckBox("", ADsettings.ringBell)
        self.detail.add(self.ringBellSwing)
        self.detail.add(JLabel("Pause mode:"))
        self.pauseSwing = JComboBox(["Disabled", "Stop trains",
                                    "Emergency stop trains", "Turn power off"])
        self.pauseSwing.setSelectedIndex(ADsettings.pauseMode)
        self.detail.add(self.pauseSwing)
        self.detail.add(JLabel("Turnouts controlled by separate system: "))
        self.separateTurnoutsSwing = JCheckBox("",
                                               ADsettings.separateTurnouts)
        self.detail.add(self.separateTurnoutsSwing)
        self.detail.add(JLabel("Signals controlled by separate system: "))
        self.separateSignalsSwing = JCheckBox("",
                                              ADsettings.separateSignals)
        self.detail.add(self.separateSignalsSwing)
        self.detail.add(JLabel("Scale: "))
        self.scaleSwing = JTextField(str(ADsettings.scale), 5)
        temppane = JPanel()
        temppane.setLayout(BoxLayout(temppane, BoxLayout.X_AXIS))
        temppane.add(JLabel("1:"))
        temppane.add(self.scaleSwing)
        self.detail.add(temppane)
        self.detail.add(JLabel("Flashing cycle of signal icons (seconds):"))
        self.flashingSwing = JTextField(str(ADsettings.flashingCycle), 5)        
        self.detail.add(self.flashingSwing)
        self.detail.add(JLabel(""))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("DISPATCHER SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Derailed trains detection:"))
        self.derailedSwing = JComboBox(["Disabled", "Enabled: only warning",
                                       "Enabled: pause script"])
        self.derailedSwing.setSelectedIndex(ADsettings.derailDetection)
        self.detail.add(self.derailedSwing)
        self.detail.add(JLabel("Trains entering wrong route detection: "))
        self.wrongRouteSwing = JComboBox(["Disabled", "Enabled: only warning",
                                         "Enabled: pause script"])
        self.wrongRouteSwing.setSelectedIndex(
                                              ADsettings.wrongRouteDetection)
        self.detail.add(self.wrongRouteSwing)
        self.detail.add(JLabel("Stalled trains detection: "))
        self.stalledSwing = JComboBox(["Disabled", "Enabled: only warning",
                                      "Enabled: pause script"])
        self.stalledSwing.setSelectedIndex(ADsettings.stalledDetection)
        self.detail.add(self.stalledSwing)
        self.detail.add(JLabel(
                        "Maximum time required to travel a block (seconds): "))
        self.stalledTimeSwing = JTextField(
                                           str(float(ADsettings.stalledTime) / 1000.), 5)
        self.detail.add(self.stalledTimeSwing)
        self.detail.add(JLabel("Lost cars detection:"))
        self.lostCarsSwing = JComboBox(["Disabled", "Enabled: only warning",
                                       "Enabled: pause script"])
        self.lostCarsSwing.setSelectedIndex(ADsettings.lostCarsDetection)
        self.detail.add(self.lostCarsSwing)
        if ADsettings.units == 1.0:
            self.detail.add(JLabel("Tollerance for lost cars detection (mm.):"))
        elif ADsettings.units == 10.0:
            self.detail.add(JLabel("Tollerance for lost cars detection (cm.):"))
        else:
            self.detail.add(JLabel(
                            "Tolerance for lost cars detection (inches):"))
        self.lostLengthSwing = JTextField(
                                          str(ADsettings.lostCarsTollerance / ADsettings.units), 4)
        self.detail.add(self.lostLengthSwing)
        self.detail.add(JLabel(
                        "Maximum number of sections occupied by a train:"))
        self.lostMaxSwing = JTextField(
                                       str(ADsettings.lostCarsSections), 4)        
        self.detail.add(self.lostMaxSwing)        
        self.detail.add(JLabel(
                        "(Most) cars are equipped with resistive wheel-sets:"))        
        self.useResistiveSwing = JCheckBox("", ADsettings.resistiveDefault)
        self.detail.add(self.useResistiveSwing)
        self.detail.add(JLabel("Train length expressed in: "))
        self.unitsSwing = JComboBox(["mm.", "cm.", "inches"])
        if ADsettings.units == 1.0:
            self.unitsSwing.setSelectedIndex(0)
        elif ADsettings.units == 10.0:
            self.unitsSwing.setSelectedIndex(1)
        else:
            self.unitsSwing.setSelectedIndex(2)
        self.detail.add(self.unitsSwing)
        self.detail.add(JLabel("Release sections based on train lenght: "))
        self.useLengthSwing = JCheckBox("", ADsettings.useLength)
        self.detail.add(self.useLengthSwing)
        if ADblock.blocksWithLength == 0:
            self.useLengthSwing.setEnabled(False)
            self.detail.add(JLabel("  (Block lengths not defined!) "))
        elif ADblock.blocksWithoutLength == 0:
            self.detail.add(JLabel("  (Length defined for all blocks)"))
        else:            
            self.detail.add(JLabel("  (Length not defined for "
                            + str(ADblock.blocksWithoutLength) + " blocks!)"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Number of sections ahead to be allocated: "))
        self.aheadSwing = JComboBox(["1", "2", "3", "4", "5"])
        self.aheadSwing.setSelectedIndex(ADsettings.allocationAhead -1)
        self.detail.add(self.aheadSwing)
        self.detail.add(JLabel("Locomotives maintenance interval (hours): "))
        self.maintenanceSwing = JTextField(str(ADsettings.maintenanceTime), 5)
        self.detail.add(self.maintenanceSwing)
        if ADsettings.units == 25.4:
            self.detail.add(JLabel(
                            "Mileage maintenance interval (scale miles): "))
            multiplier = ADsettings.scale / 1609344.
        else:
            self.detail.add(JLabel(
                            "Mileage maintenance interval (scale Km.): "))
            multiplier = ADsettings.scale / 1000000.
        self.milesSwing = JTextField(str(round(ADsettings.maintenanceMiles *
                                     multiplier, 1)), 5)
        self.detail.add(self.milesSwing)

        self.detail.add(JLabel("Override JMRI block tracking:"))
        self.blockTrackingSwing = JCheckBox("", ADsettings.blockTracking)
        self.detail.add(self.blockTrackingSwing)
        self.detail.add(JLabel("Update JMRI sections state:"))
        self.sectionTrackingSwing = JCheckBox("",
                                              ADsettings.sectionTracking)
        self.detail.add(self.sectionTrackingSwing)
        self.detail.add(JLabel("Trust turnouts KnownState:"))
        self.trustTurnoutsSwing = JCheckBox("", ADsettings.trustTurnouts)
        self.detail.add(self.trustTurnoutsSwing)
        self.detail.add(JLabel("Delay between turnout commands (seconds): "))
        self.turnoutDelaySwing = JTextField(
                                            str(float(ADsettings.turnoutDelay) / 1000.), 5)
        self.detail.add(self.turnoutDelaySwing)
        self.detail.add(JLabel("Delay before clearing signals (seconds): "))
        self.clearDelaySwing = JTextField(
                                          str(float(ADsettings.clearDelay) / 1000.), 5)
        self.detail.add(self.clearDelaySwing)
        self.detail.add(JLabel("Trust signals KnownState:"))
        self.trustSignalsSwing = JCheckBox("", ADsettings.trustSignals)
        self.detail.add(self.trustSignalsSwing)
        self.detail.add(JLabel("Delay between signal commands (seconds): "))
        self.signalDelaySwing = JTextField(
                                           str(float(ADsettings.signalDelay) / 1000.), 5)
        self.detail.add(self.signalDelaySwing)
        self.detail.add(JLabel("Automatically restart trains at script startup:"))
        self.autoRestartSwing = JCheckBox("",
                                          ADsettings.autoRestart)
        self.detail.add(self.autoRestartSwing)
        self.detail.add(JLabel(""))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("ENGINEER SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("In front of red signals:"))
        self.stopModeSwing = JComboBox(["Progressively stop train", 
                                       "Immediately stop train"])
        self.stopModeSwing.setSelectedIndex(ADsettings.stopMode)
        self.detail.add(self.stopModeSwing)
        self.detail.add(JLabel(
                        "Delay between clear signal and train departure (seconds): "))
        self.detail1 = JPanel()
        self.startDelayMinSwing = JTextField(
                                             str(float(ADsettings.startDelayMin) / 1000.), 5)
        self.startDelayMaxSwing = JTextField(
                                             str(float(ADsettings.startDelayMax) / 1000.), 5)
        self.detail1.add(JLabel("Between "))
        self.detail1.add(self.startDelayMinSwing)
        self.detail1.add(JLabel(" and "))
        self.detail1.add(self.startDelayMaxSwing)
        self.detail.add(self.detail1)
        self.detail.add(JLabel("Default actions before train departure:"))
        self.defaultStartSwing = JTextField(ADsettings.defaultStartAction, 5)
        self.detail.add(self.defaultStartSwing)
        self.detail.add(JLabel("Switch headlights ON/OFF:"))
        self.lightsSwing = JComboBox(["Never", "When train starts/stops",
                                     "When schedule starts/ends"])
        self.lightsSwing.setSelectedIndex(ADsettings.lightMode)
        self.detail.add(self.lightsSwing)
        self.detail.add(JLabel("Delay between throttle commands (seconds):"))
        self.dccDelaySwing = JTextField(
                                        str(float(ADsettings.dccDelay) / 1000.), 5)
        self.detail.add(self.dccDelaySwing)
        self.detail.add(JLabel(
                        "Maximum interval between throttle commands (seconds): "))
        self.maxIdleSwing = JTextField(
                                       str(float(ADsettings.maxIdle) / 1000.), 5)
        self.detail.add(self.maxIdleSwing)
        self.detail.add(JLabel("Acceleration/deceleration interval: "))
        self.speedRampSwing = JComboBox(["1/10 sec.", "2/10 sec.", "3/10 sec.",
                                        "4/10 sec.", "5/10 sec."])
        self.speedRampSwing.setSelectedIndex(ADsettings.speedRamp -1)
        self.detail.add(self.speedRampSwing)
        self.detail.add(JLabel("Enable self-adjustment of braking distance: "))
        self.selfLearningSwing = JCheckBox("",
                                           ADsettings.selfLearning)
        if AutoDispatcher.simulation:
            self.selfLearningSwing.enabled = False
        self.detail.add(self.selfLearningSwing)

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button (don't use the default one, since we wish 
        # the button always on)
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)
        
    # Buttons of Preferences window =================

    # define what Cancel button in Preferences Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.preferencesFrame = None

    # define what Apply button in Preferences Window does when clicked
    def whenApplyClicked(self, event):
        ADsettings.verbose = self.verboseSwing.isSelected()
        self.ringBellSwing.isSelected()
        ADsettings.ringBell = self.ringBellSwing.isSelected()
        ADsettings.pauseMode = self.pauseSwing.getSelectedIndex()
        ADsettings.separateTurnouts = (
                                       self.separateTurnoutsSwing.isSelected())
        ADsettings.separateSignals = (
                                      self.separateSignalsSwing.isSelected())
        try:
            ADsettings.scale = float(self.scaleSwing.text)
        except:
            ADsettings.scale = 1
            self.scaleSwing.text = "1"
        try:
            ADsettings.flashingCycle = float(self.flashingSwing.text)
        except:
            ADsettings.flashingCycle = 1.0
            self.flashingSwing.text = "1"
        ADsettings.derailDetection = self.derailedSwing.getSelectedIndex()
        ADsettings.stalledDetection = self.stalledSwing.getSelectedIndex()
        try:
            ADsettings.stalledTime = int(
                                         float(self.stalledTimeSwing.text) * 1000.)
        except:
            ADsettings.stalledTime = 1000
            self.stalledTimeSwing.text = "1"
        ADsettings.lostCarsDetection = (
                                        self.lostCarsSwing.getSelectedIndex())
        oldValue = ADsettings.lostCarsTollerance
        try:
            ADsettings.lostCarsTollerance = int(
                                                float(self.lostLengthSwing.text) * ADsettings.units)
        except:
            ADsettings.lostCarsTollerance = oldValue
            self.lostLengthSwing.text = str(oldValue
                                            / ADsettings.units)
        oldValue = ADsettings.lostCarsTollerance
        try:
            ADsettings.lostCarsSections = int(
                                              self.lostMaxSwing.text)
        except:
            ADsettings.lostCarsSections = oldValue
            self.lostMaxSwing.text = str(oldValue)
        ADsettings.wrongRouteDetection = (
                                          self.wrongRouteSwing.getSelectedIndex())
        i = self.unitsSwing.getSelectedIndex()
        if i == 0:
            newUnits = 1.0
        elif i == 1:
            newUnits = 10.0
        else:
            newUnits = 25.4
        if newUnits != ADsettings.units:
            ADsettings.units = newUnits
            if AutoDispatcher.trainsFrame != None:
                AutoDispatcher.trainsFrame.reDisplay()
                AutoDispatcher.preferencesFrame.show()
        ADsettings.useLength = self.useLengthSwing.isSelected()
        ADsettings.resistiveDefault = self.useResistiveSwing.isSelected()
        ADsettings.allocationAhead = (self.aheadSwing.getSelectedIndex() + 1)
        ADsettings.blockTracking = self.blockTrackingSwing.isSelected()
        ADsettings.sectionTracking = (
                                      self.sectionTrackingSwing.isSelected())
        ADsettings.trustTurnouts = self.trustTurnoutsSwing.isSelected()
        try:
            ADsettings.turnoutDelay = int(
                                          float(self.turnoutDelaySwing.text) * 1000.)
        except:
            ADsettings.turnoutDelay = 1000
            self.turnoutDelaySwing.text = "1"
        try:
            ADsettings.clearDelay = int(
                                        float(self.clearDelaySwing.text) * 1000.)
        except:
            ADsettings.clearDelay = 0
            self.clearDelaySwing.text = "0"
        ADsettings.trustSignals = self.trustSignalsSwing.isSelected()
        try:
            ADsettings.signalDelay = int(
                                         float(self.signalDelaySwing.text) * 1000.)
        except:
            ADsettings.signalDelay = 0
            self.signalDelaySwing.text = "0"
        ADsettings.autoRestart = self.autoRestartSwing.isSelected()
        try:
            ADsettings.maintenanceTime = float(self.maintenanceSwing.text)
        except:
            ADsettings.maintenanceTime = 0
            self.maintenanceSwing.text = "0"
        try:
            if ADsettings.units == 25.4:
                multiplier = 1609344. / ADsettings.scale
            else:
                multiplier = 1000000. / ADsettings.scale
            ADsettings.maintenanceMiles = (float(self.milesSwing.text) *
                                           multiplier)
        except:
            ADsettings.maintenanceMiles = 0
            self.milesSwing.text = "0"
        try:
            ADsettings.dccDelay = int(
                                      float(self.dccDelaySwing.text) * 1000.)
        except:
            ADsettings.dccDelay = 10
            self.dccDelaySwing.text = "0.01"
        ADsettings.stopMode = self.stopModeSwing.getSelectedIndex()
        try:
            ADsettings.startDelayMin = int(
                                           float(self.startDelayMinSwing.text) * 1000.)
        except:
            ADsettings.startDelayMin = 0
            self.startDelayMinSwing.text = "0"
        try:
            ADsettings.startDelayMax = int(
                                           float(self.startDelayMaxSwing.text) * 1000.)
        except:
            ADsettings.startDelayMax = 0
            self.startDelayMaxSwing.text = "0"
        ADsettings.defaultStartAction = self.defaultStartSwing.text
        ADsettings.lightMode = self.lightsSwing.getSelectedIndex()
        ADsettings.speedRamp = self.speedRampSwing.getSelectedIndex() + 1
 
        try:
            ADsettings.maxIdle = int(float(self.maxIdleSwing.text) * 1000.)
        except:
            ADsettings.maxIdle = 0
            self.maxIdleSwing.text = "0"
        ADsettings.selfLearning = self.selfLearningSwing.isSelected()
            
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Preferences changes applied")

    # Sound List Window =================

class ADsoundListFrame (AdScrollFrame):
    def __init__(self):
        # Create Sound List window
        # super.init
        AdScrollFrame.__init__(self, "List of Sounds", None)

    def createHeader(self):
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 4))
        header1.add(AutoDispatcher.centerLabel("Name"))
        header1.add(JLabel(""))
        header1.add(JLabel(""))
        header1.add(JLabel(""))

        self.header.setLayout(GridLayout(1, 2))
        self.header.add(header1)
        self.header.add(AutoDispatcher.centerLabel("Path"))

    def createDetail(self):
        self.detail.setLayout(GridLayout(len(ADsettings.soundList), 2))
        self.namesSwing = []
        ind = 0
        for s in ADsettings.soundList:
            temppane = JPanel()
            temppane.setLayout(GridLayout(1, 4))
            nameSwing = JTextField(s.name, 5)
            self.namesSwing.append(nameSwing)
            temppane.add(nameSwing)
            browseButton = JButton("Browse")
            browseButton.setActionCommand(str(ind))
            browseButton.actionPerformed = self.whenBrowseClicked
            temppane.add(browseButton)
            if ind == 0:
                temppane.add(JLabel(""))
            else:
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                temppane.add(deleteButton)
            playButton = JButton("Play")
            playButton.setActionCommand(str(ind))
            playButton.actionPerformed = self.whenPlayClicked
            temppane.add(playButton)
            self.detail.add(temppane)
            self.detail.add(JLabel(s.path))
            ind += 1
            
    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

    # Buttons of Sound List window =================
    
    # define what Cancel button in Sound List Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.soundListFrame = None

    # define what Add button in Sound List Window does when clicked
    def whenAddClicked(self, event):
        ADsettings.soundList.append(ADsound("New sound"))
        self.soundListChanged()

    # define what Apply button in Sound List Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0
        for s in ADsettings.soundList:
            s.name = self.namesSwing[ind].text
            ind += 1
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Sound list updated")
        self.soundListChanged()

    # define what Browse button in Sound List Window does when clicked
    def whenBrowseClicked(self, event):
        ind = int(event.getActionCommand())
        fc = JFileChooser(ADsettings.soundRoot)
        fc.addChoosableFileFilter(ADsoundFilter())
        retVal = fc.showOpenDialog(None)
        if retVal != JFileChooser.APPROVE_OPTION:
            return
        file = fc.getSelectedFile()
        if file == None:
            return
        ADsettings.soundRoot = file.getParent()
        ADsettings.soundList[ind].setPath(file.getPath())
        if self.namesSwing[ind].text != "New sound":
            ADsettings.soundList[ind].name = self.namesSwing[ind].text
        if ADsettings.soundList[ind].name == "New sound":
            fileName = file.getName()
            upperName = fileName.upper()
            if upperName.endswith(".WAV"):
                fileName = fileName[0:len(fileName)-4]
            if upperName.endswith(".AU"):
                fileName = fileName[0:len(fileName)-3]
            ADsettings.soundList[ind].name = fileName
        self.soundListChanged()

    # define what Delete button in Sound List Window does when clicked
    def whenDeleteClicked(self, event):
        ind = int(event.getActionCommand())
        name = ADsettings.soundList[ind].name
        if (JOptionPane.showConfirmDialog(None, "Remove sound \""
            + name
            + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1):
            return
        ADsettings.soundList.pop(ind)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Sound \"" + name + "\" removed")
        self.soundListChanged()

    # define what Play button in Sound List Window does when clicked
    def whenPlayClicked(self, event):
        ind = int(event.getActionCommand())
        ADsettings.soundList[ind].play()
        
    def soundListChanged(self):
        if AutoDispatcher.soundDefaultFrame != None:
            AutoDispatcher.soundDefaultFrame.reDisplay()
        ADsettings.newSoundDic()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

class ADsoundFilter (FileFilter):
    def __init__(self):
        FileFilter.__init__(self)
    def accept(self, f):
        if f.isDirectory():
            return True
        name = str(f.getName()).upper()
        if (name.endswith(".WAV") or 
            name.endswith(".AU")):
            return True;
        return False;
        
    def getDescription(self):
        return "Sound Clip (*.wav, *.au)"

    # Sound Default Window =================

class ADsoundDefaultFrame (AdScrollFrame):
    def __init__(self):
        # Create Sound Default window
        # super.init
        AdScrollFrame.__init__(self, "Default Sounds", None)

    def createHeader(self):
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Event"))
        self.header.add(AutoDispatcher.centerLabel("Sound"))
        self.header.add(JLabel(""))

    def createDetail(self):
        self.detail.setLayout(GridLayout(len(ADsettings.soundLabel), 3))
        self.soundsSwing = []
        sounds = ["None"]
        for s in ADsettings.soundList:
            sounds.append(s.name)
        ind = 0
        for s in ADsettings.soundLabel:
            self.detail.add(JLabel(s))
            soundSwing = JComboBox(sounds)
            soundSwing.setSelectedIndex(ADsettings.defaultSounds[ind])
            self.soundsSwing.append(soundSwing)
            self.detail.add(soundSwing)
            playButton = JButton("Play")
            playButton.setActionCommand(str(ind))
            playButton.actionPerformed = self.whenPlayClicked
            if ADsettings.defaultSounds[ind] < 1:
                playButton.enabled = False
            self.detail.add(playButton)
            ind += 1
            
    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

    # Buttons of Sound Default window =================
    
    # define what Cancel button in Sound List Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.soundDefaultFrame = None

    # define what Apply button in Sound Default Window does when clicked
    def whenApplyClicked(self, event):
        ind = 0
        for s in self.soundsSwing:
            ADsettings.defaultSounds[ind] = s.getSelectedIndex()
            ind += 1
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Default sounds changed")
        self.reDisplay()

    # define what Play button in Sound List Window does when clicked
    def whenPlayClicked(self, event):
        ind = int(event.getActionCommand())
        ind = self.soundsSwing[ind].getSelectedIndex()-1
        if ind < 0:
            return
        ADsettings.soundList[ind].play()

    # Locomotives Window =================
    
class ADlocosFrame (AdScrollFrame):
    def __init__(self):
        # Create Locomotives window
        # super.init
        AdScrollFrame.__init__(self, "Locomotives", None)

    def createHeader(self):
        self.columns = len(ADsettings.speedsList) + 8
        self.header.setLayout(GridLayout(1, self.columns))
        self.header.add(AutoDispatcher.centerLabel("Loco"))
        self.header.add(AutoDispatcher.centerLabel("Addr."))
        for s in ADsettings.speedsList:
            self.header.add(AutoDispatcher.centerLabel(s))
        self.header.add(AutoDispatcher.centerLabel("Acc."))
        self.header.add(AutoDispatcher.centerLabel("Dec."))
        self.header.add(AutoDispatcher.centerLabel("Throttle"))
        runTime = JLabel("RunTime")
        runTime.setHorizontalAlignment(JLabel.RIGHT)
        self.header.add(runTime)
        if ADsettings.units == 25.4:
            miles = JLabel("Miles")
        else:
            miles = JLabel("Km.")
        miles.setHorizontalAlignment(JLabel.RIGHT)
        self.header.add(miles)
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        self.locos = ADlocomotive.getNames()
        self.locos.sort()
        self.detail.setLayout(GridLayout(len(self.locos), self.columns))
        ind = 0 
        for ll in self.locos:
            l = ADlocomotive.getByName(ll)
            self.detail.add(AutoDispatcher.centerLabel(str(l.name)))
            self.detail.add(l.addressSwing)
            for s in l.speedSwing:
                self.detail.add(s)
            self.detail.add(l.accSwing)
            self.detail.add(l.decSwing)
            self.detail.add(l.currentSpeedSwing)
            l.outputMileage()
            self.detail.add(l.hoursSwing)
            self.detail.add(l.milesSwing)
            clearButton = JButton("Clear")
            clearButton.setActionCommand(str(ind))
            clearButton.actionPerformed = self.whenClearClicked
            self.detail.add(clearButton)
            ind += 1

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

        # Remove button
        self.removeButton = JButton("Remove locos not in JMRI roster")
        self.removeButton.actionPerformed = self.whenRemoveClicked
        self.buttons.add(self.removeButton)

    # Buttons of Locomotives window =================
    
    # define what Cancel button in Locomotives Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.locosFrame = None

    # define what Remove button in Locomotives Window does when clicked
    def whenRemoveClicked(self, event):
        locos = ADlocomotive.getList()
        newDic = {}
        removed = 0
        for l in locos:
            if l.inJmriRoster or l.usedBy != None:
                newDic[l.name] = l
            else:
                removed += 1
        if removed > 0:
            # Ask confirmation!
            if (JOptionPane.showConfirmDialog(None, "Remove " + str(removed)
                + " locomotives not included in JMRI roster?",
                "Confirmation", JOptionPane.YES_NO_OPTION) == 1):
                removed = 0
        if removed > 0:
            ADlocomotive.locoIndex = newDic
            self.reDisplay()
            if AutoDispatcher.trainsFrame != None:
                AutoDispatcher.trainsFrame.reDisplay()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, str(removed)
                                    + " locomotives removed")
            AutoDispatcher.instance.saveLocomotives()
        else:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    " No locomotive removed")
        
    # define what Apply button in Locomotives Window does when clicked
    def whenApplyClicked(self, event):
        for l in ADlocomotive.getList():
            if not l.inJmriRoster:
                l.setAddress(int(l.addressSwing.text))
            ss = []
            for i in range(len(l.speedSwing)):
                try:
                    value = float(l.speedSwing[i].text)
                except:
                    value = 0.5
                    l.speedSwing[i].text = "0.5"
                ss.append(value)
            l.setSpeedTable(ss)
            try:
                acc = int(l.accSwing.text)
            except:
                acc = 0
                l.accSwing.text = "0"
            try:
                dec = int(l.decSwing.text)
            except:
                dec = 0
                l.decSwing.text = "0"
            l.setMomentum(acc, dec)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Locomotives changes applied")
        AutoDispatcher.instance.saveLocomotives()

    # define what Clear button in Locomotives Window does when clicked
    def whenClearClicked(self, event):
        ind = int(event.getActionCommand())
        locoName = self.locos[ind]
        loco = ADlocomotive.getByName(locoName)
        loco.runningTime = 0
        loco.mileage = 0
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Mileage and timer of locomotive \"" + locoName + "\" cleared")
        AutoDispatcher.instance.saveLocomotives()
        self.reDisplay()

    # Trains Window =================
    
class ADtrainsFrame (AdScrollFrame):
    def __init__(self):
        # Create Trains window
        temppane = JPanel()
        temppane1 = JPanel()
        temppane1.setLayout(BoxLayout(temppane1, BoxLayout.X_AXIS))
        temppane1.add(JLabel(
                      " Maximum number of trains running at once (0 = no limit) : "))
        self.maxTrainsSwing = JTextField(str(ADsettings.max_trains), 4)
        temppane1.add(self.maxTrainsSwing)
        self.applyButton = JButton("Set")
        self.applyButton.actionPerformed = self.whenApplyClicked
        temppane1.add(self.applyButton)
        temppane.add(temppane1)
        # super.init
        AdScrollFrame.__init__(self, "Trains", temppane)

    def createHeader(self):
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 2))
        header1.add(AutoDispatcher.centerLabel("Train"))
        header1.add(AutoDispatcher.centerLabel("Direction"))
        header2 = JPanel()
        header2.setLayout(GridLayout(1, 2))
        header2.add(AutoDispatcher.centerLabel("Section"))
        header2.add(AutoDispatcher.centerLabel("Locomotive"))
        header3 = JPanel()
        header3.setLayout(GridLayout(1, 2))
        header3.add(AutoDispatcher.centerLabel("Reversed"))
        header3.add(AutoDispatcher.centerLabel("Dest./State"))
        header4 = JPanel()
        header4.setLayout(GridLayout(1, 2))
        header4.add(AutoDispatcher.centerLabel("Speed"))
        header4.add(JLabel(""))
        header5 = JPanel()
        header5.setLayout(GridLayout(1, 2))
        header5.add(JLabel(""))
        header5.add(JLabel(""))

        header6 = JPanel()
        header6.setLayout(GridLayout(1, 1))
        header6.add(AutoDispatcher.centerLabel("Schedule"))

        self.header.setLayout(GridLayout(1, 6))
        self.header.add(header1)
        self.header.add(header2)
        self.header.add(header3)
        self.header.add(header4)
        self.header.add(header5)
        self.header.add(header6)

    def createDetail(self):
        # Fill contents of scroll area
        locos = []
        for l in ADlocomotive.getList():
            if l.usedBy == None:
                locos.append(l.name)
                
        trains = ADtrain.getList()
        nTrains = len(trains)

        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(nTrains, 2))
        temppane2 = JPanel()
        temppane2.setLayout(GridLayout(nTrains, 2))       
        temppane3 = JPanel()
        temppane3.setLayout(GridLayout(nTrains, 2))
        temppane4 = JPanel()
        temppane4.setLayout(GridLayout(nTrains, 2))
        temppane5 = JPanel()
        temppane5.setLayout(GridLayout(nTrains, 2))
        temppane6 = JPanel()
        temppane6.setLayout(GridLayout(nTrains, 1))

        ind = 0
        for t in trains:
        
            locosList = [t.locoName]
            locosList.extend(locos)
            locosList.sort()
            t.locoRoster = JComboBox(locosList)
            t.locoRoster.setSelectedItem(t.locoName)
            enableLoco = (t.engineerSwing.getSelectedItem() != "Manual" and
                          not t.running)
            t.locoRoster.setEnabled(enableLoco)
            t.reversedSwing.setEnabled(enableLoco)
        
            temppane1.add(t.nameSwing)
            temppane1.add(t.directionSwing)

            temppane2.add(t.sectionSwing)
            temppane2.add(t.locoRoster)
            
            temppane3.add(t.reversedSwing)
            temppane3.add(t.destinationSwing)

            temppane4.add(t.speedLevelSwing)
            t.detailButton.setActionCommand(str(ind))
            t.detailButton.actionPerformed = self.whenDetailClicked
            temppane4.add(t.detailButton)
            
            temppane5.add(t.changeButton)
            temppane5.add(t.deleteButton)

            temppane6.add(t.scheduleSwing)
            ind += 1
            
        self.detail.setLayout(GridLayout(1, 4))
        self.detail.add(temppane1)
        self.detail.add(temppane2)
        self.detail.add(temppane3)
        self.detail.add(temppane4)
        self.detail.add(temppane5)
        self.detail.add(temppane6)

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Import button
        self.importButton = JButton("Import")
        self.importButton.actionPerformed = self.whenImportClicked
        self.buttons.add(self.importButton)

    # Buttons of Trains window =================
    
    # define what Cancel button in Locomotives Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.trainsFrame = None
        if AutoDispatcher.trainDetailFrame != None:
            AutoDispatcher.trainDetailFrame.dispose()
            AutoDispatcher.trainDetailFrame = None

    # define what Add button in Trains Window does when clicked
    def whenAddClicked(self, event):
        ADtrain("New train")
        AutoDispatcher.setTrainsDirty()
        self.reDisplay()
        
    # define what Import button in Trains Window does when clicked
    def whenImportClicked(self, event):
        if AutoDispatcher.importFrame == None:
            AutoDispatcher.importFrame = ADImportTrainFrame()
        else:
            AutoDispatcher.importFrame.reDisplay()

    # define what Detail button in Trains Window does when clicked
    def whenDetailClicked(self, event):
        ind = int(event.getActionCommand())
        if AutoDispatcher.trainDetailFrame != None:
            AutoDispatcher.trainDetailFrame.show()
            if ADtrain.trains[ind] == AutoDispatcher.trainDetailFrame.train:
                return
            if (JOptionPane.showConfirmDialog(None, "Save details of train \""
                + AutoDispatcher.trainDetailFrame.train.getName()
                + "\" before editing details of train \""
                + ADtrain.trains[ind].getName() + "\"?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != 1):
                AutoDispatcher.trainDetailFrame.train.whenSetClicked(None)
            AutoDispatcher.trainDetailFrame.dispose()
        AutoDispatcher.trainDetailFrame = ADtrainDetailFrame(ADtrain.trains[ind])

    # define what Apply button in Trains Window does when clicked
    def whenApplyClicked(self, event):
        try:
            ADsettings.max_trains = int(self.maxTrainsSwing.text)
        except:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Wrong maximum number of trains: "
                                    + self.maxTrainsSwing.text + " ignored")
            self.maxTrainsSwing.text = str(ADsettings.max_trains)
            return
        if ADsettings.max_trains < 0:
            ADsettings.max_trains = 0
            self.maxTrainsSwing.text = "0"
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Maximum number of trains set to "
                                + str(ADsettings.max_trains))

    def deleteTrain(self, train):
    # Routine to delete a train, called when the DEL button on train's
    # row is clicked
        # Release sections allocated to removed train
        train.releaseSections()
        # If the train has a locomotive, release it
        if train.locomotive != None:
            train.locomotive.usedBy = None
        # remove train from table
        ADtrain.remove(train)
        AutoDispatcher.setTrainsDirty()
        # force redisplay of Trains Window contents
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Train \"" + train.name + "\" removed")

    # Trains Window =================
    
class ADtrainDetailFrame (AdScrollFrame):
    def __init__(self, train):
        # Create Train Detail window
        self.train = train
        # super.init
        AdScrollFrame.__init__(self, "Train " + train.getName()
                               + " detail", None)

    def createHeader(self):
        self.header = None

    def createDetail(self):
        # Fill contents of scroll area
        engineerList = AutoDispatcher.engineers.keys()
        engineerList.sort()
        engineerList.append("Manual")

        if self.train.locomotive == None:
            rows = 7
        else:
            rows = 8

        self.detail.setLayout(BoxLayout(self.detail, BoxLayout.Y_AXIS))

        detail1 = JPanel()
        detail1.setLayout(GridLayout(rows, 2))
        
        detail1.add(JLabel("Cars are equipped with resistive wheels: "))
        detail1.add(self.train.resistiveSwing)
        detail1.add(JLabel("Actions before train departure: "))
        self.train.startActionSwing.text = self.train.startAction
        detail1.add(self.train.startActionSwing)        
        detail1.add(JLabel("Stop at beginning of sections that support this option: "))
        detail1.add(self.train.canStopAtBeginningSwing)
        if ADsettings.units == 1.0:
            detail1.add(JLabel("Train length (mm.), including"
                        " locomotive: "))
        elif ADsettings.units == 10.0:
            detail1.add(JLabel("Train length (cm.), including"
                        " locomotive: "))
        else:
            detail1.add(JLabel("Train length (inches), including"
                        " locomotive: "))
        self.train.trainLengthSwing.text = str(round(self.train.trainLength
                                               / ADsettings.units, 2))
        detail1.add(self.train.trainLengthSwing)

        detail1.add(JLabel("Sections ahead to be allocated: "))
        detail1.add(self.train.trainAllocationSwing)

        detail1.add(JLabel("Engineer: "))
        self.train.engineerSwing.removeAllItems()
        for i in engineerList:
            self.train.engineerSwing.addItem(i)
        if self.train.engineerName in engineerList:
            self.train.engineerSwing.setSelectedItem(self.train.engineerName)
        else:
            self.train.engineerSwing.setSelectedItem("Auto")
            self.train.engineerAssigned = False
        detail1.add(self.train.engineerSwing)
        if self.train.locomotive != None:
            detail1.add(JLabel(
                        "Clear braking history of this train for locomotive \""
                        + self.train.locoName + "\": "))
            clearLoco = JButton("Clear history for current locomotive")
            clearLoco.actionPerformed = self.whenClearLocoClicked
            detail1.add(clearLoco)
        detail1.add(JLabel(
                    "Clear braking history of this train for all locomotives: "))
        clearAll = JButton("Clear history for all locomotives")
        clearAll.actionPerformed = self.whenClearAllClicked
        detail1.add(clearAll)
        self.detail.add(detail1)
        
        detail1 = JPanel()
        detail1.add(JLabel("Schedule"))
        self.detail.add(detail1)

        detail1 = JPanel()
        self.scheduleSwing = JTextArea(self.train.scheduleSwing.text, 4, 60)
        self.scheduleSwing.setLineWrap(True)
        self.scheduleSwing.setWrapStyleWord(True)
        detail1.add(JScrollPane(self.scheduleSwing))
        self.detail.add(detail1)

        detail1 = JPanel()
        detail1.add(JLabel("Train speeds correspondence"))
        self.detail.add(detail1)

        detail1 = JPanel()
        detail1.setLayout(GridLayout(len(ADsettings.speedsList) + 1, 2))
        detail1.add(JLabel("Instead of"))
        detail1.add(JLabel("Use"))
        self.trainSpeedSwing = []
        ind = 1
        max = len(ADsettings.speedsList)-1
        for s in ADsettings.speedsList:
            if ind > len(self.train.trainSpeed):
                self.train.trainSpeed.append(ind)
            detail1.add(JLabel(s))
            speedSwing = JComboBox(ADsettings.speedsList)
            i = self.train.trainSpeed[ind-1]-1
            if i > max:
                i = max
            speedSwing.setSelectedIndex(i)
            self.trainSpeedSwing.append(speedSwing)
            detail1.add(speedSwing)
            ind += 1
        self.detail.add(detail1)

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.buttons.add(self.train.setButton)

    # Buttons of Train Detail window =================
    
    # define what Cancel button in Train Detail Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.trainDetailFrame = None

    # define what Clear Loco button in Train Detail Window does when clicked
    def whenClearLocoClicked(self, event):
        self.train.clearBrakingHistory(self.train.locomotive)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Braking history for locomotive cleared")

    # define what Clear All button in Train Detail Window does when clicked
    def whenClearAllClicked(self, event):
        self.train.clearBrakingHistory(None)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Braking history for train cleared")

    # Import Trains Window =================
    
class ADImportTrainFrame (AdScrollFrame):
    def __init__(self):
        # Create Operations Interface window
        # Allows user to import an Operations train in AutoDispatcher
        # super.init
        AdScrollFrame.__init__(self, "Import train", JLabel("Choose train to be imported"))

    def createHeader(self):
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Train name"))
        self.header.add(AutoDispatcher.centerLabel("Status"))
        self.header.add(JLabel(""))

    def createDetail(self):
        trainIds = []
        # Fill contents of scroll area
#        trainManager = TrainManager.instance()
#        trainIds = trainManager.getTrainsByNameList()
#        nTrains = len(trainIds)
#        self.detail.setLayout(GridLayout(nTrains, 3))
#        self.opTrains = []
#        for i in range(nTrains) :
#            opTrain = trainManager.getTrainById(trainIds[i])
#            self.opTrains.append(opTrain)
#            self.detail.add(JLabel(opTrain.getName()))
#            if opTrain.getBuilt() :
#                self.detail.add(AutoDispatcher.centerLabel("Built"))
#                importButton = JButton("Import")
#                importButton.setActionCommand(str(i))
#                importButton.actionPerformed = self.whenImportClicked
#                self.detail.add(importButton)
#            else :
#                self.detail.add(JLabel(""))
#                self.detail.add(JLabel(""))

    def createButtons(self): 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

    # Buttons of Import Trains window =================
    
    # define what Cancel button in Import Trains Window does when clicked
    def whenCancelClicked(self, event):
        AdScrollFrame.dispose(self)
        AutoDispatcher.importFrame = None
            
    # define what Import button in Import Trains Window does when clicked
    def whenImportClicked(self, event):
        ind = 0
#        ind = int(event.getActionCommand())
#        # Get Operations train
#        opTrain = self.opTrains[ind]
#        name = opTrain.getIconName()
#        engine = opTrain.getLeadEngine()
#        if engine == None :
#            engine = ""
#        else :
#            engine = engine.getNumber()
#        # Get the list of cars to be hauled by the train
#        carManager = CarManager.instance()
#        carIds = carManager.getCarsByTrainList(opTrain)
#        cars = []
#        for carId in carIds :
#            cars.append(carManager.getCarById(carId))
#        # Get train route
#        route = opTrain.getRoute()
#        routeIds = route.getLocationsBySequenceList()
#        routeLocations = []
#        for id in routeIds :
#            routeLocations.append(route.getLocationById(id))
#        # Now build our schedule
#        departStation = True
#        schedule = ""
#        previousDirection = startDirection = ""
#        startLocation = None
#        hauled = []
#        while len(routeLocations) > 0 :
#            routeLocation = routeLocations.pop(0)
#            direction = routeLocation.getTrainDirectionString().upper()
#            location = ADlocation.getByName(routeLocation.getName())
#            manualSwitching = ""
#            # Check if any car must be picked up or dropped here
#            for car in cars :
#                source = car.getRouteLocation()
#                destination = car.getRouteDestination()
#                if source == destination :
#                    continue
#                if source == routeLocation and not car in hauled :
#                    pickUp = True
#                    # does train pass twice in this location ?
#                    if routeLocation in routeLocations :
#                        # Yes. See if car must be picked up now or next time
#                        for nextLocation in routeLocations :
#                            if nextLocation == routeLocation :
#                                pickUp = False
#                                break
#                            if nextLocation == destination :
#                                break
#                    if pickUp :
#                        hauled.append(car)
#                        manualSwitching = " $M"
#                elif destination == routeLocation and car in hauled :
#                    manualSwitching = " $M"
#                    hauled.remove(car)
#            if departStation :
#                startDirection = direction
#                manualSwitching = ""
#                startLocation = location
#                routeStart = routeLocation
#                departStation = False
#            if location != None :
#                if len(schedule) > 0 :
#                    schedule += " "
#                if previousDirection != direction :
#                    schedule += "$" + direction + " "
#                    previousDirection = direction
#                schedule += "[" + location.text + "]" + manualSwitching
#        train = ADtrain(name)
#        train.opTrain = opTrain
#        if startDirection.strip() != "" :
#            train.setDirection(startDirection)
#        if engine != None and ADlocomotive.getByName(engine) != None :
#            train.changeLocomotive(engine)
#        else :
#            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
#              "Locomotive \"" + engine +
#              "\" not found. Manual running assumed!")
#            train.setEngineer("Manual")
#        # Find start section
#        if startLocation != None :
#            for section in startLocation.getSections() :
#                section.setManual(True)
#                if section.getAllocated() == None :
#                    train.setSection(section, True)
#                    if section.getAllocated() == train :
#                        break
#                section.setManual(False)
#            if section.getAllocated() != train :
#                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
#                "Canot place train \"" + name +
#                "\" in location " + startLocation.name
#                + " (all sections occupied)")
#            train.trainLength = round(float(routeStart.getTrainLength())
#              * 304.8 / ADsettings.scale)
#        if schedule.strip() != "" :
#            train.setSchedule(schedule)
#        train.updateSwing()
#        
#        AutoDispatcher.setTrainsDirty()
#        if AutoDispatcher.trainsFrame != None :
#            AutoDispatcher.trainsFrame.reDisplay()
#        self.whenCancelClicked(None)
