# Android App Architecture Template

Use this file as the default starter architecture for future Android apps based on the structure of this project.

## Purpose

This template describes a practical Android setup built around:

- Single-activity app
- Jetpack Compose UI
- Feature-first package structure
- MVVM presentation layer
- Use cases for business logic
- Repository abstraction for data access
- Hilt for dependency injection
- Room for local persistence
- DataStore for lightweight local state
- Retrofit/OkHttp for remote APIs
- Coroutines and Flow for async work

This is the baseline architecture to use when creating a new app unless the new project clearly needs something different.

## Core Principles

- Keep `core/` for shared infrastructure and cross-feature primitives.
- Keep `features/` for feature-specific code, grouped by feature first.
- Presentation depends on domain contracts/use cases, not directly on Retrofit or Room.
- Data sources stay behind repositories.
- ViewModels expose `StateFlow` for screen state and `SharedFlow` for one-off events/errors.
- Navigation stays centralized in one app nav graph unless the app grows enough to split by feature graph.
- Shared services like dispatchers, networking, database, resources, and system observers are injected through Hilt.

## Recommended Project Layout

```text
app/src/main/java/com/example/app/
├── core/
│   ├── app/
│   │   ├── App.kt
│   │   ├── MainActivity.kt
│   │   ├── AppModule.kt
│   │   └── BaseViewModel.kt
│   ├── coroutine/
│   │   └── DispatcherModule.kt
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   └── DatabaseModule.kt
│   ├── location/
│   │   ├── LocationObserver.kt
│   │   ├── FusedLocationObserver.kt
│   │   ├── LocationModule.kt
│   │   ├── LocationState.kt
│   │   └── RequestLocationPermission.kt
│   ├── navigation/
│   │   └── AppNavGraph.kt
│   ├── network/
│   │   ├── ApiModule.kt
│   │   ├── NetworkModule.kt
│   │   ├── NetworkObserver.kt
│   │   ├── NetworkConnectivityObserver.kt
│   │   └── NetworkStatus.kt
│   ├── repository/
│   │   └── RepositoryModule.kt
│   ├── usecase/
│   │   └── UseCase.kt
│   └── utils/
│       ├── StateResource.kt
│       ├── providers/
│       │   └── ResourceProvider.kt
│       └── ui/
│           └── Dimensions.kt
├── features/
│   └── feature_name/
│       ├── data/
│       │   ├── local/
│       │   │   ├── data_store/
│       │   │   └── room/
│       │   │       ├── dao/
│       │   │       └── entity/
│       │   ├── network/
│       │   │   ├── dto/
│       │   │   └── FeatureApi.kt
│       │   └── repository/
│       │       └── FeatureRepository.kt
│       ├── domain/
│       │   ├── mapper/
│       │   ├── model/
│       │   ├── repository/
│       │   │   └── FeatureRepositoryImpl.kt
│       │   └── usecase/
│       └── presentation/
│           ├── event/
│           ├── state/
│           ├── ui/
│           │   ├── mappers/
│           │   ├── model/
│           │   └── screen/
│           └── viewmodel/
└── ui/
    └── theme/
```

## Layer Responsibilities

### `core/`

Shared application-wide infrastructure.

- `core/app`
  - `App.kt`: application class with `@HiltAndroidApp`
  - `MainActivity.kt`: Compose host and app entry point
  - `AppModule.kt`: app-level providers like `Context`, `ResourceProvider`, `DataStore`
  - `BaseViewModel.kt`: shared coroutine launching, error handling, common observers
- `core/coroutine`
  - dispatcher qualifiers and Hilt bindings
- `core/network`
  - Retrofit/OkHttp setup
  - connectivity observation contracts and implementation
- `core/database`
  - Room database and DAO providers
- `core/location`
  - location observation contracts and bindings
- `core/navigation`
  - root navigation graph
- `core/repository`
  - Hilt bindings from repository implementation to repository contract
- `core/utils`
  - wrappers like `StateResource`, `ResourceProvider`, spacing constants

### `features/<feature>/data`

Raw data access details.

- API interfaces
- DTOs
- Room entities and DAOs
- DataStore classes
- Repository contract consumed by domain/use cases

### `features/<feature>/domain`

Business logic and app-facing models.

- use cases
- domain models
- mappers from DTO/entity to domain
- repository implementation that coordinates local and remote sources

### `features/<feature>/presentation`

Everything the screen needs.

- screen state sealed class
- UI events sealed class
- ViewModel
- presentation models
- UI mappers from domain to presentation
- Compose screens/components

## Flow To Follow For A New Feature

Use this order when scaffolding a new feature:

1. Create the feature package under `features/<feature_name>/`.
2. Add `data/network/dto` models and API interface if remote data is needed.
3. Add `data/local` classes if caching or preferences are needed.
4. Define the feature repository interface in `data/repository/`.
5. Implement the repository in `domain/repository/`.
6. Bind the implementation in `core/repository/RepositoryModule.kt` or a feature-specific DI module.
7. Add domain models and mapping functions.
8. Add one or more use cases in `domain/usecase/`.
9. Define `presentation/state` and `presentation/event`.
10. Build the `ViewModel` using injected use cases and shared base behavior.
11. Add presentation models and UI mappers when domain models should not be rendered directly.
12. Create the Compose screen in `presentation/ui/screen/`.
13. Register the destination in `core/navigation/AppNavGraph.kt`.
14. Add unit tests for repository behavior, mappers, and use cases.

## ViewModel Pattern

Base pattern to reuse:

- Extend a shared `BaseViewModel` when the screen needs:
  - common coroutine launch helpers
  - centralized error emission
  - network observation
  - injected resource access
- Expose:
  - `StateFlow<FeatureState>` for persistent UI state
  - optional `StateFlow` for secondary state like permissions, connectivity, selected item, etc.
  - `SharedFlow<String>` or typed UI effects for snackbars/toasts/navigation effects
- Use an `onEvent(event)` entry point for UI-driven actions.
- Keep data fetching and transformation in use cases or repository/domain layer where possible.

Suggested shape:

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val loadFeatureUseCase: LoadFeatureUseCase,
    resourceProvider: ResourceProvider,
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : BaseViewModel(
    networkObserver = ...,
    resourceProvider = resourceProvider,
    mainDispatcher = mainDispatcher,
    ioDispatcher = ioDispatcher,
    defaultDispatcher = defaultDispatcher
) {
    private val _state = MutableStateFlow<FeatureState>(FeatureState.Loading)
    val state: StateFlow<FeatureState> = _state

    fun onEvent(event: FeatureEvent) {
        when (event) {
            FeatureEvent.Load -> load()
            FeatureEvent.Refresh -> refresh()
        }
    }
}
```

## UI Pattern

Compose screen conventions:

- Resolve the ViewModel with `hiltViewModel()`.
- Collect screen state with `collectAsState()`.
- Use `LaunchedEffect` for one-off side effects like:
  - initial loading
  - snackbar collection
  - permission requests
- Keep screens declarative:
  - `Loading`
  - `Success`
  - `Error`
  - empty or permission-denied states when relevant
- Prefer small reusable composables for cards, rows, placeholders, and error blocks.

## Repository Pattern

Repository contract and implementation should follow this rule:

- Interface lives in `features/<feature>/data/repository/`
- Implementation lives in `features/<feature>/domain/repository/`

This project’s pattern uses the repository implementation as the coordinator of:

- network fetches
- local cache reads/writes
- source selection rules delegated by use cases

For future apps, keep this pattern unless a different team standard is required.

Suggested repository shape:

```kotlin
interface FeatureRepository {
    suspend fun getCachedItems(param: String): List<ItemEntity>
    suspend fun fetchRemote(request: FeatureRequestDto): FeatureResponseDto
    suspend fun saveItems(param: String, items: List<ItemEntity>)
}

class FeatureRepositoryImpl @Inject constructor(
    private val api: FeatureApi,
    private val dao: FeatureDao
) : FeatureRepository
```

## Use Case Pattern

Use cases should:

- be small and focused
- hide orchestration logic from the ViewModel
- decide between cache and network when needed
- return either domain data or a result wrapper like `StateResource`
- use injected dispatchers rather than hardcoded dispatchers

Use a result wrapper like:

```kotlin
sealed class StateResource<out T> {
    data class Success<T>(val data: T) : StateResource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : StateResource<Nothing>()
}
```

## Dependency Injection Rules

Use Hilt consistently:

- `@HiltAndroidApp` on the application class
- `@AndroidEntryPoint` on the activity
- `@HiltViewModel` on ViewModels
- `@Binds` for interfaces to implementations
- `@Provides` for third-party objects and framework-backed instances
- `@Singleton` for shared app-wide services when appropriate

Prefer separate modules by concern:

- `AppModule`
- `DispatcherModule`
- `ApiModule`
- `DatabaseModule`
- `NetworkModule`
- `LocationModule`
- `RepositoryModule`

## Data Strategy

Default strategy for future apps:

- Use Room when the feature benefits from offline cache or queryable local data.
- Use DataStore for lightweight values like:
  - last selected item
  - onboarding flags
  - user preferences
  - last known coordinates
- Let use cases decide when cache is fresh enough to reuse.
- Keep DTOs, entities, domain models, and presentation models separate when they serve different purposes.

## Navigation Strategy

Default navigation setup:

- Keep a central `AppNavGraph.kt`
- Start with string routes if the app is small
- Move to typed route wrappers or feature graphs only when the app grows

Suggested start:

```kotlin
NavHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") { HomeScreen() }
}
```

## Testing Baseline

Every new app created from this template should start with tests for:

- repository implementation behavior
- important mappers
- core use cases

Useful stack:

- JUnit
- Mockito or MockK
- Coroutines test library
- Truth
- Robolectric when Android framework behavior is involved

Recommended minimum test files per feature:

- `FeatureRepositoryImplTest`
- `FeatureMapperTest`
- `FeatureUseCaseTest`
- `AnotherCriticalUseCaseTest` when feature logic is split

## Naming Conventions

- Feature folder names: lowercase, descriptive, singular or domain-specific
  - `home`, `profile`, `forecast`, `search`
- Repository interface:
  - `FeatureRepository`
- Repository implementation:
  - `FeatureRepositoryImpl`
- Use cases:
  - `GetXUseCase`
  - `LoadXUseCase`
  - `UpdateXUseCase`
- ViewModel:
  - `FeatureViewModel`
- UI state:
  - `FeatureState`
- UI events:
  - `FeatureEvent`
- Screen:
  - `FeatureScreen`

## Initial Gradle Stack To Reuse

Base dependencies from this project worth reusing:

- Android application plugin
- Kotlin Android
- Kotlin Compose plugin
- Hilt
- KSP
- Compose BOM
- Lifecycle runtime + ViewModel
- Retrofit
- OkHttp
- Coroutines
- DataStore
- Room
- Hilt navigation compose
- Testing: JUnit, coroutines test, Truth, Mockito/MockK, Robolectric

## Starter Prompt For Future Projects

Use this prompt when starting a new app from this template:

```text
Look at /Users/alex/StudioProjects/TheWeatherApp/ARCHITECTURE_TEMPLATE.md and use it as the base architecture template.
Create the initial Android app setup with:
- single activity
- Jetpack Compose
- Hilt
- feature-first structure
- MVVM + use cases + repository pattern
- navigation graph
- base core modules
- one starter feature scaffold

Project-specific details:
- App name: <APP_NAME>
- Package name: <PACKAGE_NAME>
- Main feature: <FEATURE_DESCRIPTION>
- Needs API: yes/no
- Needs Room cache: yes/no
- Needs DataStore: yes/no
- Needs permissions/location/camera/etc: <LIST>
```

## When To Deviate From This Template

Do not force this template if the new app clearly needs one of these:

- multi-module Gradle setup
- offline-first architecture across many features
- heavy background sync/work manager flows
- MVI instead of MVVM by team decision
- paging-heavy feeds
- feature modules for dynamic delivery
- backend-driven SDK architecture rather than a standard Android app

If none of those are true, start from this template.

## Summary

This template should be treated as the default starter architecture:

- `core/` for shared platform and infrastructure code
- `features/` for feature slices
- ViewModel + state/event in presentation
- use cases for business logic
- repository abstraction for data access
- Hilt for DI
- Compose for UI
- Room/DataStore/Retrofit as needed

Use it as the blueprint for future project bootstrapping.
