import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
import javax.swing.*
import javax.swing.Timer
import javax.swing.border.EmptyBorder

class RobotLaberinto : JFrame() {
    private val laberinto = Array<IntArray?>(FILAS) { IntArray(COLUMNAS) }
    private val botones = Array(FILAS) { fila ->
        Array(COLUMNAS) { columna -> CasillaRobot(fila, columna) }
    }
    private var robotFila = 0
    private var robotColumna = 0

    private inner class CasillaRobot(val fila: Int, val columna: Int) : JButton() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (fila != robotFila || columna != robotColumna) return

            val dibujo = g.create() as Graphics2D
            try {
                dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val escala = minOf(width / 40.0, height / 44.0)
                dibujo.translate((width - 32 * escala) / 2, (height - 38 * escala) / 2)
                dibujo.scale(escala, escala)
                dibujo.color = Color(30, 50, 80)
                dibujo.stroke = BasicStroke(2f)
                dibujo.drawLine(16, 3, 16, 8)
                dibujo.color = Color(255, 170, 40)
                dibujo.fillOval(13, 0, 6, 6)
                dibujo.color = Color(30, 50, 80)
                dibujo.fillRoundRect(2, 8, 28, 18, 7, 7)
                dibujo.color = Color(190, 230, 255)
                dibujo.fillRoundRect(5, 11, 22, 12, 5, 5)
                dibujo.color = Color(30, 50, 80)
                dibujo.fillOval(8, 14, 5, 5)
                dibujo.fillOval(19, 14, 5, 5)
                dibujo.fillRoundRect(8, 27, 16, 8, 3, 3)
                dibujo.drawLine(3, 28, 6, 32)
                dibujo.drawLine(26, 32, 29, 28)
                dibujo.fillRect(8, 35, 6, 3)
                dibujo.fillRect(18, 35, 6, 3)
                dibujo.color = Color(255, 170, 40)
                dibujo.fillOval(14, 29, 4, 4)
            } finally {
                dibujo.dispose()
            }
        }
    }

    // Entrada inicial
    private var entradaFila = 0
    private var entradaColumna = 0

    // Salida inicial
    private var salidaFila = 14
    private var salidaColumna = 14

    private var herramientaActual = Herramienta.PARED

    private var lblEstado: JLabel? = null
    private var lblHerramienta: JLabel? = null
    private var txtPila: JTextArea? = null
    private var txtRuta: JTextArea? = null

    private var btnBuscar: JButton? = null

    // Para mostrar la animación
    private var timerAnimacion: Timer? = null
    private var pasosAnimacion: MutableList<PasoAnimacion>? = null
    private var indiceAnimacion = 0

    internal enum class Herramienta {
        PARED,
        BORRAR,
        ENTRADA,
        SALIDA
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    init {
        setTitle("Robot en Laberinto - Autómata de Pila")
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        setSize(1250, 850)
        setLocationRelativeTo(null)

        crearInterfaz()
        inicializarLaberinto()
        actualizarTablero()

        setVisible(true)
    }

    // =========================================================
    // CREAR INTERFAZ
    // =========================================================
    private fun crearInterfaz() {
        val principal = JPanel(BorderLayout(10, 10))
        principal.setBorder(EmptyBorder(10, 10, 10, 10))

        // -----------------------------------------------------
        // TÍTULO
        // -----------------------------------------------------
        val titulo = JLabel(
            "ROBOT EN LABERINTO - MATRIZ 15 x 15",
            SwingConstants.CENTER
        )

        titulo.setFont(Font("Arial", Font.BOLD, 24))
        principal.add(titulo, BorderLayout.NORTH)

        // -----------------------------------------------------
        // TABLERO
        // -----------------------------------------------------
        val panelTablero = JPanel(
            GridLayout(FILAS, COLUMNAS, 1, 1)
        )

        panelTablero.setBackground(Color.DARK_GRAY)

        for (fila in 0..<FILAS) {
            for (columna in 0..<COLUMNAS) {
                val boton = botones[fila][columna]

                boton.setMargin(Insets(0, 0, 0, 0))
                boton.setFont(Font("Arial", Font.BOLD, 10))
                boton.setFocusPainted(false)

                val f = fila
                val c = columna

                boton.addActionListener(ActionListener { e: ActionEvent? -> modificarCasilla(f, c) })

                panelTablero.add(boton)
            }
        }

        principal.add(panelTablero, BorderLayout.CENTER)

        // -----------------------------------------------------
        // PANEL DERECHO
        // -----------------------------------------------------
        val panelDerecho = JPanel()
        panelDerecho.setPreferredSize(Dimension(300, 0))
        panelDerecho.setLayout(
            BoxLayout(panelDerecho, BoxLayout.Y_AXIS)
        )

        lblEstado = JLabel("Listo para comenzar")
        lblEstado!!.setFont(Font("Arial", Font.BOLD, 16))

        lblHerramienta = JLabel("Herramienta: PARED")

        panelDerecho.add(lblEstado)
        panelDerecho.add(Box.createVerticalStrut(5))
        panelDerecho.add(lblHerramienta)
        panelDerecho.add(Box.createVerticalStrut(15))

        // -----------------------------------------------------
        // BOTONES DE EDICIÓN
        // -----------------------------------------------------
        val btnPared = JButton("Agregar pared")
        val btnBorrar = JButton("Borrar")
        val btnEntrada = JButton("Colocar entrada")
        val btnSalida = JButton("Colocar salida")

        configurarBoton(btnPared)
        configurarBoton(btnBorrar)
        configurarBoton(btnEntrada)
        configurarBoton(btnSalida)

        btnPared.addActionListener(ActionListener { e: ActionEvent? ->
            herramientaActual = Herramienta.PARED
            lblHerramienta!!.setText("Herramienta: PARED")
        })

        btnBorrar.addActionListener(ActionListener { e: ActionEvent? ->
            herramientaActual = Herramienta.BORRAR
            lblHerramienta!!.setText("Herramienta: BORRAR")
        })

        btnEntrada.addActionListener(ActionListener { e: ActionEvent? ->
            herramientaActual = Herramienta.ENTRADA
            lblHerramienta!!.setText("Herramienta: ENTRADA")
        })

        btnSalida.addActionListener(ActionListener { e: ActionEvent? ->
            herramientaActual = Herramienta.SALIDA
            lblHerramienta!!.setText("Herramienta: SALIDA")
        })

        panelDerecho.add(btnPared)
        panelDerecho.add(Box.createVerticalStrut(5))

        panelDerecho.add(btnBorrar)
        panelDerecho.add(Box.createVerticalStrut(5))

        panelDerecho.add(btnEntrada)
        panelDerecho.add(Box.createVerticalStrut(5))

        panelDerecho.add(btnSalida)

        panelDerecho.add(Box.createVerticalStrut(20))

        // -----------------------------------------------------
        // BUSCAR SALIDA
        // -----------------------------------------------------
        btnBuscar = JButton("BUSCAR SALIDA")

        btnBuscar!!.setFont(
            Font("Arial", Font.BOLD, 16)
        )

        configurarBoton(btnBuscar!!)

        btnBuscar!!.addActionListener(ActionListener { e: ActionEvent? -> buscarSalida() })

        panelDerecho.add(btnBuscar)

        panelDerecho.add(Box.createVerticalStrut(5))

        val btnLimpiarRuta = JButton("Limpiar recorrido")
        configurarBoton(btnLimpiarRuta)

        btnLimpiarRuta.addActionListener(ActionListener { e: ActionEvent? ->
            detenerAnimacion()
            limpiarRecorrido()
        })

        panelDerecho.add(btnLimpiarRuta)

        panelDerecho.add(Box.createVerticalStrut(5))

        val btnLimpiarTodo = JButton("Nuevo laberinto")
        configurarBoton(btnLimpiarTodo)

        btnLimpiarTodo.addActionListener(ActionListener { e: ActionEvent? -> nuevoLaberinto() })

        panelDerecho.add(btnLimpiarTodo)

        // -----------------------------------------------------
        // PILA
        // -----------------------------------------------------
        panelDerecho.add(Box.createVerticalStrut(20))

        val lblPila = JLabel("PILA / RECORRIDO")
        lblPila.setFont(Font("Arial", Font.BOLD, 14))

        panelDerecho.add(lblPila)

        txtPila = JTextArea()
        txtPila!!.setEditable(false)
        txtPila!!.setFont(Font("Monospaced", Font.PLAIN, 13))

        val scrollPila = JScrollPane(txtPila)
        scrollPila.setPreferredSize(Dimension(280, 150))
        scrollPila.setMaximumSize(
            Dimension(Int.MAX_VALUE, 150)
        )

        panelDerecho.add(scrollPila)

        // -----------------------------------------------------
        // RUTA
        // -----------------------------------------------------
        panelDerecho.add(Box.createVerticalStrut(10))

        val lblRuta = JLabel("MOVIMIENTOS")
        lblRuta.setFont(Font("Arial", Font.BOLD, 14))

        panelDerecho.add(lblRuta)

        txtRuta = JTextArea()
        txtRuta!!.setEditable(false)
        txtRuta!!.setLineWrap(true)
        txtRuta!!.setWrapStyleWord(true)

        val scrollRuta = JScrollPane(txtRuta)
        scrollRuta.setPreferredSize(Dimension(280, 130))
        scrollRuta.setMaximumSize(
            Dimension(Int.MAX_VALUE, 130)
        )

        panelDerecho.add(scrollRuta)

        principal.add(panelDerecho, BorderLayout.EAST)

        add(principal)
    }

    private fun configurarBoton(boton: JButton) {
        boton.setAlignmentX(CENTER_ALIGNMENT)

        boton.setMaximumSize(
            Dimension(Int.MAX_VALUE, 35)
        )

        boton.setFocusPainted(false)
    }

    // =========================================================
    // INICIALIZAR
    // =========================================================
    private fun inicializarLaberinto() {
        robotFila = entradaFila
        robotColumna = entradaColumna
        for (fila in 0..<FILAS) {
            for (columna in 0..<COLUMNAS) {
                laberinto[fila]!![columna] = CAMINO
            }
        }

        laberinto[entradaFila]!![entradaColumna] = ENTRADA
        laberinto[salidaFila]!![salidaColumna] = SALIDA
    }

    // =========================================================
    // MODIFICAR CASILLA
    // =========================================================
    private fun modificarCasilla(fila: Int, columna: Int) {
        detenerAnimacion()

        limpiarRecorridoSinActualizar()

        when (herramientaActual) {
            Herramienta.PARED -> if (!esEntrada(fila, columna)
                && !esSalida(fila, columna)
            ) {
                laberinto[fila]!![columna] = PARED
            }

            Herramienta.BORRAR -> if (!esEntrada(fila, columna)
                && !esSalida(fila, columna)
            ) {
                laberinto[fila]!![columna] = CAMINO
            }

            Herramienta.ENTRADA -> if (!esSalida(fila, columna)) {
                laberinto[entradaFila]!![entradaColumna] = CAMINO

                entradaFila = fila
                entradaColumna = columna

                laberinto[fila]!![columna] = ENTRADA
            }

            Herramienta.SALIDA -> if (!esEntrada(fila, columna)) {
                laberinto[salidaFila]!![salidaColumna] = CAMINO

                salidaFila = fila
                salidaColumna = columna

                laberinto[fila]!![columna] = SALIDA
            }

        }

        robotFila = entradaFila
        robotColumna = entradaColumna
        actualizarTablero()
    }

    // =========================================================
    // BUSCAR SALIDA
    // =========================================================
    private fun buscarSalida() {
        detenerAnimacion()
        limpiarRecorridoSinActualizar()

        val visitado = Array<BooleanArray?>(FILAS) { BooleanArray(COLUMNAS) }

        val pila = Stack<Posicion>()

        val pasos: MutableList<PasoAnimacion> = ArrayList<PasoAnimacion>()

        val inicio = Posicion(
            entradaFila,
            entradaColumna,
            null,
            "INICIO"
        )

        var finalEncontrado: Posicion? = null

        // La pila conserva el camino actual; cada retroceso es un paso real.
        fun explorar(actual: Posicion): Boolean {
            visitado[actual.fila]!![actual.columna] = true
            pila.push(actual)
            pasos.add(PasoAnimacion(actual.fila, actual.columna, VISITADO, copiarPila(pila)))

            if (esSalida(actual.fila, actual.columna)) {
                finalEncontrado = actual
                return true
            }

            val direcciones = arrayOf(
                Triple(0, 1, "R"), Triple(1, 0, "D"),
                Triple(0, -1, "L"), Triple(-1, 0, "U")
            )
            for ((df, dc, movimiento) in direcciones) {
                val fila = actual.fila + df
                val columna = actual.columna + dc
                if (fila !in 0 until FILAS || columna !in 0 until COLUMNAS) continue
                if (laberinto[fila]!![columna] == PARED || visitado[fila]!![columna]) continue
                if (explorar(Posicion(fila, columna, actual, movimiento))) return true
                pasos.add(PasoAnimacion(actual.fila, actual.columna, VISITADO, copiarPila(pila)))
            }

            pila.pop()
            return false
        }

        explorar(inicio)
        if (finalEncontrado == null) {
            pasos.add(PasoAnimacion(entradaFila, entradaColumna, VISITADO, copiarPila(pila)))
        }

        pasosAnimacion = pasos

        if (finalEncontrado != null) {
            val ruta = reconstruirRuta(finalEncontrado)

            for (p in ruta) {
                pasosAnimacion!!.add(
                    PasoAnimacion(
                        p.fila,
                        p.columna,
                        RUTA,
                        ArrayList<String?>()
                    )
                )
            }

            mostrarRuta(ruta)

            lblEstado!!.setText("BUSCANDO RUTA...")

            iniciarAnimacion(true, ruta.size - 1)
        } else {
            txtRuta!!.setText("No existe una ruta hacia la salida. El robot regresará al inicio.")

            lblEstado!!.setText("BUSCANDO RUTA...")

            iniciarAnimacion(false, 0)
        }
    }

    // =========================================================
    // RECONSTRUIR RUTA
    // =========================================================
    private fun reconstruirRuta(fin: Posicion?): MutableList<Posicion> {
        val ruta: MutableList<Posicion> = ArrayList<Posicion>()

        var actual = fin

        while (actual != null) {
            ruta.add(actual)
            actual = actual.padre
        }

        Collections.reverse(ruta)

        return ruta
    }

    // =========================================================
    // MOSTRAR RUTA
    // =========================================================
    private fun mostrarRuta(ruta: MutableList<Posicion>) {
        val movimientos = StringBuilder()

        for (i in 1..<ruta.size) {
            movimientos.append(ruta.get(i).movimiento)

            if (i < ruta.size - 1) {
                movimientos.append(" → ")
            }
        }

        txtRuta!!.setText(
            ("Movimientos encontrados:\n\n"
                    + movimientos
                    + "\n\nTotal de movimientos: "
                    + (ruta.size - 1))
        )
    }

    // =========================================================
    // ANIMACIÓN
    // =========================================================
    private fun iniciarAnimacion(
        encontroSalida: Boolean,
        movimientos: Int
    ) {
        indiceAnimacion = 0

        btnBuscar!!.setEnabled(false)
        actualizarTablero()

        timerAnimacion = Timer(100, ActionListener { e: ActionEvent? ->
            if (indiceAnimacion >= pasosAnimacion!!.size) {
                timerAnimacion!!.stop()

                btnBuscar!!.setEnabled(true)

                actualizarTablero()

                // Volvemos a pintar la ruta final
                if (encontroSalida) {
                    for (paso in pasosAnimacion!!) {
                        if (paso.tipo == RUTA) {
                            if (!esEntrada(
                                    paso.fila,
                                    paso.columna
                                )
                                && !esSalida(
                                    paso.fila,
                                    paso.columna
                                )
                            ) {
                                laberinto[paso.fila]!![paso.columna] = RUTA
                            }
                        }
                    }

                    actualizarTablero()

                    lblEstado!!.setText(
                        "✓ EL ROBOT PUEDE SALIR"
                    )

                    JOptionPane.showMessageDialog(
                        this,
                        ("EL ROBOT PUEDE SALIR\n\n"
                                + "Movimientos: "
                                + movimientos),
                        "Ruta encontrada",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                } else {
                    lblEstado!!.setText(
                        "✗ SIN SALIDA: ROBOT EN EL INICIO"
                    )

                    JOptionPane.showMessageDialog(
                        this,
                        "EL ROBOT ESTÁ ENCERRADO.\n"
                                + "No existe una ruta válida. El robot regresó al inicio.",
                        "Sin salida",
                        JOptionPane.WARNING_MESSAGE
                    )
                }

                return@ActionListener
            }
            val paso =
                pasosAnimacion!!.get(indiceAnimacion)

            if (paso.tipo == VISITADO) {
                lblEstado!!.text = "EXPLORANDO Y RETROCEDIENDO..."
                val filaAnterior = robotFila
                val columnaAnterior = robotColumna
                robotFila = paso.fila
                robotColumna = paso.columna
                if (!esEntrada(robotFila, robotColumna) && !esSalida(robotFila, robotColumna)) {
                    laberinto[robotFila]!![robotColumna] = VISITADO
                }
                actualizarTablero()
                mostrarPila(paso.pila)
                botones[filaAnterior][columnaAnterior].repaint()
            } else if (paso.tipo == RUTA) {
                // El robot ya llegó a la salida; resaltamos el camino encontrado.
                if (!esEntrada(paso.fila, paso.columna) && !esSalida(paso.fila, paso.columna)) {
                    laberinto[paso.fila]!![paso.columna] = RUTA
                }
                actualizarTablero()
            }
            indiceAnimacion++
        })

        timerAnimacion!!.start()
    }

    // =========================================================
    // MOSTRAR PILA
    // =========================================================
    private fun mostrarPila(pila: MutableList<String?>) {
        val texto = StringBuilder()

        texto.append("TOPE\n")
        texto.append("──────\n")

        if (pila.isEmpty()) {
            texto.append("(vacía)")
        } else {
            for (i in pila.indices.reversed()) {
                texto.append(pila.get(i)).append("\n")
            }
        }

        txtPila!!.setText(texto.toString())
    }

    private fun copiarPila(pila: Stack<Posicion>): MutableList<String?> {
        val copia: MutableList<String?> = ArrayList<String?>()

        for (p in pila) {
            copia.add(
                "q" + obtenerNumeroEstado(
                    p.fila,
                    p.columna
                )
            )
        }

        return copia
    }

    // =========================================================
    // ACTUALIZAR TABLERO
    // =========================================================
    private fun actualizarTablero() {
        for (fila in 0..<FILAS) {
            for (columna in 0..<COLUMNAS) {
                val boton = botones[fila][columna]

                val numeroEstado =
                    obtenerNumeroEstado(fila, columna)

                boton.setText("q" + numeroEstado)

                when (laberinto[fila]!![columna]) {
                    CAMINO -> {
                        boton.setBackground(Color.WHITE)
                        boton.setForeground(Color.BLACK)
                    }

                    PARED -> {
                        boton.setBackground(Color.DARK_GRAY)
                        boton.setForeground(Color.WHITE)
                    }

                    ENTRADA -> {
                        boton.setBackground(
                            Color(100, 220, 120)
                        )
                        boton.setForeground(Color.BLACK)
                        boton.setText("IN")
                    }

                    SALIDA -> {
                        boton.setBackground(
                            Color(255, 100, 100)
                        )
                        boton.setForeground(Color.BLACK)
                        boton.setText("OUT")
                    }

                    VISITADO -> {
                        boton.background = Color(255, 220, 120)
                        boton.foreground = Color.BLACK
                    }

                    RUTA -> {
                        boton.setBackground(
                            Color(100, 180, 255)
                        )
                        boton.setForeground(Color.BLACK)
                    }
                }
                if (fila == robotFila && columna == robotColumna) boton.text = ""
                boton.repaint()
            }
        }
    }

    // =========================================================
    // LIMPIAR RECORRIDO
    // =========================================================
    private fun limpiarRecorrido() {
        limpiarRecorridoSinActualizar()

        txtPila!!.setText("")
        txtRuta!!.setText("")

        lblEstado!!.setText("Listo para comenzar")

        actualizarTablero()
    }

    private fun limpiarRecorridoSinActualizar() {
        robotFila = entradaFila
        robotColumna = entradaColumna
        for (fila in 0..<FILAS) {
            for (columna in 0..<COLUMNAS) {
                if (laberinto[fila]!![columna] == RUTA
                    || laberinto[fila]!![columna] == VISITADO
                ) {
                    laberinto[fila]!![columna] = CAMINO
                }
            }
        }

        laberinto[entradaFila]!![entradaColumna] = ENTRADA
        laberinto[salidaFila]!![salidaColumna] = SALIDA
    }

    // =========================================================
    // NUEVO LABERINTO
    // =========================================================
    private fun nuevoLaberinto() {
        detenerAnimacion()

        entradaFila = 0
        entradaColumna = 0

        salidaFila = 14
        salidaColumna = 14

        inicializarLaberinto()

        txtPila!!.setText("")
        txtRuta!!.setText("")

        lblEstado!!.setText("Nuevo laberinto")

        actualizarTablero()
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================
    private fun esEntrada(fila: Int, columna: Int): Boolean {
        return fila == entradaFila
                && columna == entradaColumna
    }

    private fun esSalida(fila: Int, columna: Int): Boolean {
        return fila == salidaFila
                && columna == salidaColumna
    }

    private fun obtenerNumeroEstado(fila: Int, columna: Int): Int {
        return fila * COLUMNAS + columna
    }

    private fun detenerAnimacion() {
        if (timerAnimacion != null
            && timerAnimacion!!.isRunning()
        ) {
            timerAnimacion!!.stop()
        }

        if (btnBuscar != null) {
            btnBuscar!!.setEnabled(true)
        }
    }

    // =========================================================
    // CLASE POSICIÓN
    // =========================================================
    internal class Posicion(
        var fila: Int,
        var columna: Int,
        var padre: Posicion?,
        var movimiento: String?
    )

    // =========================================================
    // PASOS DE ANIMACIÓN
    // =========================================================
    internal class PasoAnimacion(
        var fila: Int,
        var columna: Int,
        var tipo: Int,
        var pila: MutableList<String?>
    )

    companion object {
        // =========================================================
        // CONFIGURACIÓN GENERAL
        // =========================================================
        private const val FILAS = 15
        private const val COLUMNAS = 15

        // Tipos de casilla
        private const val CAMINO = 0
        private const val PARED = 1
        private const val ENTRADA = 2
        private const val SALIDA = 3
        private const val RUTA = 4
        private const val VISITADO = 5

        // =========================================================
        // MAIN
        // =========================================================
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater(
                Runnable { RobotLaberinto() }
            )
        }
    }
}
