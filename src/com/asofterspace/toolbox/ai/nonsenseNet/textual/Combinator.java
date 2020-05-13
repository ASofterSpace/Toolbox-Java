/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual;

import com.asofterspace.toolbox.ai.nonsenseNet.GenericCombinator;

import java.util.List;


public abstract class Combinator extends GenericCombinator {

	public abstract String gatherInputFrom(List<Node> inputs);
}
