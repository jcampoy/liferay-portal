/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.remote.rest.example;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate = true, service = Application.class)
@ApplicationPath("/api/example")
public class ExampleApplication extends Application {

//	@Override
//	public Set<Class<?>> getClasses() {
//		return Collections.<Class<?>>singleton(ExampleResource.class);
//	}

	// la colección de recursos de tu app tiene que estar definida aquí.
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(ExampleResource.class, ExampleResource2.class, MyDataMessageBodyWriter.class));
	}
}
