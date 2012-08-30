package com.cj.jshintmojo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

public class JSHint {
	public static void main(String[] args) {
		try {
			InputStream source = new FileInputStream("/home/stu/projects/cj/cjo/intranet-web/src/test/javascript/account/BulkPidCreatorTest.js");
			List<Error> errors = new JSHint().run(source, "", "");
			
			System.out.println(errors.size() + " errors");
			for(Error error:errors){
				System.out.println("On line " + error.line + ", character " + error.character + ": " + error.reason);
				System.out.println(error.raw);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final Rhino rhino;

	public JSHint() {
		rhino = new Rhino();
		rhino.eval(resourceAsString("jshint.js"));
	}
	
	public List<Error> run(InputStream source, String options, String globals) {
		final List<Error> results = new ArrayList<JSHint.Error>();

		String sourceAsText = toString(source);
		
		Boolean codePassesMuster = rhino.call("JSHINT", sourceAsText, options, globals);

		if(!codePassesMuster){
			NativeArray errors = rhino.eval("JSHINT.errors");

			for(Object next : errors){
				Error error = new Error(new JSObject(next));
				results.add(error);
			}
		}

		return results;
	}

	private static String toString(InputStream in) {
		try {
			return IOUtils.toString(in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String resourceAsString(String name) {
		return toString(getClass().getResourceAsStream(name));
	}

	@SuppressWarnings("unchecked") 
	static class JSObject {
		private NativeObject a;

		public JSObject(Object o) {
			super();
			this.a = (NativeObject)o;
		}

		public <T> T dot(String name){
			return (T) a.get(name);
		}
	}

	public static class Error {
		public final String id, raw, evidence, reason;
		public final Double line, character;

		public Error(JSObject o) {
			id = o.dot("id");
			raw = o.dot("raw");
			evidence = o.dot("evidence");
			line = o.dot("line");
			character = o.dot("character");
			reason = o.dot("reason");
		}
	}
}