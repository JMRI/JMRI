<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI roster XML file into displayable HTML -->

<!-- Used by default when the roster file is displayed in a web browser-->

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

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2019')" />

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='roster-config'>

<html>
	<head>
		<title>JMRI Roster File</title>
	</head>
	
	<body>
		<h2>JMRI Roster File</h2>

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

<!-- Display each roster entry -->
<xsl:template match="roster/locomotive">
<h3>Locomotive: <xsl:value-of select="@id"/><br/>
<xsl:element name="img">
  <xsl:attribute name="src">resources/<xsl:value-of select="@iconFilePath"/></xsl:attribute>
  <xsl:attribute name="height">20</xsl:attribute>
  <xsl:attribute name="alt">No icon</xsl:attribute>
</xsl:element></h3>
roadNumber="<xsl:value-of select="@roadNumber"/>"<br/>
roadName="<xsl:value-of select="@roadName"/>"<br/>
mfg="<xsl:value-of select="@mfg"/>"<br/>
owner="<xsl:value-of select="@owner"/>"<br/>
model="<xsl:value-of select="@model"/>"<br/>
dccAddress="<xsl:value-of select="@dccAddress"/>"<br/>
comment="<xsl:value-of select="@comment"/>"<br/>
Filename: 
<xsl:element name="a">
  <xsl:attribute name="href">roster/<xsl:value-of select="@fileName"/></xsl:attribute>
  <xsl:value-of select="@fileName"/>
</xsl:element><br/>
URL:
<xsl:element name="a">
  <xsl:attribute name="href"><xsl:value-of select="@URL"/></xsl:attribute>
  <xsl:value-of select="@URL"/>
</xsl:element>
<p/>
<xsl:apply-templates/>
</xsl:template>

<!-- Display decoder info -->
<xsl:template match="decoder">
Decoder: <xsl:value-of select="@model"/>
family="<xsl:value-of select="@family"/>"
comment="<xsl:value-of select="@comment"/>"
<p/>
</xsl:template>

<xsl:template match="dcclocoaddress">
Loco address: 
number="<xsl:value-of select="@number"/>"
longaddress="<xsl:value-of select="@longaddress"/>" 
<p/>
</xsl:template>


</xsl:stylesheet>
