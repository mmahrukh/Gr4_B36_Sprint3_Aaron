package com.library2.utilities;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryUtils {

    public static String getToken(String email, String password) {

        JsonPath jp = RestAssured.given().log().uri()
                .accept(ContentType.JSON)
                .contentType(ContentType.URLENC)
                .formParam("email", email)
                .formParam("password", password)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath();

        String accessToken = jp.getString("token");

        return accessToken;

    }


    public static String generateTokenByRole(String role) {

        Map<String, String> roleCredentials = returnCredentials(role);
        String email = roleCredentials.get("email");
        String password = roleCredentials.get("password");

        return getToken(email, password);

    }

    public static Map<String, String> returnCredentials(String role) {
        String email = "";
        String password = "";

        switch (role) {
            case "librarian":
                email = ConfigurationReader.getProperty("librarian_username");
                password = ConfigurationReader.getProperty("librarian_password");
                break;
            case "student":
                email = ConfigurationReader.getProperty("student_username");
                password = ConfigurationReader.getProperty("student_password");
                break;

            default:

                throw new RuntimeException("Invalid Role Entry :\n>> " + role + " <<");
        }
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        return credentials;

    }

    public static Map<String, Object> createRandomBook() {
        Faker faker = new Faker();
        Map<String, Object> book = new HashMap();
        book.put("name", "SN_test " + faker.book().title());
        book.put("isbn", faker.code().isbn13());
        book.put("year", faker.number().numberBetween(2000, 2024));
        book.put("author", "SN_test " + faker.book().author());
        book.put("book_category_id", faker.number().numberBetween(1, 20));
        book.put("description", faker.lorem().sentence(10));
        return book;
    }

    public static Map<String, Object> createRandomUser() {
        Faker faker = new Faker();
        Map<String, Object> randomUser = new HashMap();
        randomUser.put("full_name", "SN_test " + faker.name().fullName());
        randomUser.put("email", faker.internet().emailAddress());
        randomUser.put("password", faker.internet().password());
        randomUser.put("user_group_id", faker.number().numberBetween(1, 3));
        randomUser.put("status", "ACTIVE");
        randomUser.put("start_date", "2000-01-01");
        randomUser.put("end_date", "2025-04-02");
        randomUser.put("address", "SN_test " + faker.address().fullAddress());
        return randomUser;
    }

     //-- Mahrukh's user data map
    public static Map<String, Object> getRandomUserMap() {

        Faker faker = new Faker();
        Map<String, Object> userMap = new LinkedHashMap<>();

        // fake user data
        String fullName = "Mahh" + faker.name().fullName();
        String email = fullName.substring(0, fullName.indexOf(" ")) + faker.number().numberBetween(0,10) + "@library";
        System.out.println("email = " + email);

        userMap.put("full_name", fullName);
        userMap.put("email", email);
        userMap.put("password", "libraryUser");

        //2 is librarian as role
        userMap.put("user_group_id", "2");
        userMap.put("status", "ACTIVE");
        userMap.put("start_date", "2023-03-11");
        userMap.put("end_date", "2024-03-11");
        userMap.put("address", faker.address().cityName());

        return userMap;
    }

}
