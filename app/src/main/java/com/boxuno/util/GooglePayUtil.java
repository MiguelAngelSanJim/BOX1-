package com.boxuno.util;

public class GooglePayUtil {
    public static String getPaymentDataRequest(double precio) {
        return "{"
                + "\"apiVersion\": 2,"
                + "\"apiVersionMinor\": 0,"
                + "\"allowedPaymentMethods\": [{"
                + "  \"type\": \"CARD\","
                + "  \"parameters\": {"
                + "    \"allowedAuthMethods\": [\"PAN_ONLY\", \"CRYPTOGRAM_3DS\"],"
                + "    \"allowedCardNetworks\": [\"VISA\", \"MASTERCARD\"]"
                + "  },"
                + "  \"tokenizationSpecification\": {"
                + "    \"type\": \"PAYMENT_GATEWAY\","
                + "    \"parameters\": {"
                + "      \"gateway\": \"example\","
                + "      \"gatewayMerchantId\": \"exampleGatewayMerchantId\""
                + "    }"
                + "  }"
                + "}],"
                + "\"transactionInfo\": {"
                + "  \"totalPriceStatus\": \"FINAL\","
                + "  \"totalPrice\": \"" + precio + "\","
                + "  \"currencyCode\": \"EUR\""
                + "},"
                + "\"merchantInfo\": {"
                + "  \"merchantName\": \"Box 1/ Demo\""
                + "}"
                + "}";
    }
}
