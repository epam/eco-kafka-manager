/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.ui.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class WorkerUtils {

    public static String getErrorMessageOrNullIfNoError(TaskResult<?> result) {
        return
                result != null && result.getError() != null ?
                ExceptionUtils.getRootCauseMessage(result.getError()) :
                null;
    }

    public static String getErrorMessageOrDefaultIfNoError(TaskResult<?> result, String defaultValue) {
        String error = getErrorMessageOrNullIfNoError(result);
        return error != null ? error : defaultValue;
    }

    public static String getErrorStackTraceOrNullIfNoError(TaskResult<?> result) {
        return
                result != null && result.getError() != null ?
                ExceptionUtils.getStackTrace(result.getError()) :
                null;
    }

}
