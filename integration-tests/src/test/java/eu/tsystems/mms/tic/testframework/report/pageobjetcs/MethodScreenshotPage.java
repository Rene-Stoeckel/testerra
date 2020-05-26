package eu.tsystems.mms.tic.testframework.report.pageobjetcs;

import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MethodScreenshotPage extends MethodDetailsPage {
    @Check
    private GuiElement screenShotInfoButton = new GuiElement(this.getWebDriver(), By.xpath("//i[@class='bgwhite fa fa-info']"), mainFrame);

    private GuiElement screenShot = new GuiElement(this.getWebDriver(), By.xpath("//div[@class='fotorama__html']"), mainFrame);

    public MethodScreenshotPage(WebDriver driver) {
        super(driver);
        checkPage();
    }
    public GuiElement getScreenShot() {
        return screenShot;
    }

}