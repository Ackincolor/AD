
from threading import Thread
import socket
import os, os.path
import logging

class VideoConversionUnixSocket(Thread) :
    logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)


    def __init__(self):
        Thread.__init__(self)

        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # self.server.bind("/tmp/video_conv.socket")
        self.server.bind(("localhost", 42787))
        logging.info("pid=%s", os.getpid())
        self.pid = os.getpid()
        self.video_messaging = None

    def run(self):
        self.server.listen(1)
        while True :
            client, addr = self.server.accept()
            _consuming_request = client.recv(1024)
            consuming_request = _consuming_request.decode().strip('\n')
            logging.info("CONSUMING REQUEST = %s", consuming_request)
            if "_START_" == consuming_request :
                self.video_messaging.start_consuming()
                client.send("_START_OK_".encode())
                logging.info("Replying _START_OK_")
            else :
                if "_STOP_" == consuming_request :
                    self.video_messaging.stop_consuming()
                    client.send("_STOP_OK_".encode())
                    logging.info("Replying _STOP_OK_")
                else :
                    if "_STATUS_" == consuming_request :
                        client.send(self.video_messaging.is_consuming().encode())
                        logging.info("Replying %s", self.video_messaging.is_consuming() )


            # client.send(str(self.pid).encode())
            # client.send(str(self.pid).encode())
            client.close()

    def setVideoConversionMessaging(self, _video_messaging):
        logging.info("Setting messaging.")
        self.video_messaging = _video_messaging
