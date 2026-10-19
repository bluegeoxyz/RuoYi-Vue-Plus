package org.dromara.property.charge.receivable.service.impl;

record BindingGenerationOutcome(Type type, String message) {
    enum Type {
        GENERATED,
        SKIPPED_EXISTING,
        SKIPPED_NOT_DUE,
        FAILED
    }

    static BindingGenerationOutcome of(Type type) {
        return new BindingGenerationOutcome(type, null);
    }

    static BindingGenerationOutcome failed(String message) {
        return new BindingGenerationOutcome(Type.FAILED, message);
    }
}
