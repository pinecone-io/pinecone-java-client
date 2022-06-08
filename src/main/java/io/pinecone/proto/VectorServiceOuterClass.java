// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/vector_service.proto

package io.pinecone.proto;

public final class VectorServiceOuterClass {
  private VectorServiceOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Vector_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Vector_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ScoredVector_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ScoredVector_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_UpsertRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_UpsertRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_UpsertResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_UpsertResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DeleteRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeleteRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DeleteResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeleteResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FetchRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FetchRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FetchResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FetchResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FetchResponse_VectorsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FetchResponse_VectorsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_QueryVector_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_QueryVector_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_QueryRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_QueryRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_SingleQueryResults_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SingleQueryResults_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_QueryResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_QueryResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_UpdateRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_UpdateRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_UpdateResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_UpdateResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DescribeIndexStatsRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DescribeIndexStatsRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_NamespaceSummary_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_NamespaceSummary_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DescribeIndexStatsResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DescribeIndexStatsResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DescribeIndexStatsResponse_NamespacesEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DescribeIndexStatsResponse_NamespacesEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\032proto/vector_service.proto\032\034google/pro" +
      "tobuf/struct.proto\032\034google/api/annotatio" +
      "ns.proto\032\037google/api/field_behavior.prot" +
      "o\032.protoc-gen-openapiv2/options/annotati" +
      "ons.proto\"\327\001\n\006Vector\022,\n\002id\030\001 \001(\tB \222A\032J\022\"" +
      "example-vector-1\"x\200\004\200\001\001\340A\002\022G\n\006values\030\002 \003" +
      "(\002B7\222A1J([0.1, 0.2, 0.3, 0.4, 0.5, 0.6, " +
      "0.7, 0.8]x\240\234\001\200\001\001\340A\002\022V\n\010metadata\030\003 \001(\0132\027." +
      "google.protobuf.StructB+\222A(J&{\"genre\": \"" +
      "documentary\", \"year\": 2019}\"\355\001\n\014ScoredVe" +
      "ctor\022,\n\002id\030\001 \001(\tB \222A\032J\022\"example-vector-1" +
      "\"x\200\004\200\001\001\340A\002\022\030\n\005score\030\002 \001(\002B\t\222A\006J\0040.08\022=\n\006" +
      "values\030\003 \003(\002B-\222A*J([0.1, 0.2, 0.3, 0.4, " +
      "0.5, 0.6, 0.7, 0.8]\022V\n\010metadata\030\004 \001(\0132\027." +
      "google.protobuf.StructB+\222A(J&{\"genre\": \"" +
      "documentary\", \"year\": 2019}\"d\n\rUpsertReq" +
      "uest\022&\n\007vectors\030\001 \003(\0132\007.VectorB\014\222A\006x\350\007\200\001" +
      "\001\340A\002\022+\n\tnamespace\030\002 \001(\tB\030\222A\025J\023\"example-n" +
      "amespace\"\"1\n\016UpsertResponse\022\037\n\016upserted_" +
      "count\030\001 \001(\rB\007\222A\004J\00210\"\266\001\n\rDeleteRequest\022(" +
      "\n\003ids\030\001 \003(\tB\033\222A\030J\020[\"id-0\", \"id-1\"]x\350\007\200\001\001" +
      "\022%\n\ndelete_all\030\002 \001(\010B\021\222A\016:\005falseJ\005false\022" +
      "+\n\tnamespace\030\003 \001(\tB\030\222A\025J\023\"example-namesp" +
      "ace\"\022\'\n\006filter\030\004 \001(\0132\027.google.protobuf.S" +
      "truct\"\020\n\016DeleteResponse\"h\n\014FetchRequest\022" +
      "+\n\003ids\030\001 \003(\tB\036\222A\030J\020[\"id-0\", \"id-1\"]x\350\007\200\001" +
      "\001\340A\002\022+\n\tnamespace\030\002 \001(\tB\030\222A\025J\023\"example-n" +
      "amespace\"\"\243\001\n\rFetchResponse\022,\n\007vectors\030\001" +
      " \003(\0132\033.FetchResponse.VectorsEntry\022+\n\tnam" +
      "espace\030\002 \001(\tB\030\222A\025J\023\"example-namespace\"\0327" +
      "\n\014VectorsEntry\022\013\n\003key\030\001 \001(\t\022\026\n\005value\030\002 \001" +
      "(\0132\007.Vector:\0028\001\"\252\002\n\013QueryVector\022G\n\006value" +
      "s\030\001 \003(\002B7\222A1J([0.1, 0.2, 0.3, 0.4, 0.5, " +
      "0.6, 0.7, 0.8]x\240\234\001\200\001\001\340A\002\022(\n\005top_k\030\002 \001(\rB" +
      "\031\222A\026J\00210Y\000\000\000\000\000\210\303@i\000\000\000\000\000\000\360?\022+\n\tnamespace\030" +
      "\003 \001(\tB\030\222A\025J\023\"example-namespace\"\022{\n\006filte" +
      "r\030\004 \001(\0132\027.google.protobuf.StructBR\222AOJM{" +
      "\"genre\": {\"$in\": [\"comedy\", \"documentary" +
      "\", \"drama\"]}, \"year\": {\"$eq\": 2019}}\"\324\003\n" +
      "\014QueryRequest\022+\n\tnamespace\030\001 \001(\tB\030\222A\025J\023\"" +
      "example-namespace\"\022+\n\005top_k\030\002 \001(\rB\034\222A\026J\002" +
      "10Y\000\000\000\000\000\210\303@i\000\000\000\000\000\000\360?\340A\002\022{\n\006filter\030\003 \001(\0132" +
      "\027.google.protobuf.StructBR\222AOJM{\"genre\":" +
      " {\"$in\": [\"comedy\", \"documentary\", \"dram" +
      "a\"]}, \"year\": {\"$eq\": 2019}}\022(\n\016include_" +
      "values\030\004 \001(\010B\020\222A\r:\005falseJ\004true\022*\n\020includ" +
      "e_metadata\030\005 \001(\010B\020\222A\r:\005falseJ\004true\022)\n\007qu" +
      "eries\030\006 \003(\0132\014.QueryVectorB\n\030\001\222A\005x\n\200\001\001\022D\n" +
      "\006vector\030\007 \003(\002B4\222A1J([0.1, 0.2, 0.3, 0.4," +
      " 0.5, 0.6, 0.7, 0.8]x\240\234\001\200\001\001\022&\n\002id\030\010 \001(\tB" +
      "\032\222A\027J\022\"example-vector-1\"x\200\004\"a\n\022SingleQue" +
      "ryResults\022\036\n\007matches\030\001 \003(\0132\r.ScoredVecto" +
      "r\022+\n\tnamespace\030\002 \001(\tB\030\222A\025J\023\"example-name" +
      "space\"\"l\n\rQueryResponse\022(\n\007results\030\001 \003(\013" +
      "2\023.SingleQueryResultsB\002\030\001\022\036\n\007matches\030\002 \003" +
      "(\0132\r.ScoredVector\022\021\n\tnamespace\030\003 \001(\t\"\214\002\n" +
      "\rUpdateRequest\022,\n\002id\030\001 \001(\tB \222A\032J\022\"exampl" +
      "e-vector-1\"x\200\004\200\001\001\340A\002\022D\n\006values\030\002 \003(\002B4\222A" +
      "1J([0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0" +
      ".8]x\240\234\001\200\001\001\022Z\n\014set_metadata\030\003 \001(\0132\027.googl" +
      "e.protobuf.StructB+\222A(J&{\"genre\": \"docum" +
      "entary\", \"year\": 2019}\022+\n\tnamespace\030\004 \001(" +
      "\tB\030\222A\025J\023\"example-namespace\"\"\020\n\016UpdateRes" +
      "ponse\"\033\n\031DescribeIndexStatsRequest\"4\n\020Na" +
      "mespaceSummary\022 \n\014vector_count\030\001 \001(\rB\n\222A" +
      "\007J\00550000\"\364\002\n\032DescribeIndexStatsResponse\022" +
      "?\n\nnamespaces\030\001 \003(\0132+.DescribeIndexStats" +
      "Response.NamespacesEntry\022\034\n\tdimension\030\002 " +
      "\001(\rB\t\222A\006J\0041024\022!\n\016index_fullness\030\003 \001(\002B\t" +
      "\222A\006J\0040.42\032D\n\017NamespacesEntry\022\013\n\003key\030\001 \001(" +
      "\t\022 \n\005value\030\002 \001(\0132\021.NamespaceSummary:\0028\001:" +
      "\215\001\222A\211\0012\206\001{\"namespaces\": {\"\": {\"vectorCou" +
      "nt\": 50000}, \"example-namespace-2\": {\"ve" +
      "ctorCount\": 30000}}, \"dimension\": 1024, " +
      "\"index_fullness\": 0.42}2\241\005\n\rVectorServic" +
      "e\022c\n\006Upsert\022\016.UpsertRequest\032\017.UpsertResp" +
      "onse\"8\202\323\344\223\002\024\"\017/vectors/upsert:\001*\222A\033\n\021Vec" +
      "tor Operations*\006upsert\022v\n\006Delete\022\016.Delet" +
      "eRequest\032\017.DeleteResponse\"K\202\323\344\223\002\'\"\017/vect" +
      "ors/delete:\001*Z\021*\017/vectors/delete\222A\033\n\021Vec" +
      "tor Operations*\006delete\022[\n\005Fetch\022\r.FetchR" +
      "equest\032\016.FetchResponse\"3\202\323\344\223\002\020\022\016/vectors" +
      "/fetch\222A\032\n\021Vector Operations*\005fetch\022V\n\005Q" +
      "uery\022\r.QueryRequest\032\016.QueryResponse\".\202\323\344" +
      "\223\002\013\"\006/query:\001*\222A\032\n\021Vector Operations*\005qu" +
      "ery\022c\n\006Update\022\016.UpdateRequest\032\017.UpdateRe" +
      "sponse\"8\202\323\344\223\002\024\"\017/vectors/update:\001*\222A\033\n\021V" +
      "ector Operations*\006update\022\230\001\n\022DescribeInd" +
      "exStats\022\032.DescribeIndexStatsRequest\032\033.De" +
      "scribeIndexStatsResponse\"I\202\323\344\223\002\027\022\025/descr" +
      "ibe_index_stats\222A)\n\021Vector Operations*\024d" +
      "escribe_index_statsB\300\003\n\021io.pinecone.prot" +
      "oP\001Z/github.com/pinecone-io/new-go-pinec" +
      "one/pinecone\222A\366\002\022K\n\014Pinecone API\";\n\017Pine" +
      "cone.io Ops\022\023https://pinecone.io\032\023suppor" +
      "t@pinecone.io\0329{index_name}-{project_nam" +
      "e}.svc.{environment}.pinecone.io*\001\0022\020app" +
      "lication/json:\020application/jsonZx\nv\n\nApi" +
      "KeyAuth\022h\010\002\022YAn API Key is required to c" +
      "all Pinecone APIs. Get yours at https://" +
      "www.pinecone.io/start/\032\007Api-Key \002b\020\n\016\n\nA" +
      "piKeyAuth\022\000r9\n\031More Pinecone.io API docs" +
      "\022\034https://www.pinecone.io/docsb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.StructProto.getDescriptor(),
          com.google.api.AnnotationsProto.getDescriptor(),
          com.google.api.FieldBehaviorProto.getDescriptor(),
          grpc.gateway.protoc_gen_openapiv2.options.Annotations.getDescriptor(),
        });
    internal_static_Vector_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Vector_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Vector_descriptor,
        new java.lang.String[] { "Id", "Values", "Metadata", });
    internal_static_ScoredVector_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ScoredVector_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ScoredVector_descriptor,
        new java.lang.String[] { "Id", "Score", "Values", "Metadata", });
    internal_static_UpsertRequest_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_UpsertRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_UpsertRequest_descriptor,
        new java.lang.String[] { "Vectors", "Namespace", });
    internal_static_UpsertResponse_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_UpsertResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_UpsertResponse_descriptor,
        new java.lang.String[] { "UpsertedCount", });
    internal_static_DeleteRequest_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_DeleteRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DeleteRequest_descriptor,
        new java.lang.String[] { "Ids", "DeleteAll", "Namespace", "Filter", });
    internal_static_DeleteResponse_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_DeleteResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DeleteResponse_descriptor,
        new java.lang.String[] { });
    internal_static_FetchRequest_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_FetchRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FetchRequest_descriptor,
        new java.lang.String[] { "Ids", "Namespace", });
    internal_static_FetchResponse_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_FetchResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FetchResponse_descriptor,
        new java.lang.String[] { "Vectors", "Namespace", });
    internal_static_FetchResponse_VectorsEntry_descriptor =
      internal_static_FetchResponse_descriptor.getNestedTypes().get(0);
    internal_static_FetchResponse_VectorsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FetchResponse_VectorsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_QueryVector_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_QueryVector_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_QueryVector_descriptor,
        new java.lang.String[] { "Values", "TopK", "Namespace", "Filter", });
    internal_static_QueryRequest_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_QueryRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_QueryRequest_descriptor,
        new java.lang.String[] { "Namespace", "TopK", "Filter", "IncludeValues", "IncludeMetadata", "Queries", "Vector", "Id", });
    internal_static_SingleQueryResults_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_SingleQueryResults_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_SingleQueryResults_descriptor,
        new java.lang.String[] { "Matches", "Namespace", });
    internal_static_QueryResponse_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_QueryResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_QueryResponse_descriptor,
        new java.lang.String[] { "Results", "Matches", "Namespace", });
    internal_static_UpdateRequest_descriptor =
      getDescriptor().getMessageTypes().get(12);
    internal_static_UpdateRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_UpdateRequest_descriptor,
        new java.lang.String[] { "Id", "Values", "SetMetadata", "Namespace", });
    internal_static_UpdateResponse_descriptor =
      getDescriptor().getMessageTypes().get(13);
    internal_static_UpdateResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_UpdateResponse_descriptor,
        new java.lang.String[] { });
    internal_static_DescribeIndexStatsRequest_descriptor =
      getDescriptor().getMessageTypes().get(14);
    internal_static_DescribeIndexStatsRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DescribeIndexStatsRequest_descriptor,
        new java.lang.String[] { });
    internal_static_NamespaceSummary_descriptor =
      getDescriptor().getMessageTypes().get(15);
    internal_static_NamespaceSummary_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_NamespaceSummary_descriptor,
        new java.lang.String[] { "VectorCount", });
    internal_static_DescribeIndexStatsResponse_descriptor =
      getDescriptor().getMessageTypes().get(16);
    internal_static_DescribeIndexStatsResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DescribeIndexStatsResponse_descriptor,
        new java.lang.String[] { "Namespaces", "Dimension", "IndexFullness", });
    internal_static_DescribeIndexStatsResponse_NamespacesEntry_descriptor =
      internal_static_DescribeIndexStatsResponse_descriptor.getNestedTypes().get(0);
    internal_static_DescribeIndexStatsResponse_NamespacesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DescribeIndexStatsResponse_NamespacesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.FieldBehaviorProto.fieldBehavior);
    registry.add(com.google.api.AnnotationsProto.http);
    registry.add(grpc.gateway.protoc_gen_openapiv2.options.Annotations.openapiv2Field);
    registry.add(grpc.gateway.protoc_gen_openapiv2.options.Annotations.openapiv2Operation);
    registry.add(grpc.gateway.protoc_gen_openapiv2.options.Annotations.openapiv2Schema);
    registry.add(grpc.gateway.protoc_gen_openapiv2.options.Annotations.openapiv2Swagger);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.protobuf.StructProto.getDescriptor();
    com.google.api.AnnotationsProto.getDescriptor();
    com.google.api.FieldBehaviorProto.getDescriptor();
    grpc.gateway.protoc_gen_openapiv2.options.Annotations.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
