package se.alipsa.gi

import groovy.transform.CompileStatic;

@CompileStatic
class InteractionException extends RuntimeException {
  InteractionException() {
    super()
  }

  InteractionException(String message) {
    super(message)
  }

  InteractionException(String message, Throwable cause) {
    super(message, cause)
  }

  InteractionException(Throwable cause) {
    super(cause)
  }
}
