package eu.tsystems.mms.tic.testframework.pageobjects.internal;

import eu.tsystems.mms.tic.testframework.pageobjects.TestableUiElement;

/**
 * All interactions that can be performed on a GuiElement
 * @author Mike Reiche
 */
public interface UiElementActions extends TestableUiElement {
    /**
     * Select/Deselect a selectable element.
     */
    default UiElementActions select(boolean select) {
        if (select) {
            return select();
        } else {
            return deselect();
        }
    }
    UiElementActions click();
    UiElementActions doubleClick();
    UiElementActions rightClick();
    UiElementActions select();
    UiElementActions deselect();
    UiElementActions sendKeys(CharSequence... charSequences);
    UiElementActions clear();
    UiElementActions hover();
    /**
     * This method scrolls to the element with an given offset.
     */
    UiElementActions scrollTo(int yOffset);

    default UiElementActions scrollTo() {
        return scrollTo(0);
    }

    //InteractiveGuiElement clickJS();
    //InteractiveGuiElement doubleClickJS();
    //InteractiveGuiElement rightClickJS();
    //InteractiveGuiElement mouseOver();
    //InteractiveGuiElement mouseOverJS();
}