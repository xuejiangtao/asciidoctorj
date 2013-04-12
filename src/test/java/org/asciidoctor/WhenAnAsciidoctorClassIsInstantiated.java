package org.asciidoctor;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.google.common.io.CharStreams;

public class WhenAnAsciidoctorClassIsInstantiated {

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
	
	@Test
	public void file_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		String render_file = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", new HashMap<String, Object>());
		assertRenderedFile(render_file);
		
	}
	
	@Test
	public void file_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options().inPlace(true).asMap());

		File expectedFile = new File("target/test-classes/rendersample.html");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void file_document_should_be_rendered_into_foreign_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		Map<String, Object> options = options()
										.inPlace(false)
										.safe(SafeMode.UNSAFE)
										.toDir(testFolder.getRoot())
									.asMap();
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options);

		File expectedFile = new File(testFolder.getRoot(),"rendersample.html");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void docbook_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		Map<String, Object> attributes = attributes().backend("docbook").asMap();
		Map<String, Object> options = options()
										.inPlace(true)
										.attributes(attributes)
									  .asMap();
		
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options);

		File expectedFile = new File("target/test-classes/rendersample.xml");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void string_content_with_custom_date_should_be_rendered() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/documentwithdate.asciidoc");
		
		Calendar customDate = Calendar.getInstance();
		customDate.set(Calendar.YEAR, 2012);
		customDate.set(Calendar.MONTH, 11);
		customDate.set(Calendar.DATE, 5);
		
		Map<String, Object> attributes = attributes().localDate(customDate.getTime()).asMap();
		Map<String, Object> options = options()
										.attributes(attributes)
									  .asMap();
		
		String render_file = asciidoctor.render(toString(content), options);
		assertRenderedLocalDateContent(render_file, "2012-12-05.");
		
	}
	
	@Test
	public void string_content_with_custom_time_should_be_rendered() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/documentwithtime.asciidoc");
		
		Calendar customTime = Calendar.getInstance();
		customTime.set(Calendar.HOUR_OF_DAY, 23);
		customTime.set(Calendar.MINUTE, 15);
		customTime.set(Calendar.SECOND, 0);
		
		Map<String, Object> attributes = attributes().localTime(customTime.getTime()).asMap();
		Map<String, Object> options = options()
										.attributes(attributes)
									  .asMap();
		
		String render_file = asciidoctor.render(toString(content), options);
		
		assertRenderedLocalDateContent(render_file, "23:15:00 CEST.");
		
	}
	
	@Test
	public void string_content_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/rendersample.asciidoc");
		String render_file = asciidoctor.render(toString(content), new HashMap<String, Object>());
		
		assertRenderedFile(render_file);
	}
	
	private void assertRenderedLocalDateContent(String render_content, String contentDateOrTime) throws IOException, SAXException, ParserConfigurationException {
		Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(render_content.getBytes())));
		
		assertThat(renderFileSource, hasXPath("/div/div[@class='sectionbody']/div/p", is(contentDateOrTime)));
		
	}
	
	private void assertRenderedFile(String render_file) throws IOException, SAXException, ParserConfigurationException {
		Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(render_file.getBytes())));
		
		assertThat(renderFileSource, hasXPath("/div[@class='sect1']"));
		assertThat(renderFileSource, hasXPath("/div/h2[@id='_section_a']"));
		assertThat(renderFileSource, hasXPath("/div/h2", is("Section A")));
		assertThat(renderFileSource, hasXPath("/div/div[@class='sectionbody']"));
	}
	
	private static String toString(InputStream inputStream) throws IOException {
		return CharStreams.toString( new InputStreamReader( inputStream ));
	}
	
	private static org.w3c.dom.Document inputStream2Document(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
	    DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
	    newInstance.setNamespaceAware(true);
	    org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(inputStream);
	    return parse;
	}
	
}
