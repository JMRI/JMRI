<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: CSVsummary.xsl,v 1.2 2003-08-10 17:41:58 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI decoder definition to -->
<!-- one line of a CSV file  per decoder -->

<!-- This made from the readme2html.xsl file of TestXSLT 2.7 -->

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

<xsl:template match="/">
	<!-- write header -->
	<xsl:call-template name="writeHeader" />
	<xsl:apply-templates select="decoderIndex-config/decoderIndex/familyList"/>
</xsl:template>

<!-- template to write the header line -->
<xsl:template name="writeHeader">
	<xsl:text>"mfg","model","family"</xsl:text>

			<xsl:for-each select="document('interestingCVs.xml')/CVs/CV">
				<!-- at this point, @num is the CV number -->
				<xsl:text>,"CV </xsl:text>
				<xsl:value-of select="@num" />
				<xsl:text>"</xsl:text>
			</xsl:for-each>
	
	<xsl:text>
</xsl:text>  <!-- thats a newline with no whitespace -->

</xsl:template>

<!-- Find family nodes, and process the files they reference -->
<xsl:template match="familyList">
	
	<xsl:for-each select="family">
		<xsl:variable name="filename" select="@file" />
		<xsl:for-each select="document(@file)/decoder-config/decoder/family">
			<xsl:call-template name="doFileFamily">
				<xsl:with-param name="file" select="$filename"/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:for-each>
</xsl:template>

<!-- In the decoder files, print the model info -->
<xsl:template name="doFileFamily">  <!-- invoke at family in index -->
	<xsl:param name="file"/>
	<!-- each model has a line of it's own -->
	<xsl:for-each select="model">
		<xsl:text>&quot;</xsl:text>
		<xsl:value-of select="../@mfg"/>
		<xsl:text>&quot;,&quot;</xsl:text>
		<xsl:value-of select="@model"/>
		<xsl:text>&quot;,&quot;</xsl:text>
		<xsl:value-of select="../@name"/>
		<xsl:text>&quot;,</xsl:text>
		<!-- header done; handle the CVs -->
			<xsl:for-each select="document('interestingCVs.xml')/CVs/CV">
				<!-- at this point, @num is the next CV number to look for -->
				<xsl:call-template name="findCVandShow">
					<xsl:with-param name="fileIn" select="$file"/>
					<xsl:with-param name="cv" select="@num"/>
				</xsl:call-template>
			</xsl:for-each>
		<!-- end the line -->
		<xsl:text>
</xsl:text>  <!-- that's a newline; left margin to avoid extra whitespace -->
	</xsl:for-each>
</xsl:template>

<xsl:template name="findCVandShow">  <!-- position unimportant -->
	<xsl:param name="fileIn" select="'foo'"/>
	<xsl:param name="cv"/>
		<xsl:text>&quot;</xsl:text>
		<xsl:for-each select="document($fileIn)/decoder-config/decoder/variables/variable">
			<xsl:if test="$cv = @CV">
				<!-- here current element is to be displayed, -->
				<!-- as it defines our CV -->
				<xsl: select="@label"/>
				<xsl:text>;</xsl:text>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>&quot;,</xsl:text>
</xsl:template>

</xsl:stylesheet>
