var httpStatusCodes;
httpStatusCodes = {
    100:{
        "name":"Continue",
        "detail":"This means that the server has received the request headers, and that the client should proceed to send the request body (in the case of a request for which a body needs to be sent; for example, a POST request). If the request body is large, sending it to a server when a request has already been rejected based upon inappropriate headers is inefficient. To have a server check if the request could be accepted based on the request's headers alone, a client must send Expect: 100-continue as a header in its initial request and check if a 100 Continue status code is received in response before continuing (or receive 417 Expectation Failed and not continue)."
    },
    101:{
        "name":"Switching Protocols",
        "detail":"This means the requester has asked the server to switch protocols and the server is acknowledging that it will do so."
    },
    102:{
        "name":"Processing (WebDAV) (RFC 2518)",
        "detail":"As a WebDAV request may contain many sub-requests involving file operations, it may take a long time to complete the request. This code indicates that the server has received and is processing the request, but no response is available yet. This prevents the client from timing out and assuming the request was lost."
    },
    103:{
        "name":"Checkpoint",
        "detail":"This code is used in the Resumable HTTP Requests Proposal to resume aborted PUT or POST requests."
    },
    122:{
        "name":"Request-URI too long",
        "detail":"This is a non-standard IE7-only code which means the URI is longer than a maximum of 2083 characters."
    },
    200:{
        "name":"OK",
        "detail":"Standard response for successful HTTP requests. The actual response will depend on the request method used. In a GET request, the response will contain an entity corresponding to the requested resource. In a POST request the response will contain an entity describing or containing the result of the action."
    },
    201:{
        "name":"Created",
        "detail":"The request has been fulfilled and resulted in a new resource being created."
    },
    202:{
        "name":"Accepted",
        "detail":"The request has been accepted for processing, but the processing has not been completed. The request might or might not eventually be acted upon, as it might be disallowed when processing actually takes place."
    },
    203:{
        "name":"Non-Authoritative Information (since HTTP/1.1)",
        "detail":"The server successfully processed the request, but is returning information that may be from another source."
    },
    204:{
        "name":"No Content",
        "detail":"The server successfully processed the request, but is not returning any content."
    },
    205:{
        "name":"Reset Content",
        "detail":"The server successfully processed the request, but is not returning any content. Unlike a 204 response, this response requires that the requester reset the document view."
    },
    206:{
        "name":"Partial Content",
        "detail":"The server is delivering only part of the resource due to a range header sent by the client. The range header is used by tools like wget to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams"
    },
    207:{
        "name":"Multi-Status (WebDAV) (RFC 4918)",
        "detail":"The message body that follows is an XML message and can contain a number of separate response codes, depending on how many sub-requests were made."
    },
    208:{
        "name":"Already Reported (WebDAV) (RFC 5842)",
        "detail":"The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again."
    },
    226:{
        "name":"IM Used (RFC 3229)",
        "detail":"The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance. "
    },
    300:{
        "name":"Multiple Choices",
        "detail":"Indicates multiple options for the resource that the client may follow. It, for instance, could be used to present different format options for video, list files with different extensions, or word sense disambiguation."
    },
    301:{
        "name":"Moved Permanently",
        "detail":"This and all future requests should be directed to the given URI."
    },
    302:{
        "name":"Found",
        "detail":"This is an example of industrial practice contradicting the standard. HTTP/1.0 specification (RFC 1945) required the client to perform a temporary redirect (the original describing phrase was \"Moved Temporarily\"), but popular browsers implemented 302 with the functionality of a 303. Therefore, HTTP/1.1 added status codes 303 and 307 to distinguish between the two behaviours. However, some Web applications and frameworks use the 302 status code as if it were the 303."
    },
    303:{
        "name":"See Other",
        "detail":"The response to the request can be found under another URI using a GET method. When received in response to a POST (or PUT/DELETE), it should be assumed that the server has received the data and the redirect should be issued with a separate GET message."
    },
    304:{
        "name":"Not Modified",
        "detail":"Indicates the resource has not been modified since last requested. Typically, the HTTP client provides a header like the If-Modified-Since header to provide a time against which to compare. Using this saves bandwidth and reprocessing on both the server and client, as only the header data must be sent and received in comparison to the entirety of the page being re-processed by the server, then sent again using more bandwidth of the server and client."
    },
    305:{
        "name":"Use Proxy (since HTTP/1.1)",
        "detail":"Many HTTP clients (such as Mozilla and Internet Explorer) do not correctly handle responses with this status code, primarily for security reasons."
    },
    306:{
        "name":"Switch Proxy",
        "detail":"No longer used. Originally meant \"Subsequent requests should use the specified proxy.\""
    },
    307:{
        "name":"Temporary Redirect (since HTTP/1.1)",
        "detail":"In this occasion, the request should be repeated with another URI, but future requests can still use the original URI. In contrast to 303, the request method should not be changed when reissuing the original request. For instance, a POST request must be repeated using another POST request."
    },
    308:{
        "name":"Resume Incomplete",
        "detail":"This code is used in the Resumable HTTP Requests Proposal to resume aborted PUT or POST requests."
    },
    400:{
        "name":"Bad Request",
        "detail":"The request cannot be fulfilled due to bad syntax."
    },
    401:{
        "name":"Unauthorized",
        "detail":"Similar to 403 Forbidden, but specifically for use when authentication is possible but has failed or not yet been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource."
    },
    402:{
        "name":"Payment Required",
        "detail":"Reserved for future use. The original intention was that this code might be used as part of some form of digital cash or micropayment scheme, but that has not happened, and this code is not usually used. As an example of its use, however, Apple's MobileMe service generates a 402 error (\"httpStatusCode:402\" in the Mac OS X Console log) if the MobileMe account is delinquent."
    },
    403:{
        "name":"Forbidden",
        "detail":"The request was a legal request, but the server is refusing to respond to it. Unlike a 401 Unauthorized response, authenticating will make no difference."
    },
    404:{
        "name":"Not Found",
        "detail":"The requested resource could not be found but may be available again in the future. Subsequent requests by the client are permissible."
    },
    405:{
        "name": "Method Not Allowed",
        "detail":"A request was made of a resource using a request method not supported by that resource; for example, using GET on a form which requires data to be presented via POST, or using PUT on a read-only resource."
    },
    406:{
        "name": "Not Acceptable",
        "detail":"The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request."
    },
    407:{
        "name":"Proxy Authentication Required",
        "detail":"The client must first authenticate itself with the proxy."
    },
    408:{
        "name":"Request Timeout",
        "detail":"The server timed out waiting for the request. According to W3 HTTP specifications: \"The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time.\""
    },
    409:{
        "name":"Conflict",
        "detail":"Indicates that the request could not be processed because of conflict in the request, such as an edit conflict."
    },
    410:{
        "name":"Gone",
        "detail":"Indicates that the resource requested is no longer available and will not be available again. This should be used when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code, the client should not request the resource again in the future. Clients such as search engines should remove the resource from their indices. Most use cases do not require clients and search engines to purge the resource, and a \"404 Not Found\" may be used instead."
    },
    411:{
        "name":"Length Required",
        "detail":"The request did not specify the length of its content, which is required by the requested resource."
    },
    412:{
        "name":"Precondition Failed",
        "detail":"The server does not meet one of the preconditions that the requester put on the request."
    },
    413:{
        "name":"Request Entity Too Large",
        "detail":"The request is larger than the server is willing or able to process."
    },
    414:{
        "name":"Request-URI Too Long",
        "detail":"The URI provided was too long for the server to process."
    },
    415:{
        "name":"Unsupported Media Type",
        "detail":"The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images use a different format."
    },
    416:{
        "name":"Requested Range Not Satisfiable",
        "detail":"The client has asked for a portion of the file, but the server cannot supply that portion. For example, if the client asked for a part of the file that lies beyond the end of the file."
    },
    417:{
        "name":"Expectation Failed",
        "detail":"The server cannot meet the requirements of the Expect request-header field."
    },
    418:{
        "name":"I'm a teapot (RFC 2324)",
        "detail":"This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP servers. However, known implementations do exist."
    },
    422:{
        "name":"Unprocessable Entity (WebDAV) (RFC 4918)",
        "detail":"The request was well-formed but was unable to be followed due to semantic errors."
    },
    423:{
        "name":"Locked (WebDAV) (RFC 4918)",
        "detail":"The resource that is being accessed is locked."
    },
    424:{
        "name":"Failed Dependency (WebDAV) (RFC 4918)",
        "detail":"The request failed due to failure of a previous request (e.g. a PROPPATCH)."
    },
    425:{
        "name":"Unordered Collection (RFC 3648)",
        "detail":"Defined in drafts of \"WebDAV Advanced Collections Protocol\",[14] but not present in \"Web Distributed Authoring and Versioning (WebDAV) Ordered Collections Protocol\".[15]"
    },
    426:{
        "name":"Upgrade Required (RFC 2817)",
        "detail":"The client should switch to a different protocol such as TLS/1.0."
    },
    428:{
        "name":"Precondition Required",
        "detail":"The origin server requires the request to be conditional. Intended to prevent \"the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict.\"[17] Proposed in an Internet-Draft."
    },
    429:{
        "name":"Too Many Requests",
        "detail":"The user has sent too many requests in a given amount of time. Intended for use with rate limiting schemes. Proposed in an Internet-Draft."
    },
    431:{
        "name":"Request Header Fields Too Large",
        "detail":"The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large. Proposed in an Internet-Draft."
    },
    444:{
        "name":"No Response",
        "detail":"An nginx HTTP server extension. The server returns no information to the client and closes the connection (useful as a deterrent for malware)."
    },
    449:{
        "name":"Retry With",
        "detail":"A Microsoft extension. The request should be retried after performing the appropriate action."
    },
    450:{
        "name":"Blocked by Windows Parental Controls",
        "detail":"A Microsoft extension. This error is given when Windows Parental Controls are turned on and are blocking access to the given webpage."
    },
    499:{
        "name":"Client Closed Request",
        "detail":"An Nginx HTTP server extension. This code is introduced to log the case when the connection is closed by client while HTTP server is processing its request, making server unable to send the HTTP header back."
    },
    500:{
        "name":"Internal Server Error",
        "detail":"A generic error message, given when no more specific message is suitable."
    },
    501:{
        "name":"Not Implemented",
        "detail":"The server either does not recognise the request method, or it lacks the ability to fulfill the request."
    },
    502:{
        "name":"Bad Gateway",
        "detail":"The server was acting as a gateway or proxy and received an invalid response from the upstream server."
    },
    503:{
        "name":"Service Unavailable",
        "detail":"The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state."
    },
    504:{
        "name":"Gateway Timeout",
        "detail":"The server was acting as a gateway or proxy and did not receive a timely response from the upstream server."
    },
    505:{
        "name":"HTTP Version Not Supported",
        "detail":"The server does not support the HTTP protocol version used in the request."
    },
    506:{
        "name":"Variant Also Negotiates (RFC 2295)",
        "detail":"Transparent content negotiation for the request results in a circular reference.[21]"
    },
    507:{
        "name":"Insufficient Storage (WebDAV) (RFC 4918)",
        "detail":"The server is unable to store the representation needed to complete the request."
    },
    508:{
        "name":"Loop Detected (WebDAV) (RFC 5842)",
        "detail":"The server detected an infinite loop while processing the request (sent in lieu of 208)."
    },
    509:{
        "name":"Bandwidth Limit Exceeded (Apache bw/limited extension)",
        "detail":"This status code, while used by many servers, is not specified in any RFCs."
    },
    510:{
        "name":"Not Extended (RFC 2774)",
        "detail":"Further extensions to the request are required for the server to fulfill it.[22]"
    },
    511:{
        "name":"Network Authentication Required",
        "detail":"The client needs to authenticate to gain network access. Intended for use by intercepting proxies used to control access to the network (e.g. \"captive portals\" used to require agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot). Proposed in an Internet-Draft."
    },
    598:{
        "name":"Network read timeout error",
        "detail":"This status code is not specified in any RFCs, but is used by some HTTP proxies to signal a network read timeout behind the proxy to a client in front of the proxy."
    },
    599:{
        "name":"Network connect timeout error[23]",
        "detail":"This status code is not specified in any RFCs, but is used by some HTTP proxies to signal a network connect timeout behind the proxy to a client in front of the proxy."
    }
};