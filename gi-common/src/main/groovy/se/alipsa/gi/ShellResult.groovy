package se.alipsa.gi

import groovy.transform.CompileStatic

/**
 * Result of executing a shell command.
 */
@CompileStatic
final class ShellResult {

  private final String stdout
  private final String stderr
  private final int exitCode

  ShellResult(String stdout, String stderr, int exitCode) {
    this.stdout = stdout ?: ''
    this.stderr = stderr ?: ''
    this.exitCode = exitCode
  }

  String getStdout() {
    stdout
  }

  String getStderr() {
    stderr
  }

  int getExitCode() {
    exitCode
  }

  boolean isSuccess() {
    exitCode == 0
  }

  /**
   * Makes the result convenient to use in Groovy string interpolation.
   */
  @Override
  String toString() {
    stdout
  }
}
