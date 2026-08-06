<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xi="http://www.w3.org/2001/XInclude">
    
    <xsl:output method="xml" encoding="utf-8"/>
    
    <xsl:template name="outputInfo">
        <xsl:for-each select="//decoder-config/pane[name/text() = '__DecoderFnDefs']/column/display">
            <xsl:variable name="decoders" select="string(@item)"/>
            <variables include="{$decoders}">
                <xsl:for-each select="label">
                    <xsl:variable name="features" select="."/>
                    <xsl:variable name="index" select="position()"/>
                    <xsl:variable name="outputName" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='outputNames']/label)[$index]"/>
                    <xsl:variable name="outputLabel" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='outputLabel']/label)[$index]"/>
                    <xsl:variable name="baseCV" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='baseCV']/label)[$index]"/>
                    <xsl:if test="$features != 'N'">
                        <xsl:call-template name="functionOutput">
                            <xsl:with-param name="outputLabel" select="$outputName"/>
                            <xsl:with-param name="outputShort" select="$outputLabel"/>
                            <xsl:with-param name="CVbase" select="$baseCV"/>
                            <xsl:with-param name="features" select="$features"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
            </variables>
        </xsl:for-each>

    </xsl:template>

    <!-- install new variables at end of variables element-->
    <xsl:template match="variables">
        <variables>
            <xsl:copy-of select="node()"/>
        </variables>
        <xsl:call-template name="outputInfo"/>
    </xsl:template>
    
    <!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:include href="esu/v5fnDefs.xsl"/>
        
</xsl:stylesheet>
