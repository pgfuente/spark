package org.spark.examples.model;

public class WebEvents {

	String timestamp;
	String sourceHost;
	String method;
	String url;
	String httpCode;

	public WebEvents(String timestamp, String sourceHost, String method,
			String url, String httpCode) {
		this.timestamp = timestamp;
		this.sourceHost = sourceHost;
		this.method = method;
		this.url = url;
		this.httpCode = httpCode;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}

}
