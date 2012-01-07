<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI locomotive XML file into displayable HTML -->

<!-- Used by default when the locomotive file is displayed in a web browser-->

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


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='locomotive-config'>

<html>
	<head>
		<title>JMRI Locomotive File</title>
	</head>
	
	<body>
		<h2>JMRI Locomotive File</h2>

                <xsl:apply-templates/>

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; 1997 - 2012 JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>

<!-- Index through manufacturers -->
<xsl:template match="locomotive">
<h3>Id: <xsl:value-of select="@id"/></h3>
roadNumber="<xsl:value-of select="@roadNumber"/>"<br/>
roadName="<xsl:value-of select="@roadName"/>"<br/>
mfg="<xsl:value-of select="@mfg"/>"<br/>
model="<xsl:value-of select="@model"/>"<br/>
dccAddress="<xsl:value-of select="@dccAddress"/>"<br/>
comment="<xsl:value-of select="@comment"/>"
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

<!-- Display variables inside two lines -->
<xsl:template match="decoderDef">
<hr/>
<xsl:apply-templates/>
<hr/>
</xsl:template>

<!-- Display one variable -->
<xsl:template match="varValue">
Variable: <xsl:value-of select="@item"/>
value="<xsl:value-of select="@value"/>"
<br/>
</xsl:template>

<!-- Display one CV -->
<xsl:template match="CVvalue">
CV: <xsl:value-of select="@name"/>
value="<xsl:value-of select="@value"/>"
<br/>
</xsl:template>


</xsl:stylesheet>
