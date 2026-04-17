package com.example.consty_group.main.abito

sealed class AbitoItem



data class AbitoSImple(
    val nombre: String,
    val dias: Int,
    val color: Int,
    val icono: Int
) : AbitoItem()

data class AbitoComplejo(
    val nombre: String,
    val dias: Int,
    val color: Int,
    val icono: Int
) : AbitoItem()