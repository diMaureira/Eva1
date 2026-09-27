package org.example

class Canino(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: java.time.LocalDateTime,
    tipoDueno: TipoDueno
) : Paciente(
    codigoAtencion,
    nombre,
    especie,
    fechaIngreso,
    tipoDueno
) {

    private val tarifaBase: Double = 12000.0

    override fun calcularCosto(tiempoMinutos: Int): Double {

        val horas = tiempoMinutos / 60.0
        var costo = horas * tarifaBase

        if (tipoDueno == TipoDueno.CONVENIO) {
            costo *= 0.80
        }

        return costo
    }
}