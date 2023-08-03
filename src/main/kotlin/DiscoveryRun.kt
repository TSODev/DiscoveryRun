
import api.ServiceApi
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.mordant.rendering.TextColors
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.TokenHolder
import retrofit2.HttpException
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class DiscoveryRun: CliktCommand() {
    enum class scanLevel(val level: String) { FULL("Full Discovery"), SWEEP("Sweep Scan") }
    enum class scanKind(val kind: String) { IP("IP"), CLOUD("Cloud") }

    val validURL = "^(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)(/)\$"

    val server: String by option(
        "-s", "--server",
        help = "URL API du serveur Discovery , (https et termine avec '/') \n " +
            "généralement https://server/api/v1.1/")
        .required()
//        .validate {
//        if (!it.matches(Regex(validURL)))
//            throw InvalidArgumentException("URL du serveur invalide : $it \n" +
//                    "lancer le programme avec l'option -h pour de l'aide")
//    }

    val username: String by option(
        "-u", "--username",
        help = "Login - Nom de l'utilisateur"
    ).prompt()

    val password: String by option(
        "-p", "--password",
        help = "Login - Mot de Passe de l'utilisateur"
    ).prompt(hideInput = true)

    val unsafe by option( "-x", "--unsecure",
        help = "do not verify SSL certificate checking process (useful with self signed certificate)"
    ).flag(default = true)

    val company by option(
        "-c", "--company",
        help = "nom de la société cible"
    ).default("")

    val label by option(
        "-l", "--label",
        help = "Label du job"
    ).default("DiscoveryRun App")

    val targets by option(
        "-t", "--targets",
        help = "Liste des cibles (IPv4 ou IPv6) à scanner, séparées par le caractère ','"
    ).split(",").required()

    val scan_level by option(
        "-v", "--level",
        help = "Niveau du scan").choice(
        "full" to scanLevel.FULL,
        "sweep" to scanLevel.SWEEP,
        ).default(scanLevel.FULL)

    val scan_kind by option(
        "-k", "--kind",
        help = "type de cibles pour le scan").choice(
        "IP" to scanKind.IP,
        "Cloud" to scanKind.CLOUD,
    ).default(scanKind.IP)

    val params by option(
        "-a", "--params",
        help = "Parametres additionels (string format JSON)"
    ).default("{\"detail1\":\"exemple\"}")

    override fun run() {

        echo("===============================================================================")
        echo(" Discovery Run Job Launcher - TSO pour Orange Business - 08/23 - version 1.0.0 ")
        echo("===============================================================================")


        val gson = Gson()
        var jsonParams: JsonObject
        try {
            jsonParams = gson.fromJson(params, JsonObject::class.java)
        } catch (e: JsonSyntaxException) {
            logger.error("Erreur JSON pour PARAMS : ${e.message}")
            exitProcess(-4)
        }

        if ((jsonParams.get("scope") != null) && (jsonParams.get("outpost_id") != null))
        {
            logger.info("Erreur d'argument : Vous ne devez pas spécifier 'scope' et 'outpost_id' dans la même commande ! ")
            exitProcess(-4)
        }

        if ((scan_kind == scanKind.CLOUD) && (jsonParams.get("provider") == null))
        {
            logger.info("Erreur d'argument : merci de spécifier 'provider' pour un scan Cloud ")
            exitProcess(-4)
        }

        if (password.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : PASSWORD ne peut pas être vide")
            exitProcess(-4)
        }

        if (username.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : USERNAME ne peut pas être vide")
            exitProcess(-4)
        }

        if (targets.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : si les cibles (--target=) ne sont pas spécifiées cela ne sert à rien de faire un scan!")
            exitProcess(-4)
        }

        apiCallByCoroutines(
            username,
            password,
            server,
            company,
            label,
            targets!!,
            scan_kind!!.kind,
            scan_level!!.level,
            jsonParams,
            unsafe
        )

    }

}
private fun apiCallByCoroutines(
    username: String,
    password: String,
    server: String,
    company: String,
    label: String,
    targets: List<String>,
    scan_kind: String,
    level: String,
    //       scope: String,
    params: JsonObject,
    unsafe: Boolean
) = runBlocking {
    launch { // launch new coroutine in the scope of runBlocking

        try {
            ServiceApi.apiGetToken(server, username, password, unsafe).let { token: String? ->
                if (token != null) {
                    TokenHolder.saveToken(token)
                    val runId =
                        ServiceApi.apiCreateRunJob(server, company, label, targets, scan_kind, level, params, unsafe)
                    logger.info("Run ID: $runId")
                    val info = ServiceApi.apiWaitJobResults(server, runId!!, unsafe)
                    println(
                        TextColors.yellow(
                            "Résultats :  \n" +
                                    " >" +
                                    " | Scanned : ${targets.size}" +
                                    " | Success : ${info?.Success?.count}" +
                                    " | Skipped : ${info?.Skipped?.count}" +
                                    " | No Response : ${info?.NoResponse?.count}" +
                                    " | No Access : ${info?.NoAccess?.count}" +
                                    " | Error : ${info?.Error?.count}" +
                                    " | Dropped : ${info?.Dropped?.count} | <"
                        )
                    )
                }
            }
        }
        catch (exception: HttpException) {
            logger.error("Erreur : ${exception.message}" )
            exitProcess(-1)
        }
    }
}

fun main(args: Array<String>) = DiscoveryRun().versionOption("1.0.1").main(args)