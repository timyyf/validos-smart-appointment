package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Elegant Dark Design Theme Colors
val ElegantBackground = Color(0xFF0A0C10)       // Main extremely deep space background
val ElegantSurface = Color(0xFF13161C)          // Cards, header, list item backgrounds
val ElegantSurfacePanel = Color(0xFF1A1F26)     // Footer details and secondary card backgrounds
val ElegantPrimary = Color(0xFF60A5FA)          // Vibrant corporate light blue
val ElegantSecondary = Color(0xFF10B981)        // Emerald green accent
val ElegantOnBackground = Color(0xFFE2E8F0)     // Soft light text
val ElegantOnSurface = Color(0xFFF8FAFC)        // Bright white/slate text for headers
val ElegantBorder = Color(0x0DFFFFFF)           // White/5% subtle divider lines
val ElegantBorderMedium = Color(0x1AFFFFFF)     // White/10% subtle border

// Map to system colors to ensure uniform Elegant Dark rendering
val ServicePrimary = ElegantPrimary
val ServiceSecondary = ElegantSecondary
val ServiceBackground = ElegantBackground
val ServiceSurface = ElegantSurface
val ServiceOnPrimary = Color(0xFF0A0C10)
val ServiceOnSecondary = Color(0xFF0A0C10)
val ServiceError = Color(0xFFEF4444)

val ServicePrimaryDark = ElegantPrimary
val ServiceSecondaryDark = ElegantSecondary
val ServiceBackgroundDark = ElegantBackground
val ServiceSurfaceDark = ElegantSurface
val ServiceOnPrimaryDark = Color(0xFF0A0C10)
val ServiceOnSecondaryDark = Color(0xFF0A0C10)

// Status / Audit Trail Highlights
val AuditSuccess = Color(0xFF10B981)  // Status OK / Active Emerald Green
val AuditPending = Color(0xFFF59E0B)  // Warning / Waiting Amber
val AuditDanger = Color(0xFFEF4444)   // Faltas / Alert Red
val AuditInfo = Color(0xFF3B82F6)     // Link / Action Blue
