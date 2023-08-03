# DiscoveryRun

exemple de ligne de commande: (nécessite un JVM)

Il s'agit d'un programme en ligne de commande qui permet de créer et de lancer un job de scan

Usage: DiscoveryRun [\<options>]

Options:

-s, --server=\<text>       URL API du serveur Discovery , (https et termine
avec '/') généralement https://server/api/v1.1/

-u, --username=\<text>     Login - Nom de l'utilisateur

-p, --password=\<text>     Login - Mot de Passe de l'utilisateur

-x, --unsecure            do not verify SSL certificate checking process
(useful with self signed certificate)

-c, --company=\<text>      nom de la société cible

-l, --label=\<text>        Label du job

-t, --targets=\<text>      Liste des cibles (IPv4 ou IPv6) à scanner, séparées
par le caractère ','

-v, --level=(full|sweep)  Niveau du scan

-k, --kind=(IP|Cloud)     type de cibles pour le scan

-a, --params=\<text>       Parametres additionels (string format JSON)

--version                 Show the version and exit

-h, --help                Show this message and exit

