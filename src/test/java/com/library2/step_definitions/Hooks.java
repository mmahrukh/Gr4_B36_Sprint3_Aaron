package com.library2.step_definitions;

import com.library2.utilities.ConfigurationReader;
import com.library2.utilities.DB_Utils;
import com.library2.utilities.Driver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.time.Duration;

public class Hooks {

    @Before()
    public void setBaseURI() {
        System.out.println("----- Setting BaseURI");
        RestAssured.baseURI= ConfigurationReader.getProperty("library.baseUri");
    }

    @After()
    public void endScenario(Scenario scenario){
        System.out.println("Test Result for "+scenario.getName()+" "+scenario.getStatus());
    }

    @Before("@db")
    public void dbHook() {
        System.out.println("----- creating database connection");
        DB_Utils.createConnection();
    }

    @After("@db")
    public void afterDbHook() {
        System.out.println("----- closing database connection");
        DB_Utils.destroy();

    }

    @Before("@ui")
    public void setUp() {
        Driver.getDriver().get(ConfigurationReader.getProperty("library_url"));
        Driver.getDriver().manage().window().maximize();
        Driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

    }

    @After("@ui")
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            final byte[] screenshot = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png","screenshot");
        }
        Driver.closeDriver();
    }

}
