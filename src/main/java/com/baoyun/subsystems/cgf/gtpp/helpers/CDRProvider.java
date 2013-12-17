/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.helpers;

import java.util.List;

/**
 *
 */
public interface CDRProvider {

	public int getDataRecordFormat();
	
	public int getDataRecordFormatVersion();
	
	public List<byte[]> getCDRs();
	
}
