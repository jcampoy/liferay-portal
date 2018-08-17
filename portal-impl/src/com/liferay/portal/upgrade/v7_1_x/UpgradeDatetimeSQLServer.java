/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.upgrade.v7_1_x;

import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Couso
 */
public class UpgradeDatetimeSQLServer extends UpgradeProcess {

	protected void alterDatetimeColumns() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			dropDatetimeTableIndexes();

			String selectSQL =
				"select table_name, column_name from " +
				"INFORMATION_SCHEMA.COLUMNS where DATA_TYPE = 'datetime'";

			try (PreparedStatement ps = connection.prepareStatement(selectSQL);
				 ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					String tableName = rs.getString(1);
					String columnName = rs.getString(2);

					runSQL(
						StringBundler.concat(
							"alter table ", tableName, " alter column ",
							columnName, " datetime2(6)"));
				}

				for (String createIndexSQL : _createIndexSQLs) {
					runSQL(createIndexSQL);
				}
			}
			catch (Exception e) {
				_log.error(e, e);
			}
		}
	}

	protected void dropDatetimeTableIndexes() {
		StringBundler sb = new StringBundler(11);

		sb.append("select distinct sysobjects.name as table_name, ");
		sb.append("sysindexes.name as index_name FROM sysobjects inner join ");
		sb.append("sysindexes on sysobjects.id = sysindexes.id inner join ");
		sb.append("syscolumns on sysobjects.id = syscolumns.id inner join ");
		sb.append("sysindexkeys on ((sysobjects.id = sysindexkeys.id) and ");
		sb.append("(syscolumns.colid = sysindexkeys.colid) and ");
		sb.append("(sysindexes.indid = sysindexkeys.indid)) inner join ");
		sb.append("systypes on syscolumns.xtype = systypes.xtype where ");
		sb.append("(sysobjects.type = 'U') and (sysobjects.category != 2) ");
		sb.append("and (systypes.name = 'datetime') ");
		sb.append("order by sysobjects.name, sysindexes.name");

		String sql = sb.toString();

		try (PreparedStatement ps = connection.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String indexName = rs.getString("index_name");

				String indexNameUpperCase = StringUtil.toUpperCase(indexName);

				if (indexNameUpperCase.startsWith("IX")) {

					if (_log.isInfoEnabled()) {
						_log.info(
							StringBundler.concat(
								"Dropping index ", tableName, ".", indexName));
					}

					String indexColumnNames = StringUtil.merge(
						getIndexColumnNames(indexName));

					runSQL(
						StringBundler.concat(
							"drop index ", indexName, " on ", tableName));

					_createIndexSQLs.add(
						StringBundler.concat(
							"create index ", indexNameUpperCase, " on ",
							tableName, " (", indexColumnNames, ");"));
				}
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	protected List<String> getIndexColumnNames(String indexName) {
		List<String> columnNames = new ArrayList<>();

		StringBundler
			sb = new StringBundler(9);

		sb.append("select distinct syscolumns.name as column_name from ");
		sb.append("sysobjects inner join syscolumns on sysobjects.id = ");
		sb.append("syscolumns.id inner join sysindexes on sysobjects.id = ");
		sb.append("sysindexes.id inner join sysindexkeys on ((sysobjects.id ");
		sb.append("= sysindexkeys.id) and (syscolumns.colid = ");
		sb.append("sysindexkeys.colid) and (sysindexes.indid = ");
		sb.append("sysindexkeys.indid)) where sysindexes.name = '");
		sb.append(indexName);
		sb.append("' order by ");

		String sql = sb.toString();

		try (PreparedStatement ps = connection.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				String columnName = rs.getString("column_name");

				columnNames.add(columnName);
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return columnNames;
	}

	@Override
	protected void doUpgrade() throws Exception {
		DB db = DBManagerUtil.getDB();

		if (db.getDBType() != DBType.SQLSERVER) {
			return;
		}

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		if (databaseMetaData.getDatabaseMajorVersion() < _SQL_SERVER_2008) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Type datetime2 was introduced SQL Server 2008 and your " +
						"version is older. We recommend updating to at least " +
						"that version to avoid precision related problems. " +
						"Skipping upgrade step.");
			}

			return;
		}

		alterDatetimeColumns();
	}

	private static final int _SQL_SERVER_2008 = 14;

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeDatetimeSQLServer.class);

	private final List<String> _createIndexSQLs = new ArrayList<>();

}