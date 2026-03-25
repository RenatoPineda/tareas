// Data class con propiedad calculada
data class Estudiante(
    val nombre: String,
    val grado: String,
    val notas: List<Double>
) {
    val promedio: Double
        get() = notas.average()
}

fun main() {

    // Lista de estudiantes
    val estudiantes = listOf(
        Estudiante("Juan", "5to", listOf(80.0, 75.0, 90.0)),
        Estudiante("María", "5to", listOf(60.0, 65.0, 70.0)),
        Estudiante("Carlos", "5to", listOf(85.0, 88.0, 92.0)),
        Estudiante("Ana", "5to", listOf(70.0, 72.0, 68.0)),
        Estudiante("Luis", "5to", listOf(95.0, 90.0, 93.0))
    )

    // Filtrar estudiantes con promedio >= 70
    val aprobados = estudiantes.filter { it.promedio >= 70.0 }

    // Ordenar por promedio descendente
    val ordenados = aprobados.sortedByDescending { it.promedio }

    // Muestra Top 3
    println("Top 3 estudiantes:")
    ordenados.take(3).forEach {
        println("${it.nombre} - Promedio: ${String.format("%.1f", it.promedio)}")
    }
}