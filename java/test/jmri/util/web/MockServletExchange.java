package jmri.util.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.io.*;

import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jmri.web.servlet.ServletUtil;

/**
 * Test utility that simulates a servlet request/response exchange using
 * Mockito-backed {@link HttpServletRequest} and {@link HttpServletResponse}.
 *
 * <p>Designed for unit testing servlet logic without a servlet container.
 * Captures response status, headers, redirects, content type, and body.</p>
 * <p>This is not a full servlet container simulation; only commonly used
 * servlet behaviours are implemented.</p>
 * <p>Spring Framework moved from javax.servlet to jakarta.servlet in version 6,
 * hence unable to use their Mock test classes from 5.3.39.</p>
 */
public class MockServletExchange {

    public static final String DELETE = "DELETE";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpSession session = mock(HttpSession.class);
    private final ServletConfig config = mock(ServletConfig.class);
    private final ServletContext context = mock(ServletContext.class);

    private final ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
    private final PrintWriter responseWriter =
        new PrintWriter(new OutputStreamWriter(responseOutputStream, StandardCharsets.UTF_8), true);

    private final Map<String, List<String>> headers = new HashMap<>();
    private final Map<String, String[]> parameterMap = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();

    private int status = HttpServletResponse.SC_OK;
    private String responseContentType;
    private String redirectedUrl;

    /**
     * Creates a new mocked servlet exchange.
     *
     * @param method     the HTTP method (e.g. "GET", "POST").
     * @param requestUri the request URI.
     */
    public MockServletExchange(String method, String requestUri) {

        when(config.getServletContext()).thenReturn(context);

        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getLocale()).thenReturn(Locale.ENGLISH);
        when(request.getSession()).thenReturn(session);
        when(request.getCharacterEncoding()).thenReturn(ServletUtil.UTF8);
        when(request.getParameterMap())
                .thenAnswer(inv -> Collections.unmodifiableMap(parameterMap));
        when(request.getParameter(anyString())).thenAnswer(inv -> {
            String[] values = parameterMap.get(inv.getArgument(0, String.class));
            return (values != null && values.length > 0) ? values[0] : null;
        });
        when(request.getAttribute(anyString()))
                .thenAnswer(inv -> attributes.get(inv.getArgument(0, String.class)));

        assertDoesNotThrow( () ->
            when(response.getWriter()).thenReturn(responseWriter));
        assertDoesNotThrow( () ->
            when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
                @Override
                public void write(int b) {
                    responseOutputStream.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(javax.servlet.WriteListener l) {
                }
        }));

        // Redirect capture
        assertDoesNotThrow( () ->
            doAnswer(inv -> {
                String url = inv.getArgument(0, String.class);
                if (url == null) {
                    throw new IllegalArgumentException("Redirect URL must not be null");
                }
                this.redirectedUrl = url;
                this.status = HttpServletResponse.SC_MOVED_TEMPORARILY;
                return null;
            }).when(response).sendRedirect(any())); // any passes null instances through

        // case-insensitive check
        doAnswer(inv -> {
            String name = inv.getArgument(0, String.class);
            String value = inv.getArgument(1, String.class);
            // Standardise key to lowercase for storage
            headers.put(name.toLowerCase(), new ArrayList<>(List.of(value)));
            return null;
        }).when(response).setHeader(anyString(), anyString());

        // Fix getHeader to return from the lowercase map
        when(response.getHeader(anyString())).thenAnswer(inv -> {
            String name = inv.getArgument(0, String.class);
            List<String> values = headers.get(name.toLowerCase());
            return (values != null && !values.isEmpty()) ? values.get(0) : null;
        });

        // addHeader appends
        doAnswer(inv -> {
            String name = inv.getArgument(0, String.class);
            String value = inv.getArgument(1, String.class);
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return null;
        }).when(response).addHeader(anyString(), anyString());

        doAnswer(inv -> {
            String name = inv.getArgument(0, String.class);
            long timestamp = inv.getArgument(1, Long.class);
            // Use lowercase keys to remain consistent with your setHeader mock
            headers.put(name.toLowerCase(), new ArrayList<>(List.of(getRfc7232formatHttpDate(timestamp))));
            return null;
        }).when(response).setDateHeader(anyString(), anyLong());

        // addDateHeader appends
        doAnswer(inv -> {
            String name = inv.getArgument(0, String.class);
            long timestamp = inv.getArgument(1, Long.class);
            headers.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>())
                   .add(getRfc7232formatHttpDate(timestamp));
            return null;
        }).when(response).addDateHeader(anyString(), anyLong());

        when(response.getHeaderNames())
                .thenAnswer(inv -> Collections.unmodifiableSet(headers.keySet()));

        when(response.getHeaders(anyString()))
                .thenAnswer(inv -> {
                    List<String> values = headers.get(inv.getArgument(0, String.class));
                    return values == null
                            ? Collections.emptyList()
                            : Collections.unmodifiableList(values);
                });

        doAnswer(inv -> {
            this.status = inv.getArgument(0, Integer.class);
            return null;
        }).when(response).setStatus(anyInt());

        doAnswer(inv -> {
            this.responseContentType = inv.getArgument(0, String.class);
            return null;
        }).when(response).setContentType(anyString());

        doAnswer(inv -> {
            this.attributes.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());

        withBody(""); // default empty
    }


    public final MockServletExchange withBody(String body) {
        assertDoesNotThrow( () ->
            when(request.getReader())
                .thenReturn(new BufferedReader(
                    new StringReader(body != null ? body : ""))));
        return this;
    }

    public MockServletExchange withParameter(String key, String value) {
        parameterMap.put(key, new String[]{value});
        return this;
    }

    public MockServletExchange withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public MockServletExchange withRequestContentType(String type) {
        when(request.getContentType()).thenReturn(type);
        return this;
    }

    public MockServletExchange withContextPath(String path) {
        when(request.getContextPath()).thenReturn(path);
        return this;
    }

    public MockServletExchange withPathInfo(String info) {
        when(request.getPathInfo()).thenReturn(info);
        return this;
    }


    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public HttpSession getSession() {
        return session;
    }

    public ServletConfig getConfig() {
        return config;
    }

    public int getResponseStatus() {
        return status;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    public String getResponseContentAsString() {
        responseWriter.flush();
        return responseOutputStream.toString(StandardCharsets.UTF_8);
    }

    public static String getRfc7232formatHttpDate(long timestamp) {
        return DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(timestamp));
    }

}
