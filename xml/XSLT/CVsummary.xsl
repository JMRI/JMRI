<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: CVsummary.xsl,v 1.1 2003-08-11 00:43:35 jacobsen Exp $ -->

<!-- Stylesheet to convert JMRI decoder definitions to -->
<!-- a huge HTML table of CV values -->

<!-- This made from the readme2html.xsl file of TestXSLT 2.7 -->

<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

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
		<xsl:for-each select="document(@file)/decoder-config/decoder/family">
			<xsl:call-template name="doFileFamily">
				<xsl:with-param name="file" select="$filename"/>
			</xsl:call-template>
		</xsl:for-each>
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
		<xsl:for-each select="document($fileIn)/decoder-config/decoder/variables/variable">
			<xsl:if test="$cv = @CV">
				<!-- here current element is to be displayed, -->
				<!-- as it defines our CV -->
				<xsl: select="@label"/>
				<xsl:text>;</xsl:text>
			</xsl:if>
		</xsl:for-each>
		</td>
</xsl:template>

</xsl:stylesheet>
