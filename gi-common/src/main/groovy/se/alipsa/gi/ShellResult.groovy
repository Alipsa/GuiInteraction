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
   * Returns a diagnostic representation including the exit status and both
   * output streams. Use {@code stdout} or {@code sh(...)} when only text is
   * wanted in an interpolated string.
   */
  @Override
  String toString() {
    "ShellResult(exitCode=${exitCode}, stdout='${stdout}', stderr='${stderr}')"
  }
}
