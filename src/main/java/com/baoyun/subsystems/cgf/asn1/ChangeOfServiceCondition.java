package com.baoyun.subsystems.cgf.asn1;

/**
 * <pre> ChangeOfServiceCondition ::= SEQUENCE
 * {
 * 	--
 * 	-- Used for Flow based Charging service data container
 * 	--
 * 	ratingGroup                   [1] RatingGroupId,
 * 	chargingRuleBaseName          [2] ChargingRuleBaseName OPTIONAL,
 * 	resultCode                    [3] ResultCode OPTIONAL,
 * 	localSequenceNumber           [4] LocalSequenceNumber OPTIONAL,
 * 	timeOfFirstUsage              [5] TimeStamp OPTIONAL,
 * 	timeOfLastUsage               [6] TimeStamp OPTIONAL,
 * 	timeUsage                     [7] CallDuration OPTIONAL,
 * 	serviceConditionChange        [8] ServiceConditionChange,
 * 	qoSInformationNeg             [9] EPCQoSInformation OPTIONAL,
 * 	servingNodeAddress            [10] GSNAddress OPTIONAL,
 * 	datavolumeFBCUplink           [12] DataVolumeGPRS OPTIONAL,
 * 	datavolumeFBCDownlink         [13] DataVolumeGPRS OPTIONAL,
 * 	timeOfReport                  [14] TimeStamp,
 * 	failureHandlingContinue       [16] FailureHandlingContinue OPTIONAL,
 * 	serviceIdentifier             [17] ServiceIdentifier OPTIONAL,
 * 	pSFurnishChargingInformation  [18] PSFurnishChargingInformation OPTIONAL,
 * 	aFRecordInformation           [19] SEQUENCE OF AFRecordInformation OPTIONAL,
 * 	userLocationInformation       [20] OCTET STRING OPTIONAL,
 * 	eventBasedChargingInformation [21] EventBasedChargingInformation OPTIONAL,
 * 	timeQuotaMechanism            [22] TimeQuotaMechanism OPTIONAL,
 * 	serviceSpecificInfo           [23] SEQUENCE OF ServiceSpecificInfo OPTIONAL
 * }
 * </pre>
 *
 * @author George
 *
 */
public class ChangeOfServiceCondition {

	private TimeStamp timeOfFirstUsage;

	private TimeStamp timeOfLastUsage;

	private int timeUsage;

	public ChangeOfServiceCondition() {

	}
}
