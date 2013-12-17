package com.baoyun.subsystems.cgf.asn1;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

// @formatter:off
/**
 * <pre> PGWRecord ::= SET
 * {
 * 	<b>recordType                   [0] RecordType,</b>
 * 	servedIMSI                   [3] IMSI,
 * 	<b>p-GWAddress                  [4] GSNAddress,</b>
 * 	<b>chargingID                   [5] ChargingID,</b>
 * 	<b>servingNodeAddress           [6] SEQUENCE OF GSNAddress,</b>
 * 	accessPointNameNI            [7] AccessPointNameNI OPTIONAL,
 * 	pdpPDNType                   [8] PDPType OPTIONAL,
 * 	servedPDPPDNAddress          [9] PDPAddress OPTIONAL,
 * 	dynamicAddressFlag           [11] DynamicAddressFlag OPTIONAL,
 * 	<b>recordOpeningTime            [13] TimeStamp,</b>
 * 	<b>duration                     [14] CallDuration,</b>
 * 	<b>causeForRecClosing           [15] CauseForRecClosing,</b>
 * 	diagnostics                  [16] Diagnostics OPTIONAL,
 * 	<b>recordSequenceNumber         [17] INTEGER OPTIONAL,</b>
 * 	nodeID                       [18] NodeID OPTIONAL,
 * 	recordExtensions             [19] ManagementExtensions OPTIONAL,
 * 	<b>localSequenceNumber          [20] LocalSequenceNumber OPTIONAL,</b>
 * 	apnSelectionMode             [21] APNSelectionMode OPTIONAL,
 * 	servedMSISDN                 [22] MSISDN OPTIONAL,
 * 	chargingCharacteristics      [23] ChargingCharacteristics,
 * 	chChSelectionMode            [24] ChChSelectionMode OPTIONAL,
 * 	iMSsignalingContext          [25] NULL OPTIONAL,
 * 	externalChargingID           [26] OCTET STRING OPTIONAL,
 * 	servinggNodePLMNIdentifier   [27] PLMN-Id OPTIONAL,
 * 	pSFurnishChargingInformation [28] PSFurnishChargingInformation OPTIONAL,
 * 	servedIMEISV                 [29] IMEI OPTIONAL,
 * 	<b>rATType                      [30] RATType OPTIONAL,</b>
 * 	mSTimeZone                   [31] MSTimeZone OPTIONAL,
 * 	userLocationInformation      [32] OCTET STRING OPTIONAL,
 * 	cAMELChargingInformation     [33] OCTET STRING OPTIONAL,
 * 	<b>listOfServiceData            [34] SEQUENCE OF ChangeOfServiceCondition OPTIONAL,</b>
 * 	<b>servingNodeType              [35] SEQUENCE OF ServingNodeType,</b>
 * 	servedMNNAI                  [36] SubscriptionID OPTIONAL,
 * 	p-GWPLMNIdentifier           [37] PLMN-Id OPTIONAL,
 * 	startTime                    [38] TimeStamp OPTIONAL,
 * 	stopTime                     [39] TimeStamp OPTIONAL,
 * 	served3gpp2MEID              [40] OCTET STRING OPTIONAL,
 * 	pDNConnectionID              [41] ChargingID OPTIONAL,
 * }
 * </pre>
 *
 *
 *
 */
// @formatter:on
public class InputPgwCdrObject implements Comparable<InputPgwCdrObject> {

	private static Logger log = LoggerFactory.getLogger(InputPgwCdrObject.class);

	private final AsnValue inputPgwCdr;

	private int recordType = Integer.MIN_VALUE;

	/**
	 *
	 */
	private InetAddress pgwAddress;

	private long chargingId = Long.MIN_VALUE;

	private List<InetAddress> servingNodeAddresses;

	private TimeStamp recordOpeningTime;

	private int duration = Integer.MIN_VALUE;

	private int causeForRecClosing = Integer.MIN_VALUE;

	private long recordSequenceNumber = Long.MIN_VALUE;

	private long localSequenceNumber = Long.MIN_VALUE;

	private int ratType = Integer.MIN_VALUE;

	private List<ChangeOfServiceCondition> listOfServiceData;

	private List<ServingNodeType> servingNodeTypes;

	private PgwCdrCategoryKey categoryKey;

	private PgwCdrMergingKey mergingKey;

	private boolean hasRecordSequenceNumber;

	private boolean hasLocalSequenceNumber;

	private boolean hasRatType;

	public InputPgwCdrObject(AsnValue inputPgwCdr) {

		this.inputPgwCdr = inputPgwCdr;
		try {
			Number recordSequenceNumber = MergingUtils
					.getRecordSequenceNumberValueOfInputCdr(inputPgwCdr);

			if (recordSequenceNumber == null) {

				hasRecordSequenceNumber = false;
			} else {

				hasRecordSequenceNumber = true;
				this.recordSequenceNumber = recordSequenceNumber.longValue();
			}
		} catch (Exception e) {
			log.error(
					"errors occur while getting recordSequenceNumber from input P-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasRecordSequenceNumber = false;
		}

		try {
			Number localSequenceNumber = MergingUtils
					.getLocalSequenceNumberValueOfInputCdr(inputPgwCdr);

			if (localSequenceNumber == null) {

				hasLocalSequenceNumber = false;
			} else {

				hasLocalSequenceNumber = true;
				this.localSequenceNumber = localSequenceNumber.longValue();
			}
		} catch (Exception e) {
			log.error(
					"errors occur while getting localSequenceNumber from input P-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasLocalSequenceNumber = false;
		}

		try {
			Number ratType = MergingUtils.getRatTypeValueOfInputCdr(inputPgwCdr);

			if (ratType == null) {

				hasRatType = false;
			} else {

				hasRatType = true;
				this.ratType = ratType.intValue();
			}
		} catch (Exception e) {
			log.error("errors occur while getting ratType from input P-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasRatType = false;
		}
	}

	@Override
	public String toString() {

		String stringRepr = "Input P-GW CDR: {";
		stringRepr += "recordType: " + getRecordType() + ", ";
		stringRepr += "p-GWAddress: " + getPgwAddress().getHostAddress() + ", ";
		stringRepr += "chargingID: " + getChargingId() + ", ";
		stringRepr += "servingNodeType: " + getServingNodeTypes() + ", ";
		stringRepr += "servingNodeAddress: " + getServingNodeAddresses() + ", ";
		stringRepr += "recordOpeningTime: " + getRecordOpeningTime() + ", ";
		stringRepr += "duration: " + getDuration() + ", ";
		stringRepr += "causeForRecClosing: " + getCauseForRecClosing() + ", ";
		stringRepr += "recordSequenceNumber: "
				+ (isHasRecordSequenceNumber() ? getRecordSequenceNumber() : null) + ", ";
		stringRepr += "localSequenceNumber: "
				+ (isHasLocalSequenceNumber() ? getLocalSequenceNumber() : null) + ", ";
		stringRepr += "rATType: " + (isHasRatType() ? getRatType() : null) + ", ";
		stringRepr += "}";

		return stringRepr;
	}

	@Override
	public int compareTo(InputPgwCdrObject another) {

		if (recordSequenceNumber > another.recordSequenceNumber) {
			return 1;
		} else if (recordSequenceNumber < another.recordSequenceNumber) {
			return -1;
		} else {
			return 0;
		}
	}

	public PgwCdrCategoryKey genPgwCdrCategoryKey() {

		if (categoryKey != null) {

			return categoryKey;

		} else {

			PgwCdrCategoryKey key = new PgwCdrCategoryKey();
			key.setChargingId(getChargingId());
			key.setPgwAddress(getPgwAddress());

			categoryKey = key;
			return key;
		}
	}

	public PgwCdrMergingKey genPgwCdrMergingKey() {

		if (mergingKey != null) {

			return mergingKey;
		} else {

			PgwCdrMergingKey key = new PgwCdrMergingKey();
			key.setChargingId(getChargingId());
			key.setPgwAddress(getPgwAddress());
			key.setRatType(getRatType());
			key.setServingNodeAddresss(getServingNodeAddresses());
			key.setServingNodeTypes(getServingNodeTypes());

			mergingKey = key;
			return key;
		}
	}

	public boolean isPartial() {

		return !isHasRecordSequenceNumber();
	}

	// getters/setters:

	public AsnValue getInputPgwCdr() {

		return inputPgwCdr;
	}

	/**
	 * @return the recordType
	 */
	public int getRecordType() {

		if (recordType == Integer.MIN_VALUE) {
			try {
				recordType = MergingUtils.getRecordTypeValueOfInputCdr(inputPgwCdr).intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting recordType of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return recordType;
	}

	/**
	 * @return the pgwAddress
	 */
	public InetAddress getPgwAddress() {

		if (pgwAddress == null) {
			try {
				pgwAddress = MergingUtils.getPgwAddressValueOfInputCdr(inputPgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting pgwAddress of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return pgwAddress;
	}

	/**
	 * @return the chargingId
	 */
	public long getChargingId() {

		if (chargingId == Long.MIN_VALUE) {
			try {
				chargingId = MergingUtils.getChargingIdValueOfInputCdr(inputPgwCdr).longValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting chargingId of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return chargingId;
	}

	/**
	 * @return the servingNodeAddresses
	 */
	public List<InetAddress> getServingNodeAddresses() {

		if (servingNodeAddresses == null) {
			try {
				servingNodeAddresses = MergingUtils
						.getServingNodeAddressesValueOfInputCdr(inputPgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting servingNodeAddresses of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return servingNodeAddresses;
	}

	/**
	 * @return the recordOpeningTime
	 */
	public TimeStamp getRecordOpeningTime() {

		if (recordOpeningTime == null) {
			try {
				recordOpeningTime = MergingUtils
						.getRecordOpeningTimeValueOfInputCdr(inputPgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting recordOpeningTime of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return recordOpeningTime;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {

		if (duration == Integer.MIN_VALUE) {
			try {
				duration = MergingUtils.getDurationValueOfInputCdr(inputPgwCdr).intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting duration of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return duration;
	}

	/**
	 * @return the causeForRecClosing
	 */
	public int getCauseForRecClosing() {

		if (causeForRecClosing == Integer.MIN_VALUE) {
			try {
				causeForRecClosing = MergingUtils.getCauseForRecClosingValueOfInputCdr(inputPgwCdr)
						.intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting causeForRecClosing of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return causeForRecClosing;
	}

	/**
	 * @return the recordSequenceNumber
	 */
	public long getRecordSequenceNumber() {

		if (isHasRecordSequenceNumber()) {
			return recordSequenceNumber;
		} else {
			return Long.MIN_VALUE;
		}
	}

	/**
	 * @return the localSequenceNumber
	 */
	public long getLocalSequenceNumber() {

		if (isHasLocalSequenceNumber()) {
			return localSequenceNumber;
		} else {
			return Long.MIN_VALUE;
		}
	}

	/**
	 * @return the ratType
	 */
	public int getRatType() {

		if (isHasRatType()) {
			return ratType;
		} else {
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * @return the listOfServiceData
	 */
	public List<ChangeOfServiceCondition> getListOfServiceData() {

		// FIXME: to impl.
		return listOfServiceData;
	}

	/**
	 * @return the servingNodeTypes
	 */
	public List<ServingNodeType> getServingNodeTypes() {

		if (servingNodeTypes == null) {
			try {
				servingNodeTypes = MergingUtils.getServingNodeTypesValueOfInputCdr(inputPgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting servingNodeTypes of underlying inputPgwCdr(AsnValue): {}, exception: {}.",
						inputPgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return servingNodeTypes;
	}

	public boolean isHasRecordSequenceNumber() {

		return hasRecordSequenceNumber;
	}

	public boolean isHasLocalSequenceNumber() {

		return hasLocalSequenceNumber;
	}

	public boolean isHasRatType() {

		return hasRatType;
	}
}
