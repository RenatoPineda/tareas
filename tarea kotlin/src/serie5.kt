// Data class
data class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    var stock: Int
)

// Sealed class para resultados
sealed class OpInventario {
    data class Exito(val datos: Any?) : OpInventario()
    data class Error(val mensaje: String) : OpInventario()
    object SinStock : OpInventario()
}

// Base de datos en memoria
val inventario = mutableListOf<Producto>()

// CRUD

fun agregarProducto(p: Producto): OpInventario {
    if (inventario.any { it.id == p.id }) {
        return OpInventario.Error("El producto ya existe")
    }
    inventario.add(p)
    return OpInventario.Exito(p)
}

fun buscarPorId(id: Int): Producto? = inventario.find { it.id == id }

fun actualizarStock(id: Int, nuevoStock: Int): OpInventario {
    val producto = buscarPorId(id) ?: return OpInventario.Error("Producto no encontrado")

    return if (nuevoStock <= 0) {
        producto.stock = 0
        OpInventario.SinStock
    } else {
        producto.stock = nuevoStock
        OpInventario.Exito(producto)
    }
}

fun listarDisponibles(): List<Producto> = inventario.filter { it.stock > 0 }

// EXTENSION FUNCTIONS

fun List<Producto>.valorTotal(): Double =
    this.sumOf { it.precio * it.stock }

fun List<Producto>.buscarPorNombre(query: String): List<Producto> =
    this.filter { it.nombre.contains(query, ignoreCase = true) }

// MAIN

fun main() {

    // Agregar productos
    agregarProducto(Producto(1, "Maíz", 50.0, 10))
    agregarProducto(Producto(2, "Frijol", 40.0, 5))
    agregarProducto(Producto(3, "Arroz", 30.0, 0))
    agregarProducto(Producto(4, "Melón", 25.0, 20))
    agregarProducto(Producto(5, "Tomate", 15.0, 8))

    // Buscar producto
    val buscado = buscarPorId(2)
    println("Buscar ID 2: $buscado")

    // Actualizar stock
    val resultado = actualizarStock(3, 12)
    println("Actualizar stock: $resultado")

    // Listar disponibles
    println("\nProductos disponibles:")
    listarDisponibles().forEach {
        println("${it.nombre} - Stock: ${it.stock}")
    }

    // Buscar por nombre
    println("\nBúsqueda por nombre 'ma':")
    inventario.buscarPorNombre("ma").forEach {
        println(it.nombre)
    }

    // Valor total del inventario
    val total = inventario.valorTotal()
    println("\nValor total del inventario: Q ${String.format("%.2f", total)}")
}