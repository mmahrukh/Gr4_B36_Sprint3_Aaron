package com.library2.step_definitions;

import com.library2.utilities.LibraryUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StepDefinitions {
    RequestSpecification givenPart = RestAssured.given().log().all();
    Response response;
    JsonPath jp;
    ValidatableResponse thenPart;
    String pathParam;

    @Given("I logged Library api as a {string}")
    public void iLoggedLibraryApiAsA(String role) {
        givenPart.header("x-library-token", LibraryUtils.generateTokenByRole(role));
    }

    @And("Accept header is {string}")
    public void acceptHeaderIs(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @When("I send GET request to {string} endpoint")
    public void iSendGETRequestToEndpoint(String endpoint) {
        response = givenPart.when().get(endpoint);
        jp = response.jsonPath();
        thenPart = response.then();
        // response.prettyPrint();
    }

    @Then("status code should be {int}")
    public void statusCodeShouldBe(int expectedStatusCode) {
        thenPart.statusCode(expectedStatusCode);
    }

    @And("Response Content type is {string}")
    public void responseContentTypeIs(String expectedContentType) {
        thenPart.contentType(expectedContentType);
    }

    @And("{string} field should not be null")
    public void fieldShouldNotBeNull(String path) {
        thenPart.body(path, Matchers.notNullValue());
    }


    @Given("Path Param {string} is {string}")
    public void path_param_is(String paramName, String paramValue) {

        givenPart.pathParam(paramName,paramValue);
        pathParam = paramValue;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String idField) {

        String results = jp.getString(idField);
        assertEquals(pathParam,results);
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> path) {

        for(String eachPath : path){
            Assert.assertNotNull(jp.getString(eachPath));
        }

    }
}