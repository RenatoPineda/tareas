import java.awt.*
import javax.swing.*
import java.util.concurrent.Semaphore

// -------------------------------------------------------
// PROGRAMA PARA SIMULAR EL USO DE SEMÁFOROS
// Ejemplo: Estacionamiento con 3 espacios disponibles
// -------------------------------------------------------

class Estacionamiento : JFrame() {

    // Semaphore controla cuántos carros pueden entrar.
    // El número 3 significa que solo 3 carros pueden
    // utilizar el estacionamiento al mismo tiempo.
    private val semaforo = Semaphore(3)

    // Arreglo que indica si cada espacio está ocupado.
    // false = libre
    // true = ocupado
    private val espaciosOcupados = BooleanArray(3)

    // Objeto utilizado para evitar que dos carros
    // seleccionen el mismo espacio.
    private val bloqueo = Any()

    // Etiquetas que representan los 3 espacios.
    private val espacio1 = JLabel("ESPACIO 1 - LIBRE", SwingConstants.CENTER)
    private val espacio2 = JLabel("ESPACIO 2 - LIBRE", SwingConstants.CENTER)
    private val espacio3 = JLabel("ESPACIO 3 - LIBRE", SwingConstants.CENTER)

    // Área donde aparecerán los mensajes.
    private val txtMensajes = JTextArea()

    // Botón para iniciar la simulación.
    private val btnIniciar = JButton("Iniciar simulación")

    init {

        // Título de la ventana.
        title = "Simulación de Semáforos"

        // Tamaño de la ventana.
        setSize(600, 450)

        // Centra la ventana en la pantalla.
        setLocationRelativeTo(null)

        // Termina el programa al cerrar la ventana.
        defaultCloseOperation = EXIT_ON_CLOSE

        // Diseño principal.
        layout = BorderLayout(10, 10)

        // ------------------------------------------------
        // TÍTULO
        // ------------------------------------------------

        val titulo = JLabel(
            "ESTACIONAMIENTO CON SEMÁFOROS",
            SwingConstants.CENTER
        )

        titulo.font = Font("Arial", Font.BOLD, 20)

        add(titulo, BorderLayout.NORTH)

        // ------------------------------------------------
        // PANEL DE LOS ESPACIOS
        // ------------------------------------------------

        val panelEspacios = JPanel()

        // Los 3 espacios aparecerán uno debajo del otro.
        panelEspacios.layout = GridLayout(3, 1, 10, 10)

        prepararEspacio(espacio1)
        prepararEspacio(espacio2)
        prepararEspacio(espacio3)

        panelEspacios.add(espacio1)
        panelEspacios.add(espacio2)
        panelEspacios.add(espacio3)

        // ------------------------------------------------
        // ÁREA DE MENSAJES
        // ------------------------------------------------

        txtMensajes.isEditable = false

        txtMensajes.font = Font(
            "Monospaced",
            Font.PLAIN,
            13
        )

        val scroll = JScrollPane(txtMensajes)

        // Panel central.
        val panelCentro = JPanel(BorderLayout(10, 10))

        panelCentro.border =
            BorderFactory.createEmptyBorder(10, 20, 10, 20)

        panelCentro.add(panelEspacios, BorderLayout.NORTH)
        panelCentro.add(scroll, BorderLayout.CENTER)

        add(panelCentro, BorderLayout.CENTER)

        // ------------------------------------------------
        // BOTÓN INICIAR
        // ------------------------------------------------

        btnIniciar.addActionListener {

            // Desactivamos el botón para evitar iniciar
            // dos simulaciones al mismo tiempo.
            btnIniciar.isEnabled = false

            txtMensajes.text = ""

            // Iniciamos la simulación.
            iniciarSimulacion()
        }

        add(btnIniciar, BorderLayout.SOUTH)
    }

    // ----------------------------------------------------
    // CONFIGURACIÓN VISUAL DE CADA ESPACIO
    // ----------------------------------------------------
    private fun prepararEspacio(label: JLabel) {

        label.isOpaque = true

        // Verde significa que el espacio está disponible.
        label.background = Color(120, 220, 120)

        label.foreground = Color.BLACK

        label.font = Font(
            "Arial",
            Font.BOLD,
            16
        )

        label.border = BorderFactory.createLineBorder(
            Color.BLACK,
            2
        )
    }

    // ----------------------------------------------------
    // INICIAR LA SIMULACIÓN
    // ----------------------------------------------------
    private fun iniciarSimulacion() {

        // Creamos 6 carros.
        for (i in 1..6) {

            // Cada carro se ejecutará como un hilo.
            Thread {

                // Pequeña pausa para que los carros
                // no lleguen exactamente al mismo tiempo.
                Thread.sleep((i * 500).toLong())

                carro(i)

            }.start()
        }
    }

    // ----------------------------------------------------
    // COMPORTAMIENTO DE CADA CARRO
    // ----------------------------------------------------
    private fun carro(numero: Int) {

        var espacio = -1
        var permisoObtenido = false

        try {

            mostrarMensaje(
                "Carro $numero está esperando para entrar..."
            )

            /*
             acquire()

             El carro solicita permiso al semáforo.

             Si hay menos de 3 carros dentro,
             obtiene permiso inmediatamente.

             Si ya existen 3 carros dentro,
             deberá esperar hasta que uno salga.
             */
            semaforo.acquire()

            permisoObtenido = true

            // Buscamos un espacio disponible.
            synchronized(bloqueo) {

                for (i in espaciosOcupados.indices) {

                    if (!espaciosOcupados[i]) {

                        espaciosOcupados[i] = true

                        espacio = i

                        break
                    }
                }
            }

            // Cambiamos gráficamente el espacio.
            ocuparEspacio(
                espacio,
                numero
            )

            mostrarMensaje(
                ">>> Carro $numero ENTRÓ al espacio ${espacio + 1}"
            )

            /*
             El carro permanece 4 segundos
             dentro del estacionamiento.
             */
            Thread.sleep(4000)

            mostrarMensaje(
                "<<< Carro $numero SALIÓ del espacio ${espacio + 1}"
            )

            // Liberamos gráficamente el espacio.
            liberarEspacio(espacio)

            synchronized(bloqueo) {

                espaciosOcupados[espacio] = false
            }

        } catch (e: InterruptedException) {

            mostrarMensaje(
                "Carro $numero fue interrumpido."
            )

        } finally {

            /*
             release()

             Devuelve el permiso al semáforo.

             Ahora otro carro que estaba esperando
             podrá entrar.
             */
            if (permisoObtenido) {
                semaforo.release()
            }
        }
    }

    // ----------------------------------------------------
    // CAMBIAR UN ESPACIO A OCUPADO
    // ----------------------------------------------------
    private fun ocuparEspacio(
        espacio: Int,
        carro: Int
    ) {

        SwingUtilities.invokeLater {

            val label = obtenerEspacio(espacio)

            // Rojo significa ocupado.
            label.background = Color(255, 120, 120)

            label.text =
                "ESPACIO ${espacio + 1} - CARRO $carro"
        }
    }

    // ----------------------------------------------------
    // CAMBIAR UN ESPACIO A LIBRE
    // ----------------------------------------------------
    private fun liberarEspacio(espacio: Int) {

        SwingUtilities.invokeLater {

            val label = obtenerEspacio(espacio)

            // Verde significa disponible.
            label.background = Color(120, 220, 120)

            label.text =
                "ESPACIO ${espacio + 1} - LIBRE"
        }
    }

    // ----------------------------------------------------
    // OBTENER LA ETIQUETA DEL ESPACIO
    // ----------------------------------------------------
    private fun obtenerEspacio(
        numero: Int
    ): JLabel {

        return when (numero) {

            0 -> espacio1
            1 -> espacio2
            else -> espacio3
        }
    }

    // ----------------------------------------------------
    // MOSTRAR MENSAJES EN PANTALLA
    // ----------------------------------------------------
    private fun mostrarMensaje(
        mensaje: String
    ) {

        SwingUtilities.invokeLater {

            txtMensajes.append(
                "$mensaje\n"
            )

            // Mueve automáticamente la barra hacia abajo.
            txtMensajes.caretPosition =
                txtMensajes.document.length
        }
    }
}

// -------------------------------------------------------
// FUNCIÓN PRINCIPAL
// -------------------------------------------------------
fun main() {

    /*
     SwingUtilities.invokeLater permite iniciar
     correctamente la interfaz gráfica de Swing.
     */
    SwingUtilities.invokeLater {

        // Creamos la ventana.
        val ventana = Estacionamiento()

        // Mostramos la ventana.
        ventana.isVisible = true
    }
}