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
	requires commons.codec;
	requires ehcache;
	requires httpclient;
	requires httpcore;
	requires jdom2;
	requires jtidy.r938;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires nanohttpd;
	requires rhino;
	requires slf4j.api;
	requires supertomcattools;
	requires swingx.all;
	requires syntaxpane;
}
