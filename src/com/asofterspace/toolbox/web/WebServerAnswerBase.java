/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.util.ArrayList;
import java.util.List;


public abstract class WebServerAnswerBase implements WebServerAnswer {

	protected byte[] data;

	protected String preferredCacheParadigm = null;

	protected int status = WebServer.DEFAULT_STATUS;

	protected List<String> extraHeaderLines = null;


	@Override
	public byte[] getBinaryContent() {

		return this.data;
	}

	@Override
	public long getContentLength() {

		return this.data.length;
	}

	@Override
	public String getPreferredCacheParadigm() {

		if (this.preferredCacheParadigm != null) {
			return this.preferredCacheParadigm;
		}

		return "no-store";
	}

	public void setPreferredCacheParadigm(String preferredCacheParadigm) {
		this.preferredCacheParadigm = preferredCacheParadigm;
	}

	@Override
	public List<String> getExtraHeaderLines() {
		return this.extraHeaderLines;
	}

	public void setExtraHeaderLine(String extraHeaderLine) {
		if (this.extraHeaderLines == null) {
			this.extraHeaderLines = new ArrayList<>();
		}
		this.extraHeaderLines.add(extraHeaderLine);
	}

	public void setExtraHeaderLines(List<String> extraHeaderLines) {
		this.extraHeaderLines = extraHeaderLines;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
