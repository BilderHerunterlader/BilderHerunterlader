package ch.supertomcat.bh.hoster.hostimpl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleIO;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;

/**
 * Host-Class for Rules
 * 
 * @version 0.5
 */
public class HostRules extends Host implements IHoster, IRedirect {
	/**
	 * Version
	 */
	public static final String VERSION = "0.5";

	/**
	 * Name
	 */
	public static final String NAME = "HostRules";

	/**
	 * Domains
	 */
	private List<String> domains;

	/**
	 * Rules
	 */
	private List<Rule> rules = new ArrayList<>();

	/**
	 * Flag if developer rules are enabled
	 */
	private boolean developerRulesEnabled = false;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Rule IO
	 */
	private final RuleIO ruleIO;

	/**
	 * Constructor
	 * 
	 * @param hostManager Host Manager
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws IOException
	 */
	public HostRules(HostManager hostManager) throws IOException, SAXException, JAXBException {
		super(NAME, VERSION, false);
		this.hostManager = hostManager;
		this.ruleIO = new RuleIO();
		domains = new ArrayList<>();
		domains.add("NODOMAINS");

		// DEVELOPER RULES
		File ruleFolderDeveloper = new File(ApplicationProperties.getProperty("ApplicationPath"), "developerrules");
		// Do NOT create folder, if it not exists
		loadRules(ruleFolderDeveloper, true);

		// NORMAL RULES
		File ruleFolder = new File(ApplicationProperties.getProperty("ApplicationPath"), "rules");
		// Create folder, if it not exists
		if (!ruleFolder.exists()) {
			try {
				Files.createDirectories(ruleFolder.toPath());
			} catch (IOException e) {
				logger.error("Could not create directory: {}", ruleFolder, e);
			}
		}
		loadRules(ruleFolder, false);
	}

	/**
	 * Add Rule
	 * 
	 * @param rule Rule
	 */
	public synchronized void addRule(Rule rule) {
		rules.add(rule);
	}

	/**
	 * Remove Rule
	 * 
	 * @param rule Rule
	 */
	public synchronized void removeRule(Rule rule) {
		int index = -1;
		int i = 0;
		for (Rule r : rules) {
			// Compare reference
			if (r == rule) {
				index = i;
			}
			i++;
		}

		if (index >= 0) {
			rules.remove(index);
		}
	}

	/**
	 * Save all rules
	 * 
	 * @return True if all rules were saved successfully, false otherwise
	 */
	public synchronized boolean saveAllRules() {
		boolean result = true;
		for (Rule rule : rules) {
			try {
				ruleIO.writeRule(rule);
			} catch (IOException | JAXBException e) {
				logger.error("Could not save rule {} {}: {}", rule.getName(), rule.getVersion(), rule.getFile(), e);
				result = false;
			}
		}
		return result;
	}

	/**
	 * Save rule
	 * 
	 * @param rule Rule
	 * 
	 * @return True if rule was saved successfully, false otherwise
	 */
	public synchronized boolean saveRule(Rule rule) {
		try {
			ruleIO.writeRule(rule);
			return true;
		} catch (IOException | JAXBException e) {
			logger.error("Could not save rule {} {}: {}", rule.getName(), rule.getVersion(), rule.getFile(), e);
			return false;
		}
	}

	/**
	 * @return Rules
	 */
	public synchronized List<Rule> getRules() {
		return rules;
	}

	/**
	 * @param ruleName Rule Name
	 * @return Rule or null
	 */
	public synchronized Rule getRuleByName(String ruleName) {
		return rules.stream().filter(x -> x.getName().equals(ruleName)).findFirst().orElse(null);
	}

	/**
	 * @return Domains
	 */
	public List<String> getDomains() {
		return domains;
	}

	@Override
	public boolean isFromThisHoster(String url) {
		for (Rule rule : rules) {
			if (rule.isFromThisHoster(url, null)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the rule which is responsible to parse the url
	 * 
	 * @param url URL
	 * @return Rule
	 */
	public Rule getRuleForURL(String url) {
		for (Rule rule : rules) {
			if (rule.isFromThisHoster(url, null)) {
				return rule;
			}
		}
		return null;
	}

	@Override
	public String getFilenameFromURL(String url) {
		for (Rule rule : rules) {
			if (rule.isFromThisHoster(url, null)) {
				return rule.getFilename(url);
			}
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		for (Rule rule : rules) {
			if (rule.isFromThisHoster(upo.getContainerURL(), upo.getLastRule())) {
				upo.addHoster(rule);
				String result[] = rule.getURLAndFilename(upo);
				upo.setDirectLink(result[0]);
				upo.setCorrectedFilename(result[1]);
				if (rule.getDefinition().isResend()) {
					upo.setContainerURL(result[0]);
					hostManager.parseURL(upo);
				}
				return;
			}
		}
	}

	@Override
	public boolean isFromThisRedirect(URL url) {
		for (Rule rule : rules) {
			if (rule.isFromThisRedirect(url.getURL(), null)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getURL(URL url) {
		String redirectedURL = url.getURL();

		/*
		 * As we had to rewrite some code if we wanted to also parse URL's and not only URLParseObjects
		 * we create a URLParseObject and a Pic.
		 */
		Pic pic = new Pic(url.getURL(), url.getFilenameCorrected(), url.getTargetPath());
		pic.setThumb(url.getThumb());
		pic.setThreadURL(url.getThreadURL());
		URLParseObject upo = new URLParseObject(url.getURL(), url.getThumb(), pic);

		for (Rule rule : rules) {
			if (rule.isFromThisRedirect(upo.getContainerURL(), upo.getLastRule())) {
				try {
					upo.addHoster(rule);
					String result[] = rule.getURLAndFilename(upo);
					upo.setDirectLink(result[0]);
					upo.setCorrectedFilename(result[1]);
					/*
					 * RESEND IS NOT WORKING FOR REDIRECTS
					 */
					return upo.getDirectLink();
				} catch (HostException e) {
					logger.error("Could not get URL and Filename from rule: {} {}", rule.getName(), rule.getVersion(), e);
					return redirectedURL;
				}
			}
		}
		return redirectedURL;
	}

	/**
	 * Returns the developerRulesEnabled
	 * 
	 * @return developerRulesEnabled
	 */
	public boolean isDeveloperRulesEnabled() {
		return developerRulesEnabled;
	}

	/**
	 * Load rules in given folder
	 * 
	 * @param folder Rules Folder
	 * @param developerRules True if developer rules, false otherwise
	 */
	private void loadRules(File folder, boolean developerRules) {
		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}
		if (developerRules) {
			developerRulesEnabled = true;
		}

		String developerRuleLogText = developerRules ? "Developer" : "";

		File subfiles[] = folder.listFiles(new RuleFileFilter());
		if (subfiles != null) {
			Arrays.sort(subfiles);
			for (File subFile : subfiles) {
				logger.debug("Loading {}Rule: {}", developerRuleLogText, subFile.getAbsolutePath());

				try {
					RuleIO ruleIO = new RuleIO();
					RuleDefinition ruleDefinition = ruleIO.readRule(subFile.getAbsolutePath());
					Rule r = new Rule(subFile.getAbsolutePath(), ruleDefinition, developerRules);
					rules.add(r);
					logger.info("{}Rule loaded: {} {} {}", developerRuleLogText, r.getName(), r.getVersion(), r.getFile().getName());
				} catch (Exception e) {
					logger.error("Could not load {}Rule: {}", developerRuleLogText, subFile.getAbsolutePath(), e);
				}
			}
		} else {
			logger.error("Could not list rule files from directoy: {}", folder);
		}
	}

	/**
	 * File Filter for Rules
	 */
	private static class RuleFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (!pathname.isFile()) {
				return false;
			}
			String fileName = pathname.getName();
			return fileName.startsWith("Rule") && fileName.endsWith(".xml");
		}
	}
}
