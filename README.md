# **TP Conversion**

### But : 

​	Mise en place d'un SI permettant la conversion sur demande d'un fichier vidéo.

### Etapes :

1 ) Installation de tous les composant sur une même machine virtuelle.

2 ) Séparation des composants Front Back MongoDB et RabbitMQ.

3 ) Utilisation de ATLAS MongoDb à la place d'une base de données local.

4 ) Utilisation de AzureFile pour un stockage HA.

5 ) Utilisation de PUB/SUB à la place de RabbitMQ.

###### le plus dure est à venir

6 ) Mise en place d'un service Docker local pour le Front

​		6.A ) Utilisation de GKE pour le Front

7 ) Mise en place d'un service Docker local pour le Back

​		7.A ) Utilisation de GKE pour le Back

8 ) développement d'un client Lourd (Pour le moment)

​		8.A ) Appel de l'API pour lancer une conversion

​		9.B ) Récupération de avancement

​		9.C ) Récupération du nom du fichier.

### Images Docker:

Front

```yaml
FROM azul/zulu-openjdk-alpine:11
WORKDIR /source
COPY target/video-dispatcher-1.0-SNAPSHOT.jar /source/app.jar
COPY ssl/keystore.pkcs12 /source/ssl/keystore.pkcs12
COPY video-key.json /source/ArchiDistri.json
ENV GOOGLE_APPLICATION_CREDENTIALS=key.json
RUN chmod 777 -R /source
EXPOSE 42308
CMD ["/usr/bin/java","-jar","-Dspring.profiles.active=default","/source/app.jar"]
```

Back

```yml
FROM python:3.5-alpine
ADD . /source
WORKDIR /source
COPY ca.cert.pem ./
COPY application.yml ./application.yml
RUN mkdir -p /home/lois/pyWorker
COPY azure.yml /home/lois/pyWorker/azure.yml
RUN apk add --no-cache build-base ffmpeg libffi-dev openssl-dev
RUN pip install -r requierments.txt
ENV GOOGLE_APPLICATION_CREDENTIALS=video-key.json
CMD ["python","video-conversion-worker.py"]
```



### Deploiement Kubernetes

Nous avons utilisé les templates qui sont disponibles dans les actions de github.

### Ajouts Python

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
        # ligne inutile
        pass
```



