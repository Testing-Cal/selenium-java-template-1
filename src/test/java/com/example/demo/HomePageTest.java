package com.example.demo;

import com.example.demo.infrastructure.driver.Setup;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class HomePageTest {

    Setup setup = new Setup();
    WebDriver driver;

    @BeforeTest
    public void EnterUrlToBrowser() throws Exception {
       driver = setup.setWebDriver();
    }

    @Test
    public void OpenBrowser() throws Exception {
        //WebDriver driver = setup.setWebDriver();
        String title = driver.getTitle();
        driver.navigate().to("https://www.yahoo.com");
        System.out.println("page title is :" + title);

    }

    @AfterTest
    public void quitbrowser() throws Exception {
        System.out.println("Closing the browser ");
        setup.destroyWebDriver();
    }
}