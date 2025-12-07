data class Dado(val caras: Int, var resultado: Int? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dado

        if (caras != other.caras) return false

        return true
    }

    override fun hashCode(): Int {
        return caras
    }
}
