/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2018, ..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Peter Lehmann <p.lehmann@t-systems.com>
 *     pele <p.lehmann@t-systems.com>
 */
package eu.tsystems.mms.tic.testframework.sikuli.support;

import eu.tsystems.mms.tic.testframework.sikuli.SikuliFirefoxDriver;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import java.lang.reflect.Field;

/**
 *
 */
class DefaultImageElementLocatorFactory implements ImageElementLocatorFactory {

    private final SikuliFirefoxDriver driver;

    public DefaultImageElementLocatorFactory(SikuliFirefoxDriver driverRef) {
        driver = driverRef;
    }

    @Override
    public ImageElementLocator createImageElementLocator(Field field) {
        return new DefaultImageElementLocator(driver, field);
    }

    @Override
    public ElementLocator createLocator(Field field) {
        return new DefaultElementLocator(driver, field);
    }

}
