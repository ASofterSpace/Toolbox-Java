/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Node;

import java.util.ArrayList;
import java.util.List;


public class NodeCtrl {

	private int NODES_PER_LEVEL = 100;
	private int LEVELS = 5;
	private int CALCULATION_ROUNDS = 10;

	private List<Node> nodes;


	public NodeCtrl() {

		this.nodes = new ArrayList<>();

		for (int level = 0; level < LEVELS; level++) {
			for (int i = 0; i < NODES_PER_LEVEL; i++) {
				Node node = new Node();
				// add some random ins and outs, and some random combinators, operations and splitters
				// node.addInput();
				nodes.add(node);
			}
		}
	}

	public void run() {

		for (int i = 0; i < CALCULATION_ROUNDS; i++) {
			for (Node node : nodes) {
				node.calculate();
			}
		}
	}

}
