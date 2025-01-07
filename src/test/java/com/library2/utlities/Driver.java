package com.library2.utlities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.time.Duration;

public class Driver {


    //create a private constructor to remove access to this object
    private Driver(){}

    /*
    We make the WebDriver private, because we want to close access from outside the class.
    We are making it static, because we will use it in a static method.
     */

    //private static WebDriver driver; // default value = null

    /*
        InheritableThreadLocal  --> this is like a container, bag, pool.
        in this pool we can have separate objects for each thread
        for each thread, in InheritableThreadLocal we can have separate object for that thread
     */

    private static InheritableThreadLocal<WebDriver> driverPool = new InheritableThreadLocal<>();           // driver class will provide separate webdriver object per thread

    /*
    Create a re-usable utility method which will return the same driver instance once we call it.
    If an instance doesn't exist, it will create first, and then it will always return same instance.
     */
    public static WebDriver getDriver(){

        if(driverPool.get() == null){

            /*
                if we pass the driver from terminal then use that one
                if we do not pass the driver from terminal then use the one properties file
             */
            String browser = System.getProperty("browser") != null ? browser = System.getProperty("browser") : ConfigurationReader.getProperty("browser");

            /*
            Depending on the browserType
            switch statement will determine the "case", and open the matching browser.
             */
            switch (browser){
//                case "remote-chrome":
//                    try {
//                        // assign your grid server address
//                        String gridAddress = "174.129.57.20";
//                        URL url = new URL("http://"+ gridAddress + ":4444/wd/hub");
//                        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
//                        desiredCapabilities.setBrowserName("chrome");
//                        driverPool.set(new RemoteWebDriver(url, desiredCapabilities));
//                        //driverPool.set(new RemoteWebDriver(new URL("http://0.0.0.0:4444/wd/hub"),desiredCapabilities));
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
                case "chrome":
                    driverPool.set(new ChromeDriver());
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                case "firefox":
                    driverPool.set(new FirefoxDriver());
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                case "edge":
                    driverPool.set(new EdgeDriver());
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                // opens Browser in the background
                case "headless-chrome":
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless=new");
                    driverPool.set(new ChromeDriver(options));
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                // for Driver updated version
                case "remote-allow-origins":
                    ChromeOptions options2 = new ChromeOptions();
                    options2.addArguments("--remote-allow-origins=*");
                    driverPool.set(new ChromeDriver(options2));
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
            }

        }

        return driverPool.get();

    }

    /*
    Create a new Driver.closeDriver(); it will use .quit() method to quit browsers, and then set the driver value back to null.
     */
    public static void closeDriver(){
        if (driverPool.get()!=null){
            driverPool.get().quit();        //This line will terminate the currently existing driver completely. It will not exist going forward.
            driverPool.remove();            //We assign the value back to "null" so that my "singleton" can create a newer one if needed.
        }
    }

}