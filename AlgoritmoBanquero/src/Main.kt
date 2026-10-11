import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.table.DefaultTableModel

class SimuladorBanquero : JFrame() {
    private val cantidadProcesos: JSpinner
    private val cantidadRecursos: JSpinner
    private val tablaAsignados: JTable
    private val tablaMaximos: JTable
    private val tablaDisponibles: JTable
    private val resultado: JTextArea

    init {
        setTitle("Simulador del Algoritmo del Banquero")
        setSize(1050, 750)
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        setLocationRelativeTo(null)

        val principal = JPanel(BorderLayout(10, 10))
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))

        val titulo = JLabel(
            "ALGORITMO DEL BANQUERO", SwingConstants.CENTER
        )
        titulo.setFont(Font("Arial", Font.BOLD, 25))
        titulo.setForeground(Color(25, 75, 135))
        principal.add(titulo, BorderLayout.NORTH)

        val centro = JPanel(BorderLayout(10, 10))

        val controles = JPanel(FlowLayout())
        cantidadProcesos = JSpinner(
            SpinnerNumberModel(5, 1, 20, 1)
        )
        cantidadRecursos = JSpinner(
            SpinnerNumberModel(3, 1, 10, 1)
        )

        val btnCrear = JButton("Crear matrices")
        val btnEjemplo = JButton("Cargar ejemplo")
        val btnCalcular = JButton("Ejecutar algoritmo")

        controles.add(JLabel("Procesos:"))
        controles.add(cantidadProcesos)
        controles.add(JLabel("Tipos de recursos:"))
        controles.add(cantidadRecursos)
        controles.add(btnCrear)
        controles.add(btnEjemplo)
        controles.add(btnCalcular)

        centro.add(controles, BorderLayout.NORTH)

        val matrices = JPanel(GridLayout(3, 1, 10, 10))

        tablaAsignados = JTable()
        tablaMaximos = JTable()
        tablaDisponibles = JTable()

        matrices.add(crearPanelTabla("Matriz de asignación", tablaAsignados))
        matrices.add(crearPanelTabla("Matriz máxima", tablaMaximos))
        matrices.add(crearPanelTabla("Recursos disponibles", tablaDisponibles))

        centro.add(matrices, BorderLayout.CENTER)
        principal.add(centro, BorderLayout.CENTER)

        resultado = JTextArea(11, 45)
        resultado.setFont(Font("Monospaced", Font.PLAIN, 14))
        resultado.setEditable(false)
        resultado.setLineWrap(true)
        resultado.setWrapStyleWord(true)

        val panelResultado = JPanel(BorderLayout())
        panelResultado.setBorder(
            BorderFactory.createTitledBorder("Resultado de la simulación")
        )
        panelResultado.add(JScrollPane(resultado), BorderLayout.CENTER)
        principal.add(panelResultado, BorderLayout.SOUTH)

        btnCrear.addActionListener(ActionListener { e: ActionEvent? -> crearMatrices() })
        btnEjemplo.addActionListener(ActionListener { e: ActionEvent? -> cargarEjemplo() })
        btnCalcular.addActionListener(ActionListener { e: ActionEvent? -> ejecutarAlgoritmo() })

        setContentPane(principal)
        cargarEjemplo()
    }

    private fun crearPanelTabla(nombre: String?, tabla: JTable): JPanel {
        val panel = JPanel(BorderLayout())
        panel.setBorder(BorderFactory.createTitledBorder(nombre))
        tabla.setRowHeight(25)
        tabla.setFillsViewportHeight(true)
        panel.add(JScrollPane(tabla), BorderLayout.CENTER)
        return panel
    }

    private fun crearMatrices() {
        val n = cantidadProcesos.getValue() as Int
        val m = cantidadRecursos.getValue() as Int

        val columnas = arrayOfNulls<String>(m + 1)
        columnas[0] = "Proceso"

        for (j in 0..<m) {
            columnas[j + 1] = "R" + j
        }

        val asignados: DefaultTableModel = object : DefaultTableModel(columnas, n) {
            override fun isCellEditable(fila: Int, columna: Int): Boolean {
                return columna != 0
            }
        }

        val maximos: DefaultTableModel = object : DefaultTableModel(columnas, n) {
            override fun isCellEditable(fila: Int, columna: Int): Boolean {
                return columna != 0
            }
        }

        for (i in 0..<n) {
            asignados.setValueAt("P" + i, i, 0)
            maximos.setValueAt("P" + i, i, 0)

            for (j in 1..m) {
                asignados.setValueAt(0, i, j)
                maximos.setValueAt(0, i, j)
            }
        }

        val columnasDisponibles = arrayOfNulls<String>(m)

        for (j in 0..<m) {
            columnasDisponibles[j] = "R" + j
        }

        val disponibles =
            DefaultTableModel(columnasDisponibles, 1)

        for (j in 0..<m) {
            disponibles.setValueAt(0, 0, j)
        }

        tablaAsignados.setModel(asignados)
        tablaMaximos.setModel(maximos)
        tablaDisponibles.setModel(disponibles)

        resultado.setText("Matrices creadas. Ingresa los recursos y ejecuta.")
    }

    private fun cargarEjemplo() {
        cantidadProcesos.setValue(5)
        cantidadRecursos.setValue(3)
        crearMatrices()

        val asignados = arrayOf<IntArray?>(
            intArrayOf(0, 1, 0),
            intArrayOf(2, 0, 0),
            intArrayOf(3, 0, 2),
            intArrayOf(2, 1, 1),
            intArrayOf(0, 0, 2)
        )

        val maximos = arrayOf<IntArray?>(
            intArrayOf(7, 5, 3),
            intArrayOf(3, 2, 2),
            intArrayOf(9, 0, 2),
            intArrayOf(2, 2, 2),
            intArrayOf(4, 3, 3)
        )

        val disponibles = intArrayOf(3, 3, 2)

        for (i in 0..4) {
            for (j in 0..2) {
                tablaAsignados.setValueAt(asignados[i]!![j], i, j + 1)
                tablaMaximos.setValueAt(maximos[i]!![j], i, j + 1)
            }
        }

        for (j in 0..2) {
            tablaDisponibles.setValueAt(disponibles[j], 0, j)
        }

        resultado.setText("Ejemplo cargado. Presiona 'Ejecutar algoritmo'.")
    }

    private fun leerNumero(tabla: JTable, fila: Int, columna: Int): Int {
        val valor = tabla.getValueAt(fila, columna)
        val numero = valor.toString().trim { it <= ' ' }.toInt()

        require(numero >= 0) { "Los recursos no pueden ser negativos." }

        return numero
    }

    private fun detenerEdicion(tabla: JTable) {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing()
        }
    }

    private fun ejecutarAlgoritmo() {
        try {
            detenerEdicion(tablaAsignados)
            detenerEdicion(tablaMaximos)
            detenerEdicion(tablaDisponibles)

            val n = cantidadProcesos.getValue() as Int
            val m = cantidadRecursos.getValue() as Int

            val asignacion = Array<IntArray?>(n) { IntArray(m) }
            val maximo = Array<IntArray?>(n) { IntArray(m) }
            val necesidad = Array<IntArray?>(n) { IntArray(m) }
            val disponible = IntArray(m)

            for (j in 0..<m) {
                disponible[j] = leerNumero(tablaDisponibles, 0, j)
            }

            for (i in 0..<n) {
                for (j in 0..<m) {
                    asignacion[i]!![j] =
                        leerNumero(tablaAsignados, i, j + 1)
                    maximo[i]!![j] =
                        leerNumero(tablaMaximos, i, j + 1)

                    require(asignacion[i]!![j] <= maximo[i]!![j]) {
                        ("P" + i + ": la asignación no puede "
                                + "superar el máximo en R" + j)
                    }

                    necesidad[i]!![j] =
                        maximo[i]!![j] - asignacion[i]!![j]
                }
            }

            val trabajo = disponible.copyOf(m)
            val terminado = BooleanArray(n)
            val secuencia: MutableList<Int?> = ArrayList<Int?>()

            val texto = StringBuilder()
            texto.append("=== INICIO DE LA SIMULACIÓN ===\n\n")
            texto.append("Recursos disponibles: ")
                .append(trabajo.contentToString()).append("\n\n")

            texto.append("MATRIZ DE NECESIDAD:\n")
            for (i in 0..<n) {
                texto.append("P").append(i).append(": ")
                    .append(necesidad[i].contentToString())
                    .append("\n")
            }

            texto.append("\n=== EJECUCIÓN ===\n")

            var progreso: Boolean

            do {
                progreso = false

                for (i in 0..<n) {
                    if (terminado[i]) {
                        continue
                    }

                    var puedeEjecutarse = true

                    for (j in 0..<m) {
                        if (necesidad[i]!![j] > trabajo[j]) {
                            puedeEjecutarse = false
                            break
                        }
                    }

                    if (puedeEjecutarse) {
                        texto.append("\nP").append(i)
                            .append(" puede ejecutarse.\n")

                        for (j in 0..<m) {
                            trabajo[j] += asignacion[i]!![j]
                        }

                        terminado[i] = true
                        secuencia.add(i)
                        progreso = true

                        texto.append("P").append(i)
                            .append(" termina y libera recursos.\n")
                        texto.append("Disponibles ahora: ")
                            .append(trabajo.contentToString())
                            .append("\n")
                    }
                }
            } while (progreso && secuencia.size < n)

            if (secuencia.size == n) {
                texto.append("\n=== SISTEMA EN ESTADO SEGURO ===\n")
                texto.append("Existe una secuencia segura:\n")

                for (i in secuencia.indices) {
                    if (i > 0) {
                        texto.append(" -> ")
                    }
                    texto.append("P").append(secuencia.get(i))
                }

                texto.append("\nTodos los procesos pueden finalizar.")
            } else {
                texto.append("\n=== SISTEMA EN ESTADO INSEGURO ===\n")
                texto.append("No se encontró una secuencia segura.\n")
                texto.append("Existe riesgo de interbloqueo.\n")
                texto.append("Procesos que no pudieron finalizar: ")

                for (i in 0..<n) {
                    if (!terminado[i]) {
                        texto.append("P").append(i).append(" ")
                    }
                }
            }

            resultado.setText(texto.toString())
            resultado.setCaretPosition(0)
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Revisa los datos ingresados.\n" + ex.message,
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater(Runnable {
                SimuladorBanquero().setVisible(true)
            })
        }
    }
}