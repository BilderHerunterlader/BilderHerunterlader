package ch.supertomcat.bh.downloader;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

/**
 * Extension of @link {@link CloseableHttpClient} to override the close method, to prevent the actual client from closing when this instance is used in
 * try-with-resource code
 */
public final class SharedHttpClient extends CloseableHttpClient {
	/**
	 * Delegate
	 */
	private final CloseableHttpClient delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate Delegate
	 */
	public SharedHttpClient(CloseableHttpClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public void close(CloseMode closeMode) {
		// Do not close shared client
	}

	@Override
	public void close() throws IOException {
		// Do not close shared client
	}

	@Override
	public ClassicHttpResponse executeOpen(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
		return delegate.executeOpen(target, request, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CloseableHttpResponse execute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
		return delegate.execute(target, request, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CloseableHttpResponse execute(ClassicHttpRequest request, HttpContext context) throws IOException {
		return delegate.execute(request, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CloseableHttpResponse execute(ClassicHttpRequest request) throws IOException {
		return delegate.execute(request);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CloseableHttpResponse execute(HttpHost target, ClassicHttpRequest request) throws IOException {
		return delegate.execute(target, request);
	}

	@Override
	public <T> T execute(ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
		return delegate.execute(request, responseHandler);
	}

	@Override
	public <T> T execute(ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
		return delegate.execute(request, context, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
		return delegate.execute(target, request, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
		return delegate.execute(target, request, context, responseHandler);
	}

	@Override
	protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
		// Should never be called
		throw new UnsupportedOperationException("This method of SharedHttpClient should never be called");
	}
}
