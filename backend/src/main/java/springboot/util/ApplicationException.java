/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.util;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author TGMaster
 */
@ResponseStatus
public class ApplicationException extends RuntimeException {

    private int errorCode = 0;
    private String description;

    public ApplicationException(APIStatus apiStatus) {
        super(apiStatus.getDescription());
        setAPIStatus(apiStatus);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    public ApplicationException(APIStatus apiStatus, String message) {
        super(message);
        setAPIStatus(apiStatus);
    }

    public ApplicationException(APIStatus apiStatus, Throwable cause) {
        super(apiStatus.getDescription(), cause);
        setAPIStatus(apiStatus);
    }

    public ApplicationException(APIStatus apiStatus, String message, Throwable cause) {
        super(message, cause);
        setAPIStatus(apiStatus);
    }

    private void setAPIStatus(APIStatus apiStatus) {
        this.errorCode = apiStatus.getCode();
        this.description = apiStatus.getDescription();
    }
}
