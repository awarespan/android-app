package com.awarespan.guardian.di;

import com.awarespan.guardian.data.remote.api.AuthApiService;
import com.awarespan.guardian.data.remote.api.SipApiService;
import com.awarespan.guardian.data.remote.interceptor.AuthInterceptor;
import com.awarespan.guardian.data.remote.interceptor.TokenRefreshAuthenticator;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    private static final String BASE_URL = com.awarespan.guardian.BuildConfig.API_BASE_URL;
    private static final long TIMEOUT_SECONDS = 20;

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
                                     TokenRefreshAuthenticator authenticator,
                                     com.awarespan.guardian.data.logging.DiagnosticLogger diagnosticLogger) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(com.awarespan.guardian.BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .authenticator(authenticator)
                .addInterceptor(chain -> {
                    long start = System.currentTimeMillis();
                    okhttp3.Request req = chain.request();
                    try {
                        okhttp3.Response res = chain.proceed(req);
                        long dur = System.currentTimeMillis() - start;
                        diagnosticLogger.logApiCall(req.method(), req.url().encodedPath(), res.code(), dur);
                        return res;
                    } catch (java.io.IOException e) {
                        long dur = System.currentTimeMillis() - start;
                        diagnosticLogger.warn("Network", req.method() + " " + req.url().encodedPath() + " failed after " + dur + "ms", e);
                        throw e;
                    }
                })
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    @Named("unauthenticated")
    OkHttpClient provideUnauthenticatedOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    SipApiService provideSipApi(Retrofit retrofit) {
        return retrofit.create(SipApiService.class);
    }

    @Provides
    @Singleton
    AuthApiService provideAuthApi(@Named("unauthenticated") OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService.class);
    }
}
