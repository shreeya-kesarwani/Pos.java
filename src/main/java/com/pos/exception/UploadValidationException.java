package com.pos.exception;

public class UploadValidationException extends RuntimeException {

    private final byte[] fileBytes;
    private final String filename;
    private final String contentType;

    public UploadValidationException(String message, byte[] fileBytes, String filename, String contentType) {
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
