# **TP Conversion**

### But : 

​	Mise en place d'un SI permettant la conversion sur demande d'un fichier vidéo.

### Etapes :

1 ) Installation des tous les composant sur une même machine virtuelle.

2 ) Séparation des composant Front Back MongoDB et RabbitMQ

3 ) Utilisation de ATLAS MongoDb a la place d'une base local.

4 ) Utilisation de AzureFile pour un stockage HA

5 ) Utilisation de PUB/SUB a la place de RabbitMQ

###### le plus dure est à venir

6 ) Mise en place d'un service Docker local pour le Front

​		6.A ) Utilisation de GKE pour le Front

7 ) Mise en place d'un service Docker local pour le Back

​		7.A ) Utilisation de GKE pour le Back

8 ) développement d'un client Lourd (Pour le moment)

​		8.A ) Appel de l'API pour lancer une conversion

​		9.B ) Récupération de avancement

​		9.C ) Récupération du nom du fichier.

### Difficultés :

Récupération de l'avancement de la conversion (Python) :

```python
while (not re.compile('^Press').match(line)):
    i = i + 1
    line = thread.readline().strip().decode('utf-8')
    if (re.compile('^Duration').match(line)):
        duration_total = self.timecode_value(line.split(',')[0].split(' ')[1])
        #recuperation de la durée total de la vidéo

cpl = thread.compile_pattern_list([
    pexpect.EOF,
    "^(frame=.*)",
    '(.+)'
])
while True:
    i = thread.expect_list(cpl, timeout=None)
    if i == 0:  # EOF
        #....
        #conversion terminée
        break
    elif i == 1:
        try:
            #....
            # calcul(current_time / duration_total * 100)
            print("Avancement : %.2f" % percentage)
       
    elif i == 2:
        # ....
        # ligne non reconnu
        pass
```



