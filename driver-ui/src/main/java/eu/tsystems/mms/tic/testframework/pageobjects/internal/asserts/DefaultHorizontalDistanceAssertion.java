/*
 * Testerra
 *
 * (C) 2020, Mike Reiche, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 */

package eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts;

import eu.tsystems.mms.tic.testframework.pageobjects.TestableUiElement;
import org.openqa.selenium.Rectangle;

/**
 * Default implementation of {@link HorizontalDistanceAssertion}
 * @author Mike Reiche
 */
public class DefaultHorizontalDistanceAssertion extends AbstractPropertyAssertion<Integer> implements HorizontalDistanceAssertion {

    public DefaultHorizontalDistanceAssertion(AbstractPropertyAssertion<Integer> parentAssertion, AssertionProvider<Integer> provider) {
        super(parentAssertion, provider);
    }

    @Override
    public QuantityAssertion<Integer> toRightOf(TestableUiElement guiElement) {
        return propertyAssertionFactory.createWithParent(DefaultQuantityAssertion.class, this, new AssertionProvider<Integer>() {
            @Override
            public Integer getActual() {
                Rectangle referenceRect = guiElement.waitFor().bounds().getActual();
                return provider.getActual()-(referenceRect.x+referenceRect.width);
            }

            @Override
            public String getSubject() {
                return String.format("toRightOf(%s)", guiElement);
            }
        });
    }

    @Override
    public QuantityAssertion<Integer> toLeftOf(TestableUiElement guiElement) {
        return propertyAssertionFactory.createWithParent(DefaultQuantityAssertion.class, this, new AssertionProvider<Integer>() {
            @Override
            public Integer getActual() {
                Rectangle referenceRect = guiElement.waitFor().bounds().getActual();
                return provider.getActual()-referenceRect.x;
            }

            @Override
            public String getSubject() {
                return String.format("toLeftOf(%s)", guiElement);
            }
        });
    }
}
