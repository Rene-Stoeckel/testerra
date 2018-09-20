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
package eu.tsystems.mms.tic.testframework.exceptions;

/**
 * Created by peter on 16.06.14.
 */
public class SequenceTimeoutException extends Exception {

    /**
     *
     */
    public SequenceTimeoutException() {
        this("Timeout");
    }

    /**
     *
     * @param message .
     */
    public SequenceTimeoutException(String message) {
        super(message);
    }

    /**
     *
     * @param message .
     * @param cause .
     */
    public SequenceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause .
     */
    public SequenceTimeoutException(Throwable cause) {
        super(cause);
    }

    /**
     *
     * @param message .
     * @param cause .
     * @param enableSuppression .
     * @param writableStackTrace .
     */
    public SequenceTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}