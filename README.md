# Présentation

Le framework-impersonator permet de réaliser des proxys permettant d'enregistrer les dialogues http et de répondre au requête avec des réponses précédemment enregistrées.
Les proxys créés sont basés sur Netty.

# Exemple d'implementation

## Création d'un recorder

```java
// port local du recorder
int localport = 8088;
// adresse de la cible du recorder
String targetHost = "integration.sidh.vsct.fr";
// port de la cible
int targetPort = 1181;
// Le serveur cible utilise t'il du SSL
boolean isSSLTarget = false;
 
// Dossier dans lequel seront enregistrées toutes les conversations
File storageDir = new File("/tmp/store");
 
// Implémentation de RequestIdentifier. Utilisé pour récupérer un identifiant unique à partir des requêtes 
RequestIdentifier requestIdentifier = new MD5RequestIdentifier();
 
// Transformer utilisé pour les requêtes. Si aucune transformation n'est utile, on utilise DefaultRequestTransformer qui ne fait rien.
RequestTransformer requestTransformer = new DefaultRequestTransformer();
// Transformer utilisé pour les réponses. Si aucune transformation n'est utile, on utilise DefaultResponseTransformer qui ne fait rien.
ResponseTransformer responseTransformer = new DefaultResponseTransformer();

HttpExchangeSerialiser exchangeSerialiser = new HttpExchangeSerialiser(requestIdentifier, requestTransformer, responseTransformer);

IHttpExchangeStore exchangeStore = new HttpExchangeStorage(storageDir, exchangeSerialiser);
 
// La création du recorder va directement le mettre en écoute sur le port paramétré.
Recorder.create(localport, targetHost, targetPort, exchangeStore, isSSLTarget);
```

Note : lorsque la cible à contacter utilise du SSL il faut que les certificats nécessaires soient présents dans le keystore donné au lancement de la JVM.

## Création d'un Impersonator

```java
// Implémentation de Bounds qui permet d'obtenir les bornes pour l'intervale de temps d'attente avant que l'Impersonator ne réponde à une requête. Le temps réél d'attente sera donc une valeur alléatoire entre Min et Max.
Bounds bounds = new Bounds() {
    @Override
    public int getMin() {
        return 5;
    }
 
    @Override
    public int getMax() {
        return 100;
    }
};
 
// port local de l'Impersonator
int localport = command.getLocalPort();
 
// Implémentation de RequestIdentifier. Utilisé pour récupérer un identifiant unique à partir des requêtes
RequestIdentifier requestIdentifier = new MD5RequestIdentifier();
 
// Transformer utilisé pour les requêtes. Si aucune transformation n'est utile, on utilise DefaultRequestTransformer qui ne fait rien.
RequestTransformer requestTransformer = new DefaultRequestTransformer();
// Transformer utilisé pour les réponses. Si aucune transformation n'est utile, on utilise DefaultResponseTransformer qui ne fait rien.
ResponseTransformer responseTransformer = new DefaultResponseTransformer();
 
HttpExchangeSerialiser exchangeSerialiser = new HttpExchangeSerialiser(requestIdentifier, requestTransformer, responseTransformer);
 
IHttpExchangeLoader loader = new HttpExchangeCacheLoader(storageDir, exchangeSerialiser);
 
Impersonator.create(localport, loader, bounds);
```
