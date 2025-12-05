package ru.fromchat

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

const val API_HOST = "https://fromchat.ru"
const val WS_API_HOST = "wss://fromchat.ru"

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