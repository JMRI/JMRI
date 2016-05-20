<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert JMRI decoder definitions to -->
<!-- a huge HTML table of CV values -->

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
      <xsl:param name="JmriCopyrightYear"/>

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>

<!-- avoid producing lots of blank lines -->
<xsl:strip-space elements="*" />

<xsl:template match="/">
	<!-- write header -->
	<html>
		<head>
			<title>JMRI decoder CV cross-reference</title>
		</head>
	
		<body>
			<h2>JMRI decoder CV cross-reference</h2>
	<xsl:apply-templates select="decoderIndex-config/decoderIndex/familyList"/>

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


<!-- Find family nodes, and process the files they reference -->
<xsl:template match="familyList">
		<!-- define table and fill -->
		<table border="0" cellspacing="1" cellpadding="1">
		<tr>
			<th bgcolor="#cccccc">Mfg</th>
			<th bgcolor="#cccccc">Model</th>
			<th bgcolor="#cccccc">Family</th>
			<th bgcolor="#cccccc">Length</th>

			<xsl:for-each select="document('interestingCVs.xml')/CVs/CV">
				<!-- at this point, @num is the CV number -->
				<th bgcolor="#cccccc">CV <xsl:value-of select="@num" /></th>
			</xsl:for-each>

		</tr>

	<!-- fill table rows -->	
	<xsl:for-each select="family">
		<xsl:variable name="filename" select="@file" />
		<xsl:if test="not( @file = '0NMRA_test.xml' )" >
		  <xsl:for-each select="document(concat('pages/',@file))/decoder-config/decoder/family">
			<xsl:call-template name="doFileFamily">
				<xsl:with-param name="file" select="$filename"/>
			</xsl:call-template>
		  </xsl:for-each>
		</xsl:if> 
	</xsl:for-each>
	
	<!-- end table -->
	</table>
</xsl:template>

<!-- In the decoder files, print the model info -->
<xsl:template name="doFileFamily">  <!-- invoke at family in index -->
	<xsl:param name="file"/>
	<!-- each model has a line of it's own -->
	<xsl:for-each select="model">
		<tr>
			<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@mfg"/></td>
			<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@model"/></td>
			<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@name"/></td>

		<!-- front of row done; handle the CVs -->
			<xsl:for-each select="document('interestingCVs.xml')/CVs/CV">
				<!-- at this point, @num is the next CV number to look for -->
				<xsl:call-template name="findCVandShow">
					<xsl:with-param name="fileIn" select="$file"/>
					<xsl:with-param name="cv" select="@num"/>
				</xsl:call-template>
			</xsl:for-each>
		<!-- end the line -->
		</tr>
	</xsl:for-each>
</xsl:template>

<xsl:template name="findCVandShow">  <!-- position unimportant -->
	<xsl:param name="fileIn" select="'foo'"/>
	<xsl:param name="cv"/>
		<td bgcolor="#eeeeee" valign="top" align="center">
		<xsl:for-each select="document(concat('pages/',$fileIn))/decoder-config/decoder/variables/variable">
			<xsl:if test="$cv = @CV">
				<!-- here current element is to be displayed, -->
				<!-- as it defines our CV -->
				<xsl:value-of select="@label"/><hr/>
			</xsl:if>
		</xsl:for-each>
		</td>
</xsl:template>

</xsl:stylesheet>
