package com.library2.pages;

import com.library2.utilities.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePage {

    public BasePage() {
        PageFactory.initElements(Driver.getDriver(),this);
    }

    @FindBy(xpath = "//*[@id=\"navbarDropdown\"]/span")
    public WebElement userName;

    @FindBy(xpath = "//*[@id=\"menu_item\"]/li[3]")
    public WebElement booksPageButton;



}
