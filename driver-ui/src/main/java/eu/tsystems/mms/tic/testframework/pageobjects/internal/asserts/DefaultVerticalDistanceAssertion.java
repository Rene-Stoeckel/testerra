package eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts;

import eu.tsystems.mms.tic.testframework.pageobjects.TestableUiElement;
import org.openqa.selenium.Rectangle;

public class DefaultVerticalDistanceAssertion extends AbstractPropertyAssertion<Integer> implements VerticalDistanceAssertion {

    public DefaultVerticalDistanceAssertion(PropertyAssertion<Integer> parentAssertion, AssertionProvider<Integer> provider) {
        super(parentAssertion, provider);
    }

    @Override
    public QuantityAssertion<Integer> toTopOf(TestableUiElement guiElement) {
        return propertyAssertionFactory.create(DefaultQuantityAssertion.class, this, new AssertionProvider<Integer>() {
            @Override
            public Integer getActual() {
                Rectangle referenceRect = guiElement.bounds().getActual();
                return provider.getActual()-referenceRect.y;
            }

            @Override
            public String getSubject() {
                return String.format("toTopOf(guiElement: %s)", guiElement);
            }
        });
    }

    @Override
    public QuantityAssertion<Integer> toBottomOf(TestableUiElement guiElement) {
        return propertyAssertionFactory.create(DefaultQuantityAssertion.class, this, new AssertionProvider<Integer>() {
            @Override
            public Integer getActual() {
                Rectangle referenceRect = guiElement.bounds().getActual();
                return provider.getActual()-(referenceRect.y+referenceRect.height);
            }

            @Override
            public String getSubject() {
                return String.format("toBottomOf(guiElement: %s)", guiElement);
            }
        });
    }
}