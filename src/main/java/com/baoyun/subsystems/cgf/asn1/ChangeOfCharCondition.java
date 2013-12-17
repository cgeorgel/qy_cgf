package com.baoyun.subsystems.cgf.asn1;
/**
 *
 * ChangeOfCharCondition	::= SEQUENCE
{
	--
	-- qosRequested and qosNegotiated are used in S-CDR only
	-- ePCQoSInformation used in SGW-CDR only
	--
	qosRequested				[1] QoSInformation OPTIONAL,
	qosNegotiated				[2] QoSInformation OPTIONAL,
	dataVolumeGPRSUplink		[3] DataVolumeGPRS OPTIONAL,
	dataVolumeGPRSDownlink		[4] DataVolumeGPRS OPTIONAL,
	changeCondition				[5] ChangeCondition,
	changeTime					[6] TimeStamp,
	userLocationInformation		[8] OCTET STRING OPTIONAL,
	ePCQoSInformation			[9] EPCQoSInformation OPTIONAL
}

EPCQoSInformation	::= SEQUENCE
{
	--
	-- See TS 29.212 [88] for more information
	-- 
	qCI						[1] INTEGER,
	maxRequestedBandwithUL	[2] INTEGER OPTIONAL,
	maxRequestedBandwithDL	[3] INTEGER OPTIONAL,
	guaranteedBitrateUL		[4] INTEGER OPTIONAL,
	guaranteedBitrateDL		[5] INTEGER OPTIONAL,
	aRP						[6] INTEGER OPTIONAL
}
 * @author BaoXu
 *
 */
public class ChangeOfCharCondition {
	public ChangeOfCharCondition(){
		
	}
}
