/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portlet.sites.util;

import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.test.ExecutionTestListeners;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutSetPrototype;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.PortletPreferences;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceTestUtil;
import com.liferay.portal.test.LiferayIntegrationJUnitTestRunner;
import com.liferay.portal.test.MainServletExecutionTestListener;
import com.liferay.portal.test.Sync;
import com.liferay.portal.test.SynchronousDestinationExecutionTestListener;
import com.liferay.portal.test.TransactionalCallbackAwareExecutionTestListener;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portal.util.TestPropsValues;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jose Jimenez
 */
@ExecutionTestListeners(
	listeners = {
		MainServletExecutionTestListener.class,
		SynchronousDestinationExecutionTestListener.class,
		TransactionalCallbackAwareExecutionTestListener.class
	})
@RunWith(LiferayIntegrationJUnitTestRunner.class)
@Sync
@Transactional
public class SitesUtilTest {

	@Before
	public void setUp() throws Exception {

		// Create site template

		FinderCacheUtil.clearCache();

		LayoutSetPrototype layoutSetPrototype =
			ServiceTestUtil.addLayoutSetPrototype(
				ServiceTestUtil.randomString());
		_layoutSetPrototypeGroup = layoutSetPrototype.getGroup();

		// Create two pages

		_layoutSetPrototypeLayout1 = ServiceTestUtil.addLayout(
			_layoutSetPrototypeGroup.getGroupId(),
			ServiceTestUtil.randomString(), true);
		_layoutSetPrototypeLayout2 = ServiceTestUtil.addLayout(
				_layoutSetPrototypeGroup.getGroupId(),
			ServiceTestUtil.randomString(), true);

		// set 2_columns_i to each page

		updateLayoutTemplateId(_layoutSetPrototypeLayout1, "2_columns_i");
		updateLayoutTemplateId(_layoutSetPrototypeLayout1, "2_columns_i");

		// add web content display with an article to column-1 of each page

		_layoutSetPrototypeJournalArticle1 = addJournalArticle(
			_layoutSetPrototypeGroup.getGroupId(), 0, "Test Article 1",
			"Test Content 1");

		_layoutSetPrototypeJournalContentPortletId1 =
			addJournalContentPortletToLayout(
				TestPropsValues.getUserId(), _layoutSetPrototypeLayout1,
				_layoutSetPrototypeJournalArticle1, "column-1");

		_layoutSetPrototypeJournalArticle2 = addJournalArticle(
			_layoutSetPrototypeGroup.getGroupId(), 0, "Test Article 2",
			"Test Content 2");

		_layoutSetPrototypeJournalContentPortletId2 =
			addJournalContentPortletToLayout(
				TestPropsValues.getUserId(), _layoutSetPrototypeLayout2,
				_layoutSetPrototypeJournalArticle2, "column-1");

		// Create site from site template

		_group = ServiceTestUtil.addGroup();

		SitesUtil.updateLayoutSetPrototypesLinks(
			_group, layoutSetPrototype.getLayoutSetPrototypeId(), 0, true,
			true);

		propagateChanges(_group);
	}

	@Test
	public void testReset() throws Exception {

		Layout siteLayout1 = LayoutLocalServiceUtil.getFriendlyURLLayout(
				_group.getGroupId(), false,
				_layoutSetPrototypeLayout1.getFriendlyURL());

		Layout siteLayout2 = LayoutLocalServiceUtil.getFriendlyURLLayout(
				_group.getGroupId(), false,
				_layoutSetPrototypeLayout2.getFriendlyURL());

		// modify the page (moving the porlet)

		movePortlet(TestPropsValues.getUserId(),
				siteLayout1, _layoutSetPrototypeJournalContentPortletId1,
				"column-2");

		// refresh layouts

		siteLayout1 = LayoutLocalServiceUtil.getFriendlyURLLayout(
				_group.getGroupId(), false,
				_layoutSetPrototypeLayout1.getFriendlyURL());

		Assert.assertEquals(
			SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout1), true);

		siteLayout2 = LayoutLocalServiceUtil.getFriendlyURLLayout(
			_group.getGroupId(), false,
			_layoutSetPrototypeLayout2.getFriendlyURL());

		Assert.assertEquals(
			SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout2), false);

		movePortlet(TestPropsValues.getUserId(), siteLayout2,
			_layoutSetPrototypeJournalContentPortletId2, "column-2");

		siteLayout1 = LayoutLocalServiceUtil.getFriendlyURLLayout(
			_group.getGroupId(), false,
			_layoutSetPrototypeLayout1.getFriendlyURL());

		Assert.assertEquals(
				SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout1), true);

		siteLayout2 = LayoutLocalServiceUtil.getFriendlyURLLayout(
			_group.getGroupId(), false,
			_layoutSetPrototypeLayout2.getFriendlyURL());

		Assert.assertEquals(
			SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout2), true);

		SitesUtil.resetPrototype(siteLayout1);

		propagateChanges(_group);

		siteLayout1 = LayoutLocalServiceUtil.getFriendlyURLLayout(
				_group.getGroupId(), false,
				_layoutSetPrototypeLayout1.getFriendlyURL());

		Assert.assertEquals(
				SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout1), false);

		siteLayout2 = LayoutLocalServiceUtil.getFriendlyURLLayout(
				_group.getGroupId(), false,
				_layoutSetPrototypeLayout2.getFriendlyURL());

		Assert.assertEquals(
				SitesUtil.isLayoutModifiedSinceLastMerge(siteLayout2), true);
	}

	protected JournalArticle addJournalArticle(
			long groupId, long folderId, String name, String content)
		throws Exception {

		Map<Locale, String> titleMap = new HashMap<Locale, String>();

		Locale locale = LocaleUtil.getDefault();

		String localeId = locale.toString();

		titleMap.put(locale, name);

		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();

		ServiceContext serviceContext = ServiceTestUtil.getServiceContext();

		String xmlContent = getArticleContent(content, localeId);

		return JournalArticleLocalServiceUtil.addArticle(
			TestPropsValues.getUserId(), groupId, folderId, 0, 0,
			StringPool.BLANK, true, 1, titleMap, descriptionMap, xmlContent,
			"general", null, null, null, 1, 1, 1965, 0, 0, 0, 0, 0, 0, 0, true,
			0, 0, 0, 0, 0, true, false, false, null, null, null, null,
			serviceContext);
	}

	protected String addJournalContentPortletToLayout(
			long userId, Layout layout, JournalArticle journalArticle,
			String columnId)
		throws Exception {

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		String journalPortletId = layoutTypePortlet.addPortletId(
			userId, PortletKeys.JOURNAL_CONTENT, columnId, -1);

		LayoutLocalServiceUtil.updateLayout(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			layout.getTypeSettings());

		javax.portlet.PortletPreferences prefs = getPortletPreferences(
			layout.getCompanyId(), layout.getPlid(), journalPortletId);

		prefs.setValue("articleId", journalArticle.getArticleId());
		prefs.setValue("groupId", String.valueOf(journalArticle.getGroupId()));
		prefs.setValue("showAvailableLocales", Boolean.TRUE.toString());

		updatePortletPreferences(layout.getPlid(), journalPortletId, prefs);

		return journalPortletId;
	}

	protected String getArticleContent(String content, String localeId) {
		StringBundler sb = new StringBundler();

		sb.append("<?xml version=\"1.0\"?><root available-locales=");
		sb.append("\"" + localeId + "\" ");
		sb.append("default-locale=\"" + localeId + "\">");
		sb.append("<static-content language-id=\"" + localeId + "\">");
		sb.append("<![CDATA[<p>");
		sb.append(content);
		sb.append("</p>]]>");
		sb.append("</static-content></root>");

		return sb.toString();
	}

	protected javax.portlet.PortletPreferences getPortletPreferences(
			long companyId, long plid, String portletId)
		throws Exception {

		return PortletPreferencesLocalServiceUtil.getPreferences(
			companyId, PortletKeys.PREFS_OWNER_ID_DEFAULT,
			PortletKeys.PREFS_OWNER_TYPE_LAYOUT, plid, portletId);
	}

	protected void movePortlet(
			long userId, Layout layout, String portletId, String columnId)
		throws Exception {

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		layoutTypePortlet.movePortletId(userId, portletId, columnId, -1);

		LayoutLocalServiceUtil.updateLayout(
				layout.getGroupId(), layout.isPrivateLayout(),
				layout.getLayoutId(), layout.getTypeSettings());

	}

	protected void propagateChanges(Group group) throws Exception {
		LayoutLocalServiceUtil.getLayouts(
			group.getGroupId(), false,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);
	}

	protected Layout updateLayoutTemplateId(
		Layout layout, String layoutTemplateId) throws Exception {

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		layoutTypePortlet.setLayoutTemplateId(
			TestPropsValues.getUserId(), layoutTemplateId);

		return LayoutServiceUtil.updateLayout(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			layout.getTypeSettings());
	}

	protected PortletPreferences updatePortletPreferences(
			long plid, String portletId,
			javax.portlet.PortletPreferences jxPreferences)
		throws Exception {

		PortletPreferences portletPreferences =
			PortletPreferencesLocalServiceUtil.updatePreferences(
				PortletKeys.PREFS_OWNER_ID_DEFAULT,
				PortletKeys.PREFS_OWNER_TYPE_LAYOUT, plid, portletId,
				jxPreferences);

		return portletPreferences;
	}

	private Group _group;
	private Group _layoutSetPrototypeGroup;
	private JournalArticle _layoutSetPrototypeJournalArticle1;
	private JournalArticle _layoutSetPrototypeJournalArticle2;
	private String _layoutSetPrototypeJournalContentPortletId1;
	private String _layoutSetPrototypeJournalContentPortletId2;
	private Layout _layoutSetPrototypeLayout1;
	private Layout _layoutSetPrototypeLayout2;

}