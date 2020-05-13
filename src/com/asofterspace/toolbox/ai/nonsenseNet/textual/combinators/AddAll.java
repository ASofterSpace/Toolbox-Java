/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Combinator;
import com.asofterspace.toolbox.ai.nonsenseNet.textual.Node;

import java.util.List;


public class AddAll extends Combinator {

	@Override
	public String gatherInputFrom(List<Node> inputs) {

		if (inputs.size() < 1) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		for (Node input : inputs) {
			result.append(input.getValue());
		}
		return result.toString();
	}

}
