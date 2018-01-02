package nl.ordina.tools.soa.osb.test;

public interface OSBTestConst {
	String OSB_TEST_BIND = "osbtest.bind";
	String OSB_TEST_PORT = "osbtest.port";
	String FUNCTION_PLACEHOLDER = "%function%";
	String SERVER_PLACEHOLDER = "%server%";
	String OSB_RESPONSE_DELAY = "osbtest.response.%function%.delay";
	String OSB_RESPONSE_READ_TIME_OUT = "osbtest.response.%function%.readtimeout";
	String OSB_RESPONSE_PATH = "osbtest.response.path";
	String HTTP_HEADER_URL = "_URL";
	String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	String HTTP_HEADER_FUNCTION = "SLOW_HEADER";
	String HTTP_PATH_SEPARATOR = "/";
	String HTTP_HEADER_ITEM = "SLOW_ITEM";

	String HTTP_METHOD_GET = "GET";
	String HTTP_METHOD_POST = "POST";
	String FUNCTION_SLOW = "slow";
	String FUNCTION_BLOCK = "block";
	String JMS_HOSTNAME = "osbtest.jms.%server%.server.host";
	String JMS_PORT = "osbtest.jms.%server%.server.port";
	String WLS_JMS_USER = "osbtest.jms.%server%.user";
	String WLS_JMS_CREDENTIALS = "osbtest.jms.%server%.password";
	String WLS_JMS_CONNECTION_FACTORY = "osbtest.jms.%server%.connection.factory";
	String WLS_JMS_SEND_QUEUE = "osbtest.jms.%server%.send.queue";
	String WLS_JMS_RECEIVE_QUEUE = "osbtest.jms.%server%.receive.queue";
}