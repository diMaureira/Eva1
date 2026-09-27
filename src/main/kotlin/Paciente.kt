package org.example

import java.time.LocalDateTime

open class Paciente(
    val codigoAtencion: String,
    val nombre: String,
    val especie: String,
    val fechaIngreso: LocalDateTime,
    val tipoDueno: TipoDueno
) {

    open fun calcularCosto(tiempoMinutos: Int): Double {
        return 0.0
    }
}