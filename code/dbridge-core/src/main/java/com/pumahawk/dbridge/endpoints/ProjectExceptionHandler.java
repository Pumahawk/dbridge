package com.pumahawk.dbridge.endpoints;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.pumahawk.dbridge.exceptions.ProjectException;

@ControllerAdvice
public class ProjectExceptionHandler {

  @ExceptionHandler({ SpelEvaluationException.class })
  public ResponseEntity<ErrorResponse> handleSpelExceptionEntity(final SpelEvaluationException ex) throws Exception {
    ProjectException exs = ExceptionUtils.throwableOfType(ex, ProjectException.class);
    if (exs != null) {
      return handleAccessDeniedException(exs);
    } else {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to wrap SpelEvaluationException", ex);
    }
  }
    
  @ExceptionHandler({ ProjectException.class })
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
    ProjectException ex) {
      ErrorResponse response = new ErrorResponse(ex.getResponseMessage());
      return new ResponseEntity<ErrorResponse>(response, new HttpHeaders(), ex.getStatusCode());
  }
    
}
