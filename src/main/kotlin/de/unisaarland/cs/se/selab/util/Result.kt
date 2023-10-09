package de.unisaarland.cs.se.selab.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A simple result type that represents either [Success] or [Failure].
 */
sealed class Result<out T> {

    companion object {
        /** Creates a [Success] with the given [value]. */
        fun <T> success(value: T): Result<T> = Success(value)

        /** Creates a [Failure] with the given [reason]. */
        fun <T> failure(reason: String): Result<T> = Failure(reason)
    }
}

/** A successful [Result] containing a [value]. */
data class Success<T>(val value: T) : Result<T>()

/** A failed [Result] containing a [reason]. */
data class Failure(val reason: String) : Result<Nothing>()

/**
 * If this [Result] is a [Success], executes the given [function][fn] with the result's
 * [value][Success.value].
 * The result of [fn] is then unpacked (flattened) to prevent nesting of [Result] objects.
 * Otherwise, the existing [Failure] is passed on.
 *
 * Corresponds to the operator ``flatMap``.
 */
@OptIn(ExperimentalContracts::class)
fun <T, R> Result<T>.ifSuccessFlat(fn: (T) -> Result<R>): Result<R> {
    contract {
        callsInPlace(fn, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Success<T> -> fn(value)
        is Failure -> this
    }
}

/**
 * Returns the [value][Success.value] if this [Result] is [Success], or throws the [Exception] if it is [Failure].
 * Only use this if the Result is guaranteed to be a [Success] and a [Failure] is considered an exceptional case.
 */
fun <T> Result<T>.getOrThrow(exception: Exception): T {
    return when (this) {
        is Success<T> -> value
        is Failure -> throw exception
    }
}

/**
 * If this [Result] is a [Success], executes the given [function][fn] with the result's
 * [value][Success.value].
 * The return value of [fn] is then re-packed into a [Success] object.
 * Otherwise, the existing [Failure] is passed on.
 *
 * Corresponds to the operator ``map``.
 */
@OptIn(ExperimentalContracts::class)
fun <T, R> Result<T>.ifSuccess(fn: (T) -> R): Result<R> {
    contract {
        callsInPlace(fn, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Success<T> -> Success(fn(value))
        is Failure -> Failure(reason)
    }
}

/**
 * Maps this [Result<T>][Result] to [R] by either applying the function [success]
 * if this [Result] is [Success], or the function [failure] this [Failure].
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> Result<T>.andThen(success: (T) -> R, failure: () -> R): R {
    contract {
        callsInPlace(success, InvocationKind.AT_MOST_ONCE)
        callsInPlace(failure, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Success<T> -> success(value)
        is Failure -> failure()
    }
}

/**
 * Executes one of two functions depending on the type of result:
 *  - if the result is a [Success], the function [success] is called
 *  - if the result is a [Failure], the function [failure] is called
 *
 * Corresponds to the operator ``fold``.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> Result<T>.andThen(success: (T) -> R, failure: (String) -> R): R {
    contract {
        callsInPlace(success, InvocationKind.AT_MOST_ONCE)
        callsInPlace(failure, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Success<T> -> success(value)
        is Failure -> failure(reason)
    }
}
