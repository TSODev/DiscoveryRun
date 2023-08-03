
import api.ServiceApi.Companion.apiCreateRunJob
import api.ServiceApi.Companion.apiGetToken
import api.ServiceApi.Companion.apiWaitJobResults
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.xenomachina.argparser.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.TokenHolder
import retrofit2.HttpException
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}


class ParsedArgs(parser: ArgParser) {

    val validURL = "^(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)(/)\$"

    val server by parser.storing(
        "-s", "--server",
        help = "URL API du serveur Discovery , (https et termine avec '/') \n " +
                "généralement https://server/api/v1.1/"
    )
        .addValidator {
        if (!value.matches(Regex(validURL)))
            throw InvalidArgumentException("URL du serveur invalide : $value \n" +
                "lancer le programme avec l'option -h pour de l'aide")
    }

    val unsafe by parser.flagging(
        "-x", "--unsecure",
        help = "do not verify SSL certificate checking process (useful with self signed certificate)"
    ).default(false)

    val username by parser.storing(
        "-u", "--username",
        help = "Login - Nom de l'utilisateur"
    )

    val password by parser.storing(
        "-p", "--password",
        help = "Login - Mot de passe"
    )

    val company by parser.storing(
        "-c", "--company",
        help = "nom de la société cible"
    )

    val label by parser.storing(
        "-l", "--label",
        help = "Label du job"
    )

    val targets by parser.storing(
        "-t", "--targets",
        help = "Liste des cibles (IPv4 ou IPv6) séparées par le caractère ','"
    )

    val level by parser.storing(
        "-v", "--level",
        help = "Niveau  de scan"
    )

//    val scope by parser.storing(
//        "-o", "--scope",
//        help = "scope du job run"
//    )

    val scan_kind by parser.storing(
        "-k", "--scan_kind",
        help = "type de cibles pour le scan"
    )

    val params by parser.storing(
        "-a", "--params",
        help = "Parametres additionels (string format JSON)"
    ).default("{\"detail1\":\"exemple\"}")

}
    fun main(args: Array<String>): Unit = mainBody {

        val prologue = "Discovery Job Run  : "
        val epilogue = "TSODev pour Orange Business"

        try {
            ArgParser(args, ArgParser.Mode.GNU, DefaultHelpFormatter(prologue, epilogue)).parseInto(::ParsedArgs).run {


                logger.info("===============================================================================")
                logger.info(" Discovery Run Job Launcher - TSO pour Orange Business - 08/23 - version 1.0.0 ")
                logger.info("===============================================================================")

                var listOfTargets = targets.split(',')

                val gson = Gson()
                var jsonParams = JsonObject()
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

                apiCallByCoroutines(
                    username,
                    password,
                    server,
                    company,
                    label,
                    listOfTargets,
                    scan_kind,
                    level,
                    jsonParams,
                    unsafe
                )

            }
        }
        catch (e: SystemExitException) {
            logger.error("Erreur d'argument dans la ligne de commande : ${e.message}")
            exitProcess(-4)
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
                        apiGetToken(server, username, password, unsafe).let { token: String? ->
                            if (token != null) {
                                TokenHolder.saveToken(token)
                                val runId =
                                    apiCreateRunJob(server, company, label, targets, scan_kind, level, params, unsafe)
                                logger.info("Run ID: $runId")
                                val info = apiWaitJobResults(server, runId!!, unsafe)
                                logger.info(
                                    "Resultats : \n | Success : ${info?.Success?.count}" +
                                            " | Skipped : ${info?.Skipped?.count}" +
                                            " | No Response : ${info?.NoResponse?.count}" +
                                            " | No Access : ${info?.NoAccess?.count}" +
                                            " | Error : ${info?.Error?.count}" +
                                            " | Dropped : ${info?.Dropped?.count} |"
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

