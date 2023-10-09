package de.unisaarland.cs.se.selab.parser

/**
 * A lexer for the map file.
 */
class MapLexer(private val map: String) {

    private fun readLines(): List<String> = map.lines()

    /**
     * Tokenizes the map file.
     *
     * @return a list of tokens
     */
    fun tokenize(): ArrayDeque<Token> {
        val tokenList: ArrayDeque<Token> = ArrayDeque()
        for (line in readLines()) {
            var l = line
            for (symbol in listOf("[", "]", "{", "}", ",", ";", "=", "->")) {
                l = l.replace(symbol, " $symbol ")
            }
            val rawLineStrings = l.split("\\s".toRegex()).toMutableList()
            rawLineStrings.removeAll(listOf("", null))
            val lineStrings = rawLineStrings.toList()

            for (tokenString in lineStrings) {
                tokenList += stringToToken(tokenString)
            }
        }
        return tokenList
    }

    private fun stringToToken(tokenString: String): Token {
        return when (tokenString) {
            ";" -> SemicolonT()
            "=" -> EqualsT()
            "[" -> LBracketT()
            "]" -> RBracketT()
            "{" -> LBraceT()
            "}" -> RBraceT()
            "->" -> RightarrowT()
            StreetAttribute.VILLAGE.rep -> AttributeT(StreetAttribute.VILLAGE)
            StreetAttribute.NAME.rep -> AttributeT(StreetAttribute.NAME)
            StreetAttribute.HEIGHT_LIMIT.rep -> AttributeT(StreetAttribute.HEIGHT_LIMIT)
            StreetAttribute.PRIMARY_TYPE.rep -> AttributeT(StreetAttribute.PRIMARY_TYPE)
            StreetAttribute.SECONDARY_TYPE.rep -> AttributeT(StreetAttribute.SECONDARY_TYPE)
            StreetAttribute.WEIGHT.rep -> AttributeT(StreetAttribute.WEIGHT)
            else -> IdentifierT(tokenString)
        }
    }
}
