package io.github.jukomu.jmcomic.core.net.interceptor;

import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryAndDomainRedirectInterceptorTest {

    private static final String TRANSIENT_MYSQL_RESPONSE =
            "Could not connect to mysql! Please check your database settings! conn2";
    private static final String SUCCESS_RESPONSE = "{\"code\":200}";

    @Test
    void retriesTransientMysqlResponseOnceUsingAnotherDomain() throws IOException {
        JmDomainManager domainManager = readyDomainManager("first.test", "second.test");
        RetryAndDomainRedirectInterceptor interceptor =
                new RetryAndDomainRedirectInterceptor(0, domainManager);
        SequencedChain chain = new SequencedChain(TRANSIENT_MYSQL_RESPONSE, SUCCESS_RESPONSE);

        try (Response response = interceptor.intercept(chain)) {
            assertEquals(SUCCESS_RESPONSE, response.body().string());
        }

        assertEquals(List.of("first.test", "second.test"), chain.requestHosts());
        assertEquals(1, domainManager.getDomainStates().get("first.test"));
        assertEquals(0, domainManager.getDomainStates().get("second.test"));
    }

    @Test
    void returnsSecondTransientMysqlResponseWithoutRetryingAgain() throws IOException {
        JmDomainManager domainManager = readyDomainManager("first.test", "second.test");
        RetryAndDomainRedirectInterceptor interceptor =
                new RetryAndDomainRedirectInterceptor(0, domainManager);
        SequencedChain chain = new SequencedChain(TRANSIENT_MYSQL_RESPONSE, TRANSIENT_MYSQL_RESPONSE);

        try (Response response = interceptor.intercept(chain)) {
            assertEquals(TRANSIENT_MYSQL_RESPONSE, response.body().string());
        }

        assertEquals(List.of("first.test", "second.test"), chain.requestHosts());
        assertEquals(1, domainManager.getDomainStates().get("first.test"));
        assertEquals(1, domainManager.getDomainStates().get("second.test"));
    }

    private JmDomainManager readyDomainManager(String... domains) {
        JmDomainManager domainManager = new JmDomainManager(Arrays.asList(domains));
        domainManager.setInitialized(true);
        return domainManager;
    }

    private static final class SequencedChain implements Interceptor.Chain {

        private static final MediaType TEXT_PLAIN = MediaType.get("text/plain; charset=utf-8");

        private final Request originalRequest = new Request.Builder()
                .url("https://" + JmConstants.PLACEHOLDER_HOST + "/setting")
                .build();
        private final Call call = new OkHttpClient().newCall(originalRequest);
        private final List<String> responseBodies;
        private final List<String> requestHosts = new ArrayList<>();
        private int responseIndex;

        private SequencedChain(String... responseBodies) {
            this.responseBodies = Arrays.asList(responseBodies);
        }

        private List<String> requestHosts() {
            return requestHosts;
        }

        @NotNull
        @Override
        public Request request() {
            return originalRequest;
        }

        @NotNull
        @Override
        public Response proceed(@NotNull Request request) {
            requestHosts.add(request.url().host());
            String responseBody = responseBodies.get(responseIndex++);
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(responseBody, TEXT_PLAIN))
                    .build();
        }

        @Override
        public Connection connection() {
            return null;
        }

        @NotNull
        @Override
        public Call call() {
            return call;
        }

        @Override
        public int connectTimeoutMillis() {
            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, @NotNull TimeUnit unit) {
            return this;
        }

        @Override
        public int readTimeoutMillis() {
            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withReadTimeout(int timeout, @NotNull TimeUnit unit) {
            return this;
        }

        @Override
        public int writeTimeoutMillis() {
            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, @NotNull TimeUnit unit) {
            return this;
        }
    }
}
