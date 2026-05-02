package de.kitshn.di

import de.kitshn.KitshnViewModel
import de.kitshn.KitshnViewModelArgs
import de.kitshn.SettingsViewModel
import de.kitshn.repo.FoodRepo
import de.kitshn.repo.ShoppingRepo
import de.kitshn.repo.SupermarketCategoryRepo
import de.kitshn.repo.UnitRepo
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration.Companion.seconds
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.context.startKoin

val APPLICATION_SCOPE_QUALIFIER = named("applicationScope")

/** Platform-specific bindings: [de.kitshn.AppDatabase] and its on-disk path. */
expect val platformModule: Module

private val coroutineModule = module {
    single<CoroutineScope>(APPLICATION_SCOPE_QUALIFIER) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

private val sessionModule = module {
    single { SettingsViewModel() }
    single { TandoorSession(settings = get()) }
}

private val repositoryModule = module {
    single { UnitRepo(db = get(), session = get(), scope = get(APPLICATION_SCOPE_QUALIFIER)) }
    single {
        SupermarketCategoryRepo(
            db = get(),
            session = get(),
            scope = get(APPLICATION_SCOPE_QUALIFIER),
        )
    }
    single {
        FoodRepo(
            db = get(),
            supermarketCategoryRepo = get(),
            unitRepo = get(),
            session = get(),
            scope = get(APPLICATION_SCOPE_QUALIFIER),
        )
    }
    single {
        ShoppingRepo(
            db = get(),
            unitRepo = get(),
            foodRepo = get(),
            supermarketCategoryRepo = get(),
            session = get(),
            scope = get(APPLICATION_SCOPE_QUALIFIER),
            // Shopping can be used in parallel to other users -> keep refreshed
            // TODO: propose a ServerPush or Socket approach upstream
            periodicInterval = 60.seconds,
        )
    }
}

private val viewModelModule = module {
    viewModel { (args: KitshnViewModelArgs) ->
        KitshnViewModel(
            db = get(),
            settings = get(),
            session = get(),
            unitRepo = get(),
            supermarketCategoryRepo = get(),
            foodRepo = get(),
            shoppingRepo = get(),
            applicationScope = get(APPLICATION_SCOPE_QUALIFIER),
            onBeforeCredentialsCheck = args.onBeforeCredentialsCheck,
            onLaunched = args.onLaunched,
        )
    }
}

fun sharedKoinModules(): List<Module> = listOf(
    platformModule,
    coroutineModule,
    sessionModule,
    repositoryModule,
    viewModelModule,
)

/**
 * Starts Koin with all shared modules. Must be called exactly once per process,
 * before the first composable that resolves an injected dependency.
 *
 * @param config hook for platform-specific configuration (e.g. `androidContext`).
 */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication = startKoin {
    config?.invoke(this)
    modules(sharedKoinModules())
}
