/*
 * Testerra
 *
 * (C) 2020, Peter Lehmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
 package eu.tsystems.mms.tic.testframework.core.pageobjects.testdata;

import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.PageVariables;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Created with IntelliJ IDEA. User: pele Date: 07.01.13 Time: 14:15 To change this template use File | Settings | File
 * Templates.
 */
public class WebTestPage extends AbstractWebTestPage {

    private final GuiElement input1 = new GuiElement(this.getWebDriver(), By.id("1"));
    /** A button on the page. */
    @Check(optional = true)
    private final GuiElement button1 = new GuiElement(this.getWebDriver(), By.id("4"));

    @Check
    private GuiElement specialElementFromVariable;

    /** The output text field. */
    @Check
    private final GuiElement textOutputField = new GuiElement(this.getWebDriver(), By.id("99"));

    @Deprecated
    public static class MyVariables implements PageVariables {

        int number;

        public MyVariables(int number) {
            this.number = number;
        }
    }

    /**
     * Default Page constructor.
     */
    public WebTestPage(WebDriver driver, MyVariables myVariables) {
        super(driver);

        specialElementFromVariable = new GuiElement(driver, By.id("" + myVariables.number));
    }

    public WebTestPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Test if button works as aspected.
     */
    public void assertFunctionalityOfButton1() {
        final String something = "some";
        input1.type(something);
        button1.click();
        textOutputField.asserts().assertTextContains(something);
    }

    /**
     * Click on link to open this page again.
     *
     * @return Instance of the new page.
     */
    public WebTestPage reloadPage() {
        openAgainLink.click();
        return new WebTestPage(this.getWebDriver());
    }

    public GuiElement getOpenAgain() {
        return this.openAgainLink;
    }

    /**
     * Click on not existing element
     *
     */
    public void gotoHell() {
        GuiElement guiElement = new GuiElement(this.getWebDriver(), By.xpath("id('surely_not_existing')"));
        guiElement.click();
    }

    /**
     * Proof whether non existing element is present
     *
     */
    public void nonfunctionalAssert() {
        GuiElement guiElement = new GuiElement(this.getWebDriver(), By.xpath("id('surely_not_existing')"));
        guiElement.nonFunctionalAsserts().assertIsPresent();
    }

}
