package com.baoyun.subsystems.cgf.asn1;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;


import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingKey;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

//@formatter:off
/**
*<pre>  SGWRecord 	::= SET
*{
*	recordType					[0] RecordType,
*	servedIMSI					[3] IMSI,
*	s-GWAddress					[4] GSNAddress,
*	chargingID					[5] ChargingID,
*	servingNodeAddress			[6] SEQUENCE OF GSNAddress,
*	accessPointNameNI			[7] AccessPointNameNI OPTIONAL,
*	pdpPDNType					[8] PDPType OPTIONAL,
*	servedPDPPDNAddress			[9] PDPAddress OPTIONAL,
*	dynamicAddressFlag			[11] DynamicAddressFlag OPTIONAL,
*	listOfTrafficVolumes		[12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
*	recordOpeningTime			[13] TimeStamp,
*	duration					[14] CallDuration,
*	causeForRecClosing			[15] CauseForRecClosing,
*	diagnostics					[16] Diagnostics OPTIONAL,
*	recordSequenceNumber		[17] INTEGER OPTIONAL,
*	nodeID						[18] NodeID OPTIONAL,
*	recordExtensions			[19] ManagementExtensions OPTIONAL,
*	localSequenceNumber			[20] LocalSequenceNumber OPTIONAL,
*	apnSelectionMode			[21] APNSelectionMode OPTIONAL,
*	servedMSISDN				[22] MSISDN OPTIONAL,
*	chargingCharacteristics		[23] ChargingCharacteristics,
*	chChSelectionMode			[24] ChChSelectionMode OPTIONAL,
*	iMSsignalingContext			[25] NULL OPTIONAL,
*	servingNodePLMNIdentifier	[27] PLMN-Id OPTIONAL,
*	servedIMEISV				[29] IMEI OPTIONAL,
*	rATType						[30] RATType OPTIONAL,
*	mSTimeZone 					[31] MSTimeZone OPTIONAL,
*	userLocationInformation		[32] OCTET STRING OPTIONAL,
*	sGWChange					[34] SGWChange OPTIONAL,
*	servingNodeType				[35] SEQUENCE OF ServingNodeType,
*	p-GWAddressUsed				[36] GSNAddress OPTIONAL,
*	p-GWPLMNIdentifier			[37] PLMN-Id OPTIONAL,
*	startTime					[38] TimeStamp OPTIONAL,
*	stopTime					[39] TimeStamp OPTIONAL,
*	pDNConnectionID				[40] ChargingID OPTIONAL,
*	servedPDPPDNAddressExt 		[41] PDPAddress OPTIONAL

*}</pre>
 * @author BaoXu
 *
 */

public class InputSgwCdrObject implements Comparable<InputSgwCdrObject>{
	private static Logger log = LoggerFactory.getLogger(InputPgwCdrObject.class);

	private final AsnValue inputSgwCdr;

	
	private int recordType = Integer.MIN_VALUE;

	private InetAddress sgwAddress;
	
	private InetAddress pgwAddress;

	
	private long chargingId = Long.MIN_VALUE;

	private List<InetAddress> servingNodeAddresses;

	private TimeStamp recordOpeningTime;

	private int duration = Integer.MIN_VALUE;

	private int causeForRecClosing = Integer.MIN_VALUE;

	private long recordSequenceNumber = Long.MIN_VALUE;

	private long localSequenceNumber = Long.MIN_VALUE;

	private int ratType = Integer.MIN_VALUE;

	private List<ChangeOfCharCondition> listOfTrafficVolumes;

	private List<ServingNodeType> servingNodeTypes;

	private SgwCdrCategoryKey categoryKey;

	private SgwCdrMergingKey mergingKey;

	private boolean hasRecordSequenceNumber;

	private boolean hasLocalSequenceNumber;

	private boolean hasRatType;
	
	
	
	public InputSgwCdrObject(AsnValue inputSgwCdr) {
		this.inputSgwCdr = inputSgwCdr;
		try {
			Number recordSequenceNumber = MergingUtils
					.getRecordSequenceNumberValueOfInputCdr(inputSgwCdr);

			if (recordSequenceNumber == null) {

				hasRecordSequenceNumber = false;
			} else {

				hasRecordSequenceNumber = true;
				this.recordSequenceNumber = recordSequenceNumber.longValue();
			}
		} catch (Exception e) {
			log.error(
					"errors occur while getting recordSequenceNumber from input S-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasRecordSequenceNumber = false;
		}

		try {
			Number localSequenceNumber = MergingUtils
					.getLocalSequenceNumberValueOfInputCdr(inputSgwCdr);

			if (localSequenceNumber == null) {

				hasLocalSequenceNumber = false;
			} else {

				hasLocalSequenceNumber = true;
				this.localSequenceNumber = localSequenceNumber.longValue();
			}
		} catch (Exception e) {
			log.error(
					"errors occur while getting localSequenceNumber from input S-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasLocalSequenceNumber = false;
		}

		try {
			Number ratType = MergingUtils.getRatTypeValueOfInputCdr(inputSgwCdr);

			if (ratType == null) {

				hasRatType = false;
			} else {

				hasRatType = true;
				this.ratType = ratType.intValue();
			}
		} catch (Exception e) {
			log.error("errors occur while getting ratType from input S-GW CDR, exception: {}.",
					MiscUtils.exceptionStackTrace2String(e));

			hasRatType = false;
		}
	}	
	
	@Override
	public String toString() {

		String stringRepr = "Input S-GW CDR: {";
		stringRepr += "recordType: " + getRecordType() + ", ";
		stringRepr += "s-GWAddress: " + getSgwAddress().getHostAddress() + ", ";
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
	public int compareTo(InputSgwCdrObject another) {
		if (recordSequenceNumber > another.recordSequenceNumber) {
			return 1;
		} else if (recordSequenceNumber < another.recordSequenceNumber) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public SgwCdrCategoryKey genSgwCdrCategoryKey() {

		if (categoryKey != null) {

			return categoryKey;

		} else {

			SgwCdrCategoryKey key = new SgwCdrCategoryKey();
			key.setChargingId(getChargingId());
			key.setPgwAddress(getPgwAddress());
			key.setRatType(getRatType());

			categoryKey = key;
			return key;
		}
	}

	public SgwCdrMergingKey genSgwCdrMergingKey() {

		if (mergingKey != null) {

			return mergingKey;
		} else {

			SgwCdrMergingKey key = new SgwCdrMergingKey();
			key.setChargingId(getChargingId());
			key.setSgwAddress(getSgwAddress());
			key.setPgwAddress(getPgwAddress());
			key.setRatType(getRatType());
			key.setServingNodeAddresses(getServingNodeAddresses());
			key.setServingNodeTypes(getServingNodeTypes());

			mergingKey = key;
			return key;
		}
	}
	
	public boolean isPartial() {

		return !isHasRecordSequenceNumber();
	}
	
	
	//getter方法
	
	
	public AsnValue getInputSgwCdr() {
		return inputSgwCdr;
	}


	public int getRecordType() {
		if (recordType == Integer.MIN_VALUE) {
			try {
				recordType = MergingUtils.getRecordTypeValueOfInputCdr(inputSgwCdr).intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting recordType of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return recordType;
	}


	public InetAddress getSgwAddress() {
		if (sgwAddress == null) {
			try {
				sgwAddress = MergingUtils.getPgwAddressValueOfInputCdr(inputSgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting pgwAddress of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return sgwAddress;
	}
	
	public InetAddress getPgwAddress() {
		if (pgwAddress == null) {
			try {
				pgwAddress = MergingUtils.getPgwAddressValueOfInputCdr(inputSgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting pgwAddress of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return pgwAddress;
	}




	public long getChargingId() {
		if (chargingId == Long.MIN_VALUE) {
			try {
				chargingId = MergingUtils.getChargingIdValueOfInputCdr(inputSgwCdr).longValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting chargingId of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return chargingId;
	}


	public List<InetAddress> getServingNodeAddresses() {
		if (servingNodeAddresses == null) {
			try {
				servingNodeAddresses = MergingUtils
						.getServingNodeAddressesValueOfInputCdr(inputSgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting servingNodeAddresses of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return servingNodeAddresses;
	}


	public TimeStamp getRecordOpeningTime() {
		if (recordOpeningTime == null) {
			try {
				recordOpeningTime = MergingUtils
						.getRecordOpeningTimeValueOfInputCdr(inputSgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting recordOpeningTime of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return recordOpeningTime;
	}


	public int getDuration() {
		if (duration == Integer.MIN_VALUE) {
			try {
				duration = MergingUtils.getDurationValueOfInputCdr(inputSgwCdr).intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting duration of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return duration;
	}


	public int getCauseForRecClosing() {
		if (causeForRecClosing == Integer.MIN_VALUE) {
			try {
				causeForRecClosing = MergingUtils.getDurationValueOfInputCdr(inputSgwCdr)
						.intValue();
			} catch (Exception e) {
				log.error(
						"errors occur while getting causeForRecClosing of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
			}
		}

		return causeForRecClosing;
	}


	public long getRecordSequenceNumber() {
		if (isHasRecordSequenceNumber()) {
			return recordSequenceNumber;
		} else {
			return Long.MIN_VALUE;
		}
	}


	public long getLocalSequenceNumber() {
		if (isHasLocalSequenceNumber()) {
			return localSequenceNumber;
		} else {
			return Long.MIN_VALUE;
		}
	}


	public int getRatType() {
		if (isHasRatType()) {
			return ratType;
		} else {
			return Integer.MIN_VALUE;
		}
	}


	public List<ChangeOfCharCondition> getListOfTrafficVolumes() {
		//同Pgw相同，需要拼接的字段
		return listOfTrafficVolumes;
	}


	public List<ServingNodeType> getServingNodeTypes() {
		if (servingNodeTypes == null) {
			try {
				servingNodeTypes = MergingUtils.getServingNodeTypesValueOfInputCdr(inputSgwCdr);
			} catch (Exception e) {
				log.error(
						"errors occur while getting servingNodeTypes of underlying inputSgwCdr(AsnValue): {}, exception: {}.",
						inputSgwCdr, MiscUtils.exceptionStackTrace2String(e));
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
