package de.unisaarland.cs.se.selab.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Assertion utilities.
 */
object Assertion {
    /**
     * Assert that the given value is not null.
     *
     * If null, an assertion error is thrown.
     * Otherwise, a contract ensures that the value can be used with its
     * non-nullable type.
     *
     * @param value value to check for null
     */
    @OptIn(ExperimentalContracts::class)
    fun <T> assertNotNull(value: T?): T {
        contract {
            returns() implies (value != null)
        }
        if (value == null) {
            throw AssertionError("Value is null.")
        }
        return value
    }
}
