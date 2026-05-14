package com.autoservice.domain;

public enum OrderStatus {
    UZAVRETA("Uzavretá"),
    CIASTOCNE_DOKONCENA("Čiastočne dokončená"),
    PRIPRAVA_NA_VYAJU("Príprava na výjazd"),
    CAKA_NA_DIELY("Čaká na diely"),
    CAKA_NA_SCHVALENIE("Čaká na schválenie"),
    OPRAVA("Oprava"),
    DIAGNOSTIKA("Diagnostika"),
    PRIRADENA("Priradená"),
    CAKA_NA_PRIRADENIE("Čaká na priradenie"),
    REZERVOVANA("Rezervovaná");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
