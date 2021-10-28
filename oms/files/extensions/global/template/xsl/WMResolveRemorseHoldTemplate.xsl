<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <Order>
            <xsl:attribute name="OrderHeaderKey">
                <xsl:value-of select="MonitorConsolidation/Order/OrderStatuses/OrderStatus/@OrderHeaderKey"/>
            </xsl:attribute>
                        <OrderHoldTypes>						
								<OrderHoldType>
									<xsl:attribute name="HoldType">WM_REMORSE_HOLD</xsl:attribute>									
									<xsl:attribute name="ReasonText">Resolving Remorse Hold</xsl:attribute>
									<xsl:attribute name="Status">1300</xsl:attribute>
								</OrderHoldType>
						</OrderHoldTypes>									
        </Order>
    </xsl:template>
</xsl:stylesheet>