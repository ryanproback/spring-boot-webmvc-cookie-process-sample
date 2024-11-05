package io.ryanproback.springboot3subdomaincookie;

import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

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

    private RestTemplate getRestClient() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        return builder.rootUri(serverHost + ":" + port).build();
    }


    @Test
    void testSetCookieUsingByResponseCookie() {
        var response = getRestClient().getForEntity("/set-cookie/response-cookie", String.class);
        testCookieSet(response, CookieController.COOKIE_DOMAIN);
    }

    @Test
    void testSetCookieUsingByHttpServletResponse() {
        var response = getRestClient().getForEntity("/set-cookie/http-servlet-response", String.class);

        testCookieSet(response, CookieController.COOKIE_DOMAIN);
    }

    @Test
    void testSetCookieDotUsingByResponseCookie() {
        var response = getRestClient().getForEntity("/set-cookie/response-cookie/dot", String.class);

        testCookieSet(response, CookieController.COOKIE_DOT_DOMAIN);
    }

    @Disabled
    @Test
    void testSetCookieDotUsingByHttpServletResponse() {
        try {
            getRestClient().getForEntity("/set-cookie/http-servlet-response/dot", String.class);

        } catch (HttpServerErrorException.InternalServerError ex) {
            // java.lang.IllegalArgumentException: An invalid domain [.kr-local-jainwon.com] was specified for this cookie
            // at org.apache.tomcat.util.http.Rfc6265CookieProcessor.validateDomain(Rfc6265CookieProcessor.java:191)
            return;
        }

        Assertions.fail("Should throw an exception");
    }

    @Test
    void testSetCookieDotUsingByHttpServletResponse27Legacy() {
        var response = getRestClient().getForEntity("/set-cookie/http-servlet-response/dot", String.class);
        testCookieSet(response, CookieController.COOKIE_DOT_DOMAIN);
    }

    @Test
    void testGetCookie() {
        var request = RequestEntity.get("/get-cookie")
                .header("Cookie", COOKIE_NAME + "=test-cookie")
                .build();
        var response = getRestClient().exchange(request, String.class);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // 쿠키가 요청에 포함만 되면 읽을 수 있는거임. 따라서 전달하고 말고는 브라우저에 의존함.
    @Test
    void testGetCookieDot() {
        var request = RequestEntity.get("/get-cookie/dot")
                .header("Cookie", COOKIE_DOT_NAME + "=test-cookie-dot")
                .build();
        var response = getRestClient().exchange(request, String.class);

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


    // 2.7 에서 사용하는 톰캣까지는 LegacyCookieProcessor 를 지원하기 때문에
    // 앞에 .(닷)을 붙인 쿠키도 문제가 없다.
    @TestConfiguration
    static class LegacyCookieProcessorConfiguration {
        @Bean
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
            return tomcatServletWebServerFactory -> tomcatServletWebServerFactory.addContextCustomizers(context -> context.setCookieProcessor(
                    new LegacyCookieProcessor()));
        }
    }
}
