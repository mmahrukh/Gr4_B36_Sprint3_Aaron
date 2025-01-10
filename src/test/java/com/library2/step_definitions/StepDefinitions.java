package com.library2.step_definitions;

import com.library2.pages.BasePage;
import com.library2.pages.BooksPage;
import com.library2.pages.LoginPage;
import com.library2.utilities.BrowserUtils;
import com.library2.utilities.DB_Utils;
import com.library2.utilities.LibraryUtils;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import static com.library2.utilities.LibraryUtils.createRandomBook;
import static com.library2.utilities.LibraryUtils.createRandomUser;
import static org.junit.Assert.assertEquals;

public class StepDefinitions {

    // REST Assured-related fields
    private final RequestSpecification givenPart = RestAssured.given().log().all();
    private Response response;
    private JsonPath jp;
    private ValidatableResponse thenPart;

    // Miscellaneous fields
    private String pathParam;
    private String token;
    private Map<String, Object> randomMap = new HashMap<>();

    // Page objects
    private final LoginPage loginPage = new LoginPage();
    private final BasePage base = new BooksPage();
    private final BooksPage book = new BooksPage();

    // -------------------- Library API Login --------------------

    @Given("I logged Library api as a {string}")
    public void iLoggedLibraryApiAsA(String role) {
        givenPart.header("x-library-token", LibraryUtils.generateTokenByRole(role));
    }

    @Given("I logged Library api with credentials {string} and {string}")
    public void iLoggedLibraryApiWithCredentials(String email, String password) {
        token = LibraryUtils.getToken(email, password);
        givenPart.header("x-library-token", token);
    }

    @Given("I send token information as request body")
    public void iSendTokenInformationAsRequestBody() {
        givenPart.formParam("token", token);
    }

    // -------------------- HTTP Header Configuration --------------------

    @And("Accept header is {string}")
    public void acceptHeaderIs(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @And("Request Content Type header is {string}")
    public void requestContentTypeHeaderIs(String contentType) {
        givenPart.contentType(contentType);
    }

    // -------------------- Path Parameter --------------------

    @Given("Path Param {string} is {string}")
    public void pathParamIs(String paramName, String paramValue) {
        givenPart.pathParam(paramName, paramValue);
        this.pathParam = paramValue;
    }

    // -------------------- Sending Requests --------------------

    @When("I send GET request to {string} endpoint")
    public void iSendGETRequestToEndpoint(String endpoint) {
        response = givenPart.when().get(endpoint);
        thenPart = response.then();
    }

    @When("I send POST request to {string} endpoint")
    public void iSendPOSTRequestToEndpoint(String endpoint) {
        response = givenPart.when().post(endpoint);
        jp = response.jsonPath();
        thenPart = response.then();
        response.prettyPrint();
    }

    // -------------------- Response Validations --------------------

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

    @Then("{string} field should be same with path param")
    public void fieldShouldBeSameWithPathParam(String idField) {
        jp = response.jsonPath();
        assertEquals(pathParam, jp.getString(idField));
    }

    @Then("following fields should not be null")
    public void followingFieldsShouldNotBeNull(List<String> fields) {
        fields.forEach(field -> Assert.assertNotNull(jp.getString(field)));
    }

    @And("the field value for {string} path should be equal to {string}")
    public void fieldValueShouldBeEqualTo(String path, String value) {
        thenPart.body(path, Matchers.is(value));
    }

    // -------------------- Random Data Creation --------------------

    @And("I create a random {string} as request body")
    public void iCreateRandomAsRequestBody(String input) {
        switch (input) {
            case "book" -> randomMap = createRandomBook();
            case "user" -> randomMap = createRandomUser();
            case "null" -> throw new InputMismatchException();
        }

        randomMap.forEach(givenPart::formParam);
    }

    // -------------------- UI and Database Validations --------------------

    @Given("I logged in Library UI as {string}")
    public void iLoggedInLibraryUIAs(String userType) {
        loginPage.login(userType);
    }

    @Given("I navigate to {string} page")
    public void iNavigateToPage(String pageName) {
        System.out.println("Navigate to \""+pageName+"\" page");
        base.booksPageButton.click();
    }

    @Then("UI, Database and API created book information must match")
    public void uiDatabaseAndAPICreatedBookInformationMustMatch() {
        // Search for the book in the UI
        book.searchBox.sendKeys(randomMap.get("name").toString() + Keys.ENTER);
        BrowserUtils.waitFor(1);

        // Retrieve the book details from the database
        DB_Utils.runQuery("SELECT * FROM books WHERE id = " + jp.getString("id"));
        Map<String, String> dataMap = DB_Utils.getRowMap(1);

        // Validate the book information across UI, database, and API
        DB_Utils.assertMapDB(dataMap, randomMap);
        Assert.assertEquals(randomMap.get("name").toString(), book.result_name.getText());
        Assert.assertEquals(randomMap.get("author").toString(), book.result_author.getText());
        Assert.assertEquals(randomMap.get("year").toString(), book.result_year.getText());
        Assert.assertEquals(randomMap.get("isbn").toString(), book.result_isbn.getText());
    }
}
