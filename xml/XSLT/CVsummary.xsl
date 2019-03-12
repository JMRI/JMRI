<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert JMRI decoder definitions to -->
<!-- a huge HTML table of CV values -->

<!-- This file is part of JMRI.  Copyright 2007-2018.                       -->
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
