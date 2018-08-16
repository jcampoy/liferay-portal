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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Ricardo Couso
 */
public class UpgradeDatetimeSQLServer extends UpgradeProcess {

	protected void alterDatetimeColumns() throws Exception {
		String selectSQL =
			"select table_name, column_name from INFORMATION_SCHEMA.COLUMNS " +
				"where DATA_TYPE = 'datetime'";

		try (PreparedStatement ps = connection.prepareStatement(selectSQL);
			ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				String tableName = rs.getString(1);
				String columnName = rs.getString(2);

				runSQL(
					StringBundler.concat(
						"alter table ", tableName, " alter column ", columnName,
						" datetime2(6)"));
			}
		}
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

}