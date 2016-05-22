<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI roster XML file into displayable HTML -->

<!-- Used by default when the roster file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007-2011.                            -->
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
      <xsl:param name="JmriCopyrightYear"/>


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

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>

<!-- Display each roster entry -->
<xsl:template match="roster/locomotive">
<h3>Locomotive: <xsl:value-of select="@id"/></h3>
roadNumber="<xsl:value-of select="@roadNumber"/>"<br/>
roadName="<xsl:value-of select="@roadName"/>"<br/>
mfg="<xsl:value-of select="@mfg"/>"<br/>
owner="<xsl:value-of select="@owner"/>"<br/>
model="<xsl:value-of select="@model"/>"<br/>
dccAddress="<xsl:value-of select="@dccAddress"/>"<br/>
comment="<xsl:value-of select="@comment"/>"<br/>
Filename: <xsl:value-of select="@fileName"/>
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
