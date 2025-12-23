package cool.scx.http.x.test.c;

import cool.scx.http.x.http1.request_line.Http1RequestLine;
import cool.scx.http.x.http1.request_line.InvalidRequestLineException;
import cool.scx.http.x.http1.request_line.InvalidRequestLineHttpVersionException;
import cool.scx.http.x.http1.request_line.request_target.AbsoluteForm;
import cool.scx.http.x.http1.request_line.request_target.AsteriskForm;
import cool.scx.http.x.http1.request_line.request_target.AuthorityForm;
import cool.scx.http.x.http1.request_line.request_target.OriginForm;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Http1RequestLineTest {

    public static void main(String[] args) throws Exception {
        testOriginForm();
        testAbsoluteForm();
        testAuthorityForm();
        testAsteriskForm();
        testCustomMethod();
        testInvalidSpaces();
        testInvalidRequestTarget();
        testInvalidVersion();
        testInvalidCombinations();
        test1();
        test2();
    }


    // --------- 合法请求 ---------
    @Test
    public static void testOriginForm() throws Exception {
        String[] requests = {
            "GET / HTTP/1.1",
            "GET /foo/bar HTTP/1.1",
            "GET /foo?bar=baz HTTP/1.1",
            "GET /foo?bar=baz#fragment HTTP/1.1",
            "OPTIONS /foo HTTP/1.1"
        };

        for (String req : requests) {
            Http1RequestLine line = Http1RequestLine.of(req);
            assertNotNull(line);
            assertTrue(line.requestTarget() instanceof OriginForm);
        }
    }

    @Test
    public static void testAbsoluteForm() throws Exception {
        String[] requests = {
            "GET http://example.com/index.html HTTP/1.1",
            "GET https://example.com:8443/path?query HTTP/1.1"
        };

        for (String req : requests) {
            Http1RequestLine line = Http1RequestLine.of(req);
            assertNotNull(line);
            assertTrue(line.requestTarget() instanceof AbsoluteForm);
        }
    }

    @Test
    public static void testAuthorityForm() throws Exception {
        String[] requests = {
            "CONNECT example.com:443 HTTP/1.1",
            "CONNECT [2001:db8::1]:443 HTTP/1.1"
        };

        for (String req : requests) {
            Http1RequestLine line = Http1RequestLine.of(req);
            assertNotNull(line);
            assertTrue(line.requestTarget() instanceof AuthorityForm);
        }
    }

    @Test
    public static void testAsteriskForm() throws Exception {
        String req = "OPTIONS * HTTP/1.1";
        Http1RequestLine line = Http1RequestLine.of(req);
        assertNotNull(line);
        assertTrue(line.requestTarget() instanceof AsteriskForm);
    }

    @Test
    public static void testCustomMethod() throws InvalidRequestLineException, InvalidRequestLineHttpVersionException {
        String[] requests = {
            "GETÁ /foo HTTP/1.1",
            "123 /foo HTTP/1.1",
        };
        // 我们允许这种方法
        for (String req : requests) {
            var http1RequestLine = Http1RequestLine.of(req);
        }
    }

    // --------- 非法请求 ---------
    @Test
    public static void testInvalidSpaces() {
        String[] requests = {
            "GET  /foo HTTP/1.1",
            "GET /foo  HTTP/1.1",
            " GET /foo HTTP/1.1",
            "GET /foo HTTP/1.1 ",
            "GET\t/foo HTTP/1.1"
        };

        for (String req : requests) {
            assertThrows(InvalidRequestLineException.class, () -> Http1RequestLine.of(req));
        }
    }

    @Test
    public static void testInvalidRequestTarget() {
        String[] requests = {
            "GET * HTTP/1.1",
            "GET example.com/index.html HTTP/1.1",
            "GET /foo%ZZ HTTP/1.1",
            "CONNECT example.com HTTP/1.1",
            "CONNECT example.com:99999 HTTP/1.1",
            "CONNECT example.com:abc HTTP/1.1",
            "CONNECT :443 HTTP/1.1",
            "CONNECT [2001:db8::1 HTTP/1.1"
        };

        for (String req : requests) {
            assertThrows(InvalidRequestLineException.class, () -> Http1RequestLine.of(req));
        }
    }

    @Test
    public static void testInvalidVersion() {
        String[] requests = {
            "GET /foo HTTP/1.0",
            "GET /foo HTTP/2.0",
            "GET /foo HTTP/1",
            "GET /foo HTTPS/1.1"
        };

        for (String req : requests) {
            assertThrows(InvalidRequestLineHttpVersionException.class, () -> Http1RequestLine.of(req));
        }
    }

    @Test
    public static void testInvalidCombinations() {
        String[] requests = {
            "POST * HTTP/1.1",
            "CONNECT example.com HTTP/1.0",
            "GET  http://example.com HTTP/1.0"
        };

        assertThrows(InvalidRequestLineException.class, () -> Http1RequestLine.of(requests[0]));
        assertThrows(InvalidRequestLineHttpVersionException.class, () -> Http1RequestLine.of(requests[1]));
        assertThrows(InvalidRequestLineException.class, () -> Http1RequestLine.of(requests[2]));
    }

    @Test
    public static void test1() throws InvalidRequestLineException, InvalidRequestLineHttpVersionException {

        //这是正确的
        var http1RequestLine = Http1RequestLine.of("GET /foo HTTP/1.1");

        //这里是非法http版本
        assertThrows(InvalidRequestLineHttpVersionException.class, () -> {
            var requestLine = Http1RequestLine.of("GET /foo HTTP/1.3");
        });


        //这里是 Http/0.9 理论上应该抛出 400
        assertThrows(InvalidRequestLineException.class, () -> {
            var requestLine = Http1RequestLine.of("GET /foo");
        });

        //这里是 多余空格 理论上应该抛出 400
        assertThrows(InvalidRequestLineException.class, () -> {
            var requestLine = Http1RequestLine.of("GET /foo abc HTTP/1.1");
        });

        //这里是 不可解析的路径 理论上应该抛出 400
        assertThrows(InvalidRequestLineException.class, () -> {
            var requestLine = Http1RequestLine.of("GET /% HTTP/1.1");
        });

    }

    @Test
    public static void test2() throws InvalidRequestLineException, InvalidRequestLineHttpVersionException {

        var s = Http1RequestLine.of("GET http://www.test.com/a/b/c/ HTTP/1.1");

        var c = Http1RequestLine.of("CONNECT www.test.com:443 HTTP/1.1");

        System.out.println(s.requestTarget());
        System.out.println(c.requestTarget());

    }

}
