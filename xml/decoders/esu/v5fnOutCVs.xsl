<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xi="http://www.w3.org/2001/XInclude">
    
    <xsl:output method="xml" encoding="utf-8"/>

    <!-- Create function output CV settings for each decoder defined in the xml file -->
    <!-- install new variables at end of variables element-->
    
    <xsl:template match="variables">
        <variables>
            <xsl:copy-of select="node()"/>
        </variables>
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

    <!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Template for one function output -->

    <!-- Available output features are:

         "noSP": Output does not have the option called "Servo Power"
         "sO": Output options called "Servo output",
               "Servo output Steam engine Johnson Bar Control" and "Servo output Pantograph bouncing"
         "eS": Output option called "External controlled smoke unit"
         "sC": Output option called "Sound controlled smoke unit"
         "pa": Output option called "Pantograph"
         "rC": Output option called "Roco coupler"
         "eC": Output option called "ESU coupler"

         All other output options are the most common among all LokPilot 5 and LokSound 5 decoders.

         Determined from decoder manuals and LokProgrammer 5.2.18 -->
    
    <xsl:template name="functionOutput">
        <xsl:param name="outputLabel"/>
        <xsl:param name="outputShort"/>
        <xsl:param name="CVbase"/>
        <xsl:param name="features"/>

        <!-- output mode CV -->
        <variable label="{$outputLabel} Mode" CV="16.0.{$CVbase}" item="ESU FnOut {$outputShort} Mode">
            <enumVal xmlns:xi="http://www.w3.org/2001/XInclude"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
                <enumChoice value="0">
                    <choice>(Disabled)</choice>
                    <choice xml:lang="it">Disabile</choice>
                    <choice xml:lang="de">Deaktiviert</choice>
                    <choice xml:lang="ca">Desactivat</choice>
                </enumChoice>
                <enumChoice value="1">
                    <choice>Dimmable headlight</choice>
                    <choice xml:lang="it">Luce di testa regolabile</choice>
                    <choice xml:lang="de">Dimmbares Licht</choice>
                    <choice xml:lang="ca">Intensitat de lluns</choice>
                </enumChoice>
                <enumChoice value="2">
                    <choice>Dimmable headlight (fade in/out)</choice>
                    <choice xml:lang="it">Luce di testa regolabile (sfumata in/out)</choice>
                    <choice xml:lang="de">Dimmbares Licht (Ein- und Ausblenden)</choice>
                    <choice xml:lang="ca">Intensitat llum frontal</choice>
                </enumChoice>
                <enumChoice value="3">
                    <choice>Firebox</choice>
                    <choice xml:lang="it">Fuoco Caldaia</choice>
                    <choice xml:lang="de">Feuerbüchse</choice>
                    <choice xml:lang="ca">Caixa de focs</choice>
                </enumChoice>
                <enumChoice value="4">
                    <choice>Smart firebox</choice>
                    <choice xml:lang="it">Caldaia intelligente</choice>
                    <choice xml:lang="de">Intelligente Feuerbüchse</choice>
                </enumChoice>
                <enumChoice value="5">
                    <choice>Single strobe</choice>
                    <choice xml:lang="it">Singolo Strobe</choice>
                    <choice xml:lang="ca">Estroboscòpica simple</choice>
                </enumChoice>
                <enumChoice value="6">
                    <choice>Double strobe</choice>
                    <choice xml:lang="it">Doppio Strobe</choice>
                </enumChoice>
                <enumChoice value="7">
                    <choice>Rotary beacon</choice>
                    <choice xml:lang="it">Lampeggiante rotante</choice>
                    <choice xml:lang="ca">Balisa rotatòria</choice>
                </enumChoice>
                <enumChoice value="8">
                    <choice>Strato light</choice>
                    <choice xml:lang="it">Luce Strato</choice>
                    <choice xml:lang="ca">Llum de carrer</choice>
                </enumChoice>
                <enumChoice value="9">
                    <choice>Ditch light type 1</choice>
                    <choice xml:lang="it">Luce Ditch tipo 1</choice>
                    <choice xml:lang="ca">Llum Ditch tipus 1</choice>
                </enumChoice>
                <enumChoice value="10">
                    <choice>Ditch light type 2</choice>
                    <choice xml:lang="it">Luce Ditch tipo 2</choice>
                    <choice xml:lang="ca">Llum Ditch tupus 2</choice>
                </enumChoice>
                <enumChoice value="11">
                    <choice>Oscillating headlight</choice>
                    <choice xml:lang="it">Luce di testa Oscillante</choice>
                    <choice xml:lang="ca">Llum de testera oscil·lant</choice>
                </enumChoice>
                <enumChoice value="12">
                    <choice>Flash light</choice>
                    <choice xml:lang="it">Luce a Flash</choice>
                    <choice xml:lang="de">Blinklicht</choice>
                    <choice xml:lang="ca">Llum intermitent</choice>
                </enumChoice>
                <enumChoice value="13">
                    <choice>Mars light</choice>
                    <choice xml:lang="it">Luce Mars</choice>
                    <choice xml:lang="ca">Llums Mars</choice>
                </enumChoice>
                <enumChoice value="14">
                    <choice>Gyra light</choice>
                    <choice xml:lang="it">Gyra light rotante</choice>
                    <choice xml:lang="ca">Llum Gyra Rotatòria</choice>
                </enumChoice>
                <enumChoice value="15">
                    <choice>End of train flasher</choice>
                    <choice xml:lang="it">Lampeggiante Fine Treno (FRED)</choice>
                    <choice xml:lang="ca">Llum de final de tren intermitent</choice>
                </enumChoice>
                <enumChoice value="16">
                    <choice>Neon light</choice>
                    <choice xml:lang="it">Luce Fluorescente</choice>
                    <choice xml:lang="de">Neonlicht</choice>
                    <choice xml:lang="ca">Llum fluorescent</choice>
                </enumChoice>
                <enumChoice value="17">
                    <choice>Low-energy light</choice>
                    <choice xml:lang="it">Luce a risparmio Energetico</choice>
                    <choice xml:lang="de">Energiesparlampe</choice>
                    <choice xml:lang="ca">Llum estalvi energia</choice>
                </enumChoice>
                <enumChoice value="18">
                    <choice>Single Strobe random</choice>
                </enumChoice>
                <enumChoice value="19">
                    <choice>Brake Light</choice>
                </enumChoice>
                <enumChoice value="20">
                    <choice>16 2/3 Hz flickering</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'eC')">
                    <enumChoice value="21">
                        <choice>ESU coupler</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'sC')">
                    <enumChoice value="22">
                        <choice>Sound controlled smoke unit</choice>
                        <choice xml:lang="it">Gener.Fumo sonoro</choice>
                        <choice xml:lang="de">Raucheinheit (Soundgesteuert)</choice>
                        <choice xml:lang="ca">Fumigen controlat per llum</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="23">
                    <choice>Ventilator</choice>
                    <choice xml:lang="it">Controllo Ventole</choice>
                    <choice xml:lang="de">Ventilator</choice>
                    <choice xml:lang="ca">Ventilador</choice>
                </enumChoice>
                <enumChoice value="24">
                    <choice>Seuthe smoke unit</choice>
                    <choice xml:lang="it">Gener.Fumo Seuthe</choice>
                    <choice xml:lang="de">Seuthe Rauchgenerator</choice>
                    <choice xml:lang="ca">Fumigen Seuthe</choice>
                </enumChoice>
                <enumChoice value="25">
                    <choice>Trigger smoke chuff</choice>
                    <choice xml:lang="it">Chuff fumo Trigger</choice>
                    <choice xml:lang="de">Dampfstoß-Trigger</choice>
                    <choice xml:lang="ca">Disparador del chuff del fumigen</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'eS')">
                    <enumChoice value="26">
                        <choice>External controlled smoke unit</choice>
                        <choice xml:lang="de">externer Rauchgenerator</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="27">
                        <choice>Servo output</choice>
                        <choice xml:lang="it">Servo</choice>
                        <choice xml:lang="de">Servoausgang</choice>
                        <choice xml:lang="ca">Servo</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="28">
                    <choice>Coupler</choice>
                    <choice xml:lang="it">Gancio</choice>
                    <choice xml:lang="de">Kupplung</choice>
                    <choice xml:lang="ca">Enganxall</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'rC')">
                    <enumChoice value="29">
                        <choice>Roco coupler</choice>
                        <choice xml:lang="it">Gancio Roco</choice>
                        <choice xml:lang="de">Roco Kupplung</choice>
                        <choice xml:lang="ca">Enganxall Roco</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'pa')">
                    <enumChoice value="30">
                        <choice>Pantograph</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="31">
                    <choice>PowerPack control</choice>
                </enumChoice>
                <xsl:if test="not(contains($features, 'noSP'))">
                    <enumChoice value="32">
                        <choice>Servo Power</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="33">
                    <choice>Autocoupler coil#2</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="34">
                        <choice>Servo output Steam engine Johnson Bar Control</choice>
                        <choice xml:lang="de">Servo Dampflok Umsteuerung</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="35">
                    <choice>Trigger smoke chuff (Edge Toggle)</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="36">
                        <choice>Servo output Pantograph bouncing</choice>
                    </enumChoice>
                </xsl:if>
            </enumVal>
        </variable>

    </xsl:template>
</xsl:stylesheet>
