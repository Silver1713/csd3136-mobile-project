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
+ dto
  + MovieDto
  + MovieListDto
  + TmdbExtraDtos

### Repository
One-Stop shop for accessing data
+ MovieRepositoryImpl

## Domain
### Model
+ Genre
+ Movie
+ MovieDetails
+ MovieReview
+ WatchProvider
### Repository
+ MovieRepository
## UI
### Components
+ Components
### Main
+ HomeScreen
+ HomeScreenViewModel
### Movies.List
+ MoviewListRoute
+ MovieListViewModel
### Theme
+ Color
+ Theme
+ Type

## MainActivity
