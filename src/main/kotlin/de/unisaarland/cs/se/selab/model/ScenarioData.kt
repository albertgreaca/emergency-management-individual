package de.unisaarland.cs.se.selab.model

/**
 * Data class for the scenario data.
 */
data class ScenarioData(
    val emergencies: Collection<Emergency>,
    val events: Collection<Event>
)
