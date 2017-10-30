package com.asofterspace.toolbox.selftest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asofterspace.toolbox.io.JSON;

public class JSONTest {

	@Test
	public void fromStringTest() {
		
		JSON testObject = new JSON("{\"foo\":\"bar\"}");
		
		if (testObject.getString("foo").toString().equals("bar")) {
			return;
		}
		
		fail("We stored foo:bar in a JSON object, then read the key foo - and did not get bar!");
	}

	@Test
	public void toStringTest() {
		
		JSON testObject = new JSON("{\"foo\": \"bar\"}");
		
		if (testObject.toString().equals("{\"foo\": \"bar\"}")) {
			return;
		}
		
		fail("We stored foo:bar in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

}
