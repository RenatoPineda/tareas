fun main() {

    // Datos de la persona
    val peso = 70.0       // en kilogramos
    val altura = 1.75     // en metros

    // Cálculo del IMC
    val imc = peso / (altura * altura)

    // Determinar categoría
    val categoria = when {
        imc < 18.5 -> "Bajo peso"
        imc < 25 -> "Normal"
        imc < 30 -> "Sobrepeso"
        else -> "Obesidad"
    }

    // Mostrar resultado con 2 decimales
    println("IMC: ${String.format("%.2f", imc)}")
    println("Categoría: $categoria")
}