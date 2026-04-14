package com.antoinemartin59000.saf.entityservice.serviceexception;

public enum SafServiceResultStatus {

    /**
     * Equivalent to HTTP 200
     */
    OK(SafServiceResultStatusType.SUCCESSFUL),

    /**
     * Equivalent to HTTP 400
     */
    ERROR(SafServiceResultStatusType.USER_ERROR),

    /**
     * Equivalent to HTTP 401
     */
    UNAUTHORIZED(SafServiceResultStatusType.USER_ERROR),

    /**
     * Equivalent to HTTP 403
     */
    FORBIDDEN(SafServiceResultStatusType.USER_ERROR),

    /**
     * Equivalent to HTTP 404
     */
    NOT_FOUND(SafServiceResultStatusType.USER_ERROR),

    /**
     * Equivalent to HTTP 409 (CONFLICT)
     */
    DEPENDENCY_CONFLICT(SafServiceResultStatusType.USER_ERROR),
    DUPLICATION_CONFLICT(SafServiceResultStatusType.USER_ERROR),

    /**
     * Equivalent to HTTP 500
     */
    INTERNAL_ERROR(SafServiceResultStatusType.SOFTWARE_ERROR);

    private SafServiceResultStatusType type;

    SafServiceResultStatus(SafServiceResultStatusType type) {
        this.type = type;
    }

    public SafServiceResultStatusType getType() {
        return type;
    }

}
