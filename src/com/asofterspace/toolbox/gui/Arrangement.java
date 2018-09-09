package com.asofterspace.toolbox.gui;

import java.awt.GridBagConstraints;


/**
 * This is a helper class intended to be used to create grid bag contraints
 * more quickly and easily in-line (without having to specify ten million
 * arguments...)
 */
public class Arrangement extends GridBagConstraints {

	public Arrangement(int gridx, int gridy, double weightx, double weighty) {

		super();

		this.fill = GridBagConstraints.BOTH;
		this.gridx = gridx;
		this.gridy = gridy;
		this.weightx = weightx;
		this.weighty = weighty;
	}
}