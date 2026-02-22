package com.pos.model.constants;

public enum ErrorMessages {

    // ---------- Auth ----------
    EMAIL_ALREADY_REGISTERED("Email already registered"),
    INVALID_CREDENTIALS("Invalid credentials"),
    USER_NOT_FOUND("User not found"),
    CURRENT_PASSWORD_INCORRECT("Current password is incorrect"),
    EMAIL_CANNOT_BE_EMPTY("Email cannot be empty"),
    PASSWORD_CANNOT_BE_EMPTY("Password cannot be empty"),
    INVALID_EMAIL("Invalid email"),
    NOT_LOGGED_IN("You are not logged in"),
    INVALID_SESSION_PRINCIPAL("Invalid session principal"),
    USER_BULK_EMPTY("User list cannot be empty"),

    // ---------- Client ----------
    CLIENT_ID_NOT_FOUND("Client ID does not exist"),
    CLIENT_NAME_NOT_FOUND("Client with name does not exist"),
    CLIENT_ALREADY_EXISTS("Client already exists"),
    CLIENT_NAME_TAKEN("The name is already taken by another client"),

    // ---------- DaySales ----------
    START_DATE_AFTER_END_DATE("startDate cannot be after endDate"),
    DATE_REQUIRED("date is required"),

    // ---------- Inventory ----------
    INVENTORY_NOT_FOUND("Inventory record not found"),
    INVENTORY_NOT_FOUND_FOR_PRODUCT("Inventory not found for productId"),
    QUANTITY_MUST_BE_POSITIVE("Quantity must be > 0"),
    INSUFFICIENT_INVENTORY("Insufficient inventory for productId"),
    PRODUCT_NOT_FOUND_FOR_BARCODE("Product not found for barcode"),

    // ---------- Order ----------
    ORDER_NOT_FOUND("Order not found"),
    STATUS_REQUIRED("Status is required"),
    CANNOT_CHANGE_INVOICED_ORDER("Cannot change status of an INVOICED order"),
    INVOICE_PATH_REQUIRED("Invoice path is required"),
    ORDER_ALREADY_INVOICED("Order already invoiced"),
    NO_ORDER_ITEMS_FOUND("No order items found for order"),
    EMPTY_ORDER_CANNOT_BE_INVOICED("Cannot invoice an empty order"),
    SELLING_PRICE_CANNOT_BE_NEGATIVE("Selling price cannot be negative"),
    ORDER_ID_REQUIRED("Order id is required"),
    INVALID_DATE_RANGE("Start date must be before or equal to end date"),

    // ---------- Product ----------
    PRODUCT_NOT_FOUND("Product not found"),
    AT_LEAST_ONE_BARCODE_REQUIRED("At least one barcode is required"),
    PRODUCT_BARCODE_ALREADY_EXISTS("Product with barcode already exists"),
    PRODUCTS_NOT_FOUND_FOR_BARCODES("Products not found for barcodes"),
    SOME_BARCODES_ALREADY_EXIST("Some barcodes already exist"),
    INVALID_CLIENT_NAME_BULK_UPLOAD("Invalid clientName in bulk upload"),
    SELLING_PRICE_EXCEEDS_MRP("Selling price cannot exceed MRP for product"),
    BARCODE_CANNOT_BE_MODIFIED("Barcode cannot be modified"),
    CLIENT_CANNOT_BE_MODIFIED("Client cannot be modified"),

    // ---------- Sales Report ----------
    NO_SALES_REPORT_DATA_FOUND("No sales report data found between"),
    SALES_REPORT_EMPTY("No sales report data found"),
    ERROR_DURING_NORMALIZATION("Error during data normalization"),
    START_AND_END_DATE_REQUIRED("startDate and endDate are required"),
    INVALID_UPLOAD_BARCODE_MISMATCH("Invalid upload: barcode list size mismatch"),
    CANNOT_INVOICE_EMPTY_ORDER("Cannot invoice an empty order"),
    INVOICE_NOT_GENERATED_YET("Invoice not generated yet for order"),
    INVALID_STATUS("Invalid status"),
    FAILED_TO_STORE_INVOICE_PDF("Failed to store invoice PDF"),
    EMAIL_REQUIRED("email is required"),
    DUPLICATE_EMAIL_IN_FILE("duplicate email in file"),
    INVALID_ROLE("invalid role"),
    PASSWORD_REQUIRED("password is required"),

    // ---------- TSV / Upload ----------
    EMPTY_TSV_FILE("Empty TSV file"),
    FAILED_TO_READ_TSV_FILE("Failed to read TSV file"),
    INVALID_TSV_HEADER("Invalid TSV header"),
    INVALID_TSV_HEADER_LENGTH("Invalid TSV header length"),
    MISSING_TSV_HEADER("Missing required TSV header"),
    TSV_HAS_ERRORS("TSV has errors"),
    INVALID_ROW("Invalid row"),

    // ---------- Common Validation ----------
    NAME_REQUIRED("Name is required"),
    BARCODE_REQUIRED("Barcode cannot be empty"),
    CLIENT_ID_REQUIRED("ClientId is required"),
    QUANTITY_REQUIRED("Quantity is required"),
    INVALID_QUANTITY("Invalid quantity"),
    QUANTITY_CANNOT_BE_NEGATIVE("Quantity cannot be negative"),
    MRP_REQUIRED("MRP is required"),
    INVALID_MRP("Invalid MRP"),
    MRP_CANNOT_BE_NEGATIVE("MRP cannot be negative"),
    INVOICE_ALREADY_GENERATED("Invoice already generated"),
    SELLING_PRICE_REQUIRED("Selling price is required"),
    TSV_EMPTY("TSV file is empty"),
    ROLE_REQUIRED("Role is required"),
    PRODUCT_ID_NOT_FOUND("Product not found");


    private final String msg;
    ErrorMessages(String msg) {
        this.msg = msg;
    }
    public String value() {
        return msg;
    }
}
