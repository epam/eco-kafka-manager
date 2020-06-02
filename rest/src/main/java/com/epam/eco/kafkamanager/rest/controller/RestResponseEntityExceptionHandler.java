/*
 * Copyright 2020 EPAM Systems
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
package com.epam.eco.kafkamanager.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.epam.eco.kafkamanager.AlreadyExistsException;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.rest.response.MessageResponse;

/**
 * @author Raman_Babich
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    private static final String INTERNAL_ISSUE_MESSAGE =
            "Some server side issues. Please retry again later or contact support team.";
    private static final String DEFAULT_ISSUE_MESSAGE =
            "Something went wrong. Please retry again later or contact support team.";

    @ExceptionHandler({NotFoundException.class})
    protected ResponseEntity<Object> notFound(NotFoundException ex, WebRequest request) {
        return handle(
                ex,
                MessageResponse.with(latestNotBlankMessageOrDefault(ex)),
                request,
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AlreadyExistsException.class})
    protected ResponseEntity<Object> alreadyExists(AlreadyExistsException ex, WebRequest request) {
        return handle(
                ex,
                MessageResponse.with(latestNotBlankMessageOrDefault(ex)),
                request,
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class}) // TODO: create better exception handling
    protected ResponseEntity<Object> unpocessableEntity(RuntimeException ex, WebRequest request) {
        return handle(
                ex,
                MessageResponse.with(latestNotBlankMessageOrDefault(ex)),
                request,
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({RuntimeException.class})
    protected ResponseEntity<Object> internalServerError(RuntimeException ex, WebRequest request) {
        LOGGER.error(ExceptionUtils.getStackTrace(ex));
        return handle(
                ex,
                MessageResponse.with(INTERNAL_ISSUE_MESSAGE),
                request,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> handle(RuntimeException ex, Object body, WebRequest request, HttpStatus status) {
        return handleExceptionInternal(
                ex,
                body,
                new HttpHeaders(),
                status,
                request);
    }

    private String latestNotBlankMessageOrDefault(Throwable ex) {
        Throwable current = ex;
        do {
            if (!StringUtils.isBlank(current.getMessage())) {
                return current.getMessage();
            }
            current = current.getCause();
        } while (current != null);
        return DEFAULT_ISSUE_MESSAGE;
    }

}
