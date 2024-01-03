package com.example.demo.infrastructure.driver;

import com.example.demo.util.ZaleniumUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.net.URL;

public class Setup {

    public static WebDriver driver;

    @BeforeTest
    public WebDriver setWebDriver() throws Exception {

        String browser = System.getProperty("browser");
        String remoteDriverHost;
        if (browser == null) {
            browser = "chrome";
        }
        // In this mode we would be connecting to remote web driver(similar to selenium
        // grid) meaning the script would be executed in the remote server.
        // Below is the sample code to initialize the web driver.
        remoteDriverHost = ZaleniumUtil.REMOTE_DRIVER_HOST;
        if (remoteDriverHost == null)
            throw new Exception("RemoteDriverHost not found");
        System.out.println("Remote Host is " + remoteDriverHost);
        switch (browser) {
            case "chrome":
                // To see live execution of the script in browser please open the link
                // `{remoteDriverHost}/grid/admin/live`
                //DesiredCapabilities dcChrome = DesiredCapabilities.chrome();
                ChromeOptions dcChrome = new ChromeOptions();
                dcChrome.setCapability("name", "BrowserMode");
                driver = new RemoteWebDriver(new URL(remoteDriverHost + "/wd/hub"), dcChrome);
                driver.manage().window().maximize();
                break;
            case "firefox":
                // To see live execution of the script in browser please open the link
                // `{remoteDriverHost}/grid/admin/live`
                //DesiredCapabilities dcFirefox = DesiredCapabilities.firefox();
                FirefoxOptions dcFirefox = new FirefoxOptions();
                dcFirefox.setCapability("name", "BrowserMode");
                driver = new RemoteWebDriver(new URL(remoteDriverHost + "/wd/hub"), dcFirefox);
                driver.manage().window().maximize();
                break;
            default:
                throw new IllegalArgumentException("Browser \"" + browser + "\" isn't supported.");
        }
        return driver;
    }

    @AfterTest
    public void destroyWebDriver() throws Exception {
        driver.quit();
    }
}
