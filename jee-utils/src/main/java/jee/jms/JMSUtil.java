package jee.jms;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import data.LogUtil;
import data.Util;
import jee.jndi.NamingContext;

public class JMSUtil implements Util {
	public void print(NamingContext ctx, String start, OutputStream os) {
		PrintStream ps = new PrintStream(os);
		NamingEnumeration<NameClassPair> it;
		try {
			it = ctx.getContext().list(start);

			while (it.hasMoreElements()) {
				NameClassPair nc = it.next();
				ps.println(nc.getName() + ";" + nc.getClassName());
			}
		} catch (NamingException e) {
			LogUtil.getInstance().error("naming exception", e);
		}
	}
}
