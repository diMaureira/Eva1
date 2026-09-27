package org.example

import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

fun main() = runBlocking {

    val petCare = PetCare()
    var continuar = true

    println("================================")
    println("             PETCARE")
    println("      Salud para tu Mascota     ")
    println("================================")

    while (continuar) {

        println("\n========== MENÚ ==========")
        println("1. Registrar entrada")
        println("2. Registrar salida")
        println("3. Mostrar estado de boxes")
        println("4. Mostrar consultas")
        println("5. Poner box fuera de servicio")
        println("6. Habilitar box")
        println("7. Cerrar turno")
        println("==========================")
        print("Seleccione una opción: ")

        val opcion = readlnOrNull()?.toIntOrNull()

        when (opcion) {

            // =====================================================
            // REGISTRAR ENTRADA
            // =====================================================
            1 -> {

                try {

                    println("\n===== REGISTRAR ENTRADA =====")

                    var codigo: String

                    while (true) {

                        print("Código de atención (0 para volver): ")
                        codigo = readln().uppercase()

                        // Permite cancelar y volver al menú principal.
                        if (codigo == "0") {
                            println("Registro de entrada cancelado.")
                            break
                        }

                        // Si el código es válido, continuamos con el registro.
                        if (petCare.validarCodigo(codigo)) {
                            break
                        }

                        println(
                            "Error: código inválido. " +
                                    "Debe tener dos letras, dos números y dos letras."
                        )
                    }

                    // Si el usuario escribió 0, vuelve al menú principal.
                    if (codigo == "0") {
                        continue
                    }

                    print("Nombre de la mascota: ")
                    val nombre = readln()

                    print("Especie o raza: ")
                    val especie = readln()

                    // ---------------------------------------------
                    // TIPO DE DUEÑO
                    // ---------------------------------------------

                    println("\nTipo de dueño:")
                    println("1. Particular")
                    println("2. Convenio")
                    println("3. Municipal")
                    print("Seleccione: ")

                    val opcionDueno =
                        readlnOrNull()?.toIntOrNull()

                    val tipoDueno = when (opcionDueno) {

                        1 -> TipoDueno.PARTICULAR

                        2 -> TipoDueno.CONVENIO

                        3 -> TipoDueno.MUNICIPAL

                        else -> null
                    }

                    if (tipoDueno == null) {

                        println(
                            "Error: tipo de dueño inválido."
                        )

                        continue
                    }

                    // ---------------------------------------------
                    // TIPO DE PACIENTE
                    // ---------------------------------------------

                    println("\nTipo de paciente:")
                    println("1. Canino")
                    println("2. Felino")
                    println("3. Exótico")
                    print("Seleccione: ")

                    val opcionPaciente =
                        readlnOrNull()?.toIntOrNull()

                    val paciente: Paciente? =
                        when (opcionPaciente) {

                            // CANINO
                            1 -> {

                                Canino(
                                    codigoAtencion = codigo,
                                    nombre = nombre,
                                    especie = especie,
                                    fechaIngreso = LocalDateTime.now(),
                                    tipoDueno = tipoDueno
                                )
                            }

                            // FELINO
                            2 -> {

                                Felino(
                                    codigoAtencion = codigo,
                                    nombre = nombre,
                                    especie = especie,
                                    fechaIngreso = LocalDateTime.now(),
                                    tipoDueno = tipoDueno
                                )
                            }

                            // EXÓTICO
                            3 -> {

                                print(
                                    "¿Es animal silvestre? (s/n): "
                                )

                                val respuesta =
                                    readln().lowercase()

                                val esSilvestre =
                                    when (respuesta) {

                                        "s" -> true

                                        "n" -> false

                                        else -> {

                                            println(
                                                "Error: debe ingresar s o n."
                                            )

                                            continue
                                        }
                                    }

                                Exotico(
                                    codigoAtencion = codigo,
                                    nombre = nombre,
                                    especie = especie,
                                    fechaIngreso = LocalDateTime.now(),
                                    tipoDueno = tipoDueno,
                                    esSilvestre = esSilvestre
                                )
                            }

                            else -> null
                        }

                    if (paciente == null) {

                        println(
                            "Error: tipo de paciente inválido."
                        )

                        continue
                    }

                    // Registramos la entrada mediante PetCare.
                    val resultado =
                        petCare.registrarEntrada(paciente)

                    mostrarResultado(resultado)

                } catch (e: Exception) {

                    println(
                        "Error al registrar entrada: " +
                                (e.message ?: "Datos inválidos.")
                    )
                }
            }

            // =====================================================
            // REGISTRAR SALIDA
            // =====================================================
            2 -> {

                try {

                    println("\n===== REGISTRAR SALIDA =====")

                    print("Código de atención: ")

                    val codigo =
                        readln().uppercase()

                    print(
                        "Tiempo de atención en minutos: "
                    )

                    val tiempoMinutos =
                        readlnOrNull()?.toIntOrNull()

                    if (tiempoMinutos == null) {

                        println(
                            "Error: debe ingresar un número válido."
                        )

                        continue
                    }

                    if (tiempoMinutos <= 0) {

                        println(
                            "Error: el tiempo debe ser mayor que cero."
                        )

                        continue
                    }

                    val resultado =
                        petCare.registrarSalida(
                            codigo,
                            tiempoMinutos
                        )

                    mostrarResultado(resultado)

                } catch (e: Exception) {

                    println(
                        "Error al registrar salida: " +
                                (e.message ?: "Datos inválidos.")
                    )
                }
            }

            // =====================================================
            // MOSTRAR BOXES
            // =====================================================
            3 -> {

                petCare.mostrarBoxes()
            }

            // =====================================================
            // CONSULTAS
            // =====================================================
            4 -> {

                petCare.mostrarConsultas()
            }

            5 -> {

                print("Número de box: ")
                val numeroBox = readlnOrNull()?.toIntOrNull()

                if (numeroBox == null) {
                    println("Error: número de box inválido.")
                    continue
                }

                print("Motivo: ")
                val motivo = readln()

                val resultado =
                    petCare.ponerBoxFueraDeServicio(
                        numeroBox,
                        motivo
                    )

                mostrarResultado(resultado)
            }

            6 -> {

                print("Número de box: ")
                val numeroBox = readlnOrNull()?.toIntOrNull()

                if (numeroBox == null) {
                    println("Error: número de box inválido.")
                    continue
                }

                val resultado =
                    petCare.habilitarBox(numeroBox)

                mostrarResultado(resultado)
            }

            // =====================================================
            // CERRAR TURNO
            // =====================================================
            7 -> {

                petCare.generarReporteCierre()

                println("\nTurno finalizado.")
                println("Gracias por utilizar PetCare.")

                continuar = false
            }

            // =====================================================
            // OPCIÓN INCORRECTA
            // =====================================================
            else -> {

                println(
                    "Opción inválida. " +
                            "Seleccione una opción del 1 al 7."
                )
            }
        }
    }
}


// =============================================================
// FUNCIÓN PARA MOSTRAR EL RESULTADO DE LAS OPERACIONES
// =============================================================

fun mostrarResultado(
    resultado: ResultadoOperacion
) {

    when (resultado) {

        is ResultadoOperacion.Exito -> {

            println(
                "\nÉxito: ${resultado.mensaje}"
            )
        }

        is ResultadoOperacion.Error -> {

            println(
                "\nError: ${resultado.mensaje}"
            )
        }
    }
}