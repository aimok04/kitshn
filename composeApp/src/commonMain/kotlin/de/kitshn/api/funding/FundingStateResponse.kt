package de.kitshn.api.funding

import kotlinx.serialization.Serializable

@Serializable
data class FundingStateResponse(
    val events: List<FundingEvent>,
    val costs: List<FundingCost>
)