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
package eu.tsystems.mms.tic.testframework.utils;

import eu.tsystems.mms.tic.testframework.exceptions.FennecSystemException;

import java.util.Collections;
import java.util.List;

/**
 * Created by clgr on 09.12.2014.
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    /**
     * generate the average value of all value for the given List
     * @param numberList
     * @return
     */
    public static Long getAverageValue(List<Long> numberList) {
        if (numberList == null) {
            throw new FennecSystemException("List is null");
        }

        int size = numberList.size();
        if (size == 0) {
            return 0L;
        }

        Long cummulatedValue = 0L;
        for (Long number : numberList) {
            cummulatedValue += number;
        }
        Long averageValueOfList = cummulatedValue / size;
        return averageValueOfList;
    }

    /**
     * finds the max value in the given List
     * @param numberList
     * @return
     */
    public static <T extends Object & Comparable<? super T>> T getMaxValue(List<T> numberList) {
        if (numberList == null) {
            throw new FennecSystemException("List is null");
        }

        int size = numberList.size();
        if (size == 0) {
            return null;
        }

        T maxValue = Collections.max(numberList);
        return maxValue;
    }

    /**
     * find the min value in the given List
     * @param numberList
     * @return
     */
    public static <T extends Object & Comparable<? super T>> T getMinValue(List<T> numberList) {
        if (numberList == null) {
            throw new FennecSystemException("List is null");
        }

        int size = numberList.size();
        if (size == 0) {
            return null;
        }

        T minValue = Collections.min(numberList);
        return minValue;
    }
}
