package org.spark.examples.model;

public class AuthEvent {

	String timestamp;
	String sourceHost;
	String process;
	String message;

	public AuthEvent(String timestamp, String sourceHost, String process,
			String message) {
		this.timestamp = timestamp;
		this.sourceHost = sourceHost;
		this.process = process;
		this.message = message;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getSourceHost() {
		return sourceHost;
	}
	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}
	public String getProcess() {
		return process;
	}
	public void setProcess(String process) {
		this.process = process;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	
}
