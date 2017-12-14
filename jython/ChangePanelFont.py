# Sample script to find a specific Panel and change the
# font of its contents
#
# To use this, change the "panelname" and "newfont" values 
# to match your layout
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

newfont = "OCR A Std"
panelname = "Waccamaw Coast Line"

import jmri
import javax.swing
import java.awt
import java.awt.Container

def updatefont(container) :
    for component in container :
        size = component.font.size
        style = component.font.style
        component.setFont(java.awt.Font(newfont, style, size))
        if isinstance(component, java.awt.Container) :
            updatefont(component.getComponents())
            
for frame in jmri.util.JmriJFrame.getFrameList() :
    if ( not frame.title == panelname) : continue
    print "found panel, changing fonts"
    updatefont(frame.getComponents())
    print "font change completed OK"
    