/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
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

		this.anchor = GridBagConstraints.CENTER;
		
		this.gridx = gridx;
		this.gridy = gridy;
		
		this.weightx = weightx;
		this.weighty = weighty;
		
		// only fill the available space if the component is supposed to take up more space!
		if (weightx == 0.0) {
			if (weighty == 0.0) {
				this.fill = GridBagConstraints.NONE;
			} else {
				this.fill = GridBagConstraints.VERTICAL;
			}
		} else {
			if (weighty == 0.0) {
				this.fill = GridBagConstraints.HORIZONTAL;
			} else {
				this.fill = GridBagConstraints.BOTH;
			}
		}
	}
}
