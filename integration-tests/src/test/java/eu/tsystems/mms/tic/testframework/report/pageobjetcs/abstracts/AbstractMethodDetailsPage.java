package eu.tsystems.mms.tic.testframework.report.pageobjetcs.abstracts;

import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.factory.PageFactory;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.DashboardPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodDetailsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodStackPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodStepsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class AbstractMethodDetailsPage extends AbstractReportPage {

    protected GuiElement mainFrame = new GuiElement(this.getWebDriver(), By.cssSelector("frame[name='main']"));
    /**
     * Buttons on Top
     */
    protected GuiElement backButton = new GuiElement(this.getWebDriver(), By.xpath("//div[@class='detailsmenu']"), mainFrame);
    protected GuiElement detailsButton = new GuiElement(this.getWebDriver(), By.id("buttondetails"), mainFrame);
    protected GuiElement stepsButton = new GuiElement(this.getWebDriver(), By.id("buttondlogs"), mainFrame);
    protected GuiElement stackButton = new GuiElement(this.getWebDriver(), By.id("buttondstack"), mainFrame);
    protected GuiElement minorErrorButton = new GuiElement(this.getWebDriver(), By.id("buttonminor"), mainFrame);

    /**
     * Constructor called bei PageFactory
     *
     * @param driver Webdriver to use for this Page
     */
    public AbstractMethodDetailsPage(WebDriver driver) {
        super(driver);
        driver.switchTo().defaultContent();
        driver.switchTo().frame(0);
    }

    /**
     * click Buttons on Top
     *
     * @return DashboardPage
     */
    public DashboardPage gotoDashboardPageByClickingBackButton() {
        backButton.click();
        return PageFactory.create(DashboardPage.class, this.getWebDriver());
    }

    public MethodDetailsPage gotoMethodDetailsPage() {
        detailsButton.click();
        return PageFactory.create(MethodDetailsPage.class, this.getWebDriver());
    }

    public MethodStepsPage gotoMethodStepsPage() {
        stepsButton.click();
        return PageFactory.create(MethodStepsPage.class, this.getWebDriver());
    }

    public MethodStackPage gotoMethodStackPage() {
        stackButton.click();
        return PageFactory.create(MethodStackPage.class, this.getWebDriver());
    }
}