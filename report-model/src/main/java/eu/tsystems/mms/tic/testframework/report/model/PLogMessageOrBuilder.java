// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: framework.proto

package eu.tsystems.mms.tic.testframework.report.model;

public interface PLogMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:data.PLogMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.data.PLogMessageType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.data.PLogMessageType type = 1;</code>
   * @return The type.
   */
  eu.tsystems.mms.tic.testframework.report.model.PLogMessageType getType();

  /**
   * <code>string logger_name = 2;</code>
   * @return The loggerName.
   */
  java.lang.String getLoggerName();
  /**
   * <code>string logger_name = 2;</code>
   * @return The bytes for loggerName.
   */
  com.google.protobuf.ByteString
      getLoggerNameBytes();

  /**
   * <code>string message = 3;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>string message = 3;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>int64 timestamp = 4;</code>
   * @return The timestamp.
   */
  long getTimestamp();

  /**
   * <code>.data.StackTraceCause cause = 5;</code>
   * @return Whether the cause field is set.
   */
  boolean hasCause();
  /**
   * <code>.data.StackTraceCause cause = 5;</code>
   * @return The cause.
   */
  eu.tsystems.mms.tic.testframework.report.model.StackTraceCause getCause();
  /**
   * <code>.data.StackTraceCause cause = 5;</code>
   */
  eu.tsystems.mms.tic.testframework.report.model.StackTraceCauseOrBuilder getCauseOrBuilder();
}
