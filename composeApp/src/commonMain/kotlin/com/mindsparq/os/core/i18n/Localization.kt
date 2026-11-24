package com.mindsparq.os.core.i18n

import kotlin.math.abs
import kotlin.math.roundToLong

enum class AppLanguage { EN, NE }
enum class CalendarSystem { AD, BS }

data class AppStrings(
    val dashboard: String,
    val schools: String,
    val academic: String,
    val finance: String,
    val inventory: String,
    val fieldOps: String,
    val reports: String,
    val settings: String,
    val searchHint: String,
    val toggleTheme: String,
    val toggleCalendar: String,
    val toggleLanguage: String,
    val notifications: String,
    val signInTitle: String,
    val emailLabel: String,
    val passwordLabel: String,
    val signInAction: String,
    val signOutAction: String,
    val backendNotConfiguredTitle: String,
    val backendNotConfiguredBody: String,
)

private val EnglishStrings = AppStrings(
    dashboard = "Dashboard",
    schools = "Schools",
    academic = "Academic",
    finance = "Finance",
    inventory = "Inventory",
    fieldOps = "Field Ops",
    reports = "Reports",
    settings = "Settings",
    searchHint = "Command search",
    toggleTheme = "Switch Day/Night theme",
    toggleCalendar = "Switch AD/BS calendar",
    toggleLanguage = "Switch English/Nepali",
    notifications = "Notifications",
    signInTitle = "MindSparQ OS",
    emailLabel = "Work Email",
    passwordLabel = "Password",
    signInAction = "Sign In",
    signOutAction = "Sign Out",
    backendNotConfiguredTitle = "Supabase Connection Required",
    backendNotConfiguredBody = "Set MINDSPARQ_SUPABASE_URL and MINDSPARQ_SUPABASE_ANON_KEY to connect live records.",
)

private val NepaliStrings = AppStrings(
    dashboard = "ड्यासबोर्ड",
    schools = "विद्यालयहरू",
    academic = "शैक्षिक",
    finance = "वित्त",
    inventory = "मौज्दात",
    fieldOps = "फिल्ड अपरेसन",
    reports = "प्रतिवेदन",
    settings = "सेटिङ्स",
    searchHint = "खोज्नुहोस्",
    toggleTheme = "दिन/रात थिम बदल्नुहोस्",
    toggleCalendar = "AD/BS पात्रो बदल्नुहोस्",
    toggleLanguage = "अंग्रेजी/नेपाली बदल्नुहोस्",
    notifications = "सूचनाहरू",
    signInTitle = "माइन्डस्पार्क OS",
    emailLabel = "कार्य इमेल",
    passwordLabel = "पासवर्ड",
    signInAction = "लग इन",
    signOutAction = "लग आउट",
    backendNotConfiguredTitle = "Supabase जडान आवश्यक छ",
    backendNotConfiguredBody = "लाइभ डेटा जडान गर्न MINDSPARQ_SUPABASE_URL र MINDSPARQ_SUPABASE_ANON_KEY सेट गर्नुहोस्।",
)

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.EN -> EnglishStrings
    AppLanguage.NE -> NepaliStrings
}

/**
 * Formats amounts in standard Nepalese Rupee grouping (e.g., रू 1,25,000.00).
 */
fun formatNpr(amount: Double): String {
    val negative = amount < 0
    val totalPaise = (abs(amount) * 100.0).roundToLong()
    val rupees = totalPaise / 100
    val paise = (totalPaise % 100).toString().padStart(2, '0')
    val raw = rupees.toString()
    val formattedInt = if (raw.length <= 3) {
        raw
    } else {
        val lastThree = raw.takeLast(3)
        val rest = raw.dropLast(3)
        val groupedRest = rest.reversed().chunked(2).joinToString(",").reversed()
        "$groupedRest,$lastThree"
    }
    val prefix = if (negative) "-रू " else "रू "
    return "$prefix$formattedInt.$paise"
}
