package com.assinafy.sdk.models;

/**
 * Represents the signing progress in an Assinafy API response.
 */
public class SigningProgress {

    private final int signed;
    private final int total;
    private final int pending;
    private final double percentage;

    /**
     * Creates a signing progress.
     *
     * @param signed the completed signature count
     * @param total the total signer count
     * @param pending the pending signer count
     * @param percentage the completion percentage
     */
    public SigningProgress(int signed, int total, int pending, double percentage) {
        this.signed = signed;
        this.total = total;
        this.pending = pending;
        this.percentage = percentage;
    }

    /**
     * Returns the completed signature count.
     *
     * @return the completed signature count
     */
    public int getSigned() { return signed; }

    /**
     * Returns the total signer count.
     *
     * @return the total signer count
     */
    public int getTotal() { return total; }

    /**
     * Returns the pending signer count.
     *
     * @return the pending signer count
     */
    public int getPending() { return pending; }

    /**
     * Returns the completion percentage.
     *
     * @return the completion percentage
     */
    public double getPercentage() { return percentage; }
}
