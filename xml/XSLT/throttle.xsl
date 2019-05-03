<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Stylesheet to convert a JMRI throttle XML file into displayable HTML -->

<!-- Used by default when the throttle file is displayed in a web browser-->

<!-- This is just a basic implementation for debugging purposes, without -->
<!-- any real attempt at formatting -->

<!-- This file is part of JMRI.  Copyright 2007-2018.                            -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2019')" />

<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We can also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='throttle-config'>

<html>
	<head>
		<title>JMRI Throttle File</title>
	</head>
	
	<body>
		<h2>JMRI Throttle File</h2>

                <xsl:apply-templates/>

<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
		<a href="http://www.tagadab.com/">
		<img src="https://www.tagadab.com/sites/default/files/logo-tagadab-nostrap.png" height="28" width="103" border="0" alt="Tagadab logo"/></a>
	</body>
</html>

</xsl:template>

<!-- Title each throttle frames -->
<xsl:template match="throttle-config/ThrottleFrame">
<h3>Window: <xsl:value-of select="@title"/></h3>
<xsl:apply-templates/>
</xsl:template>

<!-- Display position-->
<xsl:template match="window">
Position: <xsl:value-of select="@x"/>,<xsl:value-of select="@y"/>
Size: <xsl:value-of select="@width"/>,<xsl:value-of select="@height"/><br/>
</xsl:template>

<!-- Display control panel subwindow -->
<xsl:template match="ControlPanel">
<h4>Control Panel</h4>
<xsl:apply-templates/>
displaySpeedSlider="<xsl:value-of select="@displaySpeedSlider"/>" 
speedMode="<xsl:value-of select="@speedMode"/>" 
trackSlider="<xsl:value-of select="@trackSlider"/>" 
trackSliderMinInterval="<xsl:value-of select="@trackSliderMinInterval"/>"

</xsl:template>

<!-- Display function panel subwindow -->
<xsl:template match="FunctionPanel">
<h4>Function Panel</h4>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="FunctionButton">
Button
id="<xsl:value-of select="@id"/>"
text="<xsl:value-of select="@text"/>" 
isLockable="<xsl:value-of select="@isLockable"/>" 
isVisible="<xsl:value-of select="@isVisible"/>" 
fontSize="<xsl:value-of select="@fontSize"/>"
<br/>
</xsl:template>

<!-- Display control panel subwindow -->
<xsl:template match="AddressPanel">
<h4>Address Panel</h4>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="dcclocoaddress">
Loco address: 
number="<xsl:value-of select="@number"/>"
longaddress="<xsl:value-of select="@longaddress"/>" 
<br/>
</xsl:template>

</xsl:stylesheet>
