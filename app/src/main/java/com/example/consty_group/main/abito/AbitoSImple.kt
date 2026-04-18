package com.example.consty_group.main.abito

sealed class AbitoItem {
    abstract val nombre: String
    abstract val dias: Int
    abstract val color: Int
    abstract val icono: Int
    abstract var completadoHoy: Boolean
}

data class AbitoSImple(
    override val nombre: String,
    override val dias: Int,
    override val color: Int,
    override val icono: Int,
    override var completadoHoy: Boolean = false
) : AbitoItem()

data class AbitoComplejo(
    override val nombre: String,
    override val dias: Int,
    override val color: Int,
    override val icono: Int,
    override var completadoHoy: Boolean = false
) : AbitoItem()