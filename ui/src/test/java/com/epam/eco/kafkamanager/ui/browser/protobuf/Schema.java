// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema.proto

package com.epam.eco.kafkamanager.ui.browser.protobuf;

public final class Schema {
  private Schema() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_kafkaManager_test_ProtobufRecord_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_kafkaManager_test_ProtobufRecord_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static final com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014schema.proto\022\021kafkaManager.test\"\\\n\016Pro" +
      "tobufRecord\022\023\n\013operationId\030\001 \002(\t\022\r\n\005docI" +
      "d\030\002 \002(\005\022\022\n\nmetadataTt\030\003 \001(\t\022\022\n\nmetadataB" +
      "t\030\004 \001(\tB1\n-com.epam.eco.kafkamanager.ui." +
      "browser.protobufP\001"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_kafkaManager_test_ProtobufRecord_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_kafkaManager_test_ProtobufRecord_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_kafkaManager_test_ProtobufRecord_descriptor,
        new java.lang.String[] { "OperationId", "DocId", "MetadataTt", "MetadataBt", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
