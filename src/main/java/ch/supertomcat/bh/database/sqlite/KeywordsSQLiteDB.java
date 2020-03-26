package ch.supertomcat.bh.database.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.keywords.Keyword;

/**
 * Class for Sqlite Database connections for Queue Database
 */
public class KeywordsSQLiteDB extends SQLiteDB<Keyword> {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(KeywordsSQLiteDB.class);

	private final String selectAllEntriesSQL;

	private final String selectEntrySQL;

	private final String insertEntrySQL;

	private final String updateEntrySQL;

	private final String deleteEntrySQL;

	/**
	 * Constructor
	 * 
	 * @param databaseFile Path to the database File
	 * @param backupDatabaseOnStart True if database should be backed up on start, false otherwise
	 */
	public KeywordsSQLiteDB(String databaseFile, boolean backupDatabaseOnStart) {
		super(databaseFile, "bh_keywords", backupDatabaseOnStart);

		selectAllEntriesSQL = "SELECT * FROM " + tableName;

		selectEntrySQL = "SELECT * FROM " + tableName + " WHERE KeywordID = ?";

		StringBuilder sbInsertEntry = new StringBuilder();
		sbInsertEntry.append("INSERT INTO ");
		sbInsertEntry.append(tableName);
		sbInsertEntry.append(" (");
		sbInsertEntry.append("Title, ");
		sbInsertEntry.append("Keywords, ");
		sbInsertEntry.append("DownloadPath, ");
		sbInsertEntry.append("RelativeDownloadPath, ");
		sbInsertEntry.append("RelativePath");
		sbInsertEntry.append(") VALUES (");
		sbInsertEntry.append("?, ?, ?, ?, ?");
		sbInsertEntry.append(")");
		insertEntrySQL = sbInsertEntry.toString();

		StringBuilder sbUpdateEntry = new StringBuilder();
		sbUpdateEntry.append("UPDATE ");
		sbUpdateEntry.append(tableName);
		sbUpdateEntry.append(" SET ");
		sbUpdateEntry.append("Title = ?, ");
		sbUpdateEntry.append("Keywords = ?, ");
		sbUpdateEntry.append("DownloadPath = ?, ");
		sbUpdateEntry.append("RelativeDownloadPath = ?, ");
		sbUpdateEntry.append("RelativePath = ?");
		sbUpdateEntry.append(" WHERE KeywordID = ?");
		updateEntrySQL = sbUpdateEntry.toString();

		deleteEntrySQL = "DELETE FROM " + tableName + " WHERE KeywordID = ?";

		createDatabaseIfNotExist();
	}

	@Override
	protected synchronized boolean createDatabaseIfNotExist() {
		// Create table if not exist
		StringBuilder sbCreateTable = new StringBuilder();
		sbCreateTable.append("CREATE TABLE IF NOT EXISTS ");
		sbCreateTable.append(tableName);
		sbCreateTable.append(" (");
		sbCreateTable.append("KeywordID INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sbCreateTable.append("Title TEXT NOT NULL, ");
		sbCreateTable.append("Keywords TEXT NOT NULL, ");
		sbCreateTable.append("DownloadPath TEXT NOT NULL, ");
		sbCreateTable.append("RelativeDownloadPath TEXT NOT NULL, ");
		sbCreateTable.append("RelativePath BOOLEAN NOT NULL");
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
	}

	@Override
	protected Keyword convertResultSetToObject(ResultSet result) throws SQLException {
		int id = result.getInt("KeywordID");
		String title = result.getString("Title");
		String keywords = result.getString("Keywords");
		String downloadPath = result.getString("DownloadPath");
		String relativeDownloadPath = result.getString("RelativeDownloadPath");
		boolean relativePath = result.getBoolean("RelativePath");

		return new Keyword(id, title, keywords, downloadPath, relativePath, relativeDownloadPath);
	}

	@Override
	public synchronized List<Keyword> getAllEntries() {
		List<Keyword> keywords = new ArrayList<>();

		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectAllEntriesSQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						keywords.add(convertResultSetToObject(rs));
					}
					return keywords;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get Keywords from database '{}'", tableName, e);
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	@Override
	public synchronized Keyword getEntry(int id) {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectEntrySQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					if (!rs.first()) {
						logger.error("Could not find Keyword in database: {}", id);
						return null;
					}
					return convertResultSetToObject(rs);
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get Keyword from database '{}': {}", tableName, id, e);
			return null;
		}
	}

	@Override
	public synchronized boolean insertEntry(Keyword entry) {
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, entry.getTitle());
				statement.setString(2, entry.getKeywords());
				statement.setString(3, entry.getDownloadPath());
				statement.setString(4, entry.getRelativeDownloadPath());
				statement.setBoolean(5, entry.isRelativePath());

				int rowsAffected = statement.executeUpdate();
				if (rowsAffected <= 0) {
					logger.error("Could not insert Keyword into database '{}'. No rows affected: {}", tableName, entry.getTitle());
				} else {
					// Set ID on entry
					try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							int id = generatedKeys.getInt(1);
							entry.setId(id);
							logger.debug("Set ID for inserted Keyword: ID={}, Title={}", id, entry.getTitle());
						} else {
							logger.error("Could not get generated ID of Keyword from database '{}': {}", tableName, entry.getTitle());
						}
					}
				}
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not insert Keyword into database '{}': {}", tableName, entry.getTitle(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean insertEntries(List<Keyword> entries) {
		boolean result = true;
		for (Keyword entry : entries) {
			if (!insertEntry(entry)) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public synchronized boolean updateEntry(Keyword entry) {
		if (entry.getId() <= 0) {
			logger.warn("Could not update Keyword in database, because the Keyword has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
				statement.setString(1, entry.getTitle());
				statement.setString(2, entry.getKeywords());
				statement.setString(3, entry.getDownloadPath());
				statement.setString(4, entry.getRelativeDownloadPath());
				statement.setBoolean(5, entry.isRelativePath());
				statement.setInt(6, entry.getId());
				statement.executeUpdate();
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not update Keyword in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getTitle(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean updateEntries(List<Keyword> entries) {
		boolean result = true;
		for (Keyword entry : entries) {
			if (!updateEntry(entry)) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public synchronized boolean deleteEntry(Keyword entry) {
		if (entry.getId() <= 0) {
			logger.error("Could not delete Keyword in database, because the Keyword has no valid ID: {}", entry.getId());
			return false;
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
				statement.setInt(1, entry.getId());
				statement.executeUpdate();
			}
			logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getTitle());
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not delete Keyword into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getTitle(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean deleteEntries(List<Keyword> entries) {
		boolean result = true;
		for (Keyword entry : entries) {
			if (!deleteEntry(entry)) {
				result = false;
			}
		}
		return result;
	}
}
