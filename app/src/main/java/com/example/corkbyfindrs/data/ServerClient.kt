package com.sajsoft.cork2025.data

import android.content.Context
import android.util.Base64 // For Base64 encoding
import android.util.Log
// import com.sajsoft.cork2025.R // Removed R import if it was only for findrs_crt
import com.sajsoft.cork2025.data.models.* // Import all models
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File // Single import for File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale // Single import for Locale
import java.util.TimeZone
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection
// import java.security.KeyStore // Removed
// import java.security.cert.CertificateFactory // Removed
// import javax.net.ssl.SSLContext // Removed
// import javax.net.ssl.TrustManagerFactory // Removed
// import javax.net.ssl.X509TrustManager // Removed

// Global helper function (or move to a Utils file)
internal fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

class ServerClient(
    private val context: Context, // Context may still be needed for other things like resources
    private val userSettingsRepository: UserSettingsRepository
) {

    // private var sslContext: SSLContext? = null // Removed
    // private var trustManagerFactory: TrustManagerFactory? = null // Removed
    private val baseUrl = "https://findrs.net/ssh/"
    private val TAG = "ServerClient"

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    // init { // Removed
        // initializeSSLContext() // Removed
    // } // Removed

    // private fun initializeSSLContext() { // Removed
    //     try {
    //         val cf = CertificateFactory.getInstance("X.509")
    //         val caInput: InputStream = context.resources.openRawResource(R.raw.findrs_crt)
    //         val ca = caInput.use { cf.generateCertificate(it) }
    //
    //         val keyStoreType = KeyStore.getDefaultType()
    //         val keyStore = KeyStore.getInstance(keyStoreType)
    //         keyStore.load(null, null)
    //         keyStore.setCertificateEntry("ca", ca)
    //
    //         val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    //         trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
    //         trustManagerFactory?.init(keyStore)
    //
    //         sslContext = SSLContext.getInstance("TLS")
    //         sslContext?.init(null, trustManagerFactory?.trustManagers, null)
    //     } catch (e: Exception) {
    //         Log.e(TAG, "SSL initialization error", e)
    //     }
    // }

    suspend fun makePostRequest(endpointUrl: String, params: Map<String, String>): String = withContext(Dispatchers.IO) {
        val fullUrl = URL(baseUrl + endpointUrl)
        var urlConnection: HttpsURLConnection? = null
        try {
            urlConnection = fullUrl.openConnection() as HttpsURLConnection
            // sslContext?.let { urlConnection.sslSocketFactory = it.socketFactory } // Removed

            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            urlConnection.setRequestProperty("Accept", "application/json")

            if (endpointUrl == "usersdevices.php" || endpointUrl == "usersbikes.php") {
                urlConnection.readTimeout = 30000 // 30 seconds
                urlConnection.connectTimeout = 30000 // 30 seconds
            } else {
                urlConnection.readTimeout = 10000 // Default 10 seconds
                urlConnection.connectTimeout = 10000 // Default 10 seconds
            }
            urlConnection.doOutput = true

            val jsonParams = JSONObject()
            for ((key, value) in params) {
                jsonParams.put(key, value)
            }

            Log.d(TAG, "Sending 'POST' request to URL : $fullUrl")
            Log.d(TAG, "Post parameters : $jsonParams")

            DataOutputStream(urlConnection.outputStream).use { wr ->
                wr.write(jsonParams.toString().toByteArray(Charsets.UTF_8))
                wr.flush()
            }

            val responseCode = urlConnection.responseCode
            Log.d(TAG, "Response Code : $responseCode")

            val responseStream = if (responseCode >= HttpsURLConnection.HTTP_BAD_REQUEST) {
                urlConnection.errorStream ?: urlConnection.inputStream
            } else {
                urlConnection.inputStream
            }

            BufferedReader(InputStreamReader(responseStream, Charsets.UTF_8)).use { reader ->
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                val responseBody = response.toString()
                Log.d(TAG, "Response Body: $responseBody")
                if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
                    return@withContext responseBody
                } else {
                    throw ServerException(responseCode, "Http Error: $responseCode", responseBody)
                }
            }
        } catch (e: ServerException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Exception in makePostRequest for $endpointUrl", e)
            throw ServerException(0, "Exception for $endpointUrl: ${e.message}", null, e)
        } finally {
            urlConnection?.disconnect()
        }
    }

    suspend fun getRideList(request: GetRideListRequest): GetRideListResponse {
        val endpoint = "getridelist.php"
        // Ensure email and session ID are directly from the request or fetched if necessary
        if (request.user_email.isBlank() || request.session_id.isBlank()) {
            return GetRideListResponse(
                rv = ServerConstants.RV_NOT_LOGGED_IN,
                msg = listOf("User email or session ID missing in request."),
                rideData = null,
                rides = null,
                totalresults = null
            )
        }
        val params = mapOf(
            "session_id" to request.session_id,
            "user_email" to request.user_email,
            "bike_id" to request.bike_id.toString(),
            "index" to request.index.toString(),
            "maxresults" to request.maxresults.toString()
        )
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) {
                // Meticulously ensure all parameters are named and correctly typed,
                // matching the GetRideListResponse primary constructor.
                return GetRideListResponse(
                    rv = ServerConstants.RV_ERROR_GENERAL,
                    // Parameters from GetRideListResponse data class:
                    // rideData: List<JsonElement>? = null
                    // rides: List<RideInfo>? = null (transient)
                    // totalresults: Int? = null (transient)
                    // msg: List<String>? = null
                    msg = listOf("Empty response from server"), // Should match msg: List<String>?
                    rideData = null, // Should match rideData: List<JsonElement>?
                    rides = null,    // Should match rides: List<RideInfo>?
                    totalresults = null  // Should match totalresults: Int?
                )
            }
            // First, decode into the raw structure
            val rawResponse = json.decodeFromString<GetRideListResponse>(responseString)
            // Then, process the RIDE field
            val processedResponse = GetRideListResponse.postProcess(rawResponse, json)

            Log.i(TAG, "GetRideList for bike ${request.bike_id}: RV=${processedResponse.rv}, MSG=${processedResponse.msg?.joinToString()}, Count=${processedResponse.rides?.size}, Total=${processedResponse.totalresults}")
            return processedResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during getRideList for bike ${request.bike_id}: ${e.errorMessage} - ${e.messageBody}", e)
            return try {
                if (e.messageBody?.isNotBlank() == true) {
                    val rawErrorResponse = json.decodeFromString<GetRideListResponse>(e.messageBody)
                    GetRideListResponse.postProcess(rawErrorResponse, json) // Also process error body if it contains RIDE
                } else {
                    GetRideListResponse(
                        rv = e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL,
                        msg = listOf(e.errorMessage),
                        rideData = null,
                        rides = null,
                        totalresults = null
                    )
                }
            } catch (parseEx: Exception) {
                Log.e(TAG, "Exception parsing error messageBody for getRideList bike ${request.bike_id}", parseEx)
                GetRideListResponse(
                    rv = e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL,
                    msg = listOf(e.errorMessage),
                    rideData = null,
                    rides = null,
                    totalresults = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getRideList for bike ${request.bike_id}", e)
            if (e is kotlinx.serialization.MissingFieldException) {
                Log.e(TAG, "MissingFieldException during getRideList: ${e.message}")
            }
            return GetRideListResponse(
                rv = ServerConstants.RV_ERROR_GENERAL,
                msg = listOf("GetRideList failed: ${e.message}"),
                rideData = null,
                rides = null,
                totalresults = null
            )
        }
    }

    suspend fun uploadFile(endpointUrl: String, urlParameters: Map<String, String>, file: File): String = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder().apply {
                // sslContext?.let { ctx -> // Removed
                //     trustManagerFactory?.let { tmf -> // Removed
                //         val trustManagers = tmf.trustManagers // Removed
                //         if (trustManagers != null && trustManagers.isNotEmpty() && trustManagers[0] is X509TrustManager) { // Removed
                //             sslSocketFactory(ctx.socketFactory, trustManagers[0] as X509TrustManager) // Removed
                //         } else { // Removed
                //             Log.w(TAG, "No X509TrustManager found for OkHttp.") // Removed
                //         } // Removed
                //     } ?: Log.w(TAG, "TrustManagerFactory is null for OkHttp.") // Removed
                // } ?: Log.w(TAG, "SSLContext is null for OkHttp.") // Removed
            }.build()

            val multipartBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("text/plain".toMediaTypeOrNull()))

            for ((key, value) in urlParameters) {
                multipartBodyBuilder.addFormDataPart(key, value)
            }
            val requestBody = multipartBodyBuilder.build()

            Log.d(TAG, "Uploading file to URL: $baseUrl$endpointUrl with params: $urlParameters")

            val request = Request.Builder()
                .url(baseUrl + endpointUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string()
                Log.d(TAG, "UploadFile Response Code: ${response.code}")
                Log.d(TAG, "UploadFile Response Body: $responseBodyString")

                if (!response.isSuccessful) {
                    throw ServerException(response.code, "Upload failed for $endpointUrl", responseBodyString)
                }
                return@withContext responseBodyString ?: throw ServerException(response.code, "Empty response body from upload for $endpointUrl", null)
            }
        } catch (e: ServerException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Exception in uploadFile for $endpointUrl", e)
            throw ServerException(0, "Exception during file upload for $endpointUrl: ${e.message}", null, e)
        }
    }

    // Helper function for time formatting
    private fun timeAsFormattedString(time: Long): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date(time))
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val endpoint = "login.php"
        val params = mapOf("email" to request.email, "password" to request.password)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return LoginResponse(ServerConstants.RV_ERROR_GENERAL, listOf("Empty response from server"), null)
            val loginResponse = json.decodeFromString<LoginResponse>(responseString)
            if (loginResponse.rv == ServerConstants.RV_OK && loginResponse.sessionId != null) {
                userSettingsRepository.updateSessionId(loginResponse.sessionId)
                Log.i(TAG, "Login successful for ${request.email}.")
            } else {
                Log.w(TAG, "Login failed for ${request.email}: RV=${loginResponse.rv}, MSG=${loginResponse.msg}")
            }
            return loginResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during login for ${request.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<LoginResponse>(e.messageBody) else LoginResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), null) }
            catch (parseEx: Exception) { LoginResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), null) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login for ${request.email}", e)
            return LoginResponse(ServerConstants.RV_ERROR_GENERAL, listOf("Login failed: ${e.message}"), null)
        }
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        val endpoint = "register.php"
        val params = mapOf("email" to request.email, "password" to request.password, "repeat_password" to request.password, "lang" to request.lang)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return RegisterResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val registerResponse = json.decodeFromString<RegisterResponse>(responseString)
            Log.i(TAG, "Register for ${request.email}: RV=${registerResponse.rv}, MSG=${registerResponse.msg}")
            return registerResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during register for ${request.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<RegisterResponse>(e.messageBody) else RegisterResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { RegisterResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during register for ${request.email}", e)
            return RegisterResponse(ServerConstants.RV_ERROR_GENERAL, "Register failed: ${e.message}")
        }
    }

    suspend fun virginLookup(deviceAddress: String, token: ByteArray): VirginLookupResponse {
        val endpoint = "virginlookup.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return VirginLookupResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "device_token" to token.toHexString(), "device_address" to deviceAddress)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return VirginLookupResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val lookupResponse = json.decodeFromString<VirginLookupResponse>(responseString)
            Log.i(TAG, "VirginLookup for $deviceAddress: RV=${lookupResponse.rv}, MSG=${lookupResponse.msg}")
            return lookupResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during virginLookup for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<VirginLookupResponse>(e.messageBody) else VirginLookupResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { VirginLookupResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during virginLookup for $deviceAddress", e)
            return VirginLookupResponse(ServerConstants.RV_ERROR_GENERAL, "VirginLookup failed: ${e.message}")
        }
    }

    suspend fun registerOwner(deviceAddress: String, token: ByteArray): RegisterOwnerResponse {
        val endpoint = "registerowner.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return RegisterOwnerResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.", null)
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "device_address" to deviceAddress, "registration_token" to token.toHexString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return RegisterOwnerResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server", null)
            val ownerResponse = json.decodeFromString<RegisterOwnerResponse>(responseString)
            Log.i(TAG, "RegisterOwner for $deviceAddress: RV=${ownerResponse.rv}, MSG=${ownerResponse.msg}")
            return ownerResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during registerOwner for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<RegisterOwnerResponse>(e.messageBody) else RegisterOwnerResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage, null) }
            catch (parseEx: Exception) { RegisterOwnerResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage, null) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during registerOwner for $deviceAddress", e)
            return RegisterOwnerResponse(ServerConstants.RV_ERROR_GENERAL, "RegisterOwner failed: ${e.message}", null)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): ChangePasswordResponse {
        val endpoint = "changepassword.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return ChangePasswordResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "password" to currentPassword, "new_password" to newPassword)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return ChangePasswordResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val changePassResponse = json.decodeFromString<ChangePasswordResponse>(responseString)
            if (changePassResponse.rv == ServerConstants.RV_OK) {
                userSettingsRepository.updatePassword(newPassword)
                Log.i(TAG, "Password changed successfully for ${userPrefs.email}.")
            } else {
                Log.w(TAG, "ChangePassword failed for ${userPrefs.email}: RV=${changePassResponse.rv}, MSG=${changePassResponse.msg}")
            }
            return changePassResponse
        } catch (e: ServerException) {
             Log.e(TAG, "ServerException during changePassword for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<ChangePasswordResponse>(e.messageBody) else ChangePasswordResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { ChangePasswordResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during changePassword for ${userPrefs.email}", e)
            return ChangePasswordResponse(ServerConstants.RV_ERROR_GENERAL, "ChangePassword failed: ${e.message}")
        }
    }

    suspend fun lostPassword(email: String, newPassword: String): LostPasswordResponse {
        val endpoint = "lostpassword.php"
        val params = mapOf("email" to email, "new_password" to newPassword)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return LostPasswordResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val lostPassResponse = json.decodeFromString<LostPasswordResponse>(responseString)
            Log.i(TAG, "LostPassword for $email: RV=${lostPassResponse.rv}, MSG=${lostPassResponse.msg}")
            return lostPassResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during lostPassword for $email: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<LostPasswordResponse>(e.messageBody) else LostPasswordResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { LostPasswordResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during lostPassword for $email", e)
            return LostPasswordResponse(ServerConstants.RV_ERROR_GENERAL, "LostPassword failed: ${e.message}")
        }
    }

    suspend fun deregisterOwner(deviceAddress: String): DeregisterOwnerResponse {
        val endpoint = "deregisterowner.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return DeregisterOwnerResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "device_address" to deviceAddress)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return DeregisterOwnerResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val deregisterResponse = json.decodeFromString<DeregisterOwnerResponse>(responseString)
            Log.i(TAG, "DeregisterOwner for $deviceAddress: RV=${deregisterResponse.rv}, MSG=${deregisterResponse.msg}")
            return deregisterResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during deregisterOwner for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<DeregisterOwnerResponse>(e.messageBody) else DeregisterOwnerResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { DeregisterOwnerResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during deregisterOwner for $deviceAddress", e)
            return DeregisterOwnerResponse(ServerConstants.RV_ERROR_GENERAL, "DeregisterOwner failed: ${e.message}")
        }
    }

    suspend fun getUsersDevices(): UsersDevicesResponse {
        val endpoint = "usersdevices.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return UsersDevicesResponse(ServerConstants.RV_NOT_LOGGED_IN, listOf("User not logged in or email/session missing."), emptyList())
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return UsersDevicesResponse(ServerConstants.RV_ERROR_GENERAL, listOf("Empty response from server"), emptyList())
            val devicesResponse = json.decodeFromString<UsersDevicesResponse>(responseString)
            Log.i(TAG, "GetUsersDevices for ${userPrefs.email}: RV=${devicesResponse.rv}, Count=${devicesResponse.devices.size}")
            if (devicesResponse.rv == ServerConstants.RV_OK) {
                userSettingsRepository.updateDevices(devicesResponse.devices)
            }
            return devicesResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during getUsersDevices for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<UsersDevicesResponse>(e.messageBody) else UsersDevicesResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList()) }
            catch (parseEx: Exception) { UsersDevicesResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList()) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getUsersDevices for ${userPrefs.email}", e)
            return UsersDevicesResponse(ServerConstants.RV_ERROR_GENERAL, listOf("GetUsersDevices failed: ${e.message}"), emptyList())
        }
    }

    suspend fun getUsersBikes(): UsersBikesResponse {
        val endpoint = "usersbikes.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return UsersBikesResponse(ServerConstants.RV_NOT_LOGGED_IN, listOf("User not logged in or email/session missing."), emptyList())
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return UsersBikesResponse(ServerConstants.RV_ERROR_GENERAL, listOf("Empty response from server"), emptyList())

            val initialBikesResponse = json.decodeFromString<UsersBikesResponse>(responseString)
            Log.i(TAG, "GetUsersBikes for ${userPrefs.email}: RV=${initialBikesResponse.rv}, Count=${initialBikesResponse.bikes.size}")

            if (initialBikesResponse.rv == ServerConstants.RV_OK) {
                val bikesWithDetailsList = initialBikesResponse.bikes.map { bike ->
                    var currentBike = bike // Start with the initial bike object
                    try {
                        // Fetch logs
                        Log.d(TAG, "Fetching logs for bike ID: ${bike.id}")
                        val logsResponse = getLogs(bike.id) // Assuming getLogs is a suspend function or called from one
                        if (logsResponse.rv == ServerConstants.RV_OK) {
                            Log.d(TAG, "Successfully fetched ${logsResponse.logs.size} logs for bike ID: ${bike.id}")
                            currentBike = currentBike.copy(logs = logsResponse.logs)
                        } else {
                            Log.w(TAG, "Failed to fetch logs for bike ID: ${bike.id}, RV: ${logsResponse.rv}, MSG: ${logsResponse.msg.joinToString()}")
                        }

                        // Fetch ride list
                        Log.d(TAG, "Fetching ride list for bike ID: ${bike.id}")
                        val rideListRequest = GetRideListRequest(
                            session_id = userPrefs.sessionId,
                            user_email = userPrefs.email,
                            bike_id = bike.id,
                            index = 0,
                            maxresults = 20
                        )
                        val rideListResponse = getRideList(rideListRequest) // Assuming getRideList is a suspend function
                        if (rideListResponse.rv == ServerConstants.RV_OK && rideListResponse.rides != null) {
                            Log.i(TAG, "Successfully fetched ${rideListResponse.rides.size} rides for bike ID: ${bike.id}. Total rides: ${rideListResponse.totalresults ?: 0}")
                            // Store the fetched rides in UserSettingsRepository
                            userSettingsRepository.updateRidesForBike(bike.id, rideListResponse.rides)
                            Log.d(TAG, "Stored ${rideListResponse.rides.size} rides for bike ${bike.id} in UserSettingsRepository.")
                        } else {
                            Log.w(TAG, "Failed to fetch ride list for bike ID: ${bike.id}, RV: ${rideListResponse.rv}, MSG: ${rideListResponse.msg?.joinToString()}, Rides: ${rideListResponse.rides}")
                            // If fetching rides fails or returns no rides, we might want to clear existing rides for that bike or handle it appropriately.
                            // For now, we'll ensure that if rides is null (e.g. due to an error or empty list from server), we pass an empty list.
                            userSettingsRepository.updateRidesForBike(bike.id, emptyList())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception fetching details (logs or rides) for bike ID: ${bike.id}", e)
                        // Also ensure rides are cleared or set to empty on exception for this bike
                        userSettingsRepository.updateRidesForBike(bike.id, emptyList())
                        // currentBike remains as it was before this try block or with partial data if one part succeeded
                    }
                    currentBike // Return the (potentially updated) bike
                }
                userSettingsRepository.updateBikes(bikesWithDetailsList)
                return initialBikesResponse.copy(bikes = bikesWithDetailsList)
            }
            // If initialBikesResponse.rv was not OK
            return initialBikesResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during getUsersBikes for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try {
                if (e.messageBody?.isNotBlank() == true) json.decodeFromString<UsersBikesResponse>(e.messageBody)
                else UsersBikesResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList())
            } catch (parseEx: Exception) {
                UsersBikesResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getUsersBikes for ${userPrefs.email}", e)
            return UsersBikesResponse(ServerConstants.RV_ERROR_GENERAL, listOf("GetUsersBikes failed: ${e.message}"), emptyList())
        }
    }

    suspend fun createBike(bikeName: String, iconId: Int): CreateBikeResponse {
        val endpoint = "createbike.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return CreateBikeResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "bike_name" to bikeName, "icon_id" to iconId.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return CreateBikeResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val createResponse = json.decodeFromString<CreateBikeResponse>(responseString)
            Log.i(TAG, "CreateBike for $bikeName: RV=${createResponse.rv}, ID=${createResponse.bikeId}")
            return createResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during createBike for $bikeName: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<CreateBikeResponse>(e.messageBody) else CreateBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { CreateBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during createBike for $bikeName", e)
            return CreateBikeResponse(ServerConstants.RV_ERROR_GENERAL, "CreateBike failed: ${e.message}")
        }
    }

    suspend fun deleteBike(bikeName: String, bikeId: Int): DeleteBikeResponse {
        val endpoint = "deletebike.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return DeleteBikeResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "bike_name" to bikeName, "bike_id" to bikeId.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return DeleteBikeResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val deleteResponse = json.decodeFromString<DeleteBikeResponse>(responseString)
            Log.i(TAG, "DeleteBike for $bikeId: RV=${deleteResponse.rv}")
            return deleteResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during deleteBike for $bikeId: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<DeleteBikeResponse>(e.messageBody) else DeleteBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { DeleteBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during deleteBike for $bikeId", e)
            return DeleteBikeResponse(ServerConstants.RV_ERROR_GENERAL, "DeleteBike failed: ${e.message}")
        }
    }

    suspend fun deviceAddBike(deviceAddress: String, bikeId: Int): DeviceAddBikeResponse {
        val endpoint = "deviceaddbike.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return DeviceAddBikeResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "device_address" to deviceAddress, "bike_id" to bikeId.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return DeviceAddBikeResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val addResponse = json.decodeFromString<DeviceAddBikeResponse>(responseString)
            Log.i(TAG, "DeviceAddBike for $deviceAddress to $bikeId: RV=${addResponse.rv}")
            return addResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during deviceAddBike for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<DeviceAddBikeResponse>(e.messageBody) else DeviceAddBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { DeviceAddBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during deviceAddBike for $deviceAddress", e)
            return DeviceAddBikeResponse(ServerConstants.RV_ERROR_GENERAL, "DeviceAddBike failed: ${e.message}")
        }
    }

    suspend fun deviceRemoveBike(deviceAddress: String): DeviceRemoveBikeResponse {
        val endpoint = "deviceremovebike.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return DeviceRemoveBikeResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "device_address" to deviceAddress)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return DeviceRemoveBikeResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val removeResponse = json.decodeFromString<DeviceRemoveBikeResponse>(responseString)
            Log.i(TAG, "DeviceRemoveBike for $deviceAddress: RV=${removeResponse.rv}")
            return removeResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during deviceRemoveBike for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<DeviceRemoveBikeResponse>(e.messageBody) else DeviceRemoveBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { DeviceRemoveBikeResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during deviceRemoveBike for $deviceAddress", e)
            return DeviceRemoveBikeResponse(ServerConstants.RV_ERROR_GENERAL, "DeviceRemoveBike failed: ${e.message}")
        }
    }

    suspend fun insertLog( longitude: Double, latitude: Double, accuracy: Float, rssi: Int, logTime: String, deviceAddress: String, deviceId: String?, parked: Boolean?, battVoltage: Int?, battCritical: Boolean?, temperature: Int, alert: Boolean?, swVersion: String?, publicDataByteArray: ByteArray?, publicDataCmd: Byte?, publicDataSubCmd: Byte? ): InsertLogResponse {
        val endpoint = "insert_log2.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return InsertLogResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mutableMapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "longitude" to longitude.toString(), "latitude" to latitude.toString(), "rssi" to rssi.toString(), "accuracy" to Math.round(accuracy).toString(), "log_time" to logTime, "address" to deviceAddress, "temperature" to temperature.toString())
        deviceId?.let { params["device_id"] = it }
        parked?.let { params["parked"] = if (it) "1" else "0" }
        battVoltage?.let { params["batt_voltage"] = it.toString() }
        battCritical?.let { params["batt_critical"] = if (it) "1" else "0" }
        alert?.let { params["alert"] = if (it) "1" else "0" }
        swVersion?.let { if (it.isNotBlank()) params["sw_version"] = it.replace("_", ".") }
        if (publicDataByteArray != null && publicDataByteArray.isNotEmpty()) {
            params["publicdata"] = Base64.encodeToString(publicDataByteArray, Base64.NO_WRAP)
            publicDataCmd?.let { params["publicdata_cmd"] = it.toString() }
            publicDataSubCmd?.let { params["publicdata_subcmd"] = it.toString() }
        }
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return InsertLogResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val logResponse = json.decodeFromString<InsertLogResponse>(responseString)
            Log.i(TAG, "InsertLog for $deviceAddress: RV=${logResponse.rv}")
            return logResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during insertLog for $deviceAddress: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<InsertLogResponse>(e.messageBody) else InsertLogResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { InsertLogResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during insertLog for $deviceAddress", e)
            return InsertLogResponse(ServerConstants.RV_ERROR_GENERAL, "InsertLog failed: ${e.message}")
        }
    }

    suspend fun getLogs(bikeId: Int): GetLogsResponse {
        val endpoint = "getlogs.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return GetLogsResponse(ServerConstants.RV_NOT_LOGGED_IN, listOf("User not logged in or email/session missing."), emptyList())
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "bike_id" to bikeId.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return GetLogsResponse(ServerConstants.RV_ERROR_GENERAL, listOf("Empty response from server"), emptyList())
            val logsResponse = json.decodeFromString<GetLogsResponse>(responseString)
            Log.i(TAG, "GetLogs for $bikeId: RV=${logsResponse.rv}, Count=${logsResponse.logs.size}, MSG=${logsResponse.msg.joinToString()}")
            return logsResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during getLogs for $bikeId: ${e.errorMessage} - ${e.messageBody}", e)
            return try {
                if (e.messageBody?.isNotBlank() == true) json.decodeFromString<GetLogsResponse>(e.messageBody)
                else GetLogsResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList())
            }
            catch (parseEx: Exception) { GetLogsResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, listOf(e.errorMessage), emptyList()) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getLogs for $bikeId", e)
            return GetLogsResponse(ServerConstants.RV_ERROR_GENERAL, listOf("GetLogs failed: ${e.message}"), emptyList())
        }
    }

    suspend fun resendActivation(email: String): ResendActivationResponse {
        val endpoint = "resendactivation.php"
        val params = mapOf("email" to email)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return ResendActivationResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val activationResponse = json.decodeFromString<ResendActivationResponse>(responseString)
            Log.i(TAG, "ResendActivation for $email: RV=${activationResponse.rv}")
            return activationResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during resendActivation for $email: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<ResendActivationResponse>(e.messageBody) else ResendActivationResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { ResendActivationResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during resendActivation for $email", e)
            return ResendActivationResponse(ServerConstants.RV_ERROR_GENERAL, "ResendActivation failed: ${e.message}")
        }
    }

    suspend fun insertFeedback(starRating: Int, feedback: String?, deviceMake: String, deviceModel: String, osVersion: String, appVersion: String): InsertFeedbackResponse {
        val endpoint = "insert_feedback.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return InsertFeedbackResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mutableMapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "star_rating" to starRating.toString(), "device_make" to deviceMake, "device_model" to deviceModel, "os_version" to osVersion, "app_version" to appVersion)
        feedback?.let { if (it.isNotBlank()) params["feedback"] = it }
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return InsertFeedbackResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val feedbackResponse = json.decodeFromString<InsertFeedbackResponse>(responseString)
            Log.i(TAG, "InsertFeedback for ${userPrefs.email}: RV=${feedbackResponse.rv}")
            return feedbackResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during insertFeedback for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<InsertFeedbackResponse>(e.messageBody) else InsertFeedbackResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { InsertFeedbackResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during insertFeedback for ${userPrefs.email}", e)
            return InsertFeedbackResponse(ServerConstants.RV_ERROR_GENERAL, "InsertFeedback failed: ${e.message}")
        }
    }

    suspend fun insertRide(filePath: String, bikeId: Int, startTime: Long, endTime: Long): InsertRideResponse {
        val endpoint = "insert_ride.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return InsertRideResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val rideFile = File(filePath)
        if (!rideFile.exists()) return InsertRideResponse(ServerConstants.RV_ERROR_GENERAL, "Ride data file not found.")
        val urlParameters = mapOf("session_id" to userPrefs.sessionId, "user_email" to userPrefs.email, "bike_id" to bikeId.toString(), "start_time" to timeAsFormattedString(startTime), "end_time" to timeAsFormattedString(endTime))
        try {
            val responseString = uploadFile(endpoint, urlParameters, rideFile)
            if (responseString.isBlank()) return InsertRideResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val rideResponse = json.decodeFromString<InsertRideResponse>(responseString)
            Log.i(TAG, "InsertRide for $filePath, bike $bikeId: RV=${rideResponse.rv}")
            return rideResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during insertRide for $filePath: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<InsertRideResponse>(e.messageBody) else InsertRideResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { InsertRideResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during insertRide for $filePath", e)
            return InsertRideResponse(ServerConstants.RV_ERROR_GENERAL, "InsertRide failed: ${e.message}")
        }
    }

    suspend fun addRide(filePath: String, bikeId: Int, version: String, rideId: String, startTime: Long, endTime: Long, numberOfStops: Int, stopDuration: Int, speed: Int, startPositionTime: Long, startPositionLatitude: Double, startPositionLongitude: Double, startPositionAltitude: Int, startPositionAccuracy: Int, endPositionTime: Long, endPositionLatitude: Double, endPositionLongitude: Double, endPositionAltitude: Int, endPositionAccuracy: Int): AddRideResponse {
        val endpoint = "addride.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return AddRideResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val rideFile = File(filePath)
        if (!rideFile.exists()) return AddRideResponse(ServerConstants.RV_ERROR_GENERAL, "Ride data file not found.")
        val urlParameters = mapOf("session_id" to userPrefs.sessionId, "user_email" to userPrefs.email, "bike_id" to bikeId.toString(), "ride_id" to rideId, "version" to version, "start_time" to timeAsFormattedString(startTime), "end_time" to timeAsFormattedString(endTime), "number_of_stops" to numberOfStops.toString(), "stop_duration" to stopDuration.toString(), "speed" to speed.toString(), "start_pos_time" to startPositionTime.toString(), "start_pos_latitude" to startPositionLatitude.toString(), "start_pos_longitude" to startPositionLongitude.toString(), "start_pos_altitude" to startPositionAltitude.toString(), "start_pos_accuracy" to startPositionAccuracy.toString(), "end_pos_time" to endPositionTime.toString(), "end_pos_latitude" to endPositionLatitude.toString(), "end_pos_longitude" to endPositionLongitude.toString(), "end_pos_altitude" to endPositionAltitude.toString(), "end_pos_accuracy" to endPositionAccuracy.toString())
        try {
            val responseString = uploadFile(endpoint, urlParameters, rideFile)
            if (responseString.isBlank()) return AddRideResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val addRideResponse = json.decodeFromString<AddRideResponse>(responseString)
            Log.i(TAG, "AddRide for $filePath, ride $rideId: RV=${addRideResponse.rv}")
            return addRideResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during addRide for $filePath: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<AddRideResponse>(e.messageBody) else AddRideResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { AddRideResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during addRide for $filePath", e)
            return AddRideResponse(ServerConstants.RV_ERROR_GENERAL, "AddRide failed: ${e.message}")
        }
    }

    suspend fun updateAlertState(bikeId: Int, alertState: Int): AlertStateResponse {
        val endpoint = "alertstate.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return AlertStateResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "bike_id" to bikeId.toString(), "alert_state" to alertState.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return AlertStateResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val alertResponse = json.decodeFromString<AlertStateResponse>(responseString)
            Log.i(TAG, "UpdateAlertState for $bikeId to $alertState: RV=${alertResponse.rv}")
            return alertResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during updateAlertState for $bikeId: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<AlertStateResponse>(e.messageBody) else AlertStateResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { AlertStateResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during updateAlertState for $bikeId", e)
            return AlertStateResponse(ServerConstants.RV_ERROR_GENERAL, "UpdateAlertState failed: ${e.message}")
        }
    }

    suspend fun updateFirebaseToken(fcmToken: String): FirebaseTokenResponse {
        val endpoint = "insert_firebase_token.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return FirebaseTokenResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val currentLang = Locale.getDefault().language
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "lang" to currentLang, "token" to fcmToken)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return FirebaseTokenResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val tokenResponse = json.decodeFromString<FirebaseTokenResponse>(responseString)
            Log.i(TAG, "UpdateFirebaseToken for ${userPrefs.email}: RV=${tokenResponse.rv}")
            return tokenResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during updateFirebaseToken for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<FirebaseTokenResponse>(e.messageBody) else FirebaseTokenResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { FirebaseTokenResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during updateFirebaseToken for ${userPrefs.email}", e)
            return FirebaseTokenResponse(ServerConstants.RV_ERROR_GENERAL, "UpdateFirebaseToken failed: ${e.message}")
        }
    }

    suspend fun insertInfo(info: Map<String, String>): InsertInfoResponse {
        val endpoint = "insert_info.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return InsertInfoResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mutableMapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email)
        params.putAll(info)
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return InsertInfoResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val infoResponse = json.decodeFromString<InsertInfoResponse>(responseString)
            Log.i(TAG, "InsertInfo for ${userPrefs.email}: RV=${infoResponse.rv}")
            return infoResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during insertInfo for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<InsertInfoResponse>(e.messageBody) else InsertInfoResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { InsertInfoResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during insertInfo for ${userPrefs.email}", e)
            return InsertInfoResponse(ServerConstants.RV_ERROR_GENERAL, "InsertInfo failed: ${e.message}")
        }
    }

    suspend fun updateBikeD2DState(bikeId: Int, d2dState: Int): BikeEditD2DResponse {
        val endpoint = "bikeedit.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return BikeEditD2DResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "bike_id" to bikeId.toString(), "d2d_disable" to d2dState.toString())
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return BikeEditD2DResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val d2dResponse = json.decodeFromString<BikeEditD2DResponse>(responseString)
            Log.i(TAG, "UpdateBikeD2DState for $bikeId to $d2dState: RV=${d2dResponse.rv}")
            return d2dResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during updateBikeD2DState for $bikeId: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<BikeEditD2DResponse>(e.messageBody) else BikeEditD2DResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { BikeEditD2DResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during updateBikeD2DState for $bikeId", e)
            return BikeEditD2DResponse(ServerConstants.RV_ERROR_GENERAL, "UpdateBikeD2DState failed: ${e.message}")
        }
    }

    suspend fun requestPublicData(len: Int, cmd: Byte, prop: Byte, subCmd: Byte, data: ByteArray): PublicDataResponse {
        val endpoint = "publicdata.php"
        val userPrefs = userSettingsRepository.userPreferencesFlow.first()
        if (userPrefs.email.isBlank() || userPrefs.sessionId.isBlank()) return PublicDataResponse(ServerConstants.RV_NOT_LOGGED_IN, "User not logged in or email/session missing.")
        val params = mapOf("session_id" to userPrefs.sessionId, "email" to userPrefs.email, "len" to len.toString(), "cmd" to cmd.toString(), "prop" to prop.toString(), "subcmd" to subCmd.toString(), "data" to Base64.encodeToString(data, Base64.NO_WRAP))
        try {
            val responseString = makePostRequest(endpoint, params)
            if (responseString.isBlank()) return PublicDataResponse(ServerConstants.RV_ERROR_GENERAL, "Empty response from server")
            val pubDataResponse = json.decodeFromString<PublicDataResponse>(responseString)
            Log.i(TAG, "RequestPublicData for ${userPrefs.email}: RV=${pubDataResponse.rv}")
            return pubDataResponse
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during requestPublicData for ${userPrefs.email}: ${e.errorMessage} - ${e.messageBody}", e)
            return try { if (e.messageBody?.isNotBlank() == true) json.decodeFromString<PublicDataResponse>(e.messageBody) else PublicDataResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
            catch (parseEx: Exception) { PublicDataResponse(e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL, e.errorMessage) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during requestPublicData for ${userPrefs.email}", e)
            return PublicDataResponse(ServerConstants.RV_ERROR_GENERAL, "RequestPublicData failed: ${e.message}")
        }
    }

    private fun getFilenameFromCd(cd: String?): String? {
        if (cd == null) {
            return null
        }
        val pattern = Pattern.compile("filename=(.+)")
        val matcher = pattern.matcher(cd)
        return if (matcher.find()) matcher.group(1) else null
    }

    suspend fun makePostRequestWithFileDownload(endpointUrl: String, params: Map<String, String>): Pair<GetRideFileResponse, ByteArray?> = withContext(Dispatchers.IO) {
        val fullUrl = URL(baseUrl + endpointUrl)
        var urlConnection: HttpsURLConnection? = null
        try {
            urlConnection = fullUrl.openConnection() as HttpsURLConnection
            // sslContext?.let { urlConnection.sslSocketFactory = it.socketFactory } // Removed

            urlConnection.requestMethod = "POST"
            // Content-Type for form data, not JSON, as per original Python script
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            // Accept header might need to be adjusted if the server expects something specific for file downloads
            // urlConnection.setRequestProperty("Accept", "application/octet-stream") // Example

            urlConnection.readTimeout = 30000 // Increased timeout for file downloads
            urlConnection.connectTimeout = 30000
            urlConnection.doOutput = true

            // Encode parameters for application/x-www-form-urlencoded
            val postData = params.map { (k, v) -> "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}" }.joinToString("&")

            Log.d(TAG, "Sending 'POST' request to URL : $fullUrl for file download")
            Log.d(TAG, "Post parameters : $postData")

            DataOutputStream(urlConnection.outputStream).use { wr ->
                wr.writeBytes(postData) // Use writeBytes for x-www-form-urlencoded
                wr.flush()
            }

            val responseCode = urlConnection.responseCode
            Log.d(TAG, "Response Code : $responseCode")

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val contentDisposition = urlConnection.getHeaderField("Content-Disposition")
                val filename = getFilenameFromCd(contentDisposition)
                Log.d(TAG, "Content-Disposition: $contentDisposition, Filename: $filename")

                if (filename == null) {
                    return@withContext Pair(GetRideFileResponse(ServerConstants.RV_ERROR_GENERAL, "Filename not found in headers", null, null), null)
                }

                urlConnection.inputStream.use { inputStream ->
                    val fileBytes = inputStream.readBytes()
                    // Successfully downloaded the file
                    return@withContext Pair(GetRideFileResponse(ServerConstants.RV_OK, "File downloaded successfully.", filename, null), fileBytes)
                }
            } else {
                // Handle error responses
                val errorBody = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Error downloading file: $responseCode - $errorBody")
                throw ServerException(responseCode, "Http Error: $responseCode", errorBody)
            }
        } catch (e: ServerException) {
            throw e // Re-throw ServerException
        } catch (e: Exception) {
            Log.e(TAG, "Exception in makePostRequestWithFileDownload for $endpointUrl", e)
            throw ServerException(0, "Exception for $endpointUrl: ${e.message}", null, e)
        } finally {
            urlConnection?.disconnect()
        }
    }


    suspend fun getRideFile(request: GetRideFileRequest): GetRideFileResponse {
        val endpoint = "getridefile.php"
        if (request.user_email.isBlank() || request.session_id.isBlank() || request.ride_id.isBlank()) {
            return GetRideFileResponse(
                rv = ServerConstants.RV_NOT_LOGGED_IN,
                msg = "User email, session ID, or ride ID missing.",
                filename = null,
                filePath = null
            )
        }
        val params = mapOf(
            "session_id" to request.session_id,
            "user_email" to request.user_email,
            "ride_id" to request.ride_id
        )

        try {
            val (response, fileBytes) = makePostRequestWithFileDownload(endpoint, params)

            if (response.rv == ServerConstants.RV_OK && fileBytes != null && response.filename != null) {
                // Save the file to internal storage (e.g., app-specific directory)
                // Ensure the context is available and valid for file operations
                val directory = context.getExternalFilesDir(null) // Or context.filesDir for internal
                if (directory == null) {
                    Log.e(TAG, "Error getting application directory for saving file.")
                    return GetRideFileResponse(ServerConstants.RV_ERROR_GENERAL, "Could not access storage directory.", response.filename, null)
                }
                val file = File(directory, response.filename)

                try {
                    file.outputStream().use { it.write(fileBytes) }
                    Log.i(TAG, "Ride file ${response.filename} saved to ${file.absolutePath}")
                    return response.copy(filePath = file.absolutePath)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving ride file ${response.filename}", e)
                    return GetRideFileResponse(ServerConstants.RV_ERROR_GENERAL, "Error saving file: ${e.message}", response.filename, null)
                }
            } else {
                // Error occurred during download or filename extraction
                Log.w(TAG, "GetRideFile failed: RV=${response.rv}, MSG=${response.msg}, Filename=${response.filename}")
                return response // Return the error response from makePostRequestWithFileDownload
            }
        } catch (e: ServerException) {
            Log.e(TAG, "ServerException during getRideFile for ride ${request.ride_id}: ${e.errorMessage} - ${e.messageBody}", e)
            return GetRideFileResponse(
                rv = e.httpStatusCode ?: ServerConstants.RV_ERROR_GENERAL,
                msg = e.errorMessage,
                filename = null,
                filePath = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getRideFile for ride ${request.ride_id}", e)
            return GetRideFileResponse(
                rv = ServerConstants.RV_ERROR_GENERAL,
                msg = "GetRideFile failed: ${e.message}",
                filename = null,
                filePath = null
            )
        }
    }
}
