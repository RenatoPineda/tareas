fun main() {

    // ----- CLASE ESTUDIANTE -----
    class Estudiante(val nombre: String) {
        val notas: MutableList<Double> = mutableListOf()

        fun promedio(): Double {
            return if (notas.isNotEmpty()) notas.average() else 0.0
        }

        fun notaMasAlta(): Double {
            return if (notas.isNotEmpty()) notas.maxOrNull()!! else 0.0
        }

        fun notaMasBaja(): Double {
            return if (notas.isNotEmpty()) notas.minOrNull()!! else 0.0
        }

        fun obtenerNivel(): String {
            val prom = promedio()
            return when {
                prom >= 90 -> "Excelente"
                prom >= 60 -> "Aprobado"
                else -> "Reprobado"
            }
        }

        fun agregarNota(nota: Double) {
            if (nota in 0.0..100.0) {
                notas.add(nota)
            } else {
                println("Nota inválida ($nota). Debe estar entre 0 y 100.")
            }
        }
    }

    // ----- CLASE CURSO -----
    class Curso(val nombre: String) {
        val estudiantes: MutableList<Estudiante> = mutableListOf()

        fun promedioGeneral(): Double {
            return if (estudiantes.isNotEmpty()) {
                estudiantes.map { it.promedio() }.average()
            } else 0.0
        }

        fun estudiantesAprobados(): List<Estudiante> {
            return estudiantes.filter { it.promedio() >= 60 }
        }

        fun estudiantesReprobados(): List<Estudiante> {
            return estudiantes.filter { it.promedio() < 60 }
        }

        fun mejorEstudiante(): Estudiante? {
            return estudiantes.maxByOrNull { it.promedio() }
        }

        fun generarReporte() {
            println("===== REPORTE DEL CURSO: $nombre =====")

            for (e in estudiantes) {
                println("\nEstudiante: ${e.nombre}")
                println("Notas: ${e.notas}")
                println("Promedio: ${"%.2f".format(e.promedio())}")
                println("Mayor: ${e.notaMasAlta()}")
                println("Menor: ${e.notaMasBaja()}")
                println("Nivel: ${e.obtenerNivel()}")
            }

            println("\n--- RESUMEN GENERAL ---")
            println("Promedio del curso: ${"%.2f".format(promedioGeneral())}")

            println("\nAprobados:")
            estudiantesAprobados().forEach { println("- ${it.nombre}") }

            println("\nReprobados:")
            estudiantesReprobados().forEach { println("- ${it.nombre}") }

            val mejor = mejorEstudiante()
            if (mejor != null) {
                println("\nMejor estudiante: ${mejor.nombre} con promedio ${"%.2f".format(mejor.promedio())}")
            }
        }
    }

    // ----- MAIN -----
    val curso = Curso("Programación")

    val e1 = Estudiante("Juan")
    e1.agregarNota(85.0)
    e1.agregarNota(90.0)
    e1.agregarNota(88.0)

    val e2 = Estudiante("María")
    e2.agregarNota(95.0)
    e2.agregarNota(92.0)
    e2.agregarNota(98.0)

    val e3 = Estudiante("Carlos")
    e3.agregarNota(55.0)
    e3.agregarNota(60.0)
    e3.agregarNota(58.0)

    val e4 = Estudiante("Ana")
    e4.agregarNota(70.0)
    e4.agregarNota(75.0)
    e4.agregarNota(80.0)

    curso.estudiantes.addAll(listOf(e1, e2, e3, e4))

    curso.generarReporte()
}