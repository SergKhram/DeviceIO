package io.github.sergkhram.managers.adb

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Logger {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)
}