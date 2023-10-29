package se.alipsa.gi;

public class InteractionException extends RuntimeException {
  public InteractionException() {
    super();
  }

  public InteractionException(String message) {
    super(message);
  }

  public InteractionException(String message, Throwable cause) {
    super(message, cause);
  }

  public InteractionException(Throwable cause) {
    super(cause);
  }
}
