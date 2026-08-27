package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.enums.AssignmentMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Signer-placement rules shared by assignment creation and document-from-template creation. Both
 * routes accept the same delivery-method vocabulary and the same signing-order semantics, so the
 * checks live here once instead of in each resource.
 */
final class SigningRules {

    /** Documented {@code verification_method} values. */
    static final Set<String> VERIFICATION_METHODS = Set.of("Email", "Whatsapp", "DigitalCertificate");

    /** Documented {@code notification_methods} values. */
    static final Set<String> NOTIFICATION_METHODS = Set.of("Email", "Whatsapp");

    /** Documented {@code method} values, taken from the public enum so the two cannot drift. */
    static final Set<String> ASSIGNMENT_METHODS = Arrays.stream(AssignmentMethod.values())
            .map(AssignmentMethod::getValue)
            .collect(Collectors.toUnmodifiableSet());

    private static final String DIGITAL_CERTIFICATE = "DigitalCertificate";

    private SigningRules() {}

    /**
     * One signer's ordering inputs, decoupled from the request DTO that supplied them.
     *
     * @param step requested signing step, or {@code null} when the caller sends no order
     * @param verificationMethod requested verification method, or {@code null}
     */
    record Placement(Integer step, String verificationMethod) {}

    /**
     * Validate an assignment {@code method} value.
     *
     * @param method requested method, or {@code null} to accept the server/SDK default
     * @throws ValidationException if the method is not a documented value
     */
    static void validateMethod(String method) {
        if (method != null && !ASSIGNMENT_METHODS.contains(method)) {
            throw new ValidationException("Assignment method must be virtual or collect");
        }
    }

    /**
     * Validate one signer's delivery-method selection.
     *
     * @param verificationMethod requested verification method, or {@code null}
     * @param notificationMethods requested notification methods, or {@code null}
     * @throws ValidationException if a value is outside the documented vocabulary
     */
    static void validateDeliveryMethods(String verificationMethod, List<String> notificationMethods) {
        if (verificationMethod != null && !VERIFICATION_METHODS.contains(verificationMethod)) {
            throw new ValidationException("Verification method must be Email, Whatsapp, or DigitalCertificate");
        }
        if (notificationMethods != null && notificationMethods.stream().anyMatch(
                method -> method == null || !NOTIFICATION_METHODS.contains(method))) {
            throw new ValidationException("Notification methods must contain Email or Whatsapp");
        }
    }

    /**
     * Validate a signing order: either every signer supplies a step or none does, the supplied
     * steps are contiguous from 1, and a digital-certificate signer is alone in its step.
     *
     * @param placements one entry per signer, in request order
     * @param subject lowercase noun used in validation messages, such as {@code signer}
     * @throws ValidationException if the signing order is invalid
     */
    static void validateSigningOrder(List<Placement> placements, String subject) {
        boolean anyStep = false;
        boolean missingStep = false;
        Set<Integer> steps = new HashSet<>();
        Map<Integer, Integer> signersPerStep = new HashMap<>();
        for (Placement placement : placements) {
            Integer step = placement.step();
            anyStep |= step != null;
            missingStep |= step == null;
            signersPerStep.merge(step != null ? step : 1, 1, Integer::sum);
            if (step != null) steps.add(step);
        }
        if (anyStep && missingStep) {
            throw new ValidationException(
                    "Every " + subject + " must provide a step when signing order is used");
        }
        for (int step = 1; step <= steps.size(); step++) {
            if (!steps.contains(step)) {
                throw new ValidationException(capitalize(subject) + " steps must be contiguous starting at 1");
            }
        }
        for (Placement placement : placements) {
            if (DIGITAL_CERTIFICATE.equals(placement.verificationMethod())
                    && signersPerStep.get(placement.step() != null ? placement.step() : 1) > 1) {
                throw new ValidationException("A DigitalCertificate signer must be alone in its step");
            }
        }
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
