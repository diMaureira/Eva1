package org.example

import java.time.LocalDateTime

class Felino(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: LocalDateTime,
    tipoDueno: TipoDueno
) : Paciente(
    codigoAtencion,
    nombre,
    especie,
    fechaIngreso,
    tipoDueno
) {

    private val tarifaBase: Double = 9000.0

    override fun calcularCosto(tiempoMinutos: Int): Double {

        if (tiempoMinutos < 20) {
            return 0.0
        }

        val horas = tiempoMinutos / 60.0
        return horas * tarifaBase
    }
}