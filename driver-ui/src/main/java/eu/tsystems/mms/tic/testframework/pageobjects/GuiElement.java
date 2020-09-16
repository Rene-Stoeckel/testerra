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
package eu.tsystems.mms.tic.testframework.pageobjects;

import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.exceptions.ElementNotFoundException;
import eu.tsystems.mms.tic.testframework.execution.testng.CollectedAssertion;
import eu.tsystems.mms.tic.testframework.execution.testng.InstantAssertion;
import eu.tsystems.mms.tic.testframework.execution.testng.NonFunctionalAssertion;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.BasicUiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.HasParent;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.UiElementActions;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.UiElementAssertions;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.AssertionProvider;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.BinaryAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.DefaultBinaryAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.DefaultImageAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.DefaultQuantityAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.DefaultRectAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.DefaultStringAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.GuiElementAssert;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.GuiElementAssertDescriptionDecorator;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.GuiElementAssertFactory;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.ImageAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.PropertyAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.PropertyAssertionFactory;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.QuantityAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.RectAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.StringAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.core.AbstractGuiElementCore;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.core.GuiElementCore;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.core.GuiElementCoreFrameAwareDecorator;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.core.GuiElementCoreSequenceDecorator;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.core.GuiElementData;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.facade.DefaultGuiElementFacade;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.facade.DelayActionsGuiElementFacade;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.facade.GuiElementFacade;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.facade.GuiElementFacadeLoggingDecorator;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.frames.FrameLogic;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.frames.IFrameLogic;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.waiters.GuiElementWait;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.waiters.GuiElementWaitFactory;
import eu.tsystems.mms.tic.testframework.simulation.UserSimulator;
import eu.tsystems.mms.tic.testframework.utils.Formatter;
import eu.tsystems.mms.tic.testframework.webdrivermanager.IWebDriverFactory;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * GuiElement is the access point for most tests and is an extension of WebElement.
 * <p>
 * Authors: pele, rnhb
 */
public class GuiElement implements
    UiElementFactoryProvider,
    UiElement,
    Loggable
{
    private static final PropertyAssertionFactory propertyAssertionFactory = Testerra.injector.getInstance(PropertyAssertionFactory.class);

    private GuiElementAssert defaultAssert;
    private GuiElementAssert instantAssert;
    private GuiElementAssert collectableAssert;
    private GuiElementAssert nonFunctionalAssert;

    private GuiElementCore core;
    private final GuiElementData guiElementData;
    private GuiElementFacade decoratedFacade;
    private GuiElementWait decoratedWait;

    protected HasParent parent;
    private UserSimulator userSimulator;
    private DefaultUiElementList list;

    /**
     * Elementary constructor
     */
    private GuiElement(GuiElementData data) {
        guiElementData = data;
        guiElementData.setGuiElement(this);
        IWebDriverFactory factory = WebDriverSessionsManager.getWebDriverFactory(guiElementData.getBrowser());
        core = factory.createCore(guiElementData);
    }

    public GuiElementCore getCore() {
        return this.core;
    }

    public GuiElementData getData() {
        return this.guiElementData;
    }

    /**
     * Constructor for list elements of {@link #list}
     * Elements created by this constructor are identical to it's parent,
     * but with a different element index.
     */
    public GuiElement(GuiElement guiElement, int index) {
        this(new GuiElementData(guiElement.guiElementData, index));
        setParent(guiElement.getParent());
        createDecorators();
    }

    /**
     * Constructor for {@link UiElementFactory#createFromParent(UiElement, Locate)}
     * This is the internal standard constructor for elements with parent {@link GuiElementCore} implementations.
     */
    public GuiElement(GuiElementCore core) {
        this.core = core;
        AbstractGuiElementCore realCore = (AbstractGuiElementCore)core;
        guiElementData = realCore.guiElementData;
        guiElementData.setGuiElement(this);
        createDecorators();
    }

    /**
     * Constructor for {@link UiElementFactory#createFromPage(PageObject, Locate)}
     */
    public GuiElement(PageObject page, Locate locate) {
        this(page.getWebDriver(), locate, null);
        Page realPage = (Page)page;
        setTimeoutInSeconds(realPage.getElementTimeoutInSeconds());
        setParent(page);
    }

    /**
     * Constructor for {@link UiElementFactory#createWithFrames(Locate, UiElement...)}
     */
    public GuiElement(
        WebDriver driver,
        Locate locate,
        UiElement... frames
    ) {
        this(new GuiElementData(driver, locate));
        guiElementData.setGuiElement(this);
        if (frames != null && frames.length > 0) {
            guiElementData.setFrameLogic(new FrameLogic(driver, frames));
        }
        createDecorators();
    }

    @Deprecated
    public GuiElement(WebDriver driver, By by) {
        this(driver, by, null);
    }

    public GuiElement(WebDriver driver, Locate locate) {
        this(driver, locate, null);
    }

    @Deprecated
    public GuiElement(
        WebDriver driver,
        By by,
        UiElement... frames
    ) {
        this(driver, Locate.by(by), frames);
    }

    @Override
    public Locate getLocate() {
        return guiElementData.getLocate();
    }

    /**
     * We cannot use the GuiElementFactory for decorating the facade here,
     * since GuiElement is not always created by it's according factory
     * and implementing a GuiElementFacadeFactory is useless.
     * You can move this code to DefaultGuiElementFactory when no more 'new GuiElement()' calls exists.
     */
    private void createDecorators() {
        if (guiElementData.hasFrameLogic()) {
            // if frames are set, the waiter should use frame switches when executing its sequences
            core = new GuiElementCoreFrameAwareDecorator(core, guiElementData);
        }

        GuiElementWaitFactory waitFactory = Testerra.injector.getInstance(GuiElementWaitFactory.class);
        decoratedWait = waitFactory.create(guiElementData);

        // Wrap the core with sequence decorator, such that its methods are executed with sequence
        GuiElementCore sequenceCore = new GuiElementCoreSequenceDecorator(core, guiElementData);
        decoratedFacade = new DefaultGuiElementFacade(sequenceCore);
        decoratedFacade = new GuiElementFacadeLoggingDecorator(decoratedFacade, guiElementData);

        int delayAfterAction = Properties.DELAY_AFTER_ACTION_MILLIS.asLong().intValue();
        int delayBeforeAction = Properties.DELAY_BEFORE_ACTION_MILLIS.asLong().intValue();
        if (delayAfterAction > 0 || delayBeforeAction > 0) {
            decoratedFacade = new DelayActionsGuiElementFacade(decoratedFacade, delayBeforeAction, delayAfterAction, guiElementData);
        }
    }

    /**
     * After retrieving all WebElements of the given locator, this method can be used to filter the WebElements.
     * Different filters can be applied in conjunction (and).
     *
     * @param filter Filters to be applied
     *
     * @return The same GuiElement
     * @deprecated Use {@link Locate} instead
     */
    @Deprecated
    public GuiElement withWebElementFilter(Predicate<WebElement> filter) {
        guiElementData.getLocate().filter(filter);
        return this;
    }

    /**
     * Add a Decorator to log every action performed on this element with addition to a short description of it.
     *
     * @param description A very short description of this GuiElement, for example "Continue Shopping Button"
     *
     * @deprecated Use {@link #setName(String)} instead
     */
    @Deprecated
    public GuiElement setDescription(String description) {
        guiElementData.setName(description);
        return this;
    }

    /**
     * Get sub element by locator. Using this executes a find on the parent element and the parent.findElement for the
     * given locator. It does not wait for the subelement if the parent has been found!
     *
     * @param by Locator of new element.
     *
     * @return GuiElement
     */
    @Deprecated
    public GuiElement getSubElement(By by) {
        return getSubElement(by, null);
    }

    /**
     * Get sub element by locator. Using this executes a find on the parent element and the parent.findElement for the
     * given locator. It does not wait for the subelement if the parent has been found!
     *
     * @param by   Locator of new element.
     * @param description Description for GuiElement
     *
     * @return GuiElement
     */
    @Deprecated
    public GuiElement getSubElement(By by, String description) {
        GuiElement subElement = getSubElement(Locate.by(by));
        if (description != null) subElement.setName(description);
        return subElement;
    }

    @Deprecated
    public GuiElement getSubElement(Locate locate) {
        return (GuiElement)find(locate);
    }

    @Override
    public WebElement getWebElement() {
        return decoratedFacade.getWebElement();
    }

    @Override
    public UiElement scrollIntoView(Point offset) {
        decoratedFacade.scrollIntoView(offset);
        return this;
    }

    public By getBy() {
        return decoratedFacade.getBy();
    }

    @Deprecated
    public UiElement scrollToElement() {
        decoratedFacade.scrollToElement();
        return this;
    }
    @Deprecated
    public UiElement scrollToElement(int yOffset) {
        decoratedFacade.scrollToElement(yOffset);
        return this;
    }

    @Override
    public UiElement select() {
        decoratedFacade.select();
        return this;
    }

    @Override
    public UiElement deselect() {
        decoratedFacade.deselect();
        return this;
    }

    @Override
    public UiElement type(String text) {
        decoratedFacade.type(text);
        return this;
    }

    @Override
    public UiElement click() {
        decoratedFacade.click();
        return this;
    }

    public UiElement submit() {
        decoratedFacade.submit();
        return this;
    }

    @Override
    public UiElement sendKeys(CharSequence... charSequences) {
        decoratedFacade.sendKeys(charSequences);
        return this;
    }

    @Override
    public UiElementActions asUser() {
        if (this.userSimulator==null) {
            this.userSimulator = new UserSimulator(this);
        }
        return this.userSimulator;
    }

    @Override
    public UiElement clear() {
        decoratedFacade.clear();
        return this;
    }

    @Override
    public InteractiveUiElement hover() {
        return mouseOver();
    }

    public String getTagName() {
        return decoratedFacade.getTagName();
    }

    public String getAttribute(String attributeName) {
        return decoratedFacade.getAttribute(attributeName);
    }

    public boolean isSelected() {
        return decoratedFacade.isSelected();
    }

    public boolean isEnabled() {
        return decoratedFacade.isEnabled();
    }

    public String getText() {
        return decoratedFacade.getText();
    }

    public boolean isDisplayed() {
        return decoratedFacade.isDisplayed();
    }

    public boolean isVisible(final boolean complete) {
        return decoratedFacade.isVisible(complete);
    }

    /**
     * Checks if the element is selectable.
     * <p>
     * WARNING: To determine if the element is truly selectable, a selection or deselection will be done and reverted.
     * Keep this in mind.
     *
     * @return true, if the element is selectable.
     */
    public boolean isSelectable() {
        return decoratedFacade.isSelectable();
    }

    public Point getLocation() {
        return decoratedFacade.getLocation();
    }

    public Dimension getSize() {
        return decoratedFacade.getSize();
    }

    @Override
    public UiElement find(Locate locate) {
        return uiElementFactory.createFromParent(this, locate);
    }

    @Override
    @Deprecated
    public UiElementList<UiElement> list() {
        if (this.list == null) {
            this.list = new DefaultUiElementList(this);
        }
        return this.list;
    }

    public String getCssValue(String cssIdentifier) {
        return decoratedFacade.getCssValue(cssIdentifier);
    }

    public UiElement mouseOver() {
        decoratedFacade.mouseOver();
        return this;
    }

    public boolean isPresent() {
        return decoratedFacade.isPresent();
    }

    public Select getSelectElement() {
        return decoratedFacade.getSelectElement();
    }

    public List<String> getTextsFromChildren() {
        return decoratedFacade.getTextsFromChildren();
    }

    public boolean anyFollowingTextNodeContains(String contains) {
        TestableUiElement textElements = anyElementContainsText(contains);
        return textElements.present().getActual();
    }

    /**
     * This method is no part of any interface, because we don't know if
     * we want to support this feature at the moment.
     */
    public TestableUiElement anyElementContainsText(String text) {
        String textFinderXpath = String.format("//text()[contains(., '%s')]/..", text);
        return find(By.xpath(textFinderXpath));
    }

    @Override
    public WebDriver getWebDriver() {
        return guiElementData.getWebDriver();
    }

    @Override
    public UiElement doubleClick() {
        decoratedFacade.doubleClick();
        return this;
    }

    /**
     * @deprecated This method should not be public
     */
    @Deprecated
    public int getTimeoutInSeconds() {
        return guiElementData.getTimeoutSeconds();
    }

    public UiElement setTimeoutInSeconds(int timeoutInSeconds) {
        propertyAssertionFactory.setDefaultTimeoutSeconds(timeoutInSeconds);
        guiElementData.setTimeoutSeconds(timeoutInSeconds);
        return this;
    }

    /**
     * @deprecated This method should not be public
     */
    public UiElement restoreDefaultTimeout() {
        PageOverrides pageOverrides = Testerra.injector.getInstance(PageOverrides.class);
        guiElementData.setTimeoutSeconds(pageOverrides.getTimeoutSeconds());
        return this;
    }

    @Override
    public UiElement highlight(Color color) {
        decoratedFacade.highlight(color);
        return this;
    }

    public UiElement swipe(int offsetX, int offSetY) {
        decoratedFacade.swipe(offsetX, offSetY);
        return this;
    }

    @Deprecated
    public int getLengthOfValueAfterSendKeys(String textToInput) {
        return decoratedFacade.getLengthOfValueAfterSendKeys(textToInput);
    }

    @Deprecated
    public int getNumberOfFoundElements() {
        return decoratedFacade.getNumberOfFoundElements();
    }

    /**
     * @deprecated Use {@link #contextClick()} instead
     */
    @Deprecated
    public UiElement rightClick() {
        return this.contextClick();
    }

    @Override
    public UiElement contextClick() {
        decoratedFacade.rightClick();
        return this;
    }

    @Deprecated
    public File takeScreenshot() {
        return decoratedFacade.takeScreenshot();
    }

    /**
     * Get Frame Login object. It encapsulates the correct order of switching the frames containing this GuiElement.
     *
     * @return Frame Logic object or null.
     */
    @Override
    public IFrameLogic getFrameLogic() {
        return guiElementData.getFrameLogic();
    }

    public boolean hasFrameLogic() {
        return guiElementData.hasFrameLogic();
    }

    /**
     * Sets the abstract parent
     * @param parent {@link UiElement} or {@link PageObject}
     */
    public UiElement setParent(HasParent parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Retrieves the parent
     * @return Can be {@link UiElement} or {@link PageObject}
     */
    @Override
    public HasParent getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return guiElementData.toString();
    }

    @Override
    public String toString(boolean details) {
        return guiElementData.toString(details);
    }

    @Deprecated
    public WebDriver getDriver() {
        return getWebDriver();
    }

    public boolean hasSensibleData() {
        return guiElementData.sensibleData;
    }

    public GuiElement sensibleData() {
        guiElementData.sensibleData = true;
        return this;
    }

    @Override
    public GuiElement setName(String name) {
        guiElementData.setName(name);
        return this;
    }

    @Override
    public String getName() {
        return guiElementData.getName();
    }

    /**
     * Provides access to all functional assert methods.
     *
     * @return GuiElementAssert object for functional assertions
     */
    @Deprecated
    public GuiElementAssert asserts() {
        if (defaultAssert == null) {
            if (UiElement.Properties.DEFAULT_ASSERT_IS_COLLECTOR.asBool()) {
                defaultAssert = assertCollector();
            } else {
                defaultAssert = instantAsserts();
            }
        }
        return defaultAssert;
    }

    /**
     * Provides access to all assert methods. If an assertion fails, the assertDescription will be given as cause,
     * instead of the technical cause like an isDisplayed error.
     *
     * @param errorMessage Cause returned on assertion error.
     *
     * @return GuiElementAssert object for functional assertions
     */
    @Deprecated
    public GuiElementAssert asserts(String errorMessage) {
        GuiElementAssertDescriptionDecorator guiElementAssertDescriptionDecorator
                = new GuiElementAssertDescriptionDecorator(errorMessage, asserts());
        return guiElementAssertDescriptionDecorator;
    }

    /**
     * Provides access to all non-functional assert methods.
     *
     * @return GuiElementAssert object for non-functional assertions
     */
    @Deprecated
    public GuiElementAssert nonFunctionalAsserts() {
        if (nonFunctionalAssert==null) {
            GuiElementAssertFactory assertFactory = Testerra.injector.getInstance(GuiElementAssertFactory.class);
            NonFunctionalAssertion assertion = Testerra.injector.getInstance(NonFunctionalAssertion.class);
            nonFunctionalAssert = assertFactory.create(guiElementData, assertion, decoratedWait);
        }
        return nonFunctionalAssert;
    }

    /**
     * Provides access to all non-functional assert methods. If an assertion fails, the assertDescription will be
     * given as cause, instead of the technical cause like an isDisplayed error.
     *
     * @param errorMessage Cause returned on assertion error.
     *
     * @return GuiElementAssert object for non-functional assertions
     */
    @Deprecated
    public GuiElementAssert nonFunctionalAsserts(String errorMessage) {
        GuiElementAssertDescriptionDecorator guiElementAssertDescriptionDecorator
                = new GuiElementAssertDescriptionDecorator(errorMessage, nonFunctionalAsserts());
        return guiElementAssertDescriptionDecorator;
    }

    @Deprecated
    public GuiElementAssert instantAsserts() {
        if (instantAssert == null) {
            GuiElementAssertFactory assertFactory = Testerra.injector.getInstance(GuiElementAssertFactory.class);
            InstantAssertion assertion = Testerra.injector.getInstance(InstantAssertion.class);
            instantAssert = assertFactory.create(guiElementData, assertion, decoratedWait);
        }
        return instantAssert;
    }

    /**
     * Provides access to all functional assert methods.
     *
     * @return GuiElementAssert object for functional assertions
     * @deprecated Use {@link #asserts()} instead
     */
    @Deprecated
    public GuiElementAssert assertCollector() {
        if (collectableAssert==null) {
            GuiElementAssertFactory assertFactory = Testerra.injector.getInstance(GuiElementAssertFactory.class);
            CollectedAssertion assertion = Testerra.injector.getInstance(CollectedAssertion.class);
            collectableAssert = assertFactory.create(guiElementData, assertion, decoratedWait);
        }
        return collectableAssert;
    }

    /**
     * Provides access to all assert methods. If an assertion fails, the assertDescription will be given as cause,
     * instead of the technical cause like an isDisplayed error.
     *
     * @param errorMessage Cause returned on assertion error.
     *
     * @return GuiElementAssert object for functional assertions
     */
    @Deprecated
    public GuiElementAssert assertCollector(String errorMessage) {
        GuiElementAssertDescriptionDecorator guiElementAssertDescriptionDecorator
                = new GuiElementAssertDescriptionDecorator(errorMessage, assertCollector());
        return guiElementAssertDescriptionDecorator;
    }

    @Deprecated
    public List<GuiElement> getList() {
        List<GuiElement> guiElements = new ArrayList<>();
        list().forEach(guiElement -> guiElements.add((GuiElement)guiElement));
        return guiElements;
    }

    @Deprecated
    public GuiElement shadowRoot() {
        guiElementData.shadowRoot = true;
        return this;
    }

    /**
     * Provides access to all wait methods
     */
    @Deprecated
    public GuiElementWait waits() {
        return decoratedWait;
    }

    @Override
    public StringAssertion<String> tagName() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultStringAssertion.class, new AssertionProvider<String>() {
            @Override
            public String getActual() {
                return core.getTagName();
            }

            @Override
            public String getSubject() {
                return String.format("%s.@tagName", self);
            }
        });
    }

    @Override
    public StringAssertion<String> text() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultStringAssertion.class, new AssertionProvider<String>() {
            @Override
            public String getActual() {
                return core.getText();
            }

            @Override
            public String getSubject() {
                return String.format("%s.@text", self);
            }
        });
    }

    @Override
    public StringAssertion<String> value(String attribute) {
//        if (attribute.contains("-")) {
//            String parts[] = attribute.split("\\-+");
//            StringBuilder builder = new StringBuilder();
//            for (String part : parts) {
//                builder.append(StringUtils.capitalize(part));
//            }
//            attribute = builder.toString();
//        }
        final String finalAttribute = attribute;
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultStringAssertion.class, new AssertionProvider<String>() {
            @Override
            public String getActual() {
                return core.getAttribute(finalAttribute);
            }

            @Override
            public String getSubject() {
                return String.format("%s.@%s", self, finalAttribute);
            }
        });
    }

    @Override
    public StringAssertion<String> css(String property) {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultStringAssertion.class, new AssertionProvider<String>() {
            @Override
            public String getActual() {
                return getCssValue(property);
            }

            @Override
            public String getSubject() {
                return String.format("%s.css(@%s)", self, property);
            }
        });
    }

    @Override
    public BinaryAssertion<Boolean> present() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultBinaryAssertion.class, new AssertionProvider<Boolean>() {
            @Override
            public Boolean getActual() {
                try {
                    return core.findWebElement()!=null;
                } catch (ElementNotFoundException e) {
                    return false;
                }
            }

            @Override
            public String getSubject() {
                return String.format("%s.@present", self);
            }
        });
    }

    @Override
    public BinaryAssertion<Boolean> visible(boolean complete) {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultBinaryAssertion.class, new AssertionProvider<Boolean>() {
            @Override
            public Boolean getActual() {
                return core.isVisible(complete);
            }

            @Override
            public String getSubject() {
                return String.format("%s.visible(complete: %s)", self, complete);
            }
        });
    }

    @Override
    public BinaryAssertion<Boolean> displayed() {
        final UiElement self = this;
        BinaryAssertion<Boolean> prop = propertyAssertionFactory.create(DefaultBinaryAssertion.class, new AssertionProvider<Boolean>() {
            @Override
            public Boolean getActual() {
                try {
                    return core.isDisplayed();
                } catch (ElementNotFoundException e) {
                    return false;
                }
            }

            @Override
            public String getSubject() {
                return String.format("%s.@displayed", self);
            }
        });
        return prop;
    }

    @Override
    public BinaryAssertion<Boolean> enabled() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultBinaryAssertion.class, new AssertionProvider<Boolean>() {
            @Override
            public Boolean getActual() {
                return core.isEnabled();
            }

            @Override
            public String getSubject() {
                return String.format("%s.@enabled", self);
            }
        });
    }

    @Override
    public BinaryAssertion<Boolean> selected() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultBinaryAssertion.class, new AssertionProvider<Boolean>() {
            @Override
            public Boolean getActual() {
                return core.isSelected();
            }

            @Override
            public String getSubject() {
                return String.format("%s.@selected", self);
            }
        });
    }

    @Override
    public RectAssertion bounds() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultRectAssertion.class, new AssertionProvider<Rectangle>() {
            @Override
            public Rectangle getActual() {
                return core.getRect();
            }

            @Override
            public String getSubject() {
                return String.format("%s.bounds", self);
            }
        });
    }

    @Override
    public QuantityAssertion<Integer> numberOfElements() {
        final UiElement self = this;
        return propertyAssertionFactory.create(DefaultQuantityAssertion.class, new AssertionProvider<Integer>() {
            @Override
            public Integer getActual() {
                try {
                    return core.getNumberOfFoundElements();
                } catch (ElementNotFoundException e) {
                    return 0;
                }
            }

            @Override
            public String getSubject() {
                return String.format("%s.@numberOfElements", self);
            }
        });
    }

    @Override
    public UiElementAssertions waitFor() {
        propertyAssertionFactory.shouldWait();
        return this;
    }

    @Override
    public String createXPath() {
        Formatter formatter = Testerra.injector.getInstance(Formatter.class);
        ArrayList<String> xPathes = new ArrayList<>();
        HasParent element = this;
        do {
            if (element instanceof UiElement) {
                xPathes.add(0, formatter.byToXPath(((UiElement) element).getLocate().getBy()));
            }
            element = element.getParent();
        } while (element instanceof BasicUiElement);

        return String.join("", xPathes);
    }

    @Override
    public ImageAssertion screenshot() {
        final UiElement self = this;
        final AtomicReference<File> screenshot = new AtomicReference<>();
        screenshot.set(core.takeScreenshot());
        return propertyAssertionFactory.create(DefaultImageAssertion.class, new AssertionProvider<File>() {
            @Override
            public File getActual() {
                return screenshot.get();
            }

            @Override
            public void failed(PropertyAssertion assertion) {
                screenshot.set(core.takeScreenshot());
            }

            @Override
            public String getSubject() {
                return String.format("%s.screenshot", self);
            }
        });
    }

    /**
     * Calls isDisplayed on the underlying WebElement.
     *
     * @return isDisplayed from WebElement
     * @deprecated Use {@link #isDisplayed()} instead
     */
    @Deprecated
    public boolean isDisplayedFromWebElement() {
        return this.isDisplayed();
    }
}
