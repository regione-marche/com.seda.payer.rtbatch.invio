package com.seda.payer.rtbatch.invio;

/**
 * Metadati da utilizzare per l'invio della RT al web service di versamento
 */
public class VersamentoMetadata {

	private String versioneWS = "1.4";
	private String loginName = "";
	private String password = "";
	private String xmlSip = "";
	private String fileId = "ID1";
	private byte[] fileContent;

	public String getVersioneWS() {
		return versioneWS;
	}

	public void setVersioneWS(String versioneWS) {
		this.versioneWS = versioneWS;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getXmlSip() {
		return xmlSip;
	}

	public void setXmlSip(String xmlSip) {
		this.xmlSip = xmlSip;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

}
