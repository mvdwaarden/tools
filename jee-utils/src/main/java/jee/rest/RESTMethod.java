package jee.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jee.rest.RESTDispatchServlet.DispatchType;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface RESTMethod {
	DispatchType dispatchType();
	String URLPattern();
}
