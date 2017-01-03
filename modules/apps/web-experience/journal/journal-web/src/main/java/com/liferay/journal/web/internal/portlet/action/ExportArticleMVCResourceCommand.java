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

package com.liferay.journal.web.internal.portlet.action;

import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.web.util.ExportArticleUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PortletPreferencesIds;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Arrays;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Farache
 * @author Eduardo Garcia
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + JournalPortletKeys.JOURNAL,
		"mvc.command.name=exportArticle"
	},
	service = MVCResourceCommand.class
)
public class ExportArticleMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		try {
			String targetExtension = ParamUtil.getString(
				resourceRequest, "targetExtension");

			targetExtension = StringUtil.toUpperCase(targetExtension);

			String[] allowedExtensions = getAllowedExtensions(resourceRequest);

			if (ArrayUtil.contains(
					allowedExtensions,
					StringUtil.toUpperCase(targetExtension))) {

				_exportArticleUtil.sendFile(
					targetExtension, resourceRequest, resourceResponse);
			}
		}
		catch (Exception e) {
			PortalUtil.sendError(
				e, (ActionRequest)resourceRequest,
				(ActionResponse)resourceResponse);
		}
	}

	protected String[] getAllowedExtensions(ResourceRequest resourceRequest)
			throws PortalException {

		PortletPreferences portletPreferences =
				resourceRequest.getPreferences();

		String assetPublisherId =
				ParamUtil.getString(resourceRequest, "assetPublisherId", null);

		if (Validator.isNotNull(assetPublisherId)) {
			PortletPreferencesIds portletPreferencesIds =
				PortletPreferencesFactoryUtil.getPortletPreferencesIds(
					PortalUtil.getHttpServletRequest(resourceRequest),
					assetPublisherId);

			long originalPlid =
				ParamUtil.getLong(resourceRequest, "originalPlid", 0L);

			if (originalPlid != 0l) {
				portletPreferencesIds = new PortletPreferencesIds(
					portletPreferencesIds.getCompanyId(),
					portletPreferencesIds.getOwnerId(),
					portletPreferencesIds.getOwnerType(),
					originalPlid,
					portletPreferencesIds.getPortletId());
			}

			portletPreferences =
				PortletPreferencesLocalServiceUtil.getPreferences(
					portletPreferencesIds);
		}
		String[] extensions = portletPreferences.getValues("extensions", null);
		for (int i = 0; i < extensions.length; ++i) {
			extensions[i] = StringUtil.toUpperCase(extensions[i]);
		}
		return extensions;
	}

	@Reference(unbind = "-")
	protected void setExportArticleUtil(ExportArticleUtil exportArticleUtil) {
		_exportArticleUtil = exportArticleUtil;
	}

	private ExportArticleUtil _exportArticleUtil;

}