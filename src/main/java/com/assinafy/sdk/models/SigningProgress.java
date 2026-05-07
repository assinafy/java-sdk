package com.assinafy.sdk.models;

public class SigningProgress {

    private final int signed;
    private final int total;
    private final int pending;
    private final double percentage;

    public SigningProgress(int signed, int total, int pending, double percentage) {
        this.signed = signed;
        this.total = total;
        this.pending = pending;
        this.percentage = percentage;
    }

    public int getSigned() { return signed; }
    public int getTotal() { return total; }
    public int getPending() { return pending; }
    public double getPercentage() { return percentage; }
}
