package com.denisk.appengine.nl;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class Test {
	private static final String CATEGORY_URL_PREFIX = "category/";
	private static final String GOOD_URL_PREFIX = "good/";

	public static void main(String[] args) {
		String s = "category/agdubC1zdGFycggLEgFjGJIRDA/good/agdubC1zdGFychALEgFjGJIRDAsSAWcYkxEM/";
		String categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/good";
		RegExp p = RegExp.compile(categoryKeyRegexp);
		MatchResult exec = p.exec(s);
		System.out.println(exec.getGroup(1));
	}

}
