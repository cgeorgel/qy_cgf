package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.exception.UnrecognizedMessageException;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeaderFactory;
import com.baoyun.subsystems.cgf.utils.PrintUtils;

public class GtpPrimeMessageFactory {

	public static GtpPrimeMessage createEchoRequestMessage() {
		GtpPrimeMessage msg = new GtpPrimeEchoRequest();
		return msg;
	}

	public static GtpPrimeMessage createEchoResponseMessage(short restartCounter) {
		GtpPrimeMessage msg = new GtpPrimeEchoResponse(restartCounter);
		return msg;
	}

	public static GtpPrimeMessage createNodeAliveRequestMessage(String ip) {
		GtpPrimeMessage msg = new GtpPrimeNodeAliveRequest(ip);
		return msg;
	}	

	public static GtpPrimeMessage createNodeAliveRequestMessage(String ip, String ip2) {
		GtpPrimeMessage msg = new GtpPrimeNodeAliveRequest(ip,ip2);
		return msg;
	}		
	
	public static GtpPrimeMessage createNodeAliveResponseMessage() {
		GtpPrimeMessage msg = new GtpPrimeNodeAliveResponse();
		return msg;
	}		

	public static GtpPrimeMessage createRedirectionRequestMessage(short cause, String ip) {
		GtpPrimeMessage msg = new GtpPrimeRedirectionRequest(cause, ip);
		return msg;
	}	

	public static GtpPrimeMessage createRedirectionRequestMessage(short cause, String ip, String ip2) {
		GtpPrimeMessage msg = new GtpPrimeRedirectionRequest(cause, ip, ip2);
		return msg;
	}		
	
	public static GtpPrimeMessage createRedirectionResponseMessage(short cause) {
		GtpPrimeMessage msg = new GtpPrimeRedirectionResponse(cause);
		return msg;
	}	

	public static GtpPrimeMessage createDataTransferCDRRequestMessage() {
		GtpPrimeMessage msg = new GtpPrimeDataRecordTransferCDRRequest();
		return msg;
	}		

	public static GtpPrimeMessage createDataTransferCDRDuplRequestMessage() {
		GtpPrimeMessage msg = new GtpPrimeDataRecordTransferCDRDuplRequest();
		return msg;
	}			
	
	public static GtpPrimeMessage createDataTransferSequenceCancelRequestMessage() {
		GtpPrimeMessage msg = new GtpPrimeDataRecordTransferSequenceCancel();
		return msg;
	}
	
	public static GtpPrimeMessage createDataTransferSequenceReleaseRequestMessage() {
		GtpPrimeMessage msg = new GtpPrimeDataRecordTransferSequenceRelease();
		return msg;
	}
	
	private static GtpPrimeMessage decodeDataRecordTransfer(GtpPrimeHeader header, byte[] msg) {
		GtpPrimeMessage tmp = null;
		
		switch (msg[7]) {
			case Constants.GTP_PRIME_IET_PTC_SEND_DATA:
				tmp = new GtpPrimeDataRecordTransferCDRRequest(header,msg);
				break;
			case Constants.GTP_PRIME_IET_PTC_SEND_DUPLDATA:
				tmp = new GtpPrimeDataRecordTransferCDRDuplRequest(header,msg);
				break;
			case Constants.GTP_PRIME_IET_PTC_CANCEL_DATA:
				tmp = new GtpPrimeDataRecordTransferSequenceCancel(header,msg);
				break;
			case Constants.GTP_PRIME_IET_PTC_RELEASE_DATA:
				tmp = new GtpPrimeDataRecordTransferSequenceRelease(header,msg);
				break;
		}
		return tmp;

	}
	
	public static GtpPrimeMessage decodeFromByteArray(byte[] msg) throws UnrecognizedMessageException {
		GtpPrimeHeader header = GtpPrimeHeaderFactory.decodeFromByteArray(msg);
		//PrintUtils.printHex(msg);
		GtpPrimeMessage tmp = null;
		
		switch (header.getMessageType()) {
		case Constants.GTP_PRIME_ECHO_REQUEST:
			tmp = new GtpPrimeEchoRequest(header, msg);
			break;
		case Constants.GTP_PRIME_ECHO_RESPONSE:
			tmp = new GtpPrimeEchoResponse(header, msg);
			break;
		
		case Constants.GTP_PRIME_NODE_ALIVE_REQUEST:
			tmp = new GtpPrimeNodeAliveRequest(header, msg);			
			break;

		case Constants.GTP_PRIME_NODE_ALIVE_RESPONSE:
			tmp = new GtpPrimeNodeAliveResponse(header, msg);
			break;			
		
		case Constants.GTP_PRIME_VERSION_NOT_SUPPORTED:			
			tmp = new GtpPrimeVersionNotSupported(header,msg);
			break;

		case Constants.GTP_PRIME_REDIRECTION_REQUEST:			
			tmp = new GtpPrimeRedirectionRequest(header,msg);
			break;

		case Constants.GTP_PRIME_REDIRECTION_RESPONSE:	
			tmp = new GtpPrimeRedirectionResponse(header,msg);
			break;
			
		case Constants.GTP_PRIME_DATA_RECORD_TRANSFER_REQUEST:			
			tmp = decodeDataRecordTransfer(header,msg);
			break;

		case Constants.GTP_PRIME_DATA_RECORD_TRANSFER_RESPONSE:			
			tmp = new GtpPrimeDataRecordTransferResponse(header,msg);
			break;			
			
		default:
			tmp = new GtpPrimeMessage();
			break;

		
		}
		
		return tmp;
	}

	
}
