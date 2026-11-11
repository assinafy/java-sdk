package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class NotificationRules {
    static final Set<String> EMAIL_CODES = Set.of(
            "DocumentCompleted", "SignerDeclined", "DocumentCancelled",
            "DocumentAboutToExpire", "DocumentExpired", "DocumentExpirationReset",
            "DocumentProcessingFailed", "TemplateProcessingFailed", "SignerWhatsappFailed",
            "ContractRenewalNotice", "ContractExpiring", "ContractExpired", "ContractRenewed");
    static final Set<String> PUSH_CODES = Set.of(
            "SignerViewed", "SignerSigned", "DocumentCompleted", "SignerDeclined",
            "DocumentAboutToExpire", "DocumentExpired", "SignerIdentityReviewRequired", "SignerWhatsappFailed");
    static final Set<String> POLICY_CODES;

    static {
        Set<String> codes = new HashSet<>(EMAIL_CODES);
        codes.addAll(PUSH_CODES);
        POLICY_CODES = Set.copyOf(codes);
    }

    private NotificationRules() {}

    static void validate(Map<?, ?> changes, Set<String> allowed) {
        if (changes == null || changes.isEmpty()) throw new ValidationException("Notification changes are required");
        changes.forEach((code, enabled) -> {
            if (!(code instanceof String name) || !allowed.contains(name) || !(enabled instanceof Boolean)) {
                throw new ValidationException("Unknown notification code or nonboolean value");
            }
        });
    }
}
