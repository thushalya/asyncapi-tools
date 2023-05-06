/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.asyncapi.core.generators.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.apicurio.datamodels.models.ServerVariable;
import io.apicurio.datamodels.models.asyncapi.AsyncApiMessage;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.v25.*;
import io.ballerina.asyncapi.core.exception.BallerinaAsyncApiException;
import io.ballerina.asyncapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.asyncapi.core.GeneratorConstants;
import io.ballerina.asyncapi.core.GeneratorUtils;
import io.ballerina.asyncapi.core.generators.client.model.AASClientConfig;
import io.ballerina.asyncapi.core.generators.document.DocCommentsGenerator;
//import io.ballerina.asyncapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;


import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.asyncapi.core.GeneratorConstants.*;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;

/**
 * This class is used to generate ballerina client file according to given yaml file.
 *
 * @since 1.3.0
 */
public class BallerinaClientGenerator {

//    private final Filter filters;
    private List<ImportDeclarationNode> imports;
    private List<TypeDefinitionNode> typeDefinitionNodeList;
    private List<String> apiKeyNameList = new ArrayList<>();
    private final AsyncApi25DocumentImpl asyncAPI;
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final List<String> remoteFunctionNameList;
    private String serverURL;
    private final BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator;
    private final boolean resourceMode;

    /**
     * Returns a list of type definition nodes.
     */
    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {

        return typeDefinitionNodeList;
    }

    /**
     * Returns ballerinaAuthConfigGenerator.
     */
    public BallerinaAuthConfigGenerator getBallerinaAuthConfigGenerator() {

        return ballerinaAuthConfigGenerator;
    }

    /**
     * Set the typeDefinitionNodeList.
     */
    public void setTypeDefinitionNodeList(
            List<TypeDefinitionNode> typeDefinitionNodeList) {

        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }

    public List<String> getRemoteFunctionNameList() {

        return remoteFunctionNameList;
    }

    /**
     * Returns server URL.
     *
     * @return {@link String}
     */
    public String getServerUrl() {

        return serverURL;
    }

    public BallerinaClientGenerator(AASClientConfig AASClientConfig) {

//        this.filters = oasClientConfig.getFilters();
        this.imports = new ArrayList<>();
        this.typeDefinitionNodeList = new ArrayList<>();
        this.asyncAPI = AASClientConfig.getOpenAPI();
        this.ballerinaSchemaGenerator = new BallerinaTypesGenerator(asyncAPI,
                AASClientConfig.isNullable(), new LinkedList<>());
        this.ballerinaUtilGenerator = new BallerinaUtilGenerator();
        this.remoteFunctionNameList = new ArrayList<>();
        this.serverURL = "/";
        this.ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(false, false , asyncAPI,
                ballerinaUtilGenerator);
        this.resourceMode = AASClientConfig.isResourceMode();
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return return Syntax tree for the ballerina code.
     * @throws BallerinaAsyncApiException When function fail in process.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaAsyncApiException {

        // Create `ballerina/websocket` import declaration node
        ImportDeclarationNode importForWebsocket = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , WEBSOCKET);

        imports.add(importForWebsocket);
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        // Add authentication related records
        ballerinaAuthConfigGenerator.addAuthRelatedRecords(asyncAPI);

        // Add class definition node to module member nodes
        nodes.add(getClassDefinitionNode());

        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(nodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

//    public BallerinaUtilGenerator getBallerinaUtilGenerator() {
//
//        return ballerinaUtilGenerator;
//    }

    /**
     * Generate Class definition Node with below code structure.
     * <pre>
     *     public isolated client class Client {
     *     final websocket:Client clientEp;
     *     public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "/url")
     *     returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *     }
     *     // Remote functions
     *     remote isolated function pathParameter(int 'version, string name) returns string|error {
     *         string  path = string `/v1/${'version}/v2/${name}`;
     *         string response = check self.clientEp-> get(path);
     *         return response;
     *     }
     * }
     * </pre>
     */
    private ClassDefinitionNode getClassDefinitionNode() throws BallerinaAsyncApiException {
        // Collect members for class definition node
        List<Node> memberNodeList = new ArrayList<>();
        // Add instance variable to class definition node
        memberNodeList.addAll(createClassInstanceVariables());
        // Add init function to class definition node
        memberNodeList.add(createInitFunction());
        // Generate remote function Nodes
        memberNodeList.addAll(createRemoteFunctions(asyncAPI.getComponents().getMessages()));
        // Generate the class combining members
        MetadataNode metadataNode = getClassMetadataNode();

        String stringClassName= asyncAPI.getInfo().getTitle().trim()+ GeneratorUtils.
                removeNonAlphanumeric(asyncAPI.getChannels().getItemNames().get(0).trim())+"Client";
        IdentifierToken className= createIdentifierToken(stringClassName);
//        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generate metadata node of the class including documentation. Content of the documentation
     * will be taken from the `description` section inside the `info` section in AsyncAPI definition.
     *
     * @return {@link MetadataNode}    Metadata node of the client class
     */
    private MetadataNode getClassMetadataNode() {

        List<AnnotationNode> classLevelAnnotationNodes = new ArrayList<>();
//        if (((AsyncApi25InfoImpl)asyncAPI.getInfo()).getExtensions() != null) {
////            Map<String, JsonNode> extensions =((AsyncApi25InfoImpl)asyncAPI.getInfo()).getExtensions();
//            DocCommentsGenerator.extractDisplayAnnotation(extensions, classLevelAnnotationNodes);
//        }
        // Generate api doc
        List<Node> documentationLines = new ArrayList<>();
        if (asyncAPI.getInfo().getDescription() != null && !asyncAPI.getInfo().getDescription().isBlank()) {
            documentationLines.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    asyncAPI.getInfo().getDescription(), false));
        }
        MarkdownDocumentationNode apiDoc = createMarkdownDocumentationNode(createNodeList(documentationLines));
        return createMetadataNode(apiDoc, createNodeList(classLevelAnnotationNodes));
    }

    /**
     * Create client class init function
     * -- Scenario 1: init function when authentication mechanism is API key
     * <pre>
     *   public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl,
     *     ConnectionConfig config =  {}) returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *         self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
     *   }
     * </pre>
     * -- Scenario 2: init function when authentication mechanism is OAuth2.0
     * <pre>
     *   public isolated function init(ConnectionConfig clientConfig, string serviceUrl = "base-url") returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *   }
     * </pre>
     * -- Scenario 3: init function when no authentication mechanism is provided
     * <pre>
     *   public isolated function init(ConnectionConfig config =  {},
     *      string serviceUrl = "base-url") returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *   }
     * </pre>
     *
     * @return {@link FunctionDefinitionNode}   Class init function
     * @throws BallerinaAsyncApiException When invalid server URL is provided
     */
    private FunctionDefinitionNode createInitFunction() throws BallerinaAsyncApiException {
        ArrayList initMetaDataDoc=new ArrayList();
        FunctionSignatureNode functionSignatureNode = getInitFunctionSignatureNode(initMetaDataDoc);
        FunctionBodyNode functionBodyNode = getInitFunctionBodyNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        return createFunctionDefinitionNode(null, getInitDocComment(initMetaDataDoc), qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Create function body node of client init function.
     *
     * @return {@link FunctionBodyNode}
     */
    private FunctionBodyNode getInitFunctionBodyNode() {

        List<StatementNode> assignmentNodes = new ArrayList<>();

//        assignmentNodes.add(ballerinaAuthConfigGenerator.getWebsocketClientConfigVariableNode());
//        assignmentNodes.add(ballerinaAuthConfigGenerator.getClientConfigDoStatementNode());

        // If both apiKey and httpOrOAuth is supported
        // todo : After revamping
        if (ballerinaAuthConfigGenerator.isHttpApiKey() && ballerinaAuthConfigGenerator.isHttpOROAuth()) {
            assignmentNodes.add(ballerinaAuthConfigGenerator.handleInitForMixOfApiKeyAndHTTPOrOAuth());
        }
        // create initialization statement of websocket:Client class instance
        assignmentNodes.add(ballerinaAuthConfigGenerator.getClientInitializationNode());
        // create {@code self.clientEp = httpEp;} assignment node
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("clientEp")));
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken("websocketEp"));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(httpClientAssignmentStatementNode);


        // Get API key assignment node if authentication mechanism type is only `apiKey`
        if (ballerinaAuthConfigGenerator.isHttpApiKey() && !ballerinaAuthConfigGenerator.isHttpOROAuth()) {
            assignmentNodes.add(ballerinaAuthConfigGenerator.getApiKeyAssignmentNode());
        }
        if (ballerinaAuthConfigGenerator.isHttpApiKey()) {
            List<String> apiKeyNames = new ArrayList<>();
            apiKeyNames.addAll(ballerinaAuthConfigGenerator.getHeaderApiKeyNameList().values());
            apiKeyNames.addAll(ballerinaAuthConfigGenerator.getQueryApiKeyNameList().values());
            setApiKeyNameList(apiKeyNames);
        }
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Create function signature node of client init function.
     *
     * @return {@link FunctionSignatureNode}
     * @throws BallerinaAsyncApiException When invalid server URL is provided
     */
    private FunctionSignatureNode getInitFunctionSignatureNode(ArrayList initMetaDataNode) throws BallerinaAsyncApiException {

        serverURL = getServerURL((AsyncApi25ServersImpl) asyncAPI.getServers());
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                ballerinaAuthConfigGenerator.getConfigParamForClassInit(serverURL,initMetaDataNode));
        OptionalTypeDescriptorNode returnType = createOptionalTypeDescriptorNode(createToken(ERROR_KEYWORD),
                createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);
        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    /**
     * Provide client class init function's documentation including function description and parameter descriptions.
     *
     * @return {@link MetadataNode}    Metadata node containing entire function documentation comment.
     */
    private MetadataNode getInitDocComment(ArrayList docs) {

//        List<Node> docs = new ArrayList<>();
        String clientInitDocComment = "Gets invoked to initialize the `connector`.\n";
        Map<String, JsonNode> extensions = ((AsyncApi25InfoImpl)asyncAPI.getInfo()).getExtensions();
        if (extensions != null && !extensions.isEmpty()) {
            for (Map.Entry<String,JsonNode> extension : extensions.entrySet()) {
                if (extension.getKey().trim().equals(X_BALLERINA_INIT_DESCRIPTION)) {
                    clientInitDocComment = clientInitDocComment.concat(extension.getValue().toString());
                    break;
                }
            }
        }
        //todo: setInitDocComment() pass the references
        docs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(clientInitDocComment, true));
        if (ballerinaAuthConfigGenerator.isHttpApiKey() && !ballerinaAuthConfigGenerator.isHttpOROAuth()) {
            MarkdownParameterDocumentationLineNode apiKeyConfig = DocCommentsGenerator.createAPIParamDoc(
                    "apiKeyConfig", DEFAULT_API_KEY_DESC);
            docs.add(apiKeyConfig);
        }
        // Create method description
        MarkdownParameterDocumentationLineNode clientConfig = DocCommentsGenerator.createAPIParamDoc("config",
                "The configurations to be used when initializing the `connector`");
        docs.add(clientConfig);
        MarkdownParameterDocumentationLineNode serviceUrlAPI = DocCommentsGenerator.createAPIParamDoc("serviceUrl",
                "URL of the target service");
        docs.add(serviceUrlAPI);
        MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGenerator.createAPIParamDoc("return",
                "An error if connector initialization failed");
        docs.add(returnDoc);
        MarkdownDocumentationNode clientInitDoc = createMarkdownDocumentationNode(createNodeList(docs));
        return createMetadataNode(clientInitDoc, createEmptyNodeList());
    }

    /**
     * Generate client class instance variables.
     *
     * @return {@link List<ObjectFieldNode>}    List of instance variables
     */
    private List<ObjectFieldNode> createClassInstanceVariables() {

        //final websocket:Client clientEp;
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);
        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(WEBSOCKET),
                createToken(COLON_TOKEN), createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.CLIENT_EP);
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        ObjectFieldNode websocketClientField = createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
        fieldNodeList.add(websocketClientField);
        // add apiKey instance variable when API key security schema is given
        ObjectFieldNode apiKeyFieldNode = ballerinaAuthConfigGenerator.getApiKeyMapClassVariable();
        if (apiKeyFieldNode != null) {
            fieldNodeList.add(apiKeyFieldNode);
        }
        return fieldNodeList;
    }

    /**
     * Generate remote functions for OpenAPI operations.
     *
//     * @param paths  openAPI Paths
//     * @param filter user given tags and operations
     * @return FunctionDefinitionNodes list
     * @throws BallerinaAsyncApiException - throws when creating remote functions fails
     */
    private List<FunctionDefinitionNode> createRemoteFunctions(Map<String, AsyncApiMessage> messages)
            throws BallerinaAsyncApiException {

//        List<String> filterTags = filter.getTags();
//        List<String> filterOperations = filter.getOperations();
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
//        Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
        Set<Map.Entry<String, AsyncApiMessage>> messageItems = messages.entrySet();
        for (Map.Entry<String, AsyncApiMessage> messageItem: messageItems){
            Map<String, JsonNode> extensions=((AsyncApi25MessageImpl)messageItem.getValue()).getExtensions();
            if(extensions!=null && extensions.get(X_RESPONSE)!=null ){
//                JsonNode ref=extensions.get(X_RESPONSE).get(PAYLOAD);
                FunctionDefinitionNode functionDefinitionNode =
                        getClientMethodFunctionDefinitionNode( messageItem,extensions);
                functionDefinitionNodeList.add(functionDefinitionNode);
//


            }

        }
//        for (Map.Entry<String, PathItem> path : pathsItems) {
//            if (!path.getValue().readOperationsMap().isEmpty()) {
//                for (Map.Entry<PathItem.HttpMethod, Operation> operation :
//                        path.getValue().readOperationsMap().entrySet()) {
//                    // create display annotation of the operation
//                    List<AnnotationNode> functionLevelAnnotationNodes = new ArrayList<>();
//                    if (operation.getValue().getExtensions() != null) {
//                        Map<String, Object> extensions = operation.getValue().getExtensions();
//                        DocCommentsGenerator.extractDisplayAnnotation(extensions, functionLevelAnnotationNodes);
//                    }
//                    List<String> operationTags = operation.getValue().getTags();
//                    String operationId = operation.getValue().getOperationId();
//                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
//                        // Generate remote function only if it is available in tag filter or operation filter or both
//                        if (operationTags != null || ((!filterOperations.isEmpty()) && (operationId != null))) {
//                            if (GeneratorUtils.hasTags(operationTags, filterTags) ||
//                                    ((operationId != null) && filterOperations.contains(operationId.trim()))) {
//                                // Generate remote function
//                                FunctionDefinitionNode functionDefinitionNode =
//                                        getClientMethodFunctionDefinitionNode(
//                                                functionLevelAnnotationNodes, path.getKey(), operation);
//                                functionDefinitionNodeList.add(functionDefinitionNode);
//                            }
//                        }
//                    } else {
//                        // Generate remote function
//                        FunctionDefinitionNode functionDefinitionNode = getClientMethodFunctionDefinitionNode(
//                                functionLevelAnnotationNodes, path.getKey(), operation);
//                        functionDefinitionNodeList.add(functionDefinitionNode);
//                    }
////                }
////            }
////        }
        return functionDefinitionNodeList;
    }

//    /**
//     * Generate function definition node.
//     * <pre>
//     *     remote isolated function pathParameter(int 'version, string name) returns string|error {
//     *          string  path = string `/v1/${'version}/v2/${name}`;
//     *          string response = check self.clientEp-> get(path);
//     *          return response;
//     *    }
//     *    or
//     *     resource isolated function get v1/[string 'version]/v2/[sting name]() returns string|error {
//     *         string  path = string `/v1/${'version}/v2/${name}`;
//     *         string response = check self.clientEp-> get(path);
//     *         return response;
//     *     }
//     * </pre>
//     */
    private FunctionDefinitionNode  getClientMethodFunctionDefinitionNode(Map.Entry<String, AsyncApiMessage> message,
                                                                          Map<String, JsonNode> extensions)
            throws BallerinaAsyncApiException {
        // Create api doc for function
        List<Node> remoteFunctionDocs = new ArrayList<>();
        AsyncApi25MessageImpl messageValue= (AsyncApi25MessageImpl) message.getValue();
        String messageName=message.getKey();

        if (messageValue.getSummary() != null) {
            remoteFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    messageValue.getSummary(), true));
        } else if (messageValue.getDescription() != null && !messageValue.getDescription().isBlank()) {
            remoteFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    messageValue.getDescription(), true));
        } else {
            MarkdownDocumentationLineNode newLine = createMarkdownDocumentationLineNode(null,
                    createToken(SyntaxKind.HASH_TOKEN), createEmptyNodeList());
            remoteFunctionDocs.add(newLine);
        }

        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createToken(
                                REMOTE_KEYWORD),
                createToken(ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(REMOTE_METHOD_NAME_PREFIX +messageName);

        remoteFunctionNameList.add(messageName);

        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(asyncAPI,
                ballerinaSchemaGenerator, typeDefinitionNodeList, resourceMode);
        FunctionSignatureNode functionSignatureNode =
                functionSignatureGenerator.getFunctionSignatureNode(messageValue.getPayload(),
                        remoteFunctionDocs,extensions);
        typeDefinitionNodeList = functionSignatureGenerator.getTypeDefinitionNodeList();
//        // Create `Deprecated` annotation if an operation has mentioned as `deprecated:true`
//        if (operation.getValue().getDeprecated() != null && operation.getValue().getDeprecated()) {
//            DocCommentsGenerator.extractDeprecatedAnnotation(operation.getValue().getExtensions(),
//                    remoteFunctionDocs, annotationNodes);
//        }
        // Create metadataNode add documentation string

        List<AnnotationNode> annotationNodes = new ArrayList<>();
        MetadataNode metadataNode = createMetadataNode(createMarkdownDocumentationNode(
                createNodeList(remoteFunctionDocs)), createNodeList(annotationNodes));

        // Create Function Body
        FunctionBodyGenerator functionBodyGenerator = new FunctionBodyGenerator(imports, typeDefinitionNodeList,
                asyncAPI, ballerinaSchemaGenerator, ballerinaAuthConfigGenerator, ballerinaUtilGenerator, resourceMode);
        FunctionBodyNode functionBodyNode = functionBodyGenerator.getFunctionBodyNode(extensions, ((RequiredParameterNode)functionSignatureNode.parameters().get(0)).paramName().get().toString());
        imports = functionBodyGenerator.getImports();

        //Generate relative path
//        NodeList<Node> relativeResourcePath = resourceMode ?
//                createNodeList(GeneratorUtils.getRelativeResourcePath(path, operation.getValue(), null)) :
//                createEmptyNodeList();
        NodeList<Node> relativeResourcePath =
                createEmptyNodeList();
        return createFunctionDefinitionNode(null,
                metadataNode, qualifierList, functionKeyWord, functionName, relativeResourcePath,
                functionSignatureNode, functionBodyNode);
    }

    /**
     *
     * Generate serverUrl for client default value.
     */
    private String getServerURL(AsyncApi25ServersImpl servers) throws BallerinaAsyncApiException {


        String serverURL;
        List<AsyncApiServer > serversList=servers.getItems();
        AsyncApi25ServerImpl selectedServer =(AsyncApi25ServerImpl) serversList.get(0);
        if (!selectedServer.getUrl().startsWith("https:") && servers.getItems().size() > 1) {
            for (AsyncApiServer server : serversList) {
                if (server.getUrl().startsWith("https:")) {
                    selectedServer = (AsyncApi25ServerImpl) server;
                    break;
                }
            }
        }
        if (selectedServer.getUrl() == null) {
            serverURL = "http://localhost:9090/v1";
        } else if (selectedServer.getVariables() != null) {
            Map<String, ServerVariable> variables = selectedServer.getVariables();
            URL url;
            String resolvedUrl = GeneratorUtils.buildUrl(selectedServer.getUrl(), variables);
            //                url = new URL(resolvedUrl);
//                serverURL = url.toString();
            serverURL=resolvedUrl.toString();
        } else {
            serverURL = selectedServer.getUrl();
        }
        return serverURL;
    }

    /**
     * Return auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType() {
        return ballerinaAuthConfigGenerator.getAuthType();
    }

    /**
     * Provide list of the field names in ApiKeysConfig record to generate the Config.toml file.
     *
     * @return {@link List<String>}
     */
    public List<String> getApiKeyNameList() {
        return apiKeyNameList;
    }

    /**
     * Set the `apiKeyNameList` by adding the Api Key names available under security schemas.
     *
     * @param apiKeyNameList {@link List<String>}
     */
    public void setApiKeyNameList(List<String> apiKeyNameList) {
        this.apiKeyNameList = apiKeyNameList;
    }
}