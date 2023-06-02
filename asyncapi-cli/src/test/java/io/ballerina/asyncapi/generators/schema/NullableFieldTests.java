/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.asyncapi.generators.schema;

import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25DocumentImpl;
import io.ballerina.asyncapi.core.GeneratorUtils;
import io.ballerina.asyncapi.core.exception.BallerinaAsyncApiException;
import io.ballerina.asyncapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.asyncapi.generators.common.TestUtils;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for SwaggerParser.
 */
public class NullableFieldTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    @Test(description = "Test for nullable primitive fields")
    public void testNullablePrimitive() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_primitive_schema.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/nullable_primitive.bal",
                syntaxTree);
    }

    @Test(description = "Test for nullable array fields")
    public void testNullableArray() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_array_schema.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/nullable_array.bal",
                syntaxTree);
    }

    @Test(description = "Test for nullable array referenced schemas")
    public void testNullableArrayRefSchemas() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_ref_array.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/nullable_ref_array.bal",
                syntaxTree);
    }

    @Test(description = "Test nullable for primitive referenced type")
    public void testPrimitiveReferencedTypes() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_string_type.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String syntaxTreeContent = syntaxTree.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(syntaxTreeContent, "publictypeLatitudestring?;");
    }

    @Test(description = "Test for referenced schema with no type given")
    public void testNullTypeReference() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_null_type.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_null_type.bal", syntaxTree);
    }

    @Test(description = "Test for nullable record fields")
    public void testNullableRecord() throws IOException, BallerinaAsyncApiException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_record_schema.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/nullable_record.bal",
                syntaxTree);
    }

    @Test(description = "Test for union type generation for nullable anyOf schema")
    public void testNullableUnionType() throws IOException, BallerinaAsyncApiException, FormatterException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_anyof_schema.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/nullable_anyof_schema.bal",
                syntaxTree);
    }

    @Test(description = "Test for union type generation for nullable anyOf schema with array schema")
    public void testNullableArrayUnionType() throws IOException, BallerinaAsyncApiException, FormatterException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/nullable_anyof_array_schema.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/" +
                        "nullable_anyof_array_schema.bal", syntaxTree);
    }

    @Test(description = "Test for type generation for object schema with no properties")
    public void testNullableEmptyObjectSchema() throws IOException, BallerinaAsyncApiException, FormatterException {
        AsyncApi25DocumentImpl openAPI = GeneratorUtils.normalizeAsyncAPI(RES_DIR.resolve("swagger" +
                "/null_empty_record.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/" +
                "null_empty_record.bal", syntaxTree);
    }
}