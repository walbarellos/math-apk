package com.basecalc.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.basecalc.ColorMode

@Composable
fun acentoDaBase(base: Int, colorMode: ColorMode): Color {
    val escuro = isSystemInDarkTheme()
    return when (colorMode) {
        ColorMode.PADRAO -> when (base) {
            2  -> if (escuro) Color(0xFF6BB4FF) else Color(0xFF185FA5)
            3  -> if (escuro) Color(0xFF4EC9A0) else Color(0xFF0D6B52)
            4  -> if (escuro) Color(0xFF8BD158) else Color(0xFF3B6D11)
            5  -> if (escuro) Color(0xFFAAD96A) else Color(0xFF4F7A1C)
            6  -> if (escuro) Color(0xFFEFA94A) else Color(0xFF854F0B)
            7  -> if (escuro) Color(0xFFFFCC4A) else Color(0xFFBA7517)
            8  -> if (escuro) Color(0xFFA99EFF) else Color(0xFF534AB7)
            9  -> if (escuro) Color(0xFFEE8BB5) else Color(0xFF993556)
            10 -> if (escuro) Color(0xFFCCCCCC) else Color(0xFF555555)
            16 -> if (escuro) Color(0xFFFF9EC4) else Color(0xFFD4537E)
            else -> Color.Gray
        }
        ColorMode.ALTO_CONTRASTE -> when (base) {
            2  -> if (escuro) Color(0xFFFFFFFF) else Color(0xFF000000)
            3  -> if (escuro) Color(0xFF00E5FF) else Color(0xFF004E7A)
            4  -> if (escuro) Color(0xFFB9FF3C) else Color(0xFF2E6B00)
            5  -> if (escuro) Color(0xFFFFE85A) else Color(0xFF7A5A00)
            6  -> if (escuro) Color(0xFFFFB74D) else Color(0xFF7A3F00)
            7  -> if (escuro) Color(0xFFFF5252) else Color(0xFF7A0000)
            8  -> if (escuro) Color(0xFFB388FF) else Color(0xFF4A148C)
            9  -> if (escuro) Color(0xFF80D8FF) else Color(0xFF01579B)
            10 -> if (escuro) Color(0xFFFFFFFF) else Color(0xFF000000)
            16 -> if (escuro) Color(0xFFFF80AB) else Color(0xFF880E4F)
            else -> if (escuro) Color.White else Color.Black
        }
        ColorMode.DALTONISMO -> when (base) {
            2  -> Color(0xFF0072B2)
            3  -> Color(0xFFE69F00)
            4  -> Color(0xFF009E73)
            5  -> Color(0xFFF0E442)
            6  -> Color(0xFF56B4E9)
            7  -> Color(0xFFD55E00)
            8  -> Color(0xFFCC79A7)
            9  -> Color(0xFF000000)
            10 -> Color(0xFF666666)
            16 -> Color(0xFF999999)
            else -> Color.Gray
        }
    }
}
