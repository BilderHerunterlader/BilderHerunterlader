package ch.supertomcat.bh.hoster;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;

/**
 * Host-Class for Rules
 * 
 * @version 0.5
 */
public class HostRules extends Host implements IHoster, IRedirect {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(HostRules.class);

	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "0.5";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostRules";

	/**
	 * Domains
	 */
	private List<String> domains = null;

	/**
	 * Regeln
	 */
	private List<Rule> rules = new ArrayList<>();

	private boolean developerRulesEnabled = false;

	/**
	 * Konstruktor
	 */
	public HostRules() {
		super(NAME, VERSION, false);
		domains = new ArrayList<>();
		domains.add("NODOMAINS");

		// DEVELOPER RULES
		File ruleFolderDeveloper = new File(ApplicationProperties.getProperty("ApplicationPath") + "developerrules");
		// Do NOT create folder, if it not exists
		loadRules(ruleFolderDeveloper, true);

		// NORMAL RULES
		File ruleFolder = new File(ApplicationProperties.getProperty("ApplicationPath") + "rules");
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
	 * Regel hinzufuegen
	 * 
	 * @param rule Rule
	 */
	public void addRule(Rule rule) {
		rules.add(rule);
	}

	/**
	 * Regel loeschen
	 * 
	 * @param index Index
	 */
	public void removeRule(int index) {
		if (index >= rules.size()) {
			return;
		}
		rules.remove(index);
	}

	/**
	 * Alle Regeln speichern
	 */
	public void saveAllRules() {
		for (int i = 0; i < rules.size(); i++) {
			rules.get(i).writeRule();
		}
	}

	/**
	 * Get-Methode
	 * 
	 * @return Rules
	 */
	public List<Rule> getRules() {
		return rules;
	}

	/**
	 * Get-Methode
	 * 
	 * @param index Index
	 * @return Rule
	 */
	public Rule getRule(int index) {
		if (index >= rules.size()) {
			return null;
		}
		return rules.get(index);
	}

	/**
	 * Get-Method
	 * 
	 * @return Domains
	 */
	public List<String> getDomains() {
		return domains;
	}

	@Override
	public boolean isFromThisHoster(String url) {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).isFromThisHoster(url, null)) {
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
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).isFromThisHoster(url, null)) {
				return rules.get(i);
			}
		}
		return null;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		try {
			for (int i = 0; i < rules.size(); i++) {
				if (rules.get(i).isFromThisHoster(url, null)) {
					return rules.get(i).getFilename(url);
				}
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).isFromThisHoster(upo.getContainerURL(), upo.getLastRule())) {
				upo.addHoster(rules.get(i));
				String result[] = rules.get(i).getURLAndFilename(upo);
				upo.setDirectLink(result[0]);
				upo.setCorrectedFilename(result[1]);
				if (rules.get(i).isResend()) {
					upo.setContainerURL(result[0]);
					HostManager.instance().parseURL(upo);
				}
				return;
			}
		}
	}

	@Override
	public boolean isFromThisRedirect(URL url) {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).isFromThisRedirect(url.getURL(), null)) {
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

		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).isFromThisRedirect(upo.getContainerURL(), upo.getLastRule())) {
				try {
					upo.addHoster(rules.get(i));
					String result[] = rules.get(i).getURLAndFilename(upo);
					upo.setDirectLink(result[0]);
					upo.setCorrectedFilename(result[1]);
					/*
					 * RESEND IS NOT WORKING FOR REDIRECTS
					 */
					redirectedURL = upo.getDirectLink();
				} catch (HostException e) {
					logger.error(e.getMessage(), e);
				}
				break;
			}
		}

		pic = null;
		upo = null;
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
		try {
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
						Rule r = new Rule(subFile.getAbsolutePath(), developerRules);
						if (r.getStatusOK()) {
							rules.add(r);
							logger.info("{}Rule loaded: {} {} {}", developerRuleLogText, r.getName(), r.getVersion(), r.getFile().getName());
						} else {
							logger.error("Could not load {}Rule: {}", developerRuleLogText, subFile.getAbsolutePath());
						}
					} catch (Exception e) {
						logger.error("Could not load {}Rule: {}", developerRuleLogText, subFile.getAbsolutePath(), e);
					}
				}
			} else {
				logger.error("Could not list rule files from directoy: {}", folder);
			}
		} catch (Exception e) {
			logger.error("Exception while loading rules from directoy: {}", folder, e);
		}
	}

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
