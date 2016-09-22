package com.liferay.portal.remote.rest.example;

import com.liferay.portal.remote.rest.example.ExampleResource2.MyData;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

public class MyDataMessageBodyWriter implements MessageBodyWriter<ExampleResource2.MyData> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

		return type.isAssignableFrom(ExampleResource2.MyData.class) && mediaType.toString().equals("text/plain");
	}

	@Override
	public long getSize(MyData t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(MyData myData, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		PrintWriter pw = new PrintWriter(entityStream, true);
		pw.println("text/plain -> " + myData.getA() + " " + myData.getB() );
	}

	
}
