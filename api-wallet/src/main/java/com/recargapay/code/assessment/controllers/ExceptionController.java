package com.recargapay.code.assessment.controllers;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.recargapay.code.assessment.ports.exceptions.ApiError;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.ForBiddenException;
import com.recargapay.code.assessment.ports.exceptions.JwtHeltperException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class ExceptionController {
	


	@ExceptionHandler({WalletException.class,JwtHeltperException.class})
	public ResponseEntity<ApiError> getInternalServerErro(WalletException exception, HttpServletRequest request) {
		ApiError error = new ApiError();
		error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		error.setMsg("An internal server error occurred while processing the wallet request.");
		var origin = getOriginException(exception);
		log.error("Handled WalletExcepetion (Internal Sever error) in {}:{} - {}", 
                origin.getClassName(), 
                origin.getLineNumber(), 
                exception.getMessage(),
                exception
            );
		
	    error.setPath(request.getRequestURI());
	    error.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}


	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Void> getNotFoundException(NotFoundException exception) {
		
		var origin = getOriginException(exception);
		log.info("VEIO AQUI");
		log.info("Handled NotFoundException in {}:{} - {}", 
                origin.getClassName(), 
                origin.getLineNumber(), 
                exception.getMessage()
            );
		return ResponseEntity.notFound().build();
	}

	

	
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> getConflict(ConflictException exception) {
    	ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setMsg(exception.getMessage());
        error.setCode(HttpStatus.CONFLICT.value());
        var origin = getOriginException(exception);
        
        log.warn("Handled ConflictException in {}:{} - {}", 
                origin.getClassName(), 
                origin.getLineNumber(), 
                exception.getMessage()
            );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    
    private StackTraceElement getOriginException(Exception ex) {
    	return ex.getStackTrace()[0];
    }


}
