package cool.scx.http.x.http1.request_line.request_target;

import dev.scx.http.uri.ScxURI;

public sealed interface RequestTarget permits AbsoluteForm, AsteriskForm, AuthorityForm, OriginForm {

    ScxURI toScxURI();

}
