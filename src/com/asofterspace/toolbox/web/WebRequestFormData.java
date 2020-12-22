/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This represents the content of a request to our web server
 * in the case of form data being contained in the request
 *
 * @author Moya (a softer space, 2019)
 */
public class WebRequestFormData {

	private Map<String, WebRequestFormDataBlock> data;


	public WebRequestFormData() {

		data = new HashMap<>();
	}

	public void addBlock(String block) {

		WebRequestFormDataBlock newBlock = new WebRequestFormDataBlock(block);

		data.put(newBlock.getName(), newBlock);
	}

	public WebRequestFormDataBlock getByName(String name) {

		return data.get(name);
	}

	public Set<String> getNames() {
		return data.keySet();
	}

}
