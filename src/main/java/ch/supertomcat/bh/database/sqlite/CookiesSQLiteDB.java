package ch.supertomcat.bh.database.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import ch.supertomcat.bh.cookies.BHCookie;
import ch.supertomcat.supertomcatutils.database.sqlite.SQLiteDB;

/**
 * Class for Sqlite Database connections for Queue Database
 */
public class CookiesSQLiteDB extends SQLiteDB<BHCookie> {
	private final String selectAllEntriesSQL;

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
	public CookiesSQLiteDB(String databaseFile, boolean backupDatabaseOnStart, boolean defragDatabaseOnStart, long defragMinFileSize) {
		super(databaseFile, "bh_cookies", backupDatabaseOnStart, defragDatabaseOnStart, defragMinFileSize);

		selectAllEntriesSQL = "SELECT * FROM " + tableName;

		selectEntrySQL = "SELECT * FROM " + tableName + " WHERE CookieID = ?";

		StringBuilder sbInsertEntry = new StringBuilder();
		sbInsertEntry.append("INSERT INTO ");
		sbInsertEntry.append(tableName);
		sbInsertEntry.append(" (");
		sbInsertEntry.append("OriginAttributes, ");
		sbInsertEntry.append("Name, ");
		sbInsertEntry.append("Value, ");
		sbInsertEntry.append("Host, ");
		sbInsertEntry.append("Path, ");
		sbInsertEntry.append("ExpiryTime, ");
		sbInsertEntry.append("LastAccessedTime, ");
		sbInsertEntry.append("CreationTime, ");
		sbInsertEntry.append("Secure, ");
		sbInsertEntry.append("HttpOnly");
		sbInsertEntry.append(") VALUES (");
		sbInsertEntry.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
		sbInsertEntry.append(")");
		insertEntrySQL = sbInsertEntry.toString();

		StringBuilder sbUpdateEntry = new StringBuilder();
		sbUpdateEntry.append("UPDATE ");
		sbUpdateEntry.append(tableName);
		sbUpdateEntry.append(" SET ");
		sbInsertEntry.append("OriginAttributes = ?, ");
		sbInsertEntry.append("Name = ?, ");
		sbInsertEntry.append("Value = ?, ");
		sbInsertEntry.append("Host = ?, ");
		sbInsertEntry.append("Path = ?, ");
		sbInsertEntry.append("ExpiryTime = ?, ");
		sbInsertEntry.append("LastAccessedTime = ?, ");
		sbInsertEntry.append("CreationTime = ?, ");
		sbInsertEntry.append("Secure = ?, ");
		sbInsertEntry.append("HttpOnly = ?");
		sbUpdateEntry.append(" WHERE CookieID = ?");
		updateEntrySQL = sbUpdateEntry.toString();

		deleteEntrySQL = "DELETE FROM " + tableName + " WHERE CookieID = ?";

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
			sbCreateTable.append("CookieID INTEGER PRIMARY KEY AUTOINCREMENT, ");
			sbCreateTable.append("OriginAttributes TEXT NOT NULL DEFAULT '', ");
			sbCreateTable.append("Name TEXT, ");
			sbCreateTable.append("Value TEXT, ");
			sbCreateTable.append("Host TEXT, ");
			sbCreateTable.append("Path TEXT, ");
			sbCreateTable.append("ExpiryTime INTEGER, ");
			sbCreateTable.append("LastAccessedTime INTEGER, ");
			sbCreateTable.append("CreationTime INTEGER, ");
			sbCreateTable.append("Secure BOOLEAN, ");
			sbCreateTable.append("HttpOnly BOOLEAN, ");
			sbCreateTable.append("CONSTRAINT BHCookieUnique UNIQUE (Name, Host, Path, OriginAttributes)");
			sbCreateTable.append(")");
			String createTableSQL = sbCreateTable.toString();

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (Statement statement = con.createStatement()) {
					statement.executeUpdate(createTableSQL);
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
	protected BHCookie convertResultSetToObject(ResultSet result) throws SQLException {
		int id = result.getInt("CookieID");

		/*
		 * Currently there is no way to access all origin attributes from the cookie of apache httpclient as there is no way to get the map or at least the
		 * keys.
		 */
		// String originAttributes = result.getString("OriginAttributes");

		String name = result.getString("Name");
		String value = result.getString("Value");
		String host = result.getString("Host");
		String path = result.getString("Path");
		Instant expiryInstant;
		long expiryTime = result.getLong("ExpiryTime");
		if (result.wasNull()) {
			expiryInstant = null;
		} else {
			expiryInstant = Instant.ofEpochMilli(expiryTime);
		}

		/*
		 * Currently there is no last access time in the cookie of apache httpclient
		 */
		// long lastAccessedTime = result.getLong("LastAccessedTime");
		// Instant lastAccessedInstant = Instant.ofEpochMilli(lastAccessedTime);
		Instant creationInstant;
		long creationTime = result.getLong("CreationTime");
		if (result.wasNull()) {
			creationInstant = null;
		} else {
			creationInstant = Instant.ofEpochMilli(creationTime);
		}
		boolean secure = result.getBoolean("Secure");
		boolean httpOnly = result.getBoolean("HttpOnly");

		BasicClientCookie cookie = new BasicClientCookie(name, value);
		cookie.setDomain(host);
		cookie.setPath(path);
		cookie.setExpiryDate(expiryInstant);
		cookie.setCreationDate(creationInstant);
		cookie.setSecure(secure);
		cookie.setHttpOnly(httpOnly);

		return new BHCookie(id, cookie);
	}

	@Override
	public List<BHCookie> getAllEntries() {
		try {
			readLock.lock();
			List<BHCookie> cookies = new ArrayList<>();

			try (Connection con = getDatabaseConnection()) {
				try (PreparedStatement statement = con.prepareStatement(selectAllEntriesSQL)) {
					try (ResultSet rs = statement.executeQuery()) {
						while (rs.next()) {
							cookies.add(convertResultSetToObject(rs));
						}
						return cookies;
					}
				}
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not get Cookies from database '{}'", tableName, e);
				JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
				return new ArrayList<>();
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public BHCookie getEntry(int id) {
		try {
			readLock.lock();
			try (Connection con = getDatabaseConnection()) {
				try (PreparedStatement statement = con.prepareStatement(selectEntrySQL)) {
					try (ResultSet rs = statement.executeQuery()) {
						if (!rs.first()) {
							logger.error("Could not find Cookie in database: {}", id);
							return null;
						}
						return convertResultSetToObject(rs);
					}
				}
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not get Cookie from database '{}': {}", tableName, id, e);
				return null;
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean insertEntry(BHCookie entry) {
		try {
			writeLock.lock();
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
					insertEntry(entry, statement);
				}
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not insert Cookie into database '{}': {}", tableName, entry.getCookie().getName(), e);
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
	private void insertEntry(BHCookie entry, PreparedStatement statement) throws SQLException {
		Cookie cookie = entry.getCookie();
		statement.setString(1, "");
		statement.setString(2, cookie.getName());
		statement.setString(3, cookie.getValue());
		statement.setString(4, cookie.getDomain());
		statement.setString(5, cookie.getPath());
		Instant expiry = cookie.getExpiryInstant();
		if (expiry != null) {
			statement.setLong(6, expiry.toEpochMilli());
		} else {
			statement.setNull(6, Types.INTEGER);
		}
		statement.setLong(7, Instant.now().toEpochMilli());
		Instant creation = cookie.getCreationInstant();
		if (creation != null) {
			statement.setLong(8, creation.toEpochMilli());
		} else {
			statement.setNull(8, Types.INTEGER);
		}
		statement.setBoolean(9, cookie.isSecure());
		statement.setBoolean(10, cookie.isHttpOnly());

		int rowsAffected = statement.executeUpdate();
		if (rowsAffected <= 0) {
			logger.error("Could not insert Cookie into database '{}'. No rows affected: {}", tableName, cookie.getName());
		} else {
			// Set ID on entry
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					entry.setId(id);
					logger.debug("Set ID for inserted Cookie: ID={}, Name={}", id, cookie.getName());
				} else {
					logger.error("Could not get generated ID of Cookie from database '{}': {}", tableName, cookie.getName());
				}
			}
		}
	}

	@Override
	public boolean insertEntries(List<BHCookie> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
					for (BHCookie entry : entries) {
						try {
							insertEntry(entry, statement);
						} catch (SQLException e) {
							logger.error("Could not insert Cookie into database '{}': {}", tableName, entry.getCookie().getName(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not insert Cookies into database '{}'", tableName, e);
				result = false;
			}
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean updateEntry(BHCookie entry) {
		try {
			writeLock.lock();
			if (entry.getId() <= 0) {
				logger.warn("Could not update Cookie in database, because the Cookie has no valid ID: {}. Adding it to database instead.", entry.getId());
				return insertEntry(entry);
			}

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
					updateEntry(entry, statement);
				}
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not update Cookie in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getCookie().getName(), e);
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
	private boolean updateEntry(BHCookie entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.warn("Could not update Cookie in database, because the Cookie has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		Cookie cookie = entry.getCookie();
		statement.setString(1, "");
		statement.setString(2, cookie.getName());
		statement.setString(3, cookie.getValue());
		statement.setString(4, cookie.getDomain());
		statement.setString(5, cookie.getPath());
		Instant expiry = cookie.getExpiryInstant();
		if (expiry != null) {
			statement.setLong(6, expiry.toEpochMilli());
		} else {
			statement.setNull(6, Types.INTEGER);
		}
		statement.setLong(7, Instant.now().toEpochMilli());
		Instant creation = cookie.getCreationInstant();
		if (creation != null) {
			statement.setLong(8, creation.toEpochMilli());
		} else {
			statement.setNull(8, Types.INTEGER);
		}
		statement.setBoolean(9, cookie.isSecure());
		statement.setBoolean(10, cookie.isHttpOnly());
		statement.setInt(11, entry.getId());
		statement.executeUpdate();
		return true;
	}

	@Override
	public boolean updateEntries(List<BHCookie> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
					for (BHCookie entry : entries) {
						try {
							if (!updateEntry(entry, statement)) {
								result = false;
							}
						} catch (SQLException e) {
							logger.error("Could not update Cookie in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getCookie().getName(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not update Cookies in database '{}'", tableName, e);
				result = false;
			}
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean deleteEntry(BHCookie entry) {
		try {
			writeLock.lock();
			if (entry.getId() <= 0) {
				logger.error("Could not delete Cookie in database, because the Cookie has no valid ID: {}", entry.getId());
				return false;
			}

			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(true);
				try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
					deleteEntry(entry, statement);
				}
				logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getCookie().getName());
				return true;
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not delete Cookie into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getCookie().getName(), e);
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
	private void deleteEntry(BHCookie entry, PreparedStatement statement) throws SQLException {
		if (entry.getId() <= 0) {
			logger.error("Could not delete Cookie in database, because the Cookie has no valid ID: {}", entry.getId());
			return;
		}

		statement.setInt(1, entry.getId());
		statement.executeUpdate();
	}

	@Override
	public boolean deleteEntries(List<BHCookie> entries) {
		try {
			writeLock.lock();
			boolean result = true;
			try (Connection con = getDatabaseConnection()) {
				con.setAutoCommit(false);
				try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
					for (BHCookie entry : entries) {
						try {
							deleteEntry(entry, statement);
							logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getCookie().getName());
						} catch (SQLException e) {
							logger.error("Could not delete Cookie into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getCookie().getName(), e);
							result = false;
						}
					}
				}
				con.commit();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error("Could not delete Cookies in database '{}'", tableName, e);
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
				logger.error("Could not delete all Cookies in database '{}'", tableName, e);
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}
}
