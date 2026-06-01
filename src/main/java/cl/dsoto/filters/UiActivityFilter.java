package cl.dsoto.filters;

import cl.dsoto.security.SessionPolicy;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@WebFilter(urlPatterns = "/*")
public class UiActivityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        if (response instanceof HttpServletResponse httpResponse && isPageRequest(httpRequest)) {
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
        }

        String requestType = httpRequest.getParameter("v-r");
        boolean heartbeat = "heartbeat".equals(requestType);
        boolean poll = false;
        HttpServletRequest requestToUse = httpRequest;

        if ("uidl".equals(requestType)) {
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
            requestToUse = cachedRequest;
            poll = cachedRequest.getCachedBodyAsString().contains("ui-poll");
        }

        chain.doFilter(requestToUse, response);

        if (!heartbeat && !poll && requestToUse.getUserPrincipal() != null) {
            SessionPolicy.touch(requestToUse.getSession(false));
        }
    }

    private boolean isPageRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String requestType = request.getParameter("v-r");
        String accept = request.getHeader("Accept");

        return requestType == null
                && (accept == null || accept.contains("text/html"))
                && !uri.contains("/VAADIN/")
                && !uri.contains("/images/")
                && !uri.contains("/q/")
                && !uri.contains("/api/");
    }

    private static final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        private CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            try (InputStream inputStream = request.getInputStream()) {
                this.cachedBody = inputStream.readAllBytes();
            }
        }

        private String getCachedBodyAsString() {
            if (cachedBody.length == 0) {
                return "";
            }
            Charset charset = getCharacterEncoding() == null
                    ? StandardCharsets.UTF_8
                    : Charset.forName(getCharacterEncoding());
            return new String(cachedBody, charset);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return inputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                    throw new UnsupportedOperationException("Async read listener is not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(),
                    getCharacterEncoding() == null ? StandardCharsets.UTF_8 : Charset.forName(getCharacterEncoding())));
        }

        @Override
        public int getContentLength() {
            return cachedBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return cachedBody.length;
        }
    }
}
