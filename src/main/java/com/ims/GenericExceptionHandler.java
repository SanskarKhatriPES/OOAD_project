package com.ims;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ims.utils.ExceptionUtils;

import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GenericExceptionHandler {
	
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler()
	public Map<String, Object> handleValidationExceptions(
	  Exception ex) {
	    
		if (ex instanceof MethodArgumentNotValidException) {
			return ExceptionUtils.getErrorMap(((MethodArgumentNotValidException) ex).getBindingResult(), null);
		}
		
	    return ExceptionUtils.getErrorMap(null, ex);
	}
}
