package cool.scx.http.x.http1.request_line.request_target;

/// 例: `OPTIONS * HTTP/1.1`
///
/// 仅与 `OPTIONS` 方法一起使用, 针对整个服务器
public final class AsteriskForm implements RequestTarget {

    private static final AsteriskForm ASTERISK_FORM = new AsteriskForm();

    private AsteriskForm() {

    }

    public static AsteriskForm of() {
        return ASTERISK_FORM;
    }

}
