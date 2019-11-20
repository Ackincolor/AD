#!/usr/bin/python3.5

import logging

from configuration.configuration import Configuration
from messaging.videoconversionmessaging import VideoConversionMessaging
from database.mongodb.videoconversion import VideoConversion
from videoconvunixsocket.videoconversionunixsocket import VideoConversionUnixSocket


if __name__ == '__main__':

    logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)
    configuration = Configuration()

    #logging.info(configuration.get_rabbitmq_host())
    #logging.info(configuration.get_rabbitmq_port())
    #logging.info(configuration.get_messaging_conversion_queue())
    #logging.info(configuration.get_database_name())
    #logging.info(configuration.get_video_conversion_collection())


    video_unix_socket = VideoConversionUnixSocket()
    video_unix_socket.start()
    video_conversion_service = VideoConversion(configuration)
    video_messaging = VideoConversionMessaging(configuration, video_conversion_service)
    video_unix_socket.setVideoConversionMessaging(video_messaging)

