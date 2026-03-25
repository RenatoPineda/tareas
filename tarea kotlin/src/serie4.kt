// Data class
data class Contacto(
    val nombre: String,
    val telefono: String?,
    val email: String?
)

// Mapa de contactos
val contactos: Map<String, Contacto?> = mapOf(
    "1" to Contacto("Juan", "12345678", "juan@gmail.com"),
    "2" to Contacto("María", null, "maria@gmail.com"),
    "3" to Contacto("Carlos", "87654321", null),
    "4" to null
)

// Función para buscar contacto
fun buscarContacto(id: String): Contacto? = contactos[id]

fun main() {

    val id = "2"
    val contacto = buscarContacto(id)
    
    val telefono = contacto?.telefono ?: "No disponible"
    val email = contacto?.email ?: "No disponible"

    println("Teléfono: $telefono")
    println("Email: $email")

    // Uso de let {}
    contacto?.let {
        println("Contacto encontrado: ${it.nombre}")
    } ?: println("El contacto no existe")
}