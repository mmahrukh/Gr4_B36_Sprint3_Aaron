package com.library2.step_definitions;

import com.library2.pages.BasePage;
import com.library2.pages.BooksPage;
import com.library2.pages.LoginPage;
import com.library2.utilities.BrowserUtils;
import com.library2.utilities.DB_Utils;
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
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import static com.library2.utilities.LibraryUtils.createRandomBook;
import static com.library2.utilities.LibraryUtils.createRandomUser;
import static org.junit.Assert.assertEquals;

public class StepDefinitions {
    RequestSpecification givenPart = RestAssured.given().log().all();
    Response response;
    JsonPath jp;
    ValidatableResponse thenPart;
    String pathParam;

    LoginPage loginPage = new LoginPage();
    BasePage base = new BooksPage();
    BooksPage book = new BooksPage();
    Map<String, Object> randomMap = new HashMap();

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
        this.pathParam = paramValue;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String idField) {

        jp = response.jsonPath();
        String results = jp.getString(idField);
        assertEquals(pathParam,results);
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> path) {

        for(String eachPath : path){
            Assert.assertNotNull(jp.getString(eachPath));
        }

    }

    //-----------US_03_01-----------
    @And("Request Content Type header is {string}")
    public void requestContentTypeHeaderIs(String contentType) {
        givenPart.contentType(contentType);
    }


    @And("I create a random {string} as request body")
    public void iCreateARandomAsRequestBody(String input) {
        switch (input){
            case "book"->{
                randomMap = createRandomBook();
            }
            case "user"->{
                //randomMap = createRandomUser(); //serge's custom user map
                randomMap = getRandomUserMap(); //mahrukh's custom user map
            }
            case "null"->{
                throw new InputMismatchException();
            }
        }
        for(Map.Entry<String, Object> entry : randomMap.entrySet()){
            givenPart.formParam(entry.getKey(), entry.getValue());
        }
    }

    @When("I send POST request to {string} endpoint")
    public void iSendPOSTRequestToEndpoint(String endPoint) {
        response = givenPart.when().post(endPoint);
        jp = response.jsonPath();
        thenPart = response.then();
        response.prettyPrint();
    }

    @And("the field value for {string} path should be equal to {string}")
    public void theFieldValueForPathShouldBeEqualTo(String path, String value) {
        thenPart.body(path,Matchers.is(value));
    }



    //-----------US_03_02-----------
    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);
    }
    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String string) {
        base.booksPageButton.click();
    }
    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {

        book.searchBox.sendKeys(randomMap.get("name").toString() + Keys.ENTER);
        BrowserUtils.waitFor(1);


        DB_Utils.runQuery("select * from books where id=" + jp.getString("id"));
        Map<String,String> dataMap = DB_Utils.getRowMap(1);


        DB_Utils.assertMapDB(dataMap,randomMap);
        Assert.assertEquals(randomMap.get("name").toString(),book.result_name.getText());
        Assert.assertEquals(randomMap.get("author").toString(),book.result_author.getText());
        Assert.assertEquals(randomMap.get("year").toString(),book.result_year.getText());
        Assert.assertEquals(randomMap.get("isbn").toString(),book.result_isbn.getText());
    }

    //-- US04 --
    Map<String,String> dataMapDb;
    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

        //1. Extract user_id from the API response
        String id = jp.getString("user_id");
        System.out.println("userId = " + id);

        //2. Database - run Query & store as DB Data
        DB_Utils.runQuery("select * from books where id=" + id);
        dataMapDb = DB_Utils.getRowMap(1);

        //3. Compare API with DB
        DB_Utils.assertMapDB(dataMapDb, randomMap);
    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {
        String createdEmail = (String) randomMap.get("email");         //down-casting
        String createdPassword = (String) randomMap.get("password");  //down-casting

        loginPage.login(createdEmail, createdPassword);
    }

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {
        BrowserUtils.waitForVisibility(book.userName, 3);
        String createdUsername = randomMap.get("full_name").toString();
        assertEquals(book.userName.getText(), createdUsername);
    }


}