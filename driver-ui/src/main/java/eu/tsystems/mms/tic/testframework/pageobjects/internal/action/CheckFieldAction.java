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
 package eu.tsystems.mms.tic.testframework.pageobjects.internal.action;

import eu.tsystems.mms.tic.testframework.pageobjects.AbstractPage;
import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.Checkable;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.lang.reflect.Modifier;

public abstract class CheckFieldAction extends FieldAction {
    /**
     * Readable message template.
     * Wehere ### is guiElement and *** is simpleClassName
     */
    private static final String msgGuiElementNotFoundTemplate = "***: Mandatory GuiElement >###< was not found";
    private static final String msgGuiElementFoundTemplate = "***: Mandatory GuiElement >###< was found, but expected to be NOT";

    protected final boolean findNot;
    private final boolean fast;
    protected String readableMessage;

    protected Checkable checkableInstance;

    public CheckFieldAction(FieldWithActionConfig fieldWithActionConfig, AbstractPage declaringPage) {
        super(fieldWithActionConfig.field, declaringPage);
        this.fast = fieldWithActionConfig.fast;
        this.findNot = fieldWithActionConfig.findNot;

        String simpleClassName = declaringPage.getClass().getSimpleName();
        if (findNot) {
            readableMessage = msgGuiElementFoundTemplate.replace("###", fieldName).replace("***", simpleClassName);
        } else {
            readableMessage = msgGuiElementNotFoundTemplate.replace("###", fieldName).replace("***", simpleClassName);
        }
    }

    protected abstract void checkField(Check check, boolean fast);
    protected boolean execute = false;

    @Override
    public boolean before() {
        boolean isCheckAnnotated = field.isAnnotationPresent(Check.class);
        boolean isCheckable = Checkable.class.isAssignableFrom(typeOfField);

        if (isCheckAnnotated && !isCheckable) {
            throw new RuntimeException("Field " + fieldName + " in " + declaringClass.getCanonicalName()
                    + " is annotated with @Check, but the class is not checkable. A class is checkable, if it " +
                    "implements the marker interface " + Checkable.class.getCanonicalName());
        }

        if (isCheckable) {
            if (((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
                // if the Check annotated field is not static
                throw new RuntimeException("Checkable field MUST be non-static: " +
                        declaringClass.getCanonicalName() + "." + field.getName());
            }
            else {
                if (isCheckAnnotated) {
                    execute = true;
                }
            }
        }

        additionalBeforeCheck();
        return execute;
    }

    protected abstract void additionalBeforeCheck();

    @Override
    public void execute() {
        try {
            checkableInstance = (Checkable) field.get(declaringPage);
        } catch (IllegalAccessException e) {
            logger.error("Internal Error", e);
            return;
        } catch (IllegalArgumentException e) {
            logger.error("Internal Error. Maybe tried to get field from object that does not declare it.", e);
            return;
        }
        Check check = field.getAnnotation(Check.class);
        if (checkableInstance == null) {
            throw new RuntimeException("Field " + fieldName + " in " + declaringClass.getCanonicalName()
                    + " is annotated with @Check and was never initialized (it is null). This is not allowed" +
                    " because @Check indicates a mandatory GuiElement of a Page.");
        } else {
            logger.debug("Looking for GuiElement on " + declaringClass.getSimpleName() + ": " + fieldName
                    + " with locator " + checkableInstance.toString());
            try {
                checkField(check, fast);
            } catch (Throwable t) {
//                MethodContext methodContext = ExecutionContextController.getCurrentMethodContext();
//                if (methodContext != null && t.getMessage() != null) {
//                    methodContext.getErrorContext().setThrowable(t.getMessage(), t);
//                }
                throw t;
            }
        }
    }

    @Override
    public void after() {

    }
}
