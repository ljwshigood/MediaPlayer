package com.mediaplayer.mediaplayerlistener.bean;

import java.io.Serializable;

public class RecogniBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String applicatioinId;

	private String recordId;

	private String luabin;
	private String eof;
	private String src;

	private String version;
	
	private Result result ;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getApplicatioinId() {
		return applicatioinId;
	}

	public void setApplicatioinId(String applicatioinId) {
		this.applicatioinId = applicatioinId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getLuabin() {
		return luabin;
	}

	public void setLuabin(String luabin) {
		this.luabin = luabin;
	}

	public String getEof() {
		return eof;
	}

	public void setEof(String eof) {
		this.eof = eof;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	

}
