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

package com.liferay.site.admin.web.internal.asset;

import com.liferay.asset.kernel.model.BaseJSPAssetRenderer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Ricardo Couso
 */
public class SiteAssetRenderer extends BaseJSPAssetRenderer<Group> {

	public SiteAssetRenderer(Group group) {
		if (group.isSite()) {
			_site = group;
		}
		else {
			_site = null;
		}
	}

	@Override
	public Group getAssetObject() {
		return _site;
	}

	@Override
	public String getClassName() {
		return Group.class.getName();
	}

	@Override
	public long getClassPK() {
		return _site.getPrimaryKey();
	}

	@Override
	public long getGroupId() {
		return _site.getGroupId();
	}

	@Override
	public String getJspPath(HttpServletRequest request, String template) {
		return null;
	}

	@Override
	public String getSummary(
		PortletRequest portletRequest, PortletResponse portletResponse) {

		return null;
	}

	@Override
	public String getTitle(Locale locale) {
		try {
			return _site.getDescriptiveName(locale);
		}
		catch (PortalException pe) {
			_log.error(
				"Unable to get descriptive name for group " +
					_site.getGroupId(),
				pe);
		}

		return null;
	}

	@Override
	public long getUserId() {
		return 0;
	}

	@Override
	public String getUserName() {
		return null;
	}

	@Override
	public String getUuid() {
		return _site.getUuid();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SiteAssetRenderer.class);

	private final Group _site;

}