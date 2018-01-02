package data.test;

import org.junit.Assert;
import org.junit.Test;

import data.ConfigurationUtil;
import data.DataUtil;
import replace.ReplaceContext;

//@TODO nog even checken
public class ReplaceTest {
	@Test
	public void testReplace() {
		class Message {
			public String uuid;
			public String message;

			@SuppressWarnings("unused")
			public String getUuid() {
				return uuid;
			}

			@SuppressWarnings("unused")
			public void setUuid(String uuid) {
				this.uuid = uuid;
			}

			@SuppressWarnings("unused")
			public String getMessage() {
				return message;
			}

			@SuppressWarnings("unused")
			public void setMessage(String message) {
				this.message = message;
			}

		}
		;
		class TestReplaceContext {
			public Message request;
			public Message response;

			@SuppressWarnings("unused")
			public Message getRequest() {
				return request;
			}

			@SuppressWarnings("unused")
			public void setRequest(Message request) {
				this.request = request;
			}

			@SuppressWarnings("unused")
			public Message getResponse() {
				return response;
			}

			@SuppressWarnings("unused")
			public void setResponse(Message response) {
				this.response = response;
			}

		}
		;
		TestReplaceContext tst = new TestReplaceContext();
		tst.request = new Message();
		tst.response = new Message();
		tst.request.uuid = "1234";
		tst.request.message = "hello";
		tst.response.uuid = "4321";
		tst.response.message = "world";
		String filename = ConfigurationUtil.getInstance().getTestResourcesPath() + "xml/replacetest";
		String template = DataUtil.getInstance().readFromFile(filename + ".xml");
		ReplaceContext ctx = new ReplaceContext("ctx", tst);
		String result = ctx.replace(template);
		String compare = DataUtil.getInstance().readFromFile(filename + "-compare.xml");
		DataUtil.getInstance().writeToFile(filename + "xml.out", result);
		Assert.assertEquals("comparisson to xml/replacetest-compare.xml failed", result, compare);
		return;
	}
}
