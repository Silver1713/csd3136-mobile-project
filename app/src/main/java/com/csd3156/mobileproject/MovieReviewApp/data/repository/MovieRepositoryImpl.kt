package com.csd3156.mobileproject.MovieReviewApp.data.repository

import com.csd3156.mobileproject.MovieReviewApp.BuildConfig
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.TmdbApiService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.toDomain
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
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

    override fun getPopularMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val response = apiService.getPopularMovies()
        val movies = response.results.map { it.toDomain() }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)

    override fun getTrendingMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading)
        val response = apiService.GetTrendingMovies()
        val movies = response.results.map { it.toDomain() }
        emit(Resource.Success(movies))
    }.catch { throwable ->
        emit(
            Resource.Error(
                message = throwable.message ?: "Unable to load movies",
                throwable = throwable
            )
        )
    }.flowOn(ioDispatcher)


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
