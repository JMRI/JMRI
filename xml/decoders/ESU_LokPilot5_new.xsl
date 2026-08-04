<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
    <xsl:output method="xml" encoding="utf-8"/>

    <!-- list function outputs for each supported decoder here -->

    <!-- Available output features are:

         "sP": Output option called "Servo Power"
         "sO": Output options called "Servo output",
               "Servo output Steam engine Johnson Bar Control" and "Servo output Pantograph bouncing"
         "eS": Output option called "External controlled smoke unit"
         "sC": Output option called "Sound controlled smoke unit"
         "pa": Output option called "Pantograph"
         "rC": Output option called "Roco coupler"
         "eC": Output option called "ESU coupler"

         All other output options are common among all LokPilot 5 and LokSound 5 decoders.

         Determined from decoder manuals and LokProgrammer 5.2.18 -->
    
    
    <xsl:template name="outputInfo">

        <variables include="LokPilot 5 micro Next18,LokPilot 5 micro Next18 DCC,LokPilot 5 Fx micro Next18">
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'Headlight [1]'"/>
                <xsl:with-param name="outputShort" select="'HL-1'"/>
                <xsl:with-param name="CVbase" select="259"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'Headlight [2]'"/>
                <xsl:with-param name="outputShort" select="'HL-2'"/>
                <xsl:with-param name="CVbase" select="419"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'Rearlight [1]'"/>
                <xsl:with-param name="outputShort" select="'RL-1'"/>
                <xsl:with-param name="CVbase" select="267"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'Rearlight [2]'"/>
                <xsl:with-param name="outputShort" select="'RL-2'"/>
                <xsl:with-param name="CVbase" select="427"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX1 [1]'"/>
                <xsl:with-param name="outputShort" select="'A1-1'"/>
                <xsl:with-param name="CVbase" select="275"/>
                <xsl:with-param name="features" select="'eS,rC,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX1 [2]'"/>
                <xsl:with-param name="outputShort" select="'A1-2'"/>
                <xsl:with-param name="CVbase" select="435"/>
                <xsl:with-param name="features" select="'eS,rC,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX2 [1]'"/>
                <xsl:with-param name="outputShort" select="'A2-1'"/>
                <xsl:with-param name="CVbase" select="283"/>
                <xsl:with-param name="features" select="'rC,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX2 [2]'"/>
                <xsl:with-param name="outputShort" select="'A2-2'"/>
                <xsl:with-param name="CVbase" select="443"/>
                <xsl:with-param name="features" select="'rC,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX3'"/>
                <xsl:with-param name="outputShort" select="'A3'"/>
                <xsl:with-param name="CVbase" select="291"/>
                <xsl:with-param name="features" select="'sO,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX4'"/>
                <xsl:with-param name="outputShort" select="'A4'"/>
                <xsl:with-param name="CVbase" select="299"/>
                <xsl:with-param name="features" select="'sO,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX5'"/>
                <xsl:with-param name="outputShort" select="'A5'"/>
                <xsl:with-param name="CVbase" select="307"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX6'"/>
                <xsl:with-param name="outputShort" select="'A6'"/>
                <xsl:with-param name="CVbase" select="315"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX7'"/>
                <xsl:with-param name="outputShort" select="'A7'"/>
                <xsl:with-param name="CVbase" select="323"/>
                <xsl:with-param name="features" select="'eC,sP'"/>                
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX8'"/>
                <xsl:with-param name="outputShort" select="'A8'"/>
                <xsl:with-param name="CVbase" select="331"/>
                <xsl:with-param name="features" select="'eC,sP'"/>                
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX9'"/>
                <xsl:with-param name="outputShort" select="'A9'"/>
                <xsl:with-param name="CVbase" select="339"/>
                <xsl:with-param name="features" select="'pa,sP'"/>                
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX10'"/>
                <xsl:with-param name="outputShort" select="'A10'"/>
                <xsl:with-param name="CVbase" select="347"/>
                <xsl:with-param name="features" select="'pa,sP'"/>                
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX11'"/>
                <xsl:with-param name="outputShort" select="'A11'"/>
                <xsl:with-param name="CVbase" select="355"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX12'"/>
                <xsl:with-param name="outputShort" select="'A12'"/>
                <xsl:with-param name="CVbase" select="363"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX13'"/>
                <xsl:with-param name="outputShort" select="'A13'"/>
                <xsl:with-param name="CVbase" select="371"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX14'"/>
                <xsl:with-param name="outputShort" select="'A14'"/>
                <xsl:with-param name="CVbase" select="379"/>
                <xsl:with-param name="features" select="'sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX15'"/>
                <xsl:with-param name="outputShort" select="'A15'"/>
                <xsl:with-param name="CVbase" select="387"/>
                <xsl:with-param name="features" select="'eC,sO,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX16'"/>
                <xsl:with-param name="outputShort" select="'A16'"/>
                <xsl:with-param name="CVbase" select="395"/>
                <xsl:with-param name="features" select="'eC,sO,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX17'"/>
                <xsl:with-param name="outputShort" select="'A17'"/>
                <xsl:with-param name="CVbase" select="403"/>
                <xsl:with-param name="features" select="'sO,sP'"/>
            </xsl:call-template>
            <xsl:call-template name="functionOutput">
                <xsl:with-param name="outputLabel" select="'AUX18'"/>
                <xsl:with-param name="outputShort" select="'A18'"/>
                <xsl:with-param name="CVbase" select="411"/>
                <xsl:with-param name="features" select="'sO,sP'"/>
            </xsl:call-template>

</variables>
        <!-- end "LokPilot 5 micro Next18,LokPilot 5 micro Next18 DCC,LokPilot 5 Fx micro Next18" -->

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
