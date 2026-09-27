package org.example

import kotlinx.coroutines.delay

class PetCare {

    val nombreSistema: String = "PetCare"
    val capacidad: Int = 10

    private val boxes = MutableList(capacidad) { indice ->
        Box(indice + 1)
    }

    private val historial = mutableListOf<Ticket>()

    private var recaudacionTotal: Double = 0.0

    private var siguienteTicket: Int = 1

    // Busca el primer box disponible.
    fun buscarBoxLibre(): Box? {
        return boxes.firstOrNull {
            it.estado is EstadoBox.Libre
        }
    }

    // Cuenta cuántos boxes están disponibles.
    fun cantidadBoxesDisponibles(): Int {
        return boxes.count {
            it.estado is EstadoBox.Libre
        }
    }

    // Busca el box donde está siendo atendido un paciente.
    fun buscarBoxPorCodigo(codigo: String): Box? {
        return boxes.firstOrNull { box ->

            val estado = box.estado

            estado is EstadoBox.EnAtencion &&
                    estado.paciente.codigoAtencion == codigo
        }
    }

    // Valida el formato: dos letras, dos números y dos letras.
    fun validarCodigo(codigo: String): Boolean {
        val patron = Regex("^[A-Za-z]{2}[0-9]{2}[A-Za-z]{2}$")
        return patron.matches(codigo)
    }

    // Muestra el estado actual de todos los boxes.
    fun mostrarBoxes() {

        println("\n===== ESTADO DE BOXES =====")

        boxes.forEach { box ->

            when (val estado = box.estado) {

                is EstadoBox.Libre -> {
                    println("Box ${box.numero}: Libre")
                }

                is EstadoBox.EnAtencion -> {
                    println(
                        "Box ${box.numero}: En atención - " +
                                "${estado.paciente.nombre} - " +
                                "${estado.paciente.codigoAtencion} - " +
                                "${estado.paciente.especie}"
                    )
                }

                is EstadoBox.EnProceso -> {
                    println(
                        "Box ${box.numero}: En proceso - ${estado.motivo}"
                    )
                }

                is EstadoBox.FueraDeServicio -> {
                    println(
                        "Box ${box.numero}: Fuera de servicio - ${estado.motivo}"
                    )
                }
            }
        }
    }

    // Registra la entrada de un paciente.
    suspend fun registrarEntrada(
        paciente: Paciente
    ): ResultadoOperacion {

        if (!validarCodigo(paciente.codigoAtencion)) {

            return ResultadoOperacion.Error(
                "Código de atención inválido: ${paciente.codigoAtencion}"
            )
        }

        if (buscarBoxPorCodigo(paciente.codigoAtencion) != null) {

            return ResultadoOperacion.Error(
                "Ya existe un paciente con el código " +
                        paciente.codigoAtencion
            )
        }

        val box = buscarBoxLibre()
            ?: return ResultadoOperacion.Error(
                "Sistema sin capacidad. No hay boxes disponibles."
            )

        box.estado = EstadoBox.EnProceso(
            "Registrando entrada de ${paciente.nombre}"
        )

        println("Box ${box.numero}: registrando entrada...")
        println("Esperando confirmación...")

        delay(3000)

        box.estado = EstadoBox.EnAtencion(paciente)

        return ResultadoOperacion.Exito(
            "${paciente.nombre} registrado correctamente " +
                    "en Box ${box.numero}"
        )
    }

    // Registra la salida de un paciente.
    suspend fun registrarSalida(
        codigo: String,
        tiempoMinutos: Int
    ): ResultadoOperacion {

        if (tiempoMinutos <= 0) {

            return ResultadoOperacion.Error(
                "El tiempo de atención debe ser mayor que cero."
            )
        }

        val box = buscarBoxPorCodigo(codigo)
            ?: return ResultadoOperacion.Error(
                "Paciente con código $codigo no encontrado."
            )

        val estadoActual = box.estado

        if (estadoActual !is EstadoBox.EnAtencion) {

            return ResultadoOperacion.Error(
                "El box ${box.numero} no está en atención."
            )
        }

        val paciente = estadoActual.paciente

        box.estado = EstadoBox.EnProceso(
            "Calculando tarifa de ${paciente.nombre}"
        )

        println("Box ${box.numero}: procesando salida...")
        println("Calculando tarifa...")

        delay(6500)

        try {

            // Cada tipo de paciente ejecuta su propio cálculo.
            val costoBase = paciente.calcularCosto(tiempoMinutos)

            /*
             * Un coste menor o igual a cero normalmente es inválido.
             * El felino con menos de 20 minutos es la excepción,
             * porque su coste válido es $0.
             */
            if (
                costoBase <= 0.0 &&
                !(paciente is Felino && tiempoMinutos < 20)
            ) {

                throw IllegalArgumentException(
                    "El resultado de la tarifa es inválido."
                )
            }

            // Se aplica primero el IVA del 19%.
            var montoFinal = costoBase * 1.19

            // Luego se aplica el beneficio municipal.
            if (paciente.tipoDueno == TipoDueno.MUNICIPAL) {
                montoFinal *= 0.50
            }

            val ticket = Ticket(
                numeroTicket = siguienteTicket,
                paciente = paciente,
                tiempoMinutos = tiempoMinutos,
                montoPagado = montoFinal
            )

            siguienteTicket++

            historial.add(ticket)

            recaudacionTotal += montoFinal

            box.estado = EstadoBox.Libre

            return ResultadoOperacion.Exito(
                "Salida registrada. " +
                        "Ticket ${ticket.numeroTicket} - " +
                        "Paciente: ${paciente.nombre} - " +
                        "Monto: $${"%.0f".format(montoFinal)}"
            )

        } catch (e: IllegalArgumentException) {

            // Si ocurre un error, restauramos el paciente en el box.
            box.estado = EstadoBox.EnAtencion(paciente)

            return ResultadoOperacion.Error(
                e.message ?: "Error al calcular la tarifa."
            )
        }
    }

    // Devuelve los pacientes atendidos cuyo dueño tiene convenio.
    fun pacientesConvenio(): List<Paciente> {

        return historial
            .filter {
                it.paciente.tipoDueno == TipoDueno.CONVENIO
            }
            .map {
                it.paciente
            }
    }

    // Calcula el ingreso promedio por paciente finalizado.
    fun calcularIngresoPromedio(): Double {

        if (historial.isEmpty()) {
            return 0.0
        }

        return historial.map {
            it.montoPagado
        }.average()
    }

    // Obtiene los códigos de todos los pacientes finalizados.
    fun codigosFinalizados(): List<String> {

        return historial.map {
            it.paciente.codigoAtencion
        }
    }

    // Busca el paciente que tuvo el mayor tiempo de atención.
    fun pacienteMayorTiempo(): Paciente? {

        return historial
            .maxByOrNull {
                it.tiempoMinutos
            }
            ?.paciente
    }

    // Calcula cuánto dinero generó cada tipo de paciente.
    fun recaudacionPorTipo(): Map<String, Double> {

        return historial
            .groupBy { ticket ->

                when (ticket.paciente) {

                    is Canino -> "Canino"

                    is Felino -> "Felino"

                    is Exotico -> "Exotico"

                    else -> "Otro"
                }
            }
            .mapValues { entrada ->
                entrada.value.sumOf {
                    it.montoPagado
                }
            }
    }

    // Obtiene el tipo de paciente que produjo más ingresos.
    fun tipoMayorIngreso(): String {

        val recaudacion = recaudacionPorTipo()

        if (recaudacion.isEmpty()) {
            return "Sin datos"
        }

        return recaudacion
            .maxByOrNull {
                it.value
            }
            ?.key ?: "Sin datos"
    }

    // Muestra las consultas solicitadas por el sistema.
    fun mostrarConsultas() {

        println("\n===== CONSULTAS PETCARE =====")

        println(
            "Boxes disponibles: ${cantidadBoxesDisponibles()}"
        )

        println("\nPacientes de convenio:")

        val convenio = pacientesConvenio()

        if (convenio.isEmpty()) {

            println("No hay pacientes de convenio finalizados.")

        } else {

            convenio.forEach { paciente ->

                println(
                    "${paciente.codigoAtencion} - ${paciente.nombre}"
                )
            }
        }

        println(
            "\nIngreso promedio: " +
                    "$${"%.0f".format(calcularIngresoPromedio())}"
        )

        println("\nCódigos finalizados:")

        val codigos = codigosFinalizados()

        if (codigos.isEmpty()) {

            println("No hay pacientes finalizados.")

        } else {

            codigos.forEach { codigo ->
                println(codigo)
            }
        }

        val pacienteMayorTiempo = pacienteMayorTiempo()

        if (pacienteMayorTiempo != null) {

            println(
                "\nPaciente con mayor tiempo: " +
                        "${pacienteMayorTiempo.nombre} " +
                        "(${pacienteMayorTiempo.codigoAtencion})"
            )

        } else {

            println(
                "\nPaciente con mayor tiempo: Sin datos"
            )
        }

        println("\nRecaudación por tipo:")

        val recaudacionTipo = recaudacionPorTipo()

        if (recaudacionTipo.isEmpty()) {

            println("Sin datos.")

        } else {

            recaudacionTipo.forEach { (tipo, monto) ->

                println(
                    "$tipo: $${"%.0f".format(monto)}"
                )
            }
        }
    }

    // Genera el reporte al finalizar el turno.
    fun generarReporteCierre() {

        println("\n================================")
        println("      REPORTE DE CIERRE")
        println("          $nombreSistema")
        println("================================")

        if (historial.isEmpty()) {

            println("No se registraron atenciones finalizadas.")

        } else {

            historial.forEach { ticket ->

                val tipoPaciente = when (ticket.paciente) {

                    is Canino -> "Canino"

                    is Felino -> "Felino"

                    is Exotico -> "Exotico"

                    else -> "Paciente"
                }

                println("\nTicket N° ${ticket.numeroTicket}")
                println("Tipo: $tipoPaciente")

                println(
                    "Código: ${ticket.paciente.codigoAtencion}"
                )

                println(
                    "Paciente: ${ticket.paciente.nombre}"
                )

                println(
                    "Tiempo: ${ticket.tiempoMinutos} minutos"
                )

                println(
                    "Monto pagado: " +
                            "$${"%.0f".format(ticket.montoPagado)}"
                )

                // El exótico debe indicar si es silvestre.
                if (ticket.paciente is Exotico) {

                    println(
                        "Silvestre: " +
                                if (ticket.paciente.esSilvestre) {
                                    "Sí"
                                } else {
                                    "No"
                                }
                    )
                }

                println("--------------------------------")
            }
        }

        println(
            "\nTotal recaudado: " +
                    "$${"%.0f".format(recaudacionTotal)}"
        )

        println(
            "Cantidad de pacientes atendidos: ${historial.size}"
        )

        println(
            "Ingreso promedio: " +
                    "$${"%.0f".format(calcularIngresoPromedio())}"
        )

        println(
            "Tipo con mayor ingreso: ${tipoMayorIngreso()}"
        )

        println(
            "Boxes disponibles al cierre: " +
                    cantidadBoxesDisponibles()
        )

        println("================================")
    }
}