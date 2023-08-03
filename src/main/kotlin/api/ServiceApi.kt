package api


import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import models.*
import network.RetrofitClient
import retrofit2.HttpException

private val logger = KotlinLogging.logger {}

interface ServiceApi {

    companion object {

        fun apiGetToken(serverUrl: String, username: String, password: String, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            var token: String? = null
            val response = apiService.authenticateUser("password", username, password).execute()
            if (response.isSuccessful) {
                token = response.body()?.token!!
            } else {
                throw HttpException(response)
            }
            return token

        }

        fun apiSendEvent(serverUrl: String, source: String, type: String, params: JsonObject, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)
            var id = ""
            val data = JsonObject()
            data.addProperty("source", source)
            data.addProperty("type", type)
            data.add("params", params)
            val response = apiService.apiSendEvent(data).execute()
            if (response.isSuccessful) {
                id = response.body().toString()
            } else {
                throw HttpException(response)
            }
            return id
        }

        fun apiCreateRunJob(serverUrl: String, company: String, label: String, targets: List<String>,scan_kind: String,level: String, /*scope: String,*/params: JsonObject, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)
            var id = ""

            val data = JobRun(
                company = company,
                label = label,
                outpost_id = getValue(params,"outpost_id"),
                ranges = targets,
                restricted_org_id = getValue(params,"restricted_org_id"),
                scan_kind = scan_kind,
                scan_level = level,
                scan_options = ScanOptions(
                    CLOUD_HOST_DETECTION = true,
                    MAX_ACTIVE_IAP_SESSIONS = 50,
                    MAX_ACTIVE_SSM_SESSIONS = 50,
                    MAX_START_SSM_SESSIONS = 3,
                    NO_PING = false,
                    SESSION_LOGGING = false,
                    SKIP_IMPLICIT_SCANS = false,
                    VMWARE_GUEST_IMPLICIT_SCANS = true
                ),
                scan_params = ScanParams(
                    cluster_url = getValue(params,"cluster_url"),
                    credential = getValue(params,"credential"),
                    provider = getValue(params,"provider"),
                    region = listOf()
                ),
                scope = getValue(params,"scope")
                )

            val response = apiService.apiCreateRunJob(data).execute()
            if (response.isSuccessful) {
                id = response.body()?.uuid.toString()
            } else {
                throw HttpException(response)
            }
            return id
        }

        fun apiGetJobResults(serverUrl: String, run_id: String ,unsafe: Boolean): JobResultsItem? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)
            var id = ""
            var results: JobResultsItem? = null
            val response = apiService.apiGetJobResults(run_id).execute()
            if (response.isSuccessful) {
                if (!response.body().isNullOrEmpty()) {
                    results = response.body()!![0]
                    id = response.body().toString()
                } else {
                    logger.debug("Pas de resultats au Job lancé !")
                }
            } else {
                throw HttpException(response)
            }
            return results
        }

        suspend fun apiWaitJobResults(serverUrl: String, run_id: String, unsafe: Boolean): JobFinalResults? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
            var id = ""
            var finished = false
            var results: JobResultsItem? = null
            logger.info("En attente des résultats du scan...")
            while (!finished) {
                val response = apiDiscovery.apiGetJobResults(run_id).execute()
                if (response.isSuccessful) {
                    if (!response.body().isNullOrEmpty()) {
                        results = response.body()!![0]
//                        logger.debug("$results")
                        finished = results.finished
                        delay(3000)
                        logger.info(".")
                    } else {
                        logger.debug("Pas de resultats succes au Job lancé !")
                        finished = true
                    }
                } else {
                    throw HttpException(response)
                }
            }
            val inferred = apiDiscovery.apiGetInferred(results?.inferred!!).execute()
            logger.debug("$inferred")
            val finalResults = apiDiscovery.apiGetFinalResults(results.results).execute()
            logger.debug("$finalResults")
            return finalResults.body()
        }


        fun getValue(obj: JsonObject, key: String ): String? {
            var result = ""
            val resultValue = obj.get(key)
            logger.debug("$obj , $key , $resultValue")
            if (resultValue != null)
            {
                result = resultValue.toString().replace("\"", "")
            }
            return result
        }


    }
}




