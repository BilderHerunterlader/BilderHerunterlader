package ch.supertomcat.bh.manualtest;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.rules.RuleIO;

class DefaultRuleTest {
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
