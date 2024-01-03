package com.example.demo.infrastructure.driver;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterTest;

public class TearDown {

    private WebDriver driver;

    public TearDown() {
        this.driver = Setup.driver;
    }

    @AfterTest
    public void quitDriver() {

        driver.close();
    }

}
