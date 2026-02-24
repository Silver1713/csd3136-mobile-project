# Mobile Review Application

### Summary

Movie Review App is an Android application for movie discovery, review authoring, and personal watchlist tracking. The app combines:

- TMDB for public movie metadata.
- Firebase (Auth/Firestore/Storage) for user identity and app-owned social data.
- Room for local caching and reactive UI updates.

The implementation follows MVVM with repositories and Hilt-based dependency injection.

### Run Instructions

1. Open the project folder in Android Studio.
2. In `local.properties`, replace the TMDB key:
   `TMDB_API_TOKEN=your_tmdb_api_token_here`
   A working TMDB API key has already been provided.
3. Launch the app from Android Studio (Run on an emulator or connected Android device).
4. It is reccomended to use your own API key provided
5. You may register for an account at: <https://www.themoviedb.org> and generate your api key at: <https://www.themoviedb.org/settings/api>
