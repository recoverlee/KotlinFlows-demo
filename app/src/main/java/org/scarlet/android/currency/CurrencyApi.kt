package org.scarlet.android.currency

interface CurrencyApi {
    suspend fun getExchangeRate(countryCode: String): Double
}