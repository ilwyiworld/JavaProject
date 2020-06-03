package com.yiworld.common.protocol;

import com.google.protobuf.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ResponseProto {
    private ResponseProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(ExtensionRegistry registry) {
        registerAllExtensions((ExtensionRegistryLite) registry);
    }

    public interface ResProtocolOrBuilder extends
            // @@protoc_insertion_point(interface_extends:protocol.ResProtocol)
            MessageOrBuilder {
        /**
         * <code>required int64 responseId = 2;</code>
         */
        boolean hasResponseId();

        /**
         * <code>required int64 responseId = 2;</code>
         */
        long getResponseId();

        /**
         * <code>required string resMsg = 1;</code>
         */
        boolean hasResMsg();

        /**
         * <code>required string resMsg = 1;</code>
         */
        String getResMsg();

        /**
         * <code>required string resMsg = 1;</code>
         */
        ByteString getResMsgBytes();

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
     * Protobuf type {@code protocol.ResProtocol}
     */
    public static final class ResProtocol extends GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:protocol.ResProtocol)
            ResProtocolOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use ResProtocol.newBuilder() to construct.
        private ResProtocol(GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private ResProtocol() {
            responseId_ = 0L;
            resMsg_ = "";
            type_ = 0;
        }

        @Override
        public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private ResProtocol(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new NullPointerException();
            }
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000002;
                            resMsg_ = bs;
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000001;
                            responseId_ = input.readInt64();
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
            } catch (IOException e) {
                throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return ResponseProto.internal_static_protocol_ResProtocol_descriptor;
        }

        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return ResponseProto.internal_static_protocol_ResProtocol_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(ResProtocol.class, Builder.class);
        }

        private int bitField0_;
        public static final int RESPONSEID_FIELD_NUMBER = 2;
        private long responseId_;

        /**
         * <code>required int64 responseId = 2;</code>
         */
        public boolean hasResponseId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        /**
         * <code>required int64 responseId = 2;</code>
         */
        public long getResponseId() {
            return responseId_;
        }

        public static final int RESMSG_FIELD_NUMBER = 1;
        private volatile Object resMsg_;

        /**
         * <code>required string resMsg = 1;</code>
         */
        public boolean hasResMsg() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        /**
         * <code>required string resMsg = 1;</code>
         */
        public String getResMsg() {
            Object ref = resMsg_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                ByteString bs = (ByteString) ref;
                String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    resMsg_ = s;
                }
                return s;
            }
        }

        /**
         * <code>required string resMsg = 1;</code>
         */
        public ByteString
        getResMsgBytes() {
            Object ref = resMsg_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String) ref);
                resMsg_ = b;
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

            if (!hasResponseId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasResMsg()) {
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
                GeneratedMessageV3.writeString(output, 1, resMsg_);
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeInt64(2, responseId_);
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
                size += GeneratedMessageV3.computeStringSize(1, resMsg_);
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += CodedOutputStream.computeInt64Size(2, responseId_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += CodedOutputStream.computeInt32Size(3, type_);
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
            if (!(obj instanceof ResProtocol)) {
                return super.equals(obj);
            }
            ResProtocol other = (ResProtocol) obj;

            boolean result = true;
            result = result && (hasResponseId() == other.hasResponseId());
            if (hasResponseId()) {
                result = result && (getResponseId()
                        == other.getResponseId());
            }
            result = result && (hasResMsg() == other.hasResMsg());
            if (hasResMsg()) {
                result = result && getResMsg().equals(other.getResMsg());
            }
            result = result && (hasType() == other.hasType());
            if (hasType()) {
                result = result && (getType() == other.getType());
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
            if (hasResponseId()) {
                hash = (37 * hash) + RESPONSEID_FIELD_NUMBER;
                hash = (53 * hash) + Internal.hashLong(getResponseId());
            }
            if (hasResMsg()) {
                hash = (37 * hash) + RESMSG_FIELD_NUMBER;
                hash = (53 * hash) + getResMsg().hashCode();
            }
            if (hasType()) {
                hash = (37 * hash) + TYPE_FIELD_NUMBER;
                hash = (53 * hash) + getType();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static ResProtocol parseFrom(ByteBuffer data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ResProtocol parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ResProtocol parseFrom(ByteString data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ResProtocol parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ResProtocol parseFrom(byte[] data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static ResProtocol parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static ResProtocol parseFrom(java.io.InputStream input)
                throws IOException {
            return GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static ResProtocol parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry)
                throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static ResProtocol parseDelimitedFrom(InputStream input)
                throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
        }

        public static ResProtocol parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry)
                throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static ResProtocol parseFrom(CodedInputStream input)
                throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static ResProtocol parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(ResProtocol prototype) {
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
         * Protobuf type {@code protocol.ResProtocol}
         */
        public static final class Builder extends
                GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:protocol.ResProtocol)
                ResProtocolOrBuilder {
            public static final Descriptors.Descriptor getDescriptor() {
                return ResponseProto.internal_static_protocol_ResProtocol_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
                return ResponseProto.internal_static_protocol_ResProtocol_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                ResProtocol.class, Builder.class);
            }

            // Construct using com.crossoverjie.cim.common.protocol.ResponseProto.ResProtocol.newBuilder()
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
                responseId_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                resMsg_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                type_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Descriptors.Descriptor getDescriptorForType() {
                return ResponseProto.internal_static_protocol_ResProtocol_descriptor;
            }

            public ResProtocol getDefaultInstanceForType() {
                return ResProtocol.getDefaultInstance();
            }

            public ResProtocol build() {
                ResProtocol result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public ResProtocol buildPartial() {
                ResProtocol result = new ResProtocol(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.responseId_ = responseId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.resMsg_ = resMsg_;
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
                if (other instanceof ResProtocol) {
                    return mergeFrom((ResProtocol) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(ResProtocol other) {
                if (other == ResProtocol.getDefaultInstance()) return this;
                if (other.hasResponseId()) {
                    setResponseId(other.getResponseId());
                }
                if (other.hasResMsg()) {
                    bitField0_ |= 0x00000002;
                    resMsg_ = other.resMsg_;
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
                if (!hasResponseId()) {
                    return false;
                }
                if (!hasResMsg()) {
                    return false;
                }
                if (!hasType()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                ResProtocol parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (InvalidProtocolBufferException e) {
                    parsedMessage = (ResProtocol) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long responseId_;

            /**
             * <code>required int64 responseId = 2;</code>
             */
            public boolean hasResponseId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            /**
             * <code>required int64 responseId = 2;</code>
             */
            public long getResponseId() {
                return responseId_;
            }

            /**
             * <code>required int64 responseId = 2;</code>
             */
            public Builder setResponseId(long value) {
                bitField0_ |= 0x00000001;
                responseId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required int64 responseId = 2;</code>
             */
            public Builder clearResponseId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                responseId_ = 0L;
                onChanged();
                return this;
            }

            private Object resMsg_ = "";

            /**
             * <code>required string resMsg = 1;</code>
             */
            public boolean hasResMsg() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            /**
             * <code>required string resMsg = 1;</code>
             */
            public String getResMsg() {
                Object ref = resMsg_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString) ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        resMsg_ = s;
                    }
                    return s;
                } else {
                    return (String) ref;
                }
            }

            /**
             * <code>required string resMsg = 1;</code>
             */
            public ByteString getResMsgBytes() {
                Object ref = resMsg_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String) ref);
                    resMsg_ = b;
                    return b;
                } else {
                    return (ByteString) ref;
                }
            }

            /**
             * <code>required string resMsg = 1;</code>
             */
            public Builder setResMsg(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                resMsg_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required string resMsg = 1;</code>
             */
            public Builder clearResMsg() {
                bitField0_ = (bitField0_ & ~0x00000002);
                resMsg_ = getDefaultInstance().getResMsg();
                onChanged();
                return this;
            }

            /**
             * <code>required string resMsg = 1;</code>
             */
            public Builder setResMsgBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                resMsg_ = value;
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
            // @@protoc_insertion_point(builder_scope:protocol.ResProtocol)
        }

        // @@protoc_insertion_point(class_scope:protocol.ResProtocol)
        private static final ResProtocol DEFAULT_INSTANCE;

        static {
            DEFAULT_INSTANCE = new ResProtocol();
        }

        public static ResProtocol getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        @Deprecated
        public static final Parser<ResProtocol> PARSER = new AbstractParser<ResProtocol>() {
            public ResProtocol parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                    throws InvalidProtocolBufferException {
                return new ResProtocol(input, extensionRegistry);
            }
        };

        public static Parser<ResProtocol> parser() {
            return PARSER;
        }

        @Override
        public Parser<ResProtocol> getParserForType() {
            return PARSER;
        }

        public ResProtocol getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final Descriptors.Descriptor internal_static_protocol_ResProtocol_descriptor;
    private static final GeneratedMessageV3.FieldAccessorTable internal_static_protocol_ResProtocol_fieldAccessorTable;

    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static Descriptors.FileDescriptor descriptor;

    static {
        String[] descriptorData = {
                "\n\027BaseResponseProto.proto\022\010protocol\"B\n\016C" +
                        "IMResProtocol\022\022\n\nresponseId\030\002 \002(\003\022\016\n\006res" +
                        "Msg\030\001 \002(\t\022\014\n\004type\030\003 \002(\005B8\n$com.crossover" +
                        "jie.cim.common.protocolB\020ResponseProt" +
                        "o"
        };
        Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public ExtensionRegistry assignDescriptors(Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[]{}, assigner);
        internal_static_protocol_ResProtocol_descriptor = getDescriptor().getMessageTypes().get(0);
        internal_static_protocol_ResProtocol_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
                internal_static_protocol_ResProtocol_descriptor,
                new String[]{"ResponseId", "ResMsg", "Type",});
    }
    // @@protoc_insertion_point(outer_class_scope)
}
