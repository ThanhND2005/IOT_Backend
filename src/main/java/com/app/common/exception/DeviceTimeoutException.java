package com.app.common.exception;

/**
 * Exception thrown when an IoT device does not respond within the expected timeout period.
 */
public class DeviceTimeoutException extends AppException {

    public DeviceTimeoutException(String message) {
        super(ErrorCode.DEVICE_TIMEOUT, message);
    }

    public DeviceTimeoutException() {
        super(ErrorCode.DEVICE_TIMEOUT);
    }
}
