/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.languages;

import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.SupportingFile;

import java.io.File;
import java.util.Arrays;

public class GoClientCodegen extends AbstractGoCodegen {

    protected String packageVersion = "1.0.0";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";
    protected String packageRoot = "";
    protected Boolean testifyMock;

    public static final String WITH_XML = "withXml";
    public static final String INTERFACES = "interfaces";
    public static final String RETURN_HTTP_RESPONSES = "httpResponse";
    public static final String TESTIFY_MOCK = "testifyMock";
    public static final String PACKAGE_ROOT = "packageRoot";

    public GoClientCodegen() {
        super();

        outputFolder = "generated-code/go";
        modelTemplateFiles.put("model.mustache", ".go");
        apiTemplateFiles.put("api.mustache", ".go");

        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        embeddedTemplateDir = templateDir = "go";

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_VERSION, "Go package version.")
                .defaultValue("1.0.0"));
        cliOptions.add(CliOption.newBoolean(WITH_XML, "whether to include support for application/xml content type and include XML annotations in the model (works with libraries that provide support for JSON and XML)"));

        // option to change the order of form/body parameter
        cliOptions.add(CliOption.newBoolean(
                CodegenConstants.PREPEND_FORM_OR_BODY_PARAMETERS,
                CodegenConstants.PREPEND_FORM_OR_BODY_PARAMETERS_DESC)
                .defaultValue(Boolean.FALSE.toString()));

        cliOptions.add(CliOption.newBoolean(
                INTERFACES,
                "Main types are Interfaces instead of concrete implementations")
                .defaultValue(Boolean.FALSE.toString()));
        
        cliOptions.add(CliOption.newBoolean(
                RETURN_HTTP_RESPONSES,
                "Include the raw *http.Response in methods. Default is true.")
                .defaultValue(Boolean.TRUE.toString()));

        cliOptions.add(CliOption.newBoolean(
            TESTIFY_MOCK,
            "Include a testify mock stub for the API clients. Requires " + INTERFACES + "=true and " + RETURN_HTTP_RESPONSES + "=false")
            .defaultValue(Boolean.FALSE.toString()));

        cliOptions.add(CliOption.newString(
            PACKAGE_ROOT,
            "Sets the import path for the parent of the main generated package, for use with " + TESTIFY_MOCK + " so that the mock package can find it")
            .defaultValue(""));

    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            setPackageName("openapi");
        }
        
        if (additionalProperties.containsKey(PACKAGE_ROOT)) {
            setPackageRoot((String) additionalProperties.get(PACKAGE_ROOT));
        } else {
            setPackageRoot("");
        }
        
        if (additionalProperties.containsKey(TESTIFY_MOCK)) {
            setTestifyMock(Boolean.parseBoolean(additionalProperties.get(TESTIFY_MOCK).toString()));
        } else {
            setTestifyMock(false);
        }

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_VERSION)) {
            setPackageVersion((String) additionalProperties.get(CodegenConstants.PACKAGE_VERSION));
        } else {
            setPackageVersion("1.0.0");
        }

        additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        additionalProperties.put(CodegenConstants.PACKAGE_VERSION, packageVersion);

        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        modelPackage = packageName;
        apiPackage = packageName;

        supportingFiles.add(new SupportingFile("openapi.mustache", "api", "openapi.yaml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("configuration.mustache", "", "configuration.go"));
        supportingFiles.add(new SupportingFile("client.mustache", "", "client.go"));
        supportingFiles.add(new SupportingFile("response.mustache", "", "response.go"));
        supportingFiles.add(new SupportingFile(".travis.yml", "", ".travis.yml"));

        if (testifyMock) {
            supportingFiles.add(new SupportingFile("client_mock.mustache", apiPackage + "_mock", "client_mock.go")); 
            apiTemplateFiles.put("api_mock.mustache", ".go");
        }
        if (additionalProperties.containsKey(WITH_XML)) {
            setWithXml(Boolean.parseBoolean(additionalProperties.get(WITH_XML).toString()));
            if (withXml) {
                additionalProperties.put(WITH_XML, "true");
            }
        }
    }

    public String apiFilename(String templateName, String tag) {
        if (templateName.equals("api_mock.mustache")) {
            return apiFileFolder() + File.separator + apiPackage + "_mock" + File.separator + toApiFilename(tag) + ".go";
        } else {
            return apiFileFolder() + File.separator + toApiFilename(tag) + ".go";
        }
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see org.openapitools.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "go";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a Go client library (beta).";
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator;
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator;
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String toModelDocFilename(String name) {
        return toModelName(name);
    }

    @Override
    public String toApiDocFilename(String name) {
        return toApiName(name);
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public void setPackageRoot(String packageRoot) {
        this.packageRoot = packageRoot;
    }

    public void setTestifyMock(Boolean testifyMock) {
        this.testifyMock = testifyMock;
    }
}
