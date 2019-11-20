
import pika
from threading import Thread
import logging
from google.cloud import pubsub_v1
import time
import json
import queue

logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)
logging.getLogger("pika").setLevel(logging.INFO)

# rabbitmqadmin -H localhost -u ezip -p pize -V ezip purge queue name=video-conversion-queue
# rabbitmqadmin -H localhost -u ezip -p pize -V ezip get queue=video-conversion-queue

class VideoConversionMessaging(Thread):
    def __init__(self,_config_,converting_service):
        Thread.__init__(self)
        self.subscriber = pubsub_v1.SubscriberClient()
        self.subsciption_path = self.subscriber.subscription_path(_config_.get_project_id(),_config_.get_subscription_name())

        def callback(message):
            print('Received message: {}'.format(message))
            message.ack()

        self.subscriber.subscribe(self.subsciption_path, callback=callback)

        print('listening fo message on {}'.format(self.subsciption_path))
        while True:
            time.sleep(60)
#     def __init__(self, _config_, converting_service):
#         Thread.__init__(self)
#         self.credentials = pika.credentials.PlainCredentials(
#                                 _config_.get_rabbitmq_username(),
#                                 _config_.get_rabbitmq_password())
#         self.connection = pika.BlockingConnection(
#                             pika.ConnectionParameters(
#                                 _config_.get_rabbitmq_host(),
#                                 _config_.get_rabbitmq_port(),
#                                 _config_.get_rabbitmq_vhost(),
#                                 self.credentials))
#         self.channel = self.connection.channel()
#         self.rmq = _config_.get_messaging_conversion_queue()
#         # self.channel.basic_consume(self.on_message, self.rmq, no_ack=True)
#         self.converting_service = converting_service
#         self.consuming = "_CONSUMING_"
#         self.rendez_vous = queue.Queue(1)
#         self.pause = queue.Queue(1)
#         self.start()
#
#     def run(self):
#         while True : # "_CONSUMING_" == self.consuming :
# #            logging.info("Starts consuming on message bus before RDV")
# #            self.channel.start_consuming()
# #            logging.info("WAITING Rendez-Vous")
# #            self.rendez_vous.get()
# #            logging.info("Rendez-Vous")
# #            self.channel = self.connection.channel()
# #            self.channel.basic_consume(self.on_message, self.rmq, no_ack=True)
#             if "_CONSUMING_" == self.consuming :
#                 method, prop, body = self.channel.basic_get(self.rmq, auto_ack=True)
#                 if body :
#                     self._on_message_(body)
#                     pass
#                 else :
#                     try :
#                         self.pause.get(timeout=1)
#                     except queue.Empty :
#                         pass
#                     # self._on_message_(message)
#
#
#     def on_message(self, channel, method_frame, header_frame, body):
#         logging.info(body)
#         # logging.info('id = %s, URI = %s', body["id"], body['originPath'])
#         # logging.info('URI = %s', body['originPath'])
#         logging.info('URI = %s', body.decode())
#         convert_request = json.loads(body.decode())
#         logging.info(convert_request)
#         self.converting_service.convert(convert_request["id"], convert_request['originPath'])
#
#     def _on_message_(self,  body):
#         logging.info(body)
#         # logging.info('id = %s, URI = %s', body["id"], body['originPath'])
#         # logging.info('URI = %s', body['originPath'])
#         logging.info('URI = %s', body.decode())
#         convert_request = json.loads(body.decode())
#         logging.info(convert_request)
#         self.converting_service.convert(convert_request["id"], convert_request['originPath'])
#
#
#
#     def stop_consuming(self):
#         logging.info("Stops consuming on message bus")
#         # self.channel.stop_consuming()
#         self.consuming = "_IDLE_"
#
#     def start_consuming(self):
#         logging.info("Starts consuming on message bus")
#         #self.channel.start_consuming()
#         # self.rendez_vous.put("_CONSUMING_")
#         self.consuming = "_CONSUMING_"
#         # self.start()
#
#     def is_consuming(self):
#         return self.consuming
