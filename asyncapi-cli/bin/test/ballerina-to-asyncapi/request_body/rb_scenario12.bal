import ballerina/http;

listener http:Listener helloEp = new (9090);

type TagType record {
    int id;
    string[] category;
};

type Tag record {
    int id;
    string name;
    TagType[] tagType;
};

type Pet record {
    int id;
    string name;
};


# A fake mountain resort
@http:ServiceConfig { mediaTypeSubtypePrefix: "vnd.snowpeak.reservation", cors: { allowOrigins: ["*"] }}
service /payloadV on helloEp {
    resource function post pets(@http:Payload Pet payload) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function put pets(@http:Payload Tag payload) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
