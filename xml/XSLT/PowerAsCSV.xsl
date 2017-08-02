<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI decoder definition index and -->
<!-- definition files into an HTML selection guide page -->

<!-- This file is part of JMRI.  Copyright 2007-2011.                       -->
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

<!-- Need to instruct the XSLT processor to use text output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="text" encoding="ISO-8859-1" 
	indent="no"
	omit-xml-declaration="yes"
	standalone="no" />

<!-- avoid producing lots of blank lines -->
<xsl:strip-space elements="*" />

<!-- This first template matches our root element in the input file.
     This will trigger the generation of the header.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='decoderIndex-config'>
<xsl:text>"mfg","model","family","Max Motor Current","Max Total Current","Connector"</xsl:text>
<xsl:text>
</xsl:text>  <!-- thats a newline with no whitespace -->

		<xsl:call-template name="sizeTable"/>

</xsl:template>

<!-- template to handle a size table -->
<xsl:template name="sizeTable">

		<xsl:for-each select="/decoderIndex-config/decoderIndex/familyList/family">
		  <xsl:if test="not( @mfg = 'NMRA' )" >
			<xsl:for-each select="document(@file)/decoder-config/decoder/family/model">
			
			<!-- display model as row in table -->
			<xsl:text>"</xsl:text>
			<xsl:value-of select="../@mfg"/>
			<xsl:text>","</xsl:text>
			<xsl:value-of select="@model"/>
			<xsl:text>","</xsl:text>
			<xsl:value-of select="../@name"/>
			<xsl:text>","</xsl:text>
			<xsl:value-of select="@maxMotorCurrent"/>
			<xsl:text>","</xsl:text>
			<xsl:value-of select="@maxTotalCurrent"/>
			<xsl:text>","</xsl:text>
			<xsl:value-of select="@connector"/>
<xsl:text>"
</xsl:text>  <!-- thats a newline with no whitespace -->

			</xsl:for-each>
		  </xsl:if>
		</xsl:for-each>

</xsl:template>

</xsl:stylesheet>
