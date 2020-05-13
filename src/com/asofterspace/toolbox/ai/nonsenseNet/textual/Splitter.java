/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual;

import com.asofterspace.toolbox.ai.nonsenseNet.GenericSplitter;


public abstract class Splitter extends GenericSplitter {

	public abstract String[] distribute(String value);
}
