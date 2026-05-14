package org.example.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.example"])
class GaragemApplication

fun main(args: Array<String>) {
    runApplication<GaragemApplication>(*args)
}

