package de.unisaarland.cs.se.selab.model.assets

/**
 * Data Class containing the inquiries for all three emergency types.
 * @param policeInquiry The inquiry for the police.
 * @param ambulanceInquiry The inquiry for the ambulance.
 * @param fireInquiry The inquiry for the fire department.
 */
data class InquiryTriple(
    val policeInquiry: AssetInquiry,
    val ambulanceInquiry: AssetInquiry,
    val fireInquiry: AssetInquiry
)
