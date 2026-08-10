package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MaintenanceItemType {
    ENGINE_OIL,
    OIL_FILTER,
    AIR_FILTER,
    CABIN_FILTER,
    BRAKE_PADS,
    BRAKE_DISCS,
    BRAKE_FLUID,
    COOLANT,
    SPARK_PLUGS,
    BATTERY,
    TIRES,
    TIMING_BELT,
    TIMING_CHAIN,
    TRANSMISSION_OIL,
    FUEL_FILTER,
    OTHER
}
