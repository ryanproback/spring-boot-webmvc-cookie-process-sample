package io.ryanproback.springboot3subdomaincookie;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Objects;

import static io.ryanproback.springboot3subdomaincookie.CookieController.COOKIE_DOT_NAME;
import static io.ryanproback.springboot3subdomaincookie.CookieController.COOKIE_NAME;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBoot3SubdomainCookieApplicationTests {

    @LocalServerPort
    private int port;

    private static final String serverHost = "http://subdomain.kr-local-jainwon.com";

    @Test
    void contextLoads() {
    }

    private RestClient getRestClient() {
        return RestClient.builder()
                .baseUrl(serverHost + ":" + port)
                .build();
    }

    @Test
    void testSetCookieUsingByResponseCookie() {
        var response = getRestClient().get()
                        .uri("/set-cookie/response-cookie")
                        .retrieve()
                        .toEntity(String.class);


        testCookieSet(response, CookieController.COOKIE_DOMAIN);
    }

    @Test
    void testSetCookieUsingByHttpServletResponse() {
        var response = getRestClient().get()
                        .uri("/set-cookie/http-servlet-response")
                        .retrieve()
                        .toEntity(String.class);

        testCookieSet(response, CookieController.COOKIE_DOMAIN);
    }

    @Test
    void testSetCookieDotUsingByResponseCookie() {
        var response = getRestClient().get()
                        .uri("/set-cookie/response-cookie/dot")
                        .retrieve()
                        .toEntity(String.class);

        testCookieSet(response, CookieController.COOKIE_DOT_DOMAIN);
    }

    @Test
    void testSetCookieDotUsingByHttpServletResponse() {
        try {
            getRestClient().get()
                    .uri("/set-cookie/http-servlet-response/dot")
                    .retrieve()
                    .toEntity(String.class);

        } catch (HttpServerErrorException.InternalServerError ex) {
            // java.lang.IllegalArgumentException: An invalid domain [.kr-local-jainwon.com] was specified for this cookie
            // at org.apache.tomcat.util.http.Rfc6265CookieProcessor.validateDomain(Rfc6265CookieProcessor.java:191)
            return;
        }

        Assertions.fail("Should throw an exception");
    }

    @Test
    void testGetCookie() {
        var response = getRestClient().get()
                        .uri("/get-cookie")
                        .header("Cookie", COOKIE_NAME + "=cookie-value")
                        .retrieve()
                        .toEntity(String.class);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // 쿠키가 요청에 포함만 되면 읽을 수 있는거임. 따라서 전달하고 말고는 브라우저에 의존함.
    @Test
    void testGetCookieDot() {
        var response = getRestClient().get()
                        .uri("/get-cookie/dot")
                        .header("Cookie", COOKIE_DOT_NAME + "=cookie-value")
                        .retrieve()
                        .toEntity(String.class);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private static void testCookieSet(ResponseEntity<String> response, String domain) {
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
        Objects.requireNonNull(response.getHeaders().get("Set-Cookie")).forEach(cookie -> {
            Assertions.assertThat(cookie).contains("Domain=" + domain);
        });
    }

    // 결론.
    // 서브도메인 쿠키의 세팅과 요청에 포함해서 보내는 것은
    // 전적으로 브라우저 구현에 의존한다.
    // 크롬 기준으로 쿠키 도메인 앞에 .(닷)이 들어가지 않아도 잘 동작하게 되어 있음.
}
