package com.example.moonsyncapp.data

import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import java.time.LocalDate

/**
 * Single source of truth for cycle data.
 * Both HomeScreen (CycleViewModel) and CalendarScreen (CalendarViewModel) read from here,
 * so all screens always show the same dates and phase information.
 *
 * Backend team: replace getCycleData() with a real network/database fetch.
 */
object CycleRepository {

    fun getCycleData(): CycleData {
        val today = LocalDate.now()
        return CycleData(
            userName = "Ada",
            currentPhase = CyclePhase.FOLLICULAR,
            cycleDay = 10,
            cycleLength = 28,
            daysUntilNextPeriod = 18,
            daysUntilOvulation = 4,
            periodDaysRemaining = null,
            phaseProgress = 0.625f,
            phaseDaysRemaining = 4,
            phaseTotalDays = 9,
            nextPeriodDate = today.plusDays(18),
            ovulationDate = today.plusDays(4)
        )
    }
}
