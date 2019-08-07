package au.id.deejay.webserver.server;

import au.id.deejay.webserver.response.ResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The executor maintains a thread pool for worker threads servicing individual client connections.
 *
 * @author David Jessup
 */
public class WebServerExecutor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(WebServerExecutor.class);
	private final ResponseFactory responseFactory;
	private int port;
	private int timeout;
	private int maxThreads;
	private ExecutorService threadPool;
	private boolean running;
        private static final int maxRep = 2;

	/**
	 * Creates a new {@link WebServerExecutor}.
	 *
	 * @param port            the port the executor will listen for client connections on
	 * @param timeout         the timeout in seconds for client connections
	 * @param maxThreads      the maximum number of worker threads to use for handling requests
	 * @param responseFactory the response factory to use to generate {@link au.id.deejay.webserver.api.Response}s for
	 *                        incoming {@link au.id.deejay.webserver.api.Request}s
	 */
	public WebServerExecutor(int port, int timeout, int maxThreads, ResponseFactory responseFactory) {
		this.port = port;
		this.timeout = timeout;
		this.maxThreads = maxThreads;
		this.responseFactory = responseFactory;

		running = false;
	}

	@Override
	public void run() {
		running = true;

	//	threadPool = Executors.newFixedThreadPool(maxThreads);

	//	try (ServerSocket serverSocket = new ServerSocket()) {

			// Only block for 1 second so the running loop can escape if the server is stopped.
		//	serverSocket.setSoTimeout(1000);

		//	LOG.info("Server listening on port {}", serverSocket.getLocalPort());

                        int rep = 0;
			while (running() && rep < maxRep) {
                                try {
				        Thread thread = new Thread(){
					    public void run(){
				              try {
						  handleConnection();
				              } catch (Exception e) {
				                        System.out.println("err1"); 
						}
					    }
					};

					thread.start();

				} catch (Exception e) {
					System.out.println("err2"); 
				}
				
                                rep++;
			}

		//} catch (IOException e) {
		//	LOG.warn("Error listening for client connection.", e);
		//}
	}

	/**
	 * Checks if the executor is running.
	 *
	 * @return Returns true if the executor is running.
	 */
	public synchronized boolean running() {
		return running;
	}

	/**
	 * Stops the executor.
	 */
	public synchronized void stop() {
		running = false;

	//	LOG.info("Shutting down web server executor.");
/*
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error("Worker thread pool was interrupted while shutting down. Some client connections may have been terminated prematurely.", e);
			Thread.currentThread().interrupt();
		}
*/
	//	LOG.info("Web server executor has shutdown.");
	}

	@SuppressWarnings("squid:S1166") // Ignore the suppressed SocketTimeoutException
	private void handleConnection() throws IOException {
		try {
			// Wait for a client connection
			Socket client = new Socket();//serverSocket.accept();

			// Assign the connection to a worker
			WebWorker worker = assignWorker(client);

			// Queue the worker for execution
			//threadPool.execute(worker);

                        worker.handleConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private WebWorker assignWorker(Socket client) {
		try {
			//client.setSoTimeout(timeout * 1000);
		} catch (Exception e) {
			LOG.warn("Unable to set socket timeout", e);
		}
		return new WebWorker(client, responseFactory);
	}

}
