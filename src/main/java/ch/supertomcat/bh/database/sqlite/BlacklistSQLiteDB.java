package ch.supertomcat.bh.database.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.log.BlacklistEntry;
import ch.supertomcat.supertomcatutils.database.sqlite.SQLiteDB;

/**
 * Class for Sqlite Database connections for Blacklist Database
 */
public class BlacklistSQLiteDB extends SQLiteDB<BlacklistEntry> {
	private final String selectAllEntriesSQL;

	private final String countAllEntriesSQL;

	private final String countBlacklistedEntriesSQL;

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
	public BlacklistSQLiteDB(String databaseFile, boolean backupDatabaseOnStart, boolean defragDatabaseOnStart, long defragMinFileSize) {
		super(databaseFile, "bh_blacklist", backupDatabaseOnStart, defragDatabaseOnStart, defragMinFileSize);

		selectAllEntriesSQL = "SELECT * FROM " + tableName;

		countBlacklistedEntriesSQL = "SELECT COUNT(1) FROM " + tableName + " WHERE URL = ?";

		countAllEntriesSQL = "SELECT COUNT(BlacklistID) FROM " + tableName;

		selectEntrySQL = "SELECT * FROM " + tableName + " WHERE BlacklistID = ?";

		StringBuilder sbInsertEntry = new StringBuilder();
		sbInsertEntry.append("INSERT INTO ");
		sbInsertEntry.append(tableName);
		sbInsertEntry.append(" (");
		sbInsertEntry.append("URL");
		sbInsertEntry.append(") VALUES (");
		sbInsertEntry.append("?");
		sbInsertEntry.append(")");

		insertEntrySQL = sbInsertEntry.toString();

		StringBuilder sbUpdateEntry = new StringBuilder();
		sbUpdateEntry.append("UPDATE ");
		sbUpdateEntry.append(tableName);
		sbUpdateEntry.append(" SET ");
		sbInsertEntry.append("URL = ?");
		sbUpdateEntry.append(" WHERE BlacklistID = ?");
		updateEntrySQL = sbUpdateEntry.toString();

		deleteEntrySQL = "DELETE FROM " + tableName + " WHERE BlacklistID = ?";

		deleteAllEntriesSQL = "DELETE FROM " + tableName;

		createDatabaseIfNotExist();
	}

	@Override
	protected boolean createDatabaseIfNotExist() {
		try {
			writeLock.lock();
			// Create table if not exist
			StringBuilder sbCreateTable = new StringBuilder();

			sbCreateTable.append("CREATE TABLE IF NOT EXISTS ");
			sbCreateTable.append(tableName);
			sbCreateTable.append(" (");
			sbCreateTable.append("BlacklistID INTEGER PRIMARY KEY AUTOINCREMENT, ");
			sbCreateTable.append("URL TEXT NOT NULL");
			sbCreateTable.append(")");
			String createTableSQL = sbCreateTable.toString();

			StringBuilder sbCreateIndexURL = new StringBuilder();
			sbCreateIndexURL.append("CREATE INDEX IF NOT EXISTS BHBlacklistURLIndex ON ");
			sbCreateIndexURL.append(tableName);
			sbCreateIndexURL.append(" (");
			sbCreateIndexURL.append("URL");
			sbCreateIndexURL.append(")");
			String createIndexURLSQL = sbCreateIndexURL.toString();

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (Statement statement = con.createStatement()) {
					statement.executeUpdate(createTableSQL);
					statement.executeUpdate(createIndexURLSQL);
				}
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not create database: {}", tableName, e);
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	protected BlacklistEntry convertResultSetToObject(ResultSet result) throws SQLException {
		int id = result.getInt("BlacklistID");
		String url = result.getString("URL");

		return new BlacklistEntry(id, url);
	}

	@Override
	public List<BlacklistEntry> getAllEntries() {
		try {
			readLock.lock();
			List<BlacklistEntry> blacklistEntries = new ArrayList<>();

			try (Connection con = getDatabaseConnection()) {
				try (PreparedStatement statement = con.prepareStatement(selectAllEntriesSQL)) {
					try (ResultSet rs = statement.executeQuery()) {
						while (rs.next()) {
							blacklistEntries.add(convertResultSetToObject(rs));
						}
						return blacklistEntries;
					}
				}
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not get BlacklistEntry from database '{}'", tableName, e);
				JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
				return new ArrayList<>();
			}
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Get entries count
	 * 
	 * @return Count
	 */
	public int getEntriesCount() {
		try {
			readLock.lock();
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
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Check already downloaded
	 * 
	 * @param url URL
	 * @return True if already downloaded, false otherwise
	 */
	public boolean checkBlacklisted(String url) {
		try {
			readLock.lock();
			try (Connection con = getDatabaseConnection()) {
				try (PreparedStatement statement = con.prepareStatement(countBlacklistedEntriesSQL)) {
					statement.setString(1, url);
					try (ResultSet rs = statement.executeQuery()) {
						while (rs.next()) {
							return rs.getInt(1) > 0;
						}
					}
				}
				return false;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not get BlacklistEntry from database '{}'", tableName, e);
				return false;
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public BlacklistEntry getEntry(int id) {
		try {
			readLock.lock();
			try (Connection con = getDatabaseConnection()) {
				try (PreparedStatement statement = con.prepareStatement(selectEntrySQL)) {
					try (ResultSet rs = statement.executeQuery()) {
						if (!rs.first()) {
							logger.error("Could not find BlacklistEntry in database: {}", id);
							return null;
						}
						return convertResultSetToObject(rs);
					}
				}
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not get BlacklistEntry from database '{}': {}", tableName, id, e);
				return null;
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean insertEntry(BlacklistEntry entry) {
		try {
			writeLock.lock();
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
					insertEntry(entry, statement);
				}
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not insert BlacklistEntry into database '{}': {}", tableName, entry.getUrl(), e);
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Insert Entry
	 * 
	 * @param entry Entry
	 * @param statement Prepared Statement
	 * @throws SQLException
	 */
	private void insertEntry(BlacklistEntry entry, PreparedStatement statement) throws SQLException {
		statement.setString(1, entry.getUrl());

		int rowsAffected = statement.executeUpdate();
		if (rowsAffected <= 0) {
			logger.error("Could not insert BlacklistEntry into database '{}'. No rows affected: {}", tableName, entry.getUrl());
		} else {
			// Set ID on entry
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					entry.setId(id);
					logger.debug("Set ID for inserted BlacklistEntry: ID={}, Name={}", id, entry.getUrl());
				} else {
					logger.error("Could not get generated ID of BlacklistEntry from database '{}': {}", tableName, entry.getUrl());
				}
			}
		}
	}

	@Override
	public boolean insertEntries(List<BlacklistEntry> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
					for (BlacklistEntry entry : entries) {
						try {
							insertEntry(entry, statement);
						} catch (SQLException e) {
							logger.error("Could not insert BlacklistEntry into database '{}': {}", tableName, entry.getUrl(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not insert BlacklistEntry into database '{}'", tableName, e);
				result = false;
			}
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean updateEntry(BlacklistEntry entry) {
		try {
			writeLock.lock();
			if (entry.getId() <= 0) {
				logger.warn("Could not update BlacklistEntry in database, because it has no valid ID: {}. Adding it to database instead.", entry.getId());
				return insertEntry(entry);
			}

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
					updateEntry(entry, statement);
				}
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not update BlacklistEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getUrl(), e);
				return false;
			}
		} finally {
			writeLock.unlock();
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
	private boolean updateEntry(BlacklistEntry entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.warn("Could not update BlacklistEntry in database, because it has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		statement.setString(1, entry.getUrl());
		statement.executeUpdate();
		return true;
	}

	@Override
	public boolean updateEntries(List<BlacklistEntry> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
					for (BlacklistEntry entry : entries) {
						try {
							if (!updateEntry(entry, statement)) {
								result = false;
							}
						} catch (SQLException e) {
							logger.error("Could not update BlacklistEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getUrl(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not update BlacklistEntries in database '{}'", tableName, e);
				result = false;
			}
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean deleteEntry(BlacklistEntry entry) {
		try {
			writeLock.lock();
			if (entry.getId() <= 0) {
				logger.error("Could not delete BlacklistEntry in database, because it has no valid ID: {}", entry.getId());
				return false;
			}

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
					deleteEntry(entry, statement);
				}
				logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getUrl());
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not delete BlacklistEntry in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getUrl(), e);
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Delete Entry
	 * 
	 * @param entry Entry
	 * @param statement Prepared Statement
	 * @throws SQLException
	 */
	private void deleteEntry(BlacklistEntry entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.error("Could not delete BlacklistEntry in database, because it has no valid ID: {}", entry.getId());
			return;
		}

		statement.setInt(1, entry.getId());
		statement.executeUpdate();
	}

	@Override
	public boolean deleteEntries(List<BlacklistEntry> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
					for (BlacklistEntry entry : entries) {
						try {
							deleteEntry(entry, statement);
							logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getUrl());
						} catch (SQLException e) {
							logger.error("Could not delete BlacklistEntry into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getUrl(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not delete BlacklistEntries in database '{}'", tableName, e);
				result = false;
			}
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Delete all entries
	 * 
	 * @return True if successful, false otherwise
	 */
	public boolean deleteAllEntries() {
		try {
			writeLock.lock();
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(deleteAllEntriesSQL)) {
					statement.executeUpdate();
				}
				logger.debug("Deleted all entries in database '{}'", tableName);
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not delete all entries in database '{}'", tableName, e);
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}
}
