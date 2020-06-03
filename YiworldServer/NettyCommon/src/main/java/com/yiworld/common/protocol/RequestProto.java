package com.yiworld.common.protocol;

import com.google.protobuf.*;
import java.io.IOException;
import java.io.InputStream;

public class RequestProto {
    private RequestProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(ExtensionRegistry registry) {
        registerAllExtensions((ExtensionRegistryLite) registry);
    }

    public interface ReqProtocolOrBuilder extends
            // @@protoc_insertion_point(interface_extends:protocol.ReqProtocol)
            MessageOrBuilder {

        /**
         * <code>required int64 requestId = 2;</code>
         */
        boolean hasRequestId();

        /**
         * <code>required int64 requestId = 2;</code>
         */
        long getRequestId();

        /**
         * <code>required string reqMsg = 1;</code>
         */
        boolean hasReqMsg();

        /**
         * <code>required string reqMsg = 1;</code>
         */
        String getReqMsg();

        /**
         * <code>required string reqMsg = 1;</code>
         */
        ByteString getReqMsgBytes();

        /**
         * <code>required int32 type = 3;</code>
         */
        boolean hasType();

        /**
         * <code>required int32 type = 3;</code>
         */
        int getType();
    }

    /**
     * Protobuf type {@code protocol.ReqProtocol}
     */
    public static final class ReqProtocol extends GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:protocol.ReqProtocol)
            ReqProtocolOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use ReqProtocol.newBuilder() to construct.
        private ReqProtocol(GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private ReqProtocol() {
            requestId_ = 0L;
            reqMsg_ = "";
            type_ = 0;
        }

        @Override
        public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private ReqProtocol(CodedInputStream input,ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new NullPointerException();
            }
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields =
                    UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(
                                    input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000002;
                            reqMsg_ = bs;
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000001;
                            requestId_ = input.readInt64();
                            break;
                        }
                        case 24: {
                            bitField0_ |= 0x00000004;
                            type_ = input.readInt32();
                            break;
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return RequestProto.internal_static_protocol_ReqProtocol_descriptor;
        }

        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return RequestProto.internal_static_protocol_ReqProtocol_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(ReqProtocol.class, Builder.class);
        }

        private int bitField0_;
        public static final int REQUESTID_FIELD_NUMBER = 2;
        private long requestId_;

        /**
         * <code>required int64 requestId = 2;</code>
         */
        public boolean hasRequestId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        /**
         * <code>required int64 requestId = 2;</code>
         */
        public long getRequestId() {
            return requestId_;
        }

        public static final int REQMSG_FIELD_NUMBER = 1;
        private volatile Object reqMsg_;

        /**
         * <code>required string reqMsg = 1;</code>
         */
        public boolean hasReqMsg() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        /**
         * <code>required string reqMsg = 1;</code>
         */
        public String getReqMsg() {
            Object ref = reqMsg_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                ByteString bs = (ByteString) ref;
                String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    reqMsg_ = s;
                }
                return s;
            }
        }

        /**
         * <code>required string reqMsg = 1;</code>
         */
        public ByteString
        getReqMsgBytes() {
            Object ref = reqMsg_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String) ref);
                reqMsg_ = b;
                return b;
            } else {
                return (ByteString) ref;
            }
        }

        public static final int TYPE_FIELD_NUMBER = 3;
        private int type_;

        /**
         * <code>required int32 type = 3;</code>
         */
        public boolean hasType() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        /**
         * <code>required int32 type = 3;</code>
         */
        public int getType() {
            return type_;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            if (!hasRequestId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasReqMsg()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasType()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(CodedOutputStream output)
                throws java.io.IOException {
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                GeneratedMessageV3.writeString(output, 1, reqMsg_);
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeInt64(2, requestId_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(3, type_);
            }
            unknownFields.writeTo(output);
        }

        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += GeneratedMessageV3.computeStringSize(1, reqMsg_);
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += CodedOutputStream
                        .computeInt64Size(2, requestId_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += CodedOutputStream
                        .computeInt32Size(3, type_);
            }
            size += unknownFields.getSerializedSize();
            memoizedSize = size;
            return size;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ReqProtocol)) {
                return super.equals(obj);
            }
            ReqProtocol other = (ReqProtocol) obj;

            boolean result = true;
            result = result && (hasRequestId() == other.hasRequestId());
            if (hasRequestId()) {
                result = result && (getRequestId()
                        == other.getRequestId());
            }
            result = result && (hasReqMsg() == other.hasReqMsg());
            if (hasReqMsg()) {
                result = result && getReqMsg()
                        .equals(other.getReqMsg());
            }
            result = result && (hasType() == other.hasType());
            if (hasType()) {
                result = result && (getType()
                        == other.getType());
            }
            result = result && unknownFields.equals(other.unknownFields);
            return result;
        }

        @Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptor().hashCode();
            if (hasRequestId()) {
                hash = (37 * hash) + REQUESTID_FIELD_NUMBER;
                hash = (53 * hash) + Internal.hashLong(
                        getRequestId());
            }
            if (hasReqMsg()) {
                hash = (37 * hash) + REQMSG_FIELD_NUMBER;
                hash = (53 * hash) + getReqMsg().hashCode();
            }
            if (hasType()) {
                hash = (37 * hash) + TYPE_FIELD_NUMBER;
                hash = (53 * hash) + getType();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static ReqProtocol parseFrom(
                java.nio.ByteBuffer data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ReqProtocol parseFrom(java.nio.ByteBuffer data, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ReqProtocol parseFrom(ByteString data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ReqProtocol parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ReqProtocol parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ReqProtocol parseFrom(
                byte[] data,
                ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ReqProtocol parseFrom(java.io.InputStream input) throws java.io.IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static ReqProtocol parseFrom(
                InputStream input,
                ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static ReqProtocol parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static ReqProtocol parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static ReqProtocol parseFrom(CodedInputStream input) throws java.io.IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static ReqProtocol parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(ReqProtocol prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * Protobuf type {@code protocol.ReqProtocol}
         */
        public static final class Builder extends GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:protocol.ReqProtocol)
                ReqProtocolOrBuilder {
            public static final Descriptors.Descriptor getDescriptor() {
                return RequestProto.internal_static_protocol_ReqProtocol_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
                return RequestProto.internal_static_protocol_ReqProtocol_fieldAccessorTable.ensureFieldAccessorsInitialized(
                                ReqProtocol.class, Builder.class);
            }

            // Construct using com.crossoverjie.cim.common.protocol.RequestProto.ReqProtocol.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (GeneratedMessageV3.alwaysUseFieldBuilders) {
                }
            }

            public Builder clear() {
                super.clear();
                requestId_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                reqMsg_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                type_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Descriptors.Descriptor getDescriptorForType() {
                return RequestProto.internal_static_protocol_ReqProtocol_descriptor;
            }

            public ReqProtocol getDefaultInstanceForType() {
                return ReqProtocol.getDefaultInstance();
            }

            public ReqProtocol build() {
                ReqProtocol result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public ReqProtocol buildPartial() {
                ReqProtocol result = new ReqProtocol(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.requestId_ = requestId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.reqMsg_ = reqMsg_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.type_ = type_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder clone() {
                return (Builder) super.clone();
            }

            public Builder setField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder) super.setField(field, value);
            }

            public Builder clearField(Descriptors.FieldDescriptor field) {
                return (Builder) super.clearField(field);
            }

            public Builder clearOneof(Descriptors.OneofDescriptor oneof) {
                return (Builder) super.clearOneof(oneof);
            }

            public Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
                return (Builder) super.setRepeatedField(field, index, value);
            }

            public Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder) super.addRepeatedField(field, value);
            }

            public Builder mergeFrom(Message other) {
                if (other instanceof ReqProtocol) {
                    return mergeFrom((ReqProtocol) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(ReqProtocol other) {
                if (other == ReqProtocol.getDefaultInstance()) return this;
                if (other.hasRequestId()) {
                    setRequestId(other.getRequestId());
                }
                if (other.hasReqMsg()) {
                    bitField0_ |= 0x00000002;
                    reqMsg_ = other.reqMsg_;
                    onChanged();
                }
                if (other.hasType()) {
                    setType(other.getType());
                }
                this.mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            public final boolean isInitialized() {
                if (!hasRequestId()) {
                    return false;
                }
                if (!hasReqMsg()) {
                    return false;
                }
                if (!hasType()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(
                    CodedInputStream input,
                    ExtensionRegistryLite extensionRegistry) throws IOException {
                ReqProtocol parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (InvalidProtocolBufferException e) {
                    parsedMessage = (ReqProtocol) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long requestId_;

            /**
             * <code>required int64 requestId = 2;</code>
             */
            public boolean hasRequestId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            /**
             * <code>required int64 requestId = 2;</code>
             */
            public long getRequestId() {
                return requestId_;
            }

            /**
             * <code>required int64 requestId = 2;</code>
             */
            public Builder setRequestId(long value) {
                bitField0_ |= 0x00000001;
                requestId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required int64 requestId = 2;</code>
             */
            public Builder clearRequestId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                requestId_ = 0L;
                onChanged();
                return this;
            }

            private Object reqMsg_ = "";

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public boolean hasReqMsg() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public String getReqMsg() {
                Object ref = reqMsg_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString) ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        reqMsg_ = s;
                    }
                    return s;
                } else {
                    return (String) ref;
                }
            }

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public ByteString
            getReqMsgBytes() {
                Object ref = reqMsg_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String) ref);
                    reqMsg_ = b;
                    return b;
                } else {
                    return (ByteString) ref;
                }
            }

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public Builder setReqMsg(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                reqMsg_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public Builder clearReqMsg() {
                bitField0_ = (bitField0_ & ~0x00000002);
                reqMsg_ = getDefaultInstance().getReqMsg();
                onChanged();
                return this;
            }

            /**
             * <code>required string reqMsg = 1;</code>
             */
            public Builder setReqMsgBytes(
                    ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                reqMsg_ = value;
                onChanged();
                return this;
            }

            private int type_;

            /**
             * <code>required int32 type = 3;</code>
             */
            public boolean hasType() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            /**
             * <code>required int32 type = 3;</code>
             */
            public int getType() {
                return type_;
            }

            /**
             * <code>required int32 type = 3;</code>
             */
            public Builder setType(int value) {
                bitField0_ |= 0x00000004;
                type_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required int32 type = 3;</code>
             */
            public Builder clearType() {
                bitField0_ = (bitField0_ & ~0x00000004);
                type_ = 0;
                onChanged();
                return this;
            }

            public final Builder setUnknownFields(final UnknownFieldSet unknownFields) {
                return super.setUnknownFields(unknownFields);
            }

            public final Builder mergeUnknownFields(final UnknownFieldSet unknownFields) {
                return super.mergeUnknownFields(unknownFields);
            }
            // @@protoc_insertion_point(builder_scope:protocol.ReqProtocol)
        }

        // @@protoc_insertion_point(class_scope:protocol.ReqProtocol)
        private static final ReqProtocol DEFAULT_INSTANCE;

        static {
            DEFAULT_INSTANCE = new ReqProtocol();
        }

        public static ReqProtocol getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        @Deprecated
        public static final Parser<ReqProtocol> PARSER = new AbstractParser<ReqProtocol>() {
            public ReqProtocol parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                    throws InvalidProtocolBufferException {
                return new ReqProtocol(input, extensionRegistry);
            }
        };

        public static Parser<ReqProtocol> parser() {
            return PARSER;
        }

        @Override
        public Parser<ReqProtocol> getParserForType() {
            return PARSER;
        }

        public ReqProtocol getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final Descriptors.Descriptor internal_static_protocol_ReqProtocol_descriptor;
    private static final GeneratedMessageV3.FieldAccessorTable internal_static_protocol_ReqProtocol_fieldAccessorTable;

    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static Descriptors.FileDescriptor descriptor;

    static {
        String[] descriptorData = {
                "\n\026BaseRequestProto.proto\022\010protocol\"A\n\016CI" +
                        "MReqProtocol\022\021\n\trequestId\030\002 \002(\003\022\016\n\006reqMs" +
                        "g\030\001 \002(\t\022\014\n\004type\030\003 \002(\005B7\n$com.crossoverji" +
                        "e.cim.common.protocolB\017RequestProto"
        };
        Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public ExtensionRegistry assignDescriptors(Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[]{}, assigner);
        internal_static_protocol_ReqProtocol_descriptor = getDescriptor().getMessageTypes().get(0);
        internal_static_protocol_ReqProtocol_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
                internal_static_protocol_ReqProtocol_descriptor,
                new String[]{"RequestId", "ReqMsg", "Type",});
    }
}
