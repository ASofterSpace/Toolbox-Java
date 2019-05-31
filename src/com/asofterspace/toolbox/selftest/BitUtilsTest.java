/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.BitUtils;


public class BitUtilsTest implements Test {

	@Override
	public void runAll() {

		toStringTest();

		boolArrEqualsTest();

		byteToBitsTest();
	}

	public void toStringTest() {

		TestUtils.start("Boolean[] Equals");

		boolean[] one1 = {true, true, false, false};
		boolean[] two1 = {false, true, false};
		boolean[] three = {false, true, false, false, true};

		if (!BitUtils.toString(one1).equals("1100")) {
			TestUtils.fail("The toString method did not give us 1100!");
			return;
		}

		if (!BitUtils.toString(two1).equals("010")) {
			TestUtils.fail("The toString method did not give us 010!");
			return;
		}

		if (!BitUtils.toString(three).equals("01001")) {
			TestUtils.fail("The toString method did not give us 01001!");
			return;
		}

		TestUtils.succeed();
	}

	public void boolArrEqualsTest() {

		TestUtils.start("Boolean[] Equals");

		boolean[] one1 = {true, true, false, false};
		boolean[] one2 = {true, true, false, false};
		boolean[] two1 = {false, true, false};
		boolean[] two2 = {false, true, false};
		boolean[] three = {false, true, false, false, true};

		if (!BitUtils.equals(null, null)) {
			TestUtils.fail("The boolean arrays null and null were not seen as equal!");
			return;
		}

		if (BitUtils.equals(one1, null)) {
			TestUtils.fail("The boolean arrays one1 and null were seen as equal!");
			return;
		}

		if (BitUtils.equals(null, one2)) {
			TestUtils.fail("The boolean arrays null and one2 were seen as equal!");
			return;
		}

		if (BitUtils.equals(one1, two1)) {
			TestUtils.fail("The boolean arrays one1 and two1 were seen as equal!");
			return;
		}

		if (BitUtils.equals(one1, three)) {
			TestUtils.fail("The boolean arrays one1 and three were seen as equal!");
			return;
		}

		if (BitUtils.equals(two2, three)) {
			TestUtils.fail("The boolean arrays two2 and three were seen as equal!");
			return;
		}

		if (!BitUtils.equals(one1, one2)) {
			TestUtils.fail("The boolean arrays one1 and one2 were not seen as equal!");
			return;
		}

		if (!BitUtils.equals(two1, two2)) {
			TestUtils.fail("The boolean arrays two1 and two2 were not seen as equal!");
			return;
		}

		if (!BitUtils.equals(three, three)) {
			TestUtils.fail("The boolean arrays three and three were not seen as equal!");
			return;
		}

		TestUtils.succeed();
	}

	public void byteToBitsTest() {

		TestUtils.start("Byte to Bits");

		byte byte1 = 1;
		boolean[] arr1 = {false, false, false, false, false, false, false, true};

		byte byte2 = 120;
		boolean[] arr2 = {false, true, true, true, true, false, false, false};

		byte byte3 = -53;
		// this is: true, followed by 128 - 53 = 75
		boolean[] arr3 = {true, true, false, false, true, false, true, true};

		boolean[] res1 = BitUtils.byteToBits(byte1);

		if (!BitUtils.equals(arr1, res1)) {
			TestUtils.fail("The call byteToBits(byte1) did not give us the correct result, but gave instead " + BitUtils.toString(res1) + "!");
			return;
		}

		boolean[] res2 = BitUtils.byteToBits(byte2);

		if (!BitUtils.equals(arr2, res2)) {
			TestUtils.fail("The call byteToBits(byte2) did not give us the correct result, but gave instead " + BitUtils.toString(res2) + "!");
			return;
		}

		boolean[] res3 = BitUtils.byteToBits(byte3);

		if (!BitUtils.equals(arr3, res3)) {
			TestUtils.fail("The call byteToBits(byte3) did not give us the correct result, but gave instead " + BitUtils.toString(res3) + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void unsignedByteToBitsTest() {

		TestUtils.start("Unsigned Byte to Bits");

		int byte1 = 1;
		boolean[] arr1 = {false, false, false, false, false, false, false, true};

		int byte2 = 120;
		boolean[] arr2 = {false, true, true, true, true, false, false, false};

		int byte3 = 75;
		boolean[] arr3 = {false, true, false, false, true, false, true, true};

		int byte4 = 203;
		boolean[] arr4 = {true, true, false, false, true, false, true, true};

		boolean[] res1 = BitUtils.unsignedByteToBits(byte1);

		if (!BitUtils.equals(arr1, res1)) {
			TestUtils.fail("The call unsignedByteToBits(byte1) did not give us the correct result, but gave instead " + BitUtils.toString(res1) + "!");
			return;
		}

		boolean[] res2 = BitUtils.unsignedByteToBits(byte2);

		if (!BitUtils.equals(arr2, res2)) {
			TestUtils.fail("The call unsignedByteToBits(byte2) did not give us the correct result, but gave instead " + BitUtils.toString(res2) + "!");
			return;
		}

		boolean[] res3 = BitUtils.unsignedByteToBits(byte3);

		if (!BitUtils.equals(arr3, res3)) {
			TestUtils.fail("The call unsignedByteToBits(byte3) did not give us the correct result, but gave instead " + BitUtils.toString(res3) + "!");
			return;
		}

		boolean[] res4 = BitUtils.unsignedByteToBits(byte4);

		if (!BitUtils.equals(arr4, res4)) {
			TestUtils.fail("The call unsignedByteToBits(byte4) did not give us the correct result, but gave instead " + BitUtils.toString(res4) + "!");
			return;
		}

		TestUtils.succeed();
	}

}
