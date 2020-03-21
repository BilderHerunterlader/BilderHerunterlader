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

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;

/**
 * Class for Sqlite Database connections for Queue Database
 */
public class QueueSQLiteDB extends SQLiteDB<Pic> {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(QueueSQLiteDB.class);

	private final String selectAllEntriesSQL;

	private final String selectEntrySQL;

	private final String insertEntrySQL;

	private final String updateEntrySQL;

	private final String deleteEntrySQL;

	/**
	 * Constructor
	 * 
	 * @param databaseFile Path to the database File
	 */
	public QueueSQLiteDB(String databaseFile) {
		super(databaseFile, "bh_downloadqueue");

		selectAllEntriesSQL = "SELECT * FROM " + tableName;

		selectEntrySQL = "SELECT * FROM " + tableName + " WHERE DownloadID = ?";

		StringBuilder sbInsertEntry = new StringBuilder();
		sbInsertEntry.append("INSERT INTO ");
		sbInsertEntry.append(tableName);
		sbInsertEntry.append(" (");
		sbInsertEntry.append("ContainerURL, ");
		sbInsertEntry.append("ThreadURL, ");
		sbInsertEntry.append("ThumbURL, ");
		sbInsertEntry.append("DownloadURL, ");
		sbInsertEntry.append("TargetPath, ");
		sbInsertEntry.append("TargetFilename, ");
		sbInsertEntry.append("FixedTargetFilename, ");
		sbInsertEntry.append("LastModified, ");
		sbInsertEntry.append("FixedLastModified, ");
		sbInsertEntry.append("Size, ");
		sbInsertEntry.append("Status, ");
		sbInsertEntry.append("ErrorMessage, ");
		sbInsertEntry.append("Deactivated, ");
		sbInsertEntry.append("RenameWithContentDisposition, ");
		sbInsertEntry.append("DateTime");
		sbInsertEntry.append(") VALUES (");
		sbInsertEntry.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
		sbInsertEntry.append(")");
		insertEntrySQL = sbInsertEntry.toString();

		StringBuilder sbUpdateEntry = new StringBuilder();
		sbUpdateEntry.append("UPDATE ");
		sbUpdateEntry.append(tableName);
		sbUpdateEntry.append(" SET ");
		sbUpdateEntry.append("ContainerURL = ?, ");
		sbUpdateEntry.append("ThreadURL = ?, ");
		sbUpdateEntry.append("ThumbURL = ?, ");
		sbUpdateEntry.append("DownloadURL = ?, ");
		sbUpdateEntry.append("TargetPath = ?, ");
		sbUpdateEntry.append("TargetFilename = ?, ");
		sbUpdateEntry.append("FixedTargetFilename = ?, ");
		sbUpdateEntry.append("LastModified = ?, ");
		sbUpdateEntry.append("FixedLastModified = ?, ");
		sbUpdateEntry.append("Size = ?, ");
		sbUpdateEntry.append("Status = ?, ");
		sbUpdateEntry.append("ErrorMessage = ?, ");
		sbUpdateEntry.append("Deactivated = ?, ");
		sbUpdateEntry.append("RenameWithContentDisposition = ?, ");
		sbUpdateEntry.append("DateTime = ?");
		sbUpdateEntry.append(" WHERE DownloadID = ?");
		updateEntrySQL = sbUpdateEntry.toString();

		deleteEntrySQL = "DELETE FROM " + tableName + " WHERE DownloadID = ?";

		createDatabaseIfNotExist();
	}

	@Override
	protected synchronized boolean createDatabaseIfNotExist() {
		// Create table if not exist
		StringBuilder sbCreateTable = new StringBuilder();
		sbCreateTable.append("CREATE TABLE IF NOT EXISTS ");
		sbCreateTable.append(tableName);
		sbCreateTable.append(" (");
		sbCreateTable.append("DownloadID INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sbCreateTable.append("ContainerURL TEXT NOT NULL, ");
		sbCreateTable.append("ThreadURL TEXT NOT NULL, ");
		sbCreateTable.append("ThumbURL TEXT NOT NULL, ");
		sbCreateTable.append("DownloadURL TEXT NOT NULL, ");
		sbCreateTable.append("TargetPath TEXT NOT NULL, ");
		sbCreateTable.append("TargetFilename TEXT NOT NULL, ");
		sbCreateTable.append("FixedTargetFilename BOOLEAN NOT NULL, ");
		sbCreateTable.append("LastModified BIGINT NOT NULL, ");
		sbCreateTable.append("FixedLastModified BOOLEAN NOT NULL, ");
		sbCreateTable.append("Size BIGINT NOT NULL, ");
		sbCreateTable.append("Status INTEGER NOT NULL, ");
		sbCreateTable.append("ErrorMessage TEXT NOT NULL, ");
		sbCreateTable.append("Deactivated BOOLEAN NOT NULL, ");
		sbCreateTable.append("RenameWithContentDisposition BOOLEAN NOT NULL, ");
		sbCreateTable.append("DateTime BIGINT NOT NULL");
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
	protected Pic convertResultSetToObject(ResultSet result) throws SQLException {
		int id = result.getInt("DownloadID");
		String urlContainer = result.getString("ContainerURL");
		String threadURL = result.getString("ThreadURL");
		String thumb = result.getString("ThumbURL");
		String downloadURL = result.getString("DownloadURL");
		String targetPath = result.getString("TargetPath");
		String targetFilename = result.getString("TargetFilename");
		boolean fixedTargetFilename = result.getBoolean("FixedTargetFilename");
		long lastModified = result.getLong("LastModified");
		boolean fixedLastModified = result.getBoolean("FixedLastModified");
		long size = result.getLong("Size");
		int status = result.getInt("Status");
		PicState state = PicState.getByValue(status);
		String errMsg = result.getString("ErrorMessage");
		boolean deactivated = result.getBoolean("Deactivated");
		boolean renameWithContentDisposition = result.getBoolean("RenameWithContentDisposition");
		long dateTime = result.getLong("DateTime");

		return new Pic(id, urlContainer, targetFilename, targetPath, threadURL, thumb, downloadURL, fixedTargetFilename, lastModified, fixedLastModified, size, state, errMsg, deactivated, renameWithContentDisposition, dateTime);
	}

	@Override
	public synchronized List<Pic> getAllEntries() {
		List<Pic> pics = new ArrayList<>();

		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectAllEntriesSQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						pics.add(convertResultSetToObject(rs));
					}
					return pics;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get Pics from database '{}'", tableName, e);
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "Database-Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	@Override
	public synchronized Pic getEntry(int id) {
		try (Connection con = getDatabaseConnection()) {
			try (PreparedStatement statement = con.prepareStatement(selectEntrySQL)) {
				try (ResultSet rs = statement.executeQuery()) {
					if (!rs.first()) {
						logger.error("Could not find Pic in database: {}", id);
						return null;
					}
					return convertResultSetToObject(rs);
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not get Pic from database '{}': {}", tableName, id, e);
			return null;
		}
	}

	@Override
	public synchronized boolean insertEntry(Pic entry) {
		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(insertEntrySQL, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, entry.getContainerURL());
				statement.setString(2, entry.getThreadURL());
				statement.setString(3, entry.getThumb());
				statement.setString(4, entry.getDownloadURL());
				statement.setString(5, entry.getTargetPath());
				statement.setString(6, entry.getTargetFilename());
				statement.setBoolean(7, entry.isFixedTargetFilename());
				statement.setLong(8, entry.getLastModified());
				statement.setBoolean(9, entry.isFixedLastModified());
				statement.setLong(10, entry.getSize());
				statement.setInt(11, entry.getStatus().getValue());
				statement.setString(12, entry.getErrMsg());
				statement.setBoolean(13, entry.isDeactivated());
				statement.setBoolean(14, entry.isRenameWithContentDisposition());
				statement.setLong(15, entry.getDateTimeSimple());

				int rowsAffected = statement.executeUpdate();
				if (rowsAffected <= 0) {
					logger.error("Could not insert Pic into database '{}'. No rows affected: {}", tableName, entry.getContainerURL());
				} else {
					// Set ID on entry
					try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							int id = generatedKeys.getInt(1);
							entry.setId(id);
							logger.debug("Set ID for inserted Pic: ID={}, ContainerURL={}", id, entry.getContainerURL());
						} else {
							logger.error("Could not get generated ID of Pic from database '{}': {}", tableName, entry.getContainerURL());
						}
					}
				}
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not insert Pic into database '{}': {}", tableName, entry.getContainerURL(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean insertEntries(List<Pic> entries) {
		boolean result = true;
		for (Pic entry : entries) {
			if (!insertEntry(entry)) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public synchronized boolean updateEntry(Pic entry) {
		if (entry.getId() <= 0) {
			logger.warn("Could not update Pic in database, because the Pic has no valid ID: {}. Adding it to database instead.", entry.getId());
			return insertEntry(entry);
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(updateEntrySQL)) {
				statement.setString(1, entry.getContainerURL());
				statement.setString(2, entry.getThreadURL());
				statement.setString(3, entry.getThumb());
				statement.setString(4, entry.getDownloadURL());
				statement.setString(5, entry.getTargetPath());
				statement.setString(6, entry.getTargetFilename());
				statement.setBoolean(7, entry.isFixedTargetFilename());
				statement.setLong(8, entry.getLastModified());
				statement.setBoolean(9, entry.isFixedLastModified());
				statement.setLong(10, entry.getSize());
				statement.setInt(11, entry.getStatus().getValue());
				statement.setString(12, entry.getErrMsg());
				statement.setBoolean(13, entry.isDeactivated());
				statement.setBoolean(14, entry.isRenameWithContentDisposition());
				statement.setLong(15, entry.getDateTimeSimple());
				statement.setInt(16, entry.getId());
				statement.executeUpdate();
			}
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not update Pic in database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean updateEntries(List<Pic> entries) {
		boolean result = true;
		for (Pic entry : entries) {
			if (!updateEntry(entry)) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public synchronized boolean deleteEntry(Pic entry) {
		if (entry.getId() <= 0) {
			logger.error("Could not delete Pic in database, because the Pic has no valid ID: {}", entry.getId());
			return false;
		}

		try (Connection con = getDatabaseConnection()) {
			con.setAutoCommit(true);
			try (PreparedStatement statement = con.prepareStatement(deleteEntrySQL)) {
				statement.setInt(1, entry.getId());
				statement.executeUpdate();
			}
			logger.debug("Deleted entry with ID {}: {}", entry.getId(), entry.getContainerURL());
			return true;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Could not delete Pic into database '{}' with ID {}: {}", tableName, entry.getId(), entry.getContainerURL(), e);
			return false;
		}
	}

	@Override
	public synchronized boolean deleteEntries(List<Pic> entries) {
		boolean result = true;
		for (Pic entry : entries) {
			if (!deleteEntry(entry)) {
				result = false;
			}
		}
		return result;
	}
}
