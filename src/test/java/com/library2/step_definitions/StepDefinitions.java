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

import static com.library2.utilities.LibraryUtils.*;
import static org.junit.Assert.assertEquals;

public class StepDefinitions {

    // REST-Assured fields for managing API requests and responses
    private final RequestSpecification givenPart = RestAssured.given().log().all();
    private Response response;
    private JsonPath jp;
    private ValidatableResponse thenPart;

    // Miscellaneous fields for storing shared data
    private String pathParam;
    private String token; // Token for reuse across multiple API calls
    private Map<String, Object> randomMap = new HashMap<>();

    // Page objects for UI interaction
    private final LoginPage loginPage = new LoginPage();
    private final BasePage base = new BooksPage();
    private final BooksPage book = new BooksPage();

    // -------------------- API Scenarios --------------------

    // Logs in to the Library API using a role and retrieves the corresponding token
    @Given("I logged Library api as a {string}")
    public void iLoggedLibraryApiAsA(String role) {
        givenPart.header("x-library-token", LibraryUtils.generateTokenByRole(role));
    }

    // Logs in to the Library API using explicit email and password credentials
    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {
        token = LibraryUtils.getToken(email, password);
        givenPart.header("x-library-token", token);
    }

    // Adds the token to the request body for further API calls
    @Given("I send token information as request body")
    public void i_send_token_information_as_request_body() {
        givenPart.formParam("token", token); // Token is sent as part of the request payload
    }

    // Configures HTTP headers for API requests
    @And("Accept header is {string}")
    public void acceptHeaderIs(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @And("Request Content Type header is {string}")
    public void requestContentTypeHeaderIs(String contentType) {
        givenPart.contentType(contentType);
    }

    // Sends a GET request to a specified API endpoint
    @When("I send GET request to {string} endpoint")
    public void iSendGETRequestToEndpoint(String endpoint) {
        response = givenPart.when().get(endpoint);
        thenPart = response.then();
    }

    // Sends a POST request to a specified API endpoint
    @When("I send POST request to {string} endpoint")
    public void iSendPOSTRequestToEndpoint(String endpoint) {
        response = givenPart.when().post(endpoint);
        jp = response.jsonPath(); // Extracts the response body for further analysis
        thenPart = response.then();
        response.prettyPrint(); // Prints the response for debugging purposes
    }

    // Sets a path parameter for API requests
    @Given("Path Param {string} is {string}")
    public void pathParamIs(String paramName, String paramValue) {
        givenPart.pathParam(paramName, paramValue);
        this.pathParam = paramValue; // Stores the path parameter for validation
    }

    // -------------------- API Response Validations --------------------

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

    // Verifies that a specific field in the response matches the path parameter
    @Then("{string} field should be same with path param")
    public void fieldShouldBeSameWithPathParam(String idField) {
        jp = response.jsonPath(); // Parse the response body
        String results = jp.getString(idField);
        assertEquals(pathParam, results); // Validates the path parameter against the response
    }

    // Ensures that multiple fields in the response are not null
    @Then("following fields should not be null")
    public void followingFieldsShouldNotBeNull(List<String> fields) {
        fields.forEach(field -> Assert.assertNotNull(jp.getString(field)));
    }

    // Validates that a specific field value matches the expected value
    @And("the field value for {string} path should be equal to {string}")
    public void fieldValueShouldBeEqualTo(String path, String value) {
        thenPart.body(path, Matchers.is(value));
    }

    // -------------------- Random Data Creation --------------------

    @And("I create a random {string} as request body")
    public void iCreateRandomAsRequestBody(String input) {
        switch (input) {
            case "book" -> randomMap = createRandomBook(); // Generates random book data
            case "user" -> randomMap = getRandomUserMap(); // Generates random user data
            case "null" -> throw new InputMismatchException(); // Handles invalid input
        }

        // Adds all key-value pairs from the random map to the request
        randomMap.forEach(givenPart::formParam);
    }

    // -------------------- UI Validations --------------------

    // Logs into the Library UI using the specified user type
    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);
    }

    // Navigates to a specific page in the UI
    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String pageName) {
        System.out.println("Navigating to \""+pageName+"\" page");
        base.booksPageButton.click(); // Clicks the button for navigation
    }

    // Verifies that book information matches across UI, database, and API
    @Then("UI, Database and API created book information must match")
    public void uiDatabaseAndApiCreatedBookInformationMustMatch() {

        String query = "SELECT * FROM books WHERE id='" + jp.getString("id")+"'";

        // Search for the book in the UI
        book.searchBox.sendKeys(randomMap.get("name").toString() + Keys.ENTER);
        BrowserUtils.waitFor(1);

        System.out.println("Query running: "+query);

        // Retrieve book details from the database
        DB_Utils.runQuery(query);
        Map<String, String> dataMap = DB_Utils.getRowMap(1);

        System.out.println("Retrieved data into the map from the query:\n"+dataMap);

        // Compare data from API, UI, and database
        DB_Utils.assertMapDB(dataMap, randomMap);
        Assert.assertEquals(randomMap.get("name").toString(), book.result_name.getText());
        Assert.assertEquals(randomMap.get("author").toString(), book.result_author.getText());
        Assert.assertEquals(randomMap.get("year").toString(), book.result_year.getText());
        Assert.assertEquals(randomMap.get("isbn").toString(), book.result_isbn.getText());
    }

    // -------------------- Database Validations --------------------

    // Compares API-created user data with database data
    @Then("created user information should match with Database")
    public void createdUserInformationShouldMatchWithDatabase() {
        String id = jp.getString("user_id"); // Extracts user ID from API response
        DB_Utils.runQuery("SELECT * FROM books WHERE id=" + id); // Fetches data from the database
        Map<String, String> dataMapDb = DB_Utils.getRowMap(1);
        DB_Utils.assertMapDB(dataMapDb, randomMap); // Compares database and API data
    }

    // Validates that the created user can log into the UI
    @Then("created user should be able to login Library UI")
    public void createdUserShouldBeAbleToLoginLibraryUI() {
        String createdEmail = (String) randomMap.get("email");
        String createdPassword = (String) randomMap.get("password");
        loginPage.login(createdEmail, createdPassword);
    }

    // Verifies that the created user's name appears in the dashboard
    @Then("created user name should appear in Dashboard Page")
    public void createdUserNameShouldAppearInDashboardPage() {
        BrowserUtils.waitForVisibility(book.userName, 3); // Waits for the username to be visible
        String createdUsername = randomMap.get("full_name").toString();
        assertEquals(book.userName.getText(), createdUsername);
    }
}
