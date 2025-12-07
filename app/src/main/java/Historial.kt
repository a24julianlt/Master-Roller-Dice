import java.sql.Time
import java.util.Date

data class Historial(
    val dados: List<Dado>,
    val fecha : Date,
    val hora : Time,
    val modificador : Int,
    val resultado : Int,
    val numTirada: Int
)