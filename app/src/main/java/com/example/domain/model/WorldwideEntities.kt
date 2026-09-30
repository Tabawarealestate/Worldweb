package com.example.domain.model

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val exchangeRateToUsd: Double // 1 USD = X Currency
)

object WorldwideCurrencies {
    val list = listOf(
        CurrencyInfo("USD", "United States Dollar", "$", "🇺🇸", 1.0),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺", 0.92),
        CurrencyInfo("GBP", "British Pound Sterling", "£", "🇬🇧", 0.79),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", "🇳🇬", 1620.0),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵", 154.5),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳", 7.24),
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳", 84.4),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦", 1.38),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺", 1.54),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭", 0.90),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦", 18.2),
        CurrencyInfo("AED", "UAE Dirham", "د.إ", "🇦🇪", 3.67),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷", 5.75),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh", "🇰🇪", 129.5),
        CurrencyInfo("GHS", "Ghanaian Cedi", "GH₵", "🇬🇭", 16.3),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬", 1.34)
    )

    fun getByCode(code: String): CurrencyInfo {
        return list.find { it.code.equals(code, ignoreCase = true) } ?: list[0]
    }
}

enum class CryptoNetwork(val displayName: String, val feeUsd: Double) {
    BITCOIN("Bitcoin Mainnet", 2.50),
    ETHEREUM_ERC20("Ethereum (ERC-20)", 4.00),
    TRON_TRC20("Tron (TRC-20)", 1.00),
    SOLANA("Solana High-Speed", 0.10),
    BSC_BEP20("BNB Smart Chain (BEP-20)", 0.30)
}

data class CryptoAssetInfo(
    val symbol: String,
    val name: String,
    val supportedNetworks: List<CryptoNetwork>,
    val depositAddress: String,
    val minDeposit: Double,
    val minWithdrawal: Double
)

object WorldwideCryptoRegistry {
    val assets = listOf(
        CryptoAssetInfo("USDT", "Tether USD", listOf(CryptoNetwork.TRON_TRC20, CryptoNetwork.ETHEREUM_ERC20, CryptoNetwork.SOLANA, CryptoNetwork.BSC_BEP20), "TY8ZqP7xR3...USDT_VAULT_ADDRESS", 10.0, 10.0),
        CryptoAssetInfo("BTC", "Bitcoin", listOf(CryptoNetwork.BITCOIN), "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", 0.0005, 0.0005),
        CryptoAssetInfo("ETH", "Ethereum", listOf(CryptoNetwork.ETHEREUM_ERC20), "0x71C...ETH_VAULT_MAINNET_SETTLEMENT", 0.01, 0.01),
        CryptoAssetInfo("SOL", "Solana", listOf(CryptoNetwork.SOLANA), "So11111111111111111111111111111111111111112", 0.1, 0.1),
        CryptoAssetInfo("USDC", "USD Coin", listOf(CryptoNetwork.SOLANA, CryptoNetwork.ETHEREUM_ERC20), "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", 10.0, 10.0)
    )
}

data class PaymentAgent(
    val agentId: String,
    val name: String,
    val country: String,
    val currency: String,
    val supportedRails: List<String>,
    val rating: Double,
    val completedTransactions: Int,
    val minLimit: Double,
    val maxLimit: Double,
    val feePercent: Double,
    val isOnline: Boolean,
    val avgResponseMin: Int,
    val contactRef: String
)

object VerifiedPaymentAgentsRegistry {
    val agents = listOf(
        PaymentAgent(
            agentId = "AGT-GLB-01",
            name = "Apex Global Liquidity",
            country = "Global / UK / EU",
            currency = "USD",
            supportedRails = listOf("SEPA Instant", "SWIFT Wire", "Revolut"),
            rating = 4.98,
            completedTransactions = 14200,
            minLimit = 50.0,
            maxLimit = 100_000.0,
            feePercent = 0.5,
            isOnline = true,
            avgResponseMin = 2,
            contactRef = "apex_liquidity_desk"
        ),
        PaymentAgent(
            agentId = "AGT-NG-02",
            name = "Lagos Prime Exchange Ltd",
            country = "Nigeria",
            currency = "NGN",
            supportedRails = listOf("NIBSS Instant Transfer", "Commercial Bank Transfer", "Direct NUBAN"),
            rating = 4.95,
            completedTransactions = 28940,
            minLimit = 1000.0,
            maxLimit = 5_000_000.0,
            feePercent = 0.8,
            isOnline = true,
            avgResponseMin = 1,
            contactRef = "lagos_prime_agent"
        ),
        PaymentAgent(
            agentId = "AGT-KE-03",
            name = "Nairobi Safari FastPay",
            country = "Kenya / East Africa",
            currency = "KES",
            supportedRails = listOf("M-Pesa Express", "Airtel Money", "Pesalink"),
            rating = 4.92,
            completedTransactions = 11200,
            minLimit = 500.0,
            maxLimit = 350_000.0,
            feePercent = 0.7,
            isOnline = true,
            avgResponseMin = 3,
            contactRef = "safari_fastpay"
        ),
        PaymentAgent(
            agentId = "AGT-SA-04",
            name = "Cape Town Exchange",
            country = "South Africa",
            currency = "ZAR",
            supportedRails = listOf("EFT Secure", "Ozow", "Capitec Pay"),
            rating = 4.96,
            completedTransactions = 8900,
            minLimit = 200.0,
            maxLimit = 150_000.0,
            feePercent = 0.6,
            isOnline = true,
            avgResponseMin = 2,
            contactRef = "capetown_exchange"
        )
    )
}
