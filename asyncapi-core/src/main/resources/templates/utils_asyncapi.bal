import ballerina/url;
import nuvindu/pipe;

# Stream generator
isolated class StreamGenerator{
    private final pipe:Pipe pipe;
    private final decimal timeout;

    # Description
    #
    # + pipe - Pipe to hold stream messages
    # + timeout - Waiting time
    isolated function init(pipe:Pipe pipe, decimal timeout) returns error? {
        self.pipe = pipe;
        self.timeout = timeout;
    }


    # Next method to return next stream message
    public isolated function next() returns record {|NextMessage|CompleteMessage|ErrorMessage value;|}|error?{
        anydata responseMessage = check self.pipe.consume(self.timeout);
        NextMessage|CompleteMessage|ErrorMessage response = check responseMessage.cloneWithType();
        return {value: response};
    }
}

type SimpleBasicType string|boolean|int|float|decimal;

# Get Encoded URI for a given value.
#
# + value - Value to be encoded
# + return - Encoded string
isolated function getEncodedUri(anydata value) returns string {
    string|error encoded = url:encode(value.toString(), "UTF8");
    if (encoded is string) {
        return encoded;
    } else {
        return value.toString();
    }
}

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + return - Returns generated Path or error at failure of client initialization
isolated function getPathForQueryParam(map<anydata> queryParam) returns string|error {
    string[] param = [];
    if (queryParam.length() > 0) {
        param.push("?");
        foreach var [key, value] in queryParam.entries() {
            if value is () {
                _ = queryParam.remove(key);
                continue;
            }
            if (value is SimpleBasicType) {
                param.push(key, "=", getEncodedUri(value.toString()));

            } else {
                param.push(key, "=", value.toString());
            }
            param.push("&");
        }
        _ = param.pop();
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}


# Combine custom headers and param headers
#
# + customHeaders - Custom headers map
# + paramHeaders - Headers generated using spec
# + return - Return combine custom and spec generated headers
isolated function getCombineHeaders(map<string> customHeaders, map<string> paramHeaders) returns map<string> {
    foreach [string, string] [k, v] in paramHeaders.entries() {
        customHeaders[k] = v;
    }
    return customHeaders;

}

