package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.XlsxSheet;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class XlsxTest implements Test {

	@Override
	public void runAll() {

		rowColTest();
	}

	public void rowColTest() {

		TestUtils.start("Xlsx Row/Column");
		
		boolean success = true;
		
		success = success && "A0".equals(XlsxSheet.colRowToName(0, 0));
		success = success && "J9".equals(XlsxSheet.colRowToName(9, 9));
		success = success && "Z25".equals(XlsxSheet.colRowToName(25, 25));
		success = success && "AA26".equals(XlsxSheet.colRowToName(26, 26));
		success = success && "AB27".equals(XlsxSheet.colRowToName(27, 27));
		
		success = success && "A".equals(XlsxSheet.nameToCol("A0"));
		success = success && (0 == XlsxSheet.nameToColI("A0"));
		success = success && "0".equals(XlsxSheet.nameToRow("A0"));
		success = success && (0 == XlsxSheet.nameToRowI("A0"));
		
		success = success && "J".equals(XlsxSheet.nameToCol("J9"));
		success = success && (9 == XlsxSheet.nameToColI("J9"));
		success = success && "9".equals(XlsxSheet.nameToRow("J9"));
		success = success && (9 == XlsxSheet.nameToRowI("J9"));
		
		success = success && "Z".equals(XlsxSheet.nameToCol("Z25"));
		success = success && (25 == XlsxSheet.nameToColI("Z25"));
		success = success && "25".equals(XlsxSheet.nameToRow("Z25"));
		success = success && (25 == XlsxSheet.nameToRowI("Z25"));
		
		success = success && "AA".equals(XlsxSheet.nameToCol("AA26"));
		success = success && (26 == XlsxSheet.nameToColI("AA26"));
		success = success && "26".equals(XlsxSheet.nameToRow("AA26"));
		success = success && (26 == XlsxSheet.nameToRowI("AA26"));

		success = success && "AB".equals(XlsxSheet.nameToCol("AB27"));
		success = success && (27 == XlsxSheet.nameToColI("AB27"));
		success = success && "27".equals(XlsxSheet.nameToRow("AB27"));
		success = success && (27 == XlsxSheet.nameToRowI("AB27"));
		
		success = success && "E17".equals(XlsxSheet.colRowToName(XlsxSheet.nameToColI("E17"), XlsxSheet.nameToRowI("E17")));
		success = success && "CD9".equals(XlsxSheet.colRowToName(XlsxSheet.nameToColI("CD9"), XlsxSheet.nameToRowI("CD9")));
		success = success && "FZ99".equals(XlsxSheet.colRowToName(XlsxSheet.nameToColI("FZ99"), XlsxSheet.nameToRowI("FZ99")));
		
		if (success) {
			TestUtils.succeed();
		} else {
			TestUtils.fail("Oh no - some of the row/column operations on XlsxSheets are broken!");
		}
	}

}
