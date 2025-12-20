package com.pr0gramm3r101.utils

import androidx.annotation.IntDef
import org.jetbrains.annotations.Contract


object JobIdManager {
    const val JOB_TYPE_CHANNEL_PROGRAMS: Int = 1
    const val JOB_TYPE_CHANNEL_METADATA: Int = 2
    const val JOB_TYPE_CHANNEL_DELETION: Int = 3
    const val JOB_TYPE_CHANNEL_LOGGER: Int = 4

    const val JOB_TYPE_USER_PREFS: Int = 11
    const val JOB_TYPE_USER_BEHAVIOR: Int = 21

    //16-1 for short. Adjust per your needs
    private const val JOB_TYPE_SHIFTS = 15

    @Contract(pure = true)
    fun getJobId(@JobType jobType: Int, objectId: Int): Int {
        if (objectId > 0 && objectId < (1 shl JOB_TYPE_SHIFTS)) {
            return (jobType shl JOB_TYPE_SHIFTS) + objectId
        } else {
            throw IllegalArgumentException(String.format(
                "objectId %s must be between %s and %s",
                objectId, 0, (1 shl JOB_TYPE_SHIFTS)
            ))
        }
    }

    @IntDef(
        value = [
            JOB_TYPE_CHANNEL_PROGRAMS,
            JOB_TYPE_CHANNEL_METADATA,
            JOB_TYPE_CHANNEL_DELETION,
            JOB_TYPE_CHANNEL_LOGGER,
            JOB_TYPE_USER_PREFS,
            JOB_TYPE_USER_BEHAVIOR
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    private annotation class JobType
}