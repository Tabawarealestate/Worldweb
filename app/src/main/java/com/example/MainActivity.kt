package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.SecurityPreferences
import com.example.data.repository.AdminRepository
import com.example.data.repository.MarketDataRepository
import com.example.data.repository.MarketSurgeRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.WalletRepository
import com.example.domain.model.ContractDirection
import com.example.domain.model.InstrumentCategory
import com.example.domain.model.ProductType
import com.example.domain.model.TradeOutcome
import com.example.domain.model.TradeSelection
import com.example.domain.model.WorldwideCurrencies
import com.example.ui.components.CinematicLoadingBar
import com.example.ui.components.OrderSlipSheet
import com.example.ui.components.TradeSlipSheet
import com.example.ui.components.WithdrawalPinDialog
import com.example.ui.screens.AppPinLockScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketDetailScreen
import com.example.ui.screens.MarketSurgeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MarketSurgeViewModel
import com.example.ui.viewmodel.MarketViewModel
import com.example.ui.viewmodel.OrderSlipViewModel
import com.example.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.compose.material.icons.filled.TrendingUp
import com.example.ui.theme.ZorivoBlueHighlight
import com.example.ui.theme.ZorivoPrimaryBlue

enum class MainNavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MARKETS("Markets", Icons.Default.ShowChart),
    TRADE("Trade", Icons.Default.TrendingUp),
    SURGE("Surge", Icons.Default.Bolt),
    ORDERS("Orders", Icons.Default.ListAlt),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZorivoApp()
            }
        }
    }
}

@Composable
fun ZorivoApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val securityPrefs = remember { SecurityPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    val marketRepo = remember {
        MarketDataRepository(database.instrumentDao(), database.quoteDao())
    }
    val adminRepo = remember {
        AdminRepository(database.productDao(), database.auditDao())
    }
    val walletRepo = remember {
        WalletRepository(database.walletDao(), database.ledgerDao(), database.auditDao())
    }
    val orderRepo = remember {
        OrderRepository(database.orderDao(), database.quoteDao(), walletRepo, database.auditDao())
    }
    val surgeRepo = remember {
        MarketSurgeRepository(database.surgeDao(), database.quoteDao(), walletRepo, database.auditDao())
    }

    val marketViewModel: MarketViewModel = viewModel {
        MarketViewModel(marketRepo, adminRepo)
    }
    val orderSlipViewModel: OrderSlipViewModel = viewModel {
        OrderSlipViewModel(orderRepo)
    }
    val surgeViewModel: MarketSurgeViewModel = viewModel {
        MarketSurgeViewModel(surgeRepo)
    }
    val walletViewModel: WalletViewModel = viewModel {
        WalletViewModel(walletRepo)
    }

    // State collections
    val marketState by marketViewModel.uiState.collectAsStateWithLifecycle()
    val filteredQuotes by marketViewModel.filteredQuotes.collectAsStateWithLifecycle()
    val allInstruments by marketViewModel.allInstruments.collectAsStateWithLifecycle()
    val walletBalance by walletViewModel.walletBalance.collectAsStateWithLifecycle()
    val openOrders by orderSlipViewModel.openOrders.collectAsStateWithLifecycle()
    val settledOrders by orderSlipViewModel.settledOrders.collectAsStateWithLifecycle()
    val surgeState by surgeViewModel.uiState.collectAsStateWithLifecycle()
    val slipState by orderSlipViewModel.slipState.collectAsStateWithLifecycle()
    val walletUiState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val ledgerHistory by walletViewModel.ledgerRecords.collectAsStateWithLifecycle()

    // Navigation and Security States
    var isAuthenticated by remember { mutableStateOf(securityPrefs.isLoggedIn) }
    var isAppPinLocked by remember { mutableStateOf(securityPrefs.hasAppPin()) }
    var isSetupPinMode by remember { mutableStateOf(false) }
    var isWithdrawalPinDialogVisible by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    var selectedMarketSymbol by remember { mutableStateOf<String?>(null) }

    // Trade Slip Sheet State
    var isTradeSlipOpen by remember { mutableStateOf(false) }
    val tradeSlipSelections = remember {
        mutableStateListOf(
            TradeSelection(
                id = "SEL-01",
                symbol = "BTC/USD",
                productType = ProductType.HIGHER_LOWER,
                direction = ContractDirection.HIGHER,
                durationSeconds = 60,
                referencePrice = 84166.93,
                stake = 5000.0,
                potentialPayout = 9500.0,
                cashoutEligible = true,
                currentCashoutValue = 4850.0,
                outcome = TradeOutcome.ACTIVE
            ),
            TradeSelection(
                id = "SEL-02",
                symbol = "AAPL",
                productType = ProductType.ABOVE_BELOW,
                direction = ContractDirection.ABOVE,
                durationSeconds = 300,
                referencePrice = 338.40,
                stake = 3000.0,
                potentialPayout = 5700.0,
                cashoutEligible = true,
                currentCashoutValue = 2920.0,
                outcome = TradeOutcome.ACTIVE
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(slipState.successMessage) {
        slipState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    LaunchedEffect(walletUiState.successMessage) {
        walletUiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    LaunchedEffect(surgeState.message) {
        surgeState.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // 1. AUTH SCREEN (If not logged in)
    if (!isAuthenticated) {
        AuthScreen(
            onAuthSuccess = { email ->
                securityPrefs.userEmail = email
                securityPrefs.isLoggedIn = true
                isAuthenticated = true
                if (!securityPrefs.hasAppPin()) {
                    isSetupPinMode = true
                }
            }
        )
        return
    }

    // 2. APP RE-ENTRY PIN LOCK (If app PIN is active and locked)
    if (isAppPinLocked) {
        AppPinLockScreen(
            isSetupMode = false,
            onPinSuccess = { isAppPinLocked = false },
            onSaveNewPin = { securityPrefs.saveAppPin(it) },
            onVerifyPin = { securityPrefs.verifyAppPin(it) },
            onForgotPasswordOrLogin = {
                securityPrefs.isLoggedIn = false
                isAuthenticated = false
                isAppPinLocked = false
            }
        )
        return
    }

    // 3. CREATE APP RE-ENTRY PIN (If prompted to setup PIN)
    if (isSetupPinMode) {
        AppPinLockScreen(
            isSetupMode = true,
            onPinSuccess = { isSetupPinMode = false },
            onSaveNewPin = { securityPrefs.saveAppPin(it) },
            onVerifyPin = { true },
            onForgotPasswordOrLogin = { isSetupPinMode = false }
        )
        return
    }

    // 4. MAIN USER APPLICATION
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CinematicLoadingBar(
                isLoading = marketState.isRefreshing || walletUiState.isProcessing || slipState.isSubmitting
            )
        },
        bottomBar = {
            if (selectedMarketSymbol == null) {
                NavigationBar(
                    containerColor = TerminalSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    MainNavTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (tab == MainNavTab.TRADE) {
                                    isTradeSlipOpen = true
                                } else {
                                    currentTab = tab
                                    if (tab == MainNavTab.SURGE) {
                                        marketViewModel.selectCategory(InstrumentCategory.MARKET_SURGE)
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) ElectricCyan else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricCyan else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = TerminalDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedMarketSymbol != null -> {
                    val currentQuote = filteredQuotes.find { it.symbol == selectedMarketSymbol }
                    val currentInst = allInstruments.find { it.symbol == selectedMarketSymbol }
                    MarketDetailScreen(
                        symbol = selectedMarketSymbol!!,
                        quote = currentQuote,
                        instrument = currentInst,
                        onBack = { selectedMarketSymbol = null },
                        onOpenOrderSlip = { sym, dir, prod, price ->
                            orderSlipViewModel.openSlip(sym, dir, prod, price)
                        }
                    )
                }

                else -> {
                    when (currentTab) {
                        MainNavTab.HOME -> {
                            HomeScreen(
                                uiState = marketState,
                                quotes = filteredQuotes,
                                instruments = allInstruments,
                                walletBalance = walletBalance,
                                openOrders = openOrders,
                                surgeRound = surgeState.currentRound,
                                selectedCurrencyCode = walletUiState.selectedCurrencyCode,
                                tradeSlipCount = tradeSlipSelections.size,
                                onCategorySelected = { marketViewModel.selectCategory(it) },
                                onSearchChanged = { marketViewModel.setSearchQuery(it) },
                                onSelectMarket = { selectedMarketSymbol = it },
                                onQuickOrder = { sym, dir, price ->
                                    orderSlipViewModel.openSlip(sym, dir, ProductType.HIGHER_LOWER, price)
                                },
                                onNavigateToSurge = { currentTab = MainNavTab.SURGE },
                                onNavigateToWallet = { currentTab = MainNavTab.WALLET },
                                onOpenTradeSlip = { isTradeSlipOpen = true },
                                onLoadTradeSlipCode = { code ->
                                    tradeSlipSelections.add(
                                        TradeSelection(
                                            id = "IMP-${System.currentTimeMillis() % 1000}",
                                            symbol = "SOL/USD",
                                            productType = ProductType.HIGHER_LOWER,
                                            direction = ContractDirection.HIGHER,
                                            durationSeconds = 60,
                                            referencePrice = 188.40,
                                            stake = 2500.0,
                                            potentialPayout = 4800.0,
                                            cashoutEligible = true,
                                            currentCashoutValue = 2450.0,
                                            outcome = TradeOutcome.ACTIVE
                                        )
                                    )
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Trade Slip Code $code imported with 1 position!")
                                    }
                                },
                                onRefresh = { marketViewModel.refreshMarketData() },
                                onSelectCurrency = { walletViewModel.setCurrency(it) }
                            )
                        }

                        MainNavTab.MARKETS -> {
                            HomeScreen(
                                uiState = marketState,
                                quotes = filteredQuotes,
                                instruments = allInstruments,
                                walletBalance = walletBalance,
                                openOrders = openOrders,
                                surgeRound = surgeState.currentRound,
                                selectedCurrencyCode = walletUiState.selectedCurrencyCode,
                                tradeSlipCount = tradeSlipSelections.size,
                                onCategorySelected = { marketViewModel.selectCategory(it) },
                                onSearchChanged = { marketViewModel.setSearchQuery(it) },
                                onSelectMarket = { selectedMarketSymbol = it },
                                onQuickOrder = { sym, dir, price ->
                                    orderSlipViewModel.openSlip(sym, dir, ProductType.HIGHER_LOWER, price)
                                },
                                onNavigateToSurge = { currentTab = MainNavTab.SURGE },
                                onNavigateToWallet = { currentTab = MainNavTab.WALLET },
                                onOpenTradeSlip = { isTradeSlipOpen = true },
                                onLoadTradeSlipCode = { code ->
                                    tradeSlipSelections.add(
                                        TradeSelection(
                                            id = "IMP-${System.currentTimeMillis() % 1000}",
                                            symbol = "XAU/USD",
                                            productType = ProductType.TOUCH_NO_TOUCH,
                                            direction = ContractDirection.HIGHER,
                                            durationSeconds = 120,
                                            referencePrice = 4140.80,
                                            stake = 4000.0,
                                            potentialPayout = 7600.0,
                                            cashoutEligible = true,
                                            currentCashoutValue = 3890.0,
                                            outcome = TradeOutcome.ACTIVE
                                        )
                                    )
                                },
                                onRefresh = { marketViewModel.refreshMarketData() },
                                onSelectCurrency = { walletViewModel.setCurrency(it) }
                            )
                        }

                        MainNavTab.SURGE -> {
                            MarketSurgeScreen(
                                state = surgeState,
                                walletBalance = walletBalance,
                                onStakeChange = { surgeViewModel.setStake(it) },
                                onAutoCashoutChange = { surgeViewModel.setAutoCashout(it) },
                                onEnterRound = { surgeViewModel.enterCurrentRound() },
                                onCashOut = { surgeViewModel.cashOutNow() }
                            )
                        }

                        MainNavTab.ORDERS -> {
                            OrdersScreen(
                                openOrders = openOrders,
                                settledOrders = settledOrders,
                                onSettleOrder = { orderSlipViewModel.settleOrderNow(it) }
                            )
                        }

                        MainNavTab.WALLET -> {
                            WalletScreen(
                                walletBalance = walletBalance,
                                ledgerRecords = ledgerHistory,
                                uiState = walletUiState,
                                onOpenDeposit = { walletViewModel.openDepositDialog() },
                                onCloseDeposit = { walletViewModel.closeDepositDialog() },
                                onOpenWithdraw = { walletViewModel.openWithdrawDialog() },
                                onCloseWithdraw = { walletViewModel.closeWithdrawDialog() },
                                onDepositAmountChange = { walletViewModel.setDepositAmount(it) },
                                onWithdrawAmountChange = { walletViewModel.setWithdrawAmount(it) },
                                onBankAccountChange = { walletViewModel.setBankAccountNumber(it) },
                                onProcessDeposit = { walletViewModel.processDeposit() },
                                onProcessWithdraw = {
                                    // Trigger 4-Digit Withdrawal PIN Verification before processing
                                    isWithdrawalPinDialogVisible = true
                                },
                                onSelectCurrency = { walletViewModel.setCurrency(it) },
                                onSelectDepositRail = { walletViewModel.setDepositRail(it) },
                                onSelectAgent = { walletViewModel.selectAgent(it) }
                            )
                        }

                        MainNavTab.TRADE -> {
                            LaunchedEffect(Unit) {
                                isTradeSlipOpen = true
                                currentTab = MainNavTab.HOME
                            }
                        }
                    }
                }
            }

            // High-Speed Sportsbook-style Order Slip (Global Dockable Modal)
            OrderSlipSheet(
                state = slipState,
                onDismiss = { orderSlipViewModel.closeSlip() },
                onStakeChange = { orderSlipViewModel.setStake(it) },
                onDurationChange = { orderSlipViewModel.setDuration(it) },
                onDirectionChange = { orderSlipViewModel.setDirection(it) },
                onProductTypeChange = { orderSlipViewModel.setProductType(it) },
                onSubmitOrder = { orderSlipViewModel.placeOrder() }
            )

            // Multi-Position Trade Slip with Share Code & Cashout
            val currInfo = WorldwideCurrencies.getByCode(walletUiState.selectedCurrencyCode)
            TradeSlipSheet(
                isOpen = isTradeSlipOpen,
                onDismiss = { isTradeSlipOpen = false },
                selections = tradeSlipSelections,
                userCurrencySymbol = currInfo.symbol,
                onRemoveSelection = { id -> tradeSlipSelections.removeAll { it.id == id } },
                onExecuteCashout = { id, amount ->
                    val idx = tradeSlipSelections.indexOfFirst { it.id == id }
                    if (idx != -1) {
                        tradeSlipSelections[idx] = tradeSlipSelections[idx].copy(
                            outcome = TradeOutcome.CASHED_OUT,
                            cashoutEligible = false
                        )
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Position early cashout settled: ${currInfo.symbol}${"%,.0f".format(amount)}")
                    }
                },
                onGenerateShareCode = {
                    val code = "FX" + (1000..9999).random() + "Q" + (10..99).random()
                    code
                },
                onLoadCode = { code ->
                    tradeSlipSelections.add(
                        TradeSelection(
                            id = "SLIP-${UUID.randomUUID().toString().take(6)}",
                            symbol = "ETH/USD",
                            productType = ProductType.HIGHER_LOWER,
                            direction = ContractDirection.HIGHER,
                            durationSeconds = 120,
                            referencePrice = 3450.0,
                            stake = 3000.0,
                            potentialPayout = 5700.0,
                            cashoutEligible = true,
                            currentCashoutValue = 2850.0,
                            outcome = TradeOutcome.ACTIVE
                        )
                    )
                },
                onConfirmAllTrades = {
                    isTradeSlipOpen = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("All ${tradeSlipSelections.size} positions revalidated & locked with exchange.")
                    }
                }
            )

            // 4-Digit Withdrawal PIN Dialog
            if (isWithdrawalPinDialogVisible) {
                WithdrawalPinDialog(
                    isCreateMode = !securityPrefs.hasWithdrawalPin(),
                    onDismiss = { isWithdrawalPinDialogVisible = false },
                    onPinConfirmed = { pin ->
                        isWithdrawalPinDialogVisible = false
                        if (!securityPrefs.hasWithdrawalPin()) {
                            securityPrefs.saveWithdrawalPin(pin)
                            walletViewModel.processWithdrawal()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Withdrawal PIN created and withdrawal request submitted!")
                            }
                        } else {
                            if (securityPrefs.verifyWithdrawalPin(pin)) {
                                walletViewModel.processWithdrawal()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Incorrect withdrawal PIN. Transaction cancelled.")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
