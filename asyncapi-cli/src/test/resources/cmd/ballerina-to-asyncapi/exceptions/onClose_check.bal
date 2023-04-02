import ballerina/websocket;
import ballerina/io;


listener websocket:Listener helloEp = new (80);

public type Subscribe record{
    int id;
    string event;
};

public type Ticker record{
    int id;
};
@websocket:ServiceConfig{dispatcherKey: "event"}
service / on helloEp {
    resource function get .() returns websocket:Service| websocket:UpgradeError {
        return new ChatServer();
    }
}

service class ChatServer{
    *websocket:Service;

    remote function onSubscribe(websocket:Caller caller,Subscribe message) returns Ticker{
        return {id:1};
    }
    remote function onClose(websocket:Caller caller, int statusCode, string reason) {

        io:println(string `Client closed connection with ${statusCode} because of ${reason}`);
    }

}
