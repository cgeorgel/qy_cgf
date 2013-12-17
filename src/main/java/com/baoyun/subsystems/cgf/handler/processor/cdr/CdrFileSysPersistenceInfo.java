package com.baoyun.subsystems.cgf.handler.processor.cdr;

public class CdrFileSysPersistenceInfo {

	private String fileNamePattern;

	private String folder;

	private long snGen = 1;

	public CdrFileSysPersistenceInfo() {

	}

	public synchronized long genSn() {

		return snGen++;
	}

	public String getFileNamePattern() {

		return fileNamePattern;
	}

	public void setFileNamePattern(String fileNamePattern) {

		this.fileNamePattern = fileNamePattern;
	}

	public String getFolder() {

		return folder;
	}

	public void setFolder(String folder) {

		this.folder = folder;
	}
}
