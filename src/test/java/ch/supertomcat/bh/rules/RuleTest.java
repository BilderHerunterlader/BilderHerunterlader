package ch.supertomcat.bh.rules;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class RuleTest {
	private RuleIO ruleIO;

	@BeforeEach
	public void setUp() throws IOException, SAXException, JAXBException {
		ruleIO = new RuleIO();
	}

	@Test
	void testReadDefaultRule() throws IOException, JAXBException {
		ruleIO.readDefaultRule();
	}
}
