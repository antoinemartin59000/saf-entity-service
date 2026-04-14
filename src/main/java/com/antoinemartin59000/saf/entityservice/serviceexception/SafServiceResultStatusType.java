package com.antoinemartin59000.saf.entityservice.serviceexception;

public enum SafServiceResultStatusType {

    /**
     * For errors equivalent to HTTP 2XX
     */
    SUCCESSFUL,

    /**
     * For errors equivalent to HTTP 4XX
     */
    USER_ERROR,

    /**
     * For errors equivalent to HTTP 5XX
     */
    SOFTWARE_ERROR;

}
