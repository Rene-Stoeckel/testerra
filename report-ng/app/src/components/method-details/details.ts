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

/**
 * Optimized highlight.js import
 * @see https://github.com/highlightjs/highlight.js#es6-modules
 */
import hljs from 'highlight.js/lib/core';
import java from 'highlight.js/lib/languages/java';
import 'highlight.js/styles/darcula.css';
import {autoinject} from 'aurelia-framework';
import {MethodDetails, StatisticsGenerator} from "services/statistics-generator";
import {FailureAspectStatistics} from "services/statistic-models";
import {Config} from "services/config-dev";
import {NavigationInstruction, RouteConfig} from "aurelia-router";
import {StatusConverter} from "services/status-converter";
import {ScreenshotComparison} from "../screenshot-comparison/screenshot-comparison";
import {MdcDialogService} from '@aurelia-mdc-web/dialog';
import pixelmatch from 'pixelmatch';

export interface ILayoutComparisonContext {
    name: string,
    image, mode, distance, actualScreenshot, annotatedScreenshot, distanceScreenshot, expectedScreenshot
}

@autoinject()
export class Details {
    private _hljs = hljs;
    private _failureAspect: FailureAspectStatistics;
    private _methodDetails: MethodDetails;
    private _images: any
    private _layoutComparison: ILayoutComparisonContext;

    constructor(
        private _statistics: StatisticsGenerator,
        private _config: Config,
        private _statusConverter: StatusConverter,
        private _dialogService: MdcDialogService
    ) {
        this._hljs.registerLanguage("java", java);
    }

    activate(
        params: any,
        routeConfig: RouteConfig,
        navInstruction: NavigationInstruction
    ) {
        this._statistics.getMethodDetails(params.methodId).then(methodDetails => {
            this._methodDetails = methodDetails;
            this._layoutComparison = methodDetails.customContexts.find(value => {
                /**
                 * @todo @HNJO Please find the proper layout comparison context from the list of contexts
                 */
                // return value.name == "LayoutComparison";
                return true;
            })
            if (this._layoutComparison) {
                this._prepareComparison();
            }
            if (methodDetails.methodContext.errorContext) {
                this._failureAspect = new FailureAspectStatistics().setErrorContext(methodDetails.methodContext.errorContext);
            }
        });
    }

    private _prepareComparison() {
        this._images = {
            actual: {
                src: 'screenshots/' + this._layoutComparison.actualScreenshot.filename,
                title: "Actual screenshot"
            },
            comparison: {
                src: "",
                title: "comparison"
            },
            expected: {
                src: 'screenshots/' + this._layoutComparison.expectedScreenshot.filename,
                title: "Expected screenshot"
            }
        }

        this._loadImages(this._images).then(images => {
            const canvas: HTMLCanvasElement = document.createElement("canvas");
            const maxWidth = Math.max(images[0].width, images[1].width);
            const maxHeight = Math.max(images[0].height, images[1].height);

            //get Image data of actual screenshot via canvas
            canvas.width = maxWidth;
            canvas.height = maxHeight;
            let canvasContext = canvas.getContext("2d");
            canvasContext.drawImage(images[0], 0, 0);
            const imgData1 = canvasContext.getImageData(0, 0, maxWidth, maxHeight);

            //get Image data of expected screenshot via canvas
            canvasContext = canvas.getContext("2d");
            canvasContext.clearRect(0, 0, canvas.width, canvas.height);
            canvasContext.drawImage(images[1], 0, 0);
            const imgData2 = canvasContext.getImageData(0, 0, maxWidth, maxHeight);
            const diff = canvasContext.createImageData(maxWidth, maxHeight);

            // @ts-ignore
            pixelmatch(imgData1.data, imgData2.data, diff.data, maxWidth, maxHeight, {threshold: 0.2, includeAA: true, alpha: 0.9, diffColor:[246, 168, 33]});

            canvasContext = canvas.getContext("2d");
            canvasContext.putImageData(diff, 0, 0);
            this._images.comparison.src = canvas.toDataURL();
        })
    }

    private async _loadImages(images: Array<any>) {
        //asynchronous function to ensure images are loaded before continuing
        const promiseArray = []; // create an array for promises
        const imageArray = [];

        promiseArray.push(new Promise(resolve => {
            let img1 = new Image();

            img1.onload = resolve;

            img1.src = this._images.actual.src
            imageArray[0] = img1;
        }));

        promiseArray.push(new Promise(resolve => {
            let img2 = new Image();

            img2.onload = resolve;

            img2.src = this._images.expected.src
            imageArray[1] = img2;
        }));

        await Promise.all(promiseArray); // wait for all the images to be loaded

        return imageArray;
    }

    private _imageClicked() {
        this._dialogService.open({
            viewModel: ScreenshotComparison,
            model: {
                actual: this._images.actual,
                expected: this._images.comparison,
                comparison: this._images.expected
            },
            class: "screenshot-comparison"
        });
    }
}
