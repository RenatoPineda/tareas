fun main() {

    // Pruebas solicitadas
    println("100°C a °F = ${convertir(100.0, "C", "F")}")
    println("0°C a K = ${convertir(0.0, "C", "K")}")
    println("32°F a °C = ${convertir(32.0, "F", "C")}")
}

// Funciones de una sola línea
fun celsiusAFahrenheit(c: Double): Double = (c * 9/5) + 32
fun fahrenheitACelsius(f: Double): Double = (f - 32) * 5/9
fun celsiusAKelvin(c: Double): Double = c + 273.15

// Función principal de conversión
fun convertir(valor: Double, desde: String, hasta: String): Double =
    when {
        desde == "C" && hasta == "F" -> celsiusAFahrenheit(valor)
        desde == "F" && hasta == "C" -> fahrenheitACelsius(valor)
        desde == "C" && hasta == "K" -> celsiusAKelvin(valor)
        else -> {
            println("Conversión no válida")
            0.0
        }
    }