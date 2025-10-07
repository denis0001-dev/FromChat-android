package ru.fromchat

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

const val API_HOST = "http://10.0.2.2:8301"
const val WS_API_HOST = "ws://10.0.2.2:8301"

val DATETIME_FORMAT = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()

    char(' ')

    hour()
    char(':')
    minute()
}