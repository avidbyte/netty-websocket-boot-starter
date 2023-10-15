package io.github.avidbyte.exception;

/**
 * @author Aaron
 */
public class DeploymentException extends Exception {

    private static final long serialVersionUID = 1L;

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}