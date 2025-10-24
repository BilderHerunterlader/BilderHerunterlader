package ch.supertomcat.bh.database.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.log.LogTableModel;
import ch.supertomcat.bh.log.LogEntry;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.database.sqlite.SQLiteDB;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * Class for Sqlite Database connections for Logs Database
 */
public class LogsSQLiteDB extends SQLiteDB<LogEntry> {
	private final String selectAllEntriesSQL;

	private final String selectEntriesRangeSQL;

	private final String selectDirectoyLogEntriesSQL;

	private final String countAllEntriesSQL;

	private final String countAlreadyDownloadedEntriesSQL;

	private final String selectEntrySQL;

	private final String insertEntrySQL;

	private final String updateEntrySQL;

	private final String deleteEntrySQL;

	private final String deleteAllEntriesSQL;

	/**
	 * Constructor
	 * 
	 * @param databaseFile Path to the database File
	 * @param backupDatabaseOnStart True if database should be backed up on start, false otherwise
	 * @param defragDatabaseOnStart True if database should be deframented on start, false otherwise
	 * @param defragMinFileSize Minimum filesize for decision if database is actually defragmented or not
	 */
	public LogsSQLiteDB(String databaseFile, boolean backupDatabaseOnStart, boolean defragDatabaseOnStart, long defragMinFileSize) {
		super(databaseFile, "bh_logs", backupDatabaseOnStart, defragDatabaseOnStart, defragMinFileSize);

		selectAllEntriesSQL = "SELECT * FROM " + tableName;

		selectEntriesRangeSQL = "SELECT * FROM " + tableName + " ORDER BY DownloadTimestamp ASC LIMIT ? OFFSET ?";

		selectDirectoyLogEntriesSQL = "SELECT *, max(DownloadTimestamp) FROM " + tableName + " GROUP BY TargetPath ORDER BY DownloadTimestamp DESC LIMIT ?";

		countAlreadyDownloadedEntriesSQL = "SELECT COUNT(1) FROM " + tableName + " WHERE ContainerURL = ? OR ContainerURL = ?";

		countAllEntriesSQL = "SELECT COUNT(LogEntryID) FROM " + tableName;

		selectEntrySQL = "SELECT * FROM " + tableName + " WHERE LogEntryID = ?";

		StringBuilder sbInsertEntry = new StringBuilder();
		sbInsertEntry.append("INSERT INTO ");
		sbInsertEntry.append(tableName);
		sbInsertEntry.append(" (");
		sbInsertEntry.append("DownloadTimestamp, ");
		sbInsertEntry.append("ContainerURL, ");
		sbInsertEntry.append("ThreadURL, ");
		sbInsertEntry.append("DownloadURL, ");
		sbInsertEntry.append("ThumbURL, ");
		sbInsertEntry.append("TargetPath, ");
		sbInsertEntry.append("TargetFilename, ");
		sbInsertEntry.append("Size");
		sbInsertEntry.append(") VALUES (");
		sbInsertEntry.append("?, ?, ?, ?, ?, ?, ?, ?");
		sbInsertEntry.append(")");

		insertEntrySQL = sbInsertEntry.toString();

		StringBuilder sbUpdateEntry = new StringBuilder();
		sbUpdateEntry.append("UPDATE ");
		sbUpdateEntry.append(tableName);
		sbUpdateEntry.append(" SET ");
		sbInsertEntry.append("DownloadTimestamp = ?, ");
		sbInsertEntry.append("ContainerURL = ?, ");
		sbInsertEntry.append("ThreadURL = ?, ");
		sbInsertEntry.append("DownloadURL = ?, ");
		sbInsertEntry.append("ThumbURL = ?, ");
		sbInsertEntry.append("TargetPath = ?, ");
		sbInsertEntry.append("TargetFilename = ?, ");
		sbInsertEntry.append("Size = ?");
		sbUpdateEntry.append(" WHERE LogEntryID = ?");
		updateEntrySQL = sbUpdateEntry.toString();

		deleteEntrySQL = "DELETE FROM " + tableName + " WHERE LogEntryID = ?";

		deleteAllEntriesSQL = "DELETE FROM " + tableName;

		createDatabaseIfNotExist();
	}

	@Override
	protected synchronized boolean createDatabaseIfNotExist() {
		// Create table if not exist
		StringBuilder sbCreateTable = new StringBuilder();

		sbCreateTable.append("CREATE TABLE IF NOT EXISTS ");
		sbCreateTable.append(tableName);
		sbCreateTable.append(" (");
		sbCreateTable.append("LogEntryID INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sbCreateTable.append("DownloadTimestamp BIGINT NOT NULL, ");
		sbCreateTable.append("ContainerURL TEXT NOT NULL, ");
		sbCreateTable.append("ThreadURL TEXT NOT NULL, ");
		sbCreateTable.append("DownloadURL TEXT NOT NULL, ");
		sbCreateTable.append("ThumbURL TEXT NOT NULL, ");
		sbCreateTable.append("TargetPath TEXT NOT NULL, ");
		sbCreateTable.append("TargetFilename TEXT NOT NULL, ");
		sbCreateTable.append("Size BIGINT NOT NULL");
		sbCreateTable.append(")");
		String createTableSQL = sbCreateTable.toString();

		StringBuilder sbCreateIndexContainerURL = new StringBuilder();
		sbCreateIndexContainerURL.append("CREATE INDEX IF NOT EXISTS BHLogEntryContainerURLIndex ON ");
		sbCreateIndexContainerURL.append(tableName);
		sbCreateIndexContainerURL.append(" (");
		sbCreateIndexContainerURL.append("ContainerURL");
		sbCreateIndexContainerURL.append(")");
		String createIndexContainerURLSQL = sbCreateIndexContainerURL.toString();

		StringBuilder sbCreateIndexTargetPath = new StringBuilder();
		sbCreateIndexTargetPath.append("CREATE INDEX IF NOT EXISTS BHLogEntryTargetPathIndex ON ");
		sbCreateIndexTargetPath.append(tableName);
		sbCreateIndexTargetPath.append(" (");
		sbCreateIndexTargetPath.append("TargetPath");
		sbCreateIndexTargetPath.append(")");
		String createIndexTargetPathSQL = sbCreateIndexTargetPath.toString();

		StringBuilder sbCreateIndexTimestamp = new StringBuilder();
		sbCreateIndexTimestamp.append("CREATE INDEX IF NOT EXISTS BHLogEntryTimestampIndex ON ");
		sbCreateIndexTimestamp.append(tableName);
		sbCreateIndexTimestamp.append(" (");
		sbCreateIndexTimestamp.append("DownloadTimestamp");
		sbCreateIndexTimestamp.append(")");
		String createIndexTimestampSQL = sbCreateIndexTimestamp.toString();

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (Statement statement = con.createStatement()) {
				statement.executeUpdate(createTableSQL);
				statement.executeUpdate(createIndexContainerURLSQL);
				statement.executeUpdate(createIndexTargetPathSQL);
				statement.executeUpdate(createIndexTimestampSQL);
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not create database: {}", tableName, e);
			return false;
		}
	}

	@Override
	protected LogEntry convertResultSetToObject(ResultSet result) throws SQLException {
		int id = result.getInt("LogEntryID");
		long timestamp = result.getLong("DownloadTimestamp");
		String containerURL = result.getString("ContainerURL");
		String threadURL = result.getString("ThreadURL");
		String downloadURL = result.getString("DownloadURL");
		String thumbURL = result.getString("ThumbURL");
		String targetPath = result.getString("TargetPath");
		String targetFilename = result.getString("TargetFilename");
		long size = result.getLong("Size");

		return new LogEntry(id, timestamp, containerURL, threadURL, downloadURL, thumbURL, targetPath, targetFilename, size);
	}

	@Override
	public synchronized List<LogEntry> getAllEntries() {
		List<LogEntry> logEntries = new ArrayList<>();

		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectAllEntriesSQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						logEntries.add(convertResultSetToObject(rs));
					}
					return logEntries;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get LogEntry from database '{}'", tableName, e);
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	/**
	 * Get entries count
	 * 
	 * @return Count
	 */
	public synchronized int getEntriesCount() {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(countAllEntriesSQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						return rs.getInt(1);
					}
				}
			}
			return 0;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get count from database '{}'", tableName, e);
			return 0;
		}
	}

	/**
	 * Get entries for directory log
	 * 
	 * @param count Count
	 * @return Entries
	 */
	public synchronized List<LogEntry> getDirectoryLogEntries(int count) {
		List<LogEntry> logEntries = new ArrayList<>();

		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectDirectoyLogEntriesSQL)) {
				statement.setInt(1, count);
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						logEntries.add(convertResultSetToObject(rs));
					}
					return logEntries;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get LogEntry from database '{}'", tableName, e);
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	/**
	 * Check already downloaded
	 * 
	 * @param url URL
	 * @param alternativeURL Alternative URL
	 * @return True if already downloaded, false otherwise
	 */
	public synchronized boolean checkAlreadyDownloaded(String url, String alternativeURL) {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(countAlreadyDownloadedEntriesSQL)) {
				statement.setString(1, url);
				statement.setString(2, alternativeURL);
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						return rs.getInt(1) > 0;
					}
				}
			}
			return false;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get LogEntry from database '{}'", tableName, e);
			return false;
		}
	}

	/**
	 * Get entries from given range
	 * 
	 * @param startOffset Start Offset
	 * @param count Count
	 * @param model Model
	 * @param settingsManager Settings Manager
	 * @param dateFormat Date Format
	 */
	public synchronized void fillTableModelWithEntriesRange(int startOffset, int count, LogTableModel model, SettingsManager settingsManager, DateTimeFormatter dateFormat) {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectEntriesRangeSQL)) {
				statement.setInt(1, count);
				statement.setInt(2, startOffset);
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						long timestamp = rs.getLong("DownloadTimestamp");
						String containerURL = rs.getString("ContainerURL");
						String targetPath = rs.getString("TargetPath");
						String targetFilename = rs.getString("TargetFilename");
						long size = rs.getLong("Size");

						LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
						String strDateTime = dateTime.format(dateFormat);
						String strFilesize;
						if (size > 0) {
							strFilesize = UnitFormatUtil.getSizeString(size, settingsManager.getSizeView());
						} else {
							strFilesize = Localization.getString("Unkown");
						}
						model.addRow(containerURL, targetPath, targetFilename, strDateTime, strFilesize);
					}
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get LogEntry from database '{}'", tableName, e);
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public synchronized LogEntry getEntry(int id) {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectEntrySQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					if (!rs.first()) {
						logger.error("Could not find LogEntry in database: {}", id);
						return null;
					}
					return convertResultSetToObject(rs);
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get LogEntry from database '{}': {}", tableName, id, e);
			return null;
		}
	}

	@Override
	public synchronized boolean insertEntry(LogEntry entry) {
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
				insertEntry(entry, statement);
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not insert LogEntry into database '{}': {}", tableName, entry.getContainerURL(), e);
			return false;
		}
	}

	/**
	 * Insert Entry
	 * 
	 * @param entry Entry
	 * @param statement Prepared Statement
	 * @throws SQLException
	 */
	private synchronized void insertEntry(LogEntry entry, PreparedStatement statement) throws SQLException {
		statement.setLong(1, entry.getTimestamp());
		statement.setString(2, entry.getContainerURL());
		statement.setString(3, entry.getThreadURL());
		statement.setString(4, entry.getDownloadURL());
		statement.setString(5, entry.getThumbURL());
		statement.setString(6, entry.getTargetPath());
		statement.setString(7, entry.getTargetFilename());
		statement.setLong(8, entry.getSize());

		int rowsAffected = statement.executeUpdate();
		if (rowsAffected <= 0) {
			logger.error("Could not insert LogEntry into database '{}'. No rows affected: {}", tableName, entry.getContainerURL());
		} else {
			// Set ID on entry
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					entry.setId(id);
					logger.debug("Set ID for inserted LogEntry: ID={}, Name={}", id, entry.getContainerURL());
				} else {
					logger.error("Could not get generated ID of LogEntry from database '{}': {}", tableName, entry.getContainerURL());
				}
			}
		}
	}

	@Override
	public synchronized boolean insertEntries(List<LogEntry> entries) {
		boolean result = true;
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
				for (LogEntry entry : entries) {
					try {
						insertEntry(entry, statement);
					} catch (SQLException e) {
						logger.error("Could not insert LogEntry into database '{}': {}", tableName, entry.getContainerURL(), e);
						result = false;
					}
				}
			}
			con.commit();
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not insert LogEntry into database '{}'", tableName, e);
			result = false;
		}
		return result;
	}

	@Override
	public synchronized boolean updateEntry(LogEntry entry) {
		if (entry.getId() <= 0) {
			logger.warn("Could not update LogEntry in database, because it has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
				updateEntry(entry, statement);
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not update LogEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
			return false;
		}
	}

	/**
	 * Update Entry
	 * 
	 * @param entry Entry
	 * @param statement Prepared Statement
	 * @return True if successful, false otherwise (Only relevant when entry is not in database and is inserted)
	 * @throws SQLException
	 */
	private synchronized boolean updateEntry(LogEntry entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.warn("Could not update LogEntry in database, because it has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		statement.setLong(1, entry.getTimestamp());
		statement.setString(2, entry.getContainerURL());
		statement.setString(3, entry.getThreadURL());
		statement.setString(4, entry.getDownloadURL());
		statement.setString(5, entry.getThumbURL());
		statement.setString(6, entry.getTargetPath());
		statement.setString(7, entry.getTargetFilename());
		statement.setLong(8, entry.getSize());
		statement.executeUpdate();
		return true;
	}

	@Override
	public synchronized boolean updateEntries(List<LogEntry> entries) {
		boolean result = true;
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
				for (LogEntry entry : entries) {
					try {
						if (!updateEntry(entry, statement)) {
							result = false;
						}
					} catch (SQLException e) {
						logger.error("Could not update LogEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
						result = false;
					}
				}
			}
			con.commit();
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not update LogEntries in database '{}'", tableName, e);
			result = false;
		}
		return result;
	}

	@Override
	public synchronized boolean deleteEntry(LogEntry entry) {
		if (entry.getId() <= 0) {
			logger.error("Could not delete LogEntry in database, because it has no valid ID: {}", entry.getId());
			return false;
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
				deleteEntry(entry, statement);
			}
			logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getContainerURL());
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not delete LogEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
			return false;
		}
	}

	/**
	 * Delete Entry
	 * 
	 * @param entry Entry
	 * @param statement Prepared Statement
	 * @throws SQLException
	 */
	private synchronized void deleteEntry(LogEntry entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.error("Could not delete LogEntry in database, because it has no valid ID: {}", entry.getId());
			return;
		}

		statement.setInt(1, entry.getId());
		statement.executeUpdate();
	}

	@Override
	public synchronized boolean deleteEntries(List<LogEntry> entries) {
		boolean result = true;
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
				for (LogEntry entry : entries) {
					try {
						deleteEntry(entry, statement);
						logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getContainerURL());
					} catch (SQLException e) {
						logger.error("Could not delete LogEntry into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
						result = false;
					}
				}
			}
			con.commit();
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not delete LogEntries in database '{}'", tableName, e);
			result = false;
		}
		return result;
	}

	/**
	 * Delete all entries
	 * 
	 * @return True if successful, false otherwise
	 */
	public synchronized boolean deleteAllEntries() {
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(deleteAllEntriesSQL)) {
				statement.executeUpdate();
			}
			logger.debug("Deleted all entries in database '{}'", tableName);
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not delete all logs in database '{}'", tableName, e);
			return false;
		}
	}
}
