package ch.supertomcat.bh.database.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Class for Sqlite Database Connections
 * 
 * @param <T> Object Type
 */
public abstract class SQLiteDB<T> {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(SQLiteDB.class);

	/**
	 * Database File
	 */
	protected final String databaseFile;

	/**
	 * Table Name
	 */
	protected final String tableName;

	/**
	 * Constructor
	 * 
	 * @param databaseFile Path to the database File
	 * @param tableName Table Name
	 * @param backupDatabaseOnStart True if database should be backed up on start, false otherwise
	 */
	public SQLiteDB(String databaseFile, String tableName, boolean backupDatabaseOnStart) {
		this.databaseFile = databaseFile;
		this.tableName = tableName;

		createFolderIfNotExist();

		// Check if we have to backup the database
		if (backupDatabaseOnStart) {
			backupDatabase();
		}

		/*
		 * Open a database connection to check if the database is ready to be used, if this
		 * is not the case exit the program
		 */
		boolean openDBFailed = false;
		try (Connection con = getDatabaseConnection()) {
			con.close();
		} catch (ClassNotFoundException | SQLException e) {
			openDBFailed = true;
			String databaseAbsolutePath = new File(databaseFile).getAbsolutePath();
			String dbError = String.format(Localization.getString("ErrorDBNotOpen"), databaseAbsolutePath);
			JOptionPane.showMessageDialog(null, dbError + "\n" + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
			logger.error("Database could not be opened: {}", databaseAbsolutePath, e);
		}

		if (openDBFailed) {
			System.exit(0); // exit programm
		}
	}

	/**
	 * Returns the databaseFile
	 * 
	 * @return databaseFile
	 */
	public String getDatabaseFile() {
		return databaseFile;
	}

	/**
	 * Returns the tableName
	 * 
	 * @return tableName
	 */
	public String getTableName() {
		return tableName;
	}

	private void createFolderIfNotExist() {
		File folder = new File(ApplicationProperties.getProperty("DatabasePath"));
		if (folder.exists() == false && !folder.mkdirs()) {
			logger.error("Could not create database folder: {}", folder.getAbsolutePath());
		}
	}

	/**
	 * Creates a backup of the database
	 */
	private void backupDatabase() {
		long now = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
		String target = databaseFile + "-" + dateFormat.format(now);
		File dbFile = new File(databaseFile);
		String dbFilename = dbFile.getName();

		// Backup
		if (dbFile.exists()) {
			CopyUtil.copy(databaseFile, target);
		}

		// Delete old backup-Files
		BHUtil.deleteOldBackupFiles(new File(ApplicationProperties.getProperty("DatabasePath")), dbFilename, 3);
	}

	/**
	 * Closes all open database connections
	 */
	public void closeAllDatabaseConnections() {
		// Nothing to do
	}

	/**
	 * Returns a connection to the database
	 * 
	 * @return Connection to the database
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected Connection getDatabaseConnection() throws ClassNotFoundException, SQLException {
		// Load sqlite-jdbc-driver
		Class.forName("org.sqlite.JDBC");
		// Connect to db
		return DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
	}

	/**
	 * Creates the database and tables if it does not exist
	 * 
	 * @return True if successful, false otherwise
	 */
	protected abstract boolean createDatabaseIfNotExist();

	/**
	 * Converts the entry, which the ResultSet currently points to, to an object
	 * 
	 * @param result Result
	 * @return Object
	 * @throws SQLException
	 */
	protected abstract T convertResultSetToObject(ResultSet result) throws SQLException;

	/**
	 * Returns all entries from the database
	 * 
	 * @return All entries
	 */
	public abstract List<T> getAllEntries();

	/**
	 * Returns the entry with given ID or null if the entry was not found or an error occurred
	 * 
	 * @param id ID in database
	 * @return Entry or null if no entry with the ID found or an error occurred
	 */
	public abstract T getEntry(int id);

	/**
	 * Inserts the given entry into the database
	 * 
	 * @param entry Entry
	 * @return True if successful, false otherwise
	 */
	public abstract boolean insertEntry(T entry);

	/**
	 * Inserts the given entries into the database
	 * 
	 * @param entries Entries
	 * @return True if successful, false otherwise
	 */
	public abstract boolean insertEntries(List<T> entries);

	/**
	 * Updates the given entry in the database
	 * 
	 * @param entry Entry
	 * @return True if successful, false otherwise
	 */
	public abstract boolean updateEntry(T entry);

	/**
	 * Updates the given entries in the database
	 * 
	 * @param entries Entries
	 * @return True if successful, false otherwise
	 */
	public abstract boolean updateEntries(List<T> entries);

	/**
	 * Deletes the given entry from the database
	 * 
	 * @param entry Entry
	 * @return True if successful, false otherwise
	 */
	public abstract boolean deleteEntry(T entry);

	/**
	 * Deletes the given entries from the database
	 * 
	 * @param entries Entries
	 * @return True if successful, false otherwise
	 */
	public abstract boolean deleteEntries(List<T> entries);
}
