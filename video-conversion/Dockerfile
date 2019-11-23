FROM python:3.5-alpine
COPY ca.cert.pem ./
COPY application.yml ./application.ymll
COPY /home/lois/pyWorker/azure.yml /home/lois/pyWorker/azure.yml
COPY ./requirements.txt ./
RUN apk add --no-cache build-base ffmpeg
RUN pip install -r requirements.txt
ADD . .
CMD ["python","video-conversion-worker.py"]