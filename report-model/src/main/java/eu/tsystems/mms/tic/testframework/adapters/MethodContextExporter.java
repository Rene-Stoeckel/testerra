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
package eu.tsystems.mms.tic.testframework.adapters;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import eu.tsystems.mms.tic.testframework.internal.IDUtils;
import eu.tsystems.mms.tic.testframework.report.model.ErrorContext;
import eu.tsystems.mms.tic.testframework.report.model.FailureCorridorValue;
import eu.tsystems.mms.tic.testframework.report.model.File;
import eu.tsystems.mms.tic.testframework.report.model.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.MethodType;
import eu.tsystems.mms.tic.testframework.report.model.PClickPathEvent;
import eu.tsystems.mms.tic.testframework.report.model.PClickPathEventType;
import eu.tsystems.mms.tic.testframework.report.model.PTestStep;
import eu.tsystems.mms.tic.testframework.report.model.PTestStepAction;
import eu.tsystems.mms.tic.testframework.report.model.ScriptSource;
import eu.tsystems.mms.tic.testframework.report.model.ScriptSourceLine;
import eu.tsystems.mms.tic.testframework.report.model.StackTraceCause;
import eu.tsystems.mms.tic.testframework.report.model.context.Cause;
import eu.tsystems.mms.tic.testframework.report.model.context.CustomContext;
import eu.tsystems.mms.tic.testframework.report.model.context.report.Report;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStepAction;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MethodContextExporter extends AbstractContextExporter {
    private final Report report = new Report();
    private final Gson jsonEncoder = new Gson();
    private final java.io.File targetVideoDir = report.getFinalReportDirectory(Report.VIDEO_FOLDER_NAME);
    private final java.io.File targetScreenshotDir = report.getFinalReportDirectory(Report.SCREENSHOTS_FOLDER_NAME);
    private final java.io.File currentVideoDir = report.getFinalReportDirectory(Report.VIDEO_FOLDER_NAME);
    private final java.io.File currentScreenshotDir = report.getFinalReportDirectory(Report.SCREENSHOTS_FOLDER_NAME);
    private Consumer<File.Builder> fileConsumer;

    public MethodContextExporter setFileConsumer(Consumer<File.Builder> fileConsumer) {
        this.fileConsumer = fileConsumer;
        return this;
    }

    private static String annotationToString(Annotation annotation) {
        String json = "\"" + annotation.annotationType().getSimpleName() + "\"";
        json += " : { ";

        Method[] methods = annotation.annotationType().getMethods();
        List<String> params = new LinkedList<>();
        for (Method method : methods) {
            if (method.getDeclaringClass() == annotation.annotationType()) { //this filters out built-in methods, like hashCode etc
                try {
                    params.add("\"" + method.getName() + "\" : \"" + method.invoke(annotation) + "\"");
                } catch (Exception e) {
                    params.add("\"" + method.getName() + "\" : \"---error---\"");
                }
            }
        }
        json += String.join(", ", params);

        json += " }";
        return json;
    }

    private String mapArtifactsPath(String absolutePath) {
        String path = absolutePath.replace(report.getFinalReportDirectory().toString(), "");

        // replace all \ with /
        path = path.replaceAll("\\\\", "/");

        // remove leading /
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    public MethodContext.Builder prepareMethodContext(eu.tsystems.mms.tic.testframework.report.model.context.MethodContext methodContext) {
        MethodContext.Builder builder = MethodContext.newBuilder();

        apply(createContextValues(methodContext), builder::setContextValues);
        map(methodContext.getMethodType(), type -> MethodType.valueOf(type.name()), builder::setMethodType);
        forEach(methodContext.parameters, parameter -> builder.addParameters(parameter.toString()));
        forEach(methodContext.methodTags, annotation -> builder.addMethodTags(MethodContextExporter.annotationToString(annotation)));
        apply(methodContext.retryNumber, builder::setRetryNumber);
        apply(methodContext.methodRunIndex, builder::setMethodRunIndex);

        apply(methodContext.priorityMessage, builder::setPriorityMessage);
        apply(methodContext.threadName, builder::setThreadName);

        // test steps
        methodContext.readTestSteps().forEach(testStep -> builder.addTestSteps(prepareTestStep(testStep)));
        //value(methodContext.failedStep, MethodContextExporter::createPTestStep, builder::setFailedStep);

        map(methodContext.failureCorridorValue, value -> FailureCorridorValue.valueOf(value.name()), builder::setFailureCorridorValue);
        builder.setClassContextId(methodContext.getClassContext().getId());

        forEach(methodContext.infos, builder::addInfos);
        methodContext.readRelatedMethodContexts().forEach(m -> builder.addRelatedMethodContextIds(m.getId()));
        methodContext.readDependsOnMethodContexts().forEach(m -> builder.addDependsOnMethodContextIds(m.getId()));

        // build context
        if (methodContext.hasErrorContext()) builder.setErrorContext(this.prepareErrorContext(methodContext.getErrorContext()));
        methodContext.readSessionContexts().forEach(sessionContext -> builder.addSessionContextIds(sessionContext.getId()));

        List<CustomContext> customContexts = methodContext.readCustomContexts().collect(Collectors.toList());
        if (customContexts.size()>0) {
            builder.setCustomContextJson(jsonEncoder.toJson(customContexts));
        }

        methodContext.readVideos().forEach(video -> {
            final java.io.File targetVideoFile = new java.io.File(targetVideoDir, video.filename);
            final java.io.File currentVideoFile = new java.io.File(currentVideoDir, video.filename);

            final String videoId = IDUtils.getB64encXID();

            // link file
            builder.addVideoIds(videoId);

            // add video data
            final File.Builder fileBuilderVideo = File.newBuilder();
            fileBuilderVideo.setId(videoId);
            fileBuilderVideo.setRelativePath(targetVideoFile.getPath());
            fileBuilderVideo.setMimetype(MediaType.WEBM_VIDEO.toString());
            fillFileBasicData(fileBuilderVideo, currentVideoFile);
            this.fileConsumer.accept(fileBuilderVideo);
        });

        return builder;
    }

    private void fillFileBasicData(File.Builder builder, java.io.File file) {
        // timestamps
        long timestamp = System.currentTimeMillis();
        builder.setCreatedTimestamp(timestamp);
        builder.setLastModified(timestamp);

        // file size
        builder.setSize(file.length());
    }
//
//    public StackTrace.Builder prepareStackTrace(eu.tsystems.mms.tic.testframework.report.model.context.StackTrace stackTrace) {
//        StackTrace.Builder builder = StackTrace.newBuilder();
//
//        //apply(stackTrace.additionalErrorMessage, builder::setAdditionalErrorMessage);
//        map(stackTrace.stackTrace, this::prepareStackTraceCause, builder::setCause);
//
//        return builder;
//    }

    public StackTraceCause.Builder prepareStackTraceCause(Cause cause) {
        StackTraceCause.Builder builder = StackTraceCause.newBuilder();

        apply(cause.getClassName(), builder::setClassName);
        apply(cause.getMessage(), builder::setMessage);
        apply(cause.getStackTraceElements(), builder::addAllStackTraceElements);
        map(cause.getCause(), this::prepareStackTraceCause, builder::setCause);

        return builder;
    }

    public ScriptSource.Builder prepareScriptSource(eu.tsystems.mms.tic.testframework.report.model.context.ScriptSource scriptSource) {
        ScriptSource.Builder builder = ScriptSource.newBuilder();

        apply(scriptSource.fileName, builder::setFileName);
        apply(scriptSource.methodName, builder::setMethodName);
        forEach(scriptSource.lines, line -> builder.addLines(prepareScriptSourceLine(line)));

        return builder;
    }

    public ScriptSourceLine.Builder prepareScriptSourceLine(eu.tsystems.mms.tic.testframework.report.model.context.ScriptSource.Line line) {
        ScriptSourceLine.Builder builder = ScriptSourceLine.newBuilder();

        apply(line.line, builder::setLine);
        apply(line.lineNumber, builder::setLineNumber);
        apply(line.mark, builder::setMark);

        return builder;
    }

    public ErrorContext.Builder prepareErrorContext(eu.tsystems.mms.tic.testframework.report.model.context.ErrorContext errorContext) {
        ErrorContext.Builder builder = ErrorContext.newBuilder();

//        apply(errorContext.getReadableErrorMessage(), builder::setReadableErrorMessage);
//        apply(errorContext.getAdditionalErrorMessage(), builder::setAdditionalErrorMessage);
        errorContext.getCause().ifPresent(cause -> builder.setCause(prepareStackTraceCause(cause)));
//        apply(errorContext.errorFingerprint, builder::setErrorFingerprint);
        errorContext.getScriptSource().ifPresent(scriptSource -> builder.setScriptSource(this.prepareScriptSource(scriptSource)));
        errorContext.getExecutionObjectSource().ifPresent(scriptSource -> builder.setExecutionObjectSource(this.prepareScriptSource(scriptSource)));
        if (errorContext.getTicketId() != null) builder.setTicketId(errorContext.getTicketId().toString());
        apply(errorContext.getDescription(), builder::setDescription);

        return builder;
    }

    public PTestStep.Builder prepareTestStep(TestStep testStep) {
        PTestStep.Builder builder = PTestStep.newBuilder();

        apply(testStep.getName(), builder::setName);
        forEach(testStep.getTestStepActions(), testStepAction -> builder.addTestStepActions(prepareTestStepAction(testStepAction)));

        return builder;
    }

    public PTestStepAction.Builder prepareTestStepAction(TestStepAction testStepAction) {
        PTestStepAction.Builder testStepBuilder = PTestStepAction.newBuilder();

        apply(testStepAction.getName(), testStepBuilder::setName);
        apply(testStepAction.getTimestamp(), testStepBuilder::setTimestamp);

        testStepAction.readClickPathEvents().forEach(clickPathEvent -> {
            PClickPathEvent.Builder clickPathBuilder = PClickPathEvent.newBuilder();
            switch (clickPathEvent.getType()) {
                case WINDOW:
                    clickPathBuilder.setType(PClickPathEventType.WINDOW);
                    break;
                case CLICK:
                    clickPathBuilder.setType(PClickPathEventType.CLICK);
                    break;
                case VALUE:
                    clickPathBuilder.setType(PClickPathEventType.VALUE);
                    break;
                case PAGE:
                    clickPathBuilder.setType(PClickPathEventType.PAGE);
                    break;
                case URL:
                    clickPathBuilder.setType(PClickPathEventType.URL);
                    break;
                default:
                    clickPathBuilder.setType(PClickPathEventType.NOT_SET);
            }
            clickPathBuilder.setSubject(clickPathEvent.getSubject());
            clickPathBuilder.setSessionId(clickPathEvent.getSessionId());
            testStepBuilder.addClickpathEvents(clickPathBuilder.build());
        });

        testStepAction.readScreenshots().forEach(screenshot -> {
            // build screenshot and sources files
            final java.io.File targetScreenshotFile = new java.io.File(targetScreenshotDir, screenshot.filename);
            final java.io.File currentScreenshotFile = new java.io.File(currentScreenshotDir, screenshot.filename);

            //final java.io.File realSourceFile = new java.io.File(Report.SCREENSHOTS_DIRECTORY, screenshot.sourceFilename);
            final java.io.File targetSourceFile = new java.io.File(targetScreenshotDir, screenshot.filename);
            final java.io.File currentSourceFile = new java.io.File(currentScreenshotDir, screenshot.filename);
            final String mappedSourcePath = mapArtifactsPath(targetSourceFile.getAbsolutePath());

            final String screenshotId = IDUtils.getB64encXID();
            final String sourcesRefId = IDUtils.getB64encXID();

            // create ref link
            //builder.addScreenshotIds(screenshotId);

            // add screenshot data
            final File.Builder fileBuilderScreenshot = File.newBuilder();
            fileBuilderScreenshot.setId(screenshotId);
            fileBuilderScreenshot.setRelativePath(targetScreenshotFile.getPath());
            fileBuilderScreenshot.setMimetype(MediaType.PNG.toString());
            fileBuilderScreenshot.putAllMeta(screenshot.meta());
            fileBuilderScreenshot.putMeta("sourcesRefId", sourcesRefId);
            fillFileBasicData(fileBuilderScreenshot, currentScreenshotFile);
            this.fileConsumer.accept(fileBuilderScreenshot);

            // add sources data
            final File.Builder fileBuilderSources = File.newBuilder();
            fileBuilderSources.setId(sourcesRefId);
            fileBuilderSources.setRelativePath(mappedSourcePath);
            fileBuilderSources.setMimetype(MediaType.PLAIN_TEXT_UTF_8.toString());
            fillFileBasicData(fileBuilderSources, currentSourceFile);
            this.fileConsumer.accept(fileBuilderSources);

            testStepBuilder.addScreenshotIds(screenshotId);
        });

        testStepAction.readLogEvents().forEach(logEvent -> {
            testStepBuilder.addLogMessages(prepareLogEvent(logEvent));
        });

        testStepAction.readOptionalAssertions().forEach(assertionInfo -> {
            testStepBuilder.addOptionalAssertions(prepareErrorContext(assertionInfo));
        });

        testStepAction.readCollectedAssertions().forEach(assertionInfo -> {
            testStepBuilder.addCollectedAssertions(prepareErrorContext(assertionInfo));
        });

        return testStepBuilder;
    }
}
