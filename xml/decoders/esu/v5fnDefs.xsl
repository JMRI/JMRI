<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >    
    <xsl:output method="xml" encoding="utf-8"/>

    <!-- Template for function output -->

    <!-- The different output types are:
         Type 1: Type 8 + Servo Power
         Type 2: Type 1 + Roco Coupler + External Controlled Smoke Unit
         Type 3: Type 1 + Roco Coupler
         Type 4: Type 1 + ESU Coupler
         Type 5: Type 1 + Pantograph
         Type 6: Type 1 + Servo
         Type 7: Type 1 + ESU Coupler + Servo
         Type 8: Common among all LokPilot 5 / LokSound 5 versions
         Type 9: Type 8 + Roco Coupler + External Controlled Smoke Unit
         Type 10: Type 8 + Roco Coupler
         Type 11 to Type 20: Type 1 to Type 10 + Smoke unit (controlled by sound)

         Determined from decoder manuals and LokProgrammer 5.2.18 -->
    
    <xsl:template name="functionOutput">
        <xsl:param name="outputLabel"/>
        <xsl:param name="outputShort"/>
        <xsl:param name="CVbase"/>
        <xsl:param name="type"/>

        <!-- output mode CV -->
        <variable label="{$outputLabel} Mode" CV="16.0.{$CVbase}" item="ESU FnOut {$outputShort} Mode">
            <enumVal xmlns:xi="http://www.w3.org/2001/XInclude"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
                <enumChoice choice="(Disabled)" value="0">
                    <choice xml:lang="it">Disabile</choice>
                    <choice xml:lang="de">Deaktiviert</choice>
                    <choice xml:lang="ca">Desactivat</choice>
                </enumChoice>
                <enumChoice choice="Dimmable headlight" value="1">
                    <choice xml:lang="it">Luce di testa regolabile</choice>
                    <choice xml:lang="de">Dimmbares Licht</choice>
                    <choice xml:lang="ca">Intensitat de lluns</choice>
                </enumChoice>
                <enumChoice choice="Dimmable headlight (fade in/out)" value="2">
                    <choice xml:lang="it">Luce di testa regolabile (sfumata in/out)</choice>
                    <choice xml:lang="de">Dimmbares Licht (Ein- und Ausblenden)</choice>
                    <choice xml:lang="ca">Intensitat llum frontal</choice>
                </enumChoice>
                <enumChoice choice="Firebox" value="3">
                    <choice xml:lang="it">Fuoco Caldaia</choice>
                    <choice xml:lang="de">Feuerbüchse</choice>
                    <choice xml:lang="ca">Caixa de focs</choice>
                </enumChoice>
                <enumChoice choice="Smart firebox" value="4">
                    <choice xml:lang="it">Caldaia intelligente</choice>
                    <choice xml:lang="de">Intelligente Feuerbüchse</choice>
                </enumChoice>
                <enumChoice choice="Single strobe" value="5">
                    <choice xml:lang="it">Singolo Strobe</choice>
                    <choice xml:lang="ca">Estroboscòpica simple</choice>
                </enumChoice>
                <enumChoice choice="Double strobe" value="6">
                    <choice xml:lang="it">Doppio Strobe</choice>
                </enumChoice>
                <enumChoice choice="Rotary beacon" value="7">
                    <choice xml:lang="it">Lampeggiante rotante</choice>
                    <choice xml:lang="ca">Balisa rotatòria</choice>
                </enumChoice>
                <enumChoice choice="Strato light" value="8">
                    <choice xml:lang="it">Luce Strato</choice>
                    <choice xml:lang="ca">Llum de carrer</choice>
                </enumChoice>
                <enumChoice choice="Ditch light type 1" value="9">
                    <choice xml:lang="it">Luce Ditch tipo 1</choice>
                    <choice xml:lang="ca">Llum Ditch tipus 1</choice>
                </enumChoice>
                <enumChoice choice="Ditch light type 2" value="10">
                    <choice xml:lang="it">Luce Ditch tipo 2</choice>
                    <choice xml:lang="ca">Llum Ditch tupus 2</choice>
                </enumChoice>
                <enumChoice choice="Oscillating headlight" value="11">
                    <choice xml:lang="it">Luce di testa Oscillante</choice>
                    <choice xml:lang="ca">Llum de testera oscil·lant</choice>
                </enumChoice>
                <enumChoice choice="Flash light" value="12">
                    <choice xml:lang="it">Luce a Flash</choice>
                    <choice xml:lang="de">Blinklicht</choice>
                    <choice xml:lang="ca">Llum intermitent</choice>
                </enumChoice>
                <enumChoice choice="Mars light" value="13">
                    <choice xml:lang="it">Luce Mars</choice>
                    <choice xml:lang="ca">Llums Mars</choice>
                </enumChoice>
                <enumChoice choice="Gyra light" value="14">
                    <choice xml:lang="it">Gyra light rotante</choice>
                    <choice xml:lang="ca">Llum Gyra Rotatòria</choice>
                </enumChoice>
                <enumChoice choice="End of train flasher" value="15">
                    <choice xml:lang="it">Lampeggiante Fine Treno (FRED)</choice>
                    <choice xml:lang="ca">Llum de final de tren intermitent</choice>
                </enumChoice>
                <enumChoice choice="Neon light" value="16">
                    <choice xml:lang="it">Luce Fluorescente</choice>
                    <choice xml:lang="de">Neonlicht</choice>
                    <choice xml:lang="ca">Llum fluorescent</choice>
                </enumChoice>
                <enumChoice choice="Low-energy light" value="17">
                    <choice xml:lang="it">Luce a risparmio Energetico</choice>
                    <choice xml:lang="de">Energiesparlampe</choice>
                    <choice xml:lang="ca">Llum estalvi energia</choice>
                </enumChoice>
                <enumChoice choice="Single Strobe random" value="18">
                </enumChoice>
                <enumChoice choice="Brake Light" value="19">
                </enumChoice>
                <enumChoice choice="16 2/3 Hz flickering" value="20">
                </enumChoice>
                <xsl:if test="$type = 4 or $type = 7 or $type = 14 or $type = 17">
                    <enumChoice choice="ESU coupler" value="21">
                    </enumChoice>
                </xsl:if>
                <xsl:if test="$type > 10">
                    <enumChoice choice="Sound controlled smoke unit" value="22">
                        <choice xml:lang="it">Gener.Fumo sonoro</choice>
                        <choice xml:lang="de">Raucheinheit (Soundgesteuert)</choice>
                        <choice xml:lang="ca">Fumigen controlat per llum</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice choice="Ventilator" value="23">
                    <choice xml:lang="it">Controllo Ventole</choice>
                    <choice xml:lang="de">Ventilator</choice>
                    <choice xml:lang="ca">Ventilador</choice>
                </enumChoice>
                <enumChoice choice="Seuthe smoke unit" value="24">
                    <choice xml:lang="it">Gener.Fumo Seuthe</choice>
                    <choice xml:lang="de">Seuthe Rauchgenerator</choice>
                    <choice xml:lang="ca">Fumigen Seuthe</choice>
                </enumChoice>
                <enumChoice choice="Trigger smoke chuff" value="25">
                    <choice xml:lang="it">Chuff fumo Trigger</choice>
                    <choice xml:lang="de">Dampfstoß-Trigger</choice>
                    <choice xml:lang="ca">Disparador del chuff del fumigen</choice>
                </enumChoice>
                <xsl:if test="$type = 2 or $type = 9 or $type = 12">
                    <enumChoice choice="External controlled smoke unit" value="26">
                        <choice>External controlled smoke unit</choice>
                        <choice xml:lang="de">externer Rauchgenerator</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="$type = 6 or $type = 7 or $type = 16 or $type = 17">
                    <enumChoice choice="Servo output" value="27">
                        <choice xml:lang="it">Servo</choice>
                        <choice xml:lang="de">Servoausgang</choice>
                        <choice xml:lang="ca">Servo</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice choice="Coupler" value="28">
                    <choice xml:lang="it">Gancio</choice>
                    <choice xml:lang="de">Kupplung</choice>
                    <choice xml:lang="ca">Enganxall</choice>
                </enumChoice>
                <xsl:if test="$type = 2 or $type = 3 or $type = 9 or $type = 10 or $type = 12 or $type = 13">
                    <enumChoice choice="Roco coupler" value="29">
                        <choice xml:lang="it">Gancio Roco</choice>
                        <choice xml:lang="de">Roco Kupplung</choice>
                        <choice xml:lang="ca">Enganxall Roco</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="$type = 5 or $type = 15">
                    <enumChoice choice="Pantograph" value="30">
                    </enumChoice>
                </xsl:if>
                <enumChoice choice="PowerPack control" value="31">
                </enumChoice>
                <xsl:if test="$type &lt;= 7 or $type &gt;= 10">
                    <enumChoice choice="Servo Power" value="32">
                    </enumChoice>
                </xsl:if>
                <enumChoice choice="Autocoupler coil#2" value="33">
                </enumChoice>
                <xsl:if test="$type = 6 or $type = 7 or $type = 16 or $type = 17">
                    <enumChoice choice="Servo output Steam engine Johnson Bar Control" value="34">
                        <choice xml:lang="de">Servo Dampflok Umsteuerung</choice>
                    </enumChoice>
                </xsl:if>
                
                    
            </enumVal>

        </variable>
    </xsl:template>
</xsl:stylesheet>
