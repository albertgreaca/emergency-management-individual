package de.unisaarland.cs.se.selab.model.assets

import de.unisaarland.cs.se.selab.controller.EmergencyResponse

/**
 * This class represents a request for assets.
 */
data class AssetRequest(
    val id: Int,
    val base: Base<*>,
    val emergencyResponse: EmergencyResponse,
    val assetInquiry: AssetInquiry,
    val checkedBases: List<Int>
)
