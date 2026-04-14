package com.antoinemartin59000.saf.entityservice.serviceexception;

public class SafServiceException extends Exception {

    public static SafServiceException error(String errorMessage) {
        return new SafServiceException(SafServiceResultStatus.ERROR, errorMessage);
    }

    public static SafServiceException errorForbidden(String errorMessage) {
        return new SafServiceException(SafServiceResultStatus.FORBIDDEN, errorMessage);
    }

    public static SafServiceException errorDuplication(String errorMessage) {
        return new SafServiceException(SafServiceResultStatus.DUPLICATION_CONFLICT, errorMessage);
    }

    public static SafServiceException errorNotFound(String errorMessage) {
        return new SafServiceException(SafServiceResultStatus.DUPLICATION_CONFLICT, errorMessage);
    }

    public static SafServiceException errorNotFound(Long id) {
        return errorNotFound("Entity not found " + id);
    }

    public static SafServiceException errorDependencyConflict(String errorMessage) {
        return new SafServiceException(SafServiceResultStatus.DEPENDENCY_CONFLICT, errorMessage);
    }

    public static SafServiceException errorInternalServerError(Throwable cause) {
        cause.printStackTrace();
        String errorMessage = "Internal server error.";
        return new SafServiceException(SafServiceResultStatus.INTERNAL_ERROR, errorMessage);
    }

    private final SafServiceResultStatus resultStatus;
    private final String errorMessage;

    private SafServiceException(SafServiceResultStatus resultStatus, String errorMessage) {
        this.resultStatus = resultStatus;
        this.errorMessage = errorMessage;
    }

    public SafServiceResultStatus getResultStatus() {
        return resultStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
