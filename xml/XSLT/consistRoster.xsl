<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: consistRoster.xsl,v 1.5 2008-01-25 05:48:35 dan_boudreau Exp $ -->

<!-- Stylesheet to convert a JMRI  consist roster XML file into displayable HTML -->

<!-- Used by default when the consist roster file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007.                            -->
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


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='consist-roster-config'>

<html>
	<head>
		<title>JMRI Consist Roster File</title>
	</head>
	
	<body>
		<h2>JMRI Consist Roster File</h2>

                <xsl:apply-templates/>

	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>

<!-- Display each roster entry -->
<xsl:template match="roster/consist">
<h3>Consist:<xsl:value-of select="@id"/></h3>
Road number="<xsl:value-of select="@roadNumber"/>"<br/>
Road name="<xsl:value-of select="@roadName"/>"<br/>
Model="<xsl:value-of select="@model"/>"<br/>
Comment="<xsl:value-of select="@comment"/>"
<p/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="eng1">Lead loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>"
</xsl:template>
<br/>
<xsl:template match="eng2">Rear loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>" 
</xsl:template>
<br/>
<xsl:template match="eng3">Mid 1 loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>" 
</xsl:template>
<br/>
<xsl:template match="eng4">Mid 2 loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>" 
</xsl:template>
<br/>
<xsl:template match="eng5">Mid 3 loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>" 
</xsl:template>
<br/>
<xsl:template match="eng6">Mid 4 loco address: 
"<xsl:value-of select="@dccLocoAddress"/>"
long address="<xsl:value-of select="@longAddress"/>"
loco direction="<xsl:value-of select="@locoDir"/>" 
</xsl:template>

</xsl:stylesheet>
