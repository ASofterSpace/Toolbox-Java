package com.asofterspace.toolbox.selftest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
//import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ConfigFileTest.class,
  JSONTest.class,
})
public class AllTests {
}
