package com.fotonauts.lackr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;

public class TestMustache extends BaseTestSubstitution {

	public TestMustache(String clientImplementation) throws Exception {
		super(clientImplementation);
	}

	@Test
	public void testMustache() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name}} value:{{value}}\n" + "<!-- /lackr:mustache:template -->\n"
		        + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\" }\n" + "<!-- /lackr:mustache:eval -->\n");
		assertEquals("\n\nsome text from the template name:the name value:the value\n\n", result);
	}

	@Test
	public void testMustacheWithEsi() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name}} value:{{value}} some:{{esi.some}}\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\", \"esi\":\"ssi:include:virtual:/esi.json\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n");
		assertEquals("\n\nsome text from the template name:the name value:the value some:json crap\n\n", result);
	}

	@Test
	public void testMustacheTemplateWithEsi() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "<!--# include virtual=\"/esi.must\" -->\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\", \"esi\":\"ssi:include:virtual:/esi.json\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n");
		assertEquals("\n\nsome text from the template name:the name value:the value some:json crap\n\n", result);
	}

	@Test
	public void testMustacheParseException() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name} value:{{value}}\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n", true);
		assertNotNull("result is an error", result);
		assertTrue(result.contains("MustacheParseException"));
	}

	@Test
	public void testMustacheJsonParseException() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name}} value:{{value}}\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name \"value\": \"the value\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n", true);
		assertNotNull("result is an error", result);
		assertTrue(result.contains("JsonParseException"));
	}

	// This test is now irrelevant (and broken) as we use a default value in case a value is missing or null, 
	// and I'm at a loss trying 
	// to find another possible error case.
	@Ignore
	@Test
	public void testMustacheException() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name}} value:{{value}} blah:{{esi.blih}}\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\", \"esi\":\"ssi:include:virtual:/esi.json\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n", true);
		assertNotNull("result is an error", result);
		assertTrue(result.contains("MustacheException"));
	}

	@Test
	public void testMustacheTemplateNotFound() throws Exception {
		String result = expand("<!-- lackr:mustache:template name=\"template_name\" -->\n"
		        + "some text from the template name:{{name}} value:{{value}} blah:{{esi.blih}}\n"
		        + "<!-- /lackr:mustache:template -->\n" + "<!-- lackr:mustache:eval name=\"bogus_template_name\" -->\n"
		        + "{ \"name\": \"the name\", \"value\": \"the value\", \"esi\":\"ssi:include:virtual:/esi.json\" }\n"
		        + "<!-- /lackr:mustache:eval -->\n", true);
		assertNotNull("result is an error", result);
		assertTrue(result.contains("Mustache template not found"));
	}
}