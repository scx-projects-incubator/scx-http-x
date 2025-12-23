package cool.scx.http.x.test;

import cool.scx.http.x.http1.request_line.Http1RequestLine;
import cool.scx.http.x.http1.request_line.InvalidRequestLineException;
import cool.scx.http.x.http1.request_line.InvalidRequestLineHttpVersionException;
import dev.scx.http.uri.ScxURI;
import org.testng.annotations.Test;

import static dev.scx.http.method.HttpMethod.GET;
import static org.testng.Assert.assertEquals;

public class Http1RequestLineEncodeTest {

    public static void main(String[] args) throws InvalidRequestLineException, InvalidRequestLineHttpVersionException {
        test1();
    }

    @Test
    public static void test1() {
        var http1RequestLine = new Http1RequestLine(GET, ScxURI.of().path("/中文/bar").addQuery("aaa", "bbb")).encode();
        assertEquals(http1RequestLine, "GET /%E4%B8%AD%E6%96%87/bar?aaa=bbb HTTP/1.1");
    }

}
