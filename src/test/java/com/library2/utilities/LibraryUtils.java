package com.library2.utilities;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
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

}
