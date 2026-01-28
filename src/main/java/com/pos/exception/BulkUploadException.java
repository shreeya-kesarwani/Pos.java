package com.pos.exception;

public class BulkUploadException extends ApiException {

    private final byte[] fileBytes;
    private final String filename;
    private final String contentType;

    public BulkUploadException(String message, byte[] fileBytes, String filename, String contentType) {
        super(message);
        this.fileBytes = fileBytes;
        this.filename = filename;
        this.contentType = contentType;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}
