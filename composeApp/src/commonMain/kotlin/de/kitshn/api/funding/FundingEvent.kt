package de.kitshn.api.funding

import kotlinx.serialization.Serializable

@Serializable
class FundingEvent(
    val type: String,
    val percentage: Float,
    val goal: Double,
    val remainingSubscriptions: Int,
    val additionalContent: String?,
    val lastUpdate: Long
) {
    fun isBanner() =
        type == "banner"
}