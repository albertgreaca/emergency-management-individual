package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

/**
 * Enum for the primary street type.
 */
enum class PrimaryStreetType(val rep: String) {
    MAIN_STREET("mainStreet"), SIDE_STREET("sideStreet"), COUNTY_ROAD("countyRoad");

    companion object {
        /**
         * Get the primary street type from an Identifier token.
         */
        fun fromIdentifier(identifierT: IdentifierT): Result<PrimaryStreetType> {
            return when (val type = values().find { it.rep == identifierT.toString() }) {
                null -> Result.failure("Primary street type not valid got $identifierT")
                else -> Result.success(type)
            }
        }
    }
}

/**
 * Enum for the secondary street type.
 */
enum class SecondaryStreetType(val rep: String) {
    ONEWAY_STREET("oneWayStreet"), TUNNEL("tunnel"), NONE("none");

    companion object {
        /**
         * Get the secondary street type from an Identifier token.
         */
        fun fromIdentifier(identifierT: IdentifierT): Result<SecondaryStreetType> {
            return identifierT.convertToString().ifSuccessFlat { id ->
                when (val type = values().find { it.rep == id }) {
                    null -> Result.failure("Secondary street type ($identifierT) not valid")
                    else -> Result.success(type)
                }
            }
        }
    }
}

/**
 * Class representing the Tokens of the map file.
 */
sealed class Token(private val representation: String) {
    override fun toString(): String {
        return representation
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        return representation == other.representation
    }

    override fun hashCode(): Int {
        return representation.hashCode()
    }
}

/**
 * Class representing a digraph token.
 */
class DigraphT : Token("digraph")

/**
 * Class representing a '{' token.
 */
class LBraceT : Token("{")

/**
 * Class representing a '}' token.
 */
class RBraceT : Token("}")

/**
 * Class representing a '[' token.
 */
class LBracketT : Token("[")

/**
 * Class representing a ']' token.
 */
class RBracketT : Token("]")

/**
 * Class representing a ; token.
 */
class SemicolonT : Token(";")

/**
 * Class representing a -> token.
 */
class RightarrowT : Token("->")

/**
 * Class representing a = token.
 */
class EqualsT : Token("=")

/**
 * Enum representing the street attributes.
 */
enum class StreetAttribute(val rep: String) {
    VILLAGE("village") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return value.convertToString().ifSuccess { builder.village(it) }
        }
    },
    NAME("name") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return value.convertToString().ifSuccess { builder.name(it) }
        }
    },
    HEIGHT_LIMIT("heightLimit") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return value.convertToValue().ifSuccessFlat {
                builder.heightLimit(it)
                if (it > 0) Result.success(Unit) else Result.failure("Road with height <= 0 found")
            }
        }
    },
    WEIGHT("weight") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return value.convertToValue().ifSuccessFlat {
                builder.weight(it)
                if (it > 0) Result.success(Unit) else Result.failure("Road with weight <= 0 found.")
            }
        }
    },
    PRIMARY_TYPE("primaryType") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return PrimaryStreetType.fromIdentifier(value).ifSuccess { builder.primaryType(it) }
        }
    },
    SECONDARY_TYPE("secondaryType") {
        override fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit> {
            return SecondaryStreetType.fromIdentifier(value).ifSuccess { builder.secondaryType(it) }
        }
    },
    ;

    /**
     * Add the attribute to the builder.
     */
    abstract fun addToBuilder(builder: StreetAttributes.Builder, value: IdentifierT): Result<Unit>
}

/**
 * Class representing a street attribute token.
 */
class AttributeT(val type: StreetAttribute) : IdentifierT(type.rep)

/**
 * Class representing a identifier token.
 */
open class IdentifierT(name: String) : Token(name) {
    /**
     * Convert the identifier to an Int .
     */
    fun convertToValue(): Result<Int> {
        if (toString().length > 1 && toString().startsWith("0")) {
            return Result.failure("Leading zeros are not allowed")
        }
        return when (val value = toString().toIntOrNull()) {
            null -> Result.failure("Value needs to be a number")
            else -> Result.success(value)
        }
    }

    /**
     * Convert to string if value only contains letters and underscores.
     * Otherwise return failure.
     */
    fun convertToString(): Result<String> {
        return if (toString().matches("[a-zA-Z][a-zA-Z_]*".toRegex())) {
            Result.success(toString())
        } else {
            Result.failure("String identifier: $this contains invalid characters")
        }
    }
}
