FROM python:3.5-alpine
ADD . /source
WORKDIR /source
COPY ca.cert.pem ./
COPY application.yml ./application.yml
RUN mkdir -p /home/lois/pyWorker
COPY azure.yml /home/lois/pyWorker/azure.yml
COPY requierments.txt requierments.txt
RUN apk update && apk add libffi-dev openssl-dev
RUN apk add --no-cache build-base ffmpeg
RUN pip install -r requierments.txt
ENV GOOGLE_APPLICATION_CREDENTIALS=video-key.json
RUN ls -l 
CMD ["python","video-conversion-worker.py"]
