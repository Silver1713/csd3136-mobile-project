package com.csd3156.mobileproject.MovieReviewApp.data.repository

import com.csd3156.mobileproject.MovieReviewApp.BuildConfig
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.TmdbApiService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.toDomain
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// A repository implementation that builds on Retrofit/OkHttp/Moshi with the bearer token (from BuildConfig.TMDB_API_TOKEN)
class MovieRepositoryImpl(
    private val apiService: TmdbApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MovieRepository {

    override fun getPopularMovies(page: Int): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val genreMapper = buildGenreNameMapper()
        val response = apiService.getPopularMovies(page = page)
        val movies = response.results.map { genreMapper.map(it) }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getTrendingMovies(timeWindow: String, page: Int): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val genreMapper = buildGenreNameMapper()
        val response = apiService.getTrendingMovies(timeWindow = timeWindow, page = page)
        val movies = response.results.map { genreMapper.map(it) }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun searchMovies(
        query: String,
        page: Int,
        includeAdult: Boolean,
        year: Int?,
        primaryReleaseYear: Int?
    ): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val genreMapper = buildGenreNameMapper()
        val response = apiService.searchMovies(
            query = query,
            page = page,
            includeAdult = includeAdult,
            year = year,
            primaryReleaseYear = primaryReleaseYear
        )
        val movies = response.results.map { genreMapper.map(it) }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to search movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getMoviesByGenre(genreIds: List<Long>, page: Int): Flow<Resource<List<Movie>>> =
        discoverMovies(
            page = page,
            genreIds = genreIds
        )

    override fun discoverMovies(
        page: Int,
        sortBy: String?,
        genreIds: List<Long>?,
        releaseDateGte: String?,
        releaseDateLte: String?,
        voteAverageGte: Double?,
        voteAverageLte: Double?,
        voteCountGte: Int?,
        runtimeGte: Int?,
        runtimeLte: Int?,
        includeAdult: Boolean
    ): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val genreMapper = buildGenreNameMapper()
        val response = apiService.discoverMovies(
            page = page,
            sortBy = sortBy,
            withGenres = genreIds?.takeIf { it.isNotEmpty() }?.joinToString(","),
            releaseDateGte = releaseDateGte,
            releaseDateLte = releaseDateLte,
            voteAverageGte = voteAverageGte,
            voteAverageLte = voteAverageLte,
            voteCountGte = voteCountGte,
            runtimeGte = runtimeGte,
            runtimeLte = runtimeLte,
            includeAdult = includeAdult
        )
        val movies = response.results.map { genreMapper.map(it) }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to discover movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getMovieGenres(): Flow<Resource<List<Genre>>> = flow {
        emit(Resource.Loading)
        val response = apiService.getMovieGenres()
        val genres = response.genres.map { it.toDomain() }
        emit(Resource.Success(genres))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load genres",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getMovieDetails(movieId: Long): Flow<Resource<MovieDetails>> = flow {
        emit(Resource.Loading)
        val details = apiService.getMovieDetails(movieId)
        emit(Resource.Success(details.toDomain()))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movie details",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getMovieReviews(movieId: Long, page: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading)
        val response = apiService.getMovieReviews(movieId = movieId, page = page)
        val reviews = response.results.map { it.toDomain() }
        emit(Resource.Success(reviews))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movie reviews",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getMovieWatchProviders(
        movieId: Long,
        countryCode: String
    ): Flow<Resource<List<WatchProvider>>> = flow {
        emit(Resource.Loading)
        val response = apiService.getMovieWatchProviders(movieId = movieId)
        val providersByCountry = response.results[countryCode.uppercase()]
        val providers = providersByCountry?.toDomain(countryCode.uppercase()).orEmpty()
        emit(Resource.Success(providers))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load watch providers",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    private suspend fun buildGenreNameMapper(): GenreNameMapper {
        val genreLookup = apiService.getMovieGenres()
            .genres
            .associate { it.id to it.name }
        return GenreNameMapper(genreLookup)
    }

    class GenreNameMapper(
        private val genreIdToName: Map<Long, String>
    ) {
        fun map(movieDto: MovieDto): Movie {
            val genreNames = movieDto.genreIds.mapNotNull { genreIdToName[it] }
            return movieDto.toDomain(genres = genreNames)
        }
    }


    companion object {
        fun create(apiKey: String = BuildConfig.TMDB_API_TOKEN): MovieRepositoryImpl {
            require(apiKey.isNotBlank()) { "TMDB API key is missing" }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val newRequest = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Accept", "application/json")
                        .build()
                    chain.proceed(newRequest)
                }
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(TmdbApiService.BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(TmdbApiService::class.java)
            return MovieRepositoryImpl(service)
        }
    }
}
