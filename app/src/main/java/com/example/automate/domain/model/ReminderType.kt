package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReminderType {
    VEHICLE_LICENCE,
    VEHICLE_INSPECTION,
    INSURANCE,
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
    TRANSMISSION_OIL,
    GENERAL_SERVICE,
    DRIVING_LICENCE,
    OTHER
}
