package ch.supertomcat.bh.manualtest;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.rules.RuleIO;
import jakarta.xml.bind.JAXBException;

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
