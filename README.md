## MicroBlogs
This codebase was created to demonstrate a fully fledged fullstack application built
with **Kotlin** including CRUD operations, authentication, routing, pagination, and more.

See how a Medium.com clone (called Conduit) is built using Kotlin in Android to connect
to any other backend from https://realworld.io/.

For more information on how to this works with other backends, head over to
the [RealWorld](https://github.com/gothinkster/realworld) repo.

## Built With
- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
  - [ViewBinding](https://developer.android.com/topic/libraries/view-binding) - Generates a binding class for each XML layout file present in that module and allows you to more easily write code that interacts with views.
- [Dependency Injection](https://developer.android.com/training/dependency-injection) -
  - [Hilt-Dagger](https://dagger.dev/hilt/) - Standard library to incorporate Dagger dependency injection into an Android application.
  - [Hilt-ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack) - DI for injecting `ViewModel`.
- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
- [Moshi](https://github.com/square/moshi) - A modern JSON library for Kotlin and Java.
- [Paging3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) - The Paging library helps load and display pages of data from a larger dataset from local storage or over network.
- [ViewPager2](https://developer.android.com/guide/navigation/navigation-swipe-view-2) - Create swipe views with tabs using ViewPager2
- [Glide](https://github.com/bumptech/glide) - Glide is a fast and efficient open source media management and image loading framework for Android that wraps media decoding, memory and disk caching, and resource pooling into a simple and easy to use interface.
- [Material Components for Android](https://github.com/material-components/material-components-android) - Modular and customizable Material Design UI components for Android.
  
  
## Package Structure

    libconduit                          # Root Api
    ├── apis                            # Libconduit api
    ├── models                          # Model classes
    ├── requests                        # Different api requests
    ├── responses                       # Different api responses
    |-- ConduitClient                   # Client Api
  ----------------------------------------------------------------------------------------------------------------------------------------------------
    App                                 # Root UI
    ├── adapters                        # Different Adapters
        ├── paging adapters             # paginng adapters
        ├── viewpaging                  # viewpager adapter
        |-- recyclerviewAdapters        # recycler view adapters
    |
    ├── data                            # For data handling
        |-- repo                        # Repository with different sources
    |
    ├── di                              # Hilt di modules
    |
    ├── ui                              # Fragments
        ├── account
        ├── addEditArticle
        ├── article
        ├── auth
        ├── editArticle
        ├── favouriteArticle
        ├── feed
        ├── profile
        ├── tags
        ├── tagsFeed
        ├── userArticles
    |
    ├── uitls                           # Util file like constants, genric result classes
        ├── internet connectivity       # Monitor internet connectivity using live data
    ├── view models                     # viewmodels
 
    
 ## Demo
  video - [youtube](https://youtu.be/ATFkS2dUVoY)
  app - []
