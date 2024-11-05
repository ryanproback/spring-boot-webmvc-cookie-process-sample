package io.ryanproback.springboot3subdomaincookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class CookieController {
    public static final String COOKIE_DOMAIN = "kr-local-jainwon.com";
    public static final String COOKIE_NAME = "test-cookie";
    public static final String COOKIE_DOT_DOMAIN = ".kr-local-jainwon.com";
    public static final String COOKIE_DOT_NAME = "test-cookie-dot";

    @GetMapping("/set-cookie/response-cookie")
    ResponseEntity<String> setCookieUsingByResponseCookie() {
        var cookie = createResponseCookie(COOKIE_NAME, COOKIE_DOMAIN);

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("Cookie set by response cookie");
    }

    @GetMapping("/set-cookie/http-servlet-response")
    void setCookieUsingByHttpServletResponse(HttpServletResponse response) {
        var cookie = createCookie(COOKIE_NAME, COOKIE_DOMAIN);

        response.addCookie(cookie);
        response.setStatus(200);
    }

    @GetMapping("/set-cookie/response-cookie/dot")
    ResponseEntity<String> setCookieDotUsingByResponseCookie() {
        var cookie = createResponseCookie(COOKIE_DOT_NAME, COOKIE_DOT_DOMAIN);

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("Cookie set by response cookie");
    }

    @GetMapping("/set-cookie/http-servlet-response/dot")
    ResponseEntity<String> setCookieDotUsingByHttpServletResponse(HttpServletResponse response) {
        var cookie = createCookie(COOKIE_DOT_NAME, COOKIE_DOT_DOMAIN);

        response.addCookie(cookie);
        return ResponseEntity.ok("Cookie set by http servlet response");
    }

    @GetMapping("/get-cookie")
    ResponseEntity<String> getCookie(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return ResponseEntity.ok(cookie.getValue());
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/get-cookie/dot")
    ResponseEntity<String> getCookieDot(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (COOKIE_DOT_NAME.equals(cookie.getName())) {
                    return ResponseEntity.ok(cookie.getValue());
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    public static ResponseCookie createResponseCookie(String cookieName, String cookieDomain) {
        return ResponseCookie.from(cookieName, "test-cookie-value")
                .domain(cookieDomain)
                .path("/")
                .build();
    }

    private static Cookie createCookie(String cookieName, String cookieDomain) {
        var cookie = new Cookie(cookieName, "test-cookie-value");
        cookie.setDomain(cookieDomain);
        cookie.setPath("/");
        return cookie;
    }
}
