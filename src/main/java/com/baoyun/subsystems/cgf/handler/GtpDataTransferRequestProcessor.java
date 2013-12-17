package com.baoyun.subsystems.cgf.handler;

import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessage;

public interface GtpDataTransferRequestProcessor {

	GtpPrimeMessage process(GtpPrimeDataRecordTransferRequest dataRecordTranReq) throws Exception;
}
