/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual;

import com.asofterspace.toolbox.ai.nonsenseNet.GenericNode;

import java.util.ArrayList;
import java.util.List;


public class Node extends GenericNode {

	private List<Node> inputs;

	private List<Node> outputs;

	private Combinator combinator;

	private List<Operation> operations;

	private Splitter splitter;

	private String[] outputValues;

	private int currentlyAccessedOutVal;


	public Node() {

		this.inputs = new ArrayList<>();

		this.outputs = new ArrayList<>();

		this.operations = new ArrayList<>();

		this.outputValues = new String[0];

		currentlyAccessedOutVal = 0;
	}

	public void addInput(Node inNode) {
		inputs.add(inNode);
	}

	public void addOutput(Node outNode) {
		outputs.add(outNode);
	}

	@Override
	public void calculate() {

		String curValue = combinator.gatherInputFrom(inputs);

		for (Operation operation : operations) {
			curValue = operation.applyTo(curValue);
		}

		outputValues = splitter.distribute(curValue);

		currentlyAccessedOutVal = 0;
	}

	public String getValue() {

		String result = outputValues[currentlyAccessedOutVal];

		currentlyAccessedOutVal++;

		if (currentlyAccessedOutVal > outputValues.length) {
			currentlyAccessedOutVal = 0;
		}

		return result;
	}

}
