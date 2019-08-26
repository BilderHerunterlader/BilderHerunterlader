module ch.supertomcat.bh {
	/*
	 * Java SE Modules
	 */
	requires java.desktop;
	requires java.sql;
	
	/*
	 * Java EE Modules
	 */
	requires java.xml.bind;
	
	/*
	 * Autmatic Modules
	 */
	requires ehcache;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires org.apache.commons.codec;
	requires jdom2;
	requires jtidy.r938;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires nanohttpd;
	requires rhino;
	requires slf4j.api;
	requires supertomcatutils;
	requires swingx.all;
	requires syntaxpane;
}
