# Vote Akka Http API ---  PMR PLS 2019/2020 

L’API est documentée au format OpenApi 3 en Ligne Sur Swagger Hub : https://app.swaggerhub.com/apis/Souf/PMR-PROJECT-VOTE-API-AKKAHTTP/1.0.0
 et/ou dans le fichier [./openapi.yaml](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/openapi.yaml)

## Definition du Projet :
Ce projet coniste à définir un système qui  permet aux individuelles d'effectuer des votes sur plusieurs choix, 
après un certain délai , on affiche  les statistiques générales avec le nombre de participants.
L'API permet aussi de choisir des modes de Votes et un Captcha pour vérifier les utilisateurs ( Dans la partie Front End non encore
terminée )

## Logique Des acteurs 
https://imgur.com/a/znTfgZk




### Routes et requetes : 6 Requetes : 
###### 2 GET ( Get Vote stats, get all polls)  
###### 2 POST ( post Votes, poste polls) 
###### 1 PUT (put polls) 
###### 1 DELETE ( delete polls by Id )


#### 1.GET Polls / POST Polls
Les sondages peuvent être récupérés à partir de l'API en spécifiant un ID pour la ressource de sondages.
La structure de la valeur de retour de cet appel est la suivante

* id -- Id du Poll .
* title -- Le titre concernat le sujet du Poll - .
* choix -- une liste de Choix , appelé paprès la création du poll.
* dupcheck -- Comment gérer la vérification des votes en double. Valeurs acceptables: normal (par défaut), permissive et disabled.
* captcha -- vrai si le sondage oblige les utilisateurs à passer un CAPTCHA pour voter, faux (ou absent) sinon.

**Examples**

Retrieve data for poll ID 1


**Request**

https://localhost:9000/api//polls

Response
```
[
    {
        "captcha": false,
        "choix": [
            {
                "content": "This is an Option2",
                "id": 3,
                "idPoll": 2
            },
            {
                "content": "This is an Option2",
                "id": 4,
                "idPoll": 2
            }
        ],
        "dupcheck": "normal",
        "id": 2,
        "titles": "This is a test poll2."
    },
    {
        "captcha": false,
        "choix": [
            {
                "content": "This is an Option1",
                "id": 1,
                "idPoll": 1
            },
            {
                "content": "This is an Option2",
                "id": 2,
                "idPoll": 1
            }
        ],
        "dupcheck": "normal",
        "id": 1,
        "titles": "This is a test poll1."
    }
]
```
~~L'action POST contre la ressource polls créera un nouveau sondage.Ce taux est limité à 100 sondages créés par un utilisateur donné dans les 60 minutes~~







#### 2.GET Stats of Votes / POST Vote 
On poste le Vote par id du choix , puis on recupère la Liste Des CHoix du PollManager avec le Pattern Ask , et on effectue le calcul des Votes
et les stats dans le Vote Manager.


**Examples**


~~On spécifie aussi les dates de début et fin pour d'autres emploi de calcul des stats~~ 


* Post a Vote  data for Choix ID 1

**Request**

https://localhost:9000/api/vote/
```
{
	

                  "dateDebut": "10-11-2019",
                  "dateFin": "20-14-2015",
                  "idChoix": 2
}
```

**Reponse** 

* pour le poll de id numéro 1.

http://localhost:9000/api/vote/stats/1
```
{
    "nb_participants": 16,
    "votes": [
        {
            "id_choix": 1,
            "percentage": 56.25
        },
        {
            "id_choix": 2,
            "percentage": 43.75
        }
    ]
}
```

#### 3.PUT Poll / DELETE Poll

**Request**

* _Put Poll_



http://localhost:9000/api/poll/1

**Reponse**

```
{
   "titles":"This is a modified test poll1.",
   "choix":[{
      "content":      "This is a modified Option1"
},
{
      "content":      "This is a modified Option2"
}
   
],
   "dupcheck":"normal",
   "captcha":false
}

```

* _Delete Poll_

**Request**

http://localhost:9000/api/poll/1


**Reponse**

```

```

## TEST AVEC POSTMAN 

#### CAPTURES DE TEST :

###### GET ALL POLLS ######
![GET ALL](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/Get%20all%20Polls.jpg)
###### GET STATS BY ID ######
![GET STAT](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/Get%20Stats%20By%20Id%20Poll.jpg)
###### POST POLL ######
![POST POLL](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/Post%20Poll%20.jpg)
###### POST VOTE BY ID CHOIX ######
![POST VOTE](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/Post%20Vote%20.jpg)
###### PUT POLLS ######
![PUT POLLS](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/PUT%20POLL.jpg)
###### DELETE POLL BY ID ######
![DELETE](https://github.com/YOUSFISoufiane/Vote-Akka-Http/blob/master/img/Delete%20Poll.jpg)


















