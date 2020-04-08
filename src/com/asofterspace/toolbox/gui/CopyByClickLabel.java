/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JLabel;


/**
 * This is a regular JLabel with the difference that its text gets copied to the clipboard
 * when it is being clicked; by default, the text that is copied is the text displayed in
 * the label, but you can also set a different text to be copied
 */
public class CopyByClickLabel extends JLabel {

	public static final long serialVersionUID = 27398L;

	private String textToBeCopiedOnClick;


	public CopyByClickLabel(String text) {
		super(text);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String copiedText = textToBeCopiedOnClick;
				if (copiedText == null) {
					copiedText = getText();
				}
				StringSelection selection = new StringSelection(copiedText);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
	}

	public void setCopyText(String textToBeCopiedOnClick) {
		this.textToBeCopiedOnClick = textToBeCopiedOnClick;
	}
}
