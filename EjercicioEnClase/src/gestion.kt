class gestion(
    val nombre: String,
    val precio: Double,
    var cantidad: Int,
    val cantidadMinima: Int
) {

    fun estaEnStockCritico(): Boolean {
        return cantidad <= cantidadMinima
    }

    fun calcularValorTotal(): Double {
        return precio * cantidad
    }

    fun abastecer(unidades: Int) {
        if (unidades > 0) {
            cantidad += unidades
        }
    }

    fun vender(unidades: Int): Boolean {
        return if (unidades <= cantidad) {
            cantidad -= unidades
            true
        } else {
            false
        }
    }
}