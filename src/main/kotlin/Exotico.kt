package org.example

import java.time.LocalDateTime

class Exotico(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: LocalDateTime,
    tipoDueno: TipoDueno,
    val esSilvestre: Boolean
) : Paciente(
    codigoAtencion,
    nombre,
    especie,
    fechaIngreso,
    tipoDueno
) {

    private val tarifaBase: Double = 20000.0

    override fun calcularCosto(tiempoMinutos: Int): Double {

        val horas = tiempoMinutos / 60.0
        var costo = horas * tarifaBase

        if (esSilvestre) {
            costo *= 1.30
        }

        return costo
    }
}