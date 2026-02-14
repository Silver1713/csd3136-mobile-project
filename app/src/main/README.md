# This is an explanation of what each folder does, and how the app is structured.

## Common
For shared classes, types across the entire codebase.
+ Resource
Represents the potential states (loading, success, error), and can hold the returned object or an error message.

## Data
For storing and manipulating data.
### Local.database
Local Data (storage and manipulation)
+ Account
Room database for storing user profile and login data
+ DateConvertor, UUIDConvertor
Utility classes to convert date <-> num and UUID <-> strings 


### Remote
Remote Data (storage and manipulation)
+ API
  + TmdbApiService
  Retrofit Class used to read movie data from TMDB API, like popular movies or thumbnail images
+ dto
API json data is converted by retrofit into DTOs, which can then convert it into clean data types used by the app like Movie.
DTOs act as the middleman to "clean up and parse" json data
  + MovieDto
  Detailed movie data, converted to a Movie object
  + MovieListDto
  List of MovieDto, representing a page of movies
  + TmdbExtraDtos
  Genre, MovieDetails, Images, Videos, Reviews, Authors, CountryWatchProviders and Providers

### Repository
One-Stop shop for accessing data
+ MovieRepositoryImpl
Implementation of MovieRepository. Not accessed directly but rather through the MovieRepository interface (i.e. when creating a MovieRepository object, assign this Impl() to the object.)

## Domain
### Model
Represents Data Types related to Movie data.
+ Genre
Movie Genre
+ Movie
Basic movie details, typically for preview purposes
+ MovieDetails
Full movie details, typically used when the user clicks on the movie to see more.
+ MovieReview
A single movie review
+ WatchProvider
Platform/Country where movie is available for watching
### Repository
+ MovieRepository
Interface for getting movie data, hiding background logic for accessing the TMDB API.
Implementations: MovieRepositoryImpl in remote/repository
## UI
UI State data, UI Components and Layouts
### Components
+ Components
Reusable Composables to display items like Sections and Images
### Main
+ HomeScreen
+ HomeScreenViewModel
UI State data
### Movies.List
List of movies
+ MoviewListRoute
+ MovieListViewModel
UI State data
### Theme
+ Color
Commonly used color types in the app
+ Theme
App color schemes
+ Type
Font style

## MainActivity
Mainly controls navigation using NavDisplay and the bottom bar.