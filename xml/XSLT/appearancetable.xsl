<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: appearancetable.xsl,v 1.1 2009-12-24 04:33:52 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI appearance table file into displayable HTML    -->

<!-- Used by default when the file is displayed in a web browser            -->

<!-- This file is part of JMRI.  Copyright 2009.                            -->
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
<xsl:template match='/'>

<html>
	<head>
		<title>JMRI Appearance Table: <xsl:value-of select="aspecttable/name"/></title>
	</head>
	
	<body>
		<h2>JMRI <xsl:value-of select="aspecttable/name"/> Appearance Table</h2>


<xsl:apply-templates/>

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; 1997 - 2009 JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>

<!-- Overall table display -->
<xsl:template match="appearancetable">
    For aspect table: <xsl:value-of select="aspecttable"/>
    <p/>
    Name: <xsl:value-of select="name"/><p/>
    <xsl:value-of select="reference"/><p/>
    <xsl:value-of select="description"/><p/>
    <!-- show the appearances -->
    <xsl:apply-templates select="appearances"/>
</xsl:template>

<!-- Display each appearance -->
<xsl:template match="appearances/appearance">
<h3><xsl:value-of select="aspectname"/></h3>
    <xsl:apply-templates/>
</xsl:template>

<!-- already shown -->
<xsl:template match="aspectname"/>

<xsl:template match="show">
Show: <xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="reference">
<p/>Reference: <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
