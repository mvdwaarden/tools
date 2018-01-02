package comm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import data.LogUtil;

public abstract class TCPIPServer {
	private List<String> bindings;
	private ServerSocket listenSocket;
	private int port;
	private String keystoreFilename;
	@SuppressWarnings("unused")
	private String ksuser;
	private String kspass;
	private Thread dispatcherThread;
	private Dispatcher dispatcherWorker;
	private List<RequestResponseWorker> workers;

	public TCPIPServer(String binding, int port) {
		this(new String[] { binding }, port, null, null, null);
	}

	public TCPIPServer(String binding, int port, String keystoreFilename, String ksuser, String kspass) {
		this(new String[] { binding }, port, keystoreFilename, ksuser, kspass);
	}

	public TCPIPServer(String[] bindings, int port, String keystoreFilename, String ksuser, String kspass) {
		this.bindings = new ArrayList<>();
		for (String binding : bindings) {
			this.bindings.add(binding);
		}
		this.port = port;
		this.workers = new ArrayList<>();
		this.keystoreFilename = keystoreFilename;
		this.ksuser = ksuser;
		this.kspass = kspass;
	}

	public ServerSocket createServerSocket() {
		ServerSocket result = null;
		KeyStore ks;

		if (null != keystoreFilename) {
			try {
				ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream(keystoreFilename), kspass.toCharArray());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, kspass.toCharArray());
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(kmf.getKeyManagers(), null, null);
				SSLServerSocketFactory ssf = sc.getServerSocketFactory();
				SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(port);
				printServerSocketInfo(s);
				result = s;
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
					| UnrecoverableKeyException | KeyManagementException e) {
				LogUtil.getInstance().error("unable to create server socket", e);
			}

		} else {
			try {
				result = new ServerSocket(port, 50, Inet4Address.getByName(bindings.get(0)));
			} catch (IOException e) {
				LogUtil.getInstance().error("unable to create server socket", e);
			}
		}
		printServerSocketInfo(result);

		return result;
	}

	public void start() {
		try {
			listenSocket = createServerSocket();
			dispatcherWorker = new Dispatcher();
			dispatcherThread = new Thread(dispatcherWorker);
			dispatcherThread.start();
			LogUtil.getInstance().info("running server on [" + bindings.get(0) + ":" + port + "]");
		} catch (NullPointerException e) {
			LogUtil.getInstance().error("unable to listen on [" + bindings.get(0) + ":" + port + "]", e);
		}
	}

	public void stop() {
		if (null != dispatcherWorker) {
			dispatcherWorker.stop();
			for (RequestResponseWorker worker : workers)
				worker.stop();
			dispatcherWorker = null;
		}
	}

	public abstract StreamHandler createStreamHandler();

	private class Dispatcher implements Runnable {
		protected boolean running;

		@Override
		public void run() {
			running = true;
			try {
				while (running) {
					LogUtil.getInstance().info("accepting connections on [" + bindings.get(0) + ":" + port + "]");
					Socket conn = listenSocket.accept();
					// deferred start
					new Thread(new Runnable() {
						@Override
						public void run() {
							printSocketInfo(conn);
							LogUtil.getInstance()
									.info("accepted connection from [" + conn.getRemoteSocketAddress() + "]");
							StreamHandler handler = createStreamHandler();
							RequestResponseWorker worker = new RequestResponseWorker(conn, TCPIPServer.this, handler);
							workers.add(worker);
							Thread thr = new Thread(worker);
							thr.setName(handler.getClass().getName() + "_handle(#" + thr.hashCode() + ")");
							thr.start();
						}
					}).start();

				}
			} catch (IOException e) {
				LogUtil.getInstance().error("unable to accept incomming [" + bindings.get(0) + ":" + port + "]", e);
			}
		}

		public void stop() {
			running = false;
		}
	}

	private static void printServerSocketInfo(ServerSocket s) {
		LogUtil.getInstance().info("Server socket class: " + s.getClass());
		LogUtil.getInstance().info("   Socket address = " + s.getInetAddress().toString());
		LogUtil.getInstance().info("   Socket port = " + s.getLocalPort());

		if (s instanceof SSLServerSocket) {
			LogUtil.getInstance().info("   Need client authentication = " + ((SSLServerSocket) s).getNeedClientAuth());
			LogUtil.getInstance().info("   Want client authentication = " + ((SSLServerSocket) s).getWantClientAuth());
			LogUtil.getInstance().info("   Use client mode = " + ((SSLServerSocket) s).getUseClientMode());
		}
	}

	private static void printSocketInfo(Socket s) {
		LogUtil.getInstance().info("Socket class: " + s.getClass());
		LogUtil.getInstance().info("   Remote address = " + s.getInetAddress().toString());
		LogUtil.getInstance().info("   Remote port = " + s.getPort());
		LogUtil.getInstance().info("   Local socket address = " + s.getLocalSocketAddress().toString());
		LogUtil.getInstance().info("   Local address = " + s.getLocalAddress().toString());
		LogUtil.getInstance().info("   Local port = " + s.getLocalPort());
		if (s instanceof SSLSocket) {
			LogUtil.getInstance().info("   Need client authentication = " + ((SSLSocket) s).getNeedClientAuth());
			SSLSession ss = ((SSLSocket) s).getSession();
			LogUtil.getInstance().info("   Cipher suite = " + ss.getCipherSuite());
			LogUtil.getInstance().info("   Protocol = " + ss.getProtocol());
		}
	}

	private class RequestResponseWorker implements Runnable {
		private Socket socket;
		private TCPIPServer server;
		private StreamHandler handler;

		public RequestResponseWorker(Socket socket, TCPIPServer server, StreamHandler handler) {
			this.socket = socket;
			this.server = server;
			this.handler = handler;
		}

		@Override
		public void run() {
			try (InputStream is = socket.getInputStream(); OutputStream os = socket.getOutputStream()) {
				handler.handle(is, os);
			} catch (IOException e) {
				LogUtil.getInstance().error("unable to write to [" + socket.getRemoteSocketAddress() + "]", e);
			}
			close();
			server.unregister(this);
		}

		public void stop() {
			close();
		}

		public void close() {
			if (null != socket) {
				try {
					socket.close();
				} catch (IOException e) {
					LogUtil.getInstance().error("unable to write to [" + socket.getRemoteSocketAddress() + "]", e);
				}
			}
		}

	}

	public void unregister(RequestResponseWorker worker) {
		workers.remove(worker);
	}
 }
