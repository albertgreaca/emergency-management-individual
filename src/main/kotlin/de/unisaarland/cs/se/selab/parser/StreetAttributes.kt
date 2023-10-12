package de.unisaarland.cs.se.selab.parser

import de.unisaarland.cs.se.selab.parser.config.Constants
import de.unisaarland.cs.se.selab.util.Result
import de.unisaarland.cs.se.selab.util.ifSuccess
import de.unisaarland.cs.se.selab.util.ifSuccessFlat

/**
 * Data class for the parsed street attributes
 */
data class StreetAttributes(
    val village: String,
    val name: String,
    val heightLimit: Int,
    val primaryType: PrimaryStreetType,
    val oneWay: Boolean,
    val weight: Int
) {

    /**
     * Enum class for the primary street type
     */
    class Builder {
        private lateinit var village: String
        private lateinit var name: String
        private var heightLimit: Int = 0
        private lateinit var primaryType: PrimaryStreetType
        private lateinit var secondaryType: SecondaryStreetType
        private var weight: Int = 0

        /**
         * Set the village
         */
        fun village(value: String) = apply { this.village = value }

        /**
         * Set the name
         */
        fun name(value: String) = apply { this.name = value }

        /**
         * Set the height limit
         */
        fun heightLimit(value: Int) = apply { this.heightLimit = value }

        /**
         * Set the primary street type
         */
        fun primaryType(value: PrimaryStreetType) = apply { this.primaryType = value }

        /**
         * Set the secondary street type
         */
        fun secondaryType(value: SecondaryStreetType) = apply { this.secondaryType = value }

        /**
         * Set the weight
         */
        fun weight(value: Int) = apply { this.weight = value }

        /**
         * Build the street attributes
         */
        fun checkAndBuild(): Result<StreetAttributes> {
            if (weight < 0) {
                return Result.failure("The length of roads has to be greater than  0 ")
            }
            if (secondaryType == SecondaryStreetType.TUNNEL && heightLimit > Constants.MAX_TUNNEL_HEIGHT) {
                return Result.failure("Tunnel to high!")
            }
            return Result.success(
                StreetAttributes(
                    village,
                    name,
                    heightLimit,
                    primaryType,
                    secondaryType == SecondaryStreetType.ONEWAY_STREET,
                    weight
                )
            )
        }
    }

    companion object {
        /**
         * Parse the attribute list
         */
        fun parseAttributeList(tokenList: ArrayDeque<Token>): Result<StreetAttributes> {
            val attributesBuilder = Builder()
            return tokenList.parseNextToken<LBracketT>("Opening bracket expected for attribute list. Found: %s")
                .ifSuccessFlat {
                    var loopResult = Result.success(Unit)
                    StreetAttribute.values().forEach { requiredAttribute ->
                        loopResult =
                            loopResult.ifSuccessFlat {
                                tokenList.parseNextToken<AttributeT>("Unexpected attribute found: %s")
                            }.ifSuccessFlat { attribute ->
                                if (attribute.type == requiredAttribute) {
                                    tokenList.parseNextToken<EqualsT>("Expected equals")
                                } else {
                                    Result.failure(
                                        "Expected attribute: ${requiredAttribute.rep}" +
                                            " found: ${attribute.type.rep}"
                                    )
                                }
                                    .ifSuccessFlat { _ ->
                                        tokenList.parseNextToken<IdentifierT>("Expected a Value for an $attribute")
                                            .ifSuccessFlat { attribute.type.addToBuilder(attributesBuilder, it) }
                                    }
                            }.ifSuccessFlat {
                                tokenList.parseNextToken<SemicolonT>(
                                    "Expected Semicolon to end Attribute statement found: %s"
                                )
                            }.ifSuccess { }
                    }
                    return@ifSuccessFlat loopResult
                }.ifSuccessFlat {
                    tokenList.parseNextToken<RBracketT>("Expect attributes to end with ]")
                }.ifSuccessFlat { attributesBuilder.checkAndBuild() }
        }
    }
}
